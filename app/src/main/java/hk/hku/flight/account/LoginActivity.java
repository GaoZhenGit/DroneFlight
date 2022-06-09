package hk.hku.flight.account;

import androidx.appcompat.app.AppCompatActivity;

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
import hk.hku.flight.util.ToastUtil;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private final String REGEX = "^\\w+((-\\w+)|(\\.\\w+))*@\\w+(\\.\\w{2,3}){1,3}$";
    private final Pattern mPatten = Pattern.compile(REGEX);
    private View mBtnNext;
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

        mBtnNext.setOnClickListener(v -> {
            ToastUtil.toast("login...");
            String email = mEmailField.getText().toString();
            String password = mPasswordField.getText().toString();
            if (mIsRegisterMode) {
                onRegister(email, password);
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
        } else {
            mTitle.setText("Login");
            mChangeModeBtn.setVisibility(View.VISIBLE);
        }
    }

    private void onLogin(String email, String password) {
        Log.i(TAG, "onLogin:" + email + ":" + password);
        NetworkManager.getInstance().login(email, password, new NetworkManager.BaseCallback<NetworkManager.LoginResponse>() {
            @Override
            public void onSuccess(NetworkManager.LoginResponse data) {

            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

    private void onRegister(String email, String password) {
        Log.i(TAG, "onRegister:" + email + ":" + password);
        NetworkManager.getInstance().register(email, password, new NetworkManager.BaseCallback<NetworkManager.RegisterResponse>() {
            @Override
            public void onSuccess(NetworkManager.RegisterResponse data) {

            }

            @Override
            public void onFail(String msg) {

            }
        });
    }

    public static void checkLogin(Runnable runnable) {

    }
}