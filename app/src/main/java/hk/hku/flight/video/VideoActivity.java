package hk.hku.flight.video;

import static com.google.android.exoplayer2.ui.StyledPlayerView.SHOW_BUFFERING_WHEN_PLAYING;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hk.hku.flight.BuildConfig;
import hk.hku.flight.R;
import hk.hku.flight.util.NetworkManager;
import hk.hku.flight.util.ThreadManager;
import hk.hku.flight.view.VideoListView;

public class VideoActivity extends AppCompatActivity {
    private static final String TAG = "VideoActivity";
    private static final int MODE_RECORD = 1;
    private static final int MODE_LIVE = 2;
    private VideoListView mVideoListView;
    private StyledPlayerView mStyledPlayerView;
    private View mResultContainer;
    private View mTitle;
    private TextView mTitleText;
    private ExoPlayer mPlayer;
    private int mMode = MODE_RECORD;
    private List<NetworkManager.ResultNum> mResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_video);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
        getRecordList();//优先播放回放
    }

    private void initView() {
        mStyledPlayerView = findViewById(R.id.surface_view);
        mStyledPlayerView.setShowNextButton(false);
        mStyledPlayerView.setShowPreviousButton(false);
        mStyledPlayerView.setShowBuffering(SHOW_BUFFERING_WHEN_PLAYING);
        mStyledPlayerView.setFullscreenButtonClickListener(isFullScreen -> {
            setFullScreen(isFullScreen);
        });
        mVideoListView = findViewById(R.id.video_list_view);
        mVideoListView.setOnItemClickListener(data -> {
            playVideo(data.url);
            getVideoResult(data.videoId);
            mTitleText.setText(data.videoName);
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        mResultContainer = findViewById(R.id.video_result_container);
        mTitle = findViewById(R.id.video_title);
        mTitleText = findViewById(R.id.tv_video_title);
        findViewById(R.id.btn_video_switch).setOnClickListener(v -> {
            if (mMode == MODE_RECORD) {
                mMode = MODE_LIVE;
                ((TextView) v).setText("Live Mode");
                mTitleText.setText("Live Mode");
                stopPlay();
                getLiveList();
                mResultContainer.setVisibility(View.GONE);
            } else {
                mMode = MODE_RECORD;
                ((TextView) v).setText("Record Mode");
                mTitleText.setText("Record Mode");
                stopPlay();
                getRecordList();
                mResultContainer.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setFullScreen(boolean isFullScreen) {
        int vis = isFullScreen ? View.GONE : View.VISIBLE;
        mResultContainer.setVisibility(vis);
        mVideoListView.setVisibility(vis);
        mTitle.setVisibility(vis);
        ((ViewGroup) mStyledPlayerView.getParent()).setBackgroundColor(isFullScreen ? Color.BLACK : getResources().getColor(R.color.appback));
    }

    private void getRecordList() {
        mVideoListView.setData(new ArrayList<>());
        NetworkManager.getInstance().getRecordList("0", new NetworkManager.BaseCallback<NetworkManager.VideoListResponse>() {
            @Override
            public void onSuccess(NetworkManager.VideoListResponse data) {
                Log.i(TAG, "getRecordList onSuccess");
                List<VideoListView.VideoItemData> dataList = new ArrayList<>();
                for (NetworkManager.VideoListItem item : data.videoList) {
                    VideoListView.VideoItemData d = new VideoListView.VideoItemData();
                    d.videoId = item.id;
                    d.videoName = item.name;
                    d.videoDescription = item.description;
                    d.url = BuildConfig.HTTP_BASE + File.separator + item.url;
                    d.uid = item.user.id;
                    d.userName = item.user.name;
                    d.userAvatarUrl = item.user.avatar;
                    dataList.add(d);
                }
                ThreadManager.getInstance().runOnUiThread(() -> {
                    mVideoListView.setData(dataList);
                    if (dataList.size() > 0) {
//                        playVideo(dataList.get(0).url);
//                        getVideoResult(dataList.get(0).videoId);
                        mTitleText.setText(dataList.get(0).videoName);
                    }
                });
            }

            @Override
            public void onFail(String msg) {
                Log.i(TAG, "getRecordList onFail:" + msg);
            }
        });
    }

    private void getLiveList() {
        mVideoListView.setData(new ArrayList<>());
        mResultList = null;
        NetworkManager.getInstance().getLiveList("", new NetworkManager.BaseCallback<NetworkManager.VideoListResponse>() {
            @Override
            public void onSuccess(NetworkManager.VideoListResponse data) {
                Log.i(TAG, "getLiveList onSuccess");
                List<VideoListView.VideoItemData> dataList = new ArrayList<>();
                for (NetworkManager.VideoListItem item : data.urlRspList) {
                    VideoListView.VideoItemData d = new VideoListView.VideoItemData();
                    d.videoName = item.resultUrl;
//                    d.videoDescription = item.resultUrl;
                    d.url = item.resultUrl;
                    d.uid = item.user.id;
                    d.userName = item.user.name;
                    d.userAvatarUrl = item.user.avatar;
                    dataList.add(d);
                }
                ThreadManager.getInstance().runOnUiThread(() -> {
                    mVideoListView.setData(dataList);
                    if (dataList.size() > 0) {
//                        playVideo(dataList.get(0).url);
                        mTitleText.setText(dataList.get(0).videoName);
                    }
                });
            }

            @Override
            public void onFail(String msg) {
                Log.i(TAG, "getLiveList onFail:" + msg);
            }
        });
    }

    private void stopPlay() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        mStyledPlayerView.setPlayer(null);
    }

    private void playVideo(String videoUrl) {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
        DefaultLoadControl dl = new DefaultLoadControl.Builder()
                .setBackBuffer(10000, true)
                .setBufferDurationsMs(3000, 30000,3000, 3000)
                .build();
        mPlayer = new ExoPlayer.Builder(VideoActivity.this)
                .setLoadControl(dl)
                .build();
        if (videoUrl.startsWith("http")) {
            mPlayer.addMediaItem(MediaItem.fromUri(videoUrl));
        } else if (videoUrl.startsWith("rtmp")) {
            RtmpDataSource.Factory dataSourceFactory = new RtmpDataSource.Factory();
            MediaItem mediaItem = MediaItem.fromUri(videoUrl);
            MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
            mPlayer.addMediaSource(videoSource);
        }
        mStyledPlayerView.setPlayer(mPlayer);
        mPlayer.setPlayWhenReady(true);
        mPlayer.addListener(new Player.Listener() {
            @Override
            public void onRenderedFirstFrame() {
                mStyledPlayerView.hideController();
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Log.i(TAG, "onPlaybackStateChanged:" + playbackState);
            }
        });
        mPlayer.prepare();
    }

    private void getVideoResult(String videoId) {
        Log.i(TAG, "getVideoResult");
        mResultList = null;
        NetworkManager.getInstance().getResultNum(videoId, new NetworkManager.BaseCallback<NetworkManager.DetectionResultResponse>() {
            @Override
            public void onSuccess(NetworkManager.DetectionResultResponse data) {
                Log.i(TAG, "getVideoResult success");
                mResultList = data.resultNumList;
                mHandler.post(mProcessRunnable);
            }

            @Override
            public void onFail(String msg) {
                Log.i(TAG, "getVideoResult fail:" + msg);
            }
        });
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mProcessRunnable = new Runnable() {
        @Override
        public void run() {
            startGetPosition();
            mHandler.postDelayed(this, 200);
        }
    };

    private void startGetPosition() {
        if (mPlayer != null && mResultList != null) {
            mPlayer.getDuration();
            int index = (int) (mPlayer.getCurrentPosition() / 1000);
            if (index > 0 && index < mResultList.size()) {
                NetworkManager.ResultNum num = mResultList.get(index);
                Log.i(TAG, "update current stat:" + index + "/" + mResultList.size());
                ((TextView) findViewById(R.id.tv_with_mask)).setText(getNum(num.withMask));
                ((TextView) findViewById(R.id.tv_without_mask)).setText(getNum(num.withoutMask));
                ((TextView) findViewById(R.id.tv_unknown_mask)).setText(getNum(num.unKnown));
            }
        }
    }

    private static String getNum(int num) {
        if (num < 0) {
            num = 0;
        }
        return String.valueOf(num);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mProcessRunnable);
    }
}