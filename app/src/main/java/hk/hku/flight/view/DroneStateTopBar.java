package hk.hku.flight.view;

import static hk.hku.flight.Constant.FLAG_BATTERY_PERCENTAGE;

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
import hk.hku.flight.modules.SignalStateMgr;

public class DroneStateTopBar extends RelativeLayout {
    private TextView mBattery;
    private TextView mSignal;
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
        mSignal.setText(String.format("Up:%d Down:%d", up, down));
    }

    private void setCallbacks() {
        DroneApplication.getInstance().registerReceiver(mConnectionReceiver, new IntentFilter(Constant.FLAG_CONNECTION_CHANGE));
        DroneApplication.getInstance().registerReceiver(mBatteryReceiver, new IntentFilter(Constant.FLAG_BATTERY_PERCENTAGE));
        DroneApplication.getInstance().registerReceiver(mSignalReceiver, new IntentFilter(Constant.FLAG_SIGNAL_QUANTITY));
    }

    private void removeCallbacks() {
        DroneApplication.getInstance().unregisterReceiver(mConnectionReceiver);
        DroneApplication.getInstance().unregisterReceiver(mBatteryReceiver);
        DroneApplication.getInstance().unregisterReceiver(mSignalReceiver);
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
