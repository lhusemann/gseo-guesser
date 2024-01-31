package com.gse23.lhusemann;

import org.assertj.core.api.Assertions;
import org.junit.Test;
public class PunkteTest {

    /**
     * Akzeptanzkriterium: (3.1.1.1) Eine Punkteberechnung mit der minimalen Distanz (10.0), ist valide.
     * Äquivalenzklasse: Eine Distanz die zwischen 10m und 10km ist. (Normalablauf)
     * Ausgangszustand:
     * <p>
     * Distanz: 10m
     * <p>
     * Erwartetes Ergebnis:
     * Die Punktzahl sollte entsprechend der berechneten Formel im gültigen Bereich liegen.
     * Die Punktzahl sollte maximal sein (5000).
     * <p>
     * Erwartete Exception: Keine
     */
    @Test
    public void standardCaseEdgeCaseMin() throws NegativeDistanceException {
        //Vorbedingung
        Punkte punkte = new Punkte(10.0, 10000.0);

        //Ausführung
        punkte.berechne(10.0);

        //Annahme
        Assertions.assertThat(punkte.getCurrentPunkteNum()).isEqualTo(5000);
    }

    /**
     * Akzeptanzkriterium: (3.1.1.2) Eine Punkteberechnung mit der maximale Distanz (10000.0), ist valide.
     * Äquivalenzklasse: Eine Distanz die zwischen 10m und 10km ist. (Normalablauf)
     * Ausgangszustand:
     * <p>
     * Distanz: 10km
     * <p>
     * Erwartetes Ergebnis:
     * Die Punktzahl sollte entsprechend der berechneten Formel im gültigen Bereich liegen.
     * Die Punktzahl sollte minimal sein (0).
     * <p>
     * Erwartete Exception: Keine
     */
    @Test
    public void standardCaseEdgeCaseMax() throws NegativeDistanceException {
        //Vorbedingung
        Punkte punkte = new Punkte(10.0, 10000.0);

        //Ausführung
        punkte.berechne(10000.0);

        //Annahme
        Assertions.assertThat(punkte.getCurrentPunkteNum()).isEqualTo(0);
    }

    /**
     * Akzeptanzkriterium: (3.1.1.3) Eine Punkteberechnung ist nur dann valide,
     * wenn die Punktzahl nicht den Wert 5000 überschreitet.
     * Äquivalenzklasse: Ein Punktzahl die zwischen 0 und 5000(inklusive) ist. (Normalablauf)
     * Ausgangszustand:
     * <p>
     * Distanz: 5km
     * <p>
     * Erwartetes Ergebnis:
     * Die Punktzahl sollte entsprechend der berechneten Formel im gültigen Bereich liegen.
     * Die Punktzahl sollte hier bei 501 liegen.
     * <p>
     * Erwartete Exception: Keine
     */
    @Test
    public void standardCaseRandom() throws NegativeDistanceException {
        //Vorbedingung
        Punkte punkte = new Punkte(10.0, 10000.0);

        //Ausführung
        punkte.berechne(5000.0);

        //Annahme
        Assertions.assertThat(punkte.getCurrentPunkteNum()).isEqualTo(501);
    }

    /**
     * Akzeptanzkriterium: (3.1) Die Punkte nehmen mit zunehmender Distanz ab bzw. zu
     * Äquivalenzklasse: Eine positive Distanz unter 10m
     * Ausgangszustand:
     * <p>
     * Distanz: 9m
     * <p>
     * Erwartetes Ergebnis:
     * Die Punktzahl sollte maximal sein (5000)
     * <p>
     * Erwartete Exception: Keine
     */
    @Test
    public void belowMinDistance() throws NegativeDistanceException {
        //Vorbedingung
        Punkte punkte = new Punkte(10.0, 10000.0);

        //Ausführung
        punkte.berechne(9.0);

        //Annahme
        Assertions.assertThat(punkte.getCurrentPunkteNum()).isEqualTo(5000);
    }

    /**
     * Akzeptanzkriterium: (3.1) Die Punkte nehmen mit zunehmender Distanz ab bzw. zu
     * Äquivalenzklasse: Eine negative Distanz
     * Ausgangszustand:
     * <p>
     * Distanz: -10m
     * <p>
     * Erwartetes Ergebnis:
     * Kein Ergebnis vorhanden, auf Grund von Wurd einer Exception
     * <p>
     * Erwartete Exception: NegativeDistanceException
     */
    @Test (expected = NegativeDistanceException.class)
    public void negativeDistance() throws NegativeDistanceException {
        //Vorbedingung
        Punkte punkte = new Punkte(10.0, 10000.0);

        //Ausführung
        punkte.berechne(-10.0);

        //Annahme
        //NegativeDistanceException
    }

    /**
     * Akzeptanzkriterium: (3.1) Die Punkte nehmen mit zunehmender Distanz ab bzw. zu
     * Äquivalenzklasse: Eine Distanz über 10km
     * Ausgangszustand:
     * <p>
     * Distanz: 11000m
     * <p>
     * Erwartetes Ergebnis:
     * Die Punktzahl sollte minimal sein (0)
     * <p>
     * Erwartete Exception: Keine
     */
    @Test
    public void aboveMaxDistance() throws NegativeDistanceException {
        //Vorbedingung
        Punkte punkte = new Punkte(10.0, 10000.0);

        //Ausführung
        punkte.berechne(11000.0);

        //Annahme
        Assertions.assertThat(punkte.getCurrentPunkteNum()).isEqualTo(0);
    }

    /**
     * Akzeptanzkriterium: (1) Es wird ein Score für die aktuelle Schätzung berechnet
     * Äquivalenzklasse: Eine Distanz die zwischen 10m und 10km ist. (Normalablauf)
     * Ausgangszustand:
     * <p>
     * Distanz: 100m
     * <p>
     * Erwartetes Ergebnis:
     * Die Punktzahl sollte entsprechend der berechneten Formel im gültigen Bereich liegen.
     * Die Punktzahl sollte hier bei 3333 liegen.
     * <p>
     * Erwartete Exception: Keine
     */
    @Test
    public void standardCaseRandomHundert() throws NegativeDistanceException {
        //Vorbedingung
        Punkte punkte = new Punkte(10.0, 10000.0);

        //Ausführung
        punkte.berechne(100.0);

        //Annahme
        Assertions.assertThat(punkte.getCurrentPunkteNum()).isEqualTo(3333);
    }

    /**
     * Akzeptanzkriterium: (1) Es wird ein Score für die aktuelle Schätzung berechnet
     * Äquivalenzklasse: Eine Distanz die zwischen 10m und 10km ist. (Normalablauf)
     * Ausgangszustand:
     * <p>
     * Distanz: 200m
     * <p>
     * Erwartetes Ergebnis:
     * Die Punktzahl sollte entsprechend der berechneten Formel im gültigen Bereich liegen.
     * Die Punktzahl sollte hier bei 2831 liegen.
     * <p>
     * Erwartete Exception: Keine
     */
    @Test
    public void standardCaseRandomZweiHundert() throws NegativeDistanceException {
        //Vorbedingung
        Punkte punkte = new Punkte(10.0, 10000.0);

        //Ausführung
        punkte.berechne(200.0);

        //Annahme
        Assertions.assertThat(punkte.getCurrentPunkteNum()).isEqualTo(2831);
    }
}
