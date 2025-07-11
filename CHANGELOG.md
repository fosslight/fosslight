# Changelog

## v2.3.0 (09/07/2025)
## What Changes
- Release version 2.3.0  by @Min-Kyungsun, @FOSSLight-dev(#1096)

## 🚀 Features
- Added Security Responsible Person and Security Mail functionality in Project Information
- Added Share Url in OSS, License
- Added OSORI DB in Pre-Review > OpenSource, License 
- Added Depedendency Tree View in DEP tab when using fosslight dependency scanner
- Added 3rd party info sheet when export
- Added Important Notes for OSS in the Review Report.
- In packaging step,  the number of uploadable files has been increased to 5.
- The user can now check their issued Token information in the User Settings menu within the FOSSLight Hub.
- When SPDX and CycloneDX documents are generated, the output will be based on the package URL in the DEP tab.
- The Custom Column feature is now available in the Security tab, Project/3rd Party Identification, and Self-Check sections.
- Support CycloneDX 1.6
- Added API to add security responsible person and to set security mail
- Add tab refresh function

## 🐛 Hotfixes
- Bug fix where saving duplicated license
- Improved vulnerability data collection, synchronization, and mailing logic
- Bug fix when export yaml file and spdx file
- Fixed various management features including project/3rd party lists, copy, delete, permissions, search conditions
- Improved multiple features related to 3rd party data loading, identification, status search, BOM comparison, Excel export
- Fixed multiple bugs in OSS save, merge, bulk registration, license validation, NVD synchronization

## 🔧 Maintenance
- Cleanup legacy file/code
- Add physical file deletion logic when deleting project/3rd party
---

## v2.2.0 (19/02/2025)
## What's Changed
* Release version 2.2.0 by @FOSSLight-dev  @Min-Kyungsun @hyeinlee00 @parkcoldroad  in https://github.com/fosslight/fosslight/pull/1093

## 🚀 New Features
* 3rd Party
  - Added 3rd Party Information
  - Added 3rd Party Identification(3rd Party tab/ BOM tab)
  - Implemented a 3rd Party BOM Compare feature

* Project
  - Added codelinaro type to Pre-review

* Use httpstatus class to print http status code @parkcoldroad (https://github.com/fosslight/fosslight/pull/863)
 
## Changes
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

## 🐛 Hotfixes
  - Fixed an issue where the banned list of all files would merge and display when multiple package files are uploaded in Packaging.
  - Resolved an issue where existing data would change when saving a copied OSS with a new name (added logic to reset oss_common_id).
  - Fixed an issue where information was not displayed correctly in the Statistics menu.
  - Resolved an issue where the Vulnerability menu could not be queried.
  - Fixed an issue where the column width would not be maintained after filtering in the Grid table.
  - Addressed an issue where no email was sent when adding a new OSS version.

**Full Changelog**: https://github.com/fosslight/fosslight/compare/v2.1.1...v2.2.0
---

## v2.1.1 (13/12/2024)
## What's Changed
* Release version 2.1.1 by @FOSSLight-dev  @Min-Kyungsun @hyeinlee00 in https://github.com/fosslight/fosslight/pull/1090

## 🚀 New Features
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

 
## Changes
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

## 🐛 Hotfixes
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


**Full Changelog**: https://github.com/fosslight/fosslight/compare/v2.1.0...v2.1.1
---

## v2.1.0 (05/11/2024)
## Changes
## 🚀 Features

- Release version 2.1.0 @FOSSLight-dev @Min-Kyungsun  (#1088)
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
  * **Added Data to fosslight_create.sql**
    - Added License data to fosslight_create.sql
    - There was a bug where the Opensource List only showed part of the data due to missing License data
  * **Increased Number of Upload Files in Packaging Tab**
    - Increased the number of packaging file uploads to 4 to support up to 20GB of upload capacity

## 🐛 Hotfixes

- Release version 2.1.0 @FOSSLight-dev @Min-Kyungsun  (#1088)
  - Fixed a bug where the file count was not correct due to a Packaging verify bug
  - Fixed a bug when exporting Statistics to an Excel file
  - Fixed a bug where saving was not possible when Project > Identification > Admin was checked due to OSS Component ID matching issues
  - Fixed an error in the default column names displayed in License / Open Source List (Obligation -> Notice/Source)
  - Fixed an error where the 3rd party name did not appear when loading 3rd party data in Project > Identification
  - Fixed bugs related to Vulnerability matching


---

## v2.0.2 (15/10/2024)
## Changes
## 🐛 Hotfixes

- Release version 2.0.2 @FOSSLight-dev @Min-Kyungsun  (#1086)
  - Fixed errors occurring during the save process
  - Performance Improvement
  - Packaging Bug Fixes and Features Added
  - Comment Bug Fixes and Features Added
  - Security Tab Bug Fixes and Features Added
  - Revised the 2.0.0 version release note

---

## v2.0.1 (30/09/2024)
## Hotfix
- 2.0.1 release. Implemented due to the discontinuation of CDN support for jqgrid. @FOSSLight-dev (#1083)

---

## v2.0.0 (27/09/2024)
## Changes
- Release Hub 2.0.0 official @FOSSLight-dev, @Min-Kyungsun, @hyeinlee00 (#1081)
- bug fix and speed improvement  @FOSSLight-dev  (#1080)
- [DEV] Changed the path of verify executable to relative path @vampard (#1073)
- Added migration script for bug fixes for higher versions of v2.0.0.pre-release @hyeinlee00 (#1060)
- API changes
  - Modify report upload api to upload empty file @hyeinlee00 (#1059)
  - Change report upload api in API V2 @hyeinlee00 (#1057)
  - Bugfix/api v2 3rd party @hyeinlee00 (#1056)

## 🚀 Features

- Improved osori db related functions @FOSSLight-dev (#1053)
  - License: Add source code disclosure scope
  - OSS:
    - Add restriction
    - Store OSS information by separating it into
      - Common: OSS_COMMON Table added
      - Version: OSS_VERSION Table added
    - Store 'Download location' info for OSS in common information
    - Add PURL for each download location 
    - Subdivide the comment into
      - Common comments 
      - Version comments
    - Add 'include_cpe', 'exclude_cpe', 'version_alias' to enhance vulnerability matching
- API changes
  - Add 3rd party export APIs @Min-Kyungsun (#1053) 
  - Add common authorization in API V2 @hyeinlee00 (#1077)
  - Add get an api to get security json data to API V2 @hyeinlee00 (#1076)
  - Feature/api v2 load project @hyeinlee00 (#1075)
  - Feature/api v2 project bom @hyeinlee00 (#1072)
 
## 🐛 Hotfixes

- Bugfix in migration script @hyeinlee00 @Min-Kyungsun (#1079) (#1081)
- [DEV] Changed Character-set and Collate for NVD_CVE_V3 to utf8mb4 from utf8 @vampard (#1071)
- Bugfix user_columns in fosslight_create.sql @hyeinlee00 (#1061)
- Update unit test for hub 2.0.0.pre-release @hyeinlee00 (#1055)

---

## v2.0.1.pre-release (22/07/2024)
## 🐛 Hotfixes

- Bug fix in v2.0.0.pre-release and Update to v2.0.1.pre-release @hyeinlee00 @FOSSLight-dev @Min-Kyungsun  (#1052)
  - Fix wrong column name in fosslight_create.sql
  - Bug fix in API V2
    - Change 3rd party search API return value type
    - Fix the bug that source code analysis result was uploaded to BIN tab
  - Bug fix in email format
  - Bug fix in review report
  - Bug fix in search bar in Opensource menu
  - Bug fix in SPDX document 


## Known Issue

### Issue1
- **Issue**: Recent FOSSLight scanner report file format, which includes TLSH and checksum data, cannot be uploaded to the BIN tab.
- **Reason**: The `TLSH` and `CHECK_SUM` columns are not included in the `OSS_COMPONENTS` table.
- **Workaround**: Manually add the following columns to the `OSS_COMPONENTS` table:
  - \`TLSH\`text DEFAULT NULL
  - \`CHECK_SUM\` text DEFAULT NULL


### Issue2
- **Issue**: User custom column in list view doesn't work 
- **Reason**: USER_COLUMNS table is not included in fosslight_create.sql
- **Note**: The table is already included in the migration script 20240401085317_update_2.0.0-beta.sql
- **Workaround**: Manually add the following  SQL script to fosslight_create.sql or create table in DB:
```
  CREATE TABLE `USER_COLUMNS` (
  `COLUMNS` longtext DEFAULT NULL,
  `LIST_TYPE` varchar(20) NOT NULL DEFAULT '',
  `CREATED_DATE` datetime NOT NULL DEFAULT current_timestamp(),
  `UPDATED_DATE` datetime NOT NULL DEFAULT current_timestamp(),
  `USER_ID` varchar(45) NOT NULL DEFAULT '',
  PRIMARY KEY (`LIST_TYPE`,`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```



### Fixed Version
 Above issues will be resolved in the official version 2.0.0. 
   - Bug fix for Issue1: [de15493](https://github.com/fosslight/fosslight/commit/de15493ed7b670e35bcefbed243ce0ce041b28a8)
   - Bug fix for Issue2: [d8060a8](https://github.com/fosslight/fosslight/commit/d8060a8e6e8be1bf37b7cfac02de3c17b3362a61)

---

## v2.0.0.pre-release (05/07/2024)
## Features
- UI 2.0 release @FOSSLight-dev (#1047)
  - Detailed information about UI 2.0 will be available at https://fosslight.org/
- API v2 release @cobaltblu27 (#1047)
- Lite web release @hjcdg1 (#1047)

## Changes
- fosslight_create.sql is changed @hyeinlee00 (#1048)

---

## v1.6.3 (21/05/2024)
## Changes
- Change Distribution Type Name @hyeinlee00 (#1038)
- Fix broken images by reverting a jib migration @jongwooo (#1034)
- Modify uploaded packaging file size from 4GB to 5GB @FOSSLight-dev (#1042) 
- Update CheckOSSName Button UI as disabled in DEP tab because it cannot be used in DEP tab @hyeinlee00 @Min-Kyungsun (#1042)
- * Add 'sheetNames' parameter in oss report upload APIs @FOSSLight-dev (#1042 )
  - /api/v1/oss_report_bin
  - /api/v1/oss_report_dep
  - /api/v1/oss_report_src
  - /api/v1/oss_report_selfcheck

## 🐛 Hotfixes

- Fix API (/api/v1/prj_bom_export) @hyeinlee00 (#1037)
- Modify text notice file format @hyeinlee00 (#1027)
- Bug fix/project/dep bulk edit btn @hyeinlee00 (#1025)
- Admin check is fixed. Even if there is a warning message (excluding unconfirmed license), confirmation will proceed if admin check is checked @FOSSLight-dev (#1042)
- Apply DEPENDENCIE and REF_OSS_NAME in mapper for component copy @FOSSLight-dev (#1042)
- Change DEPENDENCIES Column data type as text in DB @FOSSLight-dev (#1042)
- Project list was modified to check the open source vulnerability score in DEP tab @FOSSLight-dev (#1042)
- Bug fix where loading open source from 3rd party SW has an error when license name has 'and', 'or' keyword @FOSSLight-dev (#1042)
- Bug fix where a watcher with a changed division was not deleted from the project watcher list @FOSSLight-dev (#1042)
- Bug fix where notice appended contents was not visible in the confirmed project @FOSSLight-dev (#1042)
- Bug fix where list export result was different from the actual search result @FOSSLight-dev (#1042)
- Bug fix where not show vulnerability list when clicking on the security vulnerability icon in the Identification tab @FOSSLight-dev (#1042)
- Data sorting logic is fixed in OSS Table @FOSSLight-dev (#1042)
- Add highcharts.js as source code for Statistics @FOSSLight-dev (#1042)
- Bug fix where open source delete and merge is failed because of out of memory error @FOSSLight-dev (#1042)
- Bug fix where detected license information was not applied @FOSSLight-dev (#1042)
- Bug fix where SPDX export failed because 'exclude' data was included @FOSSLight-dev (#1042)
- Bug fix where CycloneDX file is not generated due to empty dependency info @FOSSLight-dev (#1042)

---

## v1.6.2 (19/12/2023)
## Changes
## 🚀 Features

- Help Message is added (Role of Creator & Reviewer, How to use FOSSLight) @FOSSLight-dev (#1020)

## 🐛 Hotfixes

- Bug fix where removing empty space when uploading report file and loading data into oss table. @FOSSLight-dev (#1022)
- Bug fix where modified comments are not saved in the security tab. @FOSSLight-dev  (#1021)
- In Self-check, null license is generated. @FOSSLight-dev (#1020)
- Modify notice template css in Self-Check. @FOSSLight-dev (#1020)
- Bug fix where the loaded list only shows up to 20 items and is no longer displayed in Project > Identification. @FOSSLight-dev (#1020)
- Bug fix where warning message disappears when ID is clicked in BOM tab. @FOSSLight-dev (#1020)
- Bug fix where remove duplicates of rows with the same oss name, version, license, and copyright in OSS Notice of self-check. @FOSSLight-dev (#1020)
- Bug fix where clicking "confirm" button, a success pop up appears even if there is a warning message in bom tab. @FOSSLight-dev (#1020)
- Bug fix where saving the oss table loaded in the 3rd party tab. @FOSSLight-dev (#1020)
- Bug fix where a warning message is added to the license in case of a dual license when clicking Bulk Edit button. @FOSSLight-dev (#1020)
- Bug fix where warning messages are displayed in duplicate in the oss table. @FOSSLight-dev (#1020)
- Fix to prevent Korean language from being broken in PDF @MyunghyunNero (#1008)
- Change CheckOSSName Button in DEP tab @hyeinlee00 (#1017)

---

## v1.6.1 (23/11/2023)
## 🚀 Features
-  Added "Change" Button in Project List, 3rd party list @FOSSLight-dev (#1013)
-  In BOM tab, append all copyright when oss name and oss version is same. @FOSSLight-dev (#1011)
- CycloneDX is now supported by FOSSLight Hub. You can select the form to be issued at the packaging stage and export the SBOM in project(identification). @FOSSLight-dev (#1009)
- In OSS detail view, added OSS type row and info icon in OSS name row. When info icon is clicked, OSS information popup by version is displayed. @FOSSLight-dev (#1009)
- In License detail view, added info icon in License name row. When info icon is clicked, help message is displayed. @FOSSLight-dev (#1009)
- It is possible to delete several OSS at the same time for only admin. @FOSSLight-dev (#1009)
- Added new api /api/v1/prj_not_applicable which is possible to check “N/A” in 3rd, src, bin tab. @FOSSLight-dev (#1009)
- Add “Not the same as property” warning message in copyright column @FOSSLight-dev (#1009)
- Project > Identification > The “DEP” tab has been added to upload the results of FOSSLight Dependency Scanner. @FOSSLight-dev (#987)
- When selecting "admin check", it is possible to modify download location, homepage and copyright information. @FOSSLight-dev (#987)
- The OSS report form has been updated to version 3.2. Please note that the "DEP" sheet has been added for the dependency analysis results, and the automatic selection form for the Operating System field and Category field within the Model Info sheet has also been updated with the latest information. @FOSSLight-dev (#987)
- Add vulnerability search to OSS List @jiwon83 (#983)
- Fix to show multiple notifications @parkmuhyeun (#937)
- Combine comment into one when packaging confirm @MyunghyunNero (#984)
- Added new API that can add a watcher in project, 3rd party, self-check. (/api/v1/prj_watcher_add, /api/v1/partner_watcher_add, /api/v1/selfcheck_watcher_add) @FOSSLight-dev (#986)
- All OSS are included in the BOM when exported, regardless of the notification obligation. @FOSSLight-dev (#986)
- A button to download the FOSSLight Report in yaml format has been added to “export” button. @FOSSLight-dev (#986)
- The parameter whether to reset or not when uploading report in Project/Self-check is newly added. (/api/v1/oss_report_src, /api/v1/oss_report_bin, /api/v1/oss_report_selfcheck) @FOSSLight-dev (#986)
- Add new popup to ask enter version of oss which has N/A version to ensure accurate vulnerability when clicking “request”. @FOSSLight-dev (#986)
- When an open source with a different license for each version is saved to the system for the first time, a pop-up displays the detected license information for each saved version. @FOSSLight-dev (#986)
- Sending email when reviewer is changed as other person. @FOSSLight-dev (#986)
- Modify to do not have to press the search button @dener8 (#933)
- Add stackoverflow pattern for check oss name @dener8 (#974)
- OSS > add > new icon for newly added nicknames @Lightieey (#931)
- Added watcher search box in Project List, 3rd Party List @Youngseo-Jeon0313 (#934)
- Block unsupported file extensions in the packaging tab @MyunghyunNero (#917)
- Add Attach Pdf to Email @MyunghyunNero (#760)

## 🐛 Hotfixes
- Bug fix in Identification (CheckOSSName Button in DEP tab, first tab) @hyeinlee00 (#1010)
- In self-check, OSS Notice cannot be generated when unconfirmed license is included in oss table. @FOSSLight-dev (#1009)
- Bug fix where verify logic in packaging tab.@FOSSLight-dev (#1009)
- Bug fix when using “admin check”, save checked oss list regardless of active page. @FOSSLight-dev (#1009)
- Bug fix where displaying “fixed” in security column of project list even if remaining not fixed CVE ID. @FOSSLight-dev (#1009)
- Modify pdf with error @MyunghyunNero (#1003)
- Fix bugs related to Vulnerability when searching OSS List @Youngseo-Jeon0313 (#999)
- Bug fix where displaying same SPDXElementID in spdx document.  @FOSSLight-dev (#987)
- Fix the logic to find user information by using email. @FOSSLight-dev (#986)
- Change the condition of displaying the list in “check license”. @FOSSLight-dev (#986)
-  Do not send email when watcher is added in self check. @FOSSLight-dev (#986)
- In Bin tab, If two or more same binary names are created and any one of them has an excluded item, it is excluded from the warning message ("The following binaries are written to the OSS report as excluded, but they are in the binary.txt. Make sure it is not included in the final firmware." ). @FOSSLight-dev (#986)
- Display “delete” button of model information even if the project status is complete. @FOSSLight-dev (#986)
- Bug fix where the license disappeared after executing “bulk edit”. @FOSSLight-dev (#986)
- Bug fix were sending recalculated, discovered vulnerability email. @FOSSLight-dev (#986)
- When uploading FOSSLight report, the copyright value is entered as value entered by user. @FOSSLight-dev (#986)
- Before saving download location, homepage of oss table, remove html tag. @FOSSLight-dev (#986)
- When clicking “Check OSS Name”, ignore values after the “?” in the link. @FOSSLight-dev (#986)
- When project is copied, the comment of oss table is also copied. @FOSSLight-dev (#986)
- Bug fix when changing the settings of User Setting > Default Search Conditions, the setting is applied well. @FOSSLight-dev (#986)
- Bug fix when registering the comment via api, set user information properly. @FOSSLight-dev (#986)
- Bug fix where user’s token is not working. @FOSSLight-dev (#986)
- Even when the compressed file name is included in the path, the number of files can be counted. @FOSSLight-dev (#986)
- Bug fix when saving self-check, division information is changed as null. @FOSSLight-dev (#986)
- Bug fix where register license by using “Bulk registration” in License list. @FOSSLight-dev (#986)
- Bug fix when download the spdx document, the license is printed as spdx format. @FOSSLight-dev (#986)
- Fix to support searching by either CVE-ID or OSS Name in /api/v1/vuln… @KyuheonKim (#866)

## 🔧 Maintenance
- Use early return pattern to avoid nested conditions @parkcoldroad (#920)
- Seperate Build and Deploy-demo from publish workflow @hseungho (#963)
- Fix the RUN script format of Dockerfile @hseungho (#971)
- “There is no data to load” error message is added in report upload api when there is no row to load in FOSSLight report. @FOSSLight-dev (#986)
- “[tab name] sheet name cannot be found” error message is added in report upload api when there is no sheet to load in FOSSLight report. @FOSSLight-dev (#986)
- Display “Notice” screen before login screen. @FOSSLight-dev (#986)
- Add “comment” field in Bulk Edit of Self-check. @FOSSLight-dev (#986)
- Fetch base-check-commit-message.yml from .github @Gseungmin (#969)
- Remove unused Slack notification step @che-so (#930)
- Fix a typo of CoMail's getSndSeq comment @hseungho (#898)
- Fix a typo at Url @brorica (#905)
- Remove Unused Parameter 'binaryName' in Function @brorica (#921)
- Change string concatenation method @jaehee329 (#859)

---

## v1.6.0 (28/07/2023)
## 🚀 Features

- If project is loaded through "Load" feature in the SRC/BIN tab, the loaded project ID will be displayed in the comment field. @FOSSLight-dev 
- The comment field has been added to the OSS table of 3rd party @FOSSLight-dev 
- Display license with "Dual license: Select a license" warning message in Check License @FOSSLight-dev (#865)
- Possible to download spdx report regardless of obligation @FOSSLight-dev (#862)
- Possible to search by 3rd party id ,project id in Identification. @FOSSLight-dev (#862)
- Add "new" security tab where possible to check the vulnerability information. @FOSSLight-dev  (#849)
- Add the API(/api/v1/export_selfcheck) that can download the result file exported from the Self-Check project @FOSSLight-dev (#845)
- Add 3rd party description search @cookienc (#842)
- Add files for DB migration @soimkim (#832)
- Add a file to use mybatis migrations @soimkim (#831)
- In 3rd party, "OSS Bulk Registration" is possible. @Min-Kyungsun  (#820)
- Add reset password feature @han-gyeong (#813)
- Uploading/removing files works independently of clicking the "save" button in BIN(Android) @Min-Kyungsun (#804)
- Binary Auto Identification @FOSSLight-dev (#797)
- Fix bug with previously generated OSS notices @FOSSLight-dev (#795)
- Self Check > Check validation when add/edit self-check project @Min-Kyungsun (#792)
- Possible to check the recommended OSS Name even if the download location is not valid @Min-Kyungsun (#790)
- When project identification is confirmed, the information of bom tab is updated based on DB. @Min-Kyungsun (#789)
- License Bulk Registration @Gseungmin (#784)


## 🐛 Hotfixes

- When calculating max score of vulnerability, exclude oss which is checked as excluded. @FOSSLight-dev (#862)
- Fix the bug where html tag is displayed in self-check. @FOSSLight-dev (#853)
- Bug fix when parentheses is included in packaging path, packaging files are not verified properly. @FOSSLight-dev (#850)
- ddl script typo fix @d-h-k (#848)
- Bug fix where generating oss name of android platform in "Check OSS Name" @Min-Kyungsun (#843)
- Fix nvd rest api parameter setting @FOSSLight-dev (#841)
- Check oss name - the oss matched with name and version is existed and download location has "Not the same as property" warning message @Min-Kyungsun (#840)
- Bug fix where uploading csv file via self-check oss-report-selfcheck API. @FOSSLight-dev (#839)
- Improve the speed of loading oss table. @FOSSLight-dev  (#838)
- Fix NVD Sync Error @FOSSLight-dev  (#836)
- Fix project list cvss score maximum value select query  bug fix @FOSSLight-dev (#835)
- Change method from NVD Data feeds to REST API  bug fix @FOSSLight-dev (#834)
- Fix release date data check function when model list upload @FOSSLight-dev (#830)
- Fix bug where not all matched CVEs were retrieved. @FOSSLight-dev (#829)
- Fix bug where Vulnerability is incorrectly displayed for OSS Name @FOSSLight-dev (#827)
- Fix bugs without vulnerabilities when OSS version blank @FOSSLight-dev (#826)
- Fix function from ldap simple binding to ldap secure @FOSSLight-dev (#823)
- Fix vulnerability data matching condition @FOSSLight-dev (#822)
- Fix Vulnerability issue @FOSSLight-dev (#819)
- Fix the ability to load vulnerability data in the identification tab @FOSSLight-dev (#818)
- Fix vulnerability check function in identification tab @FOSSLight-dev (#814)
- Add checking validation of binary db data @FOSSLight-dev (#812)
- Fix the bug where sending vulnerability mail when " " is included in OSS Name @FOSSLight-dev (#811)
- Fix bug where OSS Name/Nickname does not match NVD Data when there is a space @FOSSLight-dev (#809)
- Fix the bug that the Vulnerability popup is not searched if there is a space in the OSS Name @soimkim (#806)
- After the Identification stage is confirmed, copyright text column is updated based on db @Min-Kyungsun (#805) 
- Fix the bug - fail to load autoanalysis result @Min-Kyungsun (#803)
- Bug fix - Fail to remove the compressed notice file in BIN(Android) @Min-Kyungsun (#798)
- Check License > display "Changed" text when license is changed successfully @Min-Kyungsun (#791)
- Fix if statements without curly braces @jongwooo (#788)
- Fix a bug where a space is added at the beginning when there are more than two detected licenses when loading OSS @FOSSLight-dev (#785)
- Fix the error that occurs when bom compares the same Project ID @FOSSLight-dev (#782)

## 🔧 Maintenance

- Fix typo @syleeeee (#837),  @parkmuhyeun (#870), @cookienc (#892), @hseungho (#896), @jiwon83 (#895), @parkcoldroad (#899), @che-so (#902)
- Register gitignore for auto-generated files @moto3z (#889)
- OSS > Delete WITH from the detected license field. (WITH is legacy.) @moto3z  (#867)
- Remove unused import statements and unnecessary code @D0ri123 (#869), @parkmuhyeun (#854)
- Delete duplicate code in LicenseController @Gseungmin (#887)
- Improve the speed of loading oss table. @FOSSLight-dev  (#838, #853)
- Add the file name and code values for the LDAP settings @FOSSLight-dev  (#828)
- When saving basic information, if the required fields are omitted, automatically focus on them @Min-Kyungsun (#815)
- Fix the vulnerability issue in NOTICE.html @Min-Kyungsun (#807)
- Remove all files and folder related to compressed notice file. @Min-Kyungsun (#800)
- Add space after if and for, while statements @jongwooo (#794)
- Print partner name in excel BOM @soimkim (#787, #786)
---

## v1.5.0 (23/12/2022)
## 🚀 Features

- OSS Bulk Registration > Fix type of some fields to textarea @FRESH-TUNA (#771)
- Display "Required oss name" warning message when oss name is "-" and license has source obligation. @FOSSLight-dev (#763)
- Display "Required oss name" warning message  @Min-Kyungsun (#761)
- Upload review report html template @swa07016 (#751)
- Create and download review report @70825 (#748)
- Convert html to pdf for review report @70825 (#736)
- License bulk registration @Gseungmin (#667)
- Add Android format(android.googlesource.com/platform/) to change oss name. @Min-Kyungsun (#749)
- Check OSS Name > Change the redirect url automatically. @FOSSLight-dev (#740)
- When "Change OSS Name" button is clicked, internal logic has been modified and added. @Min-Kyungsun (#727)
- OSS Bulk Registration > Show warning messages @FRESH-TUNA (#715)
- Automatically close stalled issues and pull requests @jongwooo (#725)
- Use setup-qemu-action to build multi-arch images @jongwooo (#723)
- Highlight Oss data failed to save in red @yujung7768903 (#721)

## 🐛 Hotfixes

- Fix vulnerability search condition and when save vendor data @FOSSLight-dev(#779)
- Fix bug with create project for OSS Notice @FOSSLight-dev (#778)
- Fix bug where vulnerability cannot be found if there is a space in the OSS Name. @FOSSLight-dev (#776)
- LDAP > Fix a bug that caused an error when the password was incorrect. @FOSSLight-dev (#772)
- Fix license name saved redundantly @FOSSLight-dev (#767)
- Fix bug where license is not merged in BOM if it is blank @FOSSLight-dev (#764)
- Display "Required oss name" warning message when oss name is "-" and license has source obligation. @FOSSLight-dev (#763)
- In oss detail popoup, it is available to copy oss  @Min-Kyungsun (#757)
- Display the latest oss information which has nickname or oss name of auto analysis result  @Min-Kyungsun (#755)
- Add check email when login new user @FOSSLight-dev (#747)
- Fix issuance of notice for permissive license @FOSSLight-dev (#745)
- Project List > Fix a bug that caused a permission error when clicking on an empty space in Identification. @FOSSLight-dev (#744)
- Project List > Fix bugs related to View my project only @FOSSLight-dev (#741)
- Fix link in registration mail OSS Name (version) @Min-Kyungsun (#731)
- Modify oss list table in 3rd party mail @Min-Kyungsun (#730)
- Display all referenceDiv like bom tab in "From column" of exported excel file @FOSSLight-dev (#729)
- Fix a bug where the Homepage/Download Location is saved with a warning message included. @FOSSLight-dev (#728)
- Fix the bug where it is not merged when exporting in BOM tab. @FOSSLight-dev (#726)
- Fix grid data save function @FOSSLight-dev (#724)

## 🔧 Maintenance

- Update 3rd party licenses @soimkim (#780)
- Vulnerability > Change OSS Name matching method (improved accuracy) @FOSSLight-dev (#775)
- Change the LDAP login failure log level to debug @Min-Kyungsun (#773)
- chore: Configure Dependabot for GitHub Actions @jongwooo (#766)
- LDAP > Modify the updated date to be displayed in user information when sending mail. @FOSSLight-dev (#765)
- Display "Required oss name" warning message when oss name is "-" and license has source obligation. @FOSSLight-dev (#763)
- Exclude deactivated OSS from analysis results @FOSSLight-dev (#758)
- When sync oss, it is also possible to update only comment. @Min-Kyungsun (#756)
- Display the latest oss information which has nickname or oss name of auto analysis result  @Min-Kyungsun (#755)
- When export the specific oss name of vulnerablity, append oss name to exported file name  @Min-Kyungsun (#754)
- Possible to select other version of oss even if the information is same in oss sync @Min-Kyungsun (#753)
- Add defensive code in check oss name @Min-Kyungsun (#752)
- Add code for Vulnerability Notification Score to code management (Code No. 750) @FOSSLight-dev (#746)
- (Experimental) Add file extraction tag in Notice of Android @soimkim (#742)
- When "Change OSS Name" button is clicked, internal logic has been modified and added. @Min-Kyungsun (#727)
- Show projects regardless of permissions in the project list. @FOSSLight-dev (#739)
- Separate messages of issue and pull request @jongwooo (#735)
- Add the file name for apex in NOTICE @soimkim (#737)
- Remove issue auto-close @soimkim (#734)
- Fix link in registration mail OSS Name (version) @Min-Kyungsun (#731)
- Add latest tag to docker image @jongwooo (#719)

---

## v1.4.10 (07/10/2022)
## 🚀 Features

- Github actions
    - Add Slack notification bot @jongwooo (#713)
    - Deploy image to Docker Hub @jongwooo (#675)
- OSS Bulk Registration
    - Show detail failure messages in OSS bulk feature. @FRESH-TUNA (#669)
    - Move OSS bulk button to BOM tab @FRESH-TUNA (#688)
    - Automatically load unconfirmed version/oss into bulk registration @MoonDD99 (#652)
    - Add editable and selectable to ossBulkRegRows @Gseungmin (#656)
- Mail
    - In 3rd party confirm mail, add "need to disclose" column @Min-Kyungsun (#698)
    - When sending 3rd party confirmed mail, attach the list of disclose oss. @Min-Kyungsun (#647)
- Report
    - Export button depends on project identification status @70825 (#694)
    - Support SPDX download in bom tab @70825 (#650)
    - Add a Yaml button to 3rd Party, Project, Self-check @FOSSLight-dev (#635)
- Self-check >  Add SPDX as well as FOSSLight Report to the export button download @70825 (#664)
- Add "search icon" in filter cell of table @Min-Kyungsun (#668)
- Add project bom tab export json to the API @70825 (#636)
- Change [PRJ-ID] or [3rd-ID] text to linkable text @Min-Kyungsun (#643)
- Add 'Share URL' Button in Project, 3rd Party @FOSSLight-dev  (#642)

## 🐛 Hotfixes

- Fix the compilation error - cannot find symbol @soimkim (#714)
- If permissive OSS list is included, display the information sentence in notice html. @Min-Kyungsun (#712)
- OSS Bulk Registration > Add triming logic to list attributes. @FRESH-TUNA (#708)
- OSS Bulk Registration > Fix a bug where copyright is not added @FRESH-TUNA (#707)
- Fix download location check function @FOSSLight-dev  (#710)
- Fix the broken identification ui bug @soimkim (#706)
- In comment mail, activate project/3rd party link  @Min-Kyungsun (#704)
- Remove the limit on the number of rows for the bulk edit button @FOSSLight-dev (#702)
- Fix a bug that occurs when saving to 3rd party tab in project @FOSSLight-dev (#697)
- Fix the bug that the row in which only Homepage or Copyright is entered is not loaded when uploading a report. @FOSSLight-dev (#696)
- Fix the bug where the license written as nickname does not change to license name when save is clicked @FOSSLight-dev (#693)
- When division information is changed, do not save automatically. @Min-Kyungsun (#692)
- Fix a bug where only 1 Vulnerability list is shown when there is no version in OSS detail page @FOSSLight-dev (#689)
- Fix the bug where the license flag is not saved when saving OSS @FOSSLight-dev (#686)
- Fix bug where CVE-ID is displayed as duplicate in OSS details tab @FOSSLight-dev (#681)
- Display division information even if Use YN flag of division is N @Min-Kyungsun (#679)
- Fix bug where watcher row is duplicated in project basic information @Min-Kyungsun (#651)
- When sending 3rd party confirmed mail, attach the list of disclose oss. @FOSSLight-dev (#647)
- Fix the bug where password cannot be set when signing up for non-LDAP @FOSSLight-dev (#645)
- Fix the bug where the link changes from Check License to https://https:// @FOSSLight-dev (#641)
- Fix vulnerability list > Undetected nicknames are displayed @FOSSLight-dev (#637)
- Fix the bug where SPDX download fails @FOSSLight-dev (#634)
- Fix SPDX download error in Self-check > Notice @FOSSLight-dev (#633)
- Fix bugs that are invisible even as Watchers in the 3rd party list @FOSSLight-dev (#632)

## 🔧 Maintenance

- Change the link of OSS Name (version) in OSS registration/modification mail. @FOSSLight-dev (#716)
- Fix typo email template  @70825 (#709)
- Add license text files to docker image @jongwooo (#700)
- Improve identification and 3rd party OSS loading speed @FOSSLight-dev (#701)
- Add license text files @soimkim (#699)
- Project > 3rd Party tab > Leave a comment on the history of changing nickname. @FOSSLight-dev (#691)
- Send OSS save failed return code differently @FOSSLight-dev (#690)
- When click "+" button, insert new row at the top of the table @Min-Kyungsun (#684)
- Fix the typo in OSS Detail tab. (Atrribution -> Attribution) @70825 (#678)
- Add oss list in identification confirm mail @Min-Kyungsun (#676)
- Change the label text to result.txt and binary.txt to fosslight_binary.txt. @soimkim (#674)
- Change search icon file @Min-Kyungsun (#673)
- When 3rd party confirm, remove default comment in comment history @Min-Kyungsun (#666)
- Send email with oss list and disclose oss list @Min-Kyungsun (#663)
- Change buttons to be top of the table in Binary DB, System > User management @Min-Kyungsun (#662)
- Fix oss sync function for regist comment @Min-Kyungsun (#661)
- Change downloaded file name when clicking export button @Min-Kyungsun (#660)
- Remove buttons at the bottom of the table. @Min-Kyungsun (#659)
- Add the NPM link pattern @soimkim (#658)
- Check user permission when changing divisions. @FOSSLight-dev (#657)
- Increase the height of oss version list table in  oss version list popup @Min-Kyungsun (#655)
- Remove duplicated oss list in 3rd party mail @Min-Kyungsun (#654)
- Add logic to validate for download location from input data @Min-Kyungsun (#653)
- When sending 3rd party confirmed mail, attach the list of disclose oss. @FOSSLight-dev (#647)
- Change regular expression of project link @Min-Kyungsun (#646)
- Send an email and leave a comment if additional information is modified in Project > Basic information @FOSSLight-dev (#640)
- Change the sheet name that is checked when uploading a file in Project Identification @FOSSLight-dev (#639)

---

## v1.4.9 (12/08/2022)
## 🚀 Features

- Add export feature to vulnerability popup @FRESH-TUNA (#608)
- Add CVE_ID input field in /api/v1/vulnerability_data @MiniVee (#620)

## 🐛 Hotfixes

- Fix the bug of infinite loading when searching for CVE ID with - @FOSSLight-dev (#626)

## 🔧 Maintenance
- In the basic information tab of the project, add an edit/save button in the additional information field. @Min-Kyungsun (#628)
- When adding or copying project, remove view/edit button to additional information. @Min-Kyungsun (#629)

---

## v1.4.8 (05/08/2022)
## 🚀 Features

- Skip registration step when using LDAP @Min-Kyungsun (#576)

## 🐛 Hotfixes

- Fix return value to Obligation Type @myway00 (#607)
- When renaming to another OSS while deleting OSS, check License @FOSSLight-dev  (#624)

## 🔧 Maintenance

- Change the CVSS Score for sending Vulnerability alerts from 9.0 to 8.0 @FOSSLight-dev (#621)
- Show the user's name, when Mouse hover @hataerin (#616)
- Increase the length of the project's name field @MiniVee (#613)
- Caching Dependencies to speed up workflows @jongwooo (#610)

---

## v1.4.7 (29/07/2022)
## 🐛 Hotfixes

- Remove version diff notification popup when deleting OSS @FOSSLight-dev  (#609)
- Fix the bug where search terms are entered during change division @FOSSLight-dev (#603)
- Fix bugs that do not run with docker-compose on Windows @soimkim (#598)
- Change the multilingual setting to ignore the country @yujung7768903 (#592)

## 🔧 Maintenance

- Add priority to the status tooltip of the Project List @JIY0UNG (#602)
- Increase the length of the name field  @hataerin (#604)
- Move to a cell what I double-click @Gseungmin (#580)
- Add guide comment in Configuration @MoonDD99 (#600)
- Change nickname input position of OSS List, License List when Add button is clicked @70825 (#589)
- Make it search even if space is included at the beginning and end of the word in the OSS List @MiniVee (#591)
- Fix comment in Self-check to remove newline @acisliver (#583)
- XSS prevention with jstl @swa07016 (#588)
- Show "Double click" when mouse hovers on project name @MyunghyunNero (#572)
- Hide user email as per option @MiniVee (#593)

---

## v1.4.6 (22/07/2022)
## Changes
## 🚀 Features

- Add the function to change division @FOSSLight-dev (#586)

## 🔧 Maintenance

- Reload tab to you're working on after checking OSS Name/License @FRESH-TUNA (#568)
- Add a flag to use the license's internal url @soimkim (#587)
---

## v1.4.5 (15/07/2022)
## Changes
## 🚀 Features

- Change yaml file format when oss list is exported as yaml @Min-Kyungsun (#566)

## 🐛 Hotfixes

- Show the project list in Opened Jobs of Dashboard when user is not admin @Min-Kyungsun (#565)

## 🔧 Maintenance

- Update BAT > show GUI report icon @FOSSLight-dev (#573)
- Update Dockerfile for Mac M1 @soimkim (#571)
- Update CKEditor text paste function @FOSSLight-dev  (#569)
- Add description when returning Vulnerability API @soimkim (#567)
- Hide the dropped project list in Opened Jobs of Dashboard @Min-Kyungsun (#564)

---

## v1.4.4 (07/07/2022)
## Changes
## 🐛 Hotfixes

- Fix the bug where sorting by column is not possible in the 3rd party list @FOSSLight-dev (#562)
- Fix the bug where uploaded files are not visible @FOSSLight-dev (#560)
- Fix bug where CVE-ID is returned as duplicate in API > Vulnerability. @FOSSLight-dev (#559)

## 🔧 Maintenance

- Hide user email as per option @soimkim (#561)
- Modify Model Information table in project's basic information tab @Min-Kyungsun (#558)

---

## v1.4.3 (03/07/2022)
## 🚀 Features

- Add default comments (3rd party confirmed mail) @Min-Kyungsun (#556)
- Open oss details window in oss version information @Min-Kyungsun (#555)
- Add model information to project's basic Information tab @Min-Kyungsun (#552)

## 🐛 Hotfixes

- Copy whether notice has been modified or not @FOSSLight-dev  (#554)

## 🔧 Maintenance

- Packaging > Notice tab > edit html > Show message if checked. @FOSSLight-dev (#557)
- Change the message when selecting URL @70825 (#546)

---

## v1.4.2 (24/06/2022)
## Changes
## 🔧 Maintenance

- Update download location and homepage > same link check condition @FOSSLight-dev (#551)
- Change the data format of API > bom compare > change @FOSSLight-dev (#550)
- Update UI (3rd party/Basic Information) @Min-Kyungsun (#550)


---

## v1.4.1 (17/06/2022)
## 🐛 Hotfixes

- Update Spring boot version 2.1.7 to 2.6.8 @FOSSLight-dev (#544)
- Fix a bug where a popup does not appear when clicking the R icon in the License List @FOSSLight-dev (#542)

## 🔧 Maintenance

- Shorten the loading time when searching in OSS List. @FOSSLight-dev (#547)
- Update Spring boot version 2.1.7 to 2.6.8 @FOSSLight-dev (#544)
- Change the column name to CVSS Score to Vulnerability in Self Check Tab @MyunghyunNero (#540)
- Change 'oss List' to 'OSS List'. @Gseungmin (#541)
---

## v1.4.0 (02/06/2022)
## Changes
## 🔧 Maintenance

- Update version to 1.4.0 @soimkim (#538)
- Add check license button to Self-check @FOSSLight-dev (#533)
- Update link of version @soimkim (#532)
- Upgrade Java version to 11 @FOSSLight-dev (#531)
- API > Add parameters to model_update @soimkim (#530)
- Add the release note Korean version. @soimkim (#529)

---

## v1.3.9 (27/05/2022)
## 🐛 Hotfixes

- Fix the bug that the file is deleted when copying @FOSSLight-dev (#528)
- Fix OSS bulk registration bugs @soimkim (#525)
- Fix test errors during testing packaging @soimkim (#521)

## 🔧 Maintenance

- Use OSS storage logic in OSS Bulk Registration. @soimkim (#523)
- If a Short Identifier exists in the license, display it as Short ID. @FOSSLight-dev  (#524)
- Change the UI for self-check upload @suhwan-cheon (#520)

---

## v1.3.8 (20/05/2022)
## Changes
## 🚀 Features

- When copying a project, add a pop-up to choose which level to copy to. @FOSSLight-dev  (#513)

## 🐛 Hotfixes

- Fix the bug where the model cannot be found @soimkim (#517)
- Fix the bug where the distribution site is changed when the model is updated with the api @soimkim (#516)

## 🔧 Maintenance

- Add validation error message @FOSSLight-dev (#518)
- When issuing a Notice in Self-check, load and include the OSS link instead of the Homepage by loading the Download location @FOSSLight-dev (#515)
- API > Update models regardless of status @soimkim (#514)
- Add a release note @soimkim (#510)

---

## v1.3.7 (13/05/2022)
## 🚀 Features

- Add model updates to the API @soimkim (#507)

## 🔧 Maintenance

- Check OSS Name/License > refine the download location. @FOSSLight-dev (#508)
- Check OSS Name, License > Change the way you find links @FOSSLight-dev (#506)
- Add a default comment to the Packaging confirm mail @soimkim (#505)

---

## v1.3.6 (06/05/2022)
## 🐛 Hotfixes

- Fix the bug if the download location is null @soimkim (#500)
- Fix the page not showing in OSS Bulk @soimkim (#498)
- After deleting the uploaded file in Self-check, change the error that occurs when saving. @FOSSLight-dev  (#496)
- Fix bug where suffix is added twice in version @soimkim (#494)

## 🔧 Maintenance

- Change Status Message for Row Registration Failed in OSS Bulk @soimkim (#503)
- When copying OSS and Project, try setting the name and version. @soimkim (#492)
- Fix the bug where the warning message is not displayed for the deactivated OSS @FOSSLight-dev (#490)

---

## v1.3.5 (29/04/2022)
## Changes
## 🐛 Hotfixes

- Fix a bug on clicking the Rename button @FOSSLight-dev (#489)
- Update OSS Table > Validation Downloadlocation @FOSSLight-dev (#488)
