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

// Authors:  P. Neis, E. Steiger 

/* Class descriptor: store LCL point information */

package heigit.ors.routing.traffic;

import com.vividsolutions.jts.geom.Coordinate;

public class TmcPoint
{
  private int lcd = -1;
  private int neg_off_lcd = -1;
  private int pos_off_lcd = -1;
  private Coordinate coord = null;
  
  public TmcPoint(int lcd, int neg_off_lcd, int pos_off_lcd) {
    this.lcd = lcd;
    this.neg_off_lcd = neg_off_lcd;
    this.pos_off_lcd = pos_off_lcd;
  }
  
  public String toString() {
    return "LCD: " + this.lcd + " Neg_Off_LCD: " + this.neg_off_lcd + " Pos_Off_LCD: " + this.pos_off_lcd + " Coordinate: " + this.coord;
  }

  public int getLcd()
  {
    return this.lcd;
  }
  
  public int getNeg_off_lcd()
  {
    return this.neg_off_lcd;
  }

  public int getPos_off_lcd()
  {
    return this.pos_off_lcd;
  }
  
  public Coordinate getCoordinate()
  {
    return this.coord;
  }
  
  public void setCoordinate(Coordinate coord)
  {
    this.coord = coord;
  }
}

