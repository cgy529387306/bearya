package com.bearya.robot.household.utils;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 通用帮助类
 * @author chengfu.bao
 *
 */
public class DateHelper {
	/**
	 * 时间格式
	 */
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	/**
	 * 转换字符串为日期类型
	 * @param dateStr 日期字符串
	 * @return 日期
	 */
	public static Date string2Date(String dateStr) {
		return string2Date(dateStr, DATE_FORMAT);
	}
	/**
	 * 转换字符串为日期类型
	 * @param dateStr 日期字符串
	 * @param dateFormat 日期字符串格式
	 * @return 日期
	 */
	public static Date string2Date(String dateStr, String dateFormat) {
		Date result = null;
		try {
			DateFormat df = new SimpleDateFormat(dateFormat,Locale.CHINA);
			result = df.parse(dateStr);
		} catch (Exception e) {
			result = null;
		}
		return result;
	}
	/**
	 * 转换当前日期为字符串
	 * @return
	 */
	public static String date2String(){
		return date2String(new Date());
	}
	/**
	 * 转换当前日期为字符串
	 * @param dateFormat
	 * @return
	 */
	public static String date2String(String dateFormat){
		return date2String(new Date(), dateFormat);
	}
	/**
	 * 转换日期为字符串
	 * @param date 日期
	 * @return 日期字符串
	 */
	public static String date2String(Date date){
		return date2String(date, DATE_FORMAT);
	}
	/**
	 * 转换日期为字符串
	 * @param date 日期
	 * @param dateFormat 日期字符串格式
	 * @return 日期字符串
	 */
	public static String date2String(Date date, String dateFormat){
		String dateStr = "";
		try{
			DateFormat df = new SimpleDateFormat(dateFormat,Locale.CHINA);
			dateStr = df.format(date);
		}catch (Exception e){
			dateStr = "";
		}
		return dateStr;
	}


	/**
	 * Java将Unix时间戳转换成指定格式日期字符串
	 * @param timestampString 时间戳 如："1473048265";
	 * @param formats 要格式化的格式 默认："yyyy-MM-dd HH:mm:ss";
	 *
	 * @return 返回结果 如："2016-09-05 16:06:42";
	 */
	public static String timeStamp2Date(String timestampString, String formats) {
		if (TextUtils.isEmpty(formats))
			formats = "yyyy-MM-dd HH:mm:ss";
		Long timestamp = Long.parseLong(timestampString) * 1000;
        return new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));
	}

    /**
     * Java将Unix时间戳转换成指定格式日期字符串
     * @param timestamp 时间戳 如："1473048265";
     * @param formats 要格式化的格式 默认："yyyy-MM-dd HH:mm:ss";
     *
     * @return 返回结果 如："2016-09-05 16:06:42";
     */
    public static String timeStamp2Date(long timestamp, String formats) {
        if (TextUtils.isEmpty(formats))
            formats = "yyyy-MM-dd HH:mm:ss";
        Long time = timestamp * 1000;
        return new SimpleDateFormat(formats, Locale.CHINA).format(new Date(time));
    }


	/**
     * 日期格式字符串转换成时间戳
     *
     * @param dateStr 字符串日期
     * @param format   如：yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String date2TimeStamp(String dateStr, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format,Locale.CHINA);
            return String.valueOf(sdf.parse(dateStr).getTime() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 日期格式字符串转换成时间戳
     *
     *
     * @return
     */
    public static long date2TimeStamp(Date date) {
        return date==null?new Date().getTime()/1000:date.getTime()/1000;
    }






}
