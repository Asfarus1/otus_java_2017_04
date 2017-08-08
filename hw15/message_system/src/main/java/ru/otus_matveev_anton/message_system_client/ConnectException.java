package ru.otus_matveev_anton.message_system_client;

/**
 * Created by asfarus on 08.08.2017.
 */
public class ConnectException extends RuntimeException {
    public ConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
