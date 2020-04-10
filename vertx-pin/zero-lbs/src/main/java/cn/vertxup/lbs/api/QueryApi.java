package cn.vertxup.lbs.api;

import io.vertx.tp.lbs.cv.Addr;
import io.vertx.up.annotations.Address;
import io.vertx.up.annotations.EndPoint;
import io.vertx.up.eon.ID;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@EndPoint
@Path("/api")
public interface QueryApi {
    /*
     * Countries
     */
    @Path("/countries")
    @GET
    @Address(Addr.PickUp.COUNTRIES)
    String queryCountries();

    /*
     * State from Country
     */
    @Path("/states/query/{countryId}")
    @GET
    @Address(Addr.PickUp.STATE_BY_COUNTRY)
    String queryStates(@PathParam("countryId") String countryId);

    /*
     * City from State
     */
    @Path("/cities/query/{stateId}")
    @GET
    @Address(Addr.PickUp.CITY_BY_STATE)
    String queryCities(@PathParam("stateId") String stateId);

    /*
     * Region from City
     */
    @Path("/regions/query/{cityId}")
    @GET
    @Address(Addr.PickUp.REGION_BY_CITY)
    String queryRegions(@PathParam("cityId") String cityId);

    /*
     * When init based on Region here
     */
    @Path("/regions/meta/{id}")
    @GET
    @Address(Addr.PickUp.REGION_META)
    String initRegion(@PathParam("id") String id);

    @Path("/tents")
    @GET
    @Address(Addr.PickUp.TENT_BY_SIGMA)
    String getTents(@HeaderParam(ID.Header.X_SIGMA) String sigma);

    @Path("/floors")
    @GET
    @Address(Addr.PickUp.FLOOR_BY_SIGMA)
    String getFloors(@HeaderParam(ID.Header.X_SIGMA) String sigma);
}
