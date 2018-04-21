package com.bearya.robot.household.utils;

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
			DateFormat df = new SimpleDateFormat(dateFormat);
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
			DateFormat df = new SimpleDateFormat(dateFormat);
			dateStr = df.format(date);
		}catch (Exception e){
			dateStr = "";
		}
		return dateStr;
	}
	/**
	 * 日期转换成毫秒
	 * @param date
	 * @return
	 */
	public static long date2Long(Date date){
		return date.getTime();
	}
	/**
	 * 当前日期转换成毫秒
	 * @return
	 */
	public static long date2Long(){
		return new Date().getTime();
	}
	/**
	 * 当前日期转换成毫秒字符串
	 * @return
	 */
	public static String date2LongString(){
		return String.valueOf(date2Long());
	}
	/**
	 * 毫秒转换成日期
	 * @param ms
	 * @return
	 */
	public static Date long2Date(long ms){
		return new Date(ms);
	}
	/**
	 * 将时间ms数转换为日期字符串
	 * @param ms
	 * @return
	 */
	public static String long2DateString(long ms){
		return long2DateString(ms, DATE_FORMAT);
	}
	/**
	 * 将时间ms数转换为日期字符串
	 * @param ms
	 * @param dateFormat
	 * @return
	 */
	public static String long2DateString(long ms, String dateFormat){
		String result = null;
		Date date = long2Date(ms);
		result = date2String(date, dateFormat);
		if (result==null){
			result = String.valueOf(ms);
		}
		return result;
	}
	/**
	 * 日期字符串转换为ms值
	 * @param dateString
	 * @return
	 */
	public static long dateString2Long(String dateString){
		return dateString2Long(dateString, DATE_FORMAT);
	}
	/**
	 * 日期字符串转换为ms值
	 * @param dateString
	 * @param dateFormat
	 * @return
	 */
	public static long dateString2Long(String dateString, String dateFormat){
		Date date = string2Date(dateString, dateFormat);
		if (date!=null){
			return date2Long(date);
		}
		return 0;
	}

	/*
     * 将时间转换为时间戳
     */
	public static String date2Stamp(String time) {
		try{
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());
			Date date = simpleDateFormat.parse(time);
			long ts = date.getTime();
			time = String.valueOf(ts);
			return time.substring(0, 10);
		}catch (Exception e){
			return time;
		}
	}

}
