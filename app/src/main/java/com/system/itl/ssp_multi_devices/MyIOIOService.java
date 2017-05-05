package com.system.itl.ssp_multi_devices;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;

/**
 * Created by Programmer on 16/3/2560.
 */

public class MyIOIOService extends IOIOService {

    private MyIOIOLooperManager myIOIOLooperManager;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        myIOIOLooperManager.onDestroy();
        Log.e("MyIOIOService", "myIOIOLooperManager.onDestroy();");

        stopSelf();
        Log.e("MyIOIOService", "stopSelf();");

        super.onDestroy();
    }

    @Override
    protected IOIOLooper createIOIOLooper() {

        if(myIOIOLooperManager == null){
            myIOIOLooperManager = new MyIOIOLooperManager();
            Globals g = (Globals)getApplication();
            g.SetLoopManager(myIOIOLooperManager);
            return myIOIOLooperManager;

        }else {
            return myIOIOLooperManager;
        }
    }




}
