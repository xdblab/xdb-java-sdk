package io.xdb.core.state;

import com.google.common.base.Strings;
import io.xdb.core.exception.ProcessDefinitionException;
import io.xdb.core.persistence.to_load.PersistenceSchemaToLoad;
import io.xdb.core.utils.ProcessUtil;
import io.xdb.gen.models.RetryPolicy;
import io.xdb.gen.models.StateFailureRecoveryOptions;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AsyncStateOptions {

    /**
     * Either stateClass or id must be set
     */
    private final Class<? extends AsyncState> stateClass;
    /**
     * Either stateClass or id must be set
     */
    private final String id;

    private int waitUntilApiTimeoutSeconds;
    private int executeApiTimeoutSeconds;
    private RetryPolicy waitUntilApiRetryPolicy;
    private RetryPolicy executeApiRetryPolicy;
    private StateFailureRecoveryOptions stateFailureRecoveryOptions;
    private PersistenceSchemaToLoad persistenceSchemaToLoad;

    public static AsyncStateOptionsBuilder builder(final Class<? extends AsyncState> stateClass) {
        return builder().stateClass(stateClass);
    }

    private static AsyncStateOptionsBuilder builder() {
        return new AsyncStateOptionsBuilder();
    }

    public String getId() {
        return Strings.isNullOrEmpty(id) ? ProcessUtil.getClassSimpleName(stateClass) : id;
    }

    public void validate() {
        if (stateClass == null && Strings.isNullOrEmpty(id)) {
            throw new ProcessDefinitionException("AsyncStateOptions: either stateClass or id must be set.");
        }
    }
}
