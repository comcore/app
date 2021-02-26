package com.gmail.comcorecrew.comcore.server;

/**
 * Represents the result of a request to the server which may either be success or failure.
 *
 * @param <T> the type of the expected result of the request
 */
public final class ServerResult<T> {
    /**
     * The value returned by the request, if it was successful.
     */
    public final T value;

    /**
     * The error message returned by the request, if it failed
     */
    public final String error;

    /**
     * Construct a ServerResult with a value and an error.
     *
     * @param value the value if successful
     * @param error the error message if failed
     */
    private ServerResult(T value, String error) {
        this.value = value;
        this.error = error;
    }

    /**
     * Construct a successful ServerResult from a value.
     *
     * @param value the result of the request
     * @param <T>   the type of the result
     * @return a ServerResult containing the value
     */
    public static<T> ServerResult<T> success(T value) {
        return new ServerResult<>(value, null);
    }

    /**
     * Construct a failed ServerResult from an error message.
     *
     * @param error the error message
     * @param <T>   the type of the expected result
     * @return a ServerResult containing the error message
     */
    public static<T> ServerResult<T> failure(String error) {
        return new ServerResult<>(null, error);
    }

    static<T> ServerResult<T> invalidResponse() {
        return ServerResult.failure("invalid server response");
    }

    /**
     * Check if the request was successful.
     *
     * @return true if the request was successful, false otherwise
     */
    public boolean isSuccess() {
        return error == null;
    }

    /**
     * Extract the result of the request, or return a default value if it failed.
     *
     * @param defaultValue the value to return if the request failed
     * @return the result if successful, otherwise defaultValue
     */
    public T or(T defaultValue) {
        if (isSuccess()) {
            return value;
        } else {
            return defaultValue;
        }
    }

    /**
     * Represents a transformation which can be applied to the result of a request.
     *
     * @param <T> the original type
     * @param <U> the new type after the transformation
     */
    public interface Function<T, U> {
        /**
         * Apply a transformation to the result of a request.
         *
         * @param result the result of the request
         * @return the new value to replace it with
         */
        U apply(T result);
    }

    /**
     * Transform the value stored inside the ServerResult, allowing new errors to be added.
     *
     * @param function the transformation to apply to the value
     * @param <U> the new type after the transformation
     * @return a ServerResult containing a transformed value or an error message
     */
    public<U> ServerResult<U> then(Function<T, ServerResult<U>> function) {
        if (isSuccess()) {
            return function.apply(value);
        } else {
            return ServerResult.failure(error);
        }
    }

    /**
     * Transform the value stored inside the ServerResult if it was successful.
     *
     * @param function the transformation to apply to the value
     * @param <U> the new type after the transformation
     * @return a ServerResult containing a transformed value or an unmodified error message
     */
    public<U> ServerResult<U> map(Function<T, U> function) {
        return then(value -> ServerResult.success(function.apply(value)));
    }

    /**
     * Transform the value stored inside the ServerResult if it was successful. If a
     * RuntimeException occurs, return invalidResponse.
     *
     * @param function the transformation to apply to the value
     * @param <U> the new type after the transformation
     * @return a ServerResult containing a transformed value or an error message
     */
    public<U> ServerResult<U> tryMap(Function<T, U> function) {
        return then(value -> {
            try {
                return ServerResult.success(function.apply(value));
            } catch (RuntimeException e) {
                return ServerResult.invalidResponse();
            }
        });
    }

    @Override
    public String toString() {
        if (isSuccess()) {
            return "success(" + value + ")";
        } else {
            return "failure(" + error + ")";
        }
    }
}