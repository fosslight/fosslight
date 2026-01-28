-- // update_v2.5.0
-- Migration SQL that makes the change goes here.
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES
       ('102', '101', '[FOSSLight][PRJ-${Project ID}] Action Required for Inactive Project : "${Project Name}"', '', '', 101, 'Y'),
       ('102', '844', '[FOSSLight][3rd-${3rd Party ID}] : Action Required for Inactive 3rd Party : "${3rd Party Name}"', '', '', 844, 'Y'),
       ('102', '845', '[FOSSLight][SelfCheck-${SelfCheck Project ID}] : Action Required for Inactive Self-Check : "${SelfCheck Project Name}"', '', '', 845, 'Y'),
       ('103', '101', 'Notice Inactive Project', '', '200', 101, 'Y'),
       ('103', '844', 'Notice Inactive 3rd Party', '', '205', 844, 'Y'),
       ('103', '845', 'Notice Inactive self-check', '', '213', 845, 'Y'),
       ('110', '91', 'selfcheckInfo.html', '', '845', 25, 'Y')
       ('104', '217', 'selfcheck_basic_info', '', 'SELECT PRJ_NAME, PRJ_VERSION, DIVISION, CREATOR FROM PRE_PROJECT_MASTER WHERE PRJ_ID = ?', 20, 'Y'),
       ('111', '101', 'Notice inactive Project', '', '<p>현재 해당 프로젝트는 최근 6개월 동안 업데이트 내역이 없습니다. 프로젝트의 지속적인 관리와 효율적인 자원 활용을 위해 아래와 같이 조치를 요청드립니다.</p><br><strong>프로젝트 업데이트</strong> :  현재 단계를 수행하거나 혹은 다음 단계로 진행 부탁드리겠습니다.<br> <strong>프로젝트 삭제</strong> :  더 이상 진행 계획이 없을 경우, 삭제를 해주시길 바랍니다. </br></br> <p>This project has not been updated in the past six months. To ensure continuous management and efficient use of resources, please take one of the following actions.</p> <br> <strong>Project Update</strong> :  Please proceed with the current phase or move on to the next stage.<br><strong>Project Deletion</strong> : If there are no further plans to continue, please delete the project.', 101, 'Y'),
       ('111', '844', 'Notice inactive 3rd Party', '', '<p>현재 해당 3rd Party는 최근 6개월 동안 업데이트 내역이 없습니다. 3rd Party의 지속적인 관리와 효율적인 자원 활용을 위해 아래와 같이 조치를 요청드립니다.</p><br> <strong>3rd Party 업데이트</strong> : 현재 단계를 수행하거나 혹은 다음 단계로 진행 부탁드리겠습니다.<br> <strong>3rd Party 삭제</strong> : 더 이상 진행 계획이 없을 경우, 삭제를 해주시길 바랍니다.</br></br> <p>This 3rd Party has not been updated in the past six months. To ensure continuous management and efficient use of resources, please take one of the following actions:</p><br> <strong>3rd Party Update</strong> : Please proceed with the current phase or move on to the next stage.<br>  <strong>3rd Party Deletion</strong> : If there are no further plans to continue, please delete the 3rd Party.', 844, 'Y'),
       ('111', '845', 'Notice inactive self-check', '', '<p>현재 해당 Self-Check는 최근 6개월 동안 업데이트 내역이 없습니다. Self-Check의 지속적인 관리와 효율적인 자원 활용을 위해 아래와 같이 조치를 요청드립니다.</p><br> <strong>Self-Check 업데이트</strong> : 현재 단계를 수행하거나 혹은 다음 단계로 진행 부탁드리겠습니다.<br> <strong>Self-Check 삭제</strong> : 더 이상 진행 계획이 없을 경우, 삭제를 해주시길 바랍니다.</br></br> <p>This Self-Check has not been updated in the past six months. To ensure continuous management and efficient use of resources, please take one of the following actions:</p><br> <strong>Self-Check Update</strong> : Please proceed with the current phase or move on to the next stage.<br>  <strong>Self-Check Deletion</strong> : If there are no further plans to continue, please delete the Self-Check.', 845, 'Y');

UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '42,43,44,46,52,53,54,45,55,31,56,65,34,35,36,101,812' WHERE CD_NO = '110' AND CD_DTL_NO = '31';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '70,71,72,73,74,75,76,77,701,710,844' WHERE CD_NO = '110' AND CD_DTL_NO = '40';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = 'SELECT  T3.OSS_ID , T1.OSS_NAME , T1.OSS_VERSION , CONCAT(T3.CVE_ID, \' -> \' , IF(T3.CVE_ID_TO = \'\' OR T3.CVE_ID_TO IS NULL, \'NONE\', T3.CVE_ID_TO)) AS CVE_ID , CONCAT(CAST(T3.CVSS_SCORE AS DECIMAL(10, 1)), \' -> \' , CAST(T3.CVSS_SCORE_TO AS DECIMAL(10, 1))) AS CVSS_SCORE , T2.VULN_SUMMARY , DATE_FORMAT(T2.MODI_DATE, \'%Y-%m-%d\') AS MODI_DATE , DATE_FORMAT(T2.PUBL_DATE, \'%Y-%m-%d\') AS PUBL_DATE FROM OSS_MASTER T1  LEFT JOIN NVD_CVE_V3 T2 ON T1.CVE_ID = T2.CVE_ID  INNER JOIN NVD_OSS_HIS T3 ON T1.OSS_ID = T3.OSS_ID AND T3.REG_DT > ADDDATE(SYSDATE(), INTERVAL - 1 DAY) AND CAST(T3.CVSS_SCORE AS DECIMAL(10, 1)) > CAST(T3.CVSS_SCORE_TO AS DECIMAL(10, 1)) AND T3.CVSS_SCORE_TO != \'10.0\' AND CAST(T3.CVSS_SCORE AS DECIMAL(10, 1)) >= 7.0 AND CAST(T3.CVSS_SCORE_TO AS DECIMAL(10, 1)) < 7.0 GROUP BY OSS_ID, CVE_ID ORDER BY OSS_NAME, OSS_VERSION, CVSS_SCORE DESC' WHERE CD_NO = '104' AND CD_DTL_NO = '212';

DROP TABLE IF EXISTS `REQUEST_PERMISSION`;
CREATE TABLE `REQUEST_PERMISSION` (
	`REQ_SEQ` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`REQ_ID` VARCHAR(50) NOT NULL,
	`REQ_USER_ID` VARCHAR(50) NOT NULL,
	`STATUS` VARCHAR(6) NOT NULL,
	`REQ_DT` DATETIME NULL DEFAULT NULL,
	`APP_DT` DATETIME NULL DEFAULT NULL,
	`REJ_VIEW_DT` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`REQ_SEQ`) USING BTREE,
	UNIQUE INDEX `PRJ_ID` (`REQ_ID`, `REQ_USER_ID`, `REQ_SEQ`) USING BTREE,
	INDEX `STATUS` (`STATUS`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1;
ALTER TABLE `PROJECT_MASTER` ADD `IDENTIFICATION_CSV_FILE_ID` INT(11) DEFAULT NULL;
ALTER TABLE `OSS_COMPONENTS` ADD `REF_LOADED_VAL` VARCHAR(50) DEFAULT NULL;

DROP TABLE IF EXISTS `PROJECT_FILELIST`;
CREATE TABLE `PROJECT_FILELIST` (
	`UPL_FILE_SEQ` INT(11) NOT NULL AUTO_INCREMENT,
	`REFERENCE_ID` INT(11) NOT NULL,
	`REFERENCE_DIV` VARCHAR(6) NOT NULL COLLATE 'utf8mb4',
	`FILE_SEQ` INT(11) NOT NULL,
	`FILE_ID` INT(11) NOT NULL,
	`COMPONENT_COUNT` INT(11) NULL DEFAULT '0',
	`REG_DT` DATETIME NULL DEFAULT current_timestamp(),
	PRIMARY KEY (`REFERENCE_ID`, `REFERENCE_DIV`, `FILE_SEQ`) USING BTREE,
	INDEX `FILE_SEQ` (`FILE_SEQ`) USING BTREE,
	INDEX `FILE_ID` (`FILE_ID`) USING BTREE
) ENGINE=InnoDB CHARSET=utf8mb4;

UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '<p> <strong>Project 에 대한 Open Source Compliance Process가 모두 수행되어 Complete 처리합니다. </strong><br />OSS 고지문이나 Packaging 파일에 대한 수정이 필요하신 경우, Project Information탭 우측 상단의 "Repoen" 버튼을 클릭하여 Status 변경 요청하시기 바랍니다.<br />단, Distribution에서 Model 추가/삭제는 Status 변경 없이 가능합니다.</p> <p><strong>The Open Source Compliance Process for the Project is completed.</strong><br />If you need to modify the OSS Notice or the Packaging file, please request the status change to re-perform the Identification or Packaging by clicking "Reopen" button on Project Information Tab.<br />However, you can add or delete models in the distribution without changing the status.</p>' WHERE CD_NO = '111' AND CD_DTL_NO = '35';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '<p><Strong>Open Source Compliance Process 수행 완료하지 않고, Drop 처리됩니다.</strong><br />다시 Open Source Compliance Process를 진행하고자 하시는 경우, Project Informatoin탭 우측 상단의 "Repopen" 버튼을 클릭 후 진행하시기 바랍니다.</p> <p><strong>The status of the project changes to \''Drop\'', so you don\''t need to complete the Open Source Compliance process.</strong><br />If you want to proceed the Open Source Compliance Process again, please click "Reopen" button on Project information Tab.</p>' WHERE CD_NO = '111' AND CD_DTL_NO = '812';

INSERT INTO `T2_CODE` (`CD_NO`, `CD_NM`, `CD_EXP`, `SYS_CD_YN`) VALUES ('304', 'User', 'User Entity', 'N');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES
       	('304', '001', 'userName', '', 'String|USER Namee', 1, 'Y'),
		('304', '002', 'email', '', 'String|Email', 2, 'Y'),
		('304', '003', 'divisionName', '', 'String|Division Name', 3, 'Y'),
		('304', '004', 'token', '', 'String|Token', 4, 'Y'),
		('304', '005', 'expireDate', '', 'String|Expire Date', 5, 'Y'),
		('304', '006', 'modifier', '', 'String|Modifier', 6, 'Y'),
		('304', '007', 'modifiedDate', '', 'String|Modified Date', 7, 'Y'),
		('304', '008', 'useYn', '', 'String|Use Yn', 8, 'Y');
		
ALTER TABLE `OSS_ANALYSIS_STATUS` ADD `SEND_MAIL_FLAG` CHAR(1) DEFAULT NULL;

INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES
		('102', '850', '[FOSSLight][PRJ-${Project ID}] : "${User}" requested edit permission : "${Project Name}"', '', '', 850, 'Y'),
		('102', '851', '[FOSSLight][PRJ-${Project ID}] : "${Reviewer}" rejected edit permission : "${Project Name}"', '', '', 851, 'Y'),
		('102', '852', '[FOSSLight][3rd-${3rd Party ID}] : "${User}" requested edit permission : "${3rd Party Name}"', '', '', 852, 'Y'),
		('102', '853', '[FOSSLight][3rd-${3rd Party ID}] : "${Reviewer}" rejected edit permission : "${3rd Party Name}"', '', '', 853, 'Y'),
		('102', '854', '[FOSSLight][PRJ-${Project ID}] : "${User}" canceled requested edit permission : "${Project Name}"', '', '', 854, 'Y'),
		('102', '855', '[FOSSLight][3rd-${3rd Party ID}] : "${User}" canceled requested edit permission : "${3rd Party Name}"', '', '', 855, 'Y'),
       	('102', '856', '[FOSSLight][PRJ-${Project ID}] : "${User}" approved edit permission : "${Project Name}"', '', '', 856, 'Y'),
       	('102', '857', '[FOSSLight][3rd-${3rd Party ID}] : "${User}" approved edit permission : "${3rd Party Name}"', '', '', 857, 'Y'),
       	('103', '850', 'Request Permission For Project', '', '200', 850, 'Y'),
		('103', '851', 'Reject Permission For Project', '', '200', 851, 'Y'),
		('103', '852', 'Request Permission For Partner', '', '205', 852, 'Y'),
		('103', '853', 'Reject Permission For Partner', '', '205', 853, 'Y'),
		('103', '854', 'Cancel Request Permission For Project', '', '200', 854, 'Y'),
		('103', '855', 'Cancel Request Permission For Partner', '', '205', 855, 'Y'),
       	('103', '856', 'Approved Permission For Project', '', '200', 856, 'Y'),
       	('103', '857', 'Approved Permission For Partner', '', '205', 857, 'Y');
       
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '850,851,852,853,854,855,856,857' WHERE CD_NO = '110' AND CD_DTL_NO = '90';

UPDATE `T2_CODE_DTL` SET CD_DTL_NM = '[FOSSLight][PRJ-${Project ID}] : "${Reviewer}" approved edit permission : "${Project Name}"' WHERE CD_NO = '102' AND CD_DTL_NO = '856';
UPDATE `T2_CODE_DTL` SET CD_DTL_NM = '[FOSSLight][3rd-${3rd Party ID}] : "${Reviewer}" approved edit permission : "${3rd Party Name}"' WHERE CD_NO = '102' AND CD_DTL_NO = '857';
-- //@UNDO
-- SQL to undo the change goes here.


