/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.domain.CoCode;
import oss.fosslight.domain.CoCodeDtl;
import oss.fosslight.domain.CodeDtlBean;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.repository.CodeManagerMapper;
import oss.fosslight.repository.LicenseMapper;
import oss.fosslight.repository.OssMapper;

@Component("CoCodeManager")
@Slf4j
public class CoCodeManager extends CoTopComponent {

    private static CoCodeManager instance;
    private static HashMap<String, CoCode> codes;
    private static Vector<String[]> emptyVector;
    
    public static Map<String, LicenseMaster> LICENSE_INFO = new HashMap<>();
    public static Map<String, LicenseMaster> LICENSE_INFO_UPPER = new HashMap<>();
    public static Map<String, LicenseMaster> LICENSE_INFO_BY_ID = new HashMap<>();

    public static Map<String, OssMaster> OSS_INFO_UPPER = new HashMap<>();
    public static Map<String, OssMaster> OSS_INFO_BY_ID = new HashMap<>();
    public static Map<String, String> OSS_INFO_UPPER_NAMES = new HashMap<>();
    
    public static List<String> LICENSE_USER_GUIDE_LIST = new ArrayList<>();
    
    /** ROLE OUT LICENSE */
    public static String CD_ROLE_OUT_LICENSE = "";
	public static List<String> CD_ROLE_OUT_LICENSE_ID_LIST = new ArrayList<>(); 
	
	private static CodeManagerMapper codeManagerMapper;
	
	public static void setCodeManagerMapper(CodeManagerMapper mapper) {
		codeManagerMapper = mapper;
	}

	private static LicenseMapper licenseMapper;
	
	public static void setLicenseMapper(LicenseMapper mapper) {
		licenseMapper = mapper;
	}
	
	private static OssMapper ossMapper;
	
	public static void setOssMapper(OssMapper mapper) {
		ossMapper = mapper;
	}
	
    public void init() {
        codes = new HashMap<String, CoCode>();
        emptyVector = new Vector<String[]>();
        instance = new CoCodeManager();
        List<CodeDtlBean> list = null;
        CoCode code = null;
        CoCodeDtl codedtl = null;
        
        try {
            list = codeManagerMapper.getCodeListAll();
           
            if (list == null) {
                throw new RuntimeException("SYSTEM ERR GET CODE INFO FAIL");
            }
            
            for (CodeDtlBean vo : list) {
                String s = vo.getCdNo();
                code = (CoCode) codes.get(s);
                
                if (code == null) {
                    code = new CoCode(s, vo.getCdNm());
                    codes.put(s, code);
                }         
                
                codedtl = new CoCodeDtl(
	                    vo.getCdDtlNo(),
	                    vo.getCdSubNo(), 
	                    vo.getCdDtlNm(), 
	                    vo.getCdDtlNm2(), 
	                    vo.getCdDtlExp(),
	                    Integer.parseInt(CommonFunction.avoidNull(vo.getCdOrder(), "1")),
	                    CommonFunction.avoidNull(vo.getUseYn(), CoConstDef.FLAG_YES));
                
                code.addCodeDtl(codedtl);
            }
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
        
        loadLicenseInfo();
        loadOssInfo();
    }
    
	public void refreshOssInfo () {
    	loadOssInfo();
    }
    
	private void loadOssInfo() {
		try {
			List<OssMaster> list = ossMapper.getOssInfoAll();
			List<OssMaster> listNick = ossMapper.getOssInfoAllWithNick();
			List<OssMaster> nickNameList = ossMapper.getOssAllNickNameList();
			Map<String, String[]> nickNameMap = new HashMap<>();
			Map<String, OssMaster> _ossMap = new HashMap<>();
			Map<String, String> _ossNamesMap = new HashMap<>();
			
			if (nickNameList != null) {
				for (OssMaster bean : nickNameList) {
					if (bean.getOssNickname() != null) {
						nickNameMap.put(bean.getOssName(), bean.getOssNickname().split(","));
					}
				}
			}
			
			if (list != null) {
				for (OssMaster bean : list) {
					OssMaster targetBean = null;
					String key = bean.getOssName() +"_"+ avoidNull(bean.getOssVersion()); // oss name을 nick name으로 가져온다.
					key = key.toUpperCase();
					
					if (_ossMap.containsKey(key)) {
						targetBean = _ossMap.get(key);
					} else {
						targetBean = bean;
						
						if (nickNameMap.containsKey(targetBean.getOssNameTemp())) {
							targetBean.setOssNicknames(nickNameMap.get(targetBean.getOssNameTemp()));
						}
					}
					
					OssLicense subBean = new OssLicense();
					subBean.setOssId(bean.getOssId());
					subBean.setLicenseId(bean.getLicenseId());
					subBean.setLicenseName(bean.getLicenseName());
					subBean.setLicenseType(bean.getOssLicenseType());
					subBean.setOssLicenseIdx(bean.getOssLicenseIdx());
					subBean.setOssLicenseComb(bean.getOssLicenseComb());
					subBean.setOssLicenseText(bean.getOssLicenseText());
					subBean.setOssCopyright(bean.getOssCopyright());
					
					// oss의 license type을 license의 license type 적용 이후에 set
					targetBean.setLicenseType(bean.getOssLicenseType());
					
					targetBean.addOssLicense(subBean);
					
					if (_ossMap.containsKey(key)) {
						_ossMap.replace(key, targetBean);
					} else {
						_ossMap.put(key, targetBean);
					}
					
					if (!_ossNamesMap.containsKey(bean.getOssName().toUpperCase())) {
						_ossNamesMap.put(bean.getOssName().toUpperCase(), bean.getOssName());
					}
				}
			}
			
			Map<String, OssMaster> _idMasterMap = new HashMap<>();
			for (OssMaster bean : _ossMap.values()) {
				if (!_idMasterMap.containsKey(bean.getOssId())) {
					_idMasterMap.put(bean.getOssId(), bean);
				}
			}
			
			OSS_INFO_BY_ID = _idMasterMap;
			
			if (listNick != null) {

				for (OssMaster bean : listNick) {
					//OssMaster targetBean = null;
					String key = bean.getOssName() +"_"+ avoidNull(bean.getOssVersion()); // oss name을 nick name으로 가져온다.
					String ossNickNameKey = bean.getOssName().toUpperCase();

					if (!_ossNamesMap.containsKey(ossNickNameKey)) {
						_ossNamesMap.put(ossNickNameKey, bean.getOssNameTemp());
					}
					
					String sourceKey = (bean.getOssNameTemp() + "_" + avoidNull(bean.getOssVersion())).toUpperCase();
					OssMaster sourceBean = _ossMap.get(sourceKey);
					
					bean.setLicenseDiv(sourceBean.getLicenseDiv());
					bean.setDownloadLocation(sourceBean.getDownloadLocation());
					bean.setDownloadLocationGroup(sourceBean.getDownloadLocationGroup());
					bean.setHomepage(sourceBean.getHomepage());
					bean.setSummaryDescription(sourceBean.getSummaryDescription());
					bean.setAttribution(sourceBean.getAttribution());
					bean.setCopyright(sourceBean.getCopyright());
					bean.setCvssScore(sourceBean.getCvssScore());
					bean.setCveId(sourceBean.getCveId());
					bean.setVulnYn(sourceBean.getVulnYn());
					bean.setVulnRecheck(sourceBean.getVulnRecheck());
					bean.setVulnDate(sourceBean.getVulnDate());
					bean.setLicenseType(sourceBean.getLicenseType());
					bean.setOssLicenses(sourceBean.getOssLicenses());
					bean.setOssType(sourceBean.getOssType());
					bean.setMultiLicenseFlag(sourceBean.getMultiLicenseFlag());
					bean.setDualLicenseFlag(sourceBean.getDualLicenseFlag());
					bean.setVersionDiffFlag(sourceBean.getVersionDiffFlag());
					if (sourceBean.getDetectedLicenses() != null) {
						bean.setDetectedLicenses(sourceBean.getDetectedLicenses());
					}
					
					_ossMap.put(key.toUpperCase(), bean);
				}
			
			}
			
			if (!_ossMap.isEmpty()) {
				OSS_INFO_UPPER = _ossMap;
			}
			
			if (!_ossNamesMap.isEmpty()) {
				OSS_INFO_UPPER_NAMES = _ossNamesMap;
			}
		} catch(Exception e) {
        	log.error(e.getMessage(), e);
		}
	}
	
	/**
	 * 모든 라이선스 정보를 load
	 */
    private void loadLicenseInfo() {

        try {
            List<LicenseMaster> list = licenseMapper.getLicenseInfoInit();
            List<LicenseMaster> nickList = licenseMapper.getLicenseInfoInitNick();
            
            if (list == null) {
                throw new RuntimeException("SYSTEM ERR GET CODE LICENSE MASTER INFO");
            }
            
            Map<String, LicenseMaster> license_info_map = new HashMap<>();
            Map<String, LicenseMaster> license_info_upper_map = new HashMap<>();
            Map<String, LicenseMaster> license_info_by_id_map = new HashMap<>();
            for (LicenseMaster vo : list) {
            	if (!isEmpty(vo.getLicenseNicknameStr())) {
            		for (String nick : vo.getLicenseNicknameStr().split("\\|")) {
            			vo.addLicenseNicknameList(nick);
            		}
            	}
            	
            	if (!isEmpty(vo.getRestriction())) {
            		vo.setRestrictionStr(CommonFunction.setLicenseRestrictionList(vo.getRestriction()));
            	}
            	
            	license_info_map.put(vo.getLicenseName(),vo);
            	license_info_upper_map.put(vo.getLicenseName().toUpperCase(),vo);
            	
            	//SHORT_IDENTIFIER
            	if (!isEmpty(vo.getShortIdentifier())) {
            		if (!license_info_map.containsKey(vo.getShortIdentifier())) {
                    	license_info_map.put(vo.getShortIdentifier(),vo);
            		}
            		
            		if (!license_info_upper_map.containsKey(vo.getShortIdentifier().toUpperCase())) {
            			license_info_upper_map.put(vo.getShortIdentifier().toUpperCase(), vo);
            		}
            	}
            	
            	license_info_by_id_map.put(vo.getLicenseId(), vo);
            }
            
            for (LicenseMaster vo : nickList) {
            	
            	LicenseMaster sourceBean = license_info_by_id_map.get(vo.getLicenseId());
            	vo.setLicenseNicknameList(sourceBean.getLicenseNicknameList());
            	vo.setRestrictionStr(sourceBean.getRestrictionStr());
            	            	
            	license_info_map.put(vo.getLicenseName(),vo);
            	license_info_upper_map.put(vo.getLicenseName().toUpperCase(),vo);
            }
            
            if (!license_info_map.isEmpty()) {
            	LICENSE_INFO = license_info_map;
            }
            
            if (!license_info_upper_map.isEmpty()) {
            	LICENSE_INFO_UPPER = license_info_upper_map;
            }
            
            if (!license_info_by_id_map.isEmpty()) {
            	LICENSE_INFO_BY_ID = license_info_by_id_map;
            }

            // 2017.04.27 add by yuns 
            // no opensource license 대상을 하드코딩에서 license master의 license type으로 변경
            List<LicenseMaster> roleOutLicenseList = licenseMapper.getRoleOutLicense();
            List<String> roleOutLicenseNames = new ArrayList<>();
            List<String> roleOutLicenseIds = new ArrayList<>();
            if (roleOutLicenseList != null) {
            	for (LicenseMaster bean : roleOutLicenseList) {
            		if (!roleOutLicenseNames.contains(bean.getLicenseName())) {
            			roleOutLicenseNames.add(bean.getLicenseName());
            		}
            		
            		if (!roleOutLicenseIds.contains(bean.getLicenseId())) {
            			roleOutLicenseIds.add(bean.getLicenseId());
            		}
            	}
            }
            
            String noOpensourceStr = "";
            for (String s : roleOutLicenseNames) { 
            	if (!isEmpty(noOpensourceStr)) {
            		noOpensourceStr += "|";
            	}
            	
            	noOpensourceStr += s;
            }
            
            CD_ROLE_OUT_LICENSE = noOpensourceStr;
            CD_ROLE_OUT_LICENSE_ID_LIST = roleOutLicenseIds;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
        
        if (LICENSE_INFO_UPPER != null) {
        	List<String> _licenseUserGuideList = new ArrayList<>();
        	
        	for (String s : LICENSE_INFO_UPPER.keySet()) {
        		LicenseMaster _license = LICENSE_INFO_UPPER.get(s);
        		if (_license != null && !isEmptyWithLineSeparator(_license.getDescription())) {
        			_licenseUserGuideList.add(s);
        		}
        	}
        	
        	if (!_licenseUserGuideList.isEmpty()) {
        		LICENSE_USER_GUIDE_LIST = _licenseUserGuideList;
        	}
        }
	}
    
    public void refreshLicenseInfo () {
    	loadLicenseInfo();
    }

	private CoCodeManager() {}
    
    public static CoCodeManager getInstance() {
    	if (instance == null) {
    		instance = new CoCodeManager();
    	}
    	
        return instance;
    }

    private static CoCode getCodeInstance(String s) {
        CoCode code = (CoCode) codes.get(s);
       
        if (code == null) {
        	log.debug((new StringBuilder()).append("Code No.").append(s)
                    .append(" is not found. request code no : " + avoidNull(s)).toString());
        }
        
        return code;
    }

    public String getGroupName(String s) {
        CoCode code = getCodeInstance(s);
        
        return code != null ? code.getCdNm() : null;
    }

    public static Vector<String[]> getValues(String s) {
    	return getValues(s, null);
    }
    
    public static Vector<String[]> getValues(String s, String lang) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            return code != null ? code.getCdDtlNoCdDtlNmPairVector(false) : emptyVector;
        }
    }
    
    //새로 추가 : hj-kim - 2016-05-31
    public static String getValuesJson(String s) {
    	return getValuesJson(s, null);
    }
    
    //새로 추가 : hj-kim - 2016-05-31
    public static String getValuesJson(String s, String lang) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            Gson gson = new Gson();
            String jsonStr = gson.toJson(code.getCdDtlNoCdDtlNmPairVectorBean(false));
            
            return code != null ? jsonStr: "[]";
        }
    }
    
    
    public static Vector<String[]> getValuesIgnoreUse(String s) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            return code != null ? code.getCdDtlNoCdDtlNmPairVector(true) : emptyVector;
        }
    }
    
    //새로 추가 : hj-kim - 2016-05-31
    public static String getValuesIgnoreUseJson(String s) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            Vector<CoCodeDtl> t = code.getCdDtlNoCdDtlNmPairVectorBean(true);
            
            Gson gson = new Gson();
            String jsonStr = gson.toJson(t);
            
            return code != null ? jsonStr: "[]";
        }
    }
    
    
    public static Vector<String[]> getAllValues(String s) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            return code != null ? code.getCdAllPairVector(false) : emptyVector;
        }
    }
    
    //새로 추가 : hj-kim - 2016-05-31
    public static String getAllValuesJson(String s) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            Vector<CoCodeDtl> t = code.getCdAllPairVectorBean(false);
            
            Gson gson = new Gson();
            String jsonStr = gson.toJson(t);
            
            return code != null ? jsonStr : "[]";
        }
    }
    
    
    public static Vector<String[]> getAllValuesIgnoreUse(String s) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
           
            return code != null ? code.getCdAllPairVector(true) : emptyVector;
        }
    }
    
    

    public static Vector<String> getCodes(String s) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            return code != null ? code.getCdDtlNoVector(false) : new Vector<String>();
        }
    }
    
    public static Vector<String> getCodesIgnoreUse(String s) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            return code != null ? code.getCdDtlNoVector(true) : null;
        }
    }

    public static Vector<String> getCodeNames(String s) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            return code != null ? code.getCdDtlNmVector(false) : new Vector<String>();
        }
    }
    
    public static Vector<String> getCodeNames(String s, String lang) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            return code != null ? code.getCdDtlNmVector(false) : new Vector<String>();
        }
    }
    
    
    public static String getCodeNamesJson(String s) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            Vector<String> t = code.getCdDtlNmVector(false);
            
            Gson gson = new Gson();
            String jsonStr = gson.toJson(t);
            
            return code != null ? jsonStr : "[]";
        }
    }
    
	public static String getCodeNamesJson(String s, String lang) {
		if (instance == null) {
			throw new IllegalStateException();
		} else {
			CoCode code = getCodeInstance(s);

			Vector<String> t = code.getCdDtlNmVector(false);

			Gson gson = new Gson();
			String jsonStr = gson.toJson(t);

			return code != null ? jsonStr : "[]";
		}
	}
    
    
	public static Vector<String> getCodeNamesIgnoreUse(String s) {
		if (instance == null) {
			throw new IllegalStateException();
		} else {
			CoCode code = getCodeInstance(s);
			
			return code != null ? code.getCdDtlNmVector(true) : null;
		}
	}

    public static String getCodeString(String s, String s1) {
        if (instance == null) {
            throw new IllegalStateException();
        } else {
            CoCode code = getCodeInstance(s);
            
            return code != null ? code.getCdDtlNm(s1) : "";
        }
    }

    public static String getCodeExpString(String s, String s1) {
        CoCode code = getCodeInstance(s);
        
        return code != null ? code.getCdDtlExp(s1) : "";
    }
    
    public static String getCodeExpStringAppend(String RootCd, String RootDtlCd, String SubDtlCd) {
        return getCodeExpStringAppend(RootCd, RootDtlCd, RootCd, SubDtlCd);
    }
    
    public static String getCodeExpStringAppend(String RootCd, String RootDtlCd, String SubCd, String SubDtlCd) {
        return getCodeExpString(RootCd, RootDtlCd) + getCodeExpString(SubCd, SubDtlCd);
    }
    
    public static String getCodeExpStringByName(String s, String s1) {
        CoCode code = getCodeInstance(s);
        
        return code != null ? code.getCdDtlExpByName(s1) : "";
    }
    
    public static String getSubCodeNo(String s, String s1) {
        CoCode code = getCodeInstance(s);
        
        return code != null ? code.getCdSubCdNo(s1) : "";
    }
    
    public static int getCodePriority(String s, String s1) {
        CoCode code = getCodeInstance(s);
        
        return code != null ? code.getCdDtlPrior(s1) : -1;
    }

    public String genComboBox(String s, String s1) {
        return genComboBox(s, s1, null, -1);
    }

    public String genComboBox(String s, String s1, String s2, int i) {
        CoCode code = getCodeInstance(s);
        
        if (code != null) {
            return code.createComboBoxString(s1, s2, i);
        } else {
            return "<select name='" + s1 + "'>\n</select>\n";
        }
    }
    
    public static String genOption(String s) {
    	 return genOption(s, null, -1);
    }
    
    public static String genOption(String s, String s1) {
    	return genOption(s, s1, 0);
    }
    
    public static String genOption(String s, String s1, int i) {
        CoCode code = getCodeInstance(s);
        
        if (code != null) {
            return code.createOptionString(s1, i);
        } else {
            return null;
        }
    }
	
    public static String genOptionCheckbox(String s, String s1) {
   	 	return genOptionCheckbox(s, s1, -1);
    }
    
	private static String genOptionCheckbox(String s, String s1, int i) {
		CoCode code = getCodeInstance(s);
        
        if (code != null) {
            return code.createOptionCheckboxString(s, s1, i);
        } else {
            return null;
        }
	}

	public static String genRadio(String s, String distributionType, String networkServerType) {
        CoCode code = getCodeInstance(s);
        
        if (code != null) {
            return code.createRadioString(s, distributionType, networkServerType);
        } else {
            return null;
        }
    }
	
    public static String genCheckbox(String s, String status, String callType) {
        CoCode code = getCodeInstance(s);
       
        if (code != null) {
            return code.createCheckboxString(status, s, callType);
        } else {
            return null;
        }
    }
    
    public static String genCommonCheckbox(String s, String name, String val, Boolean NAExceptionFlag) {
    	CoCode code = getCodeInstance(s);
		
    	if (!StringUtils.isEmpty(code)) {
    		return code.createCommonCheckboxString(val, name, NAExceptionFlag);
    	} else {
    		return null;
    	}
    }

    public String getCode(String s) {
        Vector<String> vector = getCodes(s);
        
        if (vector != null) {
            return (String) vector.get(0);
        } else {
            return null;
        }
    }
    
    public void refreshCodes() {

        HashMap<String, CoCode> hashmap = new HashMap<String, CoCode>();
        List<CodeDtlBean> list = null;
        CoCode code = null;
        CoCodeDtl codedtl = null;

        try {
			list = codeManagerMapper.getCodeListAll();
			
	        if (list == null) {
	            throw new RuntimeException("SYSTEM ERR GET CODE INFO FAIL");
	        }
	        
	        for (CodeDtlBean vo : list) {
	            String s = vo.getCdNo();
	            code = (CoCode) hashmap.get(s);
	            
	            if (code == null) {
	                code = new CoCode(s, vo.getCdNm());
	                hashmap.put(s, code);
	            }
	            
	            codedtl = new CoCodeDtl(
		                      vo.getCdDtlNo()
		                    , vo.getCdSubNo() 
		                    , vo.getCdDtlNm() 
		                    , vo.getCdDtlNm2() 
		                    , vo.getCdDtlExp()
		                    , Integer.parseInt(CommonFunction.avoidNull(vo.getCdOrder(), "1"))
		                    , vo.getUseYn());
	            
	            code.addCodeDtl(codedtl);
	        }
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
        
        if (!hashmap.isEmpty()) {
            codes = hashmap;
            
            // Const 파일에 직접 참조하는 코드 value 가 있을 경우 변경해줘야함
            CoConstDef.DISP_PAGENATION_DEFAULT = getCodes(CoConstDef.CD_PAGENATION).firstElement();
            CoConstDef.DISP_PAGENATION_LIST_STR = CommonFunction.arrayToString(CoCodeManager.getCodes(CoConstDef.CD_PAGENATION));
        }
    
    }
    
    public static Vector<String> getStandardCodeExp(String s){
    	Vector<String> ret = null;
    	Vector<String> codes = null;
    	Vector<String> exps = new Vector<String>();
    	int size = 0;
    	int i = 0;
    	int j = 0;
    	
    	if (instance == null) {
             throw new IllegalStateException();
        } else {
             CoCode code = getCodeInstance(s);
             codes = code != null ? code.getCdDtlNoVector(false) : new Vector<String>();
             
             while (i < codes.size()){
        		 if (!("").equals(codes.get(i))){
        			 String temp = code != null ? code.getCdDtlExp(codes.get(i)) : "";
        			 
        			 if (!temp.equals("")){
        				 exps.add(temp);
        				 CoCode code_ = getCodeInstance(temp);
        				 Vector<String> temps = code_ != null ? code_.getCdDtlNoVector(false) : null;
        				 
        				 if (temps != null && temps.size() > size){
        					 size = temps.size();
        					
        					 j++;
        				 }
        			 }
        		 }
        		 
        		 i++;
        	 }
             
             if (size > 0){
            	 code = getCodeInstance(exps.get(j-1));
				 ret = code != null ? code.getCdDtlNoVector(false) : null;
             }
         }
    	
    	 return ret;
    }
    
    public static String getLicenseUserGuideJsonString() {
    	String jsonStr = null;
    	
    	if (LICENSE_USER_GUIDE_LIST != null) {
            Gson gson = new Gson();
            jsonStr = gson.toJson(LICENSE_USER_GUIDE_LIST);
    	}
    	
        return !isEmpty(jsonStr) ? jsonStr : "[]";
    }
    
    public static String getCommentColor(String code) {
        String returnStr = "";
        
        switch (code) {
	        case "10": // Identification
	            returnStr = "#ffb526";
	           
	            break;
	        case "12": // Pacaging
	            returnStr = "#4cf58c";
	            
	            break;
	        case "14": // Distribution
	            returnStr = "#f54cf3";
	            
	            break;
	        case "19": // project Info
	            returnStr = "#059bf1";
	            
	            break;
	        case "60": // Security
	            returnStr = "#02304b";
	            
	            break;
	        default:
	            break;
        }
            
        return returnStr;
    }
    
    public static Vector<CoCodeDtl> getCodeDtls(String s) {
    	CoCode code = getCodeInstance(s);
    	if (code != null) {
    		Vector<CoCodeDtl> codeDtls = new Vector<>();
    		for (CoCodeDtl dtl : code.getCodeDtls()) {
    			if (!dtl.getUseYn().equals(CoConstDef.FLAG_NO)) {
    				codeDtls.add(dtl);
    			}
    		}
    		return codeDtls;
    	} else {
    		return null;
    	}
    }

}
