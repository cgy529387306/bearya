package com.bearya.robot.household.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.bearya.robot.household.R;

/**
 * Created by yexifeng on 17/3/12.
 */

public abstract class BaseDialog extends Dialog {
    private View blankView;
    private LinearLayout mContentView;
    private int mLayoutId;
    private boolean mTouchBlankCancel;
    protected Context mContext;

    protected BaseDialog(Context context, int layoutId, boolean touchBlankCancel) {
        super(context,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        mContext = context;
        mLayoutId = layoutId;
        mTouchBlankCancel = touchBlankCancel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_base);
        initView();
        initSubView();
    }

    protected abstract void initSubView();

    private void initView(){
        if(mTouchBlankCancel) {
            blankView = findViewById(R.id.blankView);
            blankView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
        }
        mContentView = (LinearLayout) findViewById(R.id.contentView);
        mContentView.addView(LayoutInflater.from(getContext()).inflate(mLayoutId,null));
    }

    @Override
    public void show() {
        getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        super.show();
    }
}
