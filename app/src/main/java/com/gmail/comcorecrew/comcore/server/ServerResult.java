package com.gmail.comcorecrew.comcore.server;

import com.gmail.comcorecrew.comcore.server.connection.Function;

/**
 * Represents the result of a request to the server which may either be success or failure.
 *
 * @param <T> the type of the expected result of the request
 */
public final class ServerResult<T> {
    /**
     * The data returned by the request, if it was successful.
     */
    public final T data;

    /**
     * The error message returned by the request, if it failed.
     */
    public final String errorMessage;

    /**
     * Construct a ServerResult with data or an error.
     *
     * @param data the value if successful
     * @param errorMessage the error message if failed
     */
    private ServerResult(T data, String errorMessage) {
        this.data = data;
        this.errorMessage = errorMessage;
    }

    /**
     * Construct a successful ServerResult from a value.
     *
     * @param data the result of the request
     * @param <T>  the type of the result
     * @return a ServerResult containing the value
     */
    public static <T> ServerResult<T> success(T data) {
        return new ServerResult<>(data, null);
    }

    /**
     * Construct a failed ServerResult from an error message.
     *
     * @param error the error message
     * @param <T>   the type of the expected result
     * @return a ServerResult containing the error message
     */
    public static <T> ServerResult<T> failure(String error) {
        return new ServerResult<>(null, error);
    }

    /**
     * Construct a failed ServerResult with an invalid response error message.
     *
     * @param <T> the type of the expected result
     * @return a ServerResult containing the error message
     */
    public static <T> ServerResult<T> invalidResponse() {
        return ServerResult.failure("invalid server response");
    }

    /**
     * Construct a failed ServerResult with an disconnected from server error message.
     *
     * @param <T> the type of the expected result
     * @return a ServerResult containing the error message
     */
    public static <T> ServerResult<T> disconnected() {
        return ServerResult.failure("disconnected from server");
    }

    /**
     * Check if the request was successful.
     *
     * @return true if the request was successful, false otherwise
     */
    public boolean isSuccess() {
        return errorMessage == null;
    }

    /**
     * Check if the request failed.
     *
     * @return true if the request failed, false otherwise
     */
    public boolean isFailure() {
        return errorMessage != null;
    }

    /**
     * Extract the result of the request, or return a default value if it failed.
     *
     * @param defaultValue the value to return if the request failed
     * @return the result if successful, otherwise defaultValue
     */
    public T or(T defaultValue) {
        if (isSuccess()) {
            return data;
        } else {
            return defaultValue;
        }
    }

    /**
     * Transform the data stored inside the ServerResult, allowing new errors to be added.
     *
     * @param function the transformation to apply to the data
     * @param <U> the new type after the transformation
     * @return a ServerResult containing the transformed data or an error message
     */
    public <U> ServerResult<U> then(Function<T, ServerResult<U>> function) {
        if (isSuccess()) {
            return function.apply(data);
        } else {
            return ServerResult.failure(errorMessage);
        }
    }

    /**
     * Transform the data stored inside the ServerResult if it was successful.
     *
     * @param function the transformation to apply to the data
     * @param <U> the new type after the transformation
     * @return a ServerResult containing the transformed data or an unmodified error message
     */
    public <U> ServerResult<U> map(Function<T, U> function) {
        return then(value -> ServerResult.success(function.apply(value)));
    }

    /**
     * Transform the data stored inside the ServerResult if it was successful. If a
     * RuntimeException occurs, return invalidResponse.
     *
     * @param function the transformation to apply to the data
     * @param <U> the new type after the transformation
     * @return a ServerResult containing the transformed data or an error message
     */
    public <U> ServerResult<U> tryMap(Function<T, U> function) {
        return then(value -> {
            try {
                return ServerResult.success(function.apply(value));
            } catch (RuntimeException e) {
                e.printStackTrace();
                return ServerResult.invalidResponse();
            }
        });
    }

    @Override
    public String toString() {
        if (isSuccess()) {
            return "success(" + data + ")";
        } else {
            return "failure(" + errorMessage + ")";
        }
    }
}