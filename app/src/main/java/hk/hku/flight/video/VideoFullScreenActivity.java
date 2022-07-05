package hk.hku.flight.video;

import static com.google.android.exoplayer2.ui.StyledPlayerView.SHOW_BUFFERING_ALWAYS;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import hk.hku.flight.R;

public class VideoFullScreenActivity extends AppCompatActivity {
    private static final String TAG = "VideoFullScreenActivity";
    public static final String KEY_URL = "KEY_URL";
    private static final int MODE_HTTP = 1;
    private static final int MODE_RTMP = 2;
    private StyledPlayerView mStyledPlayerView;
    private ExoPlayer mPlayer;
    private int mVideoMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_video_full_screen);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        prepareData();
        initView();
    }

    private void prepareData() {
        String videoUrl = getIntent().getStringExtra(KEY_URL);
//        videoUrl = "rtmp://36.134.145.85/live/abc";
        if (TextUtils.isEmpty(videoUrl)) {
            finish();
            return;
        }
        mPlayer = new ExoPlayer.Builder(VideoFullScreenActivity.this)
                .setLoadControl(new DefaultLoadControl())
                .build();
        if (videoUrl.startsWith("http")) {
            mPlayer.addMediaItem(MediaItem.fromUri(videoUrl));
            mVideoMode = MODE_HTTP;
        } else if (videoUrl.startsWith("rtmp")) {
            RtmpDataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();
            MediaItem mediaItem = MediaItem.fromUri(videoUrl);
            MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            mPlayer.addMediaSource(videoSource);
            mVideoMode = MODE_RTMP;
        }
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Log.i(TAG, "onPlayerError:" + error);
            }

            @Override
            public void onRenderedFirstFrame() {
                Log.i(TAG, "onRenderedFirstFrame");
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Log.i(TAG, "onPlaybackStateChanged:" + playbackState);
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
        mPlayer.setPlayWhenReady(true);
        mPlayer.prepare();
    }

    private void initView() {
        mStyledPlayerView = findViewById(R.id.surface_view);
        mStyledPlayerView.setPlayer(mPlayer);
        mStyledPlayerView.setShowNextButton(false);
        mStyledPlayerView.setShowPreviousButton(false);
        mStyledPlayerView.setShowBuffering(SHOW_BUFFERING_ALWAYS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }
}