package io.vertx.tp.error;

import io.vertx.core.http.HttpStatusCode;
import io.vertx.up.exception.WebException;

public class _500ConsumerSpecException extends WebException {
    public _500ConsumerSpecException(final Class<?> clazz, final Class<?> target) {
        super(clazz, target.getName());
    }

    @Override
    public int getCode() {
        return -80405;
    }

    @Override
    public HttpStatusCode getStatus() {
        return HttpStatusCode.INTERNAL_SERVER_ERROR;
    }
}
