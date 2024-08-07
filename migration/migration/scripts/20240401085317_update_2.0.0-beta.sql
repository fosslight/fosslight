-- Change Default Tab Name
UPDATE T2_CODE_DTL SET CD_DTL_NM='License' WHERE CD_NO=701 AND CD_DTL_NO=002;
UPDATE T2_CODE_DTL SET CD_DTL_NM='Open Source' WHERE CD_NO=701 AND CD_DTL_NO=003;
UPDATE T2_CODE_DTL SET CD_DTL_NM='Project' WHERE CD_NO=701 AND CD_DTL_NO=004;
UPDATE T2_CODE_DTL SET CD_DTL_NM='3rd Party' WHERE CD_NO=701 AND CD_DTL_NO=005;
UPDATE T2_CODE_DTL SET CD_DTL_NM='BAT' WHERE CD_NO=701 AND CD_DTL_NO=006;
UPDATE T2_CODE_DTL SET CD_DTL_NM='Binary DB' WHERE CD_NO=701 AND CD_DTL_NO=007;
UPDATE T2_CODE_DTL SET CD_DTL_NM='Vulnerability' WHERE CD_NO=701 AND CD_DTL_NO=008;
UPDATE T2_CODE_DTL SET CD_DTL_NM='Self-check' WHERE CD_NO=701 AND CD_DTL_NO=009;

-- Change Comment division
UPDATE T2_CODE_DTL SET CD_DTL_NM='ProjectInfo' WHERE CD_NO=214 AND CD_DTL_NO=19;

-- Change BOM oss info
UPDATE T2_CODE_DTL SET CD_DTL_EXP='SELECT T1.*, GROUP_CONCAT(DISTINCT(T2.LICENSE_NAME)) AS LICENSE_NAME FROM OSS_COMPONENTS T1 INNER JOIN OSS_COMPONENTS_LICENSE T2 ON T1.COMPONENT_ID = T2.COMPONENT_ID AND IFNULL(T2.EXCLUDE_YN, \'N\') = \'N\' WHERE REFERENCE_ID = ? AND REFERENCE_DIV = \'13\' AND T1.MERGE_PRE_DIV IS NULL GROUP BY T1.OSS_NAME, T1.OSS_VERSION ORDER BY COMPONENT_ID' WHERE CD_NO = 104 AND CD_DTL_NO = 201;

ALTER TABLE `PROJECT_MASTER` ADD `PACKAGE_FILE_TYPE1` varchar(1) NULL DEFAULT NULL;
ALTER TABLE `PROJECT_MASTER` ADD `PACKAGE_FILE_TYPE2` varchar(1) NULL DEFAULT NULL;
ALTER TABLE `PROJECT_MASTER` ADD `PACKAGE_FILE_TYPE3` varchar(1) NULL DEFAULT NULL;

-- Update Discovered mail
UPDATE T2_CODE_DTL SET CD_DTL_EXP = 'SELECT RTN.OSS_ID, RTN.OSS_NAME, RTN.OSS_VERSION, RTN.CVSS_SCORE, RTN.CVE_ID, RTN.VULN_SUMMARY, RTN.MODI_DATE, RTN.PUBL_DATE FROM ( SELECT T2.OSS_ID, T2.OSS_NAME, T2.OSS_VERSION, T4.CVSS_SCORE, T4.CVE_ID, T5.VULN_SUMMARY, DATE_FORMAT(T5.MODI_DATE, \'%Y-%m-%d\') AS MODI_DATE, DATE_FORMAT(T5.PUBL_DATE, \'%Y-%m-%d\') AS PUBL_DATE FROM PROJECT_MASTER T1 INNER JOIN OSS_COMPONENTS T2 ON T1.PRJ_ID = T2.REFERENCE_ID AND T2.REFERENCE_DIV = \'50\' AND T2.EXCLUDE_YN <> \'Y\' INNER JOIN OSS_MASTER T3 ON IFNULL(T2.REF_OSS_NAME, T2.OSS_NAME) = T3.OSS_NAME AND IFNULL(T2.OSS_VERSION, \'\') = IFNULL(T3.OSS_VERSION, \'\') AND T3.USE_YN = \'Y\' INNER JOIN OSS_DISCOVERED_SND_EMAIL T4 ON T3.OSS_ID = T4.OSS_ID AND T4.SND_YN != \'Y\' LEFT JOIN NVD_CVE_V3 T5 ON T4.CVE_ID = T5.CVE_ID WHERE T1.PRJ_ID = ?) RTN GROUP BY RTN.OSS_ID, RTN.CVE_ID ORDER BY RTN.OSS_NAME, RTN.CVSS_SCORE DESC, RTN.MODI_DATE DESC' WHERE CD_NO = 104 AND CD_DTL_NO = 207;

-- add column
ALTER TABLE `OSS_COMPONENTS` ADD `PACKAGE_URL` varchar(2000) NULL DEFAULT NULL;

-- alter data type
ALTER TABLE `OSS_COMPONENTS` MODIFY `COPYRIGHT` LONGTEXT DEFAULT NULL;

-- Add user columns tatbel
CREATE TABLE `USER_COLUMNS` (
  `COLUMNS` longtext DEFAULT NULL,
  `LIST_TYPE` varchar(20) NOT NULL DEFAULT '',
  `CREATED_DATE` datetime NOT NULL DEFAULT current_timestamp(),
  `UPDATED_DATE` datetime NOT NULL DEFAULT current_timestamp(),
  `USER_ID` varchar(45) NOT NULL DEFAULT '',
  PRIMARY KEY (`LIST_TYPE`,`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
