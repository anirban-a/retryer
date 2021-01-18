package retry;

@FunctionalInterface
public interface Retryable {
    ActionResponse perform();
}
