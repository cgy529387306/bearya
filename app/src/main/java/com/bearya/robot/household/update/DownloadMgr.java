package com.bearya.robot.household.update;

import android.app.DownloadManager;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;


import com.bearya.robot.household.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xifengye on 2017/2/16.
 */

public class DownloadMgr {
    private static DownloadMgr mInstance;
    private DownloadManager mDownloadManager;
    private Context mContext;
    private Handler downLoadHandler = new Handler(Looper.getMainLooper());

    public void setListener(DownloadProgressListener listener) {
        this.mListener = listener;
    }

    private DownloadProgressListener mListener;

    public interface DownloadProgressListener{
        public void onProgress(int progress);
    }


    private DownloadMgr() {

    }

    public void init(Context context){
        this.mContext = context;
        mDownloadManager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public static DownloadMgr getInstance(){
        if(mInstance==null){
            mInstance = new DownloadMgr();
        }
        return mInstance;
    }

    public Cursor query(DownloadManager.Query query) {
        return mDownloadManager.query(query);
    }

    public void clear(long... ids) {
        if (ids == null) {
            return;
        }
        if(mDownloadManager!=null){
            mDownloadManager.remove(ids);
        }
        if(downloadObserver!=null){
            downloadObserver.destory();
        }
        setListener(null);
    }


    public static class DownloadTask{
        public String url;
        public String fileName;
    }

    private static class TaskStatus{
        public long id;
        public String title;
        public String localUri;
        public int status;
        public long size;
        public long sizeTotal;
        public String url;
        public String fileName;
    }



    public TaskResult addTask(DownloadTask downloadTask){
        TaskStatus taskStatus = queryDownTask(downloadTask.url);
        TaskResult result = new TaskResult();
        result.status = DownloadManager.STATUS_RUNNING;
        if(taskStatus==null){
            LogUtils.e("新建下载项目");
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadTask.url));
            request.setDestinationInExternalFilesDir(mContext, Environment.DIRECTORY_DOWNLOADS, downloadTask.fileName);
            result.id = addRequest(request);
        }else {
            result.status = taskStatus.status;
            result.id = taskStatus.id;
        }
        //注册查询下载进度
        if(result.status != DownloadManager.STATUS_SUCCESSFUL){
            registerDownloadProgress(result.id);
        }
        return result;
    }

    public Uri fileFullPath(long id){
        return mDownloadManager.getUriForDownloadedFile(id);
    }

    public String querySuccessfulFileFullPath(long id){
        TaskStatus taskStatus = queryTaskById(id);
        if(taskStatus!=null && taskStatus.status == DownloadManager.STATUS_SUCCESSFUL){
            return taskStatus.fileName;
        }
        return "";
    }

    private long addRequest(DownloadManager.Request request){
        return mDownloadManager.enqueue(request);
    }

    private TaskStatus queryDownTask(String url){
        Map<String,TaskStatus> taskStatuses = queryDownTask(DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_SUCCESSFUL|DownloadManager.STATUS_PAUSED);
        return taskStatuses.get(url);
    }

    private Map<String,TaskStatus> queryDownTask(int status) {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(status);
        Map<String, TaskStatus> taskStatusMap = new HashMap<>();
        if (mDownloadManager.query(query) != null) {
            Cursor cursor = mDownloadManager.query(query);
            while (cursor.moveToNext()) {
                TaskStatus taskStatus = parse(cursor);
                if (taskStatus != null) {
                    taskStatusMap.put(taskStatus.url, taskStatus);
                }
            }
            cursor.close();
        }
        return taskStatusMap;
    }

    public List<TaskResult> queryTaskResultByIds(List<Long> ids){
        List<TaskResult> results = new ArrayList<>();
        for(long id:ids){
            TaskStatus taskStatus = queryTaskById(id);
            TaskResult r = new TaskResult();
            r.id = taskStatus.id;
            r.status = taskStatus.status;
            results.add(r);
        }
        return results;
    }
    private TaskStatus queryTaskById(long id){
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor cursor= mDownloadManager.query(query);
        TaskStatus taskStatus = null;
        if(cursor.moveToNext()){
            taskStatus = parse(cursor);
        }
        cursor.close();
        return taskStatus;
    }

    private TaskStatus parse(Cursor cursor)
    {
        TaskStatus taskStatus = new TaskStatus();
        long downId= cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
        String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
        String localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        String url = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
        long size= cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        long sizeTotal = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        String type = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));
        int fileNameId = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        String fileName = "";
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//                openFile(type, Uri.parse(localUri));
            Uri uri = Uri.parse(localUri);
            if(uri!=null){
                fileName = uri.getPath();
            }

        } else {
            /**Android 7.0以上的方式：请求获取写入权限，这一步报错**/
            fileName = cursor.getString(fileNameId);
//                openFile(type, Uri.parse(localUri));
        }
        taskStatus.id = downId;
        taskStatus.title = title;
        taskStatus.localUri = localUri;
        taskStatus.fileName = fileName;
        taskStatus.status = status;
        taskStatus.size = size;
        taskStatus.sizeTotal = sizeTotal;
        taskStatus.url = url;

        cursor.close();
        return taskStatus;
    }

//    private void openFile(String type, Uri uri) {
//        if (type.contains("image/")) {
//            try {
//                ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(uri, "r");
//                FileDescriptor fileDescriptor = descriptor.getFileDescriptor();
//                /**下面这句话运行效果等同于：BitmapFactory.decodeFileDescriptor()**/
//                Bitmap bitmap = BitmapFactory.decodeStream(getStreamByFileDescriptor(fileDescriptor));
////                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
//                mShowPic_iv.setVisibility(View.VISIBLE);
//                mShowPic_iv.setImageBitmap(bitmap);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    public static class TaskResult {
        public long id;
        public int status;

        @Override
        public String toString() {
            return "TaskResult{" +
                    "id=" + id +
                    ", status=" + (status==DownloadManager.STATUS_SUCCESSFUL?"已下载":"下载中...") +
                    '}';
        }

        public boolean isDownloadSuccessful(){
            return status == DownloadManager.STATUS_SUCCESSFUL;
        }
    }

    private DownloadChangeObserver downloadObserver;

    private void registerDownloadProgress(long downId){
        if(downloadObserver!=null) {
            downloadObserver.destory();
            downloadObserver = null;
        }
        downloadObserver = new DownloadChangeObserver(downId);
        mContext.getContentResolver().registerContentObserver(Uri.parse("content://downloads/"), true, downloadObserver);

    }



    /**
     * 监听下载进度
     */
    private class DownloadChangeObserver extends ContentObserver {
        private static final long FIXED_RATE = 1000;
//        private String tip;
        private long downId;
        private long mLastNotifyProgressTimeStamp;
        private Runnable progressRunnable = new Runnable() {
            @Override
            public void run() {
                int downloadProgress = getBytesAndStatus(downId);
                if(mListener!=null){
                    mListener.onProgress(downloadProgress);
                }
            }
        };


        public DownloadChangeObserver(long downId) {
            super(downLoadHandler);
            this.downId = downId;
        }

        public void destory(){
            mContext.getContentResolver().unregisterContentObserver(this);
        }

        /**
         * 当所监听的Uri发生改变时，就会回调此方法
         *
         * @param selfChange 此值意义不大, 一般情况下该回调值false
         */
        @Override
        public void onChange(boolean selfChange) {
            long timeStamp = System.currentTimeMillis();
            if(timeStamp-mLastNotifyProgressTimeStamp > FIXED_RATE) {
                mLastNotifyProgressTimeStamp = timeStamp;
                progressRunnable.run();
            }
        }

    }
    /**
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
     *
     * @param downloadId
     * @return
     */
    private int getBytesAndStatus(long downloadId) {
        int[] bytesAndStatus = new int[]{-1, -1, 0};
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = null;
        try {
            cursor = DownloadMgr.getInstance().query(query);
            if (cursor != null && cursor.moveToFirst()) {
                //已经下载文件大小
                bytesAndStatus[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                //下载文件的总大小
                bytesAndStatus[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager          .COLUMN_TOTAL_SIZE_BYTES));
                //下载状态
                bytesAndStatus[2] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return (int)(bytesAndStatus[0]*100.0f/bytesAndStatus[1]);
    }


}
