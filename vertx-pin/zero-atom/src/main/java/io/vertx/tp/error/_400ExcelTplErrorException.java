package io.vertx.tp.error;

import io.vertx.up.exception.WebException;

public class _400ExcelTplErrorException extends WebException {

    public _400ExcelTplErrorException(final Class<?> clazz) {
        super(clazz);
    }

    @Override
    public int getCode() {
        return -80512;
    }
}
