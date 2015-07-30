package com.example.campusguidv5;


import java.util.*;


public class Coordinate_LatLon{
	double Lat;
	double Lon;
	//經度的弧度
	double RadLat;
	//緯度的弧度
	double RadLon;

	public Coordinate_LatLon(double Lon, double Lat){
		this.Lat = Lat;
		this.Lon = Lon;
		
		this.RadLat = Lat * Math.PI / 180;
		this.RadLon = Lon * Math.PI / 180;
	}

	

}
	class getBearing{
		public static double getBearing(Coordinate_LatLon locat_1, Coordinate_LatLon locat_2){

			
			//double dLon = (point._lon-this._lon).toRad();
			double dLat = (locat_2.Lat - locat_1.Lat) * Math.PI / 180;
			double dLon = (locat_2.Lon - locat_1.Lon) * Math.PI / 180;
			double Radlat1 = locat_1.RadLat;
			double Radlat2 = locat_2.RadLat;
			
			double y = Math.sin(dLon) * Math.cos(Radlat2);
			double x = Math.cos(Radlat1)*Math.sin(Radlat2) -
						Math.sin(Radlat1)*Math.cos(Radlat2)*Math.cos(dLon);

			double Bearing = Math.atan2(y, x) * 180 / Math.PI;
					Bearing = ( Bearing + 360 ) % 360;

			return Bearing;
		}
	}
