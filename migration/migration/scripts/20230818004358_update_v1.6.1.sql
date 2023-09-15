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

-- //@UNDO
-- SQL to undo the change goes here.


