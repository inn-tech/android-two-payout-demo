package com.system.itl.ssp_multi_devices;

import android.app.Application;


public class Globals extends Application {

    private ThreadSSPDevice mPayoutSystem;
    private MyIOIOLooperManager myIOIOLooperManager;
    private DeviceManager mManager;


    public void SetDeviceManager(DeviceManager manager)
    {
        mManager = manager;
    }

    public DeviceManager GetDeviceManager()
    {
        return mManager;
    }

    public void SetLoopManager(MyIOIOLooperManager looperManager)
    {
        myIOIOLooperManager = looperManager;
    }

    public MyIOIOLooperManager GetLooperManager()
    {
        return myIOIOLooperManager;
    }


    public void SetCurrentSystem(ThreadSSPDevice system)
    {
        mPayoutSystem = system;
    }

    public ThreadSSPDevice GetCurrentSystem()
    {
        return mPayoutSystem;
    }




}
