package hk.hku.flight.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.sdkmanager.LiveStreamManager;
import dji.sdk.sdkmanager.LiveVideoBitRateMode;
import dji.sdk.sdkmanager.LiveVideoResolution;
import hk.hku.flight.BuildConfig;
import hk.hku.flight.FlightActivity;
import hk.hku.flight.R;
import hk.hku.flight.account.AccountManager;
import hk.hku.flight.util.DensityUtil;
import hk.hku.flight.util.NetworkManager;
import hk.hku.flight.util.SharePreferenceUtil;
import hk.hku.flight.util.ThreadManager;
import hk.hku.flight.util.ToastUtil;
import hk.hku.flight.util.Utils;

public class LiveStreamSelectDialog extends AlertDialog {
    private static final String TAG = "LiveStreamSelectDialog";
    private static final String KEY_STREAM_URLS = "KEY_STREAM_URLS";
    private RecyclerView mRecyclerView;
    private LiveStreamAdapter mAdapter;
    private final List<String> mLiveStreamList = new ArrayList<>();
    private String mSelectUrl = null;
    private LiveStreamManager mLiveStreamManager;
    private Activity mActivity;

    public LiveStreamSelectDialog(Activity context) {
        super(context);
        mActivity = context;
        setOnShowListener(dialog -> initView());
        initData();
    }

    private void initData() {
        mLiveStreamList.addAll(SharePreferenceUtil.getList(KEY_STREAM_URLS));
        String df = BuildConfig.RTMP_BASE + Utils.generateMD5(AccountManager.getInstance().getUid());
        if (!mLiveStreamList.contains(df)) {
            mLiveStreamList.add(df);
        }
    }

    private void initView() {
        setContentView(R.layout.live_stream_select_dialog);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.setLayout(DensityUtil.dip2px(320), DensityUtil.dip2px(280));
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0f;
        window.setAttributes(lp);

        mRecyclerView = findViewById(R.id.live_stream_select_dialog_recycerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mAdapter = new LiveStreamAdapter();
        mLiveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
        if (mLiveStreamManager != null) {
            mSelectUrl = mLiveStreamManager.getLiveUrl();
        }
        mRecyclerView.setAdapter(mAdapter);

        findViewById(R.id.live_stream_select_ok).setOnClickListener(v -> {
            if (TextUtils.isEmpty(mSelectUrl)) {
                Toast.makeText(getContext(), "Please select a live stream first.", Toast.LENGTH_SHORT).show();
                return;
            }
            AccountManager.getInstance().checkLogin(mActivity, this::startLive);
        });
        findViewById(R.id.live_stream_select_cancel).setOnClickListener(v -> dismiss());
        findViewById(R.id.live_stream_select_dialog_add).setOnClickListener(v -> {
            InputAlertDialog dialog = new InputAlertDialog(getContext());
            dialog.addEditText("rtmp://", "");
            dialog.setTitle("input your rtmp url");
            dialog.setOnInputCallback(result -> {
                String ret = result.get(0);
                if (!mLiveStreamList.contains(ret)) {
                    mLiveStreamList.add(ret);
                    SharePreferenceUtil.setList(KEY_STREAM_URLS, mLiveStreamList);
                    mAdapter.notifyDataSetChanged();
                }
            });
            dialog.show();
        });
    }

    private void startLive() {
        LiveStreamManager liveStreamManager = DJISDKManager.getInstance().getLiveStreamManager();
        if (liveStreamManager != null) {
            liveStreamManager.setLiveUrl(mSelectUrl);
            Log.i(TAG, "live stream change:" + mSelectUrl);
            liveStreamManager.setLiveVideoBitRateMode(LiveVideoBitRateMode.AUTO);
            liveStreamManager.setLiveVideoResolution(LiveVideoResolution.VIDEO_RESOLUTION_1280_720);
            liveStreamManager.setAudioMuted(true);
            liveStreamManager.setVideoSource(LiveStreamManager.LiveStreamVideoSource.Primary);
            ThreadManager.getInstance().submit(() -> {
                int code = liveStreamManager.startStream();
                Log.i(TAG, "LiveStreamManager live stream start:" + code);
                if (code == LiveStreamManager.STATUS_STREAMING) {
                    NetworkManager.getInstance().startLive(mSelectUrl, new NetworkManager.BaseCallback<NetworkManager.BaseResponse>() {
                        @Override
                        public void onSuccess(NetworkManager.BaseResponse data) {
                            ToastUtil.toast("live start!");
                        }

                        @Override
                        public void onFail(String msg) {
                            liveStreamManager.stopStream();
                            ToastUtil.toast(String.format("live start fail(%s)", msg));
                        }
                    });
                } else {
                    ToastUtil.toast(String.format("live start fail(%d)", code));
                }
            });
            Toast.makeText(getContext(), "starting live stream...", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private class LiveStreamAdapter extends RecyclerView.Adapter<LiveStreamViewHolder> {

        @NonNull
        @Override
        public LiveStreamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_stream_select_dialog_item, parent, false);
            return new LiveStreamViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LiveStreamViewHolder holder, int position) {
            holder.onBind(mLiveStreamList.get(position));
        }

        @Override
        public int getItemCount() {
            return mLiveStreamList.size();
        }
    }

    private class LiveStreamViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTvUrl;

        public LiveStreamViewHolder(@NonNull View itemView) {
            super(itemView);
            mTvUrl = itemView.findViewById(R.id.live_stream_select_item_url);

        }

        public void onBind(String url) {
            mTvUrl.setText(url);
            mTvUrl.setOnClickListener(v -> {
                mSelectUrl = url;
                mAdapter.notifyDataSetChanged();
            });
            mTvUrl.setOnLongClickListener(v -> {
                SpannableString spannableString = new SpannableString("DELETE");
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, spannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                        .setTitle("delete url?")
                        .setPositiveButton(spannableString, (dialog, which) -> {
                            mLiveStreamList.remove(url);
                            mAdapter.notifyDataSetChanged();
                            SharePreferenceUtil.setList(KEY_STREAM_URLS, mLiveStreamList);
                        })
                        .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss()).show();
                return true;
            });
            if (mSelectUrl != null && mSelectUrl.equals(url)) {
                mTvUrl.setBackgroundColor(Color.parseColor("#0061b2"));
            } else {
                mTvUrl.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }
}
