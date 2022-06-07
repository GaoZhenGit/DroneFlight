package hk.hku.flight.util;

import android.widget.Toast;

import hk.hku.flight.DroneApplication;

public class ToastUtil {
    public static void toast(String s) {
        ThreadManager.getInstance().runOnUiThread(() -> Toast.makeText(DroneApplication.getInstance(), s, Toast.LENGTH_SHORT).show());
    }
}
