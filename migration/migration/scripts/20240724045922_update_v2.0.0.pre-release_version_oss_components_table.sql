-- // update pre-release version to bugfix oss components table
-- Migration SQL that makes the change goes here.
ALTER TABLE `OSS_COMPONENTS`
    ADD `TLSH` text NULL DEFAULT NULL,
    ADD `CHECK_SUM` text NULL DEFAULT NULL;


