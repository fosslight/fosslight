ALTER TABLE `PROJECT_MASTER` ADD `VUL_DOC_SKIP_YN` char(1) DEFAULT 'N';

INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('214', '22', 'Identification', '', '', 10, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('214', '23', 'Identification temporary save comments', '', '', 11, 'Y');

INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('102', '132', '[FOSSLight][OSS-${OSS ID}] ${User} deleted comment on : "${OSS Name}"', '', '', 132, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('102', '233', '[FOSSLight][LIC-${License ID}] ${User} deleted comment on : "${License Name}"', '', '', 233, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('102', '341', '[FOSSLight][PRJ-${Project ID}] ${User} deleted comment on : "${Project Name}"', '', '', 341, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('102', '431', '[FOSSLight][PRJ-${Project ID}] Identification, ${User} deleted comment on : "${Project Name}"', '', '', 431, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('102', '531', '[FOSSLight][PRJ-${Project ID}] Packaging, ${User} deleted comment on : "${Project Name}"', '', '', 531, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('102', '651', '[FOSSLight][PRJ-${Project ID}] Distribution, ${User} deleted comment on : "${Project Name}"', '', '', 651, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('102', '741', '[FOSSLight][3rd-${3rd Party ID}] ${User} deleted comment on : "${3rd Party Name}"', '', '', 741, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('102', '742', '[FOSSLight][3rd-${3rd Party ID}] Identification, ${User} modified comment on : "${3rd Party Name}"', '', '', 742, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('102', '743', '[FOSSLight][3rd-${3rd Party ID}] Identification, ${User} deleted comment on : "${3rd Party Name}"', '', '', 743, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('103', '132', 'oss comment 삭제', '', '100', 132, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('103', '233', 'license comment 삭제', '', '101', 233, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('103', '341', 'Project BasicInformation에서 Comment 삭제', '', '200', 341, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('103', '431', 'Identification comment 삭제', '', '200', 431, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('103', '531', 'Packaging comment 삭제', '', '200', 531, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('103', '651', 'Distribution comment 삭제', '', '200', 651, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('103', '741', '3rd comment 삭제', '', '205', 741, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('103', '742', '3rd Identification comment 수정', '', '205', 742, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('103', '743', '3rd Identification comment 삭제', '', '205', 743, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('903', '013', 'git.codelinaro.org', '', codelinaro url', 13, 'Y');

UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '130,132,230,233,340,341,430,431,530,531,650,651,740,741,742,743' WHERE CD_NO = '110' AND CD_DTL_NO = '36';

ALTER TABLE `PRE_PROJECT_MASTER` DROP COLUMN `OS_TYPE`;
ALTER TABLE `PRE_PROJECT_MASTER` DROP COLUMN `OS_TYPE_ETC`;
ALTER TABLE `PRE_PROJECT_MASTER` DROP COLUMN `DISTRIBUTION_TYPE`;

ALTER TABLE `COMMENTS_HISTORY` MODIFY `MODIFIED_DATE` DATETIME NULL;

INSERT INTO `PROCESS_GUIDE` (`ID`, `PAGE_TARGET`, `CONTENTS`, `URL`, `USE_YN`) VALUES ('Hompage_Link', 'Main', NULL, 'https://fosslight.org/hub-guide-en/', 'Y');
INSERT INTO `PROCESS_GUIDE` (`ID`, `PAGE_TARGET`, `CONTENTS`, `URL`, `USE_YN`) VALUES ('Newsletter_Link', 'Main', NULL, 'https://fosslight.org/news/', 'Y');
INSERT INTO `PROCESS_GUIDE` (`ID`, `PAGE_TARGET`, `CONTENTS`, `URL`, `USE_YN`) VALUES ('Support_Link', 'Main', NULL, 'https://github.com/fosslight/fosslight/issues', 'Y');
INSERT INTO `PROCESS_GUIDE` (`ID`, `PAGE_TARGET`, `CONTENTS`, `URL`, `USE_YN`) VALUES ('Tips_Link', 'Main', NULL, 'https://www.youtube.com/@LGEOSPO', 'Y');

ALTER TABLE `PROJECT_MASTER` CHANGE `DESTRIBUTION_STATUS` `DISTRIBUTION_STATUS` VARCHAR(6);