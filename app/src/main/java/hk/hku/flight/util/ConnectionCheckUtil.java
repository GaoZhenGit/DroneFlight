package hk.hku.flight.util;

import android.util.Log;
import android.widget.ImageView;

import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import hk.hku.flight.DroneApplication;
import hk.hku.flight.R;
import hk.hku.flight.modules.BatteryStateMgr;
import hk.hku.flight.modules.FlyingStateMgr;
import hk.hku.flight.modules.SignalStateMgr;

public class ConnectionCheckUtil {
    private static final String TAG = "ConnectionCheckUtil";
    public static final int NO_CONNECTION = -1;
    public static final int ONLY_RC = -2;
    public static final int RC_DRONE = -3;
    public static int checkConnection() {
        BaseProduct product = DroneApplication.getProductInstance();
        if (product == null) {
            Log.i(TAG, "no product connect");
            return NO_CONNECTION;
        }
        if (product.isConnected()) {
            Log.i(TAG, "product connect");
            return RC_DRONE;
        }
        if (product instanceof Aircraft) {
            Aircraft aircraft = (Aircraft) product;
            if (aircraft.getRemoteController() != null && aircraft.getRemoteController().isConnected()) {
                Log.i(TAG, "only RC connect");
                return ONLY_RC;
            } else {
                Log.i(TAG, "no product connect 2");
            }
        } else  {
            Log.i(TAG, "no product connect 3");
        }
        return NO_CONNECTION;
    }
}
