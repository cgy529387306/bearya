package com.bearya.robot.household.update;


import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.views.BaseDialog;

/**
 * Created by yexifeng on 17/9/22.
 */

public class DialogUpdateNotice extends BaseDialog implements View.OnClickListener {
    private TextView mTvMsg;
    private TextView mBtnUpdate;
    private TextView mBtnCancel;
    private View bgView;

    public void setListener(DialogUpdateListner listener) {
        this.mListener = listener;
    }

    DialogUpdateListner mListener;

    protected DialogUpdateNotice(Context context) {
        super(context, R.layout.dialog_common, false);
    }

    @Override
    protected void initSubView() {
        bgView = findViewById(R.id.bgView);
        bgView.setBackgroundResource(R.mipmap.dialog_common_bg);
        mTvMsg = (TextView) findViewById(R.id.tvContent);
        mBtnCancel = (TextView) findViewById(R.id.btnCancel);
        mBtnUpdate = (TextView) findViewById(R.id.btnOk);

        mBtnCancel.setOnClickListener(this);
        mBtnUpdate.setOnClickListener(this);

        mTvMsg.setText("小贝检查到有新版本,更新吗?");
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnCancel){
            if(mListener!=null){
                mListener.onCancle();
            }
        }else if(v.getId() == R.id.btnOk){
            if(mListener!=null){
                mListener.onUpdate();
            }

        }
        dismiss();
    }

    public interface DialogUpdateListner {
        void onUpdate();
        void onCancle();
    }
}
