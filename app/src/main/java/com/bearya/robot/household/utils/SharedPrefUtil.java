package com.bearya.robot.household.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;

import java.util.Map;
import java.util.Set;


/**
 * @author yexifeng
 */
public class SharedPrefUtil {
    public static int MODE = Context.MODE_PRIVATE;
    private static String PREFERENCE_PACKAGE="com.bearya.robot.household";
    private SharedPreferences sp;
    private Editor editor;
    private String name = "com.bearya.robot.spsf";
    public final static String KEY_OPEN_ID = "openid";
    public final static String KEY_UNION_ID = "unionid";
    public final static String KEY_USER_ID = "userid";
    public final static String KEY_UID = "uid";
    public final static String KEY_ZIP_VERSION = "zip_version";
    public final static String KEY_LOGIN_STATE = "login_state";
    public final static String KEY_USER_INFO = "user_info";
    public final static String KEY_TOKEN = "token";

    private static SharedPrefUtil instance = null;

    public static SharedPrefUtil getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefUtil(context);
        }
        return instance;
    }

    public static SharedPrefUtil getWorldInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefUtil(context, true);
        }
        return instance;
    }

    private SharedPrefUtil(Context context, boolean isWorld) {
        try {
            Context c = context.createPackageContext(PREFERENCE_PACKAGE, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            this.sp = c.getSharedPreferences(name, MODE);
            this.editor = sp.edit();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private SharedPrefUtil(Context context) {
        this.sp = context.getSharedPreferences(name, MODE);
        this.editor = sp.edit();
    }

    /**
     * 添加信息到SharedPreferences
     *
     * @param map
     * @throws Exception
     */
    public SharedPrefUtil put(Map<String, String> map) {
        Set<String> set = map.keySet();
        for (String key : set) {
            editor.putString(key, map.get(key));
        }
        editor.commit();
        return this;
    }

    public SharedPrefUtil put(String key, String value) {
        editor.putString(key, value);
        editor.commit();
        editor.apply();
        return this;
    }

    public SharedPrefUtil put(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
        return this;
    }

    public SharedPrefUtil put(String key, long value) {
        editor.putLong(key, value);
        editor.commit();
        return this;
    }

    public SharedPrefUtil put(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
        return this;
    }

    /**
     * 删除信息
     *
     * @throws Exception
     */
    public void removeAll() throws Exception {
        editor.clear();
        editor.commit();
    }

    /**
     * 删除一条信息
     */
    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    /**
     * 获取信息
     *
     * @param key
     * @return
     * @throws Exception
     */
    public String getString(String key) {
        if (sp != null) {
            return sp.getString(key, "");
        }
        return "";
    }

    /**
     * 获取信息
     *
     * @param key
     * @return
     * @throws Exception
     */
    public String getString(String key, String defaultValue) {
        if (sp != null) {
            return sp.getString(key, defaultValue);
        }
        return defaultValue;
    }

    public long getLong(String key) {
        if (sp != null) {
            return sp.getLong(key, 0L);
        }
        return 0L;
    }

    public int getInt(String key) {
        if (sp != null) {
            return sp.getInt(key, 0);
        }
        return 0;
    }

    public boolean getBoolean(String key) {
        return sp != null && sp.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sp != null && sp.getBoolean(key, defaultValue);
    }

    /**
     * 获取此SharedPreferences的Editor实例
     *
     * @return
     */
    public Editor getEditor() {
        return editor;
    }

}

