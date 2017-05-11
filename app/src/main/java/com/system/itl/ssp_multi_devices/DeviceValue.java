package com.system.itl.ssp_multi_devices;


class DeviceValue {

    String country;
    int requestedValue;
    int [] paidValue;

    DeviceValue(int num)
    {
            paidValue = new int[num];
    }

    int GetPaidTotal()
    {

        int tot = 0;
        for (int i: paidValue
             ) {
            tot += i;
        }
        return tot;
    }

}
