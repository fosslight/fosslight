-- // update_v2.5.0 (add NVD_DATA_V3 PRODUCTVERSION index)
-- Migration SQL that makes the change goes here.
-- Add PRODUCTVERSION index used by ProjectMapper FORCE INDEX (PRODUCTVERSION)
ALTER TABLE `NVD_DATA_V3`
  ADD KEY `PRODUCTVERSION` (`PRODUCT`, `VERSION`);

-- //@UNDO
-- SQL to undo the change goes here.
ALTER TABLE `NVD_DATA_V3`
  DROP KEY `PRODUCTVERSION`;
