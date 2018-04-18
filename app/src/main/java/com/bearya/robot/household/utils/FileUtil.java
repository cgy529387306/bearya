package com.bearya.robot.household.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by yexifeng on 16/8/30.
 */
public class FileUtil {

    public static String getEmotionFilePath(Context context,String name){
        return String.format("%s/emotion/%s",context.getFilesDir(),name);
    }

    public static void copyFile(InputStream input,String newPath) throws IOException{
        FileOutputStream output = new FileOutputStream(newPath);
        byte[] b = new byte[1024 * 2];
        int len;
        while ((len = input.read(b)) != -1) {
            output.write(b, 0, len);
        }
        output.flush();
        output.close();
        input.close();
    }
    /**
     * 复制单个文件
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public static void copyFile(String oldPath, String newPath) throws IOException{
        File oldfile = new File(oldPath);
        if (oldfile.exists()) { //文件存在时
           copyFile( new FileInputStream(oldPath),newPath);
        }

    }

    /**
     * 复制整个文件夹内容
     * @param oldPath String 原文件路径 如：c:/fqf
     * @param newPath String 复制后路径 如：f:/fqf/ff
     * @return boolean
     */
    public static void copyFolder(String oldPath, String newPath) throws IOException{
        (new File(newPath)).mkdirs(); //如果文件夹不存在 则建立新文件夹
        File a=new File(oldPath);
        String[] file=a.list();
        File temp=null;
        for (String aFile : file) {
            if (!oldPath.endsWith(File.separator)) {
                oldPath = oldPath + File.separator;
            }
            String filePath = oldPath + aFile;
            temp = new File(filePath);

            if (temp.isFile()) {
                String newFilePath = newPath + File.separator + (temp.getName());
                copyFile(filePath, newFilePath);
            }
            if (temp.isDirectory()) {//如果是子文件夹
                copyFolder(oldPath + aFile, newPath + File.separator + aFile);
            }
        }
    }

    /**
     *  从assets目录中复制整个文件夹内容
     *  @param  context  Context 使用CopyFiles类的Activity
     *  @param  oldPath  String  原文件路径  如：/aa
     *  @param  newPath  String  复制后路径  如：xx:/bb/cc
     */
    public static void copyAssetsFolder(Context context,String oldPath,String newPath) throws  IOException{

            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyAssetsFolder(context,oldPath + File.separator + fileName,newPath+File.separator+fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                copyFile(is,newPath);
            }
    }

    public static void copyAssetsDatabase(Context context,String databaseFilePath)throws IOException{
        InputStream is = context.getAssets().open(databaseFilePath);
        File file = new File(String.format("data/data/%s/databases/",context.getPackageName()));
        if(!file.exists()){
            file.mkdir();
        }
        String newPath = String.format("data/data/%s/databases/bearya.db",context.getPackageName());
        copyFile(is,newPath);
    }

    public static String inputStream2String(InputStream is) throws IOException{
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }
        return buffer.toString();
    }

    public static String file2String(String filePath)throws IOException{
        InputStream inputStream = file2InputStream(filePath);
        return inputStream2String(inputStream);
    }

    public static String stringFromAssetsFile(Context context,String filePath) throws IOException {
        InputStream inputStream = context.getAssets().open(filePath);
        if(inputStream!=null) {
            return inputStream2String(inputStream);
        }
        return "";
    }

    public static InputStream file2InputStream(String filePath)throws IOException{
        return new FileInputStream(filePath);
    }

    public static String getSDPath(){
        boolean hasSDCard = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if(hasSDCard){
            return Environment.getExternalStorageDirectory().toString();
        }else
            return Environment.getDownloadCacheDirectory().toString();
    }

    public static void saveFile(String str,String filePath,String fileName){
        OutputStream fos = null;
        try {
            File file = new File(filePath);
            if(!file.exists())
                file.mkdir();
            File file1 = new File(filePath,fileName);
            fos = new FileOutputStream(file1);
            fos.write(str.getBytes());
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try {
                if(null!=fos)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String readFile(String filePath){
        InputStream fis = null;
        int len;
        byte[] mByte = new byte[1];
        try {
            fis = new FileInputStream(new File(filePath));
            len = fis.read(mByte);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                if(null!=fis)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new String(mByte);
    }

    public static void copyAssets(String assetDir, String dir,Context context) {
        String[] files;
        try {
            files = context.getResources().getAssets().list(assetDir);
        } catch (IOException e1) {
            return;
        }
        File mWorkingPath = new File(dir);
        //if this directory does not exists, make one.
        if (!mWorkingPath.exists()) {
            mWorkingPath.mkdirs();
        }
        Log.e("FileUtil拷贝歌曲", "---");

        for (String file : files) {
            try {

                File outFile = new File(mWorkingPath, file);
                if (outFile.exists())
//					outFile.delete();
                    continue;
                InputStream in = null;
                if (0 != assetDir.length())
                    in = context.getAssets().open(assetDir + "/" + file);
                else
                    in = context.getAssets().open(file);
                OutputStream out = new FileOutputStream(outFile);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                Log.e("FileUtil拷贝歌曲", "fileName:" + file + "---mWorkingPath" + mWorkingPath);
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.isFile() && file.exists() && file.delete();
    }



    public static void unZipFile(int versionCode,final String zipFileString, final String outPathString, final ProcessorListener listener) {
        Thread zipThread = new UnZipMainThread(versionCode,zipFileString, outPathString, listener);
        zipThread.start();
    }

    /**
     * 获取压缩包解压后的内存大小
     *
     * @return 返回内存long类型的值
     */
    public static long getZipTrueSize(String filePath) {
        long size = 0;
        ZipFile f;
        try {
            f = new ZipFile(filePath);
            Enumeration<? extends ZipEntry> en = f.entries();
            while (en.hasMoreElements()) {
                size += en.nextElement().getSize();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return size;
    }

    public interface ProcessorListener {
        public void onComplete(int type, int versionCode, String filepath);
        public void onError(String error);
        public void onProgress(int progress);
    }

}
