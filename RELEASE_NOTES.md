<!--
Copyright (c) 2022 LG Electronics
SPDX-License-Identifier: AGPL-3.0-only
 -->
## [1.3.7](https://github.com/fosslight/fosslight/releases/tag/v1.3.7) (2022-05-13)
### Changed
* Change the download location matching method when searching OSS in DB in Check OSS name/License.
    - Instead of checking whether the download location comparison method is included, it is changed to exact matching.    
      ex. A bug that matched github.com/fosslight/fosslight_util when searching for github.com/fosslight/fosslight has been fixed.

## [1.3.4](https://github.com/fosslight/fosslight/releases/tag/v1.3.4) (2022-04-22)
### New
* Add a **rename** button to the [OSS Details tab](https://fosslight.org/fosslight-guide-en/started/2_try/2_oss.html#oss-details-tab). The rename button changes OSS name and nickname for each version at once.

## [1.3.3](https://github.com/fosslight/fosslight/releases/tag/v1.3.3) (2022-04-15)
### New
* Add statistics to menu.

## [1.3.0](https://github.com/fosslight/fosslight/releases/tag/v1.3.0) (2022-03-25)
### New
* Add a **Bulk registration** button to the OSS List. Multiple OSS can be saved at once by entering OSS information in the spread sheet and uploading it.

### Fixed
* **Vulnerability List** > Fix the bug where unregistered OSS cannot be searched by CVE-ID.

### Deprecated
* In the Identification tab, the Bulk registration button loads an unconfirmed list of written OSS Tables, but this function has been replaced by uploading a spread sheet.