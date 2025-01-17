package oss.fosslight.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 자주 사용하는 {@link String} 유틸리티 모음 클래스.
 * <p>이 클래스는 코어 자바에서 제공하는 {@link String} 과 {@link StringBuilder}
 * 클래스에서 제공하는 기능들을 사용하기 쉽게 재정의 하였습니다.
 * @author Eddie
 * @version 1.0.1
 */
public abstract class StringUtils {

	private static final Logger logger = LoggerFactory.getLogger(StringUtils.class);

	//---------------------------------------------------------------------
	// String 포맷 관련 메서드(Method) 모음
	//---------------------------------------------------------------------
	/**
	 * 전달받은 문자열{@code String}이 비어있는지 확인합니다.
	 * <p>이 메서드는 Object를 파라미터로 받아, {@code null} 이거나 공백 문자인지 비교합니다.
	 * <p>파라미터로 전달되는 Object가 non-null이거나 non-String일 경우에는 {@code true}를 반환하지 않습니다.
	 * <p><pre class="code">
	 * StringUtils.isEmpty(null) = false;
	 * StringUtils.isEmpty("") = false;
	 *
	 * StringUtils.isEmpty("  ") = true;
	 * StringUtils.isEmpty("text") = true;
	 * </pre>
	 * @param {@link String}으로 사용되는 str
	 * @since 1.0.0
	 */
	public static boolean isEmpty(Object str) {
		return (str == null || "".equals(str));
	}

	/**
	 * 전달받은 {@code CharSequence}이 {@code null}이거나 길이(length)가 0 이상인지 확인합니다.
	 * <p>Note: 이 메서드는 전달받은 str이 완전한 공백({@code null} or {@code ""})인지 확인합니다.
	 * <p><pre class="code">
	 * StringUtils.hasLength(null) = false
	 * StringUtils.hasLength("") = false
	 * StringUtils.hasLength(" ") = true
	 * StringUtils.hasLength("Hello") = true
	 * </pre>
	 * @param {@code String}타입의 str을 받아 {@code null}이거나 공백 문자인지 확인합니다.
	 * @return {@code String}이 {@code null}이 아니거나 length가 0이상일 경우 {@code true}를 반환합니다.
	 * @see #hasText(String)
	 * @since 1.0.0
	 */
	public static boolean hasLength(CharSequence str) {
		return (str != null && str.length() > 0);
	}

	/**
	 * 전달받은 {@code CharSequence}이 {@code null}이거나 길이(length)가 0 이상인지 확인합니다.
	 * <p>Note: 이 메서드는 전달받은 str이 완전한 공백({@code null} or {@code ""})인지 확인합니다.
	 * <p><pre class="code">
	 * StringUtils.hasLength(null) = false
	 * StringUtils.hasLength("") = false
	 * StringUtils.hasLength(" ") = true
	 * StringUtils.hasLength("Hello") = true
	 * </pre>
	 * @param {@code String}타입의 str을 받아 {@code null}이거나 공백 문자인지 확인합니다.
	 * @return {@code String}이 {@code null}이 아니거나 length가 0이상일 경우 {@code true}를 반환합니다.
	 * @see #hasLength(CharSequence)
	 * @see #hasText(String)
	 * @since 1.0.0
	 */
	public static boolean hasLength(String str) {
		return (str != null && !str.isEmpty());
	}

	/**
	 * 전달받은 {@code CharSequence}에 <em>text</em>가 포함되어 있는지 확인합니다.
	 * <p>전달 받은 str이 {@code null}이거나 공백문자 이거나, 무의미한 공백({@code ""})으로만 이루어져 있을 경우
	 * {@code false}를 반환합니다.
	 * <p><pre class="code">
	 * StringUtils.hasText(null) = false
	 * StringUtils.hasText("") = false
	 * StringUtils.hasText(" ") = false
	 * StringUtils.hasText("12345") = true
	 * StringUtils.hasText(" 12345 ") = true
	 * </pre>
	 * @param {@code CharSequence}타입의 str({@code null}도 가능합니다).
	 * @return {@code CharSequence}타입이 {@code null}이거나 공백을 제외한 문자열의 길이(length)가 0이상일 경우
	 * {@code true}를 반환합니다. (공백(whitespace)로만 이뤄진 문자열은 {@code false}를 반환합니다)
	 * @see #hasText(String)
	 * @since 1.0.0
	 */
	public static boolean hasText(CharSequence str) {
		return (hasLength(str) && containsText(str));
	}

	/**
	 * 전달받은 {@code CharSequence}에 <em>text</em>가 있는지 확인합니다.
	 * <p>전달 받은 str이 {@code null}이거나 공백문자 이거나, 무의미한 공백({@code ""})으로만 이루어져 있을 경우
	 * {@code false}를 반환합니다.
	 * <p><pre class="code">
	 * StringUtils.hasText(null) = false
	 * StringUtils.hasText("") = false
	 * StringUtils.hasText(" ") = false
	 * StringUtils.hasText("12345") = true
	 * StringUtils.hasText(" 12345 ") = true
	 * </pre>
	 * @param {@code CharSequence}타입의 str({@code null}도 가능합니다).
	 * @return {@code CharSequence}타입이 {@code null}이거나 공백을 제외한 문자열의 길이(length)가 0이상일 경우
	 * {@code true}를 반환합니다. (공백(whitespace)로만 이뤄진 문자열은 {@code false}를 반환합니다)
	 * @see #hasText(CharSequence)
	 * @since 1.0.0
	 */
	public static boolean hasText(String str) {
		return (hasLength(str) && containsText(str));
	}

	/*
	 * !! private Method.
	 * Modifier를 private 레벨 이상으로 설정하지 말아주세요
	 */
	private static boolean containsText(CharSequence str) {
		int strLen = str.length();
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 전달받은 문자열을 Base32로 인코딩 합니다.
	 * <p>Note: 전달받은 문자열이 {@code null}일 경우 {@code null}을 반환합니다.
	 * <p><pre class="code">
	 * StringUtils.encodeBase32(null) = null;
	 * StringUtils.encodeBase32("") = "";
	 * StringUtils.encodeBase32("thinktree") = "ORUGS3TLORZGKZI=";
	 * </pre>
	 * @param str {@code String}타입의 문자열
	 * @return {@code Base32}로 인코딩된 문자열을 반환합니다. 공백 문자일 경우 공백을 반환합니다.
	 * @author Eddie Cho
	 * @since 1.0.1
	 */
	public static String encodeBase32(String str) {
		if(hasText(str)){
			return new Base32().encodeAsString(str.getBytes());
		}
		else return null;
	}

	//---------------------------------------------------------------------
	// String Array 관련 메서드(Method) 모음
	//---------------------------------------------------------------------

	/**
	 * 전달받은 문자열 배열({@code strArr})에 문자열({@code str})을 요소로 추가합니다.
	 * <p> Note: 파라미터{@code str}에 대한 whitespace를 확인하지 않습니다.
	 * <br>전달받은 문자열 배열이 {@code null}일 경우, 새 문자열 배열을 생성하고 {@code str}을 요소로 추가합니다.
	 *
	 * @param strArr
	 * @param str
	 * @return copy된 새 문자열 배열을 반환합니다({@code null}은 반환하지 않습니다).
	 * @since 1.0.0
	 */
	public static String[] addStringToArray(String[] strArr, String str) {
		if( strArr == null || strArr.length == 0 ) {
			return new String[] {str};
		}

		String[] newArr = new String[strArr.length + 1];
		System.arraycopy(strArr, 0, newArr, 0, strArr.length);
		newArr[strArr.length] = str;
		return newArr;
	}

	/**
	 * 전달받은 String 배열인 {@code strArr}를 List<String>으로 변환합니다.
	 * <p>Note: strArr가 {@code null}이거나 비어있는 배열일 경우 비어있는 {@link List}를 반환합니다.
	 * @param {@link ArrayList}로 변환할 {@link String} 배열
	 * @return {@code ArrayList<String>}을 반환합니다.
	 * 전달받은 strArr이 {@code null}이거나 비어있는 배열일 경우 {@code null}을 반환하지 않고
	 * 비어있는 {@link List}를 반환합니다.
	 * @since 1.0.0
	 */
	public static List<String> convertArrayToList(String[] strArr) {
		List<String> resultList = new ArrayList<>();
		if( strArr == null ) {
			return resultList;
		}

		return Arrays.asList(strArr);
	}

	/**
	 * 전달받은 {@code String} {@code Collection}을 {@code String}배열로 변환합니다..
	 * <p>{@code Collection}에는 반드시 {@code String} 요소만 있어야 합니다.
	 * @param 문자열 배열로 변환할 {@code String} {@code Collection}
	 * @return {@code String} 배열
	 * @since 1.0.0
	 */
	public static String[] toStringArray(Collection<String> collection) {
		if (collection == null) {
			return null;
		}

		return collection.toArray(new String[collection.size()]);
	}

	/**
	 * <p>낙타 표기법(camelCase)의 문자열을 언더스코어 표기법으로 변경합니다.</p>
	 * <pre>
	 *  hyunkwonIsSexy --> hyunkwon_is_sexy
	 * </pre>
	 *
	 * @param str 낙타 표기법으로 표현된 문자열
	 * @return underscore type String
	 */
	public static String camelCaseToUnderscore(String str) {
		Matcher m = Pattern.compile("(?<=[a-z])[A-Z]").matcher(str);

		StringBuffer sb = new StringBuffer();
		while (m.find())
			m.appendReplacement(sb, "_"+m.group().toLowerCase());

		m.appendTail(sb);
		return sb.toString();
	}


	/**
	 * <p>문자열을 낙타 표기법(camelCase) 변경합니다.</p>
	 * <pre>
	 *  honda_hitomi_is_cute --> hondaHitomiIsCute
	 * </pre>
	 *
	 * @param str
	 * @return camelCase type String
	 */
	public static List<Map<String, Object>> inputCamelText(List<Map<String,Object>> list){
		String key = "";
	    Object value = "";
	    Iterator<Entry<String,Object>> iterator = null;

	    List<Map<String, Object>> returnList = new ArrayList<Map<String, Object>>();

	    Entry<String,Object> entry = null;
	    int listSize = list.size();
	    for(int i=0; i<listSize; i++){
	    	Map<String,Object> inputMap = new HashMap<>();
	        iterator = list.get(i).entrySet().iterator();
	        while(iterator.hasNext()){
	            entry = iterator.next();
	            logger.debug("key : "+entry.getKey()+",\t\tvalue : "+entry.getValue());
	            key = entry.getKey().toString();
	            value = entry.getValue().toString();
	            iterator.remove();
	            inputMap.put(convert2CamelCase(key), value);
	        }
	        returnList.add(inputMap);
	    }

	    return returnList;
	}

	public static String convert2CamelCase(String underScore) {
		if (underScore.indexOf('_') < 0	&& Character.isLowerCase(underScore.charAt(0)))
			return underScore;

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

	public static String encodeFileNm(String fileName, String browser) {

	    String encodedFilename = null;
	    // if (browser.equals("MSIE")) { 2017.09.29 보안수정 RH.Jung
	    // PositionLiteralsFirstInComparisons
	    if ("MSIE".equals(browser)) {
	      try {
	        encodedFilename = URLEncoder.encode(fileName, "UTF-8");
	      } catch (UnsupportedEncodingException e) {
	        // e.printStackTrace(); 2017.09.29 보안수정 RH.Jung AvoidPrintStackTrace
	        logger.error(e.getMessage());
	      }
	      // } else if (browser.equals("Firefox")) { 2017.09.29 보안수정 RH.Jung
	      // PositionLiteralsFirstInComparisons
	    } else if ("Firefox".equals(browser)) {
	      try {
	        encodedFilename =

	            "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";
	      } catch (UnsupportedEncodingException e) {
	        // e.printStackTrace(); 2017.09.29 보안수정 RH.Jung AvoidPrintStackTrace
	        logger.error(e.getMessage());
	      }
	      // } else if (browser.equals("Opera")) { 2017.09.29 보안수정 RH.Jung
	      // PositionLiteralsFirstInComparisons
	    } else if ("Opera".equals(browser)) {
	      try {
	        encodedFilename =

	            "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";
	      } catch (UnsupportedEncodingException e) {
	        // e.printStackTrace(); 2017.09.29 보안수정 RH.Jung AvoidPrintStackTrace
	        logger.error(e.getMessage());
	      }
	      // } else if (browser.equals("Chrome")) { 2017.09.29 보안수정 RH.Jung
	      // PositionLiteralsFirstInComparisons
	    } else if ("Chrome".equals(browser)) {
	      StringBuffer sb = new StringBuffer();
	      for (int i = 0; i < fileName.length(); i++) {
	        char c = fileName.charAt(i);
	        if (c > '~') {
	          try {
	            sb.append(URLEncoder.encode("" + c, "UTF-8"));
	          } catch (UnsupportedEncodingException e) {
	            // e.printStackTrace(); 2017.09.29 보안수정 RH.Jung AvoidPrintStackTrace
	            logger.error(e.getMessage());
	          }
	        } else {
	          sb.append(c);
	        }
	      }
	      encodedFilename = sb.toString();
	      // } else if (browser.equals("Safari")) { 2017.09.29 보안수정 RH.Jung
	      // PositionLiteralsFirstInComparisons
	    } else if ("Safari".equals(browser)) {
	      try {
	        encodedFilename =

	            "\"" + new String(fileName.getBytes("UTF-8"), "8859_1") + "\"";
	      } catch (UnsupportedEncodingException e) {
	        // e.printStackTrace(); 2017.09.29 보안수정 RH.Jung AvoidPrintStackTrace
	        logger.error(e.getMessage());
	      }
	    } else {
	      try {
	        encodedFilename = URLEncoder.encode(fileName, "UTF-8");
	      } catch (UnsupportedEncodingException e) {
	        // e.printStackTrace(); 2017.09.29 보안수정 RH.Jung AvoidPrintStackTrace
	        logger.error(e.getMessage());
	      }
	    }

	    return encodedFilename;
	}

	/**
	 * <p>CharSequence에 숫자만 포함되어있는지 확인합니다.
	 * 소숫점은 유니코드 숫자가 아니며 {@code false}를 반환합니다.
	 * </p>
	 *
	 * <p>{@code null}은 {@code false}를 반환합니다..
	 * 공백 Charsequence (length()=0) 또한 {@code false}를 반환합니다.</p>
	 *
	 * <p>이 메서드는 양수 또는 음수의 선행 부호를 허용하지 않습니다.
	 * 또한 String이 아래 메서드를 통과하더라도, 이후에 Integer.parseInt 또는 Long.parseLong으로 구문 분석 할 때 NumberFormatException을 생성 할 수 있습니다
	 * </p>
	 *
	 * <pre>
	 * StringUtils.isNumeric(null)   = false
	 * StringUtils.isNumeric("")     = false
	 * StringUtils.isNumeric("  ")   = false
	 * StringUtils.isNumeric("123")  = true
	 * StringUtils.isNumeric("\u0967\u0968\u0969")  = true
	 * StringUtils.isNumeric("12 3") = false
	 * StringUtils.isNumeric("ab2c") = false
	 * StringUtils.isNumeric("12-3") = false
	 * StringUtils.isNumeric("12.3") = false
	 * StringUtils.isNumeric("-123") = false
	 * StringUtils.isNumeric("+123") = false
	 * </pre>
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} CharSequence가 {@code null}이 아니고 모두 숫자일 경우
	 * @author Eddie Cho
	 * @since 1.0.0
	 */
	public static boolean isNumeric(final CharSequence cs) {
		if (isEmpty(cs)) {
			return false;
		}
		final int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>{@code str}로 넘어오는 문자열을 통째로 URI인코딩 합니다.</p>
	 * <p>Note: 이 메서드는 매개변수로 넘어오는 문자열을 통째로 URI인코딩 합니다.</p>
	 *
	 * <p>8bit 코드페이지가 없거나 16진수로 나눠질 수 없는 문자열들은 {@code UnsupportedEncodingException}를 발생시킬 수 있습니다.
	 * 이 경우에는 매개변수로 넘어온 {@code str}을 그대로 반환합니돠.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.encodeURIComponent(null)             = ""
	 * StringUtils.encodeURIComponent("")               = ""
	 * StringUtils.encodeURIComponent("조현권")           = "%EC%A1%B0%ED%98%84%EA%B6%8C"
	 * StringUtils.encodeURIComponent("Eddie Cho")      = "Eddie%20Cho"
	 * StringUtils.encodeURIComponent("?name=야부키 나코") = "%3Fname%3D%EC%95%BC%EB%B6%80%ED%82%A4%20%EB%82%98%EC%BD%94"
	 * </pre>
	 *
	 * @author Eddie Cho
	 * @param str URI인코딩할 문자열
	 * @return URI인코딩 된 문자열
	 */
	public static String encodeURIComponent(String str) {
		if (isEmpty(str)) return "";
		String result;
		try {
			result = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			result = str;
		}
		return result;
	}

	/**
	 * <p>{@code str}로 넘어오는 문자열을 통째로 URI디코딩 합니다.</p>
	 * <p>Note: 이 메서드는 매개변수로 넘어오는 문자열을 통째로 URI디코딩 합니다.</p>
	 *
	 * <p>퍼센트 인코딩이 되어있지 않거나 8비트 코드페이지를 지원하지 않는 문자열들은 {@code UnsupportedEncodingException}를 발생시킬 수 있습니다.
	 * 이 경우에는 매개변수로 넘어온 {@code str}을 그대로 반환합니돠.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.encodeURIComponent(null)                          = ""
	 * StringUtils.encodeURIComponent("")                            = ""
	 * StringUtils.encodeURIComponent("%EC%A1%B0%ED%98%84%EA%B6%8C") = "조현권"
	 * StringUtils.encodeURIComponent("Eddie%20Cho")                 = "Eddie Cho"
	 * StringUtils.encodeURIComponent("%3Fname%3D%EC%95%BC%EB%B6%80%ED%82%A4%20%EB%82%98%EC%BD%94") = "?name=야부키 나코"
	 * </pre>
	 *
	 * @author Eddie Cho
	 * @param str URI디코딩할 문자열
	 * @return URI디코딩 된 문자열
	 */
	public static String decodeURIComponent(String str) {
		if (isEmpty(str)) return "";
		String result;
		try {
			result = URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			result = str;
		}
		return result;
	}

	/**
	 * <p>매개변수 {@code str1}과 {@code str2}를 비교합니다.</p>
	 *
	 * @param str1
	 * @param str2
	 *
	 * @return 매개변수로 받은 두 문자열의 비교
	 */
	public static boolean equals(String str1, String str2) {
		if( str1 == null ) return str1 == str2;
		else return str1.equals(str2);
	}

	public static String avoidNull(String str1) {
		return str1 == null ? "" : str1;
	}
	public static String avoidNull(String str1, String str2) {
		return isEmpty(str1) ? str2 : str1;
	}
	
	public static String makePhoneNumberFormat(String src) {
		if (src == null) {
			return "";
		}
		src = src.replaceAll("-", "");
		if (src.length() == 8) {
			return src.replaceFirst("^([0-9]{4})([0-9]{4})$", "$1-$2");
		} else if (src.length() == 12) {
			return src.replaceFirst("(^[0-9]{4})([0-9]{4})([0-9]{4})$", "$1-$2-$3");
		}
		return src.replaceFirst("(^02|[0-9]{3})([0-9]{3,4})([0-9]{4})$", "$1-$2-$3");
	}


	public static String leftPad(String str, int size, String padStr) {
		return padString(str, size, padStr, true);
	}

	public static String rightPad(String str, int size, String padStr) {
		return padString(str, size, padStr, false);
	}
	
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


	private StringUtils () {};	// SINGLETON
}
