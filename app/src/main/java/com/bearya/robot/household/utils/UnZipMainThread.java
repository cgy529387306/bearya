package com.bearya.robot.household.utils;

/**
 * Created by yexifeng on 17/8/29.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class UnZipMainThread extends Thread {

    int versionCode;
    String zipFileString;
    String outPathString;
    FileUtil.ProcessorListener listener;

    public UnZipMainThread(int version, String zipFileString, String outPathString, FileUtil.ProcessorListener listener) {
        this.zipFileString = zipFileString;
        this.outPathString = outPathString;
        this.listener = listener;
        this.versionCode = version;
    }

    @Override
    public void run() {
        super.run();
        try {
            listener.onProgress(0);
            long sumLength = 0;
            // 获取解压之后文件的大小,用来计算解压的进度
            long ziplength = FileUtil.getZipTrueSize(zipFileString);
            System.out.println("====文件的大小==" + ziplength);
            FileInputStream inputStream = new FileInputStream(zipFileString);
            ZipInputStream inZip = new ZipInputStream(inputStream);

            ZipEntry zipEntry;
            String szName = "";
            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    folder.mkdirs();
                } else {
                    File file = new File(outPathString + File.separator + szName);
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = inZip.read(buffer)) != -1) {
                        sumLength += len;
                        int progress = (int) ((sumLength * 100) / ziplength);
                        updateProgress(progress, listener);
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }
            listener.onComplete(1,versionCode,zipFileString);
            inZip.close();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError("");
        }
    }

    int lastProgress = 0;

    private void updateProgress(int progress, FileUtil.ProcessorListener listener2) {
        /** 因为会频繁的刷新,这里我只是进度>1%的时候才去显示 */
        if (progress > lastProgress) {
            lastProgress = progress;
            listener2.onProgress(progress);
        }
    }



}
