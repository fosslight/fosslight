/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.History;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2Users;
import oss.fosslight.repository.HistoryMapper;
import oss.fosslight.repository.MailManagerMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.MailService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.util.StringUtil;

@Service
@Slf4j
public class MailServiceImpl extends CoTopComponent implements MailService {
	//Service
	@Autowired T2UserService userService;
	JavaMailSender mailSender;
	
	// Mapper
	@Autowired MailManagerMapper mailManagerMapper;
	@Autowired HistoryMapper historyMapper;
	@Autowired ProjectMapper projectMapper;
	
	public void sendMailRunTimeout(){
		mailManagerMapper.updateSendMailRunTimeout(new CoMail());
	}

	@Override
	public void sendMail(History h, String[] receiverId, String[] ccIds, String[] bccIds) {
		CoMail receiverInfo = new CoMail();
		
		receiverInfo.setToIds(selectMailAddrFromIds(receiverId));					// 수신인 메일 주소
		
		if (ccIds != null && ccIds.length != 0) {
			selectMailAddrFromIds(ccIds);		// 참조 메일 주소
		}
		
		if (bccIds != null && bccIds.length != 0) {
			selectMailAddrFromIds(bccIds);		// 숨은참조 메일 주소
		}
		
		sendProcessor(h, receiverInfo);
	};

	@Override
	public String[] selectMailAddrFromIds(String[] toIds) {
		Map<String, String[]> param = new HashMap<String, String[]>();
		param.put("idArr", toIds);
		List<String> mailList = mailManagerMapper.selectMailAddrFromIds(param);
		String[] results = new String[mailList.size()];
		
		return mailList.toArray(results);
	}
	
	public void sendProcessor(History h, CoMail rcvInfo){
//		History mailInfo = historyMapper.selectOneHistoryData(h);// 받아온 History의 idx로 데이터를 조회한다.
//		Map<String, Object> mailData = new HashMap<String, Object>();
//		
//		// 수정의 경우 비교데이터
//		if ("UPDATE".equals(mailInfo.gethAction())){
//			mailData = getAsToBeHistoryData(mailInfo);
//		} else {
//			mailData = toMailData(mailInfo);
//		}
		
		CoMail coMail = new CoMail();		// 메일 데이터
		
		coMail.setToIds(rcvInfo.getToIds());	// 수신
		coMail.setCcIds(rcvInfo.getCcIds());	// 참조
		coMail.setBccIds(rcvInfo.getBccIds());	// 숨은참조
		
		mailManagerMapper.insertEmailHistory(coMail);
		
		new Thread(() -> sendEmail(coMail)).start();
	}

	// History이전 데이터와 현재 데이터를 비교합니다.
	@SuppressWarnings("unchecked")
	public Map<String,Object> getAsToBeHistoryData(History history){
		History afterData = historyMapper.selectOneHistoryData(history);				// 현재 History(기준)
		History beforeData = historyMapper.selectOneHistoryBeforeData(afterData);		// 이전 History
		
		Map<String, Object> beforeMap =  beforeData != null ? ((Map<String, Object>)fromJson((String) beforeData.gethData(), Map.class)) : null;
		Map<String, Object> afterMap =  afterData != null ? ((Map<String, Object>)fromJson((String) afterData.gethData(), Map.class)) : null;
		Map<String, Object> asTbl = new HashMap<String, Object>();
		Map<String, Object> beTbl = new HashMap<String, Object>();
		List<Map<String, Object>> asSubList = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> beSubList = new ArrayList<Map<String, Object>>();
		
		// Data : dtlCd
		// Param : String CD_NO
        // Result : Vector [ String CD_DTL_NO, String CD_DTL_NM(Key), String CD_SUB_NO(Ref Entity) ] 
		String cdNm = afterData != null ? afterData.gethType() : null; 
		
		for (String[] dtlCd : CoCodeManager.getValues(cdNm)){
			Object beforeD_ = getDataForType(cdNm, dtlCd, beforeMap);
			Object afterD_ = getDataForType(cdNm, dtlCd, afterMap);
			
			// main data
			if (beforeD_ instanceof String && afterD_ instanceof String) {
				log.debug("String value : " + dtlCd[1]);
				String bStr = beforeD_.toString();
				String aStr = afterD_.toString();
				
				// 값이 없을 경우 '-'
				asTbl.put(dtlCd[1], !StringUtil.isEmpty(bStr) ? bStr : "-");
				beTbl.put(dtlCd[1], !StringUtil.isEmpty(aStr) ? aStr : "-");
			} else { // sub list data
				log.debug("Object Key : " + dtlCd[1]);
				asTbl.put(dtlCd[1], "");
				beTbl.put(dtlCd[1], "");
				
				asSubList.add((Map<String, Object>)beforeD_);
				beSubList.add((Map<String, Object>)afterD_);
			}
		}
		
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("before", asTbl);
		dataMap.put("after", beTbl);
		dataMap.put("bSub", asSubList);
		dataMap.put("aSub", beSubList);
		dataMap.put("bModifier", beforeData != null ? beforeData.getModifier() : "");
		dataMap.put("aModifier", afterData != null ? afterData.getModifier() : "");
		dataMap.put("bModifiedDate", beforeData != null ? beforeData.getModifiedDate() : "");
		dataMap.put("aModifiedDate", afterData != null ? afterData.getModifiedDate() : "");
		
		if (!StringUtil.isEmpty(beforeData != null ? beforeData.getModifier() : "")) {
			T2Users param = new T2Users();
			param.setUserId(beforeData.getModifier());
			T2Users modifier = userService.getUser(param);
			
			dataMap.put("bModifierNm", modifier.getUserName());
			dataMap.put("bModifierMail", modifier.getEmail());
		}
		
		if (!StringUtil.isEmpty(beforeData != null ? afterData.getModifier() : "")) {
			T2Users param = new T2Users();
			param.setUserId(afterData.getModifier());
			T2Users modifier = userService.getUser(param);
			
			dataMap.put("aModifierNm", modifier.getUserName());
			dataMap.put("aModifierMail", modifier.getEmail());
		}
		
		log.debug(dataMap.toString());
		
		return dataMap;
	}
		
	// History DTO를 메일발송을 위한 HashMap<>으로 변환
	@SuppressWarnings("unchecked")
	protected Map<String, Object> toMailData(History history){
		Map<String, Object> hMap =  history != null ? ((Map<String, Object>)fromJson((String) history.gethData(), Map.class)) : null;
		Map<String, Object> mTbl = new HashMap<String, Object>();
		List<Map<String, Object>> sTblList = new ArrayList<Map<String, Object>>();
		
		// Data		: dtlCd
		// Param	: String CD_NO
		// Result	: Vector [ String CD_DTL_NO, String CD_DTL_NM(Key), String CD_SUB_NO(Ref Entity) ] 
		/*
		 * hMap 맵에서 dtlCd[1]의 값을 가져온다.
		 */
		String cdNm = history != null ? history.gethType() : null; 
		
		CoCodeManager.getValues(cdNm).forEach((dtlCd) -> {
			Object d = getDataForType(cdNm, dtlCd, hMap);
			
			// main data
			if (d instanceof String) { // d의 Type이 String일 경우
				mTbl.put(dtlCd[1], !StringUtil.isEmpty(d.toString()) ? d : "");
			} else { // sub list obj
				mTbl.put(dtlCd[1], "");
				
				try {
					sTblList.add((Map<String, Object>)d);
				} catch(Exception e){
					log.error("['" + dtlCd[1] + "'] CANNOT TYPE CAST TO 'MAP<String, Object>'. Check this Object.", e);
				}
			}
		});
		
		Map<String, Object> dataMap = new HashMap<String, Object>();
		dataMap.put("main", mTbl);
		dataMap.put("sub", sTblList);
		dataMap.put("modifier", history != null ? history.getModifier() : "");
		dataMap.put("modifiedDate", history != null ? history.getModifiedDate() : "");
		
		if (!StringUtil.isEmpty(history != null ? history.getModifier() : "")) {
			T2Users param = new T2Users();
			param.setUserId(history.getModifier());
			T2Users modifier = userService.getUser(param);
			
			dataMap.put("modifierNm", modifier.getUserName());
			dataMap.put("modifierMail", modifier.getEmail());
		}
		
		log.debug(dataMap.toString());
		
		return dataMap;
	}
	
	@SuppressWarnings("unchecked")
	public Object getDataForType(String cdNm, String[] dtlCd, Map<String, Object> dMap){
		Object ret = null;
		// EXP : TYPE(String, Code, Array, Object) | NAME | CD_NO
		String[] inf = CoCodeManager.getCodeExpString(cdNm, dtlCd[0]).split("\\|");
		
		if (inf[0].equals("String")) {
			ret = dMap != null ? escapeSql(nvl((String)dMap.get(dtlCd[1]), "")) : "";
		} else if (inf[0].equals("Code")) {
			ret = dMap != null ? nvl(CoCodeManager.getCodeString(inf[2], (String)dMap.get(dtlCd[1])), inf[2]) : "";
		} else if (inf[0].equals("Array") && dtlCd[2] == null) {
			List<String> asArr = dMap != null ? (ArrayList<String>)dMap.get(dtlCd[1]) : null;
			ret = asArr != null && asArr.size() > 0 ? String.join(", ", asArr) : "";
		} else if (inf[0].equals("Array") && dtlCd[2] != null) {
			String sCdNm = dtlCd[2]; // CD_SUB_NO(Ref Entity) 
			HashMap<String, Object> sTblMap = new HashMap<String, Object>();
			// colNames 생성
			List<Map<String, Object>> colNames = new ArrayList<Map<String, Object>>(); 
			
			// Param : String CD_NO
	        // Result : Vector&String [ String CD_DTL_NO, String CD_SUB_NO, String CD_DTL_NM, String CD_DTL_NM, String CD_DTL_EXP, String CD_ORDER ]
			HashMap<String, Object> colInfo =  new HashMap<String, Object>();
			colInfo.put("key", "no");
			colInfo.put("name", "no");
			colInfo.put("order", "0");
			colNames.add(colInfo);
			
			for (String[] v : CoCodeManager.getAllValues(sCdNm)){
				 colInfo =  new HashMap<String, Object>();
				 colInfo.put("key", v[2]);
				 colInfo.put("name", v[4].split("\\|")[1]);
				 colInfo.put("order", v[5]);
				 
				 colNames.add(colInfo);
			}
			
			sTblMap.put("type", inf[1]);		// model name
			sTblMap.put("colNames", colNames);	// list columns
			
			// colModel 생성
			List<Map<String, Object>> subList = new ArrayList<Map<String, Object>>();
			
			if (dMap != null && dMap.get(dtlCd[1]) != null){
				// sub list 생성
				int sCnt = 1;
				
				for (Map<String, Object> sMap : (List<Map<String, Object>>) dMap.get(dtlCd[1])){
					Map<String, Object> sDataMap = new HashMap<String, Object>();
					sDataMap.put("no", sCnt++);
					
					// row data 생성
					for (String[] sDtlCd : CoCodeManager.getValues(sCdNm)){
						String[] sInf = CoCodeManager.getCodeExpString(sCdNm, sDtlCd[0]).split("\\|");	// EXP : TYPE(String, Code, Array, Object) | NAME | CD_NO
						
						if (sInf[0].equals("String")) {
							sDataMap.put(sDtlCd[1], escapeSql(nvl((String)sMap.get(sDtlCd[1]))) );
						} else if (sInf[0].equals("Code")) {
							sDataMap.put(sDtlCd[1], CoCodeManager.getCodeString(sInf[1], (String)sMap.get(sDtlCd[1])) );
						}	
					}
					
					subList.add(sDataMap);
				}
			}
			
			sTblMap.put("subList", subList);
			
			ret = sTblMap;
		}
		
		return ret;
	}
	
	public String escapeSql(String str){
		return str == null ? "null" : String.format("%s", str.replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\""));
	}
	
	/** 메일 발송 **/
	public void sendEmail(CoMail coMail) {
		// Send Email Info Setting
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(coMail.getEmlFrom());
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
			log.error(e.getMessage());
			
			coMail.setSndStatus("F");	// 전송실패
			coMail.setErrorMsg(e.getMessage());
			mailManagerMapper.updateErrorMsg(coMail);
		}
	}
	
	public void sendTempMail() {
		List<Map<String, Object>> tempMailList = mailManagerMapper.getTempMail();
		
		for (Map<String, Object> mailMap : tempMailList) {
			String mailType = (String) mailMap.get("mailType");
			String mailSeq = (String) mailMap.get("mailSeq");
			CoMail mailBean = new CoMail(mailType);
			
			switch(mailType) {
				// BAT Thread return mail
				case CoConstDef.CD_MAIL_TYPE_BAT_COMPLETE:
				case CoConstDef.CD_MAIL_TYPE_BAT_ERROR:
					mailBean.setModifier((String) mailMap.get("modifier"));
					mailBean.setCreationUserId((String) mailMap.get("creator"));
					mailBean.setParamBatId((String) mailMap.get("paramBatId"));
					mailBean.setToIds(new String[] {(String) mailMap.get("toIds")});
					
					CoMailManager.getInstance().sendMail(mailBean);
					
					break;
				// Distribution process mail
				case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_DIFF_FILE:
					mailBean.setParamPrjId((String) mailMap.get("paramPrjId"));
					mailBean.setParamExpansion1((String) mailMap.get("paramExpansion1"));
					mailBean.setParamExpansion2((String) mailMap.get("paramExpansion2"));
					mailBean.setParamExpansion3((String) mailMap.get("paramExpansion3"));
					
					CoMailManager.getInstance().sendMail(mailBean);
					
					break;
				case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_REJECT:
					String prjId = (String) mailMap.get("paramPrjId");
					final Project prjBean = projectMapper.selectProjectMaster2(prjId);
					
					mailBean.setParamPrjId(prjId);
					mailBean.setComment((String) mailMap.get("comment"));
					mailBean.setParamPrjInfo(prjBean);
					
					CoMailManager.getInstance().sendMail(mailBean);
					
					break;
				case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_COMPLETE:
					mailBean.setJobType((String) mailMap.get("jobType"));
				case CoConstDef.CD_MAIL_TYPE_PROJECT_DISTRIBUTE_CANCELED:
					mailBean.setParamPrjId((String) mailMap.get("paramPrjId"));

					CoMailManager.getInstance().sendMail(mailBean);
					
					break;
			}
			
			mailManagerMapper.deleteTempMail(mailSeq);
		}
	}
}
