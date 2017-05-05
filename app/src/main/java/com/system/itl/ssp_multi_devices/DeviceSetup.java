package com.system.itl.ssp_multi_devices;



import java.util.ArrayList;

import device.itl.sspcoms.ItlCurrencyValue;
import device.itl.sspcoms.SSPDevice;


class DeviceSetup {




    ArrayList<ItlCurrencyValue> TotalStoredValue;
    ArrayList<ItlCurrencyValue> MinPayoutValue;
    ArrayList<String> PayoutCountries;
    ArrayList<DeviceConnected> DevConnections;



    DeviceSetup(){
        TotalStoredValue = new ArrayList<>();
        MinPayoutValue = new ArrayList<>();
        PayoutCountries = new ArrayList<>();
        DevConnections = new ArrayList<>();
    }



}
