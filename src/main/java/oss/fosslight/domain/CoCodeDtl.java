/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

public class CoCodeDtl {

	/** The cd dtl no. */
	String cdDtlNo;
	
	public String getCdDtlNo() {
		return cdDtlNo;
	}

	public void setCdDtlNo(String cdDtlNo) {
		this.cdDtlNo = cdDtlNo;
	}

	public String getCdDtlNm() {
		return cdDtlNm;
	}

	public void setCdDtlNm(String cdDtlNm) {
		this.cdDtlNm = cdDtlNm;
	}

	/** The cd sub no. */
    String cdSubNo;
    
    /** The cd dtl nm. */
    String cdDtlNm;

    /** The cd dtl nm. */
    String cdDtlNm2;
    
    /** The cd dtl exp. */
    String cdDtlExp;
    
    /** The prir seq. */
    int cdOrder;
    
    /** The use Flag. */
    String useYn;

    /**
     * Instantiates a new co code dtl.
     *
     * @param cdDtlNo the Code Dtl No
     * @param cdSubNo the Code Dtl Sub No
     * @param cdDtlNm the Code Dtl Name
     * @param cdDtlExp the Code Dtl Exp
     * @param cdBit the Code Bit
     * @param cdOrder the Code Order
     * @param useYn the useYn
     */
    public CoCodeDtl(String cdDtlNo, String cdSubNo, String cdDtlNm, String cdDtlNm2, String cdDtlExp, int cdOrder, String useYn)
    {
        this.cdDtlNo = cdDtlNo;
        this.cdSubNo = cdSubNo;
        this.cdDtlNm = cdDtlNm;
        this.cdDtlNm2 = cdDtlNm2;
        this.cdDtlExp = cdDtlExp;
        this.cdOrder = cdOrder;
        this.useYn = useYn;
    }

}
