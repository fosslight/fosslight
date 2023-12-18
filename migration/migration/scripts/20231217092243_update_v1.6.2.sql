-- // update v1.6.2
-- Migration SQL that makes the change goes here.

ALTER TABLE `PRE_PROJECT_MASTER`
    ADD COLUMN `PACKAGE_FILE_IDS` VARCHAR(255) NULL DEFAULT NULL;
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`)
VALUES ('110', '231', 'licenseInvalidNotify.html', '', '231', 20, 'Y'),
       ('102', '231', '[FOSSLight][LIC] Invalid License Identified', '', '', 231, 'Y');

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE `PRE_PROJECT_MASTER` DROP COLUMN `PACKAGE_FILE_IDS`;

