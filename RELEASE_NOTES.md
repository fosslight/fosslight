<!--
Copyright (c) 2022 LG Electronics
SPDX-License-Identifier: AGPL-3.0-only
 -->
<p align='right'>
  <a href="https://github.com/fosslight/fosslight_system/blob/main/docs/RELEASE_NOTES_kor.md">[Kor]</a>
</p>

# [2.0.0.pre-release](https://github.com/fosslight/fosslight/releases/tag/v2.0.0.pre-release) (2024-07-02)
### New
* UI 2.0 release 
  - Switched UI Framework to Thymeleaf
  - Enhanced with a more intuitive user interface
  - Updated UX scenarios
  - Detailed information about UI 2.0 will be available at https://fosslight.org/
* Lite release (Thanks to @hjcdg1)
  - Released Lite web for the FOSSLight system
  - Designed for personal users to perform self-checks
  - Features a simple UI and is mobile-compatible
* API v2 release (Thanks to @cobaltblu27)
  - Released API v2 for the FOSSLight system.
  - Transitioned to RESTful API architecture.
  - Improved response consistency.

# [1.6.3](https://github.com/fosslight/fosslight/releases/tag/v1.6.3) (2024-05-21)
### New
* Add 'sheetNames' parameter in oss report upload APIs
  - /api/v1/oss_report_bin
  - /api/v1/oss_report_dep
  - /api/v1/oss_report_src
  - /api/v1/oss_report_selfcheck

### Changed
* Modify uploaded packaging file size from 4GB to 5GB
* Update CheckOSSName Button UI as disabled in DEP tab because it cannot be used in DEP tab
* Notice Text Format was changed to include open source homapage link
* Change Distribution Type Names
* Some Mapper was changed:
  * Apply DEPENDENCIES and REF_OSS_NAME in mapper for component copy
  * Change DEPENDENCIES Column data type as text in DB

# [1.6.1](https://github.com/fosslight/fosslight/releases/tag/v1.6.1) (2023-11-23)
### New
* Added new API that can add a watcher in project, 3rd party, self-check. (/api/v1/prj_watcher_add, /api/v1/partner_watcher_add, /api/v1/selfcheck_watcher_add)
* Added new api /api/v1/prj_not_applicable which is possible to check “N/A” in 3rd, src, bin tab.
* The parameter whether to reset or not when uploading report in Project/Self-check is newly added. (/api/v1/oss_report_src, /api/v1/oss_report_bin, /api/v1/oss_report_selfcheck)
* A button to download the FOSSLight Report in yaml format has been added to “export” button.
* Added a new popup which displays the detected license information for each saved version when an open source with a different license for each version is saved to the system for the first time.
* The DEP tab has been added to upload the results of FOSSLight Dependency Scanner.
  - Relationship information for each package is added to the “Dependencies” column.
  - The rename function does not apply to OSS names in the DEP tab.
  - Relationship information is displayed when clicking the Dependencies icon on the BOM tab.
  - When exporting a document in SPDX format, relationship information is included.
* CycloneDX is now supported by FOSSLight Hub. You can select the form to be issued at the packaging stage and export the SBOM in project(identification).
* "admin check" is possible to modify download location, homepage and copyright information in BOM tab. 

### Changed
* All OSS are included in the BOM when exported in project and 3rd party, regardless of the notification obligation.
* The OSS report form has been updated. 
  - The "DEP" sheet has been added for the dependency analysis results
  - The automatic selection form for the Operating System field and Category field within the Model Info sheet has also been updated with the latest information.
* "Notice" shows before login screen.
* “Not the same as property” warning message is added in copyright column of OSS table.

## [1.6.0](https://github.com/fosslight/fosslight/releases/tag/v1.6.0) (2023-07-28)

### New
* Add **[the Security tab](https://fosslight.org/fosslight-guide-en/started/2_try/5_security.html)**. You can check the vulnerability information by each CVE ID of OSS lists which has vulnerabilities based on the BOM tab of the Identification step and manage it for each project.
* Available to [upgrade DB version](https://fosslight.org/fosslight-guide-en/features/3_maintenance.html#upgrading-the-db-version) using MyBatis Migrations.
* Add the reset password button.
* Add the API([/api/v1/export_selfcheck](https://fosslight.org/fosslight-guide-en/features/2_rest_api.html#rest-api-list)) that can download the result file exported from the Self-Check project
* Add a **Bulk registration** button to the License List. Multiple Licenses can be saved at once by entering License information in the spread sheet and uploading it.
* Add a **OSS Bulk Registration** button to the 3rd party. Multiple OSS can be saved at once by entering OSS information in the spread sheet and uploading it.
* Homepage information is added to the OSS table of Self-Check tab.
* New features have been added to the Identification tab, allowing users to search for 3rd parties  and projects using their ID for Search function.

### Changed
* When export the FOSSLight report, the items from 3rd party software are described as the form of "3rd-(3rd Party Name)" in the "From" column of BOM tab.
* Changes have been made to the Project, 3rd party, and Self-check tabs, where excluded OSS with the "exclude" checkbox will no longer display security vulnerabilities. If the OSS is excluded, it will be excluded when calculating max vulnerability score at the each List tab.


## [1.4.6](https://github.com/fosslight/fosslight/releases/tag/v1.4.6) (2022-07-22)
### New
* Add 'Change Division' button in Project List
### Changed
* When OSS Notice is issued, the website is basically printed instead of the internal url of the license

## [1.4.5](https://github.com/fosslight/fosslight/releases/tag/v1.4.5) (2022-07-15)
### Changed
* Update the format displayed when user clicks the yaml button to the latest.

## [1.4.3](https://github.com/fosslight/fosslight/releases/tag/v1.4.3) (2022-07-03)
### New
* Add Model information to the Basic Information tab of the Project.

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
