package hk.hku.flight.account;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import hk.hku.flight.R;
import hk.hku.flight.view.NetImageView;

public class AccountActivity extends AppCompatActivity {
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
    }
}