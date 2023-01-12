/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.common.CommonFunction;
import oss.fosslight.service.ApiBatService;
import oss.fosslight.util.StringUtil;

@Service
@Slf4j
public class ApiBatServiceImpl implements ApiBatService {
	private String JDBC_DRIVER;  
	private String DB_URL;
	private String USERNAME;
	private String PASSWORD;
	
	@Resource private Environment env;
	
	@PostConstruct
	public void setResourcePathPrefix(){
		JDBC_DRIVER = env.getProperty("bat.postgre.driver");
		DB_URL = env.getProperty("bat.postgre.host");
		USERNAME = env.getProperty("bat.postgre.username");
		PASSWORD = env.getProperty("bat.postgre.password");
	}
	
	@Override
	public List<Map<String, Object>> getBatList(Map<String, Object> paramMap) {
		List<Map<String, Object>> contents = new ArrayList<Map<String, Object>>();
		//데이터 베이스 접속
		Connection conn = null;
		Statement stmt = null;
		ResultSet totalRs = null;
		ResultSet rs = null;
		
		try{
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection(DB_URL,USERNAME,PASSWORD);
			stmt = conn.createStatement();
			
			String strWhere = " WHERE 1 = 1 ";
			
			{	
				String fileName = (String) paramMap.get("fileName");
				
				if(!StringUtil.isEmpty(fileName)) {
					strWhere += " AND UPPER(filename) LIKE UPPER('%" + fileName + "%') ";
				}
				
				String tlsh = (String) paramMap.get("tlsh");
				
				if(!StringUtil.isEmpty(tlsh)){
					strWhere += " AND UPPER(tlshchecksum) = UPPER('" + tlsh + "') ";
				}

				String checksum = (String) paramMap.get("checksum");
				
				if(!StringUtil.isEmpty(checksum)){
					strWhere += " AND UPPER(checksum) = UPPER('" + checksum + "') ";
				}
				
				String platformName = (String) paramMap.get("platformName");
				
				if(!StringUtil.isEmpty(platformName)) {
					strWhere += " AND UPPER(platformname) LIKE UPPER('%"+ platformName +"%') ";
				}
				
				String platformVersion = (String) paramMap.get("platformVersion");
				
				if(!StringUtil.isEmpty(platformVersion)) {
					strWhere += " AND UPPER(platformversion) LIKE UPPER('"+ platformVersion +"%') ";
				}
				
				String sourcePath = (String) paramMap.get("sourcePath");
				
				if(!StringUtil.isEmpty(sourcePath)) {
					strWhere += " AND UPPER(ossname) LIKE UPPER('%"+ sourcePath +"%') ";
				}
			}
			
			String sql  = "SELECT filename, pathname, checksum, tlshchecksum, ossname, ossversion, license, ";
				   sql += "parentname, platformname, platformversion, TO_CHAR(updatedate, 'YYYY-MM-DD') as updatedate FROM lgematching ";
				   sql += strWhere;
			
			rs = stmt.executeQuery(sql);
			
			while(rs.next()) {
				Map<String, Object> batData = new HashMap<String, Object>();
				
				batData.put("binaryFileName", 	rs.getString("filename"));
				batData.put("path", 			rs.getString("pathname"));
				batData.put("ossName", 			rs.getString("ossname"));
				batData.put("ossVersion", 		rs.getString("ossversion"));
				batData.put("license", 			rs.getString("license"));
				batData.put("projectName", 		rs.getString("parentname"));
				batData.put("checksum", 		rs.getString("checksum"));
				batData.put("tlsh", 			rs.getString("tlshchecksum"));
				batData.put("updateDate", 		rs.getString("updatedate"));
				
				contents.add(batData);
			}

			CommonFunction.setOssDownloadLocation(contents);
			
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if(totalRs != null) {
				try {
					totalRs.close();
				} catch (Exception e) {}
			}
			
			if(rs != null) {
				try {
					rs.close();
				} catch (Exception e) {}
			}
			
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {}
			}
			
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {}
			}
		}
		
		return contents;
	}
}