/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.cyclonedx.model.Bom;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.ExternalReference;
import org.spdx.tools.Main;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.CoConstDef;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.config.AppConstBean;
import oss.fosslight.domain.Project;
import oss.fosslight.domain.T2File;
import oss.fosslight.domain.UploadFile;
import oss.fosslight.repository.FileMapper;
import oss.fosslight.repository.ProjectMapper;
import oss.fosslight.repository.VerificationMapper;
import oss.fosslight.service.FileService;
import oss.fosslight.util.CompressUtil;
import oss.fosslight.util.DateUtil;
import oss.fosslight.util.FileUtil;
import oss.fosslight.util.SPDXUtil2;
import oss.fosslight.util.StringUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.util.*;

@Service("fileService")
@Slf4j
@PropertySources(value = {@PropertySource(value=AppConstBean.APP_CONFIG_PROPERTIES_PATH)})
public class FileServiceImpl extends CoTopComponent implements FileService {
	
	@Autowired FileMapper fileMapper;
	@Autowired VerificationMapper verificationMapper;
	@Autowired ProjectMapper projectMapper;
	
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile) {
		return uploadFile(req, registFile, null);
	}
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName) {
		return uploadFile(req,registFile,inputFileName,null);
	}
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, String oldFileId) {
		List<UploadFile> result = new ArrayList<UploadFile>();
		MultipartHttpServletRequest multipartRequest = null;

		/** S: 파일 업로드 **/
		try {
			multipartRequest = (MultipartHttpServletRequest)req;
		} catch(Exception e) {
			log.debug("error : " + e.getMessage());
			return result;
		}
		
		java.util.Iterator<String> fileNames = multipartRequest.getFileNames();

		boolean sw = true;
		String fileId = "";
		
		if (oldFileId==null || "0".equals(oldFileId) || "".equals(oldFileId)){
			fileId = fileMapper.getFileId();
			if (fileId == null){
				fileId = "1";
			}
		} else{
			fileId = oldFileId;
		}
		
		int indexNum = 0;
		
		while (fileNames.hasNext()){
			UploadFile upFile = new UploadFile();					
			boolean uploadSucc = true;
			String fileName = fileNames.next();		//input name
			
			if (inputFileName != null){
				String inputFileNameRe = inputFileName.replace("##]", "");
				int st = fileName.indexOf("[");
				int en = fileName.indexOf("]");
				
				try{
					indexNum = Integer.parseInt(fileName.substring(st+1, en));
				}catch(Exception e){
					log.error("[##] NumberFormat Exception : " + e.getMessage());
				}
				
				boolean isInput = fileName.startsWith(inputFileNameRe);
				
				if (!isInput || inputFileName == null){
					continue;
				}
			}
			
			sw=false;
			
			MultipartFile mFile = multipartRequest.getFile(fileName);
			
			if (isEmpty(mFile.getOriginalFilename())) {
				throw new RuntimeException("File Name is empty");
			}
			
			if (mFile.getSize() <= 0) {
				throw new RuntimeException("File Size is 0");
			}
			
			String originalFileName = mFile.getOriginalFilename();	//Original File name

			// originalFileName에 경로가 포함되어 있는 경우 처리
			log.debug("File upload OriginalFileName : " + originalFileName);
			
			if (originalFileName.indexOf("/") > -1) {
				originalFileName = originalFileName.substring(originalFileName.lastIndexOf("/") + 1);
				
				log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
			}
			if (originalFileName.indexOf("\\") > -1) {
				originalFileName = originalFileName.substring(originalFileName.lastIndexOf("\\") + 1);
				
				log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
			}
			
			String fileExt = FilenameUtils.getExtension(originalFileName);
			String originalFileExt = fileExt; 
			
			if (originalFileName.toLowerCase().endsWith(".tgz.gz")) {
				fileExt = "tgz.gz";
			} else if (originalFileName.toLowerCase().endsWith(".tar.bz2")) {
				fileExt = "tar.bz2";
			} else if (originalFileName.toLowerCase().endsWith(".tar.gz")) {
				fileExt = "tar.gz";
			}
			
			String uploadFilePath = "";
			String uploadThumbFilePath = "";
			
			try {
				uploadFilePath = appEnv.getProperty("upload.path", "/upload");
				uploadThumbFilePath = appEnv.getProperty("image.path", "/image");
			} catch(Exception e) {
				log.error("file upload path(get properties) : " + e.getMessage());
			}
			
			UUID randomUUID = UUID.randomUUID();
			boolean isConverted = false;
			long finalFileSize = mFile.getSize();
			
			try {
				byte[] content = mFile.getBytes();
				String contentStr = new String(content, StandardCharsets.UTF_8).trim();
				
				boolean isCycloneDxFile = isCycloneDX(contentStr);
				boolean isSpdxFile = isSPDX(contentStr);
				boolean isNotExcel = !fileExt.equalsIgnoreCase("xls");
				
				if (isCycloneDxFile || (isSpdxFile && isNotExcel)) {
					String tempId = "temp_" + UUID.randomUUID().toString().substring(0, 8);
					File tempFile = null;
					
					try {
						File tempDir = new File(uploadFilePath);
						if (!tempDir.exists()) {
							tempDir.mkdirs();
						}
						
						tempFile = File.createTempFile("spdx_origin_" + tempId, "." + fileExt, tempDir);
						try (InputStream is = mFile.getInputStream()) {
						    Files.copy(is, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
						    log.error("An error occurred while copying files: {}", e.getMessage());
						    
						    upFile.setUploadSucc(false);
		                	result.add(upFile);
		                	return result;
						}
						
						boolean isConvert = false;
						String uploadFileName = "";
						String convertFullStrPath = "";
						
			            if (isSpdxFile && isNotExcel) {
			            	fileExt = "xlsx";
							originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + "." + fileExt;
							uploadFileName = randomUUID + "." + fileExt;
				            convertFullStrPath = uploadFilePath + "/" + uploadFileName;
			            	
			            	try {
			            		if (("yaml").equalsIgnoreCase(originalFileExt.toLowerCase())) {
			            			isConvert = convertYamlToXls(tempFile.toPath(), Paths.get(convertFullStrPath));
			            		} else if (("rdf").equalsIgnoreCase(originalFileExt.toLowerCase()) || ("spdx").equalsIgnoreCase(originalFileExt.toLowerCase())) {
			            			SPDXUtil2.convert2(tempId, tempFile.getAbsolutePath(), convertFullStrPath);
			            		} else {
			            			tempFile = getCleanedSpdxFile(tempId, tempFile);
			            			if (tempFile == null) {
					            		upFile.setUploadSucc(false);
					            		upFile.setComments("parsing error.");
					            		result.add(upFile);
					                	return result;
					            	}
		            				SPDXUtil2.convert(tempId, tempFile.getAbsolutePath(), convertFullStrPath);
			            		}
			            	} catch (Exception e) {
			            		log.error("SPDXUtil2.convert error : {}", e.getMessage());
			            		upFile.setUploadSucc(false);
			            		upFile.setComments(e.getMessage());
			            		result.add(upFile);
			                	return result;
			            	}
			            	isConvert = new File(convertFullStrPath).exists();
			            } else {
			            	fileExt = "xls";
			            	originalFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + "." + fileExt;
							uploadFileName = randomUUID + "." + fileExt;
				            convertFullStrPath = uploadFilePath + "/" + uploadFileName;
			            	isConvert = convertCdxToExcel(tempFile, contentStr, convertFullStrPath);
			            }
			            
			            if ((isCycloneDxFile && !isConvert) || !isConvert) {
			            	upFile.setUploadSucc(false);
			            	upFile.setComments("parsing error.");
		                	result.add(upFile);
		                	return result;
			            }
			            
			            File convertedFile = new File(convertFullStrPath);
			            finalFileSize = convertedFile.length();
	                    isConverted = true;
	                    log.info("SPDX conversion successful : " + convertFullStrPath);
					} catch (Exception e) {
						log.error(e.getMessage());
					} finally {
						if (tempFile != null && tempFile.exists()) {
				            tempFile.delete();
				        }
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				
				upFile.setUploadSucc(false);
				upFile.setComments("conversion error.");
            	result.add(upFile);
            	return result;
			}
			
			File file = new File(uploadFilePath + "/" + randomUUID + "." + fileExt);
			if (mFile.getSize() != 0) {
		        if (!isConverted) {
		            if (!file.getParentFile().exists()) {
		                file.getParentFile().mkdirs();
		            }
		            uploadSucc = FileUtil.transferTo(mFile, file);
		        } else {
		            uploadSucc = file.exists();
		        }
		    }
			
			/** Return Setting **/
			upFile.setOriginalFilename(originalFileName);
			upFile.setFileName(randomUUID+"."+fileExt);
			upFile.setFileExt(fileExt);
			upFile.setInputName(fileName);
			upFile.setFilePath(uploadFilePath);
			upFile.setIndexNum(indexNum);
			upFile.setRegistFileId(fileId);
			upFile.setUploadSucc(uploadSucc);
			
			try {
				if (isConverted) {
					upFile.setContentType("application/vnd.ms-excel");
					upFile.setSize(finalFileSize);
				} else {
					upFile.setContentType(mFile.getContentType());
					upFile.setSize(mFile.getSize());
				}
			} catch (Exception e) {}
			
			if (isConverted) {
				T2File originFile = new T2File(); 
			    String originLogiNm = UUID.randomUUID() + "." + originalFileExt; 
			    originFile.setFileId(fileId);
			    originFile.setOrigNm(mFile.getOriginalFilename());
			    originFile.setLogiNm(originLogiNm);
			    originFile.setLogiPath(uploadFilePath);
			    originFile.setExt(originalFileExt);
			    originFile.setContentType(mFile.getContentType());
			    originFile.setSize(String.valueOf(mFile.getSize()));
			    originFile.setCreator(registFile.getCreator());
			    
			    try {
			    	Path destination = Paths.get(uploadFilePath).resolve(originLogiNm).toAbsolutePath();
			    	File originFileDest = destination.toFile();
			    	
			        if (!originFileDest.getParentFile().exists()) {
			            originFileDest.getParentFile().mkdirs();
			        }
			        mFile.transferTo(originFileDest);
			        
			        registFile(originFile); 
			        registFile.setRefFileSeq(originFile.getFileSeq()); 
			        log.info("Original file saved successfully. SEQ: {}", originFile.getFileSeq());
			    } catch (IOException e) {
			        log.error("Failed to save original physical file: {}", e.getMessage());
			    } catch (Exception e) {
			        log.error("DB Error while registering original file: {}", e.getMessage());
			    }
			}
			
			/** DB Regist Setting **/
			registFile.setFileId(fileId);
			registFile.setOrigNm(originalFileName);
			registFile.setLogiNm(randomUUID+"."+fileExt);
			registFile.setLogiPath(uploadFilePath);
			registFile.setLogiThumbNm(randomUUID+"_thumb."+fileExt);
			registFile.setLogiThumbPath(uploadThumbFilePath);
			registFile.setExt(fileExt);
			
			try {
				if (isConverted) {
					registFile.setGubn("CV");
					registFile.setContentType("application/vnd.ms-excel");
					registFile.setSize(String.valueOf(finalFileSize));
				} else {
					registFile.setContentType(mFile.getContentType());
					registFile.setSize(String.valueOf(mFile.getSize()));
				}
			} catch (Exception e) {}
			
			upFile.setRegistSeq(registFile(registFile));
			upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
			
			result.add(upFile);
		}
		
		if (sw){
			result = null;
		}
		
		return result;
	}
	
	private File getCleanedSpdxFile(String tempId, File tempFile) throws IOException {
		String content = Files.readString(tempFile.toPath(), StandardCharsets.UTF_8).trim();
		String fileName = tempFile.getName().toLowerCase();
		
	    if (content.startsWith("{")) {
	        return cleanJsonDirectly(tempFile);
	    } else if (content.startsWith("---") || fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
	        return cleanYamlDirectly(tempFile);
	    } else if (content.contains("<?xml") || content.contains("<rdf:RDF")) {
	        return cleanRdfXmlDirectly(tempFile);
	    } else if (content.contains("SPDXID:")) {
	        return cleanTagValueDirectly(tempFile);
	    }
	    
	    return tempFile;
	}

	public static boolean convertYamlToXls(Path yamlPath, Path xlsPath) {
		try {
            List<String> lines = Files.readAllLines(yamlPath, StandardCharsets.UTF_8);

            Map<String, Map<String, String>> packages = new LinkedHashMap<>();
            Map<String, Map<String, String>> files = new LinkedHashMap<>();
            Map<String, Map<String, String>> snippets = new LinkedHashMap<>();
            List<Map<String, String>> relationships = new ArrayList<>();

            List<String> block = new ArrayList<>();
            String currentType = null;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                	continue;
                }

                if (!line.startsWith(" ") && line.contains(":") && !line.startsWith("-")) {
                    if (!block.isEmpty()) {
                        processYamlBlock(block, currentType, packages, files, snippets, relationships);
                        block.clear();
                    }
                    currentType = line.split(":")[0].trim();
                    continue;
                }

                if (line.startsWith("- ") || (line.startsWith("  - ") && block.isEmpty())) {
                    if (!block.isEmpty()) {
                        processYamlBlock(block, currentType, packages, files, snippets, relationships);
                    }
                    block.clear();
                    block.add(line.trim().substring(2));
                } else {
                    block.add(line);
                }
            }

            if (!block.isEmpty()) {
                processYamlBlock(block, currentType, packages, files, snippets, relationships);
            }

            writeXls(xlsPath.toFile(), packages, files, snippets, relationships);
            return true;
        } catch (Exception e) {
            log.error("convertYamlToXls error : {}", e.getMessage());
            return false;
        }
    }

	private static void processYamlBlock(List<String> block, String type, Map<String, Map<String, String>> packages, Map<String, Map<String, String>> files, Map<String, Map<String, String>> snippets, List<Map<String, String>> relationships) {
	    if (block.isEmpty() || type == null) {
	    	return;
	    }

	    Map<String, String> element = new HashMap<>();
        String spdxId = null;
        String relatedSpdxId = null;
        String currentKey = null;
        List<String> licenseFromFiles = new ArrayList<>();

        for (String line : block) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
            	continue;
            }

            if (trimmed.contains(":") && !trimmed.startsWith("-")) {
                String[] parts = trimmed.split(":", 2);
                String key = parts[0].trim();
                String value = (parts.length > 1) ? parts[1].trim().replaceAll("^\"|\"$", "") : "";
                if (key.equalsIgnoreCase("licenseInfoFromFiles")) {
                	continue;
            	}
                currentKey = key;
                if (key.equalsIgnoreCase("SPDXID")) {
                	spdxId = value;
                } else if (key.equalsIgnoreCase("relatedSpdxElement")) {
                	relatedSpdxId = value;
                } else {
                	element.put(key, value);
                }
            } else if (trimmed.startsWith("-")) {
                String val = trimmed.substring(1).trim().replaceAll("^\"|\"$", "");
                if ("licenseInfoFromFiles".equals(currentKey)) {
                    licenseFromFiles.add(val);
                } else if (currentKey != null) {
                    String existing = element.getOrDefault(currentKey, "");
                    element.put(currentKey, existing.isEmpty() ? val : existing + ", " + val);
                }
            } else if (currentKey != null) {
                String val = trimmed.replaceAll("^\"|\"$", "");
                String existing = element.getOrDefault(currentKey, "");
                element.put(currentKey, (existing + "\n" + val).trim());
            }
        }

        if (spdxId == null) {
        	return;
        }
        element.put("SPDXID", spdxId);
        if (!licenseFromFiles.isEmpty()) {
            element.put("licenseInfoFromFiles", String.join(", ", licenseFromFiles));
        }

        switch (type) {
        	case "packages": packages.put(spdxId, element); break;
	        case "files":    files.put(spdxId, element);    break;
	        case "snippets": snippets.put(spdxId, element); break;
	        case "relationships":
	        	Map<String, String> rel = new HashMap<>();
	            rel.put("spdxElementId", spdxId);
	            rel.put("relatedSpdxElement", relatedSpdxId != null ? relatedSpdxId : "");
	            rel.put("relationshipType", element.getOrDefault("relationshipType", ""));
	            relationships.add(rel);
	            break;
        }
	}

    private static void writeXls(File xlsFile, Map<String, Map<String, String>> packages, Map<String, Map<String, String>> files, Map<String, Map<String, String>> snippets, List<Map<String, String>> relationships) throws Exception {
    	String templatePath = CommonFunction.emptyCheckProperty("export.template.path", "/template");
    	File templateFile = new File(templatePath + "/SPDXRdf_2.2.2.xls");
	    
	    try (FileInputStream fis = new FileInputStream(templateFile);
	    	Workbook workbook = WorkbookFactory.create(fis);
	    	FileOutputStream fos = new FileOutputStream(xlsFile)) {
	    	
	    	createSheet(workbook, "Package Info", packages, 1);

	        workbook.write(fos);
	        fos.flush();
	    } catch (Exception e) {
	        log.error("writeXls failed : " + e.getMessage());
	    }
    }

    private static void createSheet(Workbook workbook, String sheetName, Map<String, Map<String, String>> data, int startRow) {
    	Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
        	sheet = workbook.createSheet(sheetName);
        }

        int rowIdx = startRow;
        for (Map<String, String> c : data.values()) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) row = sheet.createRow(rowIdx);
            rowIdx++;

            setCellValue(row, 0, c.getOrDefault("name", ""));
            setCellValue(row, 1, c.getOrDefault("SPDXID", ""));
            setCellValue(row, 2, c.getOrDefault("versionInfo", ""));
            setCellValue(row, 4, c.getOrDefault("supplier", "NOASSERTION"));
            setCellValue(row, 5, c.getOrDefault("originator", "NOASSERTION"));
            setCellValue(row, 6, c.getOrDefault("homepage", "NONE"));
            setCellValue(row, 7, c.getOrDefault("downloadLocation", "NOASSERTION"));
            setCellValue(row, 12, c.getOrDefault("licenseDeclared", "NOASSERTION"));
            setCellValue(row, 13, c.getOrDefault("licenseConcluded", "NOASSERTION"));
            setCellValue(row, 14, c.getOrDefault("licenseInfoFromFiles", "NOASSERTION"));
            setCellValue(row, 16, c.getOrDefault("copyrightText", "NOASSERTION"));
            
            String filesAnalyzed = String.valueOf(c.getOrDefault("filesAnalyzed", "FALSE")).toUpperCase();
            setCellValue(row, 20, filesAnalyzed);
        }
    }

    private static void setCellValue(Row row, int cellIdx, String value) {
        Cell cell = row.getCell(cellIdx);
        if (cell == null) {
        	cell = row.createCell(cellIdx);
        }
        cell.setCellValue(value);
    }
    
	private File cleanJsonDirectly(File file) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			JsonNode rootNode = mapper.readTree(file);
	        ObjectNode root = null;
		    
		    if (!rootNode.has("spdxVersion")) {
	            boolean found = false;
	            Iterator<JsonNode> elements = rootNode.elements();
	            while (elements.hasNext()) {
	                JsonNode child = elements.next();
	                if (child.isObject() && child.has("spdxVersion")) {
	                    root = (ObjectNode) child;
	                    found = true;
	                    log.info("Wrapped SPDX data detected and normalized from a child node.");
	                    break;
	                }
	            }
	            if (!found) {
	                log.error("cleanJsonDirectly: Could not find valid SPDX content (spdxVersion missing).");
	                return null;
	            }
	        } else {
	            root = (ObjectNode) rootNode;
	        }
		    
		    String[] globalFieldsToRemove = {"relationships", "snippets", "annotations", "externalDocumentRefs", "hasExtractedLicensingInfos", "reviewers"};
		    for (String field : globalFieldsToRemove) {
		    	root.remove(field);
	    		log.info("Field '{}' removed to optimize Excel conversion and prevent row limits.", field);
		    }
		    
		    if (root.has("packages") && root.get("packages").isArray()) {
		        ArrayNode packages = (ArrayNode) root.get("packages");
		        for (JsonNode pkgNode : packages) {
		            if (pkgNode.isObject()) {
		                ObjectNode pkg = (ObjectNode) pkgNode;
		                
		                if (pkg.has("externalRefs")) {
		                    pkg.remove("externalRefs");
		                    log.debug("ExternalRefs removed from package: {}", pkg.path("name").asText());
		                }

		                pkg.remove("relationships");
		                pkg.remove("annotations");
		                pkg.remove("attributionText");
		            }
		        }
		    }
		    
//		    Set<String> validIds = new HashSet<>();
//		    
//		    if (root.has("packages")) {
//		        for (JsonNode pkg : root.get("packages")) {
//		        	validIds.add(pkg.path("SPDXID").asText());
//		        }
//		    }
//		    validIds.add(root.path("SPDXID").asText());
//
//		    if (root.has("relationships")) {
//		        ArrayNode rels = (ArrayNode) root.get("relationships");
//		        for (int i = rels.size() - 1; i >= 0; i--) {
//		            String target = rels.get(i).path("relatedSpdxElement").asText();
//		            if (!validIds.contains(target) && !target.equals("NONE")) {
//		            	rels.remove(i);
//		            }
//		        }
//		    }
		    mapper.writeValue(file, root);
		} catch (Exception e) {
			log.error("cleanJsonDirectly: {}", e.getMessage());
			return null;
		}
	    return file;
	}

	private File cleanYamlDirectly(File file) {
	    Path originalPath = file.toPath();
	    
	    try {
	    	List<String> lines = Files.readAllLines(originalPath, StandardCharsets.UTF_8);
	        Set<String> validIds = collectValidSpdxIds(lines);

	        List<String> result = new ArrayList<>();
	        List<String> relBlock = new ArrayList<>();

	        boolean insideRelationships = false;
	        int relationshipsIndent = -1;

	        for (String line : lines) {

	            String trimmed = line.trim();
	            int currentIndent = countIndent(line);

	            if (!insideRelationships && trimmed.startsWith("relationships:")) {
	                insideRelationships = true;
	                relationshipsIndent = currentIndent;
	                result.add(line);
	                continue;
	            }

	            if (insideRelationships) {
	                if (currentIndent <= relationshipsIndent && !trimmed.startsWith("-")) {
	                    processRelationshipBlock(relBlock, result, validIds);
	                    relBlock.clear();
	                    insideRelationships = false;
	                    result.add(line);
	                    continue;
	                }

	                if (trimmed.startsWith("-")) {
	                    processRelationshipBlock(relBlock, result, validIds);
	                    relBlock.clear();
	                }

	                relBlock.add(line);

	            } else {
	                result.add(line);
	            }
	        }

	        processRelationshipBlock(relBlock, result, validIds);

	        Files.write(originalPath, result, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

	        log.info("SPDX YAML clean 완료: {}", file.getName());

	    } catch (Exception e) {
	        log.error("cleanYamlDirectly error: ", e);
	        return null;
	    }

	    return file;
	}
	
	private Set<String> collectValidSpdxIds(List<String> lines) {
	    Set<String> validIds = new HashSet<>();

	    for (String line : lines) {
	        String trimmed = line.trim();
	        if (trimmed.startsWith("SPDXID:")) {

	            int idx = trimmed.indexOf(":");
	            if (idx >= 0 && trimmed.length() > idx + 1) {

	                String id = trimmed.substring(idx + 1).trim();

	                if (!id.isEmpty()) {
	                    validIds.add(id);
	                }
	            }
	        }
	    }

	    log.info("수집된 SPDXID 개수: {}", validIds.size());
	    return validIds;
	}
	
	private void processRelationshipBlock(List<String> block, List<String> result, Set<String> validIds) {
		if (block.isEmpty()) {
			return;
		}

		String spdxElementId = null;
		String relatedSpdxElement = null;

		for (String line : block) {

			String trimmed = line.trim();

			if (trimmed.startsWith("spdxElementId:")) {
				spdxElementId = extractValue(trimmed);
			}

			if (trimmed.startsWith("relatedSpdxElement:")) {
				relatedSpdxElement = extractValue(trimmed);
			}
		}

		boolean isValid = true;

		if (spdxElementId != null && !isSpecialId(spdxElementId) && !validIds.contains(spdxElementId)) {
			isValid = false;
		}

		if (relatedSpdxElement != null && !isSpecialId(relatedSpdxElement) && !validIds.contains(relatedSpdxElement)) {
			isValid = false;
		}

		if (isValid) {
			result.addAll(block);
		} else {
			log.warn("제거된 잘못된 relationship: spdxElementId={}, relatedSpdxElement={}", spdxElementId, relatedSpdxElement);
		}
	}
	
	private String extractValue(String line) {
	    int idx = line.indexOf(":");
	    if (idx >= 0 && line.length() > idx + 1) {
	        return line.substring(idx + 1).trim();
	    }
	    return null;
	}

	private boolean isSpecialId(String id) {
	    return id.equals("NONE") || id.equals("NOASSERTION") || id.startsWith("DocumentRef-");
	}

	private int countIndent(String line) {
	    int count = 0;
	    while (count < line.length() && line.charAt(count) == ' ') {
	        count++;
	    }
	    return count;
	}
	
	private File cleanRdfXmlDirectly(File file) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    Document doc = builder.parse(file);

		    Set<String> validIds = new HashSet<>();
		    NodeList nodes = doc.getElementsByTagName("*");
		    for (int i = 0; i < nodes.getLength(); i++) {
		        Element el = (Element) nodes.item(i);
		        String id = el.getAttribute("rdf:about");
		        if (id.contains("#SPDXRef-")) validIds.add(id.substring(id.indexOf("#") + 1));
		        if (el.hasAttribute("spdx:spdxId")) validIds.add(el.getAttribute("spdx:spdxId"));
		    }

		    NodeList rels = doc.getElementsByTagName("spdx:Relationship");
		    for (int i = rels.getLength() - 1; i >= 0; i--) {
		        Element rel = (Element) rels.item(i);
		        NodeList targets = rel.getElementsByTagName("spdx:relatedSpdxElement");
		        if (targets.getLength() > 0) {
		            String targetRef = ((Element) targets.item(0)).getAttribute("rdf:resource");
		            String targetId = targetRef.contains("#") ? targetRef.substring(targetRef.indexOf("#") + 1) : targetRef;
		            
		            if (!validIds.contains(targetId) && !targetId.equals("NONE")) {
		                rel.getParentNode().removeChild(rel);
		            }
		        }
		    }

		    Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.transform(new DOMSource(doc), new StreamResult(file));
		} catch (Exception e) {
			log.error("cleanRdfXmlDirectly: {}", e.getMessage());
			return null;
		}
	    return file;
	}
	
	private File cleanTagValueDirectly(File file) {
		try {
			List<String> lines = Files.readAllLines(file.toPath());
		    Set<String> validIds = new HashSet<>();
		    for (String line : lines) {
		        if (line.startsWith("SPDXID:")) validIds.add(line.replace("SPDXID:", "").trim());
		    }
		    List<String> cleaned = new ArrayList<>();
		    for (String line : lines) {
		        if (line.startsWith("Relationship:")) {
		            String[] parts = line.split("\\s+");
		            if (parts.length >= 4 && validIds.contains(parts[1]) && (validIds.contains(parts[3]) || parts[3].equals("NONE"))) {
		                cleaned.add(line);
		            }
		        } else {
		            cleaned.add(line);
		        }
		    }
		    Files.write(file.toPath(), cleaned);
		} catch (Exception e) {
			log.error("cleanTagValueDirectly: {}", e.getMessage());
			return null;
		}
	    return file;
	}
	
	private boolean convertCdxToExcel(File tempFile, String contentStr, String convertFullStrPath) throws Exception {
		String templatePath = CommonFunction.emptyCheckProperty("export.template.path", "/template");
		
	    Bom bom;
	    try {
	    	byte[] fileBytes = Files.readAllBytes(tempFile.toPath());
	        if (fileBytes.length == 0) {
	            log.error("The file contents are empty.");
	            return false;
	        }
	        if (contentStr.trim().startsWith("<")) {
	        	bom = new org.cyclonedx.parsers.XmlParser().parse(fileBytes);
	        } else {
	            bom = new org.cyclonedx.parsers.JsonParser().parse(fileBytes);
	        }
	    } catch (Exception e) {
	        log.error("Actual Exception Class: {}", e.getClass().getName());
	        
	        if (e.getCause() != null) {
	            log.error("Caused by: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
	        }

	        log.error("Error Message: {}", e.getMessage());
	        return false;
	    }

	    File resultFile = new File(convertFullStrPath);
	    File templateFile = new File(templatePath + "/SPDXRdf_2.2.2.xls");
	    
	    try (FileInputStream fis = new FileInputStream(templateFile);
	    	Workbook workbook = WorkbookFactory.create(fis);
	    	FileOutputStream fos = new FileOutputStream(resultFile)) {

	        Sheet pkgSheet = workbook.getSheet("Package Info");
	        Sheet sheetExternalRefs = workbook.getSheet("External Refs");
	        Sheet sheetRelationships = workbook.getSheet("Relationships");
	        
	        if (pkgSheet == null) {
	        	throw new Exception("Could not find 'Package Info' sheet in template.");
	        }

	        int rowIdx = 1;
	        Map<String, String> externalRefsMap = new HashMap<>();
	        Map<String, Object> relationshipsMap = new HashMap<>();
	        List<String> packageInfoidentifierList = new ArrayList<>();
	        
	        if (bom.getComponents() != null) {
	            for (Component c : bom.getComponents()) {
	                Row row = pkgSheet.createRow(rowIdx++);

	                // [A] Package Name (0)
	                row.createCell(0).setCellValue(c.getName());

	                // [B] SPDX Identifier (1)
	                String spdxId = (c.getBomRef() != null) ? "SPDXRef-" + c.getBomRef() : "SPDXRef-Package-" + UUID.randomUUID().toString().substring(0, 8);
	                row.createCell(1).setCellValue(spdxId);

	                // [C] Package Version (2)
	                row.createCell(2).setCellValue(c.getVersion() != null ? c.getVersion() : "");

	                // [D] Package FileName (3) - 보통 Name과 동일하게 처리
	                row.createCell(3).setCellValue(c.getName());

	                // [E] Package Supplier (4)
	                row.createCell(4).setCellValue(c.getPublisher() != null ? "Organization: " + c.getPublisher() : "");

	                // [F] Package Originator (5)
	                row.createCell(5).setCellValue("");

	                String targetUrl = "NOASSERTION";
	                if (c.getExternalReferences() != null) {
	                    for (ExternalReference ref : c.getExternalReferences()) {
	                        if (ref.getType() != null && "website".equalsIgnoreCase(ref.getType().getTypeName())) {
	                        	targetUrl = ref.getUrl();
	                            break;
	                        }
	                    }
	                }
	                // [G] Home Page (6) - Website URL 추출
	                row.createCell(6).setCellValue("");

	                // [H] Package Download Location (7)
	                row.createCell(7).setCellValue(targetUrl);

	                // [N] License Declared (13) - 첫 번째 라이선스 ID 추출
	                String licenseStr = "NOASSERTION";
	                
	                org.cyclonedx.model.LicenseChoice lc = c.getLicenses();
	                if (lc != null) {
	                    List<String> licenseNames = new ArrayList<>();

	                    if (lc.getLicenses() != null && !lc.getLicenses().isEmpty()) {
	                        for (org.cyclonedx.model.License l : lc.getLicenses()) {
	                            // JSON 구조의 id ("BSL-1.0") 또는 name 추출
	                            String idOrName = (l.getId() != null) ? l.getId() : l.getName();
	                            if (idOrName != null) {
	                                licenseNames.add(idOrName);
	                            }
	                        }
	                    } else if (lc.getExpression() != null) {
	                        licenseNames.add(String.valueOf(lc.getExpression()));
	                    }
	                    if (!licenseNames.isEmpty()) {
	                        licenseStr = String.join(", ", licenseNames);
	                    }
	                }
	                row.createCell(13).setCellValue(licenseStr);

	                // [R] Package Copyright Text (17)
	                row.createCell(17).setCellValue(c.getCopyright() != null ? c.getCopyright() : "");

	                // [V] Files Analyzed (21) - 대문자 FALSE 기입
	                row.createCell(21).setCellValue("FALSE");
	                
	                packageInfoidentifierList.add(spdxId);
	                if (!isEmpty(c.getPurl())) {
	                	relationshipsMap.put(c.getPurl(), spdxId);
	                	externalRefsMap.put(spdxId, c.getPurl());
	                }
	            }
	        }
	        
	        // External Refs
	     	{
	     		if (MapUtils.isNotEmpty(externalRefsMap)) {
					rowIdx = 1;

					for (String key : externalRefsMap.keySet()) {
						Row row = sheetExternalRefs.getRow(rowIdx);

						if (row == null) {
							row = sheetExternalRefs.createRow(rowIdx);
						}
									
						int cellIdx = 0;

						// Package ID
						Cell cellPackageId = row.createCell(cellIdx); cellIdx++;
						cellPackageId.setCellValue(key);
									
						// Category
						Cell cellCategory = row.createCell(cellIdx); cellIdx++;
						cellCategory.setCellValue("PACKAGE_MANAGER");
									
						// Type
						Cell cellType = row.createCell(cellIdx); cellIdx++;
						cellType.setCellValue("purl");
									
						// Locator
						Cell cellLocator = row.createCell(cellIdx); cellIdx++;
						cellLocator.setCellValue(externalRefsMap.get(key));
									
						// Comment
						cellIdx++;
									
						// User Defined
						cellIdx++;
						
						rowIdx++;
					}
				}
	     	}
	     	
	     	// Relationships
	        if (bom.getDependencies() != null) {
	        	rowIdx = 1;

				for (String _identifierB : packageInfoidentifierList) {
					int cellIdx = 0;

					Row row = sheetRelationships.getRow(rowIdx);
					if (row == null) {
						row = sheetRelationships.createRow(rowIdx);
					}
					// SPDX Identifier A
					Cell spdxIdentifierA = row.createCell(cellIdx); cellIdx++;
					spdxIdentifierA.setCellValue("SPDXRef-DOCUMENT");

					// Relationship
					Cell relationship = row.createCell(cellIdx); cellIdx++;
					relationship.setCellValue("DESCRIBES");

					// SPDX Identifier B
					Cell spdxIdentifierB = row.createCell(cellIdx); cellIdx++;
					spdxIdentifierB.setCellValue(_identifierB);

					rowIdx++;
				}
	        	
				for (org.cyclonedx.model.Dependency dep : bom.getDependencies()) {
					String key = dep.getRef();
					if (relationshipsMap.containsKey(key) && CollectionUtils.isNotEmpty(dep.getDependencies())) {
						String spdxElementId = (String) relationshipsMap.get(key);
						for (org.cyclonedx.model.Dependency dependency : dep.getDependencies()) {
							String relatedSpdxElementKey = dependency.getRef();
							if (relationshipsMap.containsKey(relatedSpdxElementKey)) {
								String relatedSpdxElement = String.valueOf(relationshipsMap.getOrDefault(relatedSpdxElementKey, ""));
								int cellIdx = 0;

								Row row = sheetRelationships.getRow(rowIdx);
								if (row == null) {
									row = sheetRelationships.createRow(rowIdx);
								}
								// SPDX Identifier A
								Cell spdxIdentifierA = row.createCell(cellIdx); cellIdx++;
								spdxIdentifierA.setCellValue(spdxElementId);

								// Relationship
								Cell relationship = row.createCell(cellIdx); cellIdx++;
								relationship.setCellValue("DEPENDS_ON");

								// SPDX Identifier B
								Cell spdxIdentifierB = row.createCell(cellIdx); cellIdx++;
								spdxIdentifierB.setCellValue(relatedSpdxElement);

								rowIdx++;
							}
						}
					}
				}
			}
	        
	        workbook.write(fos);
	        fos.flush();
	        log.info("convertCdxToExcel success : " + resultFile.getAbsolutePath());
	        
	        return true;
	    } catch (Exception e) {
	        log.error("convertCdxToExcel failed : " + e.getMessage());
	        return false;
	    }
	}

	private boolean isSPDX(String content) {
		return content.contains("SPDXVersion:") || content.contains("\"spdxVersion\"") || content.contains("spdxVersion:") || content.contains("<spdx:SpdxDocument");
	}

	private boolean isCycloneDX(String content) {
		return content.contains("\"bomFormat\"") && content.contains("\"CycloneDX\"") || content.contains("<bom") && content.contains("cyclonedx");
	}
	
	//파일 DB 등록
	public String registFile(T2File file) {
		int result = fileMapper.insertFile(file);
		
		if (result <= 0){
			return null;
		}
		
		return file.getFileSeq();
	}

	@Override
	public T2File selectFileInfo(String fileSeq) {
		T2File file = fileMapper.selectFileInfo(fileSeq);
		
		return file;
	}
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, boolean useRandomPath, String filePath) {
		return uploadFile(req, registFile, inputFileName, "", useRandomPath, filePath);
	}


	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, String oldFileId, boolean useRandomPath, String filePath){
		return uploadFile(req, registFile, inputFileName, "", useRandomPath, filePath, false);
	}
	
	@Override
	public List<UploadFile> uploadFile(HttpServletRequest req, T2File registFile, String inputFileName, String oldFileId, boolean useRandomPath, String filePath, boolean isOrigFile) {
		List<UploadFile> result = new ArrayList<UploadFile>();
		MultipartHttpServletRequest multipartRequest = null;
		
		try {
			// request가 multipartRequest가 아닐 경우.
			multipartRequest = (MultipartHttpServletRequest)req;
		} catch(Exception e) {
			log.error("Request IS NOT a type of MultipartRequest : " + e.getMessage());
			return result;
		}
		
		// request에서 파일명들을 가져온다.
		java.util.Iterator<String> fileNames = multipartRequest.getFileNames();
		
		boolean sw = true;
		String fileId = "";
		
		//구 fileId가 존제 한다면 구 fileId에 넣어준다
		if (!isEmpty(oldFileId)){
			fileId = oldFileId;
		} else {
			fileId = avoidNull(fileMapper.getFileId(), "1");
		}
		
		log.debug("Target fileId : " + fileId);

		int indexNum = 0;
		
		while (fileNames.hasNext()){
			UploadFile upFile = new UploadFile();
			registFile.setCreator(registFile.getCreator());
			boolean uploadSucc = true;
			String fileName = fileNames.next();		//input name
			
			if (inputFileName != null){
				String inputFileNameRe = inputFileName.replace("##]", "");
				int st = fileName.indexOf("[");
				int en = fileName.indexOf("]");
				
				try {
					indexNum = Integer.parseInt(fileName.substring(st+1, en));
				} catch(Exception e) {
					log.error("[##] NumberFormat Exception : " + e.getMessage());
				}
				
				log.debug("indexNum : " + indexNum + ", fileName : " + fileName);
				
				boolean isInput = fileName.startsWith(inputFileNameRe);
				
				if (!isInput || inputFileName == null){
					continue;
				}
			}
			
			sw=false;
			
			MultipartFile mFile = multipartRequest.getFile(fileName);

			if (isEmpty(mFile.getOriginalFilename())) {
				throw new RuntimeException("File Name is empty");
			}
			
			if (mFile.getSize() <= 0) {
				throw new RuntimeException("File Size is 0");
			}
			
			String originalFileName = avoidNull(registFile.getBeforeOrigNm(), mFile.getOriginalFilename());	//Original File name

			// originalFileName에 경로가 포함되어 있는 경우 처리
			log.debug("File upload OriginalFileName : " + originalFileName);
			
			if (originalFileName.indexOf("/") > -1) {
				originalFileName = originalFileName.substring(originalFileName.lastIndexOf("/") + 1);
				
				log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
			}
			if (originalFileName.indexOf("\\") > -1) {
				originalFileName = originalFileName.substring(originalFileName.lastIndexOf("\\") + 1);
				
				log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
			}
			
			String fileExt = FilenameUtils.getExtension(originalFileName);
			
			if (originalFileName.toLowerCase().endsWith(".tgz.gz")) {
				fileExt = "tgz.gz";
			} else if (originalFileName.toLowerCase().endsWith(".tar.bz2")) {
				fileExt = "tar.bz2";
			} else if (originalFileName.toLowerCase().endsWith(".tar.gz")) {
				fileExt = "tar.gz";
			}
			
			String uploadFilePath = "";
			String uploadThumbFilePath = "";
			
			try{
				/* 파일 저장 경로 설정 hk-cho */
				// 1) parameter로 filePath가 넘어오지 않았을 경우 Property에 있는 경로에 저장
				if (StringUtil.isEmpty(filePath)) {
					uploadFilePath = appEnv.getProperty("upload.path", "/upload");
					uploadThumbFilePath = appEnv.getProperty("image.path", "/image");
				} else { // 2) parameter로 filePath가 넘어왔을 경우 넘어온 filePath에 저장
					uploadFilePath = filePath;
					uploadThumbFilePath = filePath + "/" + "thumb";
				}
				
				log.debug("uploadFilePath : " + uploadFilePath);
			} catch(Exception e) {
				log.error("Wrong file upload path(get properties) : " + e.getMessage());
			}
			
			/* 랜덤 파일명 사용 여부 hk-cho */
			String phyFileNm = originalFileName;						// 서버에 저장될 물리 파일명
			String thumbPhyFileNm = phyFileNm+"_thumb."+fileExt;		// 서버에 저장될 thumb 물리 파일명
			
			if (useRandomPath){
				UUID randomUUID = UUID.randomUUID();
				phyFileNm = randomUUID+"."+fileExt;
				thumbPhyFileNm = randomUUID+"_thumb."+fileExt;
			}
			
			/** Return Setting **/
			upFile.setOriginalFilename(originalFileName);
			upFile.setInputName(fileName);
			upFile.setSize(mFile.getSize());
			upFile.setFilePath(uploadFilePath);
			upFile.setFileExt(fileExt);
			upFile.setFileName(phyFileNm);
			upFile.setIndexNum(indexNum);
			upFile.setRegistFileId(fileId);
			if (!isEmpty(registFile.getActualFileNm())) {
				upFile.setActualFilename(registFile.getActualFileNm());
				registFile.setActualFileNm(null);
			}
			
			try {
				upFile.setContentType(mFile.getContentType());
			} catch (Exception e) {}
			
			/** DB Regist Setting **/
			registFile.setFileId(fileId);
			registFile.setOrigNm(originalFileName);
			registFile.setLogiNm(phyFileNm);
			registFile.setLogiPath(uploadFilePath);
			registFile.setLogiThumbNm(thumbPhyFileNm);
			registFile.setLogiThumbPath(uploadThumbFilePath);
			registFile.setExt(fileExt);
			registFile.setSize(mFile.getSize()+"");

			try {
				registFile.setContentType(mFile.getContentType());
			} catch (Exception e) {}
			
			if (mFile.getSize()!=0){ //File Null Check
				new File(filePath).mkdirs();
				
				if (isOrigFile) {
					uploadSucc = FileUtil.transferTo(mFile, new File(filePath + "/" + originalFileName));
				} else {
					uploadSucc = FileUtil.transferTo(mFile, new File(filePath + "/" + phyFileNm));
				}
				
				if (uploadSucc){
					try {
						String regiSeq = registFile(registFile);
						
						upFile.setRegistSeq(regiSeq); //새로추가된 SEQ(primaryKey a.i)
						upFile.setRegistFileId(fileId); //FileId(group개념의 컬럼)
						upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
					} catch(Exception e) {
						log.error("file regist error : " + e.getMessage());
						uploadSucc=false;
					}
				}
				
				upFile.setUploadSucc(uploadSucc);
			}
			
			result.add(upFile);
		}
		
		if (sw){
			result = null;
		}
		
		return result;
	}

	@Override
	public String registFileWithFileName(String filePath, String fileName) {
		T2File fileInfo = new T2File();
		fileInfo.setFileId(fileMapper.getFileId());
		fileInfo.setOrigNm(fileName);
		fileInfo.setLogiNm(fileName);
		fileInfo.setLogiPath(filePath);
		fileInfo.setExt(FilenameUtils.getExtension(fileName));
		
		try {
			if (fileName.toLowerCase().endsWith(".tgz.gz")) {
				fileInfo.setExt("tgz.gz");
			} else if (fileName.toLowerCase().endsWith(".tar.bz2")) {
				fileInfo.setExt("tar.bz2");
			} else if (fileName.toLowerCase().endsWith(".tar.gz")) {
				fileInfo.setExt("tar.gz");
			}
		} catch (Exception e) {
			//TODO: handle exception
			log.error("file regist error : " + e.getMessage());
		}

		fileInfo.setSize("1");
		
		return registFile(fileInfo);
	}

	@Override
	public String registFileDownload(String filePath, String fileName, String logiFileName) {
		T2File fileInfo = new T2File();
		fileInfo.setFileId(fileMapper.getFileId());
		fileInfo.setGubn(CoConstDef.FILE_GUBUN_FILE_DOWNLOAD);
		fileInfo.setOrigNm(fileName);
		fileInfo.setLogiNm(logiFileName);
		fileInfo.setLogiPath(filePath);
		fileInfo.setExt(FilenameUtils.getExtension(fileName));
		
		try {
			if (avoidNull(fileName.toLowerCase()).endsWith(".tgz.gz")) {
				fileInfo.setExt("tgz.gz");
			} else if (avoidNull(fileName.toLowerCase()).endsWith(".tar.bz2")) {
				fileInfo.setExt("tar.bz2");
			} else if (avoidNull(fileName.toLowerCase()).endsWith(".tar.gz")) {
				fileInfo.setExt("tar.gz");
			}
		} catch (Exception e) {
			//TODO: handle exception
			log.error("file regist error : " + e.getMessage());
		}
		
		fileInfo.setSize("1");
		
		return registFile(fileInfo);
	}
	
	@Override
	public T2File selectFileInfoById(String fileId) {
		return fileMapper.selectFileInfoById(fileId);
	}

	@Override
	public String copyFileInfo(String orgFileId) {
		T2File fileInfo = new T2File();
		fileInfo.setFileId(fileMapper.getFileId());
		fileInfo.setOrgFileId(orgFileId);
		
		fileMapper.copyFileInfo(fileInfo);
		
		return fileInfo.getFileId();
	}

	@Override
	public T2File selectFileInfoByLogiName(T2File bean) {
		return fileMapper.selectFileInfoByName(bean);
	}
	
	//wgetUrl 파일 upload
	@Override
	public List<UploadFile> uploadWgetFile(HttpServletRequest req, T2File registFile, Map<Object, Object> map, boolean isOrigFile) {
		List<UploadFile> result = new ArrayList<UploadFile>();
		log.debug("<-------- uploadWgetFile Start------->");
		/** S: 파일 업로드 **/
		String url = (String) map.get("wgetUrl");
		String filePath = (String) map.get("filePath");
		String prjId = (String) map.get("prjId");
		String uploadFilePath = "";
		String uploadThumbFilePath = "";
		boolean uploadSucc = true;
		UploadFile upFile = new UploadFile();
		
		if (StringUtil.isEmpty(filePath)){
			try {
				uploadFilePath = appEnv.getProperty("packaging.path", "/upload/packaging") + "/" + prjId;
				uploadThumbFilePath = appEnv.getProperty("packaging.path", "/upload/packaging") + "/" + prjId + "/thumb";
				
				File dir = new File(uploadFilePath);
				
				if (!dir.exists()){
					dir.mkdirs();
				}
			} catch(Exception e) {
				log.error("file upload path(get properties) : " + e.getMessage());
			}
		} else {
			uploadFilePath = filePath;
			uploadThumbFilePath = filePath + File.separator + "thumb";
			File dir = new File(filePath);
			 
	        if (!dir.exists()) { //폴더 없으면 폴더 생성
	            dir.mkdirs();
	        }
		}
		
		int ShellCommanderResult = 9;
		int indexNum = 0;
		// Url의 index 다음 문자열 부터 분리후 저장
		String originalFileName = avoidNull(url).trim();
		
		if (originalFileName.indexOf("/") > -1) {
			originalFileName = originalFileName.substring(originalFileName.lastIndexOf('/') + 1);
		}
		
		int i = originalFileName.lastIndexOf('.'); 
	    // 마지막 .부터 나머지 문자열을 f에 저장
		String fileName = "";		//input name
		String fileExt = "";
		
		if (i > -1) {
			fileName = originalFileName.substring(0,i);
			fileExt = FilenameUtils.getExtension(originalFileName);
		} else {
			fileName = originalFileName;
		}
		
		if (originalFileName.toLowerCase().endsWith(".tgz.gz")) {
			fileExt = "tgz.gz";
		} else if (originalFileName.toLowerCase().endsWith(".tar.bz2")) {
			fileExt = "tar.bz2";
		} else if (originalFileName.toLowerCase().endsWith(".tar.gz")) {
			fileExt = "tar.gz";
		}
		
		UUID randomUUID = UUID.randomUUID();
		
		log.info("WGET STart");
		log.info("WGET URL : " + url);
		log.info("WGET FileName : " + originalFileName);
		log.info("WGET Save as File Name :" + randomUUID+"."+fileExt);
		
		//주소에서 파일 가져오기
		// 네트워크 상황에 따라서 대용량 파일을 정상적으로 다운로드 받지 못하는 현상이 발생하여 (유추) NIO 방식으로 변경함
		ReadableByteChannel readChannel = null;
		FileChannel writeChannel = null;
		FileOutputStream fileOS = null;
		
		try {
			ignoreSsl();
			readChannel = Channels.newChannel(new URL(url.replaceAll("\\s", "%20")).openStream());
			
			if (isOrigFile) {
				if (i > -1) {
					fileOS = new FileOutputStream(uploadFilePath+"/"+fileName+"."+fileExt);
				} else {
					fileOS = new FileOutputStream(uploadFilePath+"/"+originalFileName+".so");
				}
			} else {
				fileOS = new FileOutputStream(uploadFilePath+"/"+randomUUID+"."+fileExt);
			}
			  
			writeChannel = fileOS.getChannel(); 
			writeChannel.transferFrom(readChannel, 0, Long.MAX_VALUE);
			ShellCommanderResult = 0;
		} catch (Exception e) {
			log.error(e.getMessage());
			ShellCommanderResult = -1;
		} finally {
			if (writeChannel != null) {
				try {
					writeChannel.close();
				} catch (Exception e) {
					log.debug(e.getMessage(), e);
				}
			}
			
			if (fileOS != null) {
				try {
					fileOS.close();
				} catch (Exception e) {
					log.debug(e.getMessage(), e);
				}
			}
			
			if (readChannel != null) {
				try {
					readChannel.close();
				} catch (Exception e) {
					log.debug(e.getMessage(), e);
				}
			}
		}
		
		if (ShellCommanderResult == 0){
			File getfile = new File(uploadFilePath+"/"+randomUUID+"."+fileExt);
			long fileSize = getfile.length();
			
			String fileId = "";
			//fileId
			fileId = avoidNull(fileMapper.getFileId(), "1");
				
			/** Return Setting **/
			upFile.setOriginalFilename(originalFileName);
			upFile.setInputName(fileName);
			upFile.setSize(fileSize);
			upFile.setFilePath(uploadFilePath);
			upFile.setFileName(randomUUID+"."+fileExt);
			upFile.setIndexNum(indexNum);
			upFile.setRegistFileId(fileId);
			
			try {
				upFile.setContentType(fileExt);
			} catch (Exception e) {}
				
			/** DB Regist Setting **/
			registFile.setFileId(fileId);
			registFile.setOrigNm(originalFileName);
			registFile.setLogiNm(randomUUID+"."+fileExt);
			registFile.setLogiPath(uploadFilePath);
			registFile.setLogiThumbNm(randomUUID+"_thumb."+fileExt);
			registFile.setLogiThumbPath(uploadThumbFilePath);
			registFile.setExt(fileExt);
			registFile.setSize(fileSize+"");
			
			try {
				registFile.setContentType(fileExt);
			} catch (Exception e) {}
			
			upFile.setRegistSeq(registFile(registFile));
			upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
			
		} else {
			uploadSucc = false;
		}
		
		upFile.setUploadSucc(uploadSucc);
		upFile.setWgetResult(ShellCommanderResult);
		result.add(upFile);

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String setClearFiles(Map<Object, Object> map) {
		String deleteComment = "";
		String uploadComment = "";
		String prjId = (String) map.get("prjId");
		List<String> fileSeqs =	(List<String>)map.get("fileSeqs");
		List<T2File> uploadFileInfos = new ArrayList<T2File>();
		File file = null;
		
		Project prjParam = new Project();
		prjParam.setPrjId(prjId);
		ArrayList<String> newPackagingFileIdList = new ArrayList<String>();
		newPackagingFileIdList.add(fileSeqs.size() > 0 ? fileSeqs.get(0) : null);
		newPackagingFileIdList.add(fileSeqs.size() > 1 ? fileSeqs.get(1) : null);
		newPackagingFileIdList.add(fileSeqs.size() > 2 ? fileSeqs.get(2) : null);
		newPackagingFileIdList.add(fileSeqs.size() > 3 ? fileSeqs.get(3) : null);
		newPackagingFileIdList.add(fileSeqs.size() > 4 ? fileSeqs.get(4) : null);
		prjParam.setPackageFileId(newPackagingFileIdList.get(0));
		prjParam.setPackageFileId2(newPackagingFileIdList.get(1));
		prjParam.setPackageFileId3(newPackagingFileIdList.get(2));
		prjParam.setPackageFileId4(newPackagingFileIdList.get(3));
		prjParam.setPackageFileId5(newPackagingFileIdList.get(4));
		
		for (String fileSeq : fileSeqs){
			T2File paramT2File = new T2File();
			paramT2File.setFileSeq(fileSeq);
			T2File uploadFileInfo = fileMapper.getFileInfo(paramT2File);
			if (uploadFileInfo != null) {
				uploadFileInfos.add(uploadFileInfo);
			}
		}
						
		String publicUrl = appEnv.getProperty("upload.path", "/upload");
		String packagingUrl = appEnv.getProperty("packaging.path", "/upload/packaging") + "/" + prjId;
		List<T2File> result = fileMapper.selectPackagingFileInfo(prjId); // verify한 file을 select함.

		if (result.size() > 0){
			for (T2File res : result){
				String rtnFilePath = res.getLogiPath();
				String rtnFileName = res.getLogiNm();
				String rtnFileSeq = res.getFileSeq();
				
				if (publicUrl.equals(rtnFilePath)){
					// select한 filePath가 upload Dir 일 경우 해당 파일만 삭제함.
					file = new File(rtnFilePath + "/" + rtnFileName);
					
					for (String fileSeq : fileSeqs) {
						if (file.exists() && !rtnFileSeq.equals(fileSeq)){
							int reuseCnt = fileMapper.getPackgingReuseCnt(rtnFileName);
								
							if (reuseCnt == 0){
								T2File delFile = new T2File();
								delFile.setFileSeq(rtnFileSeq);
								delFile.setGubn("A");
								int returnSuccess = fileMapper.updateFileDelYnKessan(delFile);
								
								if (returnSuccess > 0) {
									if (file.delete()){
										log.debug(rtnFilePath + "/" + rtnFileName + " is delete success.");
									} else {
										log.debug(rtnFilePath + "/" + rtnFileName + " is delete failed.");
									}
								}
							}
						}
					}
				}
			}
			
			deleteFiles(packagingUrl, uploadFileInfos, prjId, null); // 'upload/packaging/#{prjId}' 의 Directory가 있는지 체크 후 삭제 처리함.( 현재등록한 file을 제외한 나머지를 삭세처리 )
		} else {
			deleteFiles(packagingUrl, uploadFileInfos, prjId, null); // verify 한 file이 없을경우 packagingUrl도 같이 검사하여 delete를 함.
		}
		
		// packaging File comment
		try {
			Project project = projectMapper.selectProjectMaster(prjParam.getPrjId());
			ArrayList<String> origPackagingFileIdList = new ArrayList<String>();
			origPackagingFileIdList.add(project.getPackageFileId());
			origPackagingFileIdList.add(project.getPackageFileId2());
			origPackagingFileIdList.add(project.getPackageFileId3());
			origPackagingFileIdList.add(project.getPackageFileId4());
			origPackagingFileIdList.add(project.getPackageFileId5());
			
			int idx = 0;
			
			for (String fileId : origPackagingFileIdList){
				T2File fileInfo = new T2File();
				
				if (!isEmpty(fileId) && !fileId.equals(newPackagingFileIdList.get(idx))){
					//fileInfo.setFileSeq(fileId);
					fileInfo = fileMapper.selectFileInfo(fileId);
					deleteComment += "Packaging file, "+fileInfo.getOrigNm()+", was deleted by "+loginUserName()+". <br>";
				}
				
				if (!isEmpty(newPackagingFileIdList.get(idx)) && !newPackagingFileIdList.get(idx).equals(fileId)){
					//fileInfo.setFileSeq(newPackagingFileIdList.get(idx));
					fileInfo = fileMapper.selectFileInfo(newPackagingFileIdList.get(idx));
					oss.fosslight.domain.File resultFile = verificationMapper.selectVerificationFile(newPackagingFileIdList.get(idx));
					
					if (CoConstDef.FLAG_YES.equals(resultFile.getReuseFlag())){
						uploadComment += "Packaging file, "+fileInfo.getOrigNm()+", was loaded from Project ID: "+resultFile.getRefPrjId()+" by "+loginUserName()+". <br>";
					} else {
						uploadComment += "Packaging file, "+fileInfo.getOrigNm()+", was uploaded by "+loginUserName()+". <br>";
					}
				}
				
				idx++;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}		
		
		verificationMapper.updatePackagingReuseMap(prjParam);
		
		return deleteComment + uploadComment;
	}

	@Override
	public void deleteFiles(String url, List<T2File> uploadFileInfos, String prjId, T2File vulDOCFileInfo) {
		File file = new File(url);
		ArrayList<String> LogiNms = new ArrayList<String>();
		ArrayList<String> reuseNms = new ArrayList<String>();
		
		for (T2File uploadFileInfo : uploadFileInfos){
			LogiNms.add(uploadFileInfo.getLogiNm());
		}
		
		// 현재 proejct Packaging File 중 재사용중인 packaging File 이 있다면 제거 불가
		List<T2File> reusePackaging = fileMapper.getReusePackagingInfo();
		String vulDOCFileLogiNm = vulDOCFileInfo != null ? vulDOCFileInfo.getLogiNm() : "";
		
		for (T2File reuse : reusePackaging){
			reuseNms.add(reuse.getLogiNm());
		}
		
		if (file.exists()){
			for (File f : file.listFiles()){
				String fileNm = f.getName();
				if (!isEmpty(vulDOCFileLogiNm) && vulDOCFileLogiNm.equalsIgnoreCase(fileNm)) continue;
				
				if (!LogiNms.contains(fileNm)){
					T2File delFile = new T2File();
					delFile.setLogiPath(url);
					delFile.setLogiNm(f.getName());
					
					int returnSuccess = fileMapper.updateReuseChkFileDelYnByFilePathNm(delFile);
					
					if (returnSuccess > 0 && !reuseNms.contains(fileNm)){
						if (f.delete()){
							log.debug(url + "/" + f.getName() + " is delete success.");
						}else{
							log.debug(url + "/" + f.getName() + " is delete failed.");
						}
					}
				}
			}
		}
		
		// 재사용을 했었던 file중 다른 project에서도 재사용을 하지 않은 file 있는지 확인하고 재사용을 안한다면 file 삭제 / 추후 reuse하는 다른 project에서도 reuseFlag가 N이 되면 지우는 case이므로 log는 남기지 않음.
		List<T2File> reusePackagingFileList = fileMapper.getPackgingReuseCntToList(prjId);
		
		for (T2File reusePackagingFile : reusePackagingFileList){ // reuseCnt가 0인 값만 불러오고 삭제처리 후 hidden flag를 Y로 변경 그리고 재검색시 조회 불가상태로 만듦.
			File reuseFile = new File(reusePackagingFile.getLogiPath());
			
			if (reuseFile.exists()){
				for (File f : reuseFile.listFiles()){
					if (reusePackagingFile.getLogiNm().equals(f.getName())){
						T2File delFile = new T2File();
						delFile.setLogiPath(reusePackagingFile.getLogiPath());
						delFile.setLogiNm(f.getName());
						int returnSuccess = fileMapper.updateFileDelYnByFilePathNm(delFile);
						String[] refPrjId = reusePackagingFile.getLogiPath().split("/");
						
						fileMapper.setReusePackagingFileHidden(refPrjId[refPrjId.length-1], reusePackagingFile.getLogiPath(), f.getName());
						
						if (returnSuccess > 0){
							if (f.delete()){
								log.debug(url + "/" + f.getName() + " is delete success.");
							}else{
								log.debug(url + "/" + f.getName() + " is delete failed.");
							}
						}
					}
				}
			}
		}
	}

	@Override
	public List<UploadFile> setReusePackagingFile(String refFileSeq) {
		List<UploadFile> result = new ArrayList<UploadFile>();
		UploadFile upFile = new UploadFile();
		
		String FileId = fileMapper.getFileId();
		
		T2File file = selectFileInfo(refFileSeq);
		file.setFileId(FileId);
		file.setCreator(loginUserName());
		
		fileMapper.insertFile(file);
		
		file = selectFileInfoById(FileId);
		
		upFile.setOriginalFilename(file.getOrigNm());
		upFile.setSize(Long.parseLong(file.getSize()));
		upFile.setFilePath(file.getLogiPath());
		upFile.setFileName(file.getLogiNm());
		upFile.setContentType(file.getExt());
		upFile.setRegistSeq(file.getFileSeq());
		upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
		
		result.add(upFile);
		
		return result;
	}

	@Override
	public Map<String, Object> uploadNoticeXMLFile(HttpServletRequest req, T2File registFile, String oldFileId, String prjId) {
		Map<String, Object> resultMap = new HashMap<>();
		List<UploadFile> result = new ArrayList<UploadFile>();
		MultipartHttpServletRequest multipartRequest = null;

		/** S: 파일 업로드 **/
		try {
			multipartRequest = (MultipartHttpServletRequest)req;
		} catch(Exception e) {
			log.debug("error : " + e.getMessage());
			
			return resultMap;
		}
		
		java.util.Iterator<String> fileNames = multipartRequest.getFileNames();

		boolean sw = true;
		String fileId = "";
		
		//구 fileId가 존제 한다면 구 fileId에 넣어준다
		if (oldFileId==null || "0".equals(oldFileId) || "".equals(oldFileId)){
			fileId = fileMapper.getFileId();
			if (fileId == null){
				fileId = "1";
			}
		} else {
			fileId = oldFileId;
		}
		
		int indexNum = 0;
		
		while (fileNames.hasNext()){
			UploadFile upFile = new UploadFile();
					
			boolean uploadSucc = true;
			String fileName = fileNames.next();		//input name
			
			sw = false;
			
			MultipartFile mFile = multipartRequest.getFile(fileName);
			
			if (isEmpty(mFile.getOriginalFilename())) {
				throw new RuntimeException("File Name is empty");
			}
			
			if (mFile.getSize() <= 0) {
				throw new RuntimeException("File Size is 0");
			}
			
			String originalFileName = mFile.getOriginalFilename();	//Original File name

			// originalFileName에 경로가 포함되어 있는 경우 처리
			log.debug("File upload OriginalFileName : " + originalFileName);
			
			if (originalFileName.indexOf("/") > -1) {
				originalFileName = originalFileName.substring(originalFileName.lastIndexOf("/") + 1);
				
				log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
			}
			if (originalFileName.indexOf("\\") > -1) {
				originalFileName = originalFileName.substring(originalFileName.lastIndexOf("\\") + 1);
				
				log.debug("File upload OriginalFileName Substring with File.separator : " + originalFileName);
			}
			
			String fileExt = FilenameUtils.getExtension(originalFileName);
			
			if (originalFileName.toLowerCase().endsWith(".tgz.gz")) {
				fileExt = "tgz.gz";
			} else if (originalFileName.toLowerCase().endsWith(".tar.bz2")) {
				fileExt = "tar.bz2";
			} else if (originalFileName.toLowerCase().endsWith(".tar.gz")) {
				fileExt = "tar.gz";
			}
			
			String uploadFilePath = "";
			String uploadThumbFilePath = "";
			
			try{
				uploadFilePath = appEnv.getProperty("android.upload.path", "/upload/android_notice") + "/" + prjId;
				uploadThumbFilePath = appEnv.getProperty("image.path", "/image") + "/" + prjId + "/thumb";
			}catch(Exception e){
				log.error("file upload path(get properties) : " + e.getMessage());
			}
			
			UUID randomUUID = UUID.randomUUID();
			File file = new File(uploadFilePath+"/"+randomUUID+"."+fileExt);

			/** Return Setting **/
			upFile.setOriginalFilename(originalFileName);
			upFile.setInputName(fileName);
			upFile.setSize(mFile.getSize());
			upFile.setFilePath(uploadFilePath);
			upFile.setFileName(randomUUID+"."+fileExt);
			upFile.setFileExt(fileExt);
			upFile.setIndexNum(indexNum);
			upFile.setRegistFileId(fileId);
			
			try {
				upFile.setContentType(mFile.getContentType());
			} catch (Exception e) {}
			
			/** DB Regist Setting **/
			registFile.setFileId(fileId);
			registFile.setOrigNm(originalFileName);
			registFile.setLogiNm(randomUUID+"."+fileExt);
			registFile.setLogiPath(uploadFilePath);
			registFile.setLogiThumbNm(randomUUID+"_thumb."+fileExt);
			registFile.setLogiThumbPath(uploadThumbFilePath);
			registFile.setExt(fileExt);
			registFile.setSize(mFile.getSize()+"");
			
			try {
				registFile.setContentType(mFile.getContentType());
			} catch (Exception e) {}
			
			upFile.setRegistSeq(registFile(registFile));
			upFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
			
			if (mFile.getSize()!=0){ //File Null Check
				if (! file.exists()){ //경로상에 파일이 존재하지 않을 경우
					try {
						if (file.getParentFile().mkdirs()){ //경로에 해당하는 디렉토리들을 생성
								boolean upSucc = file.createNewFile(); //이후 파일 생성
								
								if (!upSucc){
									uploadSucc=false;
								}
						} 
					}
					catch (IOException e) {
						log.error("file upload create error : " + e.getMessage());
						
						uploadSucc=false;
					}
				}
				
				uploadSucc = FileUtil.transferTo(mFile, file);
				
				upFile.setUploadSucc(uploadSucc);
			}
			result.add(upFile);
			
			boolean zipFile = false;
			try {
				File convertHTMLFile = null;
				
				if ("XML".equals(fileExt.toUpperCase())) {
					convertHTMLFile = CommonFunction.convertXMLToHTML(file, false);
				} else if ("ZIP".equals(fileExt.toUpperCase())) {
					zipFile = true;
					FileUtil.decompress(uploadFilePath + "/" + file.getName(), uploadFilePath + "/" + randomUUID);
					convertHTMLFile = CommonFunction.convertZIPToHtml(new File(uploadFilePath + "/" + randomUUID));
				} else if ("TAR.GZ".equals(fileExt.toUpperCase())) {
					CompressUtil.decompressTarGZ(file, uploadFilePath + "/" + randomUUID);
					convertHTMLFile = CommonFunction.convertXMLToHTML(new File(uploadFilePath + "/" + randomUUID), true);
				}
				
				if (convertHTMLFile != null) {
					long convertHTMLFileSize = convertHTMLFile.length();
					
					if (convertHTMLFileSize > 0){
						UploadFile convertNoticeFile = new UploadFile();
						String convertFileId = fileMapper.getFileId();
						String convertNoticeFileName = "Notice-"+prjId+"_"+DateUtil.getCurrentDateTime(DateUtil.DATE_PATTERN)+".html";
						
						convertNoticeFile.setOriginalFilename(convertNoticeFileName);
						convertNoticeFile.setInputName(convertNoticeFileName);
						convertNoticeFile.setSize(convertHTMLFileSize);
						convertNoticeFile.setFilePath(uploadFilePath + "/" + randomUUID);
						convertNoticeFile.setFileName(convertHTMLFile.getName());
						convertNoticeFile.setFileExt("html");
						convertNoticeFile.setIndexNum(indexNum+1);
						convertNoticeFile.setRegistFileId(convertFileId);
						convertNoticeFile.setContentType("text/html");
						
						/** DB Regist Setting **/
						T2File registConvertHTML = new T2File();
						registConvertHTML.setFileId(convertFileId);
						registConvertHTML.setOrigNm(convertNoticeFileName);
						registConvertHTML.setLogiNm(convertHTMLFile.getName());
						registConvertHTML.setLogiPath(uploadFilePath + "/" + randomUUID);
						registConvertHTML.setLogiThumbNm(convertHTMLFile.getName().replace(".html", "_thumb.html"));
						registConvertHTML.setLogiThumbPath(uploadFilePath + "/" + randomUUID + "/thumb");
						registConvertHTML.setExt("html");
						registConvertHTML.setSize(Long.toString(convertHTMLFileSize));
						registFile.setContentType("text/html");
						convertNoticeFile.setRegistSeq(registFile(registConvertHTML));
						convertNoticeFile.setCreatedDate(CommonFunction.getCurrentDateTime(CoConstDef.DATABASE_FORMAT_DATE_ALL));
						
						result.add(convertNoticeFile);
					}
				} else {
					if (zipFile) {
						resultMap.put("msg", getMessage("msg.common.convert.html.file.fail"));
					}
				}
			} catch (Throwable e) {
				log.debug(e.getMessage());
				if (zipFile) {
					resultMap.put("msg", getMessage("msg.common.convert.html.file.fail"));
				}
			}
		}
		
		if (sw){
			result = null;
		}
		
		resultMap.put("file", result);
		
		return resultMap;
	}

	@Override
	public void deletePhysicalFile(T2File file, String flag) {
		if (file == null || isEmpty(file.getLogiPath()) || isEmpty(file.getLogiNm())) {
			return;
		}

		T2File fileInfo = fileMapper.getFileInfo2(file);
		if (fileInfo == null) {
			fileInfo = file;
		}

		if (fileInfo == null || isEmpty(fileInfo.getLogiPath()) || isEmpty(fileInfo.getLogiNm())) {
			return;
		}

		String filePath = fileInfo.getLogiPath() + "/" + fileInfo.getLogiNm();
		File physicalFile = new File(filePath);
		File folder = null;

		// Android report 업로드(zip/tar.gz)는 파일과 함께 압축 해제된 폴더도 같이 삭제한다.
		String lowerExt = avoidNull(fileInfo.getExt()).toLowerCase();
		boolean archiveFile = "zip".equals(lowerExt) || "tar.gz".equals(lowerExt) || "tar.bz2".equals(lowerExt) || "tgz.gz".equals(lowerExt);
		if (archiveFile && !isEmpty(fileInfo.getLogiPath()) && !isEmpty(fileInfo.getLogiNm())) {
			String folderName = fileInfo.getLogiNm();
			String extSuffix = "." + lowerExt;
			if (folderName.toLowerCase().endsWith(extSuffix)) {
				folderName = folderName.substring(0, folderName.length() - extSuffix.length());
			} else {
				folderName = FilenameUtils.getBaseName(folderName);
			}
			folder = new File(fileInfo.getLogiPath(), folderName);

			if (folder != null && folder.exists()) {
				try {
					FileUtils.deleteDirectory(folder);
				} catch (Exception e) {
					log.info("Failed to delete folder {} : {}", folder.getAbsolutePath(), e.getMessage(), e);
				}
			}
		}
//		else if (fileInfo.getLogiPath().contains("android_notice") && "html".equals(lowerExt)) {
//			folder = new File(fileInfo.getLogiPath());
//		}

		if ("VERIFY".equalsIgnoreCase(flag) || CoConstDef.CD_CHECK_OSS_SELF.equals(flag) || CoConstDef.CD_CHECK_OSS_PARTNER.equals(flag) || CoConstDef.CD_CHECK_OSS_IDENTIFICATION.equals(flag)) {
			filePath = file.getLogiPath() + "/" + file.getLogiNm();
			physicalFile = new File(filePath);
		}

		if (physicalFile.exists() && !physicalFile.delete()) {
			log.info("{} is delete failed.", filePath);
		}


	}

	@Override
	public String copyPhysicalFile(String fileId, String prjId, boolean isFileId) {
		boolean fileCopyFlag = false;
		String newFileId = fileMapper.getFileId();
		List<T2File> orgFileInfoList = null;
		
		if (isFileId) {
			orgFileInfoList = fileMapper.getFileInfoList(fileId);
		} else {
			T2File orgFile = selectFileInfo(fileId);
			if (orgFile != null) {
				orgFileInfoList = new ArrayList<>();
				orgFileInfoList.add(orgFile);
			}
		}
		
		if (!CollectionUtils.isEmpty(orgFileInfoList)) {
			for (T2File orgFile : orgFileInfoList) {
				String baseFile = orgFile.getLogiPath() + "/" + orgFile.getLogiNm();
				
				UUID randomUUID = UUID.randomUUID();
				String copyFileName = randomUUID + "." + orgFile.getExt();
				String newFile = orgFile.getLogiPath();
				if (!isFileId) {
					newFile = CommonFunction.emptyCheckProperty("packaging.path", "/upload/packaging") + "/" + prjId;
					new File(newFile).mkdirs();
				}
				
				if (FileUtil.copyFile(baseFile, newFile, copyFileName)) {
					T2File fileInfo = new T2File();
					fileInfo.setFileId(newFileId);
					fileInfo.setFileSeq(orgFile.getFileSeq());
					fileInfo.setLogiNm(copyFileName);
					fileInfo.setLogiThumbNm(randomUUID + "_thumb." + orgFile.getExt());
					if (!isFileId) {
						fileInfo.setLogiPath(newFile);
						fileInfo.setLogiThumbPath(newFile + "/thumb");
					} else {
						fileInfo.setLogiPath(orgFile.getLogiPath());
						fileInfo.setLogiThumbPath(orgFile.getLogiThumbPath());
					}
					
					fileMapper.insertCopyPhysicalFileInfo(fileInfo);
					if (!isFileId) {
						fileInfo = selectFileInfoById(newFileId);
						newFileId = fileInfo.getFileSeq();
					}
					fileCopyFlag = true;
				} else {
					fileCopyFlag = false;
				}
				
				if (!fileCopyFlag) {
					newFileId = null;
					log.error("physical file copy error");
					break;
				}
			}
		}
		
		if (!fileCopyFlag) {
			newFileId = null;
			log.error("physical file copy error");
		}
		
		return newFileId;
	}
	
	private void ignoreSsl() {
		HostnameVerifier hv = new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
	    		return true;
	    	}
		};
		try {
			trustAllHttpsCertificates();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}

	private static void trustAllHttpsCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }
	
	static class miTM implements TrustManager,X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
 
        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }
 
        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }
 
        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
 
        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
    }

	@Override
	public T2File uploadSingleFile(MultipartFile mFile, String fileId, String fileGubn, Path descFilePath, boolean useRandomFileName) {
		T2File fileInfo = null;
		if(mFile != null && !mFile.isEmpty()) {
			try {
				fileInfo = new T2File();
				fileInfo.setCreator(loginUserName());
				fileInfo.setGubn(fileGubn);
				fileInfo.setOrigNm(mFile.getOriginalFilename());
				fileInfo.setExt(FilenameUtils.getExtension(fileInfo.getOrigNm()));
				fileInfo.setLogiNm(fileInfo.getOrigNm());
				fileInfo.setLogiPath(descFilePath.toString());
				fileInfo.setContentType(mFile.getContentType());
				fileInfo.setSize(Long.toString(mFile.getSize()));
				if(useRandomFileName) {
					fileInfo.setLogiNm(MessageFormat.format("{0}.{1}", UUID.randomUUID(), fileInfo.getExt()));
				}

				descFilePath.toFile().mkdirs();
				
				Path destinationFile = descFilePath.resolve(fileInfo.getLogiNm());
				try (InputStream inputStream = mFile.getInputStream()) {
					Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
				}
				
				if(isEmpty(fileId)) {
					fileId = fileMapper.getFileId();
				}
				fileInfo.setFileId(fileId);
				fileMapper.insertFile(fileInfo);
			} catch (IOException e) {
				log.error("Failed upload file {}, {}", loginUserName(), mFile.getOriginalFilename());
				log.error(e.getMessage(), e);
				return null;
			}

		} else {
			log.warn("MultipartFile is empty");
		}
		return fileInfo;
	}

	@Override
	public List<T2File> getFileInfoList(String fileId) {
		return fileMapper.getFileInfoList(fileId);
	}
}
