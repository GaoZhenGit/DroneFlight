package hk.hku.flight.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import hk.hku.flight.Constant;
import hk.hku.flight.DroneApplication;
import hk.hku.flight.R;
import hk.hku.flight.modules.BatteryStateMgr;
import hk.hku.flight.modules.FlyingStateMgr;
import hk.hku.flight.modules.SignalStateMgr;

public class DroneStateTopBar extends RelativeLayout {
    private TextView mBattery;
    private TextView mSignal;
    private TextView mAltitude;
    private TextView mAltitudeSpeed;
    private TextView mDistance;
    private TextView mDistanceSpeed;
    private TextView mSatellite;
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBattery();
        }
    };
    private final BroadcastReceiver mSignalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSignal();
        }
    };
    private final BroadcastReceiver mFlyingStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateFlyingState();
        }
    };
    private final BroadcastReceiver mConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBattery();
            updateSignal();
        }
    };
    public DroneStateTopBar(Context context) {
        super(context);
        init(context);
    }

    public DroneStateTopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.drone_state_top_bar, this, true);
        setBackgroundResource(R.drawable.default_background);

        mBattery = findViewById(R.id.tv_state_top_bar_battery);
        updateBattery();

        mSignal = findViewById(R.id.tv_state_top_bar_signal);
        updateSignal();

        mAltitude = findViewById(R.id.tv_state_top_bar_altitude);
        mAltitudeSpeed = findViewById(R.id.tv_state_top_bar_vspeed);
        mDistance = findViewById(R.id.tv_state_top_bar_distance);
        mDistanceSpeed = findViewById(R.id.tv_state_top_bar_hspeed);
        mSatellite = findViewById(R.id.tv_state_top_bar_satellite);
        updateFlyingState();
    }

    private void updateBattery() {
        if (mBattery == null) {
            return;
        }
        int percentage = BatteryStateMgr.getInstance().getBatteryPercentage();
        if (percentage > 0) {
            mBattery.setText(percentage + "%");
        } else {
            mBattery.setText("N/A");
        }
    }

    private void updateSignal() {
        if (mSignal == null) {
            return;
        }
        int up = SignalStateMgr.getInstance().getUpSignal();
        int down = SignalStateMgr.getInstance().getDownSignal();
        mSignal.setText(String.format("↑:%d%% ↓:%d%%", up, down));
    }

    private void updateFlyingState() {
        if (mAltitude != null) {
            float altitude = FlyingStateMgr.getInstance().getAltitude();
            mAltitude.setText(String.format("H:%.1fm", altitude));
        }
        if (mAltitudeSpeed != null) {
            double altitudeSpeed = FlyingStateMgr.getInstance().getVerticalV();
            mAltitudeSpeed.setText(String.format("%.1fm/s", altitudeSpeed));
        }
        if (mDistance != null) {
            double distance = FlyingStateMgr.getInstance().getDistance();
            mDistance.setText(String.format("D:%.1fm", distance));
        }
        if (mDistanceSpeed != null) {
            double distanceSpeed = FlyingStateMgr.getInstance().getHorizonV();
            mDistanceSpeed.setText(String.format("%.1fm/s", distanceSpeed));
        }
        if (mSatellite != null) {
            int satellite = FlyingStateMgr.getInstance().getSatelliteCount();
            mSatellite.setText(String.valueOf(satellite));
        }
    }

    private void setCallbacks() {
        DroneApplication.getInstance().registerReceiver(mConnectionReceiver, new IntentFilter(Constant.FLAG_CONNECTION_CHANGE));
        DroneApplication.getInstance().registerReceiver(mBatteryReceiver, new IntentFilter(Constant.FLAG_BATTERY_PERCENTAGE));
        DroneApplication.getInstance().registerReceiver(mSignalReceiver, new IntentFilter(Constant.FLAG_SIGNAL_QUANTITY));
        DroneApplication.getInstance().registerReceiver(mFlyingStateReceiver, new IntentFilter(Constant.FLAG_FLIGHT_CONTROL_STATE));
    }

    private void removeCallbacks() {
        DroneApplication.getInstance().unregisterReceiver(mConnectionReceiver);
        DroneApplication.getInstance().unregisterReceiver(mBatteryReceiver);
        DroneApplication.getInstance().unregisterReceiver(mSignalReceiver);
        DroneApplication.getInstance().unregisterReceiver(mFlyingStateReceiver);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setCallbacks();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks();
    }
}
