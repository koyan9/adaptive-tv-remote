package io.github.koyan9.tvremote.integration;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.TimeoutException;

final class IntegrationErrors {

    private IntegrationErrors() {
    }

    static boolean isTimeout(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        if (throwable instanceof HttpTimeoutException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof TimeoutException) {
            return true;
        }
        return isTimeout(throwable.getCause());
    }
}
