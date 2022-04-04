/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.springframework.util.StringUtils;

import oss.fosslight.common.CoConstDef;

/**
 * The Class CoCode.
 */
public class CoCode {


    /** The cd no. */
    private String cdNo;
    
    /** The cd nm. */
    private String cdNm;
    
    /** The code dtls. */
    private Vector<CoCodeDtl> codeDtls;
    
    /** The code dtls hash. */
    private HashMap<String, CoCodeDtl> codeDtlsHash;
    /** The code dtls hash. by Code dtls name */
    private HashMap<String, CoCodeDtl> codeDtlsNameHash;

    /**
     * Gets the cd no.
     *
     * @return the string
     */
    public String getCdNo() {
        return cdNo;
    }

    /**
     * Sets the cd no.
     *
     * @param cdNo the new cd no
     */
    public void setCdNo(String cdNo) {
        this.cdNo = cdNo;
    }

    /**
     * Gets the cd nm.
     *
     * @return the string
     */
    public String getCdNm() {
        return cdNm;
    }

    /**
     * Sets the cd nm.
     *
     * @param cdNm the new cd nm
     */
    public void setCdNm(String cdNm) {
        this.cdNm = cdNm;
    }

    /**
     * Instantiates a new co code.
     *
     * @param s the s
     * @param s1 the s1
     */
    public CoCode(String s, String s1)
    {
        codeDtls = new Vector<CoCodeDtl>();
        codeDtlsHash = new HashMap<String, CoCodeDtl>();
        codeDtlsNameHash = new HashMap<String, CoCodeDtl>();
        cdNo = s;
        cdNm = s1;
    }

    /**
     * Adds the code dtl.
     *
     * @param codedtl the codedtl
     */
    public void addCodeDtl(CoCodeDtl codedtl)
    {
        codeDtls.add(codedtl);
        codeDtlsHash.put(codedtl.cdDtlNo, codedtl);
        codeDtlsNameHash.put(codedtl.cdDtlNm, codedtl);
    }

    /**
     * Gets the cd dtl nm.
     *
     * @param s the s
     * @return the string
     */
    public String getCdDtlNm(String s)
    {
        CoCodeDtl codedtl = (CoCodeDtl)codeDtlsHash.get(s);
        return codedtl != null ? codedtl.cdDtlNm : "";
    }

    /**
     * Gets the cd dtl exp.
     *
     * @param s the s
     * @return the string
     */
    public String getCdDtlExp(String s)
    {
        CoCodeDtl codedtl = (CoCodeDtl)codeDtlsHash.get(s);
        
        return (codedtl != null && CoConstDef.FLAG_YES.equals(codedtl.useYn)) ? codedtl.cdDtlExp : "";
    }
    

    public String getCdDtlExpByName(String s)
    {
        CoCodeDtl codedtl = (CoCodeDtl)codeDtlsNameHash.get(s);
        return (codedtl != null && CoConstDef.FLAG_YES.equals(codedtl.useYn)) ? codedtl.cdDtlExp : "";
    }
    
    public String getCdSubCdNo(String s)
    {
        CoCodeDtl codedtl = (CoCodeDtl)codeDtlsHash.get(s);
        return (codedtl != null && CoConstDef.FLAG_YES.equals(codedtl.useYn)) ? codedtl.cdSubNo : "";
    }
    
    /**
     * Gets the cd dtl prior.
     *
     * @param s the s
     * @return the int
     */
    public int getCdDtlPrior(String s)
    {
        CoCodeDtl codedtl = (CoCodeDtl)codeDtlsHash.get(s);
        return ((codedtl != null && CoConstDef.FLAG_YES.equals(codedtl.useYn)) ? Integer.valueOf(codedtl.cdOrder).intValue() : null);
    }

    /**
     * Gets the cd dtl no, cd dtl nm pair vector.
     * @param locale 
     *
     * @return the vector
     */
    public Vector<String[]> getCdDtlNoCdDtlNmPairVector(boolean ignoreDel)
    {
        Vector<String[]> vector = new Vector<String[]>();
        int i = 0;
        for(int j = codeDtls.size(); i < j; i++)
        {
            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(i);
            if(!ignoreDel && CoConstDef.FLAG_NO.equals(codedtl.useYn)) {
            	continue;
            }
            vector.add(new String[] {
                codedtl.cdDtlNo, codedtl.cdDtlNm, codedtl.cdSubNo, codedtl.cdDtlExp
            });
        }

        return vector;
    }
    
    public Vector<CoCodeDtl> getCdDtlNoCdDtlNmPairVectorBean(boolean ignoreDel)
    {
        Vector<CoCodeDtl> vector = new Vector<CoCodeDtl>();
        int i = 0;
        for(int j = codeDtls.size(); i < j; i++)
        {
            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(i);
            if(!ignoreDel && CoConstDef.FLAG_NO.equals(codedtl.useYn)) {
            	continue;
            }
            vector.add(codedtl);
        }

        return vector;
    }
    
    
    /**
     * Gets the cd dtl no, cd dtl nm pair vector, cd dtl exp.
     *
     * @return the vector
     */
    public Vector<String[]> getCdAllPairVector(boolean ignoreDel)
    {
        Vector<String[]> vector = new Vector<String[]>();
        int i = 0;
        for(int j = codeDtls.size(); i < j; i++)
        {
            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(i);
            if(!ignoreDel && CoConstDef.FLAG_NO.equals(codedtl.useYn)) {
            	continue;
            }
            vector.add(new String[] {
                codedtl.cdDtlNo, codedtl.cdSubNo, codedtl.cdDtlNm, codedtl.cdDtlNm2, codedtl.cdDtlExp, codedtl.cdOrder+""
            });
        }
        return vector;
    }
    
    public Vector<CoCodeDtl> getCdAllPairVectorBean(boolean ignoreDel)
    {
        Vector<CoCodeDtl> vector = new Vector<CoCodeDtl>();
        int i = 0;
        for(int j = codeDtls.size(); i < j; i++)
        {
            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(i);
            if(!ignoreDel && CoConstDef.FLAG_NO.equals(codedtl.useYn)) {
            	continue;
            }
            vector.add(codedtl);
        }

        return vector;
    }
    

    /**
     * Gets the cd dtl no vector.
     *
     * @return the vector
     */
    public Vector<String> getCdDtlNoVector(boolean ignoreDel)
    {
        Vector<String> vector = new Vector<String>();
        int i = 0;
        for(int j = codeDtls.size(); i < j; i++)
        {
            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(i);
            if(!ignoreDel && CoConstDef.FLAG_NO.equals(codedtl.useYn)) {
            	continue;
            }
            vector.add(codedtl.cdDtlNo);
        }

        return vector;
    }

    /**
     * Gets the cd dtl nm vector.
     * @param locale 
     *
     * @return the vector
     */
	public Vector<String> getCdDtlNmVector(boolean ignoreDel) {
		Vector<String> vector = new Vector<String>();
		int i = 0;
		for (int j = codeDtls.size(); i < j; i++) {
			CoCodeDtl codedtl = (CoCodeDtl) codeDtls.get(i);
			if (!ignoreDel && CoConstDef.FLAG_NO.equals(codedtl.useYn)) {
				continue;
			}
			vector.add(codedtl.cdDtlNm);
		}

		return vector;
	}

    /**
     * Creates the combo box string.
     *
     * @param s the s
     * @param s1 the s1
     * @param i the i
     * @return the string
     */
    public String createComboBoxString(String s, String s1, int i)
    {
        StringBuffer stringbuffer = (new StringBuffer("<select name='")).append(s).append("'>\n");
        int j = 0;
        for(int k = codeDtls.size(); j < k; j++)
        {
            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(j);
            stringbuffer.append("    <option value='").append(codedtl.cdDtlNo).append('\'');
            if((i == 0 && codedtl.cdDtlNo.equals(s1)) || (i == 1 && codedtl.cdDtlNm.equals(s1)))
            {
                stringbuffer.append(" selected");
            }
            stringbuffer.append(">").append(codedtl.cdDtlNm).append("</option>\n");
        }

        return stringbuffer.append("</select>\n").toString();
    }

    /**
     * Creates the option string.
     *
     * @param s the s
     * @param i the i
     * @return the string
     */
    public String createOptionString(String s, int i)
    {
        StringBuffer stringbuffer = new StringBuffer();
        int j = 0;
        for(int k = codeDtls.size(); j < k; j++)
        {
            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(j);
            
            if (CoConstDef.FLAG_NO.equals(codedtl.useYn)) 
            {
            	continue;
            }
            
            stringbuffer.append("    <option VALUE='").append(codedtl.cdDtlNo).append('\'');
            
            if( (i == 0 && codedtl.cdDtlNo.equals(s) ) || ( i == 1 && codedtl.cdDtlNm.equals(s) ))
            {
                stringbuffer.append(" selected");
            }
            stringbuffer.append(">").append(codedtl.cdDtlNm).append("</option>\n");
        }

        return stringbuffer.toString();
    }

    /**
     * Creates the checkbox item string.
     *
     * @param s the s
     * @return the string
     */
    public String createCheckboxString(String status, String cd, String callType)
    {
        StringBuffer stringbuffer = new StringBuffer();
        
        int j = 0;
        if(CoConstDef.CD_PROJECT_STATUS.equals(cd)) {
	        for(int k = codeDtls.size(); j < k; j++)
	        {
	            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(j);
	            
	            if (CoConstDef.FLAG_NO.equals(codedtl.useYn)) 
	            {
	            	continue;
	            }
	            
	            stringbuffer.append("    <input name='statuses' type='checkbox' value='").append(codedtl.cdDtlNo).append("' ").append((status.indexOf(codedtl.cdDtlNo)>-1)?"checked='checked'":""); 
	            stringbuffer.append(" style='margin:2px 5px 0px 0px;' />&nbsp;").append(codedtl.cdDtlNm).append("&nbsp;&nbsp;&nbsp;&nbsp;");
	        }
        } else if(CoConstDef.CD_LICENSE_RESTRICTION.equals(cd)) {
        	int newLineIdx = 0; 
        	if("list".equals(callType)){
        		newLineIdx = 3;
        	}else if("edit".equals(callType)){
        		newLineIdx = 4;
        	}
        	
        	List<String> restrictionList = Arrays.asList(status.split(","));
        	
        	for(int k = codeDtls.size(); j < k; j++)
	        {
	            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(j);
	            
	            if (CoConstDef.FLAG_NO.equals(codedtl.useYn)) 
	            {
	            	continue;
	            }
	            
	            stringbuffer.append("    <input name='restrictions' type='checkbox' value='").append(codedtl.cdDtlNo).append("' ").append((restrictionList.contains(codedtl.cdDtlNo))?"checked='checked'":""); 
	            stringbuffer.append(" style='margin:2px 5px 0px 0px;' />&nbsp;").append(codedtl.cdDtlNm).append("&nbsp;&nbsp;&nbsp;&nbsp;"+(j==newLineIdx?"<br>"+("list".equals(callType)?"<label></label>":""):""));
	        }
        } else if(CoConstDef.CD_NOTICE_INFO.equals(cd)) {
        	for(int k = codeDtls.size(); j < k; j++)
	        {
	            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(j);
	            
	            if (CoConstDef.FLAG_NO.equals(codedtl.useYn)) 
	            {
	            	continue;
	            }
	            
	            stringbuffer.append("<input name='noticeType' type='checkbox' value='").append(codedtl.cdDtlNo).append("' ").append(CoConstDef.FLAG_YES.equals(codedtl.cdDtlExp)?"checked='checked'":"");
	            
	            if(CoConstDef.CD_NOTICE_HTML_STR.equals(codedtl.cdDtlNm.toUpperCase())) 
	            {
	            	stringbuffer.append(" disabled ");
	            }
	            
	            stringbuffer.append("  />&nbsp;").append(codedtl.cdDtlNm).append("&nbsp;&nbsp;&nbsp;&nbsp;");
	        }
        }
        return stringbuffer.toString();
    }
    
    /**
     * Creates the checkbox item string (common).
     *
     * @param s the s
     * @return the string
     */
    public String createCommonCheckboxString(String val, String name, Boolean NAExceptionFlag){
        StringBuffer stringbuffer = new StringBuffer();
        int j = 0;
        NAExceptionFlag = StringUtils.isEmpty(NAExceptionFlag) ? true : NAExceptionFlag; // default 시 true로 setting
        
        if(!StringUtils.isEmpty(name)) {
        	List<String> values = Arrays.asList(val.split(","));
	        for(int k = codeDtls.size(); j < k; j++){
	            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(j);
	           
	            if (CoConstDef.FLAG_NO.equals(codedtl.useYn)) 
	            {
	            	continue;
	            }
	            
	            if(!NAExceptionFlag && codedtl.cdDtlNo.equals("NA")) // default로 NA도 생성 false값 입력시 예외처리
	            	continue;
	            
	            stringbuffer.append("    <input name='").append(name).append("' type='checkbox' value='").append(codedtl.cdDtlNo).append("' ").append((values.contains(codedtl.cdDtlNo)) ? "checked='checked'":""); 
	            stringbuffer.append(" style='margin:2px 5px 0px 0px;' />&nbsp;").append(codedtl.cdDtlNm).append("&nbsp;&nbsp;&nbsp;&nbsp;");
	        }
        }
        
        return stringbuffer.toString();
    }
    
    /**
     * Creates the option string.
     *
     * @param s the s
     * @param i the i
     * @return the string
     */
    public String createRadioString(String s, String distributionType, String networkServerType) {
        StringBuffer stringbuffer = new StringBuffer();
        int j = 0;
        
        if(StringUtils.isEmpty(distributionType)) {
        	distributionType = CoConstDef.CD_GENERAL_MODEL;
        }
        
        if(StringUtils.isEmpty(networkServerType)) {
        	networkServerType = CoConstDef.FLAG_NO;
        }
        
        for(int k = codeDtls.size(); j < k; j++) {
            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(j);
            
            if (CoConstDef.FLAG_NO.equals(codedtl.useYn) 
            		|| codedtl.cdDtlNo.equals(CoConstDef.CD_NETWORK_SERVER)) {
            	continue;
            }
            
            stringbuffer.append("<span class='radioSet'>")
            				.append("<input type='radio' name='distributionType' value='")
            				.append(codedtl.cdDtlNo).append("' ").append(" onclick='fn.chgOssNotice(\"").append(codedtl.cdDtlExp).append("\")' ")
            				.append(" id='radio_").append(codedtl.cdDtlNo).append("' ")
            				.append((distributionType.indexOf(codedtl.cdDtlNo)>-1)?"checked='checked'":"").append(" />")
            					.append("<label for='radio_").append(codedtl.cdDtlNo).append("'>").append(codedtl.cdDtlNm).append("</label>")
            			.append("</span>");
        }
        
        stringbuffer.append("<br>&nbsp;● Network service only?&nbsp;&nbsp;");
        stringbuffer.append("<span class=\"radioSet\">")
        				.append("<input name='networkServerType' type='radio' value='Y' id='networkServerType_Y'")
        				.append(networkServerType.equals(CoConstDef.FLAG_YES) ? "checked='checked'" : "").append(" />")
        				.append("<label for='networkServerType_Y'>Yes</label>")
        			.append("</span>");
        stringbuffer.append("<span class=\"radioSet\">")
        				.append("<input name='networkServerType' type='radio' value='N' id='networkServerType_N'")
        				.append(networkServerType.equals(CoConstDef.FLAG_NO) ? "checked='checked'" : "").append(" />")
        				.append("<label for='networkServerType_N'>No</label>")
        			.append("</span>");

        return stringbuffer.toString();
    }

	public String createOptionCheckboxString(String s, String s1, int i) {
		StringBuffer stringbuffer = new StringBuffer();
        int j = 0;
        for(int k = codeDtls.size(); j < k; j++)
        {
            CoCodeDtl codedtl = (CoCodeDtl)codeDtls.get(j);
            
            if (CoConstDef.FLAG_NO.equals(codedtl.useYn)) 
            {
            	continue;
            }
            
            if(s1.equals("oss")) {
            	stringbuffer.append("<li><input type='checkbox' name='mostUsedOssChartDivision' value='").append(codedtl.cdDtlNo).append("' ")
                .append(" id='checkbox_").append(codedtl.cdDtlNo).append("' />")
                .append("<label for='checkbox_").append(codedtl.cdDtlNo).append("'>").append(codedtl.cdDtlNm).append("</label></li>");
            } else {
            	stringbuffer.append("<li><input type='checkbox' name='mostUsedLicenseChartDivision' value='").append(codedtl.cdDtlNo).append("' ")
                .append(" id='checkbox_").append(codedtl.cdDtlNo).append("' />")
                .append("<label for='checkbox_").append(codedtl.cdDtlNo).append("'>").append(codedtl.cdDtlNm).append("</label></li>");
            }
        }

        return stringbuffer.toString();
	}
}
