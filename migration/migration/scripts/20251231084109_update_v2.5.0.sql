-- // update_v2.5.0
-- Migration SQL that makes the change goes here.
INSERT INTO `T2_CODE_DTL` (`CD_NO`, `CD_DTL_NO`, `CD_DTL_NM`, `CD_SUB_NO`, `CD_DTL_EXP`, `CD_ORDER`, `USE_YN`) VALUES
       ('102', '101', '[FOSSLight][PRJ-${Project ID}] Action Required for Inactive Project : "${Project Name}"', '', '', 101, 'Y'),
       ('102', '844', '[FOSSLight][3rd-${3rd Party ID}] : Action Required for Inactive 3rd Party : "${3rd Party Name}"', '', '', 844, 'Y'),
       ('102', '845', '[FOSSLight][SelfCheck-${SelfCheck Project ID}] : Action Required for Inactive Self-Check : "${SelfCheck Project Name}"', '', '', 845, 'Y'),
       ('103', '101', 'Notice Inactive Project', '', '200', 101, 'Y'),
       ('103', '844', 'Notice Inactive 3rd Party', '', '205', 844, 'Y'),
       ('103', '845', 'Notice Inactive self-check', '', '213', 845, 'Y'),
       ('110', '91', 'selfcheckInfo.html', '', '845', 25, 'Y')
       ('104', '217', 'selfcheck_basic_info', '', 'SELECT PRJ_NAME, PRJ_VERSION, DIVISION, CREATOR FROM PRE_PROJECT_MASTER WHERE PRJ_ID = ?', 20, 'Y'),
       ('111', '101', 'Notice inactive Project', '', '<p>현재 해당 프로젝트는 최근 6개월 동안 업데이트 내역이 없습니다. 프로젝트의 지속적인 관리와 효율적인 자원 활용을 위해 아래와 같이 조치를 요청드립니다.</p><br><strong>프로젝트 업데이트</strong> :  현재 단계를 수행하거나 혹은 다음 단계로 진행 부탁드리겠습니다.<br> <strong>프로젝트 삭제</strong> :  더 이상 진행 계획이 없을 경우, 삭제를 해주시길 바랍니다. </br></br> <p>This project has not been updated in the past six months. To ensure continuous management and efficient use of resources, please take one of the following actions.</p> <br> <strong>Project Update</strong> :  Please proceed with the current phase or move on to the next stage.<br><strong>Project Deletion</strong> : If there are no further plans to continue, please delete the project.', 101, 'Y'),
       ('111', '844', 'Notice inactive 3rd Party', '', '<p>현재 해당 3rd Party는 최근 6개월 동안 업데이트 내역이 없습니다. 3rd Party의 지속적인 관리와 효율적인 자원 활용을 위해 아래와 같이 조치를 요청드립니다.</p><br> <strong>3rd Party 업데이트</strong> : 현재 단계를 수행하거나 혹은 다음 단계로 진행 부탁드리겠습니다.<br> <strong>3rd Party 삭제</strong> : 더 이상 진행 계획이 없을 경우, 삭제를 해주시길 바랍니다.</br></br> <p>This 3rd Party has not been updated in the past six months. To ensure continuous management and efficient use of resources, please take one of the following actions:</p><br> <strong>3rd Party Update</strong> : Please proceed with the current phase or move on to the next stage.<br>  <strong>3rd Party Deletion</strong> : If there are no further plans to continue, please delete the 3rd Party.', 844, 'Y'),
       ('111', '845', 'Notice inactive self-check', '', '<p>현재 해당 Self-Check는 최근 6개월 동안 업데이트 내역이 없습니다. Self-Check의 지속적인 관리와 효율적인 자원 활용을 위해 아래와 같이 조치를 요청드립니다.</p><br> <strong>Self-Check 업데이트</strong> : 현재 단계를 수행하거나 혹은 다음 단계로 진행 부탁드리겠습니다.<br> <strong>Self-Check 삭제</strong> : 더 이상 진행 계획이 없을 경우, 삭제를 해주시길 바랍니다.</br></br> <p>This Self-Check has not been updated in the past six months. To ensure continuous management and efficient use of resources, please take one of the following actions:</p><br> <strong>Self-Check Update</strong> : Please proceed with the current phase or move on to the next stage.<br>  <strong>Self-Check Deletion</strong> : If there are no further plans to continue, please delete the Self-Check.', 845, 'Y');

UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '42,43,44,46,52,53,54,45,55,31,56,65,34,35,36,101,812' WHERE CD_NO = '110' AND CD_DTL_NO = '31';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = '70,71,72,73,74,75,76,77,701,710,844' WHERE CD_NO = '110' AND CD_DTL_NO = '40';
UPDATE `T2_CODE_DTL` SET CD_DTL_EXP = 'SELECT  T3.OSS_ID , T1.OSS_NAME , T1.OSS_VERSION , CONCAT(T3.CVE_ID, \' -> \' , IF(T3.CVE_ID_TO = \'\' OR T3.CVE_ID_TO IS NULL, \'NONE\', T3.CVE_ID_TO)) AS CVE_ID , CONCAT(CAST(T3.CVSS_SCORE AS DECIMAL(10, 1)), \' -> \' , CAST(T3.CVSS_SCORE_TO AS DECIMAL(10, 1))) AS CVSS_SCORE , T2.VULN_SUMMARY , DATE_FORMAT(T2.MODI_DATE, \'%Y-%m-%d\') AS MODI_DATE , DATE_FORMAT(T2.PUBL_DATE, \'%Y-%m-%d\') AS PUBL_DATE FROM OSS_MASTER T1  LEFT JOIN NVD_CVE_V3 T2 ON T1.CVE_ID = T2.CVE_ID  INNER JOIN NVD_OSS_HIS T3 ON T1.OSS_ID = T3.OSS_ID AND T3.REG_DT > ADDDATE(SYSDATE(), INTERVAL - 1 DAY) AND CAST(T3.CVSS_SCORE AS DECIMAL(10, 1)) > CAST(T3.CVSS_SCORE_TO AS DECIMAL(10, 1)) AND T3.CVSS_SCORE_TO != \'10.0\' AND CAST(T3.CVSS_SCORE AS DECIMAL(10, 1)) >= 7.0 AND CAST(T3.CVSS_SCORE_TO AS DECIMAL(10, 1)) < 7.0 GROUP BY OSS_ID, CVE_ID ORDER BY OSS_NAME, OSS_VERSION, CVSS_SCORE DESC' WHERE CD_NO = '104' AND CD_DTL_NO = '212';

-- //@UNDO
-- SQL to undo the change goes here.


