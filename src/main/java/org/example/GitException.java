package org.example;

public class GitException extends RuntimeException {
    ErrorCode errorCode;

    public GitException(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public GitException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
