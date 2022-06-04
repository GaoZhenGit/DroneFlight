package hk.hku.flight.modules;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import dji.sdk.airlink.AirLink;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import hk.hku.flight.Constant;
import hk.hku.flight.DroneApplication;

public class SignalStateMgr {
    private static final String TAG = "SignalStateMgr";

    private static class InstanceHolder {
        public volatile static SignalStateMgr instance = new SignalStateMgr();
    }

    public static SignalStateMgr getInstance() {
        return SignalStateMgr.InstanceHolder.instance;
    }

    private BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    public void addSignalCallback() {
        Log.i(TAG, "addSignalCallback");
        BaseProduct product = DroneApplication.getProductInstance();
        if (product == null) {
            return;
        }
        AirLink airLink = product.getAirLink();
        if (airLink == null) {
            return;
        }
        airLink.setDownlinkSignalQualityCallback(null);
        airLink.setDownlinkSignalQualityCallback(i -> {
            Log.i(TAG, "Down link SignalQuality:" + i);
            mDownSignal = i;
            Intent intent = new Intent(Constant.FLAG_SIGNAL_QUANTITY);
            intent.putExtra(Constant.FLAG_SIGNAL_QUANTITY, i);
            DroneApplication.getInstance().sendBroadcast(intent);
        });
        airLink.setUplinkSignalQualityCallback(null);
        airLink.setUplinkSignalQualityCallback(i -> {
            Log.i(TAG, "Up link SignalQuality:" + i);
            mUpSignal = i;
            Intent intent = new Intent(Constant.FLAG_SIGNAL_QUANTITY);
            intent.putExtra(Constant.FLAG_SIGNAL_QUANTITY, i);
            DroneApplication.getInstance().sendBroadcast(intent);
        });
        Log.i(TAG, "addSignalCallback success");
    }

    private int mUpSignal = -1;

    public int getUpSignal() {
        BaseProduct product = DroneApplication.getProductInstance();
        if (product == null) {
            return -1;
        }
        AirLink airLink = product.getAirLink();
        if (airLink == null) {
            return -1;
        }
        return mUpSignal;
    }
    private int mDownSignal = -1;

    public int getDownSignal() {
        BaseProduct product = DroneApplication.getProductInstance();
        if (product == null) {
            return -1;
        }
        AirLink airLink = product.getAirLink();
        if (airLink == null) {
            return -1;
        }
        if (!airLink.isConnected()) {
            return -1;
        }
        return mDownSignal;
    }
}
