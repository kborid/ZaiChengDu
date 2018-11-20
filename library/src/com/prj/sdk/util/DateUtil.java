package com.prj.sdk.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期操作工具类.
 * 
 * @author LiaoBo
 */

public class DateUtil {

	private static final String	FORMAT	= "yyyy-MM-dd HH:mm:ss";

	public static Date str2Date(String str) {
		return str2Date(str, null);
	}

	public static Date str2Date(String str, String format) {
		if (str == null || str.length() == 0) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			date = sdf.parse(str);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;

	}

	public static Calendar str2Calendar(String str) {
		return str2Calendar(str, null);

	}

	public static Calendar str2Calendar(String str, String format) {

		Date date = str2Date(str, format);
		if (date == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		return c;

	}

	public static String date2Str(Calendar c) {// yyyy-MM-dd HH:mm:ss
		return date2Str(c, null);
	}

	public static String date2Str(Calendar c, String format) {
		if (c == null) {
			return null;
		}
		return date2Str(c.getTime(), format);
	}

	public static String date2Str(Date d) {// yyyy-MM-dd HH:mm:ss
		return date2Str(d, null);
	}

	public static String date2Str(Date d, String format) {// yyyy-MM-dd HH:mm:ss
		if (d == null) {
			return null;
		}
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String s = sdf.format(d);
		return s;
	}

	public static String getCurDateStr() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		StringBuilder sb = new StringBuilder();
		return sb.append(c.get(Calendar.YEAR)).append("-").append((c.get(Calendar.MONTH) + 1)).append("-").append(c.get(Calendar.DAY_OF_MONTH)).append(" ")
				.append(c.get(Calendar.HOUR_OF_DAY)).append(":").append(c.get(Calendar.MINUTE)).append(":").append(c.get(Calendar.SECOND)).toString();
	}

	/**
	 * 获得当前日期的字符串格式
	 * 
	 * @param format
	 * @return
	 */
	public static String getCurDateStr(String format) {
		Calendar c = Calendar.getInstance();
		return date2Str(c, format);
	}

	// 格式到秒
	public static String getMillon(long time) {

		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(time);

	}

	// 格式到分
	public static String getMinutes(long time) {
		return getMinutes(time, FORMAT);
	}
	public static String getMinutes(long time, String format) {
		if (format == null || format.length() == 0) {
			format = FORMAT;
		}
		return new SimpleDateFormat(format).format(time);
	}

	// 格式到天
	public static String getDay(long time) {

		return new SimpleDateFormat("yyyy-MM-dd").format(time);

	}

	// 格式到毫秒
	public static String getSMillon(long time) {

		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(time);

	}

	/**
	 * 检查有效时间
	 * 
	 * @param time
	 * @param begTime
	 * @param endTime
	 * @return
	 */
	public static boolean checkValidTime(String time, String begTime, String endTime) {
		if (time == null)
			return false;
		return time.compareTo(begTime) >= 0 && time.compareTo(endTime) <= 0;
	}

	/**
	 * 获取两个日期之间的间隔天数
	 * 
	 * @return
	 */
	public static int getGapCount(Date startDate, Date endDate) {
		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.setTime(startDate);
		fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
		fromCalendar.set(Calendar.MINUTE, 0);
		fromCalendar.set(Calendar.SECOND, 0);
		fromCalendar.set(Calendar.MILLISECOND, 0);

		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTime(endDate);
		toCalendar.set(Calendar.HOUR_OF_DAY, 0);
		toCalendar.set(Calendar.MINUTE, 0);
		toCalendar.set(Calendar.SECOND, 0);
		toCalendar.set(Calendar.MILLISECOND, 0);

		return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
	}

	/**
	 * 根据给定的时间字符串，返回月 日 时 分 秒
	 * 
	 * @param allDate
	 *            like "yyyy-MM-dd hh:mm:ss SSS"
	 * @return
	 */
	public static String getMonthTomTime(String allDate) {
		return allDate.substring(5, 19);
	}

	/**
	 * 根据给定的时间字符串，返回月 日 时 分 月到分钟
	 * 
	 * @param allDate
	 *            like "MM-dd hh:mm"
	 * @return
	 */
	public static String getMonthTime(String allDate) {
		return allDate.substring(5, 16);
	}

	/**
	 * 根据给定的时间字符串，返回年月日
	 * 
	 * @param allDate
	 *            like "yyyy-MM-dd"
	 * @return
	 */
	public static String getY_M_D(String allDate) {
		return allDate.substring(0, 10);
	}

	/**
	 * 判断白天还是夜晚
	 * 
	 * @return true 夜晚
	 */
	public static boolean getDayOrNight() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH");
		String hour = sdf.format(new Date());
		int k = Integer.parseInt(hour);
		if ((k >= 0 && k < 6) || (k >= 18 && k < 24)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 日期变量转成对应的星期字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToWeek(Date date) {
		String WEEK[] = {"星期日", "星期一 ", "星期二", "星期三 ", "星期四 ", "星期五 ", "星期六"};
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int dayIndex = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayIndex < 1 || dayIndex > 7) {
			return null;
		}

		return WEEK[dayIndex - 1];
	}
}
