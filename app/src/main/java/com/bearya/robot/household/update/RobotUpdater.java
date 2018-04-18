package com.bearya.robot.household.update;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.android.volley.VolleyError;
import com.bearya.robot.household.entity.VersionInfo;
import com.bearya.robot.household.http.CheckUpdateJsonObjectListener;
import com.bearya.robot.household.http.CheckUpdateResponse;
import com.bearya.robot.household.http.VolleyRequestQueue;
import com.bearya.robot.household.update.zip.ZipInfo;
import com.bearya.robot.household.update.zip.Zipper;
import com.bearya.robot.household.utils.CodeUtils;
import com.bearya.robot.household.utils.CommonUtils;
import com.bearya.robot.household.utils.FileUtil;
import com.bearya.robot.household.utils.LogUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xifengye on 2017/2/17.
 */


public class RobotUpdater implements DownloadMgr.DownloadProgressListener {
    public static final String KEY_TASK_IDS = "key_task_ids";
    private Context context;
    private SharedPreferences preferences;

    private BroadcastReceiver receiver;
    private TaskHistoryHelper taskHistoryHelper;
    private VolleyRequestQueue volleyRequestQueue;
    private Downloader downloader;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private int mCountOfDownload;//下载总量(不是全部的,单表示某总类型的下载数量)
    private int mIndexOfDownloading;//已下载数量
    private CheckHelper mCheckHelper;
    private int mUpdateType;//更新类型


    public boolean isCheckUpdateComplete() {
        return isCheckUpdateComplete;
    }

    private boolean isCheckUpdateComplete = false;//是否已经完成检查更新

    private static RobotUpdater mInstance;

    public static RobotUpdater getInstance() {
        if (mInstance == null) {
            mInstance = new RobotUpdater();
        }
        return mInstance;
    }

    @Override
    public void onProgress(int progress) {
        postProgress(downloader.getCurrentTaskId(), progress);
    }

    public interface UpdateListener {
        public void onNewDownloadTask(String url, long taskId);

        public void onDownloadOne(String filePath);

        public void onDownloadAll();

        public void onNothingUpdate(String updateTypeName);

        public void onError(String error);

        public void onProgress(long taskId, int progress);

        public void updateTip(String tip);
    }

    private UpdateListener mUpdateListener;

    public void setUpdateListener(UpdateListener updateListener) {
        this.mUpdateListener = updateListener;
    }

    private RobotUpdater() {

    }

    public void init(Context context) {
        this.context = context;
        DownloadMgr.getInstance().init(context);
        DownloadMgr.getInstance().setListener(this);
        volleyRequestQueue = new VolleyRequestQueue(context);
        preferences = context.getSharedPreferences("ROBOT_PREFERENCES", Context.MODE_PRIVATE);
        registerDownloadCompleteReceiver();
        taskHistoryHelper = new TaskHistoryHelper(preferences);
        continueHistory(taskHistoryHelper.getTaskHistory());
    }


    public String getUpdateTypeName(int type) {
        StringBuilder sb = new StringBuilder();
        if ((type & IUpdateChecker.UPDATE_ZIP) != 0) {
            sb.append("资源包 ");
        }
        if ((type & IUpdateChecker.UPDATE_DB) != 0) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("数据库 ");
        }
        if ((type & IUpdateChecker.UPDATE_APK) != 0) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append("V").append(getApkVersionName());
        }
        return sb.toString();
    }

    private String getApkVersionName() {
        return CommonUtils.getVersionName(context);
    }

    public void requestUpdateCheck(final int type, final Context activity) {
        init(activity.getApplicationContext());
        CheckUpdateListener checkUpdateListener = new CheckUpdateListener() {
            @Override
            public void onUpdate(final List<VersionInfo> versionInfos) {
                CommonDialog.DialogUpdateListener dialogUpdateListener = new CommonDialog.DialogUpdateListener() {
                    @Override
                    public void onUpdate() {
                        isCheckUpdateComplete = true;
                        //ActionDefine.sendMessagerToServices(context,new Messager(Messager.UPDATE_SOFT,"versionInfo",versionInfos));
                    }

                    @Override
                    public void onCancel() {
                        LogUtils.e("更新检查:不更新");
                    }
                };
                CommonDialog dialogUpdateNotice = new CommonDialog(activity, "检查到有新版本,是否更新?");
                dialogUpdateNotice.setListener(dialogUpdateListener);
                dialogUpdateNotice.show();
            }

            @Override
            public void onNothing(int type) {
                LogUtils.e("更新检查:Nothing");
            }

            @Override
            public void onError(String error) {
                LogUtils.e("更新检查-error:%s" + error);

            }
        };
        requestUpdateCheck(type, checkUpdateListener);


    }

    public void requestUpdateCheck(final int type, final CheckUpdateListener listener) {
        this.mUpdateType = type;
        mIndexOfDownloading = 1;
        String url = "https://api.bearya.com/v1/source/apk/update?device=3";//获取版本号URL
        CheckUpdateJsonObjectListener checkUpdateJsonObjectListener = new CheckUpdateJsonObjectListener() {
            @Override
            public void onCheckResp(CheckUpdateResponse response) {
                LogUtils.d("requestUpdateCheck", "onCheckResp .........");
                if (response != null) {
                    LogUtils.d("requestUpdateCheck", "onCheckResp .........1");
                    List<VersionInfo> versionInfos = filterVersionInfo(type, response);
                    if (listener != null) {
                        LogUtils.d("requestUpdateCheck", "onCheckResp .........2");
                        if (CodeUtils.isEmpty(versionInfos)) {
                            listener.onNothing(type);
                        } else {
                            listener.onUpdate(versionInfos);
                        }
                    }
                } else {
                    LogUtils.d("requestUpdateCheck", "onCheckResp .........3");
                    if (listener != null) {
                        listener.onNothing(type);
                    }
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                super.onErrorResponse(error);
                LogUtils.d("requestUpdateCheck", "onErrorResponse .........");
                String errorstr = error.getMessage();
                if (TextUtils.isEmpty(errorstr)) {
                    errorstr = "更新出错!";
                }
                if (listener != null) {
                    listener.onError("更新失败");
                }
            }
        };
        try {
            requestUpdateInfo(url, checkUpdateJsonObjectListener);
        } catch (Exception e) {
            LogUtils.d("requestUpdateCheck", "Exception .........");
            if (listener != null) {
                listener.onError("更新失败");
            }
            e.printStackTrace();
        }
    }

    public void downloadWithVersionInfos(List<VersionInfo> versionInfos) {
        downloader = new Downloader(versionInfos);
        downloader.start();
    }

    public void requestUdate(final int type) {
        requestUpdateCheck(type, new CheckUpdateListener() {
            @Override
            public void onUpdate(List<VersionInfo> versionInfos) {
                downloadWithVersionInfos(versionInfos);
            }

            @Override
            public void onNothing(int type) {
                onNothingUpdate(getUpdateTypeName(type));
            }

            @Override
            public void onError(String error) {
                onDownloadError(error);
            }
        });
    }


    private void onNothingUpdate(String updateTypeName) {
        if (mUpdateListener != null) {
            mUpdateListener.onNothingUpdate(updateTypeName);
        }
    }


    class Downloader {
        List<VersionInfo> versionInfos;
        Map<Long, DownloadInfo> downloadInfoMap;
        private Map<Long, String> downloadCacheDireExistTaskId = new HashMap<>();//存放本地已经存在的任务ID(自己创建的)
        private long mCurrentDownloadTaskId;

        public void clear() {
            if (versionInfos != null && versionInfos.size() > 0) {
                versionInfos.clear();
            }
            if (downloadInfoMap != null && downloadInfoMap.size() > 0) {
                downloadInfoMap.clear();
            }

        }

        public Downloader(List<VersionInfo> versionInfos) {
            this.versionInfos = versionInfos;
            mCountOfDownload = versionInfos.size();
            downloadInfoMap = new HashMap<>();
        }

        public void start() {
            if (versionInfos != null && versionInfos.size() > 0) {
                VersionInfo versionInfo = versionInfos.remove(0);
                if (!checkSDAvailableSize(versionInfo.pack_size)) {
                    LogUtils.e("下载空间不足");
                    versionInfos.clear();
                    if (mUpdateListener != null) {
                        mUpdateListener.onError("SD卡空间不足");
                    }
                    return;
                }
                DownloadInfo info = download(versionInfo);
                mCurrentDownloadTaskId = info.taskId;
                downloadInfoMap.put(info.taskId, info);
                if (info.isDownloadSuccessful()) {
                    onDownloadTaskSuccessful(info.taskId);
                } else {
                    if (mUpdateListener != null) {
                        mUpdateListener.updateTip(versionInfo.tips);
                        mUpdateListener.onNewDownloadTask(versionInfo.download_url, info.taskId);
                    }
                }
            } else {
                downloadComplete();
            }
        }

        private boolean checkSDAvailableSize(long size) {
            return CommonUtils.getSDAvailableSize(context) - size > 0;
        }

        public DownloadInfo download(VersionInfo versionInfo) {
            DownloadStatus downloadStatus = addDownloadTask(versionInfo);
            DownloadInfo downloadInfo = new DownloadInfo(downloadStatus.taskId, versionInfo.version);
            downloadInfo.status = downloadStatus.status;
            downloadInfo.rebootToInstall = versionInfo.isRebootToInstall();
            return downloadInfo;
        }

        /**
         * 添加下载任务
         *
         * @param versionInfo
         */
        private DownloadStatus addDownloadTask(VersionInfo versionInfo) {
            LogUtils.e("添加%s" + versionInfo.getExtenionName() + "下载任务:%s" + versionInfo.toString());
            String fileName = versionInfo.getDownloadFileName();
            String fileInDownloadCacheDir = checkFileExistInDownloadCacheDir(fileName);
            if (!TextUtils.isEmpty(fileInDownloadCacheDir)) {//要下载的文件本地已经存在了
                long id = 10000000 + downloadCacheDireExistTaskId.size() + 1;
                downloadCacheDireExistTaskId.put(id, fileInDownloadCacheDir);
                return new DownloadStatus(id, DownloadManager.STATUS_SUCCESSFUL);
            } else {
                DownloadMgr.DownloadTask downloadTask = new DownloadMgr.DownloadTask();
                downloadTask.url = versionInfo.download_url;
                downloadTask.fileName = fileName;
                DownloadMgr.TaskResult result = DownloadMgr.getInstance().addTask(downloadTask);
                return new DownloadStatus(result.id, result.status);
            }
        }

        /**
         * 下载目录中是否存在将要下载的文件
         *
         * @param fileName 将要下载的文件名
         * @return 返回已存在的文件绝对路径
         */
        private String checkFileExistInDownloadCacheDir(String fileName) {
            if (context != null) {
                String downloadCacheDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                File file = new File(String.format("%s/%s", downloadCacheDirectory, fileName).toString());
                if (file != null && file.exists()) {
                    return file.getAbsolutePath();
                }
            }
            return null;
        }

        private DownloadInfo getDownloadInfoByTaskId(long taskId) {
            if (downloadInfoMap.containsKey(taskId)) {
                return downloadInfoMap.get(taskId);
            }
            return null;
        }

        public void onDownloadTaskSuccess(long taskId, String fileName) {
            DownloadInfo downloadInfo = getDownloadInfoByTaskId(taskId);
            if (downloadInfo != null) {
                IUpdateChecker.SupportExtionsFile extionsFile = checkSupportExtension(fileName);
                downloadInfo.setFilePath(fileName);
                if (extionsFile == IUpdateChecker.SupportExtionsFile.db) {
                    saveDbUpdateVersionCode(downloadInfo.versionCode);
                }
                mIndexOfDownloading++;
                if (mIndexOfDownloading > downloadInfoMap.size()) {
                    mIndexOfDownloading = downloadInfoMap.size();
                }
            }
            start();
        }

        private IUpdateChecker.SupportExtionsFile checkSupportExtension(String fileFullPath) {
            String extension = fileFullPath.substring(fileFullPath.lastIndexOf(".") + 1);
            for (IUpdateChecker.SupportExtionsFile supportExtionsFile : IUpdateChecker.SupportExtionsFile.values()) {
                if (supportExtionsFile.name().equals(extension)) {
                    return supportExtionsFile;
                }
            }
            return null;
        }

        private void downloadComplete() {
            new DownloadCopleteHandler().start();
        }

        public List<InstallInfo> getApkInstallInfo() {
            List<InstallInfo> zipFile = new ArrayList<>();
            for (DownloadInfo downloadInfo : downloadInfoMap.values()) {
                if (checkSupportExtension(downloadInfo.filePath) == IUpdateChecker.SupportExtionsFile.apk) {
                    zipFile.add(new InstallInfo(downloadInfo.filePath, downloadInfo.rebootToInstall));
                }
            }
            return zipFile;
        }

        public String getDatabaseFilePath() {
            for (DownloadInfo downloadInfo : downloadInfoMap.values()) {
                if (checkSupportExtension(downloadInfo.filePath) == IUpdateChecker.SupportExtionsFile.db) {
                    return downloadInfo.filePath;
                }
            }
            return "";
        }

        public List<ZipInfo> getZipInfos() {
            List<ZipInfo> zipFile = new ArrayList<>();
            for (DownloadInfo downloadInfo : downloadInfoMap.values()) {
                if (checkSupportExtension(downloadInfo.filePath) == IUpdateChecker.SupportExtionsFile.zip) {
                    zipFile.add(downloadInfo.getZipInfo());
                }
            }
            Collections.sort(zipFile, new Comparator<ZipInfo>() {
                @Override
                public int compare(ZipInfo o1, ZipInfo o2) {
                    return o1.getVersionCode() - o2.getVersionCode();
                }
            });
            return zipFile;
        }

        public long getCurrentTaskId() {
            return mCurrentDownloadTaskId;
        }


        public boolean isExistDownloadCacheDireTaskId(long id) {
            return downloadCacheDireExistTaskId.keySet().contains(id);
        }

        public String getFileInDownloadCacheDir(long id) {
            return downloadCacheDireExistTaskId.get(id);
        }

        public long[] getTaskIds() {
            long[] ids = new long[downloadInfoMap.size()];
            int i = 0;
            for (long id : downloadInfoMap.keySet()) {
                ids[i++] = id;
            }
            return ids;
        }
    }


    class CheckHelper {
        private final CheckUpdateResponse checkUpdateResponse;
        private Map<Integer, IUpdateChecker> checkerMap;

        public CheckHelper(int type, CheckUpdateResponse response) {
            this.checkerMap = getChecker(type);
            this.checkUpdateResponse = response;
        }

        private Map<Integer, IUpdateChecker> getChecker(int type) {
            Map<Integer, IUpdateChecker> checkerMap = new HashMap<>();
            if ((type & IUpdateChecker.UPDATE_APK) != 0) {
                checkerMap.put(IUpdateChecker.UPDATE_APK, new ApkUpdateChecker(context));
            }
            if ((type & IUpdateChecker.UPDATE_ZIP) != 0) {
                checkerMap.put(IUpdateChecker.UPDATE_ZIP, new ZipUpdateChecker(preferences));
            }
            if ((type & IUpdateChecker.UPDATE_DB) != 0) {

            }
            return checkerMap;

        }

        private IUpdateChecker getCheckerByType(int type) {
            if (checkerMap.containsKey(type)) {
                return checkerMap.get(type);
            }
            return null;
        }

        public List<VersionInfo> getVersionInfoByType(int type) {
            IUpdateChecker updateChecker = getCheckerByType(type);
            if (updateChecker != null) {
                return updateChecker.check(checkUpdateResponse.getVersionList(type));
            }
            return Collections.emptyList();
        }
    }


    /**
     * 检查在退出App之间是否有已经下载成功的任务,有则通知出去
     *
     * @param ids
     */
    private void continueHistory(List<Long> ids) {
        List<DownloadMgr.TaskResult> result = DownloadMgr.getInstance().queryTaskResultByIds(ids);
        for (DownloadMgr.TaskResult r : result) {
            if (r.isDownloadSuccessful()) {
                onDownloadTaskSuccessful(r.id);
            }
        }

    }

    /**
     * 注册下载完成广播
     */
    private void registerDownloadCompleteReceiver() {
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (reference > 0) {
                    onDownloadTaskSuccessful(reference);
                }
            }
        };
        context.registerReceiver(receiver, filter);
    }


    /**
     * 更新信息返回
     *
     * @param response
     */
    private List<VersionInfo> filterVersionInfo(int type, CheckUpdateResponse response) {
        mCheckHelper = new CheckHelper(type, response);
        List<VersionInfo> versionInfos = new ArrayList<>();
        List<VersionInfo> zipVersionInfo = mCheckHelper.getVersionInfoByType(IUpdateChecker.UPDATE_ZIP);
        versionInfos.addAll(zipVersionInfo);
        List<VersionInfo> dbVersionInfo = mCheckHelper.getVersionInfoByType(IUpdateChecker.UPDATE_DB);
        versionInfos.addAll(dbVersionInfo);
        List<VersionInfo> apkVersionInfo = mCheckHelper.getVersionInfoByType(IUpdateChecker.UPDATE_APK);
        versionInfos.addAll(apkVersionInfo);

        LogUtils.e("更新信息返回:size=" + versionInfos.size());

        return versionInfos;
    }


    private int getMaxVersionCode(List<VersionInfo> versionInfos) {
        int maxVersionCode = 0;
        for (VersionInfo versionInfo : versionInfos) {
            int versionCode = getMaxVersionCode(versionInfo);
            if (versionCode > maxVersionCode) {
                maxVersionCode = versionCode;
            }
        }
        return maxVersionCode;
    }

    private int getMaxVersionCode(VersionInfo versionInfo) {
        try {
            int versionCode = Integer.valueOf(versionInfo.version);
            return versionCode;
        } catch (Exception e) {
        }
        return 0;
    }


    /**
     * 下载成功
     *
     * @param id
     */
    private void onDownloadTaskSuccessful(long id) {
        String localFileName = null;
        taskHistoryHelper.onTaskSuccessful(id);
        try {
            if (downloader.isExistDownloadCacheDireTaskId(id)) {
                localFileName = downloader.getFileInDownloadCacheDir(id);
            } else {
                localFileName = DownloadMgr.getInstance().querySuccessfulFileFullPath(id);
            }
            if (!TextUtils.isEmpty(localFileName)) {
                LogUtils.d("文件下载地址=%s", localFileName);
                downloader.onDownloadTaskSuccess(id, localFileName);
                if (mUpdateListener != null) {
                    mUpdateListener.onDownloadOne(localFileName);
                }
            }
        } catch (Exception e) {
            onDownloadError(e.getMessage());
        }
    }

    private void onDownloadError(String error) {
        if (mUpdateListener != null) {
            mUpdateListener.onError(error);
        }
    }


    /**
     * 安装APK
     *
     * @param localFileName
     */
    public static void rebootToInstallApk(Context context, String localFileName) {
        LogUtils.d("apk下载成功= %s", localFileName);
        CommonUtils.installPackage(context, localFileName);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 获取更新信息
     *
     * @param url
     * @param listener
     * @throws UnsupportedEncodingException
     */
    private void requestUpdateInfo(String url, CheckUpdateJsonObjectListener listener) throws UnsupportedEncodingException {
        LogUtils.d("requestUpdateCheck", "listener .........="+listener);
        JSONObject params = getUrlParam();
        if (params != null) {
            volleyRequestQueue.postRespJsonObject(url, params, listener);
        }
    }

    /**
     * 包装参数
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    private JSONObject getUrlParam() throws UnsupportedEncodingException {
        //String encodeStr = String.format("zip_version=%d&st=%d&imei=%s", ZipUpdateChecker.getLastZipVersionCode(preferences),System.currentTimeMillis(),DeviceUtil.getIMEI(context));
        //String encoded = URLEncoder.encode(encodeStr,"UTF-8");
        //String md5Str = String.format("%s&%s",encoded,Config.APP_KEY);
        //String sing = StringUtil.MD5(md5Str).toLowerCase();
        JSONObject params = new JSONObject();
        try {
            //params.put("sign", sing);
            LogUtils.e("params:%s" + params.toString());
            return params;
        } catch (Exception e) {
        }
        return null;
    }


    public void destory() {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
        if (downloader != null) {
            DownloadMgr.getInstance().clear(downloader.getTaskIds());
            downloader.clear();
        }
        if (volleyRequestQueue != null) {
            volleyRequestQueue.clear();
        }
        context = null;
        mInstance = null;
    }

    class TaskHistoryHelper {
        SharedPreferences sp;
        List<Long> taskHistory = new ArrayList<>();

        public TaskHistoryHelper(SharedPreferences sp) {
            this.sp = sp;
            String idsString = sp.getString(KEY_TASK_IDS, "");
            if (idsString.length() > 0) {
                String[] ids = idsString.split("#");
                for (String id : ids) {
                    if (id.length() > 0) {
                        taskHistory.add(Long.valueOf(id));
                    }
                }
            }
        }

        public void addTaskHistory(long taskId) {
            if (!taskHistory.contains(taskId)) {
                taskHistory.add(taskId);
                save();
            }
        }

        public void onTaskSuccessful(long taskId) {
            taskHistory.remove(taskId);
            save();
        }

        public List<Long> getTaskHistory() {
            return taskHistory;
        }

        private void save() {
            StringBuilder sb = new StringBuilder();
            for (long id : taskHistory) {
                if (sb.length() > 0) {
                    sb.append("#");
                }
                sb.append(id);
            }
            if (sb.length() > 0) {
                sp.edit().putString(KEY_TASK_IDS, sb.toString());
            }
        }
    }


    private void postProgress(final long taskId, final int progress) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mUpdateListener != null) {
                    mUpdateListener.onProgress(taskId, progress);
                }
            }
        });
    }

    private void saveVersionCode(int type, int verisonCode) {
        if (mCheckHelper != null) {
            switch (type) {
                case IUpdateChecker.UPDATE_ZIP: {
                    ZipUpdateChecker zipUpdateChecker = (ZipUpdateChecker) mCheckHelper.getCheckerByType(IUpdateChecker.UPDATE_ZIP);
                    if (zipUpdateChecker != null) {
                        zipUpdateChecker.onUpdateSuccess(verisonCode);
                    }
                    break;
                }
            }
        }
    }

    class DownloadCopleteHandler {
        private List<IProcessor> processors;

        private void deleteFile(String filepath) {
            FileUtil.deleteFile(filepath);
        }

        final FileUtil.ProcessorListener listener = new FileUtil.ProcessorListener() {
            @Override
            public void onComplete(int type, int versionCode, String filepath) {
                saveVersionCode(type, versionCode);
                if (type == IUpdateChecker.UPDATE_ZIP) {
                    deleteFile(filepath);//删除已解压的Zip文件
                }
                start();
            }

            @Override
            public void onError(String error) {
                start();
            }

            @Override
            public void onProgress(int progress) {
                LogUtils.e("资源解压进度:%d" + progress);
                if (mUpdateListener != null) {
                    mUpdateListener.updateTip("资源解压中...");
                }
                postProgress(0, progress);
            }
        };

        public DownloadCopleteHandler() {
            this.processors = getMutilProcessorList();
        }

        private void processor(IProcessor processor) {
            processor.processor(listener);
        }

        /**
         * 处理已下载
         */
        public void start() {
            if (processors != null && processors.size() > 0) {
                IProcessor processor = processors.remove(0);
                processor(processor);
            } else {
                updateComplete();
            }
        }

        private List<IProcessor> getMutilProcessorList() {
            List<IProcessor> processors = new ArrayList<>();
            EnderProcessor enderProcessor = new EnderProcessor();

            IMutilProcessor zipProcessor = new ZipProcessor();
            if (!zipProcessor.isEmpty()) {
                processors.addAll(zipProcessor.getMutilProcessor());
            }

            String databaseFilePath = downloader.getDatabaseFilePath();
            if (!TextUtils.isEmpty(databaseFilePath)) {
                enderProcessor.setFilePath(databaseFilePath);
                enderProcessor.setType(CommonUtils.UpdateType.UPDATE_TYPE_DATABASE);
            }

            IFileProcessor apkProcessor = new ApksProcessor();
            if (!apkProcessor.isEmpty()) {
                processors.addAll(apkProcessor.getMutilProcessor());
                String apkFilePath = apkProcessor.getFilePath();
                if (!TextUtils.isEmpty(apkFilePath)) {
                    enderProcessor.setFilePath(apkFilePath);
                    enderProcessor.setType(CommonUtils.UpdateType.UPDATE_TYPE_APK);
                }
            }

            if (!enderProcessor.isEmpty()) {
                processors.addAll(enderProcessor.getMutilProcessor());
            }

            return processors;
        }
    }

    private void updateComplete() {
        LogUtils.e("更新完成");
        if (mUpdateListener != null) {
            mUpdateListener.onDownloadAll();
        }

    }

    //保存maxversion
    private void saveDbUpdateVersionCode(int versionCode) {

    }


    class DownloadInfo {
        public long taskId;
        public int status;
        public int versionCode;

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String filePath;
        public boolean rebootToInstall;

        public DownloadInfo(long taskId, int versionCode) {
            this.taskId = taskId;
            this.versionCode = versionCode;
        }


        public boolean isDownloadSuccessful() {
            return status == DownloadManager.STATUS_SUCCESSFUL;
        }

        public ZipInfo getZipInfo() {
            return new ZipInfo(versionCode, filePath);
        }
    }


    class ZipProcessor implements IMutilProcessor {
        List<IProcessor> zippers;

        public ZipProcessor() {
            zippers = getZipers();
        }

        @Override
        public List<IProcessor> getMutilProcessor() {
            return zippers;
        }

        @Override
        public boolean reboot() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return (zippers == null) || (zippers.size() == 0);
        }

        private List<IProcessor> getZipers() {
            List<ZipInfo> zipFiles = downloader.getZipInfos();
            List<IProcessor> zippers = new ArrayList<>();
            if (zipFiles.size() > 0) {
                for (ZipInfo zipInfo : zipFiles) {
                    zippers.add(new Zipper(zipInfo));
                }
            }
            return zippers;
        }
    }


    class InstallInfo {
        public String filePath;
        public boolean reboot;

        public InstallInfo(String filePath, boolean rebootToInstall) {
            this.filePath = filePath;
            this.reboot = rebootToInstall;
        }
    }

    class ApksProcessor implements IFileProcessor {
        List<IProcessor> apkProcessorList;

        public ApksProcessor() {
            apkProcessorList = getInstaller();
        }

        @Override
        public List<IProcessor> getMutilProcessor() {
            return apkProcessorList;
        }

        @Override
        public boolean reboot() {
            if (isEmpty()) {
                return false;
            } else {
                ApkProcessor apkProcessor = (ApkProcessor) apkProcessorList.get(apkProcessorList.size() - 1);
                return apkProcessor.reboot();
            }
        }

        @Override
        public boolean isEmpty() {
            return apkProcessorList == null || (apkProcessorList.size() == 0);
        }

        private List<IProcessor> getInstaller() {
            List<IProcessor> apkProcessors = new ArrayList<>();
            List<InstallInfo> installInfos = downloader.getApkInstallInfo();
            if (installInfos.size() > 0) {
                for (InstallInfo info : installInfos) {
                    apkProcessors.add(new ApkProcessor(context, info));
                }
                Collections.sort(apkProcessors, new Comparator<IProcessor>() {
                    @Override
                    public int compare(IProcessor o1, IProcessor o2) {

                        return ((ApkProcessor) o1).getRebootFlag() - ((ApkProcessor) o2).getRebootFlag();
                    }
                });

                return apkProcessors;
            }
            return Collections.emptyList();
        }

        @Override
        public String getFilePath() {
            if (isEmpty()) {
                return "";
            }
            ApkProcessor processor = (ApkProcessor) apkProcessorList.get(apkProcessorList.size() - 1);
            return processor.getFilePath();
        }
    }


    class EnderProcessor implements IMutilProcessor {
        public void setType(CommonUtils.UpdateType type) {
            this.type = type;
        }

        private CommonUtils.UpdateType type;

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        private String filePath;

        public EnderProcessor() {

        }

        @Override
        public List<IProcessor> getMutilProcessor() {
            List<IProcessor> processors = new ArrayList<>();
            processors.add(new IProcessor() {
                @Override
                public boolean processor(FileUtil.ProcessorListener listener) {
                    LogUtils.e("enderProgressPath=" + filePath);
                    if (type == CommonUtils.UpdateType.UPDATE_TYPE_APK) {
                        rebootToInstallApk(context, filePath);
                    }
                    return true;
                }

                @Override
                public boolean reboot() {
                    return false;
                }
            });
            return processors;
        }

        @Override
        public boolean reboot() {
            return !isEmpty();
        }

        @Override
        public boolean isEmpty() {
            return TextUtils.isEmpty(filePath);
        }
    }

    class DownloadStatus {
        public long taskId;
        public int status;

        public DownloadStatus(long taskId, int status) {
            this.status = status;
            this.taskId = taskId;
        }
    }

    public int getCountOfDownload() {
        return mCountOfDownload;
    }

    public int getCountOfDownloaded() {
        return mIndexOfDownloading;
    }

    public interface CheckUpdateListener {
        void onUpdate(List<VersionInfo> versionInfos);

        void onNothing(int type);

        void onError(String error);
    }

}
