package hk.hku.flight;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import dji.sdk.camera.VideoFeeder;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import hk.hku.flight.account.AccountManager;
import hk.hku.flight.util.NetworkManager;
import hk.hku.flight.util.ThreadManager;
import hk.hku.flight.util.ToastUtil;
import hk.hku.flight.util.VideoFeedView;
import hk.hku.flight.view.LiveStreamSelectDialog;

public class FlightActivity extends AppCompatActivity {
    private static final String TAG = "FlightActivity";
    private VideoFeedView videoFeedView;
    private ImageView mIcLive;
    private LiveStreamSelectDialog mLiveStreamSelectDialog;
    private LiveStreamManager mLiveStreamManager;

    private LiveStreamManager.OnLiveChangeListener onLiveChangeListener = new LiveStreamManager.OnLiveChangeListener() {
        @Override
        public void onStatusChanged(int i) {
            Log.i(TAG, "LiveStreamManager onStatusChanged:" + i);
            if (i == LiveStreamManager.STATUS_STREAMING) {
                onLiveStreamSuccess();
            } else if (i == LiveStreamManager.STATUS_STOP || i == LiveStreamManager.STATUS_LIVING_INTERRUPTION_ERROR) {
                onLiveStreamStop(i, "");
            }
        }
    };
    private LiveStreamManager.OnLiveErrorStatusListener onLiveErrorStatusListener = new LiveStreamManager.OnLiveErrorStatusListener() {
        @Override
        public void onError(int i, String s) {
            Log.i(TAG, "LiveStreamManager onError:" + i + "," + s);
            onLiveStreamStop(i, s);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_flight);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = 1;
        window.setAttributes(lp);
        initView();
        initLiveStream();
    }

    private void initView() {
        videoFeedView = findViewById(R.id.preview_view);
        videoFeedView.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
        Aircraft aircraft = DroneApplication.getAircraftInstance();
        if (aircraft == null) {
            ToastUtil.toast("connection loss!");
            finish();
            return;
        }

        mIcLive = findViewById(R.id.ic_live);
        initLiveStream();
        findViewById(R.id.btn_live_menu).setOnClickListener(v -> {
            if (mLiveStreamManager != null && mLiveStreamManager.isStreaming()) {
                new AlertDialog.Builder(FlightActivity.this)
                        .setTitle("stop live?")
                        .setPositiveButton("STOP!", (dialog, which) -> {
                            NetworkManager.getInstance().stopLive(mLiveStreamManager.getLiveUrl(), new NetworkManager.BaseCallback<NetworkManager.BaseResponse>() {
                                @Override
                                public void onSuccess(NetworkManager.BaseResponse data) {
                                    Log.i(TAG, "stopLive success");
                                }

                                @Override
                                public void onFail(String msg) {
                                    Log.i(TAG, "stopLive fail:" + msg);
                                }
                            });
                            mLiveStreamManager.stopStream();
                        })
                        .setNegativeButton("DON'T STOP", (dialog, which) -> dialog.dismiss()).show();
            } else {
                AccountManager.getInstance().checkLogin(FlightActivity.this, () -> {
                    mLiveStreamSelectDialog = new LiveStreamSelectDialog(FlightActivity.this);
                    mLiveStreamSelectDialog.show();
                });
            }
        });
    }

    private void initLiveStream() {
        mLiveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
        if (mLiveStreamManager == null) {
            return;
        }
        mLiveStreamManager.addLiveErrorStatusListener(onLiveErrorStatusListener);
        mLiveStreamManager.registerListener(onLiveChangeListener);
        if (mLiveStreamManager.isStreaming()) {
            mIcLive.setImageResource(R.drawable.live);
        } else {
            mIcLive.setImageResource(R.drawable.live_stop);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLiveStreamManager != null) {
            mLiveStreamManager.unregisterListener(onLiveChangeListener);
            mLiveStreamManager.removeLiveErrorStatusListener(onLiveErrorStatusListener);
        }
    }

    private void onLiveStreamSuccess() {
        ThreadManager.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mIcLive.setImageResource(R.drawable.live);
            }
        });
    }

    private void onLiveStreamStop(int code, String desc) {
        ThreadManager.getInstance().submit(() -> {
            if (mLiveStreamManager.isStreaming()) {
                mLiveStreamManager.stopStream();
            }
        });
        ThreadManager.getInstance().runOnUiThread(() -> {
            mIcLive.setImageResource(R.drawable.live_stop);
            if (code != LiveStreamManager.STATUS_STOP) {
                ToastUtil.toast("live stream error:(" + code + "):" + desc);
            }
        });
    }
}