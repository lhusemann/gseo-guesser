package com.gse23.lhusemann;

public final class Distanz {
    private Distanz() {
    }
    //berechnet Distanz
    public static double calcDistanceNum(double latOne, double latTwo, double lonOne, double lonTwo) {
        double deltaLat = getRadians(latTwo - latOne);
        double deltaLon = getRadians(lonTwo - lonOne);
        double haversine = havHelper(deltaLat) + Math.cos(getRadians(latOne)) * Math.cos(getRadians(latTwo))
                * havHelper(deltaLon);
        final double radErde = 6371008.7;
        return 2.0 * radErde * Math.asin(Math.sqrt(haversine));
    }
    public String calcDistanceText(double latOne, double latTwo, double lonOne, double lonTwo) {
        double deltaLat = getRadians(latTwo - latOne);
        double deltaLon = getRadians(lonTwo - lonOne);
        double haversine = havHelper(deltaLat) + Math.cos(getRadians(latOne)) * Math.cos(getRadians(latTwo))
                * havHelper(deltaLon);
        final double radErde = 6371008.7;
        double distance = 2.0 * radErde * Math.asin(Math.sqrt(haversine));
        return Formatierung.getUnit(distance);
    }
    //rechnet von degree in radians um
    private static Double getRadians(Double degree) {
        final double hundertAchtzig = 180.0;
        return degree * Math.PI / hundertAchtzig;
    }
    private static Double havHelper(Double theta) {
        return Math.sin(theta / 2) * Math.sin(theta / 2);
    }

}
