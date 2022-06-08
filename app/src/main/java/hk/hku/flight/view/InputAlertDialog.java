package hk.hku.flight.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import hk.hku.flight.R;
import hk.hku.flight.util.DensityUtil;

public class InputAlertDialog extends AlertDialog {
    private OnInputCallback mOnInputCallback;
    private String mTitleText;
    private String mDefaultText;
    private EditText mEditText;
    protected InputAlertDialog(@NonNull Context context) {
        super(context);
        setOnShowListener(dialog -> init());
    }

    public void setTitle(String title) {
        mTitleText = title;
    }
    public void setDefaultText(String s) {
        mDefaultText = s;
    }
    public void setOnInputCallback(OnInputCallback onInputCallback) {
        mOnInputCallback = onInputCallback;
    }
    private void init() {
        setContentView(R.layout.input_alert_dialog);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.setLayout(DensityUtil.dip2px(480), WindowManager.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0f;
        window.setAttributes(lp);

        mEditText = findViewById(R.id.input_dialog_edittext);
        mEditText.setText(mDefaultText);
        mEditText.setOnFocusChangeListener((view, focused) -> {
            if (focused) {
                //dialog弹出软键盘
                window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
        });
        ((TextView)findViewById(R.id.input_dialog_title)).setText(mTitleText);

        findViewById(R.id.input_dialog_ok).setOnClickListener(v -> {
            if (mOnInputCallback != null) {
                mOnInputCallback.onClickOk(mEditText.getText().toString());
            }
            dismiss();
        });
        findViewById(R.id.input_dialog_cancel).setOnClickListener(v -> dismiss());
    }

    public interface OnInputCallback {
        void onClickOk(String result);
    }
}
