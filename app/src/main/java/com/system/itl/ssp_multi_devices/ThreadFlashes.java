package com.system.itl.ssp_multi_devices;

import android.util.Log;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;



class ThreadFlashes extends Thread {

    /**********************************************************************************************
     * Variable(s)
     **********************************************************************************************/
    private static DigitalOutput digitalOutputSmallFlash;
    private boolean running;
    private String deviceName;
    private int flashIntervalMs;

    /**********************************************************************************************
     * Constructor(s)
     **********************************************************************************************/
     ThreadFlashes(String tag) {

        deviceName = tag;
        running = false;
        flashIntervalMs = 1000;

    }

    /**********************************************************************************************
     * Method/function(s)
     **********************************************************************************************/
    void setup(IOIO ioio_, int intervalms) {

        flashIntervalMs = intervalms;

        try {

            digitalOutputSmallFlash = ioio_.openDigitalOutput(0); //Deliver the electrical current to the leg.

        } catch (ConnectionLostException e) {
            e.printStackTrace();
        }
    }


    String GetDeviceName()
    {
        return deviceName;
    }

    public void Close()
    {

        running = false;
    }


    @Override
    public void run() {


        running = true;
        try {
            while (running) {
                digitalOutputSmallFlash.write(true);
                Thread.sleep(flashIntervalMs);
                digitalOutputSmallFlash.write(false);
                Thread.sleep(flashIntervalMs);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e("ThreadFlashesServiced", "" + e);

        } catch (ConnectionLostException e) {
            e.printStackTrace();
            Log.e("ThreadFlashesServiced", "" + e);
        }

    }

}
