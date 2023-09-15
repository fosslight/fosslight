-- // update v1.6.1
-- Migration SQL that makes the change goes here.

DELETE FROM `T2_CODE_DTL` WHERE `CD_NO` = "122";
DELETE FROM `T2_CODE` WHERE `CD_NO` = "122";



-- //@UNDO
-- SQL to undo the change goes here.


