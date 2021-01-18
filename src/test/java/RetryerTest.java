import org.junit.Assert;
import org.junit.Test;
import retry.ActionResponse;
import retry.ConfigurationBundle;
import retry.Retry;
import retry.Retryable;

import java.util.Random;

public class RetryerTest {
    private static final Integer THRESHOLD = 3;
    private static Integer counter = 0;

    @Test
    public void simpleRetryTest() {
        ConfigurationBundle configurationBundle = new ConfigurationBundle();
        configurationBundle.setId("mock-task").setRetryCount(3).setWaitDuration(1000L);
        Retryable retryable = () -> {
            ActionResponse<Integer> response = new ActionResponse<>();
            Integer result = mockNumberGeneratorService();
            response.setResult(result);
            response.setSuccess(result != -1);
            return response;
        };
        ActionResponse<Integer> actionResponse =
                Retry.getInstance().retryAndGetActionResponse(retryable, configurationBundle);

        Assert.assertTrue(actionResponse.isSuccess());
        Assert.assertFalse(actionResponse.getResult() == -1);
    }

    private Integer mockNumberGeneratorService() {
        int result;
        if (counter < THRESHOLD) { result = -1; } else {
            result = new Random().nextInt(100);
        }
        counter++;
        return result;
    }

    @Test
    public void retryTestWithExceptions_Success() {
        counter = 0;
        ConfigurationBundle configurationBundle = new ConfigurationBundle();
        configurationBundle.setId("mock-task").setRetryCount(3).setWaitDuration(2000L);
        Retryable retryable = () -> {
            ActionResponse<Integer> response = new ActionResponse<>();
            Integer result = -1;
            try {
                result = mockNumberGeneratorServiceWithExceptions();
                response.setSuccess(true);
            } catch (RuntimeException e) {
                response.setSuccess(false);
            }
            response.setResult(result);
            return response;
        };

        ActionResponse<Integer> actionResponse =
                Retry.getInstance().retryAndGetActionResponse(retryable, configurationBundle);

        Assert.assertTrue(actionResponse.isSuccess());
        Assert.assertFalse(actionResponse.getResult() == -1);
    }

    private Integer mockNumberGeneratorServiceWithExceptions() throws RuntimeException {
        int result = mockNumberGeneratorService();
        if (result == -1) { throw new RuntimeException("Could not generate a number"); }
        return result;
    }

    @Test
    public void retryAndFail() {
        counter = 0;
        ConfigurationBundle configurationBundle = new ConfigurationBundle();
        configurationBundle.setId("mock-task").setRetryCount(THRESHOLD - 1).setWaitDuration(2000L);
        Retryable retryable = () -> {
            ActionResponse<Integer> response = new ActionResponse<>();
            Integer result = -1;
            try {
                result = mockNumberGeneratorServiceWithExceptions();
                response.setSuccess(true);
            } catch (RuntimeException e) {
                response.setSuccess(false);
            }
            response.setResult(result);
            return response;
        };

        ActionResponse<Integer> actionResponse =
                Retry.getInstance().retryAndGetActionResponse(retryable, configurationBundle);

        Assert.assertFalse(actionResponse.isSuccess());
        Assert.assertTrue(actionResponse.getResult() == -1);
    }
}
