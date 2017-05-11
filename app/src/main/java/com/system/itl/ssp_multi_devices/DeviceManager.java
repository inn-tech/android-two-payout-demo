package com.system.itl.ssp_multi_devices;


import java.util.ArrayList;
import java.util.Collections;

import device.itl.sspcoms.ItlCurrency;
import device.itl.sspcoms.ItlCurrencyValue;
import device.itl.sspcoms.PayoutRoute;
import device.itl.sspcoms.SSPDevice;
import device.itl.sspcoms.SSPPayoutEvent;

import static device.itl.sspcoms.SSPPayoutEvent.PayoutEvent.CashPaidIn;
import static device.itl.sspcoms.SSPPayoutEvent.PayoutEvent.PayinEnded;
import static device.itl.sspcoms.SSPPayoutEvent.PayoutEvent.PayoutEnded;

class DeviceManager {


    private enum SystemPayMode{
        Idle,
        PayIn,
        PayOut,
        Refill,
    }


    private ArrayList<ThreadSSPDevice> sspDevices;
    private ArrayList<ThreadFlashes> ledFlashes;
    private DeviceValue payoutRequestStatus;
    private DeviceValue payinRequestStatus;
    private SystemPayMode payMode;


    DeviceManager() {
        sspDevices = new ArrayList<>();
        ledFlashes = new ArrayList<>();

        payoutRequestStatus = new DeviceValue(2);
        payinRequestStatus = new DeviceValue(2);
        payMode = SystemPayMode.Idle;

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


    void RefillMode(boolean enable)
    {

        for (ThreadSSPDevice s: sspDevices
             ) {
            if(enable){
                payMode = SystemPayMode.Refill;
                s.SSPEnable();
            }else{
                payMode = SystemPayMode.Idle;
                s.SSPDisable();
            }
        }
    }


    /**
     *
     * @param curbuy
     * @return
     */
    boolean BuyItem(final ItlCurrency curbuy)
    {
        payinRequestStatus.country = curbuy.country;
        payinRequestStatus.requestedValue = curbuy.value;
        payinRequestStatus.paidValue[0] = 0;
        payinRequestStatus.paidValue[1] = 0;

        payMode = SystemPayMode.PayIn;
        // enable devices
        for (ThreadSSPDevice dev : sspDevices
                ) {
            dev.SSPEnable();
        }



        return true;
    }


    /**
     * Select payouts for requested values
     * @param curpayout
     * @return boolean value true for success, false for failure
     */
     boolean PayoutAmount(final ItlCurrency curpayout)
    {

        payoutRequestStatus.country = curpayout.country;
        payoutRequestStatus.requestedValue = curpayout.value;
        payoutRequestStatus.paidValue[0] = 0;
        payoutRequestStatus.paidValue[1] = 0;

        payMode = SystemPayMode.PayOut;
        // get all stored levels from both payouts
        ArrayList<ItlCurrency> payLevels = new ArrayList<>();
        for (ThreadSSPDevice payout: sspDevices
                ) {
            // ignore not connected devices
            if(payout.GetDevice().IsConnected()) {
                for (ItlCurrency cur : payout.GetDeviceCurrencyList()
                        ) {
                    if (cur.route == PayoutRoute.PayoutStore) {
                        ItlCurrency c = new ItlCurrency();
                        c.country = cur.country;
                        c.value = cur.value;
                        c.storedLevel = cur.storedLevel;
                        c.deviceTag = payout.GetSystemName();
                        payLevels.add(c);
                    }
                }
            }
        }
        // sort with highest value first
        Collections.sort(payLevels, new CustomListComparator());

        //calculate
        int amt = curpayout.value;
        for (ItlCurrency st: payLevels
                ) {
            if(st.storedLevel > 0) {
                int levPay = amt / st.value;
                if (levPay > 0) {
                    if(st.storedLevel >= levPay) {
                        st.level = levPay;
                        amt -= levPay * st.value;
                        st.storedLevel -= levPay;
                    }else{
                        amt -= st.storedLevel * st.value;
                        st.level = st.storedLevel;
                        st.storedLevel = 0;
                    }
                } else {
                    st.level = 0;
                }
                if (amt == 0){
                    break;
                }
            }

        }
        // payout not possible
        if(amt != 0){
            return false;
        }

        ItlCurrency cPay1 = new ItlCurrency();
        cPay1.country = curpayout.country;
        cPay1.value = 0;
        cPay1.deviceTag = "SP0";
        ItlCurrency cPay2 = new ItlCurrency();
        cPay2.country = curpayout.country;
        cPay2.value = 0;
        cPay2.deviceTag = "SP1";

        for (ItlCurrency cur: payLevels
             ) {
            if(cur.deviceTag.equals("SP0")){
                cPay1.value += cur.value * cur.level;
            }else{
                cPay2.value += cur.value * cur.level;
            }

        }

        for (ThreadSSPDevice dev: sspDevices
             ) {
            if(dev.GetSystemName().equals("SP0") && cPay1.value > 0){
                dev.PayoutAmount(cPay1);
            }
            if(dev.GetSystemName().equals("SP1") && cPay2.value > 0){
                dev.PayoutAmount(cPay2);
            }

        }


        return true;

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

        // update the main status display
        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MainActivity.mainActivity.DisplaySystemStatus(GetSetup());
            }
        });


        switch (ev.event) {
            case CashPaidOut:
                MainActivity.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(tag.equals("SP0")) {
                            payoutRequestStatus.paidValue[0] = ev.value;
                        }
                        if(tag.equals("SP1")){
                            payoutRequestStatus.paidValue[1] = ev.value;
                        }
                        MainActivity.mainActivity.UpdatePayoutStatus(ev.event,payoutRequestStatus);
                    }
                });
                break;
            case CashStoreInPayout:

                break;
            case CashLevelsChanged:


                break;
            case PayoutStarted:
                MainActivity.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.mainActivity.UpdatePayoutStatus(ev.event,payoutRequestStatus);
                    }
                });
                break;
            case PayoutEnded:
                // have we paid total from both
                if(payoutRequestStatus.paidValue[0] +
                        payoutRequestStatus.paidValue[1] == payoutRequestStatus.requestedValue) {
                    MainActivity.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mainActivity.UpdatePayoutStatus(PayoutEnded, payoutRequestStatus);
                        }
                    });
                }
                // disable after payout
                for (ThreadSSPDevice s: sspDevices
                        ) {
                    if(s.GetSystemName().equals(tag)){
                        s.SSPDisable();
                    }
                }
                break;
            case PayinStarted:
                MainActivity.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.mainActivity.UpdatePayinStatus(ev.event,payinRequestStatus);
                    }
                });
                break;
            case PayinEnded:
                /*
                // have we paid total from both
                if(tag.equals("SP0")) {
                    payoutRequestStatus.paidValue[0] += ev.value;
                }
                if(tag.equals("SP1")){
                    payoutRequestStatus.paidValue[1] += ev.value;
                }
                MainActivity.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.mainActivity.UpdatePayinStatus(CashPaidIn,payoutRequestStatus);
                    }
                });
                if(payoutRequestStatus.paidValue[0] +
                        payoutRequestStatus.paidValue[1] >= payoutRequestStatus.requestedValue) {
                    MainActivity.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mainActivity.UpdatePayoutStatus(PayoutEnded, payoutRequestStatus);
                        }
                    });
                    // disable after payout
                    for (ThreadSSPDevice s: sspDevices
                            ) {
                        if(s.GetSystemName().equals(tag)){
                            s.SSPDisable();
                        }
                    }

                }
                */
                break;
            case CashPaidIn:
                if(tag.equals("SP0")) {
                    payinRequestStatus.paidValue[0] += ev.value;
                }
                if(tag.equals("SP1")){
                    payinRequestStatus.paidValue[1] += ev.value;
                }
                MainActivity.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.mainActivity.UpdatePayinStatus(CashPaidIn,payinRequestStatus);
                    }
                });
                // have we paid >= requested value
                if(payMode == SystemPayMode.PayIn && payinRequestStatus.GetPaidTotal() >= payinRequestStatus.requestedValue) {
                    MainActivity.mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.mainActivity.UpdatePayinStatus(PayinEnded, payinRequestStatus);
                        }
                    });
                    // disable after payout for all bills in
                    for (ThreadSSPDevice s: sspDevices
                            ) {
                            s.SSPDisable();
                    }

                    if(payinRequestStatus.GetPaidTotal() > payinRequestStatus.requestedValue){
                        final ItlCurrency change = new ItlCurrency();
                        change.country = payinRequestStatus.country;
                        change.value = payinRequestStatus.GetPaidTotal() - payinRequestStatus.requestedValue;
                        PayoutAmount(change);
                    }
                }
                break;
            case EmptyStarted:

                break;
            case EmptyEnded:

                break;
            case PayoutConfigurationFail:
                //TODO handle config failures
                break;
            case PayoutAmountInvalid:
                MainActivity.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.mainActivity.ShowAlert("Unable to pay request " + ev.country + " " + String.format("%.2f",(ev.realvalueRequested)));
                    }
                });
                break;
            case PayoutRequestFail:
                MainActivity.mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.mainActivity.ShowAlert("Unable to pay request " + ev.country + " " + String.format("%.2f",(ev.realvalueRequested)));
                    }
                });
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

}
