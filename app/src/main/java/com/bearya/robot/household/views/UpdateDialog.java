package com.bearya.robot.household.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.R;
import com.bearya.robot.household.entity.VersionInfo;
import com.bearya.robot.household.update.RobotUpdater;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class UpdateDialog extends BYBaseDialog {
	private Context mContext;
	private TextView tvMessage;
	private TextView cancelUpdate;
	private TextView updateTitle;
	private TextView updateProgress;
	private BYProgressView downloadProgress;
	private boolean isDownloading = false;
	private VersionInfo mVersionInfo;

	@SuppressLint("SimpleDateFormat")
	public UpdateDialog createDialog(Context context) {
		mContext = context;
		headInit(context, R.layout.dialog_update);
		tvMessage = view.findViewById(R.id.tvContent);
		btnConfirm = view.findViewById(R.id.btnOk);
		btnCancel = view.findViewById(R.id.btnCancel);
		updateTitle = view.findViewById(R.id.tvUpdate);
		updateProgress = view.findViewById(R.id.tvUpdateProgress);
		cancelUpdate = view.findViewById(R.id.cancel_update);
		downloadProgress = view.findViewById(R.id.download_progress);
		init();
		return this;
	}

	public UpdateDialog setVersionInfo(VersionInfo versionInfo) {
		mVersionInfo = versionInfo;
		return this;
	}

	private UpdateDialog init() {
		btnConfirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (confirmCallback != null) {
					confirmCallback.callback();
				}
				isDownloading = true;
				tvMessage.setVisibility(View.GONE);
				updateTitle.setVisibility(View.VISIBLE);
				downloadProgress.setVisibility(View.VISIBLE);
				updateProgress.setVisibility(View.VISIBLE);
				cancelUpdate.setVisibility(View.VISIBLE);
				downloadProgress.setProgress(0);
				updateProgress.setText(String.format(mContext.getString(R.string.dialog_update_progress),0)+"%");
				//DownLoading
				List<VersionInfo> versionInfos = new ArrayList<>();
				versionInfos.add(mVersionInfo);
				RobotUpdater.getInstance().init(MyApplication.getContext());
				RobotUpdater.getInstance().setUpdateListener(new RobotUpdater.UpdateListener() {
					@Override
					public void onNewDownloadTask(String url, long taskId) {

					}

					@Override
					public void onDownloadOne(String filePath) {
						LogUtils.d("UpdateDialog","onDownloadOne ....filePath = "+filePath);
						downloadProgress.setProgress(100);
						updateProgress.setText(String.format(mContext.getString(R.string.dialog_update_progress),100)+"%");
						CommonUtils.installPackage(mContext, filePath);
					}

					@Override
					public void onDownloadAll() {
						LogUtils.d("UpdateDialog","onDownloadAll ....");
					}

					@Override
					public void onNothingUpdate(String updateTypeName) {

					}

					@Override
					public void onError(String error) {

					}

					@Override
					public void onProgress(long taskId, int progress) {
						LogUtils.d("UpdateDialog","progress = "+progress);
						downloadProgress.setProgress(progress);
						updateProgress.setText(String.format(mContext.getString(R.string.dialog_update_progress),progress)+"%");
					}

					@Override
					public void updateTip(String tip) {

					}
				});
				RobotUpdater.getInstance().downloadWithVersionInfos(versionInfos);
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (cancelCallback != null) {
					cancelCallback.callback();
				}
				closeDialog();

			}
		});

		cancelUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RobotUpdater.getInstance().destory();
				closeDialog();
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
		return this;
	}

	public UpdateDialog setMessage(String message) {
		tvMessage.setText(message);
		return this;
	}

	public UpdateDialog setMessage(int messageId) {
		tvMessage.setText(messageId);
		return this;
	}

}
