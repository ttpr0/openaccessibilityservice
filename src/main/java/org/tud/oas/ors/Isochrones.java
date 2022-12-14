package org.tud.oas.ors;

import java.util.List;

import org.heigit.ors.api.requests.isochrones.IsochronesRequest;
import org.heigit.ors.api.requests.isochrones.IsochronesRequestEnums;
import org.heigit.ors.isochrones.IsochroneMapCollection;
import org.heigit.ors.api.requests.common.APIEnums;
import org.heigit.ors.api.requests.common.APIEnums.Profile;

public final class Isochrones {
    public static IsochroneMapCollection requestIsochrones(Double[][] locations, List<Double> ranges) throws Exception {
        IsochronesRequest request = new IsochronesRequest();
        request.setProfile(Profile.DRIVING_CAR);
        request.setLocations(locations);
        request.setLocationType(IsochronesRequestEnums.LocationType.DESTINATION);
        request.setRange(ranges);
        request.setRangeType(IsochronesRequestEnums.RangeType.TIME);
        request.setRangeUnit(APIEnums.Units.METRES);
        request.setSmoothing(5.0);
        request.setResponseType(APIEnums.RouteResponseType.GEOJSON);

        request.generateIsochronesFromRequest();
        return request.getIsoMaps();
    }
}
