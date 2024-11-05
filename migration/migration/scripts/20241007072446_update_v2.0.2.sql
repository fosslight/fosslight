ALTER TABLE `OSS_COMPONENTS_LICENSE` DROP CONSTRAINT `fk_OSS_COMPONENTS_LICENSE_OSS_COMPONENTS1`;

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
    ('205', 'FREV', 'Final Review', '', '', 6, 'Y'),
    ('206', 'FREV', 'Final Review', '', 'Final Review', 6, 'Y');
