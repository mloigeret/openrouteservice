/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014-2016
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/
package heigit.ors.services.routing.requestprocessors.json;

import javax.servlet.http.HttpServletRequest;

import com.graphhopper.util.Helper;
import com.vividsolutions.jts.geom.Coordinate;

import heigit.ors.exceptions.ParameterOutOfRangeException;
import heigit.ors.exceptions.UnknownParameterValueException;
import heigit.ors.routing.RouteExtraInfoFlag;
import heigit.ors.routing.RouteSearchParameters;
import heigit.ors.routing.RoutingProfileManager;
import heigit.ors.routing.RoutingProfileType;
import heigit.ors.routing.WeightingMethod;
import heigit.ors.services.routing.RouteInstructionsFormat;
import heigit.ors.services.routing.RoutingRequest;
import heigit.ors.services.routing.RoutingServiceSettings;
import heigit.ors.util.DistanceUnit;
import heigit.ors.util.DistanceUnitUtil;

public class JsonRoutingRequestParser 
{
	public static RoutingRequest parseFromRequestParams(HttpServletRequest request) throws Exception
	{
		RoutingRequest req = new RoutingRequest();
		RouteSearchParameters searchParams = req.getSearchParameters();
		
		String value = request.getParameter("profile");
		if (!Helper.isEmpty(value))
			searchParams.setProfileType(RoutingProfileType.getFromString(value));
		
		value = request.getParameter("preference");
		if (!Helper.isEmpty(value))
			searchParams.setWeightingMethod(WeightingMethod.getFromString(value));
		
		value = request.getParameter("coordinates");
		if (!Helper.isEmpty(value))
		{
			String[] coordValues = value.split("\\|");
			Coordinate[] coords = new Coordinate[coordValues.length];

			for (int i = 0; i < coordValues.length; i++)
			{
				String[] locations = coordValues[i].split(",");
				if (locations.length == 3)
					coords[i] = new Coordinate(Double.parseDouble(locations[0]), Double.parseDouble(locations[1]), Integer.parseInt(locations[2]));
				else
					coords[i] = new Coordinate(Double.parseDouble(locations[0]),Double.parseDouble(locations[1]));
			} 
			
			if (coords.length > RoutingServiceSettings.getMaximumWayPoints())
				throw new ParameterOutOfRangeException("coordinates", Integer.toString(coords.length), Integer.toString(RoutingServiceSettings.getMaximumWayPoints()));
			
			req.setCoordinates(coords);
		}		
		
		value = request.getParameter("units");
		if (!Helper.isEmpty(value))
			req.setUnits(DistanceUnitUtil.getFromString(value, DistanceUnit.Meters));		

		value = request.getParameter("language");
		if (!Helper.isEmpty(value))
			req.setLanguage(value);
		
		value = request.getParameter("geometry");
		if (!Helper.isEmpty(value))
			req.setIncludeGeometry(Boolean.parseBoolean(value));
		
		value = request.getParameter("geometry_format");
		if (!Helper.isEmpty(value))
		{
			if (!("geojson".equalsIgnoreCase(value) || "polyline".equalsIgnoreCase(value) || "encodedpolyline".equalsIgnoreCase(value)))
				throw new UnknownParameterValueException("geometry_format", value);

			req.setGeometryFormat(value);
		}
		
		value = request.getParameter("geometry_simplify");
		if (!Helper.isEmpty(value))
		   req.setSimplifyGeometry(Boolean.parseBoolean(value));

		value = request.getParameter("instructions");
		if (!Helper.isEmpty(value))
			req.setIncludeInstructions(Boolean.parseBoolean(value));
		
		value = request.getParameter("elevation");
		if (!Helper.isEmpty(value))
			req.setIncludeElevation(Boolean.parseBoolean(value));
		
		value = request.getParameter("instructions_format");
		if (!Helper.isEmpty(value))
		{
			RouteInstructionsFormat instrFormat = RouteInstructionsFormat.fromString(value);
			if (instrFormat == RouteInstructionsFormat.UNKNOWN)
				throw new UnknownParameterValueException("instructions_format", value);
			req.setInstructionsFormat(instrFormat);
		}
		
		value = request.getParameter("extra_info");
		if (!Helper.isEmpty(value))
			req.setExtraInfo(RouteExtraInfoFlag.getFromString(value));
		
		value = request.getParameter("attributes");
		if (!Helper.isEmpty(value))
			req.setAttributes(value.split("\\|"));
	
		value = request.getParameter("options");
		if (!Helper.isEmpty(value))
			searchParams.setOptions(value);
		
		value = request.getParameter("id");
		if (!Helper.isEmpty(value))
			req.setId(value);
			
		return req;		
	}
}