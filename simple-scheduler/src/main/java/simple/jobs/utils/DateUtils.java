package simple.jobs.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang.time.DateUtils {
	
	public static final int MINIMAL_DAYS_IN_FIRSTWEEK = 4;
	public static final int FIRST_DAY_OF_WEEK = 2;

	public static Date parse(String date, String pattern) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			sdf.setLenient(false);
			return sdf.parse(date);
		}
		catch (Exception ex) {
		}
		return null;
	}
	
	/**
	 * @描述: 得到date的昨天
	 * 
	 */
	public static Date getYesterdayByDate(String date, String pattern) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			sdf.setLenient(false);
			Date temp = sdf.parse(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(temp);
			cal.add(Calendar.DAY_OF_MONTH, -1);
			return cal.getTime();
		}
		catch (Exception ex) {
		}
		return null;
	}

	/**
	 * @描述: 得到某一月的天数
	 * 
	 */
	public static int getDaysOfMonth(int year, int month) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, 1);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @描述: 得到某一月的天数
	 * 
	 */
	public static int getDaysOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @描述: 得到某一季的天数
	 * 
	 */
	public static int getDaysOfQuarter(int year, int quarter) {
		Calendar cal = Calendar.getInstance();
		int days = 0;
		int firstMonth = (quarter - 1) * 3;
		for (int i = firstMonth; i < firstMonth + 3; i++) {
			cal.set(year, i, 1);
			days += cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		}
		return days;
	}

	/**
	 * @描述: 得到某一年的天数
	 * 
	 */
	public static int getDaysOfYear(int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, 0, 1);
		return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	/**
	 * @描述: 得到某一年的天数
	 * 
	 */
	public static int getDaysOfYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	/**
	 * @描述: 得到某一周的星期一的时间
	 * 
	 */
	public static Date getFirstDayOfWeek(int year, int week) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(FIRST_DAY_OF_WEEK);
		cal.setMinimalDaysInFirstWeek(MINIMAL_DAYS_IN_FIRSTWEEK);
		cal.set(year, Calendar.JANUARY, 1);
		int addDays = week * 7;
		if (cal.get(Calendar.WEEK_OF_YEAR) == 1) {
			addDays = (week - 1) * 7;
		}
		cal.add(Calendar.DATE, addDays);
		cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
		return cal.getTime();

	}

	/**
	 * @描述: 得到某一周的第一天
	 * 
	 */
	public static Date getFirstDayOfWeek(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(FIRST_DAY_OF_WEEK);
		cal.setMinimalDaysInFirstWeek(MINIMAL_DAYS_IN_FIRSTWEEK);
		cal.setTime(date);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 1) {
			dayOfWeek = 8;
		}
		
		cal.setTimeInMillis((cal.getTimeInMillis() + (FIRST_DAY_OF_WEEK - dayOfWeek) * 86400000L));
		return cal.getTime();
	}

	/**
	 * @描述: 得到某一月的第一天
	 * 
	 */
	public static Date getFirstDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}
	
	/**
	 * @描述: 得到某一月的最后一天
	 * 
	 */
	public static Date getLastDayOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}

	/**
	 * @描述: 得到上月的第一天
	 * 
	 */
	public static Date getFirstDayOfLastMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cal.getTimeInMillis() - cal.get(Calendar.DAY_OF_MONTH) * 86400000L);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
	}

	/**
	 * @描述: 得到上月的最后一天
	 * 
	 */
	public static Date getLastDayOfLastMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cal.getTimeInMillis() - cal.get(Calendar.DAY_OF_MONTH) * 86400000L);
		return cal.getTime();
	}

	/**
	 * @描述: 得到下月的第一天
	 * 
	 */
	public static Date getFirstDayOfNextMonth() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.setTimeInMillis(cal.getTimeInMillis() + cal.getActualMaximum(Calendar.DAY_OF_MONTH) * 86400000L);
		return cal.getTime();
	}

	/**
	 * @描述: 得到下月的最后一天
	 * 
	 */
	public static Date getLastDayOfNextMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(cal.getTimeInMillis() + cal.getActualMaximum(Calendar.DAY_OF_MONTH) * 86400000L);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.setTimeInMillis(cal.getTimeInMillis() + (cal.getActualMaximum(Calendar.DAY_OF_MONTH)-1) * 86400000L);
		return cal.getTime();
	}


	/**
	 * @描述: 得到某一日期的周数
	 * 
	 */
	public static int getWeekOfDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.setFirstDayOfWeek(FIRST_DAY_OF_WEEK);
		cal.setMinimalDaysInFirstWeek(MINIMAL_DAYS_IN_FIRSTWEEK);
		cal.set(year, month - 1, day, 0, 0, 0);
		return cal.get(Calendar.WEEK_OF_YEAR);

	}

	/**
	 * @描述: 得到某一日期的天数
	 * 
	 */
	public static int getDayOfDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, day, 0, 0, 0);
		return cal.get(Calendar.DAY_OF_YEAR);
	}
	
	public static Date getDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date getDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date yesterday() {
		return new Date(System.currentTimeMillis() - 86400000L);
	}
	
	public static Date getEndDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2099);
		cal.set(Calendar.MONTH, 12 - 1);
		cal.set(Calendar.DATE, 31);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	//根据日期取得星期几  
    public static String getWeek(Date date){  
        String[] weeks = {"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};  
        Calendar cal = Calendar.getInstance();  
        cal.setTime(date);  
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;  
        if(week_index<0){  
            week_index = 0;  
        }   
        return weeks[week_index];  
    }  
    
    public static void main(String[] args) {
		System.out.println(getWeek(new Date()));
	}
}
