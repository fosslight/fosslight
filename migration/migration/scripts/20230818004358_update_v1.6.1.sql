-- // update v1.6.1
-- Migration SQL that makes the change goes here.

DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "122";
DELETE FROM `T2_CODE` WHERE `CD_NO` = "122";
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "120" AND `CD_DTL_NO` = "16";
INSERT INTO
    `T2_CODE_DTL` (
    `CD_NO`,
    `CD_DTL_NO`,
    `CD_DTL_NM`,
    `CD_SUB_NO`,
    `CD_DTL_EXP`,
    `CD_ORDER`,
    `USE_YN`
)
VALUES
    ('120', '16', '프로젝트 PACKAGING FILE', '', 'zip,tar.gz,gz', 6, 'Y'),
    ('903', '010', 'stackoverflow.com/revisions/', '', 'stackoverflow url', 10, 'Y');

ALTER TABLE `PROJECT_MASTER` ADD `IDENTIFICATION_SUB_STATUS_DEP` varchar(1) NULL DEFAULT NULL;
ALTER TABLE `PROJECT_MASTER` ADD `DEP_CSV_FILE_ID` int(11) NULL DEFAULT NULL;
ALTER TABLE `OSS_COMPONENTS` ADD `DEPENDENCIES` varchar(2000) NULL DEFAULT NULL;
ALTER TABLE `OSS_COMPONENTS` ADD `REF_OSS_NAME` varchar(200) NULL DEFAULT NULL;

INSERT INTO `PROCESS_GUIDE` VALUES ('License_Edit_Info', 'License_Edit', '<div style="background:#eeeeee; border:1px solid #cccccc; padding:5px 10px">&bull;<strong> SPDX License인 경우 :</strong> SPDX 링크로 website를 추가<br />&bull;<strong> License 이름이 한글인 경우 :</strong> 1. License 이름 영문으로 변경하고, 2. 한글 License명은 License text 최상단에 포함<br />&bull;<strong> MIT/BSD-like인 경우 :</strong> MIT/BSD-like (OSS_Name) 으로 이름을 설정</div>', '', 'Y');

ALTER TABLE `PROJECT_MASTER` ADD `CDX_JSON_FILE_ID` int(11) NULL DEFAULT NULL;
ALTER TABLE `PROJECT_MASTER` ADD `CDX_XML_FILE_ID` int(11) NULL DEFAULT NULL;

UPDATE T2_CODE_DTL
SET CD_DTL_EXP = '<i class="far fa-file-alt fa-1-3x" title="Notice"></i>'
WHERE CD_NO='217' AND CD_DTL_NO='10';

UPDATE T2_CODE_DTL
SET CD_DTL_EXP = '<i class="far fa-file-alt fa-1-3x mr-1" title="Notice"></i><i class="far fa-file-code fa-1-3x" title="Source Code"></i>'
WHERE CD_NO='217' AND CD_DTL_NO='11';

-- External link list [Demo]
INSERT INTO `PROCESS_GUIDE` VALUES ('Hompage_Link', 'Main', NULL, 'https://fosslight.org/fosslight-guide-en/about/', 'Y');
INSERT INTO `PROCESS_GUIDE` VALUES ('Newsletter_Link', 'Main',NULL, 'https://fosslight.org/news/', 'Y');
INSERT INTO `PROCESS_GUIDE` VALUES ('Tips_Link', 'Main',NULL, 'https://www.youtube.com/@LGEOSPO', 'Y');
INSERT INTO `PROCESS_GUIDE` VALUES ('Support_Link', 'Main',NULL, 'https://github.com/fosslight/fosslight/issues', 'Y');

-- External link list [LGE] Reference
-- INSERT INTO `PROCESS_GUIDE` VALUES ('Hompage_Link', 'Main', NULL, 'http://collab.lge.com/main/x/VacZIg', 'Y');
-- INSERT INTO `PROCESS_GUIDE` VALUES ('Newsletter_Link', 'Main',NULL, 'http://collab.lge.com/main/x/QwYBFw', 'Y');
-- INSERT INTO `PROCESS_GUIDE` VALUES ('Tips_Link', 'Main',NULL, 'http://collab.lge.com/main/x/Kkh1KQ', 'Y');
-- INSERT INTO `PROCESS_GUIDE` VALUES ('Support_Link', 'Main',NULL, 'http://clm.lge.com/issue/projects/OSC', 'Y');

-- //@UNDO
-- SQL to undo the change goes here.


