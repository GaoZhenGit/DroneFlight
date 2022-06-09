package hk.hku.flight.account;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import hk.hku.flight.DroneApplication;
import hk.hku.flight.util.SharePreferenceUtil;

public class AccountManager {
    private static final String KEY_UID = "KEY_UID";
    private static final String KEY_USER_NAME = "KEY_USER_NAME";
    private static final String KEY_EMAIL = "KEY_EMAIL";
    private static final String KEY_AVATAR = "KEY_AVATAR";

    private static class InstanceHolder {
        private static AccountManager instance = new AccountManager();
    }

    public static AccountManager getInstance() {
        return InstanceHolder.instance;
    }

    public boolean isLogin() {
        if (!TextUtils.isEmpty(getEmail()) && !TextUtils.isEmpty(getUserName()) && !TextUtils.isEmpty(getUid())) {
            return true;
        } else {
            return false;
        }
    }

    public User getUser() {
        User user = new User();
        user.email = getEmail();
        user.name = getUserName();
        user.id = getUid();
        user.avatar = getAvatar();
        return user;
    }

    public String getEmail() {
        return SharePreferenceUtil.get(KEY_EMAIL, "");
    }

    public String getUserName() {
        return SharePreferenceUtil.get(KEY_USER_NAME, "");
    }

    public String getUid() {
        return SharePreferenceUtil.get(KEY_UID, "");
    }

    public String getAvatar() {
        return SharePreferenceUtil.get(KEY_AVATAR, "");
    }


    public void setLocalUserInfo(String uid, String userName, String email, String avatar) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_UID, uid);
        map.put(KEY_USER_NAME, userName);
        map.put(KEY_EMAIL, email);
        map.put(KEY_AVATAR, avatar);
        SharePreferenceUtil.put(map);
    }

    public void clearUserInfo() {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_UID, "");
        map.put(KEY_USER_NAME, "");
        map.put(KEY_EMAIL, "");
        map.put(KEY_AVATAR, "");
        SharePreferenceUtil.put(map);
    }
}
