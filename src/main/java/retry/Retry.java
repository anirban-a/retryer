package retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public final class Retry {
    private static final Retry INSTANCE = new Retry();
    private static final Logger log = LoggerFactory.getLogger(Retry.class);
    private final Long DEFAULT_WAIT_TIME_MILLIS = 1_000L;
    private final Integer DEFAULT_RETRY_COUNT = 3;

    private Retry() {
        // This class will be eagerly instantiated and maintain only single instance.
    }

    public static Retry getInstance() {
        return INSTANCE;
    }

    /**
     * @param retryable
     * @param configurationBundle
     * @return Action that will contain the result of the provided task.
     * @implNote This method will be blocking in nature.
     */
    public ActionResponse retryAndGetActionResponse(Retryable retryable, ConfigurationBundle configurationBundle) {
        Action action = retry(retryable, configurationBundle);
        try {
            action.join();
        } catch (InterruptedException e) {
            log.error("Thread Interrupted!");
        }
        return action.getExecutionResponse();
    }

    /**
     * @param retryable
     * @param configurationBundle
     * @return Action that will contain the result of the provided task.
     * @implNote This method will be non-blocking.
     */
    public Action retry(Retryable retryable, ConfigurationBundle configurationBundle) {
        Action action = new Action();
        action.addJob(retryable);
        action.setActionID(configurationBundle.getId());
        action.setWaitDuration(configurationBundle.getWaitDuration());
        action.setRetryCount(configurationBundle.getRetryCount());
        action.start();
        return action;
    }

    public final class Action extends Thread {
        private Retryable retryable;
        private Status status;
        private Long waitDuration = null;
        private Integer retryCount = null;
        private ActionResponse actionResponse;
        private String actionID;

        private void setWaitDuration(Long waitDuration) {
            this.waitDuration = waitDuration;
        }

        private void setRetryCount(Integer retryCount) {
            this.retryCount = retryCount;
        }

        public String getActionID() {
            return actionID;
        }

        private void setActionID(String actionID) {
            this.actionID = actionID;
        }

        private void addJob(Retryable retryable) {
            this.retryable = retryable;
            status = Status.PENDING;
        }

        @Override
        public void run() {
            if (waitDuration == null) { waitDuration = DEFAULT_WAIT_TIME_MILLIS; }
            if (retryCount == null) { retryCount = DEFAULT_RETRY_COUNT; }
            log.info("Performing task: " + actionID);
            actionResponse = retryable.perform();
            while (!actionResponse.isSuccess() && retryCount > 0) {
                log.info("Retrying.. Number of retries left = " + retryCount);
                try {
                    TimeUnit.MILLISECONDS.sleep(waitDuration);
                } catch (InterruptedException e) {
                    log.error("Thread sleep interrupted while waiting for retry.");
                }
                actionResponse = retryable.perform();
                retryCount--;
            }
            if (actionResponse.isSuccess()) {
                status = Status.SUCCESS;
            } else { status = Status.FAILED; }
            log.info("Task status: " + status);
        }

        public ActionResponse getExecutionResponse() {
            return actionResponse;
        }

        public Status getStatus() {
            return status;
        }
    }
}
