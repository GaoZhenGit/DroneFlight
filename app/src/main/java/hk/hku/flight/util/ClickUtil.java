package hk.hku.flight.util;

public class ClickUtil {
    private static long sLastClickTime;
    private static long sInterval = 500;
    public static boolean isDoubleClickInSec() {
        if (System.currentTimeMillis() - sLastClickTime > sInterval) {
            sLastClickTime = System.currentTimeMillis();
            return false;
        } else {
            sLastClickTime = System.currentTimeMillis();
            return true;
        }
    }
}
