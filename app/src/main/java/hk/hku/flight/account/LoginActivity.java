package hk.hku.flight.account;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hk.hku.flight.R;
import hk.hku.flight.util.NetworkManager;
import hk.hku.flight.util.ThreadManager;
import hk.hku.flight.util.ToastUtil;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private final String REGEX = "^\\w+((-\\w+)|(\\.\\w+))*@\\w+(\\.\\w{2,3}){1,3}$";
    private final Pattern mPatten = Pattern.compile(REGEX);
    private View mBtnNext;
    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private TextView mTitle;
    private TextView mChangeModeBtn;
    private boolean mIsRegisterMode = false;
    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkInput();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_login);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
    }

    private void initView() {
        mBtnNext = findViewById(R.id.login_ok);
        mEmailField = findViewById(R.id.login_email);
        mEmailField.addTextChangedListener(mTextWatcher);

        mPasswordField = findViewById(R.id.login_password);
        mPasswordField.addTextChangedListener(mTextWatcher);

        mNameField = findViewById(R.id.login_name);
        mNameField.addTextChangedListener(mTextWatcher);

        mBtnNext.setOnClickListener(v -> {
            String email = mEmailField.getText().toString();
            String password = mPasswordField.getText().toString();
            if (mIsRegisterMode) {
                String name = mNameField.getText().toString();
                onRegister(name, email, password);
            } else {
                onLogin(email, password);
            }
        });

        mIsRegisterMode = false;
        mTitle = findViewById(R.id.login_title);

        mChangeModeBtn = findViewById(R.id.login_change_mode);
        mChangeModeBtn.setOnClickListener(v -> changeMode(true));
        String word = "New user? Create an account.";
        SpannableString changeModeWord = new SpannableString(word);
        ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor("#0000cc"));
        changeModeWord.setSpan(span, word.indexOf("Create"), word.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        mChangeModeBtn.setText(changeModeWord);
    }

    private void checkInput() {
        boolean inputsValid = true;
        String email = mEmailField.getText().toString();
        Matcher matcher = mPatten.matcher(email);
        if (TextUtils.isEmpty(email)) {
            mEmailField.setBackgroundResource(R.drawable.default_background_round);
            inputsValid = false;
        } else if (matcher.matches()) {
            mEmailField.setBackgroundResource(R.drawable.default_background_round);
        } else {
            mEmailField.setBackgroundResource(R.drawable.invalid_input);
            inputsValid = false;
        }
        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            inputsValid = false;
            mPasswordField.setBackgroundResource(R.drawable.invalid_input);
        } else {
            mPasswordField.setBackgroundResource(R.drawable.default_background_round);
        }

        if (mIsRegisterMode) {
            String name = mNameField.getText().toString();
            if (TextUtils.isEmpty(name)) {
                inputsValid = false;
                mNameField.setBackgroundResource(R.drawable.invalid_input);
            } else {
                mNameField.setBackgroundResource(R.drawable.default_background_round);
            }
        }
        mBtnNext.setEnabled(inputsValid);
    }

    @Override
    public void onBackPressed() {
        if (mIsRegisterMode) {
            changeMode(false);
        } else {
            super.onBackPressed();
        }
    }

    private void changeMode(boolean isRegister) {
        mIsRegisterMode = isRegister;
        if (isRegister) {
            mTitle.setText("Register");
            mChangeModeBtn.setVisibility(View.GONE);
            mNameField.setVisibility(View.VISIBLE);
        } else {
            mTitle.setText("Login");
            mChangeModeBtn.setVisibility(View.VISIBLE);
            mNameField.setVisibility(View.GONE);
        }
    }

    private void onLogin(String email, String password) {
        Log.i(TAG, "onLogin:" + email + ":" + password);
        ToastUtil.toast("login...");
        mBtnNext.setEnabled(false);
        NetworkManager.getInstance().login(email, password, new NetworkManager.BaseCallback<NetworkManager.LoginResponse>() {
            @Override
            public void onSuccess(NetworkManager.LoginResponse data) {
                Log.i(TAG, "onLogin success");
                AccountManager.getInstance().setLocalUserInfo(
                        data.user.id,
                        data.user.name,
                        data.user.email,
                        data.user.avatar);
                ThreadManager.getInstance().runOnUiThread(() -> {
                    ToastUtil.toast("login success");
                    setResult(RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onFail(String msg) {
                ToastUtil.toast("login fail:" + msg);
                ThreadManager.getInstance().runOnUiThread(() -> checkInput());
            }
        });
    }

    private void onRegister(String name, String email, String password) {
        Log.i(TAG, "onRegister:" + email + ":" + password);
        ToastUtil.toast("creating account...");
        mBtnNext.setEnabled(false);
        NetworkManager.getInstance().register(name, email, password, new NetworkManager.BaseCallback<NetworkManager.RegisterResponse>() {
            @Override
            public void onSuccess(NetworkManager.RegisterResponse data) {
                Log.i(TAG, "onRegister success");
                ThreadManager.getInstance().runOnUiThread(() -> {
                    changeMode(false);
                    checkInput();
                    ToastUtil.toast("creating account success");
                });
            }

            @Override
            public void onFail(String msg) {
                Log.i(TAG, "onRegister fail");
                ToastUtil.toast("creating account fail:" + msg);
                ThreadManager.getInstance().runOnUiThread(() -> checkInput());
            }
        });
    }

    public static void checkLogin(Activity activity, Runnable runnable) {
        if (AccountManager.getInstance().isLogin()) {
            String name = AccountManager.getInstance().getUserName();
            Log.i(TAG, "checkLogin as:" + name);
            if (runnable != null) {
                runnable.run();
            }
        } else {
            Log.i(TAG, "checkLogin false");
            activity.startActivity(new Intent(activity, LoginActivity.class));
        }
    }
}