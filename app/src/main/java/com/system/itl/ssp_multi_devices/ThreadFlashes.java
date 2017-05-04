package com.system.itl.ssp_multi_devices;

import android.util.Log;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.android.IOIOActivity;

/**
 * Created by Programmer on 16/3/2560.
 */

public class ThreadFlashes extends Thread {

    /**********************************************************************************************
     * Variable(s)
     **********************************************************************************************/
    public static DigitalOutput digitalOutputSmallFlash;
    private boolean running;

    /**********************************************************************************************
     * Constructor(s)
     **********************************************************************************************/
    public ThreadFlashes() {

        running = false;

    }

    /**********************************************************************************************
     * Method/function(s)
     **********************************************************************************************/
    public void setup(IOIO ioio_) {
        try {
            digitalOutputSmallFlash = ioio_.openDigitalOutput(0); //Deliver the electrical current to the leg.

        } catch (ConnectionLostException e) {
            e.printStackTrace();
            Log.e("ThreadFlashesServiced", "" + e);
        }
    }


    public void Close()
    {

        running = false;
    }


    @Override
    public void run() {
        // super.run();
        //Intent intent = new Intent("BoolEx");

        running = true;
        try {
            while (running) {
                Log.e("ThreadFlashesServiced", "status: " + this.getState());
                digitalOutputSmallFlash.write(true);

                Thread.sleep(1000);

                digitalOutputSmallFlash.write(false);

                Thread.sleep(1000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e("ThreadFlashesServiced", "" + e);

        } catch (ConnectionLostException e) {
            e.printStackTrace();
            Log.e("ThreadFlashesServiced", "" + e);
        }

        Log.e("ThreadFlashesServiced", "ThreadFlashesServiced status: " + this.getState());
    }

}
