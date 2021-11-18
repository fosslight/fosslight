/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class HrCodeVO.
 */
public class CodeBean extends ComBean implements Serializable{
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** 코드번호. */
    private String cdNo;

    /** 코드명. */
    private String cdNm;

    /** 코드설명. */
    private String cdExp;

    /** 시스템코드여부. */
    private String sysCdYn;
    
    /** 코드상세번호. */
    private String[] cdDtlNo;
    
    /** 코드상세명. */
    private String[] cdDtlNm;
    
    /** 코드상세명2. */
    private String[] cdDtlNm2;
    
    /** 코드상세설명. */
    private String[] cdDtlExp;
    
    /** 코드우선순위. */
    private String[] cdOrder;

    /** 코드사용여부. */
    private String[] useYn;
    
  
	/**
     * Gets the 코드번호.
     *
     * @return the 코드번호
     */
    public String getCdNo() {
        return cdNo;
    }

    /**
     * Sets the 코드번호.
     *
     * @param cdNo the new 코드번호
     */
    public void setCdNo(String cdNo) {
        this.cdNo = cdNo;
    }

    /**
     * Gets the 코드명.
     *
     * @return the 코드명
     */
    public String getCdNm() {
        return cdNm;
    }

    /**
     * Sets the 코드명.
     *
     * @param cdNm the new 코드명
     */
    public void setCdNm(String cdNm) {
        this.cdNm = cdNm;
    }

    /**
     * Gets the 코드설명.
     *
     * @return the 코드설명
     */
    public String getCdExp() {
        return cdExp;
    }

    /**
     * Sets the 코드설명.
     *
     * @param cdExp the new 코드설명
     */
    public void setCdExp(String cdExp) {
        this.cdExp = cdExp;
    }

    /**
     * Gets the 시스템코드여부.
     *
     * @return the 시스템코드여부
     */
    public String getSysCdYn() {
        return sysCdYn;
    }

    /**
     * Sets the 시스템코드여부.
     *
     * @param sysCdYn the new 시스템코드여부
     */
    public void setSysCdYn(String sysCdYn) {
        this.sysCdYn = sysCdYn;
    }

    /**
     * Gets the 코드상세번호.
     *
     * @return the 코드상세번호
     */
    public String[] getCdDtlNo() {
        return cdDtlNo != null ? cdDtlNo.clone() : null;
    }

    /**
     * Sets the 코드상세번호.
     *
     * @param cdDtlNo the new 코드상세번호
     */
    public void setCdDtlNo(String[] cdDtlNo) {
        this.cdDtlNo = cdDtlNo;
    }

    /**
     * Gets the 코드상세명.
     *
     * @return the 코드상세명
     */
    public String[] getCdDtlNm() {
        return cdDtlNm != null ? cdDtlNm.clone() : null;
    }

    /**
     * Sets the 코드상세명.
     *
     * @param cdDtlNm the new 코드상세명
     */
    public void setCdDtlNm(String[] cdDtlNm) {
        this.cdDtlNm = cdDtlNm;
    }
    
    /**
     * Gets the 코드상세명2.
     *
     * @return the 코드상세명2
     */
    public String[] getCdDtlNm2() {
        return cdDtlNm2 != null ? cdDtlNm2.clone() : null;
	}
    
    /**
     * Sets the 코드상세명2.
     *
     * @param cdDtlNm the new 코드상세명2
     */
	public void setCdDtlNm2(String[] cdDtlNm2) {
		this.cdDtlNm2 = cdDtlNm2;
	}

	/**
     * Gets the 코드상세설명.
     *
     * @return the 코드상세설명
     */
    public String[] getCdDtlExp() {
        return cdDtlExp != null ? cdDtlExp.clone() : null;
    }

    /**
     * Sets the 코드상세설명.
     *
     * @param cdDtlExp the new 코드상세설명
     */
    public void setCdDtlExp(String[] cdDtlExp) {
        this.cdDtlExp = cdDtlExp;
    }

    /**
     * Gets the 코드우선순위.
     *
     * @return the 코드우선순위
     */
    public String[] getCdOrder() {
        return cdOrder != null ? cdOrder.clone() : null;
    }

    /**
     * Sets the 코드우선순위.
     *
     * @param cdOrder the new 코드우선순위
     */
    public void setCdOrder(String[] cdOrder) {
        this.cdOrder = cdOrder;
    }

    public String[] getUseYn() {
        return useYn != null ? useYn.clone() : null;
	}

	public void setUseYn(String[] useYn) {
		this.useYn = useYn;
	}

	
	/**
     * requst parameter로부터 코드 상세 정보 Bean List 작성
     * @param key
     * @param type
     * @return
     */
    public List<CodeDtlBean> getDtlList() {
        if(getCdNo() == null || getCdDtlNo() == null || getCdDtlNo().length == 0) {
            return null;
        }
        
        ArrayList<CodeDtlBean> list = new ArrayList<CodeDtlBean>();
        
        CodeDtlBean dtlBean = null;
        int cnt = 0;
        for(String s : getCdDtlNo()) {
            if(s.isEmpty()) {
                continue;
            }
            dtlBean = new CodeDtlBean();
            dtlBean.setCdNo(this.getCdNo());
            dtlBean.setCdDtlNo(s);
            dtlBean.setCdDtlNm(this.getCdDtlNm()[cnt]);
            if(this.getCdDtlExp() == null || this.getCdDtlExp().length < cnt+1) {
            } else {
                dtlBean.setCdDtlExp(this.getCdDtlExp()[cnt]);
            }
            dtlBean.setCdOrder(this.getCdOrder()[cnt]);
            dtlBean.setUseYn(this.getUseYn()[cnt]);
            list.add(dtlBean);
            cnt++;
        }
        
        return list;
    }

}
