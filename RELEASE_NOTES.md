<!--
Copyright (c) 2022 LG Electronics
SPDX-License-Identifier: AGPL-3.0-only
 -->
<p align='right'>
  <a href="https://github.com/fosslight/fosslight_system/blob/main/docs/RELEASE_NOTES_kor.md">[Kor]</a>
</p>

## [2.3.0](https://github.com/fosslight/fosslight/releases/tag/v2.3.0) (2025-07-09)

### New
* 3rd Party
  - Added 3rd Party Information Sheet when exporting the FOSSLight Report.
* Project
  - A new field has been added to Project Information for specifying the Security Responsible Person.
    - They will also receive security-related emails from FOSSLight Hub with creator and editors.
  - OSORI DB Information Addition
    - In the Pre-Review > Open Source and License tabs, users can now see  data from the OSORI database.
  - DEP Tab Dependency Tree View
    - When analysis is performed using the FOSSLight Dependency Scanner, the relationships between each dependency can be visualized in a tree structure.
* API
  - Added API to update the Security Responsible Person information.(/api/v2/projects/{id}/security-person)
  - Added API to update the Security Mail information.(/api/v2/projects/{id}/security-mail)
* Common
  - Expanded Custom Column Feature
    - The Custom Column feature is now available in the Security tab, Project/3rd Party Identification, and Self-Check sections.
  - Tab refresh
    - If you enter a tab in any way other than clicking on the open tab at the top, a refresh pop-up will appear. 

### Changed
* Project
  - Packaging
    - Previously, up to 4 OSS Package files could be uploaded, but with this update, the number of uploadable files has been increased to 5.
  - SPDX, CycloneDX 
    - When SPDX and CycloneDX documents are generated, the output will be based on the package URL in the DEP tab. 
    - Even if the OSS Name and OSS Version are the same, each will be output separately if the package URLs are different, allowing all relationships to be displayed.
  - Support CycloneDX 1.6
* API
  - In addition to email, the user can now check their issued Token information in the User Settings menu within the FOSSLight Hub.
  - Added security mail, security person, editors and publicYn information in GEP /api/v2/projects API
  - Added bomSave parameter in /api/v2/projects/{id}/{tab_name}/reports API
  - Added modelNameExactYn parameter in GET /api/v2/projects API
  - Added reset all option in /api/v2/projects/{id}/reset API
* License, OSS
  - Added Share URL button
  - Change color of Restriction icon based on the level
가* DataBase
  -  Added column
    - PROJECT_MASTER: PACKAGE_FILE_ID5
  - Deleted columns
    - PROJECT_MASTER: PACKAGE_VUL_DOC_FILE_ID, VUL_DOC_SKIP_YN
  - Added table  
    - NVD_DATA_RUNNING_ON_WITH_TEMP, NVD_DATA_RUNNING_ON_WITH 

## [2.2.0](https://github.com/fosslight/fosslight/releases/tag/v2.2.0) (2025-02-19)

### New
* 3rd Party
  - Added 3rd Party Information
  - Added 3rd Party Identification(3rd Party tab/ BOM tab)
  - Implemented a 3rd Party BOM Compare feature

* Project
  - Added codelinaro type to Pre-review
 
### Changed
* Project
  - Enabled the ability to change the status of multiple projects simultaneously.
  - Updated BOM merge conditions:
     - If OSS Name is “-”, merge if license, homepage, and download location are the same.
  - Adjusted the Loaded list to display items in the order of most recently added.
* Open Source
  - OSS name can now only be changed through the edit button in the detailed screen.
  - Changed the initial list display to sort by modified date in descending order.
* License
  - Changed the initial list display to sort by modified date in descending order.
  - Modified the License text field to allow null values.
* Review Report
  - Updated to include OSS Important Notes information in the output.
  - Provided links to detailed screens when clicking on OSS and License names.
* DataBase
  -  Updated column names
     - PROJECT_MASTER: DESTRIBUTION_STATUS > DISTRIBUTION_STATUS
  - Deleted columns
     - PRE_PROJECT_MASTER: OSS_TYPE, OS_TYPE_ETC, DISTRIBUTION_TYPE
* Mail
  - Added a notification feature for users in the division (Code No: 200) when USE_YN is changed to N in Code Management.

* **Bug fix**
  - Fixed an issue where the banned list of all files would merge and display when multiple package files are uploaded in Packaging.
  - Resolved an issue where existing data would change when saving a copied OSS with a new name (added logic to reset oss_common_id).
  - Fixed an issue where information was not displayed correctly in the Statistics menu.
  - Resolved an issue where the Vulnerability menu could not be queried.
  - Fixed an issue where the column width would not be maintained after filtering in the Grid table.
  - Addressed an issue where no email was sent when adding a new OSS version.

## [2.2.0](https://github.com/fosslight/fosslight/releases/tag/v2.2.0) (2025-02-19)

### New
* 3rd Party
  - Added 3rd Party Information
  - Added 3rd Party Identification(3rd Party tab/ BOM tab)
  - Implemented a 3rd Party BOM Compare feature

* Project
  - Added codelinaro type to Pre-review
 
### Changed
* Project
  - Enabled the ability to change the status of multiple projects simultaneously.
  - Updated BOM merge conditions:
     - If OSS Name is “-”, merge if license, homepage, and download location are the same.
  - Adjusted the Loaded list to display items in the order of most recently added.
* Open Source
  - OSS name can now only be changed through the edit button in the detailed screen.
  - Changed the initial list display to sort by modified date in descending order.
* License
  - Changed the initial list display to sort by modified date in descending order.
  - Modified the License text field to allow null values.
  - Added a notification feature for users in the division (Code No: 200) when USE_YN is changed to N in Code Management.
* Review Report
  - Updated to include OSS Important Notes information in the output.
  - Provided links to detailed screens when clicking on OSS and License names.
* DataBase
  -  Updated column names
     - PROJECT_MASTER: DESTRIBUTION_STATUS > DISTRIBUTION_STATUS
  - Deleted columns
     - PRE_PROJECT_MASTER: OSS_TYPE, OS_TYPE_ETC, DISTRIBUTION_TYPE

* **Bug fix**
  - Fixed an issue where the banned list of all files would merge and display when multiple package files are uploaded in Packaging.
  - Resolved an issue where existing data would change when saving a copied OSS with a new name (added logic to reset oss_common_id).
  - Fixed an issue where information was not displayed correctly in the Statistics menu.
  - Resolved an issue where the Vulnerability menu could not be queried.
  - Fixed an issue where the column width would not be maintained after filtering in the Grid table.
  - Addressed an issue where no email was sent when adding a new OSS version.

## [2.1.1](https://github.com/fosslight/fosslight/releases/tag/v2.1.1) (2024-12-13)

### New
* Open Source
  - Added Important Notes section
* Project
  - Added cargo type to Pre-review
  - Added functionality to allow appending in file format in the Packaging - Notice section
* UI
  - Added icon color based on level in Restrictions
* API
  - Added Project reset API
  - Added Project delete API

### Changed
* Project
  - Changed terminology in Information: Watcher -> Editor
  - Removed unnecessary confirmation popup when saving BOM
  - Changed warning message level in Identification > BIN tab
    - OSS Name different and License cases, lowered level from Warning -> Info
  - Modified Packaging to prevent '/' from being entered in the path
  - Deleted fosslight_binary.txt area. Replaced with fosslight binary report to include tlsh and checksum values.
  - Modified to prevent BOM Compare for projects without permissions
  - Changed permission check logic to make modifications impossible in Request status
  - Handled to disallow input of single and double quotes during Distribution
* Open Source
  - Deleted items corresponding to OSS_COMMON in Sync functionality
  - Added restriction in Sync
  - Modified Sync operation to add comment with current version
  - Added CPE-related items to the List search criteria
* Mail
  - Displayed changes related to Open Source common information
  - Changed format for Open Source all version comments
  - Added Open Source purl information
  - Modified query to retrieve info table from Vulnerability Discovered email. 
    Adjusted query to ensure OSS Name is also used in the Dependency tab
* Review Report
  - Changed conditions for displaying License review
* UI
  - Automatically adds input box values when the save button is clicked in License / Open Source details.
* API
  - Changed API name according to the terminology change from Watcher to Editor
  - Modified User permission check functionality in API calls to align with UI
  - Project search API
    - Added parameter for paging
    - Changed key in return values
  - Removed limit on the number of Project creations
  - Changed to use random tokens during token generation
* **Bug fix**
  - Project > Identification
    - Fixed status bar bug
    - Corrected pre-review error
    - Resolved issue with copyright information not updating
  - Project > Packaging
    - Fixed bug in the verify process
    - Modified to prevent physical deletion of packaging files when referenced by multiple projects
    - Fixed issue where the 4th packaging file was not visible when loading previous project or could not be copied
  - Project > Security
    - Fixed issue with status indication on the Security button
    - Resolved issue where vulnerability list for open source without versions was not visible
  - 3rd Party
    - Fixed bug preventing deletion of related documents
    - Corrected bug in the 3rd party creation screen
  - License
    - Fixed bug when sending mail with only comments added in License
  - Open source
    - Modified to display restrictions of licenses linked to open source on the detail page
    - Fixed bug related to Purl creation
  - Vulnerability
    - Modified logic related to recalculation
  - DB
    - Fixed bug causing duplicate OSS COMMON IDs
    - Added missing tables to fosslight_create.sql
    - Added missing code data
      - Source code disclosure scope
      - Restriction
* Other Changes
  - Legacy code deletion: Removed unused JSP and library files
  - Changed verify script path to an absolute path including root.dir

## [2.1.0](https://github.com/fosslight/fosslight/releases/tag/v2.1.0) (2024-11-05)

### New

* **Added Security Tab Features**
  - Renamed internal tabs to Need to Resolve / Full Discovered
    - Changed from the previous Total / Fixed / Not Fixed classification to Need to Resolve / Full Discovered
    - Need to Resolve: Displays CVE IDs above the standard score.
      The standard score can be set in the Code management menu under Security Vulnerability Standard Score
    - Full Discovered: Displays all detected CVE IDs
  - Added Columns: Vulnerability Link, Security Comments
    - Vulnerability Link:
    - Security Comments: Added a Security Comments column to leave comments on the results of Vulnerability Resolution
  - Added Excel upload feature
* **Added Security Mail Enable/Disable Feature**
  - Added an option to set whether to receive Security Mail for the project
  - Can be set in Project Information
  - Reason for disabling Security Mail is mandatory
* **Added Binary List to Packaging > Source Tab**
  - Added a binary list feature to prevent binaries from being collected instead of source code during the packaging process
* **Added v2.1.0 Migration Script**
  - 20241025020001_update_v2.1.0.sql: Migration script for v2.1.0 changes
  - 20241104111630_update_v2.1.0_update_license_data.sql: Migration script to update license data used in open source

### Changed

* **Added Data to fosslight_create.sql**
  - Added License data to fosslight_create.sql
  - There was a bug where the Opensource List only showed part of the data due to missing License data
* **Increased Number of Upload Files in Packaging Tab**
  - Increased the number of packaging file uploads to 4 to support up to 20GB of upload capacity
* **Bug Fixes**
  - Fixed a bug where the file count was not correct due to a Packaging verify bug
  - Fixed a bug when exporting Statistics to an Excel file
  - Fixed a bug where saving was not possible when Project > Identification > Admin was checked due to OSS Component ID matching issues
  - Fixed an error in the default column names displayed in License / Open Source List (Obligation -> Notice/Source)
  - Fixed an error where the 3rd party name did not appear when loading 3rd party data in Project > Identification
  - Fixed bugs related to Vulnerability matching

## [2.0.2](https://github.com/fosslight/fosslight/releases/tag/v2.0.2) (2024-10-14)

### Changed
* **Changed License / Open Source Obligation Notation**
  - Separated the Obligation column: Obligation -> Notice / Source
  - Changed to a checkmark for cases where there are obligations in Notice / Source
* **Changes to Database Minor**
  - Added the previously missing Final Review Status to the T2_CODE_DTL table
  - Foreign key removed from the OSS_COMPONENTS_LICENSE table
* **Fixed errors occurring during the save process**
  - Corrected the error in setting the component ID during the license aggregation process 
    when a new entry is made in the grid data save process.
  - Fixed issues occurring during identification save due to foreign key constraints
* **Performance Improvement**
  - Improved the save process speed for Project, 3rd party, and self-check.
* **Packaging Bug Fixes and Features Added**
  - Fixed the bug that occurred during Packaging verify after Project copy.
  - Changed the Packaging process to disallow blank spaces in the path.
* **Comment Bug Fixes and Features Added**
  - Fixed the bug where the confirm process would not proceed if there was a comment message.
  - Admin can now load draft comments when confirming/rejecting.
* **Security Tab Bug Fixes and Features Added**
  - Fixed the error that occurred when no vulnerabilities were detected in the Security tab.
* Revised the 2.0.0 version release note
  - Specific content updates regarding the features and fixes included in version 2.0.0.


## [2.0.1](https://github.com/fosslight/fosslight/releases/tag/v2.0.1) (2024-09-30)

### Changed
* **Switched to using local resources for Jqgrid**
  - Due to the discontinuation of CDN support for jqgrid, it has been changed to use local resources.


## [2.0.0](https://github.com/fosslight/fosslight/releases/tag/v2.0.0) (2024-09-27)

### NEW
* **Opensource Database Schema Changes**
  - For efficient management of open source information, 
    The OSS Master has been separated into 'OSS_COMMON' and 'OSS_VERSION'.
    - **'OSS_COMMON' Table**: Added to manage the common information of OSS.
    - **'OSS_VERSION' Table**: Added to manage the version information of OSS.

* **Enhanced License and Opensource Information**
  - Restrictions have been updated based on OSORI data.
  - Added Source Code Disclosing Scope information to License.
  - Opensource now includes restriction information.
  - Comments can be distinguished between the overall version of open source and specific versions.
  - Download location information of open source is managed as common information.
  - PURL has been added to the download location.

* **Enhanced Matching of Open Source Security Vulnerabilities**
  - 'Include CPE', 'Exclude CPE', and 'OSS Version Alias' have been added to open source 
    to enhance security vulnerability matching.


### Changed
* **fosslight_create.sql Updated for Hub Version 2.0**
  - The fosslight_create.sql has been updated to version 2.0.0.

* **API V2 Updates**
  - Changed the authorization method in the API v2 Swagger UI.
  - Added 3rd party export API.
  - Modified the name for BOM export/JSON API.

* **Bugfixes**
  - Fixed issues in the UI.

* **Performance Improvement**
  - Added a column to PROJECT_MASTER to improve the speed of the Project list screen.

### Notes
Hub version 2.0.0 is a major update that includes changes to the database schema.
We provide a migration script to upgrade to Hub version 2.0.0 for existing users.  
(Filename: 20240725150921_update_v2.0.0.sql)

This migration script involves several schema changes, so it is recommended to review the overall content.  
Please note that the migration script only supports normal cases.

Errors may arise during the migration process depending on the data content. 
**Particular attention should be given to the following points**:

1. **Change of foreign key in OSS_LICENSE_DECLARED, OSS_LICENSE_DETECTED tables**
  - If there is invalid data in OSS_ID, an error may occur when changing the foreign key.
  - Therefore, it is necessary to check data integrity and perform corrections in advance if necessary.

2. **Deletion and modification of data in T2_CODE, T2_CODE_DTL**
  - The codes to be updated: 913, 230
  - Please check if you have been using these codes by modifying them.
  - This may affect related business logic or data references, so prior review is necessary.


In addition, the main changes are summarized as follows:
1. **Separation of Opensource related databases**
 - Separated into 'OSS_COMMON' and 'OSS_VERSION'.

2. **Addition of OSS_DOWNLOADLOCATION_COMMON and OSS_NICKNAME_COMMON Tables**
  - The schema has been modified to link open source common information (OSS_COMMON_ID) with the download location.
  - The schema has been modified to link open source common information (OSS_COMMON_ID) with the nickname.

3. **Addition of OSS_INCLUDE_CPE, OSS_EXCLUDE_CPE, and OSS_VERSION_ALIAS Tables**
  - Tables have been added to enhance data management for security vulnerability matching.

4. **Schema Change of LICENSE_NICKNAME Table**
  - The schema has been modified to use LICENSE_ID.

5. **Addition of DISCLOSING_SRC Column in LICENSE_MASTER Table**
  - A column has been added for DISCLOSING_SRC information.

6. **Addition of Column in PROJECT_MASTER Table**
  - A column has been added to improve the speed of the project list screen.

Please review the Migration script thoroughly, referring to the above content.
Before executing the Migration script, please make sure to back up the database,
and first run the script in a test environment to ensure there are no issues.


## [2.0.1.pre-release](https://github.com/fosslight/fosslight/releases/tag/v2.0.1.pre-release) (2024-07-22)

### Changed
* Fix wrong column name in fosslight_create.sql
* Bug fix in API V2
  - Change 3rd party search API return value type
  - Fix the bug that source code analysis result was uploaded to BIN tab
* Bug fix in email format
* Bug fix in review report
* Bug fix in search bar in Opensource menu
* Bug fix in SPDX document 


## [2.0.0.pre-release](https://github.com/fosslight/fosslight/releases/tag/v2.0.0.pre-release) (2024-07-02)

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


## [1.6.3](https://github.com/fosslight/fosslight/releases/tag/v1.6.3) (2024-05-21)

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


## [1.6.1](https://github.com/fosslight/fosslight/releases/tag/v1.6.1) (2023-11-23)

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
