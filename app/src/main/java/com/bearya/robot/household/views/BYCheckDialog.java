package com.bearya.robot.household.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;

import com.bearya.robot.household.R;

public class BYCheckDialog extends BYBaseDialog {

	TextView tvMessage;
	@SuppressLint("SimpleDateFormat")
	public BYCheckDialog createDialog(Activity context) {
		headInit(context, R.layout.dialog_check);
		tvMessage = view.findViewById(R.id.tvContent);
		btnConfirm = view.findViewById(R.id.btnOk);
		btnCancel =  view.findViewById(R.id.btnCancel);
		bottomInit();
		return this;
	}

	public BYCheckDialog setMessage(String message) {
		tvMessage.setText(message);
		return this;
	}

	public BYCheckDialog setMessage(int messageId) {
		tvMessage.setText(messageId);
		return this;
	}

}
