/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import oss.fosslight.common.CommonFunction;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {
	private static final String ILLEGAL_EXP = "[:\\\\/%*?:|\"<>]";
	
	/**
	 * 외부에서 생성자를 호출하지 못하도록 접근제어자 private 으로 설정
	 */
	private FileUtil(){}
	
	/**
	 * Part 에서 File Name을 추출하여 리턴한다.
	 * 
	 * @param javax.servlet.http.Part
	 * @return File Name(Type String)
	 */
	public static String getFileName(Part part) {
		for (String cd : part.getHeader("content-disposition").split(";")) {
			if (cd.trim().startsWith("filename")) {
				return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		
		return null;
	}

	public static File writeFile(MultipartFile multipart, String fPath, String fName) {
		File file = null;
		
		if (multipart != null && fPath != null && fName != null) {
			try {
				file = new File(fPath+ "/" + fName);
				
				multipart.transferTo(file);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				log.info("Failed transfer From Multipart, retry with input stream");
				
				try {
					FileUtils.copyInputStreamToFile(multipart.getInputStream(), file);
				} catch (IOException e1) {
					log.error(e.getMessage(), e);
				}
			}	
		}
		
		return file;
	}

	public static boolean transferTo(MultipartFile multipart, File file) {
		if (multipart != null && file != null) {
			try {
				multipart.transferTo(file);
				
				return true;
			} catch (Exception e) {
				// local개발환경(window)에서는 상대 path를 사용하면 에러가 발생할 수 있다. 
				log.debug("Failed transfer From Multipart, retry with input stream");
				
				try {
					FileUtils.copyInputStreamToFile(multipart.getInputStream(), file);
					
					return true;
				} catch (IOException e1) {
					log.error(e.getMessage(), e);
				}
			}	
		}
		
		return false;
	}
	
	public static boolean moveTo(String srcFilePath, String copyFilePath, String copyFileName) {
		if (!StringUtil.isEmpty(srcFilePath) && !StringUtil.isEmpty(copyFilePath)) {
			try {
				Path srcFile = Path.of(srcFilePath);
				Path copyPath = Path.of(copyFilePath);
				
				if (!StringUtil.isEmpty(copyFileName)) {
					if (!Files.exists(copyPath)) {
						Files.createDirectory(copyPath);
					}
					
					copyPath = copyPath.resolve(copyFileName);
				}
				
				Files.move(srcFile, copyPath);
				
				return true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return false;
	}
	
	
	public static boolean writeFile(String filePath, String fileName, String contents) {
		BufferedWriter fw = null;
		try {
			File dir = new File(filePath);
			
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			// BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath + "/" + fileName), Charset.forName("UTF8")));
            fw.write(contents);
            
            fw.flush();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			
			return false;
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (Exception e) {}
			}
		}
		
		return true;
	}

	public static boolean writeReviewReportFile(String filePath, String fileName, String contents) {
		try {
			File dir = new File(filePath);

			if (!dir.exists()) {
				dir.mkdirs();
			}
			PdfUtil.html2pdf(contents, filePath + "/" + fileName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);

			return false;
		}

		return true;
	}
	
    /**
     * 파일 이름이 유효한지 확인한다.
     * 
     * @param fileName 파일의 이름, Path를 제외한 순수한 파일의 이름.. 
     * @return
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().length() == 0) {
        	return false;
        }
        
        return !Pattern.compile(ILLEGAL_EXP).matcher(fileName).find();
    }
 
    /**
     * 파일 이름에 사용할 수 없는 캐릭터를 바꿔서 유효한 파일로 만든다.
     * 
     * @param fileName 파일 이름, Path를 제외한 순수한 파일의 이름..
     * @param replaceStr 파일 이름에 사용할 수 없는 캐릭터의 교체 문자
     * @return
     */
    public static String makeValidFileName(String fileName, String replaceStr) {
        if (fileName == null || fileName.trim().length() == 0 || replaceStr == null) {
            return String.valueOf(System.currentTimeMillis());      
        }
 
        return fileName.replaceAll(ILLEGAL_EXP, replaceStr).replaceAll("\t", "").trim();
    }

	public static void zip(String inputFolder, String filePath, String zipName, String zipRootEntryName) throws Exception {
		// 압축파일을 저장할 파일을 선언한다.
		File file = new File(filePath + File.separator + zipName);
		try (			
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			// ZipOutputStream 선언
			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
		) {
			// 압축할 대상이 있는 폴더를 파일로 선언한다.
			File inputFile = new File(inputFolder);
			
			// 압축을 할 대상이 file이면 zipFile 메소드를,
			// 폴더이면 zipFolder 메소드를 호출한다.
			if (inputFile.isFile()) {
				zipFile(zipOutputStream, inputFile, "");
			} else if (inputFile.isDirectory()) {
				zipFolder(zipOutputStream, inputFile, "", zipRootEntryName);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public static void zipFolder(ZipOutputStream zipOutputStream, File inputFile, String parentName, String zipEntryName) throws Exception {
		String myName = parentName + inputFile.getName() + File.separator;
		
		if (zipEntryName != null && zipEntryName.trim().length() > 0) {
			myName = zipEntryName + File.separator;
		}
		
		// ZipEntry를 생성 후 zip 메소드에서 인자값으로 받은 파일의 구성 정보를 생성한다.
		ZipEntry folderZipEntry = new ZipEntry (myName);
		zipOutputStream.putNextEntry (folderZipEntry);
		// zip 메소드에서 인자값으로 전달받은 파일의 구성파일들을 list형식으로 저장한다.
		File[] contents = inputFile.listFiles();
		
		// inputFolder의 구성파일이 파일이면 zipFile 메소드를 호출하고,
		// 폴더일 경우 현재 zipFolder 메소드를 재귀호출
		if (contents != null) {
			for (File file : contents) {
				if (file.isFile()) {
					zipFile(zipOutputStream, file, myName);
				} else if (file.isDirectory()) {
					zipFolder(zipOutputStream, file, myName, "");
				}
				
				zipOutputStream.closeEntry ();
			}
		}
	}

	public static void zipFile(ZipOutputStream zipOutputStream, File inputFile, String parentName) throws Exception {
		ZipEntry zipEntry = new ZipEntry (parentName + inputFile.getName());
		try (
			FileInputStream fileInputStream = new FileInputStream(inputFile);
		){
			// ZipEntry생성 후 zip 메소드에서 인자값으로 전달받은 파일의 구성 정보를 생성한다.
			zipOutputStream.putNextEntry (zipEntry);
			byte[] buf = new byte[4096];
			int byteRead;
			// 압축대상 파일을 설정된 사이즈만큼 읽어들인다.
			// buf의 size는 원하는대로 설정가능하다.
			while ((byteRead = fileInputStream.read(buf)) > 0) {
				zipOutputStream.write(buf, 0, byteRead);
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static boolean copyFile(String srcFilePath, String copyFilePath, String copyFileName) {
		if (!StringUtil.isEmpty(srcFilePath) && !StringUtil.isEmpty(copyFilePath)) {
			try {
				File dir = new File(copyFilePath);
				
				if (!dir.exists()) {
					dir.mkdirs();
				}
				
				Path srcFile = Path.of(srcFilePath);
				Path copyPath = Path.of(copyFilePath);
				
				if (!StringUtil.isEmpty(copyFileName)) {
					copyPath = copyPath.resolve(copyFileName);
				}
				
				Files.copy(srcFile, copyPath);
				
				return true;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		
		return false;
	}
	
	/**
     * Downloads a file from a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
	 * @throws Exception 
     */
    public static void downloadFile(String fileURL, String saveDir)
            throws Exception {
    	HttpURLConnection httpConn = null;
    	FileOutputStream outputStream = null;
    	InputStream inputStream = null;
        URL url = new URL(fileURL);
        try {
             httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
     
            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");
     
                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    
                    if (index > 0) {
                        fileName = disposition.substring(index + 10,
                                disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1, fileURL.length());
                }
     
                // opens input stream from the HTTP connection
                inputStream = httpConn.getInputStream();
                String saveFilePath = saveDir + File.separator + fileName;

                if (!Files.exists(Path.of(saveDir))) {
                    Files.createDirectories(Path.of(saveDir));
                }

                // opens an output stream to save into file
                outputStream = new FileOutputStream(saveFilePath);
     
                int bytesRead = -1;
                byte[] buffer = new byte[4096];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } else {
            	throw new Exception("No file to download. Server replied HTTP code: " + responseCode);
            }			
		} finally {
			if (httpConn != null) {
				try {
					httpConn.disconnect();
				} catch (Exception e) {}
			}

			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e) {}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {}
			}
		}

    }
    
    
    /**
     * 압축풀기 메소드
     * @param zipFileName 압축파일
     * @param directory 압축 풀 폴더
     */
    public static void decompress(String zipFileName, String directory) throws IOException {
        File zipFile = new File(zipFileName);
        FileInputStream fis = null;
        ZipInputStream zis = null;
        ZipEntry zipentry = null;
        
        try {
            fis = new FileInputStream(zipFile);
            zis = new ZipInputStream(fis);
            
            while ((zipentry = zis.getNextEntry ()) != null) {
                String filename = zipentry.getName();
                File file = new File(directory, filename);
                
                if (zipentry.isDirectory()) {
                    file.mkdirs();
                } else {
                    createFile(file, zis);
                }
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
            
            if (fis != null) {
                fis.close();
            }
        }
    }
    
    /**
     * 파일 만들기 메소드
     * @param file 파일
     * @param zis Zip스트림
     */
    public static void createFile(File file, ZipInputStream zis) throws IOException {
    	FileOutputStream fos = null;
        File parentDir = new File(file.getParent());
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try  {
        	fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            int size = 0;
            
            while ((size = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, size);
            }
        } finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {}
			}
		}
    }
    
	public static void backupRawData(String sourceFilePath, String destPath) {
		try {
			// zip 파일 백업
			Path zipPath = Path.of(sourceFilePath);
			Path movePath = Path.of(destPath);
			
			if (!Files.exists(movePath)) {
				Files.createDirectories(movePath);
			}
			
			if (Files.exists(zipPath)) {
				Files.move(zipPath, movePath.resolve(zipPath.getFileName() + "_" + CommonFunction.getCurrentDateTime("yyyyMMddHHmmss")));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static File getAutoAnalysisFile(String fileformat, String path) {
		File file = new File(path);

		if (!file.exists()) {
			log.error("파일정보를 찾을 수 없습니다. file path : " + path);
			return null;
		}

		for (File f : file.listFiles()) {
			if (f.isFile()) {
				String[] fileName = f.getName().split("\\.");
				String fileExt = (fileName[fileName.length - 1]).toUpperCase();

				if(fileExt.equals(fileformat.toUpperCase())) {
					file = f;
					break;
				}
			}
		}
		return file;
	}
}
