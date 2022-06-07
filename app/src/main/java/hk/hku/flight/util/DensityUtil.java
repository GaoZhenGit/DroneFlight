package hk.hku.flight.util;

import hk.hku.flight.DroneApplication;

public class DensityUtil {
    public static int dip2px(float dpValue) {
        float scale = DroneApplication.getInstance().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
