-- // update v1.5.0
-- Migration SQL that makes the change goes here.
ALTER TABLE `NVD_DATA_SCORE_V3` ADD `VENDOR` varchar(128) NOT NULL DEFAULT '';
ALTER TABLE `NVD_DATA_SCORE_V3` ADD `VENDORPRODUCT` varchar(255) NULL;
ALTER TABLE `NVD_DATA_SCORE_V3` ADD KEY `VENDORPRODUCT_VERSION` (`VENDORPRODUCT`,`VERSION`);
ALTER TABLE `NVD_DATA_TEMP_V3` ADD `VENDOR` varchar(128) NOT NULL DEFAULT '';
ALTER TABLE `NVD_DATA_TEMP_V3` ADD `VENDORPRODUCT` varchar(255) NULL;
ALTER TABLE `NVD_DATA_V3` ADD `VENDOR` varchar(128) NOT NULL DEFAULT '';
ALTER TABLE `NVD_DATA_V3` ADD `VENDORPRODUCT` varchar(255) NULL;
ALTER TABLE `NVD_DATA_V3` ADD KEY `VENDORPRODUCT_VERSION` (`VENDORPRODUCT`,`VERSION`);
ALTER TABLE `OSS_DOWNLOADLOCATION` CHANGE `SORT_ORDER` `SORT_ORDER` varchar(50) DEFAULT NULL;
ALTER TABLE `PROJECT_MASTER` ADD `REVIEW_REPORT_FILE_ID` int (11) DEFAULT NULL;
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "705" OR `CD_NO` = "911" OR `CD_NO` = "912";
DELETE FROM `T2_CODE` WHERE `CD_NO` = "705" OR `CD_NO` = "911" OR `CD_NO` = "912";
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "910";
INSERT INTO
    `T2_CODE` (`CD_NO`, `CD_NM`, `CD_EXP`, `SYS_CD_YN`)
VALUES
    (
        '750',
        'Vulnerability Mailing Score',
        'Vulnerability Mailing Score Code',
        'N'
    );
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "110" AND `CD_DTL_NO` = "30";
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "110" AND `CD_DTL_NO` = "31";
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "110" AND `CD_DTL_NO` = "70";
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "111" AND `CD_DTL_NO` = "41";
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "111" AND `CD_DTL_NO` = "72";
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "103" AND `CD_DTL_NO` = "46";
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "103" AND `CD_DTL_NO` = "72";
DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "705" AND `CD_DTL_NO` = "100";
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
    ('102', '470', '[FOSSLight][3rd-${3rd Party ID}] ${User} commit Binary DB (${BinaryCommitResult}) : "${Project Name}"', '', '', 470, 'Y'),
    ('102', '671', '[FOSSLight][PRJ-${Project ID}] Distribution, ${User} changed description: [${Project Name}]', '', 'Distribution, changed description', 671, 'Y'),
    ('102', '700', '[FOSSLight][3rd-${3rd Party ID}] ${User} changed : "${3rd Party Name}"', '', '', 700, 'Y'),
    ('102', '100', '[FOSSLight][PRJ-${Project ID}] Packaging, ${User} requested : " ${Project Name}"', '', '', 100, 'Y'),
    ('102', '817', '[FOSSLight] Your password has been reset : ${User}', '', '${User}''s password has been reset.', 817, 'Y'),
    ('103', '46', 'Project Identification Confirmed Only', '', '200, 201, 202', 46, 'Y'),
    ('103', '670', 'redistribution changed description file', '', '200,203,204', 670, 'Y'),
    ('103', '671', 'redistribution changed description', '', '200,203,204', 671, 'Y'),
    ('103', '700', '3rd party 기본정보 변경 시', '', '205', 700, 'Y'),
    ('103', '72', '3rd party confirm 시', '', '205, 209, 215', 72, 'Y'),
    ('103', '100', 'identification(BIN) confirm', '', '200, 202, 211', 100, 'Y'),
    ('104', '215', 'partner_disclose_oss_info', '', 'SELECT T1.COMPONENT_ID, T1.REFERENCE_ID, T1.REFERENCE_DIV, T1.COMPONENT_IDX, T1.OSS_ID, T1.OSS_NAME, T1.OSS_VERSION, T1.DOWNLOAD_LOCATION, T1.HOMEPAGE, T1.FILE_PATH, T1.EXCLUDE_YN, T1.BINARY_NAME, T1.BINARY_SIZE, T1.BINARY_NOTICE, T1.CUSTOM_BINARY_YN, T1.REF_PARTNER_ID, T1.REF_PRJ_ID, T1.REF_BAT_ID, T1.REF_COMPONENT_ID, T1.REPORT_FILE_ID, T1.MERGE_PRE_DIV, T1.OBLIGATION_TYPE, T1.BAT_STRING_MATCH_PERCENTAGE, T1.BAT_PERCENTAGE, T1.BAT_SCORE, T1.BAT_CHECKSUM, T1.VERIFY_FILE_COUNT, T1.VERIFIED_YN, T1.CREATED_DATE, T1.MODIFIED_DATE, T1.COMMENTS, T1.REF_DIV, GROUP_CONCAT(T2.LICENSE_NAME) AS LICENSE_NAME, GROUP_CONCAT(T2.LICENSE_ID ORDER BY T2.COMPONENT_LICENSE_ID) AS LICENSE_ID_LIST FROM OSS_COMPONENTS T1 INNER JOIN OSS_COMPONENTS_LICENSE T2 ON T1.COMPONENT_ID = T2.COMPONENT_ID AND IFNULL(T2.EXCLUDE_YN, \'N\') = \'N\' WHERE REFERENCE_ID = ? AND REFERENCE_DIV = \'20\' AND MERGE_PRE_DIV IS NULL AND T1.OBLIGATION_TYPE =\'11\' AND IFNULL(T1.EXCLUDE_YN, \'N\') = \'N\' GROUP BY COMPONENT_ID ORDER BY COMPONENT_ID', 16, 'Y'),
    ('104', '216', 'partner_not_disclose_oss_info', '', 'SELECT T1.COMPONENT_ID, T1.REFERENCE_ID, T1.REFERENCE_DIV, T1.COMPONENT_IDX, T1.OSS_ID, T1.OSS_NAME, T1.OSS_VERSION, T1.DOWNLOAD_LOCATION, T1.HOMEPAGE, T1.FILE_PATH, T1.EXCLUDE_YN, T1.BINARY_NAME, T1.BINARY_SIZE, T1.BINARY_NOTICE, T1.CUSTOM_BINARY_YN, T1.REF_PARTNER_ID, T1.REF_PRJ_ID, T1.REF_BAT_ID, T1.REF_COMPONENT_ID, T1.REPORT_FILE_ID, T1.MERGE_PRE_DIV, T1.OBLIGATION_TYPE, T1.BAT_STRING_MATCH_PERCENTAGE, T1.BAT_PERCENTAGE, T1.BAT_SCORE, T1.BAT_CHECKSUM, T1.VERIFY_FILE_COUNT, T1.VERIFIED_YN, T1.CREATED_DATE, T1.MODIFIED_DATE, T1.COMMENTS, T1.REF_DIV, GROUP_CONCAT(T2.LICENSE_NAME) AS LICENSE_NAME, GROUP_CONCAT(T2.LICENSE_ID ORDER BY T2.COMPONENT_LICENSE_ID) AS LICENSE_ID_LIST FROM OSS_COMPONENTS T1 INNER JOIN OSS_COMPONENTS_LICENSE T2 ON T1.COMPONENT_ID = T2.COMPONENT_ID AND IFNULL(T2.EXCLUDE_YN, \'N\') = \'N\' WHERE REFERENCE_ID = ? AND REFERENCE_DIV = \'20\' AND MERGE_PRE_DIV IS NULL AND T1.OBLIGATION_TYPE =\'11\' AND IFNULL(T1.EXCLUDE_YN, \'N\') = \'N\' GROUP BY COMPONENT_ID ORDER BY COMPONENT_ID', 17, 'Y'),
    ('110', '30', 'projectInfo.html', '', '30,40,41,50,51,60,90,61,62,33,66,67,68,38,92,99,100,670,671', 5, 'Y'),
    ('110', '31', 'commentWithProjectInfo.html', '', '42,43,44,46,52,53,54,45,55,31,56,65,34,35,36,812', 6, 'Y'),
    ('110', '41', 'partnerModify.html', '', '700', 19, 'Y'),
    ('110', '70', 'binaryDBDataCommitInfo.html', '', '47,470', 17, 'Y'),
    ('110', '71', 'resetUserPassword.html', '', '817', 18, 'Y'),
    ('111', '41', 'Project Identification Confirmed', '', '<p>BOM 탭의 Download Location, Homepage, Copyright text 정보가 DB 기반으로 업데이트 되었습니다.<br />Packaging 수행 후 Request 클릭하여 리뷰 요청해주시기 바랍니다.<br /> OSS Notice에 대하여 수정이 필요한 경우 (ex- text형식으로 발행), Packaging내 Notice탭에서 설정바랍니다.<br /><br />Download Location, Homepage and Copyright text in BOM tab have been updated based on DB.<br />After performing Packaging, click Request to request a review.<br />If it is necessary to modify the OSS Notice (ex- should be issued in text format), please set it in the Notice tab in Packaging.</p>', 41, 'Y'),
    ('111', '46', 'Project Identification Confirmed Only', '', '<p>BOM 탭의 Download Location, Homepage, Copyright text 정보가 DB 기반으로 업데이트 되었습니다.<br />Download Location, Homepage and Copyright text in BOM tab have been updated based on DB.</p>', 46, 'Y'),
    ('111', '72', '3rd party review confirm', '', '<p>3rd Party Software 리뷰가 완료되었습니다.</br > Project List - Identification - 3rd Party 탭에서 Confrim된 3rd Party Software를 불러올 수 있습니다.</br > 혹은 배포하는 Software가 3rd Party Software로만 구성되는 경우, 3rd Party List - 3rd Party 우측 상단의 <strong>Create Project for OSS Notice</strong>
버튼을 클릭하여, Identification Confirm 상태의 Project를 바로 생성할 수 있습니다.</br ></br > 3rd Party Software is confirmed by reviewer.</br > You can load this confirmed 3rd Party Software from "Project List - Identifiation - 3rd Party tab".</br > Or you can directly create project (Identification Confirmed status) by clicking the <strong>Create Project for OSS Notice button</strong> at the top right of the 3rd Party when the distributed software consists only of 3rd party software.</br ></br > </p>', 72, 'Y'),
    ('111', '100', 'Project Identification(BIN) Confirmed', '', '<p>Packaging 수행 후 Request 클릭하여 리뷰 요청해주시기 바랍니다.<br /> OSS Notice에 대하여 수정이 필요한 경우 (ex- text형식으로 발행), Packaging내 Notice탭에서 설정바랍니다.<br /><br />After performing Packaging, click Request to request a review.<br />If it is necessary to modify the OSS Notice (ex- should be issued in text format), please set it in the Notice tab in Packaging.</p>', 100, 'Y'),
    (
        '750',
        '100',
        'Vulnerability Mailing Standard Score',
        '',
        '8.0',
        1,
        'Y'
    ),
    ('903', '007', 'www.npmjs.org/package/', '', 'npm url', 7, 'Y'),
    (
        '903',
        '008',
        'android.googlesource.com/platform',
        '',
        'android url',
        8,
        'Y'
    ),
    ('903', '009', 'www.nuget.org/packages/', '', 'nuget url', 9, 'Y'),
    ('910', '100', 'ldap server url', '', '', 1, 'Y'),
    ('910', '200', 'ldap domain', '', '', 2, 'Y');
CREATE TABLE `BINARY_DATA_HIS` (
        `ACTION_ID` VARCHAR(50) NOT NULL COMMENT 'ACTION GROUP ID',
        `ACTION_TYPE` VARCHAR(50) NOT NULL COMMENT 'INSERT/DELETE',
        `FILE_NAME` VARCHAR(255) NOT NULL,
        `PATH_NAME` VARCHAR(1024) NULL DEFAULT NULL,
        `SOURCE_PATH` VARCHAR(1024) NULL DEFAULT NULL,
        `CHECK_SUM` VARCHAR(255) NULL DEFAULT NULL,
        `TLSH_CHECK_SUM` VARCHAR(1024) NULL DEFAULT NULL,
        `OSS_NAME` VARCHAR(255) NULL DEFAULT NULL,
        `OSS_VERSION` VARCHAR(255) NULL DEFAULT NULL,
        `LICENSE` VARCHAR(255) NULL DEFAULT NULL,
        `PARENT_NAME` VARCHAR(255) NULL DEFAULT NULL,
        `PLATFORM_NAME` VARCHAR(255) NULL DEFAULT NULL,
        `PLATFORM_VERSION` VARCHAR(255) NULL DEFAULT NULL,
        `UPDATE_DATE` TIMESTAMP NULL DEFAULT NULL,
        `CREATOR` VARCHAR(50) NULL DEFAULT NULL,
        `CREATED_DATE` TIMESTAMP NULL DEFAULT current_timestamp(),
        `COMMENT` MEDIUMTEXT NULL DEFAULT NULL,
        `MODIFIER` VARCHAR(50) NULL DEFAULT NULL,
        INDEX `ACTION_ID_ACTION_TYPE` (`ACTION_ID`, `ACTION_TYPE`) USING BTREE
);
CREATE TABLE `BINARY_DATA` (
        `FILE_NAME` VARCHAR(255) NOT NULL,
        `PATH_NAME` VARCHAR(1024) NOT NULL,
        `SOURCE_PATH` VARCHAR(1024) NULL DEFAULT NULL,
        `CHECK_SUM` VARCHAR(255) NOT NULL,
        `TLSH_CHECK_SUM` VARCHAR(1024) NULL DEFAULT NULL,
        `OSS_NAME` VARCHAR(255) NOT NULL,
        `OSS_VERSION` VARCHAR(255) NULL DEFAULT NULL,
        `LICENSE` VARCHAR(255) NULL DEFAULT NULL,
        `PARENT_NAME` VARCHAR(255) NULL DEFAULT NULL,
        `PLATFORM_NAME` VARCHAR(255) NULL DEFAULT NULL,
        `PLATFORM_VERSION` VARCHAR(255) NULL DEFAULT NULL,
        `UPDATE_DATE` TIMESTAMP NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
        INDEX `FILE_NAME_CHECK_SUM` (`FILE_NAME`, `CHECK_SUM`) USING BTREE
);
-- //@UNDO
-- SQL to undo the change goes here.


