<!--
Copyright (c) 2022 LG Electronics
SPDX-License-Identifier: AGPL-3.0-only
 -->
<p align='right'>
  <a href="https://github.com/fosslight/fosslight_system/blob/main/docs/RELEASE_NOTES_kor.md">[Kor]</a>
</p>

## [1.4.2](https://github.com/fosslight/fosslight/releases/tag/v1.4.2) (2022-06-24)
### Changed
* Change the data format of API > bom compare > change

## [1.4.1](https://github.com/fosslight/fosslight/releases/tag/v1.4.1) (2022-06-17)
### Changed
* Update Spring boot version 2.1.7 to 2.6.8
* Shorten the loading time when searching in OSS List

## [1.4.0](https://github.com/fosslight/fosslight/releases/tag/v1.4.0) (2022-06-03)
### Changed
* Change Java version from 8 to 11
### New
* Add check license button to Self-check

## [1.3.9](https://github.com/fosslight/fosslight/releases/tag/v1.3.9) (2022-05-27)
### Fixed
* OSS bulk registration (Stable version)
    - Fix the bugs that OSS Name, NickName stored duplicate.

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

## [1.2.34](https://github.com/fosslight/fosslight/releases/tag/v1.2.34) (2022-03-18)
### New
* When copying a project, make it possible to select the status to copy (Status: Identification, Packaging).

## [1.2.33](https://github.com/fosslight/fosslight/releases/tag/v1.2.33) (2022-03-11)
### Changed
* Self-check > If a red warning message appears according to the admin, mark obligation as unclear.

## [1.2.32](https://github.com/fosslight/fosslight/releases/tag/v1.2.32) (2022-03-04)
### New
* Add vulnerability score to 3rd party list.

## [1.2.31](https://github.com/fosslight/fosslight/releases/tag/v1.2.31) (2022-02-25)
### Changed
* Make it possible to change the division of the 3rd party and the project.

## [1.2.29](https://github.com/fosslight/fosslight/releases/tag/v1.2.29) (2022-02-11)
### New
* Add the function to save search conditions in the personal settings tab (click the login user name in the left menu).

## [1.2.24](https://github.com/fosslight/fosslight/releases/tag/v1.2.24) (2021-12-31)
### New
* Self-check > When entering a URL, link with **[FOSSLight Scanner Service](https://github.com/fosslight/fosslight_scanner_service)** to upload the OSS analysis result.
* Add Check OSS Name/License function in 3rd party.
* Add a **bulk edit** button to the top of the OSS Table so that user can edit multiple rows at once.

## [1.2.22](https://github.com/fosslight/fosslight/releases/tag/v1.2.22) (2021-12-17)
### New
* Add OSS Notice issuance function to Self-Check.
    - It is possible to issue OSS notice even for unregistered OSS. However, all licenses must be registered.
* Add Check License to Project's identification tab.
### Changed
* In Check OSS Name, make it possible to select from multiple OSS Names.

## [1.2.21](https://github.com/fosslight/fosslight/releases/tag/v1.2.21) (2021-12-10)
### Fixed
* Fix the bug where SPDX(json, yaml) is not included in OSS Notice compressed file.

## [1.2.19](https://github.com/fosslight/fosslight/releases/tag/v1.2.19) (2021-12-03)
### New
* User can copy the confirmed 3rd party to the project by clicking the "Create project for OSS Notice" button.
* Save language settings per user.

## [1.2.17](https://github.com/fosslight/fosslight/releases/tag/v1.2.17) (2021-12-19)
### New
* Add **Check License** in Self-Check.

## [1.2.15](https://github.com/fosslight/fosslight/releases/tag/v1.2.15) (2021-11-05)
### New
* Support multiple webpages of license.
* Add OSS Type to search condition in OSS List.

## [1.2.13](https://github.com/fosslight/fosslight/releases/tag/v1.2.13) (2021-10-27)
### New
* Make it possible to upload the SPDX Spread sheet to a 3rd party.

## [1.2.12](https://github.com/fosslight/fosslight/releases/tag/v1.2.12) (2021-10-22)
### New
* Make it possible to upload the SPDX Spread sheet to self-check and project.
* Make it possible to upload the csv to a 3rd party.
* Add SPDX (json, yaml) to the publishable OSS Notice format.

## [1.2.11](https://github.com/fosslight/fosslight/releases/tag/v1.2.11) (2021-10-15)
### New
* Make it possible to upload the csv to a self-check and project.
* Make the language selectable in English and Korean.

## [1.2.9](https://github.com/fosslight/fosslight/releases/tag/v1.2.9) (2021-10-01)
### Fixed
* Fix the comments history shows properly in 3rd party, project.

## [1.2.5](https://github.com/fosslight/fosslight/releases/tag/v1.2.5) (2021-09-10)
### New
* When the user clicks Check OSS Name, for OSS not in DB, suggest OSS name based on Download location.

## [1.2.4](https://github.com/fosslight/fosslight/releases/tag/v1.2.4) (2021-09-03)
### New
* Add Docker mail server in docker-compose.
* Show license information popup when clicking the restriction icon.

## [1.2.3](https://github.com/fosslight/fosslight/releases/tag/v1.2.3) (2021-08-27)
### New
* Add a synchronization feature to the OSS Details tab to allow bulk changes to OSS information for different versions.

## [1.2.2](https://github.com/fosslight/fosslight/releases/tag/v1.2.2) (2021-08-20)
### New
* Add "deactivate" to the OSS details tab.
    - A deactivated OSS is an OSS that is no longer in use. If deactivated OSS is written in the OSS Table, an error message indicates that it is a deactivated oss, and confirmation is not possible.

## [1.2.0](https://github.com/fosslight/fosslight/releases/tag/v1.2.0) (2021-07-30)
### Changed
* Change the existing license of OSS to a **Declared license** and add a **Detected license**.

## [1.0](https://github.com/fosslight/fosslight/releases/tag/v1.0) (2021-04-30)
> Release FOSSLight Hub v1.0