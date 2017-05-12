package com.system.itl.ssp_multi_devices;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import device.itl.sspcoms.ItlCurrency;
import device.itl.sspcoms.SSPPayoutEvent;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    static MainActivity mainActivity;
    TextView txtHeader1;
    TextView txtSP0Status;
    TextView txtSP1Status;
    TextView txtBuy1;
    TextView txtBuy2;
    TextView txtBuy3;
    TextView refillStatus;
    TextView txtBuyStatus;
    Dialog dialogBuy = null;

    private String m_DeviceCountry;
    private int[] vendValues;




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
        txtSP0Status.setText("");
        txtSP0Status.setBackgroundResource(R.color.colorNotConnected);

        txtSP1Status = (TextView)findViewById(R.id.txtSP1Status);
        txtSP1Status.setText("");
        txtSP1Status.setBackgroundResource(R.color.colorNotConnected);

        vendValues = new int[] {5,10,20};

        txtBuy1 = (TextView)findViewById(R.id.txtBuy1);
        txtBuy1.setText(String.valueOf(vendValues[0]) + ".00");
        txtBuy2 = (TextView)findViewById(R.id.txtBuy2);
        txtBuy2.setText(String.valueOf(vendValues[1]) + ".00");
        txtBuy3 = (TextView)findViewById(R.id.txtBuy3);
        txtBuy3.setText(String.valueOf(vendValues[2]) + ".00");

        Button bttnBuy1 = (Button)findViewById(R.id.bttnBuy1);
        bttnBuy1.setOnClickListener(this);
        Button bttnBuy2 = (Button)findViewById(R.id.bttnBuy2);
        bttnBuy2.setOnClickListener(this);
        Button bttnBuy3 = (Button)findViewById(R.id.bttnBuy3);
        bttnBuy3.setOnClickListener(this);

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
                        curpay.realvalue = curpay.value/100;
                        Globals g = (Globals)getApplication();
                        MyIOIOLooperManager m =  g.GetLooperManager();
                        if(!m.GetManagerInstance().PayoutAmount(curpay)){
                            ShowAlert("Unable to pay request " + curpay.country + " " + String.format("%.2f",(curpay.realvalue)));
                        }

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
        MyIOIOLooperManager m0 =  g.GetLooperManager();
        ItlCurrency curbuy = new ItlCurrency();

        switch(view.getId()){
            case R.id.bttnBuy1:
                curbuy.country = m_DeviceCountry;
                curbuy.value = vendValues[0] * 100;
                ShowBuyDialog(curbuy);
                break;
            case R.id.bttnBuy2:
                curbuy.country = m_DeviceCountry;
                curbuy.value = vendValues[1] * 100;
                ShowBuyDialog(curbuy);
                break;
            case R.id.bttnBuy3:
                curbuy.country = m_DeviceCountry;
                curbuy.value = vendValues[2] * 100;
                ShowBuyDialog(curbuy);
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


        LinearLayout spLayout = (LinearLayout)findViewById(R.id.content_smart_payout);
        spLayout.setVisibility(View.VISIBLE);
        LinearLayout mLayout = (LinearLayout)findViewById(R.id.content_main);
        mLayout.setVisibility(View.INVISIBLE);


    }


    public void UpdatePayoutStatus(SSPPayoutEvent.PayoutEvent ev, DeviceValue payStatus)
    {
        switch(ev)
        {
            case PayoutStarted:
                txtSP0Status.setText("Requested payout " + String.format("%.2f",(double)payStatus.GetRealRequestedValue()));
            break;
            case PayoutEnded:
                txtSP1Status.setText("Pay complete " + String.format("%.2f",payStatus.GetPaidTotalRealValue()));
            break;
            case CashPaidOut:
                txtSP1Status.setText("Paid " + String.format("%.2f",payStatus.GetPaidTotalRealValue()));
                break;
        }
    }


    public void UpdatePayinStatus(SSPPayoutEvent.PayoutEvent ev, DeviceValue payStatus)
    {
        switch(ev)
        {
            case PayinStarted:
                txtSP0Status.setText("Pay value " + String.format("%.2f",payStatus.GetRealRequestedValue()));
                break;
            case PayinEnded:
                txtSP1Status.setText("Pay in complete " + String.format("%.2f",payStatus.GetPaidTotalRealValue()));
                break;
            case CashPaidIn:
                txtSP1Status.setText("Paid " + String.format("%.2f",payStatus.GetPaidTotalRealValue()));
                break;
        }
    }





    public void UpdateHeaderDisplay(DeviceSetup setup)
    {
        txtHeader1.setBackgroundResource(R.color.colorConnected);
        if(setup.TotalStoredValue.size() > 0) {
            txtHeader1.setText("Total Value: " + setup.TotalStoredValue.get(0).country + " " + String.format("%.2f",setup.TotalStoredValue.get(0).realValue) + "\r\n");
            txtHeader1.append("Min Value: " + String.format("%.2f",setup.MinPayoutValue.get(0).realValue));

            if(refillStatus != null){
                refillStatus.setText("Total Value: " + setup.TotalStoredValue.get(0).country + " " + String.format("%.2f",setup.TotalStoredValue.get(0).realValue));
            }

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


    public void UpdateBuyStatus(DeviceValue value)
    {

        if(dialogBuy != null){
            txtBuyStatus.setText("Paid: " + value.country + " " +
                    String.format("%.2f",value.GetPaidTotalRealValue()));
            // all paid, close dialog
            if(value.GetPaidTotal() >= value.requestedValue){
                dialogBuy.dismiss();
            }
        }

    }


    public void DisplayDisconnected(DeviceSetup setup)
    {
        UpdateHeaderDisplay(setup);
    }


    public void DisplaySystemStatus(DeviceSetup setup) {

        UpdateHeaderDisplay(setup);

    }


    public void ShowAlert(String message)
    {

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
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
        Globals g = (Globals)getApplication();
        final MyIOIOLooperManager m =  g.GetLooperManager();


        switch(id){
            case R.id.action_settings:
                break;

            case R.id.action_sp0:
                Intent intentSP0 = new Intent(this, DeviceConfiguration.class);
                g.SetCurrentSystem(GetPayout("SP0"));
                startActivity(intentSP0);
                break;
            case R.id.action_sp1:
                Intent intentSP1 = new Intent(this, DeviceConfiguration.class);
                g.SetCurrentSystem(GetPayout("SP1"));
                startActivity(intentSP1);
                break;

            case R.id.action_start_refill:

                ShowRefillDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void ShowRefillDialog() {

        Globals g = (Globals)getApplication();
        final MyIOIOLooperManager m =  g.GetLooperManager();

        m.GetManagerInstance().RefillMode(true);
        final Dialog dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_refill);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);

        Button yes = (Button) dialog.findViewById(R.id.bttnStopRefill);
        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                m.GetManagerInstance().RefillMode(false);
                dialog.dismiss();

            }
        });

        refillStatus =(TextView) dialog.findViewById(R.id.txtRefill);
        refillStatus.setText("Enter your refill bills...");

        dialog.show();
    }


    private void ShowBuyDialog(ItlCurrency buy) {

        Globals g = (Globals)getApplication();
        final MyIOIOLooperManager m =  g.GetLooperManager();

        m.GetManagerInstance().RefillMode(true);
        final Dialog dialog = new Dialog(this,
                android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_refill);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        dialog.getWindow().setAttributes(lp);

        Button yes = (Button) dialog.findViewById(R.id.bttnStopRefill);
        yes.setText("Cancel");
        yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                m.GetManagerInstance().CancelBuy();
                dialog.dismiss();
                dialogBuy = null;
            }
        });

        txtBuyStatus =(TextView) dialog.findViewById(R.id.txtRefill);
        txtBuyStatus.setText("Enter payment: " + buy.country + " " + String.format("%.2f",(double)buy.value/100));

        dialogBuy = dialog;

        dialog.show();
        m.GetManagerInstance().BuyItem(buy);
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
