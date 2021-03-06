package hk.hku.flight.view;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hk.hku.flight.R;
import hk.hku.flight.video.VideoFullScreenActivity;

public class VideoListView extends FrameLayout {
    private RecyclerView mMainView;
    private View mEmptyView;
    private List<VideoItemData> mDataList = new ArrayList<>();
    private VideoListAdapter mAdapter;
    private OnItemClickListener onItemClickListener;

    public VideoListView(@NonNull Context context) {
        super(context);
        initView();
    }

    public VideoListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setData(List<VideoItemData> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        if (mDataList.isEmpty()) {
            mEmptyView.setVisibility(View.VISIBLE);
            mMainView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mMainView.setVisibility(View.VISIBLE);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.video_list_view, this, true);
        mMainView = findViewById(R.id.video_list_view_main);
        mEmptyView = findViewById(R.id.video_list_view_empty_view);
        mMainView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter = new VideoListAdapter();
        mMainView.setAdapter(mAdapter);
        mEmptyView.setVisibility(View.VISIBLE);
        mMainView.setVisibility(View.GONE);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    private class VideoListAdapter extends RecyclerView.Adapter<VideoListViewHolder> {

        @NonNull
        @Override
        public VideoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);
            return new VideoListViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoListViewHolder holder, int position) {
            holder.onBind(mDataList.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }
    }

    private class VideoListViewHolder extends RecyclerView.ViewHolder {
        private final NetImageView mVideoCover;
        private final TextView mVideoName;
        private final TextView mVideoDesc;
        private final NetImageView mUserAvatar;
        private final TextView mUserName;

        public VideoListViewHolder(@NonNull View itemView) {
            super(itemView);
            mVideoCover = itemView.findViewById(R.id.video_list_item_img);
            mVideoName = itemView.findViewById(R.id.video_list_item_name);
            mVideoDesc = itemView.findViewById(R.id.video_list_item_desc);
            mUserAvatar = itemView.findViewById(R.id.video_list_item_user_avatar);
            mUserName = itemView.findViewById(R.id.video_list_item_user_name);
        }

        public void onBind(VideoItemData data) {
            if (!TextUtils.isEmpty(data.url) && data.url.startsWith("rtmp")) {
                mVideoCover.loadDrawable(getContext().getResources().getDrawable(R.drawable.default_live));
            } else {
                mVideoCover.loadHttpVideoPreview(data.url);
            }
            mVideoName.setText(data.videoName);
            mVideoDesc.setText(data.videoDescription);
            mUserName.setText(data.userName);
            mUserAvatar.loadRound(data.userAvatarUrl);
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onClick(data);
                    return;
                }
                Intent intent = new Intent(getContext(), VideoFullScreenActivity.class);
                intent.putExtra(VideoFullScreenActivity.KEY_URL, data.url);
                getContext().startActivity(intent);
            });
        }
    }

    public interface OnItemClickListener {
        void onClick(VideoItemData data);
    }

    public static class VideoItemData {
        public String videoId;
        public String url;
        public String videoName;
        public String videoDescription;
        public String uid;
        public String userName;
        public String userAvatarUrl;
    }
}
