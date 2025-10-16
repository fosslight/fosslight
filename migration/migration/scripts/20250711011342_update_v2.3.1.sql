
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
       ('210', '16', 'DEP', '', '', 2, 'Y');
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