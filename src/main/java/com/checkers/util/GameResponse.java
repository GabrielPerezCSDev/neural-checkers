package main.java.com.checkers.util;

/**
 * A simple data class representing a game response.
 */
public class GameResponse<T> {
    private final boolean success;
    private final String message;
    private final T data;

    // Full constructor
    public GameResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Convenience constructor (no data)
    public GameResponse(boolean success, String message) {
        this(success, message, null);
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        if (data == null) {
            return "GameResponse{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    '}';
        } else {
            return "GameResponse{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", data=" + data +
                    '}';
        }
    }
}