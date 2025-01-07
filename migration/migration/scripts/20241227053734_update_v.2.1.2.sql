ALTER TABLE `PROJECT_MASTER` ADD `VULDOC_SKIP_YN` char(1) DEFAULT 'N';

INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('214', '22', 'Identification', '', '', 10, 'Y');
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES ('214', '23', 'Identification temporary save comments', '', '', 11, 'Y');