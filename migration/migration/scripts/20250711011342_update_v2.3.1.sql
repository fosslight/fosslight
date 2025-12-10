
INSERT INTO `T2_CODE` (`CD_NO`, `CD_NM`, `CD_EXP`, `SYS_CD_YN`) VALUES
    ('207', 'Distribution Type', 'Depending on the type of project, Detail Description should be set. T for Transfer in-hous or Preceeding Software, N for Network Server, and A for Android or Yocto Model.', 'N');

INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES
	('207', '10', 'General', '', '', 1, 'Y'),
	('207', '100', 'In-house only', '', 'T', 10, 'Y'),
	('207', '20', 'Software only release (ex. Android application)', '', '', 2, 'N'),
	('207', '30', 'Transfer in-house', '', 'T', 3, 'Y'),
	('207', '40', 'B2B', '', '', 4, 'Y'),
	('207', '50', 'Preceding', '', 'T', 5, 'Y'),
	('207', '60', 'Network Server', '', 'N', 6, 'N'),
	('207', '70', 'ODM&OEM', '', '', 7, 'N'),
	('207', '80', 'Android Model (ex. Android Phone / TV)', '', 'A', 8, 'N'),
	('207', '90', 'Yocto Model', '', 'A', 9, 'N'),
	('207', '101', 'Self-Check', '', 'T', 11, 'Y');

INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES
       ('913', '019', 'crates.io/crates/', '', 'cargo', 19, 'Y'),
       ('210', '16', 'DEP', '', '', 2, 'Y'),
       ('207', '110', 'Contribution', '', 'T', 12, 'Y');
UPDATE `T2_CODE_DTL` SET CD_DTL_NM = '3rd Party' WHERE CD_NO = '210' AND CD_DTL_NO = '10';
UPDATE `T2_CODE_DTL` SET CD_DTL_NM = 'SRC' WHERE CD_NO = '210' AND CD_DTL_NO = '11';
UPDATE `T2_CODE_DTL` SET CD_DTL_NM = 'BIN' WHERE CD_NO = '210' AND CD_DTL_NO = '15';
UPDATE `T2_CODE_DTL` SET CD_DTL_NM = 'BOM' WHERE CD_NO = '210' AND CD_DTL_NO = '13';

UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = 'zip,tar.gz,gz,tar.xz,tar.bz2' WHERE CD_NO = '120' AND CD_DTL_NO = '16';

ALTER TABLE `OSS_ANALYSIS_MAP` ADD `COMMENTS_FLAG` char(1) DEFAULT NULL, ADD `COMMENTS` mediumtext DEFAULT NULL;
ALTER TABLE `OSS_ANALYSIS_STATUS` ADD `COMMENTS` mediumtext DEFAULT NULL;

ALTER TABLE `PARTNER_MASTER` ADD `CVE_ID` varchar(20) DEFAULT NULL, ADD `CVSS_SCORE_MAX` varchar(20) DEFAULT NULL;

INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES
	('301', '013', 'includeCpes', NULL, 'Array|Include CPE', 13, 'Y'),
	('301', '014', 'excludeCpes', NULL, 'Array|Exclude CPE', 14, 'Y'),
	('301', '015', 'ossVersionAliases', NULL, 'Array|OSS Version Alias', 15, 'Y'),
	('913', '020', 'nuget.org/packages/', '', 'nuget', 20, 'Y');
	
ALTER TABLE `OSS_ANALYSIS_MAP` ADD `COREVIEWER_YN` CHAR(1) DEFAULT 'N';

ALTER TABLE `PRE_PROJECT_MASTER` ADD `SRC_SCAN_FILE_ID` INT(11) DEFAULT NULL;

ALTER TABLE `PROJECT_MASTER` ADD `TRANSFER_DIVISION` varchar(100) DEFAULT NULL;

UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '<p>Identification 탭에 Open Source 목록을 작성 후 SBOM 탭에서 Request를 클릭하여 리뷰 요청하십시오.<br />Fill out the Open Source list in the Identification  and request a review by clicking Request in the SBOM tab.<br /><br />- Guide : https://fosslight.org/fosslight-guide-en/tutorial/1_project.html#2-identification</p>' WHERE CD_NO = '111' AND CD_DTL_NO = '33';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '<p>Identification 탭에 Open Source 목록을 작성 후 SBOM 탭에서 Request를 클릭하여 리뷰 요청하십시오.<br />Fill out the Open Source list in the Identification  and request a review by clicking Request in the SBOM tab.<br /><br />- Guide : https://fosslight.org/fosslight-guide-en/tutorial/1_project.html#2-identification</p>' WHERE CD_NO = '111' AND CD_DTL_NO = '37';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '<p>SBOM 탭의 Download Location, Homepage, Copyright text 정보가 DB 기반으로 업데이트 되었습니다.<br />Packaging 수행 후 Request 클릭하여 리뷰 요청해주시기 바랍니다.<br /> OSS Notice에 대하여 수정이 필요한 경우 (ex- text형식으로 발행), Packaging내 Notice탭에서 설정바랍니다.<br /><br />Download Location, Homepage and Copyright text in SBOM tab have been updated based on DB.<br />After performing Packaging, click Request to request a review.<br />If it is necessary to modify the OSS Notice (ex- should be issued in text format), please set it in the Notice tab in Packaging.</p>' WHERE CD_NO = '111' AND CD_DTL_NO = '41';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '<p>SBOM 탭의 Download Location, Homepage, Copyright text 정보가 DB 기반으로 업데이트 되었습니다.<br />Download Location, Homepage and Copyright text in SBOM tab have been updated based on DB.</p>' WHERE CD_NO = '111' AND CD_DTL_NO = '46';

CREATE TABLE IF NOT EXISTS `SEARCH_TEMPORARY` (
	`PRODUCT` VARCHAR(200) NOT NULL,
	`VERSION` VARCHAR(200) NOT NULL,
	INDEX `PRODUCT_VERSION` (`PRODUCT`, `VERSION`) USING BTREE
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = 'SELECT  T3.OSS_ID , T3.OSS_NAME , T3.OSS_VERSION , CONCAT(T3.CVE_ID, \' -> \' , IF(T3.CVE_ID_TO = \'\' OR T3.CVE_ID_TO IS NULL, \'NONE\', T3.CVE_ID_TO)) AS CVE_ID , CONCAT(CAST(T3.CVSS_SCORE AS DECIMAL(10, 1)), \' -> \' , CAST(T3.CVSS_SCORE_TO AS DECIMAL(10, 1))) AS CVSS_SCORE , T2.VULN_SUMMARY , DATE_FORMAT(T2.MODI_DATE, \'%Y-%m-%d\') AS MODI_DATE , DATE_FORMAT(T2.PUBL_DATE, \'%Y-%m-%d\') AS PUBL_DATE FROM OSS_MASTER T1  LEFT JOIN NVD_CVE_V3 T2 ON T1.CVE_ID = T2.CVE_ID  INNER JOIN NVD_OSS_HIS T3 ON T1.OSS_NAME = T3.OSS_NAME  AND IF(IFNULL(T1.OSS_VERSION, \'\') = \'\', \'-\', T1.OSS_VERSION) = T3.OSS_VERSION  AND T3.REG_DT > ADDDATE(SYSDATE(), INTERVAL - 1 DAY) AND CAST(T3.CVSS_SCORE AS DECIMAL(10, 1)) > CAST(T3.CVSS_SCORE_TO AS DECIMAL(10, 1)) AND T3.CVSS_SCORE_TO != \'10.0\' GROUP BY OSS_ID, CVE_ID ORDER BY OSS_NAME, OSS_VERSION, CVSS_SCORE DESC' WHERE CD_NO = '104' AND CD_DTL_NO = '212';