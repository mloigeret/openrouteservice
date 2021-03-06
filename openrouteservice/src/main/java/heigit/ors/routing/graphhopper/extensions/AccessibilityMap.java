/*|----------------------------------------------------------------------------------------------
 *|														Heidelberg University
 *|	  _____ _____  _____      _                     	Department of Geography		
 *|	 / ____|_   _|/ ____|    (_)                    	Chair of GIScience
 *|	| |  __  | | | (___   ___ _  ___ _ __   ___ ___ 	(C) 2014
 *|	| | |_ | | |  \___ \ / __| |/ _ \ '_ \ / __/ _ \	
 *|	| |__| |_| |_ ____) | (__| |  __/ | | | (_|  __/	Berliner Strasse 48								
 *|	 \_____|_____|_____/ \___|_|\___|_| |_|\___\___|	D-69120 Heidelberg, Germany	
 *|	        	                                       	http://www.giscience.uni-hd.de
 *|								
 *|----------------------------------------------------------------------------------------------*/

package heigit.ors.routing.graphhopper.extensions;

import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.storage.SPTEntry;

public class AccessibilityMap {
	private IntObjectMap<SPTEntry> map;
	private SPTEntry edgeEntry;
	
	public AccessibilityMap(IntObjectMap<SPTEntry> map, SPTEntry edgeEntry)
	{
		this.map = map;
		this.edgeEntry = edgeEntry;
	}
	
	public boolean isEmpty()
	{
		return map.size() == 0;
	}
	
	public IntObjectMap<SPTEntry> getMap()
	{
		return map;
	}
	
	public SPTEntry getEdgeEntry()
	{
		return edgeEntry;
	}
}
