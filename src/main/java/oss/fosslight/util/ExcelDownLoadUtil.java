/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.cyclonedx.generators.BomGeneratorFactory;
import org.cyclonedx.CycloneDxSchema;
import org.cyclonedx.exception.GeneratorException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.Dependency;
import org.cyclonedx.model.ExternalReference;
import org.cyclonedx.model.License;
import org.cyclonedx.model.LicenseChoice;
import org.cyclonedx.model.Metadata;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.OrganizationalEntity;
import org.cyclonedx.model.metadata.ToolInformation;
import org.cyclonedx.model.vulnerability.Vulnerability.Source;
import org.spdx.library.SpdxVerificationHelper;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.api.dto.ExcelData;
import oss.fosslight.api.dto.LicenseDto;
import oss.fosslight.api.dto.OssDto;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.controller.ProjectController;
import oss.fosslight.domain.BinaryAnalysisResult;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssAnalysis;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.OssLicense;
import oss.fosslight.domain.OssMaster;
import oss.fosslight.domain.OssNotice;
import oss.fosslight.domain.PartnerMaster;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.Statistics;
import oss.fosslight.domain.T2Users;
import oss.fosslight.domain.Vulnerability;
import oss.fosslight.repository.OssMapper;
import oss.fosslight.repository.SelfCheckMapper;
import oss.fosslight.service.BinaryDataHistoryService;
import oss.fosslight.service.CacheService;
import oss.fosslight.service.FileService;
import oss.fosslight.service.LicenseService;
import oss.fosslight.service.OssService;
import oss.fosslight.service.PartnerService;
import oss.fosslight.service.ProjectService;
import oss.fosslight.service.SelfCheckService;
import oss.fosslight.service.StatisticsService;
import oss.fosslight.service.T2UserService;
import oss.fosslight.service.VerificationService;
import oss.fosslight.service.VulnerabilityService;
import oss.fosslight.validation.T2CoValidationResult;
import oss.fosslight.validation.custom.T2CoProjectValidator;

@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_PROPERTIES_PATH)})
@Slf4j
public class ExcelDownLoadUtil extends CoTopComponent {
	private static String 						downloadpath 	= "";
	private static String						writepath		= CommonFunction.emptyCheckProperty("export.template.path", "/template");
	// Service
	private static PartnerService 				partnerService 	= getWebappContext().getBean(PartnerService.class);
	private static LicenseService 				licenseService 	= getWebappContext().getBean(LicenseService.class);
	private static ProjectService 				projectService 	= getWebappContext().getBean(ProjectService.class);
	private static T2UserService 				userService 	= getWebappContext().getBean(T2UserService.class);
	private static OssService 					ossService 		= getWebappContext().getBean(OssService.class);
	private static FileService					fileService		= getWebappContext().getBean(FileService.class);
	private static SelfCheckService 			selfCheckService 	= getWebappContext().getBean(SelfCheckService.class);
	private static VerificationService 			verificationService 	= getWebappContext().getBean(VerificationService.class);
	private static BinaryDataHistoryService 	binaryDataHistoryService 	= getWebappContext().getBean(BinaryDataHistoryService.class);
	private static VulnerabilityService 		vulnerabilityService 	= getWebappContext().getBean(VulnerabilityService.class);
//	private static ComplianceService 			complianceService = getWebappContext().getBean(ComplianceService.class);
	private static StatisticsService 			statisticsService = getWebappContext().getBean(StatisticsService.class);
	private static ProjectService 				prjService	= getWebappContext().getBean(ProjectService.class);
	private static CacheService					cacheService	= getWebappContext().getBean(CacheService.class);
	
	// Mapper
	private static OssMapper					ossMapper		= getWebappContext().getBean(OssMapper.class);
	private static SelfCheckMapper				selfCheckMapper = getWebappContext().getBean(SelfCheckMapper.class);
	
	// Controller
	private static ProjectController projectController	= getWebappContext().getBean(ProjectController.class);
	
	private static final int MAX_RECORD_CNT = 99999;
	private static final int MAX_RECORD_CNT_LIST = Integer.parseInt(CoCodeManager.getCodeExpString(CoConstDef.CD_EXCEL_DOWNLOAD, CoConstDef.CD_MAX_ROW_COUNT))+1;	

	private static String getReportExcelPost (String prjId, String type) throws IOException, InvalidFormatException {
		Workbook wb = null;
		Sheet sheet1 = null;
		FileInputStream inFile=null;

		// download file name
		String downloadFileName = "fosslight_report"; // Default

		try {
			//cover
			// Android Model의 Report에만 vulnerability score를 추가 표시하기 위해 type이 null인 경우 Android model 여부를 사전에 확인할 필요가 있음
			Project projectInfo = new Project();
			projectInfo.setPrjId(prjId);
			projectInfo = projectService.getProjectDetail(projectInfo);
			
			if (isEmpty(type) && CoConstDef.FLAG_YES.equals(projectInfo.getAndroidFlag())) {
				type = CoConstDef.CD_DTL_COMPONENT_ID_ANDROID;
			}
			
			inFile= new FileInputStream(new File(downloadpath+"/ProjectReport.xlsx"));
			
			wb = WorkbookFactory.create(inFile);
			
			{
				if (!isEmpty(projectInfo.getNoticeTypeEtc())) {
					wb.setSheetName(6, "BIN (" + CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, projectInfo.getNoticeTypeEtc()) + ")");
				}
				
				sheet1 = wb.getSheetAt(0);
				
				reportSheet(wb, sheet1, projectInfo);
				
			}
			//fosslight_report_[date]_prj-[ID].xlsx
			downloadFileName += "_" + CommonFunction.getCurrentDateTime() + "_prj-" + StringUtil.deleteWhitespaceWithSpecialChar(prjId);

			ProjectIdentification ossListParam = new ProjectIdentification();
			ossListParam.setReferenceId(prjId);
			
			//3rdparty
			if (isEmpty(type) || CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(type)) {
				ossListParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
				
				reportIdentificationSheet(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER, wb.getSheetAt(2), projectService.getIdentificationGridList(ossListParam), projectInfo);
			}
			
			//dep
			if (isEmpty(type) || CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(type)) {
				ossListParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_DEP);
				
				reportIdentificationSheet(CoConstDef.CD_DTL_COMPONENT_ID_DEP, wb.getSheetAt(3), projectService.getIdentificationGridList(ossListParam), projectInfo);
			}
			
			//src
			if (isEmpty(type) || CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(type)) {
				ossListParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_SRC);
				
				reportIdentificationSheet(CoConstDef.CD_DTL_COMPONENT_ID_SRC, wb.getSheetAt(4), projectService.getIdentificationGridList(ossListParam), projectInfo);
			}
			
			//BIN
			if (isEmpty(type) || CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(type)) {
				ossListParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BIN);
				
				reportIdentificationSheet(CoConstDef.CD_DTL_COMPONENT_ID_BIN, wb.getSheetAt(5), projectService.getIdentificationGridList(ossListParam), projectInfo);
			}
			
			//BIN(ANDROID)
			if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(type) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(type)) {
				ossListParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
				
				reportIdentificationSheet(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID, wb.getSheetAt(6), projectService.getIdentificationGridList(ossListParam), projectInfo);
			}
			
			//bom
			if (isEmpty(type) || CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(type)) {
				ossListParam.setReferenceDiv(isEmpty(type) ? CoConstDef.CD_DTL_COMPONENT_ID_BOM : type);
				ossListParam.setMerge(CoConstDef.FLAG_NO);
				
				Map<String, Object> map = projectService.getIdentificationGridList(ossListParam);
				if (!CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(type)) {
					map.replace("rows", projectService.setMergeGridData((List<ProjectIdentification>) map.get("rows")));
				}
				
				reportIdentificationSheet(CoConstDef.CD_DTL_COMPONENT_ID_BOM, wb.getSheetAt(7), map, projectInfo);
			}
			
			// model
			// OSS Report를 출력을 시작한 대상이 BOM Tab, platform generated인 경우에 대해서는 Model Info를 출력하도록 수정.
			if (isEmpty(type) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(type)) {
				Map<String, List<Project>> modelMap = projectService.getModelList(prjId);
				
				if (modelMap != null) {
					List<Project> modelList = modelMap.get("rows");
					if (modelList != null && !modelList.isEmpty()) {
						reportProjectModelSheet(sheet1, wb.getSheetAt(1), modelList, projectInfo);
					}
				}
			}
			
			wb.setSheetVisibility(8, SheetVisibility.VERY_HIDDEN);
			
			if (!isEmpty(type)) {
				removeReportSheet(type, wb, wb.getNumberOfSheets());
			}
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e) {}
			}
		}

		return makeExcelFileId(wb,downloadFileName);
	}
	private static void removeReportSheet(String type, Workbook wb, int sheetCnt) {
		boolean isRemoveSheet = false;
		if (CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(type)) {
			for (int i = 0; i < sheetCnt; i++) {
				String sheetName = wb.getSheetName(i);
				if (!sheetName.equalsIgnoreCase("3rd Party Info")) {
					if (!sheetName.equalsIgnoreCase("3rd Party")) {
						wb.removeSheetAt(i);
						isRemoveSheet = true;
					}
					if (isRemoveSheet) {
						break;
					}
				}
			}
		} else {
			for (int i = 0; i < sheetCnt; i++) {
				String sheetName = wb.getSheetName(i);
				if (!sheetName.equalsIgnoreCase("Project Info") && !sheetName.equalsIgnoreCase("Model Info") && !sheetName.equalsIgnoreCase(".Data")) {
					switch (type) {
						case CoConstDef.CD_DTL_COMPONENT_ID_PARTNER :
							if (!sheetName.equalsIgnoreCase("3rd Party")) {
								wb.removeSheetAt(i);
								isRemoveSheet = true;
							}
							break;
						case CoConstDef.CD_DTL_COMPONENT_ID_DEP :
							if (!sheetName.equalsIgnoreCase("DEP")) {
								wb.removeSheetAt(i);
								isRemoveSheet = true;
							}
							break;
						case CoConstDef.CD_DTL_COMPONENT_ID_SRC :
							if (!sheetName.equalsIgnoreCase("SRC")) {
								wb.removeSheetAt(i);
								isRemoveSheet = true;
							}
							break;
						case CoConstDef.CD_DTL_COMPONENT_ID_BIN :
							if (!sheetName.equalsIgnoreCase("BIN")) {
								wb.removeSheetAt(i);
								isRemoveSheet = true;
							}
							break;
						case CoConstDef.CD_DTL_COMPONENT_ID_ANDROID :
							if (!sheetName.equalsIgnoreCase("BIN (Android)") && !sheetName.equalsIgnoreCase("BIN (Yocto)")) {
								wb.removeSheetAt(i);
								isRemoveSheet = true;
							}
							break;
						case CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM :
							if (!sheetName.equalsIgnoreCase("BIN (Android)") && !sheetName.equalsIgnoreCase("BIN (Yocto)") && !sheetName.equalsIgnoreCase("BOM")) {
								wb.removeSheetAt(i);
								isRemoveSheet = true;
							}
							break;
					}
					if (isRemoveSheet) {
						break;
					}
				}
			}
		}
		if (isRemoveSheet) {
			removeReportSheet(type, wb, wb.getNumberOfSheets());
		}
	}
	private static void reportProjectModelSheet(Sheet coverSheet, Sheet modelSheet, List<Project> modelList, Project projectInfo) {
		List<String[]> rows = new ArrayList<>();
		int modelCnt = 1;
		
		for (Project bean : modelList) {
			List<String> params = new ArrayList<>();
			params.add(String.valueOf(modelCnt++));
			params.add(bean.getModelName()); // model name
			params.add(CommonFunction.makeCategoryFormat(projectInfo.getDistributeTarget(), bean.getCategory())); // category name
			params.add(CommonFunction.formatDateSimple(bean.getReleaseDate()));
			rows.add(params.toArray(new String[params.size()]));
		}
		
		makeSheet(modelSheet, rows, 2);
	}

	private static void reportIdentificationSheet(String type, Sheet sheet, Map<String, Object> listMap, Project projectInfo) {
		reportIdentificationSheet(type, sheet, listMap, projectInfo, false);
	}

	/**
	 * 분석결과서 Export 처리
	 * 화면(초길표시)와 동일하게 정렬하기 위해서 validation을 수행한다.
	 * @param type
	 * @param sheet
	 * @param listMap
	 * @param projectInfo
	 * @param isSelfCheck
	 */
	@SuppressWarnings("unchecked")
	private static void reportIdentificationSheet(String type, Sheet sheet, Map<String, Object> listMap, Project projectInfo, boolean isSelfCheck) {
		List<ProjectIdentification> list = null;
		if (listMap != null && (listMap.containsKey("mainData") || listMap.containsKey("rows") )) {
			list = (List<ProjectIdentification>) listMap.get(listMap.containsKey("mainData") ? "mainData" : "rows");
			List<String[]> rows = new ArrayList<>();
			Map<String, List<String>> rowsMap = new HashMap<>();
			//Excell export sort
			T2CoProjectValidator pv = new T2CoProjectValidator();

			if (!CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type) && !CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(type) && !CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM.equals(type)) {
				pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
				pv.setAppendix("mainList", list);
				
				if (CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(type) || CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(type)) {
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_SOURCE);
					
					if (CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(type)) {
						pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BIN);
					}

					pv.setAppendix("projectId", avoidNull(projectInfo.getPrjId()));
					// sub grid
					pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) listMap.get("subData"));
				} else if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(type)) {
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_ANDROID);
					pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) listMap.get("subData"));

					Project prjInfo = projectService.getProjectBasicInfo(projectInfo.getPrjId());
					List<String> noticeBinaryList = null;
					List<String> existsBinaryName = null;
					
					if (prjInfo != null) {
						if (!isEmpty(prjInfo.getSrcAndroidNoticeFileId())) {
							noticeBinaryList = CommonFunction.getNoticeBinaryList(
									fileService.selectFileInfoById(prjInfo.getSrcAndroidNoticeFileId()));
						}

						if (!isEmpty(prjInfo.getSrcAndroidResultFileId())) {
							existsBinaryName = CommonFunction.getExistsBinaryNames(
									fileService.selectFileInfoById(prjInfo.getSrcAndroidResultFileId()));
						}
					}

					if (noticeBinaryList != null) {
						pv.setAppendix("noticeBinaryList", noticeBinaryList);
					}
					
					if (existsBinaryName != null) {
						pv.setAppendix("existsResultBinaryName", existsBinaryName);
					}
				} else if ((CoConstDef.CD_DTL_COMPONENT_ID_BAT.equals(type) || CoConstDef.CD_DTL_COMPONENT_BAT.equals(type))) {
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BAT);
					pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) listMap.get("subData"));
				} else if (CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(type)) {
					pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_PARTNER);
					pv.setAppendix("subListMap", (Map<String, List<ProjectIdentification>>) listMap.get("subData"));
				}
			} else {
				pv.setProcType(pv.PROC_TYPE_IDENTIFICATION_BOM_MERGE);
				pv.setAppendix("bomList", list);
			}
			
			T2CoValidationResult vr = pv.validate(new HashMap<>());
			list = (List<ProjectIdentification>) CommonFunction.identificationSortByValidInfo(list, vr.getValidMessageMap(), vr.getDiffMessageMap(), vr.getInfoMessageMap(), false);
			
			// exclude 된 라이선스는 제외한다. (bom은 제외되어 있음)
			for (ProjectIdentification bean : list) {
				if (bean.getComponentLicenseList() != null && !bean.getComponentLicenseList().isEmpty()) {
					List<ProjectIdentification> newLicenseList = new ArrayList<>();
					
					for (ProjectIdentification licenseBean : bean.getComponentLicenseList()) {
						if (!CoConstDef.FLAG_YES.equals(licenseBean.getExcludeYn())) {
							newLicenseList.add(licenseBean);
						}
					}
					
					bean.setComponentLicenseList(newLicenseList);
				}
			}
			
			// self check인 경우 oss_id가 있는 경우, 부가 정보를 추가해서 export한다.
			if (isSelfCheck) {
				Map<String, OssMaster> regOssInfoMapWithOssId = null;
				OssMaster _ossParam = new OssMaster();
				
				for (ProjectIdentification bean : list) {
					if (!isEmpty(bean.getOssId())) {
						_ossParam.addOssIdList(bean.getOssId());
					}
				}
				
				if (_ossParam.getOssIdList() != null && !_ossParam.getOssIdList().isEmpty()) {
					regOssInfoMapWithOssId = ossService.getBasicOssInfoListById(_ossParam);
				}
			}
			
			if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type)) {
				list.sort(Comparator.comparing(ProjectIdentification::getGroupingColumn));
			} 
			
			String currentGroupKey = null;
			int idx = 1;
			for (ProjectIdentification bean : list) {
				if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(type) || CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM.equals(type)) {
					if (currentGroupKey != null && currentGroupKey.equals(bean.getGroupingColumn())) {
						for (String[] editRow : rows) {
							if (!isEmpty(bean.getOssName()) && !bean.getOssName().equals("-")
									&& editRow[1].equals(bean.getOssName()) && editRow[2].equals(bean.getOssVersion())) {
								String referenceDiv = editRow[8];
								String referenceDivChk = bean.getRefDiv();
								
								if (!isEmpty(referenceDivChk)) {
									switch (avoidNull(referenceDivChk)) {
									case CoConstDef.CD_DTL_COMPONENT_ID_PARTNER:
										referenceDivChk = "3rd";
										break;
									case CoConstDef.CD_DTL_COMPONENT_ID_DEP:
										referenceDivChk = "DEP";
										break;
									case CoConstDef.CD_DTL_COMPONENT_ID_SRC:
										referenceDivChk = "SRC";
										break;
									case CoConstDef.CD_DTL_COMPONENT_ID_BIN:
										referenceDivChk = "BIN";
										
										break;
									case CoConstDef.CD_DTL_COMPONENT_ID_ANDROID:
										if (avoidNull(projectInfo.getNoticeTypeEtc()).equals("1")) {
											referenceDivChk = "BIN(ANDROID)";
										} else if (avoidNull(projectInfo.getNoticeTypeEtc()).equals("2")) {
											referenceDivChk = "BIN(YOCTO)";
										}
									case CoConstDef.CD_DTL_COMPONENT_PARTNER:
										referenceDivChk = "3rd Party";
										break;
									default:
										break;
									}
									
									if (!referenceDiv.contains(referenceDivChk)) {
										referenceDiv = referenceDiv + "," + referenceDivChk;
										editRow[8] = referenceDiv;
										break;
									}
								}
							}
						}
						
						continue;
					} else {
						currentGroupKey = bean.getGroupingColumn();
					}
				}
				
				// bom의 경우
				if (bean.getOssComponentsLicenseList() != null && !bean.getOssComponentsLicenseList().isEmpty()) {
					boolean isMainRow = true;
					
					List<String> params = new ArrayList<>();
					// main 정보
					params.add(isMainRow ? ( CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(type) || CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM.equals(type) ? bean.getRefComponentIdx() : bean.getComponentIdx() ) : "");
					params.add(bean.getOssName()); // OSS Name
					params.add(bean.getOssVersion()); // OSS Version
					params.add(bean.getLicenseName()); // LICENSE
					params.add(isMainRow ? bean.getDownloadLocation() : ""); // download url
					params.add(isMainRow ? bean.getHomepage() : ""); // home page url
					params.add(isMainRow ? bean.getCopyrightText() : "");
					
					String licenseTextUrl = "";
					
					for (String licenseName : bean.getLicenseName().split(",")) {
						String licenseUrl = CommonFunction.getLicenseUrlByName(licenseName.trim());
						
						if (isEmpty(licenseUrl)) {
							boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
							
							licenseUrl = CommonFunction.makeLicenseInternalUrl(CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(licenseName).toUpperCase()), distributionFlag);
						}
						
						if (!isEmpty(licenseUrl)) {
							if (!isEmpty(licenseTextUrl)) {
								licenseTextUrl += ", ";
							}
							
							licenseTextUrl += licenseUrl;
						}
					}
					
					params.add(licenseTextUrl); //license text => license homepage
					
					String refSrcTab = "";
					String thirdKey = "3rd";
					
					String[] refDivs = bean.getRefDiv().split("[,]");
					for (String refDiv : refDivs) {
						switch (avoidNull(refDiv)) {
							case CoConstDef.CD_DTL_COMPONENT_ID_PARTNER:
								if (!isEmpty(refSrcTab)) refSrcTab += ",";
									refSrcTab += thirdKey;
								break;
							case CoConstDef.CD_DTL_COMPONENT_ID_DEP:
								if (!isEmpty(refSrcTab)) refSrcTab += ",";
								refSrcTab += "DEP";
								break;
							case CoConstDef.CD_DTL_COMPONENT_ID_SRC:
								if (!isEmpty(refSrcTab)) refSrcTab += ",";
								refSrcTab += "SRC";
								break;
							case CoConstDef.CD_DTL_COMPONENT_ID_BIN:
								if (!isEmpty(refSrcTab)) refSrcTab += ",";
								refSrcTab += "BIN";
								break;
							case CoConstDef.CD_DTL_COMPONENT_ID_ANDROID:
								if (!isEmpty(refSrcTab)) refSrcTab += ",";
								if (avoidNull(projectInfo.getNoticeTypeEtc()).equals("1")) {
									refSrcTab += "BIN(ANDROID)";
								} else if (avoidNull(projectInfo.getNoticeTypeEtc()).equals("2")) {
									refSrcTab += "BIN(YOCTO)";
								}
								break;
							case CoConstDef.CD_DTL_COMPONENT_PARTNER:
								if (!isEmpty(refSrcTab)) refSrcTab += ",";
								refSrcTab += "3rd Party";
								break;
							default:
								break;
						}
					}
					if (refSrcTab.contains(thirdKey)) {
						String[] thirdIds = avoidNull(bean.getRefPartnerId(), "").split(",");
						List<String> thirdNames = new ArrayList<String>();
						for (String thirdId : thirdIds) {
							String thirdPartyName = projectService.getPartnerFormatName(thirdId, true);
							if (!isEmpty(thirdPartyName) && !thirdNames.contains(thirdKey + "-" + thirdPartyName)) {
								thirdNames.add(thirdKey + "-" + thirdPartyName);
							}
						}
						if (thirdNames.size() > 0) {
							String thirdName = String.join(",", thirdNames);
							refSrcTab = refSrcTab.replace(thirdKey, thirdName);
						}
					}
					// from
					params.add(isMainRow ? refSrcTab : "");
					// main 정보 (license 정보 후처리)
//					params.add(isMainRow ? bean.getFilePath() : ""); // path
					// vulnerability
					params.add(isMainRow ? (new BigDecimal(avoidNull(bean.getCvssScore(), "0.0")).equals(new BigDecimal("0.0")) ? "" : bean.getCvssScore()) : "");
					// dependencies
//					params.add(isMainRow ? (isEmpty(bean.getDependencies()) ? "" : bean.getDependencies()) : "");
					// notice
					params.add(isMainRow ? ( (CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType()) || CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())) ? "O" : "")  : "");
					// source code
					params.add(isMainRow ? ( (CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())) ? "O" : "") : "");
					// Restriction
					params.add(isMainRow ? (isEmpty(bean.getRestriction()) ? "" : bean.getRestriction()) : "");
					
					addColumnWarningMessage(type, bean, vr, params);
					
					rows.add(params.toArray(new String[params.size()]));
				} else {
					// exclude 제외
					if ((CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(type) && CoConstDef.FLAG_YES.equals(bean.getExcludeYn()))) {
						continue;
					}
					
					List<String> params = new ArrayList<>();
					
					// main 정보
					//params.add(isSelfCheck ? bean.getComponentIdx() : bean.getComponentId()); //ID
					params.add(isSelfCheck ? bean.getComponentIdx() : bean.getComponentIdx());

					// TODO 3rd party 이름 가져올 수 있나?
					if (CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(type)) {

						params.add(projectService.getPartnerFormatName(bean.getRefPartnerId(), false)); //3rd Party
					}

					if (CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(type) || CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(type) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(type)) {
						params.add(bean.getBinaryName()); // Binary Name
					}
					
					if (!CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(type) && !CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(type)) {
						if (CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(type)) {
							params.add(bean.getPackageUrl());
						} else {
							params.add(bean.getFilePath()); // path
						}
					}
					
					if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(type)) {
						params.add(bean.getBinaryNotice()); // notice
					}
					
					params.add(bean.getOssName()); // OSS Name
					params.add(bean.getOssVersion()); // OSS Version
					String licenseNameList = "";
					
					if (bean.getComponentLicenseList() != null) {
						for ( ProjectIdentification project : bean.getComponentLicenseList()) {
							if (!isEmpty(licenseNameList)) {
								licenseNameList += ",";
							}
							
							LicenseMaster lm = CoCodeManager.LICENSE_INFO_UPPER.get(project.getLicenseName().toUpperCase());
							
							if (lm != null) {
								licenseNameList += (!isEmpty(lm.getShortIdentifier()) ? lm.getShortIdentifier() : lm.getLicenseName());
							}else {
								licenseNameList += project.getLicenseName();
							}
						}
					}
					
					params.add(licenseNameList); // license
					params.add(bean.getDownloadLocation()); // download url
					params.add(bean.getHomepage()); // home page url
					params.add(bean.getCopyrightText());
					
					if (!(CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(type)
							|| CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(type)
							|| CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(type)
							|| CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(type))
						|| isSelfCheck) {
						String licenseTextUrl = "";
						
						for (String licenseName : licenseNameList.split(",")) {
							String licenseUrl = CommonFunction.getLicenseUrlByName(licenseName.trim());						
							
							if (isEmpty(licenseUrl)) {
								boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
								
								licenseUrl = CommonFunction.makeLicenseInternalUrl(CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(licenseName).toUpperCase()), distributionFlag);
							}
							
							if (!isEmpty(licenseUrl)) {
								if (!isEmpty(licenseTextUrl)) {
									licenseTextUrl += ", ";
								}
								
								licenseTextUrl += licenseUrl;
							}
						}
						
						params.add(licenseTextUrl);
					}
					
					if (CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(type)) {
//						params.add(""); // check list > Modified or not
						params.add((new BigDecimal(avoidNull(bean.getCvssScore(), "0.0")).equals(new BigDecimal("0.0")) ? "" : bean.getCvssScore())); // Vuln
					}
					
					if (!CoConstDef.CD_DTL_COMPONENT_ID_PARTNER.equals(type) && !isSelfCheck) { // selfcheck에서는 출력하지 않음.
						if ( CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
							params.add("Exclude");
						} else {
							params.add("");
						}
					}
					
					// Comment
					String _comm = "";
					
					if (!isSelfCheck 
						&& (CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(type) 
							|| CoConstDef.CD_DTL_COMPONENT_ID_SRC.equals(type) 
							|| CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(type) 
							|| CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(type))) {
						_comm = avoidNull(bean.getComments().trim());
						
						params.add(_comm); 
					}
					
					
					// Vulnerability
					if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(type)) {
						params.add(new BigDecimal(avoidNull(bean.getCvssScore(), "0.0")).equals(new BigDecimal("0.0")) ? "" : bean.getCvssScore()); // Vuln
						params.add(isEmpty(bean.getRestriction()) ? "" : bean.getRestriction());
					}
					
					if (isSelfCheck){
						params.add((new BigDecimal(avoidNull(bean.getCvssScore(), "0.0")).equals(new BigDecimal("0.0")) ? "" : bean.getCvssScore())); // Vuln
						
						boolean errRowFlag = false;
						
						for (String errCd : vr.getErrorCodeMap().keySet()) {
							if (errCd.contains(bean.getComponentId())) {
								errRowFlag = true;
								
								break;
							} 
						}
						
						if (errRowFlag) {
							// notice
							params.add("");
							// source code
							params.add("");
						}else {
							// notice
							params.add(( (CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType()) || CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())) ? "O" : ""));
							// source code
							params.add(( (CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())) ? "O" : ""));
						}
						
						// Restriction
						params.add((isEmpty(bean.getRestriction()) ? "" : bean.getRestriction()));
					}
					
					if (CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(type)){
						params.add(isEmpty(bean.getComments()) ? "" : bean.getComments());
						// notice
						params.add(( (CoConstDef.CD_DTL_OBLIGATION_NOTICE.equals(bean.getObligationType()) || CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())) ? "O" : ""));
						// source code
						params.add(( (CoConstDef.CD_DTL_OBLIGATION_DISCLOSURE.equals(bean.getObligationType())) ? "O" : ""));
						// Restriction
						params.add((isEmpty(bean.getRestriction()) ? "" : bean.getRestriction()));
					}
					
					if (isSelfCheck){
						if (bean.getExcludeYn().equals(CoConstDef.FLAG_YES)) {
							params.add("Exclude");
						} else {
							params.add("");
						}
					}
					
					if (CoConstDef.CD_DTL_COMPONENT_ID_DEP.equals(type)){
						if (!isEmpty(bean.getDependencies())) {
							params.add(bean.getDependencies());
						} else {
							params.add("");
						}
					}
					
					addColumnWarningMessage(type, bean, vr, params);
					rowsMap.put(String.valueOf(idx), params);
//					rows.add(params.toArray(new String[params.size()]));
				}
				idx++;
			}

			if (!MapUtils.isEmpty(rowsMap)) {
				for (String key : rowsMap.keySet()) {
					List<String> params = rowsMap.get(key);
					rows.add(params.toArray(new String[params.size()]));
				}
				rowsMap.clear();
			}

			//시트 만들기
			if (!rows.isEmpty()) {
				int startIdx = isSelfCheck ? 1 : 2;
				
				try {
					Font font = WorkbookFactory.create(true).createFont();
					
					if (isSelfCheck) {
						makeSheetAddWarningMsg(type, sheet, rows, startIdx, false, font);
					} else {
						makeSheetAddWarningMsg(type, sheet, rows, startIdx, true, font);
					}
					
					if (CoConstDef.CD_DTL_COMPONENT_ID_BIN.equals(type)) {
						sheet.setColumnHidden(11, true);
						sheet.setColumnHidden(12, true);
					} else if (CoConstDef.CD_DTL_COMPONENT_ID_ANDROID.equals(type)) {
						sheet.setColumnHidden(16, true);
						sheet.setColumnHidden(17, true);
					}
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	
	private static void addColumnWarningMessage(String type, ProjectIdentification bean, T2CoValidationResult vr, List<String> params) {
		String message = "";
		
		if (!vr.getValidMessageMap().isEmpty()) {
			String gridId = "";
			if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(type) || CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM.equals(type)) {
				gridId = bean.getComponentId();
			} else {
				gridId = bean.getGridId();
			}
			
			for (String key : vr.getValidMessageMap().keySet()) {
				if (key.contains(".") && gridId.equals(key.split("[.]")[1])) {
					if (!isEmpty(message)) {
						message += "/";
					}
					
					message += warningMsgCode((key.split("[.]")[0]).toUpperCase()) + vr.getValidMessageMap().get(key) + "(FONT_RED)";
				}
			}
		}
		
		if (!vr.getDiffMessageMap().isEmpty()) {
			String gridId = "";
			if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(type) || CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM.equals(type) || CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM.equals(type)) {
				gridId = bean.getComponentId();
			} else {
				gridId = bean.getGridId();
			}
			for (String key : vr.getDiffMessageMap().keySet()) {
				if (key.contains(".") && gridId.equals(key.split("[.]")[1])) {
					if (!isEmpty(message)) {
						message += "/";
					}
					
					message += warningMsgCode((key.split("[.]")[0]).toUpperCase()) + vr.getDiffMessageMap(true).get(key) + "(FONT_BLUE)";
				}
			}
		}
		
		params.add(isEmpty(message) ? "" : message);
	}
	
	private static String warningMsgCode(String key) {
		String warningMsgCode = "";
		switch(key) {
			case "OSSNAME" :
			case "OSS NAME" :
			case "OSS NAME ( OPEN SOURCE SOFTWARE NAME )":
			case "OSS COMPONENT":
			case "PACKAGE NAME":
				warningMsgCode = "(ON) ";
				break;
			case "OSSVERSION" :
			case "PACKAGE VERSION":
				warningMsgCode = "(OV) ";
				break;
			case "LICENSE" :
			case "LICENSENAME" :
			case "LICENSE NAME" :
				warningMsgCode = "(L) ";
				break;
			case "DOWNLOADLOCATION" :
			case "PACKAGE DOWNLOAD LOCATION":
				warningMsgCode = "(D) ";
				break;
			case "HOMEPAGE" :
			case "OSS WEBSITE":
			case "HOME PAGE":
				warningMsgCode = "(H) ";
				break;
			case "SOURCENAMEORPATH" :
			case "SOURCECODEPATH" :
			case "SOURCE NAME OR PATH" :
			case "SOURCE CODE PATH" :
				warningMsgCode = "(S) ";
				break;
			case "BINARYNAME" :
			case "BINARYNAMEORSOURCEPATH" :
			case "BINARY NAME" :
			case "BINARY NAME OR SOURCE PATH" :
				warningMsgCode = "(B) ";
				break;
			case "BINARYNOTICE":
				warningMsgCode = "(N) ";
				break;
			default :
				break;
		}
		
		return warningMsgCode;
	}
	/**
	 * 
	 * @param sheet
	 * @param rows
	 * @용도 시트 만들기
	 */
	private static void makeSheet(Sheet sheet, List<String[]> rows) {
		int startRow= 1;
		int startCol = 0;
		int endCol = 0;
		int templateRowNum = 1;
		
		if (rows.isEmpty()){
			
		}else{
			endCol = rows.get(0).length-1;
		}
		
		int shiftRowNum = rows.size();
		
		Row templateRow = sheet.getRow(templateRowNum);
		Cell templateCell = templateRow.getCell(0);
		CellStyle style = templateCell.getCellStyle();
		
		startRow = templateRow.getRowNum();		
		
		for (int i = startRow; i < startRow+shiftRowNum; i++){
			String[] rowParam = rows.get(i-startRow);
			
			Row row = sheet.createRow(i);
			for (int colNum=startCol; colNum<=endCol; colNum++){
				
				Cell cell=row.createCell(colNum);
				cell.setCellStyle(style);
				
				if (!isEmpty(rowParam[colNum])) {
					cell.setCellValue(rowParam[colNum]);
				} else {
					cell.setBlank();
				}
			}
		}
	}
	
	private static void makeSheet(Sheet sheet, List<String[]> rows, int templateRowNum) {
		makeSheet(sheet, rows, templateRowNum, false);
	}
	
	private static void makeSheet(Sheet sheet, List<String[]> rows, int templateRowNum, boolean useLastCellComment) {
		int startRow= 1;
		int startCol = 0;
		int endCol = 0;
		if (rows.isEmpty()){
		}else{
			endCol = rows.get(0).length-1;
		}
		int shiftRowNum = rows.size();
		
		Row templateRow = sheet.getRow(templateRowNum);
		
		startRow = templateRow.getRowNum();

		Row styleTemplateRow = sheet.getRow(startRow);
		Cell templateCell = styleTemplateRow.getCell(0);
		CellStyle style = templateCell.getCellStyle();
		
		for (int i = startRow; i < startRow + shiftRowNum; i++) {
			String[] rowParam = rows.get(i - startRow);

			Row row = sheet.getRow(i);
			if (row == null) {
				row = sheet.createRow(i);
			}

			for (int colNum = startCol; colNum <= endCol; colNum++) {
				Cell cell = row.getCell(colNum);
				
				if (cell == null) {
					cell = row.createCell(colNum);
				}
				
				// comment의 경우 줄바꿈 처리
				if (useLastCellComment && colNum == endCol) {
					CellStyle cs = style;
					cs.setWrapText(true);
					cell.setCellStyle(cs);
				} else {
					cell.setCellStyle(style);
				}
				
				// 수식 삭제
				if (CellType.FORMULA == cell.getCellType()) {
					cell.setCellType(CellType.BLANK);
				}
				
				String cellValue = avoidNull(rowParam[colNum]);
				cell.setCellType(CellType.STRING);
				if (!isEmpty(cellValue)) {
					cell.setCellValue(cellValue);
				} else {
					cell.setBlank();
				}
			}
		}
	}
	
	private static void makeSheetAddWarningMsg(String type, Sheet sheet, List<String[]> rows, int templateRowNum, boolean useLastCellComment, Font font) {
		int startRow= 1;
		int startCol = 0;
		int endCol = 0;
		if (rows.isEmpty()){
		}else{
			endCol = rows.get(0).length-1;
		}
		int shiftRowNum = rows.size();
		
		Row templateRow = sheet.getRow(templateRowNum);
		
		startRow = templateRow.getRowNum();

		Row styleTemplateRow = sheet.getRow(startRow);
		Cell templateCell = styleTemplateRow.getCell(0);
		CellStyle style = templateCell.getCellStyle();
		
		for (int i = startRow; i < startRow + shiftRowNum; i++) {
			String[] rowParam = rows.get(i - startRow);

			Row row = sheet.getRow(i);
			if (row == null) {
				row = sheet.createRow(i);
			}

			for (int colNum = startCol; colNum <= endCol; colNum++) {
				Cell cell = row.getCell(colNum);
				
				if (cell == null) {
					cell = row.createCell(colNum);
				}
				
				// comment의 경우 줄바꿈 처리
				if ((useLastCellComment && colNum == endCol - 1) || colNum == endCol) {
					CellStyle cs = style;
					cs.setWrapText(true);
					cell.setCellStyle(cs);
				} else {
					cell.setCellStyle(style);
				}
				
				// 수식 삭제
				if (CellType.FORMULA == cell.getCellType()) {
					cell.setCellType(CellType.BLANK);
				}
				
				cell.setCellType(CellType.STRING);
				// warning message의 경우 색상 처리
				if (colNum == endCol) {
					String cellValue = avoidNull(rowParam[colNum]);
					String richTextStr = cellValue.replaceAll("[(]FONT_RED[)]", "").replaceAll("[(]FONT_BLUE[)]", "").replaceAll("[/]", System.lineSeparator());
					
					RichTextString messageStr = new XSSFRichTextString(richTextStr);
					{
						String[] messageArr = cellValue.split("/");
						int startIndex = 0;
						
						for (int j=0; j<messageArr.length; j++) {
							String message = "";
							if (messageArr[j].contains("(FONT_RED)")) {
								message = messageArr[j].split("[(]FONT_RED[)]")[0];
								font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
								messageStr.applyFont(startIndex, startIndex + message.length(), font);
							} else if (messageArr[j].contains("(FONT_BLUE)")){
								message = messageArr[j].split("[(]FONT_BLUE[)]")[0];
								font.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
								messageStr.applyFont(startIndex, startIndex + message.length(), font);
							}
							startIndex = startIndex + message.length() + System.lineSeparator().length();
						}
					}
					
					cell.setCellValue(messageStr);
				} else {
					String cellValue = avoidNull(rowParam[colNum]);
					if (!isEmpty(cellValue)) {
						cell.setCellValue(cellValue);
					} else {
						cell.setBlank();
					}
				}
			}
		}
	}
	
	private static void makeSheet2(Sheet sheet, List<String[]> rows) {
		int startRow= 2;
		int startCol = 0;
		int endCol = 0;
		int templateRowNum = 2;
		
		if (rows.isEmpty()) {
			
		} else {
			endCol = rows.get(0).length-1;
		}
		
		int shiftRowNum = rows.size();
		
		Row templateRow = sheet.getRow(templateRowNum);
		Cell templateCell = templateRow.getCell(0);
		CellStyle style = templateCell.getCellStyle();
		
		startRow = templateRow.getRowNum();
		
		for (int i = startRow; i < startRow+shiftRowNum; i++){
			String[] rowParam = rows.get(i-startRow);
			
			Row row = sheet.createRow(i);
			
			for (int colNum=startCol; colNum<=endCol; colNum++){
				Cell cell=row.createCell(colNum);
				cell.setCellStyle(style);
				cell.setCellType(CellType.STRING);
				if (!isEmpty(rowParam[colNum])) {
					cell.setCellValue(rowParam[colNum]);
				} else {
					cell.setBlank();
				}
			}
		}
	}
	
	private static void makeChartSheet(Sheet sheet, List<String[]> rows) {
		int startRow= 0;
		int startCol = 0;
		int endCol = 0;
		
		if (rows.isEmpty()) {
		} else {
			endCol = rows.get(0).length-1;
		}
		
		int shiftRowNum = rows.size();
		
		for (int i = startRow; i < startRow + shiftRowNum; i++) {
			String[] rowParam = rows.get(i - startRow);

			Row row = sheet.getRow(i);
			
			if (row == null) {
				row = sheet.createRow(i);
			}

			for (int colNum = startCol; colNum <= endCol; colNum++) {
				Cell cell = row.getCell(colNum);
				
				if (cell == null) {
					cell = row.createCell(colNum);
				}
				
				// 수식 삭제
				if (CellType.FORMULA == cell.getCellType()) {
					cell.setCellType(CellType.BLANK);
				}
				
				cell.setCellType(CellType.STRING);
				if (!isEmpty(avoidNull(rowParam[colNum]))) {
					cell.setCellValue(avoidNull(rowParam[colNum]));
				} else {
					cell.setBlank();
				}
			}
		}
	}
	
	/**
	 * 
	 * @param sheet
	 * @param rows
	 * @용도 시트 만들기
	 */
	private static void reportSheet(Workbook wb,Sheet sheet, Project project) {
		// About the report
		// Creator
		Cell authorCell = sheet.getRow(1).getCell(1);
		authorCell.setCellType(CellType.STRING);
		authorCell.setCellValue(avoidNull(project.getPrjUserName(), project.getCreator()));
		// Division of Creator
		Cell divisionrCell = sheet.getRow(2).getCell(1);
		divisionrCell.setCellType(CellType.STRING);
		divisionrCell.setCellValue(avoidNull(project.getPrjDivisionName()));
		// Report Creation Date
		Cell dateCell = sheet.getRow(3).getCell(1);
		dateCell.setCellType(CellType.STRING);
		dateCell.setCellValue(CommonFunction.formatDate(project.getCreatedDate()));
		
		// About the project
		// Project Name
		Cell nameCell = sheet.getRow(6).getCell(1);
		nameCell.setCellType(CellType.STRING);
		CellStyle hyperLinkStyle = wb.createCellStyle();
		Font hyperLinkFont = wb.createFont();
		hyperLinkFont.setUnderline(Font.U_SINGLE);
		hyperLinkFont.setColor(IndexedColors.BLUE.getIndex());
		hyperLinkStyle.setFont(hyperLinkFont);
		hyperLinkStyle.setBorderTop(BorderStyle.THIN);
		hyperLinkStyle.setBorderBottom(BorderStyle.THIN);
		hyperLinkStyle.setBorderRight(BorderStyle.THIN);
		Hyperlink hyperlink = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
		hyperlink.setAddress(CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/project/shareUrl/" + project.getPrjId());
		nameCell.setHyperlink(hyperlink);
		nameCell.setCellStyle(hyperLinkStyle);
		nameCell.setCellValue(project.getPrjName());
		// version
		Cell versionCell = sheet.getRow(7).getCell(1);
		versionCell.setCellType(CellType.STRING);
		versionCell.setCellValue(avoidNull(project.getPrjVersion()));
		// Software Type
		Cell softwareType = sheet.getRow(8).getCell(1);
		softwareType.setCellType(CellType.STRING);
		softwareType.setCellValue(CoConstDef.COMMON_SELECTED_ETC.equals(project.getOsType()) ? project.getOsTypeEtc() : CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, project.getOsType()));
		// Distribution Type
		Cell distributionType = sheet.getRow(9).getCell(1);
		distributionType.setCellType(CellType.STRING);
		distributionType.setCellValue(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTION_TYPE, project.getDistributionType()));
		// Network Service Only?
		Cell networkServiceOnly = sheet.getRow(10).getCell(1);
		networkServiceOnly.setCellType(CellType.STRING);
		networkServiceOnly.setCellValue(project.getNetworkServerType());
				
		// About OSC Process
		String suffixUrl = CommonFunction.getProperty("fosslight.suffix");
		if (!isEmpty(suffixUrl)) {
			// Distribution Site
			Cell distributionSite = sheet.getRow(13).getCell(1);
			distributionSite.setCellType(CellType.STRING);
			distributionSite.setCellValue(CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_CODE, project.getDistributeTarget()));
			// Notice Type
			String noticeTypeStr = CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, project.getNoticeType());

			if (!isEmpty(project.getNoticeTypeEtc())) {
				noticeTypeStr += " (" +CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, project.getNoticeTypeEtc()) + ")";
			}

			Cell noticeType = sheet.getRow(14).getCell(1);
			noticeType.setCellType(CellType.STRING);
			noticeType.setCellValue(noticeTypeStr);
			// Comment
			Cell comment = sheet.getRow(15).getCell(1);
			comment.setCellType(CellType.STRING);
			comment.setCellValue(CommonFunction.html2text(project.getComment()));

		} else {
			// Notice Type
			String noticeTypeStr = CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, project.getNoticeType());

			if (!isEmpty(project.getNoticeTypeEtc())) {
				noticeTypeStr += " (" +CoCodeManager.getCodeString(CoConstDef.CD_PLATFORM_GENERATED, project.getNoticeTypeEtc()) + ")";
			}

			Cell noticeType = sheet.getRow(13).getCell(1);
			noticeType.setCellType(CellType.STRING);
			noticeType.setCellValue(noticeTypeStr);
			// Comment
			Cell comment = sheet.getRow(14).getCell(1);
			comment.setCellType(CellType.STRING);
			comment.setCellValue(CommonFunction.html2text(project.getComment()));
		}
	}

	private static void partnerReportSheet(Workbook wb, Sheet sheet, PartnerMaster partner) {
		// About the report
		// Creator
		Cell authorCell = sheet.getRow(1).getCell(1);
		authorCell.setCellType(CellType.STRING);
		authorCell.setCellValue(partner.getCreatorName());
		// Division of Creator
		Cell divisionrCell = sheet.getRow(2).getCell(1);
		divisionrCell.setCellType(CellType.STRING);
		divisionrCell.setCellValue(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, partner.getDivision()));
		// Report Creation Date
		Cell dateCell = sheet.getRow(3).getCell(1);
		dateCell.setCellType(CellType.STRING);
		dateCell.setCellValue(CommonFunction.formatDate(partner.getCreatedDate()));
		
		// About the 3rd party
		// 3rd Party Software Name
		Cell partySoftwareNameCell = sheet.getRow(6).getCell(1);
		partySoftwareNameCell.setCellType(CellType.STRING);
		CellStyle hyperLinkStyle = wb.createCellStyle();
		Font hyperLinkFont = wb.createFont();
		hyperLinkFont.setUnderline(Font.U_SINGLE);
		hyperLinkFont.setColor(IndexedColors.BLUE.getIndex());
		hyperLinkStyle.setFont(hyperLinkFont);
		hyperLinkStyle.setBorderTop(BorderStyle.THIN);
		hyperLinkStyle.setBorderBottom(BorderStyle.THIN);
		hyperLinkStyle.setBorderRight(BorderStyle.THIN);
		Hyperlink hyperlink = wb.getCreationHelper().createHyperlink(HyperlinkType.URL);
		hyperlink.setAddress(CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/partner/shareUrl/" + partner.getPartnerId());
		partySoftwareNameCell.setHyperlink(hyperlink);
		partySoftwareNameCell.setCellStyle(hyperLinkStyle);
		partySoftwareNameCell.setCellValue(partner.getSoftwareName());
		// 3rd Party Name
		Cell partyName = sheet.getRow(7).getCell(1);
		partyName.setCellType(CellType.STRING);
		partyName.setCellValue(partner.getPartnerName());
		// 3rd Party Software Version
		Cell partySoftwareVersion = sheet.getRow(8).getCell(1);
		partySoftwareVersion.setCellType(CellType.STRING);
		partySoftwareVersion.setCellValue(avoidNull(partner.getSoftwareVersion()));
		// Delivery Form
		Cell deliveryForm = sheet.getRow(9).getCell(1);
		deliveryForm.setCellType(CellType.STRING);
		deliveryForm.setCellValue(CoCodeManager.getCodeString(CoConstDef.CD_PARTNER_DELIVERY_FORM, partner.getDeliveryForm()));
		// Description
		Cell description = sheet.getRow(10).getCell(1);
		description.setCellType(CellType.STRING);
		description.setCellValue(CommonFunction.html2text(partner.getDescription()));
	}
	
	/**
	 * 
	 * @param licenseList
	 * @return
	 * @throws Exception
	 * @용도 license 엑셀
	 */
	private static String getLicenseExcel(List<LicenseMaster> licenseList) throws Exception{
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;
		
		try {
			inFile= new FileInputStream(new File(downloadpath+"/LicenseList.xlsx"));
			try {wb = new XSSFWorkbook(inFile);} catch (IOException e) {log.error(e.getMessage());}
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "LicenseList");
			
			List<String[]> rows = new ArrayList<>();
			
			for (int i = 0; i < licenseList.size(); i++){
				LicenseMaster param = licenseList.get(i);
				String[] rowParam = {
					param.getLicenseId()
					, param.getLicenseName()
					, param.getShortIdentifier()
					, convertLineSeparator(param.getLicenseNicknameList())
					, param.getLicenseType()
					, param.getRestrictionStr()
					, CommonFunction.makeLicenseObligationStr(param.getObligationChecks())
					, param.getWebpage()
					, param.getDescription() // user guide
					, param.getAttribution()
				};
				
				rows.add(rowParam);
			}
			
			//시트 만들기
			makeSheet(sheet, rows);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return makeExcelFileId(wb,"LicenseList");
	}
	
	private static String convertLineSeparator(List<String> list) {
		String rtn = "";
		if (list != null) {
			for (String s : list) {
				if (!isEmpty(s)) {
					if (!isEmpty(rtn)) {
						rtn += "\r\n";
					}
					
					rtn += s;
				}
			}
		}
		
		return rtn;
	}

	/**
	 * 
	 * @param partner
	 * @return
	 * @throws Exception
	 * @용도 oss 엑셀 파일
	 */
	private static String getOssExcel(List<OssMaster> oss) throws Exception{
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;
		
		try {
			inFile= new FileInputStream(new File(downloadpath+"/OssList.xlsx"));
			try {wb = new XSSFWorkbook(inFile);} catch (IOException e) {log.error(e.getMessage());}
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "ossList");
			
			List<String[]> rows = new ArrayList<>();
			for (int i = 0; i < oss.size(); i++){
				OssMaster param = oss.get(i);
				String[] rowParam = {
					param.getOssId()
					, param.getOssName()
					, convertPipeToLineSeparator(param.getOssNickname())
					, param.getOssVersion()
					, CommonFunction.makeOssTypeStr(param.getOssType())
					, param.getLicenseName()
					, param.getLicenseType()
					, param.getRestriction()
					, CoCodeManager.getCodeString(CoConstDef.CD_OBLIGATION_TYPE, param.getObligationType())
					, param.getDetectedLicense()
					, param.getHomepage()
					, param.getDownloadLocation()
					, param.getCopyright()
					, param.getAttribution()
					, param.getCvssScore()
				};
				
				rows.add(rowParam);
			}
			
			//시트 만들기
			makeSheet(sheet, rows);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return makeExcelFileId(wb,"OssList");
	}
	
	private static String convertPipeToLineSeparator(String nick) {
		String rtn = "";
		
		if (!isEmpty(nick)) {
			for (String s : nick.split("\\|")) {
				if (!isEmpty(s)) {
					if (!isEmpty(rtn)) {
						rtn += "\r\n";
					}
					
					rtn += s;
				}
			}
		}
		
		return rtn;
	}

	/**
	 * 
	 * @param projectList
	 * @return
	 * @throws Exception
	 * @용도 Project 엑셀
	 */
	private static String getProjectExcel(List<Project> projectList) throws Exception{
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;
		
		try {
			inFile= new FileInputStream(new File(downloadpath+"/ProjectList.xlsx"));
			
			try {
				wb = new XSSFWorkbook(inFile);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "ProjectList");
			
			Project expParam = new Project();
			
			for (Project p : projectList) {
				expParam.addPrjIdList(p.getPrjId());
			}
			
			Map<String, Map<String, String>> projectExpandInfo = projectService.getProjectDownloadExpandInfo(expParam);
			
			List<String[]> rows = new ArrayList<>();
			
			List<String> customNvdMaxScoreInfoList = new ArrayList<>();
			Map<String, OssMaster> ossInfoMap = CoCodeManager.OSS_INFO_UPPER;
			
			for (int i = 0; i < projectList.size(); i++){
				Project param = projectList.get(i);
				Map<String, String> expandInfo = projectExpandInfo.get(param.getPrjId());
				String nvdMaxScore = "";
				String conversionCveInfo = cacheService.findIdentificationMaxNvdInfo(param.getPrjId(), null);
				if (conversionCveInfo != null) {
					String[] conversionCveData = conversionCveInfo.split("\\@");
					nvdMaxScore = conversionCveData[3];
				}
				
				String[] rowParam = {
					param.getPrjId()
					, param.getStatus()
					, param.getPrjName()
					, param.getPrjVersion()
					, CoConstDef.COMMON_SELECTED_ETC.equals(avoidNull(param.getOsType(), CoConstDef.COMMON_SELECTED_ETC)) ? param.getOsTypeEtc() : CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, param.getOsType())
					, param.getDistributionType()
					, param.getIdentificationStatus()
					, getExpandProjectInfo(expandInfo, "PARTNER_CNT")
					, getExpandProjectInfo(expandInfo, "SRC_CNT")
					, getExpandProjectInfo(expandInfo, "BAT_CNT")
					, getExpandProjectInfo(expandInfo, "BOM_CNT") + "(" + getExpandProjectInfo(expandInfo, "DISCLOSE_CNT") + ")"
					, param.getVerificationStatus()
					, getExpandProjectInfo(expandInfo, "NOTICE_TYPE")
					, getExpandProjectInfo(expandInfo, "NOTICE_FILE_NAME")
					, getExpandProjectInfo(expandInfo, "PACKAGE_FILE_NAME")
//					, param.getDistributionStatus()
//					, getExpandProjectInfo(expandInfo, "DISTRIBUTE_TARGET")
//					, getExpandProjectInfo(expandInfo, "DISTRIBUTE_NAME")
//					, getExpandProjectInfo(expandInfo, "DISTRIBUTE_MASTER_CATEGORY")
//					, getExpandProjectInfo(expandInfo, "MODEL_INFO")
//					, getExpandProjectInfo(expandInfo, "DISTRIBUTE_DEPLOY_TIME")
					, nvdMaxScore
					, param.getDivision()
					, param.getCreator()
					, CommonFunction.formatDate(param.getCreatedDate())
					, param.getReviewer()
				};
				
				rows.add(rowParam);
			}
			
			//시트 만들기
			makeSheet2(sheet, rows);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e2) {
				}
			}
		}
		return makeExcelFileId(wb,"ProjectList");
	}
	
	private static String getExpandProjectInfo(Map<String, String> map, String target) {
		String rtnStr = "";
		
		if (map != null && map.containsKey(target)) {
			rtnStr = avoidNull(String.valueOf(map.get(target)));
			
			switch (target) {
				case "NOTICE_TYPE":
					rtnStr = CoCodeManager.getCodeString(CoConstDef.CD_NOTICE_TYPE, rtnStr);
					
					break;
				case "DISTRIBUTE_TARGET":
					rtnStr = CoCodeManager.getCodeString(CoConstDef.CD_DISTRIBUTE_CODE, rtnStr);
					
					break;
				case "DISTRIBUTE_DEPLOY_TIME":
					if (!isEmpty(rtnStr)) {
						rtnStr = CommonFunction.formatDate(rtnStr);
					}
					
					break;
				case "DISTRIBUTE_MASTER_CATEGORY":
					if (!isEmpty(rtnStr)) {
						rtnStr = StringUtil.leftPad(rtnStr, 6, "0");
						rtnStr = CommonFunction.makeCategoryFormat(String.valueOf(map.get("DISTRIBUTE_TARGET")), rtnStr.substring(0, 3), rtnStr.substring(3));
					}
					
					break;
				case "MODEL_INFO":
					if (!isEmpty(rtnStr)) {
						// T3.CATEGORY, '@',T3.SUBCATEGORY, '@',T3.MODEL_NAME, '@', T3.RELEASE_DATE
						String[] modelInfos = rtnStr.split("\\|");
						rtnStr = "";
						int modelSeq = 0;
						for (String model : modelInfos) {
							if (!isEmpty(rtnStr)) {
								rtnStr += "\n";
							}
							
							if (rtnStr.length() > 32000) {
								break;
							}
							
							String tmp = "";
							String[] _tmpArr = model.split("@"); 
							if (_tmpArr != null && _tmpArr.length == 4) {
								// category
								if (!isEmpty(_tmpArr[0]) && !isEmpty(_tmpArr[1])) {
									rtnStr += CommonFunction.makeCategoryFormat(String.valueOf(map.get("DISTRIBUTE_TARGET")), _tmpArr[0], _tmpArr[1]);
								}
								
								tmp += ",　";
								tmp += _tmpArr[2];
								tmp += ",　";
								if (!isEmpty(_tmpArr[3])) {
									tmp += CommonFunction.formatDateSimple(_tmpArr[3]);
								}
								rtnStr += tmp;
								modelSeq++;
							}
						}
						
						if (modelInfos.length-modelSeq > 0) {
							rtnStr += "and " + (modelInfos.length-modelSeq);
						}
					}
					
					break;
				default:
					break;
			}
		}
		
		return rtnStr;
	}
	
	private static String getPartnerExcelId(List<PartnerMaster> ossList) throws Exception{
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;

		try {
			inFile= new FileInputStream(new File(downloadpath+"/3rdList.xlsx"));
			wb = new XSSFWorkbook(inFile);
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "3rdList");
		
			if (ossList != null && !ossList.isEmpty()) {
				List<String[]> rows = new ArrayList<>();
				
				for (PartnerMaster bean : ossList) {
					List<String> params = new ArrayList<>();
					
					// main 정보
					params.add(bean.getPartnerId()); //ID
					params.add(bean.getPartnerName());
					params.add(bean.getSoftwareName());
					params.add(bean.getSoftwareVersion()); 
					params.add(CoCodeManager.getCodeString(CoConstDef.CD_IDENTIFICATION_STATUS, bean.getStatus()));
					params.add(CoCodeManager.getCodeString(CoConstDef.CD_PARTNER_DELIVERY_FORM, bean.getDeliveryForm()));
					params.add(bean.getDescription());
					params.add(bean.getFileName());
					params.add(bean.getFileName2());					
					params.add(bean.getDivision());
					params.add(bean.getCreator());
					params.add(CommonFunction.formatDate(bean.getCreatedDate()));
					
					rows.add(params.toArray(new String[params.size()]));
				}
				
				//시트 만들기
				makeSheet(sheet, rows);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return makeExcelFileId(wb,"PartnerList");
	}
	
	/**
	 * 
	 * @param batList
	 * @return
	 * @throws Exception
	 * @용도 user 엑셀
	 */
	private static String getUserExcelId(List<T2Users> userList) throws Exception{
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;
		
		try {
			inFile= new FileInputStream(new File(downloadpath+"/UserList.xlsx"));
			wb = new XSSFWorkbook(inFile);
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "UserList");
			
			List<String[]> rows = new ArrayList<>();
			
			for (int i = 0; i < userList.size(); i++){
				T2Users param = userList.get(i);
				String[] rowParam = {
					String.valueOf(i+1)
					, param.getUserId()
					, param.getEmail()
					, param.getUserName()
					, param.getDivision()
					, param.getCreatedDate()
					, param.getUseYn()
					, param.getAuthority()
				};
				
				rows.add(rowParam);
			}
			
			//시트 만들기
			makeSheet(sheet, rows);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return makeExcelFileId(wb, "UserList");
	}
	
	private static String getModelExcel(List<Project> modelList, String distributionType) throws Exception {
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;
		List<String[]> rows = null;
		
		try {
			inFile= new FileInputStream(new File(downloadpath+ (CoConstDef.CD_DISTRIBUTE_SITE_SKS.equals(distributionType) ? "/SKS_ModelList.xlsx" : "/ModelList.xlsx") ));
			wb = WorkbookFactory.create(inFile);
			String mainModelCode = CoConstDef.CD_MODEL_TYPE;
			
			// category sheet 생성
			sheet = wb.getSheetAt(1);
			rows = new ArrayList<>();
			int idx = 1;
			
			for (String mCode : CoCodeManager.getCodes(mainModelCode)) {
				String sCode = CoCodeManager.getSubCodeNo(mainModelCode, mCode);
				
				for (String subCode : CoCodeManager.getCodes(sCode)) {
					String categoryName = CoCodeManager.getCodeString(mainModelCode, mCode);
					String subCategoryName = CoCodeManager.getCodeString(sCode, subCode);
					
					String[] rowParam = {
						Integer.toString(idx++)
						, categoryName
						, subCategoryName
						, categoryName + " > " + subCategoryName
						, mCode
						, subCode
					};
					
					rows.add(rowParam);
				}
			}
			
			makeSheet(sheet, rows);
			
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "ModelList");
			
			rows = new ArrayList<>();
			
			if (modelList != null) {
				for (int i = 0; i < modelList.size(); i++){
					Project param = modelList.get(i);
					String main = StringUtil.substring(param.getCategory(), 0, 3);
					String sub = StringUtil.substring(param.getCategory(), 3);
					
					String mainStr =  CoCodeManager.getCodeString(mainModelCode, main);
					String subStr = CoCodeManager.getCodeString(CoCodeManager.getSubCodeNo(mainModelCode, main), sub);
					param.setCategory(mainStr+" > "+subStr);
					
					String[] rowParam = {
						param.getModelName()
						, param.getCategory()
						, param.getReleaseDate()
						, ""
					};
					
					rows.add(rowParam);
				}		
			}
			
			if (rows.size() > 0) {
				DataValidationHelper dvHelper = sheet.getDataValidationHelper();
				DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint("'Category List'!$D$2:$D$1048576");
				CellRangeAddressList addressList = new CellRangeAddressList(1, rows.size(), 1, 1);
				DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
	
				if (validation instanceof XSSFDataValidation) {
					validation.setSuppressDropDownArrow(true);
				} else {
					// If the Datavalidation contains an instance of the
					// HSSFDataValidation
					// class then 'true' should be passed to the
					// setSuppressDropDownArrow()
					// method and the call to setShowErrorBox() is not
					// necessary.
					validation.setSuppressDropDownArrow(false);
				}
				
				sheet.addValidationData(validation);
			}
			
			//시트 만들기
			makeSheet(sheet, rows);
			
			wb.setActiveSheet(0);

		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return makeExcelFileId(wb,CoConstDef.CD_DISTRIBUTE_SITE_SKS.equals(distributionType) ? "SKS_ModelList" : "ModelList");
	}

	private static String makeExcelFileId(Workbook wb, String target) throws IOException {
		return makeExcelFileId(wb, target, "xlsx");
	}
	
	private static String makeExcelFileId(Workbook wb, String target, String exp) throws IOException {
		UUID randomUUID = UUID.randomUUID();
		String fileName = CommonFunction.replaceSlashToUnderline(target);
		String logiFileName = fileName + "_" + randomUUID+"."+exp;
		String excelFilePath = writepath+"/download/";
		
		FileOutputStream outFile = null;
		
		try {
			if (!Files.exists(Paths.get(excelFilePath))) {
				Files.createDirectories(Paths.get(excelFilePath));
			}
			outFile = new FileOutputStream(excelFilePath + logiFileName);
			wb.write(outFile);
			
			// db 등록
			return fileService.registFileDownload(excelFilePath, fileName + "."+exp, logiFileName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (outFile != null) {
				try {
					outFile.close();
				} catch (Exception e2) {}
			}
		}
		
		return null;
	}
	
//	private static String makeCsvFileId(String target, List<String[]> datas) throws IOException {
//		
//		UUID randomUUID = UUID.randomUUID();
//		String fileName = CommonFunction.replaceSlashToUnderline(target)+"_"+CommonFunction.getCurrentDateTime();
//		String logiFileName = fileName + "_" + randomUUID+".csv";
//		String excelFilePath = writepath+"/download/";
//		CSVWriter cw = null;
//		
//		FileWriter fileWriter = null;
//		CSVPrinter csvFilePrinter = null;
//		CSVFormat csvFileFormat = CSVFormat.EXCEL;
//		
//		FileOutputStream outFile = null;
//		
//		try {			
//			fileWriter = new FileWriter(excelFilePath + logiFileName);
//
//			if (!Files.exists(Paths.get(excelFilePath))) {
//				Files.createDirectories(Paths.get(excelFilePath));
//			}
//			
//			csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
//			
//			for (String[] row : datas) {
//				csvFilePrinter.printRecord(Arrays.asList(row));
//			}
//			
//			fileWriter.flush();
//			// db 등록
//			return fileService.registFileDownload(excelFilePath, fileName + ".csv", logiFileName);
//		} catch (Exception e) {
//			log.error(e.getMessage(), e);
//		} finally {
//			if (outFile != null) {
//				try {
//					outFile.close();
//				} catch (Exception e2) {}
//			}
//			
//			if (cw != null) {
//				try {
//					cw.close();
//				} catch (Exception e2) {}
//			}
//			
//			if (fileWriter != null) {
//				try {
//					fileWriter.close();
//				} catch (Exception e2) {}
//			}
//			
//			if (csvFilePrinter != null) {
//				try {
//					csvFilePrinter.close();
//				} catch (Exception e2) {}
//			}
//		}
//		
//		return null;
//	}
	
	private static String makeAnalysisListExcelFileId(Workbook wb, String target, String exp, String prjId) throws IOException {
		UUID randomUUID = UUID.randomUUID();
		String fileName = CommonFunction.replaceSlashToUnderline(target)+"_"+CommonFunction.getCurrentDateTime();
		String logiFileName = fileName + "_" + randomUUID+"."+exp;
		String analysisSavePath = CommonFunction.emptyCheckProperty("autoanalysis.input.path", "/autoanalysis/input/dev") + "/" + prjId;
		
		FileOutputStream outFile = null;
		
		try {
			if (!Files.exists(Paths.get(analysisSavePath))) {
				Files.createDirectories(Paths.get(analysisSavePath));
			}
			
			outFile = new FileOutputStream(analysisSavePath + "/" + logiFileName);
			
			wb.write(outFile);
			
			// db 등록
			return fileService.registFileDownload(analysisSavePath, fileName + "." + exp, logiFileName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (outFile != null) {
				try {
					outFile.close();
				} catch (Exception e2) {}
			}
		}
		
		return null;
	}

	private static <T extends ExcelData> String makeExcelDataId(List<T> dataList, String type) throws Exception {
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;

		try {
			inFile= new FileInputStream(downloadpath + File.separator + type + ".xlsx");
			try {wb = new XSSFWorkbook(inFile);} catch (IOException e) {log.error(e.getMessage());}
            sheet = wb.getSheetAt(0);
			wb.setSheetName(0, type);
			List<String[]> rows = dataList.stream().map(ExcelData::toRow).collect(Collectors.toList());
			makeSheet(sheet, rows);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception ignored) {
				}
			}
		}

		return makeExcelFileId(wb, type);
	}

	public static String getExcelDownloadId(String type, String dataStr, String filepath) throws Exception {
		return getExcelDownloadId(type, dataStr, filepath, null);
	}

	@SuppressWarnings({ "unchecked", "serial" })
	public static String getExcelDownloadId(String type, String dataStr, String filepath, String extParam) throws Exception {
		downloadpath = filepath;
		String downloadId = null;
		
		switch (type) {
			case "verification":
				downloadId = makeVerificationExcel((List<ProjectIdentification>) fromJson(dataStr, new TypeToken<List<ProjectIdentification>>(){}.getType()));
				
				break;
			case "project" :	//Project List			
				// status, publicYn 조건 추가로 data 가공
				ObjectMapper mapper = new ObjectMapper();
				Map<String, Object> projectMap = new HashMap<String, Object>();
				projectMap = mapper.readValue((String) dataStr, new TypeReference<Map<String, Object>>(){});
				
				if (projectMap != null){ 
					if (projectMap.get("statuses") != null) {
						String statuses = String.valueOf(projectMap.get("statuses"));
						
						if (!isEmpty(statuses)){
							String[] arrStatuses = statuses.split(",");
							projectMap.put("arrStatuses", arrStatuses);
						}
					}
					
					projectMap.put("publicYn", (isEmpty(String.valueOf(projectMap.get("publicYn")))?CoConstDef.FLAG_YES:String.valueOf(projectMap.get("publicYn"))));
					dataStr = mapper.writeValueAsString(projectMap);
				}
				
				Type 				projectType = new TypeToken<Project>(){}.getType();
				Project 			project 	= (Project) fromJson(dataStr, projectType);
				
				List<String> watcherList = new ArrayList<>();
				String[] watchers = project.getWatchers();
				for (String watcher : watchers) {
					if (!isEmpty(watcher)) watcherList.add(watcher);
				}
				project.setWatchers(watcherList.toArray(new String[watcherList.size()]));
				
				project.setStartIndex(0);
				project.setPageListSize(MAX_RECORD_CNT);
				project.setExcelDownloadFlag(CoConstDef.FLAG_YES);
				Map<String, Object> prjMap =	 projectService.getProjectList(project);
				
				if (isMaximumRowCheck((int) prjMap.get("records"))){
					downloadId	= getProjectExcel((List<Project>) prjMap.get("rows"));
				}
				
				break;
			case "report" :		//project report
			case "bom" :		//project bom
				downloadId = getReportExcelPost(dataStr, null);
				
				break;
			case "dep" :		//DEP List
				downloadId = getReportExcelPost(dataStr, CoConstDef.CD_DTL_COMPONENT_ID_DEP);
				
				break;
			case "src" :		//SRC List
				downloadId = getReportExcelPost(dataStr, CoConstDef.CD_DTL_COMPONENT_ID_SRC);
				
				break;
			case "bin" :		//bin List
				downloadId = getReportExcelPost(dataStr, CoConstDef.CD_DTL_COMPONENT_ID_BIN);
				
				break;
			case "binAndroid" :		//bin android List
				downloadId = getReportExcelPost(dataStr, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
				
				break;
			case "binAndroidBom" :	//bin android bom List
				downloadId = getReportExcelPost(dataStr, CoConstDef.CD_DTL_COMPONENT_ID_ANDROID_BOM);
				
				break;
			case "partner" :		//identification > 3rd List
				downloadId = getReportExcelPost(dataStr, CoConstDef.CD_DTL_COMPONENT_ID_PARTNER);
				
				break;
			case "license" :	//License List
				Type 				licenseType = new TypeToken<LicenseMaster>(){}.getType();
				LicenseMaster 		license 	= (LicenseMaster) fromJson(dataStr, licenseType);
				license.setStartIndex(0);
				license.setPageListSize(MAX_RECORD_CNT);
				List<LicenseMaster> licenseList = licenseService.getLicenseMasterListExcel(license);
				
				if (isMaximumRowCheck(licenseService.selectLicenseMasterTotalCount(license))){
					downloadId 	= getLicenseExcel(licenseList);
				}
				
				break;
			case "oss" :		//Oss List
				Type 				ossType 	= new TypeToken<OssMaster>(){}.getType();
				OssMaster 			oss 		= (OssMaster) fromJson(dataStr, ossType);
				oss.setStartIndex(0);
				oss.setPageListSize(MAX_RECORD_CNT);
				
				Map<String,Object> ossMap 	= ossService.getOssMasterList(oss);
				downloadId 	= getOssExcel((List<OssMaster>) ossMap.get("rows"));
				
				break;
			case "model" :		//project model
				Type 				modelType 	= new TypeToken<List<Project>>(){}.getType();
				List<Project>		modelList 		= (List<Project>) fromJson(dataStr, modelType);
				
				downloadId = getModelExcel(modelList, extParam);
				
				break;
			case "selfCheckList" :
				downloadId = getSelfCheckListExcelPost(dataStr, null);
				
				break;
			case "selfReport" :		//selfCheck Project
				downloadId = getSelftReportExcelPost(dataStr);
				
				break;
			case "partnerCheckList" :
				downloadId = getPartnerChecklistReportExcelPost(dataStr, CoConstDef.CD_DTL_COMPONENT_PARTNER);
				
				break;
			case "partnerBomList" :
			case "partnerBomList_demo" :
				downloadId = getPartnerChecklistReportExcelPost(dataStr, null);
				
				break;
			case "spdx" :
				downloadId = getVerificationSPDX_SpreadSheetExcelPost(dataStr);

				break;
			case "spdx_sbom" :
				downloadId = getSBOMSPDX_SpreadSheetExcelPost(dataStr);

				break;
			case "spdx_self" :
				downloadId = getSelfCheckSPDX_SpreadSheetExcelPost(dataStr);
				
				break;
			case "binaryDBLog" :
				Type 	binaryDBLogType = new TypeToken<BinaryAnalysisResult>(){}.getType();
				BinaryAnalysisResult bianryDbLogBean = (BinaryAnalysisResult) fromJson(dataStr, binaryDBLogType);
				
				Map<String, String> exceptionMap = new HashMap<>();
				exceptionMap.put("binaryName", 		"fileName");
				exceptionMap.put("filePath", 		"pathName");
				exceptionMap.put("tlsh", 			"tlshCheckSum");
				exceptionMap.put("parentname",		"parentName");
				exceptionMap.put("platformname", 	"platformName");
				exceptionMap.put("platformversion", "platformVersion");
				exceptionMap.put("updatedate", 		"updateDate");
				exceptionMap.put("createddate", 	"createdDate");
				
				String filterCondition = CommonFunction.getFilterToString(bianryDbLogBean.getFilters(), null, exceptionMap);
				
				if (!isEmpty(filterCondition)) {
					bianryDbLogBean.setFilterCondition(filterCondition);
				}
	
				bianryDbLogBean.setSidx("actionId");
				bianryDbLogBean.setSord("desc");
				bianryDbLogBean.setPageListSize(MAX_RECORD_CNT_LIST);
				
				Map<String, Object> bianryDbLogMap = binaryDataHistoryService.getBinaryDataHistoryList(bianryDbLogBean);
				
				if (isMaximumRowCheck((int) bianryDbLogMap.get("records"))){
					downloadId = getBinaryDBLogExcel((List<BinaryAnalysisResult>) bianryDbLogMap.get("rows"));
				}
				
				break;
			case "3rd" :		//3rd Party List
				Type 				partnerType = new TypeToken<PartnerMaster>(){}.getType();
				PartnerMaster 		partner 	= (PartnerMaster) fromJson(dataStr, partnerType);
	
				if (partner.getStatus() != null) {
					String statuses = partner.getStatus();
					
					if (!isEmpty(statuses)) {
						String[] arrStatuses = statuses.split(",");
						partner.setArrStatuses(arrStatuses);
					}
				}
				
				List<String> partnerWatcherList = new ArrayList<>();
				String[] partnerWatchers = partner.getWatchers();
				for (String partnerWatcher : partnerWatchers) {
					if (!isEmpty(partnerWatcher)) partnerWatcherList.add(partnerWatcher);
				}
				partner.setWatchers(partnerWatcherList.toArray(new String[partnerWatcherList.size()]));
				
				partner.setStartIndex(0);
				partner.setPageListSize(MAX_RECORD_CNT);
				partner.setModelFlag(CoConstDef.FLAG_NO);
				
				Map<String, Object> partnerList =	 partnerService.getPartnerMasterList(partner);
				downloadId	= getPartnerExcelId((List<PartnerMaster>) partnerList.get("rows"));
				
				break;
//			case "3rdModel" :		//3rd Party List
//				Type 				partnerModelType = new TypeToken<PartnerMaster>(){}.getType();
//				PartnerMaster 		partnerModel	  = (PartnerMaster) fromJson(dataStr, partnerModelType);
//	
//				if (partnerModel.getStatus() != null) {
//					String statuses = partnerModel.getStatus();
//					
//					if (!isEmpty(statuses)) {
//						String[] arrStatuses = statuses.split(",");
//						partnerModel.setArrStatuses(arrStatuses);
//					}
//				}
//				
//				partnerModel.setStartIndex(0);
//				partnerModel.setPageListSize(MAX_RECORD_CNT);
//				partnerModel.setModelFlag(CoConstDef.FLAG_YES);
//				
//				Map<String, Object> partnerModelList =	 partnerService.getPartnerStatusList(partnerModel);
//				
//				if (isMaximumRowCheck((int) partnerModelList.get("records"))){
//					downloadId	= getPartnerModelExcelId((List<PartnerMaster>) partnerModelList.get("rows"));
//				}
//				
//				break;
//			case "modelStatus":
//				Type 			ProjectModelType = new TypeToken<Project>(){}.getType();
//				Project 		ProjectModel	 = (Project) fromJson(dataStr, ProjectModelType);
//				
//				if (!isEmpty(ProjectModel.getModelName())){
//					String[] modelNames = ProjectModel.getModelName().split(",");
//					String[] productGroups = ProjectModel.getProductGroup().split(",");
//					List<String> modelListInfo = new ArrayList<String>();
//					List<String> productGroupListInfo = new ArrayList<String>();
//					
//					for (String modelName : modelNames){
//						modelListInfo.add(modelName);
//					}
//					
//					for (String productGroup : productGroups){
//						productGroupListInfo.add(productGroup);
//					}
//					
//					ProjectModel.setModelListInfo(modelListInfo);
//					ProjectModel.setProductGroups(productGroupListInfo);
//					ProjectModel.setPageListSize(MAX_RECORD_CNT_LIST);
//					
//					Map<String, Object> map = complianceService.getModelList(ProjectModel);
//					
//					if (isMaximumRowCheck((int) map.get("records"))){
//						downloadId	= getModelStatusExcelId((List<Project>) map.get("rows"), ProjectModel);
//					}
//				} else {
//					downloadId	= getModelStatusExcelId(null, null);
//				}
//				
//				break;
			case "vulnerability" :		//vulnerability 2018-07-26 choye 추가 
				Type vulnerabilityType = new TypeToken<Vulnerability>(){}.getType();
				Vulnerability vulnerability = (Vulnerability) fromJson(dataStr, vulnerabilityType);
				vulnerability.setSidx("cveId");
				vulnerability.setPageListSize(MAX_RECORD_CNT_LIST);
				
				Map<String, Object> vulnerabilityMap =	vulnerabilityService.getVulnerabilityList(vulnerability, true);
				
				if (isMaximumRowCheck((int) vulnerabilityMap.get("records"))){
					downloadId = getVulnerabilityExcel((List<Vulnerability>) vulnerabilityMap.get("rows"), false);
				}
				
				break;

			case "vulnerabilityPopup":	//export in vulnerability popup
				Type ossMaster = new TypeToken<OssMaster>(){}.getType();
				OssMaster bean = (OssMaster) fromJson(dataStr, ossMaster);
				bean.setPageListSize(MAX_RECORD_CNT_LIST);

				Map<String, Object> vulnerabilityPopupMap = vulnerabilityService.getVulnListByOssName(bean);

				if (isMaximumRowCheck((int) vulnerabilityPopupMap.get("records"))){
					downloadId = getVulnerabilityExcel((List<Vulnerability>) vulnerabilityPopupMap.get("rows"), true);
				}

				break;

			case "autoAnalysis":
				OssMaster ossBean = new OssMaster();
				ossBean.setPrjId(dataStr);
				ossBean.setStartAnalysisFlag(CoConstDef.FLAG_YES);
				ossBean.setPageListSize(MAX_RECORD_CNT);
				
				Map<String, Object> analysisList = ossService.getOssAnalysisList(ossBean);
				
				downloadId = getAnalysisListExcel((List<OssAnalysis>) analysisList.get("rows"), (String) ossBean.getPrjId());
				
				break;
			case "user" :		//UserManagement List
				List<T2Users> 		userList = userService.getUserListExcel();
				downloadId = getUserExcelId(userList);
				
				break;
			case "bomcompare" :
				downloadId = getBomCompareExcelId(dataStr, true);
				
				break;
			case "3rdParty_bomcompare" :
				downloadId = getBomCompareExcelId(dataStr, false);
				
				break;
			case "security":
				Type prj = new TypeToken<Project>(){}.getType();
				Project param = (Project) fromJson(dataStr, prj);
				Map<String, Object> result = projectService.getSecurityGridList(param);
				Project projectMaster = projectService.getProjectDetail(param);
				
				downloadId = getSecurityExcelId(result, projectMaster, param.getCode());
				
				break;
			case "lite-oss":
				Type ossListType = new TypeToken<List<OssDto>>(){}.getType();
				var liteOssData = (List<OssDto>) fromJson(dataStr, ossListType);
				downloadId = makeExcelDataId(liteOssData, "OssList");
				break;
			case "lite-licenses":
				Type licenseListType = new TypeToken<List<LicenseDto>>(){}.getType();
				var liteLicenseData = (List<LicenseDto>) fromJson(dataStr, licenseListType);
				downloadId = makeExcelDataId(liteLicenseData, "LicenseList");
			case "cycloneDXJson" :
			case "cycloneDXXml" :
				downloadId = getCycloneDXFileId(type, dataStr, extParam.equals("verify") ? true : false);
				break;
			default:
				break;
		}
		
		return downloadId;
	}
	
	@SuppressWarnings("unchecked")
	private static String getSecurityExcelId(Map<String, Object> result, Project projectMaster, String code) throws IOException {
		List<OssComponents> securityGridList = null;
		switch (code) {
			case "total" : securityGridList = (List<OssComponents>) result.get("totalList");
				break;
			default : securityGridList = (List<OssComponents>) result.get("fullDiscoveredList");
				break;
		}
		
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;
		
		// download file name
		String downloadFileName = "fosslight_security"; // Default
		downloadFileName += "_" + CommonFunction.getCurrentDateTime() + "_prj-" + StringUtil.deleteWhitespaceWithSpecialChar(projectMaster.getPrjId());
		
		try {
			inFile= new FileInputStream(new File(downloadpath+"/Security.xlsx"));
			wb = WorkbookFactory.create(inFile);
			CreationHelper creationHelper = wb.getCreationHelper();
			CellStyle style = wb.createCellStyle();
			CellStyle hyperLinkStyle = wb.createCellStyle();
			Font hyperLinkFont = wb.createFont();
			hyperLinkFont.setUnderline(Font.U_SINGLE);
			hyperLinkFont.setColor(IndexedColors.BLUE.getIndex());
			hyperLinkStyle.setFont(hyperLinkFont);
			sheet = wb.getSheetAt(7);
			
			if (securityGridList != null){
				List<String[]> rowInfoData = new ArrayList<>();
				List<String[]> rowDatas = new ArrayList<>();
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date now = new Date();
				String now_dt = format.format(now);
				
				String[] rowInfoParam = {
						now_dt
						, projectMaster.getPrjName()
						, projectMaster.getPrjVersion()
						, projectMaster.getPrjUserName()
						, CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, projectMaster.getDivision())
				};
				
				rowInfoData.add(rowInfoParam);
				
				int num = 1;
				for (OssComponents bean : securityGridList) {
					String[] rowParam = {
						String.valueOf(num++)
						, bean.getOssName()
						, bean.getOssVersion()
						, bean.getCveId()
						, bean.getPublDate()
						, bean.getCvssScore()
						, bean.getVulnerabilityResolution()
						, bean.getVulnerabilityLink()
						, bean.getSecurityComments()
					};
					
					rowDatas.add(rowParam);
				}
				
				makeSecuritySheet(creationHelper, sheet, style, hyperLinkStyle, rowInfoData, rowDatas, true, false);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e) {}
			}
		}
		
		return makeExcelFileId(wb, downloadFileName);
	}
	
	private static void makeSecuritySheet(CreationHelper creationHelper, Sheet sheet, CellStyle style, CellStyle hyperLinkStyle, List<String[]> infoRows, List<String[]> rows, boolean isProject, boolean isDemo) {
		int infoStartRow= 1;
		int startRow= 8;
		int startCol = 0;
		int endCol = 0;
		if (!isProject || isDemo) {
			startRow = 9;
		}
		if (!infoRows.isEmpty()) {
			endCol = infoRows.get(0).length-1;
		}
		int shiftRowNum = infoRows.get(0).length;
		String[] rowParam = infoRows.get(0);
		
		for (int i = infoStartRow; i < infoStartRow+shiftRowNum; i++){
			Row templateRow = sheet.getRow(i);
			Cell templateCell = templateRow.getCell(3);
			CellStyle st = templateCell.getCellStyle();
			
			Row row = sheet.getRow(i);
			Cell cell = getCell(row, 3);
			cell.setCellStyle(st);
			cell.setCellType(CellType.STRING);
			if (!isEmpty(rowParam[i-infoStartRow])) {
				cell.setCellValue(rowParam[i-infoStartRow]);
			} else {
				cell.setBlank();
			}
		}
		
		if (!rows.isEmpty()) {
			endCol = rows.get(0).length-1;
		}
		
		int rowIndex = 0;
		if (!isProject) {
			Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.DOCUMENT);
			for (int i = startRow; i < startRow+rows.size(); i++){
				Row row = sheet.createRow(i);
				for (int colNum=startCol; colNum<=endCol; colNum++){
					Cell cell = row.createCell(colNum);
					style.setWrapText(false);
					String cellData = rows.get(rowIndex)[colNum];
					
					if (colNum == 7 || colNum == 8) {
						if (!isEmpty(cellData)) {
							if (cellData.contains(",")) {
								String[] splitData = cellData.split(",");
								String sData = "";
								for (int j=0; j<splitData.length; j++) {
									sData += splitData[j];
									sData += "\n";
								}
								cellData = sData.substring(0, sData.length()-1);
								hyperLinkStyle.setWrapText(true);
							}
							hyperlink.setAddress(cell.getStringCellValue());
							cell.setCellValue(cellData);
							cell.setCellStyle(hyperLinkStyle);
						} else {
							cell.setBlank();
						}
					} else {
						cell.setCellStyle(style);
						if (!isEmpty(cellData)) {
							cell.setCellValue(cellData);
						} else {
							cell.setBlank();
						}
					}
				}
				rowIndex++;
			}
		} else {
			for (int i = startRow; i < startRow+rows.size(); i++){
				Row row = sheet.createRow(i);
				for (int colNum=startCol; colNum<=endCol; colNum++){
					Cell cell = row.createCell(colNum);
					String cellData = rows.get(rowIndex)[colNum];
					if (!isEmpty(cellData)) {
						cell.setCellValue(cellData);
					} else {
						cell.setBlank();
					}
					cell.setCellStyle(style);
				}
				rowIndex++;
			}
		}
	}
	/**
	 * Binary DB excel download
	 * @param bianrySearchBean
	 * @return
	 * @throws IOException 
	 * @throws InvalidFormatException 
	 */
	private static String getBinaryDBLogExcel(List<BinaryAnalysisResult> list) throws InvalidFormatException, IOException {
		Workbook wb = null;
		Sheet sheet1 = null;
		FileInputStream inFile=null;
		// download file name
		String downloadFileName = "FOSSLight-BinaryDBLog"; // Default

		try {
			inFile= new FileInputStream(new File(downloadpath+"/BinaryDBLog.xlsx"));
			wb = WorkbookFactory.create(inFile);
			sheet1 = wb.getSheetAt(0);
			
			if (list != null){
				List<String[]> rowDatas = new ArrayList<>();
				
				for (BinaryAnalysisResult bean : list) {
					String[] rowParam = {
							bean.getActionId()
							, bean.getActionType()
							, avoidNull(bean.getBinaryName())
							, avoidNull(bean.getFilePath())
							, avoidNull(bean.getSourcePath())
							, avoidNull(bean.getCheckSum())
							, avoidNull(bean.getTlsh())
							, avoidNull(bean.getOssName())
							, avoidNull(bean.getOssVersion())
							, avoidNull(bean.getLicense())
							, avoidNull(bean.getParentname())
							, avoidNull(bean.getPlatformname())
							, avoidNull(bean.getPlatformversion())
							, avoidNull(bean.getUpdatedate())
							, avoidNull(bean.getCreatedDate())
							, avoidNull(bean.getModifier())
							, avoidNull(bean.getComment())
						};
					
					rowDatas.add(rowParam);
				}
				
				makeSheet(sheet1, rowDatas);
			}
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e) {}
			}
		}

		return makeExcelFileId(wb,downloadFileName);
	}

	/**
	 * SPDX Spread sheet 생성
	 * @param dataStr
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static String getVerificationSPDX_SpreadSheetExcelPost(String dataStr) throws IOException {
		Workbook wb = null;
		Sheet sheetDoc = null; // Document info
		Sheet sheetPackage = null; // Package Info
		Sheet sheetExternalRefs = null; // ExternalRefs
		Sheet sheetLicense = null; // Extracted License Info
		Sheet sheetPerFile = null; // Per File Info
		Sheet sheetRelationships = null; // Relationships
		FileInputStream inFile=null;

		// download file name
		String downloadFileName = "SPDXRdf-"; // Default

		Type ossNoticeType = new TypeToken<OssNotice>(){}.getType();

		OssNotice ossNotice = (OssNotice) fromJson(dataStr, ossNoticeType);
		ossNotice.setFileType("text");

		String prjId = ossNotice.getPrjId();

		try {
			inFile= new FileInputStream(new File(downloadpath+"/SPDXRdf_2.2.2.xls"));

			wb = WorkbookFactory.create(inFile);
			sheetDoc = wb.getSheetAt(0);
			sheetPackage = wb.getSheetAt(1);
			sheetExternalRefs = wb.getSheetAt(2);
			sheetLicense = wb.getSheetAt(3);
			sheetPerFile = wb.getSheetAt(4);
			sheetRelationships = wb.getSheetAt(5);

			String createdTime = CommonFunction.getCurrentDateTime("yyyyMMddhhmm");
			String createdTimeFull = CommonFunction.getCurrentDateTime("yyyy-MM-dd hh:mm:ss");
			Date createdDateTime = DateUtil.getCurrentDate();

			Project projectInfo = new Project();
			projectInfo.setPrjId(prjId);
			projectInfo = projectService.getProjectDetail(projectInfo);

			projectInfo.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_DEP);
			List<OssComponents> dependenciesDataList = projectService.getDependenciesDataList(projectInfo);
			Map<String, Object> relationshipsMap = new HashMap<>();
			
			T2Users userInfo = new T2Users();
			userInfo.setUserId(projectInfo.getCreator());

			Map<String, Object> packageInfo = verificationService.getNoticeHtmlInfo(ossNotice);

			String strPrjName = projectInfo.getPrjName();

			if (!isEmpty(projectInfo.getPrjVersion())) {
				strPrjName += "-" + projectInfo.getPrjVersion();
			}

			downloadFileName += FileUtil.makeValidFileName(strPrjName, "_").replaceAll(" ", "").replaceAll("--", "-");

			List<String> packageInfoidentifierList = new ArrayList<>();
			Map<String, String> externalRefsMap = new HashMap<>();
			
			//Document Info
			{
				Row row = sheetDoc.getRow(1);

				int cellIdx = 0;
				// Spreadsheet Version
				cellIdx ++;
				// SPDX Version
				cellIdx ++;
				// Data License
				cellIdx ++;

				// SPDX Identifier
				cellIdx++;

				// License List Version
				cellIdx ++;

				// Document Name
				Cell cellDocumentName = getCell(row, cellIdx); cellIdx++;
				cellDocumentName.setCellValue(strPrjName);

				// Document Namespace
				Cell cellDocumentNamespace = getCell(row, cellIdx); cellIdx++;
				String spdxidentifier = "SPDXRef-" + strPrjName.replaceAll(" ", "") + "-" + createdTime;
				String domain = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org/");
				if (!domain.endsWith("/")) {
					domain += "/";
				}
				cellDocumentNamespace.setCellValue(domain + spdxidentifier);

				// Document Contents
				cellIdx++;

				//External Document References
				cellIdx ++;
				// Document Comment
				cellIdx ++;

				// Creator
				Cell cellCreator = getCell(row, cellIdx); cellIdx++;
				String strCreator = "Person: ";
				userInfo = userService.getUser(userInfo);
				strCreator += projectInfo.getCreator() + " (" + userInfo.getEmail() + ")";
				cellCreator.setCellValue(strCreator);

				// Created
				Cell cellCreated = getCell(row, cellIdx); cellIdx++;
				cellCreated.setCellValue(createdDateTime);

				// Creator Comment
			}

			// Package Info
			{
				List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("noticeObligationList");

				List<OssComponents> sourceList = (List<OssComponents>) packageInfo.get("disclosureObligationList");

				boolean hideOssVersionFlag = CoConstDef.FLAG_YES.equals(ossNotice.getHideOssVersionYn());

				if (sourceList != null && !sourceList.isEmpty()) {
					noticeList.addAll(sourceList);
				}

				noticeList = verificationService.setMergeGridData(noticeList); // merge Data

				int rowIdx = 1;

				for (OssComponents bean : noticeList) {
					Row row = sheetPackage.getRow(rowIdx);

					if (row == null) {
						row = sheetPackage.createRow(rowIdx);
					}

					String attributionText = "";

					int cellIdx = 0;

					// Package Name
					Cell cellPackageName = getCell(row, cellIdx); cellIdx++;
					cellPackageName.setCellValue(bean.getOssName());

					// SPDX Identifier
					Cell cellSPDXIdentifier = getCell(row, cellIdx); cellIdx++;
					String ossName = bean.getOssName().replace("&#39;", "\'"); // ossName에 '가 들어갈 경우 정상적으로 oss Info를 찾지 못하는 증상이 발생하여 현재 값으로 치환.

					String relationshipsKey = bean.getPackageUrl(); // (ossName + "(" + avoidNull(bean.getOssVersion()) + ")").toUpperCase();
					String spdxRefId = "";
					
					if (ossName.equals("-")) {
						spdxRefId = "SPDXRef-File-" + bean.getComponentId();
						cellSPDXIdentifier.setCellValue("SPDXRef-File-" + bean.getComponentId());
						packageInfoidentifierList.add("SPDXRef-File-" + bean.getComponentId());
					} else {
						spdxRefId = "SPDXRef-Package-" + bean.getOssId();
						cellSPDXIdentifier.setCellValue("SPDXRef-Package-" + bean.getOssId());
						packageInfoidentifierList.add("SPDXRef-Package-" + bean.getOssId());
					}

					if (!isEmpty(relationshipsKey)) {
						relationshipsMap.put(relationshipsKey, spdxRefId);
						externalRefsMap.put(spdxRefId, relationshipsKey);
					}
					
					// Package Version
					Cell cellPackageVersion = getCell(row, cellIdx); cellIdx++;
					cellPackageVersion.setCellValue(hideOssVersionFlag ? "" : avoidNull(bean.getOssVersion()));

					// Package FileName
					cellIdx++;

					// Package Supplier
					Cell packageSupplier = getCell(row, cellIdx); cellIdx++;
					packageSupplier.setCellValue("Person: \"\"");

					// Package Originator
					Cell packageOriginator = getCell(row, cellIdx); cellIdx++;
					packageOriginator.setCellValue("Organization: \"\"");

					// Home Page
					Cell cellHomePage = getCell(row, cellIdx); cellIdx++;
					cellHomePage.setCellValue(avoidNull(bean.getHomepage()));

					// Package Download Location
					Cell cellPackageDownloadLocation = getCell(row, cellIdx); cellIdx++;
					String downloadLocation = bean.getDownloadLocation();

					if (isEmpty(downloadLocation)) {
						downloadLocation = "NONE";
					}

					// Invalid download location is output as NONE
					if (SpdxVerificationHelper.verifyDownloadLocation(downloadLocation) != null) {
						downloadLocation = "NONE";
					}

					cellPackageDownloadLocation.setCellValue(downloadLocation);

					// Package Checksum
					cellIdx++;

					// Package Verification Code
					cellIdx++;

					// Verification Code Excluded Files
					cellIdx++;

					// Source Info
					cellIdx++;

					// License Declared
					Cell cellLicenseDeclared = getCell(row, cellIdx); cellIdx++;

					OssMaster _ossBean = null;
					if (ossName.equals("-")) {
						String licenseStr = CommonFunction.licenseStrToSPDXLicenseFormat(bean.getLicenseName());
						cellLicenseDeclared.setCellValue(licenseStr);
						attributionText = bean.getAttribution();
					} else {
						_ossBean = CoCodeManager.OSS_INFO_UPPER.get( (ossName + "_" + avoidNull(bean.getOssVersion())).toUpperCase());
						String licenseStr = CommonFunction.makeLicenseExpression(_ossBean.getOssLicenses(), false, true);
						if (licenseStr.contains("LicenseRef-")) {
							licenseStr = CommonFunction.removeSpecialCharacters(licenseStr, false).replaceAll("\\(", "-").replaceAll("\\)", "");
						}
						if (_ossBean.getOssLicenses().size() > 1) {
							licenseStr = "(" + licenseStr + ")";
						}
						cellLicenseDeclared.setCellValue(CommonFunction.licenseStrToSPDXLicenseFormat(licenseStr));
						attributionText = avoidNull(_ossBean.getAttribution()); // oss attribution
					}

					// License Concluded
					Cell cellLicenseConcluded = getCell(row, cellIdx); cellIdx++;
					String srtLicenseName = "";

					for (OssComponentsLicense liBean : bean.getOssComponentsLicense()) {
						if (!isEmpty(srtLicenseName)) {
							srtLicenseName += " AND ";
						}

						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(liBean.getLicenseName()).toUpperCase())) {
							LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(liBean.getLicenseName()).toUpperCase());

							if (!isEmpty(liMaster.getShortIdentifier())) {
								liBean.setLicenseName(liMaster.getShortIdentifier());
							} else {
								liBean.setLicenseName("LicenseRef-" + liBean.getLicenseName());
							}

							if (!isEmpty(attributionText)) {
								attributionText += "\n";
							}

							attributionText += avoidNull(liMaster.getAttribution()); // license attribution
						}
						
						if (liBean.getLicenseName().startsWith("LicenseRef-")) {
							liBean.setLicenseName(CommonFunction.removeSpecialCharacters(liBean.getLicenseName(), true).replaceAll("\\(", "-").replaceAll("\\)", ""));
						}
						
						srtLicenseName += liBean.getLicenseName();
					}

					if (!bean.getOssComponentsLicense().isEmpty() && bean.getOssComponentsLicense().size() > 1) {
						srtLicenseName = "(" + srtLicenseName + ")";
					}

					cellLicenseConcluded.setCellValue(CommonFunction.licenseStrToSPDXLicenseFormat(srtLicenseName));

					// License Info From Files
					Cell licenseInfoFromFiles = getCell(row, cellIdx); cellIdx++;

					if (ossName.equals("-")) {
						licenseInfoFromFiles.setCellValue(CommonFunction.licenseStrToSPDXLicenseFormat(bean.getLicenseName()));
					} else {
						licenseInfoFromFiles.setCellValue(CommonFunction.makeLicenseFromFiles(_ossBean, true)); // Declared & Detected License Info (중복제거)
					}

					// License Comments
					cellIdx++;

					// Package Copyright Text
					Cell cellPackageCopyrightText = getCell(row, cellIdx); cellIdx++;
					String copyrightText = StringUtil.substring(CommonFunction.brReplaceToLine(bean.getCopyrightText()), 0, 32762);

					if (copyrightText.isEmpty() || copyrightText.equals("-")) {
						copyrightText = "NOASSERTION";
					}

					cellPackageCopyrightText.setCellValue(copyrightText);


					// Summary
					cellIdx++;

					// Description
					cellIdx++;


					// Attribution Text
					Cell attributionInfo = getCell(row, cellIdx); cellIdx++;
					attributionInfo.setCellValue(hideOssVersionFlag ? bean.getOssAttribution().replaceAll("<br>", "\n") : attributionText);

					// Files Analyzed
					Cell filesAnalyzed = getCell(row, cellIdx); cellIdx++;
					filesAnalyzed.setCellValue("false");

					// User Defined Columns...

					rowIdx++;
				}
			}

			// External Refs
			{
				if (!externalRefsMap.isEmpty()) {
					int rowIdx = 1;

					for (String key : externalRefsMap.keySet()) {
						Row row = sheetExternalRefs.getRow(rowIdx);

						if (row == null) {
							row = sheetExternalRefs.createRow(rowIdx);
						}
									
						int cellIdx = 0;

						// Package ID
						Cell cellPackageId = getCell(row, cellIdx); cellIdx++;
						cellPackageId.setCellValue(key);
									
						// Category
						Cell cellCategory = getCell(row, cellIdx); cellIdx++;
						cellCategory.setCellValue("PACKAGE_MANAGER");
									
						// Type
						Cell cellType = getCell(row, cellIdx); cellIdx++;
						cellType.setCellValue("purl");
									
						// Locator
						Cell cellLocator = getCell(row, cellIdx); cellIdx++;
						cellLocator.setCellValue(externalRefsMap.get(key));
									
						// Comment
						cellIdx++;
									
						// User Defined
						cellIdx++;
						
						rowIdx++;
					}
				}
			}
			
			// Extracted License Info
			{
				// BOM에 사용된 OSS Info중 License identifier가 설정되어 있지 않은 license 정보만 출력한다.
				List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("noticeObligationList");
				Map<String, LicenseMaster> nonIdetifierNoticeList = new HashMap<>();

				for (OssComponents ocBean : noticeList) {
					String ossName = ocBean.getOssName().replace("&#39;", "\'");

					List<String> licenseList = new ArrayList<>();
					if (ossName.equals("-")) {
						licenseList = Arrays.asList(ocBean.getLicenseName());
					} else {
						OssMaster _ossBean = CoCodeManager.OSS_INFO_UPPER.get((ossName + "_" + avoidNull(ocBean.getOssVersion())).toUpperCase());
						licenseList = Arrays.asList(CommonFunction.makeLicenseFromFiles(_ossBean, false).split(","));
					}

					for (String licenseNm : licenseList) {
						LicenseMaster lmBean = CoCodeManager.LICENSE_INFO.get(licenseNm);
						if (lmBean != null && isEmpty(lmBean.getShortIdentifier()) && !nonIdetifierNoticeList.containsKey(lmBean.getLicenseId())) {
							nonIdetifierNoticeList.put(lmBean.getLicenseId(), lmBean);
						}
					}
				}

				int rowIdx = 1;

				for (LicenseMaster bean : nonIdetifierNoticeList.values()) {
					int cellIdx = 0;

					Row row = sheetLicense.getRow(rowIdx);

					if (row == null) {
						row = sheetLicense.createRow(rowIdx);
					}

					String _licenseName = bean.getLicenseNameTemp().replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-");

					// Identifier
					Cell cellIdentifier = getCell(row, cellIdx); cellIdx++;
					cellIdentifier.setCellValue("LicenseRef-" + _licenseName);

					// Extracted Text
					Cell cellExtractedText = getCell(row, cellIdx); cellIdx++;
					cellExtractedText.setCellValue(StringUtil.substring(CommonFunction.brReplaceToLine(bean.getLicenseText()), 0, 32762) );

					// License Name
					Cell cellLicenseName = getCell(row, cellIdx); cellIdx++;
					cellLicenseName.setCellValue(bean.getLicenseNameTemp());

					// Cross Reference URLs
					Cell cellCrossReferenceURLs = getCell(row, cellIdx); cellIdx++;
					boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
					cellCrossReferenceURLs.setCellValue(avoidNull(CommonFunction.makeLicenseInternalUrl(bean, distributionFlag)));

					// Comment
					rowIdx ++;
				}
			}

			// Per File Info sheet
			{
				// oss name이 "-" 인 case 만
				List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("addOssComponentList");
				List<OssComponents> nonIdetifierNoticeList = new ArrayList<>();

				for (OssComponents bean : noticeList) {
					// set false because "Per file info sheet" is not currently output
					if ("-".equals(bean.getOssName()) && false) {
						nonIdetifierNoticeList.add(bean);
					}
				}

				int rowIdx = 1;

				for (OssComponents bean : nonIdetifierNoticeList) {
					int cellIdx = 0;
					String attributionText = "";
					Row row = sheetPerFile.getRow(rowIdx);

					if (row == null) {
						row = sheetPerFile.createRow(rowIdx);
					}

					// File Name
					Cell fileName = getCell(row, cellIdx); cellIdx++;
					fileName.setCellValue(avoidNull(bean.getFilePath(), "./"));

					// SPDX Identifier
					Cell sPDXIdentifier = getCell(row, cellIdx); cellIdx++;
					sPDXIdentifier.setCellValue("SPDXRef-File-" + bean.getComponentId());

					// Package Identifier
					cellIdx++;

					// File Type(s)
					Cell fileType = getCell(row, cellIdx); cellIdx++;
					fileType.setCellValue("SOURCE");

					// File Checksum(s)
					cellIdx++;

					// License Concluded
					Cell cellLicenseConcluded = getCell(row, cellIdx); cellIdx++;
					String srtLicenseName = "";

					for (OssComponentsLicense liBean : bean.getOssComponentsLicense()) {
						if (!isEmpty(srtLicenseName)) {
							srtLicenseName += " AND ";
						}
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(liBean.getLicenseName()).toUpperCase())) {
							LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(liBean.getLicenseName()).toUpperCase());

							if (!isEmpty(liMaster.getShortIdentifier())) {
								liBean.setLicenseName(liMaster.getShortIdentifier());
							} else {
								liBean.setLicenseName("LicenseRef-" + liBean.getLicenseName());
							}

							if (!isEmpty(attributionText)) {
								attributionText += "\n";
							}

							attributionText += avoidNull(liMaster.getAttribution());
						}

						liBean.setLicenseName(liBean.getLicenseName().replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-"));

						srtLicenseName += liBean.getLicenseName();
					}

					if (!bean.getOssComponentsLicense().isEmpty() && bean.getOssComponentsLicense().size() > 1) {
						srtLicenseName = "(" + srtLicenseName + ")";
					}

					cellLicenseConcluded.setCellValue(srtLicenseName);

					// License Info From Files
					Cell licenseInfoFromFiles = getCell(row, cellIdx); cellIdx++;
					licenseInfoFromFiles.setCellValue(srtLicenseName); // License Concluded 란과 동일한 값으로 표시

					// License Comments
					cellIdx++;

					// File Copyright Text
					Cell fileCopyrightText = getCell(row, cellIdx); cellIdx++;
					fileCopyrightText.setCellValue(StringUtil.substring(CommonFunction.brReplaceToLine(bean.getCopyrightText()), 0, 32762) );

					// Notice Text
					cellIdx++;

					// Artifact of Project
					cellIdx++;

					// Artifact of Homepage
					cellIdx++;

					// Artifact of URL
					cellIdx++;

					// Contributors
					cellIdx++;

					// File Comment
					cellIdx++;

					// File Dependencies
					cellIdx++;

					// Attrinbution Info
					Cell attributionInfo = getCell(row, cellIdx); cellIdx++;
					attributionInfo.setCellValue(attributionText);

					// User Defined Columns...
					cellIdx++;
					rowIdx ++;
				}
			}

			// sheetRelationships
			{
				int rowIdx = 1;

				for (String _identifierB : packageInfoidentifierList) {
					int cellIdx = 0;

					Row row = sheetRelationships.getRow(rowIdx);
					if (row == null) {
						row = sheetRelationships.createRow(rowIdx);
					}
					// SPDX Identifier A
					Cell spdxIdentifierA = getCell(row, cellIdx); cellIdx++;
					spdxIdentifierA.setCellValue("SPDXRef-DOCUMENT");

					// Relationship
					Cell relationship = getCell(row, cellIdx); cellIdx++;
					relationship.setCellValue("DESCRIBES");

					// SPDX Identifier B
					Cell spdxIdentifierB = getCell(row, cellIdx); cellIdx++;
					spdxIdentifierB.setCellValue(_identifierB);

					rowIdx++;
				}
				
				List<String> duplicateCheckList = new ArrayList<>();
				for (OssComponents oss : dependenciesDataList) {
					String key = oss.getPackageUrl(); // (oss.getOssName() + "(" + oss.getOssVersion() + ")").toUpperCase();
					if (relationshipsMap.containsKey(key)) {
						String spdxElementId = (String) relationshipsMap.get(key);
						String[] dependencies = oss.getDependencies().split(",");
						for (String dependency : dependencies) {
							if (relationshipsMap.containsKey(dependency)) {
								String relatedSpdxElement = (String) relationshipsMap.get(dependency);
								String duplicateKey = spdxElementId + "|" + relatedSpdxElement;
								if (duplicateCheckList.contains(duplicateKey)) {
									continue;
								} else {
									duplicateCheckList.add(duplicateKey);
								}
								
								int cellIdx = 0;

								Row row = sheetRelationships.getRow(rowIdx);
								if (row == null) {
									row = sheetRelationships.createRow(rowIdx);
								}
								// SPDX Identifier A
								Cell spdxIdentifierA = getCell(row, cellIdx); cellIdx++;
								spdxIdentifierA.setCellValue(spdxElementId);

								// Relationship
								Cell relationship = getCell(row, cellIdx); cellIdx++;
								relationship.setCellValue("DEPENDS_ON");

								// SPDX Identifier B
								Cell spdxIdentifierB = getCell(row, cellIdx); cellIdx++;
								spdxIdentifierB.setCellValue(relatedSpdxElement);

								rowIdx++;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e) {}
			}
		}

		return makeExcelFileId(wb,downloadFileName, "xls");
	}
	
	@SuppressWarnings("unchecked")
	private static String getSBOMSPDX_SpreadSheetExcelPost(String dataStr) throws IOException {
		Workbook wb = null;
		Sheet sheetDoc = null; // Document info
		Sheet sheetPackage = null; // Package Info
		Sheet sheetExternalRefs = null;
		Sheet sheetLicense = null; // Extracted License Info
		Sheet sheetPerFile = null; // Per File Info
		Sheet sheetRelationships = null; // Relationships
		FileInputStream inFile=null;

		// download file name
		String downloadFileName = "SPDXRdf-"; // Default

		String prjId = dataStr;
		boolean thirdPartyCheckFlag = false;
		
		if (prjId.startsWith("3rd_")) {
			thirdPartyCheckFlag = true;
			String[] prjIdSplit = dataStr.split("_");
			prjId = prjIdSplit[1];
		}
		
		OssNotice ossNotice = new OssNotice();
		ossNotice.setPrjId(dataStr);
		ossNotice.setFileType("text");

		try {
			inFile= new FileInputStream(new File(downloadpath+"/SPDXRdf_2.2.2.xls"));

			wb = WorkbookFactory.create(inFile);
			sheetDoc = wb.getSheetAt(0);
			sheetPackage = wb.getSheetAt(1);
			sheetExternalRefs = wb.getSheetAt(2);
			sheetLicense = wb.getSheetAt(3);
			sheetPerFile = wb.getSheetAt(4);
			sheetRelationships = wb.getSheetAt(5);

			String createdTime = CommonFunction.getCurrentDateTime("yyyyMMddhhmm");
			String createdTimeFull = CommonFunction.getCurrentDateTime("yyyy-MM-dd hh:mm:ss");
			Date createdDateTime = DateUtil.getCurrentDate();

			List<OssComponents> dependenciesDataList = null;
			Map<String, Object> packageInfo = null;
			Map<String, Object> relationshipsMap = new HashMap<>();
			
			String strPrjName = "";
			String creator = "";
			T2Users userInfo = new T2Users();
			
			if (!thirdPartyCheckFlag) {
				Project projectInfo = new Project();
				projectInfo.setPrjId(prjId);
				projectInfo = projectService.getProjectDetail(projectInfo);
				
				creator = projectInfo.getCreator();
				strPrjName = projectInfo.getPrjName();
				if (!isEmpty(projectInfo.getPrjVersion())) {
					strPrjName += "-" + projectInfo.getPrjVersion();
				}
				
				ossNotice.setRefDiv(projectInfo.getReferenceDiv());
				packageInfo = projectService.getExportDataForSBOMInfo(ossNotice);
				
				if (CoConstDef.CD_DTL_COMPONENT_ID_BOM.equals(projectInfo.getReferenceDiv())) {
					projectInfo.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_DEP);
					dependenciesDataList = projectService.getDependenciesDataList(projectInfo);
				}
			} else {
				PartnerMaster partner = new PartnerMaster();
				partner.setPartnerId(prjId);
				partner = partnerService.getPartnerMasterOne(partner);
				
				creator = partner.getCreator();
				strPrjName = partner.getPartnerName();
				
				packageInfo = partnerService.getExportDataForSbomInfo(partner);
			}
			
			userInfo.setUserId(creator);
			downloadFileName += FileUtil.makeValidFileName(strPrjName, "_").replaceAll(" ", "").replaceAll("--", "-");

			List<String> packageInfoidentifierList = new ArrayList<>();
			Map<String, String> externalRefsMap = new HashMap<>();

			//Document Info
			{
				Row row = sheetDoc.getRow(1);

				int cellIdx = 0;
				// Spreadsheet Version
				cellIdx ++;
				// SPDX Version
				cellIdx ++;
				// Data License
				cellIdx ++;

				// SPDX Identifier
				cellIdx++;

				// License List Version
				cellIdx ++;

				// Document Name
				Cell cellDocumentName = getCell(row, cellIdx); cellIdx++;
				cellDocumentName.setCellValue(strPrjName);

				// Document Namespace
				Cell cellDocumentNamespace = getCell(row, cellIdx); cellIdx++;
				String spdxidentifier = "SPDXRef-" + strPrjName.replaceAll(" ", "") + "-" + createdTime;
				String domain = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org/");
				if (!domain.endsWith("/")) {
					domain += "/";
				}
				cellDocumentNamespace.setCellValue(domain + spdxidentifier);

				// Document Contents
				cellIdx++;

				//External Document References
				cellIdx ++;
				// Document Comment
				cellIdx ++;

				// Creator
				Cell cellCreator = getCell(row, cellIdx); cellIdx++;
				String strCreator = "Person: ";
				userInfo = userService.getUser(userInfo);
				strCreator += creator + " (" + userInfo.getEmail() + ")";
				cellCreator.setCellValue(strCreator);

				// Created
				Cell cellCreated = getCell(row, cellIdx); cellIdx++;
				cellCreated.setCellValue(createdDateTime);

				// Creator Comment
			}

			// Package Info
			{
				List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("noticeObligationList");

				List<OssComponents> sourceList = (List<OssComponents>) packageInfo.get("disclosureObligationList");

				boolean hideOssVersionFlag = CoConstDef.FLAG_YES.equals(ossNotice.getHideOssVersionYn());

				if (sourceList != null && !sourceList.isEmpty()) {
					noticeList.addAll(sourceList);
				}

				if (packageInfo.containsKey("notObligationList")) {
					List<OssComponents> notObligationList = (List<OssComponents>) packageInfo.get("notObligationList");
					if (notObligationList != null && !notObligationList.isEmpty()) {
						noticeList.addAll(notObligationList);
					}
				}
				
//				noticeList = verificationService.setMergeGridData(noticeList); // merge Data

				int rowIdx = 1;

				for (OssComponents bean : noticeList) {
					Row row = sheetPackage.getRow(rowIdx);

					if (row == null) {
						row = sheetPackage.createRow(rowIdx);
					}

					String attributionText = "";

					int cellIdx = 0;

					// Package Name
					Cell cellPackageName = getCell(row, cellIdx); cellIdx++;
					cellPackageName.setCellValue(bean.getOssName());

					// SPDX Identifier
					Cell cellSPDXIdentifier = getCell(row, cellIdx); cellIdx++;
					String ossName = bean.getOssName().replace("&#39;", "\'");

//					String relationshipsKey = (ossName + "(" + avoidNull(bean.getOssVersion()) + ")").toUpperCase();
					String relationshipsKey = bean.getPackageUrl();
					String spdxRefId = "";
					
					if (ossName.equals("-") || (bean.getOssId() == null || bean.getOssId().isEmpty())) {
						spdxRefId = "SPDXRef-File-";
					} else {
						spdxRefId = "SPDXRef-Package-";
					}
					spdxRefId += bean.getComponentId();
					
					cellSPDXIdentifier.setCellValue(spdxRefId);
					packageInfoidentifierList.add(spdxRefId);
					if (!isEmpty(relationshipsKey)) {
						relationshipsMap.put(relationshipsKey, spdxRefId);
						externalRefsMap.put(spdxRefId, relationshipsKey);
					}
						
					// Package Version
					Cell cellPackageVersion = getCell(row, cellIdx); cellIdx++;
					cellPackageVersion.setCellValue(hideOssVersionFlag ? "" : avoidNull(bean.getOssVersion()));

					// Package FileName
					cellIdx++;

					// Package Supplier
					Cell packageSupplier = getCell(row, cellIdx); cellIdx++;
					packageSupplier.setCellValue("Person: \"\"");

					// Package Originator
					Cell packageOriginator = getCell(row, cellIdx); cellIdx++;
					packageOriginator.setCellValue("Organization: \"\"");

					// Home Page
					Cell cellHomePage = getCell(row, cellIdx); cellIdx++;
					cellHomePage.setCellValue(avoidNull(bean.getHomepage()));

					// Package Download Location
					Cell cellPackageDownloadLocation = getCell(row, cellIdx); cellIdx++;
					String downloadLocation = bean.getDownloadLocation();

					if (isEmpty(downloadLocation)) {
						downloadLocation = "NONE";
					}

					// Invalid download location is output as NONE
					if (SpdxVerificationHelper.verifyDownloadLocation(downloadLocation) != null) {
						downloadLocation = "NONE";
					}

					cellPackageDownloadLocation.setCellValue(downloadLocation);

					// Package Checksum
					cellIdx++;

					// Package Verification Code
					cellIdx++;

					// Verification Code Excluded Files
					cellIdx++;

					// Source Info
					cellIdx++;

					// License Declared
					Cell cellLicenseDeclared = getCell(row, cellIdx); cellIdx++;

					OssMaster _ossBean = null;
					if (ossName.equals("-")) {
						String licenseStr = CommonFunction.licenseStrToSPDXLicenseFormat(bean.getLicenseName());
						cellLicenseDeclared.setCellValue(CommonFunction.removeSpecialCharacters(licenseStr, true).replaceAll("\\(", "-").replaceAll("\\)", ""));
						attributionText = bean.getAttribution();
					} else {
						_ossBean = CoCodeManager.OSS_INFO_UPPER.get( (ossName + "_" + avoidNull(bean.getOssVersion())).toUpperCase());
						if (_ossBean != null) {
							String licenseStr = CommonFunction.makeLicenseExpression(_ossBean.getOssLicenses(), false, true);
							licenseStr = CommonFunction.removeSpecialCharacters(licenseStr, false).replaceAll("\\(", "-").replaceAll("\\)", "");
							if (licenseStr.contains("++")) {
								licenseStr = licenseStr.replaceAll("\\++", "\\+");
							}
							if (_ossBean.getOssLicenses().size() > 1) {
								licenseStr = "(" + licenseStr + ")";
							}

							cellLicenseDeclared.setCellValue(licenseStr);
							attributionText = avoidNull(_ossBean.getAttribution()); // oss attribution
						} else {
							boolean multiFlag = false;
							String licenseStr = "";
							
							if (bean.getLicenseName().contains(",")) {
								multiFlag = true;
								for (String license : bean.getLicenseName().split(",")) {
									if (!isEmpty(license)) {
										licenseStr += " AND ";
									}
									
									String licenseName = "";
									
									if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(license).toUpperCase())) {
										LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(license).toUpperCase());
										
										if (!isEmpty(liMaster.getShortIdentifier())) {
											licenseName = liMaster.getShortIdentifier();
										} else {
											licenseName = "LicenseRef-" + license;
										}
									} else {
										licenseName = "LicenseRef-" + license;
									}
									
									if (licenseName.startsWith("LicenseRef-")) {
										licenseName = CommonFunction.removeSpecialCharacters(licenseName, true).replaceAll("\\(", "-").replaceAll("\\)", "");
									}
									
									licenseStr += licenseName;
								}
							} else {
								if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(bean.getLicenseName()).toUpperCase())) {
									licenseStr = CommonFunction.licenseStrToSPDXLicenseFormat(bean.getLicenseName());
								} else {
									licenseStr = "LicenseRef-" + CommonFunction.removeSpecialCharacters(bean.getLicenseName(), true).replaceAll("\\(", "-").replaceAll("\\)", "");
								}
							}
							if (licenseStr.contains("++")) {
								licenseStr = licenseStr.replaceAll("\\++", "\\+");
							}
							if (multiFlag) {
								licenseStr = "(" + licenseStr + ")";
							}
							cellLicenseDeclared.setCellValue(licenseStr);
							attributionText = bean.getAttribution();
						}
					}

					// License Concluded
					Cell cellLicenseConcluded = getCell(row, cellIdx); cellIdx++;
					String srtLicenseName = "";

					for (OssComponentsLicense liBean : bean.getOssComponentsLicense()) {
						if (!isEmpty(srtLicenseName)) {
							srtLicenseName += " AND ";
						}

						String licenseName = "";
						
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(liBean.getLicenseName()).toUpperCase())) {
							LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(liBean.getLicenseName()).toUpperCase());

							if (!isEmpty(liMaster.getShortIdentifier())) {
								licenseName = liMaster.getShortIdentifier();
							} else {
								licenseName = "LicenseRef-" + liBean.getLicenseName();
							}

							if (!isEmpty(attributionText)) {
								attributionText += "\n";
							}

							attributionText += avoidNull(liMaster.getAttribution()); // license attribution
						} else {
							licenseName = "LicenseRef-" + liBean.getLicenseName();
						}
						
						if (licenseName.startsWith("LicenseRef-")) {
							licenseName = CommonFunction.removeSpecialCharacters(licenseName, true).replaceAll("\\(", "-").replaceAll("\\)", "");
						}
						srtLicenseName += licenseName;
					}

					if (!bean.getOssComponentsLicense().isEmpty() && bean.getOssComponentsLicense().size() > 1) {
						srtLicenseName = "(" + srtLicenseName + ")";
					}
					if (srtLicenseName.contains("++")) {
						srtLicenseName = srtLicenseName.replaceAll("\\++", "\\+");
					}
					cellLicenseConcluded.setCellValue(srtLicenseName);

					// License Info From Files
					Cell licenseInfoFromFiles = getCell(row, cellIdx); cellIdx++;

					if (ossName.equals("-")) {
						String licenseInfoFromFilesString = CommonFunction.licenseStrToSPDXLicenseFormat(CommonFunction.removeSpecialCharacters(bean.getLicenseName(), true).replaceAll("\\(", "-").replaceAll("\\)", ""));
						if (licenseInfoFromFilesString.contains("++")) {
							licenseInfoFromFilesString = licenseInfoFromFilesString.replaceAll("\\++", "\\+");
						}
						licenseInfoFromFiles.setCellValue(licenseInfoFromFilesString);
					} else if (_ossBean != null) {
						String licenseInfo = CommonFunction.makeLicenseFromFiles(_ossBean, true);
						if (licenseInfo.contains("++")) {
							licenseInfo = licenseInfo.replaceAll("\\++", "\\+");
						}
						licenseInfoFromFiles.setCellValue(licenseInfo);
					} else {
						licenseInfoFromFiles.setCellValue(""); // OSS Info가 없으므로 빈값이 들어감.
					}

					// License Comments
					cellIdx++;

					// Package Copyright Text
					Cell cellPackageCopyrightText = getCell(row, cellIdx); cellIdx++;
					String copyrightText = StringUtil.substring(CommonFunction.brReplaceToLine(bean.getCopyrightText()), 0, 32762);

					if (copyrightText.isEmpty() || copyrightText.equals("-")) {
						copyrightText = "NOASSERTION";
					}

					cellPackageCopyrightText.setCellValue(copyrightText);


					// Summary
					cellIdx++;

					// Description
					cellIdx++;


					// Attribution Text
					Cell attributionInfo = getCell(row, cellIdx); cellIdx++;
					attributionInfo.setCellValue(hideOssVersionFlag ? bean.getOssAttribution().replaceAll("<br>", "\n") : attributionText);

					// Files Analyzed
					Cell filesAnalyzed = getCell(row, cellIdx); cellIdx++;
					filesAnalyzed.setCellValue("false");

					// User Defined Columns...

					rowIdx++;
				}
			}

			// External Refs
			{
				if (!externalRefsMap.isEmpty()) {
					int rowIdx = 1;

					for (String key : externalRefsMap.keySet()) {
						Row row = sheetExternalRefs.getRow(rowIdx);

						if (row == null) {
							row = sheetExternalRefs.createRow(rowIdx);
						}
									
						int cellIdx = 0;

						// Package ID
						Cell cellPackageId = getCell(row, cellIdx); cellIdx++;
						cellPackageId.setCellValue(key);
									
						// Category
						Cell cellCategory = getCell(row, cellIdx); cellIdx++;
						cellCategory.setCellValue("PACKAGE_MANAGER");
									
						// Type
						Cell cellType = getCell(row, cellIdx); cellIdx++;
						cellType.setCellValue("purl");
									
						// Locator
						Cell cellLocator = getCell(row, cellIdx); cellIdx++;
						cellLocator.setCellValue(externalRefsMap.get(key));
									
						// Comment
						cellIdx++;
									
						// User Defined
						cellIdx++;
						
						rowIdx++;
					}
				}
			}
			
			// Extracted License Info
			{
				// BOM에 사용된 OSS Info중 License identifier가 설정되어 있지 않은 license 정보만 출력한다.
				List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("noticeObligationList");
				Map<String, LicenseMaster> nonIdetifierNoticeList = new HashMap<>();

				for (OssComponents ocBean : noticeList) {
					String ossName = ocBean.getOssName().replace("&#39;", "\'");

					List<String> licenseList = new ArrayList<>();
					if (ossName.equals("-")) {
						licenseList = Arrays.asList(ocBean.getLicenseName());
					} else {
						OssMaster _ossBean = CoCodeManager.OSS_INFO_UPPER.get((ossName + "_" + avoidNull(ocBean.getOssVersion())).toUpperCase());
						licenseList = Arrays.asList(CommonFunction.makeLicenseFromFiles(_ossBean, false).split(","));
					}

					for (String licenseNm : licenseList) {
						LicenseMaster lmBean = CoCodeManager.LICENSE_INFO.get(licenseNm);
						if (lmBean != null && isEmpty(lmBean.getShortIdentifier()) && !nonIdetifierNoticeList.containsKey(lmBean.getLicenseId())) {
							nonIdetifierNoticeList.put(lmBean.getLicenseId(), lmBean);
						}
					}
				}

				int rowIdx = 1;

				for (LicenseMaster bean : nonIdetifierNoticeList.values()) {
					int cellIdx = 0;

					Row row = sheetLicense.getRow(rowIdx);

					if (row == null) {
						row = sheetLicense.createRow(rowIdx);
					}

					String _licenseName = bean.getLicenseNameTemp().replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-");
					if (_licenseName.contains("++")) {
						_licenseName = _licenseName.replaceAll("\\++", "\\+");
					}
					// Identifier
					Cell cellIdentifier = getCell(row, cellIdx); cellIdx++;
					cellIdentifier.setCellValue("LicenseRef-" + _licenseName);

					// Extracted Text
					Cell cellExtractedText = getCell(row, cellIdx); cellIdx++;
					cellExtractedText.setCellValue(StringUtil.substring(CommonFunction.brReplaceToLine(bean.getLicenseText()), 0, 32762) );

					// License Name
					Cell cellLicenseName = getCell(row, cellIdx); cellIdx++;
					cellLicenseName.setCellValue(bean.getLicenseNameTemp());

					// Cross Reference URLs
					Cell cellCrossReferenceURLs = getCell(row, cellIdx); cellIdx++;
					boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
					cellCrossReferenceURLs.setCellValue(avoidNull(CommonFunction.makeLicenseInternalUrl(bean, distributionFlag)));

					// Comment
					rowIdx ++;
				}
			}

			// Per File Info sheet
			{
				// oss name이 "-" 인 case 만
				List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("addOssComponentList");
				List<OssComponents> nonIdetifierNoticeList = new ArrayList<>();

				for (OssComponents bean : noticeList) {
					// set false because "Per file info sheet" is not currently output
					if ("-".equals(bean.getOssName()) && false) {
						nonIdetifierNoticeList.add(bean);
					}
				}

				int rowIdx = 1;

				for (OssComponents bean : nonIdetifierNoticeList) {
					int cellIdx = 0;
					String attributionText = "";
					Row row = sheetPerFile.getRow(rowIdx);

					if (row == null) {
						row = sheetPerFile.createRow(rowIdx);
					}

					// File Name
					Cell fileName = getCell(row, cellIdx); cellIdx++;
					fileName.setCellValue(avoidNull(bean.getFilePath(), "./"));

					// SPDX Identifier
					Cell sPDXIdentifier = getCell(row, cellIdx); cellIdx++;
					sPDXIdentifier.setCellValue("SPDXRef-File-" + bean.getComponentId());

					// Package Identifier
					cellIdx++;

					// File Type(s)
					Cell fileType = getCell(row, cellIdx); cellIdx++;
					fileType.setCellValue("SOURCE");

					// File Checksum(s)
					cellIdx++;

					// License Concluded
					Cell cellLicenseConcluded = getCell(row, cellIdx); cellIdx++;
					String srtLicenseName = "";

					for (OssComponentsLicense liBean : bean.getOssComponentsLicense()) {
						if (!isEmpty(srtLicenseName)) {
							srtLicenseName += " AND ";
						}
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(liBean.getLicenseName()).toUpperCase())) {
							LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(liBean.getLicenseName()).toUpperCase());

							if (!isEmpty(liMaster.getShortIdentifier())) {
								liBean.setLicenseName(liMaster.getShortIdentifier());
							} else {
								liBean.setLicenseName("LicenseRef-" + liBean.getLicenseName());
							}

							if (!isEmpty(attributionText)) {
								attributionText += "\n";
							}

							attributionText += avoidNull(liMaster.getAttribution());
						}

						liBean.setLicenseName(liBean.getLicenseName().replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-"));

						srtLicenseName += liBean.getLicenseName();
					}

					if (!bean.getOssComponentsLicense().isEmpty() && bean.getOssComponentsLicense().size() > 1) {
						srtLicenseName = "(" + srtLicenseName + ")";
					}

					cellLicenseConcluded.setCellValue(srtLicenseName);

					// License Info From Files
					Cell licenseInfoFromFiles = getCell(row, cellIdx); cellIdx++;
					licenseInfoFromFiles.setCellValue(srtLicenseName); // License Concluded 란과 동일한 값으로 표시

					// License Comments
					cellIdx++;

					// File Copyright Text
					Cell fileCopyrightText = getCell(row, cellIdx); cellIdx++;
					fileCopyrightText.setCellValue(StringUtil.substring(CommonFunction.brReplaceToLine(bean.getCopyrightText()), 0, 32762) );

					// Notice Text
					cellIdx++;

					// Artifact of Project
					cellIdx++;

					// Artifact of Homepage
					cellIdx++;

					// Artifact of URL
					cellIdx++;

					// Contributors
					cellIdx++;

					// File Comment
					cellIdx++;

					// File Dependencies
					cellIdx++;

					// Attrinbution Info
					Cell attributionInfo = getCell(row, cellIdx); cellIdx++;
					attributionInfo.setCellValue(attributionText);

					// User Defined Columns...
					cellIdx++;
					rowIdx ++;
				}
			}

			// sheetRelationships
			{
				if (dependenciesDataList != null) {
					int rowIdx = 1;

					for (String _identifierB : packageInfoidentifierList) {
						int cellIdx = 0;

						Row row = sheetRelationships.getRow(rowIdx);
						if (row == null) {
							row = sheetRelationships.createRow(rowIdx);
						}
						// SPDX Identifier A
						Cell spdxIdentifierA = getCell(row, cellIdx); cellIdx++;
						spdxIdentifierA.setCellValue("SPDXRef-DOCUMENT");

						// Relationship
						Cell relationship = getCell(row, cellIdx); cellIdx++;
						relationship.setCellValue("DESCRIBES");

						// SPDX Identifier B
						Cell spdxIdentifierB = getCell(row, cellIdx); cellIdx++;
						spdxIdentifierB.setCellValue(_identifierB);

						rowIdx++;
					}
					
					for (OssComponents oss : dependenciesDataList) {
						String key = oss.getPackageUrl();
						if (!isEmpty(key) && relationshipsMap.containsKey(key)) {
							String spdxElementId = (String) relationshipsMap.get(key);
							String[] dependencies = oss.getDependencies().split(",");
							for (String dependency : dependencies) {
								String relatedSpdxElementKey = dependency;
								if (relationshipsMap.containsKey(relatedSpdxElementKey)) {
									String relatedSpdxElement = (String) relationshipsMap.get(relatedSpdxElementKey);
									int cellIdx = 0;

									Row row = sheetRelationships.getRow(rowIdx);
									if (row == null) {
										row = sheetRelationships.createRow(rowIdx);
									}
									// SPDX Identifier A
									Cell spdxIdentifierA = getCell(row, cellIdx); cellIdx++;
									spdxIdentifierA.setCellValue(spdxElementId);

									// Relationship
									Cell relationship = getCell(row, cellIdx); cellIdx++;
									relationship.setCellValue("DEPENDS_ON");

									// SPDX Identifier B
									Cell spdxIdentifierB = getCell(row, cellIdx); cellIdx++;
									spdxIdentifierB.setCellValue(relatedSpdxElement);

									rowIdx++;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e) {}
			}
		}

		return makeExcelFileId(wb,downloadFileName, "xls");
	}
	
	@SuppressWarnings("unchecked")
	private static String getSelfCheckSPDX_SpreadSheetExcelPost(String dataStr) throws IOException {

		Workbook wb = null;
		Sheet sheetDoc = null; // Document info
		Sheet sheetPackage = null; // Package Info
		Sheet sheetLicense = null; // Extracted License Info
		Sheet sheetPerFile = null; // Per File Info
		Sheet sheetRelationships = null; // Relationships
		FileInputStream inFile=null;
		
		// download file name
		String downloadFileName = "SPDXRdf-"; // Default
		
		Type ossNoticeType = new TypeToken<OssNotice>(){}.getType();
		
		OssNotice ossNotice = (OssNotice) fromJson(dataStr, ossNoticeType);
		ossNotice.setFileType("text");
		
		String prjId = ossNotice.getPrjId();
		
		try {
			inFile= new FileInputStream(new File(downloadpath+"/SPDXRdf_2.2.2.xls"));
			
			wb = WorkbookFactory.create(inFile);
			sheetDoc = wb.getSheetAt(0);
			sheetPackage = wb.getSheetAt(1);
			sheetLicense = wb.getSheetAt(3);
			sheetPerFile = wb.getSheetAt(4);
			sheetRelationships = wb.getSheetAt(5);
			
			String createdTime = CommonFunction.getCurrentDateTime("yyyyMMddhhmm");
			String createdTimeFull = CommonFunction.getCurrentDateTime("yyyy-MM-dd hh:mm:ss");
			Date createdDateTime = DateUtil.getCurrentDate();
			
			Project projectInfo = new Project();
			projectInfo.setPrjId(prjId);
			projectInfo = selfCheckService.getProjectDetail(projectInfo);

			T2Users userInfo = new T2Users();
			userInfo.setUserId(projectInfo.getCreator());
			
			Map<String, Object> packageInfo = selfCheckService.getExportDataForSBOMInfo(ossNotice);
			
			String strPrjName = projectInfo.getPrjName();
			
			if (!isEmpty(projectInfo.getPrjVersion())) {
				strPrjName += "-" + projectInfo.getPrjVersion();
			}
			
			downloadFileName += FileUtil.makeValidFileName(strPrjName, "_").replaceAll(" ", "").replaceAll("--", "-");
			
			List<String> packageInfoidentifierList = new ArrayList<>();
			
			//Document Info
			{				
				Row row = sheetDoc.getRow(1);
				
				int cellIdx = 0;
				// Spreadsheet Version
				cellIdx ++;
				// SPDX Version
				cellIdx ++;
				// Data License
				cellIdx ++;
				
				// SPDX Identifier
				cellIdx++;
				
				// License List Version
				cellIdx ++;
				
				// Document Name
				Cell cellDocumentName = getCell(row, cellIdx); cellIdx++;
				cellDocumentName.setCellValue(strPrjName);
				
				// Document Namespace
				Cell cellDocumentNamespace = getCell(row, cellIdx); cellIdx++;
				String spdxidentifier = "SPDXRef-" + strPrjName.replaceAll(" ", "") + "-" + createdTime;
				String domain = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org/");
				if (!domain.endsWith("/")) {
					domain += "/";
				}
				cellDocumentNamespace.setCellValue(domain + spdxidentifier);
				
				// Document Contents
				cellIdx++;
				
				//External Document References
				cellIdx ++;
				// Document Comment
				cellIdx ++;
				
				// Creator
				Cell cellCreator = getCell(row, cellIdx); cellIdx++;
				String strCreator = "Person: ";
				userInfo = userService.getUser(userInfo);
				strCreator += projectInfo.getCreator() + " (" + userInfo.getEmail() + ")";
				cellCreator.setCellValue(strCreator);
				
				// Created
				Cell cellCreated = getCell(row, cellIdx); cellIdx++;
				cellCreated.setCellValue(createdDateTime);
				
				// Creator Comment
			}
			
			// Package Info
			{
				List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("noticeObligationList");
				
				List<OssComponents> sourceList = (List<OssComponents>) packageInfo.get("disclosureObligationList");
				
				boolean hideOssVersionFlag = CoConstDef.FLAG_YES.equals(ossNotice.getHideOssVersionYn());
				
				// permissive oss와 copyleft oss를 병합
				if (sourceList != null && !sourceList.isEmpty()) {
					noticeList.addAll(sourceList);
				}
				
				if (packageInfo.containsKey("notObligationList")) {
					List<OssComponents> notObligationList = (List<OssComponents>) packageInfo.get("notObligationList");
					if (notObligationList != null && !notObligationList.isEmpty()) {
						noticeList.addAll(notObligationList);
					}
				}
				
				noticeList = selfCheckService.setMergeGridData(noticeList); // merge Data
				
				int rowIdx = 1;
				
				for (OssComponents bean : noticeList) {
					
					Row row = sheetPackage.getRow(rowIdx);
					
					if (row == null) {
						row = sheetPackage.createRow(rowIdx);
					}
					
					String attributionText = "";
					
					int cellIdx = 0;
					
					// Package Name
					Cell cellPackageName = getCell(row, cellIdx); cellIdx++;
					cellPackageName.setCellValue(bean.getOssName());
					
					// SPDX Identifier
					Cell cellSPDXIdentifier = getCell(row, cellIdx); cellIdx++;
					String ossName = bean.getOssName().replace("&#39;", "\'"); // ossName에 '가 들어갈 경우 정상적으로 oss Info를 찾지 못하는 증상이 발생하여 현재 값으로 치환.

					if (!isEmpty(bean.getOssId())) {
						cellSPDXIdentifier.setCellValue("SPDXRef-Package-" + bean.getOssId());
						packageInfoidentifierList.add("SPDXRef-Package-" + bean.getOssId());
					} else {
						// ossName.equals("-") or unconfirmed OSS Name || OSS Version
						cellSPDXIdentifier.setCellValue("SPDXRef-File-" + bean.getComponentId());
						packageInfoidentifierList.add("SPDXRef-File-" + bean.getComponentId());
					}
					
					// Package Version
					Cell cellPackageVersion = getCell(row, cellIdx); cellIdx++;
					cellPackageVersion.setCellValue(hideOssVersionFlag ? "" : avoidNull(bean.getOssVersion()));
					
					// Package FileName
					cellIdx++;
					
					// Package Supplier
					Cell packageSupplier = getCell(row, cellIdx); cellIdx++;
					packageSupplier.setCellValue("Person: \"\"");
					
					// Package Originator
					Cell packageOriginator = getCell(row, cellIdx); cellIdx++;
					packageOriginator.setCellValue("Organization: \"\"");
					
					// Home Page
					Cell cellHomePage = getCell(row, cellIdx); cellIdx++;
					cellHomePage.setCellValue(avoidNull(bean.getHomepage()));
					
					// Package Download Location
					Cell cellPackageDownloadLocation = getCell(row, cellIdx); cellIdx++;
					String downloadLocation = bean.getDownloadLocation();

					if (downloadLocation.isEmpty()) {
						downloadLocation = "NONE";
					}

					// Invalid download location is output as NONE
					if (SpdxVerificationHelper.verifyDownloadLocation(downloadLocation) != null) {
						downloadLocation = "NONE";
					}

					cellPackageDownloadLocation.setCellValue(downloadLocation);
					
					// Package Checksum
					cellIdx++;
					
					// Package Verification Code
					cellIdx++;
					
					// Verification Code Excluded Files
					cellIdx++;
					
					// Source Info
					cellIdx++;
					
					// License Declared
					Cell cellLicenseDeclared = getCell(row, cellIdx); cellIdx++;

					OssMaster _ossBean = null;
					if (ossName.equals("-")) {
						String licenseStr = CommonFunction.licenseStrToSPDXLicenseFormat(bean.getLicenseName());
						cellLicenseDeclared.setCellValue(licenseStr);
						attributionText = bean.getAttribution();
					} else {
						_ossBean = CoCodeManager.OSS_INFO_UPPER.get( (ossName + "_" + avoidNull(bean.getOssVersion())).toUpperCase());
						
						if (_ossBean != null) {
							String licenseStr = CommonFunction.makeLicenseExpression(_ossBean.getOssLicenses(), false, true);
	
							if (_ossBean.getOssLicenses().size() > 1) {
								licenseStr = "(" + licenseStr + ")";
							}
	
							cellLicenseDeclared.setCellValue(licenseStr);
							attributionText = avoidNull(_ossBean.getAttribution()); // oss attribution
						} else {
							String licenseStr = CommonFunction.licenseStrToSPDXLicenseFormat(bean.getLicenseName());
							cellLicenseDeclared.setCellValue(licenseStr);
							attributionText = bean.getAttribution();
						}
					}
					
					// License Concluded
					Cell cellLicenseConcluded = getCell(row, cellIdx); cellIdx++;
					String srtLicenseName = "";
					
					for (OssComponentsLicense liBean : bean.getOssComponentsLicense()) {
						if (!isEmpty(srtLicenseName)) {
							srtLicenseName += " AND ";
						}
						
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(liBean.getLicenseName()).toUpperCase())) {
							LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(liBean.getLicenseName()).toUpperCase());
							
							if (!isEmpty(liMaster.getShortIdentifier())) {
								liBean.setLicenseName(liMaster.getShortIdentifier());
							} else {
								liBean.setLicenseName("LicenseRef-" + liBean.getLicenseName());
							}
							
							if (!isEmpty(attributionText)) {
								attributionText += "\n";
							}
							
							attributionText += avoidNull(liMaster.getAttribution()); // license attribution
						}
						
						liBean.setLicenseName(liBean.getLicenseName().replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-"));
						srtLicenseName += liBean.getLicenseName();
					}
					
					if (!bean.getOssComponentsLicense().isEmpty() && bean.getOssComponentsLicense().size() > 1) {
						srtLicenseName = "(" + srtLicenseName + ")";
					}
					
					cellLicenseConcluded.setCellValue(srtLicenseName);
					
					// License Info From Files
					Cell licenseInfoFromFiles = getCell(row, cellIdx); cellIdx++;

					if (ossName.equals("-")) {
						licenseInfoFromFiles.setCellValue(CommonFunction.licenseStrToSPDXLicenseFormat(bean.getLicenseName()));
					} else if (_ossBean != null) {
						licenseInfoFromFiles.setCellValue(CommonFunction.makeLicenseFromFiles(_ossBean, true)); // Declared & Detected License Info (중복제거)
					} else {
						licenseInfoFromFiles.setCellValue(""); // OSS Info가 없으므로 빈값이 들어감.
					}
					
					// License Comments
					cellIdx++;
					
					// Package Copyright Text
					Cell cellPackageCopyrightText = getCell(row, cellIdx); cellIdx++;
					String copyrightText = StringUtil.substring(CommonFunction.brReplaceToLine(bean.getCopyrightText()), 0, 32762);

					if (copyrightText.isEmpty() || copyrightText.equals("-")) {
						copyrightText = "NOASSERTION";
					}

					cellPackageCopyrightText.setCellValue(copyrightText);


					// Summary
					cellIdx++;
					
					// Description
					cellIdx++;
					
					
					// Attribution Text
					Cell attributionInfo = getCell(row, cellIdx); cellIdx++;
					attributionInfo.setCellValue(hideOssVersionFlag ? bean.getOssAttribution().replaceAll("<br>", "\n") : attributionText);
					
					// Files Analyzed
					Cell filesAnalyzed = getCell(row, cellIdx); cellIdx++;
					filesAnalyzed.setCellValue("false");
					
					// User Defined Columns...
					
					rowIdx++;
				}
			}
			
			// Extracted License Info
			{
				// BOM에 사용된 OSS Info중 License identifier가 설정되어 있지 않은 license 정보만 출력한다.
				List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("noticeObligationList");
				Map<String, LicenseMaster> nonIdetifierNoticeList = new HashMap<>();
				
				for (OssComponents ocBean : noticeList) {
					String ossName = ocBean.getOssName().replace("&#39;", "\'");

					List<String> licenseList = new ArrayList<>();
					if (ossName.equals("-")) {
						licenseList = Arrays.asList(ocBean.getLicenseName());
					} else {
						OssMaster _ossBean = CoCodeManager.OSS_INFO_UPPER.get((ossName + "_" + avoidNull(ocBean.getOssVersion())).toUpperCase());
						
						if (_ossBean != null) {
							licenseList = Arrays.asList(CommonFunction.makeLicenseFromFiles(_ossBean, false).split(","));
						}
					}
					
					for (String licenseNm : licenseList) {
						LicenseMaster lmBean = CoCodeManager.LICENSE_INFO.get(licenseNm);
						if (lmBean != null && isEmpty(lmBean.getShortIdentifier()) && !nonIdetifierNoticeList.containsKey(lmBean.getLicenseId())) {
							nonIdetifierNoticeList.put(lmBean.getLicenseId(), lmBean);
						}
					}
				}
				
				int rowIdx = 1;
				
				for (LicenseMaster bean : nonIdetifierNoticeList.values()) {
					int cellIdx = 0;
					
					Row row = sheetLicense.getRow(rowIdx);
					
					if (row == null) {
						row = sheetLicense.createRow(rowIdx);
					}
				
					String _licenseName = bean.getLicenseNameTemp().replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-");
					
					// Identifier
					Cell cellIdentifier = getCell(row, cellIdx); cellIdx++;
					cellIdentifier.setCellValue("LicenseRef-" + _licenseName);
					
					// Extracted Text
					Cell cellExtractedText = getCell(row, cellIdx); cellIdx++;
					cellExtractedText.setCellValue(StringUtil.substring(CommonFunction.brReplaceToLine(bean.getLicenseText()), 0, 32762) );
					
					// License Name
					Cell cellLicenseName = getCell(row, cellIdx); cellIdx++;
					cellLicenseName.setCellValue(bean.getLicenseNameTemp());
					
					// Cross Reference URLs
					Cell cellCrossReferenceURLs = getCell(row, cellIdx); cellIdx++;
					boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
					cellCrossReferenceURLs.setCellValue(avoidNull(CommonFunction.makeLicenseInternalUrl(bean, distributionFlag)));
					
					// Comment							
					rowIdx ++;
				}
			}
			
			// Per File Info sheet
			{
				// oss name이 "-" 인 case 만 
				List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("addOssComponentList");
				List<OssComponents> nonIdetifierNoticeList = new ArrayList<>();
				
				for (OssComponents bean : noticeList) {
					// set false because "Per file info sheet" is not currently output
					if ("-".equals(bean.getOssName()) && false) {
						nonIdetifierNoticeList.add(bean);
					}
				}
				
				int rowIdx = 1;
				
				for (OssComponents bean : nonIdetifierNoticeList) {
					int cellIdx = 0;
					String attributionText = "";
					Row row = sheetPerFile.getRow(rowIdx);
					
					if (row == null) {
						row = sheetPerFile.createRow(rowIdx);
					}
				
					// File Name
					Cell fileName = getCell(row, cellIdx); cellIdx++;
					fileName.setCellValue(avoidNull(bean.getFilePath(), "./"));
					
					// SPDX Identifier
					Cell sPDXIdentifier = getCell(row, cellIdx); cellIdx++;
					sPDXIdentifier.setCellValue("SPDXRef-File-" + bean.getComponentId());
					
					// Package Identifier
					cellIdx++;
					
					// File Type(s)
					Cell fileType = getCell(row, cellIdx); cellIdx++;
					fileType.setCellValue("SOURCE");
					
					// File Checksum(s)
					 cellIdx++;
					
					// License Concluded
					Cell cellLicenseConcluded = getCell(row, cellIdx); cellIdx++;
					String srtLicenseName = "";
					
					for (OssComponentsLicense liBean : bean.getOssComponentsLicense()) {
						if (!isEmpty(srtLicenseName)) {
							srtLicenseName += " AND ";
						}
						
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(liBean.getLicenseName()).toUpperCase())) {
							LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(liBean.getLicenseName()).toUpperCase());
							
							if (!isEmpty(liMaster.getShortIdentifier())) {
								liBean.setLicenseName(liMaster.getShortIdentifier());
							} else {
								liBean.setLicenseName("LicenseRef-" + liBean.getLicenseName());
							}
							
							if (!isEmpty(attributionText)) {
								attributionText += "\n";
							}
							
							attributionText += avoidNull(liMaster.getAttribution());
						}
						
						liBean.setLicenseName(liBean.getLicenseName().replaceAll("\\(", "-").replaceAll("\\)", "").replaceAll(" ", "-").replaceAll("--", "-"));
						
						srtLicenseName += liBean.getLicenseName();
					}
					
					if (!bean.getOssComponentsLicense().isEmpty() && bean.getOssComponentsLicense().size() > 1) {
						srtLicenseName = "(" + srtLicenseName + ")";
					}
					
					cellLicenseConcluded.setCellValue(srtLicenseName);
					
					// License Info From Files
					Cell licenseInfoFromFiles = getCell(row, cellIdx); cellIdx++;
					licenseInfoFromFiles.setCellValue(srtLicenseName); // License Concluded 란과 동일한 값으로 표시
					
					// License Comments
					cellIdx++;
					
					// File Copyright Text
					Cell fileCopyrightText = getCell(row, cellIdx); cellIdx++;
					fileCopyrightText.setCellValue(StringUtil.substring(CommonFunction.brReplaceToLine(bean.getCopyrightText()), 0, 32762) );
					
					// Notice Text
					cellIdx++;
					
					// Artifact of Project
					cellIdx++;
					
					// Artifact of Homepage
					cellIdx++;
					
					// Artifact of URL
					cellIdx++;
					
					// Contributors
					cellIdx++;
					
					// File Comment
					cellIdx++;
					
					// File Dependencies
					cellIdx++;
					
					// Attrinbution Info
					Cell attributionInfo = getCell(row, cellIdx); cellIdx++;
					attributionInfo.setCellValue(attributionText);
					
					// User Defined Columns...
					cellIdx++;					
					rowIdx ++;
				}
			}
			
			// sheetRelationships
			{
				int rowIdx = 1;
				
				for (String _identifierB : packageInfoidentifierList) {
					int cellIdx = 0;
					
					Row row = sheetRelationships.getRow(rowIdx);
					if (row == null) {
						row = sheetRelationships.createRow(rowIdx);
					}
					// SPDX Identifier A
					Cell spdxIdentifierA = getCell(row, cellIdx); cellIdx++;
					spdxIdentifierA.setCellValue("SPDXRef-DOCUMENT");
					
					// Relationship
					Cell relationship = getCell(row, cellIdx); cellIdx++;
					relationship.setCellValue("DESCRIBES");
				
					// SPDX Identifier B
					Cell spdxIdentifierB = getCell(row, cellIdx); cellIdx++;
					spdxIdentifierB.setCellValue(_identifierB);
					
					rowIdx++;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e) {}
			}
		}
		
		return makeExcelFileId(wb,downloadFileName, "xls");
	}

	private static Cell getCell(Row row, int cellIdx) {
		Cell cell = row.getCell(cellIdx);
		
		if (cell == null) {
			cell = row.createCell(cellIdx);
		}
		
		return cell;
	}

	/**
	 * 3rd party export<br>
	 * 기존 업로드한 check list data 파일을 재사용한다.
	 * @param prjId
	 * @return
	 * @throws InvalidFormatException
	 * @throws IOException
	 */
	private static String getPartnerChecklistReportExcelPost(String prjId, String type) throws InvalidFormatException, IOException {
		FileInputStream inFile=null;
		Workbook wb = null;
		
		// download file name
		String downloadFileName = "fosslight-report"; // Default

		try {
			boolean isDemo = false;
			if (!isEmpty(prjId) && prjId.endsWith("_demo")) {
				isDemo = true;
				prjId = prjId.replace("_demo", "");
			}
			//cover
			PartnerMaster partnerInfo = new PartnerMaster();
			partnerInfo.setPartnerId(prjId);
			partnerInfo = partnerService.getPartnerMasterOne(partnerInfo);
			
			ProjectIdentification ossListParam = new ProjectIdentification();
			ossListParam.setReferenceId(prjId);
			
			inFile= new FileInputStream(new File(downloadpath+"/OssCheckList.xlsx"));
			wb = WorkbookFactory.create(inFile);
			
			{
				partnerReportSheet(wb, wb.getSheetAt(0), partnerInfo);
			}
			
			//fosslight_report_[date]_3rd-[ID].xlsx
			downloadFileName += "_" + CommonFunction.getCurrentDateTime() + "_3rd-" + StringUtil.deleteWhitespaceWithSpecialChar(prjId);
			
			if (isEmpty(type) || CoConstDef.CD_DTL_COMPONENT_PARTNER.equals(type)) {
				ossListParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER);
				reportIdentificationSheet(CoConstDef.CD_DTL_COMPONENT_PARTNER, wb.getSheetAt(1), projectService.getIdentificationGridList(ossListParam), null, false);
			}
			
			if (isEmpty(type) || CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM.equals(type)) {
				ossListParam.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
				ossListParam.setMerge(CoConstDef.FLAG_NO);
				reportIdentificationSheet(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM, wb.getSheetAt(2), projectService.getIdentificationGridList(ossListParam), null, false);
				reportSecuritySheet(isDemo, CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM, wb, partnerInfo, partnerService.getSecurityGridList(isDemo, partnerInfo));
			}
			
			if (!isEmpty(type)) {
				removeReportSheet(type, wb, wb.getNumberOfSheets());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e) {}
			}
		}

		return makeExcelFileId(wb,downloadFileName);
	}

	private static void reportSecuritySheet(boolean isDemo, String cdDtlComponentPartnerBom, Workbook wb, PartnerMaster partnerMaster, List<OssComponents> securityGridList) {
		try {
			CreationHelper creationHelper = wb.getCreationHelper();
			CellStyle style = wb.createCellStyle();
			CellStyle hyperLinkStyle = wb.createCellStyle();
			Font hyperLinkFont = wb.createFont();
			hyperLinkFont.setUnderline(Font.U_SINGLE);
			hyperLinkFont.setColor(IndexedColors.BLUE.getIndex());
			hyperLinkStyle.setFont(hyperLinkFont);
			Sheet sheet = wb.getSheetAt(3);
			
			if (securityGridList != null){
				List<String[]> rowInfoData = new ArrayList<>();
				List<String[]> rowDatas = new ArrayList<>();
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date now = new Date();
				String now_dt = format.format(now);
				String creatorName = partnerMaster.getCreatorName();
				if (!isEmpty(CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, partnerMaster.getDivision()))) {
					creatorName += " (" + CoCodeManager.getCodeString(CoConstDef.CD_USER_DIVISION, partnerMaster.getDivision()) + ")";
				}
				
				String[] rowInfoParam = {
						now_dt
						, partnerMaster.getPartnerName()
						, partnerMaster.getSoftwareVersion()
						, creatorName
				};
				
				rowInfoData.add(rowInfoParam);
				
				int num = 1;
				for (OssComponents bean : securityGridList) {
					if (!isDemo) {
						String[] rowParam = {
								String.valueOf(num++)
								, bean.getOssName()
								, bean.getOssVersion()
								, bean.getCveId()
								, bean.getPublDate()
								, bean.getCvssScore()
								, bean.getVulnerabilityResolution()
								, bean.getVulnerabilityLink()
								, bean.getOfficialPatchLink()
								, bean.getVerStartEndRange()
								, bean.getSecurityComments()
							};
						
						rowDatas.add(rowParam);
					} else {
						String[] rowParam = {
								String.valueOf(num++)
								, bean.getOssName()
								, bean.getOssVersion()
								, bean.getCveId()
								, bean.getPublDate()
								, bean.getCvssScore()
								, bean.getVulnerabilityResolution()
								, bean.getVulnerabilityLink()
								, bean.getSecurityComments()
							};
						
						rowDatas.add(rowParam);
					}
				}
				
				makeSecuritySheet(creationHelper, sheet, style, hyperLinkStyle, rowInfoData, rowDatas, false, isDemo);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public static String getExcelDownloadIdOss(String type, OssMaster oss, String filepath) throws Exception {
		downloadpath = filepath;
		String downloadId = null;
		
		if (isMaximumRowCheck(ossMapper.selectOssMasterTotalCount(oss))){
			oss.setStartIndex(0);
			oss.setPageListSize(MAX_RECORD_CNT);
			oss.setSearchFlag(CoConstDef.FLAG_NO); // 화면 검색일 경우 "Y" export시 "N"
			Map<String,Object> ossMap 	= ossService.getOssMasterList(oss);
			
			downloadId 	= getOssExcel((List<OssMaster>) ossMap.get("rows"));
		}
		
		return downloadId;
	}
	
	private static String makeVerificationExcel (List<ProjectIdentification> verificationList)  throws Exception {
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile = null;

		try {
			inFile= new FileInputStream(new File(downloadpath+"/VerificationList.xlsx"));
			wb = WorkbookFactory.create(inFile);
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "DisclosureOSSList");
			
			// 화면상에 편집가능한 path 정보를 제외하고는 componentid로 DB정보를 참조한다.
			OssComponents param = new OssComponents();
			
			for (ProjectIdentification bean : verificationList) {
				param.addOssComponentsIdList(bean.getComponentId());
			}
			
			Map<String, OssComponents> dbDataMap = new HashMap<>();
			List<OssComponents> list = projectService.selectOssComponentsListByComponentIds(param);
			
			if (list != null) {
				for (OssComponents bean : list) {
					dbDataMap.put(bean.getComponentId(), bean);
				}
			}
			
			List<String[]> rows = new ArrayList<>();
			
			for (ProjectIdentification bean : verificationList) {
				OssComponents dbBean = null;
				
				if (dbDataMap.containsKey(bean.getComponentId())) {
					dbBean = dbDataMap.get(bean.getComponentId());
				}
				
				String[] rowParam = {
						bean.getComponentIdx()
						, bean.getReferenceDiv()
						, dbBean != null ? dbBean.getOssName() : bean.getOssName()
								, dbBean != null ? dbBean.getOssVersion() : bean.getOssVersion()
										, dbBean != null ? dbBean.getDownloadLocation() : bean.getDownloadLocation()
												, dbBean != null ? dbBean.getHomepage() : bean.getHomepage()
														, bean.getLicenseName()
														, bean.getFilePath()
				};
				
				rows.add(rowParam);
			}
			
			//시트 만들기
			makeSheet(sheet, rows);
			
			return makeExcelFileId(wb,"PackagingOSSList");
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	private static String getSelfCheckListExcelPost(String search, String type) throws Exception{
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;
		
		try {
			inFile= new FileInputStream(new File(downloadpath+"/selfCheckList.xlsx"));
			try {wb = new XSSFWorkbook(inFile);} catch (IOException e) {log.error(e.getMessage());}
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "selfCheckList");
			
			Type collectionType2 = new TypeToken<Project>(){}.getType();
			Project project = (Project) fromJson(search, collectionType2);
			List<Project> selfCheckList = selfCheckMapper.getSelfCheckList(project);
			List<String[]> rows = new ArrayList<>();
			
			for (int i = 0; i < selfCheckList.size(); i++){
				Project param = selfCheckList.get(i);
				
				String[] rowParam = {
					param.getPrjId()
					, param.getPrjName()
					, param.getPrjVersion()
//					, CoConstDef.COMMON_SELECTED_ETC.equals(avoidNull(param.getOsType(), CoConstDef.COMMON_SELECTED_ETC)) ? param.getOsTypeEtc() : CoCodeManager.getCodeString(CoConstDef.CD_OS_TYPE, param.getOsType())
//					, param.getDistributionType()
					, param.getCvssScore()
					, (isEmpty(param.getBomCnt()) ? "0" : param.getBomCnt()) + "(" + (isEmpty(param.getDiscloseCnt()) ? "0" : param.getDiscloseCnt()) + ")"
//					, param.getDivision()
					, param.getCreator()
					, CommonFunction.formatDate(param.getCreatedDate())
				};
				
				rows.add(rowParam);
			}
			
			//시트 만들기
			makeSheet(sheet, rows);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		
		return makeExcelFileId(wb,"selfCheckList");
	}
	
	private static String getSelftReportExcelPost (String prjId) throws IOException, InvalidFormatException {
		Workbook wb = null;
		FileInputStream inFile=null;

		// download file name
		String downloadFileName = "fosslight_report"; // Default
		try {
			inFile= new FileInputStream(new File(downloadpath+"/SelfCheck-Report.xlsx"));
			wb = WorkbookFactory.create(inFile);
			//cover
			Project projectInfo = new Project();
			
			{
				projectInfo.setPrjId(prjId);
				projectInfo = selfCheckService.getProjectDetail(projectInfo);
			}
			
			//fosslight_report_[date]_self-[ID].xlsx
			downloadFileName += "_" + CommonFunction.getCurrentDateTime() + "_self-" + StringUtil.deleteWhitespaceWithSpecialChar(prjId);
			
			ProjectIdentification ossListParam = new ProjectIdentification();
			ossListParam.setReferenceId(prjId);
			ossListParam.setReferenceDiv(CoConstDef.CD_DTL_SELF_COMPONENT_ID);
			ossListParam.setMerge(CoConstDef.FLAG_NO);
			
			// self check는 기본적으로 Identification SRC와 동일하게 동작
			// self check의 경우만 추가 처리가 필요한 경우는 isSelfCheck flag로 판단한다.
			Map<String, Object> selfCheckGridInfo = selfCheckService.getIdentificationGridList(ossListParam);
			reportIdentificationSheet(CoConstDef.CD_DTL_COMPONENT_ID_SRC, wb.getSheetAt(0), selfCheckGridInfo, projectInfo, true);
			
			// Vuln 출력
			// selfCheckService.getIdentificationGridList 에서 OSS_Master를 기준으로
			try {
				Sheet vulnSheet = wb.getSheetAt(1);
				
				if (vulnSheet == null) {
					vulnSheet = wb.createSheet("Vulnerability");
				}

				List<Vulnerability> vulnList = selfCheckService.getAllVulnListWithProject(projectInfo.getPrjId());
				List<String[]> vulnRows = new ArrayList<>();
				
				if (vulnList != null && !vulnList.isEmpty()) {
					String _host = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org");
					for (Vulnerability vulnBean : vulnList) {
						List<String> params = new ArrayList<>();
						String url = _host+"/vulnerability/vulnpopup?ossName=";
												
						// PRODUCT
						if (!isEmpty(vulnBean.getOssName())) {
							// nick name에 의해 조회된 경우
							params.add(vulnBean.getOssName());
							params.add(avoidNull(vulnBean.getProduct()));
						} else {
							params.add(avoidNull(vulnBean.getProduct()));
							params.add("-");
							
						}
						
						url += vulnBean.getProduct(); // OSS PRODUCT으로 검색
						
						// VERSION
						params.add(avoidNull(vulnBean.getVersion()));
						params.add(avoidNull(vulnBean.getCvssScore()));
						// vulnpopup url
						
						if (!isEmpty(vulnBean.getVersion()) && !"-".equals(vulnBean.getVersion())) {
							url += "&ossVersion=" + vulnBean.getVersion(); // + "&vulnType=v"; 사용하지 않는 parameter
						}
												
						params.add(url);
						
						vulnRows.add(params.toArray(new String[params.size()]));
					}
				}
				
				if (vulnRows != null && !vulnRows.isEmpty()) {
					makeSheet(vulnSheet, vulnRows, 2, true);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e) {}
			}
		}

		return makeExcelFileId(wb,downloadFileName);
	}
	
	/* 2018-07-26 choye 추가 */
	private static String getVulnerabilityExcel(List<Vulnerability> vulnerabilityList, boolean isPopup) throws Exception{
		Workbook wb = null;
		Sheet sheet = null;
		String fileName = "VulnerabilityList";
		
		try (
			FileInputStream inFile= new FileInputStream(new File(downloadpath+"/VulnerabilityReport.xlsx"));
		) {
			try {
				wb = new XSSFWorkbook(inFile);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "vulnerabilityList");
			
			List<String[]> rows = new ArrayList<>();
			Set<String> ossName = new HashSet<>();

			for (Vulnerability param : vulnerabilityList){
				String[] rowParam = {
					param.getProduct()
					, param.getVersion()
					, param.getCveId()
					, param.getCvssScore()
					, convertPipeToLineSeparator(param.getVulnSummary())
					, param.getPublDate()
					, param.getModiDate()
					, param.getVendor()
				};
				ossName.add(param.getProduct());
				rows.add(rowParam);
			}
			//시트 만들기
			if (isPopup) {
				CreationHelper creationHelper = wb.getCreationHelper();
				CellStyle hyperLinkStyle = wb.createCellStyle();
				Font hyperLinkFont = wb.createFont();
				hyperLinkFont.setUnderline(Font.U_SINGLE);
				hyperLinkFont.setColor(IndexedColors.BLUE.getIndex());
				hyperLinkStyle.setFont(hyperLinkFont);
				hyperLinkStyle.setBorderBottom(BorderStyle.THIN);
				
				makeVulnerabilityPopupSheet(creationHelper, hyperLinkStyle, sheet, rows);
			} else {
				makeSheet(sheet, rows);
			}
			if (ossName.size() == 1) {
				fileName += "_" + ossName.toString().substring(1,ossName.toString().length()-1);
			}
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		}
		return makeExcelFileId(wb, fileName);
	}
	
	private static void makeVulnerabilityPopupSheet(CreationHelper creationHelper, CellStyle hyperLinkStyle, Sheet sheet, List<String[]> rows) {
		int startRow= 1;
		int startCol = 0;
		int endCol = 0;
		int templateRowNum = 1;
		
		if (rows.isEmpty()) {
		} else {
			endCol = rows.get(0).length-1;
		}
		
		int shiftRowNum = rows.size();
		
		Row templateRow = sheet.getRow(templateRowNum);
		Cell templateCell = templateRow.getCell(0);
		CellStyle style = templateCell.getCellStyle();
		
		startRow = templateRow.getRowNum();		
		
		for (int i = startRow; i < startRow+shiftRowNum; i++) {
			String[] rowParam = rows.get(i-startRow);
			
			Row row = sheet.createRow(i);
			for (int colNum=startCol; colNum<=endCol; colNum++) {
				Cell cell=row.createCell(colNum);
				
				if (colNum == 2 && !isEmpty(rowParam[colNum])) {
					cell.setCellValue(rowParam[colNum]);
					Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
					hyperlink.setAddress("https://nvd.nist.gov/vuln/detail/" + rowParam[colNum]);
					cell.setHyperlink(hyperlink);
					cell.setCellStyle(hyperLinkStyle);
				} else {
					if (!isEmpty(rowParam[colNum])) {
						cell.setCellValue(rowParam[colNum]);
					} else {
						cell.setBlank();
					}
					cell.setCellStyle(style);
				}
			}
		}
	}
	
	private static boolean isMaximumRowCheck(int totalRow){
		final String RowCnt = CoCodeManager.getCodeExpString(CoConstDef.CD_EXCEL_DOWNLOAD, CoConstDef.CD_MAX_ROW_COUNT);
		
		if (totalRow > Integer.parseInt(RowCnt)){
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings({ "unchecked"})
	public static String getChartExcel(Statistics chartParams, String filePath) throws Exception {
		Map<String, Object> chartDataMap = new HashMap<String, Object>();
		Statistics result = new Statistics();
		Statistics result2 = new Statistics();
		Statistics ChartData = chartParams;
		ChartData.setIsRawData(CoConstDef.FLAG_NO);
		boolean projectUseFlag = CommonFunction.propertyFlagCheck("menu.project.use.flag", CoConstDef.FLAG_YES);
		boolean partnerUseFlag = CommonFunction.propertyFlagCheck("menu.partner.use.flag", CoConstDef.FLAG_YES);
		
		if (projectUseFlag) {
			ChartData.setCategoryType("STT");
			result = (Statistics) statisticsService.getDivisionalProjectChartData(ChartData).get("chartData");
			
			ChartData.setCategoryType("REV");
			result2 = (Statistics) statisticsService.getDivisionalProjectChartData(ChartData).get("chartData");
			result.getDataArray().addAll(result2.getDataArray());
			result.getTitleArray().addAll(result2.getTitleArray());
			
			ChartData.setCategoryType("DST");
			result2 = (Statistics) statisticsService.getDivisionalProjectChartData(ChartData).get("chartData");
			result.getDataArray().addAll(result2.getDataArray());
			result.getTitleArray().addAll(result2.getTitleArray());
			chartDataMap.put("divisionalProjectChart", result);
		}

		ChartData.setPieSize(50);
		ChartData.setChartType("OSS");
		ChartData.setCategoryType("REV");
		chartDataMap.put("mostUsedOssChart", (List<Statistics>) statisticsService.getMostUsedChartData(ChartData).get("chartData"));
		
		ChartData.setChartType("LICENSE");
		ChartData.setCategoryType("REV");
		chartDataMap.put("mostUsedLicenseChart", (List<Statistics>) statisticsService.getMostUsedChartData(ChartData).get("chartData"));
		
		chartDataMap.put("updatedOssChart", (Statistics) statisticsService.getUpdatedOssChartData(ChartData).get("chartData"));
		
		chartDataMap.put("updatedLicenseChart", (Statistics) statisticsService.getUpdatedLicenseChartData(ChartData).get("chartData"));
		
		if (partnerUseFlag) {
			ChartData.setCategoryType("3rdSTT");
			result = (Statistics) statisticsService.getTrdPartyRelatedChartData(ChartData).get("chartData");
			
			ChartData.setCategoryType("REV");
			result2 = (Statistics) statisticsService.getTrdPartyRelatedChartData(ChartData).get("chartData");
			result.getDataArray().addAll(result2.getDataArray());
			result.getTitleArray().addAll(result2.getTitleArray());
			chartDataMap.put("trdPartyRelatedChart", result);
		}
		
		ChartData.setIsRawData(CoConstDef.FLAG_YES);
		chartDataMap.put("userRelatedChart", (List<Statistics>) statisticsService.getUserRelatedChartData(ChartData).get("chartData"));
		
		return getChartDataExcelId(chartDataMap, filePath);
	}
	
	/**
	 * 
	 * @param partner
	 * @return
	 * @throws Exception
	 * @용도 3rd 엑셀 파일 ID
	 */
	@SuppressWarnings("unchecked")
	private static String getChartDataExcelId(Map<String, Object> chartDataMap, String filepath) throws Exception{
		downloadpath = filepath;
		
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;
		
		try {
			inFile= new FileInputStream(new File(downloadpath+"/chartDataList.xlsx"));
			wb = new XSSFWorkbook(inFile);
			String[] sheetIdxList = new String[] {"divisionalProjectChart", "mostUsedOssChart", "mostUsedLicenseChart", "updatedOssChart", "updatedLicenseChart", "trdPartyRelatedChart", "userRelatedChart"};
			if (chartDataMap != null && !chartDataMap.isEmpty()) {
				for (String key : chartDataMap.keySet()) {
					String chartName = key;
					int idx = 1;
					sheet = wb.getSheetAt((int) Arrays.asList(sheetIdxList).indexOf(chartName));
					
					List<String[]> rows = new ArrayList<>();
					List<String> params = new ArrayList<>();
					
					switch(chartName) {
						case "divisionalProjectChart":
						case "updatedOssChart":
						case "updatedLicenseChart":
						case "trdPartyRelatedChart":
							Statistics chartData = (Statistics) chartDataMap.get(chartName);
							List<String> divisionList = new ArrayList<String>();
							
							if (!chartName.startsWith("updated")) {
								divisionList = CoCodeManager.getCodeNames(CommonFunction.getCoConstDefVal("CD_USER_DIVISION"));
							}
							
							/* Title */
							params = new ArrayList<>();
							params.add("NO");  // seq
							params.add(!chartName.startsWith("updated") ? "Division" : "Date"); // divisionNm
							
							for (String title : chartData.getTitleArray()) {
								params.add(title); // category
							}
							
							rows.add(params.toArray(new String[params.size()]));
							
							/* Data */
							for (int seq = 0, length = chartData.getDataArray().get(0).size() ; seq < length ; seq++) {
								params = new ArrayList<>();
								params.add(Integer.toString(idx));  // seq
								
								if (!chartName.startsWith("updated")) {
									params.add(divisionList.get(seq)); // category
								}else {
									params.add(chartData.getCategoryList().get(seq)); // category
								}
								
								for (List<Integer> arr : chartData.getDataArray()) {
									params.add(Integer.toString(arr.get(seq))); // category Cnt
								}
								
								idx++;
								
								rows.add(params.toArray(new String[params.size()]));
							}
							
							break;
						case "mostUsedOssChart":
						case "mostUsedLicenseChart":
							params = new ArrayList<>();
							
							params.add("NO");  // seq
							params.add("mostUsedOssChart".equals(chartName) ? "OSS Name" : "License Name"); // OSS Name
							params.add("Cnt"); // OSS Cnt
							
							rows.add(params.toArray(new String[params.size()]));
							
							for (Statistics stat : (List<Statistics>) chartDataMap.get(chartName)) {
								params = new ArrayList<>();
								params.add(Integer.toString(idx));  // seq
								params.add(stat.getColumnName());	// OSS Name || License Name
								params.add(Integer.toString(stat.getColumnCnt())); // OSS || License Cnt
								
								idx++;
								rows.add(params.toArray(new String[params.size()]));
							}
							
							break;
						case "userRelatedChart":
							params = new ArrayList<>();
							
							params.add("NO");  // seq
							params.add("Division"); // Division
							params.add("Total"); // Total Cnt
							params.add("Activator"); // Activator Cnt
							
							rows.add(params.toArray(new String[params.size()]));
							
							for (Statistics stat : (List<Statistics>) chartDataMap.get(chartName)) {
								params = new ArrayList<>();
								params.add(Integer.toString(idx));  // seq
								params.add(stat.getDivisionNm());	// Division
								params.add(Integer.toString(stat.getCategory0Cnt())); // Total Cnt
								params.add(Integer.toString(stat.getCategory1Cnt())); // Activor Cnt
								
								idx++;
								
								rows.add(params.toArray(new String[params.size()]));
							}
							
							break;
					}
					
					//시트 만들기
					makeChartSheet(sheet, rows);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return makeExcelFileId(wb,"chartDataList");
	}
	
	/* 2018-07-26 choye 추가 */
	private static String getAnalysisListExcel(List<OssAnalysis> analysisList, String prjId) throws Exception{
		Workbook wb = null;
		Sheet sheet = null;
		InputStream inFile=null;
		
		try {
			inFile= new FileInputStream(new File(downloadpath+"/AutoAnalysisList.xlsx"));
			try {wb = new XSSFWorkbook(inFile);} catch (IOException e) {log.error(e.getMessage());}
			sheet = wb.getSheetAt(0);
			wb.setSheetName(0, "Auto Analysis Input");
			
			List<String[]> rows = new ArrayList<>();

			for (OssAnalysis param : analysisList){
				String[] rowParam = {
					param.getGridId()
					, param.getOssName()
					, param.getOssVersion()
					, param.getLicenseName()
					, param.getDownloadLocation().replaceAll("%40", "@")
					, param.getHomepage().replaceAll("%40", "@")
						, param.getReferenceDiv()
				};
				
				rows.add(rowParam);
			}
			
			//시트 만들기
			makeSheet2(sheet, rows);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (inFile != null) {
				try {
					inFile.close();
				} catch (Exception e2) {
				}
			}
		}
		
		return makeAnalysisListExcelFileId(wb, "AutoAnalysisList", "xlsx", prjId);
	}
	
	/**
	 * 
	 * @param bomCompareList
	 * @return
	 * @throws Exception
	 * @용도 bomcompare 엑셀
	 */
	@SuppressWarnings("unchecked")
	private static String getBomCompareExcelId(String dataStr, boolean isProject) throws Exception{
		Workbook wb = null;
		Sheet sheet = null;
		FileInputStream inFile=null;
		
		ObjectMapper objMapper = new ObjectMapper();
		
		try {
			Map<String, String> map = objMapper.readValue(dataStr, Map.class);
			
			Map<String, Object> beforeBom = new HashMap<String, Object>();
			Map<String, Object> afterBom = new HashMap<String, Object>();
			List<ProjectIdentification> beforeBomList = null;
			List<ProjectIdentification> afterBomList = null;
			
			String beforeId = "";
			String afterId = "";
			
			String beforeCompareInfo = "";
			String afterCompareInfo = "";
			String beforeCompareUrl = "";
			String afterCompareUrl = "";
			
			if (isProject) {
				String beforePrjId = map.get("beforePrjId").toString();
				String afterPrjId = map.get("afterPrjId").toString();			
				
				beforeId = beforePrjId;
				afterId = afterPrjId;
				
				Project beforePrjInfo = projectService.getProjectBasicInfo(beforePrjId);
				beforeCompareInfo = beforePrjInfo.getPrjName();
				if (!isEmpty(beforePrjInfo.getPrjVersion())) {
					beforeCompareInfo += " (" + beforePrjInfo.getPrjVersion() + ")";
				}
				if (!isEmpty(beforeCompareInfo)) {
					beforeCompareUrl = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/project/shareUrl/" + beforeId;
				}
				String beforeReferenceDiv = "";
				
				ProjectIdentification beforeIdentification = new ProjectIdentification();
				beforeIdentification.setReferenceId(beforePrjId);
				
				if(!beforePrjInfo.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
					beforeReferenceDiv = CoConstDef.CD_DTL_COMPONENT_ID_BOM;
					beforeIdentification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
					beforeIdentification.setMerge("N");
				} else {
					beforeReferenceDiv = CoConstDef.CD_DTL_COMPONENT_ID_ANDROID;
					beforeIdentification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
				}
				
				Project afterPrjInfo = projectService.getProjectBasicInfo(afterPrjId);
				afterCompareInfo = afterPrjInfo.getPrjName();
				if (!isEmpty(afterPrjInfo.getPrjVersion())) {
					afterCompareInfo += " (" + afterPrjInfo.getPrjVersion() + ")";
				}
				if (!isEmpty(afterCompareInfo)) {
					afterCompareUrl = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/project/shareUrl/" + afterId;
				}
				String afterReferenceDiv = "";
				
				ProjectIdentification AfterIdentification = new ProjectIdentification();
				AfterIdentification.setReferenceId(afterPrjId);
				
				if(!afterPrjInfo.getNoticeType().equals(CoConstDef.CD_NOTICE_TYPE_PLATFORM_GENERATED)) {
					afterReferenceDiv = CoConstDef.CD_DTL_COMPONENT_ID_BOM;
					AfterIdentification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_BOM);
					AfterIdentification.setMerge("N");
				} else {
					afterReferenceDiv = CoConstDef.CD_DTL_COMPONENT_ID_ANDROID;
					AfterIdentification.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_ANDROID);
				}
				
				beforeBom = projectController.getOssComponentDataInfo(beforeIdentification, beforeReferenceDiv);
				if (beforeReferenceDiv.equals(CoConstDef.CD_DTL_COMPONENT_ID_BOM)) {
					if (!beforeBom.containsKey("rows") || (List<ProjectIdentification>) beforeBom.get("rows") == null) {
					} else {
						beforeBomList = (List<ProjectIdentification>) beforeBom.get("rows");
					}
				} else {
					if (!beforeBom.containsKey("mainData") || (List<ProjectIdentification>) beforeBom.get("mainData") == null) {
					} else {
						beforeBomList = projectService.setMergeGridDataByAndroid((List<ProjectIdentification>) beforeBom.get("mainData"));
					}
				}
				
				afterBom = projectController.getOssComponentDataInfo(AfterIdentification, afterReferenceDiv);
				if (afterReferenceDiv.equals(CoConstDef.CD_DTL_COMPONENT_ID_BOM)) {
					if (!afterBom.containsKey("rows") || (List<ProjectIdentification>) afterBom.get("rows") == null) {
					} else {
						afterBomList = (List<ProjectIdentification>) afterBom.get("rows");
					}
				} else {
					if (!afterBom.containsKey("mainData") || (List<ProjectIdentification>) afterBom.get("mainData") == null) {
					} else {
						afterBomList = projectService.setMergeGridDataByAndroid((List<ProjectIdentification>) afterBom.get("mainData"));
					}
				}
			} else {
				String beforePartnerId = map.get("beforePartnerId").toString();
				String afterPartnerId = map.get("afterPartnerId").toString();
				
				beforeId = "3rd_" + beforePartnerId;
				afterId = "3rd_" + afterPartnerId;
				
				ProjectIdentification param = new ProjectIdentification();
				param.setReferenceId(beforePartnerId);
				param.setMerge(CoConstDef.FLAG_NO);
				param.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_PARTNER_BOM);
				
				beforeBom = projectService.getIdentificationGridList(param, true);
				if (!beforeBom.containsKey("rows") || (List<ProjectIdentification>) beforeBom.get("rows") == null) {
				} else {
					beforeBomList = (List<ProjectIdentification>) beforeBom.get("rows");
				}
				
				param.setReferenceId(afterPartnerId);
				afterBom = projectService.getIdentificationGridList(param, true);
				if (!afterBom.containsKey("rows") || (List<ProjectIdentification>) afterBom.get("rows") == null) {
				} else {
					afterBomList = (List<ProjectIdentification>) afterBom.get("rows");
				}
				
				PartnerMaster partnerInfo = new PartnerMaster();
				partnerInfo.setPartnerId(beforePartnerId);
				partnerInfo = partnerService.getPartnerMasterOne(partnerInfo);
				
				beforeCompareInfo = partnerInfo.getSoftwareName();
				if (!isEmpty(partnerInfo.getSoftwareVersion())) {
					beforeCompareInfo += " (" + partnerInfo.getSoftwareVersion() + ")";
				}
				if (!isEmpty(beforeCompareInfo)) {
					beforeCompareUrl = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/partner/shareUrl/" + beforePartnerId;
				}
				
				partnerInfo.setPartnerId(afterPartnerId);
				partnerInfo = partnerService.getPartnerMasterOne(partnerInfo);
				
				afterCompareInfo = partnerInfo.getSoftwareName();
				if (!isEmpty(partnerInfo.getSoftwareVersion())) {
					afterCompareInfo += " (" + partnerInfo.getSoftwareVersion() + ")";
				}
				if (!isEmpty(afterCompareInfo)) {
					afterCompareUrl = CommonFunction.emptyCheckProperty("server.domain", "http://fosslight.org") + "/partner/shareUrl/" + afterPartnerId;
				}
			}
			
			if (beforeBomList == null || afterBomList == null) {// before, after값 중 하나라도 null이 있으면 비교 불가함. 
				throw new Exception(); 
			}
			
			List<Map<String, String>> bomCompareListExcel = prjService.getBomCompare(beforeBomList, afterBomList, "excel");
			
			try {
				inFile= new FileInputStream(new File(downloadpath + "/BOM_Compare.xlsx")); 
				wb = new XSSFWorkbook(inFile);
				CreationHelper creationHelper = wb.getCreationHelper();
				CellStyle hyperLinkStyle = wb.createCellStyle();
				hyperLinkStyle.setAlignment(HorizontalAlignment.CENTER);
				hyperLinkStyle.setVerticalAlignment(VerticalAlignment.CENTER);
				Font hyperLinkFont = wb.createFont();
				hyperLinkFont.setUnderline(Font.U_SINGLE);
				hyperLinkFont.setColor(IndexedColors.BLUE.getIndex());
				hyperLinkStyle.setFont(hyperLinkFont);
				sheet = wb.getSheetAt(0); 
				wb.setSheetName(0, "BOM_Compare_"+beforeId+"_"+afterId);
			  
				List<String[]> rows = new ArrayList<String[]>();
			  
				for (int i = 0; i < bomCompareListExcel.size(); i++){ 
					String[] rowParam = {
							bomCompareListExcel.get(i).get("status"),
							bomCompareListExcel.get(i).get("beforeossname"),
							bomCompareListExcel.get(i).get("beforelicense"),
							bomCompareListExcel.get(i).get("afterossname"),
							bomCompareListExcel.get(i).get("afterlicense")
					};
					rows.add(rowParam);
				}
				
				//시트 만들기 
				makeBomCompareSheet(sheet, rows, beforeCompareInfo, afterCompareInfo, beforeCompareUrl, afterCompareUrl, creationHelper, hyperLinkStyle); 
			} catch (FileNotFoundException e) {
				log.error(e.getMessage(), e); 
			} finally { 
				if (inFile != null) { 
					try {inFile.close();} 
					catch (Exception e2) {} 
				}
			}
			
			return makeBomCompareExcelFileId(beforeId, afterId, wb, "BOM_Compare", "xlsx");
		} catch (IOException e){
			log.error(e.getMessage());
		}
		
		return null;
	}
	
	private static void makeBomCompareSheet(Sheet sheet, List<String[]> rows, String beforeCompareInfo, String afterCompareInfo, String beforeCompareUrl, String afterCompareUrl, CreationHelper creationHelper, CellStyle hyperLinkStyle) {
		int startRow = 2;
		int startCol = 0;
		int endCol = 0;
		int templateRowNum = 2;
		
		if (rows.isEmpty()){
		} else {
			endCol = rows.get(0).length-1;
		}
		
		int shiftRowNum = rows.size();
		
		Row templateRow = sheet.getRow(templateRowNum);
		Cell templateCell = templateRow.getCell(0);
		CellStyle style = templateCell.getCellStyle();
		
		Row headerRow = sheet.createRow(0);
		Cell headerCell = headerRow.createCell(1);
		headerCell.setCellStyle(style);
		
		if (!isEmpty(beforeCompareInfo)) {
			if (!isEmpty(beforeCompareUrl)) {
				Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
				hyperlink.setAddress(beforeCompareUrl);
				headerCell.setHyperlink(hyperlink);
				headerCell.setCellStyle(hyperLinkStyle);
			}
			headerCell.setCellValue(beforeCompareInfo);
		} else {
			headerCell.setBlank();
		}
		
		Cell headerCell2 = headerRow.createCell(3);
		headerCell2.setCellStyle(style);
		
		if (!isEmpty(afterCompareInfo)) {
			if (!isEmpty(afterCompareUrl)) {
				Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
				hyperlink.setAddress(afterCompareUrl);
				headerCell2.setHyperlink(hyperlink);
				headerCell2.setCellStyle(hyperLinkStyle);
			}
			headerCell2.setCellValue(afterCompareInfo);
		} else {
			headerCell2.setBlank();
		}
		
		startRow = templateRow.getRowNum();		
		
		for (int i = startRow; i < startRow+shiftRowNum; i++){
			String[] rowParam = rows.get(i-startRow);
			
			Row row = sheet.createRow(i);
			for (int colNum=startCol; colNum<=endCol; colNum++){
				
				Cell cell=row.createCell(colNum);
				cell.setCellStyle(style);
				
				if (!isEmpty(rowParam[colNum])) {
					cell.setCellValue(rowParam[colNum]);
				} else {
					cell.setBlank();
				}
			}
		}
	}
	private static String makeBomCompareExcelFileId(String beforePrjId, String afterPrjId, Workbook wb, String target, String exp) throws IOException {
		String fileName = CommonFunction.replaceSlashToUnderline(target) + "_" + beforePrjId + "_" + afterPrjId + "_" + CommonFunction.getCurrentDateTime();
		String logiFileName = fileName + "." + exp;
		String excelFilePath = writepath + "/download/";
		
		FileOutputStream outFile = null;
		
		try {
			if (!Files.exists(Paths.get(excelFilePath))) {
				Files.createDirectories(Paths.get(excelFilePath));
			}
			outFile = new FileOutputStream(excelFilePath + logiFileName);
			wb.write(outFile);
			
			// db 등록
			return fileService.registFileDownload(excelFilePath, fileName + "."+exp, logiFileName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (outFile != null) {
				try {
					outFile.close();
				} catch (Exception e2) {}
			}
		}
		
		return null;
	}
	
	private static String getCycloneDXFileId(String type, String dataStr, boolean verifyFlag) {
		// download file name
		String downloadFileName = "CycloneDX-"; // Default
		
		String prjId = dataStr;
		boolean thirdPartyCheckFlag = false;
		boolean selfCheckFlag = false;
		
		if (prjId.startsWith("3rd_")) {
			thirdPartyCheckFlag = true;
			String[] prjIdSplit = dataStr.split("_");
			prjId = prjIdSplit[1];
		} else if (prjId.startsWith("self_")) {
			selfCheckFlag = true;
			String[] prjIdSplit = prjId.split("_");
			prjId = prjIdSplit[1];
		}
		
		OssNotice ossNotice = new OssNotice();
		ossNotice.setPrjId(prjId);
		ossNotice.setFileType("text");
		
		List<OssComponents> dependenciesDataList = null;
		Map<String, Object> packageInfo = null;
		Project projectInfo = null;
		PartnerMaster partner = null;
		
		String strPrjName = "";
		String creator = "";
		
		Date timeStamp = null;
		SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		
		try {
			if (!thirdPartyCheckFlag) {
				projectInfo = new Project();
				projectInfo.setPrjId(prjId);
				
				if (!selfCheckFlag) {
					projectInfo = projectService.getProjectDetail(projectInfo);
				} else {
					projectInfo = selfCheckService.getProjectDetail(projectInfo);
				}
				
				ossNotice.setRefDiv(projectInfo.getReferenceDiv());
				String createDate = projectInfo.getCreatedDate();
				
				timeStamp = sdformat.parse(createDate.replace(" ", "T") + ".000");
				
				creator = projectInfo.getCreator();
				strPrjName = projectInfo.getPrjName();
				if (!isEmpty(projectInfo.getPrjVersion())) {
					strPrjName += "-" + projectInfo.getPrjVersion();
				}
				
				downloadFileName += FileUtil.makeValidFileName(strPrjName, "_").replaceAll(" ", "").replaceAll("--", "-");
				
				if (!selfCheckFlag) {
					if (verifyFlag) {
						packageInfo = verificationService.getNoticeHtmlInfo(ossNotice);
					} else {
						packageInfo = projectService.getExportDataForSBOMInfo(ossNotice);
					}
					
					projectInfo.setReferenceDiv(CoConstDef.CD_DTL_COMPONENT_ID_DEP);
					dependenciesDataList = projectService.getDependenciesDataList(projectInfo);
				} else {
					packageInfo = selfCheckService.getExportDataForSBOMInfo(ossNotice);
				}
			} else {
				partner = new PartnerMaster();
				partner.setPartnerId(prjId);
				partner = partnerService.getPartnerMasterOne(partner);
				
				timeStamp = sdformat.parse(partner.getCreatedDate().replace(" ", "T") + ".000");
				
				creator = partner.getCreator();
				strPrjName = partner.getPartnerName();
				
				downloadFileName += FileUtil.makeValidFileName(strPrjName, "_").replaceAll(" ", "").replaceAll("--", "-");
				
				packageInfo = partnerService.getExportDataForSbomInfo(partner);
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		Bom bom = generatorCycloneDXBOM(packageInfo, dependenciesDataList, timeStamp, creator, verifyFlag, projectInfo, partner);
		
		UUID randomUUID = UUID.randomUUID();
		String fileName = CommonFunction.replaceSlashToUnderline(downloadFileName);
		String logiFileName = fileName + "_" + randomUUID;
		String ext = type.toUpperCase().endsWith("JSON") ? ".json" : ".xml";
		logiFileName += ext;
		String excelFilePath = writepath+"/download/";
		
		String fileId = "";
		FileWriter fw = null;
		try {
			fw = new FileWriter(excelFilePath + "/" + logiFileName, true);
			if (type.toUpperCase().endsWith("JSON")) {
				fw.write(BomGeneratorFactory.createJson(CycloneDxSchema.VERSION_LATEST, bom).toJsonString());
			} else {
				fw.write(BomGeneratorFactory.createXml(CycloneDxSchema.VERSION_LATEST, bom).toXmlString());
			}
			fileId = fileService.registFileDownload(excelFilePath, fileName + ext, logiFileName);
		} catch (IOException | GeneratorException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (Exception e2) {}
			}
		}
		
		return fileId;
	}
	
	@SuppressWarnings("unchecked")
	private static Bom generatorCycloneDXBOM(Map<String, Object> packageInfo, List<OssComponents> dependenciesDataList, Date timeStamp, String creator, boolean verifyFlag, Project prjInfo, PartnerMaster partnerInfo) {
		Bom bom = new Bom();
		
		String id = "";
		String name = "";
		String version = "";
		
		if (prjInfo != null) {
			id = prjInfo.getPrjId();
			name = prjInfo.getPrjName();
			version = prjInfo.getPrjVersion();
		} else if (partnerInfo != null) {
			id = "3rd_" + partnerInfo.getPartnerId();
			name = partnerInfo.getSoftwareName();
			version = partnerInfo.getSoftwareVersion();
		}
		
		List<OssComponents> noticeList = (List<OssComponents>) packageInfo.get("noticeObligationList");
		List<OssComponents> sourceList = (List<OssComponents>) packageInfo.get("disclosureObligationList");
		
		if (sourceList != null && !sourceList.isEmpty()) {
			noticeList.addAll(sourceList);
		}

		if (!verifyFlag && packageInfo.containsKey("notObligationList")) {
			List<OssComponents> notObligationList = (List<OssComponents>) packageInfo.get("notObligationList");
			if (notObligationList != null && !notObligationList.isEmpty()) {
				noticeList.addAll(notObligationList);
			}
		}
		
		if (verifyFlag) {
			noticeList = verificationService.setMergeGridData(noticeList); // merge Data
		}
		
		Map<String, Object> relationshipsMap = new HashMap<>();
		
		Metadata meta = new Metadata();
		meta.setTimestamp(timeStamp);
		
		ToolInformation toolInfo = new ToolInformation();
		List<Component> tools = new ArrayList<>();
		Component tool = new Component();
		tool.setType(org.cyclonedx.model.Component.Type.APPLICATION);
		tool.setName("FOSSLight Hub");
		tool.setVersion(CommonFunction.getProperty("project.version"));
		tools.add(tool);
		toolInfo.setComponents(tools);
		meta.setToolChoice(toolInfo);
		
		Component metaComponent = new Component();
		metaComponent.setType(org.cyclonedx.model.Component.Type.APPLICATION);
		metaComponent.setName(name);
		metaComponent.setVersion(version);
		metaComponent.setBomRef(id);
		meta.setComponent(metaComponent);
		
		List<OrganizationalContact> authors = new ArrayList<>();
		OrganizationalContact organizationalContract = new OrganizationalContact();
		organizationalContract.setName(creator);
		authors.add(organizationalContract);
		meta.setAuthors(authors);
		
		OrganizationalEntity organizationalEntity = new OrganizationalEntity();
		organizationalEntity.setName("LG Electronics");
		organizationalEntity.setUrls(Arrays.asList(new String[] {"https://opensource.lge.com"}));
		meta.setSupplier(organizationalEntity);
		
		bom.setMetadata(meta);
		
		List<Component> componentList = new ArrayList<>();
		List<org.cyclonedx.model.vulnerability.Vulnerability> vulnerablityList = new ArrayList<>();
		List<String> checkCveIdList = new ArrayList<>();
		List<Dependency> dependencyList = new ArrayList<>();
		boolean distributionFlag = CommonFunction.propertyFlagCheck("distribution.use.flag", CoConstDef.FLAG_YES);
		OssMaster ossMaster = new OssMaster();
		
		for (OssComponents bean : noticeList) {
			String ossName = bean.getOssName();
			String ossVersion = bean.getOssVersion();
			
			Component component = new Component();
			ExternalReference external = new ExternalReference();
			List<ExternalReference> externalList = new ArrayList<>();
			
			String relationshipsKey = bean.getPackageUrl(); // (ossName + "(" + avoidNull(bean.getOssVersion()) + ")").toUpperCase();
			String bomRef = bean.getComponentId();
			
			component.setType(org.cyclonedx.model.Component.Type.LIBRARY);
			component.setBomRef(bomRef);
			component.setName(ossName);
			component.setVersion(bean.getOssVersion());
			
			if (!isEmpty(relationshipsKey)) {
				relationshipsMap.put(relationshipsKey, bomRef);
				component.setPurl(relationshipsKey);
			} else {
				if (!isEmpty(bean.getDownloadLocation())) {
					for (String downloadLocation : bean.getDownloadLocation().split(",")) {
						if (!isEmpty(downloadLocation)) {
							ossMaster.setDownloadLocation(downloadLocation);
							String purl = ossService.getPurlByDownloadLocation(ossMaster);
							if (!isEmpty(purl)) {
								relationshipsKey = purl;
								component.setPurl(purl);
							}
							break;
						}
					}
				}
			}
//			if (!isEmpty(bean.getDownloadLocation())) {
//				for (String downloadLocation : bean.getDownloadLocation().split(",")) {
//					if (!isEmpty(downloadLocation)) {
//						ossMaster.setDownloadLocation(downloadLocation);
//						String purl = ossService.getPurlByDownloadLocation(ossMaster);
//						if (!isEmpty(purl)) {
//							component.setPurl(purl);
//						}
//						break;
//					}
//				}
//			}
			
			LicenseChoice licenseChoice = new LicenseChoice();
			List<License> licenseList = new ArrayList<>();
			
			OssMaster _ossBean = null;
			if (ossName.equals("-")) {
				String licenseStr = CommonFunction.licenseStrToSPDXLicenseFormat(bean.getLicenseName());
				licenseStr = CommonFunction.removeSpecialCharacters(licenseStr, true).replaceAll("\\(", "-").replaceAll("\\)", "");
				
				if (licenseStr.contains(",")) {
					for (String license : licenseStr.split(",")) {
						License li = new License();
						li.setName(license.trim());
						licenseList.add(li);
					}
				} else {
					License li = new License();
					li.setName(licenseStr.trim());
					licenseList.add(li);
				}
			} else {
				_ossBean = CoCodeManager.OSS_INFO_UPPER.get( (ossName + "_" + avoidNull(bean.getOssVersion())).toUpperCase());
				if (_ossBean != null) {
					for (OssLicense ossLicense : _ossBean.getOssLicenses()) {
						License li = new License();
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(ossLicense.getLicenseName()).toUpperCase())) {
							LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(ossLicense.getLicenseName()).toUpperCase());
							if (!isEmpty(liMaster.getShortIdentifier())) {
								li.setId(liMaster.getShortIdentifier());
							} else {
								li.setName(liMaster.getLicenseName());
								String internalUrl = CommonFunction.makeLicenseInternalUrl(liMaster, distributionFlag);
								if (!isEmpty(internalUrl)) {
									li.setUrl(internalUrl);
								}
							}
						} else {
							li.setName(ossLicense.getLicenseName());
						}
						
						licenseList.add(li);
					}
				} else {
					if (bean.getLicenseName().contains(",")) {
						for (String license : bean.getLicenseName().split(",")) {
							License li = new License();
							if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(license).toUpperCase())) {
								LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(license).toUpperCase());
								if (!isEmpty(liMaster.getShortIdentifier())) {
									li.setId(liMaster.getShortIdentifier());
								} else {
									li.setName(liMaster.getLicenseName());
									String internalUrl = CommonFunction.makeLicenseInternalUrl(liMaster, distributionFlag);
									if (!isEmpty(internalUrl)) {
										li.setUrl(internalUrl);
									}
								}
							} else {
								li.setName(license.trim());
							}
							
							licenseList.add(li);
						}
					} else {
						License li = new License();
						if (CoCodeManager.LICENSE_INFO_UPPER.containsKey(avoidNull(bean.getLicenseName()).toUpperCase())) {
							LicenseMaster liMaster = CoCodeManager.LICENSE_INFO_UPPER.get(avoidNull(bean.getLicenseName()).toUpperCase());
							if (!isEmpty(liMaster.getShortIdentifier())) {
								li.setId(liMaster.getShortIdentifier());
							} else {
								li.setName(liMaster.getLicenseName());
								String internalUrl = CommonFunction.makeLicenseInternalUrl(liMaster, distributionFlag);
								if (!isEmpty(internalUrl)) li.setUrl(internalUrl);
							}
						} else {
							li.setName(CommonFunction.removeSpecialCharacters(bean.getLicenseName(), true).replaceAll("\\(", "-").replaceAll("\\)", ""));
						}
						licenseList.add(li);
					}
				}
			}
			
			licenseChoice.setLicenses(licenseList);
			component.setLicenses(licenseChoice);
			
			String copyrightText = StringUtil.substring(CommonFunction.brReplaceToLine(bean.getCopyrightText()), 0, 32762);
			if (!copyrightText.isEmpty() && !copyrightText.equals("-")) {
				component.setCopyright(copyrightText);
			}
						
			// download location
			String downloadLocation = bean.getDownloadLocation();
			external.setType(org.cyclonedx.model.ExternalReference.Type.WEBSITE);
			
			if (!isEmpty(downloadLocation)) {
				external.setUrl(downloadLocation);
			}
			
			externalList.add(external);
			component.setExternalReferences(externalList);
			componentList.add(component);
			
			// vulnerability
			if (!isEmpty(ossName) && !ossName.equals("-")) {
				OssMaster param = new OssMaster();
				param.setOssName(ossName);
				param.setOssVersion(isEmpty(ossVersion) ? "N/A" : ossVersion);
				Map<String, Object> vulnMap = vulnerabilityService.getVulnListByOssName(param);
				List<Vulnerability> vulnList = (List<Vulnerability>) vulnMap.get("rows");
				if (vulnList != null && !vulnList.isEmpty()) {
					for (Vulnerability vulnerability : vulnList) {
						String cveId = vulnerability.getCveId();
						String key = bomRef + "_" + cveId;
						if (!checkCveIdList.contains(key)) {
							org.cyclonedx.model.vulnerability.Vulnerability vuln = new org.cyclonedx.model.vulnerability.Vulnerability();
							Source vulnSource = new Source();
							vulnSource.setName("NVD"); // source set name : NVD
							vulnSource.setUrl("https://nvd.nist.gov/vuln/detail/" + cveId); // source set nvd url
							vuln.setBomRef(bomRef);
							vuln.setId(cveId);
							vuln.setSource(vulnSource);
							vulnerablityList.add(vuln);
							checkCveIdList.add(key);
						}
					}
				}
			}
		}
		
		if (dependenciesDataList != null && !dependenciesDataList.isEmpty()) {
			List<Dependency> depList = null;
			// dependency
			for (OssComponents oss : dependenciesDataList) {
				String key = oss.getPackageUrl();
				if (!isEmpty(key) && relationshipsMap.containsKey(key)) {
					depList = new ArrayList<>();
					
					String componentId = (String) relationshipsMap.get(key);
					String[] dependencies = oss.getDependencies().split(",");
					Dependency bomRefDep = new Dependency(componentId);
					boolean isExists = false;
					
					for (String dependency : dependencies) {
						if (relationshipsMap.containsKey(dependency)) {
							isExists = true;
							String relatedComponentId = (String) relationshipsMap.get(dependency);
							Dependency dep = new Dependency(relatedComponentId);
							bomRefDep.addDependency(dep);
						}
					}
					if (isExists) {
						depList.add(bomRefDep);
					}
				}
				
				if (!CollectionUtils.isEmpty(depList)) {
					dependencyList.addAll(depList);
					depList = null;
				}
			}
		}
		
		bom.setComponents(componentList);
		if (!dependencyList.isEmpty()) {
			bom.setDependencies(dependencyList);
		}
		if (!vulnerablityList.isEmpty()) {
			bom.setVulnerabilities(vulnerablityList);
		}
	
		return bom;
	}
}

