package io.vertx.up.log;

import io.vertx.quiz.ZeroBase;
import org.junit.Test;

public class AnnalTk extends ZeroBase {

    @Test
    public void testAnnal() {
        final Annal logger = Annal.get(AnnalTk.class);
    }
}
