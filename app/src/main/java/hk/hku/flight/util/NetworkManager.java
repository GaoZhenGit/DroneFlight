package hk.hku.flight.util;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import hk.hku.flight.BuildConfig;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

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

    public static class LoginResponse extends BaseResponse {

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

    public void register(String name, String email, BaseCallback<RegisterResponse> callback) {
        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
//        userMap.put("avatar", avatar);
//        userMap.put("password", psw);
        userMap.put("desc", "");
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("user", userMap);
        Gson gson = new Gson();
        String reqJson = gson.toJson(reqMap);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), reqJson);
        Call<RegisterResponse> call = mService.register(requestBody);
        call.enqueue(callback);
    }

    public interface NetworkService {
        @POST("register")
        Call<RegisterResponse> register(@Body RequestBody body);

        @POST("login")
        Call<LoginResponse> login(@Body RequestBody body);
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
