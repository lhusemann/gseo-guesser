package com.gse23.lhusemann;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Timer;
import java.util.TimerTask;

public class RoundTimer {
    private int  time;
    private int counter;
    private Timer timer;
    private TimerTask timerTask;
    private PropertyChangeSupport support;
    private static final int SEKUNDE = 1000;
    RoundTimer(int time) {
        this.time = time;
        this.support = new PropertyChangeSupport(this);
    }
    //Basiert auf https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            this.support.firePropertyChange("ZeitVorbei", 1, 0);

        }
    }
    //Basiert auf https://stackoverflow.com/questions/10029831/how-do-you-use-a-timertask-to-run-a-thread
    public void startTimer() {
        timer = new Timer();
        counter = time;
        timerTask = new TimerTask() {
            public void run() {
                support.firePropertyChange("ChangeTimer", counter+1, counter);
                counter--;
                if (counter < 1) {
                    stopTimer();
                }
            }
        };
        timer.schedule(timerTask, 1, SEKUNDE);
    }
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
