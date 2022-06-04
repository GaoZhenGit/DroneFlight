package hk.hku.flight.modules;

import android.content.Intent;
import android.util.Log;

import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.LocationCoordinate3D;
import dji.common.model.LocationCoordinate2D;
import dji.common.util.LocationUtils;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;
import dji.sdk.util.LocationUtil;
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
            updateStates(flightControllerState);
            Log.i(TAG, "altitude change:" + mAltitude);
            Intent intent = new Intent(Constant.FLAG_FLIGHT_CONTROL_STATE);
            DroneApplication.getInstance().sendBroadcast(intent);
        });
    }

    private float mAltitude = 0;
    private double mDistance = 0;
    private double mHorizonV = 0;
    private float mVerticalV = 0;
    private int mSatelliteCount = 0;

    public float getAltitude() {
        FlightControllerState state = check();
        if (state == null) {
            return 0;
        } else {
            updateStates(state);
        }
        return mAltitude;
    }

    public double getDistance() {
        FlightControllerState state = check();
        if (state == null) {
            return 0;
        } else {
            updateStates(state);
        }
        return mDistance;
    }

    public double getHorizonV() {
        FlightControllerState state = check();
        if (state == null) {
            return 0;
        } else {
            updateStates(state);
        }
        return mHorizonV;
    }

    public float getVerticalV() {
        FlightControllerState state = check();
        if (state == null) {
            return 0;
        } else {
            updateStates(state);
        }
        return mVerticalV;
    }

    public int getSatelliteCount() {
        FlightControllerState state = check();
        if (state == null) {
            return 0;
        } else {
            updateStates(state);
        }
        return mSatelliteCount;
    }

    private FlightControllerState check() {
        Aircraft aircraft = DroneApplication.getAircraftInstance();
        if (aircraft == null) {
            return null;
        }
        FlightController flightController = aircraft.getFlightController();
        if (flightController == null) {
            return null;
        }
        if (!flightController.isConnected()) {
            return null;
        }
        return flightController.getState();
    }

    private void updateStates(FlightControllerState state) {
        mAltitude = state.getAircraftLocation().getAltitude();
        LocationCoordinate2D home = state.getHomeLocation();
        LocationCoordinate3D drone = state.getAircraftLocation();
        LocationCoordinate2D drone2d = new LocationCoordinate2D(drone.getLatitude(), drone.getLongitude());
        mDistance = LocationUtils.gps2m(home, drone2d);
        mVerticalV = state.getVelocityZ();
        mHorizonV = Math.sqrt(Math.pow(state.getVelocityX(), 2) + Math.pow(state.getVelocityY(), 2));
        mSatelliteCount = state.getSatelliteCount();
    }

}
