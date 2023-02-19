package org.tud.oas.routing;

import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.requests.isochrones.IsochronesRequestEnums;
import org.heigit.ors.isochrones.IsochroneMap;
import org.heigit.ors.isochrones.IsochroneMapCollection;
import org.heigit.ors.routing.RoutingProfileManager;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.APIEnums.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EmbeddedORS implements IRoutingProvider {
	static final Logger logger = LoggerFactory.getLogger(EmbeddedORS.class);

    public EmbeddedORS() {
        logger.info("load Routing Graph");
		RoutingProfileManager.getInstance();
        logger.info("finished loading Routing Graph");
    }

    public List<IsochroneCollection> requestIsochrones(Double[][] locations, List<Double> ranges) {
        IsochronesRequest request = new IsochronesRequest();
        request.setProfile(Profile.DRIVING_CAR);
        request.setLocations(locations);
        request.setLocationType(IsochronesRequestEnums.LocationType.DESTINATION);
        request.setRange(ranges);
        request.setRangeType(IsochronesRequestEnums.RangeType.TIME);
        request.setRangeUnit(APIEnums.Units.METRES);
        request.setSmoothing(5.0);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);

        try {
            request.generateIsochronesFromRequest();
        }
        catch (Exception e) {
            return null;
        }

        var iso_colls = new ArrayList<IsochroneCollection>(locations.length);

        var iso_maps = request.getIsoMaps();
        for (IsochroneMap iso_map : iso_maps.getIsochroneMaps()) {
            List<Isochrone> isochrones = new ArrayList<Isochrone>();
            for (var iso : iso_map.getIsochrones()) {
                isochrones.add(new Isochrone(iso.getGeometry(), iso.getValue()));
            }
            IsochroneCollection iso_coll = new IsochroneCollection(iso_map.getEnvelope(), isochrones, iso_map.getCenter());
            iso_colls.add(iso_coll);
        }

        return iso_colls;
    }
    
}
