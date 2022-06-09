package hk.hku.flight.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import hk.hku.flight.DroneApplication;

public class SharePreferenceUtil {
    private static final String SP_KEY_DEFAULT = "SP_KEY_DEFAULT";
    public static void set(String key, String value) {
        SharedPreferences sp = DroneApplication.getInstance().getSharedPreferences(SP_KEY_DEFAULT, Context.MODE_PRIVATE);
        sp.edit().putString(key, value).apply();
    }

    public static void put(Map<String, String> map) {
        SharedPreferences sp = DroneApplication.getInstance().getSharedPreferences(SP_KEY_DEFAULT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.apply();
    }

    public static void setList(String key, List<String> values) {
        SharedPreferences sp = DroneApplication.getInstance().getSharedPreferences(SP_KEY_DEFAULT, Context.MODE_PRIVATE);
        StringBuffer sb = new StringBuffer();
        for (String v : values) {
            sb.append(v).append(",");
        }
        sp.edit().putString(key, sb.toString()).apply();
    }

    public static String get(String key, String defString) {
        SharedPreferences sp = DroneApplication.getInstance().getSharedPreferences(SP_KEY_DEFAULT, Context.MODE_PRIVATE);
        return sp.getString(key, defString);
    }

    public static List<String> getList(String key) {
        SharedPreferences sp = DroneApplication.getInstance().getSharedPreferences(SP_KEY_DEFAULT, Context.MODE_PRIVATE);
        String l = sp.getString(key, "");
        String[] sl = l.split(",");
        List<String> ret = new ArrayList<>();
        for (String s : sl) {
            if (!TextUtils.isEmpty(s)) {
                ret.add(s);
            }
        }
        return ret;
    }
}
