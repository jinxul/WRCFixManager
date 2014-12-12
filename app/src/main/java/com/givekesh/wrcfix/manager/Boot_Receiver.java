package com.givekesh.wrcfix.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Ahmad-Pc on 12/12/2014.
 */
public class Boot_Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent();
        service.setClassName("com.givekesh.wrcfix", "com.givekesh.wrcfix.WrcFixer");
        context.startService(service);
    }
}
