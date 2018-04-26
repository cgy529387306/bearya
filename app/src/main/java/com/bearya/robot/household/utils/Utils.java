package com.bearya.robot.household.utils;

import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

public final class Utils {

	public final static String CATEGORY = "";
	public final static int SUCCEESS = 10000;
	public final static int FAILURE = 10001;
	public static boolean ISLOGIN = false;
	public final static String APPID="wx2525acb42cc09c25";
	public final static String APPSECRET="9ea6c05b66af01e4694993b02a1b4865";
	public static String CURRENT_USER="";
	
	
	static class Platform {

		private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
		private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
		private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

		static class BuildProperties {

			private final Properties properties;

			private BuildProperties() throws IOException {
				properties = new Properties();
				properties.load(new FileInputStream(new File(Environment
						.getRootDirectory(), "build.prop")));
			}

			public boolean containsKey(final Object key) {
				return properties.containsKey(key);
			}

			public boolean containsValue(final Object value) {
				return properties.containsValue(value);
			}

			public Set<java.util.Map.Entry<Object, Object>> entrySet() {
				return properties.entrySet();
			}

			public String getProperty(final String name) {
				return properties.getProperty(name);
			}

			public String getProperty(final String name,
					final String defaultValue) {
				return properties.getProperty(name, defaultValue);
			}

			public boolean isEmpty() {
				return properties.isEmpty();
			}

			public Enumeration<Object> keys() {
				return properties.keys();
			}

			public Set<Object> keySet() {
				return properties.keySet();
			}

			public int size() {
				return properties.size();
			}

			public Collection<Object> values() {
				return properties.values();
			}

			public static BuildProperties newInstance() throws IOException {
				return new BuildProperties();
			}
		}

		/**
		 * 是否是 小米设备
		 * 
		 * @return
		 */
		public static boolean isMIUI() {
			try {
				final BuildProperties prop = BuildProperties.newInstance();
				return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
						|| prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
						|| prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
			} catch (final IOException e) {
				return false;
			}
		}

		/**
		 * 是否是 魅族设备
		 * 
		 * @return
		 */
		public static boolean isFlyme() {
			try {
				final Method method = Build.class.getMethod("hasSmartBar");
				return method != null;
			} catch (final Exception e) {
				return false;
			}
		}
	}

	public static class StatusBarIconManager {
		/**
		 * 
		 * @param act
		 * @param type
		 */
		public static void color(Activity act, TYPE type) {

			if (Platform.isMIUI()) {
				MIUI(act, type);
			} else if (Platform.isFlyme()) {
				Flyme(act, type);
			}
		}

		/**
		 * 魅族改变状态栏图标颜色
		 * 
		 * @param act
		 */
		public static void Flyme(Activity act, TYPE type) {
			try {
				WindowManager.LayoutParams lp = act.getWindow().getAttributes();
				Field meizuFlags = WindowManager.LayoutParams.class
						.getDeclaredField("meizuFlags");
				int newFlag = 0;

				if (type == TYPE.BLACK)
					newFlag = meizuFlags.getInt(lp) | 0x200; // 如果要改成白的，就把meizuFlags
																// &~0x200 就可以了。
				else if (type == TYPE.WHITE)
					newFlag = meizuFlags.getInt(lp) & ~0x200;

				meizuFlags.set(lp, newFlag);
				act.getWindow().setAttributes(lp);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * MIUI 改变状态栏图标颜色 只支持 6 或以上
		 * 
		 * @param context
		 * @param type
		 */
		public static void MIUI(Activity context, TYPE type) {
			Window window = context.getWindow();
			Class clazz = window.getClass();
			try {
				int tranceFlag = 0;
				int darkModeFlag = 0;
				Class layoutParams = Class
						.forName("android.view.MiuiWindowManager$LayoutParams");
				Field field = layoutParams
						.getField("EXTRA_FLAG_STATUS_BAR_TRANSPARENT");
				tranceFlag = field.getInt(layoutParams);
				field = layoutParams
						.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
				darkModeFlag = field.getInt(layoutParams);
				Method extraFlagField = clazz.getMethod("setExtraFlags",
						int.class, int.class);
				if (type == TYPE.TRANSPARENT) {
					extraFlagField.invoke(window, tranceFlag, tranceFlag);// 只需要状态栏透明
				} else if (type == TYPE.BLACK) {
					extraFlagField.invoke(window, tranceFlag | darkModeFlag,
							tranceFlag | darkModeFlag);// 状态栏透明且黑色字体
				} else if (type == TYPE.WHITE) {
					extraFlagField.invoke(window, 0, darkModeFlag);// 清除黑色字体
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public enum TYPE {
			TRANSPARENT, WHITE, BLACK
		}
	}
}
