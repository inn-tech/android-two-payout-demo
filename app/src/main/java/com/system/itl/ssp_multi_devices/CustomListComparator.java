package com.system.itl.ssp_multi_devices;

import java.util.Comparator;
import device.itl.sspcoms.ItlCurrency;

public class CustomListComparator implements Comparator<ItlCurrency> {
    @Override
    public int compare(ItlCurrency o1, ItlCurrency o2) {
        return (o1.value > o2.value) ? -1:0;
    }
}
