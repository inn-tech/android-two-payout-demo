package com.system.itl.ssp_multi_devices;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import device.itl.sspcoms.BarCodeReader;
import device.itl.sspcoms.DeviceEvent;
import device.itl.sspcoms.DeviceEventListener;
import device.itl.sspcoms.DeviceFileUpdateListener;
import device.itl.sspcoms.DevicePayoutEventListener;
import device.itl.sspcoms.DeviceSetupListener;
import device.itl.sspcoms.ItlCurrency;
import device.itl.sspcoms.ItlCurrencyValue;
import device.itl.sspcoms.PayoutRoute;
import device.itl.sspcoms.SSPComsConfig;
import device.itl.sspcoms.SSPDevice;
import device.itl.sspcoms.SSPPayoutEvent;
import device.itl.sspcoms.SSPSystem;
import device.itl.sspcoms.SSPUpdate;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;

class ThreadSSPDevice extends Thread implements  DeviceSetupListener, DeviceEventListener, DeviceFileUpdateListener,DevicePayoutEventListener {

    private Uart uartBankSP0;
    private InputStream inputStreamBankSP0;
    private OutputStream outputStreamBankSP0;

    private IOIO ioio = null;

    private final int READBUF_SIZE = 256;
    private int WRITEBUF_SIZE = 4096;
    private byte[] rbuf = new byte[READBUF_SIZE];
    private byte[] wbuf = new byte[WRITEBUF_SIZE];

    private SSPSystem ssp = null;
    private boolean setupRun = false;
    private int uartTx;
    private int uartRx;
    private boolean isRunning;
    private DeviceManager managerInstance;


    ThreadSSPDevice(String tag){

        // ssp object
        ssp = new SSPSystem();
        ssp.setOnEventUpdateListener(this);
        ssp.setOnDeviceSetupListener(this);
        ssp.setOnDeviceFileUpdateListener(this);
        ssp.setOnPayoutEventListener(this);

        ssp.SetAddress(0);
        ssp.EscrowMode(false);
        ssp.SetESSPMode(true, 0x123456701234567L);
        ssp.SetSystemName(tag);

    }


    void setup(DeviceManager ins , IOIO ioio_, int power, int uarttx, int uartrx) {
        try {

            ioio = ioio_;
            managerInstance = ins;

            // copy to class variables
            uartRx = uartrx;
            uartTx = uarttx;
            // enable the power to BV20
            ioio_.openDigitalOutput(power, true);
            // set up comms
            uartBankSP0 = ioio_.openUart(uarttx, uartrx, 9600, Uart.Parity.NONE, Uart.StopBits.TWO);
            // serial IO streams
            inputStreamBankSP0 = uartBankSP0.getInputStream();
            outputStreamBankSP0 = uartBankSP0.getOutputStream();

            setupRun = true;


        } catch (ConnectionLostException e) {
            Log.e("ThreadBankSP0", "setup() -  " + e);
            e.printStackTrace();
        }
    }


    void RunSSPSystem()
    {
        if(ssp != null) {
           ssp.Run();
        }

    }


    void Close(){
        isRunning = false;
    }


    ArrayList<ItlCurrencyValue> GetTotalStoredValue()
    {
        return ssp.GetCurrentStoredTotal();
    }

    ArrayList<ItlCurrency> GetDeviceCurrencyList()
    {

        return ssp.GetDeviceCurrencyList();

    }


    public  void EscrowMode(boolean mode)
    {
        ssp.EscrowMode(mode);

    }


    String GetDeviceType()
    {

        return  ssp.GetDeviceType();
    }


    void SetRoutes()
    {
        ssp.RunRouteQueue();
    }


    ArrayList<ItlCurrencyValue> GetMinimumPayout()
    {
        return ssp.GetMinimumPayout();
    }



    SSPDevice GetDevice()
    {
        return ssp.GetDevice();
    }



    void StopSSPSystem()
    {
        if(ssp != null) {
            ssp.Close();
        }

    }


    void SSPEnable()
    {
        if(ssp != null) {
            ssp.EnableDevice();
        }
    }


    void SSPDisable()
    {
        if(ssp != null) {
            ssp.DisableDevice();
        }
    }


    public void SetBillEscrowAction(SSPSystem.BillAction action)
    {
        ssp.SetBillEscrowAction(action);
    }


    public boolean SetSSPDownload(SSPUpdate update)
    {
        return ssp.SetDownload(update);

    }


    String GetSystemName()
    {
        return ssp.GetSystemName();
    }


    void SetBarcocdeConfig(BarCodeReader cfg)
    {
        if(ssp != null){
            ssp.SetBarCodeConfiguration(cfg);
        }
    }


    int GetDeviceCode()
    {
        if(ssp != null){
            return ssp.GetDevice().headerType.getValue();
        }else{
            return -1;
        }
    }

    void SetPayoutRoute(ItlCurrency cur, PayoutRoute rt)
    {
        if(ssp != null) {
            ssp.SetPayoutRoute(cur, rt);
        }
    }


    void PayoutAmount(ItlCurrency cur)
    {
        if(ssp != null) {
            ssp.PayoutAmount(cur);
        }
    }

    void FloatAmount(ItlCurrency amt, ItlCurrency min)
    {
        if(ssp != null){
            ssp.FloatAmount(amt,min);
        }
    }


    void EmptyPayout()
    {
        if(ssp != null){
            ssp.EmptyPayout();
        }
    }


    /**
     * Fired when a new connected device is detected.
     * @param sspDevice the SSPdevice information class
     */
    @Override
    public void OnNewDeviceSetup(SSPDevice sspDevice) {
        synchronized (ThreadSSPDevice.this) {


            // apply stored config for routes
            SharedPreferences routes =
                    MainActivity.mainActivity.getSharedPreferences(ssp.GetSystemName()
                            + "_Routes", Context.MODE_PRIVATE);
            if(routes != null) { // settings exist
                for (ItlCurrency cur : sspDevice.currency
                        ) {
                    String rt = routes.getString(cur.country + " " + String.valueOf(cur.value),"none");
                    // get this value and code
                    if (!rt.equals("none")){
                        // saved as PAYOUT
                        if(rt.equals("PS")){
                            if(cur.route == PayoutRoute.Cashbox){
                                // change to payout
                                cur.newRoute = PayoutRoute.PayoutStore;
                                cur.routeChangeRequest = true;
                            }
                        }
                        //saved as CASHBOX
                        if(rt.equals("CB")){
                            if(cur.route == PayoutRoute.PayoutStore){
                                // change to cashbox
                                cur.newRoute = PayoutRoute.Cashbox;
                                cur.routeChangeRequest = true;
                            }
                        }
                    }
                }
                // set command to change requested routes on object
                ssp.RunRouteQueue();

            }

            // call setup event in manager
            managerInstance.NewSetupEvent(sspDevice, ssp.GetSystemName());
        }
    }


    /**
     * Fired when the device is detected as disconnected
     * @param sspDevice
     */
    @Override
    public void OnDeviceDisconnect(final SSPDevice sspDevice) {


        synchronized (ThreadSSPDevice.this) {
            managerInstance.NewComDisconnectEvent(sspDevice, ssp.GetSystemName());
        }
    }



    /**
     * Fires when a new device event is detected
     * @param deviceEvent object of event info
     */
    @Override
    public void OnDeviceEvent(final DeviceEvent deviceEvent) {
        /*
        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (deviceEvent.event) {

                    case Ready:
                        MainActivity.txtEvents.setText("READY\r\n");
                        break;
                    case BillRead:
                        MainActivity.txtEvents.setText("Reading bill\r\n");
                        break;
                    case BillEscrow:
                        MainActivity.txtEvents.setText("Bill Escrow " + deviceEvent.currency + " " +
                                String.valueOf((int) deviceEvent.value) + ".00\r\n");
                        if (MainActivity.swEscrow.isChecked()) {
                            MainActivity.btnAccept.setVisibility(View.VISIBLE);
                            MainActivity.btnReject.setVisibility(View.VISIBLE);
                        }
                        break;
                    case BillStacked:
                        if (MainActivity.swEscrow.isChecked()) {
                            MainActivity.btnAccept.setVisibility(View.INVISIBLE);
                            MainActivity.btnReject.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case BillReject:
                        MainActivity.txtEvents.append("Bill reject\r\n");
                        if (MainActivity.swEscrow.isChecked()) {
                            MainActivity.btnAccept.setVisibility(View.INVISIBLE);
                            MainActivity.btnReject.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case BillJammed:
                        MainActivity.txtEvents.setText("Bill jammed\r\n");
                        break;
                    case BillFraud:
                        break;
                    case BillCredit:
                        MainActivity.txtEvents.append("Bill Credit " + deviceEvent.currency + " " +
                                String.valueOf((int) deviceEvent.value) + ".00\r\n");
                        break;
                    case Full:
                        break;
                    case Initialising:
                        break;
                    case Disabled:
                        MainActivity.txtEvents.setText("Device disabled\r\n");
                        break;
                    case SoftwareError:
                        MainActivity.txtEvents.setText("Software error!\r\n");
                        break;
                }
            }
        }); */

    }

    @Override
    public void OnNewPayoutEvent(final SSPPayoutEvent ev) {


        synchronized (ThreadSSPDevice.this) {
            managerInstance.NewBillPayoutEvent(ev, ssp.GetSystemName());
        }

    }


    /**
     * Fires in response to the device sspUpdate process status changes
     * @param sspUpdate The SSPUpdate object giving info about the sspUpdate
     */
    @Override
    public void OnFileUpdateStatus(final SSPUpdate sspUpdate) {

        /*
        MainActivity.mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {


                switch (sspUpdate.UpdateStatus) {
                    case dwnInitialise:
                        MainActivity.txtDevice.setText("Downloading " + sspUpdate.fileName + "\r\n");
                        break;
                    case dwnRamCode:
                        MainActivity.progBar.setMax(sspUpdate.numberOfRamBlocks);
                        MainActivity.txtEvents.setText("Ram Block " + sspUpdate.blockIndex + " of " + sspUpdate.numberOfRamBlocks);
                        MainActivity.progBar.setProgress(sspUpdate.blockIndex);
                        break;
                    case dwnMainCode:
                        MainActivity.progBar.setMax(sspUpdate.numberOfBlocks);
                        MainActivity.txtEvents.setText("Main Block " + sspUpdate.blockIndex + " of " + sspUpdate.numberOfBlocks);
                        MainActivity.progBar.setProgress(sspUpdate.blockIndex);
                        break;
                    case dwnComplete:
                        MainActivity.txtEvents.setText("Download complete\r\n");
                        MainActivity.progBar.setProgress(0);
                        MainActivity.progBar.setVisibility(View.INVISIBLE);
                        break;
                    case dwnError:
                        MainActivity.progBar.setVisibility(View.INVISIBLE);
                        MainActivity.txtEvents.setText("Error during download!!!\r\n");
                        break;
                }

            }
        });
        */


    }




    @Override
    public void run() {


        int readSize = 0;

        isRunning = true;


        try {
            while (isRunning) {

                if(ssp != null && setupRun) {
                    // any ssp data to read ?(response from BV20)
                    if (inputStreamBankSP0.available() > 0) {
                        readSize = inputStreamBankSP0.read(rbuf);
                        ssp.ProcessResponse(rbuf, readSize);
                    }
                    // any SSP data to transmit to BV20 ?
                    int newdatalen = ssp.GetNewData(wbuf);
                    if (newdatalen > 0) {
                        outputStreamBankSP0.write(wbuf, 0, newdatalen);
                        ssp.SetComsBufferWritten(true);
                    }


                    // coms config changes
                    SSPComsConfig cfg = ssp.GetComsConfig();
                    if (cfg.configUpdate == SSPComsConfig.ComsConfigChangeState.ccNewConfig) {
                        cfg.configUpdate = SSPComsConfig.ComsConfigChangeState.ccUpdating;
                        SetConfig(cfg.baud);
                        cfg.configUpdate = SSPComsConfig.ComsConfigChangeState.ccUpdated;
                    }
                }

                sleep(100);
            }
        } catch (IOException | NullPointerException | InterruptedException e) {
            Log.e("ThreadBankSP0", "IOException -  " + e);
            e.printStackTrace();
        }
    }


    /**
     * Change the uart settings in response to ssp object request
     * @param baud
     */
    public void SetConfig(final int baud)
    {
        try {

            uartBankSP0.close();
            uartBankSP0 = ioio.openUart(uartTx, uartRx, baud, Uart.Parity.NONE, Uart.StopBits.TWO);

            // serial IO streams
            inputStreamBankSP0 = uartBankSP0.getInputStream();
            outputStreamBankSP0 = uartBankSP0.getOutputStream();


        } catch (ConnectionLostException e) {

            e.printStackTrace();
        }

    }



}
