package cn.vertxup.ui.api;

import io.vertx.core.json.JsonObject;
import io.vertx.tp.ke.cv.KeField;
import io.vertx.tp.ui.cv.Addr;
import io.vertx.up.annotations.Address;
import io.vertx.up.annotations.EndPoint;
import io.vertx.up.eon.ID;

import javax.ws.rs.*;

@EndPoint
@Path("/api")
public interface UiApi {
    /*
     * Condition should be:
     * sigma +
     * {
     *     app,
     *     module,
     *     page
     * }
     * AMP means three parameters here.
     */
    @Path("/ui/page")
    @POST
    @Address(Addr.Page.FETCH_AMP)
    JsonObject fetchPage(@HeaderParam(ID.Header.X_SIGMA) String sigma,
                         @BodyParam JsonObject body);

    /*
     * Condition should be:
     * {
     *     control, ( form Id, list Id )
     *     type
     * }
     */
    @Path("/ui/control")
    @POST
    @Address(Addr.Control.FETCH_BY_ID)
    JsonObject fetchControl(@BodyParam JsonObject body);

    /*
     * Condition should be:
     * {
     *     control
     * }
     */
    @Path("/ui/ops")
    @POST
    @Address(Addr.Control.FETCH_OP)
    JsonObject fetchOp(@BodyParam JsonObject body);

    /*
     * Fetch form configuration by
     * code, this method is for single form fetch
     */
    @Path("/ui/form/:code")
    @GET
    @Address(Addr.Control.FETCH_FORM_BY_CODE)
    JsonObject fetchForm(@HeaderParam(ID.Header.X_SIGMA) String sigma,
                         @PathParam(KeField.CODE) String name);
}
