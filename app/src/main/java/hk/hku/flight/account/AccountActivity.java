package hk.hku.flight.account;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hk.hku.flight.BuildConfig;
import hk.hku.flight.R;
import hk.hku.flight.util.NetworkManager;
import hk.hku.flight.util.ThreadManager;
import hk.hku.flight.view.NetImageView;
import hk.hku.flight.view.VideoListView;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccountActivity";
    private VideoListView mRecordVideoListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_account);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
    }

    private void initView() {
        NetImageView avatar = findViewById(R.id.account_avatar);
        avatar.loadRound(AccountManager.getInstance().getAvatar());

        TextView userName = findViewById(R.id.account_user_name);
        userName.setText(AccountManager.getInstance().getUserName());

        TextView userEmail = findViewById(R.id.account_user_email);
        userEmail.setText(AccountManager.getInstance().getEmail());

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            AccountManager.getInstance().clearUserInfo();
            finish();
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        mRecordVideoListView = findViewById(R.id.live_record_listview);
        NetworkManager.getInstance().getRecordList(AccountManager.getInstance().getUid(), new NetworkManager.BaseCallback<NetworkManager.VideoListResponse>() {
            @Override
            public void onSuccess(NetworkManager.VideoListResponse data) {
                Log.i(TAG, "onSuccess");
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
                ThreadManager.getInstance().runOnUiThread(() -> mRecordVideoListView.setData(dataList));
            }

            @Override
            public void onFail(String msg) {
                Log.i(TAG, "onFail:" + msg);
            }
        });
    }
}