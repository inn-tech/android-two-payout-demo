package com.system.itl.ssp_multi_devices;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import device.itl.sspcoms.ItlCurrency;
import device.itl.sspcoms.ItlCurrencyValue;
import device.itl.sspcoms.SSPDevice;
import device.itl.sspcoms.SSPPayoutEvent;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    static MainActivity mainActivity;
    TextView txtHeader1;
    TextView txtSP0Status;
    TextView txtSP1Status;

    private String m_DeviceCountry;
    private String[] pickerValuesSP0;
    private String[] pickerValuesSP1;




    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainActivity = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        txtHeader1 = (TextView)findViewById(R.id.txtSP0);
        txtHeader1.setText("");
        txtHeader1.setBackgroundResource(R.color.colorNotConnected);
        txtHeader1.setOnClickListener(this);

        txtSP0Status = (TextView)findViewById(R.id.txtSP0Status);
        txtSP0Status.setText("SP0..");
        txtSP0Status.setBackgroundResource(R.color.colorNotConnected);
        txtSP0Status.setOnClickListener(this);


        txtSP1Status = (TextView)findViewById(R.id.txtSP1Status);
        txtSP1Status.setText("SP1..");
        txtSP1Status.setBackgroundResource(R.color.colorNotConnected);
        txtSP1Status.setOnClickListener(this);




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


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter cash request " + m_DeviceCountry);

                final EditText txt = new EditText(mainActivity);
                txt.setInputType(InputType.TYPE_CLASS_NUMBER);
                txt.setPadding(20,45,20,45);
                txt.setGravity(Gravity.CENTER);
                txt.requestFocus();
                builder.setView(txt);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        ItlCurrency curpay = new ItlCurrency();
                        curpay.country = m_DeviceCountry;
                        curpay.value = Integer.valueOf(txt.getText().toString()) * 100;
                        Globals g = (Globals)getApplication();
                        MyIOIOLooperManager m =  g.GetLooperManager();
                        m.GetManagerInstance().PayoutAmount(curpay);

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });


    }



    @Override
    public void onClick(View view) {

        Globals g = (Globals)getApplication();

        switch(view.getId()){
            case R.id.txtSP0Status:
                Intent intentSP0 = new Intent(this, DeviceConfiguration.class);
                g.SetCurrentSystem(GetPayout("SP0"));
                startActivity(intentSP0);
                break;
            case R.id.txtSP1Status:
                Intent intentSP1 = new Intent(this, DeviceConfiguration.class);
                g.SetCurrentSystem(GetPayout("SP1"));
                startActivity(intentSP1);
                break;
        }

    }


    private ThreadSSPDevice GetPayout(String payoutName)
    {

        Globals g = (Globals)getApplication();
        MyIOIOLooperManager m =  g.GetLooperManager();

        return m.GetPayoutInstance(payoutName);

    }


    public void DisplayNewSetup(DeviceSetup setup)
    {
        for (DeviceConnected con: setup.DevConnections
             ) {
            if(con.Connected){
                m_DeviceCountry = setup.PayoutCountries.get(0);
            }
        }
        UpdateHeaderDisplay(setup);

    }


    public void UpdateHeaderDisplay(DeviceSetup setup)
    {
        txtHeader1.setBackgroundResource(R.color.colorConnected);
        if(setup.TotalStoredValue.size() > 0) {
            txtHeader1.setText("Total Value: " + setup.TotalStoredValue.get(0).country + " " + String.valueOf(setup.TotalStoredValue.get(0).value) + "\r\n");
            txtHeader1.append("Min Value: " + String.valueOf(setup.MinPayoutValue.get(0).value));
        }


        for (DeviceConnected con: setup.DevConnections
             ) {
            switch (con.Tag){
                case "SP0":
                    if(con.Connected){
                        txtSP0Status.setBackgroundResource(R.color.colorConnected);
                    }else{
                        txtSP0Status.setBackgroundResource(R.color.colorNotConnected);
                    }
                    break;
                case "SP1":
                    if(con.Connected){
                        txtSP1Status.setBackgroundResource(R.color.colorConnected);
                    }else{
                        txtSP1Status.setBackgroundResource(R.color.colorNotConnected);
                    }
                    break;
            }
        }

    }



    public void DisplayDisconnected(DeviceSetup setup)
    {
        UpdateHeaderDisplay(setup);
    }


    public void DisplayPayoutEvents(DeviceSetup setup) {

        UpdateHeaderDisplay(setup);


        /*

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
                        txtHeader1.setText("SP0: " + deviceType + " connected " +  m_DeviceCountry + "\r\n");
                        txtHeader1.append("Stored value: " + storedvalue.get(0).country + " " + String.format("%.2f",storedvalue.get(0).realValue) + " ");
                        txtHeader1.append("Min value: " + minPayoutValue.get(0).country + " " + String.format("%.2f",minPayoutValue.get(0).realValue) + "\r\n");
                        break;
                    case "SP1":
                        storedvalue = GetPayout("SP1").GetTotalStoredValue();
                        minPayoutValue = GetPayout("SP1").GetMinimumPayout();
                        deviceType = GetPayout("SP1").GetDeviceType();
                        txtSP0Status.setText("SP1: " + deviceType + " connected " +  m_DeviceCountry + "\r\n");
                        txtSP0Status.append("Stored value: " + storedvalue.get(0).country + " " + String.format("%.2f",storedvalue.get(0).realValue) + " ");
                        txtSP0Status.append("Min value: " + minPayoutValue.get(0).country + " " + String.format("%.2f",minPayoutValue.get(0).realValue)  + "\r\n");
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
                ShowAlert("Unable to pay request " + ev.country + " " + String.format("%.2f",(ev.realvalueRequested)));
                break;
            case PayoutRequestFail:
                ShowAlert("Unable to pay request " + ev.country + " " + String.format("%.2f",(ev.realvalueRequested)));
                break;

            case RouteChanged:


                break;
            case PayoutDeviceNotConnected:

                break;
            case PayoutDeviceEmpty:

                break;
            case PayoutDeviceDisabled:

                break;

        } */
    }


    private void ShowAlert(String message)
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.show();

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
