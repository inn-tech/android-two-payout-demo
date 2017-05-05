package com.system.itl.ssp_multi_devices;


import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;


class MyIOIOLooperManager extends BaseIOIOLooper {


    private DeviceManager deviceManager;


    MyIOIOLooperManager() {
        deviceManager = new DeviceManager();
    }

    @Override
    protected void setup() throws ConnectionLostException, InterruptedException {


        /* led flash */
        ThreadFlashes fl1 = new ThreadFlashes("LED1");
        fl1.setup(ioio_ ,500);
        deviceManager.Add(fl1);

        /* Bill validator payout device */
        ThreadSSPDevice SP0 = new ThreadSSPDevice("SP0");
        SP0.setup(deviceManager,ioio_, 7, 5, 6);
        deviceManager.Add(SP0);
        /* Bill validator payout device */
        ThreadSSPDevice SP1 = new ThreadSSPDevice("SP1");
        SP1.setup(deviceManager,ioio_, 10, 4, 3);
        deviceManager.Add(SP1);

        /* start all the device threads that have been added */
        deviceManager.StartAll();


    }

    /* loop not used in this application */
    @Override
    public void loop() throws ConnectionLostException, InterruptedException {
        /* sleep in loop to allow OS */
        Thread.sleep(100);
    }

    @Override
    public void disconnected() {
        deviceManager.CloseAll();
    }


    void onDestroy() {
        deviceManager.CloseAll();
    }

    /**
     * Get the instance of the Payout device threads for use in  other classes
     * @param tag : The string name of the instance to return
     * @return ThreadSSPDevice: Instance reference to requested PayoutDevice
     */
    ThreadSSPDevice GetPayoutInstance(String tag)
    {

        ThreadSSPDevice payoutInstance = null;

        for (ThreadSSPDevice s: deviceManager.GetSSPDevices()
                ) {
            if(s.GetSystemName().equals(tag)){
                payoutInstance = s;
                break;
            }
        }

        return  payoutInstance;

    }


}
