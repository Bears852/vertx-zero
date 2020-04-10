package io.vertx.tp.modular.jooq;

import io.vertx.tp.atom.modeling.data.DataEvent;
import io.vertx.tp.modular.jooq.internal.Jq;
import org.jooq.DSLContext;
import org.jooq.SelectWhereStep;

@SuppressWarnings("all")
class JQAggregate {
    private final transient DSLContext context;
    private final transient JQTerm term;

    JQAggregate(final DSLContext context) {
        this.context = context;
        this.term = new JQTerm(context);
    }

    DataEvent count(final DataEvent event) {
        return this.context.transactionResult(configuration -> Jq.doCount(this.getClass(), event, (tables, ingest) -> {
            final SelectWhereStep query = this.term.getSelectSample(event, tables, ingest);
            return (long) query.fetch().size();
        }));
    }
}
