
INSERT INTO `T2_CODE` (`CD_NO`, `CD_NM`, `CD_EXP`, `SYS_CD_YN`) VALUES
    ('207', 'Distribution Type', 'Depending on the type of project, Detail Description should be set. T for Transfer in-hous or Preceeding Software, N for Network Server, and A for Android or Yocto Model.', 'N');

INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES
                                                                                                                   ('207', '10', 'General', '', '', 1, 'Y'),
                                                                                                                   ('207', '100', 'In-house only', '', 'T', 10, 'Y'),
                                                                                                                   ('207', '20', 'Software only release (ex. Android application)', '', '', 2, 'N'),
                                                                                                                   ('207', '30', 'Transfer in-house', '', 'T', 3, 'Y'),
                                                                                                                   ('207', '40', 'B2B', '', '', 4, 'Y'),
                                                                                                                   ('207', '50', 'Preceding', '', 'T', 5, 'Y'),
                                                                                                                   ('207', '60', 'Network Server', '', 'N', 6, 'N'),
                                                                                                                   ('207', '70', 'ODM&OEM', '', '', 7, 'N'),
                                                                                                                   ('207', '80', 'Android Model (ex. Android Phone / TV)', '', 'A', 8, 'N'),
                                                                                                                   ('207', '90', 'Yocto Model', '', 'A', 9, 'N'),
                                                                                                                   ('207', '101', 'Self-Check', '', 'T', 11, 'Y');



