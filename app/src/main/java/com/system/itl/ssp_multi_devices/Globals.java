package com.system.itl.ssp_multi_devices;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import device.itl.sspcoms.SSPSystem;


public class Globals extends Application {

    private ThreadBankPayout mPayoutSystem;
    private MyIOIOLooperManager myIOIOLooperManager;


    public void SetLoopManager(MyIOIOLooperManager looperManager)
    {
        myIOIOLooperManager = looperManager;
    }

    public MyIOIOLooperManager GetLooperManager()
    {
        return myIOIOLooperManager;
    }


    public void SetCurrentSystem(ThreadBankPayout system)
    {
        mPayoutSystem = system;
    }

    public ThreadBankPayout GetCurrentSystem()
    {
        return mPayoutSystem;
    }

    /*
    private SSPSystem data;

    private String threadBankName;


    public SSPSystem getDeviceData(){
        return this.data;
    }

    public void setDeviceData(SSPSystem d){
        this.data=d;
    }


    public String getThreadBankName(){

        return threadBankName;
    }

    public void setThreadBankName(String name){
        threadBankName = name;
    }
    */

}
