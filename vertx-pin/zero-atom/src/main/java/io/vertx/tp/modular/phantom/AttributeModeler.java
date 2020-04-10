package io.vertx.tp.modular.phantom;

import cn.vertxup.atom.domain.tables.daos.MAttributeDao;
import cn.vertxup.atom.domain.tables.pojos.MAttribute;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.tp.ke.cv.KeField;
import io.vertx.up.log.Annal;
import io.vertx.up.unity.Ux;

import java.util.List;
import java.util.function.Function;

class AttributeModeler implements AoModeler {
    private static final Annal LOGGER = Annal.get(AttributeModeler.class);

    @Override
    public Function<JsonObject, Future<JsonObject>> apply() {
        return modelJson -> {
            LOGGER.debug("[ Ox ] 2. AoModeler.attribute() ：{0}", modelJson.encode());
            return Ux.Jooq.on(MAttributeDao.class)
                    .fetchAsync(KeField.MODEL_ID, this.getModelId(modelJson))
                    .compose(Ux::fnJArray)
                    .compose(attributes -> Ux.future(modelJson.put(KeField.Modeling.ATTRIBUTES, attributes)));
        };
    }

    @Override
    public JsonObject executor(final JsonObject modelJson) {
        LOGGER.debug("[ Ox ] (Sync) 2. AoModeler.attribute() ：{0}", modelJson.encode());
        // List
        final List<MAttribute> attrList = Ux.Jooq.on(MAttributeDao.class)
                .fetch(KeField.MODEL_ID, this.getModelId(modelJson));
        // JsonArray
        final JsonArray attrArr = Ux.toArray(attrList);

        modelJson.put(KeField.Modeling.ATTRIBUTES, attrArr);
        return modelJson;
    }

    private String getModelId(final JsonObject modelJson) {
        final JsonObject model = modelJson.getJsonObject(KeField.MODEL);
        return model.getString(KeField.KEY);
    }
}
