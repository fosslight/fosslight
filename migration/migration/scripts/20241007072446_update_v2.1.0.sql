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
    ('111', '820', 'project terminate default contents', '', '<p><Strong>Complete된 프로젝트이지만 더 이상 서비스되지 않아 Terminate 처리되었습니다.</strong><br />본 프로젝트의 배포 (opensource.lge.com)는 그대로 유지되며, 보안 취약점 관련 메일은 더 이상 발송되지 않습니다.&nbsp;</p> <p><strong>This project status is changed from "complete" to "terminate" because it is no longer in service.</strong><br />Please note that the distribution (opensource.lge.com) will be remained but the vulnerabillity related emails will be not sent from now on.&nbsp;</p>', 820, 'Y'),
    ('102', '820', '[FOSSLight][PRJ-${Project ID}] ${User} terminated : "${Project Name}"', '', '', 820, 'Y');

UPDATE T2_CODE_DTL
SET CD_DTL_EXP = '42,43,44,46,52,53,54,45,55,31,56,65,34,35,36,812,820'
WHERE CD_NO='110' AND CD_DTL_NO='31';

ALTER TABLE `PROJECT_MASTER` ADD `TERMINATE_YN` char NULL;

ALTER TABLE `OSS_COMPONENTS_LICENSE` DROP CONSTRAINT `fk_OSS_COMPONENTS_LICENSE_OSS_COMPONENTS1`;


