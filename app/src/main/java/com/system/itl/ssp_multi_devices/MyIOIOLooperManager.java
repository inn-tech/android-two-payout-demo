package com.system.itl.ssp_multi_devices;

import android.content.Context;
import android.util.Log;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

/**
 * Created by Programmer on 16/3/2560.
 */

public class MyIOIOLooperManager extends BaseIOIOLooper {

    public IOIO ioioStatic;
    private ThreadFlashes threadFlashes;
    private ThreadBankPayout SP0;
    private ThreadBankPayout SP1;



    /**********************************************************************************************
     * Constructor(s)
     **********************************************************************************************/
    MyIOIOLooperManager(Context contextGiven) {
        /*********************************************************************************************
         Variable(s)
         */

        threadFlashes = null;
        SP0 = null;
        SP1 = null;

    }

    /**********************************************************************************************
     * Method/function(s)
     **********************************************************************************************/
    @Override
    protected void setup() throws ConnectionLostException, InterruptedException {

        ioioStatic = ioio_;


        if(threadFlashes == null) {
            threadFlashes = new ThreadFlashes();
            threadFlashes.setup(ioioStatic);
            threadFlashes.start();
        }

        if(SP0 == null) {
            SP0 = new ThreadBankPayout("SP0");
            SP0.setup(ioioStatic, 7, 5, 6);
            SP0.start();
            SP0.RunSSPSystem();
            SP0.SSPDisable();
        }

        if(SP1 == null) {
            SP1 = new ThreadBankPayout("SP1");
            SP1.setup(ioioStatic, 10, 4, 3);
            SP1.start();
            SP1.RunSSPSystem();
            SP1.SSPDisable();
        }



    }

    /* loop not used in this application  */
    @Override
    public void loop() throws ConnectionLostException, InterruptedException {

        /* sleep in loop to allow OS */
        Thread.sleep(100);
    }

    @Override
    public void disconnected() {

        if(threadFlashes != null){
            threadFlashes.Close();
        }
        if(SP0 != null){
            SP0.StopSSPSystem();
        }
        if(SP1 != null){
            SP1.StopSSPSystem();
        }

    }


    void onDestroy() {

        if(threadFlashes != null){
            threadFlashes.Close();
        }
        if(SP0 != null){
            SP0.StopSSPSystem();
        }
        if(SP1 != null){
            SP1.StopSSPSystem();
        }

    }

    /**
     * Get the instance of the Payout device threads for use in  other classes
     * @param tag : The string name of the instance to return
     * @return ThreadBankPayout: Instance reference to requested PayoutDevice
     */
    ThreadBankPayout GetPayoutInstance(String tag)
    {

        ThreadBankPayout payoutInstance = null;

        switch(tag){
            case "SP0":
                payoutInstance = SP0;
                break;
            case "SP1":
                payoutInstance = SP1;
                break;
        }

        return  payoutInstance;

    }


}
