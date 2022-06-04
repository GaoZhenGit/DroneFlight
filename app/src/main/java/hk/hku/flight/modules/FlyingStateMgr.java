package hk.hku.flight.modules;

import android.content.Intent;
import android.util.Log;

import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import hk.hku.flight.Constant;
import hk.hku.flight.DroneApplication;

public class FlyingStateMgr {
    private static final String TAG = "FlyingStateMgr";

    private static class InstanceHolder {
        public volatile static FlyingStateMgr instance = new FlyingStateMgr();
    }

    public static FlyingStateMgr getInstance() {
        return FlyingStateMgr.InstanceHolder.instance;
    }

    public void addFlyingStateCallback() {
        Log.i(TAG, "addFlyingStateCallback");
        Aircraft aircraft = DroneApplication.getAircraftInstance();
        if (aircraft == null) {
            return;
        }
        FlightController flightController = aircraft.getFlightController();
        if (flightController == null) {
            return;
        }
        FlightControllerState state = flightController.getState();
        mAltitude = state.getTakeoffLocationAltitude();
        flightController.setStateCallback(null);
        flightController.setStateCallback(flightControllerState -> {
            mAltitude = flightControllerState.getAircraftLocation().getAltitude();
            Log.i(TAG, "altitude change:" + mAltitude);
            Intent intent = new Intent(Constant.FLAG_FLIGHT_CONTROL_STATE);
            DroneApplication.getInstance().sendBroadcast(intent);
        });
    }

    private float mAltitude = 0;
    public float getAltitude() {
        Aircraft aircraft = DroneApplication.getAircraftInstance();
        if (aircraft == null) {
            return 0;
        }
        FlightController flightController = aircraft.getFlightController();
        if (flightController == null) {
            return 0;
        }
        if (!flightController.isConnected()) {
            return 0;
        }
        FlightControllerState state = flightController.getState();
        if (state == null) {
            return 0;
        }
        mAltitude = state.getAircraftLocation().getAltitude();
        return mAltitude;
    }

}
