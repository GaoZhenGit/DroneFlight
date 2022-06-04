package hk.hku.flight;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import dji.common.airlink.SignalQualityCallback;
import dji.midware.component.DJIComponentManager;
import dji.sdk.airlink.AirLink;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.payload.Payload;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import hk.hku.flight.util.VideoFeedView;

public class FlightActivity extends AppCompatActivity {

    private VideoFeedView videoFeedView;
//    private TextureView textureView;
    private Payload payload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_flight);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
    }

    private void initView() {
        videoFeedView = findViewById(R.id.preview_view);
        videoFeedView.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
        Aircraft aircraft = DroneApplication.getAircraftInstance();
        if (aircraft == null) {
            Toast.makeText(this, "connection loss!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        VideoFeeder.getInstance().getPrimaryVideoFeed().addVideoDataListener(new VideoFeeder.VideoDataListener() {
            @Override
            public void onReceive(byte[] bytes, int i) {

            }
        });

    }
}