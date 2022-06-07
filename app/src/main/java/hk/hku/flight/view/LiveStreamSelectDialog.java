package hk.hku.flight.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
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
import hk.hku.flight.R;
import hk.hku.flight.util.DensityUtil;
import hk.hku.flight.util.ThreadManager;
import hk.hku.flight.util.ToastUtil;

public class LiveStreamSelectDialog extends AlertDialog {
    private static final String TAG = "LiveStreamSelectDialog";
    private RecyclerView mRecyclerView;
    private LiveStreamAdapter mAdapter;
    private final List<String> mLiveStreamList = new ArrayList<>();
    private String mSelectUrl = null;

    public LiveStreamSelectDialog(Context context) {
        super(context);
        setOnShowListener(dialog -> init());
        mLiveStreamList.add("rtmp://192.168.0.105/live/livestream");
    }


    private void init() {
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
        mRecyclerView.setAdapter(mAdapter);

        findViewById(R.id.live_stream_select_ok).setOnClickListener(v -> {
            if (TextUtils.isEmpty(mSelectUrl)) {
                Toast.makeText(getContext(), "Please select a live stream first.", Toast.LENGTH_SHORT).show();
                return;
            }
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
                        ToastUtil.toast( "live start!");
                    } else {
                        ToastUtil.toast(String.format("live start fail(%d)", code));
                    }
                });
                Toast.makeText(getContext(), "starting live stream...", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        findViewById(R.id.live_stream_select_cancel).setOnClickListener(v -> dismiss());
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
            if (mSelectUrl != null && mSelectUrl.equals(url)) {
                mTvUrl.setBackgroundColor(Color.parseColor("#0061b2"));
            } else {
                mTvUrl.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }
}
