package hk.hku.flight.video;

import static com.google.android.exoplayer2.ui.StyledPlayerView.SHOW_BUFFERING_WHEN_PLAYING;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
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
            int vis = isFullScreen ? View.GONE : View.VISIBLE;
            mResultContainer.setVisibility(vis);
            mVideoListView.setVisibility(vis);
            mTitle.setVisibility(vis);
            ((ViewGroup) mStyledPlayerView.getParent()).setBackgroundColor(isFullScreen ? Color.BLACK : getResources().getColor(R.color.appback));
        });
        mVideoListView = findViewById(R.id.video_list_view);
        mVideoListView.setOnItemClickListener(data -> {
            playVideo(data.url);
            mTitleText.setText(data.videoName);
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        mResultContainer = findViewById(R.id.video_result_container);
        mTitle = findViewById(R.id.video_title);
        mTitleText = findViewById(R.id.tv_video_title);
        findViewById(R.id.btn_video_switch).setOnClickListener(v -> {
            if (mMode == MODE_RECORD) {
                mMode = MODE_LIVE;
                ((TextView)v).setText("Live Mode");
                stopPlay();
                getLiveList();
                mResultContainer.setVisibility(View.GONE);
            } else {
                mMode = MODE_RECORD;
                ((TextView)v).setText("Record Mode");
                stopPlay();
                getRecordList();
                mResultContainer.setVisibility(View.VISIBLE);
            }
        });
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
                        playVideo(dataList.get(0).url);
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
        NetworkManager.getInstance().getLiveList("0", new NetworkManager.BaseCallback<NetworkManager.VideoListResponse>() {
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
                        playVideo(dataList.get(0).url);
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
        mPlayer = new ExoPlayer.Builder(VideoActivity.this)
                .setLoadControl(new DefaultLoadControl())
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
}