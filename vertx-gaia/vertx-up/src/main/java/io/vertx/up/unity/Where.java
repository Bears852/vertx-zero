package io.vertx.up.unity;

import io.vertx.core.json.JsonObject;
import io.vertx.up.eon.Strings;
import io.vertx.up.util.Ut;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

class Where {
    /*
     * 1 - instant is "YYYY-MM-DD HH:mm:ss"
     * 2 - Because here should convert to "YYYY-MM-DD" instead of equal here
     * 3 - generate between `>` and `<` into filters
     */
    static JsonObject whereDay(final JsonObject filters, final String field,
                               final Instant instant) {
        /*
         * field / instant
         */
        if (Ut.notNil(field) && Objects.nonNull(instant)) {
            final LocalDateTime current = Ut.toDateTime(instant);
            /* Get today date */
            final LocalDateTime begin = LocalDateTime.of(current.toLocalDate(), LocalTime.MIN);
            final LocalDateTime end = LocalDateTime.of(current.toLocalDate(), LocalTime.MAX);
            final JsonObject condition = new JsonObject();
            condition.put(field + ",<", Ut.parse(end).toInstant());
            condition.put(field + ",>", Ut.parse(begin).toInstant());
            condition.put(Strings.EMPTY, Boolean.TRUE);
            filters.put("$" + field, condition);
        }
        return filters;
    }
}
