package com.sicknet.hedera.relations.ants.exception;

public class NoAnyAvailableRelationsException extends Exception {
    public NoAnyAvailableRelationsException() {
        super("No available relations found for the account");
    }

    public NoAnyAvailableRelationsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAnyAvailableRelationsException(Throwable cause) {
        super(cause);
    }
}
