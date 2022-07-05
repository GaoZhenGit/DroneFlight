package hk.hku.flight;

import static hk.hku.flight.Constant.FLAG_CONNECTION_CHANGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import hk.hku.flight.account.AccountActivity;
import hk.hku.flight.account.AccountManager;
import hk.hku.flight.modules.BatteryStateMgr;
import hk.hku.flight.modules.FlyingStateMgr;
import hk.hku.flight.modules.SignalStateMgr;
import hk.hku.flight.util.ClickUtil;
import hk.hku.flight.util.ConnectionCheckUtil;
import hk.hku.flight.util.ToastUtil;
import hk.hku.flight.view.NetImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE, // Gimbal rotation
            Manifest.permission.INTERNET, // API requests
            Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.BLUETOOTH, // Bluetooth connected products
            Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
            Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO // Speaker accessory
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private NetImageView mMainAvatar;
    private TextView mMainName;
    private static final int REQUEST_PERMISSION_CODE = 12345;

    private final BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkRegister();
            checkConnect();
        }
    };

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
            Intent attachedIntent = new Intent();
            attachedIntent.setAction(DJISDKManager.USB_ACCESSORY_ATTACHED);
            sendBroadcast(attachedIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
        setInfoCallback();
    }

    private void initView() {
        checkPermission();
        findViewById(R.id.btn_permission).setOnClickListener(v -> requestNeedPermission());
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        }
        findViewById(R.id.btn_register).setOnClickListener(v -> {
            checkPermission();
            if (!missingPermission.isEmpty()) {
                Toast.makeText(getBaseContext(), "Please click \"Permission\" first.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isRegistrationInProgress.get()) {
                startSDKRegistration();
            }
        });
        checkConnect();
        findViewById(R.id.btn_drone).setOnClickListener(v -> checkConnect());
        findViewById(R.id.btn_RC).setOnClickListener(v -> checkConnect());
        findViewById(R.id.btn_start_fly).setOnClickListener(v -> {
            if (!v.isEnabled()) {
                return;
            }
            if (ClickUtil.isDoubleClickInSec()) {
                return;
            }
            Intent intent = new Intent(MainActivity.this, FlightActivity.class);
            startActivity(intent);
        });
        mMainAvatar = findViewById(R.id.main_avatar);
        mMainName = findViewById(R.id.main_user_name);
        checkLogin();
        findViewById(R.id.btn_login).setOnClickListener(v -> {
            if (ClickUtil.isDoubleClickInSec()) {
                return;
            }
            AccountManager.getInstance().checkLogin(MainActivity.this, () -> startActivity(new Intent(MainActivity.this, AccountActivity.class)));
        });
    }

    private void startSDKRegistration() {
        showToast("Initializing...");
        AsyncTask.execute(() -> DJISDKManager.getInstance().registerApp(getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
            @Override
            public void onRegister(DJIError djiError) {
                if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                    showToast("Initialize Success");
                    DJISDKManager.getInstance().startConnectionToProduct();
                    isRegistrationInProgress.set(true);
                    checkRegister();
                } else {
                    showToast("Initialize fail:" + djiError.getDescription());
                }
                Log.v(TAG, djiError.getDescription());
            }

            @Override
            public void onProductDisconnect() {
                Log.d(TAG, "onProductDisconnect");
                showToast("Product Disconnected");
                notifyStatusChange();

            }

            @Override
            public void onProductConnect(BaseProduct baseProduct) {
                Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                showToast("Product Connected");
                notifyStatusChange();

            }

            @Override
            public void onProductChanged(BaseProduct baseProduct) {
                Log.d(TAG, String.format("onProductChanged newProduct:%s", baseProduct));
                notifyStatusChange();
            }

            @Override
            public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                          BaseComponent newComponent) {
                Log.i(TAG, "onComponentChange: " + newComponent);
                if (newComponent != null) {
                    newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                        @Override
                        public void onConnectivityChange(boolean isConnected) {
                            Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                            notifyStatusChange();
                        }
                    });
                }
                Log.d(TAG,
                        String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                componentKey,
                                oldComponent,
                                newComponent));
            }

            @Override
            public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

            }

            @Override
            public void onDatabaseDownloadProgress(long l, long l1) {

            }
        }));
    }

    private void checkPermission() {
        missingPermission.clear();
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            ((ImageView) findViewById(R.id.ic_permission)).setImageResource(R.drawable.state_ok);
        } else {
            ((ImageView) findViewById(R.id.ic_permission)).setImageResource(R.drawable.state_error);
        }
    }

    private void requestNeedPermission() {
        checkPermission();
        if (missingPermission.isEmpty()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    private void checkRegister() {
        if (isRegistrationInProgress.get()) {
            ((ImageView) findViewById(R.id.ic_register)).setImageResource(R.drawable.state_ok);
        } else {
            ((ImageView) findViewById(R.id.ic_register)).setImageResource(R.drawable.state_error);
        }
    }

    private void checkConnect() {
        Log.i(TAG, "checkConnect");
        switch (ConnectionCheckUtil.checkConnection()) {
            case ConnectionCheckUtil.RC_DRONE:
                ((ImageView) findViewById(R.id.ic_drone)).setImageResource(R.drawable.state_ok);
                ((ImageView) findViewById(R.id.ic_RC)).setImageResource(R.drawable.state_ok);
                findViewById(R.id.btn_start_fly).setEnabled(true);

                mHandler.postDelayed(() -> BatteryStateMgr.getInstance().addBatteryCallback(), 1000);
                mHandler.postDelayed(() -> SignalStateMgr.getInstance().addSignalCallback(), 1000);
                mHandler.postDelayed(() -> FlyingStateMgr.getInstance().addFlyingStateCallback(), 1000);
                break;
            case ConnectionCheckUtil.ONLY_RC:
                ((ImageView) findViewById(R.id.ic_drone)).setImageResource(R.drawable.state_error);
                ((ImageView) findViewById(R.id.ic_RC)).setImageResource(R.drawable.state_ok);
                findViewById(R.id.btn_start_fly).setEnabled(false);
                break;
            case ConnectionCheckUtil.NO_CONNECTION:
            default:
                ((ImageView) findViewById(R.id.ic_drone)).setImageResource(R.drawable.state_error);
                ((ImageView) findViewById(R.id.ic_RC)).setImageResource(R.drawable.state_error);
                findViewById(R.id.btn_start_fly).setEnabled(false);
                break;
        }
    }

    /**
     * Result of runtime permission request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }

    private void checkLogin() {
        if (mMainAvatar == null || mMainName == null) {
            return;
        }
        if (AccountManager.getInstance().isLogin()) {
            mMainAvatar.loadRound(AccountManager.getInstance().getAvatar());
            mMainName.setText(AccountManager.getInstance().getUserName());
        } else {
            mMainAvatar.load(null);
            mMainName.setText("Account");
        }
    }

    private void setInfoCallback() {
        registerReceiver(mConnectionReceiver, new IntentFilter(FLAG_CONNECTION_CHANGE));
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg) {
        ToastUtil.toast(toastMsg);
    }

    private long mLastBackPressTime = 0;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - mLastBackPressTime > 2000) {
            showToast("press home again to exit");
            mLastBackPressTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}