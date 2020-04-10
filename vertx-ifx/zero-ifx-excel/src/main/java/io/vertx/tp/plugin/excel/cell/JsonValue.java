package io.vertx.tp.plugin.excel.cell;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.up.eon.Strings;
import io.vertx.up.util.Ut;

/*
 * Fix issue of excel length: 32767 characters
 */
@SuppressWarnings("all")
public class JsonValue implements ExValue {
    @Override
    public String to(final Object value) {
        final String[] pathArr = value.toString().split(Strings.COLON);
        String literal = value.toString();
        if (2 == pathArr.length) {
            final String path = pathArr[1];
            if (Ut.notNil(path)) {
                final String content = Ut.ioString(path.trim());
                if (Ut.notNil(content)) {
                    if (Ut.isJArray(content)) {
                        final JsonArray normalized = Ut.toJArray(content);
                        literal = normalized.encodePrettily();
                    } else if (Ut.isJObject(content)) {
                        final JsonObject normalized = Ut.toJObject(content);
                        literal = normalized.encodePrettily();
                    }
                }
            }
        }
        return literal;
    }
}
