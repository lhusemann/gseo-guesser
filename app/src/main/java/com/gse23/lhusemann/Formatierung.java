package com.gse23.lhusemann;

import java.text.NumberFormat;

public final class Formatierung {
    private Formatierung() {
    }
    //gibt Wert in richter Maßeinheit und gerundet aus
    public static String getUnit(double numberDouble) {
        NumberFormat form = NumberFormat.getInstance();
        form.setMaximumFractionDigits(2);
        final int kilometer = 1000;
        if (isKM(numberDouble)) {
            return removeKomma(form.format(numberDouble / kilometer)) + " km";
        } else {
            return removeKomma(form.format(numberDouble)) + " m";
        }
    }
    //entfernt komma aus String
    private static String removeKomma(String number) {
        return number.replaceAll(",", "");
    }
    //testet ob Kilometer
    private static boolean isKM(double numberDouble) {
        int number = (int) numberDouble;
        final int kilometer = 1000;
        return number / kilometer >= 1;
    }
    //prüft ob valider Laengengrad
    public static boolean isValidLongitude(double longitude) {
        final double min = -180.0;
        final double max = 180.0;
        return min <= longitude && longitude <= max;
    }
    //prüft ob valider Breitengrad
    public static boolean isValidLatitude(double latitude) {
        final double min = -90;
        final double max = 90;
        return min <= latitude && latitude <= max;
    }
}
