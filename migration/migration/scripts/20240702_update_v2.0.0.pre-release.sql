-- // update v1.6.2
-- Migration SQL that makes the change goes here.

ALTER TABLE `PRE_PROJECT_MASTER`
    ADD COLUMN `PACKAGE_FILE_IDS` VARCHAR(255) NULL DEFAULT NULL;
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`)
VALUES ('110', '232', 'licenseInvalidNotify.html', '', '232', 20, 'Y'),
       ('102', '232', '[FOSSLight][LIC] Invalid License Identified', '', '', 232, 'Y');

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE `PRE_PROJECT_MASTER` DROP COLUMN `PACKAGE_FILE_IDS`;

