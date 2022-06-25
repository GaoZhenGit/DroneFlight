package hk.hku.flight.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import hk.hku.flight.R;
import hk.hku.flight.util.DensityUtil;
import hk.hku.flight.util.NetworkManager;

public class StopLiveDialog extends AlertDialog {
    private static final String TAG = "StopLiveDialog";
    private Activity mActivity;

    public StopLiveDialog(Activity context) {
        super(context);
        mActivity = context;
        setOnShowListener(dialog -> initView());
    }

    private void initView() {
        setContentView(R.layout.stop_live_dialog);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.setLayout(DensityUtil.dip2px(320), WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0f;
        window.setAttributes(lp);

        findViewById(R.id.btn_stop_save).setOnClickListener(v -> stopWithSave());
        findViewById(R.id.btn_stop_without_save).setOnClickListener(v -> stopWithoutSave());
        findViewById(R.id.btn_cancel).setOnClickListener(v -> dismiss());
    }

    private void stopWithoutSave() {
        LiveStreamManager liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
        NetworkManager.getInstance().stopLive(liveStreamManager.getLiveUrl(), new NetworkManager.BaseCallback<NetworkManager.BaseResponse>() {
            @Override
            public void onSuccess(NetworkManager.BaseResponse data) {
                Log.i(TAG, "stopLive success");
            }

            @Override
            public void onFail(String msg) {
                Log.i(TAG, "stopLive fail:" + msg);
            }
        });
        liveStreamManager.stopStream();
        dismiss();
    }

    private void stopWithSave() {
        Log.i(TAG, "stopWithSave");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date curDate = new Date(System.currentTimeMillis());
        String str = formatter.format(curDate);
        InputAlertDialog inputAlertDialog = new InputAlertDialog(mActivity);
        inputAlertDialog.addEditText(System.currentTimeMillis() + ".mp4","");
        inputAlertDialog.addEditText("live shot in " + str,"");
        inputAlertDialog.setOnInputCallback(results -> {
            String name = results.get(0);
            String description = results.get(1);
            LiveStreamManager liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
            NetworkManager.getInstance().stopAndSaveLive(
                    liveStreamManager.getLiveUrl(),
                    name, description,
                    new NetworkManager.BaseCallback<NetworkManager.BaseResponse>() {
                @Override
                public void onSuccess(NetworkManager.BaseResponse data) {
                    Log.i(TAG, "stopAndSaveLive success");
                }

                @Override
                public void onFail(String msg) {
                    Log.i(TAG, "stopAndSaveLive fail:" + msg);
                }
            });
            if (liveStreamManager != null) {
                liveStreamManager.stopStream();
            }
            dismiss();
        });
        inputAlertDialog.setTitle("Input video information");
        inputAlertDialog.show();
    }
}
