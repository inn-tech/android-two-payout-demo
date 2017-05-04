package com.system.itl.ssp_multi_devices;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import device.itl.sspcoms.DeviceSetupListener;
import device.itl.sspcoms.ItlCurrency;
import device.itl.sspcoms.ItlCurrencyValue;
import device.itl.sspcoms.SSPDevice;
import device.itl.sspcoms.SSPPayoutEvent;
import device.itl.sspcoms.SSPSystem;
import ioio.lib.api.IOIO;
import ioio.lib.util.IOIOLooper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    static MainActivity mainActivity;
    TextView txtSP0;
    TextView txtSP1;






    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainActivity = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txtSP0 = (TextView)findViewById(R.id.txtSP0);
        txtSP0.setText("SP0 Waiting for connection...");
        txtSP0.setBackgroundResource(R.color.colorNotConnected);
        txtSP0.setOnClickListener(this);

        txtSP1 = (TextView)findViewById(R.id.txtSP1);
        txtSP1.setText("SP1 Waiting for connection...");
        txtSP1.setBackgroundResource(R.color.colorNotConnected);
        txtSP1.setOnClickListener(this);





        /* ask for permission to storeage read  */
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {


                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_STORAGE);
            }
        }


                /* start and run threads controlling devices */
        startService(new Intent(getBaseContext(),MyIOIOService.class));





        Button bttnGetCash = (Button)findViewById(R.id.bttnGetCash);
        bttnGetCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ItlCurrency cur = new ItlCurrency();
                cur.country = "USD";
                cur.value = 500;
                GetPayout("SP0").PayoutAmount(cur);
            }
        });


    }



    @Override
    public void onClick(View view) {

        Globals g = (Globals)getApplication();

        switch(view.getId()){
            case R.id.txtSP0:
                Intent intentSP0 = new Intent(this, DeviceConfiguration.class);
                g.SetCurrentSystem(GetPayout("SP0"));
                startActivity(intentSP0);
                break;
            case R.id.txtSP1:
                Intent intentSP1 = new Intent(this, DeviceConfiguration.class);
                g.SetCurrentSystem(GetPayout("SP1"));
                startActivity(intentSP1);
                break;
        }

    }


    private ThreadBankPayout GetPayout(String payoutName)
    {

        Globals g = (Globals)getApplication();
        MyIOIOLooperManager m =  g.GetLooperManager();

        return m.GetPayoutInstance(payoutName);

    }


    public void DisplayNewSetup(SSPDevice sspDevice, String deviceName)
    {

        ArrayList<ItlCurrencyValue> storedvalue;
        ArrayList<ItlCurrencyValue> minPayoutValue;


        switch(deviceName){
            case "SP0":
                storedvalue = GetPayout("SP0").GetTotalStoredValue();
                minPayoutValue = GetPayout("SP0").GetMinimumPayout();
                txtSP0.setBackgroundResource(R.color.colorConnected);
                txtSP0.setText("SP0: " + sspDevice.type.toString() + " connected " +  sspDevice.shortDatasetVersion  + "\r\n");
                txtSP0.append("Stored value: " + storedvalue.get(0).country + " " + String.format("%.2f",storedvalue.get(0).realValue) + " ");
                txtSP0.append("Min value: " + minPayoutValue.get(0).country + " " + String.format("%.2f",minPayoutValue.get(0).realValue) + "\r\n");
                break;
            case "SP1":
                storedvalue = GetPayout("SP1").GetTotalStoredValue();
                minPayoutValue = GetPayout("SP1").GetMinimumPayout();
                txtSP1.setBackgroundResource(R.color.colorConnected);
                txtSP1.setText("SP1: " + sspDevice.type.toString() + " connected " +  sspDevice.shortDatasetVersion  + "\r\n");
                txtSP1.append("Stored value: " + storedvalue.get(0).country + " " + String.format("%.2f",storedvalue.get(0).realValue) + " ");
                txtSP1.append("Min value: " + minPayoutValue.get(0).country + " " + String.format("%.2f",minPayoutValue.get(0).realValue)  + "\r\n");
                break;
        }

    }


    public  void DisplayDisconnected(SSPDevice sspDevice, String deviceName)
    {
        switch(deviceName){
            case "SP0":
                txtSP1.setBackgroundResource(R.color.colorNotConnected);
                txtSP1.setText("SP0 disconnected\r\n");
                break;
            case "SP1":
                txtSP1.setBackgroundResource(R.color.colorNotConnected);
                txtSP1.setText("SP1 disconnected\r\n");
                break;
        }

    }


    public  void DisplayPayoutEvents(SSPPayoutEvent ev, String deviceName) {


        ArrayList<ItlCurrencyValue> storedvalue;
        ArrayList<ItlCurrencyValue> minPayoutValue;
        String deviceType;


        switch (ev.event) {
            case CashPaidOut:

                break;
            case CashStoreInPayout:

                break;
            case CashLevelsChanged:

                switch(deviceName){
                    case "SP0":
                        storedvalue = GetPayout("SP0").GetTotalStoredValue();
                        minPayoutValue = GetPayout("SP0").GetMinimumPayout();
                        deviceType = GetPayout("SP0").GetDeviceType();
                        txtSP0.setText("SP0: " + deviceType + " connected " +  deviceType + "\r\n");
                        txtSP0.append("Stored value: " + storedvalue.get(0).country + " " + String.format("%.2f",storedvalue.get(0).realValue) + " ");
                        txtSP0.append("Min value: " + minPayoutValue.get(0).country + " " + String.format("%.2f",minPayoutValue.get(0).realValue) + "\r\n");
                        break;
                    case "SP1":
                        storedvalue = GetPayout("SP1").GetTotalStoredValue();
                        minPayoutValue = GetPayout("SP1").GetMinimumPayout();
                        deviceType = GetPayout("SP1").GetDeviceType();
                        txtSP1.setText("SP1: " + deviceType + " connected " +  deviceType + "\r\n");
                        txtSP1.append("Stored value: " + storedvalue.get(0).country + " " + String.format("%.2f",storedvalue.get(0).realValue) + " ");
                        txtSP1.append("Min value: " + minPayoutValue.get(0).country + " " + String.format("%.2f",minPayoutValue.get(0).realValue)  + "\r\n");
                        break;
                }

                break;
            case PayoutStarted:

                break;
            case PayoutEnded:

                break;
            case PayinStarted:

                break;
            case PayinEnded:

                break;
            case EmptyStarted:

                break;
            case EmptyEnded:

                break;
            case PayoutConfigurationFail:
                //TODO handle config failures
                break;
            case PayoutAmountInvalid:

                break;
            case PayoutRequestFail:
                //TODO handle this
                break;

            case RouteChanged:


                break;
            case PayoutDeviceNotConnected:

                break;
            case PayoutDeviceEmpty:

                break;
            case PayoutDeviceDisabled:

                break;

        }
    }



    @Override
    public void onDestroy() {

        stopService(new Intent(getBaseContext(), MyIOIOService.class));

        super.onDestroy();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.action_settings:
                break;
            case R.id.action_start_refill:
                GetPayout("SP0").SSPEnable();
                GetPayout("SP1").SSPEnable();
                break;
            case R.id.action_stop_refill:
                GetPayout("SP0").SSPDisable();
                GetPayout("SP1").SSPDisable();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
