package hk.hku.flight.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hk.hku.flight.BuildConfig;
import hk.hku.flight.account.AccountManager;
import hk.hku.flight.account.User;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class NetworkManager {
    public static final String baseUrl = BuildConfig.HTTP_BASE;

    private static class InstanceHolder {
        private static NetworkManager instance = new NetworkManager();
    }

    private NetworkService mService;

    public static NetworkManager getInstance() {
        return InstanceHolder.instance;
    }

    public NetworkManager() {
        Retrofit retrofit = new Retrofit
                .Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mService = retrofit.create(NetworkService.class);
    }

    public static class ImageRsp extends BaseResponse {
        public String urlSuffix;

        @Override
        public String toString() {
            return "ImageRsp{" +
                    "result='" + result + '\'' +
                    ", failReason='" + failReason + '\'' +
                    ", urlSuffix='" + urlSuffix + '\'' +
                    '}';
        }
    }

    public void uploadImage(File imageFile, BaseCallback<ImageRsp> callback) {
        String fileName = imageFile.getName();
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
        MultipartBody.Part part = MultipartBody.Part.createFormData("fileName", fileName, requestFile);
        mService.imageUpload(part).enqueue(callback);
    }

    public static class LoginResponse extends BaseResponse {
        public User user;
    }

    public void login(String email, String password, BaseCallback<LoginResponse> callback) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("email", email);
        userMap.put("password", password);
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("user", userMap);
        Gson gson = new Gson();
        String reqJson = gson.toJson(reqMap);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), reqJson);
        Call<LoginResponse> call = mService.login(requestBody);
        call.enqueue(callback);
    }

    public static class RegisterResponse extends BaseResponse {

    }

    public void register(String name, String email, String password, String avatar, BaseCallback<RegisterResponse> callback) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("avatar", avatar);
        userMap.put("password", password);
        userMap.put("desc", "");
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("user", userMap);
        Gson gson = new Gson();
        String reqJson = gson.toJson(reqMap);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), reqJson);
        Call<RegisterResponse> call = mService.register(requestBody);
        call.enqueue(callback);
    }

    public void startLive(String streamUrl, BaseCallback<BaseResponse> callback) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", AccountManager.getInstance().getUid());
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("user", userMap);
        reqMap.put("streamUrl", streamUrl);
        Gson gson = new Gson();
        String reqJson = gson.toJson(reqMap);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), reqJson);
        Call<BaseResponse> call = mService.startLive(requestBody);
        call.enqueue(callback);
    }

    public void stopLive(String streamUrl, BaseCallback<BaseResponse> callback) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", AccountManager.getInstance().getUid());
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("user", userMap);
        reqMap.put("streamUrl", streamUrl);
        Gson gson = new Gson();
        String reqJson = gson.toJson(reqMap);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), reqJson);
        Call<BaseResponse> call = mService.stopLive(requestBody);
        call.enqueue(callback);
    }

    public void stopAndSaveLive(String streamUrl, String name, String description, BaseCallback<BaseResponse> callback) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", AccountManager.getInstance().getUid());
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("user", userMap);
        reqMap.put("streamUrl", streamUrl);
        reqMap.put("name", name);
        reqMap.put("description", description);
        Gson gson = new Gson();
        String reqJson = gson.toJson(reqMap);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), reqJson);
        Call<BaseResponse> call = mService.stopAndSaveLive(requestBody);
        call.enqueue(callback);
    }

    public static class VideoListItem {
        public String id;
        public User user;
        public String name;
        public String description;
        public String url;
        public String resultUrl;
    }
    public static class VideoListResponse extends BaseResponse {
        public User user;
        public List<VideoListItem> videoList;
        public List<VideoListItem> urlRspList;
    }

    public void getRecordList(String uid, BaseCallback<VideoListResponse> callback) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", uid);
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("user", userMap);
        Gson gson = new Gson();
        String reqJson = gson.toJson(reqMap);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), reqJson);
        Call<VideoListResponse> call = mService.getRecordList(requestBody);
        call.enqueue(callback);
    }

    public void getLiveList(String uid, BaseCallback<VideoListResponse> callback) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("id", uid);
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("user", userMap);
        Gson gson = new Gson();
        String reqJson = gson.toJson(reqMap);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), reqJson);
        Call<VideoListResponse> call;
        if (TextUtils.isEmpty(uid)) {
            call = mService.getLiveList(requestBody);
        } else {
            call = mService.getLiveByUid(requestBody);
        }
        call.enqueue(callback);
    }

    public static class ResultNum {
        public int withMask;
        public int withoutMask;
        public int unKnown;
    }
    public static class DetectionResultResponse extends BaseResponse{
        public List<ResultNum> resultNumList;
    }

    public void getResultNum(String videoId, BaseCallback<DetectionResultResponse> callback) {
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("videoId", videoId);
        Gson gson = new Gson();
        String reqJson = gson.toJson(reqMap);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), reqJson);
        Call<DetectionResultResponse> call = mService.getResultNum(requestBody);
        call.enqueue(callback);
    }

    public interface NetworkService {
        @POST("register")
        Call<RegisterResponse> register(@Body RequestBody body);

        @POST("login")
        Call<LoginResponse> login(@Body RequestBody body);

        @POST("imageUpload")
        @Multipart
        Call<ImageRsp> imageUpload(@Part MultipartBody.Part imgs);

        @POST("startLive")
        Call<BaseResponse> startLive(@Body RequestBody body);

        @POST("stopLive")
        Call<BaseResponse> stopLive(@Body RequestBody body);

        @POST("stopAndSaveLive")
        Call<BaseResponse> stopAndSaveLive(@Body RequestBody body);

        @POST("getVideo")
        Call<VideoListResponse> getRecordList(@Body RequestBody body);

        @POST("getLiveList")
        Call<VideoListResponse> getLiveList(@Body RequestBody body);

        @POST("getLiveByUid")
        Call<VideoListResponse> getLiveByUid(@Body RequestBody body);

        @POST("getResultNum")
        Call<DetectionResultResponse> getResultNum(@Body RequestBody body);
    }

    public static class BaseResponse {
        public String result;
        public String failReason;
    }

    public abstract static class BaseCallback<T extends BaseResponse> implements Callback<T> {
        abstract public void onSuccess(T data);

        abstract public void onFail(String msg);

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            onFail(t.getMessage());
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (!response.isSuccessful()) {
                onFail("code:" + response.code());
            } else {
                if (response.body().result.equalsIgnoreCase("success")) {
                    onSuccess(response.body());
                } else {
                    onFail(response.body().failReason);
                }
            }
        }
    }
}
