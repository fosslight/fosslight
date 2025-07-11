INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('903', '014', 'pkg.go.dev', '', 'go url', 14, 'Y');

INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('111', '460', 'Project Identification Confirmed Network Service Only', '', '<p>Network상의 이용을 배포로 간주하는 License (ex- AGPL-3.0)가 사용되지 않았음을 확인했으며, OSC Process 완료되었습니다.<br />The OSC Process is completed because the Network Copyleft License (ex- AGPL-3.0) is not used.</p>', 460, 'Y');

ALTER TABLE `PROJECT_MASTER` ADD `PACKAGE_FILE_ID5` int(11) DEFAULT NULL, ADD `PACKAGE_FILE_TYPE5` varchar(1) DEFAULT NULL, ADD `OSDD_SOURCE_FILE_NAME5` varchar(512) DEFAULT 'N', ADD `OSDD_SOURCE_FILE_ETAG5` varchar(512) DEFAULT 'N', ADD `SECPERSON` varchar(50) DEFAULT NULL;

ALTER TABLE `LICENSE_MASTER` CHANGE `LICENSE_NAME` `LICENSE_NAME` VARCHAR(600);
ALTER TABLE `LICENSE_NICKNAME` CHANGE `LICENSE_NICKNAME` `LICENSE_NICKNAME` VARCHAR(600);
ALTER TABLE `OSS_COMPONENTS_LICENSE` CHANGE `LICENSE_NAME` `LICENSE_NAME` VARCHAR(600);
ALTER TABLE `PRE_OSS_COMPONENTS_LICENSE` CHANGE `LICENSE_NAME` `LICENSE_NAME` VARCHAR(600);

ALTER TABLE `PROJECT_MASTER` DROP COLUMN `PACKAGE_VUL_DOC_FILE_ID`;
ALTER TABLE `PROJECT_MASTER` DROP COLUMN `VUL_DOC_SKIP_YN`;

UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = 'SELECT T.* FROM ( SELECT T1.COMPONENT_ID, T1.REF_COMPONENT_ID, T1.REFERENCE_ID, T1.OSS_NAME, T1.OSS_VERSION, T1.DOWNLOAD_LOCATION, T1.HOMEPAGE, GROUP_CONCAT(T3.LICENSE_NAME ORDER BY T3.LICENSE_ID DESC SEPARATOR \',\') AS LICENSE_NAME FROM ( SELECT A1.* FROM OSS_COMPONENTS A1 INNER JOIN OSS_MASTER A3  ON A1.OSS_NAME = A3.OSS_NAME AND A1.OSS_VERSION = A3.OSS_VERSION AND A3.USE_YN = \'Y\' WHERE REFERENCE_ID = ? AND REFERENCE_DIV = (SELECT CASE WHEN NOTICE_TYPE = \'80\' THEN \'17\' ELSE \'13\' END REFERENCE_DIV FROM OSS_NOTICE WHERE PRJ_ID = ?) AND MERGE_PRE_DIV IS NULL  AND A1.OBLIGATION_TYPE = \'11\' ) T1 INNER JOIN OSS_COMPONENTS T2 ON T1.REF_COMPONENT_ID = T2.COMPONENT_ID INNER JOIN OSS_COMPONENTS_LICENSE T3 ON T1.REF_COMPONENT_ID = T3.COMPONENT_ID GROUP BY T1.COMPONENT_ID) T' WHERE CD_NO = '104' AND CD_DTL_NO = '202';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = 'SELECT T1.DISTRIBUTE_NAME, T1.DISTRIBUTE_MASTER_CATEGORY, T1.DISTRIBUTE_SOFTWARE_TYPE, T1.DISTRIBUTE_DEPLOY_TIME, T1.DISTRIBUTE_DEPLOY_YN, T1.DISTRIBUTE_DEPLOY_MODEL_YN, T1.DISTRIBUTE_DEPLOY_ERROR_MSG, T1.DISTRIBUTE_TARGET, (SELECT A1.ORIG_NM FROM T2_FILE A1 WHERE A1.FILE_SEQ = T1.PACKAGE_FILE_ID) AS PACKAGE_FILE_ID, (SELECT A1.ORIG_NM FROM T2_FILE A1 WHERE A1.FILE_SEQ = T1.PACKAGE_FILE_ID2) AS PACKAGE_FILE_ID2, (SELECT A1.ORIG_NM FROM T2_FILE A1 WHERE A1.FILE_SEQ = T1.PACKAGE_FILE_ID3) AS PACKAGE_FILE_ID3, (SELECT A1.ORIG_NM FROM T2_FILE A1 WHERE A1.FILE_SEQ = T1.PACKAGE_FILE_ID4) AS PACKAGE_FILE_ID4, (SELECT A1.ORIG_NM FROM T2_FILE A1 WHERE A1.FILE_SEQ = T1.PACKAGE_FILE_ID5) AS PACKAGE_FILE_ID5, (SELECT A1.ORIG_NM FROM T2_FILE A1 WHERE A1.FILE_SEQ = T1.NOTICE_FILE_ID) AS NOTICE_FILE_ID FROM PROJECT_MASTER T1 WHERE T1.PRJ_ID = ?' WHERE CD_NO = '104' AND CD_DTL_NO = '203';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = 'SELECT T1.CREATOR, T1.CREATED_DATE, T1.OSS_ID, T1.OSS_NAME, T1.OSS_VERSION, IFNULL(( SELECT GROUP_CONCAT(D.DOWNLOAD_LOCATION ORDER BY D.OSS_DL_IDX ASC) FROM OSS_DOWNLOADLOCATION D WHERE D.OSS_COMMON_ID = T1.OSS_COMMON_ID), T1.DOWNLOAD_LOCATION) AS DOWNLOAD_LOCATION, IFNULL(( SELECT GROUP_CONCAT(D.PURL ORDER BY D.OSS_DL_IDX ASC) FROM OSS_DOWNLOADLOCATION D WHERE D.OSS_COMMON_ID = T1.OSS_COMMON_ID), \'\') AS PURL, T1.HOMEPAGE, T1.SUMMARY_DESCRIPTION, T1.ATTRIBUTION, T1.IMPORTANT_NOTES, T1.COPYRIGHT, T1.LICENSE_TYPE AS OSS_LICENSE_TYPE, T1.OBLIGATION_TYPE AS OSS_OBLIGATION_TYPE, GROUP_CONCAT(T4.OSS_NICKNAME SEPARATOR \'\n\') AS OSS_NICKNAME, T2.LICENSE_ID, T2.OSS_LICENSE_IDX, T2.OSS_LICENSE_COMB, T2.OSS_COPYRIGHT, T2.OSS_LICENSE_TEXT, IF(IFNULL(T3.SHORT_IDENTIFIER, \'\') = \'\', T3.LICENSE_NAME, T3.SHORT_IDENTIFIER) AS LICENSE_NAME, T3.LICENSE_TYPE, SUB1.OSS_TYPE, IFNULL(( SELECT GROUP_CONCAT(IF(LM.SHORT_IDENTIFIER = \'\' OR LM.SHORT_IDENTIFIER IS NULL, LM.LICENSE_NAME, LM.SHORT_IDENTIFIER) ORDER BY OSS_LICENSE_IDX ASC) FROM OSS_LICENSE_DETECTED ODT INNER JOIN LICENSE_MASTER LM ON ODT.LICENSE_ID = LM.LICENSE_ID WHERE ODT.OSS_ID = T1.OSS_ID), \'\') AS DETECTED_LICENSE, (SELECT GROUP_CONCAT(CPE23URI) FROM OSS_INCLUDE_CPE WHERE OSS_COMMON_ID = T1.OSS_COMMON_ID) AS INCLUDE_CPE, (SELECT GROUP_CONCAT(CPE23URI) FROM OSS_EXCLUDE_CPE WHERE OSS_COMMON_ID = T1.OSS_COMMON_ID) AS EXCLUDE_CPE, (SELECT GROUP_CONCAT(OSS_VERSION_ALIAS) FROM OSS_VERSION_ALIAS WHERE OSS_ID = T1.OSS_ID) AS OSS_VERSION_ALIAS FROM OSS_MASTER T1 INNER JOIN OSS_LICENSE_DECLARED T2 ON T1.OSS_ID = T2.OSS_ID INNER JOIN LICENSE_MASTER T3 ON T2.LICENSE_ID = T3.LICENSE_ID LEFT OUTER JOIN OSS_NICKNAME T4 ON T1.OSS_COMMON_ID = T4.OSS_COMMON_ID INNER JOIN ( SELECT OSS_ID, CONCAT(IF(MULTI_LICENSE_FLAG = \'N\', \'0\', \'1\'), IF(DUAL_LICENSE_FLAG = \'N\', \'0\', \'1\'), IF(VERSION_DIFF_FLAG = \'N\', \'0\', \'1\')) AS OSS_TYPE FROM OSS_MASTER_LICENSE_FLAG) SUB1 ON T1.OSS_ID = SUB1.OSS_ID WHERE T1.OSS_ID = ? GROUP BY OSS_ID, OSS_LICENSE_IDX' WHERE CD_NO = '104' AND CD_DTL_NO = '100';

ALTER TABLE `USER_COLUMNS` CHANGE `LIST_TYPE` `LIST_TYPE` VARCHAR(50);

UPDATE T2_CODE_DTL SET CD_DTL_EXP='SELECT RTN.OSS_ID, RTN.OSS_NAME, RTN.OSS_VERSION, RTN.CVSS_SCORE, RTN.CVE_ID, RTN.VULN_SUMMARY, RTN.MODI_DATE, RTN.PUBL_DATE FROM ( SELECT T2.OSS_ID, T2.OSS_NAME, T2.OSS_VERSION, T4.CVSS_SCORE, T4.CVE_ID, T5.VULN_SUMMARY, DATE_FORMAT(T5.MODI_DATE, \'%Y-%m-%d\') AS MODI_DATE, DATE_FORMAT(T5.PUBL_DATE, \'%Y-%m-%d\') AS PUBL_DATE FROM PROJECT_MASTER T1 INNER JOIN OSS_COMPONENTS T2 ON T1.PRJ_ID = T2.REFERENCE_ID  AND T2.REFERENCE_DIV = (SELECT CASE WHEN NOTICE_TYPE = \'80\' THEN \'17\' ELSE \'13\' END REFERENCE_DIV FROM OSS_NOTICE WHERE PRJ_ID = ?)  AND T2.EXCLUDE_YN <> \'Y\' INNER JOIN OSS_MASTER T3 ON IFNULL(T2.REF_OSS_NAME, T2.OSS_NAME) = T3.OSS_NAME AND IFNULL(T2.OSS_VERSION, \'\') = IFNULL(T3.OSS_VERSION, \'\') AND T3.USE_YN = \'Y\' INNER JOIN OSS_DISCOVERED_SND_EMAIL T4 ON T3.OSS_ID = T4.OSS_ID AND T4.SND_YN != \'Y\' LEFT JOIN NVD_CVE_V3 T5 ON T4.CVE_ID = T5.CVE_ID WHERE T1.PRJ_ID = ?) RTN GROUP BY RTN.OSS_ID, RTN.CVE_ID ORDER BY RTN.OSS_NAME, RTN.CVSS_SCORE DESC, RTN.MODI_DATE DESC' WHERE CD_NO=104 AND CD_DTL_NO=207;
UPDATE T2_CODE_DTL SET CD_DTL_EXP='SELECT RTN.OSS_ID, RTN.OSS_NAME, RTN.OSS_VERSION, RTN.CVSS_SCORE, RTN.CVE_ID, RTN.VULN_SUMMARY, RTN.MODI_DATE, RTN.PUBL_DATE FROM ( SELECT T2.OSS_ID, T2.OSS_NAME, T2.OSS_VERSION, T4.CVSS_SCORE, T4.CVE_ID, T5.VULN_SUMMARY, DATE_FORMAT(T5.MODI_DATE, \'%Y-%m-%d\') AS MODI_DATE, DATE_FORMAT(T5.PUBL_DATE, \'%Y-%m-%d\') AS PUBL_DATE, CAST(HIS.CVSS_SCORE AS DECIMAL(10, 1)) AS HIS_CVSS_SCORE, CAST(HIS.CVSS_SCORE_TO AS DECIMAL(10, 1)) AS HIS_CVSS_SCORE_TO FROM PROJECT_MASTER T1 INNER JOIN OSS_COMPONENTS T2 ON T1.PRJ_ID = T2.REFERENCE_ID AND T2.REFERENCE_DIV = \'50\' AND T2.EXCLUDE_YN <> \'Y\' INNER JOIN OSS_MASTER T4 ON T2.OSS_NAME = T4.OSS_NAME AND T2.OSS_VERSION = T4.OSS_VERSION AND T4.VULN_YN = \'Y\' AND T4.USE_YN = \'Y\' AND T4.VULN_DATE > ADDDATE(SYSDATE(), INTERVAL - 2 DAY) LEFT JOIN NVD_CVE_V3 T5 ON T4.CVE_ID = T5.CVE_ID INNER JOIN NVD_OSS_HIS HIS ON IFNULL(T4.OSS_NAME, T2.OSS_NAME) = HIS.OSS_NAME AND T2.OSS_VERSION = HIS.OSS_VERSION AND HIS.REG_DT > ADDDATE(SYSDATE(), INTERVAL - 1 DAY) WHERE T1.PRJ_ID = ?) RTN WHERE RTN.HIS_CVSS_SCORE_TO < RTN.HIS_CVSS_SCORE AND HIS_CVSS_SCORE >= ? AND HIS_CVSS_SCORE_TO < ? GROUP BY RTN.OSS_ID ORDER BY RTN.OSS_NAME' WHERE CD_NO=104 AND CD_DTL_NO=210;
UPDATE T2_CODE_DTL SET CD_DTL_EXP='SELECT RTN.OSS_ID, RTN.OSS_NAME, RTN.OSS_VERSION, IFNULL(RTN.CVSS_SCORE, \'\') AS CVSS_SCORE, IFNULL(RTN.CVE_ID, \'\') AS CVE_ID, IFNULL(RTN.VULN_SUMMARY, \'\') AS VULN_SUMMARY, IFNULL(RTN.MODI_DATE, \'\') AS MODI_DATE, IFNULL(RTN.PUBL_DATE, \'\') AS PUBL_DATE FROM ( SELECT T2.OSS_ID, T2.OSS_NAME, T2.OSS_VERSION, T4.CVSS_SCORE, T4.CVE_ID, T5.VULN_SUMMARY, DATE_FORMAT(T5.MODI_DATE, \'%Y-%m-%d\') AS MODI_DATE, DATE_FORMAT(T5.PUBL_DATE, \'%Y-%m-%d\') AS PUBL_DATE, CAST(HIS.CVSS_SCORE AS DECIMAL(10, 1)) AS HIS_CVSS_SCORE, CAST(HIS.CVSS_SCORE_TO AS DECIMAL(10, 1)) AS HIS_CVSS_SCORE_TO FROM PROJECT_MASTER T1 INNER JOIN OSS_COMPONENTS T2 ON T1.PRJ_ID = T2.REFERENCE_ID AND T2.REFERENCE_DIV = \'50\' AND T2.EXCLUDE_YN <> \'Y\' INNER JOIN OSS_MASTER T4 ON T2.OSS_NAME = T4.OSS_NAME AND T2.OSS_VERSION = T4.OSS_VERSION AND T4.VULN_YN = \'N\' AND T4.USE_YN = \'Y\' AND T4.VULN_DATE IS NULL LEFT JOIN NVD_CVE_V3 T5 ON T4.CVE_ID = T5.CVE_ID INNER JOIN NVD_OSS_HIS HIS ON IFNULL(T4.OSS_NAME, T2.OSS_NAME) = HIS.OSS_NAME AND T2.OSS_VERSION = HIS.OSS_VERSION AND HIS.REG_DT > ADDDATE(SYSDATE(), INTERVAL - 3 DAY) WHERE T1.PRJ_ID = ?) RTN WHERE RTN.HIS_CVSS_SCORE_TO < RTN.HIS_CVSS_SCORE AND HIS_CVSS_SCORE >= ? AND HIS_CVSS_SCORE_TO < ? GROUP BY RTN.OSS_ID ORDER BY RTN.OSS_NAME' WHERE CD_NO=104 AND CD_DTL_NO=214;

ALTER TABLE `OSS_COMPONENTS` CHANGE `COPYRIGHT` `COPYRIGHT` MEDIUMTEXT;

DELETE FROM T2_CODE_DTL WHERE CD_NO IN ('204','207','211','216','220','221','222','223','304','305','306','400','401','500','501','502','702','703','904','906');
DELETE FROM T2_CODE WHERE CD_NO IN ('204','207','211','216','220','221','222','223','304','305','306','400','401','500','501','502','702','703','904','906');

DELETE FROM T2_CODE_DTL WHERE CD_NO = '122';
DELETE FROM T2_CODE WHERE CD_NO = '122';

DROP TABLE IF EXISTS `NVD_DATA_RUNNING_ON_WITH_TEMP`;
CREATE TABLE IF NOT EXISTS `NVD_DATA_RUNNING_ON_WITH_TEMP` (
	`MATCH_CRITERIA_ID` VARCHAR(128) NOT NULL,
	`CVE_ID` VARCHAR(16) NOT NULL,
	`VENDOR` VARCHAR(128) NOT NULL,
	`PRODUCT` VARCHAR(128) NOT NULL,
	`REL_MATCH_CRITERIA` VARCHAR(128) NOT NULL,
	PRIMARY KEY (`MATCH_CRITERIA_ID`, `CVE_ID`, `VENDOR`, `PRODUCT`, `REL_MATCH_CRITERIA`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `NVD_DATA_RUNNING_ON_WITH`;
CREATE TABLE IF NOT EXISTS `NVD_DATA_RUNNING_ON_WITH` (
	`MATCH_CRITERIA_ID` VARCHAR(128) NOT NULL,
	`CVE_ID` VARCHAR(16) NOT NULL,
	`VENDOR` VARCHAR(128) NOT NULL,
	`PRODUCT` VARCHAR(128) NOT NULL,
	`REL_MATCH_CRITERIA` VARCHAR(128) NOT NULL,
	PRIMARY KEY (`MATCH_CRITERIA_ID`, `CVE_ID`, `VENDOR`, `PRODUCT`, `REL_MATCH_CRITERIA`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE T2_CODE_DTL SET CD_DTL_EXP = 'SELECT T1.*, T3.USER_NAME AS SECPERSON_NM , T2.NOTICE_TYPE
FROM PROJECT_MASTER T1 LEFT OUTER JOIN OSS_NOTICE T2
ON T1.PRJ_ID = T2.PRJ_ID LEFT JOIN T2_USERS T3 ON T1.SECPERSON = T3.USER_ID
WHERE T1.PRJ_ID = ?'
WHERE CD_NO='104' AND CD_DTL_NO ='200';
