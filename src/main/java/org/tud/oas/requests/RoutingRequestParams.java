package org.tud.oas.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RoutingRequestParams", description = """
		Parameters for the routing.
		Parameters will be used for queries on routing backend.
		All Parameters are optional, is usefull defaults will be set.
		""")
public class RoutingRequestParams {
	// *************************************
	// routing provider
	// *************************************
	@Schema(name = "routing_provider", description = """
			Routing Provider used (e.g. ors-api, ...).
			""", example = "ors-api")
	@JsonProperty("routing_provider")
	public String routing_provider;

	// *************************************
	// standard routing params
	// *************************************
	@Schema(name = "profile", description = """
			Routing profile to be used (e.g. driving-car, ...).
			""", example = "driving-car")
	@JsonProperty("profile")
	public String profile;

	@Schema(name = "range_type", description = """
			Routing metric (travel-time or distance).
			""", example = "time")
	@JsonProperty("range_type")
	public String range_type;

	// *************************************
	// additional routing params
	// *************************************
	@Schema(name = "avoid_borders", description = """
			Sets wheather borders should be avoided (cross country).
			""", example = "all")
	@JsonProperty("avoid_borders")
	public String avoid_borders;

	@Schema(name = "avoid_features", description = """
			Sets which road segments should be avoided.
			""", example = "[\"highway\", \"ferries\"]")
	@JsonProperty("avoid_features")
	public String[] avoid_features;

	@Schema(name = "avoid_polygons", description = """
			Sets an area to avid while routing.
			Polygon or MultiPolygon formatted as GeoJSON.
			""", example = "{\"type\": \"Polygon\", \"coordinates\": [[9.1, 50.1], [9.9, 50.1], [9.4, 50.8]]}")
	@JsonProperty("avoid_polygons")
	public Object avoid_polygons;

	// *************************************
	// isochrone params
	// *************************************
	@Schema(name = "location_type", description = """
			Sets weather input locations should be used as origin or destination of travel.
			""", example = "start")
	@JsonProperty("location_type")
	public String location_type;

	@Schema(name = "isochrone_smoothing", description = """
			Sets how much isochrones should be smoothed.
			High values lead to a high degree of smoothing.
			""", example = "25")
	@JsonProperty("isochrone_smoothing")
	public Float isochrone_smoothing;

	// *************************************
	// distance params
	// *************************************
	@Schema(name = "avg_speed", description = """
			Speed used to convert distances (provider "distance") to range_type "time" in km/h.
			""", example = "50")
	@JsonProperty("avg_speed")
	public Float avg_speed;
}
