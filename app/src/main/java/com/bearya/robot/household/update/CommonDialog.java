package com.bearya.robot.household.update;


import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.views.BaseDialog;

/**
 * Created by yexifeng on 17/9/22.
 */

public class CommonDialog extends BaseDialog implements View.OnClickListener {
    private TextView mTvMsg;
    private TextView mBtnUpdate;
    private TextView mBtnCancel;
    private View bgView;

    private String mMsg;

    public void setListener(DialogUpdateListener listener) {
        this.mListener = listener;
    }

    DialogUpdateListener mListener;

    protected CommonDialog(Context context) {
        super(context, R.layout.dialog_common, false);
    }

    public CommonDialog(Context context, String msg) {
        super(context, R.layout.dialog_common, false);
        this.mMsg = msg;
    }

    @Override
    protected void initSubView() {
        bgView = findViewById(R.id.bgView);
        mTvMsg = (TextView) findViewById(R.id.tvContent);
        mBtnCancel = (TextView) findViewById(R.id.btnCancel);
        mBtnUpdate = (TextView) findViewById(R.id.btnOk);

        mBtnCancel.setOnClickListener(this);
        mBtnUpdate.setOnClickListener(this);

        mTvMsg.setText(mMsg);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnCancel){
            if(mListener!=null){
                mListener.onCancel();
            }
        }else if(v.getId() == R.id.btnOk){
            if(mListener!=null){
                mListener.onUpdate();
            }

        }
        dismiss();
    }

    public interface DialogUpdateListener {
        void onUpdate();
        void onCancel();
    }
}
