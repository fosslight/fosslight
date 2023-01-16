package oss.fosslight.util;

import com.trendmicro.tlsh.Tlsh;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TlshUtil {
	public static int compareTlshDistance(String tlsh1, String tlsh2) {
		try {
			Tlsh tlshTest1 = Tlsh.fromTlshStr(tlsh1);
			Tlsh tlshTest2 = Tlsh.fromTlshStr(tlsh2);
			
			return tlshTest1.totalDiff(tlshTest2, true);
		} catch (IllegalArgumentException iae) {
			// 임시로 illegalArgumentException이 발생을 하면 비교를 실패한 것으로 간주하고 -1을 리턴하도록 변경함.
			// 추후 tlsh 4.5.0 version에서 처리가 가능할 경우 재검토 필요함.
//			Tlsh tlshTest1 = getTlsh(tlsh1);
//			Tlsh tlshTest2 = getTlsh(tlsh2);
//			
//			return tlshTest1.totalDiff(tlshTest2, true);
			log.error(iae.getMessage(), iae);
			return -1;
		}
	}
}