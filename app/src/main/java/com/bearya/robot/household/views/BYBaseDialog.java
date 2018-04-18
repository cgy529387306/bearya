package com.bearya.robot.household.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bearya.robot.household.R;


public class BYBaseDialog {
    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public TextView getBtnConfirm() {
        return btnConfirm;
    }

    public void setBtnConfirm(Button btnConfirm) {
        this.btnConfirm = btnConfirm;
    }

    public TextView getBtnCancel() {
        return btnCancel;
    }

    public void setBtnCancel(Button btnCancel) {
        this.btnCancel = btnCancel;
    }

    protected Dialog dialog;
    protected TextView btnConfirm;
    protected TextView btnCancel;
    protected TextView dialogTitle;
    protected View view;

    public DialogCallback confirmCallback;
    public DialogCallback cancelCallback;
    public DialogCallback dismissCallback;

    public void bottomInit() {
        btnConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (confirmCallback != null) {
                    confirmCallback.callback();
                }

                BYBaseDialog.this.closeDialog();

            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (cancelCallback != null) {
                    cancelCallback.callback();
                }
                BYBaseDialog.this.closeDialog();

            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (dismissCallback != null) {
                    dismissCallback.callback();
                }
            }
        });

        dialog.setCancelable(false);
    }

    public void bottomConfirmInit() {
        btnConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (confirmCallback != null) {
                    confirmCallback.callback();
                }

            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (cancelCallback != null) {
                    cancelCallback.callback();
                }
                BYBaseDialog.this.closeDialog();

            }
        });

        dialog.setCancelable(false);
    }

    public BYBaseDialog setCancelable(boolean cancelable) {
        dialog.setCancelable(cancelable);
        return this;
    }

    public void showDialog() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void closeDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public BYBaseDialog setConfirmCallback(DialogCallback callback) {

        this.confirmCallback = callback;
        return this;

    }

    public BYBaseDialog setCancelCallback(DialogCallback callback) {
        this.cancelCallback = callback;
        return this;

    }

    public BYBaseDialog setDismisCallback(DialogCallback callback) {
        dismissCallback = callback;
        return this;

    }

    public void headInit(Context context, int layout) {
        dialog = new Dialog(context, R.style.dialog_transparent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);//Theme.Translucent.NoTitleBar
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(layout, null);
        dialog.setContentView(view);
    }

    public void setTitle(String title) {
        if (dialogTitle != null) {
            dialogTitle.setText(title);
        }
    }
}

