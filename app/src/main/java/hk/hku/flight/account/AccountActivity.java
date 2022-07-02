package hk.hku.flight.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

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
    private VideoListView mLiveListView;
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private TabLayoutMediator mTabLayoutMediator;
    private VideoListPageAdapter mPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_account);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
        loadData();
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
        mTabLayout = findViewById(R.id.account_list_tab);
        mViewPager = findViewById(R.id.account_list_pager);
        mPageAdapter = new VideoListPageAdapter();
        mViewPager.setAdapter(mPageAdapter);
        mTabLayout.setTabTextColors(getColor(android.R.color.darker_gray),Color.BLACK);
        mTabLayout.setSelectedTabIndicator(null);
        mTabLayoutMediator = new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Record");
                    break;
                case 1:
                    tab.setText("Live");
                    break;
            }
        });
        mTabLayoutMediator.attach();
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                onPageSelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        onPageSelect(0);
        mRecordVideoListView = new VideoListView(this);
        mLiveListView = new VideoListView(this);
    }

    private void onPageSelect(int index) {

    }

    private void loadData() {
        NetworkManager.getInstance().getRecordList(AccountManager.getInstance().getUid(), new NetworkManager.BaseCallback<NetworkManager.VideoListResponse>() {
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
                ThreadManager.getInstance().runOnUiThread(() -> mRecordVideoListView.setData(dataList));
            }

            @Override
            public void onFail(String msg) {
                Log.i(TAG, "getRecordList onFail:" + msg);
            }
        });

        NetworkManager.getInstance().getLiveList(AccountManager.getInstance().getUid(), new NetworkManager.BaseCallback<NetworkManager.VideoListResponse>() {
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
                ThreadManager.getInstance().runOnUiThread(() -> mLiveListView.setData(dataList));
            }

            @Override
            public void onFail(String msg) {
                Log.i(TAG, "getLiveList onFail:" + msg);
            }
        });
    }

    private class VideoListPageAdapter extends RecyclerView.Adapter<VideoListPageViewHolder> {

        @NonNull
        @Override
        public VideoListPageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 1) {
                mLiveListView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
                return new VideoListPageViewHolder(mLiveListView);
            } else {
                mRecordVideoListView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
                return new VideoListPageViewHolder(mRecordVideoListView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull VideoListPageViewHolder holder, int position) {

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private static class VideoListPageViewHolder extends RecyclerView.ViewHolder {

        public VideoListPageViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}