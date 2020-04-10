package io.vertx.up.uca.web.anima;

import io.vertx.up.eon.em.MessageModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Service discovery usage.
 */
public class DetectScatter extends WorkerScatter {

    @Override
    protected Set<MessageModel> getModel() {
        return new HashSet<MessageModel>() {
            {
                add(MessageModel.DISCOVERY_PUBLISH);
            }
        };
    }
}
