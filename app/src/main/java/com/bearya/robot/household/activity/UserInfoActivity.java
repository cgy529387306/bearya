package com.bearya.robot.household.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bearya.robot.household.R;
import com.bearya.robot.household.qcloud.QServiceCfg;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.DateHelper;
import com.bearya.robot.household.utils.JsonHelper;
import com.bearya.robot.household.utils.NavigationHelper;
import com.bearya.robot.household.views.BaseActivity;
import com.bearya.robot.household.views.BottomMenuDialog;
import com.bearya.robot.household.views.ClearableEditText;
import com.codbking.widget.DatePickDialog;
import com.codbking.widget.OnSureLisener;
import com.codbking.widget.bean.DateType;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.listener.CosXmlProgressListener;
import com.tencent.cos.xml.listener.CosXmlResultListener;
import com.tencent.cos.xml.model.CosXmlRequest;
import com.tencent.cos.xml.model.CosXmlResult;
import com.tencent.cos.xml.model.object.PutObjectRequest;

import java.io.File;
import java.util.Date;


public class UserInfoActivity extends BaseActivity implements View.OnClickListener {

    private ImageView imvHead; //头像
    private ImageView imvDad; //选择爸爸
    private ImageView imvMom; //选择妈妈
    private ImageView imvOther; // 选择其他
    private TextView tvBoy; //男
    private TextView tvGirl; //女
    private ClearableEditText edtBirth;//出生日期
    private TextView tvNext;
    private ClearableEditText edtName;
    private int gender = 1;
    private String relationship = "father";
    private String avatar = "";
    private QServiceCfg qServiceCfg;
    private PutObjectRequest putObjectRequest;
    private BottomMenuDialog pickDialog;
    private String mTempFilePath = CommonUtils.getBaseCachePath()
            .concat(String.valueOf(System.currentTimeMillis())).concat(".png");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.string.babyInfo,R.layout.activity_user_info,getString(R.string.skip));
        initView();
        initListener();
        initQCloud();
    }

    private void initQCloud(){
        qServiceCfg = QServiceCfg.instance(UserInfoActivity.this);
    }

    @Override
    protected void onRightTip() {
        super.onRightTip();
        finish();
    }

    private void initView() {
        imvHead = (ImageView) findViewById(R.id.imv_head);
        imvDad = (ImageView) findViewById(R.id.imv_dad);
        imvMom = (ImageView) findViewById(R.id.imv_mom);
        imvOther = (ImageView) findViewById(R.id.imv_other);
        tvBoy = (TextView) findViewById(R.id.tv_boy);
        tvGirl = (TextView) findViewById(R.id.tv_girl);
        edtBirth = (ClearableEditText) findViewById(R.id.edt_birth);
        tvNext = (TextView) findViewById(R.id.tv_next);
        edtName = (ClearableEditText) findViewById(R.id.edt_name);
    }

    private void initListener() {
        imvDad.setOnClickListener(this);
        imvMom.setOnClickListener(this);
        imvOther.setOnClickListener(this);
        tvBoy.setOnClickListener(this);
        tvGirl.setOnClickListener(this);
        edtBirth.setOnClickListener(this);
        tvNext.setOnClickListener(this);
        imvHead.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imv_dad) {
            checkID(0);
        } else if (id == R.id.imv_mom) {
            checkID(1);
        } else if (id == R.id.imv_other) {
            checkID(2);
        } else if (id == R.id.tv_boy) {
            checkSex(0);
        } else if (id == R.id.tv_girl) {
            checkSex(1);
        } else if (id == R.id.edt_birth) {
            showTimePicker();
        }else if (id == R.id.imv_head){
            pickPhoto();
        }else if (id == R.id.tv_next) {
            String name = edtName.getText().toString().trim();
            String birthDay = edtBirth.getText().toString();
            if (TextUtils.isEmpty(name)){
                showToast(getString(R.string.input_baby_name));
                return;
            }
            if (TextUtils.isEmpty(name)){
                showToast(getString(R.string.select_birthday));
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString("name", name);
            bundle.putString("birth", birthDay);
            bundle.putString("relationship", relationship);
            bundle.putInt("gender", gender);
            bundle.putString("avatar", !TextUtils.isEmpty(avatar) ? avatar : "");
            NavigationHelper.startActivity(this, HabitActivity.class, null, false);
        }
    }

    private void showTimePicker() {
        DatePickDialog dialog = new DatePickDialog(this);
        dialog.setYearLimt(100);
        dialog.setTitle(getString(R.string.select_time));
        dialog.setType(DateType.TYPE_YMD);
        dialog.setMessageFormat("yyyy-MM-dd");
        dialog.setOnChangeLisener(null);
        dialog.setOnSureLisener(new OnSureLisener() {
            @Override
            public void onSure(Date date) {
                String dateStr = DateHelper.date2String("yyyy-MM-dd");
                edtBirth.setText(dateStr);
            }
        });
        dialog.show();
    }


    private void checkID(int type) {
        imvDad.setImageResource(type == 0 ? R.mipmap.icon_check : R.mipmap.icon_uncheck);
        imvMom.setImageResource(type == 1 ? R.mipmap.icon_check : R.mipmap.icon_uncheck);
        imvOther.setImageResource(type == 2 ? R.mipmap.icon_check : R.mipmap.icon_uncheck);
        if (type == 0) {
            relationship = "father";
        } else if (type == 1) {
            relationship = "mother";
        } else if (type == 2) {
            relationship = "other";
        }
    }

    private void checkSex(int type) {
        tvBoy.setBackgroundColor(type == 0 ? getResources().getColor(R.color.colorSex) : getResources().getColor(R.color.colorViewBg));
        tvBoy.setTextColor(type == 0 ? getResources().getColor(R.color.colorWhite) : getResources().getColor(R.color.colorHint));
        tvGirl.setBackgroundColor(type == 1 ? getResources().getColor(R.color.colorSex) : getResources().getColor(R.color.colorViewBg));
        tvGirl.setTextColor(type == 1 ? getResources().getColor(R.color.colorWhite) : getResources().getColor(R.color.colorHint));
        gender = type;
    }

    /**
     * 采用异步回调操作
     */
    public void uploadAvatar(String path) {
        String bucket = QServiceCfg.bucket;
        String cosPath = "app-photo/family/" + System.currentTimeMillis() + ".png";
        putObjectRequest = new PutObjectRequest(bucket, cosPath,
                path);
        putObjectRequest.setProgressListener(new CosXmlProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                float result = (float) (progress * 100.0 / max);
                Log.w("XIAO", "progress =" + (long) result + "%");
            }
        });
        putObjectRequest.setSign(600, null, null);
        showLoadingView();
        qServiceCfg.cosXmlService.putObjectAsync(putObjectRequest, new CosXmlResultListener() {
            @Override
            public void onSuccess(CosXmlRequest cosXmlRequest, CosXmlResult cosXmlResult) {
                closeLoadingView();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(cosXmlResult.printResult());
                showToast("上传成功:"+stringBuilder.toString());
            }

            @Override
            public void onFail(CosXmlRequest cosXmlRequest, CosXmlClientException qcloudException, CosXmlServiceException qcloudServiceException) {
                closeLoadingView();
                StringBuilder stringBuilder = new StringBuilder();
                if (qcloudException != null) {
                    stringBuilder.append(qcloudException.getMessage());
                } else {
                    stringBuilder.append(qcloudServiceException.toString());
                }
                showToast("上传失败:"+stringBuilder.toString());
            }
        });
    }

    private void pickPhoto() {
        if (pickDialog == null) {
            pickDialog = new BottomMenuDialog.Builder(UserInfoActivity.this)
                    .addMenu("拍照", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pickImageFromCamera();
                            pickDialog.dismiss();
                        }
                    }).addMenu("相册", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pickImageFromGallery();
                            pickDialog.dismiss();
                        }
                    }).create();
        }
        pickDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 1:
                // 相册
                cropPickedImage(data.getData());
                break;
            case 2:
                // 拍照
                File file = new File(mTempFilePath);
                if (!file.exists()) {
                    return;
                }
                cropPickedImage(Uri.parse("file://".concat(mTempFilePath)));
                break;
            case 3:
                uploadAvatar(mTempFilePath);
                break;
            default:
                break;
        }
    }

    /**
     * 相册图片选取
     */
    private void pickImageFromGallery() {
        try {
            Intent intentFromGallery = new Intent();
            // 设置文件类型
            intentFromGallery.setType("image/*");
            intentFromGallery.setAction(Intent.ACTION_PICK);
            startActivityForResult(intentFromGallery, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照获取图片
     */
    private void pickImageFromCamera() {
        try {
            Intent intent = new Intent();
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            // 图片缓存的地址
            mTempFilePath = CommonUtils.getBaseCachePath()
                    .concat(String.valueOf(System.currentTimeMillis()))
                    .concat(".png");
            File file = new File(mTempFilePath);
            Uri uri = Uri.fromFile(file);
            // 设置图片的存放地址
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 裁剪选取的图片
     *
     * @param uri
     */
    private void cropPickedImage(Uri uri) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");// 可裁剪
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("scale", true);

            // 图片缓存的地址
            mTempFilePath = CommonUtils.getBaseCachePath()
                    .concat(String.valueOf(System.currentTimeMillis()))
                    .concat(".png");
            File file = new File(mTempFilePath);
            Uri uriCache = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriCache);
            intent.putExtra("outputFormat", "JPEG");// 图片格式
            intent.putExtra("noFaceDetection", true);// 取消人脸识别
            intent.putExtra("return-data", false);
            startActivityForResult(intent, 3);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

}
