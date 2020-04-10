package io.vertx.tp.modular.dao.internal;

import io.vertx.core.json.JsonObject;
import io.vertx.tp.atom.refine.Ao;
import io.vertx.tp.modular.jooq.internal.Jq;
import io.vertx.up.atom.query.Criteria;
import io.vertx.up.atom.query.Inquiry;
import io.vertx.up.commune.Record;

/**
 * 工具类
 * 1. 只支持集合结果，包括分页专用结果
 * 2. 支持 SELECT 返回搜索结果
 * 3. 连接查询引擎做细粒度查询
 * 4. 返回结果必须是固定格式：
 * {
 * * "count":XX
 * * "data":[
 * * ]
 * }
 */
public class Searchor extends AbstractUtil<Searchor> {

    private Searchor() {
    }

    public static Searchor create() {
        return new Searchor();
    }

    public JsonObject search(final JsonObject filters) {
        Ao.infoSQL(this.getLogger(), "执行方法：Searcher.search");
        return Jq.onPagination(this.irInquiry(Inquiry.create(filters)), this.jooq::search);
    }

    public Record[] query(final JsonObject criteriaJson) {
        Ao.infoSQL(this.getLogger(), "执行方法：Searcher.query");
        final Criteria criteria = Criteria.create(criteriaJson);
        return Jq.onRecords(this.irCond(criteria), this.jooq::query);
    }
}
