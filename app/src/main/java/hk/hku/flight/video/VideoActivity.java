package hk.hku.flight.video;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.ExoPlayer;
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
    private VideoListView mVideoListView;
    private StyledPlayerView mStyledPlayerView;
    private ExoPlayer mPlayer;
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
        mVideoListView = findViewById(R.id.video_list_view);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void getRecordList() {
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
                ThreadManager.getInstance().runOnUiThread(() -> mVideoListView.setData(dataList));
            }

            @Override
            public void onFail(String msg) {
                Log.i(TAG, "getRecordList onFail:" + msg);
            }
        });
    }

    private void getLiveList() {
        NetworkManager.getInstance().getLiveList("0", new NetworkManager.BaseCallback<NetworkManager.VideoListResponse>() {
            @Override
            public void onSuccess(NetworkManager.VideoListResponse data) {

            }

            @Override
            public void onFail(String msg) {

            }
        });
    }
}