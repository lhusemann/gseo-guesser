package com.gse23.lhusemann;

public class Distanz {
    private Distanz() {
    }
    //berechnet Distanz
    public static Double getDistance(Double latOne, Double latTwo, Double lonOne, Double lonTwo) {
        double deltaLat = getRadians(latTwo - latOne);
        double deltaLon = getRadians(lonTwo - lonOne);
        double haversine = havHelper(deltaLat) + Math.cos(getRadians(latOne)) * Math.cos(getRadians(latTwo))
                * havHelper(deltaLon);
        final double radErde = 6371008.7;
        return 2.0 * radErde * Math.asin(Math.sqrt(haversine));
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
