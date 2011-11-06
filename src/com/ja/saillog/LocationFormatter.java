package com.ja.saillog;

public class LocationFormatter {
	public static String formatLatitude(double latitude) {	
		return formatCoordinate(latitude, "N", "S", 90);
	}
	
	public static String formatLongitude(double longitude) {
		return formatCoordinate(longitude, "E", "W", 180);
	}
	
	private static String formatCoordinate(double coord, 
										   String positiveHemisphere,
										   String negativeHemisphere,
										   double maxCoord) {
		if (coord > maxCoord || coord < (-1 * maxCoord)) {
			return "";
		}
		
		String hemisphere = positiveHemisphere;
		if (coord < 0) {
			hemisphere = negativeHemisphere;
		}
		double abslat = Math.abs(coord);
		
		long decimal = (long) abslat;
		double fractional = abslat - decimal;
		
		double minutes = fractional * 60.0;
		
		return String.format("%s %d¡ %.1f'", hemisphere, decimal, minutes);
	}
										   
										   
										   
}
