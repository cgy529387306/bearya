package com.bearya.robot.household.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bearya.robot.household.MyApplication;
import com.bearya.robot.household.entity.BabyInfo;

import org.json.JSONObject;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Qiujz on 2017/11/29.
 */

public class CommonUtils {
    /*public static final String WILDDOG_BASE_URL = "https://wd2884820246jgtlik.wilddogio.com";
    public static final String WILDDOG_ON_LINE = "online";
    public static final String WILDDOG_DEVICES = "devices";
    public static final String WILDDOG_SERVER = "server";
    public static final String WILDDOG_CLIENT = "client";*/

//    public static final String actionUp = "{\"totalTime\":2100,\"actions\":[{\"startTime\":0,\"endTime\":2000,\"actionSet\":[{\"faceid\":1012,\"type\":\"emotion\"},{\"type\":\"wheel\",\"direction\":257,\"leftspeed\":80,\"rightspeed\":80}]},{\"startTime\":2001,\"endTime\":2100,\"actionSet\":[{\"type\":\"reset\",\"basicaction\":300}]}]}";
//    public static final String actionDown = "{\"totalTime\":2100,\"actions\":[{\"startTime\":0,\"endTime\":2000,\"actionSet\":[{\"faceid\":1033,\"type\":\"emotion\"},{\"type\":\"wheel\",\"direction\":258,\"leftspeed\":80,\"rightspeed\":80}]},{\"startTime\":2001,\"endTime\":2100,\"actionSet\":[{\"type\":\"reset\",\"basicaction\":300}]}]}";
//    public static final String actionLeft = "{\"totalTime\":4100,\"actions\":[{\"startTime\":0,\"endTime\":4000,\"actionSet\":[{\"faceid\":1022,\"type\":\"emotion\"},{\"type\":\"wheel\",\"direction\":259,\"leftspeed\":80,\"rightspeed\":80}]},{\"startTime\":4001,\"endTime\":4100,\"actionSet\":[{\"type\":\"reset\",\"basicaction\":300}]}]}";
//    public static final String actionRight = "{\"totalTime\":4100,\"actions\":[{\"startTime\":0,\"endTime\":4000,\"actionSet\":[{\"faceid\":1022,\"type\":\"emotion\"},{\"type\":\"wheel\",\"direction\":260,\"leftspeed\":80,\"rightspeed\":80}]},{\"startTime\":4001,\"endTime\":4100,\"actionSet\":[{\"type\":\"reset\",\"basicaction\":300}]}]}";
    public static final String actionUp = "go_forward";
    public static final String actionDown = "go_backward";
    public static final String actionLeft = "go_left";
    public static final String actionRight = "go_right";
    public static final String actionStop = "stop";
    public static enum UpdateType{
        UPDATE_TYPE_APK,
        UPDATE_TYPE_ZIP,
        UPDATE_TYPE_DATABASE,
        ;
    }
    private static Toast toast = null;

    public static void showToast(Context mContext, String content) {
        if (toast == null) {
            toast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
        }
        toast.setText(content);
        toast.show();
    }

    public static  boolean isServiceRunning(Context mContext, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 当前网络是否可用
     *
     * @return
     */
    public static boolean isNetAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo networkinfo = connectivity.getActiveNetworkInfo();
                if (networkinfo != null) {
                    if (networkinfo.isAvailable() && networkinfo.isConnected()) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static int getVersionCodeByAppId(Context context, String appid){
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(appid, PackageManager.GET_CONFIGURATIONS);
            if(packageInfo!=null){
                return packageInfo.versionCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getVersionName(Context context) {
        try {
            String pkName = context.getPackageName();
            String versionName = context.getPackageManager().getPackageInfo(
                    pkName, 0).versionName;

            return versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1.0.0";
    }

    public static int getVersionCode(Context context) {
        try {
            String pkName = context.getPackageName();
            int versionCode = context.getPackageManager().getPackageInfo(
                    pkName, 0).versionCode;
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static long getSDAvailableSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

    public static void installPackage(Context context,String apkPath){
        /*Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(apkPath)),"application/vnd.android.package-archive");
        context.startActivity(intent);*/
        File apkFile = new File(apkPath);
        if (!apkFile.exists()) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(context, "com.bearya.robot.household.fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * 获取设备基本信息，以便追加到请求头部发送给服务器
     * @return
     */
    public static String getMobileInfo(Context context) {
        final StringBuilder mobile = new StringBuilder();

        TelephonyManager phoneInfo = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != phoneInfo) {
            // 设备ID
            String deviceId = phoneInfo.getDeviceId();
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = "00000000";
            }
            mobile.append(deviceId).append(",");
            // 序列号
            String serial = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? Build.SERIAL : "";
            if (TextUtils.isEmpty(serial)) {
                serial = "00000000";
            }
            mobile.append(serial).append(",");
            // 品牌
            mobile.append(Build.BRAND).append(",");
            // 型号
            mobile.append(Build.MODEL).append(",");
            // 系统版本及其版本号
            mobile.append("Android").append(Build.VERSION.RELEASE).append("(").append(Build.VERSION.SDK_INT).append(")");
        }

        return mobile.toString();
    }

    // Get the device ID
    public static String getIMEI(Context context) {
        String auid = Settings.Secure.ANDROID_ID + "android_id";
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            try {
                auid = tm.getDeviceId();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            tm = null;
        }
        if (TextUtils.isEmpty(auid))
            auid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (TextUtils.isEmpty(auid))
            auid = null;
        System.out.println("imei" + auid);
        return auid;
    }

    /**
     * 获取当前应用程序的包名
     * @param context 上下文对象
     * @return 返回包名
     */
    public static String getAppProcessName(Context context) {
        //当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
                return info.processName;//返回包名
        }
        return "";
    }

    public static void hideSoftInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),0);//强制隐藏键盘
    }

    /**
     * MD5加密
     *
     * @param s
     * @return
     */
    public final static String MD5(String s) {
        char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            byte[] strTemp = s.getBytes();
            // 使用MD5创建MessageDigest对象
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte b : md) {
                // 将没个数(int)b进行双字节加密
                str[k++] = hexDigits[b >> 4 & 0xf];
                str[k++] = hexDigits[b & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断对象是否为NULL或空
     * @param object 对象
     * @return 是否为NULL或空
     */
    public static boolean isEmpty (Object object){
        boolean result = false;
        if (object == null){
            result = true;
        } else {
            if (object instanceof String){
                result = ((String)object).equals("");
            }else if (object instanceof Date) {
                result = ((Date) object).getTime() == 0;
            }else if (object instanceof Long){
                result = ((Long)object).longValue() == Long.MIN_VALUE;
            }else if (object instanceof Integer){
                result = ((Integer)object).intValue() == Integer.MIN_VALUE;
            }else if (object instanceof Collection){
                result = ((Collection<?>)object).size() == 0;
            }else if (object instanceof Map){
                result = ((Map<?, ?>)object).size() == 0;
            }else if (object instanceof JSONObject){
                result = !((JSONObject)object).keys().hasNext();
            }else{
                result = object.toString().equals("");
            }
        }
        return result;
    }
    /**
     * 判断对象是否不为NULL或空
     * @param object 对象
     * @return 是否不为NULL或空
     */
    public static boolean isNotEmpty(Object object){
        return !isEmpty(object);
    }

    /**
     * <p> 取得基本的缓存路径(无SD卡则使用RAM)
     *
     * @return 类似这样的路径 /mnt/sdcard/Android/data/demo.android/cache/ 或者 /data/data/demo.android/cache/
     */
    public static String getBaseCachePath() {
        String result = null;
        // 有些机型外部存储不按套路出牌的
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    && Environment.getExternalStorageDirectory().canWrite()) {
                result = MyApplication.getContext().getExternalCacheDir().getAbsolutePath().concat(File.separator);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(result)) {
            result = MyApplication.getContext().getCacheDir().getPath().concat(File.separator);
        }
        return result;
    }

    /**
     * <p> 取得默认类型的基本的文件路径(无SD卡则使用RAM)
     * <p> 默认为下载目录
     *
     * @return 类似这样的路径 /mnt/sdcard/Android/data/demo.android/files/Download/ 或者 /data/data/demo.android/files/
     */
    public static String getBaseFilePath() {
        String result = null;
        try {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    && Environment.getExternalStorageDirectory().canWrite()) {
                result = MyApplication.getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        .getAbsolutePath().concat(File.separator);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(result)) {
            result = MyApplication.getContext().getFilesDir().getPath().concat(File.separator);
        }
        return result;
    }

    /**
     * 根据手机的分辨率从 dip转成为 px
     *
     * @param context
     *            {@link Context} 当前界面的context
     * @param dpValue
     *            {@link float} 要转换的dip数值
     */
    public static int dip2px(Context context, float dpValue) {
//		final float scale = context.getResources().getDisplayMetrics().density;
//		return (dpValue * scale + 0.5f);
        float fpx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpValue, context.getResources().getDisplayMetrics());
        return (int)fpx;
    }

    /**
     * 根据手机的分辨率从 px转成为 dip
     *
     * @param context
     *            {@link Context} 当前界面的context
     * @param pxValue
     *            {@link float} 要转换的px数值
     */
    public static float px2dip(Context context, float pxValue) {
//		final float scale = context.getResources().getDisplayMetrics().density;
//		return (pxValue / scale + 0.5f);
        float fdp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,
                pxValue, context.getResources().getDisplayMetrics());
        return fdp;
    }

    /**
     * 平分list成n份 数据量尽可能相等
     * @param list 需要平分的list
     * @param n    平分成n分
     * @return
     */
    public static <T> List<List<T>> splitList(List<T> list, int n) {
        List<List<T>> strList = new ArrayList<>();
        if (list == null) return strList;
        int size = list.size();
        int quotient = size / n; // 商数
        int remainder = size % n; // 余数
        int offset = 0; // 偏移量
        int len = quotient > 0 ? n : remainder; // 循环长度
        int start = 0;    // 起始下标
        int end = 0;    // 结束下标
        List<T> tempList = null;
        for (int i = 0; i < len; i++) {
            if (remainder != 0) {
                remainder--;
                offset = 1;
            } else {
                offset = 0;
            }
            end = start + quotient + offset;
            tempList = list.subList(start, end);
            start = end;
            strList.add(tempList);
        }
        return strList;
    }

    /**
     * 判断该字符串是否为中文
     * @param string
     * @return
     */
    public static boolean isChinese(String string){
        int n = 0;
        for(int i = 0; i < string.length(); i++) {
            n = (int)string.charAt(i);
            if(!(19968 <= n && n <40869)) {
                return false;
            }
        }
        return true;
    }

    public static void ScaleUpDowm(View view) {
        ScaleAnimation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(1200);
        view.startAnimation(animation);
    }

    /**
     * 字符串转换成整数 ,转换失败将会 return 0;
     *
     * @param str 字符串
     * @return
     */
    public static int stringToInt(String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        } else {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    public static boolean isHasBaby(BabyInfo babyInfo){
        return babyInfo!=null && !TextUtils.isEmpty(babyInfo.getName());
    }

}
