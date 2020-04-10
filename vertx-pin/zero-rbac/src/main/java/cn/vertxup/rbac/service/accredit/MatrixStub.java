package cn.vertxup.rbac.service.accredit;

import cn.vertxup.rbac.domain.tables.pojos.SResource;
import io.vertx.core.Future;
import io.vertx.tp.rbac.atom.ScRequest;
import io.vertx.up.commune.config.DataBound;

/*
 * ResourceMatrix capture for user/role session storage
 */
public interface MatrixStub {
    /*
     * Fetch DataBound by:
     * request - userId, session, fetchProfile
     */
    Future<DataBound> fetchBound(ScRequest request, SResource resource);
}
