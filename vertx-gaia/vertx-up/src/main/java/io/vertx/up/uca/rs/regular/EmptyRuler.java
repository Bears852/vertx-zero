package io.vertx.up.uca.rs.regular;

import io.vertx.up.atom.Rule;
import io.vertx.up.exception.WebException;

import java.util.Collection;

class EmptyRuler extends BaseRuler {
    @Override
    public WebException verify(final String field,
                               final Object value,
                               final Rule rule) {
        WebException error = null;
        if (null != value && value instanceof Collection) {
            final Collection reference = (Collection) value;
            if (reference.isEmpty()) {
                error = failure(field, value, rule);
            }
        }
        return error;
    }
}
