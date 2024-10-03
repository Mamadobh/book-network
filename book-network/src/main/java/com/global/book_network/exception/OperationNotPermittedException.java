package com.global.book_network.exception;

public class OperationNotPermittedException extends RuntimeException {
    public OperationNotPermittedException() {

    }

    public OperationNotPermittedException(String msg) {
        super(msg);
    }
}
