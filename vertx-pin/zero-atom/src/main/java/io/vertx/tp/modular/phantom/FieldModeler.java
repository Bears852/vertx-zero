package io.vertx.tp.modular.phantom;

import cn.vertxup.atom.domain.tables.daos.MFieldDao;
import cn.vertxup.atom.domain.tables.pojos.MField;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.tp.ke.cv.KeField;
import io.vertx.up.log.Annal;
import io.vertx.up.unity.Ux;

import java.util.List;
import java.util.function.Function;

class FieldModeler implements AoModeler {

    private static final Annal LOGGER = Annal.get(FieldModeler.class);

    @Override
    public Function<JsonObject, Future<JsonObject>> apply() {
        return schemaJson -> {
            LOGGER.debug("[ Ox ] 6.1. AoModeler.field() ：{0}", schemaJson.encode());
            // 读取所有的Fields
            final JsonObject entityJson = AoModeler.getEntity(schemaJson);
            return Ux.Jooq.on(MFieldDao.class)
                    .<MField>fetchAndAsync(this.onCriteria(entityJson))
                    .compose(Ux::fnJArray)
                    .compose(fields -> Ux.future(this.onResult(schemaJson, fields)));
        };
    }

    @Override
    public JsonObject executor(final JsonObject schemaJson) {
        LOGGER.debug("[ Ox ] (Sync) 6.1. AoModeler.field() ：{0}", schemaJson.encode());
        final JsonObject entityJson = AoModeler.getEntity(schemaJson);
        // List
        final List<MField> fields = Ux.Jooq.on(MFieldDao.class)
                .fetchAnd(this.onCriteria(entityJson));
        // JsonArray
        final JsonArray fieldArr = Ux.toArray(fields);
        return this.onResult(schemaJson, fieldArr);
    }

    private JsonObject onResult(final JsonObject schemaJson,
                                final JsonArray fields) {
        return schemaJson.put(KeField.Modeling.FIELDS, fields);
    }

    private JsonObject onCriteria(final JsonObject entityJson) {
        final JsonObject filters = new JsonObject();
        filters.put("entityId", entityJson.getString(KeField.KEY));
        return filters;
    }
}