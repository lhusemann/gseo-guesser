package com.gse23.lhusemann;

import java.text.NumberFormat;

public class FormatUmrechner {
    final static String komma = ",";
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
        return number.replaceAll(komma, "");
    }
    //testet ob Kilometer
    private static boolean isKM(double numberDouble) {
        int number = (int) numberDouble;
        final int kilometer = 1000;
        return number / kilometer >= 1;
    }
}
