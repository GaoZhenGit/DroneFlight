package hk.hku.flight;

import static hk.hku.flight.Constant.FLAG_BATTERY_PERCENTAGE;
import static hk.hku.flight.Constant.FLAG_CONNECTION_CHANGE;
import static hk.hku.flight.Constant.FLAG_DOWN_SIGNAL_QUANTITY;
import static hk.hku.flight.Constant.FLAG_UP_SIGNAL_QUANTITY;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.airlink.SignalQualityCallback;
import dji.common.battery.BatteryState;
import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.airlink.AirLink;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.battery.Battery;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import hk.hku.flight.util.ThreadManager;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.VIBRATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);
    private static final int REQUEST_PERMISSION_CODE = 12345;

    private final BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "on Device state change, try re-register");
            BaseProduct product = DJISDKManager.getInstance().getProduct();
            if (product == null) {
                Log.i(TAG, "connection loss");
                return;
            }
            AirLink airLink = product.getAirLink();
            if (airLink == null) {
                Log.i(TAG, "airLink connection loss");
            } else {
                airLink.setUplinkSignalQualityCallback(null);
                airLink.setDownlinkSignalQualityCallback(i -> {
                    Log.i(TAG, "Down link SignalQuality:" + i);
                    Intent intent1 = new Intent(FLAG_DOWN_SIGNAL_QUANTITY);
                    intent1.putExtra(FLAG_DOWN_SIGNAL_QUANTITY, i);
                    sendBroadcast(intent1);
                });
                airLink.setDownlinkSignalQualityCallback(null);
                airLink.setUplinkSignalQualityCallback(i -> {
                    Log.i(TAG, "Up link SignalQuality:" + i);
                    Intent intent12 = new Intent(FLAG_UP_SIGNAL_QUANTITY);
                    intent12.putExtra(FLAG_UP_SIGNAL_QUANTITY, i);
                    sendBroadcast(intent12);
                });
            }
            Battery battery = product.getBattery();
            if (battery == null) {
                Log.i(TAG, "battery connection loss");
            } else {
                battery.setStateCallback(null);
                battery.setStateCallback(batteryState -> {
                    int percentage = batteryState.getChargeRemainingInPercent();
                    Log.i(TAG, "battery percentage:" + percentage);
                    Intent intent13 = new Intent(FLAG_BATTERY_PERCENTAGE);
                    intent13.putExtra(FLAG_BATTERY_PERCENTAGE, percentage);
                    sendBroadcast(intent13);
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setInfoCallback();
    }

    private void initView() {
        findViewById(R.id.btn_init).setOnClickListener(view -> {
            if (isRegistrationInProgress.compareAndSet(false, true)) {
                checkAndRequestPermissions();
            } else {
                mConnectionReceiver.onReceive(null, null);
            }
        });

        findViewById(R.id.btn_open).setOnClickListener(v -> {
            if (!missingPermission.isEmpty()) {
                Log.i(TAG, "lack of permission:" + missingPermission);
                showToast("lack of permission:" + missingPermission);
                return;
            }

            Intent intent = new Intent(MainActivity.this, FlightActivity.class);
            startActivity(intent);
        });
    }

    private void startSDKRegistration() {
        showToast("registering...");
        ThreadManager.getInstance().submit(() -> DJISDKManager.getInstance().registerApp(getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
            @Override
            public void onRegister(DJIError djiError) {
                if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                    showToast("Register Success");
                    DJISDKManager.getInstance().startConnectionToProduct();
                } else {
                    showToast("Register sdk fails, please check the bundle id and network connection!");
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

    private void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showToast("Need to grant the permissions!");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
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
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
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

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show());

    }
}