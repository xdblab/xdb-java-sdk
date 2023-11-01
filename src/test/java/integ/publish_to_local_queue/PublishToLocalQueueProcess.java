package integ.publish_to_local_queue;

import static integ.publish_to_local_queue.TestPublishToLocalQueueProcess.DEDUP_ID;
import static integ.publish_to_local_queue.TestPublishToLocalQueueProcess.PAYLOAD_1;
import static integ.publish_to_local_queue.TestPublishToLocalQueueProcess.PAYLOAD_1_2;
import static integ.publish_to_local_queue.TestPublishToLocalQueueProcess.PAYLOAD_2;
import static integ.publish_to_local_queue.TestPublishToLocalQueueProcess.QUEUE_1;
import static integ.publish_to_local_queue.TestPublishToLocalQueueProcess.QUEUE_2;
import static integ.publish_to_local_queue.TestPublishToLocalQueueProcess.QUEUE_3;

import io.xdb.core.command.CommandRequest;
import io.xdb.core.command.LocalQueueCommand;
import io.xdb.core.communication.Communication;
import io.xdb.core.encoder.JacksonJsonObjectEncoder;
import io.xdb.core.persistence.Persistence;
import io.xdb.core.process.Process;
import io.xdb.core.state.AsyncState;
import io.xdb.core.state.StateDecision;
import io.xdb.core.state.StateSchema;
import io.xdb.gen.models.CommandResults;
import io.xdb.gen.models.CommandStatus;
import io.xdb.gen.models.Context;
import io.xdb.gen.models.LocalQueueResult;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Component;

@Component
public class PublishToLocalQueueProcess implements Process {

    @Override
    public StateSchema getStateSchema() {
        return StateSchema.withStartingState(new PublishToLocalQueueStartingState(), new PublishToLocalQueueState1());
    }
}

class PublishToLocalQueueStartingState implements AsyncState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(final Context context, final Void input, final Communication communication) {
        System.out.println("PublishToLocalQueueStartingState.waitUntil: " + input);

        // will be consumed by PublishToLocalQueueState1
        communication.publishToLocalQueue(QUEUE_2, PAYLOAD_2);

        return CommandRequest.anyCommandComplete(new LocalQueueCommand(QUEUE_1), new LocalQueueCommand(QUEUE_3));
    }

    @Override
    public StateDecision execute(
        final Context context,
        final Void input,
        final CommandResults commandResults,
        final Persistence persistence,
        final Communication communication
    ) {
        System.out.println("PublishToLocalQueueStartingState.execute: " + input);

        // will be consumed by PublishToLocalQueueState1
        communication.publishToLocalQueue(QUEUE_1, DEDUP_ID, PAYLOAD_1);

        final List<LocalQueueResult> localQueueResults = commandResults.getLocalQueueResults();

        Assertions.assertEquals(2, localQueueResults.size());

        Assertions.assertEquals(QUEUE_1, localQueueResults.get(0).getQueueName());
        Assertions.assertEquals(CommandStatus.COMPLETED_COMMAND, localQueueResults.get(0).getStatus());
        Assertions.assertEquals(1, localQueueResults.get(0).getMessages().size());
        Assertions.assertEquals(
            PAYLOAD_1_2,
            new JacksonJsonObjectEncoder()
                .decode(localQueueResults.get(0).getMessages().get(0).getPayload(), String.class)
        );

        Assertions.assertEquals(QUEUE_3, localQueueResults.get(1).getQueueName());
        Assertions.assertEquals(CommandStatus.WAITING_COMMAND, localQueueResults.get(1).getStatus());
        Assertions.assertNull(localQueueResults.get(1).getMessages());

        return StateDecision.singleNextState(PublishToLocalQueueState1.class, null);
    }
}

class PublishToLocalQueueState1 implements AsyncState<Void> {

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(final Context context, final Void input, final Communication communication) {
        System.out.println("PublishToLocalQueueState1.waitUntil: " + input);

        // will be consumed by itself
        communication.publishToLocalQueue(QUEUE_2);

        return CommandRequest.allCommandsComplete(new LocalQueueCommand(QUEUE_1, 2), new LocalQueueCommand(QUEUE_2, 2));
    }

    @Override
    public StateDecision execute(
        final Context context,
        final Void input,
        final CommandResults commandResults,
        final Persistence persistence,
        final Communication communication
    ) {
        System.out.println("PublishToLocalQueueState1.execute: " + input);

        final List<LocalQueueResult> localQueueResults = commandResults.getLocalQueueResults();
        Assertions.assertEquals(2, localQueueResults.size());

        Assertions.assertEquals(QUEUE_1, localQueueResults.get(0).getQueueName());
        Assertions.assertEquals(CommandStatus.COMPLETED_COMMAND, localQueueResults.get(0).getStatus());
        Assertions.assertEquals(2, localQueueResults.get(0).getMessages().size());
        Assertions.assertEquals(
            PAYLOAD_1,
            new JacksonJsonObjectEncoder()
                .decode(localQueueResults.get(0).getMessages().get(0).getPayload(), String.class)
        );
        Assertions.assertNull(
            new JacksonJsonObjectEncoder()
                .decode(localQueueResults.get(0).getMessages().get(1).getPayload(), String.class)
        );

        Assertions.assertEquals(QUEUE_2, localQueueResults.get(1).getQueueName());
        Assertions.assertEquals(CommandStatus.COMPLETED_COMMAND, localQueueResults.get(1).getStatus());
        Assertions.assertEquals(2, localQueueResults.get(1).getMessages().size());
        Assertions.assertEquals(
            PAYLOAD_2,
            new JacksonJsonObjectEncoder()
                .decode(localQueueResults.get(1).getMessages().get(0).getPayload(), String.class)
        );
        Assertions.assertNull(
            new JacksonJsonObjectEncoder()
                .decode(localQueueResults.get(1).getMessages().get(1).getPayload(), String.class)
        );

        return StateDecision.gracefulCompleteProcess();
    }
}
