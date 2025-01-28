package main.java.com.checkers.util;

public final class GameResponseUtil {

    private GameResponseUtil() {}

    /**
     * Generates a GameResponse object.
     *
     * If the passed-in data is a String, we interpret it as the "message."
     * In that case, we return a GameResponse<Void> (no 'data' field).
     *
     * Otherwise, we embed the object (data) in the GameResponse.
     * That becomes a GameResponse<T> with some default or provided message.
     */
    public static <T> GameResponse<?> generateResponse(boolean success, T data) {
        if (data instanceof String) {
            // If data is a String, treat it as the "message" only (no data).
            return new GameResponse<Void>(success, (String) data);
        } else {
            // Otherwise, treat 'data' as the response payload.
            // You can customize the message here as needed.
            return new GameResponse<T>(success, "Some default message", data);
        }
    }

    /**
     * Overloaded method: If you *always* want to pass in a separate message and data,
     * you can use this version. Then you can skip the instance-of check.
     */
    public static <T> GameResponse<T> generateResponse(boolean success, String message, T data) {
        return new GameResponse<>(success, message, data);
    }

    /**
     * Overloaded method for a message-only response (no data).
     */
    public static GameResponse<Void> generateResponse(boolean success, String message) {
        return new GameResponse<>(success, message);
    }
}