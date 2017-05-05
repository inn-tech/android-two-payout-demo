package com.system.itl.ssp_multi_devices;


import java.lang.reflect.Array;
import java.util.ArrayList;

import device.itl.sspcoms.ItlCurrency;
import device.itl.sspcoms.ItlCurrencyValue;
import device.itl.sspcoms.SSPDevice;
import device.itl.sspcoms.SSPPayoutEvent;

class DeviceManager {


    private ArrayList<ThreadSSPDevice> sspDevices;
    private ArrayList<ThreadFlashes> ledFlashes;



    DeviceManager() {
        sspDevices = new ArrayList<>();
        ledFlashes = new ArrayList<>();
    }


    void Add(ThreadSSPDevice sspdev) {
        sspDevices.add(sspdev);
    }

    void Add(ThreadFlashes ledflash) {
        ledFlashes.add(ledflash);
    }

    /**
     * Start all threads
     */
    void StartAll() {
        /* LED devices */
        for (ThreadFlashes f : ledFlashes
                ) {
            f.start();
        }
        /* SSP Devices */
        for (ThreadSSPDevice s : sspDevices
                ) {
            s.start();
            s.RunSSPSystem();
            s.SSPDisable();
        }
    }


    /**
     * Stop all running threads and delete from lists
     */
    void CloseAll() {

        for (ThreadFlashes f : ledFlashes
                ) {
            f.Close();
        }

        for (ThreadSSPDevice s : sspDevices
                ) {
            s.StopSSPSystem();
            s.Close();
        }

        ledFlashes.clear();
        sspDevices.clear();
    }


    ArrayList<ThreadSSPDevice> GetSSPDevices() {
        return sspDevices;
    }

    ArrayList<ThreadFlashes> GetFlashDevices() {
        return ledFlashes;
    }


    private DeviceSetup GetSetup()
    {

        DeviceSetup setup = new DeviceSetup();

        for (ThreadSSPDevice dev : sspDevices
                ) {

            DeviceConnected con = new DeviceConnected(dev.GetSystemName(),dev.GetDevice().IsConnected());
            setup.DevConnections.add(con);
            // only add for connected devices
            if(con.Connected) {

                for (ItlCurrency cur : dev.GetDevice().currency
                        ) {

                    if (setup.PayoutCountries.size() == 0) {
                        setup.PayoutCountries.add(cur.country);
                    } else {
                        boolean cfound = false;
                        for (String s : setup.PayoutCountries
                                ) {
                            if (cur.country.equals(s)) {
                                cfound = true;
                            }
                        }
                        if (!cfound) {
                            setup.PayoutCountries.add(cur.country);
                        }
                    }

                    if (setup.MinPayoutValue.size() == 0) {
                        ItlCurrencyValue m1 = new ItlCurrencyValue();
                        m1.country = cur.country;
                        m1.value = (cur.storedLevel > 0 ? cur.value : 0);
                        setup.MinPayoutValue.add(m1);
                    } else {
                        boolean foundmin = false;
                        for (ItlCurrencyValue m : setup.MinPayoutValue
                                ) {
                            if (m.country.equals(cur.country)) {
                                foundmin = true;
                                if (cur.storedLevel > 0) {
                                    if (cur.value < m.value || m.value == 0) {
                                        m.value = cur.value;
                                    }
                                }
                            }
                        }
                        if (!foundmin) {
                            ItlCurrencyValue m2 = new ItlCurrencyValue();
                            m2.country = cur.country;
                            m2.value = (cur.storedLevel > 0 ? cur.value : 0);
                            setup.MinPayoutValue.add(m2);
                        }

                    }

                /* first item add */
                    if (setup.TotalStoredValue.size() == 0) {
                        ItlCurrencyValue v1 = new ItlCurrencyValue();
                        v1.country = cur.country;
                        v1.value = cur.value * cur.level;
                        setup.TotalStoredValue.add(v1);
                    } else {
                        boolean found = false;
                        // find exiting country code
                        for (ItlCurrencyValue v : setup.TotalStoredValue
                                ) {
                            if (v.country.equals(cur.country)) {
                                // add value
                                v.value += (cur.storedLevel * cur.value);
                                found = true;
                            }
                        }
                        // new code found - add value
                        if (!found) {
                            ItlCurrencyValue v2 = new ItlCurrencyValue();
                            v2.country = cur.country;
                            v2.value = (cur.storedLevel * cur.value);
                            setup.TotalStoredValue.add(v2);
                        }
                    }
                }
            }

        }

        return setup;
    }



    /* Notify events  */
    void NewSetupEvent(final SSPDevice sspDevice, final String tag) {

        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.mainActivity.DisplayNewSetup(GetSetup());
            }
        });

    }

    void NewComDisconnectEvent(final SSPDevice sspDevice, final String tag) {

        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.mainActivity.DisplayDisconnected(GetSetup());
            }
        });

    }

    void NewBillPayoutEvent(final SSPPayoutEvent ev, final String tag) {

        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.mainActivity.DisplayPayoutEvents(GetSetup());
            }
        });


    }

}
