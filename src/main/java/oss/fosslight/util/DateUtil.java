/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.i18n.LocaleContextHolder;

import oss.fosslight.common.CoConstDef;

/**
 * DESC : Joda Time 을 사용하여 날짜, 시간 및 요일 계산, 유효성 체크와 포맷 변경 등의 기능을 제공한다.<br><br>
 * 
 * Apache LICENSE-2.0의 Anyframe Utils(Version 1.0.1)을 기반으로 작성된 클래스임을 명시한다.
 * 
 */
public final class DateUtil {

	private DateUtil() { }
	
	public static final int HOURS_24 = 24;
	
	public static final int MINUTES_60 = 60;
	
	public static final int SECONDS_60 = 60;

	public static final int MILLI_SECONDS_1000 = 1000;
	
	/** Date pattern */
	public static final String DATE_PATTERN_DASH = "yyyy-MM-dd";

	/** Time pattern */
	public static final String TIME_PATTERN = "HH:mm";

	/** Date Time pattern */
	public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

	/** Date HMS pattern */
	public static final String DATE_HMS_PATTERN = "yyyyMMddHHmmss";
	
	public static final String DATE_HMS_PATTERN2 = "yyyyMMddHHmm";

	/** Time stamp pattern */
	public static final String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";

	/** year pattern (yyyy)*/
    public static final String YEAR_PATTERN = "yyyy";

    /** month pattern (MM) */
    public static final String MONTH_PATTERN = "MM";

    /** day pattern (dd) */
    public static final String DAY_PATTERN = "dd";
    
    /** date pattern (yyyyMMdd) */
    public static final String DATE_PATTERN = "yyyyMMdd";

    /** date pattern (yyyy년 M월 d일) */
    public static final String DATE_DISP_SIMPLE_PATTERN = "yyyy년 M월 d일";
    
    public static final String DATE_DISP_SIMPLE_PATTERN_TIME = "yyyy년 M월 d일 HH시mm분";
    
    public static final String DATE_DISP_PATTERN = "yyyy년 M월 d일 (E)";

    /** hour, minute, second pattern (HHmmss) */
    public static final String TIME_HMS_PATTERN = "HHmmss";

    /** hour, minute, second pattern (HH:mm:ss) */
    public static final String TIME_HMS_PATTERN_COLONE = "HH:mm:ss";
    
	/**
	 * 현재 날짜, 시간을 조회하여 문자열 형태로 반환한다.<br>
	 *
	 * @return (yyyy-MM-dd HH:mm:ss) 포맷으로 구성된 현재 날짜와 시간
	 */
	public static String getCurrentDateTime() {
		return getCurrentDateTime(DATE_TIME_PATTERN);
	}
	
	/**
	 * 현재 날짜, 시간을 조회을 조회하고, pattern 형태의 포맷으로 문자열을 반환한다.<br><br>
	 *
	 * DateUtils.getCurrentDateTime("yyyy년 MM월 dd일 hh시 mm분 ss초") = "2012년 04월 12일 20시 41분 50초"<br>
	 * DateUtils.getCurrentDateTime("yyyy-MM-dd hh-mm-ss") = "2012-04-12 20:41:50"
	 * 
	 * @param pattern 날짜 및 시간에 대한 포맷
	 * @return patter 포맷 형태로 구성된 현재 날짜와 시간
	 */
	public static String getCurrentDateTime(String pattern) {
		DateTime dt = new DateTime();
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		
		return fmt.print(dt);
	}		
	
	/**
	 * 현재 년월을 조회하여 "yyyy-MM" 포맷의 문자열을 반환한다.
	 *
	 * @return (yyyy-MM) 포맷으로 구성된 현재 년월
	 */
	public static String getThisMonth() {
		return getCurrentDateTime("yyyy-MM");
	}		
	
	/**
	 * 현재 년월을 조회하고, pattern 형태의 포맷으로 문자열을 반환한다.
	 *
	 * @param pattern 날짜 및 시간에 대한 포맷
	 * @return patter 포맷 형태로 구성된 현재 년월
	 */
	public static String getThisMonth(String pattern) {
		return getCurrentDateTime(pattern);
	}
	
	/**
	 * 현재 년을 조회하여 "yyyy" 포맷의 문자열을 반환한다.
	 *
	 * @return (yyyy-MM) 포맷으로 구성된 현재 년도
	 */
	public static String getThisYear() {
		return getCurrentDateTime("yyyy");
	}
	
	/**
	 * 입력받은 일자의 요일을 반환한다.<br><br>
	 *
	 * DateUtils.getDayOfWeek("2010-11-26") = "금"
	 *
	 * @param str (yyyy-MM-dd) 포맷의 문자열
	 * @return 입력받은 일자에 해당하는 요일
	 */
	public static String getDayOfWeek(String str) {
		return getDayOfWeek(str, true, LocaleContextHolder.getLocale());
	}
	
	public static String getDayOfWeek(String str, Boolean abbreviation, Locale locale) {
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN_DASH);
		DateTime dt = fmt.parseDateTime(str);
		DateTime.Property dayOfWeek = dt.dayOfWeek();

		return abbreviation ? dayOfWeek.getAsShortText(locale) : dayOfWeek.getAsText(locale);
	}
	
	public static int getDays(Calendar cal1, Calendar cal2) {
		
		long min = getMinutes(cal1, cal2);

		return (int) (min / (HOURS_24 * MINUTES_60));
	}
	
	public static int getDays(String startDate, String endDate) {
		return getDays(startDate, endDate, DATE_PATTERN_DASH);
	}
	
	public static int getDays(String startDate, String endDate, String pattern) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);

		DateTime startDateTime = fmt.parseDateTime(startDate);
		DateTime endDateTime = fmt.parseDateTime(endDate);

		long startMillis = startDateTime.getMillis();
		long endMillis = endDateTime.getMillis();

		int result = (int) (startMillis / (60 * 60 * 1000 * 24));
		int result1 = (int) (endMillis / (60 * 60 * 1000 * 24));

		return result1 - result;
	}
	
	public static boolean equals(Date date1, String date2) {
		return equals(date1, date2, DATE_PATTERN_DASH);
	}
	
	public static boolean equals(Date date1, String date2, String date2pattern) {
		Date date = convertStringToDate(date2, date2pattern);
		
		return equals(date1, date);
	}
	
	public static boolean equals(Date date1, Date date2) {
		if (date1.getTime() == date2.getTime()) {
			return true;
		}
		
		return false;
	}	
	
	public static boolean greaterThan(Date date1, String date2) {
		return greaterThan(date1, date2, DATE_PATTERN_DASH);
	}
	
	public static boolean greaterThan(Date date1, String date2, String date2pattern) {
		Date date = convertStringToDate(date2, date2pattern);
		
		return greaterThan(date1, date);
	}
	
	public static boolean greaterThan(Date date1, Date date2) {
		if (date1.getTime() > date2.getTime()) {
			return true;
		}
		
		return false;
	}
	
	public static boolean greaterThan(Timestamp timestamp1, String timestamp2) {
		return greaterThan(timestamp1, timestamp2, TIMESTAMP_PATTERN);
	}
	
	public static boolean greaterThan(Timestamp timestamp1, String timestamp2, String timestamp2pattern) {
		Timestamp date = convertStringToTimestamp(timestamp2, timestamp2pattern);
		
		return greaterThan(timestamp1, date);
	}
	
	public static boolean greaterThan(Timestamp timestamp1, Timestamp timestamp2) {
		if (timestamp1.getTime() > timestamp2.getTime()) {
			return true;
		}
		
		return false;
	}
	
	public static String addDays(String date, int days) {
		if (days == 0) {
			return date;
		}
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN_DASH);
		DateTime dt = fmt.parseDateTime(date);
		DateTime subtracted = dt.withFieldAdded(DurationFieldType.days(), days);
		
		return fmt.print(subtracted);
	}
	
	public static String addDaysYYYYMMDD(String date, int days) {
		if (days == 0) {
			return date;
		}
		
		date = date.replaceAll("[^\\d]*", "");
		
		if(date.length() > 8) {
			date = date.substring(0, 8);
		}
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN);
		DateTime dt = fmt.parseDateTime(date);
		DateTime subtracted = dt.withFieldAdded(DurationFieldType.days(), days);
		
		return fmt.print(subtracted);
	}
	
	public static String addMonths(String date, int months) {
		if (months == 0) {
			return date;
		}
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN_DASH);
		DateTime dt = fmt.parseDateTime(date);
		DateTime subtracted = dt.withFieldAdded(DurationFieldType.months(), months);
		
		return fmt.print(subtracted);
	}
	
	public static String addYears(String date, int years) {
		if (years == 0) {
			return date;
		}
		
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN_DASH);
		DateTime dt = fmt.parseDateTime(date);
		DateTime subtracted = dt.withFieldAdded(DurationFieldType.years(), years);
		
		return fmt.print(subtracted);
	}
	
	public static String addYearMonthDay(String date, int years, int months, int days) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN_DASH);
		DateTime dt = fmt.parseDateTime(date);

		if (years != 0) {
			dt = dt.withFieldAdded(DurationFieldType.years(), years);
		}	
		if (months != 0) {
			dt = dt.withFieldAdded(DurationFieldType.months(), months);
		}	
		if (days != 0) {
			dt = dt.withFieldAdded(DurationFieldType.days(), days);
		}	

		return fmt.print(dt);
	}
	public static String getFirstDateOfMonth(String date) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN_DASH);
		DateTime dt = fmt.parseDateTime(date);
		DateTime dtRet = new DateTime(dt.getYear(), dt.getMonthOfYear(), 1, 0, 0, 0, 0);
		
		return fmt.print(dtRet);
	}
	
	public static String getLastDateOfMonth(String date) {
		String firstDateOfMonth = getFirstDateOfMonth(date);
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN_DASH);
		DateTime dt = fmt.parseDateTime(firstDateOfMonth);
		dt = dt.plusMonths(1).minusDays(1);
		
		return fmt.print(dt);
	}
	
	public static String getFirstDateOfPrevMonth(String date) {
		String firstDateOfMonth = getFirstDateOfMonth(date);
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN_DASH);
		DateTime dt = fmt.parseDateTime(firstDateOfMonth);
		dt = dt.minusMonths(1);
		
		return fmt.print(dt);
	}
	
	public static String getLastDateOfPrevMonth(String date) {
		String firstDateOfMonth = getFirstDateOfMonth(date);
		DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN_DASH);
		DateTime dt = fmt.parseDateTime(firstDateOfMonth);
		dt = dt.minusDays(1);
		
		return fmt.print(dt);
	}
	
	public static boolean isDate(String date) {
		return isDate(date, DATE_PATTERN_DASH);
	}
	
	public static boolean isDate(String date, String pattern) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		DateTime dt = new DateTime();
		
		try {
			dt = fmt.parseDateTime(date);
		} catch (Exception e) {				
			return false;
		}

		if (!fmt.print(dt).equals(date)) {
			return false;
		}
		
		return true;
	}

	/**
	 * 입력된 시간이 유효한 시간인지를 체크한다.<br><br>
	 * 
	 * DateUtils.isTime("11:56") = true<br>
	 * DateUtils.isTime("31:56") = false
	 *
	 * @param time (HH:mm) 포맷형태의 시간
	 * @return 유효한 시간이면 true를 그렇지않으면 false를 반환
	 */
	public static boolean isTime(String time) {
		return isTime(time, TIME_PATTERN);
	}

	/**
	 * 입력된 시간이 유효한 시간인지를 체크한다.
	 *
	 * @param time 입력시간
	 * @param pattern 포맷형태 
	 * @return 유효한 시간이면 true를 그렇지 않으면 false를 반환
	 */
	public static boolean isTime(String time, String pattern) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		DateTime dt = new DateTime();
		
		try {
			dt = fmt.parseDateTime(time);
		} catch (Exception e) {
			return false;
		}

		if (!fmt.print(dt).equals(time)) {
			return false;
		}
		
		return true;
	}

	/**
	 * 문자열을 java.util.Date 타입으로 변경한다.<br><br>
	 * 
	 * DateUtils.convertStringToDate("2010-12-14")
	 *
	 * @param str (yyyy-MM-dd) 포맷의 Date형으로 변환할 문자열
	 * @return (yyyy-MM-dd) 포맷의 Date형
	 */
	public static Date convertStringToDate(String str) {
		return convertStringToDate(str, DATE_PATTERN_DASH);
	}

	/**
	 * 문자열을 java.util.Date 타입으로 변경한다.<br><br>
	 *
	 * DateUtils.convertStringToDate("2010-12-14 16:26:33", "yyyy-MM-dd HH:mm:ss")
	 * 
	 * @param str pattern 포맷의 Date형으로 변환할 문자열
	 * @param pattern 변경할 포맷
	 * @return pattern 포맷의 Date형
	 */
	public static Date convertStringToDate(String str, String pattern) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		return fmt.parseDateTime(str).toDate();
	}

	/**
	 * java.util.Date 타입을 문자열로 변경한다.<br><br>
	 * 
	 * DateUtils.convertDateToString(new Date(1292311593557l)) = "2010-12-14"
	 *
	 * @param date Date형의 입력된 날짜
	 * @return (yyyy-MM-dd) 포맷의 문자열
	 */
	public static String convertDateToString(Date date) {
		return convertDateToString(date, DATE_PATTERN_DASH);
	}

	/**
	 * java.util.Date 타입을 패턴에 맞는 문자열로 변경한다.<br><br>
	 * 
	 * DateUtils.convertDateToString(new Date(1292311593557l, "yyyy/MM/dd") = "2010/12/14"
	 *
	 * @param date Date형의 입력된 날짜
	 * @param pattern 날짜 패턴
	 * @return pattern 포맷의 문자열
	 */
	public static String convertDateToString(Date date, String pattern) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		
		return fmt.print(date.getTime());
	}

	/**
	 * 문자열을 java.sql.Date 타입으로 변환한다.<br><br>
	 * 
	 * DateUtils.convertStringToSQLDate("2010-12-14")
	 *
	 * @param str (yyyy-MM-dd) 포맷의 Sql Date형으로 변환할 문자열
	 * @return (yyyy-MM-dd) 포맷의 Sql Date형
	 */
	public static java.sql.Date convertStringToSQLDate(String str) {
		return convertStringToSQLDate(str, DATE_PATTERN_DASH);
	}

	/**
	 * 문자열을 java.sql.Date 타입으로 변환한다.<br><br>
	 * 
	 * DateUtils.convertStringToSQLDate("2010/12/14", "yyyy/MM/dd")
	 *
	 * @param str pattern 포맷의 Sql Date형으로 변환할 문자열
	 * @param pattern 날짜 패턴
	 * @return pattern 포맷의 Sql Date형
	 */
	public static java.sql.Date convertStringToSQLDate(String str, String pattern) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		
		return new java.sql.Date(fmt.parseDateTime(str).getMillis());
	}

	/**
	 * 문자열을 java.sql.Timestamp 타입으로 변환한다.<br><br>
	 * 
	 * DateUtils.convertStringToTimestamp("2010-12-14")
	 *
	 * @param str (yyyy-MM-dd) 포맷의 Timestamp형으로 변환할 문자열
	 * @return (yyyy-MM-dd) 포맷의 Timestamp형
	 */
	public static Timestamp convertStringToTimestamp(String str) {
		return convertStringToTimestamp(str, DATE_PATTERN_DASH);
	}
	
	public static Timestamp convertStringToTimestamp(String str, String pattern) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
		
		return new Timestamp(fmt.parseDateTime(str).getMillis());
	}

	/**
	 * java.sql.Timestamp 타입의 일자를 문자열로 변환한다.<br><br>
	 * 
	 * DateUtils.convertTimestampToString(new Timestamp(1292311593000l))<br>
	 * DateUtils.convertTimestampToString(new Timestamp(new Date().getTime()))
	 *
	 * @param date (yyyy-MM-dd) 포맷의 문자열로 변환할 timestamp
	 * @return (yyyy-MM-dd) 포맷의 문자열
	 */
	public static String convertTimestampToString(Timestamp date) {
		return convertTimestampToString(date, DATE_PATTERN_DASH);
	}

	/**
	 * java.sql.Timestamp 타입의 일자를 패턴에 맞는 문자열로 변환한다.<br><br>
	 * 
	 * DateUtils.convertTimestampToString(new Timestamp(1292311593000l), "yyyy/MM/dd hh:mm") = "2010/12/14 16:26"
	 *
	 * @param date pattern 포맷의 문자열로 변환할 timestamp
	 * @param pattern 날짜 패턴
	 * @return pattern 포맷의 문자열
	 */
	public static String convertTimestampToString(Timestamp date, String pattern) {
		if (date == null) {
			return "";
		}
		
		return convertDateToString(date, pattern);
	}

	/**
	 * 문자열을 java.util.Calendar 타입으로 변환한다.<br><br>
	 * 
	 * DateUtils.convertStringToCalender("20101214123412")
	 *
	 * @param str Calendar형으로 변환할 (yyyyMMddHHmmss) 포맷의 문자열
	 * @return Calendar 
	 */
	public static Calendar convertStringToCalender(String str) {
		if ((str == null) || (str.length() < 14)) {
			return null;
		}

		String year = str.substring(0, 4);
		String month = str.substring(4, 6);
		String day = str.substring(6, 8);
		String hour = str.substring(8, 10);
		String minute = str.substring(10, 12);
		String second = str.substring(12, 14);

		return (new GregorianCalendar(StringUtil.string2integer(year), StringUtil.string2integer(month) - 1, StringUtil
				.string2integer(day), StringUtil.string2integer(hour), StringUtil.string2integer(minute), StringUtil
				.string2integer(second)));
	}

	/**
	 * java.util.Calendar타입의 일자를 문자열로 변환한다.<br><br>
	 * 
	 * DateUtils.convertCalendarToString(new GregorianCalendar(2010, 11, 14, 12, 34, 12)) = "20101214123412000"
	 *
	 * @param calendar 문자열로 변환할 calendar
	 * @return (yyyyMMddHHmmss) 포맷 형태의 문자열
	 */
	public static String convertCalendarToString(Calendar calendar) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		return dateFormat.format(calendar.getTime()) + "000";
	}
	
	public static String convertStringToString(String str, String basePattern, String wantedPattern) {
		DateTimeFormatter basefmt = DateTimeFormat.forPattern(basePattern);
		DateTimeFormatter wantedfmt = DateTimeFormat.forPattern(wantedPattern);
		DateTime dt = basefmt.parseDateTime(str);
		
		return wantedfmt.print(dt);
	}
	

	/**
	 * 입력된 두 일자 사이의 분을 계산하여 반환한다.<br><br>
	 * 
	 * DateUtils.getMinutes(new GregorianCalendar(2010, 11, 14, 12, 34, 12), new GregorianCalendar(2010, 11, 14, 13, 32, 12)) = 58
	 *
	 * @param cal1 입력된 calendar1
	 * @param cal2 입력된 calendar2
	 * @return 입력된 두 일자 사이의 분
	 */
	public static int getMinutes(Calendar cal1, Calendar cal2) {
		long utc1 = cal1.getTimeInMillis();
		long utc2 = cal2.getTimeInMillis();

		long result = (utc2 - utc1) / (SECONDS_60 * MILLI_SECONDS_1000);

		return (int) result;
	}

	/**
	 * 입력된 두 일자 사이의 분을 계산하여 반환한다.<br><br>
	 * 
	 * DateUtils.getMinutes("20121212012321","20121212014321") = 20
	 *
	 * @param date1 (yyyyMMddHHmmss) 포맷의 문자열
	 * @param date2 (yyyyMMddHHmmss) 포맷의 문자열
	 * @return 입력된 두 일자 사이의 분
	 */
	public static int getMinutes(String date1, String date2) {
		Calendar cal1 = convertStringToCalender(date1);
		Calendar cal2 = convertStringToCalender(date2);

		if(cal1 == null || cal2 == null) {
			return -1;
		}

		return getMinutes(cal1, cal2);
	}

	/**
	 * 어제 일자를 반환한다.
	 *
	 * @return (yyyy-MM-dd) 포맷의 어제 일자
	 */
	public static String getYesterday() {
		return getYesterday(DATE_PATTERN_DASH);
	}

	/**
	 * 어제 일자를 반환한다.
	 *
	 * @param pattern 날짜 포맷
	 * @return pattern 포맷의 어제 일자
	 */
	public static String getYesterday(String pattern) {
		Calendar cal = getCalendar();
		cal.add(Calendar.DATE, -1);
		Date date = cal.getTime();
		
		return convertDateToString(date, pattern);
	}

	/**
	 * 한국 시간대에 맞는java.util.Calendar 타입의 일자를 반환한다.
	 *
	 * @return Calendar 타입
	 */
	public static Calendar getCalendar() {
		Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+09:00"), Locale.KOREA);
		calendar.setTime(new Date());

		return calendar;
	}

	/**
	 * 두 일자 사이의 일자 목록을 반환한다.<br><br>
	 * 
	 * DateUtils.getDates("2010-12-17", "2010-12-20") = 2010-12-17, 2010-12-18, 2010-12-19, 2010-12-20
	 *
	 * @param startDay (yyyy-MM-dd) 포맷의 시작일
	 * @param endDay (yyyy-MM-dd) 포맷의 종료일
	 * @return 시작일과 종료일 사이의 일자 목록(String 배열)
	 */
	public static String[] getDates(String startDay, String endDay) {
		return getDates(startDay, endDay, DATE_PATTERN_DASH);
	}

	/**
	 * 두 일자 사이의 일자 목록을 반환한다.<br><br>
	 * 
	 * DateUtils.getDates("2010/12/17", "2010/12/20", "yyyy/MM/dd") = 2010/12/17, 2010/12/18, 2010/12/19, 2010/12/20
	 *
	 * @param startDay 시작일
	 * @param endDay 종료일
	 * @param pattern 일자 포맷
	 * @return pattern 포맷 형식의 시작일과 종요일 사이의 일자 목록(String 배열)
	 */
	public static String[] getDates(String startDay, String endDay, String pattern) {
		List<String> result = new ArrayList<String>();
		result.add(startDay);

		Calendar cal = getCalendar();
		cal.setTime(convertStringToDate(startDay, pattern));
		String nextDay = convertDateToString(cal.getTime(), pattern);

		while (!nextDay.equals(endDay)) {
			cal.add(Calendar.DATE, 1);
			nextDay = convertDateToString(cal.getTime(), pattern);
			result.add(nextDay);
		}
		
		return result.toArray(new String[0]);
	}		
	
    /**
     * 현재 일자를 yyyy-MM-dd 패턴의 문자열로 반환한다.<br><br>
     * 
     * DateUtils.getCurrentDateAsString = 2009-04-28
     * 
     * @return (yyyy-MM-dd) 포맷 형태의 현재 일자
     */
    public static String getCurrentDateAsString() {
        return getCurrentDateAsString(DATE_PATTERN_DASH);
    }
    
    /**
     * 현재 일자를 지정된 패턴의 문자열로 반환한다.<br><br>
     * 
     * DateUtils.getCurrentDateAsString("yyyyMMdd") = 20090428
     * 
     * @param pattern 일자의 포맷 형태
     * @return patter 포맷 형태의 현재 일자
     */
    public static String getCurrentDateAsString(String pattern) {
    	SimpleDateFormat df = new SimpleDateFormat(pattern);
    	
    	return df.format(new Date());
    }
    
    /**
     * 현재 일자를 java.sql.Date 타입으로 반환한다.
     * 
     * @return java.sql.Date 타입의 현재 일자
     */
    public static java.sql.Date getCurrentDate() {
        return new java.sql.Date((new java.util.Date()).getTime());
    }
    
    /**
     * 현재 시각에 대한 Time 객체를 반환한다.
     * 
     * @return java.sql.Time 타입의 현재 시각
     */
    public static Time getCurrentTime() {
        return new Time(new Date().getTime());              
    }
    
    /**
     * 현재 시각에 대한 Time 문자열을 반환한다.
     * 
     * @return (HH:mm:ss) 포맷의 현재 시각
     */
    public static String getCurrentTimeAsString() {
    	return new Time(new Date().getTime()).toString();              
    }
    
    /**
     * 현재 시각에 대한 Timestamp 객체를 반환한다.
     * 
     * @return java.sql.Timestamp 타입의 현재 시각
     */
    public static Timestamp getCurrentTimestamp() {
    	Timestamp timestamp = new Timestamp(new Date().getTime()); 
    	
    	return timestamp;
    }
    
    /**
     * 현재 시각에 대한 Timestamp 문자열을 구한다.
     * 
     * @return (yyyy-MM-dd HH:mm:ss.SSS) 포맷의 현재 시각 
     */
    public static String getCurrentTimestampAsString() {
    	return getCurrentTimestamp().toString();
    }
    
    /**
     * 입력된 일자를 기준으로 해당년도가 윤년인지 여부를 반환한다.
     * 
     * @param  inputDate (yyyy-MM-dd) 형식의 일자
     * @return 윤년이면 true를 그렇지 않으면 false를 반환
     */
	public static boolean isLeapYear(String inputDate) {
		return isLeapYear(Integer.parseInt(inputDate.substring(0, 4)));
	}
    
    /**
     * 정수형태로 입력된 년도를 기준으로 해당년도가 윤년인지 여부를 반환한다.
     * 
     * @param year 년도
     * @return year이 윤년이면 true를 그렇지 않으면 false를 반환
     */
    public static boolean isLeapYear(int year) {
    	return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) ? true : false;
    }
    
    /**
     * yyyy-mm-dd hh:mm:ss.SSS or yyyy-mm-dd 로 받은 타입을 yyyy년 yy월 mm일 타입으로 변환한다.
     * 
     * @param date
     * @return
     */
    public static String dateTypeConvert(String date){
    	if(!date.isEmpty()){
    		date = date.substring(0, 10);
        	String[] temp = date.split("-");
        	date = date.format("%s년 %s월 %s일", 	temp[0], temp[1], temp[2]);
    	}
    	
    	return date;
    }
    

    /**
     * 파라미터의 해당하는 년월의 전달을 구한다.
     * @param yearMonth yyyyMM
     * @param minVal
     * @return
     */
    public static String getBeforeYearMonthByYM(String yearMonth, int minVal){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        Calendar cal = Calendar.getInstance();
        int year = Integer.parseInt(yearMonth.substring(0,4));
        int month = Integer.parseInt(yearMonth.substring(4,6));
    
        cal.set(year, month-minVal, 0);
    
        String beforeYear = dateFormat.format(cal.getTime()).substring(0,4); 
        String beforeMonth = dateFormat.format(cal.getTime()).substring(4,6); 
        String retStr = beforeYear + beforeMonth;
        
        return retStr;
    }

    /**
     * 해당년월의 마지막 날짜를 구한다.
     * @param yearMonth yyyyMM
     * @return
     */
    public static String getLastDayOfMonth(String yearMonth){  
        String year = yearMonth.substring(0,4);
        String month = yearMonth.substring(4,6);
     
        int _year = Integer.parseInt(year);
        int _month = Integer.parseInt(month);
     
        Calendar calendar = Calendar.getInstance();
        calendar.set(_year, (_month-1), 1); //월은 0부터 시작  
        String lastDay = String.valueOf(calendar.getActualMaximum(Calendar.DATE));
 
        return lastDay;
    }
    
    /**
     * 문자열로 입력된 날짜의 포맷을 입력받은 포맷 문자열로 변경한다 
     * yyyyMMdd -> 사용자 지정 포멧
     * @param date
     * @param convDateFormat
     * @return
     */
    public static String dateFormatConvert(String date, String orgDateFormat, String convDateFormat) {
    	try{
    		if(StringUtil.isEmpty(date)) {
    			return date;
    		}
    		
    		SimpleDateFormat sdfOrg = new SimpleDateFormat(orgDateFormat);
    		SimpleDateFormat sdfConv = new SimpleDateFormat(convDateFormat);
    		
    		Date orgDate = sdfOrg.parse(date);
    		
    		return sdfConv.format(orgDate);
    	} catch (Exception e) {
    		return date;
    	}
    }
    
    /**
     * Date 표시 형식 변환 
     * yyyyMMdd -> 사용자 지정 포멧
     * @param date
     * @param convDateFormat
     * @return
     */
    public static String dateFormatConvert(String date, String convDateFormat) {
    	return dateFormatConvert(date, DATE_PATTERN, convDateFormat);
    }
    
    /**
     * Date 표시 형식 변환 
     * yyyyMMdd -> yyyy년 M월 d일
     * @param date
     * @param convDateFormat
     * @return
     */
    public static String dateFormatConvert(String date) {
    	return dateFormatConvert(date, DATE_PATTERN, DATE_DISP_SIMPLE_PATTERN);
    }
    
    public static boolean isNewestPeriod(String date, int period){
    	return isNewestPeriod(DateUtil.convertStringToSQLDate(date), period);
    }
    
    public static boolean isNewestPeriod(String date, String format, int period){
    	return isNewestPeriod(DateUtil.convertStringToSQLDate(date, format), period);
    }
    
    public static boolean isNewestPeriod(Date date, int period){
    	// 현재 날짜
    	String toDay = DateUtil.getCurrentDateAsString();
		int dCount = DateUtil.getDays(DateUtil.convertDateToString(date, CoConstDef.DISP_FORMAT_DATE_YYYYMMDD), toDay);
		
    	return dCount >= 0 && dCount <= period-1;
    }
    
    public static boolean isLastPeriod(String date, int period){
    	return isLastPeriod(DateUtil.convertStringToSQLDate(date), period);
    }
    
    public static boolean isLastPeriod(String date, String format, int period){
    	return isLastPeriod(DateUtil.convertStringToSQLDate(date, format), period);
    }
    
    public static boolean isLastPeriod(Date date, int period){
    	// 현재 날짜
    	String toDay = DateUtil.getCurrentDateAsString();
		int dCount = DateUtil.getDays(toDay, DateUtil.convertDateToString(date, CoConstDef.DISP_FORMAT_DATE_YYYYMMDD));
    	
		return dCount >= 0 && dCount <= period-1;
    }
    
    public static int getPeriodStatus(String date1, String date2){
    	return getPeriodStatus(convertStringToSQLDate(date1), convertStringToSQLDate(date2));
    }
    
    public static int getPeriodStatus(String date1, String date2, String format){
    	return getPeriodStatus(convertStringToSQLDate(date1, format), convertStringToSQLDate(date2, format));
    }
    
    public static int getPeriodStatus(Date date1, Date date2){
    	int ret = 0; // 진행중
    	// 현재 날짜
    	Date toDay = convertStringToSQLDate(getCurrentDateAsString());
		
    	if(!((toDay.compareTo(date1) >= 0) && (toDay.compareTo(date2) <= 0))){
			ret = (toDay.compareTo(date1) < 0) ? -1 : 1;
		}
		
		return ret;
    }
    
    public static int getDiffMonth(String date1, String date2) {
    	Date fromDate = convertStringToSQLDate(date1, DATE_PATTERN);
    	Date toDate = convertStringToSQLDate(date2, DATE_PATTERN);
    	
    	int m1 = fromDate.getYear() * 12 + fromDate.getMonth();
        int m2 = toDate.getYear() * 12 + toDate.getMonth();
        
        return m2 - m1 + 1;
    }
}
