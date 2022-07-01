package hk.hku.flight.video;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;

import hk.hku.flight.R;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";
    private SurfaceView mSurfaceView;
    private ExoPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_video);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prepareData();
        initView();
    }

    private void prepareData() {
        mPlayer = new ExoPlayer.Builder(VideoActivity.this).build();
//        mPlayer.addMediaItem(MediaItem.fromUri("https://stream7.iqilu.com/10339/upload_transcode/202002/18/20200218114723HDu3hhxqIT.mp4"));
        RtmpDataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();
        MediaItem mediaItem = MediaItem.fromUri("rtmp://172.16.30.80/live/abc");
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
        mPlayer.addMediaSource(videoSource);
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Log.i(TAG, "onPlayerError:" + error);
            }

            @Override
            public void onEvents(Player player, Player.Events events) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < events.size(); i++) {
                    sb.append(events.get(i)).append(",");
                }
                Log.i(TAG, "onEvents:" + sb.toString());
            }
        });
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                Log.i(TAG, "surfaceCreated");
                mPlayer.setVideoSurface(holder.getSurface());
                mPlayer.prepare();
                mPlayer.play();
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                Log.i(TAG, "surfaceChanged");
                mPlayer.setVideoSurface(holder.getSurface());
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                Log.i(TAG, "surfaceDestroyed");
                mPlayer.stop();
                mPlayer.release();
            }
        });
    }


}