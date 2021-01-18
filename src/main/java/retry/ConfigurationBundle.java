package retry;

public class ConfigurationBundle {
    private String id;
    private Long waitDuration;
    private Integer retryCount;

    public String getId() {
        return id;
    }

    /**
     * @param id Task ID - to identify the task in logs
     * @return ConfigurationBundle
     * @apiNote (Optional) id will be used to denote and log about the task being performed.
     */
    public ConfigurationBundle setId(String id) {
        this.id = id;
        return this;
    }

    public Long getWaitDuration() {
        return waitDuration;
    }

    /**
     * @param waitDuration in milli-seconds
     * @return ConfigurationBundle
     */
    public ConfigurationBundle setWaitDuration(Long waitDuration) {
        this.waitDuration = waitDuration;
        return this;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public ConfigurationBundle setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
        return this;
    }
}
