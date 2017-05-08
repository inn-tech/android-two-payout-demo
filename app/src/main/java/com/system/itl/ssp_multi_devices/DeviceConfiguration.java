package com.system.itl.ssp_multi_devices;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;



import device.itl.sspcoms.ItlCurrency;
import device.itl.sspcoms.PayoutRoute;
import device.itl.sspcoms.SSPDevice;

import static com.system.itl.ssp_multi_devices.Constants.FIRST_COLUMN;
import static com.system.itl.ssp_multi_devices.Constants.SECOND_COLUMN;
import static com.system.itl.ssp_multi_devices.Constants.THIRD_COLUMN;
;import static com.system.itl.ssp_multi_devices.Constants.FOURTH_COLUMN;


public class DeviceConfiguration extends AppCompatActivity {


    ListView lstDevice;
    TextView txtDevice;
   // SSPSystem sspSystem;
 //   SSPDevice sspDevice;
    ThreadSSPDevice payoutSystem;
    ArrayList<HashMap<String, String>> list;
    ArrayList<ItlCurrency> routeUpdate;
    Button bttnApply;
    ListViewAdapter adapter;
    String currentBankName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_configuration);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Globals g = (Globals)getApplication();
        payoutSystem = g.GetCurrentSystem();
        routeUpdate = new ArrayList<>();
        bttnApply = (Button)findViewById(R.id.bttnApplyRoutes);


        // set change routes to current
        for (ItlCurrency itlCurrency : payoutSystem.GetDeviceCurrencyList()) {
            itlCurrency.newRoute = itlCurrency.route;
        }
        UpdateDisplay();

        lstDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(i >= 1) {
                    // get selected denomination and change route over
                    ItlCurrency r_cur = payoutSystem.GetDeviceCurrencyList().get(i - 1);
                    if (r_cur.newRoute == PayoutRoute.PayoutStore) {
                        r_cur.newRoute = PayoutRoute.Cashbox;
                    } else {
                        r_cur.newRoute = PayoutRoute.PayoutStore;
                    }
                    // set or reset flag depending on route change from current
                    if(r_cur.newRoute.Compare(r_cur.route.getValue())){
                        r_cur.routeChangeRequest = false;
                    }else{
                        r_cur.routeChangeRequest = true;
                    }
                    UpdateDisplay();
                }
                // show or hide apply button if there are changes to be made
                boolean showApply = false;
                for (ItlCurrency cur: payoutSystem.GetDeviceCurrencyList()
                     ) {
                    if(cur.routeChangeRequest) {
                        showApply = true;
                        break;
                    }
                }
                if(showApply){
                    bttnApply.setVisibility(View.VISIBLE);
                }else{
                    bttnApply.setVisibility(View.INVISIBLE);
                }

            }

        });

        //Apply changes button
        bttnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveRoutesToPreferences(payoutSystem.GetSystemName());
                // set new route change queue
                switch (payoutSystem.GetSystemName()){
                    case "SP0":
                        payoutSystem.SetRoutes();
                        break;
                    case "SP1":
                        payoutSystem.SetRoutes();
                        break;
                }

                finish();
            }
        });


    }


    private void SaveRoutesToPreferences(String systemName)
    {
        SharedPreferences routes = getSharedPreferences(systemName + "_Routes", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = routes.edit();

        for (ItlCurrency cur : payoutSystem.GetDevice().currency
             ) {
            editor.putString(cur.country + " " + String.valueOf(cur.value), cur.newRoute.toString() );
            editor.apply();
        }

    }


    public void UpdateDisplay()
    {


        txtDevice = (TextView)findViewById(R.id.txtDevice);
        lstDevice = (ListView)findViewById(R.id.listDevice);

        SSPDevice sspDevice = payoutSystem.GetDevice();

        txtDevice.setText("SP: " + sspDevice.type.toString() + "\r\n");
        txtDevice.append("Type: " + sspDevice.headerType.toString() + "\r\n");
        txtDevice.append("Firmware: " + sspDevice.firmwareVersion + "\r\n");
        txtDevice.append("Dataset: " + sspDevice.datasetVersion + "\r\n");
        txtDevice.append("Serial number: " + String.valueOf(sspDevice.serialNumber) + "\r\n");

        //display the channel info
        list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> hdr = new HashMap<String, String>();
        hdr.put(FIRST_COLUMN, "Code");
        hdr.put(SECOND_COLUMN, "Value");
        hdr.put(THIRD_COLUMN, "Stored");
        hdr.put(FOURTH_COLUMN, "Route");
        list.add(hdr);
        double totalValue = 0.0;
        for (ItlCurrency itlCurrency : payoutSystem.GetDeviceCurrencyList()) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp.put(FIRST_COLUMN, itlCurrency.country);
            temp.put(SECOND_COLUMN, String.valueOf(String.format("%.2f", itlCurrency.realvalue)));
            temp.put(THIRD_COLUMN, String.valueOf(itlCurrency.storedLevel));
            double vl = itlCurrency.realvalue * (double) itlCurrency.storedLevel;
            // display request route change if set, otherwise disply current route
            if(itlCurrency.newRoute != PayoutRoute.RouteNone){
                temp.put(FOURTH_COLUMN, itlCurrency.newRoute.toString());
            }else {
                temp.put(FOURTH_COLUMN, itlCurrency.route.toString());
            }
            list.add(temp);
            totalValue += vl;
        }
        HashMap<String, String> tot = new HashMap<String, String>();
        tot.put(FIRST_COLUMN, "");
        tot.put(SECOND_COLUMN, "Total");
        tot.put(THIRD_COLUMN, String.format("%.2f", totalValue));
        tot.put(FOURTH_COLUMN, "");
        list.add(tot);

        adapter =new ListViewAdapter(this, list);
        lstDevice.setAdapter(adapter);

    }


    @Override
    public void onBackPressed() {
        // reset route changes for cancelled update
        for (ItlCurrency itlCurrency : payoutSystem.GetDeviceCurrencyList()) {
            itlCurrency.newRoute = PayoutRoute.RouteNone;
        }

        super.onBackPressed();
    }


}
