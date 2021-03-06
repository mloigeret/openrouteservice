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
package heigit.ors.geocoding.geocoders;

public class GeocodingResult  {
	public String country;
	public String countryCode;
	public String region;
	public String county;
	public String municipality;
	public String locality;
	public String borough;
	public String neighbourhood;
	public String postalCode;
	public String street;
	public String name;
	public String houseNumber;
	public String objectName;
	public String placeType;
	
	public double latitude;
	public double longitude;
	
	public double distance = 0.0;
	
	public float confidence = 0.5F;
}
