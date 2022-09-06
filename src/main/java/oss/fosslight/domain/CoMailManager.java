/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.domain;

import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import difflib.DiffUtils;
import difflib.Patch;
import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.repository.MailManagerMapper;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.repository.T2UserMapper;
import oss.fosslight.service.FileService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.StringUtil;

/**
 * The Class CoMailManager.
 */
@Component
@Slf4j
public class CoMailManager extends CoTopComponent {
	
	/** The instance. */
	private static CoMailManager instance;
	private static JavaMailSender mailSender;
	private static MailManagerMapper	mailManagerMapper;
	private static T2UserMapper	userMapper;
	private static ProjectService projectService;
	private static FileService fileService;
	private static OssMapper ossMapper;
	
	private static String DEFAULT_BCC;
	private static String[] BAT_FAILED_BCC;
	private static String[] MAIL_LIST_SECURITY;

	/** The conn str. */
	private static String connStr;
	
	/** The conn user. */
	private static String connUser;

	/** The conn pw. */
	private static String connPw;
		
	/**
	 * Instantiates a new co mail manager.
	 */
	private CoMailManager() { }

    /**
     * Gets the single instance of CoMailManager.
     *
     * @return single instance of CoMailManager
     */
    public static CoMailManager getInstance() {
    	if(instance == null) {
    		instance = new CoMailManager();
        	mailSender = (JavaMailSender) getWebappContext().getBean(JavaMailSender.class);
        	mailManagerMapper = (MailManagerMapper) getWebappContext().getBean(MailManagerMapper.class);
        	userMapper = (T2UserMapper) getWebappContext().getBean(T2UserMapper.class);
        	projectService = (ProjectService) getWebappContext().getBean(ProjectService.class);
        	fileService = (FileService) getWebappContext().getBean(FileService.class);
        	ossMapper = (OssMapper) getWebappContext().getBean(OssMapper.class);
            DEFAULT_BCC = avoidNull(CommonFunction.getProperty("smtp.default.bcc"));
            BAT_FAILED_BCC = avoidNull(CommonFunction.getProperty("smtp.default.bat")).split(",");	// (To be added) BAT Detail setting
            MAIL_LIST_SECURITY = avoidNull(CommonFunction.getProperty("smtp.default.security")).split(","); // (To be added) Vulnerability Detail setting
            connStr = CommonFunction.makeJdbcUrl( CommonFunction.getProperty("spring.datasource.url"));
            connUser = CommonFunction.getProperty("spring.datasource.username");
            connPw = CommonFunction.getProperty("spring.datasource.password");
		}
        return instance;
    }
	
    /**
     * Send mail.
     *
     * @param bean the bean
     * @return true, if successful
     */
    @SuppressWarnings("unchecked")
	public boolean sendMail(CoMail bean) {
    	boolean procResult = true;
    	
    	boolean EMAIL_USE_FLAG = CoConstDef.FLAG_YES.equals(CoCodeManager.getCodeExpString(CoConstDef.CD_SYSTEM_SETTING, CoConstDef.CD_SMTP_USED_FLAG));
    	
    	if(!EMAIL_USE_FLAG) {
    		return procResult;
    	}
    	
    	try {
    		// Check the required things
    		if(bean == null || isEmpty(bean.getMsgType())) {
    			log.error("Mail Bean or Type is Empty");
    			return false;
    		}

    		boolean isTest = !"REAL".equals(avoidNull(CommonFunction.getProperty("server.mode"))); 

    		Map<String, Object> convertDataMap = new HashMap<>();
    		convertDataMap.put("mailType", bean.getMsgType());
    		convertDataMap.put("isModify", false); // Check if the data are modified
    		String msgType = bean.getMsgType();
    		
    		// To use the same title in case of recalculated
    		if(CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_REMOVE_RECALCULATED.equals(msgType)) {
    			msgType = CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED;
    		}
    		
			String title = (isTest ? "[TEST]" : "") + CoCodeManager.getCodeString(CoConstDef.CD_MAIL_TYPE, msgType);

			title = makeMailSubject(title, bean);
			// Add a direct link to OSS information (ADMIN ONLY)
			if(CoConstDef.CD_MAIL_TYPE_OSS_REGIST.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_OSS_UPDATE.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_ADDNICKNAME_UPDATE.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_OSS_MODIFIED_COMMENT.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_OSS_DEACTIVATED.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_OSS_ACTIVATED.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_LICENSE_REGIST.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE_TYPE.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_LICENSE_RENAME.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_LICENSE_MODIFIED_COMMENT.equals(bean.getMsgType())
					) {
				convertDataMap.put("contentsTitle", StringUtil.replace(makeMailSubject((isTest ? "[TEST]" : "") + CoCodeManager.getCodeString(CoConstDef.CD_MAIL_TYPE, bean.getMsgType()), bean, true), "[FOSSLight]", ""));
			} else {
				convertDataMap.put("contentsTitle", StringUtil.replace(title, "[FOSSLight]", ""));
			}
    		
    		// Get component information
    		String mailComponents = CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_COMPONENT, bean.getMsgType());

			// Acquire data by component without comparing changes (compare VO data directly)
			if (!isEmpty(mailComponents)) {
				for (String component : mailComponents.split(",")) {
					if (!isEmpty(component)) {
						component = component.trim();
						Object _contents = setContentsInfo(bean, component);
						if (_contents != null) {
							if (CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_PROJECT_RECALCULATED_ALL.equals(component)) {
								component = CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_OSS;
							}
							convertDataMap.put(CoCodeManager.getCodeString(CoConstDef.CD_MAIL_COMPONENT_NAME, component), _contents);
						}
					}
				}
			}
    		
    		if(CoConstDef.CD_MAIL_TYPE_OSS_DELETE.equals(bean.getMsgType()) && bean.getParamOssInfo() != null) {
    			OssMaster oss_basic_info = bean.getParamOssInfo();
    			String result = appendChangeStyleLinkFormatArray(oss_basic_info.getDownloadLocation());
				oss_basic_info.setDownloadLocation(result);
				
    			convertDataMap.put(CoCodeManager.getCodeString(CoConstDef.CD_MAIL_COMPONENT_NAME, CoConstDef.CD_MAIL_COMPONENT_OSSBASICINFO), oss_basic_info); // oss_basic_info
    		}
    		
    		if(CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_REJECT.equals(bean.getMsgType()) && bean.getParamPrjInfo() != null) {

				// In case of distribution reject, distribution info must be received as a separate parameter (already deleted)
    			convertDataMap.put(CoCodeManager.getCodeString(CoConstDef.CD_MAIL_COMPONENT_NAME, CoConstDef.CD_MAIL_COMPONENT_PROJECT_DISTRIBUTIONINFO), bean.getParamPrjInfo()); // oss_basic_info
    		}
    		
    		// TODO 92번 메일 ( vulnerability recalculated )의 경우 as-is to-be 가 존재하기 때문에 예외처리한다.
    		// 94번 메일 NVD Data > CVSS Score가 9.0이상인 대상중 현재 배치로인해 NVD Data가 삭제된 경우 mail 발송
    		if(CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED.equals(bean.getMsgType())
    				|| CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_REMOVE_RECALCULATED.equals(bean.getMsgType())) {
    			if(bean.getParamOssInfoMap() == null || bean.getParamOssInfoMap().isEmpty()) {
    				return false;
    			}
    			
    			String recalculatedMsgType = CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED.equals(bean.getMsgType()) 
    					? CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_RECALCULATED 
    					: CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_REMOVE_RECALCULATED;
    			
    			// 변경된 oss id 과 해당 프로젝트에서 사용중인 oss 목록을 비교하기 위해서 210 번 쿼리를 사용한다.
    			List<OssMaster> _contents = (List<OssMaster>) setContentsInfo(bean, recalculatedMsgType);
    			List<OssMaster> _reMakeContents = new ArrayList<>();
    			for(OssMaster _bean : _contents) {
					String key = _bean.getOssName().toUpperCase()+"_"+_bean.getOssVersion();
					
    				if(bean.getParamOssInfoMap().containsKey(key)) {
    					OssMaster reMakeBean = bean.getParamOssInfoMap().get(key);
    					OssMaster diffBean = CoCodeManager.OSS_INFO_UPPER.get(key);
    					if(diffBean == null) {
    						return false;
    					}
    					
						if("0".equals(reMakeBean.getCvssScoreTo())) { 
							reMakeBean.setCveIdTo("NONE");
						}
						
						_reMakeContents.add(reMakeBean);
    				}
    			}
    			
    			if(_reMakeContents.isEmpty()) {
    				return false;
    			}

    			convertDataMap.put("vulnerability_prj_recalc_oss_info", _reMakeContents);
    			
    		}
    		
    		// ldap Search시 사용자 정보가 변경된 경우
    		if(CoConstDef.CD_MAIL_TYPE_CHANGED_USER_INFO.equals(bean.getMsgType()) && bean.getParamList() != null) {
    			List<Map<String, Object>> userList = bean.getParamList();
    			List<T2Users> changedUserList = new ArrayList<>();
    			List<T2Users> retireeUserList = new ArrayList<>();
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	        Calendar c1 = Calendar.getInstance();
    	        String strToday = sdf.format(c1.getTime());
    	        
    			for(Map<String, Object> userInfo : userList) {
    				T2Users t2user = new T2Users();
    				t2user.setUserId((String) userInfo.get("userId"));
    				t2user.setModifiedDate(strToday);
    				
    				// 정보 변경 사용자
    				if(userInfo.containsKey("beforeUserName")) {
    					String userName  = (String) userInfo.get("beforeUserName");
    						   userName += " -> ";
    						   userName += (String) userInfo.get("afterUserName");
    						   
    					t2user.setUserName(userName);
    					
    					changedUserList.add(t2user);
    				}
    				// 퇴직자
    				else {
    					t2user.setUserName((String) userInfo.get("afterUserName"));
    					String useYn  = (String) userInfo.get("beforeUseYn");
		    				   useYn += " -> ";
		    				   useYn += (String) userInfo.get("afterUseYn");
		    				   
    					t2user.setUseYn(useYn);
    					
    					retireeUserList.add(t2user);
    				}
    				
    			}
    			
    			if(changedUserList.size() > 0) {
    				convertDataMap.put("changed_user_info", changedUserList);
    			}
    			
    			if(retireeUserList.size() > 0) {
    				convertDataMap.put("retiree_user_info", retireeUserList);
    			}
    			
    		}
    		
    		// Common
    		{
        		if(isEmpty(bean.getCreationUserId())) {
        			bean.setCreationUserId(bean.getLoginUserName());
        		}
        		
        		if(!isEmpty(bean.getComment())) {
        			// FIXME
        			if(CoConstDef.CD_MAIL_TYPE_PROJECT_MODIFIED_COMMENT.equals(bean.getMsgType())
        					||CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_MODIFIED_COMMENT.equals(bean.getMsgType())
        					||CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_MODIFIED_COMMENT.equals(bean.getMsgType())
        					||CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_MODIFIED_COMMENT.equals(bean.getMsgType())
        					||CoConstDef.CD_MAIL_TYPE_PARTER_MODIFIED_COMMENT.equals(bean.getMsgType())
        					||CoConstDef.CD_MAIL_TYPE_OSS_MODIFIED_COMMENT.equals(bean.getMsgType())
        					||CoConstDef.CD_MAIL_TYPE_LICENSE_MODIFIED_COMMENT.equals(bean.getMsgType())
        					) {
        				bean.setComment(appendChangeStyleMultiLine(bean.getParamExpansion1(), bean.getParamExpansion2()));
        			}
        			convertDataMap.put("comment", bean.getComment());
        		}
    			
    			if(isEmpty(bean.getModifier())) {
    				bean.setModifier(bean.getLoginUserName());
    			}
    			convertDataMap.put("modifier", bean.getModifier());
    			if(isEmpty(bean.getModifiedDate())) {
    				bean.setModifiedDate(DateUtil.getCurrentDateAsString());
    			}
    			convertDataMap.put("modifiedDate", bean.getModifiedDate());

    			T2Users userParam = new T2Users();
    			userParam.setUserId(bean.getModifier());
    			T2Users userInfo = userMapper.getUser(userParam);
    			
    			convertDataMap.put("modifierNm", makeUserNameFormatWithDivision(userInfo));
    			
    			// In case of comparing changes
    			if(bean.getCompareDataBefore() != null) {
    				convertDataMap.put("before", bean.getCompareDataBefore());
    			}

    			if(bean.getCompareDataAfter() != null) {
    				convertDataMap.put("after", bean.getCompareDataAfter());
    			}
    			
    			// 프로젝트 기본정보가 변경된 경우, 코드변환하여 다시 맵에 격납한다.
    			if(CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED.equals(bean.getMsgType())
    					|| CoConstDef.CD_MAIL_TYPE_PROJECT_COPIED.equals(bean.getMsgType())) {
    				convertDataMap.put("before", convertCodeProjectBaiscInfo( bean.getCompareDataBefore()));
    				convertDataMap.put("after", convertCodeProjectBaiscInfo( bean.getCompareDataAfter()));
    			}
    			
    			if(CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION.equals(bean.getMsgType())) {
    				convertDataMap.put("paramOssInfo", bean.getParamOssInfo());
    				convertModifiedData(convertDataMap, bean.getMsgType());
				}
    			
    			// as-is, to-be 비교 메일의 경우, 변경 부분 서식을 위한 처리추가
    			if(bean.getCompareDataBefore() != null || bean.getCompareDataAfter() != null) {
    				convertModifiedData(convertDataMap, bean.getMsgType());
    			}
    			
    			// 17.08.02 sw-yun 22번 메일은 더이상 사용하지 않고, 21번 메일에 포함
    			if(CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE.equals(bean.getMsgType()) || CoConstDef.CD_MAIL_TYPE_LICENSE_RENAME.equals(bean.getMsgType())) {
    				// license type이 변경된 경우 사용중인 프로젝트 리스트를 표시
    				if(!isEmpty(bean.getParamLicenseId()) 
    						&&  bean.getCompareDataBefore() != null && bean.getCompareDataAfter() != null 
    						&& !((LicenseMaster) bean.getCompareDataBefore()).getLicenseType().equals(((LicenseMaster) bean.getCompareDataAfter()).getLicenseType())) {
    					
    					// license type이 변경된 oss list
    					
    					if(bean.getParamOssList() != null) {
    						
    						List<Project> prjListFinal = new ArrayList<>();
    						List<String> duplCheckPrjIdList = new ArrayList<>();
    						for(OssMaster ossInfo : bean.getParamOssList()) {
            					List<Project> prjList = ossMapper.getOssChangeForUserList(ossInfo);
            					
                				if(prjList != null && !prjList.isEmpty()) {
                    				for(Project prjBean : prjList) {
                    					if(!duplCheckPrjIdList.contains(prjBean.getPrjId())) {
                          					prjBean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, prjBean.getDistributionType()));
                        					prjBean.setCreator(makeUserNameFormatWithDivision(prjBean.getCreator()));
                        					prjBean.setCreatedDate(CommonFunction.formatDateSimple(prjBean.getCreatedDate()));
                        					prjBean.setReviewer(makeUserNameFormatWithDivision(prjBean.getReviewer()));
                        					prjListFinal.add(prjBean);
                        					duplCheckPrjIdList.add(prjBean.getPrjId());
                    					}
                    				}
                				}    
    						}
    						
    						if(!prjListFinal.isEmpty()) {
    							convertDataMap.put("projectList", prjListFinal);    	
    						}
						
    						// 메일 형식으로 코드 변환
    						for(OssMaster ossInfo : bean.getParamOssList()) {
    							
    							if(!isEmpty(ossInfo.getOssVersion())) {
    								ossInfo.setOssName(ossInfo.getOssName() + " (" + ossInfo.getOssVersion() +")");
    							}
    							
    							ossInfo.setLicenseName(CommonFunction.makeLicenseExpression(ossInfo.getOssLicenses()));
    							ossInfo.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, ossInfo.getLicenseType()));
    							ossInfo.setObligation(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, ossInfo.getObligationType()));

    							ossInfo.setOrgLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, ossInfo.getOrgLicenseType()));
    							ossInfo.setOrgObligationType(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, ossInfo.getOrgObligationType()));
    						}
							convertDataMap.put("ossList", bean.getParamOssList());
    					}

    				}
    			}

    			// 17.08.02 sw-yun 12번 메일은 더이상 사용하지 않고, 11번 메일에 포함
    			if(CoConstDef.CD_MAIL_TYPE_OSS_UPDATE.equals(bean.getMsgType()) 
    					|| CoConstDef.CD_MAIL_TYPE_ADDNICKNAME_UPDATE.equals(bean.getMsgType()) 
    					|| CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME.equals(bean.getMsgType())) {
    				if(!isEmpty(bean.getParamOssId()) 
    						&&  bean.getCompareDataBefore() != null && bean.getCompareDataAfter() != null 
    						&& !((OssMaster) bean.getCompareDataBefore()).getLicenseType().equals(((OssMaster) bean.getCompareDataAfter()).getLicenseType())) {
    					OssMaster paramVo = CoCodeManager.OSS_INFO_BY_ID.get(bean.getParamOssId());
    					if(paramVo == null) {
    						paramVo = (OssMaster) bean.getCompareDataBefore();
    					}
        				List<Project> prjList = ossMapper.getOssChangeForUserList(paramVo);
        				if(prjList != null && !prjList.isEmpty()) {
            				for(Project prjBean : prjList) {
            					prjBean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, prjBean.getDistributionType()));
            					prjBean.setCreator(makeUserNameFormatWithDivision(prjBean.getCreator()));
            					prjBean.setCreatedDate(CommonFunction.formatDateSimple(prjBean.getCreatedDate()));
            					prjBean.setReviewer(makeUserNameFormatWithDivision(prjBean.getReviewer()));
            				}
            				convertDataMap.put("projectList", prjList);    	
        				}
    				}
    			}
    			
    			if(CoConstDef.CD_MAIL_TYPE_OSS_RENAME.equals(bean.getMsgType())) {
    				if(!isEmpty(bean.getParamOssId()) 
    						&&  bean.getCompareDataBefore() != null && bean.getCompareDataAfter() != null) {
        				List<Project> prjList = ossMapper.getOssChangeForUserList((OssMaster) bean.getCompareDataBefore());
        				if(prjList != null && !prjList.isEmpty()) {
            				for(Project prjBean : prjList) {
            					prjBean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, prjBean.getDistributionType()));
            					prjBean.setCreator(makeUserNameFormatWithDivision(prjBean.getCreator()));
            					prjBean.setCreatedDate(CommonFunction.formatDateSimple(prjBean.getCreatedDate()));
            					prjBean.setReviewer(makeUserNameFormatWithDivision(prjBean.getReviewer()));
            				}
            				convertDataMap.put("projectList", prjList);    	
        				}
    				}
    			}
    			
    			if(CoConstDef.CD_MAIL_TYPE_OSS_DELETE.equals(bean.getMsgType())) {
    				if(!isEmpty(bean.getParamOssId()) ) {
    					
        				List<Project> prjList = ossMapper.getOssChangeForUserList(bean.getParamOssInfo());
        				if(prjList != null && !prjList.isEmpty()) {
            				for(Project prjBean : prjList) {
            					prjBean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, prjBean.getDistributionType()));
            					prjBean.setCreator(makeUserNameFormatWithDivision(prjBean.getCreator()));
            					prjBean.setCreatedDate(CommonFunction.formatDateSimple(prjBean.getCreatedDate()));
            					prjBean.setReviewer(makeUserNameFormatWithDivision(prjBean.getReviewer()));
            				}
            				convertDataMap.put("projectList", prjList);    	
        				}
    				}
    				
    			}
				

				if(CoConstDef.CD_MAIL_TOKEN_CREATE_TYPE.equals(bean.getMsgType())) {
					T2Users user = new T2Users();
					user.setUserId(bean.getParamUserId());
					user = userMapper.getUser(user);
					
					convertDataMap.put("mailType", CoConstDef.CD_MAIL_TOKEN_CREATE_TYPE);
					convertDataMap.put("token", user.getToken());
					convertDataMap.put("expireDate", user.getExpireDate());
					convertDataMap.put("tokenInfo", CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TOKEN_CREATE_TYPE));
	    		}
				

				if(CoConstDef.CD_MAIL_TOKEN_DELETE_TYPE.equals(bean.getMsgType())) {
					convertDataMap.put("mailType", CoConstDef.CD_MAIL_TOKEN_DELETE_TYPE);
					convertDataMap.put("tokenInfo", CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_DEFAULT_CONTENTS, CoConstDef.CD_MAIL_TOKEN_DELETE_TYPE));
	    		}
				
				if(CoConstDef.CD_MAIL_PACKAGING_UPLOAD_SUCCESS.equals(bean.getMsgType())
						|| CoConstDef.CD_MAIL_PACKAGING_UPLOAD_FAILURE.equals(bean.getMsgType())) {
					convertDataMap.put("mailType", bean.getMsgType());
					convertDataMap.put("prjId", bean.getParamPrjId());
					convertDataMap.put("fileName", bean.getParamExpansion1());
					convertDataMap.put("errorMsg", bean.getParamExpansion2());
	    		}
    			
				if(CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_INVATED.equals(bean.getMsgType())
						|| CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_REGISTED.equals(bean.getMsgType())
						|| CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_INVATED.equals(bean.getMsgType())
						|| CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_REGISTED.equals(bean.getMsgType())
						|| CoConstDef.CD_MAIL_TYPE_BAT_WATCHER_INVATED.equals(bean.getMsgType())
						|| CoConstDef.CD_MAIL_TYPE_BAT_WATCHER_REGISTED.equals(bean.getMsgType())
						|| CoConstDef.CD_MAIL_TYPE_PROJECT_REQUESTTOOPEN_COMMENT.equals(bean.getMsgType())
						|| CoConstDef.CD_MAIL_TYPE_SELFCHECK_PROJECT_WATCHER_INVATED.equals(bean.getMsgType())
						) {
					if(!isEmpty(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_TYPE, bean.getMsgType()))) {
						String subTitle = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_TYPE, bean.getMsgType()));

						Project project = null;
						PartnerMaster partner = null;
						BinaryMaster binary = null;
						
						if(subTitle.indexOf("${Project Name}") > -1) {
							String _s = "";
							if(!isEmpty(bean.getParamPrjId())) {
								project = mailManagerMapper.getProjectInfoById(bean.getParamPrjId());
							}
							
							if(project != null) {
								if(subTitle.indexOf("${Project Name}") > -1) {
									if(subTitle.indexOf("${Project ID}") < 0) {
										_s += "(" + bean.getParamPrjId() +")";
									}
									_s += project.getPrjName();
									if(!isEmpty(project.getPrjVersion())) {
										_s += " (" + project.getPrjVersion() +")";
									}
								}
							}

							subTitle = StringUtil.replace(subTitle, "${Project Name}", _s);
							if(subTitle.indexOf("${Project Name}") > -1) {
								subTitle = StringUtil.replace(subTitle, "${Project Name}", _s);
							}
						}
						
						if(subTitle.indexOf("${Project ID}") > -1) {
							String _s = avoidNull(bean.getParamPrjId());
							subTitle = StringUtil.replace(subTitle, "${Project ID}", _s);
							if(subTitle.indexOf("${Project ID}") > -1) {
								subTitle = StringUtil.replace(subTitle, "${Project ID}", _s);
							}
						}
						
						if(subTitle.indexOf("${3rd Party Name}") > -1) {
							String _s = "";
							if(!isEmpty(bean.getParamPartnerId())) {
								partner = mailManagerMapper.getPartnerInfo(bean.getParamPartnerId());
							}
							
							if(partner != null) {
								if(subTitle.indexOf("${3rd Party Name}") > -1) {
									if(subTitle.indexOf("${3rd Party ID}") < 0) {
										_s += "(" + partner.getPartnerId() +")";
									}
									_s += partner.getPartnerName();
								}
							}

							subTitle = StringUtil.replace(subTitle, "${3rd Party Name}", _s);
							if(subTitle.indexOf("${3rd Party Name}") > -1) {
								subTitle = StringUtil.replace(subTitle, "${3rd Party Name}", _s);
							}
						}
						
						if(subTitle.indexOf("${3rd Party ID}") > -1) {
							String _s = avoidNull(bean.getParamPartnerId());
							subTitle = StringUtil.replace(subTitle, "${3rd Party ID}", _s);
							if(subTitle.indexOf("${3rd Party ID}") > -1) {
								subTitle = StringUtil.replace(subTitle, "${3rd Party ID}", _s);
							}
						}
						
						if(subTitle.indexOf("${SelfCheck Project Name}") > -1) {
							String _s = "";
							if(!isEmpty(bean.getParamPrjId())) {
								project = mailManagerMapper.getSelfCheckProjectInfoById(bean.getParamPrjId());
							}
							
							if(project != null) {
								if(subTitle.indexOf("${SelfCheck Project Name}") > -1) {
									_s += "(" + bean.getParamPrjId() +")";  // SelfCheck project ID
									_s += project.getPrjName();				// SelfCheck Project Name
									if(!isEmpty(project.getPrjVersion())) { // SelfCheck Project Version
										_s += " (" + project.getPrjVersion() +")";
									}
								}
							}

							subTitle = StringUtil.replace(subTitle, "${SelfCheck Project Name}", _s);
							if(subTitle.indexOf("${SelfCheck Project Name}") > -1) {
								subTitle = StringUtil.replace(subTitle, "${SelfCheck Project Name}", _s);
							}
						}
						
						if(subTitle.indexOf("${Binary Name}") > -1) {
							String _s = "";
							if(!isEmpty(bean.getParamPrjId())) {
								binary = mailManagerMapper.getBinaryInfo(bean.getParamBatId());
							}
							
							if(binary != null) {
								if(subTitle.indexOf("${Binary Name}") > -1) {
									if(subTitle.indexOf("${Binary ID}") < 0) {
										_s += "(" + binary.getBatId() +")";
									}
									_s += binary.getBinaryFileName();
								}
							}

							subTitle = StringUtil.replace(subTitle, "${Binary Name}", _s);
							if(subTitle.indexOf("${Binary Name}") > -1) {
								subTitle = StringUtil.replace(subTitle, "${Binary Name}", _s);
							}
						}
						
						if(subTitle.indexOf("${Binary ID}") > -1) {
							String _s = avoidNull(bean.getParamBatId());
							subTitle = StringUtil.replace(subTitle, "${Binary ID}", _s);
							if(subTitle.indexOf("${Binary ID}") > -1) {
								subTitle = StringUtil.replace(subTitle, "${Binary ID}", _s);
							}
						}

						if(subTitle.indexOf("${Email}") > -1) {
							String _convEmail = avoidNull(bean.getParamEmail());

							subTitle = StringUtil.replace(subTitle, "${Email}", _convEmail);
						}

						if(subTitle.indexOf("${User}") > -1) {
							String _convUser = avoidNull(makeUserNameFormatWithDivision(!isEmpty(bean.getParamUserId()) ? bean.getParamUserId() : bean.getLoginUserName()));

							subTitle = StringUtil.replace(subTitle, "${User}", _convUser);
							if(subTitle.indexOf("${User}") > -1) {
								subTitle = StringUtil.replace(subTitle, "${User}", _convUser);
							}
						}
						
						if(subTitle.indexOf("${Stage}") > -1) {
							String _stage = avoidNull(bean.getStage());

							subTitle = StringUtil.replace(subTitle, "${Stage}", _stage);
						}
						
						convertDataMap.put("contentsSubTitle", subTitle);
						
					}
				} else if(CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DELETED.equals(bean.getMsgType()) 
						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DIFF_FILE.equals(bean.getMsgType())) {

					if(!isEmpty(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_TYPE, bean.getMsgType()))) {
						String subTitle = avoidNull(CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_TYPE, bean.getMsgType()));
						
						if(subTitle.indexOf("${Param1}") > -1) {
							subTitle = StringUtil.replace(subTitle, "${Param1}", avoidNull(bean.getParamExpansion1()));
						}
						if(subTitle.indexOf("${Param2}") > -1) {
							subTitle = StringUtil.replace(subTitle, "${Param2}", avoidNull(bean.getParamExpansion2()));
						}
						if(subTitle.indexOf("${Param3}") > -1) {
							subTitle = StringUtil.replace(subTitle, "${Param3}", avoidNull(bean.getParamExpansion3()));
						}
						convertDataMap.put("contentsSubTitle", subTitle);
					}
				} else if(CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_COMPLETE.equals(bean.getMsgType()) && !isEmpty(bean.getParamExpansion1())
						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_EDIT_FILE.equals(bean.getMsgType()) && !isEmpty(bean.getParamExpansion1())) {
					String subTitle = "";
					
					subTitle += "<br>" + bean.getParamExpansion1();
					if(!isEmpty(bean.getParamExpansion2())) {
						subTitle += bean.getParamExpansion2();
					}
					if(!isEmpty(bean.getParamExpansion3())) {
						subTitle += bean.getParamExpansion3();
					}

					convertDataMap.put("contentsSubTitle", subTitle);
				}
    		}
    		
    		// mail Template
    		String msgContents = getVelocityTemplateContent(getTemplateFilePath(bean.getMsgType()), convertDataMap);
    		if(isEmpty(msgContents)) {
    			throw new Exception("Can not convert mail contents Email Type : " + bean.getMsgType());
    		}
    		
    		bean.setEmlMessage(msgContents);
    		// title
    		title = title.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
    		bean.setEmlTitle(title);
    		
    		Project prjInfo = null;
    		PartnerMaster partnerInfo = null;
    		BinaryMaster batInfo = null;
    		List<String> watcherList = null;
    		List<String> ccList = null;
    		List<String> toList = null;
    		switch (bean.getMsgType()) {
    		case CoConstDef.CD_MAIL_TYPE_OSS_REGIST:
    		case CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION:
    		case CoConstDef.CD_MAIL_TYPE_OSS_UPDATE:
    		case CoConstDef.CD_MAIL_TYPE_ADDNICKNAME_UPDATE:
    		case CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME:
    		case CoConstDef.CD_MAIL_TYPE_OSS_RENAME:
    		case CoConstDef.CD_MAIL_TYPE_OSS_DELETE:
    		case CoConstDef.CD_MAIL_TYPE_OSS_MODIFIED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_LICENSE_REGIST:
    		case CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE:
    		case CoConstDef.CD_MAIL_TYPE_LICENSE_RENAME:
    		case CoConstDef.CD_MAIL_TYPE_LICENSE_DELETE:
    		case CoConstDef.CD_MAIL_TYPE_LICENSE_MODIFIED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_OSS_DEACTIVATED:
    		case CoConstDef.CD_MAIL_TYPE_OSS_ACTIVATED:
    			// Set creator to sender and cc the other Admin users
    			bean.setToIds(selectMailAddrFromIds(new String[]{bean.getLoginUserName()}));
    			bean.setCcIds(selectAdminMailAddr());
    			break;
    		case CoConstDef.CD_MAIL_TYPE_VULNERABILITY_OSS:
    		case CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED_ALL:
    		case CoConstDef.CD_MAIL_TYPE_CHANGED_USER_INFO:
    			bean.setToIds(selectAdminMailAddr());
    			break;
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REQ_REVIEW:
       		case CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_REQ_REVIEW:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONF:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CANCELED_CONF:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REJECT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_SELF_REJECT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CONF:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CANCELED_CONF:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_REJECT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_SELF_REJECT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_COMPLETE:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_RESERVED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_CANCELED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DELETED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DIFF_FILE:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_EDIT_FILE:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_REJECT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_FAILED:
    		case CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT:
    		case CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED:
    		case CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_REMOVE_RECALCULATED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_ADDED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_MODIFIED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_ADDED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_MODIFIED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_ADDED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_MODIFIED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_MODIFIED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_ADDED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_ADD:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_CHANGED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DELETED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_ONLY:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_COMFIRMED_ONLY:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_COPIED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_COMPLETED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_DROPPED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_REOPENED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_CREATED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_REGISTED:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_REQUESTTOOPEN_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_BINARY_DATA_COMMIT:
    			
    			// to : project creator + cc : watcher + reviewer
    			prjInfo = mailManagerMapper.getProjectInfo(bean.getParamPrjId());
    			
    			if(CoConstDef.CD_MAIL_TYPE_PROJECT_MODIFIED_COMMENT.equals(bean.getMsgType())
    					|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_MODIFIED_COMMENT.equals(bean.getMsgType())
    					|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_MODIFIED_COMMENT.equals(bean.getMsgType())
    					|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_MODIFIED_COMMENT.equals(bean.getMsgType())    					
    					) {
    				toList = new ArrayList<>();
    				ccList = new ArrayList<>();
    				// 로그인 사용자가 Admin이면 to : project creator cc: reviewer, watcher
    				if(CommonFunction.isAdmin()) {
    					toList.addAll(mailManagerMapper.setProjectWatcherMailList(bean.getParamPrjId())); // creator를 포함하고 있음
        				if(!isEmpty(prjInfo.getReviewer())) {
        					ccList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getReviewer()})));
        				} else {
        					ccList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				} else {
    					ccList.addAll(mailManagerMapper.setProjectWatcherMailList(bean.getParamPrjId())); // creator를 포함하고 있음
        				if(!isEmpty(prjInfo.getReviewer())) {
        					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getReviewer()})));
        				} else {
        					toList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				}
    				
        			if(toList != null && !toList.isEmpty()) {
        				bean.setToIds(toList.toArray(new String[toList.size()]));
        			}
        			if(ccList != null && !ccList.isEmpty()) {
        				bean.setCcIds(ccList.toArray(new String[ccList.size()]));
        			}
    				
    			}
    			else if(CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_ADDED_COMMENT.equals(bean.getMsgType()) 
    					|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_ADDED_COMMENT.equals(bean.getMsgType())
    					|| CoConstDef.CD_MAIL_TYPE_PROJECT_ADDED_COMMENT.equals(bean.getMsgType())
    					|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_ADDED_COMMENT.equals(bean.getMsgType())) {
    				// comment send의 경우, 일단 자신은 cc로 보낸다.
    				toList = new ArrayList<>();
    				ccList = new ArrayList<>();
    				
    				if("W".equals(bean.getReceiveFlag())) {
            			watcherList = mailManagerMapper.setProjectWatcherMailList(bean.getParamPrjId());
            			if(watcherList != null && !watcherList.isEmpty()) {
            				toList.addAll(watcherList);
            			} else {
            				// watcher가 설정되어 있지 않은 경우, 그냥 자신에게 보낸다
            				toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{bean.getLoginUserName()})));
            			}
    				} else if("R".equals(bean.getReceiveFlag())) {
        				if(!isEmpty(prjInfo.getReviewer())) {
    						toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getReviewer()})));
        				} else {
        					toList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				} else if("C".equals(bean.getReceiveFlag())) {
						toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getCreator()})));
    				} else if("WR".equals(bean.getReceiveFlag())) {

            			watcherList = mailManagerMapper.setProjectWatcherMailList(bean.getParamPrjId());
            			if(watcherList != null && !watcherList.isEmpty()) {
            				toList.addAll(watcherList);
            			} else {
            				// watcher가 설정되어 있지 않은 경우, 그냥 자신에게 보낸다
            				toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{bean.getLoginUserName()})));
            			}
        				if(!isEmpty(prjInfo.getReviewer())) {
        					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getReviewer()})));
        				} else {
        					toList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				
    				}
    				
        			if(toList != null && !toList.isEmpty()) {
        				bean.setToIds(toList.toArray(new String[toList.size()]));
        			}
        			if(ccList != null && !ccList.isEmpty()) {
        				bean.setCcIds(ccList.toArray(new String[ccList.size()]));
        			}
    				
    			} else {
    				
    				ccList = new ArrayList<>();
    				toList = new ArrayList<>();
    				
    				// to 설정 -------------------------------------
    				// Reviewer
    				if(CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_ADD.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_CHANGED.equals(bean.getMsgType())) {
    					if(bean.getToIds() != null && bean.getToIds().length > 0) {
    						toList.addAll(Arrays.asList(selectMailAddrFromIds(bean.getToIds())));
    					} else if(!isEmpty(prjInfo.getReviewer())) {
    						toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getReviewer()})));
    					}
    				} else if(CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REQ_REVIEW.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_SELF_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_REQ_REVIEW.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CANCELED_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_SELF_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_COMPLETE.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_RESERVED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_CANCELED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DELETED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DIFF_FILE.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_EDIT_FILE.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_FAILED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_REQUESTTOOPEN_COMMENT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_BINARY_DATA_COMMIT.equals(bean.getMsgType())
    						) {
        				if(!isEmpty(prjInfo.getReviewer())) {
        					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getReviewer()})));
        				} else {
        					toList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				}
    				// Creator, watcher, Reviewer
    				else if(CoConstDef.CD_MAIL_TYPE_PROJECT_DELETED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_COPIED.equals(bean.getMsgType())
    						){
    					if(!bean.isToIdsCheckDivision()) {
    						toList.addAll(mailManagerMapper.setProjectWatcherMailList(bean.getParamPrjId())); // creator를 포함하고 있음
    					} else {
    						toList.addAll(mailManagerMapper.setProjectWatcherMailListNotCheckDivision(bean.getParamPrjId()));
    					}
    					
        				if(!isEmpty(prjInfo.getReviewer())) {
        					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getReviewer()})));
        				} else {
        					toList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				} 
    				// Creator, watcher
    				else if(CoConstDef.CD_MAIL_TYPE_PROJECT_CREATED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CANCELED_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_ONLY.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_COMFIRMED_ONLY.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_REMOVE_RECALCULATED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_COMPLETED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DROPPED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_REOPENED.equals(bean.getMsgType())
    						) {
						toList.addAll(mailManagerMapper.setProjectWatcherMailList(bean.getParamPrjId())); // creator를 포함
    				}
    				// Creator only
    				else if(CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_REGISTED.equals(bean.getMsgType())) {
    					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getCreator()})));
    				}
    				
    				// cc 설정 ------------------------------------
    				if(CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_ADD.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_CHANGED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REQ_REVIEW.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_SELF_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_REQ_REVIEW.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CANCELED_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_SELF_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_COMPLETE.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_RESERVED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_CANCELED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DELETED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DIFF_FILE.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_EDIT_FILE.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_FAILED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_REGISTED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_REQUESTTOOPEN_COMMENT.equals(bean.getMsgType())
    						) {
    					ccList.addAll(mailManagerMapper.setProjectWatcherMailList(bean.getParamPrjId()));
    					if(CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_ADD.equals(bean.getMsgType())
    							|| CoConstDef.CD_MAIL_TYPE_PROJECT_REVIEWER_CHANGED.equals(bean.getMsgType())){
    						ccList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{bean.getLoginUserName()})));	
    					}
    				}
    				// admin all
    				else if(CoConstDef.CD_MAIL_TYPE_PROJECT_CREATED.equals(bean.getMsgType())) {
    					ccList.addAll(Arrays.asList(selectAdminMailAddr()));
    				}
    				// Reviewer
    				else if(CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CANCELED_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_IDENTIFICATION_CONFIRMED_ONLY.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_ADDED_COMMENT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_PACKAGING_COMFIRMED_ONLY.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_ADDED_COMMENT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_REMOVE_RECALCULATED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_COMPLETED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DROPPED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PROJECT_REOPENED.equals(bean.getMsgType())
    						) {
        				if(!isEmpty(prjInfo.getReviewer())) {
        					ccList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{prjInfo.getReviewer()})));
        				} else {
        					ccList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
        				
        				// Secuirty 파트 추가
        				if(!isTest && 
        						(
        								CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT.equals(bean.getMsgType()) 
        								|| CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_RECALCULATED.equals(bean.getMsgType()))
        								|| CoConstDef.CD_MAIL_TYPE_VULNERABILITY_PROJECT_REMOVE_RECALCULATED.equals(bean.getMsgType())) {
        					ccList.addAll(Arrays.asList(MAIL_LIST_SECURITY));
        				}
    				}
    				
    				
    				if(toList != null && !toList.isEmpty()) {
    					bean.setToIds(toList.toArray(new String[toList.size()]));
    				}
        			if(ccList != null && !ccList.isEmpty()) {
        				bean.setCcIds(ccList.toArray(new String[ccList.size()]));
        			}
    			}
    			

    			break;

    		case CoConstDef.CD_MAIL_TYPE_PARTER_REQ_REVIEW:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_CONF:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_CANCELED_CONF:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_REJECT:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_SELF_REJECT:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_REVIEWER_CHANGED:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_ADDED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_DELETED:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_REGISTED:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_MODIFIED_COMMENT:
    		case CoConstDef.CD_MAIL_TYPE_PARTNER_BINARY_DATA_COMMIT:
    			// to :  creator + cc : watcher + reviewer
    			partnerInfo = mailManagerMapper.getPartnerInfo(bean.getParamPartnerId());
    			if(CoConstDef.CD_MAIL_TYPE_PARTER_MODIFIED_COMMENT.equals(bean.getMsgType())) {
					toList = new ArrayList<>();
    				ccList = new ArrayList<>();
    				// 로그인 사용자가 Admin이면 to : project creator cc: reviewer, watcher
    				if(CommonFunction.isAdmin()) {
    					toList.addAll(mailManagerMapper.setPartnerWatcherMailList(bean.getParamPartnerId())); // creator를 포함하고 있음
        				if(!isEmpty(partnerInfo.getReviewer())) {
        					ccList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getReviewer()})));
        				} else {
        					ccList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				} else {
    					ccList.addAll(mailManagerMapper.setPartnerWatcherMailList(bean.getParamPartnerId())); // creator를 포함하고 있음
        				if(!isEmpty(partnerInfo.getReviewer())) {
        					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getReviewer()})));
        				} else {
        					toList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				}
    				
        			if(toList != null && !toList.isEmpty()) {
        				bean.setToIds(toList.toArray(new String[toList.size()]));
        			}
        			if(ccList != null && !ccList.isEmpty()) {
        				bean.setCcIds(ccList.toArray(new String[ccList.size()]));
        			}
        			
    			}
    			else if(CoConstDef.CD_MAIL_TYPE_PARTER_ADDED_COMMENT.equals(bean.getMsgType())) {
    				// comment send의 경우, 일단 자신은 cc로 보낸다.
					toList = new ArrayList<>();
    				ccList = new ArrayList<>();
    				
    				if("W".equals(bean.getReceiveFlag())) {
            			watcherList = mailManagerMapper.setPartnerWatcherMailList(bean.getParamPartnerId());
            			if(watcherList != null && !watcherList.isEmpty()) {
            				toList.addAll(watcherList);
            			} else {
            				// watcher가 설정되어 있지 않은 경우, 그냥 자신에게 보낸다
            				toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{bean.getLoginUserName()})));
            			}
    				} else if("R".equals(bean.getReceiveFlag())) {
        				if(!isEmpty(partnerInfo.getReviewer())) {
    						toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getReviewer()})));
        				} else {
        					toList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				} else if("C".equals(bean.getReceiveFlag())) {
						toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getCreator()})));
    				} else if("WR".equals(bean.getReceiveFlag())) {
            			watcherList = mailManagerMapper.setPartnerWatcherMailList(bean.getParamPartnerId());
            			if(watcherList != null && !watcherList.isEmpty()) {
            				toList.addAll(watcherList);
            			} else {
            				// watcher가 설정되어 있지 않은 경우, 그냥 자신에게 보낸다
            				toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{bean.getLoginUserName()})));
            			}
        				if(!isEmpty(partnerInfo.getReviewer())) {
        					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getReviewer()})));
        				} else {
        					toList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				}
    				
        			if(toList != null && !toList.isEmpty()) {
        				bean.setToIds(toList.toArray(new String[toList.size()]));
        			}
        			if(ccList != null && !ccList.isEmpty()) {
        				bean.setCcIds(ccList.toArray(new String[ccList.size()]));
        			}
    				
    			
    			} else {
        			ccList = new ArrayList<>();
        			toList = new ArrayList<>();
        			// to list ------------------------------------------------------------
        			
        			// reviewer
    				if(CoConstDef.CD_MAIL_TYPE_PARTER_REVIEWER_CHANGED.equals(bean.getMsgType())) {
    					if(bean.getToIds() != null && bean.getToIds().length > 0) {
    						toList.addAll(Arrays.asList(selectMailAddrFromIds(bean.getToIds())));
    					} else if(!isEmpty(partnerInfo.getReviewer())) {
    						toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getReviewer()})));
    					}
    				} 
    				// Reviewer
    				if(CoConstDef.CD_MAIL_TYPE_PARTER_REQ_REVIEW.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_SELF_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_DELETED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTNER_BINARY_DATA_COMMIT.equals(bean.getMsgType())
    						) {
        				if(!isEmpty(partnerInfo.getReviewer())) {
        					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getReviewer()})));
        				} else {
        					toList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				}
    				// creator, watcher
    				else if(CoConstDef.CD_MAIL_TYPE_PARTER_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_CANCELED_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_REJECT.equals(bean.getMsgType())
    						) {
    					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getCreator()})));
    					toList.addAll(mailManagerMapper.setPartnerWatcherMailList(bean.getParamPartnerId()));
    				}
    				// Creator only
    				else if(CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_REGISTED.equals(bean.getMsgType())) {
    					toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getCreator()})));
    				}
    				
    				// cc -----------------------------------------------------------------------

    				// Creator, Watcher
    				if(CoConstDef.CD_MAIL_TYPE_PARTER_REVIEWER_CHANGED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_REQ_REVIEW.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_SELF_REJECT.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_DELETED.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_REGISTED.equals(bean.getMsgType())
    						) {
    					ccList.addAll(mailManagerMapper.setPartnerWatcherMailList(bean.getParamPartnerId()));
    					if(CoConstDef.CD_MAIL_TYPE_PARTER_REVIEWER_CHANGED.equals(bean.getMsgType())) {
    						ccList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{bean.getLoginUserName()})));
    					}
    				} 
    				// Reviwer
    				else if(CoConstDef.CD_MAIL_TYPE_PARTER_CANCELED_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_CONF.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_PARTER_REJECT.equals(bean.getMsgType())
    						) {
        				if(!isEmpty(partnerInfo.getReviewer())) {
        					ccList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{partnerInfo.getReviewer()})));
        				} else {
        					ccList.addAll(Arrays.asList(selectAdminMailAddr()));
        				}
    				}
    				
        			if(toList != null && !toList.isEmpty()) {
        				bean.setToIds(toList.toArray(new String[toList.size()]));
        			}
        			if(ccList != null && !ccList.isEmpty()) {
        				bean.setCcIds(ccList.toArray(new String[ccList.size()]));
        			}
    			}

    			break;

    		case CoConstDef.CD_MAIL_TYPE_BAT_WATCHER_REGISTED:
    			batInfo = mailManagerMapper.getBinaryInfo(bean.getParamBatId());

    			ccList = new ArrayList<>();
    			toList = new ArrayList<>();
    			// to list ------------------------------------------------------------
    			// Creator only
				toList.addAll(Arrays.asList(selectMailAddrFromIds(new String[]{batInfo.getCreator()})));
    			
				// cc -----------------------------------------------------------------------
				// Creator, Watcher
				ccList.addAll(mailManagerMapper.binaryWatcherMailList(bean.getParamBatId()));
    			if(toList != null && !toList.isEmpty()) {
    				bean.setToIds(toList.toArray(new String[toList.size()]));
    			}
    			if(ccList != null && !ccList.isEmpty()) {
    				bean.setCcIds(ccList.toArray(new String[ccList.size()]));
    			}
    			break;
    		case CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_INVATED:
    		case CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_INVATED:
    		case CoConstDef.CD_MAIL_TYPE_BAT_WATCHER_INVATED:
    		case CoConstDef.CD_MAIL_TYPE_SELFCHECK_PROJECT_WATCHER_INVATED:
    			bean.setToIds(new String[]{bean.getParamEmail()});
				bean.setCcIds(selectMailAddrFromIds(new String[]{bean.getLoginUserName()}));
    			break;
    		default:
    			// 호출하는 쪽에서 설정된 경우
    			if(bean.getToIds() != null && bean.getToIds().length > 0) {
    				bean.setToIds(selectMailAddrFromIds(bean.getToIds()));
    			}
    			if(bean.getCcIds() != null && bean.getCcIds().length > 0) {
    				bean.setCcIds(selectMailAddrFromIds(bean.getCcIds()));
    			}
    			if(bean.getBccIds() != null && bean.getBccIds().length > 0) {
    				bean.setBccIds(selectMailAddrFromIds(bean.getBccIds()));
    			}
    			
    			break;
    		}
    		
    		// BAT 분석 실패시 Admin 담당자 bcc 추가
    		if(CoConstDef.CD_MAIL_TYPE_BAT_ERROR.equals(bean.getMsgType())) {
    			bean.setBccIds(BAT_FAILED_BCC);
    		}
    		
    		if((Boolean) convertDataMap.get("isModify")
    				&& !isEmpty(bean.getComment())
    				&& ( CoConstDef.CD_MAIL_TYPE_OSS_UPDATE.equals(bean.getMsgType()) 
    						|| CoConstDef.CD_MAIL_TYPE_ADDNICKNAME_UPDATE.equals(bean.getMsgType())
    						|| CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME.equals(bean.getMsgType()) 
    						||  CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE.equals(bean.getMsgType())
    						||  CoConstDef.CD_MAIL_TYPE_LICENSE_RENAME.equals(bean.getMsgType()))) {
    			convertDataMap.put("isModify", false);
    		}
    		

    		if(!(Boolean) convertDataMap.get("isModify")){
    			// 수신자 중복문제 수정
    			try {
    				List<String> _toList = null;
    				List<String> _ccList = null;
    				if(bean.getToIds() != null) {
    					_toList = Arrays.asList(bean.getToIds());
    				} else {
    					_toList = new ArrayList<>();
    				}
    				if(bean.getCcIds() != null) {
    					_ccList = Arrays.asList(bean.getCcIds());
    				} else {
    					_ccList = new ArrayList<>();
    				}
    				
    				
    				// 일단 자기자신에 중복된게 있으면 삭제
    				List<String> _toListFinal = new ArrayList<String>(new HashSet<String>(_toList));
    				List<String> _ccListFinal = new ArrayList<>();
    				List<String> _ccListTmp = new ArrayList<String>(new HashSet<String>(_ccList));
    				
    				// tolist를 기준으로 cclist에서 삭제
    				for(String s : _ccListTmp) {
    					if(!_toListFinal.contains(s)) {
    						_ccListFinal.add(s);
    					}
    				}
    				
    				// 다시 array로
    				bean.setToIds(_toListFinal.toArray(new String[_toListFinal.size()]));
    				bean.setCcIds(_ccListFinal.toArray(new String[_ccListFinal.size()]));
    				
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
    			
    			
    			mailManagerMapper.insertEmailHistory(bean);
        		// 발송처리
        		new Thread(() -> sendEmail(bean)).start();
    		}
			
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			procResult = false;
		}
    	return procResult;
    }
    
    
    

	private Object convertCodeProjectBaiscInfo(Object bean) {
		Project convBean = (Project) bean;
		// opertating system
		convBean.setOsType(CoConstDef.COMMON_SELECTED_ETC.equals(convBean.getOsType()) ? convBean.getOsTypeEtc() : CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, convBean.getOsType()));
		// distribution type
		convBean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, convBean.getDistributionType()));
		// distribution site
		convBean.setDistributeTarget(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_CODE, convBean.getDistributeTarget()));
		// due date
		// modified date
		if(!isEmpty(convBean.getModifiedDate())) {
			convBean.setModifiedDate(DateUtil.dateFormatConvert(convBean.getModifiedDate(), DateUtil.TIMESTAMP_PATTERN, DateUtil.DATE_PATTERN_DASH));
		}
		// modifier
		if(!isEmpty(convBean.getModifier())) {
			convBean.setModifier(makeUserNameFormatWithDivision(convBean.getModifier()));
		}
		// reviewer
		if(!isEmpty(convBean.getReviewer())) {
			convBean.setReviewer(makeUserNameFormatWithDivision(convBean.getReviewer()));
		}
		
		// created date
		if(!isEmpty(convBean.getCreatedDate())) {
			convBean.setCreatedDate(DateUtil.dateFormatConvert(convBean.getCreatedDate(), DateUtil.TIMESTAMP_PATTERN, DateUtil.DATE_PATTERN_DASH));
		}
		// creator
		if(!isEmpty(convBean.getCreator())) {
			convBean.setCreator(makeUserNameFormatWithDivision(convBean.getCreator()));
		}
		
		
		String noticeTypeEtc = null;
		if(!isEmpty(convBean.getNoticeTypeEtc())) {
			noticeTypeEtc = " (" +CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, convBean.getNoticeTypeEtc()) + ")";
		}
		
		if(!isEmpty(convBean.getNoticeType())) {
			String noticeType = CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, convBean.getNoticeType());
			if(!isEmpty(noticeTypeEtc)) {
				noticeType += noticeTypeEtc;
			}
			
			convBean.setNoticeType(noticeType);
		}
		
		if(!isEmpty(convBean.getPriority())) {
			convBean.setPriority(CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, convBean.getPriority()));
		}
		
		if(!isEmpty(convBean.getDivision())) {
			convBean.setDivision(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, convBean.getDivision()));
		}

		return convBean;
	}

	private String makeMailSubject(String title, CoMail bean) {
		return makeMailSubject(title, bean, false);
	}
	// 1) ${User}
	// 2) ${OSS Name}
	// 3) ${License Name}
	// 4) ${Project Name}
    /**
     * 1) ${User}
     * 2) ${OSS Name}
     * 3) ${License Name}
     * 4) ${Creator}
     * 5) ${Reviewer}
     * 
     * @param title
     * @param bean
     * @return
     */
	private String makeMailSubject(String title, CoMail bean, boolean isMailBodySubject) {

		if(title.indexOf("${User}") > -1) {
			if(CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_COMPLETE.equals(bean.getMsgType())) { // 예약한 사용자명으로 치환해야함
				if("BATCH".equals(bean.getJobType())) {
					title = StringUtil.replace(title, "${User}", avoidNull(makeUserNameFormat(avoidNull(mailManagerMapper.getDistributeReservedUser(bean.getParamPrjId()), bean.getLoginUserName()) )));
				} else {
					title = StringUtil.replace(title, "${User}", avoidNull(makeUserNameFormat(bean.getLoginUserName())));					
				}
			}
			else if(CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_INVATED.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_PROJECT_WATCHER_REGISTED.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_INVATED.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_PARTER_WATCHER_REGISTED.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_BAT_WATCHER_INVATED.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_BAT_WATCHER_REGISTED.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TOKEN_CREATE_TYPE.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TOKEN_DELETE_TYPE.equals(bean.getMsgType())
					|| CoConstDef.CD_MAIL_TYPE_SELFCHECK_PROJECT_WATCHER_INVATED.equals(bean.getMsgType())
					) {
				String _convUser = avoidNull(makeUserNameFormat(!isEmpty(bean.getParamUserId()) ? bean.getParamUserId() : bean.getLoginUserName()));

				title = StringUtil.replace(title, "${User}", _convUser);
				if(title.indexOf("${User}") > -1) {
					title = StringUtil.replace(title, "${User}", _convUser);
				}
			}
			else {
				title = StringUtil.replace(title, "${User}", avoidNull(makeUserNameFormat(bean.getLoginUserName())));
			}
		}
		
		
		if(title.indexOf("${OSS Name}") > -1) {
			String _s = "";
			OssMaster ossInfo = null;

			// rename인 경우
			// oss name = after oss
			// before oss name = before oss
			if(CoConstDef.CD_MAIL_TYPE_OSS_RENAME.equals(bean.getMsgType())) {
				ossInfo = (OssMaster) bean.getCompareDataAfter();
			}
			else if(!isEmpty(bean.getParamOssId())) {
				ossInfo = mailManagerMapper.getOssInfoById(bean.getParamOssId());
			}
			
			if(ossInfo != null) {
				if(title.indexOf("${OSS ID}") < 0) {
					_s += "(" + bean.getParamOssId() +")";
				}
				_s += ossInfo.getOssName();
				if(!isEmpty(ossInfo.getOssVersion())) {
					_s += " (" + ossInfo.getOssVersion() +")";
				}
				// Admin에게만 발송되는 oss 관련 메일의 경우 바로가기 link 형식으로 발송
				if(isMailBodySubject 
						&& (
								CoConstDef.CD_MAIL_TYPE_OSS_REGIST.equals(bean.getMsgType()) 
								|| CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION.equals(bean.getMsgType())
								|| CoConstDef.CD_MAIL_TYPE_OSS_UPDATE.equals(bean.getMsgType())
								|| CoConstDef.CD_MAIL_TYPE_ADDNICKNAME_UPDATE.equals(bean.getMsgType())
								|| CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME.equals(bean.getMsgType())
								|| CoConstDef.CD_MAIL_TYPE_OSS_DEACTIVATED.equals(bean.getMsgType())
								|| CoConstDef.CD_MAIL_TYPE_OSS_ACTIVATED.equals(bean.getMsgType())

								)
						)
				{
					String linkUrl = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org");
					linkUrl += "/oss/edit/" + bean.getParamOssId();
					_s = "<a href='"+linkUrl+"' target='_blank'>" + _s + "</a>";
				}
			}
			
			title = StringUtil.replace(title, "${OSS Name}", _s);
		}
		
		if(title.indexOf("${OSS ID}") > -1) {
			title = StringUtil.replace(title, "${OSS ID}", avoidNull(bean.getParamOssId()));
		}
		
		// 삭제된 이메일에 대한 정보
		if(title.indexOf("${OSS Before Name}") > -1) {
			String _s = "";
			OssMaster ossInfo = null;
			
			if(bean.getCompareDataBefore() != null) {
				ossInfo = (OssMaster) bean.getCompareDataBefore();
			}
			
			if(ossInfo != null) {
				_s += ossInfo.getOssName();
				if(!isEmpty(ossInfo.getOssVersion())) {
					_s += " (" + ossInfo.getOssVersion() +")";
				}
			}
			
			title = StringUtil.replace(title, "${OSS Before Name}", _s);
		}
		
		if(title.indexOf("${License Name}") > -1) {
			String _s = "";
			LicenseMaster licenseInfo = null;
			if(!isEmpty(bean.getParamLicenseId())) {
				licenseInfo = mailManagerMapper.getLicenseInfoById(bean.getParamLicenseId());
			}
			
			if(licenseInfo != null) {
				if(title.indexOf("${License ID}") < 0) {
					_s += "(" + bean.getParamLicenseId() +")";
				}
				_s += licenseInfo.getLicenseName();
			}
			if (isMailBodySubject
					&& (CoConstDef.CD_MAIL_TYPE_LICENSE_REGIST.equals(bean.getMsgType())
							|| CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE.equals(bean.getMsgType())
							|| CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE_TYPE.equals(bean.getMsgType())
							|| CoConstDef.CD_MAIL_TYPE_LICENSE_RENAME.equals(bean.getMsgType())
							|| CoConstDef.CD_MAIL_TYPE_LICENSE_MODIFIED_COMMENT.equals(bean.getMsgType())
					)) {
				String linkUrl = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org");
				linkUrl += "/license/edit/" + bean.getParamLicenseId();
				_s = "<a href='" + linkUrl + "' target='_blank'>" + _s + "</a>";
			}
			title = StringUtil.replace(title, "${License Name}", _s);
		}
		
		if(title.indexOf("${License ID}") > -1) {
			title = StringUtil.replace(title, "${License ID}", avoidNull(bean.getParamLicenseId()));
		}
		
		if(title.indexOf("${License Before Name}") > -1) {
			String _s = "";
			LicenseMaster licenseInfo = null;
			
			if(bean.getCompareDataBefore() != null) {
				licenseInfo = (LicenseMaster) bean.getCompareDataBefore();
			}
			if(licenseInfo != null) {
				_s += licenseInfo.getLicenseName();
			}
			
			title = StringUtil.replace(title, "${License Before Name}", _s);
		}
		
		if(title.indexOf("${Project Name}") > -1) {
			String _s = "";
			String _s2 = "";
			String _s3 = "";
			String _s4 = "";
			String _s5 = "";
			Project project = null;
			if(!isEmpty(bean.getParamPrjId())) {
				project = mailManagerMapper.getProjectInfoById(bean.getParamPrjId());
			}
			
			if(project != null) {
				if(title.indexOf("${Project Name}") > -1) {
					if(title.indexOf("${Project ID}") < 0) {
						_s += "(" + bean.getParamPrjId() +")";
					}
					_s += project.getPrjName();
					if(!isEmpty(project.getPrjVersion())) {
						_s += " (" + project.getPrjVersion() +")";
					}
					
					String url = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/project/view/" + bean.getParamPrjId();
					_s = "<a href='"+url+"' target='_blank'>" + _s + "</a>";
				}
				
				if(title.indexOf("${Creator}") > -1) {
					_s2 = avoidNull(makeUserNameFormat(project.getCreator()));
				}
				
				if(title.indexOf("${ReviewerTo}") == -1 
						&& title.indexOf("${Reviewer}") > -1 
						&& !isEmpty(project.getReviewer())) {
					_s3 = avoidNull(makeUserNameFormat(project.getReviewer()));
				}
				
				if(title.indexOf("${LastDistributor}") > -1) {
					if(!isEmpty(project.getDistributeDeployUser())) {
						_s4 = avoidNull(makeUserNameFormat(project.getDistributeDeployUser()));
					} else {
						_s4 = avoidNull(makeUserNameFormat(project.getCreator()));
					}
				}
				
				if(title.indexOf("${Rejector}") > -1 && !isEmpty(project.getDistributeRejector())) {
					_s5 = avoidNull(makeUserNameFormat(project.getDistributeRejector()));
				}
			}

			if(title.indexOf("${Project Name}") > -1) {
				title = StringUtil.replace(title, "${Project Name}", _s);
			}

			if(title.indexOf("${Creator}") > -1) {
				title = StringUtil.replace(title, "${Creator}", _s2);
			}
			
			if(title.indexOf("${ReviewerTo}") > -1) {
				String[] toIds = bean.getToIds();
				title = StringUtil.replace(title, "${Reviewer}", avoidNull(makeUserNameFormat(toIds[0])));
				title = StringUtil.replace(title, "${ReviewerTo}", avoidNull(makeUserNameFormat(toIds[1])));
			}
			
			if(title.indexOf("${ReviewerTo}") == -1 && title.indexOf("${Reviewer}") > -1) {
				title = StringUtil.replace(title, "${Reviewer}", _s3);
			}
			
			if(title.indexOf("${LastDistributor}") > -1) {
				title = StringUtil.replace(title, "${LastDistributor}", _s4);
			}

			if(title.indexOf("${Rejector}") > -1) {
				title = StringUtil.replace(title, "${Rejector}", _s5);
			}
			
			if(title.indexOf("${Project ID}") > -1) {
				title = StringUtil.replace(title, "${Project ID}", avoidNull(bean.getParamPrjId()));
			}
		}
		
		if(title.indexOf("${3rd Party Name}") > -1) {
			String _s = "";
			String _s2 = "";
			String _s3 = "";
			
			PartnerMaster partnerInfo = null;
			if(!isEmpty(bean.getParamPartnerId())) {
				partnerInfo = mailManagerMapper.getPartnerInfo(bean.getParamPartnerId());
			}
			
			if(partnerInfo != null) {
				if(title.indexOf("${3rd Party Name}") > -1) {
					if(title.indexOf("${3rd Party ID}") < 0) {
						_s += "(" + bean.getParamPartnerId() +")";
					}
					_s += partnerInfo.getPartnerName();
					if(!isEmpty(partnerInfo.getSoftwareName())) {
						_s += " (" + partnerInfo.getSoftwareName() +")";
					}
					
					String url = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/partner/view/" + bean.getParamPartnerId();
					_s = "<a href='" + url + "' target='_blank'>" + _s + "</a>";
				}
				
				if(title.indexOf("${Creator}") > -1) {
					_s2 = avoidNull(makeUserNameFormat(partnerInfo.getCreator()));
				}

				if(title.indexOf("${Reviewer}") > -1 && !isEmpty(partnerInfo.getReviewer())) {
					_s3 = avoidNull(makeUserNameFormat(partnerInfo.getReviewer()));
				}
			}

			
			if(title.indexOf("${3rd Party Name}") > -1) {
				title = StringUtil.replace(title, "${3rd Party Name}", _s);
			}

			if(title.indexOf("${Creator}") > -1) {
				title = StringUtil.replace(title, "${Creator}", _s2);
			}
			
			if(title.indexOf("${Reviewer}") > -1) {
				title = StringUtil.replace(title, "${Reviewer}", _s3);
			}
			
			if(title.indexOf("${3rd Party ID}") > -1) {
				title = StringUtil.replace(title, "${3rd Party ID}", avoidNull(bean.getParamPartnerId()));
			}
		}
		
		if(title.indexOf("${Binary Name}") > -1) {
			String _s = "";
			BinaryMaster binaryInfo = null;
			if(!isEmpty(bean.getParamBatId())) {
				binaryInfo = mailManagerMapper.getBinaryInfo(bean.getParamBatId());
				if(title.indexOf("${Binary ID}") < 0) {
					_s = "(" + bean.getParamBatId() + ")";
				}
			}
			
			if(binaryInfo != null) {
				_s += binaryInfo.getBinaryFileName();
			}
			
			title = StringUtil.replace(title, "${Binary Name}", _s);
			
			if(title.indexOf("${Binary ID}") > -1) {
				title = StringUtil.replace(title, "${Binary ID}", avoidNull(bean.getParamBatId()));
			}
		}
		

		if(title.indexOf("${Email}") > -1) {
			String _convEmail = avoidNull(bean.getParamEmail());

			title = StringUtil.replace(title, "${Email}", _convEmail);
		}
		
		if(title.indexOf("${Copied Project ID}") > -1) {
			Project projectInfo = (Project) bean.getCompareDataBefore();

			title = StringUtil.replace(title, "${Copied Project ID}", projectInfo.getPrjId());
			title = StringUtil.replace(title, "${Copied Project Name}", projectInfo.getPrjName());
		}
		
		if(CoConstDef.CD_MAIL_PACKAGING_UPLOAD_SUCCESS.equals(bean.getMsgType())
				|| CoConstDef.CD_MAIL_PACKAGING_UPLOAD_FAILURE.equals(bean.getMsgType())) {
			if(title.indexOf("${File Name}") > -1) {
				title = StringUtil.replace(title, "${File Name}", bean.getParamExpansion1()); // file name set
			}

			if(title.indexOf("${Project ID}") > -1) {
				title = StringUtil.replace(title, "${Project ID}", bean.getParamPrjId());
			}
		}
		
		if(title.indexOf("${BinaryCommitResult}") > -1) {
			title = StringUtil.replace(title, "${BinaryCommitResult}", bean.getBinaryCommitResult());
		}
		
		return title;
	}

	private Map<String, Object> convertModifiedData(Map<String, Object> convertDataMap, String msgType) {
		boolean isModified = false;
		if(CoConstDef.CD_MAIL_TYPE_OSS_UPDATE.equals(msgType)
				|| CoConstDef.CD_MAIL_TYPE_ADDNICKNAME_UPDATE.equals(msgType)
				|| CoConstDef.CD_MAIL_TYPE_OSS_CHANGE_NAME.equals(msgType)
				|| CoConstDef.CD_MAIL_TYPE_OSS_RENAME.equals(msgType)
				) {
			
			if(CoConstDef.CD_MAIL_TYPE_OSS_RENAME.equals(msgType)) {
				isModified = true;
			}
			OssMaster before = (OssMaster) convertDataMap.get("before");
			OssMaster after = (OssMaster) convertDataMap.get("after");
			
			before.setCopyright(CommonFunction.htmlEscape(before.getCopyright()));
			after.setCopyright(CommonFunction.htmlEscape(after.getCopyright()));
			before.setSummaryDescription(CommonFunction.htmlEscape(before.getSummaryDescription()));
			after.setSummaryDescription(CommonFunction.htmlEscape(after.getSummaryDescription()));
			before.setAttribution(CommonFunction.htmlEscape(before.getAttribution()));
			after.setAttribution(CommonFunction.htmlEscape(after.getAttribution()));
			
			after.setOssName(appendChangeStyleMultiLine(before.getOssName(), after.getOssName()));
			isModified = checkEquals(before.getOssName(), after.getOssName(), isModified);
			if(before.getOssNicknames() != null || after.getOssNicknames() != null) {
				List<String> _beforeList = before.getOssNicknames() == null ? new ArrayList<>() : Arrays.asList(before.getOssNicknames());
				List<String> _afterList = after.getOssNicknames() == null ? new ArrayList<>() : Arrays.asList(after.getOssNicknames());
				
				List<String> _newBeforeList = new ArrayList<>(_beforeList);
				List<String> _newAfterList = new ArrayList<>(_afterList);

				Collections.sort(_newBeforeList);
				Collections.sort(_newAfterList);

				
				List<String> addList = new ArrayList<>();
				List<String> delList = new ArrayList<>();
				
				// 삭제 여부 체크
				for(String s : _newBeforeList) {
					if(!isEmpty(s) && !_newAfterList.contains(s)) {
						delList.add(s);
						//_newAfterList.add(idx, "");
						isModified = true;
					}
				}
				// 추가 여부 체크
				for(String s : _newAfterList) {
					if(!isEmpty(s) && !_newBeforeList.contains(s)) {
						addList.add(s);
						//_newBeforeList.add(idx, "");
						isModified = true;
					}
				}
				
				// 하나의 통합 list로 만들어서 순서 정렬
				List<String> tmpList = _newBeforeList;
				tmpList.addAll(_newAfterList);
				
				List<String> mergeList = new ArrayList<>(new HashSet<String>(tmpList));
				Collections.sort(mergeList);

				List<String> _finalBeforeList = new ArrayList<>();
				List<String> _finalAfterList = new ArrayList<>();
				for(String s : mergeList) {
					if(delList.contains(s)) {
						_finalBeforeList.add(appendChangeStyleMultiLine(s, ""));
						_finalAfterList.add("");
					} else if(addList.contains(s)) {
						_finalAfterList.add(appendChangeStyleMultiLine("", s));
						_finalBeforeList.add("");
					} else {
						_finalBeforeList.add(s);
						_finalAfterList.add(s);
					}
				}
				
				String[] beforeArry = _finalBeforeList.toArray(new String[_finalBeforeList.size()]);
				String[] afterArry =  _finalAfterList.toArray(new String[_finalAfterList.size()]);
				
				before.setOssNicknames(beforeArry);
				after.setOssNicknames(afterArry);
				
			}
			
			after.setOssVersion(appendChangeStyleMultiLine(before.getOssVersion(), after.getOssVersion()));
			isModified = checkEquals(before.getOssVersion(), after.getOssVersion(), isModified);
			after.setLicenseName(appendChangeStyleMultiLine(before.getLicenseName(), after.getLicenseName()));
			isModified = checkEquals(before.getLicenseName(), after.getLicenseName(), isModified);
			if(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_DIV, "M").equals(after.getLicenseDiv())){ // multi license일때 만
				List<OssLicense> beforeLicenses = before.getOssLicenses();
				List<OssLicense> afterLicenses = after.getOssLicenses();
				int len = beforeLicenses.size() > afterLicenses.size() ? beforeLicenses.size() : afterLicenses.size();

				for(int i = 0 ; i < len ; i++){
					boolean isBefore = beforeLicenses.size() > i;
					boolean isAfter = afterLicenses.size() > i;
					
					if(isBefore && isAfter){ // 수정시
						OssLicense beforeLicense = beforeLicenses.get(i);
						OssLicense afterLicense = afterLicenses.get(i);
						
						afterLicense.setOssLicenseComb(appendChangeStyle(beforeLicense.getOssLicenseComb(), afterLicense.getOssLicenseComb()));
						afterLicense.setLicenseName(appendChangeStyle(beforeLicense.getLicenseName(), afterLicense.getLicenseName()));
						afterLicense.setOssCopyright(appendChangeStyle(beforeLicense.getOssCopyright(), afterLicense.getOssCopyright()));
						isModified = checkEquals(beforeLicense.getOssCopyright(), afterLicense.getOssCopyright(), isModified);
						
						afterLicenses.set(i, afterLicense);
						
					}else if(!isBefore && isAfter) { // 신규 등록시
						OssLicense afterLicense = afterLicenses.get(i);
						
						afterLicense.setOssLicenseComb(appendChangeStyle("", afterLicense.getOssLicenseComb()));
						afterLicense.setLicenseName(appendChangeStyle("", afterLicense.getLicenseName()));
						afterLicense.setOssCopyright(appendChangeStyle("", afterLicense.getOssCopyright()));
						isModified = checkEquals("", afterLicense.getOssCopyright(), isModified);
						
						afterLicenses.set(i, afterLicense);
					}else if(isBefore && !isAfter){ // 삭제시
						OssLicense beforeLicense = beforeLicenses.get(i);
						OssLicense afterLicense = new OssLicense(); 
						
						afterLicense.setOssLicenseComb(appendChangeStyle(beforeLicense.getOssLicenseComb(), ""));
						afterLicense.setLicenseName(appendChangeStyle(beforeLicense.getLicenseName(), ""));
						afterLicense.setOssCopyright(appendChangeStyle(beforeLicense.getOssCopyright(), ""));
						isModified = checkEquals(beforeLicense.getOssCopyright(), "", isModified);
						
						afterLicenses.add(i, afterLicense);
					}
				}
			}
			after.setCopyright(appendChangeStyleMultiLine(before.getCopyright(), after.getCopyright(), true));
			isModified = checkEquals(before.getCopyright(), after.getCopyright(), isModified);
			
			String[] beforeUrl = before.getDownloadLocation().split(",");
			String[] afterUrl = after.getDownloadLocation().split(",");
			String resultDownloadLocation = appendChangeStyleLinkFormatArray(beforeUrl, afterUrl, 0);
			after.setDownloadLocationLinkFormat(resultDownloadLocation);
			isModified = checkEquals(before.getDownloadLocation(), after.getDownloadLocation(), isModified);
			after.setHomepageLinkFormat(appendChangeStyleLinkFormat(before.getHomepage(), after.getHomepage()));
			isModified = checkEquals(before.getHomepage(), after.getHomepage(), isModified);
			after.setSummaryDescription(appendChangeStyleMultiLine(before.getSummaryDescription(), after.getSummaryDescription(), true));
			isModified = checkEquals(before.getSummaryDescription(), after.getSummaryDescription(), isModified);
			after.setAttribution(appendChangeStyleMultiLine(before.getAttribution(), after.getAttribution(), true));
			isModified = checkEquals(before.getAttribution(), after.getAttribution(), isModified);
			
			String beforeDetectedLicense = before.getDetectedLicense();
			String afterDetectedLicense = after.getDetectedLicense();
			
			if(!isEmpty(beforeDetectedLicense) || !isEmpty(afterDetectedLicense)) {
				List<String> _beforeList = Arrays.asList(beforeDetectedLicense.split(","));
				List<String> _afterList = Arrays.asList(afterDetectedLicense.split(","));

				Collections.sort(_beforeList);
				Collections.sort(_afterList);
				
				List<String> addList = new ArrayList<>();
				List<String> delList = new ArrayList<>();
				
				// 삭제 여부 체크
				for(String s : _beforeList) {
					if(!isEmpty(s) && !_afterList.contains(s)) {
						delList.add(s);
						isModified = true;
					}
				}
				// 추가 여부 체크
				for(String s : _afterList) {
					if(!isEmpty(s) && !_beforeList.contains(s)) {
						addList.add(s);
						isModified = true;
					}
				}
				
				if(_beforeList.size() != _afterList.size()) {
					isModified = true;
				}
				
				// 하나의 통합 list로 만들어서 순서 정렬
				List<String> tmpList = Stream.concat(_beforeList.stream(), _afterList.stream()).collect(Collectors.toList());
				
				List<String> mergeList = new ArrayList<>(new HashSet<String>(tmpList));
				Collections.sort(mergeList);

				List<String> _finalBeforeList = new ArrayList<>();
				List<String> _finalAfterList = new ArrayList<>();
				
				for(String s : mergeList) {
					if(!isEmpty(s)) {
						if(delList.contains(s)) {						
							_finalBeforeList.add(appendChangeStyleMultiLine(s, ""));
						} else if(addList.contains(s)) {
							_finalAfterList.add(appendChangeStyleMultiLine("", s));
						} else {
							_finalBeforeList.add(s);
							_finalAfterList.add(s);
						}
					}
				}
				
				before.clearDetectLicense();
				after.clearDetectLicense();
				
				before.addDetectedLicense(String.join(", ", _finalBeforeList));
				after.addDetectedLicense(String.join(", ", _finalAfterList));
			}
			
			//데이터 변경 없을시
			if(!isModified){
				convertDataMap.replace("isModify", true);
			}
			
			convertDataMap.replace("before", before);
			convertDataMap.replace("after", after);
			// oss modify
		} else if(CoConstDef.CD_MAIL_TYPE_LICENSE_UPDATE.equals(msgType) || CoConstDef.CD_MAIL_TYPE_LICENSE_RENAME.equals(msgType)) {
			// license modify
			LicenseMaster before = (LicenseMaster) convertDataMap.get("before");
			LicenseMaster after = (LicenseMaster) convertDataMap.get("after");
			
			before.setDescription(CommonFunction.htmlEscape(before.getDescription()));
			after.setDescription(CommonFunction.htmlEscape(after.getDescription()));
			before.setAttribution(CommonFunction.htmlEscape(before.getAttribution()));
			after.setAttribution(CommonFunction.htmlEscape(after.getAttribution()));
			before.setLicenseText(CommonFunction.htmlEscape(before.getLicenseText()));
			after.setLicenseText(CommonFunction.htmlEscape(after.getLicenseText()));
			
			after.setLicenseName(appendChangeStyle(before.getLicenseName(), after.getLicenseName()));
			isModified = checkEquals(before.getLicenseName(), after.getLicenseName(), isModified);
			after.setShortIdentifier(appendChangeStyle(before.getShortIdentifier(), after.getShortIdentifier()));
			isModified = checkEquals(before.getShortIdentifier(), after.getShortIdentifier(), isModified);
			if(before.getLicenseNicknames() != null || after.getLicenseNicknames() != null) {
				List<String> _beforeList = before.getLicenseNicknames() == null ? new ArrayList<>() : Arrays.asList(before.getLicenseNicknames());
				List<String> _afterList = after.getLicenseNicknames() == null ? new ArrayList<>() : Arrays.asList(after.getLicenseNicknames());
				
				List<String> _newBeforeList = new ArrayList<>(_beforeList);
				List<String> _newAfterList = new ArrayList<>(_afterList);

				Collections.sort(_beforeList);
				Collections.sort(_afterList);
				
				// 갯수를 맞춰서 정렬
				if(_beforeList.size() < _afterList.size()) {
					for(int i=_beforeList.size(); i<_afterList.size(); i++) {
						_newBeforeList.add("");
					}
				} else if(_afterList.size() < _beforeList.size()) {
					for(int i=_afterList.size(); i<_beforeList.size(); i++) {
						_newAfterList.add("");
					}
				}
				
				_beforeList = _newBeforeList;
				_afterList = _newAfterList;
				
				// delete case
				for(int i=0; i<_beforeList.size(); i++) {
					String s = _beforeList.get(i);
					if(!isEmpty(s) && !s.equals(_afterList.get(i))) {
						// 동일한 position에 값이 다른 경우 삭제로 판단
						_newAfterList.add(i, "");
						_newBeforeList.add(""); // size를 맞춰준다.
					}
				}

				_beforeList = _newBeforeList;
				_afterList = _newAfterList;
				
				// add
				for(int i=0; i<_afterList.size(); i++) {
					String s = _afterList.get(i);
					if(!isEmpty(s) && !s.equals(_beforeList.get(i))) {
						_newBeforeList.add(i, "");
						appendChangeStyle(_newAfterList.get(i));
						isModified = true;
						_newAfterList.add("");
					}
				}
				
				
				_beforeList = _newBeforeList;
				_afterList = _newAfterList;
				
				// before and after 모두 공백인 row는 삭제
				for(int i=0; i<_beforeList.size(); i++) {
					if(isEmpty(_beforeList.get(i)) && isEmpty(_afterList.get(i))) {
						_newBeforeList.remove(i);
						_newAfterList.remove(i);
					}
				}
				
				for(int i=0; i<_newBeforeList.size(); i++) {
					if(isEmpty(_newAfterList.get(i))){
						_newBeforeList.set(i, appendChangeStyle(_newBeforeList.get(i), _newAfterList.get(i)));
					}else{
						_newAfterList.set(i, appendChangeStyle(_newBeforeList.get(i), _newAfterList.get(i)));
					}
					isModified = checkEquals(_newBeforeList.get(i), _newAfterList.get(i), isModified);
				}
				
				String[] beforeArry = _newBeforeList.toArray(new String[_newBeforeList.size()]);
				String[] afterArry =  _newAfterList.toArray(new String[_newAfterList.size()]);
				
				before.setLicenseNicknames(beforeArry);
				after.setLicenseNicknames(afterArry);
			}
			
			after.setLicenseType(appendChangeStyle(before.getLicenseType(), after.getLicenseType()));
			isModified = checkEquals(before.getLicenseType(), after.getLicenseType(), isModified);
			after.setObligation(appendChangeStyle(before.getObligation(), after.getObligation()));
			isModified = checkEquals(before.getObligation(), after.getObligation(), isModified);
			String[] beforeWebPage = isEmpty(before.getWebpage()) ? new String[0] : before.getWebpage().split(",");
			String[] afterWebPage = isEmpty(after.getWebpage()) ? new String[0] : after.getWebpage().split(",");
			String resultWebPage = appendChangeStyleLinkFormatArray(beforeWebPage, afterWebPage, 0);
			after.setWebpageLinkFormat(resultWebPage);
			isModified = checkEquals(before.getWebpage(), after.getWebpage(), isModified);
			after.setDescription(appendChangeStyleMultiLine(before.getDescription(), after.getDescription(), true));
			isModified = checkEquals(before.getDescription(), after.getDescription(), isModified);
			after.setAttribution(appendChangeStyleMultiLine(before.getAttribution(), after.getAttribution(), true));
			isModified = checkEquals(before.getAttribution(), after.getAttribution(), isModified);
			after.setLicenseText(appendChangeStyleMultiLine(before.getLicenseText(), after.getLicenseText(), true));
			isModified = checkEquals(before.getLicenseText(), after.getLicenseText(), isModified);
			
			if(before.getRestriction() != null || after.getRestriction() != null) {
				List<String> _beforeList = before.getRestriction() == null ? new ArrayList<>() : Arrays.asList(before.getRestriction().split(","));
				List<String> _afterList = after.getRestriction() == null ? new ArrayList<>() : Arrays.asList(after.getRestriction().split(","));
				

				List<String> _newBeforeList = new ArrayList<>();
				List<String> _newAfterList = new ArrayList<>();
				
				for(String cd : CoCodeManager.getCodes(CoConstDef.CD_LICENSE_RESTRICTION)) {
					// 둘다 없으면 변경사항 없음
					if(!_beforeList.contains(cd) && !_afterList.contains(cd)) {
						continue;
					}
					
					// add
					if(!_beforeList.contains(cd) && _afterList.contains(cd)) {
						_newBeforeList.add("");
						_newAfterList.add(changeStyle(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, cd), "mod"));
						isModified = true;
					}
					// delete
					else if(_beforeList.contains(cd) && !_afterList.contains(cd)) {
						_newBeforeList.add(changeStyle(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, cd), "del", true));
						_newBeforeList.add("");
						isModified = true;
						
					}
					// 변경 없음
					else {
						_newBeforeList.add(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, cd));
						_newAfterList.add(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, cd));
					}
				}
				
				String[] beforeArry = _newBeforeList.toArray(new String[_newBeforeList.size()]);
				String[] afterArry =  _newAfterList.toArray(new String[_newAfterList.size()]);
				
				before.setArrRestriction(beforeArry);
				after.setArrRestriction(afterArry);
			}
			
			//데이터 변경 없을시
			if(!isModified){
				convertDataMap.replace("isModify", true);
			}
			
			convertDataMap.replace("before", before);
			convertDataMap.replace("after", after);
			
		} else if(CoConstDef.CD_MAIL_TYPE_PROJECT_CHANGED.equals(msgType)||CoConstDef.CD_MAIL_TYPE_PROJECT_COPIED.equals(msgType)) {
			// Project modify
			Project before = (Project) convertDataMap.get("before");
			Project after = (Project) convertDataMap.get("after");

			isModified = checkEquals(before.getPrjName(), after.getPrjName(), isModified);
			isModified = checkEquals(before.getPrjVersion(), after.getPrjVersion(), isModified);
			isModified = checkEquals(before.getOsType(), after.getOsType(), isModified);
			isModified = checkEquals(before.getDistributionType(), after.getDistributionType(), isModified);
			isModified = checkEquals(before.getDistributeTarget(), after.getDistributeTarget(), isModified);
			isModified = checkEquals(before.getComment(), after.getComment(), isModified);
			isModified = checkEquals(before.getCreator(), after.getCreator(), isModified);
			isModified = checkEquals(before.getNoticeType(), after.getNoticeType(), isModified);
			isModified = checkEquals(before.getNetworkServerType(), after.getNetworkServerType(), isModified);
			isModified = checkEquals(before.getPriority(), after.getPriority(), isModified);
			isModified = checkEquals(before.getDivision(), after.getDivision(), isModified);
			
			after.setPrjName(appendChangeStyle(before.getPrjName(), after.getPrjName()));
			after.setPrjVersion(appendChangeStyle(before.getPrjVersion(), after.getPrjVersion()));
			after.setOsType(appendChangeStyle(before.getOsType(), after.getOsType()));
			after.setDistributionType(appendChangeStyle(before.getDistributionType(), after.getDistributionType()));
			after.setDistributeTarget(appendChangeStyle(before.getDistributeTarget(), after.getDistributeTarget()));
			after.setComment(appendChangeStyleDiv(before.getComment(), after.getComment()));
			after.setCreator(appendChangeStyle(before.getCreator(), after.getCreator()));
			after.setNoticeType(appendChangeStyle(before.getNoticeType(), after.getNoticeType()));
			after.setNetworkServerType(appendChangeStyle(before.getNetworkServerType(), after.getNetworkServerType()));
			after.setPriority(appendChangeStyle(before.getPriority(), after.getPriority()));
			after.setDivision(appendChangeStyle(before.getDivision(), after.getDivision()));
			

			if(before.getModelList().size() > 0 || after.getModelList().size() > 0) {

				List<String> _beforeList = new ArrayList<>();
				if(before.getModelList().size() > 0){
					for(int i=0; i < before.getModelList().size(); i++){
						String categoryName = CommonFunction.makeCategoryFormat(before.getDistributeTarget(),before.getModelList().get(i).getCategory());
						String before_str = categoryName+"/"+before.getModelList().get(i).getModelName()+"/"+before.getModelList().get(i).getReleaseDate();
						_beforeList.add(before_str);
					}
				}
						 
				List<String> _afterList = new ArrayList<>();
				if(after.getModelList().size() > 0){
					for(int i=0; i < after.getModelList().size(); i++){
						String categoryName = CommonFunction.makeCategoryFormat(after.getDistributeTarget(),after.getModelList().get(i).getCategory());
						String after_str = categoryName+"/"+after.getModelList().get(i).getModelName()+"/"+after.getModelList().get(i).getReleaseDate();
						_afterList.add(after_str);
					}
				}
				
				List<String> _newBeforeList = new ArrayList<>(_beforeList);
				List<String> _newAfterList = new ArrayList<>(_afterList);

				Collections.sort(_beforeList);
				Collections.sort(_afterList);
				
				// 갯수를 맞춰서 정렬
				if(_beforeList.size() < _afterList.size()) {
					for(int i=_beforeList.size(); i<_afterList.size(); i++) {
						_newBeforeList.add("");
					}
				} else if(_afterList.size() < _beforeList.size()) {
					for(int i=_afterList.size(); i<_beforeList.size(); i++) {
						_newAfterList.add("");
					}
				}
				
				_beforeList = new ArrayList<>(_newBeforeList);
				_afterList = new ArrayList<>(_newAfterList);

				// delete case
				int b_size = _beforeList.size();
				for(int i=0; i<b_size; i++) {
					String s = _beforeList.get(i);
					if(!isEmpty(s) && !s.equals(_afterList.get(i))) {
						// 동일한 position에 값이 다른 경우 삭제로 판단
						_newAfterList.add(i, "");
						_newBeforeList.add(i+1,""); // size를 맞춰준다.
					}					
				}

				_beforeList = new ArrayList<>(_newBeforeList);
				_afterList = new ArrayList<>(_newAfterList);
				
				// add
				int a_size = _afterList.size();
				for(int i=0; i<a_size; i++) {
					String s = _afterList.get(i);
					if(!isEmpty(s) && !s.equals(_beforeList.get(i))) {
						if(!isEmpty(_beforeList.get(i))){
							_newBeforeList.add(i, "");
							appendChangeStyle(_newAfterList.get(i));
							isModified = true;
							_newAfterList.add(i-1,"");
						}
					}
				}
				
				_beforeList = _newBeforeList;
				_afterList = _newAfterList;
				
				// before and after 모두 공백인 row는 삭제
				for(int i=0; i<_beforeList.size(); i++) {
					if(isEmpty(_beforeList.get(i)) && isEmpty(_afterList.get(i))) {
						_newBeforeList.remove(i);
						_newAfterList.remove(i);
					}
				}

				for(int i=0; i<_newBeforeList.size(); i++) {
					if(isEmpty(_newAfterList.get(i))){
						_newBeforeList.set(i, appendChangeStyle(_newBeforeList.get(i), _newAfterList.get(i)));
					}else{
						_newAfterList.set(i, appendChangeStyle(_newBeforeList.get(i), _newAfterList.get(i)));
					}

					isModified = checkEquals(_newBeforeList.get(i), _newAfterList.get(i),  isModified);
					
				}
				
				before.setModelListInfo(_newBeforeList);
				after.setModelListInfo(_newAfterList);
			}
			//watcher
			if(before.getWatcherList().size() > 0 || after.getWatchers() != null) {

				List<String> _beforeList = new ArrayList<>();
				if(before.getWatcherList().size() > 0){
					for(int i=0; i < before.getWatcherList().size(); i++){
						String before_str = "";
						if(isEmpty(before.getWatcherList().get(i).getPrjEmail())){
							before_str = makeUserNameFormatWithDivision(before.getWatcherList().get(i).getPrjDivision(), before.getWatcherList().get(i).getPrjUserId());
						}else{
							before_str = before.getWatcherList().get(i).getPrjEmail();
						}
						_beforeList.add(before_str);
					}
				}

				List<String> _afterList = new ArrayList<>();
				String[] watchers = after.getWatchers();
				if(watchers != null && watchers.length > 0){
					for(int i=0; i < watchers.length; i++){
						String[] after_array = watchers[i].split("/");
						String after_str = "";
						if("Email".equals(after_array[1])){
							after_str = after_array[0];
						}else{
							after_str = makeUserNameFormatWithDivision(after_array[0], after_array[1]);
						}
						_afterList.add(after_str);
					}
				}
				
				List<String> _newBeforeList = new ArrayList<>(_beforeList);
				List<String> _newAfterList = new ArrayList<>(_afterList);

				Collections.sort(_beforeList);
				Collections.sort(_afterList);
				
				// 갯수를 맞춰서 정렬
				if(_beforeList.size() < _afterList.size()) {
					for(int i=_beforeList.size(); i<_afterList.size(); i++) {
						_newBeforeList.add("");
					}
				} else if(_afterList.size() < _beforeList.size()) {
					for(int i=_afterList.size(); i<_beforeList.size(); i++) {
						_newAfterList.add("");
					}
				}
				
				_beforeList = new ArrayList<>(_newBeforeList);
				_afterList = new ArrayList<>(_newAfterList);

				// delete case
				int b_size = _beforeList.size();
				for(int i=0; i<b_size; i++) {
					String s = _beforeList.get(i);
					if(!isEmpty(s) && !s.equals(_afterList.get(i))) {
						// 동일한 position에 값이 다른 경우 삭제로 판단
						_newAfterList.add(i, "");
						_newBeforeList.add(i+1,""); // size를 맞춰준다.
					}					
				}

				_beforeList = new ArrayList<>(_newBeforeList);
				_afterList = new ArrayList<>(_newAfterList);
				
				// add
				int a_size = _afterList.size();
				for(int i=0; i<a_size; i++) {
					String s = _afterList.get(i);
					if(!isEmpty(s) && !s.equals(_beforeList.get(i))) {
						if(!isEmpty(_beforeList.get(i))){
							_newBeforeList.add(i, "");
							appendChangeStyle(_newAfterList.get(i));
							isModified = true;
							_newAfterList.add(i-1,"");
						}
					}
				}
				
				_beforeList = _newBeforeList;
				_afterList = _newAfterList;
				
				// before and after 모두 공백인 row는 삭제
				for(int i=0; i<_beforeList.size(); i++) {
					if(isEmpty(_beforeList.get(i)) && isEmpty(_afterList.get(i))) {
						_newBeforeList.remove(i);
						_newAfterList.remove(i);
					}
				}

				for(int i=0; i<_newBeforeList.size(); i++) {
					if(isEmpty(_newAfterList.get(i))){
						_newBeforeList.set(i, appendChangeStyle(_newBeforeList.get(i), _newAfterList.get(i)));
					}else{
						_newAfterList.set(i, appendChangeStyle(_newBeforeList.get(i), _newAfterList.get(i)));
					}

					isModified = checkEquals(_newBeforeList.get(i), _newAfterList.get(i),  isModified);
					
				}
				
				before.setWatcherListInfo(_newBeforeList);
				after.setWatcherListInfo(_newAfterList);
			}
			
			//데이터 변경 없을시
			if(!isModified){
				convertDataMap.replace("isModify", true);
			}
			
			convertDataMap.replace("before", before);
			convertDataMap.replace("after", after);
		}else if(CoConstDef.CD_MAIL_TYPE_OSS_REGIST_NEWVERSION.equals(msgType)) {
			OssMaster om = (OssMaster) convertDataMap.get("paramOssInfo");
						
			if(om != null) {
				List<String> checkOssNickNamesAdd = new ArrayList<>();
				
				if(om.getOssNicknames() != null && om.getOssNicknames().length > 0) {
					if(om.getExistOssNickNames() != null && om.getExistOssNickNames().length > 0) {
						checkOssNickNamesAdd = Arrays.asList(om.getOssNicknames()).stream().filter(x -> !Arrays.asList(om.getExistOssNickNames()).contains(x)).collect(Collectors.toList());
					}else {
						checkOssNickNamesAdd = Arrays.asList(om.getOssNicknames());
					}
				}
				
				if(checkOssNickNamesAdd != null && checkOssNickNamesAdd.size() > 0) {
					String changeOssNickName = "";
					
					if(om.getExistOssNickNames() != null && om.getExistOssNickNames().length > 0) {
						for(String nickName : om.getExistOssNickNames()) {
							changeOssNickName += nickName + "<br/>";
						}
					}
					
					for(String ossNickName : checkOssNickNamesAdd) {
						if(!isEmpty(ossNickName)) {
							ossNickName = appendChangeStyle("newVersion_ossNickNameAdd", ossNickName);
							changeOssNickName += ossNickName + "<br/>";
						}
					}
					
					if(!isEmpty(changeOssNickName)) {
						OssMaster ossBasicInfo = (OssMaster) convertDataMap.get("oss_basic_info");
						ossBasicInfo.setOssNickname(changeOssNickName);
						convertDataMap.replace("oss_basic_info", ossBasicInfo);
					}
				}
			}
		}
		return convertDataMap;
	}


	private boolean checkEquals(String before, String after, boolean isModified) {
		if(isModified) {
			return isModified;
		}
		return !avoidNull(before).equals(after);
	}
	
	private String appendChangeStyleLinkFormat(String downloadLocation) {
		return "<a href='"+downloadLocation+"' target='_blank'>" + downloadLocation + "</a>";
	}
	
	private String appendChangeStyleLinkFormatArray(String downloadLocations){
		String[] downloadLocation = avoidNull(downloadLocations).split(",");
		String result = "";
		
		for(int idx = 0; idx < downloadLocation.length; idx++){
			if(idx > 0){
				result += "<br>";
			}
			
			result += appendChangeStyleLinkFormat(downloadLocation[idx]);
		}
		
		return result;
	}
	private String appendChangeStyleLinkFormat(String before, String after) {
		return "<a href='"+after+"' target='_blank'>" + appendChangeStyle(before, after) + "</a>";
	}
	
	private String appendChangeStyleLinkFormatArray(String[] before, String[] after, int seq) {
		int length = before.length > after.length ? before.length : after.length;
		String result = "";
		String beforeVal = (before.length > seq ? before[seq] : "");
		String afterVal = (after.length > seq ? after[seq] : "");
		
		if(length == seq) {
			return result;
		} else {
			if(seq > 0) {
				result = "<br>";
			}
		}
		
		result += "<a href='"+afterVal+"' target='_blank'>" + appendChangeStyle(beforeVal, afterVal) + "</a>" + appendChangeStyleLinkFormatArray(before, after, ++seq);
		
		return result;
	}
	
	private String appendChangeStyle(String before, String after) {
		before = avoidNull(before).trim();
		after = avoidNull(after).trim();
		if(!avoidNull(before).equals(after)) {
			if(isEmpty(after)){
				after = changeStyle(before,"del");
			}else{
				after = changeStyle(after,"mod");
			}
		}
		return after;
	}
	private String appendChangeStyleMultiLine(String before, String after) {
		return appendChangeStyleMultiLine(before, after, false);
	}
	
	private String appendChangeStyleMultiLine(String before, String after, boolean brFlag) {
		
		List<String> original = Arrays.asList(CommonFunction.brReplaceToLine(before).split("\n"));
		List<String> revised = Arrays.asList(CommonFunction.brReplaceToLine(after).split("\n"));
		

        Patch<String> patch = DiffUtils.diff(original, revised);
        List<String> udiff = DiffUtils.generateUnifiedDiff("original", "revised",
                original, patch, 100);
        // 변경된 부분이 있다면, after에 삭제와 추가를 모푸 포함해서 return 한다.
        if(udiff != null && udiff.size() > 0) {
        	String changeStr = "";
        	int ignoreIdx = 0;
        	for(String s : udiff) {
        		if(ignoreIdx < 3) {
            		ignoreIdx ++;
        			continue;
        		}
        		
        		// flag를 받아서 필요한 영역에서만 br tag를 넣음
        		if(!isEmpty(changeStr) && brFlag) {
        			changeStr += "<br/>";
        		}
        		
        		String changeMode = s.substring(0, 1);
        		String str = s.substring(1);
        		if("+".equals(changeMode)) {
        			changeStr += changeStyle(str,"mod", true);
        		} else if("-".equals(changeMode)) {
        			changeStr += changeStyle(str,"del", true);
        		} else {
        			changeStr += str;
        		}
        		ignoreIdx ++;
        	}
        	
        	return changeStr;
        }
		
		return after;
	}
	
	private String appendChangeStyleDiv(String before, String after) {
		before = avoidNull(before).trim();
		after = avoidNull(after).trim();
		if(!avoidNull(before).equals(after)) {
			if(isEmpty(after)){
				after = changeStyleDiv(before,"del");
			}else{
				after = changeStyleDiv(after,"mod");
			}
		}
		return after;
	}

	private String appendChangeStyle(String s) {
		return "<p>" + s + "</p>";
	}
	
	private String changeStyle(String s, String tp) {
		return changeStyle(s, tp, false);
	}
	
	private String changeStyle(String s, String tp, boolean useLineDeco) {
		String appendHtml = "";
		if(tp == "del"){
			if(useLineDeco) {
				appendHtml = "<span style=\"background-color:red;text-decoration:line-through;\">" + s + "</span>";
			} else {
				appendHtml = "<span style=\"background-color:red\">" + s + "</span>";
			}
		}else if(tp == "mod"){
			appendHtml = "<span style=\"background-color:yellow\">" + s + "</span>";
		}
		return appendHtml;
	}
	
	/**
	 * editor로 작성된 html 태그를 포함하는 항목의 경우 span의 background 속성이 적용되지 않아, div용을 추가함
	 * @param s
	 * @param tp
	 * @return
	 */
	private String changeStyleDiv(String s, String tp) {
		String appendHtml = "";
		if(tp == "del"){
			appendHtml = "<div style=\"background-color:red\">" + s + "</div>";
		}else if(tp == "mod"){
			appendHtml = "<div style=\"background-color:yellow\">" + s + "</div>";
		}
		return appendHtml;
	}

	private String getTemplateFilePath(String msgType) {
		for(String s : CoCodeManager.getCodes(CoConstDef.CD_MAIL_COMPONENT_TEMPLATE)) {
			for(String type : CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_COMPONENT_TEMPLATE, s).split(",")) {
				if(msgType.equals(type.trim())) {
					return "/template/email/" + CoCodeManager.getCodeString(CoConstDef.CD_MAIL_COMPONENT_TEMPLATE, s);
				}
			}
		}
		return null;
	}

	private String makeUserNameFormat(T2Users userInfo) {
		String rtn = "";
		if(userInfo != null) {
			rtn += avoidNull(userInfo.getUserName());
			rtn += "(" + avoidNull(userInfo.getUserId()) + ")";
		}
		return rtn;
	}
	
	private String makeUserNameFormatWithDivision(T2Users userInfo) {
		String rtn = "";
		if(userInfo != null) {
			if(!isEmpty(userInfo.getDivision())) {
				String _division = CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, userInfo.getDivision());
				if(!isEmpty(_division)) {
					rtn += _division + " ";
				}
			}
			rtn += avoidNull(userInfo.getUserName());
			rtn += "(" + avoidNull(userInfo.getUserId()) + ")";
		}
		return rtn;
	}
	
	public String makeUserNameFormat(String userId) {
		if(isEmpty(userId)) {
			return "";
		}
		T2Users userParam = new T2Users();
		userParam.setUserId(userId);
		return makeUserNameFormat(userMapper.getUser(userParam));
	}
	
	public String makeUserNameFormatWithDivision(String userId) {
		return makeUserNameFormatWithDivision(null, userId);
	}
	public String makeUserNameFormatWithDivision(String divisionCd, String userId) {
		String rtnVal = "";
		
		if(!isEmpty(divisionCd) && "all".equalsIgnoreCase(userId)) {
			String _division = CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, divisionCd);
			if(!isEmpty(_division)) {
				rtnVal += _division + "/" + userId;
			}
		} else {
			T2Users userParam = new T2Users();
			userParam.setUserId(userId);
			T2Users userInfo = userMapper.getUser(userParam);
			if(userInfo != null && !isEmpty(userInfo.getUserName())) {
				if(!isEmpty(userInfo.getDivision())) {
					String _division = CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, userInfo.getDivision());
					if(!isEmpty(_division)) {
						rtnVal += _division + " ";
					}
				}
				
				rtnVal += userInfo.getUserName() + "(" + userId + ")";
			}
		}

		
		if(isEmpty(rtnVal)) {
			return userId;
		}

		return rtnVal;
	}

	private Object setContentsInfo(CoMail bean, String component) {
		List<String> param = new ArrayList<>();
		// 순서대로
		switch (component) {
			case CoConstDef.CD_MAIL_COMPONENT_OSSBASICINFO:
				param.add(bean.getParamOssId());
				return makeOssBasicInfo(getMailComponentData(param, component));
			case CoConstDef.CD_MAIL_COMPONENT_LICENSEBASICINFO:
				param.add(bean.getParamLicenseId());
				return makeLicenseBasicInfo(getMailComponentData(param, component));
			case CoConstDef.CD_MAIL_COMPONENT_PROJECT_BASICINFO:
			case CoConstDef.CD_MAIL_COMPONENT_SELFCHECK_PROJECT_BASICINFO:
				param.add(bean.getParamPrjId());
				return makeProjectBasicInfo(getMailComponentData(param, component));
			case CoConstDef.CD_MAIL_COMPONENT_PROJECT_BOMOSSINFO:
			case CoConstDef.CD_MAIL_COMPONENT_PROJECT_DISCROSEOSSINFO:
				ProjectIdentification ossListParam = new ProjectIdentification();
				ossListParam.setReferenceId(bean.getParamPrjId());
				ossListParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
				ossListParam.setMerge(CoConstDef.FLAG_NO);
				Map<String, Object> mailComponentDataMap = projectService.getIdentificationGridList(ossListParam);
				
				if(CoConstDef.CD_MAIL_COMPONENT_PROJECT_DISCROSEOSSINFO.equals(component)) {
					Project project = new Project();
					project.setPrjId(bean.getParamPrjId());
					mailComponentDataMap.put("projectBean", projectService.getProjectDetail(project));
				}
				return makeIdentificationOssListInfo(mailComponentDataMap, component);
			case CoConstDef.CD_MAIL_COMPONENT_PROJECT_DISTRIBUTIONINFO:
				param.add(bean.getParamPrjId());
				return makeDistributionInfo(getMailComponentData(param, component));
			case CoConstDef.CD_MAIL_COMPONENT_PROJECT_MODELINFO:
				param.add(bean.getParamPrjId());
				return makeModelInfo(getMailComponentData(param, component), bean.getMsgType());
			case CoConstDef.CD_MAIL_COMPONENT_PARTNER_BASICINFO:
				param.add(bean.getParamPartnerId());
				return makePartnerBasicInfo(getMailComponentData(param, component));
			case CoConstDef.CD_MAIL_COMPONENT_PARTNER_OSSLIST:
			case CoConstDef.CD_MAIL_COMPONENT_PARTNER_DISCLOSEOSSINFO:
				param.add(bean.getParamPartnerId());
				return makePartnerOssListInfo(getMailComponentData(param, component));
			case CoConstDef.CD_MAIL_COMPONENT_BATRESULT:
				param.add(bean.getParamBatId());
				return makeBatResultInfo(getMailComponentData(param, component));
			case CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_PRJ:
				param.add(bean.getParamPrjId());
				return makeVulnerabilityInfo(getMailComponentData(param, component));
			case CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_OSS:
			case CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_PROJECT_RECALCULATED_ALL:
				return makeVulnerabilityInfo(getMailComponentDataWithArray(bean.getParamOssKey(), component));
			case CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_RECALCULATED:
			case CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_REMOVE_RECALCULATED:
				param.add(bean.getParamPrjId());
				return makeVulnerabilityInfo(getMailComponentData(param, component));
			case CoConstDef.CD_MAIL_COMPONENT_PACKAGING_REQUESTED_URL:
				return CoCodeManager.getCodeExpString(CoConstDef.CD_COLLAB_INFO, CoConstDef.CD_PACKAGING_REQUESTED_URL);
			default:
				break;
		}
		return null;
	}

	/**
	 * 3rd party 관련 메일의 OSS LIST 정보반환
	 * @param mailComponentData
	 * @return
	 */
	private List<OssComponents> makePartnerOssListInfo(List<Map<String, Object>> mailComponentData) {
		List<OssComponents> list = null;
		Set<Map<String, String>> ossSet = new HashSet<Map<String, String>>();
		if(mailComponentData != null && !mailComponentData.isEmpty()) {
			list = new ArrayList<>();
			for(Map<String, Object> dataMap : mailComponentData) {
				Map<String, String> oss = new HashMap<String, String>();
				oss.put("OSS_NAME", (String)dataMap.get("OSS_NAME"));
				oss.put("OSS_VERSION", (String)dataMap.get("OSS_VERSION"));
				oss.put("LICENSE_NAME", (String)dataMap.get("LICENSE_NAME"));
				ossSet.add(oss);
			}

			for(Map<String, String> dataSet : ossSet) {
				OssComponents bean = new OssComponents();
				bean.setOssName(dataSet.get("OSS_NAME"));
				bean.setOssVersion(dataSet.get("OSS_VERSION"));
				bean.setLicenseName(dataSet.get("LICENSE_NAME"));
				list.add(bean);
			}

		}
		return list;
	}

	private List<OssMaster> makeVulnerabilityInfo(List<Map<String, Object>> mailComponentData) {
		List<OssMaster> list = null;
		OssMaster bean;
		for(Map<String, Object> dataMap : mailComponentData) {
			if(list == null) {
				list = new ArrayList<>();
			}
			bean = new OssMaster();
			
			if(dataMap.containsKey("PRJ_ID")) {
				bean.setPrjId((String) dataMap.get("PRJ_ID"));
			}
			if(dataMap.containsKey("COMPONENT_ID")) {
				bean.setComponentId((String) dataMap.get("COMPONENT_ID"));
			}
			
			bean.setOssId((String) dataMap.get("OSS_ID"));
			bean.setOssName((String) dataMap.get("OSS_NAME"));
			bean.setOssVersion((String) dataMap.get("OSS_VERSION"));
			bean.setCveId((String) dataMap.get("CVE_ID"));
			bean.setCvssScore(String.valueOf(dataMap.get("CVSS_SCORE")));
			bean.setVulnSummary((String) dataMap.get("VULN_SUMMARY"));
			bean.setPublishedDate((String) dataMap.get("PUBL_DATE"));
			bean.setModifiedDate((String) dataMap.get("MODI_DATE"));
			list.add(bean);
		}
		
		return list;
	}

	private BinaryMaster makeBatResultInfo(List<Map<String, Object>> mailComponentData) {
		BinaryMaster bean = null;
		for(Map<String, Object> dataMap : mailComponentData) {
			bean = new BinaryMaster();
			bean.setBatId((String) dataMap.get("BAT_ID"));
			bean.setSoftwareName((String) dataMap.get("SOFTWARE_NAME"));
			bean.setSoftwareVersion((String) dataMap.get("SOFTWARE_VERSION"));
			bean.setPartnerName(avoidNull((String) dataMap.get("PARTNER_NAME")));
			bean.setBinaryFileId((String) dataMap.get("BINARY_FILE_ID"));
			bean.setBatStatus((String) dataMap.get("BAT_STATUS"));
			bean.setBatResultCount((String) dataMap.get("BAT_RESULT_COUNT"));
			bean.setCreator(makeUserNameFormatWithDivision((String) dataMap.get("CREATOR")));
			
			if(!isEmpty((String) dataMap.get("BAT_ERROR_MSG"))) {
				bean.setBatErrorMsg((String) dataMap.get("BAT_ERROR_MSG"));
			}
			break;
		}
		
		if(bean != null && !isEmpty(bean.getBatStatus())) {
			bean.setBatStatus(CoCodeManager.getCodeString(CoConstDef.CD_BAT_STATUS, bean.getBatStatus()));
		}
		return bean;
	}

	private PartnerMaster makePartnerBasicInfo(List<Map<String, Object>> mailComponentData) {
		PartnerMaster bean = null;
		for(Map<String, Object> dataMap : mailComponentData) {
			bean = new PartnerMaster();
			bean.setPartnerId((String) dataMap.get("PARTNER_ID"));
			bean.setStatus((String) dataMap.get("STATUS"));
			bean.setPartnerName((String) dataMap.get("PARTNER_NAME"));
			bean.setSoftwareName((String) dataMap.get("SOFTWARE_NAME"));
			bean.setSoftwareVersion((String) dataMap.get("SOFTWARE_VERSION"));
			bean.setDeliveryForm(CoCodeManager.getCodeString(CoConstDef.CD_PARTNER_DELIVERY_FORM, (String) dataMap.get("DELIVERY_FORM")));
			bean.setDescription((String) dataMap.get("DESCRIPTION"));
			bean.setConfirmationFileId((String) dataMap.get("CONFIRMATION_FILE_ID"));
			bean.setOssFileId((String) dataMap.get("OSS_FILE_ID"));
			bean.setReviewer(makeUserNameFormatWithDivision((String) dataMap.get("REVIEWER")));
			bean.setCreator(makeUserNameFormatWithDivision((String) dataMap.get("CREATOR")));
			bean.setCreatedDate(CommonFunction.formatDate((String) dataMap.get("CREATED_DATE")));
			bean.setDivision(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, (String) dataMap.get("DIVISION")));
			break;
		}
		return bean;
	}

	private List<Project> makeModelInfo(List<Map<String, Object>> mailComponentData, String mailType) {
		List<Project> list = null;
		Project bean;
		String distributeTargetCode = null;
		for(Map<String, Object> dataMap : mailComponentData) {
			if(list == null) {
				list = new ArrayList<>();
			}
			if(isEmpty(distributeTargetCode)) {
				distributeTargetCode = CoConstDef.CD_DTL_DISTRIBUTE_SKS.equals(avoidNull( (String) dataMap.get("DISTRIBUTE_TARGET"), CoConstDef.CD_DTL_DISTRIBUTE_LGE)) ? CoConstDef.CD_MODEL_TYPE2 : CoConstDef.CD_MODEL_TYPE;
			}
			bean = new Project();
			bean.setPrjId((String) dataMap.get("PRJ_ID"));
			bean.setCategory(CommonFunction.makeCategoryFormat((String) dataMap.get("DISTRIBUTE_TARGET"),  (String) dataMap.get("CATEGORY"), (String) dataMap.get("SUBCATEGORY")));
			bean.setModelName((String) dataMap.get("MODEL_NAME"));
			bean.setReleaseDate(CommonFunction.formatDateSimple((String) dataMap.get("RELEASE_DATE")));
			bean.setModifier((String) dataMap.get("MODIFIER"));
			if(dataMap.get("MODIFIED_DATE") != null && !isEmpty((String) dataMap.get("MODIFIED_DATE"))) {
				bean.setModifiedDate(CommonFunction.formatDateSimple((String) dataMap.get("MODIFIED_DATE")));
			}
			if(dataMap.containsKey("DEL_YN") && CoConstDef.FLAG_YES.equals(dataMap.get("DEL_YN"))) {
				bean.setCategory(changeStyle(bean.getCategory(), "del", true));
				bean.setModelName(changeStyle(bean.getModelName(), "del", true));
				bean.setReleaseDate(changeStyle(bean.getReleaseDate(), "del", true));
			} 
			
			// distribution 예약 및 예약취소의 경우만 modifier 가 설정되지 않은 경우, 신규 추가된 것으로 표시한다. 
			else if( (CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_RESERVED.equals(mailType) || CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_CANCELED.equals(mailType) ) 
					&& isEmpty(bean.getModifiedDate())) {
				bean.setCategory(changeStyle(bean.getCategory(), "mod"));
				bean.setModelName(changeStyle(bean.getModelName(), "mod"));
				bean.setReleaseDate(changeStyle(bean.getReleaseDate(), "mod"));
			}
			list.add(bean);
		}
		
		return list;
	}

	private Project makeDistributionInfo(List<Map<String, Object>> mailComponentData) {
		Project bean = null;
		for(Map<String, Object> dataMap : mailComponentData) {
			bean = new Project();
			bean.setDistributeName((String) dataMap.get("DISTRIBUTE_NAME"));
			bean.setDistributeMasterCategory((String) dataMap.get("DISTRIBUTE_MASTER_CATEGORY"));
			bean.setDistributeSoftwareType((String) dataMap.get("DISTRIBUTE_SOFTWARE_TYPE"));
			bean.setDistributeDeployTime((String) dataMap.get("DISTRIBUTE_DEPLOY_TIME"));
			bean.setDistributeDeployYn((String) dataMap.get("DISTRIBUTE_DEPLOY_YN"));
			bean.setDistributeDeployModelYn((String) dataMap.get("DISTRIBUTE_DEPLOY_MODEL_YN"));
			bean.setDistributeDeployErrorMsg((String) dataMap.get("DISTRIBUTE_DEPLOY_ERROR_MSG"));
			bean.setDistributeTarget((String) dataMap.get("DISTRIBUTE_TARGET"));
			bean.setPackageFileId((String) dataMap.get("PACKAGE_FILE_ID"));
			bean.setPackageFileId2((String) dataMap.get("PACKAGE_FILE_ID2"));
			bean.setPackageFileId3((String) dataMap.get("PACKAGE_FILE_ID3"));
			bean.setNoticeFileId((String) dataMap.get("NOTICE_FILE_ID"));
			
			// code convert
			
			// master category
			if(!isEmpty(bean.getDistributeMasterCategory())) {
				bean.setDistributeMasterCategory(CommonFunction.makeCategoryFormat(bean.getDistributeTarget(), bean.getDistributeMasterCategory().substring(0, 3), bean.getDistributeMasterCategory().substring(3)));
			}
			if(!isEmpty(bean.getDistributeSoftwareType())) {
				bean.setDistributeSoftwareType(CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_DEFAULT_SOFTWARE_TYPE, bean.getDistributeSoftwareType()));
			}
			// target site
			if(!isEmpty(bean.getDistributeTarget())) {
				bean.setDistributeTarget(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_CODE, bean.getDistributeTarget()));
			}
			break;
		}
		return bean;
	}

	@SuppressWarnings("unchecked")
	private List<OssComponents> makeIdentificationOssListInfo(Map<String, Object>  mailComponentDataMap, String component) {
		List<ProjectIdentification> projectList = null;
		List<OssComponents> list = null;
		OssComponents bean = null;
		String currentGroupKey = null;
		Project project = (Project) mailComponentDataMap.get("projectBean");
		String networkServerType = "";
		String networkRestriction = "";
		
		if(project != null) {
			networkServerType = project.getNetworkServerType();
			networkRestriction = CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, CoConstDef.CD_LICENSE_NETWORK_RESTRICTION).toUpperCase();
		}
		
		if(mailComponentDataMap != null && (mailComponentDataMap.containsKey("mainData") || mailComponentDataMap.containsKey("rows") )) {
			projectList = (List<ProjectIdentification>) mailComponentDataMap.get(mailComponentDataMap.containsKey("mainData") ? "mainData" : "rows");
			for(ProjectIdentification prjBean : projectList) {
				// exclude 제외
				if(CoConstDef.FLAG_YES.equals(prjBean.getExcludeYn())) {
					continue;
				}
				
				if(currentGroupKey != null && currentGroupKey.equals(prjBean.getGroupingColumn())) {
					continue;
				} else {
					currentGroupKey = prjBean.getGroupingColumn();
				}
				
				
				if(CoConstDef.CD_MAIL_COMPONENT_PROJECT_DISCROSEOSSINFO.equals(component)
						&& !CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK.equals(prjBean.getObligationLicense()) ? prjBean.getObligationType() : prjBean.getObligationLicense())) {
					continue;
				}
				
				if(CoConstDef.FLAG_YES.equals(networkServerType)
						&& !networkRestriction.equals(prjBean.getRestriction().toUpperCase())) {
					continue;
				}
				
				if(list == null) {
					list = new ArrayList<>();
				}
				bean = new OssComponents();
				bean.setOssName(prjBean.getOssName()); 
				bean.setOssVersion(prjBean.getOssVersion()); 
				bean.setLicenseName(prjBean.getLicenseName()); 
				list.add(bean);
			}
		}
		return list;
	}

	private Project makeProjectBasicInfo(List<Map<String, Object>> mailComponentData) {
		Project bean = null;
		String packageFileName = null;
		String packageFileName2 = null;
		String packageFileName3 = null;
		String noticeFileName = null;
		for(Map<String, Object> dataMap : mailComponentData) {
			bean = new Project();
			bean.setPrjId(avoidNull((String) dataMap.get("PRJ_ID"))); 
			bean.setPrjName(avoidNull((String) dataMap.get("PRJ_NAME"))); 
			bean.setPrjVersion(avoidNull((String) dataMap.get("PRJ_VERSION"))); 
			bean.setDistributionType(avoidNull((String) dataMap.get("DISTRIBUTION_TYPE"))); 
			bean.setNetworkServerType(avoidNull((String) dataMap.get("NETWORK_SERVER_TYPE")));
			bean.setComment(avoidNull((String) dataMap.get("COMMENT"))); 
			bean.setOsType(avoidNull((String) dataMap.get("OS_TYPE"))); 
			bean.setOsTypeEtc(avoidNull((String) dataMap.get("OS_TYPE_ETC"))); 
			bean.setDistributeTarget(avoidNull((String) dataMap.get("DISTRIBUTE_TARGET"))); 
			bean.setCreator(makeUserNameFormatWithDivision(avoidNull((String) dataMap.get("CREATOR")))); 
			bean.setReviewer(makeUserNameFormatWithDivision(avoidNull((String) dataMap.get("REVIEWER")))); 
			bean.setIdentificationStatus(avoidNull((String) dataMap.get("IDENTIFICATION_STATUS"))); 
			bean.setVerificationStatus(avoidNull((String) dataMap.get("VERIFICATION_STATUS"))); 
			bean.setDestributionStatus(avoidNull((String) dataMap.get("DESTRIBUTION_STATUS")));
			bean.setNoticeType(avoidNull((String) dataMap.get("NOTICE_TYPE")));
			bean.setNoticeTypeEtc(avoidNull((String) dataMap.get("NOTICE_TYPE_ETC")));
			bean.setPriority(avoidNull((String) dataMap.get("PRIORITY")));
			bean.setDivision(avoidNull((String) dataMap.get("DIVISION")));
			
			packageFileName = (String) dataMap.get("PACKAGE_FILE_ID");
			packageFileName2 = (String) dataMap.get("PACKAGE_FILE_ID2");
			packageFileName3 = (String) dataMap.get("PACKAGE_FILE_ID3");
			noticeFileName = (String) dataMap.get("NOTICE_FILE_ID");
			break;
		}

		if(bean != null) {
			// published file 정보가 존재할 경우 + packaging 상태가 confirm인 경우 파일 정보 노출
			if(CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(bean.getVerificationStatus())) {
				if(!isEmpty(packageFileName)) {
					T2File packageFile = fileService.selectFileInfo(packageFileName);
					if(packageFile != null) {
						bean.setPackageFileId(packageFile.getOrigNm());
					}
				}
				if(!isEmpty(packageFileName2)) {
					T2File packageFile2 = fileService.selectFileInfo(packageFileName2);
					if(packageFile2 != null) {
						bean.setPackageFileId2(packageFile2.getOrigNm());
					}
				}
				if(!isEmpty(packageFileName3)) {
					T2File packageFile3 = fileService.selectFileInfo(packageFileName3);
					if(packageFile3 != null) {
						bean.setPackageFileId3(packageFile3.getOrigNm());
					}
				}
				if(!isEmpty(noticeFileName)) {
					T2File noticeFile = fileService.selectFileInfo(noticeFileName);
					if(noticeFile != null) {
						bean.setNoticeFileId(noticeFile.getOrigNm());
					}
				}
			}
			
			// 코드변환
			if(!isEmpty(bean.getDistributionType())) {
				bean.setDistributionType(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, bean.getDistributionType()));
			}
			if(!isEmpty(bean.getOsType())) {
				bean.setOsType(CoConstDef.COMMON_SELECTED_ETC.equals(bean.getOsType()) ? bean.getOsTypeEtc() : CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, bean.getOsType()));
			}
			if(!isEmpty(bean.getIdentificationStatus())) {
				bean.setIdentificationStatus(CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, bean.getIdentificationStatus()));
			}
			if(!isEmpty(bean.getVerificationStatus())) {
				bean.setVerificationStatus(CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, bean.getVerificationStatus()));
			}
			if(!isEmpty(bean.getDestributionStatus())) {
				bean.setDestributionStatus(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_STATUS, bean.getDestributionStatus()));
			}
			if(!isEmpty(bean.getDistributeTarget())) {
				bean.setDistributeTarget(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_CODE, bean.getDistributeTarget()));
			}
			
			if(!isEmpty(bean.getNoticeType())) {
				String noticeType = CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, bean.getNoticeType());
				String noticeTypeEtc = CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, bean.getNoticeTypeEtc());
				if(!isEmpty(noticeTypeEtc)) {
					noticeType += " (" + noticeTypeEtc + ")";
				}
				
				bean.setNoticeType(noticeType);
			}
			
			if(!isEmpty(bean.getPriority())) {
				bean.setPriority(CoCodeManager.getCodeString(CoConstDef.CD_PROJECT_PRIORITY, bean.getPriority()));
			}
			
			if(!isEmpty(bean.getDivision())) {
				bean.setDivision(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, bean.getDivision()));
			}
						
		}
		
		return bean;
	}

	/**
	 * 101 라이선스 기본정보
	 * @param mailComponentData
	 * @return
	 */
	private LicenseMaster makeLicenseBasicInfo(List<Map<String, Object>> mailComponentData) {
		LicenseMaster bean = null;
		for(Map<String, Object> dataMap : mailComponentData) {
			bean = new LicenseMaster();
			bean.setLicenseId(avoidNull((String) dataMap.get("LICENSE_ID"))); 
			bean.setLicenseName(avoidNull((String) dataMap.get("LICENSE_NAME"))); 
			bean.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, avoidNull((String) dataMap.get("LICENSE_TYPE")))); 
			bean.setObligationDisclosingSrcYn(avoidNull((String) dataMap.get("OBLIGATION_DISCLOSING_SRC_YN"))); 
			bean.setObligationNotificationYn(avoidNull((String) dataMap.get("OBLIGATION_NOTIFICATION_YN"))); 
			bean.setObligationNeedsCheckYn(avoidNull((String) dataMap.get("OBLIGATION_NEEDS_CHECK_YN"))); 
			
			if(CoConstDef.FLAG_YES.equals(bean.getObligationNeedsCheckYn())) {
				bean.setObligation(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, CoConstDef.CD_DTL_OBLIGATION_NEEDSCHECK)); 
			} else if(CoConstDef.FLAG_YES.equals(bean.getObligationDisclosingSrcYn())) {
				bean.setObligation(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE));
			} else if(CoConstDef.FLAG_YES.equals(bean.getObligationNotificationYn())) {
				bean.setObligation(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, CoConstDef.CD_DTL_OBLIGATION_NOTICE));
			}
			
			bean.setShortIdentifier(avoidNull((String) dataMap.get("SHORT_IDENTIFIER"))); 
			bean.setWebpage(avoidNull((String) dataMap.get("WEBPAGE")));
			if(!isEmpty(bean.getWebpage()) && !(bean.getWebpage().startsWith("http://") || bean.getWebpage().startsWith("https://"))) {
				bean.setWebpage("http://" + bean.getWebpage());
			}
			
			bean.setDescription(CommonFunction.htmlEscape(avoidNull((String) dataMap.get("DESCRIPTION")))); 
			bean.setLicenseText(CommonFunction.htmlEscape(avoidNull((String) dataMap.get("LICENSE_TEXT")))); 
			bean.setAttribution(CommonFunction.htmlEscape(avoidNull((String) dataMap.get("ATTRIBUTION")))); 
		
			bean.setLicenseNickname(avoidNull((String) dataMap.get("LICENSE_NICKNAME")));
			
			// RESTRICTION 정보 메일에 추가
			String restrictionStr = "";
			if(!isEmpty((String) dataMap.get("RESTRICTION"))) {
				for(String restrictionCd : ((String) dataMap.get("RESTRICTION")).split(",")) {
					if(!isEmpty(restrictionStr)) {
						restrictionStr += ", ";
					}
					restrictionStr += CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_RESTRICTION, restrictionCd);
				}
			}
			bean.setRestriction(restrictionStr);
			
			break;
		}
		return bean;
	}

	/**
	 * 100 OSS 기본정보
	 * @param mailComponentData
	 * @return
	 */
	private OssMaster makeOssBasicInfo(List<Map<String, Object>> mailComponentData) {
		OssMaster bean = null;
		
		
		// oss master, license list, nick name list를 분리한다
		boolean isFirst = true;
		OssLicense license = null;
		
		for(Map<String, Object> dataMap : mailComponentData) {
			if(isFirst) {
				bean = new OssMaster();
				bean.setOssId(avoidNull((String) dataMap.get("OSS_ID")));
				bean.setOssName((String) dataMap.get("OSS_NAME"));
				bean.setOssVersion((String) dataMap.get("OSS_VERSION"));
				bean.setOssType(avoidNull((String) dataMap.get("OSS_TYPE")));
				String result = appendChangeStyleLinkFormatArray((String) dataMap.get("DOWNLOAD_LOCATION"));
				bean.setDownloadLocation(result);
				bean.setHomepage(avoidNull((String) dataMap.get("HOMEPAGE")));
				bean.setSummaryDescription(CommonFunction.htmlEscape(avoidNull((String) dataMap.get("SUMMARY_DESCRIPTION"))));
				bean.setAttribution(CommonFunction.htmlEscape(avoidNull((String) dataMap.get("ATTRIBUTION"))));
				bean.setCopyright(CommonFunction.htmlEscape(avoidNull((String) dataMap.get("COPYRIGHT")))); // copyright 는 tag 를 포함하고 있기 때문에, 메일 발송시는 (html) escape 처리함
				bean.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, avoidNull((String) dataMap.get("OSS_LICENSE_TYPE"))));
				bean.setObligation(CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, avoidNull((String) dataMap.get("OSS_OBLIGATION_TYPE"))));
				bean.setOssNickname(avoidNull((String) dataMap.get("OSS_NICKNAME")));
				if(!isEmpty((String) dataMap.get("CREATED_DATE"))) {
					bean.setCreatedDate(DateUtil.dateFormatConvert((String) dataMap.get("CREATED_DATE"), DateUtil.TIMESTAMP_PATTERN, DateUtil.DATE_PATTERN_DASH));
				}
				
				if(!isEmpty((String) dataMap.get("CREATOR"))) {
					bean.setCreator(CoMailManager.getInstance().makeUserNameFormat((String) dataMap.get("CREATOR")));
				}
				
				if(!isEmpty(bean.getOssId()) && CoCodeManager.OSS_INFO_BY_ID.containsKey(bean.getOssId())) {
					bean.setMultiLicenseFlag(CoCodeManager.OSS_INFO_BY_ID.get(bean.getOssId()).getLicenseDiv());
				}
				
				String detectedLicenses = dataMap.containsKey("DETECTED_LICENSE") ? (String) dataMap.get("DETECTED_LICENSE") : "";
				bean.setDetectedLicense(detectedLicenses);
			}
			if(dataMap.containsKey("LICENSE_ID")) {
				license = new OssLicense();
				license.setLicenseId((String) dataMap.get("LICENSE_ID"));
				license.setOssLicenseIdx((String) dataMap.get("OSS_LICENSE_IDX"));
				license.setOssLicenseComb((String) dataMap.get("OSS_LICENSE_COMB"));
				license.setOssCopyright(CommonFunction.htmlEscape(avoidNull((String) dataMap.get("OSS_COPYRIGHT"))));
				license.setOssLicenseText((String) dataMap.get("OSS_LICENSE_TEXT"));
				license.setLicenseName((String) dataMap.get("LICENSE_NAME"));
				license.setLicenseType(CoCodeManager.getCodeString(CoConstDef.CD_LICENSE_TYPE, avoidNull((String) dataMap.get("LICENSE_TYPE"))));
				bean.addOssLicense(license);
			}
			
			isFirst = false;
		}
		if(bean != null && bean.getOssLicenses() != null && !bean.getOssLicenses().isEmpty()) {
			bean.setLicenseName(CommonFunction.makeLicenseExpression(bean.getOssLicenses()));
		}
		
		return bean;
	}

	/**
	 * Sets the basic info.
	 *
	 * @param params the params
	 * @param key the key
	 * @return 
	 */
	private List<Map<String, Object>> getMailComponentData(List<String> params, String key) {
		// sql 문 생성
		String sql = CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_COMPONENT_NAME, key);
		List<Map<String, Object>> dataList = new ArrayList<>();

		try (
				Connection conn = DriverManager.getConnection(connStr, connUser, connPw);
				PreparedStatement pstmt = conn.prepareStatement(sql);
		) {

			int parameterIndex = 1;
			for (String param : params) {

				pstmt.setString(parameterIndex++, param);
			}
			try (
					ResultSet rs = pstmt.executeQuery();
			) {
				if (rs != null) {
					ResultSetMetaData rsmd = rs.getMetaData();
					int colCount = rsmd.getColumnCount(); // 컬럼수
					Map<String, Object> dataMap;
					while (rs.next()) {
						dataMap = new HashMap<>();
						for (int colIdx = 1; colIdx <= colCount; colIdx++) {
							String _contents = (String) rs.getString(colIdx);
							if (avoidNull(_contents).indexOf("\n") > -1) {
								_contents = _contents.replaceAll("\n", "<br />");
							}
							dataMap.put(rsmd.getColumnLabel(colIdx), _contents);
						}

						if (CoConstDef.CD_MAIL_COMPONENT_OSSBASICINFO.equals(key)) {
							if (dataMap.containsKey("identificationStatus") && dataMap.containsKey("verificationStatus")
									&& CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(dataMap.get("identificationStatus")) && CoConstDef.CD_DTL_IDENTIFICATION_STATUS_CONFIRM.equals(dataMap.get("verificationStatus"))) {
								if (dataMap.containsKey("noticeFileId")) {
									dataMap.remove("noticeFileId");
								}
								if (dataMap.containsKey("packageFileId")) {
									dataMap.remove("packageFileId");
								}
							}

						}

						if (CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_PRJ.equals(key) || CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_OSS.equals(key) || CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_PROJECT_RECALCULATED_ALL.equals(key)) {
							dataMap.remove("noticeFileId");
							dataMap.remove("packageFileId");
						}

						dataList.add(dataMap);
					}
				}
			}

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return dataList;
	}
	

	private List<Map<String, Object>> getMailComponentDataWithArray(List<String> params, String key) {
		// sql 문 생성
		String sql = CoCodeManager.getCodeExpString(CoConstDef.CD_MAIL_COMPONENT_NAME, key);
		List<Map<String, Object>> dataList = new ArrayList<>();
		// sql param 생성
		sql = sql.replace("?", createInQuery(params));
		try(
			Connection conn = DriverManager.getConnection(connStr, connUser, connPw);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery()
		) {
			
			if (rs != null) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int colCount = rsmd.getColumnCount(); // 컬럼수
				Map<String, Object> dataMap;
				while (rs.next()) {
					dataMap = new HashMap<>();
					for(int colIdx=1; colIdx<=colCount; colIdx++) {
						String _contents = (String)rs.getString(colIdx);
						if(avoidNull(_contents).indexOf("\n") > -1) {
							_contents = _contents.replaceAll("\n", "<br />");
						}
						dataMap.put(rsmd.getColumnLabel(colIdx), _contents);
					}
					
					if(CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_PRJ.equals(key) ||  CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_OSS.equals(key) || CoConstDef.CD_MAIL_COMPONENT_VULNERABILITY_PROJECT_RECALCULATED_ALL.equals(key)) {
						dataMap.remove("noticeFileId");
						dataMap.remove("packageFileId");
					}
					dataList.add(dataMap);
				}
			}
			
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
		}
		return dataList;
	}

	private CharSequence createInQuery(List<String> params) {
		StringBuilder keyList = new StringBuilder();
		for(String key : params) {
			if(keyList.length() > 0) {
				keyList.append(" OR ");
			}
			keyList.append(" CONCAT(UPPER(T1.OSS_NAME), '_', T1.OSS_VERSION) = '"+key+"'");
		}
		
		return keyList.toString();
	}

	/**
	 * Get velocity template content.
	 *
	 * @param path the path
	 * @param model the model
	 * @return the string
	 */
	private String getVelocityTemplateContent(String path, Map<String, Object> model) {
		VelocityContext context = new VelocityContext();
		Writer writer = new StringWriter();
		VelocityEngine vf = new VelocityEngine();
		Properties props = new Properties();
		
		for(String key : model.keySet()) {
			if(!"templateUrl".equals(key)) {
				context.put(key, model.get(key));
			}
		}
		
		context.put("domain", CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org"));
		context.put("commonFunction", CommonFunction.class);
	    
		props.put("resource.loader", "class");
	    props.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
	    props.put("input.encoding", "UTF-8");
	    
	    vf.init(props);
	    
		try {
			Template template = vf.getTemplate(path); // file name
			template.merge(context, writer);
			
			return writer.toString();
		} catch (Exception e) {
			log.error("Exception occured while processing velocity template:" + e.getMessage());
		}
		return "";
	}
	
	private String[] selectMailAddrFromIds(String[] toIds) {
		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("idArr", toIds);
		List<String> mailList = mailManagerMapper.selectMailAddrFromIds(param);
		String[] results = new String[mailList.size()];
		return mailList.toArray(results);
	}

	private String[] selectAdminMailAddr() {
		String adminMailStr = avoidNull(CommonFunction.getProperty("smtp.default.admin"));
		
		if(isEmpty(adminMailStr)) {
			List<String> adminMailList = mailManagerMapper.selectAdminMailAddr();
			String[] array = new String[adminMailList.size()];
			
			return adminMailList.toArray(array);
		} else {
			return new String[]{adminMailStr};
		}
	}
	
	private void sendEmail(CoMail coMail) {
		// Send Email Info Setting
		try{
			MimeMessage message = mailSender.createMimeMessage();
			
			String _replyToId = "";
			if(!isEmpty(coMail.getParamLicenseId())) {
				_replyToId = "LI" +coMail.getParamLicenseId();
			} else if(!isEmpty(coMail.getParamOssId())) {
				_replyToId = "OS"+coMail.getParamOssId();
			} else if(!isEmpty(coMail.getParamPrjId())) {
				_replyToId = "PJ"+coMail.getParamPrjId();
			} else if(!isEmpty(coMail.getParamPartnerId())) {
				_replyToId = "PN"+coMail.getParamPartnerId();
			}
			
			if(!isEmpty(_replyToId)) {
				_replyToId = "<OSC." +_replyToId + "@fosslight.org>";
				message.setHeader("In-Reply-To", _replyToId);
				message.setHeader("References", _replyToId);
			}
			
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			
			String userId = coMail.getLoginUserName();
			String userName = "";
			
			if(isEmpty(userId)) {
				// 시스템에서 발송하는 case
				// 1. bat 관련
				if(!isEmpty(coMail.getParamBatId())) {
					BinaryMaster batBean = mailManagerMapper.getBinaryInfo(coMail.getParamBatId());
					if(batBean != null && !isEmpty(batBean.getCreator())) {
						userId = batBean.getCreator();
					}
				}
			}
			
			if(!isEmpty(userId)) {
				T2Users userParam = new T2Users();
				userParam.setUserId(userId);
				T2Users userInfo = userMapper.getUser(userParam);
				
				if(userInfo != null && !isEmpty(userInfo.getUserName())) {
					userName = userInfo.getUserName();
				}
			}
			
			String mailFrom = CoCodeManager.getCodeExpString(CoConstDef.CD_SMTP_SETTING, CoConstDef.CD_SMTP_EMAIL_ADDRESS);

			if(CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DELETED.equals(coMail.getMsgType()) 
						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DIFF_FILE.equals(coMail.getMsgType()) 
						|| CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_EDIT_FILE.equals(coMail.getMsgType()) ) {
				InternetAddress from = new InternetAddress(mailFrom, "FOSSLight Hub" + " (FOSSLight)", "UTF-8");
				helper.setFrom(from);
			}
			else if(!isEmpty(userId) && !isEmpty(userName)) {
				if(userName.length() > 15 && userName.contains("/") && userName.split("/").length > 1) {
					userName = userName.substring(0, userName.lastIndexOf("/"));
				}
				InternetAddress from = new InternetAddress(mailFrom, userName + " " + userId + " (FOSSLight)", "UTF-8");
				helper.setFrom(from);
			} else {
				helper.setFrom(mailFrom);
			}

            if(!isEmpty(DEFAULT_BCC)) {
            	String[] _bcc = coMail.getBccIds() == null ? new String[]{} : coMail.getBccIds();
            	List<String> _bccList = new ArrayList<>(Arrays.asList(_bcc));
            	if(_bccList == null || _bccList.isEmpty()) {
            		_bccList = new ArrayList<>();
            	}
            	_bccList.add(DEFAULT_BCC);
            	coMail.setBccIds(_bccList.toArray(new String[_bccList.size()]));
            }
            
            helper.setTo(coMail.getToIds());
			helper.setCc(coMail.getCcIds() != null ? coMail.getCcIds() : new String[]{});
			helper.setBcc(coMail.getBccIds() != null ? coMail.getBccIds() : new String[]{});
			helper.setSubject(coMail.getEmlTitle());
			helper.setText(coMail.getEmlMessage(), true);
			
			// Email Send
			mailSender.send(message);
			// Email History Status Update
			coMail.setSndStatus("C");	// 전송완료
			mailManagerMapper.updateSendStatus(coMail);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
			
			coMail.setSndStatus("F");	// 전송실패
			coMail.setErrorMsg(e.getMessage());
			mailManagerMapper.updateErrorMsg(coMail);
		}		
	}

	public void sendErrorMail(CoMail bean) {
		boolean isProd = "REAL".equals(avoidNull(CommonFunction.getProperty("server.mode")));
		if(isProd) {
			bean.setToIds(selectAdminMailAddr());
			try {
				bean.setCreationUserId("system");
				mailManagerMapper.insertEmailHistory(bean);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			
			// 발송처리
			new Thread(() -> sendEmail(bean)).start();
		}
	}
}


