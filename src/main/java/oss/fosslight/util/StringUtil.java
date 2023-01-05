/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.CharUtils;
import org.springframework.web.util.HtmlUtils;

/**
 * DESC : 문자열 관련 검색, 변환, 치환, 유효성 체크 등의 기능을 제공한다.<br><br>
 *
 * Apache LICENSE-2.0의 Anyframe Utils(Version 1.0.1)을 기반으로 작성된 클래스임을 명시한다.<br>
 * 또한, Spring의 StringUtils, commons-lang의 StringUtils의 래퍼 기능을 제공한다.
 *
 */
public final class StringUtil {
	private StringUtil() {}
	
	public static final String DEFAULT_EMPTY_STRING = "";

	/** UTF-8용 1바이트 캐릭터 셋 */
	private static final int ONE_BYTE = 0x00007F;

	/** UTF-8용 2바이트 캐릭터 셋 */
	private static final int TWO_BYTE = 0x0007FF;

	/** UTF-8용 3바이트 캐릭터 셋 */
	private static final int THREE_BYTE = 0x00FFFF;
	
	/**
     * The empty String <code>""</code>.
     * @since 2.0
     */
    public static final String EMPTY = "";
	
    /**
     * Represents a failed index search.
     * @since 2.1
     */
    public static final int INDEX_NOT_FOUND = -1;

    /** 코드를 받아 문자열로 변환에 사용 INT*/
	private static final int HEX_TO_STRING_INT = 16;
	
	/**
	 * 문자열을 "…"가 포함된 지정한 사이즈로 문자열을 축소한다 최대크기는 4보다 커야한다.<br />
	 *
	 * <pre>
	 * StringUtils.abbreviate(null, *)      = null
	 * StringUtils.abbreviate("", 4)        = ""
	 * StringUtils.abbreviate("abcdefg", 6) = "abc..."
	 * StringUtils.abbreviate("abcdefg", 7) = "abcdefg"
	 * StringUtils.abbreviate("abcdefg", 8) = "abcdefg"
	 * StringUtils.abbreviate("abcdefg", 4) = "a..."
	 * StringUtils.abbreviate("abcdefg", 3) = IllegalArgumentException
	 * </pre>
	 *
	 * @param str 문자열
	 * @param maxWidth 최대크기
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String abbreviate(String str, int maxWidth) {
		return org.apache.commons.lang.StringUtils.abbreviate(str, maxWidth);
	}
	
	/**
	 * 문자열을 "…"가 포함된 지정한 사이즈로 문자열을 앞/뒤로 축소한다.<br />
	 *
	 * <pre>
	 * StringUtils.abbreviate(null, *, *)                = null
	 * StringUtils.abbreviate("", 0, 4)                  = ""
	 * StringUtils.abbreviate("abcdefghijklmno", -1, 10) = "abcdefg..."
	 * StringUtils.abbreviate("abcdefghijklmno", 0, 10)  = "abcdefg..."
	 * StringUtils.abbreviate("abcdefghijklmno", 1, 10)  = "abcdefg..."
	 * StringUtils.abbreviate("abcdefghijklmno", 4, 10)  = "abcdefg..."
	 * StringUtils.abbreviate("abcdefghijklmno", 5, 10)  = "...fghi..."
	 * StringUtils.abbreviate("abcdefghijklmno", 6, 10)  = "...ghij..."
	 * StringUtils.abbreviate("abcdefghijklmno", 8, 10)  = "...ijklmno"
	 * StringUtils.abbreviate("abcdefghijklmno", 10, 10) = "...ijklmno"
	 * StringUtils.abbreviate("abcdefghijklmno", 12, 10) = "...ijklmno"
	 * StringUtils.abbreviate("abcdefghij", 0, 3)        = IllegalArgumentException
	 * StringUtils.abbreviate("abcdefghij", 5, 6)        = IllegalArgumentException
	 * </pre>
	 *
	 * @param str 문자열
	 * @param offset  left edge of source String
	 * @param maxWidth 최대크기
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String abbreviate(String str, int offset, int maxWidth) {
		return org.apache.commons.lang.StringUtils.abbreviate(str, offset, maxWidth);
	}

	/**
	 * 문자열중 첫번째 문자를 대문자로 만든다.<br />
	 *
	 * <pre>
	 * StringUtils.capitalize(null)  = null
	 * StringUtils.capitalize("")    = ""
	 * StringUtils.capitalize("cat") = "Cat"
	 * StringUtils.capitalize("cAt") = "CAt"
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String capitalize(String str) {
		return org.apache.commons.lang.StringUtils.capitalize(str);
	}

	/**
	 * 문자열의 길이가 변환길이가 되도록 문자열 좌우에 공백문자를 우측부터 하나씩 추가한다.<br />
	 *
	 * <pre>
	 * StringUtils.center(null, *)   = null
	 * StringUtils.center("", 4)     = "    "
	 * StringUtils.center("ab", -1)  = "ab"
	 * StringUtils.center("ab", 4)   = " ab "
	 * StringUtils.center("abcd", 2) = "abcd"
	 * StringUtils.center("a", 4)    = " a  "
	 * </pre>
	 *
	 * @param str 문자열
	 * @param size 변환길이
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String center(String str, int size) {
		return org.apache.commons.lang.StringUtils.center(str, size);
	}

	/**
	 * 문자열의 길이가 변환길이가 되도록 문자열 좌우에 삽입문자를 하나씩 우측부터 추가한다.<br />
	 * 
	 * <pre>
	 * StringUtils.center(null, *, *)     = null
	 * StringUtils.center("", 4, ' ')     = "    "
	 * StringUtils.center("ab", -1, ' ')  = "ab"
	 * StringUtils.center("ab", 4, ' ')   = " ab"
	 * StringUtils.center("abcd", 2, ' ') = "abcd"
	 * StringUtils.center("a", 4, ' ')    = " a  "
	 * StringUtils.center("a", 4, 'y')    = "yayy"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param size 변환길이
	 * @param padChar 삽입문자
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String center(String str, int size, char padChar) {
		return org.apache.commons.lang.StringUtils.center(str, size, padChar);
	}

	/**
	 * 문자열의 길이가 변환길이가 되도록 문자열 좌우에 삽입문자열을 하나씩 우측부터 추가한다.<br />
	 *
	 * <pre>
	 * StringUtils.center(null, *, *)     = null
	 * StringUtils.center("", 4, " ")     = "    "
	 * StringUtils.center("ab", -1, " ")  = "ab"
	 * StringUtils.center("ab", 4, " ")   = " ab"
	 * StringUtils.center("abcd", 2, " ") = "abcd"
	 * StringUtils.center("a", 4, " ")    = " a  "
	 * StringUtils.center("a", 4, "yz")   = "yayz"
	 * StringUtils.center("abc", 7, null) = "  abc  "
	 * StringUtils.center("abc", 7, "")   = "  abc  "
	 * </pre>
	 *
	 * @param str 문자열
	 * @param size 변환길이
	 * @param padStr 삽입문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String center(String str, int size, String padStr) {
		return org.apache.commons.lang.StringUtils.center(str, size, padStr);
	}

	/**
	 * 문자열 맨 끝에있는 &quot;<code>\n</code>&quot;,<br />
	 * &quot;<code>\r</code>&quot;, or &quot;<code>\r\n</code>&quot;을 제거한다.<br />
	 * 
	 * <pre>
	 * StringUtils.chomp(null)          = null
	 * StringUtils.chomp("")            = ""
	 * StringUtils.chomp("abc \r")      = "abc "
	 * StringUtils.chomp("abc\n")       = "abc"
	 * StringUtils.chomp("abc\r\n")     = "abc"
	 * StringUtils.chomp("abc\r\n\r\n") = "abc\r\n"
	 * StringUtils.chomp("abc\n\r")     = "abc\n"
	 * StringUtils.chomp("abc\n\rabc")  = "abc\n\rabc"
	 * StringUtils.chomp("\r")          = ""
	 * StringUtils.chomp("\n")          = ""
	 * StringUtils.chomp("\r\n")        = ""
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String chomp(String str) {
		return org.apache.commons.lang.StringUtils.chomp(str);
	}

	/**
	 * 문자열 맨 끝에 구분자가 있으면 이를 제거한다.<br />
	 * 
	 * <pre>
	 * StringUtils.chomp(null, *)         = null
	 * StringUtils.chomp("", *)           = ""
	 * StringUtils.chomp("foobar", "bar") = "foo"
	 * StringUtils.chomp("foobar", "baz") = "foobar"
	 * StringUtils.chomp("foo", "foo")    = ""
	 * StringUtils.chomp("foo ", "foo")   = "foo "
	 * StringUtils.chomp(" foo", "foo")   = " "
	 * StringUtils.chomp("foo", "foooo")  = "foo"
	 * StringUtils.chomp("foo", "")       = "foo"
	 * StringUtils.chomp("foo", null)     = "foo"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param separator 구분자
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String chomp(String str, String separator) {
		return org.apache.commons.lang.StringUtils.chomp(str, separator);
	}

	/**
	 * 문자열 맨 끝에있는 문자 하나를 제거한다.<br />
	 *
	 * <pre>
	 * StringUtils.chop(null)          = null
	 * StringUtils.chop("")            = ""
	 * StringUtils.chop("abc \r")      = "abc "
	 * StringUtils.chop("abc\n")       = "abc"
	 * StringUtils.chop("abc\r\n")     = "abc"
	 * StringUtils.chop("abc")         = "ab"
	 * StringUtils.chop("abc\nabc")    = "abc\nab"
	 * StringUtils.chop("a")           = ""
	 * StringUtils.chop("\r")          = ""
	 * StringUtils.chop("\n")          = ""
	 * StringUtils.chop("\r\n")        = ""
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String chop(String str) {
		return org.apache.commons.lang.StringUtils.chop(str);
	}

	/**
	 * 두단어를 사전 편찬 순서대로 비교한다.<br><br>
	 *
	 * StringUtils.compareTo("Anyframe Java Test", "Anyframe Java Test") = 0
	 *
	 * @param sourceStr 단어1
	 * @param anotherStr 단어2
	 * @return 두 단어가 같을 경우 0
	 *         단어1이 단어2보다 작을 경우 0 미만
	 *         단어1이 단어2보다 클 경우 0 초과
	 * @see String#compareTo(String)
	 */
	public static int compareTo(String sourceStr, String anotherStr) {
		if (sourceStr == null || anotherStr == null) {
			return -1;
		}
		
		return sourceStr.compareTo(anotherStr);
	}

	/**
	 * 두단어를 대소 문자 무시하고 사전 편찬 순서대로 비교한다.<br><br>
	 *
	 * StringUtils.compareToIgnoreCase("anyframe java test", "Anyframe Java Test") = 0
	 *
	 * @param sourceStr 단어1
	 * @param anotherStr 단어2
	 * @return 두 단어가 같을 경우 0
	 *         단어1이 단어2보다 작을 경우 0 미만
	 *         단어1이 단어2보다 클 경우 0 초과
	 * @see String#compareToIgnoreCase(String)
	 */
	public static int compareToIgnoreCase(String sourceStr, String anotherStr) {
		if (sourceStr == null || anotherStr == null) {
			return -1;
		}
		
		return sourceStr.compareToIgnoreCase(anotherStr);
	}

	/**
	 * 문자열 내에서 검색문자가 포함하고 있는지 확인한다.<br />
	 * 
	 * <code>null</code> 또는 공백일경우 <code>false</code>를 반환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.contains(null, *)    = false
	 * StringUtils.contains("", *)      = false
	 * StringUtils.contains("abc", 'a') = true
	 * StringUtils.contains("abc", 'z') = false
	 * </pre>
	 * 
	 * @param str 문자열
	 * @param searchChar 검색문자
	 * @return 검색문자가 포함되어있는 경우 true ,
	 *  포함되어있지 않거나 입력 문자열이 <code>null</code>인경우 false
	 */
	public static boolean contains(String str, char searchChar) {
		return org.apache.commons.lang.StringUtils.contains(str, searchChar);
	}

	/**
	 * 문자열 내에서 검색문자열이 포함하고 있는지 확인한다.<br />
	 * 
	 * <code>null</code>일경우 <code>false</code>를 반환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.contains(null, *)     = false
	 * StringUtils.contains(*, null)     = false
	 * StringUtils.contains("", "")      = true
	 * StringUtils.contains("abc", "")   = true
	 * StringUtils.contains("abc", "a")  = true
	 * StringUtils.contains("abc", "z")  = false
	 * </pre>
	 * 
	 * @param str 문자열
	 * @param searchStr 검색문자열
	 * @return 검색문자가 포함되어있는 경우 true ,
	 *  포함되어있지 않거나 입력 문자열이 <code>null</code>인경우 false
	 */
	public static boolean contains(String str, String searchStr) {
		return org.apache.commons.lang.StringUtils.contains(str, searchStr);
	}

	/**
	 * 문자열 내에서 검색문자열이 대소문자를 무시하고 포함하고 있는지 확인한다.<br />
	 * 
	 * <code>null</code>일경우 <code>false</code>를 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.containsIgnoreCase(null, *) = false
	 * StringUtils.containsIgnoreCase(*, null) = false
	 * StringUtils.containsIgnoreCase("", "") = true
	 * StringUtils.containsIgnoreCase("abc", "") = true
	 * StringUtils.containsIgnoreCase("abc", "a") = true
	 * StringUtils.containsIgnoreCase("abc", "z") = false
	 * StringUtils.containsIgnoreCase("abc", "A") = true
	 * StringUtils.containsIgnoreCase("abc", "Z") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchStr 검색문자열
	 * @return 검색문자가 포함되어있는 경우 true ,
	 *  포함되어있지 않거나 입력 문자열이 <code>null</code>인경우 false
	 */
	public static boolean containsIgnoreCase(String str, String searchStr) {
		return org.apache.commons.lang.StringUtils.containsIgnoreCase(str, searchStr);
	}

	/**
	 * 입력된 문자열이 주어진 character들을 포함하는지 체크<br><br>
	 *
	 * StringUtils.containsInvalidChars("abc/", new char[] { '*', '/' }) = true
	 *
	 * @param str 문자열
	 * @param invalidChars 체크할 캐릭터들
	 * @return 문자열에 캐릭터들이 포함되어 있을 경우 true
	 */
	public static boolean containsInvalidChars(String str, char[] invalidChars) {
		if (str == null || invalidChars == null) {
			return false;
		}
		
		int strSize = str.length();
		int validSize = invalidChars.length;
		
		for (int i = 0; i < strSize; i++) {
			char ch = str.charAt(i);
			
			for (int j = 0; j < validSize; j++) {
				if (invalidChars[j] == ch) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * 입력된 문자열이 주어진 character들을 포함하는지 체크<br><br>
	 *
	 * StringUtils.containsInvalidChars("abc*abc", "*") = true
	 *
	 * @param str 문자열
	 * @param invalidChars 체크할 캐릭터들
	 * @return 문자열에 캐릭터들이 포함되어 있을 경우 true
	 */
	public static boolean containsInvalidChars(String str, String invalidChars) {
		if (str == null || invalidChars == null) {
			return true;
		}
		
		return containsInvalidChars(str, invalidChars.toCharArray());
	}

	/**
	 * 입력된 문자열에 대해서 같은 character가 동일하게 반복하는지 체크<br><br>
	 *
	 * StringUtils.containsMaxSequence("abbbbc", "4") = true
	 *
	 * @param str 문자열
	 * @param maxSeqNumber 같은 캐릭터가 반복되는 횟수
	 * @return 같은 캐릭터가 주어진 횟수만큼 반복될 경우 true
	 */
	public static boolean containsMaxSequence(String str, String maxSeqNumber) {
		int occurence = 1;
		int max = Integer.valueOf(maxSeqNumber);
		
		if (str == null) {
			return false;
		}
	
		int sz = str.length();
		
		for (int i = 0; i < (sz - 1); i++) {
			if (str.charAt(i) == str.charAt(i + 1)) {
				occurence++;
	
				if (occurence == max) {
					return true;
				}
			} else {
				occurence = 1;
			}
		}
		
		return false;
	}

	/**
	 * 입력된 underscore 형태의 문자열을 camel case 형태로 변환<br><br>
	 *
	 * StringUtils.convertToCamelCase("anyframe_java_test") = "anyframeJavaTest"
	 *
	 * @param underscore underscore 형태의 문자열
	 * @return camel case 형태로 변환된 문자열
	 */
	public static String convertToCamelCase(String underscore) {
		return convertToCamelCase(underscore, "_");
	}

	/**
	 * 주어진 char의 형태에 맞게 잘라진 문자열을 camel case 형태로 변환<br><br>
	 *
	 * StringUtils.convertToCamelCase("anyframe-java-test", "-") = "anyframeJavaTest"
	 *
	 * @param targetString 대상 문자열
	 * @param posChar 문자열을 자를 기준 캐릭터
	 * @return camel case 형태로 변환된 문자열
	 */
	public static String convertToCamelCase(String targetString, String posChar) {
		StringBuilder result = new StringBuilder();
		boolean nextUpper = false;
		String allLower = targetString.toLowerCase();
	
		for (int i = 0; i < allLower.length(); i++) {
			char currentChar = allLower.charAt(i);
			
			if (currentChar == CharUtils.toChar(posChar)) {
				nextUpper = true;
			} else {
				if (nextUpper) {
					currentChar = Character.toUpperCase(currentChar);
					nextUpper = false;
				}
				
				result.append(currentChar);
			}
		}
		
		return result.toString();
	}

	/**
	 * camel case 형태의 문자열을 underscore 형태의 문자열로 변환<br><br>
	 *
	 * StringUtils.convertToUnderScore("anyframeJavaTest") = "anyframe_java_test"
	 *
	 * @param camelCase  camel case 형태의 문자열
	 * @return underscore 형태로 변환된 문자열
	 */
	public static String convertToUnderScore(String camelCase) {
		String result = "";
		
		for (int i = 0; i < camelCase.length(); i++) {
			char currentChar = camelCase.charAt(i);
			// This is starting at 1 so the result does not end up with an
			// underscore at the begin of the value
			
			if (i > 0 && Character.isUpperCase(currentChar)) {
				result = result.concat("_");
			}
			
			result = result.concat(Character.toString(currentChar).toLowerCase());
		}
		
		return result;
	}

	/**
	 * 문자열중에 검색문자열이 포함되어있는 갯수를 반환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.countMatches(null, *)       = 0
	 * StringUtils.countMatches("", *)         = 0
	 * StringUtils.countMatches("abba", null)  = 0
	 * StringUtils.countMatches("abba", "")    = 0
	 * StringUtils.countMatches("abba", "a")   = 2
	 * StringUtils.countMatches("abba", "ab")  = 1
	 * StringUtils.countMatches("abba", "xxx") = 0
	 * </pre>
	 *
	 * @param str 문자열
	 * @param sub 검색문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static int countMatches(String str, String sub) {
		return org.apache.commons.lang.StringUtils.countMatches(str, sub);
	}

	/**
	 * 한 String 객체(sub)의 패턴이 다른 String 객체(main)안에서 몇 번 등장하는지 계산한다. - 등장 패턴의 위치는<br>
	 * 좌측에서부터 계산하고 겹치지 않는 형태로 계산한다. + 예를 들어, "aa"는 "aaa"에서 두 번 등장하는 것이 아니라, <br>
	 * 한 번 등장하는 것으로 계산한다. <br><br>
	 * 
	 * StringUtils.countPattern("aaa", "aa") = 1
	 * 
	 * @param str
	 *            the String to check
	 * @param pattern
	 *            the pattern to count
	 * @return the number of occurrences
	 */
	public static int countPattern(String str, String pattern) {
		if (str == null || pattern == null || "".equals(pattern)) {
			return 0;
		}
		
		int count = 0;
		int pos = 0;
		
		while (str.indexOf(pattern, pos) != -1) {
			int index = str.indexOf(pattern, pos);
			
			count++;
			
			pos = index + pattern.length();
		}
		
		return count;
	}

	/**
	 * 첫번째 문자열과 두번째 문자열을 비교해서 같으면 세번째 문자열을 다르면 네번재 문자열을 반환한다.<br><br>
	 *
	 * StringUtils.decode("Java", "Test", "Good", "Bad") = "bad"
	 *
	 * @param source 첫번째 문자열
	 * @param target 두번째 문자열
	 * @param result 두 문자열이 같을 때 반환할 문자열
	 * @param base 두 문자열이 다를 때 반환할 문자열
	 * @return 두 문자열이 같으면 세번째 문자열을 다르면 네번재 문자열을 반환
	 */
	public static String decode(String source, String target, String result, String base) {
		if (source == null && target == null) {
			return result;
		} else if (source == null && target != null) {
			return base;
		} else if (source.trim().equals(target)) {
			return result;
		}
		
		return base;
	}

	/**
	 * 문자열이 <code>null</code> 또는 공백 또는 공백문자 이면 <code>defaultStr</code>을 반환하고<br />
	 * 아니면 <code>str</code>을 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.defaultIfBlank(null, "NULL")  = "NULL"
	 * StringUtils.defaultIfBlank("", "NULL")    = "NULL"
	 * StringUtils.defaultIfBlank(" ", "NULL")   = "NULL"
	 * StringUtils.defaultIfBlank("bat", "NULL") = "bat"
	 * StringUtils.defaultIfBlank("", null)      = null
	 * </pre>
	 * @param str 문자열
	 * @param defaultStr 초기설정문자
	 * @return 결과문자열
	 */
	public static String defaultIfBlank(String str, String defaultStr) {
		return org.apache.commons.lang.StringUtils.defaultIfEmpty(str, defaultStr);
	}

	/**
	 * 문자열이 <code>null</code> 또는 공백이면 <code>defaultStr</code>을 반환하고<br />
	 * 아니면 <code>str</code>을 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.defaultIfEmpty(null, "NULL")  = "NULL"
	 * StringUtils.defaultIfEmpty("", "NULL")    = "NULL"
	 * StringUtils.defaultIfEmpty("bat", "NULL") = "bat"
	 * StringUtils.defaultIfEmpty("", null)      = null
	 * </pre>
	 *
	 * @param str 문자열
	 * @param defaultStr 초기설정문자
	 * @return 결과문자열
	 */
	public static String defaultIfEmpty(String str, String defaultStr) {
		return org.apache.commons.lang.StringUtils.defaultIfEmpty(str, defaultStr);
	}

	/**
	 * 문자열이 <code>null</code>이면 공백를 반환하고 아니면 <code>str</code>을 반환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.defaultString(null)  = ""
	 * StringUtils.defaultString("")    = ""
	 * StringUtils.defaultString("bat") = "bat"
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 결과문자열, 입력문자열이 null일경우 공백
	 */
	public static String defaultString(String str) {
		return org.apache.commons.lang.StringUtils.defaultString(str);
	}

	/**
	 * 문자열이 <code>null</code>이면 <code>defaultStr</code>을 반환하고<br />
	 * 아니면 <code>str</code>을 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.defaultString(null, "NULL")  = "NULL"
	 * StringUtils.defaultString("", "NULL")    = ""
	 * StringUtils.defaultString("bat", "NULL") = "bat"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param defaultStr 초기설정문자
	 * @return 결과문자열, 문자열이 null일경우 초기설정문자
	 */
	public static String defaultString(String str, String defaultStr) {
		return org.apache.commons.lang.StringUtils.defaultString(str, defaultStr);
	}

	/**
	 * 한 String 객체 안에서 특정 패턴 안에 포함된 모든 character들을 제거한다.<br><br>
	 * 
	 * StringUtils.deleteChars("zzAccBxx", "AB") = "zzccxx"
	 *
	 * @param str
	 *            the source String to search
	 * @param chars
	 *            the char to search for and remove
	 * @return the substring with the char removed if found
	 */
	public static String deleteChars(String str, String chars) {
		if (str == null || chars == null) {
			return str;
		}
		
		String value = str;
		
		for (int i = 0; i < chars.length(); i++) {
			value = removeChar(value, chars.charAt(i));
		}
		
		return value;
	}

	/**
	 * 한 String 객체 안에서 특정 패턴을 제거한다. - 등장 패턴의 위치는 좌측에서부터 계산하고<br>
	 * 겹치지 않는 형태로 계산한다. + 따라서, 제거된 후에도 old 패턴은 남아 있을 수 있다.<br><br>
	 * 
	 * StringUtils.deletePattern("aababa", "aba")는 "aba"<br>
	 * StringUtils.deletePattern("zzABCcc", "ABC") => "zzcc"
	 *
	 * @param str
	 *            the source String to search
	 * @param pattern
	 *            the String to search for and remove
	 * @return the substring with the string removed if found
	 */
	public static String deletePattern(String str, String pattern) {
		return replacePattern(str, pattern, "");
	}

	/**
	 * 문자열중 공백문자가 있으면 모두 제거한다.<br />
	 * 
	 * <pre>
	 * StringUtils.deleteWhitespace(null)         = null
	 * StringUtils.deleteWhitespace("")           = ""
	 * StringUtils.deleteWhitespace("abc")        = "abc"
	 * StringUtils.deleteWhitespace("   ab  c  ") = "abc"
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 결과문자열, 문자열이 null일겨우 <code>null</code>
	 */
	public static String deleteWhitespace(String str) {
		return org.apache.commons.lang.StringUtils.deleteWhitespace(str);
	}
	
	public static String deleteWhitespaceWithSpecialChar(String str) {
		if(str == null) {
			return "";
		}
		
		return deleteWhitespace(str).replaceAll("[^a-zA-Z0-9]", "");
	}

	/**
	 * StringTokenizer를 이용하지 않고 처리하여, 연속된 delimiter 사이는 비어 있는 token으로 간주된다. <br>
	 * 주어진 String이 null일 경우, null을 return한다. <br>
	 * delimiter가 null일 경우, 주어진 String을 하나의 element로 가지는 String[]를 return한다.<br><br>
	 * 
	 * StringUtils.delimitedStringToStringArray("aaa.bbb.ccc.ddd", "."); = test[0]="aaa", test[1]="bbb"...
	 *
	 * @param str 
	 *            the silgle String to convert
	 * @param delimiter
	 *            delimiter for conversioin
	 * @return array of String values
	 */
	public static String[] delimitedStringToStringArray(String str, String delimiter) {
		if (str == null) {
			return null;
		}
		
		if (delimiter == null) {
			return new String[] {str};
		}
		
		List<String> tokens = new ArrayList<String>();
		int pos = 0;
		
		while (str.indexOf(delimiter, pos) != -1) {
			int index = str.indexOf(delimiter, pos);
			tokens.add(str.substring(pos, index));
			pos = index + delimiter.length();
		}
		
		if (pos <= str.length()) {
			tokens.add(str.substring(pos));
		}
		
		return tokens.toArray(new String[tokens.size()]);
	}

	/**
	 * 두문자열를 비교하여 다른부분을 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.difference(null, null) = null
	 * StringUtils.difference("", "") = ""
	 * StringUtils.difference("", "abc") = "abc"
	 * StringUtils.difference("abc", "") = ""
	 * StringUtils.difference("abc", "abc") = ""
	 * StringUtils.difference("ab", "abxyz") = "xyz"
	 * StringUtils.difference("abcde", "abxyz") = "xyz"
	 * StringUtils.difference("abcde", "xyz") = "xyz"
	 * </pre>
	 *
	 * @param str1 문자열1
	 * @param str2 문자열2
	 * @return 결과문자열
	 */
	public static String difference(String str1, String str2) {
		return org.apache.commons.lang.StringUtils.difference(str1, str2);
	}

	/**
	 * 문자열이 검색문자로 끝나는지 확인한다.<br />
	 *
	 * <pre>
	 * StringUtils.endsWith(null, null)      = true
	 * StringUtils.endsWith(null, "def")     = false
	 * StringUtils.endsWith("abcdef", null)  = false
	 * StringUtils.endsWith("abcdef", "def") = true
	 * StringUtils.endsWith("ABCDEF", "def") = false
	 * StringUtils.endsWith("ABCDEF", "cde") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @param suffix 검색문자
	 * @return 문자열이 검색문자로 끝나는경우와 문자열 양쪽모두
	 *  <code>null</code>인경우 <code>true</code> 
	 */
	public static boolean endsWith(String str, String suffix) {
		return org.apache.commons.lang.StringUtils.endsWith(str, suffix);
	}

	/**
	 * 문자열이 검색문자로 대소문자 구분없이 끝나는지 확인한다.<br />
	 *
	 * <pre>
	 * StringUtils.endsWithIgnoreCase(null, null)      = true
	 * StringUtils.endsWithIgnoreCase(null, "def")     = false
	 * StringUtils.endsWithIgnoreCase("abcdef", null)  = false
	 * StringUtils.endsWithIgnoreCase("abcdef", "def") = true
	 * StringUtils.endsWithIgnoreCase("ABCDEF", "def") = true
	 * StringUtils.endsWithIgnoreCase("ABCDEF", "cde") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @param suffix 검색문자
	 * @return 문자열이 검색문자로 대소문자 구분없이 끝나는경우와 문자열 양쪽모두
	 *  <code>null</code>인경우 <code>true</code> 
	 */
	public static boolean endsWithIgnoreCase(String str, String suffix) {
		return org.apache.commons.lang.StringUtils.endsWithIgnoreCase(str, suffix);
	}

	/**
	 * 두개의 문자열이 같은지 비교한다.<br />
	 *
	 * <pre>
	 * StringUtils.equals(null, null)   = true
	 * StringUtils.equals(null, "abc")  = false
	 * StringUtils.equals("abc", null)  = false
	 * StringUtils.equals("abc", "abc") = true
	 * StringUtils.equals("abc", "ABC") = false
	 * </pre>
	 *
	 * @param str1 첫번째 문자열
	 * @param str2 두번째 문자열
	 * @return 두 개의 문자열을 비교하여 같으면 true, 아니면 false를 반환
	 */
	public static boolean equals(String str1, String str2) {
		return org.apache.commons.lang.StringUtils.equals(str1, str2);
	}

	/**
	 * 두개의 문자열을 영문 대소문자를 무시하고 같은지 비교한다.<br />
	 *
	 * <pre>
	 * StringUtils.equalsIgnoreCase(null, null)   = true
	 * StringUtils.equalsIgnoreCase(null, "abc")  = false
	 * StringUtils.equalsIgnoreCase("abc", null)  = false
	 * StringUtils.equalsIgnoreCase("abc", "abc") = true
	 * StringUtils.equalsIgnoreCase("abc", "ABC") = true
	 * </pre>
	 *
	 * @param str1 첫번째 문자열
	 * @param str2 두번째 문자열
	 * @return 두 개의 문자열을 영문 대소문자를 무시하고 비교하여 같으면 true, 아니면 false를 반환
	 */
	public static boolean equalsIgnoreCase(String str1, String str2) {
		return org.apache.commons.lang.StringUtils.equalsIgnoreCase(str1, str2);
	}

	/**
	 * 두개의 문자열이 다른지 비교한다.<br />
	 *
	 * <pre>
	 * StringUtils.notEquals(null, null)   = false
	 * StringUtils.notEquals(null, "abc")  = true
	 * StringUtils.notEquals("abc", null)  = true
	 * StringUtils.notEquals("abc", "abc") = false
	 * StringUtils.notEquals("abc", "ABC") = true
	 * </pre>
	 *
	 * @param str1 첫번째 문자열
	 * @param str2 두번째 문자열
	 * @return 두 개의 문자열을 비교하여 다르면 true, 아니면 false를 반환
	 */
	public static boolean notEquals(String str1, String str2) {
		return !org.apache.commons.lang.StringUtils.equals(str1, str2);
	}

	/**
	 * 두개의 문자열을 영문 대소문자를 무시하고 다른지 비교한다.<br />
	 *
	 * <pre>
	 * StringUtils.notEqIgnoreCase(null, null)   = false
	 * StringUtils.notEqIgnoreCase(null, "abc")  = true
	 * StringUtils.notEqIgnoreCase("abc", null)  = true
	 * StringUtils.notEqIgnoreCase("abc", "abc") = false
	 * StringUtils.notEqIgnoreCase("abc", "ABC") = false
	 * </pre>
	 *
	 * @param str1 첫번째 문자열
	 * @param str2 두번째 문자열
	 * @return 두 개의 문자열을 영문 대소문자를 무시하고 비교하여 다르면 true, 아니면 false를 반환
	 */
	public static boolean notEqIgnoreCase(String str1, String str2) {
		return !org.apache.commons.lang.StringUtils.equalsIgnoreCase(str1, str2);
	}

	/**
	 * 해당하는 문자열에 대해서 byte 단위에 대해서 길이 계산해서 총 길이 반환<br><br>
	 *
	 * StringUtils.getByteLength("Anyframe Java Test") = 18
	 *
	 * @param str 문자열
	 * @return 문자열의 byte 단위 길이
	 */
	public static int getByteLength(String str) {
		if (str == null) {
			return -1;
		}
		
		int size = 0;
	
		for (int i = 0; i < str.length(); i++) {
			size += getByteLength(str.charAt(i));
		}
		
		return size;
	}
	
	private static int getByteLength(char charat) {
		int charCode = charat;
	
		if (charCode <= ONE_BYTE) {
			return 1;
		} else if (charCode <= TWO_BYTE) {
			return 2;
		} else if (charCode <= THREE_BYTE) {
			return 3;
		} else {
			return 4;
		}
	}

	/**
	 * 배열안의 문자열를 비교하여 같은부분의 문자열을 반환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.getCommonPrefix(null) = ""
	 * StringUtils.getCommonPrefix(new String[] {}) = ""
	 * StringUtils.getCommonPrefix(new String[] {"abc"}) = "abc"
	 * StringUtils.getCommonPrefix(new String[] {null, null}) = ""
	 * StringUtils.getCommonPrefix(new String[] {"", ""}) = ""
	 * StringUtils.getCommonPrefix(new String[] {"", null}) = ""
	 * StringUtils.getCommonPrefix(new String[] {"abc", null, null}) = ""
	 * StringUtils.getCommonPrefix(new String[] {null, null, "abc"}) = ""
	 * StringUtils.getCommonPrefix(new String[] {"", "abc"}) = ""
	 * StringUtils.getCommonPrefix(new String[] {"abc", ""}) = ""
	 * StringUtils.getCommonPrefix(new String[] {"abc", "abc"}) = "abc"
	 * StringUtils.getCommonPrefix(new String[] {"abc", "a"}) = "a"
	 * StringUtils.getCommonPrefix(new String[] {"ab", "abxyz"}) = "ab"
	 * StringUtils.getCommonPrefix(new String[] {"abcde", "abxyz"}) = "ab"
	 * StringUtils.getCommonPrefix(new String[] {"abcde", "xyz"}) = ""
	 * StringUtils.getCommonPrefix(new String[] {"xyz", "abcde"}) = ""
	 * StringUtils.getCommonPrefix(new String[] {"i am a machine", "i am a robot"}) = "i am a "
	 * </pre>
	 *
	 * @param strs 문자배열
	 * @return 결과문자열
	 */
	public static String getCommonPrefix(String[] strs) {
		return org.apache.commons.lang.StringUtils.getCommonPrefix(strs);
	}


	/**
	 * 주어진 문자열에 대해서 해당하는 문자열이 포함되어 있는 숫자 반환<br><br>
	 *
	 * StringUtils.getContainsCount("Anyframe Java Test", "a") = 3
	 *
	 * @param str 주어진 문자열
	 * @param sub 검색할 문자열
	 * @return 문자열이 포함되어 있는 숫자
	 */
	public static int getContainsCount(String str, String sub) {
		return org.springframework.util.StringUtils.countOccurrencesOf(str, sub);
	}

	/**
	 * 대소문자 구분없이 주어진 문자열에 대해서 해당하는 문자열이 포함되어 있는 갯수를 반환<br><br>
	 *
	 * StringUtils.getContainsCountIgnoreCase("Anyframe Java Test", "test") = 1
	 *
	 * @param str 주어진 문자열
	 * @param sub 검색할 문자열
	 * @return 문자열이 포함되어 있는 갯수
	 * @see org.springframework.util.StringUtils#countOccurrencesOf(String,
	 *      String)
	 */
	public static int getContainsCountIgnoreCase(String str, String sub) {
		return org.springframework.util.StringUtils.countOccurrencesOf(str.toLowerCase(), sub.toLowerCase());
	}

	/**
	 * 입력된 문자열을 주어진 token에 대해서 분리 후 마지막 문자열 반환<br><br>
	 *
	 * StringUtils.getLastString("Anyframe_Java_Test", "_") = "Test"
	 *
	 * @param origStr 문자열
	 * @param strToken 분리할 token
	 * @return token에 대해서 분리된 마지막 문자열
	 */
	public static String getLastString(String origStr, String strToken) {
		StringTokenizer str = new StringTokenizer(origStr, strToken);
		String lastStr = "";
		
		while (str.hasMoreTokens()) {
			lastStr = str.nextToken();
		}
		
		return lastStr;
	}


	/**
	 * 문자열1을 문자열2로 변환하기위해 치환,삽입,삭제하지 않으면 않되는 최소의 문자수를 반환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.getLevenshteinDistance(null, *)             = IllegalArgumentException
	 * StringUtils.getLevenshteinDistance(*, null)             = IllegalArgumentException
	 * StringUtils.getLevenshteinDistance("","")               = 0
	 * StringUtils.getLevenshteinDistance("","a")              = 1
	 * StringUtils.getLevenshteinDistance("aaapppp", "")       = 7
	 * StringUtils.getLevenshteinDistance("frog", "fog")       = 1
	 * StringUtils.getLevenshteinDistance("fly", "ant")        = 3
	 * StringUtils.getLevenshteinDistance("elephant", "hippo") = 7
	 * StringUtils.getLevenshteinDistance("hippo", "elephant") = 7
	 * StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz") = 8
	 * StringUtils.getLevenshteinDistance("hello", "hallo")    = 1
	 * </pre>
	 *
	 * @param s 문자열1
	 * @param t 문자열2
	 * @return 문자열1을 문자열2로 변환하기위해 치환,삽입,삭제하지 않으면 않되는 최소의 문자수
	 */
	public static int getLevenshteinDistance(String s, String t) {
		return org.apache.commons.lang.StringUtils.getLevenshteinDistance(s, t);
	}

	/**
	 * 입력된 문자열을 주어진 token에 대해서 분리 후 arraylist 형태로 반환<br><br>
	 *
	 * StringUtils.getStringArray("Anyframe/Java/Test", "/")
	 *
	 * @param str 문자열
	 * @param strToken 분리할 token
	 * @return token에 대해서 분리된 arraylist
	 */
	public static String[] getStringArray(String str, String strToken) {
		if (str.indexOf(strToken) != -1) {
			StringTokenizer st = new StringTokenizer(str, strToken);
			String[] stringArray = new String[st.countTokens()];
			
			for (int i = 0; st.hasMoreTokens(); i++) {
				stringArray[i] = st.nextToken();
			}
			
			return stringArray;
		}
		
		return new String[] {str};
	}

	/**
	 * 입력된 문자열을 ,(콤마)에 대해서 분리 후 List<String>으로 반환<br><br>
	 *
	 * @param lst 문자열
	 * @return ,(콤마)로 분리된 List
	 */
	public static List<String> getTokens(String lst) {
		return getTokens(lst, ",");
	}

	/**
	 * 입력된 문자열을 주어진 separator에 대해서 분리 후 List<String>으로 반환<br><br>
	 *
	 * StringUtils.getTokens("Anyframe/Java/Test", "/")
	 *
	 * @param lst 문자열
	 * @param separator 분리할 기준 문자열
	 * @return 기준 문자열로 분리된 List
	 */
	public static List<String> getTokens(String lst, String separator) {
		List<String> tokens = new ArrayList<String>();
	
		if (lst != null) {
			StringTokenizer st = new StringTokenizer(lst, separator);
			
			while (st.hasMoreTokens()) {
				String en = st.nextToken().trim();
				tokens.add(en);
			}
		}
		
		return tokens;
	}

	/**
	 * 코드를 받아 문자열로 변환한다(유니코드)
	 *
	 * @param str
	 *            the String to convert
	 * @return UniCode String
	 */
	public static String hexToString(String str) {
		String inStr = str;
		char[] inChar = inStr.toCharArray();
		StringBuffer sb = new StringBuffer();
	
		for (int i = 0; i < inChar.length; i += 4) {
			String hex = str.substring(i, i + 4);
			sb.append((char) Integer.parseInt(hex, HEX_TO_STRING_INT));
		}
		
		return sb.toString();
	}

	/**
	 * 문자열 내에서 시작인덱스로부터 첫번째 검색문자의 인덱스를 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.indexOf(null, *, *)          = -1
	 * StringUtils.indexOf("", *, *)            = -1
	 * StringUtils.indexOf("aabaabaa", 'b', 0)  = 2
	 * StringUtils.indexOf("aabaabaa", 'b', 3)  = 5
	 * StringUtils.indexOf("aabaabaa", 'b', 9)  = -1
	 * StringUtils.indexOf("aabaabaa", 'b', -1) = 2
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchChar  찾을문자
	 * @param startPos  시작인덱스
	 * @return null 또는 빈 문자열은 INDEX_NOT_FOUND (-1)를 반환
	 */
	public static int indexOf(String str, char searchChar, int startPos) {
		return org.apache.commons.lang.StringUtils.indexOf(str, searchChar, startPos);
	}
	
	public static int indexOf(String str, String searchChar) {
		return org.apache.commons.lang.StringUtils.indexOf(str, searchChar);
	}

	/**
	 * 문자열 내에서 시작인덱스로부터 첫번째 검색문자열의 인덱스를 반환한다.<br />
	 *
	 * Null 문자열은-1을 반환<br />
	 * 부정적인 시작 위치는 0으로 처리<br />
	 * 빈 검색 문자열 항상 일치<br />
	 * 시작 위치가 문자열 길이 보다 큰 일치<br />
	 * 빈 검색 문자열<br />
	 *
	 * <pre>
	 * StringUtils.indexOf(null, *, *)          = -1
	 * StringUtils.indexOf(*, null, *)          = -1
	 * StringUtils.indexOf("", "", 0)           = 0
	 * StringUtils.indexOf("", *, 0)            = -1 (except when * = "")
	 * StringUtils.indexOf("aabaabaa", "a", 0)  = 0
	 * StringUtils.indexOf("aabaabaa", "b", 0)  = 2
	 * StringUtils.indexOf("aabaabaa", "ab", 0) = 1
	 * StringUtils.indexOf("aabaabaa", "b", 3)  = 5
	 * StringUtils.indexOf("aabaabaa", "b", 9)  = -1
	 * StringUtils.indexOf("aabaabaa", "b", -1) = 2
	 * StringUtils.indexOf("aabaabaa", "", 2)   = 2
	 * StringUtils.indexOf("abc", "", 9)        = 3
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchStr 검색문자열
	 * @param startPos 시작인덱스
	 * @return 검색 문자열의 첫 번째 인덱스 없는 경우-1 일치 하거나 null 문자열 입력
	 */
	public static int indexOf(String str, String searchStr, int startPos) {
		return org.apache.commons.lang.StringUtils.indexOf(str, searchStr, startPos);
	}

	/**
	 * 문자열 내에서 검색문자배열중 포함하고 있는지 검색후<br />
	 * 검색된 첫번째 인덱스를 반환한다.<br />
	 * 
	 * 문자열이 <code>null</code> 일경우 <code>-1</code>을 반환.<br />
	 * 검색배열이 <code>null</code> 또는 공백배열일경우 <code>-1</code>을 반환.<br />
	 * 배열내 검색문자열이 공백일경우 <code>0</code>을 반환. <br />
	 *
	 * <pre>
	 * StringUtils.indexOfAny(null, *)                     = -1
	 * StringUtils.indexOfAny(*, null)                     = -1
	 * StringUtils.indexOfAny(*, [])                       = -1
	 * StringUtils.indexOfAny("zzabyycdxx", ["ab","cd"])   = 2
	 * StringUtils.indexOfAny("zzabyycdxx", ["cd","ab"])   = 2
	 * StringUtils.indexOfAny("zzabyycdxx", ["mn","op"])   = -1
	 * StringUtils.indexOfAny("zzabyycdxx", ["zab","aby"]) = 1
	 * StringUtils.indexOfAny("zzabyycdxx", [""])          = 0
	 * StringUtils.indexOfAny("", [""])                    = 0
	 * StringUtils.indexOfAny("", ["a"])                   = -1
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchStrs 검색문자열
	 * @return 검색된 첫번째인덱스, 검색되지않은 경우 -1반환.
	 */
	public static int indexOfAny(String str, String[] searchStrs) {
		return org.apache.commons.lang.StringUtils.indexOfAny(str, searchStrs);
	}

	/**
	 * 두문자열를 비교하여 다른부분의 인덱스를 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.indexOfDifference(null, null) = -1
	 * StringUtils.indexOfDifference("", "") = -1
	 * StringUtils.indexOfDifference("", "abc") = 0
	 * StringUtils.indexOfDifference("abc", "") = 0
	 * StringUtils.indexOfDifference("abc", "abc") = -1
	 * StringUtils.indexOfDifference("ab", "abxyz") = 2
	 * StringUtils.indexOfDifference("abcde", "abxyz") = 2
	 * StringUtils.indexOfDifference("abcde", "xyz") = 0
	 * </pre>
	 *
	 * @param str1 문자열1
	 * @param str2 문자열2
	 * @return 결과문자열, 비교하여 같으면 -1
	 */
	public static int indexOfDifference(String str1, String str2) {
		return org.apache.commons.lang.StringUtils.indexOfDifference(str1, str2);
	}

	/**
	 * 배열안의 문자열를 비교하여 다른부분의 인덱스를 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.indexOfDifference(null) = -1
	 * StringUtils.indexOfDifference(new String[] {}) = -1
	 * StringUtils.indexOfDifference(new String[] {"abc"}) = -1
	 * StringUtils.indexOfDifference(new String[] {null, null}) = -1
	 * StringUtils.indexOfDifference(new String[] {"", ""}) = -1
	 * StringUtils.indexOfDifference(new String[] {"", null}) = 0
	 * StringUtils.indexOfDifference(new String[] {"abc", null, null}) = 0
	 * StringUtils.indexOfDifference(new String[] {null, null, "abc"}) = 0
	 * StringUtils.indexOfDifference(new String[] {"", "abc"}) = 0
	 * StringUtils.indexOfDifference(new String[] {"abc", ""}) = 0
	 * StringUtils.indexOfDifference(new String[] {"abc", "abc"}) = -1
	 * StringUtils.indexOfDifference(new String[] {"abc", "a"}) = 1
	 * StringUtils.indexOfDifference(new String[] {"ab", "abxyz"}) = 2
	 * StringUtils.indexOfDifference(new String[] {"abcde", "abxyz"}) = 2
	 * StringUtils.indexOfDifference(new String[] {"abcde", "xyz"}) = 0
	 * StringUtils.indexOfDifference(new String[] {"xyz", "abcde"}) = 0
	 * StringUtils.indexOfDifference(new String[] {"i am a machine", "i am a robot"}) = 7
	 * </pre>
	 *
	 * @param strs 문자배열
	 * @return 결과문자열, 비교하여 같으면 -1
	 * @since 2.4
	 */
	public static int indexOfDifference(String[] strs) {
		return org.apache.commons.lang.StringUtils.indexOfDifference(strs);
	}

	/**
	 * 대소문자를 구분없이 기준 문자열에서 찾고자 하는 문자열이 포함되어 있는 경우 그 첫번째 문자열의 인덱스를 반환한다.<br><br>
	 *
	 * StringUtils.indexOfIgnoreCase("Anyframe Java Test", "java") = 9
	 *
	 * @param str 기준 문자열
	 * @param search 검색할 문자열
	 * @return 첫번째 문자열의 인덱스
	 * @see String#indexOf(String)
	 */
	public static int indexOfIgnoreCase(String str, String search) {
		if (str == null || search == null) {
			return -1;
		}
		
		return str.toLowerCase().indexOf(search.toLowerCase());
	}

	/**
	 * 입력된 문자열이 유니코드 문자로만 구성되었는지 체크<br><br>
	 *
	 * StringUtils.isAlpha("abcfds") = true
	 *
	 * @param str 문자열
	 * @return 문자열이 유니코드 문자로만 구성되어있을 경우 true
	 */
	public static boolean isAlpha(String str) {
		if (str == null) {
			return false;
		}
		
		int sz = str.length();
		
		if (sz == 0) {
			return false;
		}
		
		for (int i = 0; i < sz; i++) {
			if (!Character.isLetter(str.charAt(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 문자열이 모두 문자 또는 숫자인지 확인한다.<br />
	 *
	 * <pre>
	 * StringUtils.isAlphanumeric(null)   = false
	 * StringUtils.isAlphanumeric("")     = true
	 * StringUtils.isAlphanumeric("  ")   = false
	 * StringUtils.isAlphanumeric("abc")  = true
	 * StringUtils.isAlphanumeric("ab c") = false
	 * StringUtils.isAlphanumeric("ab2c") = true
	 * StringUtils.isAlphanumeric("ab-c") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 문자 또는 숫자 또는 문자열이 null이 아닐경우 <code>true</code>
	 */
	public static boolean isAlphanumeric(String str) {
		return org.apache.commons.lang.StringUtils.isAlphanumeric(str);
	}

	/**
	 * 문자열이 모두 문자 또는 숫자 이거나 <br />
	 * 공백문자인지 확인한다.<br />
	 *
	 * <pre>
	 * StringUtils.isAlphanumericSpace(null)   = false
	 * StringUtils.isAlphanumericSpace("")     = true
	 * StringUtils.isAlphanumericSpace("  ")   = true
	 * StringUtils.isAlphanumericSpace("abc")  = true
	 * StringUtils.isAlphanumericSpace("ab c") = true
	 * StringUtils.isAlphanumericSpace("ab2c") = true
	 * StringUtils.isAlphanumericSpace("ab-c") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 문자열의 문자가 문자 또는 숫자이거나 공백문자 또는 문자열이 null이 아닐경우 <code>true</code>
	 */
	public static boolean isAlphanumericSpace(String str) {
		return org.apache.commons.lang.StringUtils.isAlphanumericSpace(str);
	}

	/**
	 * 문자열이 모두 문자 이거나 공백문자인것을 확인한다.<br />
	 *
	 * <code>null</code> will return <code>false</code>
	 * An empty String (length()=0) will return <code>true</code>.<br />
	 *
	 * <pre>
	 * StringUtils.isAlphaSpace(null)   = false
	 * StringUtils.isAlphaSpace("")     = true
	 * StringUtils.isAlphaSpace("  ")   = true
	 * StringUtils.isAlphaSpace("abc")  = true
	 * StringUtils.isAlphaSpace("ab c") = true
	 * StringUtils.isAlphaSpace("ab2c") = false
	 * StringUtils.isAlphaSpace("ab-c") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 문자열의 문자가 모두 문자이거나 공백문자 또는 문자열이 null이 아닐경우 <code>true</code>
	 */
	public static boolean isAlphaSpace(String str) {
		return org.apache.commons.lang.StringUtils.isAlphaSpace(str);
	}

	/**
	 * printable한 문자열인가를 확인한다.<br />
	 * 반환치가 「return ch >= 32 && ch < 127」경우 또는 공백문자는 true<br />
	 * 
	 * <pre>
	 * StringUtils.isAsciiPrintable(null)     = false
	 * StringUtils.isAsciiPrintable("")       = true
	 * StringUtils.isAsciiPrintable(" ")      = true
	 * StringUtils.isAsciiPrintable("Ceki")   = true
	 * StringUtils.isAsciiPrintable("ab2c")   = true
	 * StringUtils.isAsciiPrintable("!ab-c~") = true
	 * StringUtils.isAsciiPrintable("\u0020") = true
	 * StringUtils.isAsciiPrintable("\u0021") = true
	 * StringUtils.isAsciiPrintable("\u007e") = true
	 * StringUtils.isAsciiPrintable("\u007f") = false
	 * StringUtils.isAsciiPrintable("Ceki G\u00fclc\u00fc") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 문자열의 모든 문자가  printable한 문자일경우 <code>true</code> 
	 */
	public static boolean isAsciiPrintable(String str) {
		return org.apache.commons.lang.StringUtils.isAsciiPrintable(str);
	}

	/**
	 * 문자열이 빈 공백 또는 NULL인지 확인한다.<br />
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @return null 일경우 true
	 *         공백문자 일경우  true
	 *         이외의 경우 false 
	 */
	public static boolean isBlank(String str) {
	    return org.apache.commons.lang.StringUtils.isBlank(str);
	}

	//중복되거나 불필요한 메소드 여기까지...확인. -> 
	/**
	 * 주어진 String이 '숫자'로만 구성되어 있는지를 판별한다.<br>
	 * 숫자인지의 판별은 Java의 기본 판별 기준을 준수한다.<br> 
	 * 주어진 String이 null일 경우, false를 return한다.<br><br>
	 * 
	 * StringUtils.isDigit("1234") = true<br>
	 * StringUtils.isDigit("1234A") = false
	 *
	 * @param str
	 *            the String to check, may be null
	 * @return true if String contains only digits, false if not or null string
	 *         input
	 */
	public static boolean isDigit(String str) {
		if (str == null) {
			return false;
		}
		
		char[] chars = str.toCharArray();
		
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDigit(chars[i])) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * 주어진 문자열이 null 또는 공백일 경우 참 반환<br><br>
	 *
	 * StringUtils.isEmpty("") = true
	 *
	 * @param str 문자열
	 * @return null 또는 공백일 경우 true
	 */
	public static boolean isEmpty(String[] strArray) {
		return (strArray == null || strArray.length == 0);
	}

	/**
	 * 주어진 문자열이 null 또는 공백일 경우 참 반환<br><br>
	 *
	 * StringUtils.isEmpty("") = true
	 *
	 * @param str 문자열
	 * @return null 또는 공백일 경우 true
	 */
	public static boolean isEmpty(String str) {
		return (str == null || str.length() == 0);
	}

	// Mid
	//-----------------------------------------------------------------------
	/**
	 * trim한 문자열이 null 또는 공백일 경우 참 반환<br><br>
	 *
	 * StringUtils.isEmptyTrimmed(" ") = true
	 *
	 * @param str 문자열
	 * @return trim한 문자열이 null 또는 공백일 경우 true
	 */
	public static boolean isEmptyTrimmed(String str) {
		return (str == null || str.trim().length() == 0);
	}

	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 주어진 String이 특정한 포맷으로 구성되었는지를 검사한다.
	 *
	 * @param str
	 *            the String to check, may be null
	 * @param pattern
	 *            the pattern to check, may be null
	 * @return true if String contains the given pattern, false if not or null
	 *         string input
	 */
	public static boolean isFormattedString(String str, String pattern) {
		if (str == null || pattern == null) {
			return false;
		} else {
			return str.matches(pattern);
		}
	}

	/**
	 * 주어진 character가 한글인지의 여부를 판별한다.<br><br>
	 *
	 * StringUtils.isHangul('가') = true<br>
	 * StringUtils.isHangul('T') = false
	 *
	 * @param str
	 *            the String to check, may be null
	 * @return true if the String contains only Korean Language, false if not
	 */
	public static boolean isHangul(char str) {
		String unicodeBlock = Character.UnicodeBlock.of(str).toString();
		
		return unicodeBlock.equals("HANGUL_JAMO")
				|| unicodeBlock.equals("HANGUL_SYLLABLES")
				|| unicodeBlock.equals("HANGUL_COMPATIBILITY_JAMO");
	}

	/**
	 * 주어진 String에 대해서, 한글로만 되어 있는지 혹은 한글이 포함되어 있는지를 판별한다.<br>
	 * full을 true로 설정할 경우, 한글로만 되어 있는지를 판별한다.<br>
	 * '한글로만 되어 있는지'는 영어나 기타 언어가 아님을 의미하는 것이 아니고,<br>
	 * 숫자나 기타 기호 문자 등도 없음을 의미한다.<br>
	 * full을 false로 설정할 경우, 한글이 하나라도 포함되어 있는지를 판별한다.<br><br>
	 *
	 * StringUtils.isHangul("가나다", true) = true<br>
	 * StringUtils.isHangul("가나다abc", true) = false<br>
	 * StringUtils.isHangul("가abc", false) = true<br>
	 * StringUtils.isHangul("abcd", false) = false
	 *
	 * @param str
	 *            the String to check, may be null
	 * @param checkForAll
	 *            flag for check only Korean characters(true) or any Korean
	 *            characters(false)
	 * @return true if the String contains only Korean Language(when checkForAll
	 *         is true) or any Korean characters(when checkForAll is false),
	 *         false if not
	 */
	public static boolean isHangul(String str, boolean checkForAll) {
		char[] chars = str.toCharArray();
		
		if (!checkForAll) {
			for (int i = 0; i < chars.length; i++) {
				if (isHangul(chars[i])) {
					return true;
				}
			}
			
			return false;
		} else {
			for (int i = 0; i < chars.length; i++) {
				if (!isHangul(chars[i])) {
					return false;
				}
			}
			
			return true;
		}
	}

	/**
	 * 주어진 String이 '문자'로만 구성되어 있는지를 판별한다.<br>
	 * 문자인지의 판별은 Java의 기본 판별 기준을 준수한다.<br>
	 * 주어진 String이 null일 경우, false를 return한다.<br><br>
	 *
	 * StringUtils.isLetter("test") = true<br>
	 * StringUtils.isLetter("test가나")= true<br>
	 * StringUtils.isLetter("test#$%") = false<br>
	 * StringUtils.isLetter("test123") = false
	 *
	 * @param str
	 *            the String to check, may be null
	 * @return true if String contains only letters, false if not or null string
	 *         input
	 */
	public static boolean isLetter(String str) {
		if (str == null) {
			return false;
		}
		
		char[] chars = str.toCharArray();
		
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isLetter(chars[i])) {
				return false;
			}
		}
		
		return true;
	}

	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 주어진 String이 '문자'나 '숫자'로만 구성되어 있는지를 판별한다.<br>
	 * 문자나 숫자인지의 판별은 Java의 기본 판별 기준을 준수한다.<br>
	 * 주어진 String이 null일 경우, false를 return한다.<br><br>
	 * 
	 * StringUtils.isLetterOrDigit("12가나") = true<br>
	 * StringUtils.isLetterOrDigit("12가나@#%") = false
	 *
	 * @param str
	 *            the String to check, may be null
	 * @return true if String contains only letters or digits, false if not or
	 *         null string input
	 */
	public static boolean isLetterOrDigit(String str) {
		if (str == null) {
			return false;
		}
		
		char[] chars = str.toCharArray();
		
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isLetterOrDigit(chars[i])) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * 문자열이 빈 공백 또는 NULL이 아닌지 확인한다.<br />
	 *
	 * <pre>
	 * StringUtils.isNotBlank(null)      = false
	 * StringUtils.isNotBlank("")        = false
	 * StringUtils.isNotBlank(" ")       = false
	 * StringUtils.isNotBlank("bob")     = true
	 * StringUtils.isNotBlank("  bob  ") = true
	 * </pre>
	 *
	 * @param str 문자열
	 * @return null이 아닐경우 true
	 *         공백문자가 아닐경우  true
	 *         이외의 경우 false 
	 */
	public static boolean isNotBlank(String str) {
	    return org.apache.commons.lang.StringUtils.isNotBlank(str);
	}

	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 주어진 문자열이 null 또는 공백이 아닐 경우 참 반환<br><br>
	 *
	 * StringUtils.isNotEmpty("abc") = true
	 *
	 * @param str 문자열
	 * @return null 또는 공백이 아닐 경우 true
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 입력인자로 전달된 문자열이 숫자가 아닌 문자가 포함되어있는지 여부를 리턴한다.<br><br>
	 *
	 * StringUtils.isNotNumeric("12345") = false<br>
	 * StringUtils.isNumeric("12345ABC") = true
	 *
	 * @param str
	 *            the String to check, may be null
	 * @return true if String contains any letters, false if not or null string
	 *         input
	 */
	public static boolean isNotNumeric(String str) {
		if (str == null) {
			return false;
		}
		
		int sz = str.length();
		
		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return true;
			}
		}
	
		return false;
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 문자열이 모두 숫자인가를 확인한다.<br />
	 *
	 * <pre>
	 * StringUtils.isNumeric(null)   = false
	 * StringUtils.isNumeric("")     = true
	 * StringUtils.isNumeric("  ")   = false
	 * StringUtils.isNumeric("123")  = true
	 * StringUtils.isNumeric("12 3") = false
	 * StringUtils.isNumeric("ab2c") = false
	 * StringUtils.isNumeric("12-3") = false
	 * StringUtils.isNumeric("12.3") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 문자열의 모든 문자가 숫자 또는 문자열이 null이 아닐경우<code>true</code>
	 */
	public static boolean isNumeric(String str) {
		return org.apache.commons.lang.StringUtils.isNumeric(str);
	}

	/**
	 * 문자열이 숫자 또는 공백문자인가를 확인한다.<br />
	 * 
	 * <pre>
	 * StringUtils.isNumericSpace(null)   = false
	 * StringUtils.isNumericSpace("")     = true
	 * StringUtils.isNumericSpace("  ")   = true
	 * StringUtils.isNumericSpace("123")  = true
	 * StringUtils.isNumericSpace("12 3") = true
	 * StringUtils.isNumericSpace("ab2c") = false
	 * StringUtils.isNumericSpace("12-3") = false
	 * StringUtils.isNumericSpace("12.3") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 문자열이 숫자 또는 공백문자 이거나 문자열이 null이 아닐경우<code>true</code>
	 */
	public static boolean isNumericSpace(String str) {
		return org.apache.commons.lang.StringUtils.isNumericSpace(str);
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 주어진 String이 Space만을 가지고 있는지를 검사한다.<br>
	 * Space의 판별 기준은, String.trim()에서 제거되는 대상을 기준으로 한다.<br>
	 * 주어진 String이 null이면, false를 return한다.<br><br>
	 *
	 * StringUtils.isSpaceOnly("   ") = true<br>
	 * StringUtils.isSpaceOnly("") = true<br>
	 * StringUtils.isSpaceOnly("test") = false
	 *
	 * @param str
	 *            the String to check, may be null
	 * @return true if String contains only whitespace, false if not or null
	 *         string input
	 */
	public static boolean isSpaceOnly(String str) {
		if (str == null) {
			return false;
		} else {
			return str.trim().length() <= 0;
		}
	}

	/**
	 * 문자열이 공백 또는 공백문자인가를 확인한다.<br />
	 *
	 * <pre>
	 * StringUtils.isWhitespace(null)   = false
	 * StringUtils.isWhitespace("")     = true
	 * StringUtils.isWhitespace("  ")   = true
	 * StringUtils.isWhitespace("abc")  = false
	 * StringUtils.isWhitespace("ab2c") = false
	 * StringUtils.isWhitespace("ab-c") = false
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 문자열이 공백 또는 공백문자 이거나 문자열이 null이 아닐경우<code>true</code>
	 */
	public static boolean isWhitespace(String str) {
		return org.apache.commons.lang.StringUtils.isWhitespace(str);
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * collection에서 문자열을 읽어와 구분자로 연결시킨다.<br />
	 *
	 * @param collection Collection
	 * @param separator 구분자
	 * @return 결과문자열, collection가 null일경우 <code>null</code>
	 */
	@SuppressWarnings("rawtypes")
	public static String join(Collection collection, char separator) {
		return org.apache.commons.lang.StringUtils.join(collection, separator);
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * collection에서 문자열을 읽어와 구분자로 연결시킨다.<br />
	 *
	 * @param collection Collection
	 * @param separator 구분자
	 * @return 결과문자열, collection가 null일경우 <code>null</code>
	 */
	@SuppressWarnings("rawtypes")
	public static String join(Collection collection, String separator) {
		return org.apache.commons.lang.StringUtils.join(collection, separator);
	}

	/**
	 * iterator에서 문자열을 읽어와 구분자로 연결시킨다.<br />
	 * 
	 * @param iterator Iterator
	 * @param separator 구분자
	 * @return 결과문자열, iterator가 null일경우 <code>null</code>
	 */
	@SuppressWarnings("rawtypes")
	public static String join(Iterator iterator, char separator) {
		return org.apache.commons.lang.StringUtils.join(iterator, separator);
	}

	// Mid
	//-----------------------------------------------------------------------
	/**
	 * iterator에서 문자열을 읽어와 구분자로 연결시킨다.<br />
	 *
	 * @param iterator Iterator
	 * @param separator 구분자
	 * @return 결과문자열, iterator가 null일경우 <code>null</code>
	 */
	@SuppressWarnings("rawtypes")
	public static String join(Iterator iterator, String separator) {
		return org.apache.commons.lang.StringUtils.join(iterator, separator);
	}

	/**
	 * 배열에서 문자열을 읽어와 모두 연결시킨다.<br />
	 *
	 * <pre>
	 * StringUtils.join(null)            = null
	 * StringUtils.join([])              = ""
	 * StringUtils.join([null])          = ""
	 * StringUtils.join(["a", "b", "c"]) = "abc"
	 * StringUtils.join([null, "", "a"]) = "a"
	 * </pre>
	 *
	 * @param array 배열
	 * @return 결과문자열, 배열이 null일경우 <code>null</code>
	 */
	public static String join(Object[] array) {
		return org.apache.commons.lang.StringUtils.join(array);
	}

	/**
	 * 배열에서 문자열을 읽어와 모두 연결시킨다.<br />
	 *
	 * <pre>
	 * StringUtils.join(null, *)               = null
	 * StringUtils.join([], *)                 = ""
	 * StringUtils.join([null], *)             = ""
	 * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
	 * StringUtils.join(["a", "b", "c"], null) = "abc"
	 * StringUtils.join([null, "", "a"], ';')  = ";;a"
	 * </pre>
	 *
	 * @param array 배열
	 * @param separator 구분자
	 * @return 결과문자열, 배열이 null일경우 <code>null</code>
	 */
	public static String join(Object[] array, char separator) {
		return org.apache.commons.lang.StringUtils.join(array, separator);
	}

	/**
	 * 배열에서 문자열을 읽어와 배열의 시작인덱스부터 끝인덱스전까지 <br />
	 * 구분자를 사이에두고 연결시킨다.<br />
	 *
	 * <pre>
	 * StringUtils.join(null, *)               = null
	 * StringUtils.join([], *)                 = ""
	 * StringUtils.join([null], *)             = ""
	 * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
	 * StringUtils.join(["a", "b", "c"], null) = "abc"
	 * StringUtils.join([null, "", "a"], ';')  = ";;a"
	 * </pre>
	 *
	 * @param array 배열
	 * @param separator 구분자
	 * @param startIndex 시작인덱스
	 * @param endIndex 끝인덱스
	 * @return 결과문자열, 배열이 null일경우 <code>null</code>
	 */
	public static String join(Object[] array, char separator, int startIndex, int endIndex) {
		return org.apache.commons.lang.StringUtils.join(array, separator, startIndex, endIndex);
	}

	/**
	 * 배열에서 문자열을 읽어와 모두 구분자를 사이에두고 연결시킨다.<br />
	 *
	 * <pre>
	 * StringUtils.join(null, *)                = null
	 * StringUtils.join([], *)                  = ""
	 * StringUtils.join([null], *)              = ""
	 * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
	 * StringUtils.join(["a", "b", "c"], null)  = "abc"
	 * StringUtils.join(["a", "b", "c"], "")    = "abc"
	 * StringUtils.join([null, "", "a"], ',')   = ",,a"
	 * </pre>
	 *
	 * @param array 배열
	 * @param separator 구분자
	 * @return 결과문자열, 배열이 null일경우 <code>null</code>
	 */
	public static String join(Object[] array, String separator) {
		return org.apache.commons.lang.StringUtils.join(array, separator);
	}

	/**
	 * 배열에서 문자열을 읽어와 배열의 시작인덱스부터 끝인덱스전까지 <br />
	 * 구분자를 사이에두고 연결시킨다.<br />
	 *
	 * <pre>
	 * StringUtils.join(null, *)                = null
	 * StringUtils.join([], *)                  = ""
	 * StringUtils.join([null], *)              = ""
	 * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
	 * StringUtils.join(["a", "b", "c"], null)  = "abc"
	 * StringUtils.join(["a", "b", "c"], "")    = "abc"
	 * StringUtils.join([null, "", "a"], ',')   = ",,a"
	 * </pre>
	 *
	 * @param array 배열
	 * @param separator 구분자
	 * @param startIndex 시작인덱스
	 * @param endIndex 끝인덱스
	 * @return 결과문자열, 배열이 null일경우 <code>null</code>
	 */
	public static String join(Object[] array, String separator, int startIndex, int endIndex) {
		return org.apache.commons.lang.StringUtils.join(array, separator, startIndex, endIndex);
	}

	/**
	 * 문자열 내에서 마지막 검색문자의 인덱스를 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.lastIndexOf(null, *)         = -1
	 * StringUtils.lastIndexOf("", *)           = -1
	 * StringUtils.lastIndexOf("aabaabaa", 'a') = 7
	 * StringUtils.lastIndexOf("aabaabaa", 'b') = 5
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchChar 검색문자
	 * @return 검색 문자 마지막 인덱스 없는 경우-1 일치 또는 null 문자열 입력
	 */
	public static int lastIndexOf(String str, char searchChar) {
		return org.apache.commons.lang.StringUtils.lastIndexOf(str, searchChar);
	}

	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 문자열 내에서 시작인덱스로부터 마지막 검색문자의 인덱스를 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.lastIndexOf(null, *, *)          = -1
	 * StringUtils.lastIndexOf("", *,  *)           = -1
	 * StringUtils.lastIndexOf("aabaabaa", 'b', 8)  = 5
	 * StringUtils.lastIndexOf("aabaabaa", 'b', 4)  = 2
	 * StringUtils.lastIndexOf("aabaabaa", 'b', 0)  = -1
	 * StringUtils.lastIndexOf("aabaabaa", 'b', 9)  = 5
	 * StringUtils.lastIndexOf("aabaabaa", 'b', -1) = -1
	 * StringUtils.lastIndexOf("aabaabaa", 'a', 0)  = 0
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchChar 검색문자
	 * @param startPos 시작인덱스
	 * @return 검색 문자 마지막 인덱스 없는 경우-1 일치 또는 null 문자열 입력
	 */
	public static int lastIndexOf(String str, char searchChar, int startPos) {
		return org.apache.commons.lang.StringUtils.lastIndexOf(str, searchChar, startPos);
	}

	/**
	 * 문자열 내에서 마지막 검색문자열의 인덱스를 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.lastIndexOf(null, *)          = -1
	 * StringUtils.lastIndexOf(*, null)          = -1
	 * StringUtils.lastIndexOf("", "")           = 0
	 * StringUtils.lastIndexOf("aabaabaa", "a")  = 7
	 * StringUtils.lastIndexOf("aabaabaa", "b")  = 5
	 * StringUtils.lastIndexOf("aabaabaa", "ab") = 4
	 * StringUtils.lastIndexOf("aabaabaa", "")   = 8
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchStr 검색문자열
	 * @return 검색 문자열의 마지막 인덱스 없는 경우-1 일치 또는 null 문자열 입력
	 */
	public static int lastIndexOf(String str, String searchStr) {
		return org.apache.commons.lang.StringUtils.lastIndexOf(str, searchStr);
	}

	/**
	 * 문자열 내에서 시작인덱스로부터 앞으로 검색문자열인덱스를 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.lastIndexOf(null, *, *)          = -1
	 * StringUtils.lastIndexOf(*, null, *)          = -1
	 * StringUtils.lastIndexOf("aabaabaa", "a", 8)  = 7
	 * StringUtils.lastIndexOf("aabaabaa", "b", 8)  = 5
	 * StringUtils.lastIndexOf("aabaabaa", "ab", 8) = 4
	 * StringUtils.lastIndexOf("aabaabaa", "b", 9)  = 5
	 * StringUtils.lastIndexOf("aabaabaa", "b", -1) = -1
	 * StringUtils.lastIndexOf("aabaabaa", "a", 0)  = 0
	 * StringUtils.lastIndexOf("aabaabaa", "b", 0)  = -1
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchStr 검색문자열
	 * @param startPos 시작인덱스
	 * @return 검색 문자열의 마지막 인덱스 없는 경우-1 일치 또는 null 문자열 입력
	 */
	public static int lastIndexOf(String str, String searchStr, int startPos) {
		return org.apache.commons.lang.StringUtils.lastIndexOf(str, searchStr, startPos);
	}

	/**
	 * 문자열 내에서 검색문자배열중 포함하고 있는지 검색후<br />
	 * 최후에 검색된 인덱스를 반환한다.<br />
	 * 
	 * 문자열이 <code>null</code> 일경우 <code>-1</code>을 반환.<br />
	 * 검색배열이 <code>null</code> 또는 공백배열일경우 <code>-1</code>을 반환.<br />
	 * 배열내 검색문자열이 <code>null</code> 또는 공백일경우 <code>-1</code>을 반환. <br />
	 *
	 * <pre>
	 * StringUtils.lastIndexOfAny(null, *)                   = -1
	 * StringUtils.lastIndexOfAny(*, null)                   = -1
	 * StringUtils.lastIndexOfAny(*, [])                     = -1
	 * StringUtils.lastIndexOfAny(*, [null])                 = -1
	 * StringUtils.lastIndexOfAny("zzabyycdxx", ["ab","cd"]) = 6
	 * StringUtils.lastIndexOfAny("zzabyycdxx", ["cd","ab"]) = 6
	 * StringUtils.lastIndexOfAny("zzabyycdxx", ["mn","op"]) = -1
	 * StringUtils.lastIndexOfAny("zzabyycdxx", ["mn","op"]) = -1
	 * StringUtils.lastIndexOfAny("zzabyycdxx", ["mn",""])   = 10
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchStrs 검색문자열
	 * @return 검색된 최후의 인덱스, 검색되지않은 경우 -1반환.
	 */
	public static int lastIndexOfAny(String str, String[] searchStrs) {
		return org.apache.commons.lang.StringUtils.lastIndexOfAny(str, searchStrs);
	}

	/**
	 * 주어진 String 객체에 대해서 주어진 길이만큼 왼쪽 부분을 떼어 반환한다.<br>
	 * 주어진 길이보다 주어진 String의 길이가 작을 경우에는 주어진 String을 그대로 반환한다.<br>
	 * "..."을 붙이지 않는 점을 제외하고는 splitHead()와 동일하다.<br><br>
	 *
	 * StringUtils.left("1234567", 3) = "123"
	 *
	 * @param str
	 *            the String to get the leftmost characters from, may be null
	 * @param len
	 *            the length of the required String
	 * @return the leftmost characters, null if null String input
	 */
	public static String left(String str, int len) {
		if (str == null) {
			return null;
		} else if (len <= 0 || str.length() <= len) {
			return str;
		} else {
			return str.substring(0, len);
		}
	}

	/**
	 * 해당하는 문자열에 대해서 입력된 길이만큼 부족한 길이를 왼쪽부터 공백으로 채워넣는다.<br><br>
	 *
	 * StringUtils.leftPad("Anyframe", 12) = "    Anyframe"
	 *
	 * @param str 문자열
	 * @param size 공백이 채워진 문자열의 전체 길이
	 * @return 부족한 길이만큼 왼쪽부터 공백이 채워진 문자열
	 */
	public static String leftPad(String str, int size) {
		return leftPad(str, size, ' ');
	}

	/**
	 * 해당하는 문자열에 대해서 입력된 길이만큼 부족한 길이를 왼쪽부터 지정된 character로 채워넣는다.<br><br>
	 *
	 * StringUtils.leftPad("Anyframe", 12, 'a') = "aaaaAnyframe"
	 *
	 * @param str 문자열
	 * @param size 캐릭터가 채워진 문자열의 전체 길이
	 * @param padChar 채워넣을 캐릭터
	 * @return 부족한 길이만큼 왼쪽부터 캐릭터가 채워진 문자열
	 */
	public static String leftPad(String str, int size, char padChar) {
		return padChar(str, size, padChar, true);
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 해당하는 문자열에 대해서 입력된 길이만큼 부족한 길이를 왼쪽부터 지정된 문자열로 채워넣는다.<br><br>
	 *
	 * StringUtils.leftPad("Anyframe", 12, "Java") = "JavaAnyframe"
	 *
	 * @param str 문자열
	 * @param size 문자열이 채워진 문자열의 전체 길이
	 * @param padStr 채워넣을 문자열
	 * @return 부족한 길이만큼 왼쪽부터 문자열이 채워진 문자열
	 */
	public static String leftPad(String str, int size, String padStr) {
		return padString(str, size, padStr, true);
	}

	/**
	 * 문자열의 왼쪽의 공백 문자열 제거<br><br>
	 *
	 * StringUtils.leftTrim(" Anyframe Java Test") = "Anyframe Java Test"
	 *
	 * @param str 문자열
	 * @return 왼쪽 공백을 제거한 문자열
	 * @see org.springframework.util.StringUtils#trimLeadingWhitespace(String)
	 */
	public static String leftTrim(String str) {
		return org.springframework.util.StringUtils.trimLeadingWhitespace(str);
	}

	/**
	 * 문자열을 소문자로 변환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.lowerCase(null)  = null
	 * StringUtils.lowerCase("")    = ""
	 * StringUtils.lowerCase("aBc") = "abc"
	 * </pre>
	 *
	 * @param str 문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String lowerCase(String str) {
		return org.apache.commons.lang.StringUtils.lowerCase(str);
	}

	/**
	 * 문자열의 pos 인덱스부터 len 길이만큼의 문자열을 구한다.<br />
	 *
	 * <pre>
	 * StringUtils.mid(null, *, *)    = null
	 * StringUtils.mid(*, *, -ve)     = ""
	 * StringUtils.mid("", 0, *)      = ""
	 * StringUtils.mid("abc", 0, 2)   = "ab"
	 * StringUtils.mid("abc", 0, 4)   = "abc"
	 * StringUtils.mid("abc", 2, 4)   = "c"
	 * StringUtils.mid("abc", 4, 2)   = ""
	 * StringUtils.mid("abc", -2, 2)  = "ab"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param pos 시작인덱스
	 * @param len 길이 the length of the required String
	 * @return 결과 문자열, 문자열이 null의 경우 <code>null</code>
	 */
	public static String mid(String str, int pos, int len) {
		return org.apache.commons.lang.StringUtils.mid(str, pos, len);
	}

	/**
	 * CRLF(newLine)가 포함된 문자열을 입력인자로 받아 CRLF(개행문자)를 SPACE로 변환하여 리턴한다.<br><br>
	 * 
	 * StringUtils.newLineToSpace("\r\ntest") = " test"
	 *
	 * @param str
	 *            the String to convert
	 * @return the converted string
	 */
	public static String newLineToSpace(String str) {
		String output;
	
		output = str.replace("\r\n", " ");
	
		return output;
	}

	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 주어진 String 객체를 검사하여 null일 경우 "" 을 반환하고, 아니면 원본을 반환한다.<br><br>
	 *
	 * StringUtils.nullToEmpty("test") = "test"<br>
	 * StringUtils.nullToEmpty(null) = ""
	 *
	 * @param str
	 *            the String to check
	 * @return empty string if the given String is null, given string if not
	 */
	public static String nullToEmpty(String str) {
		if (str == null || str.length() <= 0) {
			return DEFAULT_EMPTY_STRING;
		} else {
			return str;
		}
	}

	/**
	 * 주어진 Object가 null이 아닐 경우 그 Object를 반환하고, null일 경우 default Object를 반환한다.<br><br>
	 *
	 * StringUtils.nvl(null, "NULL TEST") = "NULL TEST"<br>
	 * StringUtils.nvl("test", "NULL TEST") = "test"
	 *
	 * @param inputObject
	 *            the Object to check
	 * @param defaultObject
	 *            the default Object
	 * @return Returns the default Object if the given Object is null, returns
	 *         the given Object if not
	 */
	public static Object nvl(Object inputObject, Object defaultObject) {
		return inputObject != null ? inputObject : defaultObject;
	}

	/**
	 * 주어진 String이 null이 아닐 경우 그 String을 반환하고, null일 경우 default String을 반환한다.<br><br>
	 *
	 * StringUtils.nvl(null, "NULL TEST") = "NULL TEST"<br>
	 * StringUtils.nvl("test", "NULL TEST")) = "test"
	 *
	 * @param inputString
	 *            the String to check
	 * @param defaultString
	 *            the default String
	 * @return Returns the default String if the given String is null, returns
	 *         the given String if not
	 */
	public static String nvl(String inputString, String defaultString) {
		return (String) nvl((Object) inputString, (Object) defaultString);
	}
	
	public static String nvl(Object inputObject, String defaultString) {
		return (String) nvl((Object) inputObject, (Object) defaultString);
	}
	
	public static String nvl(Object inputObject) {
		return (String) nvl((Object) inputObject, (Object) "");
	}

	/**
	 * 문자열 내에서 검색문자열의 n번째 해당하는 인덱스를 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.ordinalIndexOf(null, *, *)          = -1
	 * StringUtils.ordinalIndexOf(*, null, *)          = -1
	 * StringUtils.ordinalIndexOf("", "", *)           = 0
	 * StringUtils.ordinalIndexOf("aabaabaa", "a", 1)  = 0
	 * StringUtils.ordinalIndexOf("aabaabaa", "a", 2)  = 1
	 * StringUtils.ordinalIndexOf("aabaabaa", "b", 1)  = 2
	 * StringUtils.ordinalIndexOf("aabaabaa", "b", 2)  = 5
	 * StringUtils.ordinalIndexOf("aabaabaa", "ab", 1) = 1
	 * StringUtils.ordinalIndexOf("aabaabaa", "ab", 2) = 4
	 * StringUtils.ordinalIndexOf("aabaabaa", "", 1)   = 0
	 * StringUtils.ordinalIndexOf("aabaabaa", "", 2)   = 0
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchStr 검색문자열
	 * @param ordinal n번째
	 * @return 검색 문자열의 n 번째 인덱스-1 (INDEX_NOT_FOUND) 없으면 일치 또는 null 문자열 입력
	 */
	public static int ordinalIndexOf(String str, String searchStr, int ordinal) {
		return org.apache.commons.lang.StringUtils.ordinalIndexOf(str, searchStr, ordinal);
	}

	/**
	 * 문자열의 시작점부터 종료점까지 변환문자로 변환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.overlay(null, *, *, *)            = null
	 * StringUtils.overlay("", "abc", 0, 0)          = "abc"
	 * StringUtils.overlay("abcdef", null, 2, 4)     = "abef"
	 * StringUtils.overlay("abcdef", "", 2, 4)       = "abef"
	 * StringUtils.overlay("abcdef", "", 4, 2)       = "abef"
	 * StringUtils.overlay("abcdef", "zzzz", 2, 4)   = "abzzzzef"
	 * StringUtils.overlay("abcdef", "zzzz", 4, 2)   = "abzzzzef"
	 * StringUtils.overlay("abcdef", "zzzz", -1, 4)  = "zzzzef"
	 * StringUtils.overlay("abcdef", "zzzz", 2, 8)   = "abzzzz"
	 * StringUtils.overlay("abcdef", "zzzz", -2, -3) = "zzzzabcdef"
	 * StringUtils.overlay("abcdef", "zzzz", 8, 10)  = "abcdefzzzz"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param overlay 변환문자
	 * @param start 시작점
	 * @param end 종료점
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String overlay(String str, String overlay, int start, int end) {
		return org.apache.commons.lang.StringUtils.overlay(str, overlay, start, end);
	}

	private static String padChar(String str, int size, char padChar, boolean isLeft) {
		if (str == null) {
			return null;
		}
		
		int originalStrLength = str.length();
	
		if (size < originalStrLength) {
			return str;
		}
	
		int difference = size - originalStrLength;
	
		StringBuilder strBuf = new StringBuilder();
	
		if (!isLeft) {
			strBuf.append(str);
		}
	
		for (int i = 0; i < difference; i++) {
			strBuf.append(padChar);
		}
	
		if (isLeft) {
			strBuf.append(str);
		}
	
		return strBuf.toString();
	}

	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 특정한 문자(char)와 일정한 길이 값을 입력으로 받아 해당 크기만큼 문자가 반복되는 문자열을 생성한다.<br>
	 * 주어진 길이 값이 0이면 ""을 주어진 길이 값이 0보다 작으면 null을 리턴한다.<br>
	 * length는 String.getBytes().length 기준이 아닌 String.length() 기준임을 유의한다.<br><br>
	 *
	 * StringUtils.padding(5, 'e') = "eeeee"
	 *
	 * @param size
	 *            the length to pad to
	 * @param padChar
	 *            the character to pad with
	 * @return padded String
	 */
	public static String padding(int size, char padChar) {
		if (size < 0) {
			return null;
		}
		
		StringBuffer buffer = new StringBuffer(size);
		
		for (int i = 0; i < size; i++) {
			buffer.insert(i, padChar);
		}
		
		return buffer.toString();
	}
	
	// Mid
	//-----------------------------------------------------------------------
	private static String padString(String str, int size, String padStr, boolean isLeft) {
		if (str == null) {
			return null;
		}
		
		int originalStrLength = str.length();
	
		if (size < originalStrLength) {
			return str;
		}
	
		int difference = size - originalStrLength;
	
		String tempPad = "";
		
		if (difference > 0) {
			if (padStr == null || "".equals(padStr)) {
				padStr = " ";
			}
			
			do {
				for (int j = 0; j < padStr.length(); j++) {
					tempPad += padStr.charAt(j);
					
					if (str.length() + tempPad.length() >= size) {
						break;
					}
				}
			} while (difference > tempPad.length());
			
			if (isLeft) {
				str = tempPad + str;
			} else {
				str = str + tempPad;
			}
		}
	
		return str;
	}

	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 문자열 내에서 삭제문자가 포함되어있는 부분을 삭제후 반환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.remove(null, *)       = null
	 * StringUtils.remove("", *)         = ""
	 * StringUtils.remove("queued", 'u') = "qeed"
	 * StringUtils.remove("queued", 'z') = "queued"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param remove 삭제문자
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String remove(String str, char remove) {
		return org.apache.commons.lang.StringUtils.remove(str, remove);
	}

	/**
	 * 문자열 내에서 삭제문자열이 포함되어있는 부분을 삭제후 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.remove(null, *)        = null
	 * StringUtils.remove("", *)          = ""
	 * StringUtils.remove(*, null)        = *
	 * StringUtils.remove(*, "")          = *
	 * StringUtils.remove("queued", "ue") = "qd"
	 * StringUtils.remove("queued", "zz") = "queued"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param remove 삭제문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String remove(String str, String remove) {
		return org.apache.commons.lang.StringUtils.remove(str, remove);
	}

	/**
	 * 입력된 문자열에 대해서 제거할 문자열을 모두 제거<br><br>
	 *
	 * StringUtils.removeAll("Anyframe Java Test", "Java") = "Anyfrme Test"
	 *
	 * @param str 문자열
	 * @param charsToDelete 제거할 문자열
	 * @return 문자열을 제거한 문자열
	 * @see org.springframework.util.StringUtils#deleteAny(String, String)
	 */
	public static String removeAll(String str, String charsToDelete) {
		return org.springframework.util.StringUtils.deleteAny(str,
				charsToDelete);
	}

	/**
	 * 한 String 객체 안에서 주어진 문자(char)를 제거한다.<br><br>
	 *
	 * StringUtils.removeChar("ABBBBBC", 'B') = "AC"
	 *
	 * @param str
	 *            the source String to search
	 * @param remove
	 *            the char to search for and remove
	 * @return the substring with the char removed if found
	 */
	public static String removeChar(String str, char remove) {
		return replacePattern(str, String.valueOf(remove), "");
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 한 String 객체 안에서 특정 문자들을 제거한다.<br>
	 * 제거할 대상 문자는 다음과 같다. {'/', '-', ':', ',', '.', '%' }<br><br>
	 *
	 * StringUtils.removeCharAll("test/-") = "test"
	 *
	 * @param str
	 *            the source String to search
	 * @return the substring with specified chars removed if found
	 */
	public static String removeCharAll(String str) {
		char[] targetCharacters = {'/', '-', ':', ',', '.', '%'};
		
		return removeCharAll(str, targetCharacters);
	}

	/**
	 * 한 String 객체 안에서 주어진 문자들을 제거한다.<br><br>
	 *
	 * StringUtils.removeCharAll("AbbzzB", new char[]{'b','z'}) = "AB"
	 *
	 * @param str
	 *            the source String to search
	 * @param remove
	 *            chars to search for (case insensitive) and remove
	 * @return the substring with given chars removed if found
	 */
	public static String removeCharAll(String str, char[] remove) {
		String value = str;
		
		for (int i = 0; i < remove.length; i++) {
			value = removeChar(value, remove[i]);
		}
		
		return value;
	}

	/**
	 * 문자열의 뒤에서부터 삭제문자열과 같은 부분을 삭제후 나머지를 반환한다.<br /> 
	 *
	 * <pre>
	 * StringUtils.removeEnd(null, *)      = null
	 * StringUtils.removeEnd("", *)        = ""
	 * StringUtils.removeEnd(*, null)      = *
	 * StringUtils.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
	 * StringUtils.removeEnd("www.domain.com", ".com")   = "www.domain"
	 * StringUtils.removeEnd("www.domain.com", "domain") = "www.domain.com"
	 * StringUtils.removeEnd("abc", "")    = "abc"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param remove 삭제문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String removeEnd(String str, String remove) {
		return org.apache.commons.lang.StringUtils.removeEnd(str, remove);
	}

	/**
	 * 문자열의 뒤에서부터 삭제문자열과 영문대소문자 구분없이 같은 부분을 삭제후 나머지를 반환한다.<br /> 
	 *
	 * <pre>
	 * StringUtils.removeEndIgnoreCase(null, *)      = null
	 * StringUtils.removeEndIgnoreCase("", *)        = ""
	 * StringUtils.removeEndIgnoreCase(*, null)      = *
	 * StringUtils.removeEndIgnoreCase("www.domain.com", ".com.")  = "www.domain.com"
	 * StringUtils.removeEndIgnoreCase("www.domain.com", ".com")   = "www.domain"
	 * StringUtils.removeEndIgnoreCase("www.domain.com", "domain") = "www.domain.com"
	 * StringUtils.removeEndIgnoreCase("abc", "")    = "abc"
	 * StringUtils.removeEndIgnoreCase("www.domain.com", ".COM") = "www.domain")
	 * StringUtils.removeEndIgnoreCase("www.domain.COM", ".com") = "www.domain")
	 * </pre>
	 *
	 * @param str 문자열
	 * @param remove 삭제문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String removeEndIgnoreCase(String str, String remove) {
		return org.apache.commons.lang.StringUtils.removeEndIgnoreCase(str, remove);
	}

	/**
	 * unescaped된 문자열에 대해 HTML tag 형태로 바꿔준다.<br><br>
	 *
	 * StringUtils.removeEscapeChar("&lt;html&gt;Anyframe Java Test&lt;html&gt;") = "<html>Anyframe Java Test<html>"
	 *
	 * @param input unescaped된 문자열
	 * @return HTML tag 형태로 바뀐 문자열
	 * @see HtmlUtils#htmlUnescape(String)
	 */
	public static String removeEscapeChar(String input) {
		return HtmlUtils.htmlUnescape(input);
	}

	/**
	 * 문자열에서 삭제문자열을 삭제한나머지 문자열을 반환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.removeStart(null, *)      = null
	 * StringUtils.removeStart("", *)        = ""
	 * StringUtils.removeStart(*, null)      = *
	 * StringUtils.removeStart("www.domain.com", "www.")   = "domain.com"
	 * StringUtils.removeStart("domain.com", "www.")       = "domain.com"
	 * StringUtils.removeStart("www.domain.com", "domain") = "www.domain.com"
	 * StringUtils.removeStart("abc", "")    = "abc"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param remove 삭제문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String removeStart(String str, String remove) {
		return org.apache.commons.lang.StringUtils.removeStart(str, remove);
	}

	/**
	 * 문자열의 시작부터 삭제문자열과 영문대소문자 구분없이 같은 부분을 삭제후 나머지를 반환한다.<br /> 
	 *
	 * <pre>
	 * StringUtils.removeStartIgnoreCase(null, *)      = null
	 * StringUtils.removeStartIgnoreCase("", *)        = ""
	 * StringUtils.removeStartIgnoreCase(*, null)      = *
	 * StringUtils.removeStartIgnoreCase("www.domain.com", "www.")   = "domain.com"
	 * StringUtils.removeStartIgnoreCase("www.domain.com", "WWW.")   = "domain.com"
	 * StringUtils.removeStartIgnoreCase("domain.com", "www.")       = "domain.com"
	 * StringUtils.removeStartIgnoreCase("www.domain.com", "domain") = "www.domain.com"
	 * StringUtils.removeStartIgnoreCase("abc", "")    = "abc"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param remove 삭제문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String removeStartIgnoreCase(String str, String remove) {
		return org.apache.commons.lang.StringUtils.removeStartIgnoreCase(str, remove);
	}

	/**
	 * 문자열의 모든 공백 문자열 제거<br><br>
	 *
	 * StringUtils.removeWhitespace("Anyframe Java Test") = "AnyframeJavaTest"
	 *
	 * @param str 문자열
	 * @return 공백을 제거한 문자열
	 * @see org.springframework.util.StringUtils#trimAllWhitespace(String)
	 */
	public static String removeWhitespace(String str) {
		return org.springframework.util.StringUtils.trimAllWhitespace(str);
	}

	/**
	 * 문자열을 반복횟수만큼 반복하여 반환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.repeat(null, 2) = null
	 * StringUtils.repeat("", 0)   = ""
	 * StringUtils.repeat("", 2)   = ""
	 * StringUtils.repeat("a", 3)  = "aaa"
	 * StringUtils.repeat("ab", 2) = "abab"
	 * StringUtils.repeat("a", -2) = ""
	 * </pre>
	 *
	 * @param str 문자열
	 * @param repeat 반복횟수
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String repeat(String str, int repeat) {
		return org.apache.commons.lang.StringUtils.repeat(str, repeat);
	}

	/**
	 * 입력된 문자열에 대해서 해당하는 character를 찾아 주어진 문자열로 변경<br><br>
	 *
	 * StringUtils.replace("Anyframe/Common", "/", "|") = "Anyframe|Common"
	 *
	 * @param str 문자열
	 * @param replacedStr 검색할 문자열
	 * @param replaceStr 변경할 문자열
	 * @return 검색된 문자열을 변경한 문자열
	 */
	public static String replace(String str, String replacedStr, String replaceStr) {
		String newStr = "";
		
		if (str.indexOf(replacedStr) != -1) {
			String s1 = str.substring(0, str.indexOf(replacedStr));
			String s2 = str.substring(str.indexOf(replacedStr) + replacedStr.length());
			
			newStr = s1 + replaceStr + s2;
		} else {
			return str;
		}
		
		return newStr;
	}

	/**
	 * 문자열 내에서 검색문자열을 검색후 치환최대치 만큼 치환문자열로 치환하여 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *, *)         = null
	 * StringUtils.replace("", *, *, *)           = ""
	 * StringUtils.replace("any", null, *, *)     = "any"
	 * StringUtils.replace("any", *, null, *)     = "any"
	 * StringUtils.replace("any", "", *, *)       = "any"
	 * StringUtils.replace("any", *, *, 0)        = "any"
	 * StringUtils.replace("abaa", "a", null, -1) = "abaa"
	 * StringUtils.replace("abaa", "a", "", -1)   = "b"
	 * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
	 * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
	 * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
	 * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
	 * </pre>
	 *
	 * @param text 문자열
	 * @param searchString 검색문자열
	 * @param replacement 치환문자열
	 * @param max 치환최대치
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String replace(String text, String searchString, String replacement, int max) {
		return org.apache.commons.lang.StringUtils.replace(text, searchString, replacement, max);
	}

	/**
	 * 입력된 문자열이 주어진 문자열과 일치하는 모든 문자열을 바꿔야할 문자열로 변경<br><br>
	 *
	 * StringUtils.replaceAll("Anyframe Java Test Anyframe Java Test", "Anyframe", "Enterprise") = "Enterprise Java Test Enterprise Java Test"
	 *
	 * @param source 문자열
	 * @param regex 검색할 문자열
	 * @param replacement 변경할 문자열
	 * @return 검색된 모든 문자열을 변경한 문자열
	 * @see String#replaceAll(String, String)
	 */
	public static String replaceAll(String source, String regex, String replacement) {
		if (source == null) {
			return null;
		}
		
		return source.replaceAll(regex, replacement);
	}

	// Replace, character based
	//-----------------------------------------------------------------------
	/**
	 * 문자열에서 검색문자를 치환문자로 모두 변환한다.<br />
	 * 
	 * <pre>
	 * StringUtils.replaceChars(null, *, *)        = null
	 * StringUtils.replaceChars("", *, *)          = ""
	 * StringUtils.replaceChars("abcba", 'b', 'y') = "aycya"
	 * StringUtils.replaceChars("abcba", 'z', 'y') = "abcba"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchChar 검색문자
	 * @param replaceChar 치환문자
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String replaceChars(String str, char searchChar, char replaceChar) {
		return org.apache.commons.lang.StringUtils.replaceChars(str, searchChar, replaceChar);
	}

	/**
	 * 문자열에서 검색문자열를 치환문자열로 모두 변환한다.<br />
	 *
	 * <pre>
	 * StringUtils.replaceChars(null, *, *)           = null
	 * StringUtils.replaceChars("", *, *)             = ""
	 * StringUtils.replaceChars("abc", null, *)       = "abc"
	 * StringUtils.replaceChars("abc", "", *)         = "abc"
	 * StringUtils.replaceChars("abc", "b", null)     = "ac"
	 * StringUtils.replaceChars("abc", "b", "")       = "ac"
	 * StringUtils.replaceChars("abcba", "bc", "yz")  = "ayzya"
	 * StringUtils.replaceChars("abcba", "bc", "y")   = "ayya"
	 * StringUtils.replaceChars("abcba", "bc", "yzx") = "ayzya"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param searchChars 검색문자열
	 * @param replaceChars 치환문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String replaceChars(String str, String searchChars, String replaceChars) {
		return org.apache.commons.lang.StringUtils.replaceChars(str, searchChars, replaceChars);
	}

	/**
	 * 문자열을 검색리스트 및 치환리스트의 각각 같은 인덱스의 문자열로 치환하여 반환한다.<br />
	 * 
	 * <pre>
	 *  StringUtils.replaceEach(null, *, *)        = null
	 *  StringUtils.replaceEach("", *, *)          = ""
	 *  StringUtils.replaceEach("aba", null, null) = "aba"
	 *  StringUtils.replaceEach("aba", new String[0], null) = "aba"
	 *  StringUtils.replaceEach("aba", null, new String[0]) = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, null)  = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""})  = "b"
	 *  StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"})  = "aba"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"})  = "wcte"
	 *  (example of how it does not repeat)
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"})  = "dcte"
	 * </pre>
	 * 
	 * @param text 문자열
	 * @param searchList 검색리스트
	 * @param replacementList 치환리스트
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String replaceEach(String text, String[] searchList, String[] replacementList) {
		return org.apache.commons.lang.StringUtils.replaceEach(text, searchList, replacementList);
	}

	/**
	 * 문자열을 검색리스트 및 치환리스트의 각각 같은 인덱스의 문자열로 치환하는 작업을<br /> 
	 * 검색리스트의 길이만큼 반복하여 반환한다.<br />
	 * 
	 * <pre>
	 *  StringUtils.replaceEachRepeatedly(null, *, *) = null
	 *  StringUtils.replaceEachRepeatedly("", *, *) = ""
	 *  StringUtils.replaceEachRepeatedly("aba", null, null) = "aba"
	 *  StringUtils.replaceEachRepeatedly("aba", new String[0], null) = "aba"
	 *  StringUtils.replaceEachRepeatedly("aba", null, new String[0]) = "aba"
	 *  StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, null) = "aba"
	 *  StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, new String[]{""}) = "b"
	 *  StringUtils.replaceEachRepeatedly("aba", new String[]{null}, new String[]{"a"}) = "aba"
	 *  StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}) = "wcte"
	 *  StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}) = "tcte"
	 *  StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}) = IllegalArgumentException
	 * </pre>
	 * 
	 * @param text 문자열
	 * @param searchList 검색리스트
	 * @param replacementList 치환리스트
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String replaceEachRepeatedly(String text, String[] searchList, String[] replacementList) {
		return org.apache.commons.lang.StringUtils.replaceEachRepeatedly(text, searchList, replacementList);
	}

	/**
	 * 입력된 문자열이 주어진 문자열과 일치하는 첫번째 문자열을 바꿔야할 문자열로 변경<br><br>
	 *
	 * StringUtils.replaceFirst("Anyframe Java Test Anyframe Java Test", "Anyframe", "Enterprise") = "Enterprise Java Test Anyframe Java Test"
	 *
	 * @param source 문자열
	 * @param regex 검색할 문자열
	 * @param replacement 변경할 문자열
	 * @return 검색된 문자열 중 첫번째 문자열을 변경한 문자열
	 * @see String#replaceFirst(String, String)
	 */
	public static String replaceFirst(String source, String regex, String replacement) {
		if (source == null) {
			return null;
		}
		
		return source.replaceFirst(regex, replacement);
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * HTML tag가 들어있는 문자열에 대해 unescape해준다.<br><br>
	 *
	 * StringUtils.replaceHtmlEscape("<html>Anyframe Java Test<html>") = "&lt;html&gt;Anyframe Java Test&lt;html&gt;"
	 *
	 * @param input HTML tag가 들어있는 문자열
	 * @return HTML tag를 unescape한 문자열
	 * @see HtmlUtils#htmlEscape(String)
	 */
	public static String replaceHtmlEscape(String input) {
		return HtmlUtils.htmlEscape(input);
	}

	/**
	 * 입력된 문자열이 주어진 문자열과 일치하는 마지막 문자열을 바꿔야할 문자열로 변경<br><br>
	 *
	 * StringUtils.replaceLast("Anyframe Java Test Anyframe Java Test", "Anyframe", "Enterprise") = "Anyframe Java Test Enterprise Java Test"
	 *
	 * @param source 문자열
	 * @param regex 검색할 문자열
	 * @param replacement 변경할 문자열
	 * @return 검색된 문자열 중 마지막 문자열을 변경한 문자열
	 */
	public static String replaceLast(String source, String regex, String replacement) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(source);
		
		if (!matcher.find()) {
			return source;
		}
		
		int lastMatchStart = 0;
		
		do {
			lastMatchStart = matcher.start();
		} while (matcher.find());
		
		matcher.find(lastMatchStart);
		
		StringBuffer sb = new StringBuffer(source.length());
		matcher.appendReplacement(sb, replacement);
		matcher.appendTail(sb);
		
		return sb.toString();
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 문자열 내에서 검색문자열을 검색후 치환문자열로 치환하여 반환한다.<br />
	 *
	 * <pre>
	 * StringUtils.replaceOnce(null, *, *)        = null
	 * StringUtils.replaceOnce("", *, *)          = ""
	 * StringUtils.replaceOnce("any", null, *)    = "any"
	 * StringUtils.replaceOnce("any", *, null)    = "any"
	 * StringUtils.replaceOnce("any", "", *)      = "any"
	 * StringUtils.replaceOnce("aba", "a", null)  = "aba"
	 * StringUtils.replaceOnce("aba", "a", "")    = "ba"
	 * StringUtils.replaceOnce("aba", "a", "z")   = "zba"
	 * </pre>
	 *
	 * @param text 문자열
	 * @param searchString 검색문자열
	 * @param replacement 치환문자열
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String replaceOnce(String text, String searchString, String replacement) {
		return org.apache.commons.lang.StringUtils.replaceOnce(text, searchString, replacement);
	}

	/**
	 * 한 String 객체 안에서 특정 패턴(old)을 다른 패턴(new)으로 변환한다.<br>
	 * 등장 패턴의 위치는 좌측에서부터 계산하고 겹치지 않는 형태로 계산한다.<br>
	 * 따라서, 변환된 후에도 old 패턴은 남아 있을 수 있다.<br><br>
	 * 
	 * StringUtils.replacePattern("abaa", "aba", "bab") = "baba"
	 *
	 * @param text
	 *            text to search and replace in, may be null
	 * @param searchString
	 *            the String to search for, may be null
	 * @param replacement
	 *            the String to replace it with, may be null
	 * @return the text with any replacements processed, null if null String
	 *         input
	 */
	public static String replacePattern(String text, String searchString, String replacement) {
		if (text == null) {
			return null;
		}
		
		if (searchString == null || replacement == null) {
			return text;
		}
		
		StringBuffer sbuf = new StringBuffer();
		int pos = 0;
		int index = text.indexOf(searchString);
		int patLen = searchString.length();
		
		for (; index >= 0; index = text.indexOf(searchString, pos)) {
			sbuf.append(text.substring(pos, index));
			sbuf.append(replacement);
			pos = index + patLen;
		}
		
		sbuf.append(text.substring(pos));
		
		return sbuf.toString();
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 입력된 문자열의 순서를 반대로 바꿈<br><br>
	 *
	 * StringUtils.reverse("Anyframe Java Test") = "tseT avaJ emarfynA"
	 *
	 * @param str 문자열
	 * @return 순서가 반대로 바뀐 문자열
	 */
	public static String reverse(String str) {
		if (str == null) {
			return null;
		}
		
		return new StringBuilder(str).reverse().toString();
	}

	/**
	 * 구분자를 구분으로 문자열을 나눈 후 나눠진 단어들을 역순으로 바꾼다.<br />
	 *
	 * <pre>
	 * StringUtils.reverseDelimited(null, *)      = null
	 * StringUtils.reverseDelimited("", *)        = ""
	 * StringUtils.reverseDelimited("a.b.c", 'x') = "a.b.c"
	 * StringUtils.reverseDelimited("a.b.c", ".") = "c.b.a"
	 * </pre>
	 *
	 * @param str 문자열
	 * @param separatorChar 구분자
	 * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
	 */
	public static String reverseDelimited(String str, char separatorChar) {
		return org.apache.commons.lang.StringUtils.reverseDelimited(str, separatorChar);
	}
	
	// Mid
	//-----------------------------------------------------------------------
	/**
	 * 주어진 String 객체에 대해서 주어진 길이만큼 오른쪽 부분을 떼어 반환한다.<br>
	 * 주어진 길이보다 주어진 String의 길이가 작을 경우에는 주어진 String을 그대로 반환한다.<br>
	 * "..."을 붙이지 않는 점을 제외하고는 splitTail()와 동일하다.<br><br>
	 *
	 * StringUtils.right("1234567", 3) = "567"
	 *
	 * @param str
	 *            the String to get the rightmost characters from, may be null
	 * @param len
	 *            the length of the required String
	 * @return the rightmost characters, null if null String input
	 */
	public static String right(String str, int len) {
		if (str == null) {
			return null;
		} else if (len <= 0 || str.length() <= len) {
			return str;
		} else {
			return str.substring(str.length() - len);
		}
	}

	/**
	 * 해당하는 문자열에 대해서 입력된 길이만큼 부족한 길이를 오른쪽부터 공백으로 채워넣는다.<br><br>
	 *
	 * StringUtils.rightPad("Anyframe", 12) = "Anyframe    "
	 *
	 * @param str 문자열
	 * @param size 공백이 채워진 문자열의 전체 길이
	 * @return 부족한 길이만큼 오른쪽부터 공백이 채워진 문자열
	 */
	public static String rightPad(String str, int size) {
		return rightPad(str, size, ' ');
	}

	/**
	 * 해당하는 문자열에 대해서 입력된 길이만큼 부족한 길이를 오른쪽부터 지정된 문자로 채워넣는다.<br><br>
	 *
	 * StringUtils.rightPad("Anyframe", 12, 'a') = "Anyframeaaaa"
	 *
	 * @param str 문자열
	 * @param size 캐릭터가 채워진 문자열의 전체 길이
	 * @param padChar 채워넣을 캐릭터
	 * @return 부족한 길이만큼 오른쪽부터 캐릭터가 채워진 문자열
	 */
	public static String rightPad(String str, int size, char padChar) {
		return padChar(str, size, padChar, false);
	}

	/**
	 * 해당하는 문자열에 대해서 입력된 길이만큼 부족한 길이를 오른쪽부터 지정된 문자열로 채워넣는다.<br><br>
	 *
	 * StringUtils.rightPad("Anyframe", 12, "Java") = "AnyframeJava"
	 *
	 * @param str 문자열
	 * @param size 문자열이 채워진 문자열의 전체 길이
	 * @param padStr 채워넣을 문자열
	 * @return 부족한 길이만큼 오른쪽부터 문자열이 채워진 문자열
	 */
	public static String rightPad(String str, int size, String padStr) {
		return padString(str, size, padStr, false);
	}

	/**
	 * 문자열의 오른쪽의 공백 문자열 제거<br><br>
	 *
	 * StringUtils.rightTrim("Anyframe Java Test ") = "Anyframe Java Test"
	 *
	 * @param str 문자열
	 * @return 오른쪽 공백을 제거한 문자열
	 * @see org.springframework.util.StringUtils#trimTrailingWhitespace(String)
	 */
	public static String rightTrim(String str) {
		return org.springframework.util.StringUtils.trimTrailingWhitespace(str);
	}

	/**
     * 공백문자를 구분자로 사용하여 문자열을 분리한다.<br />
     *
     * <pre>
     * StringUtils.split(null)       = null
     * StringUtils.split("")         = []
     * StringUtils.split("abc def")  = ["abc", "def"]
     * StringUtils.split("abc  def") = ["abc", "def"]
     * StringUtils.split(" abc ")    = ["abc"]
     * </pre>
     *
     * @param str 문자열
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] split(String str) {
    	return org.apache.commons.lang.StringUtils.split(str);
    }

	/**
	 * 주어진 String에 대해서 separator(char)를 이용하여 tokenize한 후 String[]로 뽑아낸다.<br>
	 * 연속된 separator 사이는 token이 되지 않는다.<br>
	 * 주어진 String이 null일 경우, null을 return한다.<br><br>
	 *
	 * StringUtils.split("aaVbbVcc", 'V') = {"aa", "bb", "cc"}
	 *
	 * @param str
	 *            the String to parse
	 * @param separator
	 *            the character used as the delimiter
	 * @return an array of parsed Strings
	 */
	public static String[] split(String str, char separator) {
		StringBuffer tempStringBuffer = new StringBuffer();
		tempStringBuffer.append(separator);
		
		return tokenizeToStringArray(str, tempStringBuffer.toString(), false, false);
	}

	/**
     * 문자열을 구분자로 사용하여 분리한다.<br />
     *
     * <pre>
     * StringUtils.split(null, *)         = null
     * StringUtils.split("", *)           = []
     * StringUtils.split("abc def", null) = ["abc", "def"]
     * StringUtils.split("abc def", " ")  = ["abc", "def"]
     * StringUtils.split("abc  def", " ") = ["abc", "def"]
     * StringUtils.split("ab:cd:ef", ":") = ["ab", "cd", "ef"]
     * </pre>
     *
     * @param str 문자열
     * @param separatorChars 구분자
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] split(String str, String separatorChars) {
    	return org.apache.commons.lang.StringUtils.split(str, separatorChars);
    }
	
	// Mid
	//-----------------------------------------------------------------------
	/**
     * 문자열을 구분자를 사용하여 최대크기 만큼의 배열로 분리한다.<br />
     *
     * <pre>
     * StringUtils.split(null, *, *)            = null
     * StringUtils.split("", *, *)              = []
     * StringUtils.split("ab de fg", null, 0)   = ["ab", "cd", "ef"]
     * StringUtils.split("ab   de fg", null, 0) = ["ab", "cd", "ef"]
     * StringUtils.split("ab:cd:ef", ":", 0)    = ["ab", "cd", "ef"]
     * StringUtils.split("ab:cd:ef", ":", 2)    = ["ab", "cd:ef"]
     * </pre>
     *
     * @param str 문자열
     * @param separatorChars 구분자
     * @param max 최대크기
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] split(String str, String separatorChars, int max) {
    	return org.apache.commons.lang.StringUtils.split(str, separatorChars, max);
    }

	/**
     * 문자열을 <code>java.lang.Character.getType(char)</code>의 리턴타입으로 구분하여 <br />
     * 배열로 분할한다.영문대소문자타입구분<br />
     * 
     * <pre>
     * StringUtils.splitByCharacterType(null)         = null
     * StringUtils.splitByCharacterType("")           = []
     * StringUtils.splitByCharacterType("ab de fg")   = ["ab", " ", "de", " ", "fg"]
     * StringUtils.splitByCharacterType("ab   de fg") = ["ab", "   ", "de", " ", "fg"]
     * StringUtils.splitByCharacterType("ab:cd:ef")   = ["ab", ":", "cd", ":", "ef"]
     * StringUtils.splitByCharacterType("number5")    = ["number", "5"]
     * StringUtils.splitByCharacterType("fooBar")     = ["foo", "B", "ar"]
     * StringUtils.splitByCharacterType("foo200Bar")  = ["foo", "200", "B", "ar"]
     * StringUtils.splitByCharacterType("ASFRules")   = ["ASFR", "ules"]
     * </pre>
     * @param str 문자열
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] splitByCharacterType(String str) {
    	return org.apache.commons.lang.StringUtils.splitByCharacterType(str);
    }

	// Mid
	//-----------------------------------------------------------------------
	/**
     * 문자열을 <code>java.lang.Character.getType(char)</code>의 리턴타입으로 구분하여 <br />
     * 배열로 분할한다.영문대소문자타입구분하지않음<br />
     * 
     * <pre>
     * StringUtils.splitByCharacterTypeCamelCase(null)         = null
     * StringUtils.splitByCharacterTypeCamelCase("")           = []
     * StringUtils.splitByCharacterTypeCamelCase("ab de fg")   = ["ab", " ", "de", " ", "fg"]
     * StringUtils.splitByCharacterTypeCamelCase("ab   de fg") = ["ab", "   ", "de", " ", "fg"]
     * StringUtils.splitByCharacterTypeCamelCase("ab:cd:ef")   = ["ab", ":", "cd", ":", "ef"]
     * StringUtils.splitByCharacterTypeCamelCase("number5")    = ["number", "5"]
     * StringUtils.splitByCharacterTypeCamelCase("fooBar")     = ["foo", "Bar"]
     * StringUtils.splitByCharacterTypeCamelCase("foo200Bar")  = ["foo", "200", "Bar"]
     * StringUtils.splitByCharacterTypeCamelCase("ASFRules")   = ["ASF", "Rules"]
     * </pre>
     * @param str 문자열
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] splitByCharacterTypeCamelCase(String str) {
    	return org.apache.commons.lang.StringUtils.splitByCharacterTypeCamelCase(str);
    }

	/**
     * 문자열을 구분자를 사용하여 배열로 분리한다.<br />
     *
     * <pre>
     * StringUtils.splitByWholeSeparator(null, *)               = null
     * StringUtils.splitByWholeSeparator("", *)                 = []
     * StringUtils.splitByWholeSeparator("ab de fg", null)      = ["ab", "de", "fg"]
     * StringUtils.splitByWholeSeparator("ab   de fg", null)    = ["ab", "de", "fg"]
     * StringUtils.splitByWholeSeparator("ab:cd:ef", ":")       = ["ab", "cd", "ef"]
     * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-") = ["ab", "cd", "ef"]
     * </pre>
     *
     * @param str 문자열
     * @param separator 구분자
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] splitByWholeSeparator(String str, String separator) {
    	return org.apache.commons.lang.StringUtils.splitByWholeSeparator(str, separator);
    }

	/**
     * 문자열을 구분자를 사용하여 최대크기 만큼의 배열로 분리한다.<br />
     *
     * <pre>
     * StringUtils.splitByWholeSeparator(null, *, *)               = null
     * StringUtils.splitByWholeSeparator("", *, *)                 = []
     * StringUtils.splitByWholeSeparator("ab de fg", null, 0)      = ["ab", "de", "fg"]
     * StringUtils.splitByWholeSeparator("ab   de fg", null, 0)    = ["ab", "de", "fg"]
     * StringUtils.splitByWholeSeparator("ab:cd:ef", ":", 2)       = ["ab", "cd:ef"]
     * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 5) = ["ab", "cd", "ef"]
     * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2) = ["ab", "cd-!-ef"]
     * </pre>
     *
     * @param str 문자열
     * @param separator 구분자
     * @param max 최대크기
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
	public static String[] splitByWholeSeparator(String str, String separator, int max) {
    	return org.apache.commons.lang.StringUtils.splitByWholeSeparator(str, separator, max);
    }

	/**
     * 문자열을 구분자를 사용하여 배열로 분리한다.<br />
     *
     * <pre>
     * StringUtils.splitByWholeSeparatorPreserveAllTokens(null, *)               = null
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("", *)                 = []
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab de fg", null)      = ["ab", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   de fg", null)    = ["ab", "", "", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:cd:ef", ":")       = ["ab", "cd", "ef"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-") = ["ab", "cd", "ef"]
     * </pre>
     *
     * @param str 문자열
     * @param separator 구분자
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] splitByWholeSeparatorPreserveAllTokens(String str, String separator) {
    	return org.apache.commons.lang.StringUtils.splitByWholeSeparatorPreserveAllTokens(str, separator);
    }

	/**
     * 문자열을 구분자를 사용하여 최대크기 만큼의 배열로 분리한다.<br />
     *
     * <pre>
     * StringUtils.splitByWholeSeparatorPreserveAllTokens(null, *, *)               = null
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("", *, *)                 = []
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab de fg", null, 0)      = ["ab", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   de fg", null, 0)    = ["ab", "", "", "de", "fg"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:cd:ef", ":", 2)       = ["ab", "cd:ef"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-", 5) = ["ab", "cd", "ef"]
     * StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-", 2) = ["ab", "cd-!-ef"]
     * </pre>
     *
     * @param str 문자열
     * @param separator 구분자
     * @param max 최대크기
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] splitByWholeSeparatorPreserveAllTokens(String str, String separator, int max) {
    	return org.apache.commons.lang.StringUtils.splitByWholeSeparatorPreserveAllTokens(str, separator, max);
    }

	/**
	 * 해당문자열의 사이즈 만큼 앞에서부터 자른다.<br><br>
	 *
	 * StringUtils.splitHead("Anyframe Java Test", 3) = "Any"
	 *
	 * @param str 문자열
	 * @param size 문자열을 자를 길이
	 * @return 길이만큼 앞에서부터 자른 문자열
	 */
	public static String splitHead(String str, int size) {
		if (str == null) {
			return "";
		}
		
		if (str.length() > size) {
			str = str.substring(0, size);
		}
		
		return str;
	}

	/**
	 * 주어진 String 객체에 대해서 주어진 길이만큼 앞부분을 떼어 반환한다.<br>
	 * 주어진 길이보다 주어진 String의 길이가 작을 경우에는 주어진 String을 그대로 반환한다.<br>
	 * 떼어낸 앞부분의 뒤에 "..."을 붙여서 반환한다.<br><br>
	 *
	 * StringUtils.splitHead("12345678", 3) = "123..."
	 *
	 * @param str
	 *            the String to get the leftmost characters from, may be null
	 * @param len
	 *            the length of the required String
	 * @return the leftmost characters with ellipsis, null if null String input
	 */
	public static String splitHeadWithEllipsis(String str, int len) {
		if (str == null) {
			return null;
		} else if (len <= 0 || str.length() <= len) {
			return str;
		} else {
			return str.substring(0, len) + "...";
		}
	}

	/**
     * 공백문자를 구분자로 사용하여 문자열을 분리한다.<br />
     *
     * <pre>
     * StringUtils.splitPreserveAllTokens(null)       = null
     * StringUtils.splitPreserveAllTokens("")         = []
     * StringUtils.splitPreserveAllTokens("abc def")  = ["abc", "def"]
     * StringUtils.splitPreserveAllTokens("abc  def") = ["abc", "", "def"]
     * StringUtils.splitPreserveAllTokens(" abc ")    = ["", "abc", ""]
     * </pre>
     *
     * @param str 문자열
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] splitPreserveAllTokens(String str) {
    	return org.apache.commons.lang.StringUtils.splitPreserveAllTokens(str);
    }

	/**
     * 문자열을 구분자로 사용하여 배열로 분리한다.<br />
     *
     * <pre>
     * StringUtils.splitPreserveAllTokens(null, *)         = null
     * StringUtils.splitPreserveAllTokens("", *)           = []
     * StringUtils.splitPreserveAllTokens("a.b.c", '.')    = ["a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a..b.c", '.')   = ["a", "", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a:b:c", '.')    = ["a:b:c"]
     * StringUtils.splitPreserveAllTokens("a\tb\nc", null) = ["a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a b c", ' ')    = ["a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("a b c ", ' ')   = ["a", "b", "c", ""]
     * StringUtils.splitPreserveAllTokens("a b c  ", ' ')   = ["a", "b", "c", "", ""]
     * StringUtils.splitPreserveAllTokens(" a b c", ' ')   = ["", a", "b", "c"]
     * StringUtils.splitPreserveAllTokens("  a b c", ' ')  = ["", "", a", "b", "c"]
     * StringUtils.splitPreserveAllTokens(" a b c ", ' ')  = ["", a", "b", "c", ""]
     * </pre>
     *
     * @param str 문자열
     * @param separatorChar 구분자
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] splitPreserveAllTokens(String str, char separatorChar) {
    	return org.apache.commons.lang.StringUtils.splitPreserveAllTokens(str, separatorChar);
    }

	/**
     * 문자열을 구분자로 사용하여 배열로 분리한다.<br />
     *
     * <pre>
     * StringUtils.splitPreserveAllTokens(null, *)           = null
     * StringUtils.splitPreserveAllTokens("", *)             = []
     * StringUtils.splitPreserveAllTokens("abc def", null)   = ["abc", "def"]
     * StringUtils.splitPreserveAllTokens("abc def", " ")    = ["abc", "def"]
     * StringUtils.splitPreserveAllTokens("abc  def", " ")   = ["abc", "", def"]
     * StringUtils.splitPreserveAllTokens("ab:cd:ef", ":")   = ["ab", "cd", "ef"]
     * StringUtils.splitPreserveAllTokens("ab:cd:ef:", ":")  = ["ab", "cd", "ef", ""]
     * StringUtils.splitPreserveAllTokens("ab:cd:ef::", ":") = ["ab", "cd", "ef", "", ""]
     * StringUtils.splitPreserveAllTokens("ab::cd:ef", ":")  = ["ab", "", cd", "ef"]
     * StringUtils.splitPreserveAllTokens(":cd:ef", ":")     = ["", cd", "ef"]
     * StringUtils.splitPreserveAllTokens("::cd:ef", ":")    = ["", "", cd", "ef"]
     * StringUtils.splitPreserveAllTokens(":cd:ef:", ":")    = ["", cd", "ef", ""]
     * </pre>
     *
     * @param str 문자열
     * @param separatorChars 구분자
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] splitPreserveAllTokens(String str, String separatorChars) {
    	return org.apache.commons.lang.StringUtils.splitPreserveAllTokens(str, separatorChars);
    }

	/**
     * 문자열을 구분자를 사용하여 최대크기 만큼의 배열로 분리한다.<br />
     *
     * <pre>
     * StringUtils.splitPreserveAllTokens(null, *, *)            = null
     * StringUtils.splitPreserveAllTokens("", *, *)              = []
     * StringUtils.splitPreserveAllTokens("ab cd ef", null, 0)   = ["ab", "cd", "ef"]
     * StringUtils.splitPreserveAllTokens("ab   cd ef", null, 0) = [ab, , , cd, ef]
     * StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 0)    = ["ab", "cd", "ef"]
     * StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 2)    = ["ab", "cd:ef"]
     * StringUtils.splitPreserveAllTokens("ab   de fg", null, 2) = ["ab", "  de fg"]
     * StringUtils.splitPreserveAllTokens("ab   de fg", null, 3) = ["ab", "", " de fg"]
     * StringUtils.splitPreserveAllTokens("ab   de fg", null, 4) = ["ab", "", "", "de fg"]
     * </pre>
     *
     * @param str 문자열
     * @param separatorChars 구분자
     * @param max 최대크기
     * @return 결과문자배열, 문자열이 null일경우 <code>null</code>
     */
    public static String[] splitPreserveAllTokens(String str, String separatorChars, int max) {
    	return org.apache.commons.lang.StringUtils.splitPreserveAllTokens(str, separatorChars, max);
    }

	/**
	 * 해당문자열의 사이즈 만큼 뒤에서부터 자른다.<br><br>
	 *
	 * StringUtils.splitTail("Anyframe Java Test", 3) = "est"
	 *
	 * @param str 문자열
	 * @param size 문자열을 자를 길이
	 * @return 길이만큼 뒤에서부터 자른 문자열
	 */
	public static String splitTail(String str, int size) {
		if (str == null) {
			return "";
		}
		
		if (str.length() > size) {
			str = str.substring(str.length() - size);
		}
		
		return str;
	}

    // Mid
	//-----------------------------------------------------------------------
	/**
	 * 주어진 String 객체에 대해서 주어진 길이만큼 뒷부분을 떼어 반환한다.<br>
	 * 주어진 길이보다 주어진 String의 길이가 작을 경우에는 주어진 String을 그대로 반환한다.<br>
	 * 떼어낸 뒷부분의 앞에 "..."을 붙여서 반환한다.<br><br>
	 *
	 * StringUtils.splitTail("12345678", 3) = "...678"
	 *
	 * @param str
	 *            the String to get the rightmost characters from, may be null
	 * @param len
	 *            the length of the required String
	 * @return the rightmost characters with ellipsis, null if null String input
	 */
	public static String splitTailWithEllipsis(String str, int len) {
		if (str == null) {
			return null;
		} else if (len <= 0 || str.length() <= len) {
			return str;
		} else {
			return "..." + str.substring(str.length() - len);
		}
	}

    /**
     * 문자열이 검색문자로 시작되는지 확인한다.<br />
     *
     * <pre>
     * StringUtils.startsWith(null, null)      = true
     * StringUtils.startsWith(null, "abc")     = false
     * StringUtils.startsWith("abcdef", null)  = false
     * StringUtils.startsWith("abcdef", "abc") = true
     * StringUtils.startsWith("ABCDEF", "abc") = false
     * </pre>
     *
     * @param str 문자열
     * @param prefix 검색문자열
     * @return 문자열이 검색문자로 시작되는경우와 문자열 양쪽모두 <code>null</code>인경우 <code>true</code> 
     */
    public static boolean startsWith(String str, String prefix) {
    	return org.apache.commons.lang.StringUtils.startsWith(str, prefix);
    }
    
    /**
     * 문자열이 검색문자로 대소문자 구분없이 시작되는지 확인한다.<br />
     *
     * <pre>
     * StringUtils.startsWithIgnoreCase(null, null)      = true
     * StringUtils.startsWithIgnoreCase(null, "abc")     = false
     * StringUtils.startsWithIgnoreCase("abcdef", null)  = false
     * StringUtils.startsWithIgnoreCase("abcdef", "abc") = true
     * StringUtils.startsWithIgnoreCase("ABCDEF", "abc") = true
     * </pre>
     *
     * @param str 문자열
     * @param prefix 검색문자
     * @return 문자열이 검색문자로 대소문자 구분없이 시작되는경우와 문자열 양쪽모두
     *  <code>null</code>인경우 <code>true</code> 
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
    	return org.apache.commons.lang.StringUtils.startsWithIgnoreCase(str, prefix);
    }

    /**
	 * 문자열을 숫자로 변환하여 리턴한다.<br><br>
	 *
	 * StringUtils.string2integer("14") = 14
	 *
	 * @param str
	 *            string representation of a number
	 * @return integer integer type of string
	 */
	public static int string2integer(String str) {
		int ret = Integer.parseInt(str.trim());

		return ret;
	}

    /**
	 * 문자열을 BigDecimal로 변환하여 리턴한다.<br><br>
	 * 
	 * StringUtils.stringToBigDecimal("14") = 14
	 *
	 * @param str
	 *            the String value to convert
	 * @return the converted BigDecimal
	 */
	public static BigDecimal stringToBigDecimal(String str) {
		if ("".equals(rightTrim(str))) {
			return new BigDecimal(0);
		} else {
			return new BigDecimal(str);
		}
	}

    /**
	 * 문자열의 내용 중 정해진 위치의 일부 문자열을 BigDecimal로 변환하여 리턴한다.<br><br>
	 *
	 * StringUtils.stringToBigDecimal("123456", 0, 2) = 12
	 *
	 * @param str 문자열
	 * @param pos 시작위치
	 * @param len 길이
	 * @return 변환된 BigDecimal 값
	 */
	public static BigDecimal stringToBigDecimal(String str, int pos, int len) {
		if ("".equals(rightTrim(str))) {
			return new BigDecimal(0);
		} else if (str.length() < pos + len) {
			return stringToBigDecimal(leftPad(str, pos + len, "0"));
		} else {
			return stringToBigDecimal(str.substring(pos, pos + len));
		}
	}

    /**
	 * 문자열을 받아 해당하는 hex 코드로 만들어 반환한다. (유니코드)<br><br>
	 *
	 * StringUtils.stringToHex("123") = "003100320033"
	 *
	 * @param str 문자열
	 * @return 변환된 hex 문자열
	 */
	public static String stringToHex(String str) {
		String inStr = str;

		char[] inChar = inStr.toCharArray();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < inChar.length; i++) {
			String hex = Integer.toHexString((int) inChar[i]);
			
			if (hex.length() == 2) {
				hex = "00" + hex;
			}
			
			sb.append(hex);
		}
		
		return sb.toString();
	}

    /**
	 * 문자열을 정수형으로 변환하여 리턴한다.<br><br>
	 *
	 * StringUtils.stringToNumn("123") = 123
	 *
	 * @param str 문자열
	 * @return 정수형으로 변환된 값
	 */
	public static int stringToNumn(String str) {
		if ("".equals(rightTrim(str))) {
			return 0;
		} else {
			return Integer.parseInt(str);
		}
	}

    /**
	 * 문자열의 내용 중 정해진 위치의 일부 문자열을 정수형으로 변환하여 리턴한다.<br><br>
	 *
	 * StringUtils.stringToNumn("123456789", 5, 3) = 678
	 *
	 * @param str 문자열
	 * @param pos 시작위치
	 * @param len 길이
	 * @return 문자열의 일부가 정수형으로 변환된 값
	 */
	public static int stringToNumn(String str, int pos, int len) {
		if ("".equals(rightTrim(str))) {
			return 0;
		} else if (str.length() < pos + len) {
			return stringToNumn(leftPad(str, pos + len, "0"));
		} else {
			return stringToNumn(str.substring(pos, pos + len));
		}
	}

    /**
     * 문자열의 시작과 끝에서 스페이스을 제거한다.<br />
     *
     * <pre>
     * StringUtils.strip(null)     = null
     * StringUtils.strip("")       = ""
     * StringUtils.strip("   ")    = ""
     * StringUtils.strip("abc")    = "abc"
     * StringUtils.strip("  abc")  = "abc"
     * StringUtils.strip("abc  ")  = "abc"
     * StringUtils.strip(" abc ")  = "abc"
     * StringUtils.strip(" ab c ") = "ab c"
     * </pre>
     *
     * @param str 문자열
     * @return null 문자열 일경우 null
     * 		   "" 일경우 ""
     */
    public static String strip(String str) {
    	return org.apache.commons.lang.StringUtils.strip(str);
    }

    /**
     * 문자열 좌우에 stripChars에 존재하는 문자를 제거한다.<br />
     *
     * <pre>
     * StringUtils.strip(null, *)          = null
     * StringUtils.strip("", *)            = ""
     * StringUtils.strip("abc", null)      = "abc"
     * StringUtils.strip("  abc", null)    = "abc"
     * StringUtils.strip("abc  ", null)    = "abc"
     * StringUtils.strip(" abc ", null)    = "abc"
     * StringUtils.strip("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str 문자열
     * @param stripChars 공백으로 취급 하는 문자열
     * @return null 문자열 일경우 null
     * 		   "" 일경우 ""
     */
    public static String strip(String str, String stripChars) {
    	return org.apache.commons.lang.StringUtils.strip(str, stripChars);
    }

    /**
     * 배열에 있는 모든 문자열들을 앞뒤에 있는 스페이스를 제거한다.<br />
     *
     * <pre>
     * StringUtils.stripAll(null)             = null
     * StringUtils.stripAll([])               = []
     * StringUtils.stripAll(["abc", "  abc"]) = ["abc", "abc"]
     * StringUtils.stripAll(["abc  ", null])  = ["abc", null]
     * </pre>
     *
     * @param strs 문자열
     * @return strip결과 문자열
     */
    public static String[] stripAll(String[] strs) {
    	return org.apache.commons.lang.StringUtils.stripAll(strs);
    }
    
    // Mid
    //-----------------------------------------------------------------------
    /**
     * 배열에 있는 모든 문자열을 대상으로 앞뒤에 존재하는 제거문자들을 제거한다.<br />
     *
     * <pre>
     * StringUtils.stripAll(null, *)                = null
     * StringUtils.stripAll([], *)                  = []
     * StringUtils.stripAll(["abc", "  abc"], null) = ["abc", "abc"]
     * StringUtils.stripAll(["abc  ", null], null)  = ["abc", null]
     * StringUtils.stripAll(["abc  ", null], "yz")  = ["abc  ", null]
     * StringUtils.stripAll(["yabcz", null], "yz")  = ["abc", null]
     * </pre>
     *
     * @param strs 문자열
     * @param stripChars 제거문자들
     * @return strip 결과 문자열
     */
    public static String[] stripAll(String[] strs, String stripChars) {
    	return org.apache.commons.lang.StringUtils.stripAll(strs, stripChars);
    }

    /**
     * 문자열을 뒤에서부터 제거문자들 하나식 제거한다.<br />
     *
     * <pre>
     * StringUtils.stripEnd(null, *)          = null
     * StringUtils.stripEnd("", *)            = ""
     * StringUtils.stripEnd("abc", "")        = "abc"
     * StringUtils.stripEnd("abc", null)      = "abc"
     * StringUtils.stripEnd("  abc", null)    = "  abc"
     * StringUtils.stripEnd("abc  ", null)    = "abc"
     * StringUtils.stripEnd(" abc ", null)    = " abc"
     * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
     * StringUtils.stripEnd("120.00", ".0")   = "12"
     * </pre>
     *
     * @param str 문자열
     * @param stripChars 공백으로 취급 하는 문자열
     * @return null 문자열 일경우 null
     * 		   "" 일경우 ""
     */
    public static String stripEnd(String str, String stripChars) {
    	return org.apache.commons.lang.StringUtils.stripEnd(str, stripChars);
    }

    /**
     * 문자열을 앞에서부터 제거문자들 하나식 제거한다.<br />
     *
     * <pre>
     * StringUtils.stripStart(null, *)          = null
     * StringUtils.stripStart("", *)            = ""
     * StringUtils.stripStart("abc", "")        = "abc"
     * StringUtils.stripStart("abc", null)      = "abc"
     * StringUtils.stripStart("  abc", null)    = "abc"
     * StringUtils.stripStart("abc  ", null)    = "abc  "
     * StringUtils.stripStart(" abc ", null)    = "abc "
     * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str 문자열
     * @param stripChars  제거문자들
     * @return null 문자열 일경우 null
     * 		   "" 일경우 ""
     */
    public static String stripStart(String str, String stripChars) {
    	return org.apache.commons.lang.StringUtils.stripStart(str, stripChars);
    }

    /**
     * 처리문자가 NULL일경우 공백을 반환, 아닐경우 좌우에 있는<br />
     * 스페이스를 제거한다.<br />  
     *
     * <pre>
     * StringUtils.stripToEmpty(null)     = ""
     * StringUtils.stripToEmpty("")       = ""
     * StringUtils.stripToEmpty("   ")    = ""
     * StringUtils.stripToEmpty("abc")    = "abc"
     * StringUtils.stripToEmpty("  abc")  = "abc"
     * StringUtils.stripToEmpty("abc  ")  = "abc"
     * StringUtils.stripToEmpty(" abc ")  = "abc"
     * StringUtils.stripToEmpty(" ab c ") = "ab c"
     * </pre>
     *
     * @param str 문자열
     * @return null 문자열 또는 "" 일경우 ""
     */
    public static String stripToEmpty(String str) {
    	return org.apache.commons.lang.StringUtils.stripToEmpty(str);
    }

    /**
     * 문자열 좌우에 있는 스페이스를 제거한다.<br />
     *
     * <pre>
     * StringUtils.stripToNull(null)     = null
     * StringUtils.stripToNull("")       = null
     * StringUtils.stripToNull("   ")    = null
     * StringUtils.stripToNull("abc")    = "abc"
     * StringUtils.stripToNull("  abc")  = "abc"
     * StringUtils.stripToNull("abc  ")  = "abc"
     * StringUtils.stripToNull(" abc ")  = "abc"
     * StringUtils.stripToNull(" ab c ") = "ab c"
     * </pre>
     *
     * @param str 문자열
     * @return null 문자열 또는 "" 일경우 null
     */
    public static String stripToNull(String str) {
    	return org.apache.commons.lang.StringUtils.stripToNull(str);
    }

    /**
     * 문자열 내에서 문자열을 취득한다.<br />
     * 
     * <p> 
     * 문자열의 마이너스도 사용가능한 <code>n</code>번째에서 부터 마지막까지의 문자열을 취득.
     * </p>
     * 
     * <p>
     * 문자열이 <code>null</code>일경우 <code>null</code>을 반환.
     * 문자열이 공백일경우 공백을 반환.
     * </p>
     *
     * <pre>
     * StringUtils.substring(null, *)   = null
     * StringUtils.substring("", *)     = ""
     * StringUtils.substring("abc", 0)  = "abc"
     * StringUtils.substring("abc", 2)  = "c"
     * StringUtils.substring("abc", 4)  = "
     * "
     * StringUtils.substring("abc", -2) = "bc"
     * StringUtils.substring("abc", -4) = "abc"
     * </pre>
     *
     * @param str 문자열
     * @param start 시작점
     * @return 시작점으로부터의 문자열, 문자열이 null일경우 <code>null</code>
     */
    public static String substring(String str, int start) {
    	return org.apache.commons.lang.StringUtils.substring(str, start);
    }

    /**
     * 문자열 내에서 문자열을 취득한다.<br />
     * 
     * <p> 
     * 문자열의 마이너스도 사용가능한 <code>n</code>번째에서 부터 마지막까지의 문자열을 취득.
     * </p>
     * 
     * <p>
     * 문자열이 <code>null</code>일경우 <code>null</code>을 반환.
     * 문자열이 공백일경우 공백을 반환.
     * </p>
     *
     * <pre>
     * StringUtils.substring(null, *, *)    = null
     * StringUtils.substring("", * ,  *)    = "";
     * StringUtils.substring("abc", 0, 2)   = "ab"
     * StringUtils.substring("abc", 2, 0)   = ""
     * StringUtils.substring("abc", 2, 4)   = "c"
     * StringUtils.substring("abc", 4, 6)   = ""
     * StringUtils.substring("abc", 2, 2)   = ""
     * StringUtils.substring("abc", -2, -1) = "b"
     * StringUtils.substring("abc", -4, 2)  = "ab"
     * </pre>
     *
     * @param str 문자열
     * @param start 시작점
     * @param end 종료점
     * @return 시작점으로부터 종료점까지의 문자열, 문자열이 null일경우 <code>null</code>
     */
    public static String substring(String str, int start, int end) {
    	return org.apache.commons.lang.StringUtils.substring(str, start, end);
    }

    /**
     * 문자열의 처음 SEPARATOR부분부터 문자열을 구한다.<br />
     *
     * <pre>
     * StringUtils.substringAfter(null, *)      = null
     * StringUtils.substringAfter("", *)        = ""
     * StringUtils.substringAfter(*, null)      = ""
     * StringUtils.substringAfter("abc", "a")   = "bc"
     * StringUtils.substringAfter("abcba", "b") = "cba"
     * StringUtils.substringAfter("abc", "c")   = ""
     * StringUtils.substringAfter("abc", "d")   = ""
     * StringUtils.substringAfter("abc", "")    = "abc"
     * </pre>
     *
     * @param str 문자열
     * @param separator SEPARATOR
     * @return 결과문자열, 문자열이 null일경우 <code>null</code>
     */
    public static String substringAfter(String str, String separator) {
    	return org.apache.commons.lang.StringUtils.substringAfter(str, separator);
    }

    /**
     * 문자열의 마지막 separator부분부터 문자열을 구한다.<br />
     *
     * <pre>
     * StringUtils.substringAfterLast(null, *)      = null
     * StringUtils.substringAfterLast("", *)        = ""
     * StringUtils.substringAfterLast(*, "")        = ""
     * StringUtils.substringAfterLast(*, null)      = ""
     * StringUtils.substringAfterLast("abc", "a")   = "bc"
     * StringUtils.substringAfterLast("abcba", "b") = "a"
     * StringUtils.substringAfterLast("abc", "c")   = ""
     * StringUtils.substringAfterLast("a", "a")     = ""
     * StringUtils.substringAfterLast("a", "z")     = ""
     * </pre>
     *
     * @param str 문자열
     * @param separator SEPARATOR
     * @return 결과문자열, 문자열이 null일경우 <code>null</code>
     */
    public static String substringAfterLast(String str, String separator) {
    	return org.apache.commons.lang.StringUtils.substringAfterLast(str, separator);
    }

    // SubStringAfter/SubStringBefore
    //-----------------------------------------------------------------------
    /**
     * 문자열의 처음 SEPARATOR부분까지 문자열을 구한다.<br />
     *
     * <pre>
     * StringUtils.substringBefore(null, *)      = null
     * StringUtils.substringBefore("", *)        = ""
     * StringUtils.substringBefore("abc", "a")   = ""
     * StringUtils.substringBefore("abcba", "b") = "a"
     * StringUtils.substringBefore("abc", "c")   = "ab"
     * StringUtils.substringBefore("abc", "d")   = "abc"
     * StringUtils.substringBefore("abc", "")    = ""
     * StringUtils.substringBefore("abc", null)  = "abc"
     * </pre>
     *
     * @param str 문자열
     * @param separator SEPARATOR
     * @return 결과문자열, 문자열이 null일경우 <code>null</code>
     */
    public static String substringBefore(String str, String separator) {
    	return org.apache.commons.lang.StringUtils.substringBefore(str, separator);
    }

    /**
     * 문자열의 마지막 separator부분까지 문자열을 구한다.<br />
     * 
     * <pre>
     * StringUtils.substringBeforeLast(null, *)      = null
     * StringUtils.substringBeforeLast("", *)        = ""
     * StringUtils.substringBeforeLast("abcba", "b") = "abc"
     * StringUtils.substringBeforeLast("abc", "c")   = "ab"
     * StringUtils.substringBeforeLast("a", "a")     = ""
     * StringUtils.substringBeforeLast("a", "z")     = "a"
     * StringUtils.substringBeforeLast("a", null)    = "a"
     * StringUtils.substringBeforeLast("a", "")      = "a"
     * </pre>
     *
     * @param str 문자열
     * @param separator SEPARATOR
     * @return 결과문자열, 문자열이 null일경우 <code>null</code>
     */
    public static String substringBeforeLast(String str, String separator) {
    	return org.apache.commons.lang.StringUtils.substringBeforeLast(str, separator);
    }

    // Substring between
    //-----------------------------------------------------------------------
    /**
     * 문자열에서 TAG사이에 있는 문자열을 구한다.<br />
     * 
     * <pre>
     * StringUtils.substringBetween(null, *)            = null
     * StringUtils.substringBetween("", "")             = ""
     * StringUtils.substringBetween("", "tag")          = null
     * StringUtils.substringBetween("tagabctag", null)  = null
     * StringUtils.substringBetween("tagabctag", "")    = ""
     * StringUtils.substringBetween("tagabctag", "tag") = "abc"
     * </pre>
     *
     * @param str 문자열
     * @param tag TAG
     * @return 결과문자열, 문자열에 TAG가 존재하지않을 경우<code>null</code>
     */
    public static String substringBetween(String str, String tag) {
    	return org.apache.commons.lang.StringUtils.substringBetween(str, tag);
    }

    /**
     * 문자열에서 OPEN부터 CLOSE까지 사이에 있는 문자열을 구한다.<br />
     * 
     * <pre>
     * StringUtils.substringBetween("wx[b]yz", "[", "]") = "b"
     * StringUtils.substringBetween(null, *, *)          = null
     * StringUtils.substringBetween(*, null, *)          = null
     * StringUtils.substringBetween(*, *, null)          = null
     * StringUtils.substringBetween("", "", "")          = ""
     * StringUtils.substringBetween("", "", "]")         = null
     * StringUtils.substringBetween("", "[", "]")        = null
     * StringUtils.substringBetween("yabcz", "", "")     = ""
     * StringUtils.substringBetween("yabcz", "y", "z")   = "abc"
     * StringUtils.substringBetween("yabczyabcz", "y", "z")   = "abc"
     * </pre>
     *
     * @param str 문자열
     * @param open OPEN
     * @param close CLOSE
     * @return 결과문자열, 문자열에 TAG가 존재하지않을 경우<code>null</code>
     */
    public static String substringBetween(String str, String open, String close) {
    	return org.apache.commons.lang.StringUtils.substringBetween(str, open, close);
    }

    /**
     * 문자열로부터 OPEN과 CLOSE에 감싸진 부분문자열을 배열로 구한다.<br />
     *
     * <pre>
     * StringUtils.substringsBetween("[a][b][c]", "[", "]") = ["a","b","c"]
     * StringUtils.substringsBetween(null, *, *)            = null
     * StringUtils.substringsBetween(*, null, *)            = null
     * StringUtils.substringsBetween(*, *, null)            = null
     * StringUtils.substringsBetween("", "[", "]")          = []
     * </pre>
     *
     * @param str 문자열
     * @param open OPEN
     * @param close CLOSE
     * @return 결과문자배열, 문자열과 OPEN 및 CLOSE중 null인 경우<code>null</code>
     */
	public static String[] substringsBetween(String str, String open, String close) {
    	return org.apache.commons.lang.StringUtils.substringsBetween(str, open, close);
    }

    /**
     * 대문자는 소문자로 변환하고 소문자는 대문자로 변환한다.<br />
     *
     * <pre>
     * StringUtils.swapCase(null)                 = null
     * StringUtils.swapCase("")                   = ""
     * StringUtils.swapCase("The dog has a BONE") = "tHE DOG HAS A bone"
     * </pre>
     *
     * @param str  the String to swap case, may be null
     * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
     */
    public static String swapCase(String str) {
    	return org.apache.commons.lang.StringUtils.swapCase(str);
    }

    /**
	 * 첫번째 문자를 대문자로 변경<br><br>
	 *
	 * StringUtils.swapFirstLetterCase("java") = "Java"
	 *
	 * @param str 문자열
	 * @return 첫번째 문자를 대문자로 변경한 문자열
	 */
	public static String swapFirstLetterCase(String str) {
		StringBuilder sbuf = new StringBuilder(str);
		sbuf.deleteCharAt(0);
		
		if (Character.isLowerCase(str.substring(0, 1).toCharArray()[0])) {
			sbuf.insert(0, str.substring(0, 1).toUpperCase());
		} else {
			sbuf.insert(0, str.substring(0, 1).toLowerCase());
		}
		
		return sbuf.toString();
	}

    /**
	 * 주어진 String에 대해서 delimiter를 이용하여 tokenize한 후 String[]로 뽑아낸다.<br>
	 * Java의 StringTokenizer를 이용하여 처리한다.<br>
	 * 옵션에 따라 공백(space)에 대한 처리(trim), 값이 ""인 token에 대한 포함 여부를 결정할 수 있다.<br>
	 * StringTokenizer를 이용하므로, 연속된 delimiter 사이는 token이 되지 않는다.<br>
	 * 주어진 String이 null일 경우 null을 return한다.<br>
	 * delimiter가 null일 경우 주어진 String을 하나의 element로 가지는 String[]를 return한다.<br><br>
	 *
	 * StringUtils.tokenizeToStringArray("aaa.bbb.ccc.ddd", ".", true, true) = {"aaa", "bbb", "ccc", "ddd"}
	 *
	 * @param str 문자열
	 * @param separator 구분자
	 * @param trimTokens 공백제거 여부
	 * @param ignoreEmptyTokens 빈 토큰 무시 여부
	 * @return an array of parsed Strings
	 */
	public static String[] tokenizeToStringArray(String str, String separator, boolean trimTokens, boolean ignoreEmptyTokens) {
		if (str == null) {
			return null;
		}
		
		if (separator == null) {
			return new String[] {str};
		}
		
		StringTokenizer st = new StringTokenizer(str, separator);
		List<String> tokens = new ArrayList<String>();
		
		do {
			if (!st.hasMoreTokens()) {
				break;
			}
			
			String token = st.nextToken();
			
			if (trimTokens) {
				token = token.trim();
			}
			
			if (!ignoreEmptyTokens || token.length() != 0) {
				tokens.add(token);
			}
		} while (true);
		
		return tokens.toArray(new String[tokens.size()]);
	}

    /**
	 * 입력된 문자열로 부터 숫자만 추출하여 '-'가 포함된 전화번호 형태의 문자열로 포매팅하여 리턴한다.<br><br>
	 *
	 * StringUtils.toTelephoneNumberFormat("032-123-4567") = "032-123-4567"<br>
	 * StringUtils.toTelephoneNumberFormat("021234567") = "02-123-4567"<br>
	 * StringUtils.toTelephoneNumberFormat("0212345678") = "02-1234-5678"<br>
	 * StringUtils.toTelephoneNumberFormat("1234567") = "123-4567"
	 *
	 * @param str 문자열
	 * @return 전화번호 형태로 변환된 문자열
	 */
	public static String toTelephoneNumberFormat(String str) {
		int endNumberDigit = 4;
		int minNumberDigit = 7;

		if (StringUtil.isEmpty(str)) {
			return null;
		}

		String origin = str.trim();
		String tempNumber;

		int originLength = origin.length();

		// extract numeric chars only
		if (StringUtil.isNotNumeric(origin)) {
			StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < originLength; i++) {
				if (Character.isDigit(origin.charAt(i))) {
					sb.append(origin.charAt(i));
				}
			}
			
			tempNumber = sb.toString();
		} else {
			tempNumber = origin;
		}

		int numberLength = tempNumber.length();

		if (numberLength < minNumberDigit) {
			return tempNumber;
		}

		String firstNumber = "";
		String secondNumber = "";
		String thirdNumber = "";

		if (tempNumber.charAt(0) == '0') { // local number or mobile number
			if (tempNumber.charAt(1) == '2') { // Seoul
				firstNumber = tempNumber.substring(0, 2);
				secondNumber = tempNumber.substring(2, numberLength - endNumberDigit);
				thirdNumber = tempNumber.substring(numberLength - endNumberDigit, numberLength); // split last 4 digits
			} else { // local number or mobile number
				firstNumber = tempNumber.substring(0, 3);
				secondNumber = tempNumber.substring(3, numberLength - endNumberDigit);
				thirdNumber = tempNumber.substring(numberLength - endNumberDigit, numberLength); // split last 4 digits
			}
			
			return firstNumber + "-" + secondNumber + "-" + thirdNumber;
		} else { // telephone number without local number
			firstNumber = tempNumber.substring(0, numberLength - endNumberDigit);
			secondNumber = tempNumber.substring(numberLength - endNumberDigit, numberLength);

			return firstNumber + "-" + secondNumber;
		}
	}

    /**
	 * 주어진 6자리 숫자 String을 "111-111" 형태의 우편번호 포맷으로 변환한다.<br>
	 * 주어진 String이 6자리가 아닐 경우 ""를 return한다.<br>
	 * 주어진 String이 숫자만으로 구성되어 있지 않을 경우 ""을 return한다.<br><br>
	 *
	 * StringUtils.toZipCodePattern("111111") = "111-111"
	 *
	 * @param str 문자열
	 * @return 우편번호 형태로 변환된 문자열
	 */
	public static String toZipCodePattern(String str) {
		if (str == null) {
			return "";
		}
		
		if (str.length() != 6 || !isDigit(str)) {
			return "";
		} else {
			StringBuffer buffer = new StringBuffer();
			buffer.append(str.substring(0, 3));
			buffer.append('-');
			buffer.append(str.substring(3, 6));
			
			return buffer.toString();
		}
	}

    /**
     * 문자열의 좌우에 있는 스페이스를 제거한다.<br />
     * 
     * <pre>
     * StringUtils.trim(null)          = null
     * StringUtils.trim("")            = ""
     * StringUtils.trim("     ")       = ""
     * StringUtils.trim("abc")         = "abc"
     * StringUtils.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str 문자열
     * @return null 문자열 일경우 null
     * 		   "" 일경우 ""
     */
    public static String trim(String str) {
        return org.apache.commons.lang.StringUtils.trim(str);
    }

    /**
	 * 입력된 문자열에서 주어진 문자열과 일치하는 부분을 trim한다.<br><br>
	 *
	 * StringUtils.trim("Anyframe*Java", "*") = "AnyframeJava"
	 *
	 * @param origString 문자열
	 * @param trimString trim할 문자열
	 * @return 주어진 문자열에서 trim할 문자열을 trim한 문자열
	 */
	public static String trim(String origString, String trimString) {
		int startPosit = origString.indexOf(trimString);
		
		if (startPosit != -1) {
			int endPosit = trimString.length() + startPosit;
			
			return origString.substring(0, startPosit) + origString.substring(endPosit);
		}
		
		return origString;
	}

    /**
     * 처리문자가 NULL일경우 공백을 반환, 아닐경우 좌우에 있는<br />
     * 스페이스를 제거한다.<br />
     * <pre>
     * StringUtils.trimToEmpty(null)          = ""
     * StringUtils.trimToEmpty("")            = ""
     * StringUtils.trimToEmpty("     ")       = ""
     * StringUtils.trimToEmpty("abc")         = "abc"
     * StringUtils.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param str 문자열
     * @return null 문자열 또는 ""일 경우 ""
     */
    public static String trimToEmpty(String str) {
    	return org.apache.commons.lang.StringUtils.trimToEmpty(str);
    }

    /**
     * 문자열의 좌우에 있는 스페이스를 제거한다.<br />
     * 제거후 결과가 공백일경우 NULL를 반환<br />
     *
     * <pre>
     * StringUtils.trimToNull(null)          = null
     * StringUtils.trimToNull("")            = null
     * StringUtils.trimToNull("     ")       = null
     * StringUtils.trimToNull("abc")         = "abc"
     * StringUtils.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param str 문자열
     * @return null 문자열 또는 ""일 경우 null
     */
    public static String trimToNull(String str) {
    	return org.apache.commons.lang.StringUtils.trimToNull(str);
    }

    /**
     * 문자열의 첫문자를 소문자로 변환한다.<br />
     *
     * <pre>
     * StringUtils.uncapitalize(null)  = null
     * StringUtils.uncapitalize("")    = ""
     * StringUtils.uncapitalize("Cat") = "cat"
     * StringUtils.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     * @param str 문자열
     * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
     */
    public static String uncapitalize(String str) {
    	return org.apache.commons.lang.StringUtils.uncapitalize(str);
    }

    /**
     * 문자열을 대문자로 변환한다.<br />
     *
     * <pre>
     * StringUtils.upperCase(null)  = null
     * StringUtils.upperCase("")    = ""
     * StringUtils.upperCase("aBc") = "ABC"
     * </pre>
     *
     * @param str 문자열
     * @return 결과문자열, 입력문자열이 null일경우 <code>null</code>
     */
    public static String upperCase(String str) {
    	return org.apache.commons.lang.StringUtils.upperCase(str);
    }


	// 파라미터 -> Map
	public static HashMap<String, Object> convertStringToQueryMap(String query)  {  
	    String[] params = query.split("&");  
	    HashMap<String, Object> map = new HashMap<String, Object>();  
	    
	    for (String param : params) {  
	        String param_array[] = param.split("=");  
	        String name = "";
	        String value = "";
	        
	        try{
	        	name = param_array[0];  
	        	value = param_array[1];  
	        	
	        	map.put(name.trim(), value.trim());
	        } catch(Exception e){}
	    }
	    
	    return map;  
	}
	
	public static String createLicenseCode(){
		String ramdom_code = getRandom("ABC123",16);
		String st1=ramdom_code.substring(0,4);
		String st2=ramdom_code.substring(4,8);
		String st3=ramdom_code.substring(8,12);
		String st4=ramdom_code.substring(12,16);
		String return_code = st1+"-"+st2+"-"+st3+"-"+st4;
		
		return return_code;
	}
	
	// 랜덤 함수 생성
	public static String getRandom(String type, int loopCount) {
		String dummyString="1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijlmnopqrstuvwxyz";
		
		if(type != null){
			if(type.equals("123")) {
				dummyString = "1234567890";
			} else if(type.equals("abc")) {
				dummyString = "abcdefghijlmnopqrstuvwxyz";
			} else if(type.equals("ABC")) {
				dummyString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			} else if(type.equals("ABC123") || type.equals("123ABC")) {
				dummyString = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			} else if(type.equals("abc123") || type.equals("123abc")) {
				dummyString = "1234567890abcdefghijklmnopqrstuvwxyz";
			} else if(type.equals("aA1!")) {
				dummyString = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ@#$%_-";
			}
		}
		
		Random random=new Random();
		// char 48=0 65=A 90=Z 97=a 122=z
		StringBuilder tempBuilder=new StringBuilder(100);
		int randomInt;
		char tempChar;
		
		for(int loop=0; loop<loopCount; loop++) {
			randomInt=random.nextInt(dummyString.length());
			tempChar=dummyString.charAt(randomInt);
			tempBuilder.append(tempChar);
		}
		
		return tempBuilder.toString();
	}
	
	public static String convertTimestampToDate(int timestamp) {
		String dd=null;
		String hh=null;
		String ii=null;
		String ss=null;
		
		try {
			int temp=timestamp;

			dd=String.valueOf((int)Math.floor(temp/(3600*24)));

			temp=temp%(3600*24);
			hh=String.format("%02d", (int)Math.floor(temp/(60*60)));

			temp=temp%(60*60);
			ii=String.format("%02d", (int)Math.floor(temp/60));

			temp=temp%60;
			ss=String.format("%02d", (int)Math.floor(temp));
		} catch (Exception e) {
			return "-";
		}
		
		return String.format("%sd %s:%s:%s", dd,hh,ii,ss);
	}
	
	public static String createYMDDir(){
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.KOREA);
		Date currentTime=new Date();
		String mTime=mSimpleDateFormat.format(currentTime);
		
		return mTime;
	}
	
	public static String avoidNull(String s0, String s1) {
        return StringUtil.isEmptyTrimmed(s0) ? s1 : s0;
    }

	public static String convert2CamelCase(String underScore) {
		if (underScore.indexOf('_') < 0	&& Character.isLowerCase(underScore.charAt(0))) {
			return underScore;
		}

		StringBuilder result = new StringBuilder();
		boolean nextUpper = false;
		int len = underScore.length();

		for (int i = 0; i < len; i++) {
			char currentChar = underScore.charAt(i);
			if (currentChar == '_') {
				nextUpper = true;
			} else {
				if (nextUpper) {
					result.append(Character.toUpperCase(currentChar));
					nextUpper = false;
				} else {
					result.append(Character.toLowerCase(currentChar));
				}
			}
		}
		return result.toString();
	}
}