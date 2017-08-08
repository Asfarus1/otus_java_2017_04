package ru.otus_matveev_anton;

public class MessageFormatException extends RuntimeException {
    public MessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
