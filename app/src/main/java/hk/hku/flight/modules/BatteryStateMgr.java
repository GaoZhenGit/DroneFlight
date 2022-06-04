package hk.hku.flight.modules;

import static hk.hku.flight.Constant.FLAG_BATTERY_PERCENTAGE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import hk.hku.flight.Constant;
import hk.hku.flight.DroneApplication;

public class BatteryStateMgr {
    private static final String TAG = "BatteryStateMgr";
    private static class InstanceHolder {
        public volatile static BatteryStateMgr instance = new BatteryStateMgr();
    }
    public static BatteryStateMgr getInstance() {
        return BatteryStateMgr.InstanceHolder.instance;
    }
    public void addBatteryCallback() {
        BaseProduct product = DroneApplication.getProductInstance();
        if (product == null) {
            return;
        }
        Battery battery = product.getBattery();
        if (battery == null) {
            return;
        }
        battery.setStateCallback(null);
        battery.setStateCallback(batteryState -> {
            int percentage = batteryState.getChargeRemainingInPercent();
            Log.i(TAG, "on update:" + percentage);
            mBatteryPercentage = percentage;
            Intent batteryStateIntent = new Intent(FLAG_BATTERY_PERCENTAGE);
            batteryStateIntent.putExtra(FLAG_BATTERY_PERCENTAGE, percentage);
            DroneApplication.getInstance().sendBroadcast(batteryStateIntent);
        });
    }
    private int mBatteryPercentage = -1;
    public int getBatteryPercentage() {
        BaseProduct product = DroneApplication.getProductInstance();
        if (product == null) {
            return -1;
        }
        Battery battery = product.getBattery();
        if (battery == null) {
            return -1;
        }
        return mBatteryPercentage;
    }
}
