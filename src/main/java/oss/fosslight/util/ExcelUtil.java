/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoCodeManager;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.ColumnMissingException;
import oss.fosslight.common.ColumnNameDuplicateException;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.domain.LicenseMaster;
import oss.fosslight.domain.OssAnalysis;
import oss.fosslight.domain.OssComponents;
import oss.fosslight.domain.OssComponentsLicense;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.ProjectIdentification;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.CodeMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.service.FileService;
import com.opencsv.CSVReader;

import lombok.extern.slf4j.Slf4j;

@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_PROPERTIES_PATH)})
@Slf4j
public class ExcelUtil extends CoTopComponent {
	// Service
	private static FileService 		fileService 		= (FileService) 		getWebappContext().getBean(FileService.class);
	
	// Mapper
	private static ProjectMapper 	projectMapper 		= (ProjectMapper) 		getWebappContext().getBean(ProjectMapper.class);
	private static CodeMapper 		codeMapper 			= (CodeMapper) 			getWebappContext().getBean(CodeMapper.class);
	
	public static List<Object> getSheetNames(List<UploadFile> list, String excelLocalPath) throws InvalidFormatException, FileNotFoundException, IOException {
		List<Object> sheetNameList = new ArrayList<Object>();
		//파일 만들기
		String fileName = list.get(0).getFileName();
		String[] ext = StringUtil.split(fileName, ".");
		String extType = ext[ext.length-1];
		String codeExt[] = StringUtil.split(codeMapper.selectExtType("12"),",");
		int count = 0;
		
		for(int i = 0; i < codeExt.length; i++){
			if(codeExt[i].equals(extType)){
				count++;
			};
		}
		
		if(count != 1) {
			sheetNameList = null;
		} else {
			File file = new File(excelLocalPath + "/" + fileName);
			//엑셀 컬럼 읽기
			Workbook wb = null;
			
			try {
				wb = WorkbookFactory.create(file);
				int sheetNum = wb.getNumberOfSheets();
				
				for(int i = 0; i < sheetNum; i++){
					String no = String.valueOf(i);
					String name = wb.getSheetName(i);
					
					// Excel내 sheet이름이 "."으로 시작되는 경우 (예: .Data), 사용자가 선택하도록 보여주는 list에서 제외하여 주시기 바랍니다.
					if(avoidNull(name).trim().startsWith(".")) {
						continue;
					}
					
					Map<String, Object> param = new HashMap<String, Object>();
					param.put("no",no);
					param.put("name",name);
					
					sheetNameList.add(param);
				}
			} finally {
				try {
					wb.close();
				} catch (Exception e) {}
			}

		}
		
		return sheetNameList;
	}
	
	public static List<OssComponents> getOssList(MultipartHttpServletRequest req, String excelLocalPath) {
		Iterator<String> fileNames = req.getFileNames();
		List<OssComponents> ossList = new ArrayList<OssComponents>();
		
		while(fileNames.hasNext()){
			//파일 만들기
			MultipartFile multipart = req.getFile(fileNames.next());
			String fileName = multipart.getOriginalFilename();
			String[] fileNameArray = StringUtil.split(fileName, File.separator);
			fileName = fileNameArray[fileNameArray.length-1];
			
			File file = new File(excelLocalPath + "/" + fileName);
			FileUtil.transferTo(multipart, file);
			//엑셀 컬럼 읽기
			HSSFWorkbook wbHSSF = null;
			XSSFWorkbook wbXSSF	 = null;
			String extType = file.getName().split("[.]")[1].trim();
			
			int rowindex=0;
			int colindex=0;		
			
			if (extType.isEmpty()) {
				return null;
			} else if("xls".equals(extType) || "XLS".equals(extType)) {
				try{
					wbHSSF = new HSSFWorkbook(new FileInputStream(file));
					
					HSSFSheet sheet = wbHSSF.getSheetAt(0);
					List<OssComponentsLicense> ossLicList = new ArrayList<OssComponentsLicense>();
					String originalCom = "";
					int totalSize = 0;
					
					for(rowindex = 0; rowindex < sheet.getPhysicalNumberOfRows(); rowindex++) {
						HSSFRow row = sheet.getRow(rowindex);
						int maxcols = row.getLastCellNum();
						OssComponents ossCom = new OssComponents();
						OssComponentsLicense ossLicense = new OssComponentsLicense();
						//다중 라이센스 여부
						String emptyYN = CoConstDef.FLAG_NO;
						
						for(colindex = 0; colindex < maxcols; colindex++) {
							HSSFCell cell = row.getCell(colindex);
							String value = getCellData(cell);
							
							if(rowindex != 0){
								//oss name
								if(colindex == 1) {
									if("null".equals(value) || "".equals(value)){
										emptyYN = CoConstDef.FLAG_YES;
									}else{
										ossCom.setOssName(value);	
									}
								} else if(colindex == 2) { // oss version
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossCom.setOssVersion(value);
									}
								} else if(colindex == 3) { // download location
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossCom.setDownloadLocation(value);
									}
								} else if(colindex == 4) { // homepage
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossCom.setHomepage(value);
									}
								} else if(colindex == 5){ // license
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossLicense.setLicenseName(value);
									}
								} else if(colindex == 6){ // license text
									if("null".equals(value) || "".equals(value)){
									} else {
										ossLicense.setLicenseText(value);
									}
								} else if(colindex == 7) { // copyright text
									if("null".equals(value) || "".equals(value)){
									} else {
										ossLicense.setCopyrightText(value);
									}
								} else if(colindex == 8) { // path or file
									if("null".equals(value) || "".equals(value)){
									} else {
										ossCom.setFilePath(value);
									}
								}
							}
						}
						
						if(rowindex == 1) {
							ossList.add(ossCom);
							ossLicList.add(ossLicense);
							originalCom = "0";
							totalSize = 0;
						} else if(rowindex == 0) {
							
						} else {
							if(rowindex == sheet.getPhysicalNumberOfRows()) {
								if(CoConstDef.FLAG_YES.equals(emptyYN)) {
									ossLicList.add(ossLicense);
									ossList.get(totalSize-1).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
									ossList.get(totalSize-1).getOssComponentsLicense().addAll(ossLicList);
								} else {
									ossList.add(ossCom);
									totalSize = totalSize+1;
									ossList.get(totalSize-1).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
									ossList.get(totalSize-1).getOssComponentsLicense().addAll(ossLicList);
									ossLicList.clear();
									ossLicList.add(ossLicense);
									ossList.get(totalSize).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
									ossList.get(totalSize).getOssComponentsLicense().addAll(ossLicList);
								}
							} else {
								if(CoConstDef.FLAG_YES.equals(emptyYN)){
									ossLicList.add(ossLicense);
								} else {
									ossList.add(ossCom);
									totalSize = totalSize+1;
									
									if("".equals(originalCom)){
										ossList.get(totalSize-1).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
										ossList.get(totalSize-1).getOssComponentsLicense().addAll(ossLicList);
									} else {
										ossList.get(Integer.parseInt(originalCom)).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
										ossList.get(Integer.parseInt(originalCom)).getOssComponentsLicense().addAll(ossLicList);
										originalCom = "";
									}
									
									ossLicList.clear();
									
									ossLicList.add(ossLicense);
								}
							}
						}
					}
				} catch(IOException e) {
					log.error(e.getMessage(), e);
				} finally {
					try {
						wbHSSF.close();
					}catch(Exception e) {}
				}
			} else if("xlsx".equals(extType) || "XLSX".equals(extType)) {
				try{
					wbXSSF = new XSSFWorkbook(new FileInputStream(file));
					
					XSSFSheet sheet = wbXSSF.getSheetAt(0);
					List<OssComponentsLicense> ossLicList = new ArrayList<OssComponentsLicense>();
					String originalCom = "";
					int totalSize = 0;
					
					for(rowindex = 0; rowindex <= sheet.getPhysicalNumberOfRows(); rowindex++){
						XSSFRow row = sheet.getRow(rowindex);
						int maxcols = row.getLastCellNum();
						OssComponents ossCom = new OssComponents();
						OssComponentsLicense ossLicense = new OssComponentsLicense();
						//다중 라이센스 여부
						String emptyYN = CoConstDef.FLAG_NO;
						
						for(colindex = 0; colindex < maxcols; colindex++){
							XSSFCell cell = row.getCell(colindex);
							String value = getCellData(cell);
							
							if(rowindex != 0){
								if(colindex == 1){ // oss name
									if("null".equals(value) || "".equals(value)) {
										emptyYN = CoConstDef.FLAG_YES;
									} else {
										ossCom.setOssName(value);	
									}
								} else if(colindex == 2) { // oss version
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossCom.setOssVersion(value);
									}
								} else if(colindex == 3) { // download location
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossCom.setDownloadLocation(value);
									}
								} else if(colindex == 4) { // homepage
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossCom.setHomepage(value);
									}
								} else if(colindex == 5) { // license
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossLicense.setLicenseName(value);
									}
								} else if(colindex == 6) { // license text
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossLicense.setLicenseText(value);
									}
								} else if(colindex == 7) { // copyright text
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossLicense.setCopyrightText(value);
									}
								} else if(colindex == 8) { // path or file
									if("null".equals(value) || "".equals(value)) {
									} else {
										ossCom.setFilePath(value);
									}
								}
							}
						}
						
						if(rowindex == 1){
							ossList.add(ossCom);
							ossLicList.add(ossLicense);
							originalCom = "0";
							totalSize = 0;
						} else if(rowindex == 0) {
							
						} else {
							if(rowindex == sheet.getPhysicalNumberOfRows()) {
								if(CoConstDef.FLAG_YES.equals(emptyYN)){
									ossLicList.add(ossLicense);
									ossList.get(totalSize-1).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
									ossList.get(totalSize-1).getOssComponentsLicense().addAll(ossLicList);
								} else {
									ossList.add(ossCom);
									totalSize = totalSize+1;
									ossList.get(totalSize-1).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
									ossList.get(totalSize-1).getOssComponentsLicense().addAll(ossLicList);
									ossLicList.clear();
									ossLicList.add(ossLicense);
									ossList.get(totalSize).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
									ossList.get(totalSize).getOssComponentsLicense().addAll(ossLicList);
								}
							} else {
								if(CoConstDef.FLAG_YES.equals(emptyYN)) {
									ossLicList.add(ossLicense);
								} else {
									ossList.add(ossCom);
									totalSize = totalSize+1;
									
									if("".equals(originalCom)) {
										ossList.get(totalSize-1).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
										ossList.get(totalSize-1).getOssComponentsLicense().addAll(ossLicList);
									} else {
										ossList.get(Integer.parseInt(originalCom)).setOssComponentsLicense(new ArrayList<OssComponentsLicense>());
										ossList.get(Integer.parseInt(originalCom)).getOssComponentsLicense().addAll(ossLicList);
										originalCom = "";
									}
									
									ossLicList.clear();
									
									ossLicList.add(ossLicense);
								}
							}
						}
					}
				} catch(IOException e) {
					log.error(e.getMessage(), e);
				} finally {
					try {
						wbXSSF.close();
					} catch (Exception e2) {}
				}
			}
		}
		
		return ossList;
	}

	public static String getCellData(HSSFCell cell) {
		String value = "";
		
		if(cell == null) {
			value = null;
		} else {
			CellType cellType = cell.getCellType();
			switch (cellType) {
				case FORMULA:
					if(CellType.NUMERIC == cell.getCachedFormulaResultType()) {
						cell.setCellType(CellType.STRING);
						value = cell.getStringCellValue();	
					} else if(CellType.STRING == cell.getCachedFormulaResultType()) {
						value = cell.getStringCellValue();	
					}
					
					break;
				case NUMERIC:
					cell.setCellType(CellType.STRING);
					value = cell.getStringCellValue();	
					
					break;
				case STRING:
					value = cell.getStringCellValue() + "";
					
					break;
				case BLANK:
					value = cell.getStringCellValue() + "";
					
					break;
				case ERROR:
					value = cell.getErrorCellValue() + "";
					
					break;
				default:
					break;
			}
		}
		
		return value;
	}
	
	public static String getCellData(XSSFCell cell) {
		String value = "";
		
		if(cell == null) {
			value = null;
		} else {
			CellType cellType = cell.getCellType();
			
			switch (cellType) {
				case FORMULA:
					if(CellType.NUMERIC == cell.getCachedFormulaResultType()) {
						cell.setCellType(CellType.STRING);
						value = cell.getStringCellValue();	
					} else if(CellType.STRING == cell.getCachedFormulaResultType()) {
						value = cell.getStringCellValue();	
					}
					
					break;
				case NUMERIC:
					cell.setCellType(CellType.STRING);
					value = cell.getStringCellValue();	
					
					break;
				case STRING:
					value = cell.getStringCellValue() + "";
					
					break;
				case BLANK:
					value = cell.getStringCellValue() + "";
					
					break;
				case ERROR:
					value = cell.getErrorCellValue() + "";
					
					break;
				default:
					break;
			}
		}
		
		return value;
	}

	/**
	 * 
	 * @param readType Report종류(3rd, SRC, BAT ...)
	 * @param checkId true:Report Data를 기준으로 AutoIdentification 수행여부(ossId등 미입력 상목 fill)
	 * @param targetSheetNums
	 * @param fileSeq
	 * @param list
	 * @param errMsg
	 * @return
	 */
	public static boolean readReport(String readType, boolean checkId, String[] targetSheetNums, String fileSeq, 
	        List<OssComponents> list,List<String> errMsgList) {
		
		T2File fileInfo = fileService.selectFileInfo(fileSeq);
		if(fileInfo == null) {
			log.error("파일정보를 찾을 수 없습니다. fileSeq : " + avoidNull(fileSeq));
			errMsgList.add("파일 정보를 찾을 수 없습니다.");
			return false;
		}
		if(!Arrays.asList("XLS", "XLSX", "XLSM").contains(avoidNull(fileInfo.getExt()).toUpperCase())) {
			log.error("허용하지 않는 파일 입니다. fileSeq : " + avoidNull(fileSeq));
			errMsgList.add("허용하지 않는 파일 입니다.");
			return false;
		}
		File file = new File(fileInfo.getLogiPath() + "/" + fileInfo.getLogiNm());
		if(!file.exists() || !file.isFile()) {
			log.error("파일정보를 찾을 수 없습니다. fileSeq : " + avoidNull(fileSeq));
			errMsgList.add("파일 정보를 찾을 수 없습니다.");
			return false;
		}
		List<Map<String, String>> errList = new ArrayList<>(); // 전체 sheet에 대한 에러메시지 저장 리스트
		//File file = new File("D:/yuns/test/ETD-929/ETD-929.xlsx");
		// read excel file
		Workbook wb = null;
		try {
			 /* 
			    OSS Report의 압축률이 낮아서 관련 내용 검토결과 0.9이하로 압축률이 되어 있을 경우 문제가 발생 할수 있음.
			 	다만, 확장자를 변경시 정상동작을 하고 있어서 해당 code는 주석처리를 해둠.
			    //ZipSecureFile.setMinInflateRatio(-1.0d);
			 */
			 wb = WorkbookFactory.create(file);
			
			// Son System의 Final List 시트가 선택되어 잇을 경우, 다른 sheet는 무시한다.
			try {
				Sheet sheet = wb.getSheetAt(Integer.parseInt(targetSheetNums[0]));
			} catch (Exception e) {
				int sheetIdx = wb.getSheetIndex(readType);
				targetSheetNums[0] = Integer.toString(sheetIdx);
			}
			
			if(hasFileListSheet(targetSheetNums, wb)) {
				// 1. final list sheet data 취득
				Sheet sheet = wb.getSheet("Final List");
				Map<String, String> finalListErrMsg = new HashMap<>();
				finalListErrMsg = readSheet(sheet, list, true, readType, errMsgList);
				if(!finalListErrMsg.isEmpty()) {
					errList.add(finalListErrMsg);
				}
				Map<String, OssComponents> allDataMap = new HashMap<>();
				// path 경로 확인
				if(!list.isEmpty()) {
					// 모든 시트의 정보를 읽는다.(No는 중복될 수 없다는 전제)
					for (int i = 0; i < wb.getNumberOfSheets(); i++) {
						// final list는 제외
						Sheet _sheet = wb.getSheetAt(i);
						if("Final List".equalsIgnoreCase(_sheet.getSheetName())) {
							continue;
						}
						List<OssComponents> _list = new ArrayList<>();
						Map<String, String> errMsg = new HashMap<>();
						errMsg = readSheet(_sheet, _list, true, readType, errMsgList);
						if(!errMsg.isEmpty()) {
							errList.add(errMsg);
						}
						for(OssComponents data : _list) {
							 allDataMap.put(data.getReportKey(), data);
						}
					}
					
					// ref col에서 참조하고 있는 File path가 다른 경우
					List<OssComponents> addDiffRefList = new ArrayList<>();
					for(OssComponents bean : list) {
						if(bean.getFinalListRefList() != null && !bean.getFinalListRefList().isEmpty()) {
							
							// file 단위로 입력되어 있기 때문에, ref col 이 1개 이상일 경우 path단위로 그룹핑
							if(bean.getFinalListRefList().size() > 1) {
								List<String> _keyList = new ArrayList<>();
								for(String refKey : bean.getFinalListRefList()) {
									if(allDataMap.containsKey(refKey)) {
										// file 단위로 입력되어 있기 때문에, ref col 이 1개 이상일 경우 path단위로 그룹핑
										// path 취득
										String _path = allDataMap.get(refKey).getFilePath();
/*										if(isEmpty(_path)) {
											continue;
										}
										// 디렉토리 경로로 설정되어 있는지 확인
										if(!_path.endsWith("/") && !_path.endsWith("/*") && _path.indexOf("/") > -1) {
											_path = _path.substring(0, _path.lastIndexOf("/") + 1);
										}
										
										if(_path.endsWith("/*")) {
											_path = _path.substring(0, _path.length() -1);
										}*/
										
										if(!_keyList.contains(_path)) {
											if(!isEmpty(bean.getFilePath()) && !_keyList.contains(bean.getFilePath())) {
												// TODO 기존에 등록된 path와 다른 경로가 참조되었을 경우 어떻게 할 것인가?
												// row를 추가 한다.
												addDiffRefList.add(bean);
											} else {
												bean.setFilePath(_path);
											}
											_keyList.add(_path);
										}
									}
								}	
							} else {
 								String refKey = bean.getFinalListRefList().get(0);
								if(allDataMap.containsKey(refKey)) {
									bean.setFilePath(allDataMap.get(refKey).getFilePath());	
								}
							}
							
						}
					}
					
					if(!addDiffRefList.isEmpty()) {
						list.addAll(addDiffRefList);
					}
					
				}
				// 2. ref sheet
			} else {
				for (String sheetIdx : targetSheetNums) {
					if(StringUtil.isNotNumeric(sheetIdx)) {
						continue;
					}
					// get target sheet
					Sheet sheet = wb.getSheetAt(StringUtil.string2integer(sheetIdx));
					Map<String, String> errMsg = new HashMap<>();
					errMsg = readSheet(sheet, list, false, readType, errMsgList);
					if(!errMsg.isEmpty()) {
						errList.add(errMsg);
					}
				}				
			}
			if(!errList.isEmpty()) { // sheet 별로 저장 된 에러 메시지를 에러 종류 별로 재 가공
				String returnMsg = "";
				String missHeader = "";
				List<String> dupCol = new ArrayList<>();
				List<String> reqCol = new ArrayList<>();
				List<String> errRow = new ArrayList<>();
				String emptySheet = "";
				boolean newLineYn = false;
				for(Map<String, String> item : errList) {
					missHeader += item.containsKey("missHeader")?(isEmpty(missHeader)?"":", ").concat(item.get("missHeader")):"";
					if(item.containsKey("dupCol")) {
						dupCol.add(item.get("dupCol"));
					}
					if(item.containsKey("reqCol")) {
						reqCol.add(item.get("reqCol"));
					}
					if(item.containsKey("errRow")) {
						errRow.add(item.get("errRow"));
					}
					emptySheet += item.containsKey("emptySheet")?(isEmpty(emptySheet)?"":", ").concat(item.get("emptySheet")):"";
				}
				if(!isEmpty(missHeader)) {
					returnMsg += "Can not found Header Row, Sheet Name : [".concat(missHeader).concat("]");
					newLineYn = true;
				}
				if(!dupCol.isEmpty()) {
					returnMsg += newLineYn?"<br/><br/>":"";
					for(int i = 0 ; i < dupCol.size() ; i++) {
						returnMsg += (i==0?"":"<br/>").concat(dupCol.get(i));
					}
					newLineYn = true;
				}
				if(!reqCol.isEmpty()) {
					returnMsg += newLineYn?"<br/><br/>":"";
					for(int i = 0 ; i < reqCol.size() ; i++) {
						returnMsg += (i==0?"":"<br/>").concat(reqCol.get(i));
					}
					newLineYn = true;
				}
				if(!errRow.isEmpty()) {
					returnMsg += newLineYn?"<br/><br/>":"";
					for(int i = 0 ; i < errRow.size() ; i++) {
						returnMsg += (i==0?"":"<br/>").concat(errRow.get(i));
					}
					newLineYn = true;
				}
				if(!isEmpty(emptySheet)) {
					returnMsg += newLineYn?"<br/><br/>":"";
					returnMsg += "There are no OSS listed. Sheet Name : [".concat(emptySheet).concat("]");
					returnMsg += "<br><br>";
					returnMsg += "사용한 Open Source가 없으면, OSS Name란에 하이픈(\"-\")을 기재하고, License란에서는 \"LGE Proprietary License\" (3rd Party가 자체 개발한 File일 경우, Other Proprietary License)를 선택하십시오.";
					returnMsg += "<br><br>";	
					returnMsg += "For the files that do not use open source at all, enter \"-\" on the OSS Name field and select \"LGE Proprietary License\" on the License field (\"Other Proprietary License\" for the file obtained from the 3rd Party).";
				}
				throw new ColumnMissingException(returnMsg);
			}

		} catch (ColumnNameDuplicateException e) {
			log.error("There are duplicated header rows in the OSS Report. Please check it.<br/>" + e.getMessage());
			errMsgList.add("There are duplicated header rows in the OSS Report. Please check it.<br/>" + e.getMessage());
			return false;
		} catch (ColumnMissingException e) {
			log.error(e.getMessage());
			errMsgList.add(e.getMessage());
			return false;
		} catch (Exception e) {
			log.error("Failed Read Excel File,  file seq : " + fileSeq);
			log.error(e.getMessage(), e);
			errMsgList.add("report 파일에 오류가 있습니다. <br/>" + e.getMessage());
			return false;
		} finally {
			try {
				wb.close();
			} catch (Exception e2) {}
		}
		
		
		// 사용자 입력 값을 기준으로 OSS Master 정보를 비교 ID를 설정한다.
		if(checkId && list != null && !list.isEmpty()) {

			// AutoIdentification  처리중 라이선스명이 사용자설정 명칭(닉네임)이 DB에 등록된 정식명칭등으로 치환되는 내용을 사용자에게 표시하기 위해 
			// 엑셀 파일의 원본 내용을 보관한다.
			List<OssComponents> orgList = new ArrayList<>();
			orgList = list;
			
			try {
				OssComponentUtil.getInstance().makeOssComponent(list, true);
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException
					| NoSuchMethodException e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
				errMsgList.add("오픈소슨 또는 라이선스 검출시 애러가 발생했습니다.<br/>" + e.getMessage());
				return false;
			}

			checkLicenseNameChanged(orgList, list, fileSeq);
		}
		return true;
	}

	public static Map<String, String> readSheet(Sheet sheet, List<OssComponents> list, boolean readNoCol,
	        String readType, List<String> errMsgList) {
		int DefaultHeaderRowIndex = 2; // header index
		
		int ossNameCol = -1;
		int ossVersionCol = -1;
		int downloadLocationCol = -1;
		int homepageCol = -1;
		int licenseCol = -1;
		int licenseTextCol = -1;
		int copyrightTextCol = -1;
		int pathOrFileCol = -1;
		int binaryNameCol = -1;
		int excludeCol = -1;
		int commentCol = -1;
		// 기존 레프트 load시 제외대상 처리 여부
		// "Loaded on Product" 가 X 인 row는 제외 처리
		int loadOnProductCol = -1;
		// final list 전용
		int finalListRefCol = -1;
		int noCol = -1;
		// OSS Name => nickName check
		int nickNameCol = -1;
		
		Map<String, String> errMsg = new HashMap<>();
		
		DefaultHeaderRowIndex = findHeaderRowIndex(sheet);
		
		if(DefaultHeaderRowIndex < 0) {
			if(!readNoCol) {
				log.warn("Can not found Header Row, Sheet Name : " + sheet.getSheetName());
				errMsg.put("missHeader", sheet.getSheetName());
			}
		} else {
			// set column index
			Row headerRow = sheet.getRow(DefaultHeaderRowIndex);
			Iterator<Cell> iter = headerRow.cellIterator();
			int colIdx = 0;
			List<String> dupColList = new ArrayList<>();
			
			while (iter.hasNext()) {
				Cell cell = (Cell) iter.next();
				String value = avoidNull(getCellData(cell)).toUpperCase().trim();
				// 각 컬럼별 colindex 찾기
				// 기존 report와 해더 칼럼명 호환 처리가 필요한 경우 여기에 추가
				
				switch (value) {
					case "OSS NAME":
					case "OSS NAME ( OPEN SOURCE SOFTWARE NAME )":
					case "OSS COMPONENT":
						if(ossNameCol > -1) {
							dupColList.add(value);
						}
						
						ossNameCol = colIdx;
						
						break;
					case "OSS VERSION":
						if(ossVersionCol > -1) {
							dupColList.add(value);
						}
						
						ossVersionCol = colIdx;
						
						break;
					case "DOWNLOAD LOCATION":
						if(downloadLocationCol > -1) {
							dupColList.add(value);
						}
						
						downloadLocationCol = colIdx;
						
						break;
					case "HOMEPAGE":
					case "OSS WEBSITE":
						if(homepageCol > -1) {
							dupColList.add(value);
						}
						
						homepageCol = colIdx;
						
						break;
					case "LICENSE":
						if(licenseCol > -1) {
							dupColList.add(value);
						}
						
						licenseCol = colIdx;
						
						break;
					case "LICENSE TEXT":
						if(licenseTextCol > -1) {
							dupColList.add(value);
						}
						
						licenseTextCol = colIdx;
						
						break;
					case "COPYRIGHT TEXT":
					case "COPYRIGHT & LICENSE":
						if(copyrightTextCol > -1) {
							dupColList.add(value);
						}
						
						copyrightTextCol = colIdx;
						
						break;
					case "PATH OR FILE":
					case "PATH":
					case "FILE OR DIRECTORY":
					case "FILE PATH":
					case "DIRECTORY":
					case "SOURCE CODE DIRECTORY (FILE)":
					case "PATH OR BINARY" :
					case "BINARY NAME OR (IF DELIVERY FORM IS SOURCE CODE) SOURCE PATH":
					case "SOURCE NAME OR PATH":
						if(pathOrFileCol > -1) {
							dupColList.add(value);
						}
						pathOrFileCol = colIdx;
						break;
					case "LOADED ON PRODUCT":
						if(loadOnProductCol > -1) {
							dupColList.add(value);
						}
						
						loadOnProductCol = colIdx;
						
						break;
					case "REF. COLUMNS":
						if(finalListRefCol > -1) {
							dupColList.add(value);
						}
						
						finalListRefCol = colIdx;
						
						break;
					case "NO":
					case "ID":
						if(noCol > -1) {
							dupColList.add(value);
						}
						
						noCol = colIdx;
						
						break;
					case "BINARY FILE":
					case "BINARY/LIBRARY FILE":
					case "BINARY NAME":
						if(binaryNameCol > -1) {
							dupColList.add(value);
						}
						
						binaryNameCol = colIdx;
						
						break;
					case "EXCLUDE":
						if(excludeCol > -1) {
							dupColList.add(value);
						}
						
						excludeCol = colIdx;
						
						break;
					case "COMMENT":
						if(commentCol > -1) {
							dupColList.add(value);
						}
						
						commentCol = colIdx;
						
						break;
					case "NICK NAME":
						if(nickNameCol > -1) {
							dupColList.add(value);
						}
						
						nickNameCol = colIdx;
						
						break;
					default:
						break;
				}
				
				colIdx++;
			}
			
			// header 중복 체크
			if(!dupColList.isEmpty()) {
				String msg = dupColList.toString();
				msg = "There are duplicated. Sheet Name : [".concat(sheet.getSheetName()).concat("],  Filed Name : ").concat(msg);
				
				errMsg.put("dupCol", msg);
			}
			
			// 필수 header 누락 시 Exception
			List<String> colNames = new ArrayList<String>();
			
			if(ossNameCol < 0) {
				colNames.add("OSS NAME");
			}
			
			if(ossVersionCol < 0) {
				colNames.add("OSS VERSION");
			}
			
			if(licenseCol < 0) {
				colNames.add("LICENSE");
			}
			
			if(!colNames.isEmpty()) {
				String msg = colNames.toString();
				msg = "No required fields were found. Sheet Name : [".concat(sheet.getSheetName()).concat("],  Filed Name : ").concat(msg);
				errMsg.put("reqCol", msg);
			}
			
			String lastOssName = "";
			String lastOssVersion = "";
			String lastFilePath = "";
			String lastBinaryName = "";
			String lastExcludeYn = "";

			List<String> duplicateCheckList = new ArrayList<>();
			List<String> errRow = new ArrayList<>();
			
			for(int rowIdx = DefaultHeaderRowIndex+1; rowIdx < sheet.getPhysicalNumberOfRows(); rowIdx++) {
			    try {
    				// final list의 ref Data를 찾을 경우, No Cell이 없는 시트는 무시
    				if(readNoCol && noCol < 0) {
    					continue;
    				}
    				
    				Row row = sheet.getRow(rowIdx);
    				
    				if(row == null) {
    					continue;
    				}
    				
    				// 신분석결과서 대응 (작성 가이드 row는 제외)
    				if(noCol > -1 &&  "-".equals(avoidNull(getCellData(row.getCell(noCol))))) {
    					continue;
    				}
    				
    				OssComponents bean = new OssComponents();
    				
    				// 기본정보
    				bean.setOssName(ossNameCol < 0 ? "" : avoidNull(getCellData(row.getCell(ossNameCol))).trim().replaceAll("\t", ""));
    				bean.setOssVersion(ossVersionCol < 0 ? "" : avoidNull(getCellData(row.getCell(ossVersionCol))).trim().replaceAll("\t", ""));
    				bean.setDownloadLocation(downloadLocationCol < 0 ? "" : avoidNull(getCellData(row.getCell(downloadLocationCol))).trim().replaceAll("\t", ""));
    				bean.setHomepage(homepageCol < 0 ? "" : avoidNull(getCellData(row.getCell(homepageCol))).trim().replaceAll("\t", ""));
    				bean.setFilePath(pathOrFileCol < 0 ? "" : avoidNull(getCellData(row.getCell(pathOrFileCol))).trim().replaceAll("\t", ""));
    				bean.setBinaryName(binaryNameCol < 0 ? "" : avoidNull(getCellData(row.getCell(binaryNameCol))).trim().replaceAll("\t", ""));
    				bean.setCopyrightText(copyrightTextCol < 0 ? "" : getCellData(row.getCell(copyrightTextCol)));
    				bean.setComments(commentCol < 0 ? "" : getCellData(row.getCell(commentCol)));
    				bean.setOssNickName(nickNameCol < 0 ? "" : getCellData(row.getCell(nickNameCol)));
    				
    				duplicateCheckList.add(avoidNull(bean.getOssName()) + "-" + avoidNull(bean.getOssVersion()) + "-" + avoidNull(bean.getLicenseName()));
    				
    				// final list인 경우
    				if(finalListRefCol > 0) {
    					bean.setFinalListRefList(Arrays.asList(avoidNull(getCellData(row.getCell(finalListRefCol))).split(",")));
    				}
    				
    				if(readNoCol) {
    					bean.setReportKey(getCellData(row.getCell(noCol)));
    				}
    				
    				// oss Name을 입력하지 않거나, 이전 row와 oss name, oss version이 동일한 경우, 멀티라이선스로 판단
    				OssComponentsLicense subBean = new OssComponentsLicense();
    				subBean.setLicenseName(licenseCol < 0 ? "" : getCellData(row.getCell(licenseCol)));
    				subBean.setLicenseText(licenseTextCol < 0 ? "" : getCellData(row.getCell(licenseTextCol)));
    				
    				if("false".equals(subBean.getLicenseText()) || "-".equals(subBean.getLicenseText())) {
    					subBean.setLicenseText("");
    				}
    				
    				if("false".equals(bean.getCopyrightText())) {
    					bean.setCopyrightText("");
    				}
    				
    				// file path에 개행이 있는 경우 콤마로 변경
    				if(!isEmpty(bean.getFilePath()) && (bean.getFilePath().indexOf("\r\n") > -1 || bean.getFilePath().indexOf("\n") > -1)) {
    					String _tmpFilePath = bean.getFilePath().trim().replaceAll("\r\n", "\n");
    					String _replaceFilePath = "";
    					
    					for(String s : _tmpFilePath.split("\n")) {
    						if(!isEmpty(s)) {
    							if(!isEmpty(_replaceFilePath)) {
    								_replaceFilePath += ",";
    							}
    							
    							_replaceFilePath += s.trim();
    						}
    					}
    					
    					bean.setFilePath(_replaceFilePath);
    				}
    				
    				// empty row check
    				if(isEmpty(bean.getOssName()) && isEmpty(bean.getOssVersion()) && isEmpty(subBean.getLicenseName()) && isEmpty(bean.getBinaryName()) && isEmpty(bean.getFilePath())) {
    					continue;
    				}
    
    				// 기존 레포트에서 load on product 칼럼이 있는 경우, X선택된 row는 제외
    				// 또는 신분석결과서의 exclude 칼럼
    				if( (loadOnProductCol > 0 && "X".equalsIgnoreCase(getCellData(row.getCell(loadOnProductCol)))) 
    						|| (excludeCol > -1 && "Exclude".equalsIgnoreCase(getCellData(row.getCell(excludeCol)))) ) {
    					bean.setExcludeYn(CoConstDef.FLAG_YES);
    				}
    				
    				// default
    				bean.setExcludeYn(avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO));
    
    				// homepage와 download location이 http://로 시작하지 않을 경우 자동으로 체워줌
    				if(!isEmpty(bean.getHomepage()) 
    						&& !(bean.getHomepage().toLowerCase().startsWith("http://") 
    								|| bean.getHomepage().toLowerCase().startsWith("https://") 
    								|| bean.getHomepage().toLowerCase().startsWith("ftp://"))
    						&& !bean.getDownloadLocation().toLowerCase().startsWith("git://")) {
    					bean.setHomepage("http://" + bean.getHomepage());
    				}
    				
    				if(!isEmpty(bean.getDownloadLocation()) 
    						&& !(bean.getDownloadLocation().toLowerCase().startsWith("http://") 
    								|| bean.getDownloadLocation().toLowerCase().startsWith("https://")  
    								|| bean.getDownloadLocation().toLowerCase().startsWith("ftp://")
    							) 
    						&& !bean.getDownloadLocation().toLowerCase().startsWith("git://")) {
    					bean.setDownloadLocation("http://" + bean.getDownloadLocation());
    				}
    				
    				// 이슈 처리로 인해 주석처리함. OSS Name, OSS Version을 작성하지 않은 OSS Info는 상단 OSS Info와 merge하지 않음.
					bean.addOssComponentsLicense(subBean);
					
					list.add(bean);
					
					lastOssName = bean.getOssName();
					lastOssVersion = bean.getOssVersion();
					lastFilePath = bean.getFilePath();
					lastBinaryName = bean.getBinaryName();
					lastExcludeYn = bean.getExcludeYn();
			    } catch (Exception e) {
			    	errRow.add("Row : " + String.valueOf(rowIdx) + ", Message : " + e.getMessage());
			    }
			}
			
			if(!errRow.isEmpty()) {
				String msg = errRow.toString();
				msg = "Error Sheet. Sheet Name : [".concat(sheet.getSheetName()).concat("],  ").concat(msg);
				errMsg.put("errRow", msg);
			}
			if(list.isEmpty()) {
				errMsg.put("emptySheet", sheet.getSheetName());
			}
		}
		
		return errMsg;
	}

	private static boolean hasSameLicense(OssComponents ossComponents, OssComponentsLicense subBean) {
		if(ossComponents != null && subBean != null) {
			if(ossComponents.getOssComponentsLicense() == null || ossComponents.getOssComponentsLicense().isEmpty()) {
				return false;
			}
			
			// license name이 닉네임으로 등록될 수 있어서 알단, DB에 등록되어 있는 라이선스의 경우 정식 명칭을 기준으로 비교
			// 둘중에 하나라도 db에 등록되어 있지 않은 경우, 원본을 기준으로 비교
			for(OssComponentsLicense license : ossComponents.getOssComponentsLicense()) {
				if(avoidNull(subBean.getLicenseName()).equalsIgnoreCase(license.getLicenseName())) {
					return true;
				}
				
				String _temp1 = avoidNull(license.getLicenseName());
				
				if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(_temp1.toUpperCase())) {
					_temp1 = CoCodeManager.LICENSE_INFO_UPPER.get(_temp1.toUpperCase()).getLicenseId();
				}
				
				String _temp2 = avoidNull(subBean.getLicenseName());
				
				if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(_temp2.toUpperCase())) {
					_temp2 = CoCodeManager.LICENSE_INFO_UPPER.get(_temp2.toUpperCase()).getLicenseId();
				}
				
				if(_temp1.equals(_temp2)) {
					return true;
				}
			}
		}
		
		return false;
	}

	private static void readAndroidBuildImageSheet(Sheet sheet, List<OssComponents> list, List<String> errMsgList) {
		int DefaultHeaderRowIndex = 2; // default header index
		
		int ossNameCol = -1;
		int ossVersionCol = -1;
		int downloadLocationCol = -1;
		int homepageCol = -1;
		int licenseCol = -1;
		int licenseTextCol = -1;
		int copyrightTextCol = -1;
		int pathOrFileCol = -1;
		int binaryFileCol = -1;
		// 기존 레프트 load시 제외대상 처리 여부
		// "Loaded on Product" 가 X 인 row는 제외 처리
		int loadOnProductCol = -1;
		int noCol = -1;
		int noticeHtmlCol = -1;
		int excludeCol = -1;
		int commentCol = -1;
		
		DefaultHeaderRowIndex = findHeaderRowIndex(sheet);
		
		if(DefaultHeaderRowIndex < 0) {
			errMsgList.add("Can not found Header Row, Sheet Name : " + sheet.getSheetName());
			
			return;
		}
		
		// set column index
		Row headerRow = sheet.getRow(DefaultHeaderRowIndex);
		Iterator<Cell> iter = headerRow.cellIterator();
		int colIdx = 0;
		
		while (iter.hasNext()) {
			Cell cell = (Cell) iter.next();
			String value = avoidNull(getCellData(cell)).toUpperCase();
			
			// 각 컬럼별 colindex 찾기
			// 기존 report와 해더 칼럼명 호환 처리가 필요한 경우 여기에 추가
			switch (value) {
				case "OSS COMPONENT":
				case "OSS NAME":
					if(ossNameCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					ossNameCol = colIdx;
					
					break;
				case "OSS VERSION":
					if(ossVersionCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					ossVersionCol = colIdx;
					
					break;
				case "DOWNLOAD LOCATION":
					if(downloadLocationCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					downloadLocationCol = colIdx;
					
					break;
				case "HOMEPAGE":
				case "OSS WEBSITE":
					if(homepageCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					homepageCol = colIdx;
					
					break;
				case "LICENSE":
					if(licenseCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					licenseCol = colIdx;
					
					break;
				case "LICENSE TEXT":
					if(licenseTextCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					licenseTextCol = colIdx;
					
					break;
				case "COPYRIGHT TEXT":
				case "COPYRIGHT & LICENSE":
					if(copyrightTextCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					copyrightTextCol = colIdx;
					
					break;
				case "BINARY/LIBRARY FILE":
				case "BINARY NAME":
					if(binaryFileCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					binaryFileCol = colIdx;
					
					break;
				case "DIRECTORY":
				case "SOURCE CODE PATH":
					if(pathOrFileCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					pathOrFileCol = colIdx;
					
					break;
				case "LOADED ON PRODUCT":
					if(loadOnProductCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					loadOnProductCol = colIdx;
					
					break;
				case "NO":
				case "ID":
					if(noCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					noCol = colIdx;
					
					break;
				case "NOTICE.HTML":
					if(noticeHtmlCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					noticeHtmlCol = colIdx;
					
					break;
				case "EXCLUDE":
					if(excludeCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					excludeCol = colIdx;
					
					break;
				case "COMMENT":
					if(commentCol > -1) {
						throw new ColumnNameDuplicateException(sheet.getSheetName() + "." + value);
					}
					
					commentCol = colIdx;
					
					break;
				default:
					break;
			}
			
			colIdx++;
		}
		
		// 필수 header 누락 시 Exception
		List<String> colNames = new ArrayList<String>();
		if(ossNameCol < 0) {
			colNames.add("OSS NAME");
		}
		
		if(ossVersionCol < 0) {
			colNames.add("OSS VERSION");
		}
		
		if(licenseCol < 0) {
			colNames.add("LICENSE");
		}
		
		if(colNames.size() > 0) {
			String colNameTxt = "";
			
			for(int j = 0 ; j < colNames.size() ; j++) {
				colNameTxt += (j==0?"":", ") + colNames.get(j);
			}
			
			throw new ColumnMissingException("No required fields were found in the report file.<br/>Sheet Name: " + sheet.getSheetName() + ", Filed Name: " + colNameTxt);
		}

		String lastOssName = "";
		String lastOssVersion = "";
		String lastFilePath = "";
		String lastBinaryName = "";
		
		for(int rowIdx = DefaultHeaderRowIndex+1; rowIdx < sheet.getPhysicalNumberOfRows(); rowIdx++) {
		    try {
    			Row row = sheet.getRow(rowIdx);
    			
    			// 신분석결과서 대응 (작성 가이드 row는 제외)
    			if(noCol > -1 &&  "-".equals(avoidNull(getCellData(row.getCell(noCol))))) {
    				continue;
    			}
    			
    			OssComponents bean = new OssComponents();
    			// android bin의 경우 binary name을 무조건 수정할 수 있다.
    			// [MC요청] 2. Project List – OSS List에서 Binary DB 검색 결과 제공
    			// BIN[Android] binary name 수정 불가(최종변경)
    			//bean.setCustomBinaryYn(CoConstDef.FLAG_YES);
    			
    			// 기본정보
    			bean.setOssName(ossNameCol < 0 ? "" : getCellData(row.getCell(ossNameCol)));
    			bean.setOssVersion(ossVersionCol < 0 ? "" : getCellData(row.getCell(ossVersionCol)));
    			bean.setFilePath(pathOrFileCol < 0 ? "" : getCellData(row.getCell(pathOrFileCol)));
    			bean.setBinaryName(binaryFileCol < 0 ? "" : getCellData(row.getCell(binaryFileCol)));
    			bean.setBinaryNotice(noticeHtmlCol < 0 ? "" : getCellData(row.getCell(noticeHtmlCol)));
    			bean.setHomepage(homepageCol < 0 ? "" : getCellData(row.getCell(homepageCol)));
    			bean.setDownloadLocation(downloadLocationCol < 0 ? "" : getCellData(row.getCell(downloadLocationCol)));
    			bean.setCopyrightText(copyrightTextCol < 0 ? "" : getCellData(row.getCell(copyrightTextCol)));
    			bean.setComments(commentCol < 0 ? "" : getCellData(row.getCell(commentCol)));
    			
    			if(isEmpty(bean.getBinaryName())) {
    				continue;
    			}
    			
    			// android 의 경우는 list에는 포함시키고 exclude하는 것으로 변경
    			// 기존 레포트에서 load on product 칼럼이 있는 경우, X선택된 row는 제외
    			if( (loadOnProductCol > 0 && "X".equalsIgnoreCase(getCellData(row.getCell(loadOnProductCol)))) 
    					|| (excludeCol > -1 && "Exclude".equalsIgnoreCase(getCellData(row.getCell(excludeCol))) ) ) {
    				bean.setExcludeYn(CoConstDef.FLAG_YES);
    			}
    			
    			// default
    			bean.setExcludeYn(avoidNull(bean.getExcludeYn(), CoConstDef.FLAG_NO));
    			
    			// oss Name을 입력하지 않거나, 이전 row와 oss name, oss version이 동일한 경우, 멀티라이선스로 판단
    			// license 정보
    			OssComponentsLicense subBean = new OssComponentsLicense();
    			subBean.setLicenseName(licenseCol < 0 ? "" : getCellData(row.getCell(licenseCol)));
    			subBean.setLicenseText(licenseTextCol < 0 ? "" : getCellData(row.getCell(licenseTextCol)));
    			
    			if("false".equals(subBean.getLicenseText()) || "-".equals(subBean.getLicenseText())) {
    				subBean.setLicenseText("");
    			}
    			
    			if("false".equals(bean.getCopyrightText())) {
    				bean.setCopyrightText("");
    			}
    
    
    			// homepage와 download location이 http://로 시작하지 않을 경우 자동으로 체워줌(* download location의 경우 git으로 시작 시 http://를 붙이지 않음.)
    			if(!isEmpty(bean.getHomepage()) 
    					&& !(bean.getHomepage().toLowerCase().startsWith("http://") 
    							|| bean.getHomepage().toLowerCase().startsWith("https://"))
    					&& !bean.getDownloadLocation().toLowerCase().startsWith("git://")) {
    				bean.setHomepage("http://" + bean.getHomepage());
    			}
    			
    			if(!isEmpty(bean.getDownloadLocation()) 
    					&& !(bean.getDownloadLocation().toLowerCase().startsWith("http://") 
    							|| bean.getDownloadLocation().toLowerCase().startsWith("https://"))
    					&& !bean.getDownloadLocation().toLowerCase().startsWith("git://")) {
    				bean.setDownloadLocation("http://" + bean.getDownloadLocation());
    			}
    			
				bean.addOssComponentsLicense(subBean);
				list.add(bean);
				lastOssName = bean.getOssName();
				lastOssVersion = bean.getOssVersion();
				lastFilePath = bean.getFilePath();
				lastBinaryName = bean.getBinaryName();
		    } catch (Exception e) {
		        errMsgList.add("Error Row -> Sheet : ["+sheet.getSheetName()+"], Row : ["+rowIdx+"] message : ["+e.getMessage()+"]" );
		    }
		}
		
		if(list.isEmpty()) {
			errMsgList.add("There are no OSS listed. Sheet Name : " + sheet.getSheetName());
		}
	}
	/**
	 * Sheet명이 final list이고, ref. columns 칼럼을 포함하고 있을 경우, son system의 final list로 판단.
	 * @param targetSheetNums
	 * @param wb
	 * @return
	 */
	private static boolean hasFileListSheet(String[] targetSheetNums, Workbook wb) {
		for (String sheetIdx : targetSheetNums) {
			if(StringUtil.isNotNumeric(sheetIdx)) {
				continue;
			}
			
			Sheet sheet = wb.getSheetAt(Integer.parseInt(sheetIdx));
			
			if("Final List".equalsIgnoreCase(sheet.getSheetName()) && hasCol(sheet, "Ref. Columns")) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Sheet 내에서 calVal에 해당하는 cell이 존재하는지 체크한다.
	 * 최대 row 10개, cell 20개 까지만 체크해서 존재하지 않을 경우 false를 반환한다.
	 * @param sheet
	 * @param colVal
	 * @return
	 */
	private static boolean hasCol(Sheet sheet, String colVal) {
		int rowCheckMaxCnt = 10;
		int cellCheckMaxCnt = 20;
		int rowCnt = 0;
		
		for (Row row : sheet) {
			int cellcnt = 0;
			
			for (Cell cell : row) {
				if(colVal.equalsIgnoreCase(getCellData(cell))) {
					return true;
				}
				
				if(cellcnt > cellCheckMaxCnt) {
					break;
				}
				
				cellcnt ++;
			}
			
			if(rowCnt > rowCheckMaxCnt ) {
				break;
			}
			
			rowCnt ++;
		}
		
		return false;
	}

	/**
	 * row의 시작지점을 찾는다. (NO cell이 포함되어 있는 row)
	 * @param sheet
	 * @return
	 */
	private static int findHeaderRowIndex(Sheet sheet) {
		// 최대 10번째 row 까지만 검색
		for (Row row : sheet) {
			// 최대 5개까지만 확인
			int maxCnt = 0;
			
			for (Cell cell : row) {
				String id = avoidNull(getCellData(cell)).trim().toUpperCase();
				
				if("NO".equalsIgnoreCase(id) || "ID".equalsIgnoreCase(id)) {
					return row.getRowNum();
				}
				
				if(maxCnt > 5) {
					break;
				}
			}
		}
		
		// 찾지못한경우 신규 report 양식으로 지정
		return -1;
	}

	private static String getCellData(Cell cell) {
		String value = "";
		
		if(cell == null) {
			
		} else {
			CellType cellType = cell.getCellType();
			switch (cellType) {
				case FORMULA:
					if(CellType.NUMERIC == cell.getCachedFormulaResultType()) {
						cell.setCellType(CellType.STRING);
						value = cell.getStringCellValue();	
					} else if(CellType.STRING == cell.getCachedFormulaResultType()) {
						value = cell.getStringCellValue();	
					}
					
					break;
				case NUMERIC:
					value = Double.toString(cell.getNumericCellValue());
					
					Cell _tempCell = cell;
					_tempCell.setCellType(CellType.STRING);
					String _temp = _tempCell.getStringCellValue();
					
					if(_temp.indexOf(".") == -1 && value.indexOf(".") > -1) {
						value = value.substring(0, value.indexOf("."));
					}
					
					break;
				case STRING:
					value = cell.getStringCellValue() + "";
					
					break;
				case BLANK:
					value = cell.getStringCellValue() + "";
					
					break;
				case ERROR:
					value = cell.getErrorCellValue() + "";
					
					break;
				case BOOLEAN:
					value = cell.getBooleanCellValue() + "";
					
					break;
				default:
					break;
			}
		}
		
		if(value == null) {
			value = "";
		}
		
		// 트림으로 공백 제거 되지 않는것들 정규식으로 앞 뒤 공백만 제거 한다
		return "false".equals(value) ? "" : StringUtil.isNumeric(value) ? StringUtil.trim(value) : StringUtil.trim(value).replaceAll("(^\\p{Z}+|\\p{Z}+$)", "");
	}

	public static Map<String, List<Project>> getModelList(MultipartHttpServletRequest req, String localPath, String distributionType, String prjId) {
		Map<String, List<Project>> result = new HashMap<>();
		List<Project> resultModel = new ArrayList<Project>();
		List<Project> resultModelDelete = new ArrayList<Project>();
		Iterator<String> fileNames = req.getFileNames();
		List<Project> modelList = projectMapper.selectModelList(prjId);
		List<Project> modelDeleteList = projectMapper.selectDeleteModelList(prjId);
		
		// osdd에 존재하는 model만 체크한다.
		Map<String, Project> osddModelInfo = new HashMap<>();
		
		if(modelList != null) {
			for(Project bean : modelList) {
				if(CoConstDef.FLAG_YES.equals(bean.getOsddSyncYn())) {
					osddModelInfo.put(bean.getCategory() + "|" + bean.getModelName().trim().toUpperCase(), bean);
				}
			}
			
			for(Project bean : modelDeleteList) {
				if(CoConstDef.FLAG_YES.equals(bean.getOsddSyncYn())) {
					osddModelInfo.put(bean.getCategory() + "|" + bean.getModelName().trim().toUpperCase(), bean);
				}
			}
		}
		
		String modelCode = CoConstDef.CD_DISTRIBUTE_SITE_SKS.equals(distributionType) ? CoConstDef.CD_MODEL_TYPE2 : CoConstDef.CD_MODEL_TYPE;
		
		while(fileNames.hasNext()){
			MultipartFile multipart = req.getFile(fileNames.next());
			String fileName = multipart.getOriginalFilename();
			String[] ext = StringUtil.split(fileName, ".");
			String extType = ext[ext.length-1];
			String codeExt[] = StringUtil.split(codeMapper.selectExtType("11"),",");
			ArrayList<String> duplicateModel = new ArrayList<String>();
			int count = 0;
			
			for(int i = 0; i < codeExt.length; i++){
				if(codeExt[i].equals(extType)){
					count++;
				};
			}
			
			if(count != 1) {
				result = null;
			} else {
				if(fileName.indexOf("/") > -1) {
					fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
					
					log.debug("File upload OriginalFileName Substring with File.separator : " + fileName);
				}
				
				if(fileName.indexOf("\\") > -1) {
					fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
					
					log.debug("File upload OriginalFileName Substring with File.separator : " + fileName);
				}
				
				File file = new File(localPath+ "/" + fileName );
				FileUtil.transferTo(multipart, file);
				Workbook wb = null;
				
				try {
					wb = WorkbookFactory.create(file);
					int rowindex=0;
					int colindex=0;		
					Sheet sheet = wb.getSheetAt(0);
					Row row = null;
						
					for(int idx = 1; idx <= sheet.getPhysicalNumberOfRows(); idx++){
						row = sheet.getRow(idx);
						
						if(row == null) {
							continue;
						}
						
						int maxcols = row.getLastCellNum();
						Project param = new Project();
						
						for(colindex = 0; colindex < maxcols; colindex++) {
							Cell cell = row.getCell(colindex);
							String value = getCellData(cell);
							
							if(colindex == 0) {
								param.setModelName(value.trim().toUpperCase());
							} else if(colindex == 1) {
								String main = StringUtil.trim(StringUtil.split(value, ">")[0]);
								String sub = StringUtil.trim(StringUtil.split(value, ">")[1]);
								
								for(String s : CoCodeManager.getCodes(modelCode)) {
									if(CoCodeManager.getCodeString(modelCode, s).equals(main)) {
										main = s;
										String subCdNo = CoCodeManager.getSubCodeNo(modelCode, s);
										for(String subCode : CoCodeManager.getCodes(subCdNo)) {
											if(CoCodeManager.getCodeString(subCdNo, subCode).equals(sub)) {
												sub = subCode;
												break;
											}
										}
										
										break;
									}
								}
								
								param.setCategory(main+sub);
							} else if(colindex == 2) {
								boolean isDateFormat = false;
								
								try {
									isDateFormat = org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell);
								} catch (Exception e) {}
								
								if(isDateFormat) {
									SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
									param.setReleaseDate(formatter.format(cell.getDateCellValue()));
								} else {
									param.setReleaseDate(getCellData(cell));
								}
							} else if(colindex == 3) {
								// delete 처리
								value = avoidNull(value).trim();
								
								if(!isEmpty(value) && CoConstDef.FLAG_YES.equals(value) || "1".equals(value)) {
									param.setDelYn(CoConstDef.FLAG_YES);
								} else {
									param.setDelYn(CoConstDef.FLAG_NO);
								}
							}
						}
						
						String key = param.getCategory() + "|" + param.getModelName();
						
						if(osddModelInfo.containsKey(key)) {
							Project modelBean = osddModelInfo.get(key);
							param.setOsddSyncYn(CoConstDef.FLAG_YES);
							param.setModifier(modelBean.getModifier());
							param.setModifiedDate(modelBean.getModifiedDate());
						}
						
						if(!CoConstDef.FLAG_YES.equals(param.getDelYn())) {
							if(!duplicateModel.contains(param.getModelName())) {
								param.setGridId(prjId + CoConstDef.FLAG_NO + ++rowindex);
								resultModel.add(param);
								duplicateModel.add(param.getModelName());
							}
						} else {
							param.setGridId(prjId + CoConstDef.FLAG_YES + ++rowindex);
							resultModelDelete.add(param);
						}
					}
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		result.put("currentModelList", resultModel);
		result.put("delModelList", resultModelDelete);
		
		return result;
	}
	
	public static List<ProjectIdentification> getVerificationList(MultipartHttpServletRequest req, String localPath) {
		//硫��떚�뙆�듃由ы�섏뒪�듃 �뙆�씪濡� 蹂��솚�븯湲�
		List<ProjectIdentification> result = new ArrayList<ProjectIdentification>();
		Iterator<String> fileNames = req.getFileNames();
		
		while(fileNames.hasNext()){
			MultipartFile multipart = req.getFile(fileNames.next());
			String fileName = multipart.getOriginalFilename();

			String extType = FilenameUtils.getExtension(fileName);
			UUID randomUUID = UUID.randomUUID();

			File file = FileUtil.writeFile(multipart, localPath, randomUUID + "." + extType);
			Workbook wb = null;
			
			try {
				wb = WorkbookFactory.create(file);
				Sheet sheet = wb.getSheetAt(0);
				int startRowIdx = findHeaderRowIndex(sheet);
				
				if(startRowIdx < 0) {
					log.warn("Can not found Header Row, fileName : " + fileName + " , Sheet Name : " + sheet.getSheetName());
					
					return null;
				}

				Row headerRow = sheet.getRow(startRowIdx);
				Iterator<Cell> iter = headerRow.cellIterator();
				int _componentId = -1;
				int _filePath = -1;
				int _ossName = -1;
				int _ossVersion = -1;
				int _license = -1;
				
				int colIdx = 0;
				
				while (iter.hasNext()) {
					Cell cell = (Cell) iter.next();
					String value = avoidNull(getCellData(cell)).toUpperCase();

					switch (value) {
						case "ID":			_componentId = colIdx;	break;
						case "PATH":		_filePath = colIdx;		break;
						case "OSS NAME":	_ossName = colIdx;		break;
						case "OSS VERSION":	_ossVersion = colIdx;	break;
						case "LICENSE":		_license = colIdx;		break;
					}
					
					colIdx++;
				}
				
				if(_componentId < 0 || _filePath < 0) {
					return null;
				}
			
				for(int rowIdx = startRowIdx +1; rowIdx <= sheet.getPhysicalNumberOfRows(); rowIdx++){
					Row row = sheet.getRow(rowIdx);
					
					if(row == null) {
						continue;
					}
					
					ProjectIdentification param = new ProjectIdentification();
					param.setComponentId(getCellData(row.getCell(_componentId)));
					param.setFilePath(getCellData(row.getCell(_filePath)));
					param.setOssName(getCellData(row.getCell(_ossName)));
					param.setOssVersion(getCellData(row.getCell(_ossVersion)));
					param.setLicenseName(getCellData(row.getCell(_license)));
					
					if(!StringUtil.isEmpty(param.getComponentId())){
						result.add(param);	
					}
				}
			} catch (IOException ioe) {
				log.error(ioe.getMessage(), ioe);
			}
		}
		
		return result;
	}

	/**
	 * Read Android Build Image
	 * @param readType
	 * @param checkId
	 * @param targetSheetNums
	 * @param fileSeq
	 * @param resultFileSeq 
	 * @param list
	 * @param errMsg
	 * @return
	 */
	public static boolean readAndroidBuildImage(String readType, boolean checkId, String[] targetSheetNums, String fileSeq, String resultFileSeq, List<OssComponents> list, List<String> errMsgList) {
		T2File fileInfo = fileService.selectFileInfoById(fileSeq);
		
		if(fileInfo == null) {
			log.error("파일정보를 찾을 수 없습니다. fileSeq : " + avoidNull(fileSeq));
			
			errMsgList.add("파일 정보를 찾을 수 없습니다.");
			
			return false;
		}
		
		if(!Arrays.asList("XLS", "XLSX", "XLSM").contains(avoidNull(fileInfo.getExt()).toUpperCase())) {
			log.error("허용하지 않는 파일 입니다. fileSeq : " + avoidNull(fileSeq));
			
			errMsgList.add("허용하지 않는 파일 입니다.");
			
			return false;
		}
		
		File file = new File(fileInfo.getLogiPath() + "/" + fileInfo.getLogiNm());
		
		if(!file.exists() || !file.isFile()) {
			log.error("파일정보를 찾을 수 없습니다. fileSeq : " + avoidNull(fileSeq));
			
			errMsgList.add("파일 정보를 찾을 수 없습니다.");
			
			return false;
		}
		
		Workbook wb = null;
		
		try {
			wb = WorkbookFactory.create(file);
			int sheetSeq = 0;
			
			// Son System의 Final List 시트가 선택되어 잇을 경우, 다른 sheet는 무시한다.
			for (String sheetIdx : targetSheetNums) {
				// get target sheet
				Sheet sheet = null;
				
				try {
					sheet = wb.getSheetAt(StringUtil.string2integer(sheetIdx));
				} catch (Exception e) {
					sheetSeq = wb.getSheetIndex(readType);
					sheet = wb.getSheetAt(sheetSeq);
				}
				
				readAndroidBuildImageSheet(sheet, list, errMsgList);
			}
		} catch (ColumnNameDuplicateException e) {
			log.error("There are duplicated header rows in the OSS Report. Please check it.<br/>" + e.getMessage());
			
			errMsgList.add("There are duplicated header rows in the OSS Report. Please check it.<br/>" + e.getMessage());
			
			return false;
		} catch (ColumnMissingException e) {
			log.error(e.getMessage());
			
			errMsgList.add(e.getMessage());
			
			return false;
		} catch (Exception e) {
			log.error("Failed Read Excel File,  file seq : " + fileSeq);
			log.error(e.getMessage(), e);
			
			errMsgList.add("report 파일에 오류가 있습니다. <br/>" + e.getMessage());
			
			return false;
		} finally {
			try {
				wb.close();
			} catch (Exception e2) {}
		}
		
		// 사용자 입력 값을 기준으로 OSS Master 정보를 비교 ID를 설정한다.
		if(checkId && list != null && !list.isEmpty()) {
			// result.txt를 기준으로 삭제 또는 추가
			// result text가 있는 경우
			// <Removed>로 시작하는 row는 삭제
			List<String> removedCheckList = new ArrayList<>(); // 더이상 사용하지 않음, 공통함수에서 사용하지 않도록 처리(CommonFunction.getAndroidResultFileInfo)
			List<OssComponents> addCheckList = new ArrayList<>();
			List<String> existsResultTextBinaryName = null;
			
			if(!isEmpty(resultFileSeq)) {
				List<String> existsBinaryName = new ArrayList<>();
				
				for(OssComponents bean : list) {
					if(!isEmpty(bean.getBinaryName())) {
						existsBinaryName.add(bean.getBinaryName());
					}
				}
				
				T2File resultFileInfo = fileService.selectFileInfoById(resultFileSeq);
				Map<String, Object> _resultFileInfoMap = CommonFunction.getAndroidResultFileInfo(resultFileInfo, existsBinaryName);
				
				if(_resultFileInfoMap.containsKey("removedCheckList")) {
					removedCheckList = (List<String>) _resultFileInfoMap.get("removedCheckList");
				}
				
				if(_resultFileInfoMap.containsKey("addCheckList")) {
					addCheckList = (List<OssComponents>) _resultFileInfoMap.get("addCheckList");
				}
				
				if(_resultFileInfoMap.containsKey("existsResultTextBinaryNameList")) {
					existsResultTextBinaryName = (List<String>) _resultFileInfoMap.get("existsResultTextBinaryNameList"); 
				}
			}
			
			String addedByResultTxtStr = "";
			String deletedByResultTxtStr = "";
			String excludeCheckResultTxtStr = "";
			// result text에서 추가된 binary
			// result.txt에 있으나 OSS Report에 없는 경우 => load되는 OSS List에 해당 Binary를 추가. 팝업을 띄우고 Comment로 추가된 binary목록을 남김.
			
			if(!addCheckList.isEmpty()) {
				list.addAll(addCheckList);
				addedByResultTxtStr = "<b>The following binaries were added to OSS List automatically because they exist in the result.txt.</b>";
				
				for(OssComponents bean : addCheckList) {
					addedByResultTxtStr += "<br> - " + bean.getBinaryName();
				}
			}
			
			
			// 더이상 사용하지 않음
			if(!removedCheckList.isEmpty()) {
				// result list에서 삭제 대상 제외
				for(OssComponents bean : list) {
					if(removedCheckList.contains(bean.getBinaryName())) {
						bean.setExcludeYn(CoConstDef.FLAG_YES);
						
						deletedByResultTxtStr += bean.getBinaryName() + " is excluded by result.txt file.<br>";
					}
				}
			}
			
			// result.txt에 있으나 OSS Report에서 exclude 처리된 경우 => Exclude체크 된 것을 유지. 2번의 Comment내용과 함께 팝업에도 뜨고 Comment로 exclude되어있음을 남김.
			if(existsResultTextBinaryName != null) {
				boolean isFirst = true;
				
				for(OssComponents bean : list) {
					if(existsResultTextBinaryName.contains(bean.getBinaryName()) && CoConstDef.FLAG_YES.equals(bean.getExcludeYn())) {
						if(isFirst) {
							excludeCheckResultTxtStr = "<b>The following binaries are written to the OSS report as excluded, but they are in the result.txt. Make sure it is not included in the final firmware.</b>";
							
							isFirst = false;
						}
						
						excludeCheckResultTxtStr += "<br> - " + bean.getBinaryName();
					}
				}
			}
			
			// result.txt에 의해 추가 삭제된 oss의 경우 세션에 격납
			// client 화면에 표시 및 save시 코멘트 내용에 추가함

			if(!isEmpty(addedByResultTxtStr) || !isEmpty(deletedByResultTxtStr) || !isEmpty(excludeCheckResultTxtStr)) {
				String _sessionData = "<b>OSS List has been changed by result.txt file. </b><br><br>";
				
				if(!isEmpty(addedByResultTxtStr)) {
					_sessionData += addedByResultTxtStr;
				}
				
				if(!isEmpty(deletedByResultTxtStr)) {
					if(!isEmpty(_sessionData)) {
						_sessionData += "<br><br>";
					}
					
					_sessionData += deletedByResultTxtStr;
				}
				
				if(!isEmpty(excludeCheckResultTxtStr)) {
					if(!isEmpty(_sessionData)) {
						_sessionData += "<br><br>";
					}
					
					_sessionData += excludeCheckResultTxtStr;
				}
				
				putSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileSeq), _sessionData);
			}

			// AutoIdentification  처리중 라이선스명이 사용자설정 명칭(닉네임)이 DB에 등록된 정식명칭등으로 치환되는 내용을 사용자에게 표시하기 위해 
			// 엑셀 파일의 원본 내용을 보관한다.
			List<OssComponents> orgList = new ArrayList<>();
			orgList = list;
			
			try {
				OssComponentUtil.getInstance().makeOssComponent(list, true);
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
				log.error(e.getMessage(), e);
				
				deleteSession(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_ANDROID_CHANGED_RESULTTEXT, resultFileSeq));
				
				errMsgList.add("오픈소슨 또는 라이선스 검출시 애러가 발생했습니다." + e.getMessage());
				
				return false;
			}
			
			checkLicenseNameChanged(orgList, list, fileSeq);
		}
		
		return true;
	}

	private static void checkLicenseNameChanged(List<OssComponents> orgList, List<OssComponents> list, String fileSeq) {
		Map<String, String> orgLikeMap = new HashMap<>();
		List<String> licenseCheckParam = new ArrayList<>();
		
		for(OssComponents bean : orgList) {
			if(isEmpty(bean.getOssName())) {
				continue;
			}
			
			String _key = avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion());
			
			if(bean.getOssComponentsLicense() != null) {
				for(OssComponentsLicense license : bean.getOssComponentsLicense()) {
					if(!isEmpty(license.getLicenseName()) && !licenseCheckParam.contains(license.getLicenseName())) {
						licenseCheckParam.add(license.getLicenseName());
					}
					
					// bsd-like, mit-like 처리
					if(!orgLikeMap.containsKey(_key) 
							&& !isEmpty(license.getLicenseName()) 
							&& (license.getLicenseName().toUpperCase().startsWith("MIT-LIKE") || license.getLicenseName().toUpperCase().startsWith("BSD-LIKE"))) {
						orgLikeMap.put(_key, license.getLicenseName());
					}
				}
			}
		}

		List<String> licenseNickNameCheckResult = new ArrayList<>();
		
		for(String licenseName : licenseCheckParam) {
			if(CoCodeManager.LICENSE_INFO_UPPER.containsKey(licenseName.toUpperCase())) {
				LicenseMaster licenseMaster = CoCodeManager.LICENSE_INFO_UPPER.get(licenseName.toUpperCase());
				
				if(licenseMaster.getLicenseNicknameList() != null && !licenseMaster.getLicenseNicknameList().isEmpty()) {
					for(String s : licenseMaster.getLicenseNicknameList()) {
						String newName = avoidNull(licenseMaster.getShortIdentifier(), licenseMaster.getLicenseName());
						if(licenseName.equalsIgnoreCase(s) && !licenseName.equals(newName)) {
							String disp = licenseName + " => " + newName;
							
							if(!licenseNickNameCheckResult.contains(disp)) {
								licenseNickNameCheckResult.add(disp);
								
								break;
							}
						}
					}
				}
			}
		}
		
		if(!orgLikeMap.isEmpty()) {
			Map<String, String> convertLikeMap = new HashMap<>();
			// bsd, mit like 계열의 라이선스가 포함되어 있으면, convert된 list에서 like계열의 라이선스 정보를 격납한다.
			
			for(OssComponents bean : list) {
				if(isEmpty(bean.getOssName())) {
					continue;
				}
				
				if(bean.getOssComponentsLicense() != null) {
					String _key = avoidNull(bean.getOssName()) + "_" + avoidNull(bean.getOssVersion());
					
					for(OssComponentsLicense license : bean.getOssComponentsLicense()) {
						if(!convertLikeMap.containsKey(_key) 
								&& !isEmpty(license.getLicenseName()) 
								&& (license.getLicenseName().toUpperCase().startsWith("MIT-LIKE") || license.getLicenseName().toUpperCase().startsWith("BSD-LIKE"))) {
							convertLikeMap.put(_key, license.getLicenseName());
						}
					}
				}
			}
			
			for(String ossKey : orgLikeMap.keySet()) {
				if(convertLikeMap.containsKey(ossKey)) {
					if(!orgLikeMap.get(ossKey).equals(convertLikeMap.get(ossKey))) {
						String disp = orgLikeMap.get(ossKey) + " => " + convertLikeMap.get(ossKey);
						
						if(!licenseNickNameCheckResult.contains(disp)) {
							licenseNickNameCheckResult.add(disp);
						}
					}
				}
			}
		}
		

		if(!licenseNickNameCheckResult.isEmpty()) {
			StringBuffer changedNick = new StringBuffer();
			changedNick.append("<b>The following license names will be changed to names registered on the system for efficient management.</b><br><br>");
			changedNick.append("<b>License Names</b><br>");
			
			for(String s : licenseNickNameCheckResult) {
				changedNick.append(s).append("<br>");
			}
			
			putSessionObject(CommonFunction.makeSessionKey(loginUserName(), CoConstDef.SESSION_KEY_UPLOAD_REPORT_CHANGEDLICENSE, fileSeq), changedNick.toString());
		}		
	}

	public static List<Project> getModelList(MultipartHttpServletRequest req, String localPath) {
		List<Project> result = new ArrayList<Project>();
		Iterator<String> fileNames = req.getFileNames();
		
		while(fileNames.hasNext()){
			MultipartFile multipart = req.getFile(fileNames.next());
			String fileName = multipart.getOriginalFilename();
	
			String extType = FilenameUtils.getExtension(fileName);
			UUID randomUUID = UUID.randomUUID();
	
			File file = FileUtil.writeFile(multipart, localPath, randomUUID + "." + extType);
			Workbook wb = null;
			
			try {
				wb = WorkbookFactory.create(file);
				int sheetIdx = wb.getSheetIndex("All Model(Software) List");
				Sheet sheet = wb.getSheetAt(sheetIdx);
				int startRowIdx = findHeaderRowIndex(sheet);
				
				if(startRowIdx < 0) {
					log.warn("Can not found Header Row, fileName : " + fileName + " , Sheet Name : " + sheet.getSheetName());
					return null;
				}
	
				Row headerRow = sheet.getRow(startRowIdx);
				Iterator<Cell> iter = headerRow.cellIterator();
				int productGroup = -1;
				int modelName = -1;
				
				int colIdx = 0;
				
				while (iter.hasNext()) {
					Cell cell = (Cell) iter.next();
					String value = avoidNull(getCellData(cell)).toUpperCase();
	
					switch (value) {
						case "PRODUCT GROUP":			productGroup = colIdx;	break;
						case "MODEL(SOFTWARE) NAME":	modelName = colIdx;		break;
					}
					
					colIdx++;
				}
				
				if(modelName < 0) {
					return null;
				}
			
				for(int rowIdx = startRowIdx +1; rowIdx <= sheet.getPhysicalNumberOfRows(); rowIdx++){
					Row row = sheet.getRow(rowIdx);
					
					if(row == null) {
						continue;
					}
					
					Project param = new Project();
					param.setModelName(getCellData(row.getCell(modelName)));
					param.setProductGroup(getCellData(row.getCell(productGroup)));
					
					if(!StringUtil.isEmpty(param.getModelName())){
						result.add(param);	
					}
				}
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		
		return result;
	}
	
	public static Map<String, Object> getAnalysisResultList(Map<String, Object> map) {
		String analysisResultListPath = (String) map.get("analysisResultListPath");
		Map<String, Object> readData = new HashMap<>();
		File file = new File(analysisResultListPath);
		
		if(!file.exists()) {
			log.error("파일정보를 찾을 수 없습니다. file path : " + analysisResultListPath);
			
			return map;
		}
		
		for(File f : file.listFiles()) {
			if(f.isFile()) {
				String[] fileName = f.getName().split("\\.");
				String fileExt = (fileName[fileName.length-1]).toUpperCase();
				
				switch(fileExt) {
					case "CSV":	
						file = f;	
						
						break;
					default:  // 기존 생성되어있던 file은 삭제하지 않고 존재여부만 log로 기록함.
						log.debug("File Name : " + f.getName() + " , File Ext : " + fileExt);	
						
						break;
				}
			}
		}
		
		try {
			FileReader csvFile = new FileReader(file); // CSV File만 가능함.
			CSVReader csvReader = new CSVReader(csvFile, '|');
			List<String[]> allData = csvReader.readAll();
			
			readData = readAnalysisList(allData, (List<OssAnalysis>) map.get("rows"));
			
			if(!readData.isEmpty()) {
				// isValid - false(throws 발생) || true(data 조합)
				if((boolean) readData.get("isValid")) {
					CommonFunction.setAnalysisResultList(readData);
					readData.put("page", (int) map.get("page"));
					readData.put("total", (int) map.get("total"));
					readData.put("records", (int) map.get("records"));
				} else {
					readData.put("rows", new ArrayList<OssAnalysis>());
				}
			}
		} catch(Exception e) {
			log.error(e.getMessage());
		} 
		
		return readData;
	}
	
	public static Map<String, Object> readAnalysisList(List<String[]>  csvDataList, List<OssAnalysis> analysisList) {		
		int gridIdCol = -1;
		int resultCol = -1;
		int ossNameCol = -1;
		int nickNameCol = -1;
		int ossVersionCol = -1;
		int licenseCol = -1;
		int concludedLicenseCol = -1;
		int askalonoLicenseCol = -1;
		int scancodeLicenseCol = -1;
		int needReviewLicenseAskalonoCol = -1;
		int needReviewLicenseScancodeCol = -1;
		int downloadLocationCol = -1;
		int homepageCol = -1;
		int copyrightTextCol = -1;
		
		Map<String, Object> result = new HashMap<>();
		List<OssAnalysis> analysisResultList = new ArrayList<OssAnalysis>();
		
		int colIdx = 0;
		List<String> dupColList = new ArrayList<>();
		String[] titleRow = csvDataList.get(0); // titleRow 추출
		
		for (String col : titleRow) {
			col = col.toUpperCase();
			// 각 컬럼별 colindex 찾기
			// 기존 report와 해더 칼럼명 호환 처리가 필요한 경우 여기에 추가
			switch (col) {
				case "ID":
					if(gridIdCol > -1) {
						dupColList.add(col);
					}
					
					gridIdCol = colIdx;
					
					break;
				case "RESULT":
					if(resultCol > -1) {
						dupColList.add(col);
					}
					
					resultCol = colIdx;
					
					break;
				case "OSS NAME":
					if(ossNameCol > -1) {
						dupColList.add(col);
					}
					
					ossNameCol = colIdx;
					
					break;
				case "NICKNAME":
					if(nickNameCol > -1) {
						dupColList.add(col);
					}
					
					nickNameCol = colIdx;
					
					break;
				case "OSS VERSION":
					if(ossVersionCol > -1) {
						dupColList.add(col);
					}
					
					ossVersionCol = colIdx;
					
					break;
				case "LICENSE":
					if(licenseCol > -1) {
						dupColList.add(col);
					}
					
					licenseCol = colIdx;
					
					break;
				case "CONCLUDED LICENSE":
					if(concludedLicenseCol > -1) {
						dupColList.add(col);
					}
					
					concludedLicenseCol = colIdx;
					
					break;
				case "LICENSE(ASKALONO)":
					if(askalonoLicenseCol > -1) {
						dupColList.add(col);
					}
					
					askalonoLicenseCol = colIdx;
					
					break;
				case "LICENSE(SCANCODE)":
					if(scancodeLicenseCol > -1) {
						dupColList.add(col);
					}
					
					scancodeLicenseCol = colIdx;
					
					break;
				case "NEED REVIEW LICENSE(ASKALONO)":
					if(needReviewLicenseAskalonoCol > -1) {
						dupColList.add(col);
					}
					
					needReviewLicenseAskalonoCol = colIdx;
					
					break;
				case "NEED REVIEW LICENSE(SCANCODE)":
					if(needReviewLicenseScancodeCol > -1) {
						dupColList.add(col);
					}
					
					needReviewLicenseScancodeCol = colIdx;
					
					break;
				case "DOWNLOAD LOCATION":
					if(downloadLocationCol > -1) {
						dupColList.add(col);
					}
					
					downloadLocationCol = colIdx;
					
					break;
				case "HOMEPAGE":
					if(homepageCol > -1) {
						dupColList.add(col);
					}
					
					homepageCol = colIdx;
					
					break;
				case "COPYRIGHT TEXT":
					if(copyrightTextCol > -1) {
						dupColList.add(col);
					}
					
					copyrightTextCol = colIdx;
					
					break;
				default:
					break;
			}
			
			colIdx++;
		}
		
		// header 중복 체크
		if(!dupColList.isEmpty()) {
			String msg = dupColList.toString();
			msg = "There are duplicated. Filed Name : ".concat(msg);
			result.put("isValid", false);
			result.put("errorMsg", msg);
			
			return result;
		}
		
		// 필수 header 누락 시 Exception
		List<String> colNames = new ArrayList<String>();
		
		if(ossNameCol < 0) {
			colNames.add("OSS NAME");
		}
		
		if(ossVersionCol < 0) {
			colNames.add("OSS VERSION");
		}
		
		if(licenseCol < 0) {
			colNames.add("LICENSE");
		}
		
		if(!colNames.isEmpty()) {
			String msg = colNames.toString();
			msg = "Column Name Empty : ".concat(msg);
			result.put("isValid", false);
			result.put("errorMsg", msg);
			
			return result;
		}
		
		List<String> errRow = new ArrayList<>();
		List<String> analysisListIds = new ArrayList<String>();
		
		for(OssAnalysis analysisBean : analysisList) {
			analysisListIds.add(analysisBean.getGridId());
		}
		
		int rowSeq = 0;
		
		for(String[] row : csvDataList) {
		    try {
				if(rowSeq < 2) {
					rowSeq++;
					
					continue;
				}
				
				OssAnalysis bean = new OssAnalysis();
				
				// 기본정보
				String gridId = gridIdCol < 0 ? "" : avoidNull(row[gridIdCol]).trim().replaceAll("\t", "");
				int checkLength = analysisListIds.stream().filter(a -> a.equals(gridId)).collect(Collectors.toList()).size();
				
				if(checkLength == 1) {
					bean.setGridId(gridId);
					bean.setResult(resultCol < 0 ? "" : avoidNull(row[resultCol]).trim().replaceAll("\t", ""));
					bean.setOssName(ossNameCol < 0 ? "" : avoidNull(row[ossNameCol]).trim().replaceAll("\t", ""));
					bean.setOssNickname(nickNameCol < 0 ? "" : row[nickNameCol]);
					bean.setOssVersion(ossVersionCol < 0 ? "" : avoidNull(row[ossVersionCol]).trim().replaceAll("\t", ""));
					bean.setLicenseName(licenseCol < 0 ? "" : avoidNull(row[licenseCol]).trim().replaceAll("\t", "").replaceAll(",", " AND "));
					bean.setConcludedLicense(concludedLicenseCol < 0 ? "" : avoidNull(row[concludedLicenseCol]).trim().replaceAll("\t", "").replaceAll(",", " AND "));
					bean.setAskalonoLicense(askalonoLicenseCol < 0 ? "" : avoidNull(row[askalonoLicenseCol]).trim().replaceAll("\t", "").replaceAll(",", " AND "));
					bean.setScancodeLicense(scancodeLicenseCol < 0 ? "" : avoidNull(row[scancodeLicenseCol]).trim().replaceAll("\t", "").replaceAll(",", " AND "));
					bean.setNeedReviewLicenseAskalono(needReviewLicenseAskalonoCol < 0 ? "" : avoidNull(row[needReviewLicenseAskalonoCol]).trim().replaceAll("\t", "").replaceAll(",", " AND "));
					bean.setNeedReviewLicenseScanode(needReviewLicenseScancodeCol < 0 ? "" : avoidNull(row[needReviewLicenseScancodeCol]).trim().replaceAll("\t", "").replaceAll(",", " AND "));
					bean.setDownloadLocation(downloadLocationCol < 0 ? "" : avoidNull(row[downloadLocationCol]).trim().replaceAll("\t", ""));
					bean.setHomepage(homepageCol < 0 ? "" : avoidNull(row[homepageCol]).trim().replaceAll("\t", ""));
					bean.setOssCopyright(copyrightTextCol < 0 ? "" : avoidNull(row[copyrightTextCol]).trim().replaceAll("\t", ""));
					
					analysisResultList.add(bean);
				}
		    } catch (Exception e) {
		    	errRow.add("Row : " + String.valueOf(rowSeq) + ", Message : " + e.getMessage());
		    }
		}
		
		if(!errRow.isEmpty()) {
			String msg = errRow.toString();
			msg = "Error Row : ".concat(msg);
			result.put("isValid", false);
			result.put("errorMsg", msg);
			
			return result;
		}
		
		if(analysisResultList.isEmpty()) {
			result.put("isValid", false);
			result.put("errorMsg", "empty Row");
			
			return result;
		}
		
		result.put("isValid", true);
		result.put("rows", analysisResultList); // analysisResultList
		result.put("analysisList", analysisList);
		
		return result;
	}
}
