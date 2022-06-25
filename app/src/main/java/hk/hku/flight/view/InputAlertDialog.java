package hk.hku.flight.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import hk.hku.flight.R;
import hk.hku.flight.util.DensityUtil;

public class InputAlertDialog extends AlertDialog {
    private OnInputCallback mOnInputCallback;
    private String mTitleText;
    private LinearLayout mEditTextContainer;
    private List<EditText> mEditTextList = new ArrayList<>();
    protected InputAlertDialog(@NonNull Context context) {
        super(context);
        setOnShowListener(dialog -> init());
    }

    public void setTitle(String title) {
        mTitleText = title;
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

        mEditTextContainer = findViewById(R.id.input_dialog_container);
        for (EditText et: mEditTextList) {
            mEditTextContainer.addView(et);
        }
        ((TextView)findViewById(R.id.input_dialog_title)).setText(mTitleText);

        findViewById(R.id.input_dialog_ok).setOnClickListener(v -> {
            if (mOnInputCallback != null) {
                List<String> result = new ArrayList<>();
                for(EditText et: mEditTextList) {
                    result.add(et.getText().toString());
                }
                mOnInputCallback.onClickOk(result);
            }
            dismiss();
        });
        findViewById(R.id.input_dialog_cancel).setOnClickListener(v -> dismiss());
    }

    public void addEditText(String defaultText, String hintText) {
        Window window = getWindow();
        EditText editText = new EditText(getContext());
        editText.setText(defaultText);
        editText.setHint(hintText);
        editText.setOnFocusChangeListener((view, focused) -> {
            if (focused) {
                //dialog弹出软键盘
                window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
        });
        editText.setTextColor(Color.WHITE);
        editText.setHeight(DensityUtil.dip2px(40));
        mEditTextList.add(editText);
    }

    public interface OnInputCallback {
        void onClickOk(List<String> results);
    }
}
