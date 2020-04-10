package io.vertx.up.eon;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public interface Constants {

    String DEFAULT_GROUP = "__VERTX_ZERO__";

    int DEFAULT_INSTANCES = 32;

    boolean DEFAULT_HA = true;

    String DEFAULT_JOB = "jobs";

    String DEFAULT_JOB_NAMESPACE = "zero.vertx.jobs";

    String DEFAULT_JOOQ_HISTORY = "orbit";

    String DEFAULT_JOOQ = "provider";

    /**
     * Scanned data to distinguish mode
     * 1) Only Interface Style could have the indexes key such as 0,1,2 consider as data key.
     * 2) The mode impact different flow of Envelop
     */
    ConcurrentMap<Integer, String> INDEXES = new ConcurrentHashMap<Integer, String>() {
        {
            this.put(0, "0");
            this.put(1, "1");
            this.put(2, "2");
            this.put(3, "3");
            this.put(4, "4");
            this.put(5, "5");
            this.put(6, "6");
            this.put(7, "7");
        }
    };
}
