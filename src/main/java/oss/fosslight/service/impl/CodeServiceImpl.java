/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.domain.CoMail;
import oss.fosslight.domain.CoMailManager;
import oss.fosslight.domain.LicenseMaster;

import oss.fosslight.domain.T2Code;
import oss.fosslight.domain.T2CodeDtl;
import oss.fosslight.domain.T2Users;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.T2UserMapper;
import oss.fosslight.service.CodeService;
import oss.fosslight.service.LicenseService;

@Slf4j
@Service
public class CodeServiceImpl extends CoTopComponent implements CodeService {
	@Autowired CodeMapper codeMapper;
	@Autowired T2UserMapper t2UserMapper;
	@Autowired LicenseService licenseService;

	/**
	 * 코드 목록 조회
	 */
	@Override
	public Map<String, Object> getCodeList(T2Code vo) throws Exception {
		Map<String, Object> map = null;
		int records = codeMapper.selectCodeTotalCount(vo);
		
		if (records > 0) {
			vo.setTotListSize(records);
			map = getGridPagerMap(vo);
			map.put("rows", codeMapper.selectCodeList(vo));
		}
		
		return map == null ? new HashMap<String, Object>() : map;
	}

	/**
	 * 코드상세 목록 조회
	 */
	@Override
	public ArrayList<T2CodeDtl> getCodeDetailList(T2CodeDtl vo) throws Exception {
		ArrayList<T2CodeDtl> codeDetailList = codeMapper.selectCodeDetailList(vo);
		
		if (codeDetailList.size() > 0){
			for (T2CodeDtl t2CodeDtl : codeDetailList) {
				if (isGithubTokenCodeDtl(t2CodeDtl)) {
					t2CodeDtl.setCdDtlExp("");
				}
				t2CodeDtl.setCdDtlNoOrign(t2CodeDtl.getCdDtlNo());
			}
		}
			
		return codeDetailList;
	}

	private boolean isGithubTokenCodeDtl(T2CodeDtl t2CodeDtl) {
		return isExternalServiceCodeNo(t2CodeDtl.getCdNo()) && isGithubTokenCodeDtlNo(t2CodeDtl.getCdDtlNo());
	}

	/**
	 * 코드 저장
	 */
//	@CacheEvict(value="autocompleteCache", allEntries=true)
	@Transactional
	@Override
	public void setCode(T2Code vo) throws Exception {
		// 추가
		if (CoConstDef.GRID_OPERATION_ADD.equals(vo.getOper())) {
			codeMapper.insertCode(vo);
		} else if (CoConstDef.GRID_OPERATION_EDIT.equals(vo.getOper())) { // 수정
			codeMapper.updateCode(vo);
		} else { // 삭제
			codeMapper.deleteCodeDetailAll(vo);
			codeMapper.deleteCode(vo);
		}
	}

	@Override
	public boolean isExists(T2Code vo) {
		return codeMapper.selectCodeTotalCount(vo) > 0;
	}

	@Override
//	@CacheEvict(value="autocompleteCache", allEntries=true)
	@Transactional
	public void setCodeDetails(List<T2CodeDtl> dtlList, String cdNo) throws Exception {
		// if code_no equals CD_USER_DIVISION value, after code_detail_name and detain_no check, division_no update
		if (CoConstDef.CD_USER_DIVISION.equals(cdNo)) {
			T2CodeDtl codeDtlVO = new T2CodeDtl();
			codeDtlVO.setCdNo(cdNo);
			List<T2CodeDtl> beforeCodeDetailList = codeMapper.selectCodeDetailList(codeDtlVO);

			List<T2CodeDtl> filteredCodeDetailList = beforeCodeDetailList
							.stream()
							.filter(before->
									dtlList
										.stream()
										.filter(after->
											(before.getCdDtlNm()).equalsIgnoreCase(after.getCdDtlNm()) && before.getUseYn().equals(after.getUseYn()) && !before.getCdDtlNo().equals(after.getCdDtlNo())).collect(Collectors.toList()).size() > 0
									).collect(Collectors.toList());

			List<T2CodeDtl> filteredAfterCodeDetailList = dtlList
					.stream()
					.filter(before->
							beforeCodeDetailList
								.stream()
								.filter(after->
									(before.getCdDtlNm()).equalsIgnoreCase(after.getCdDtlNm()) && before.getUseYn().equals(after.getUseYn()) && !before.getCdDtlNo().equals(after.getCdDtlNo())).collect(Collectors.toList()).size() > 0
							).collect(Collectors.toList());

			// update division_no value of statistics_MostUsed table
			for (T2CodeDtl cdDtl : filteredCodeDetailList) {
				for (T2CodeDtl cdDtlAfter : filteredAfterCodeDetailList) {
					if (cdDtl.getCdDtlNm().equalsIgnoreCase(cdDtlAfter.getCdDtlNm())) {
						T2CodeDtl codeDtl = new T2CodeDtl();
						codeDtl.setCdDtlNo(cdDtl.getCdDtlNo());
						codeDtl.setCdDtlNoNew(cdDtlAfter.getCdDtlNo());
						codeMapper.updateStatisticsMostUsed(codeDtl);
					}
				}
			}

			// send email if useYn changed from "Y" to "N"
			for (T2CodeDtl before : beforeCodeDetailList) {
				for (T2CodeDtl after : dtlList) {
					if (before.getCdDtlNo().equals(after.getCdDtlNo()) &&
							before.getUseYn().equalsIgnoreCase("Y") &&
							after.getUseYn().equalsIgnoreCase("N")) {

						List<T2Users> t2UsersList = t2UserMapper.selectAllUsersUpdatedDivision(after.getCdDtlNo());
						List<String> toIds = new ArrayList<>();
						for (T2Users user: t2UsersList){
							System.out.println(user);
							toIds.add(user.getEmail());
						}
						try {
							CoMail mailBean = new CoMail(CoConstDef.CD_MAIL_TYPE_OSS_UPDATE);
							mailBean.setEmlTitle("Division이 비활성화되었습니다. 로그인 후 왼쪽 메뉴바의 User name을 클릭하여 Division을 업데이트해주십시오.");
							mailBean.setEmlMessage("Division이 비활성화되었습니다. 로그인 후 왼쪽 메뉴바의 User name을 클릭하여 Division을 업데이트해주십시오.");
							mailBean.setToIds(toIds.toArray(new String[toIds.size()]));
							CoMailManager.getInstance().sendMail(mailBean);
						} catch (Exception e) {
							log.error(e.getMessage(), e);
						}

					}
				}
			}

		}

		List<String> delRestrictionList = null;
		if (CoConstDef.CD_LICENSE_RESTRICTION.equals(cdNo)) {
			T2CodeDtl param = new T2CodeDtl();
			param.setCdNo(cdNo);
			List<T2CodeDtl> codeDetailList = codeMapper.selectCodeDetailList(param);
			
			if (codeDetailList != null && !codeDetailList.isEmpty()) {
				delRestrictionList = codeDetailList
						.stream()
						.filter(e -> 
								dtlList
									.stream()
									.filter(a ->
											(a.getCdDtlNo() + "|" + a.getCdDtlNm()).equalsIgnoreCase(e.getCdDtlNo() + "|" + e.getCdDtlNm())
											).collect(Collectors.toList()).size() == 0
									).map(b -> b.getCdDtlNo())
						.collect(Collectors.toList());
									
			}
		}
		
		if (isExternalServiceCodeNo(cdNo) && hasGithubTokenCodeDtl(dtlList)) {
			// 1. 기존 github token값 보관
			T2CodeDtl githubTokenDtl = codeMapper.getCodeDetail(cdNo, CoConstDef.CD_DTL_GITHUB_TOKEN);

			// 2. 코드 상세 전체 삭제
			T2Code codeVo = new T2Code();
			codeVo.setCdNo(cdNo);

			codeMapper.deleteCodeDetailAll(codeVo);

			// 3. 전체 상세 코드 재등록(추가 / 변경 / 삭제)
			for (T2CodeDtl codeDtl : dtlList) {
				if (isGithubTokenCodeDtlNo(codeDtl.getCdDtlNo()) && codeDtl.getCdDtlExp().isEmpty()) {
					codeMapper.insertCodeDetail(githubTokenDtl);
				} else {
					codeMapper.insertCodeDetail(codeDtl);
				}
			}
		} else {
			// 1. 코드 상세 전체 삭제
			T2Code codeVo = new T2Code();
			codeVo.setCdNo(cdNo);

			codeMapper.deleteCodeDetailAll(codeVo);

			// 2. 전체 상세 코드 재등록(추가 / 변경 / 삭제)
			for (T2CodeDtl vo : dtlList) {
				codeMapper.insertCodeDetail(vo);
			}
		}
		
		// delete license restriction information
		if (delRestrictionList != null && !delRestrictionList.isEmpty()) {
			LicenseMaster licenseMaster = new LicenseMaster();
			licenseMaster.setArrRestriction(delRestrictionList.toArray(new String[delRestrictionList.size()]));
			licenseService.deleteLicenseMasterForRestriction(licenseMaster);
		}
	}

	private boolean isGithubTokenCodeDtlNo(String cdDtlNo) {
		return cdDtlNo.equals(CoConstDef.CD_DTL_GITHUB_TOKEN);
	}

	private boolean isExternalServiceCodeNo(String cdNo) {
		return cdNo.equals(CoConstDef.CD_EXTERNAL_SERVICE_SETTING);
	}

	private boolean hasGithubTokenCodeDtl(List<T2CodeDtl> dtlList) {
		return dtlList.stream()
				.anyMatch(codeDtl -> codeDtl.getCdDtlNo().equals(CoConstDef.CD_DTL_GITHUB_TOKEN));
	}

	@Override
	@Cacheable(value="autocompleteCache", key="#root.methodName")
	public List<T2Code> getcodeList(T2Code t2Code) {
		List<T2Code> result = codeMapper.getCodeList(t2Code);
		
		return result;
	}

	@Override
	@Cacheable(value="autocompleteCache", key="#root.methodName")
	public List<T2Code> getcodeNmList(T2Code t2Code) {
		List<T2Code> result = codeMapper.getCodeNmList(t2Code);
		
		return result;
	}

}