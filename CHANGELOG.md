# Changelog

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

---

## v1.3.4 (22/04/2022)
## 🚀 Features

- Add a Rename button to the OSS Details tab. @FOSSLight-dev (#486)
- Display the version-specific popup when click the cell of the ID column in the BOM @FOSSLight-dev (#480)
- Mail > Print nickname changes when OSS version is changed. @FOSSLight-dev (#479)

## 🐛 Hotfixes

- Fix the error that the screen changes when user selects Reviewer from the User Setting, Project List, 3rd Party List. @FOSSLight-dev (#483)

## 🔧 Maintenance

- Update the comments and messages @soimkim (#484)
- Update newLogo in login, signup, menu bar @MoonDD99 (#478)

---

## v1.3.3 (15/04/2022)
## 🚀 Features

- Add statistics to menu @FOSSLight-dev (#475)

## 🐛 Hotfixes

- Fix the bug where the request button disappears when packaging rejects. @FOSSLight-dev (#476)

---

## v1.3.2 (09/04/2022)
## 🐛 Hotfixes

- Fix the bug of loading all licenses even though it is a dual license in Check License. @FOSSLight-dev  (#473)
- Fix the bug that an error occurs when downloading the SPDX file @FOSSLight-dev  (#467)
- Change the indication of unclear obligation to OSS Name : - @FOSSLight-dev  (#466)

## 🔧 Maintenance

- Add a commit message checker @soimkim (#471)

---

## v1.3.1 (01/04/2022)
## 🚀 Features

- Load user information from LDAP @FOSSLight-dev (#456)
- Adopt docker-compose Variable substitution @darjeeling (#453)

## 🐛 Hotfixes

- Fix bugs in `Check License` and `Check OSS Name`. @FOSSLight-dev (#463)

## 🔧 Maintenance

- Update OSS Type Mark > VersionDiff Service Transactional Declaration @FOSSLight-dev (#465)
- Change the sheet name of the 3rd party checklist @soimkim (#455)

---

## v1.3.0 (25/03/2022)
## 🚀 Features
- Register OSS in bulk by uploading Excel @doggai10 (#418)

## 🐛 Hotfixes

- Fix the bug where License, OSS, Project, and 3rd Party List could not be loaded. @soimkim (#446)
- Fix the bug where unregistered OSS cannot be searched by CVE-ID. @FOSSLight-dev  (#440)

## 🔧 Maintenance

- Display pointer when mouse hovers on project name @qkrdmstlr3 (#444)
- Self-check > Notice tab > Don't print unconfirmed licenses @FOSSLight-dev (#445)
- Change the character that separates multiple nicknames from `\n` to `,` @soimkim (#437)
- OSS Bulk > Separate the function to check header column @soimkim (#439)
- OSS Bulk > Separate the function to read data by column @soimkim (#438)
- Show up to 5 Vulnerability in OSS details @FOSSLight-dev (#436)
- Add Sample template to OSS Bulk Registration @soimkim (#435)
- Do not load if OSS Name or Declared License is null in OSS Bulk @FOSSLight-dev  (#434)

---

## v1.2.34 (18/03/2022)
## 🚀 Features

- Add a function to copy even the status when copying the project. @FOSSLight-dev (#429)

## 🐛 Hotfixes

- Fix a bug where Homepage could not be loaded for nickname when OSS Notice was issued. @FOSSLight-dev (#428)
- Self-Check > Mark as obligation unclear for licenses that are not included in Declared or Detected licenses. @FOSSLight-dev (#426)
- Fix the bug where OSS is renamed when copying and saving. @FOSSLight-dev (#425)
- Fix the bug that Copyright is not displayed in OSS Notice @FOSSLight-dev (#424)

## 🔧 Maintenance

- Show 'list more' in Vulnerability in OSS details @FOSSLight-dev (#430)
- Self-check > Mark Obligation unclear for deactivate. @FOSSLight-dev (#427)


---

## v1.2.33 (11/03/2022)
## Changes
## 🔧 Maintenance

- Change the condition Obligation: unclear in self-check. @FOSSLight-dev (#422)

---

## v1.2.32 (04/03/2022)
## Changes
## 🔧 Maintenance

- Add vulnerability Score to 3rd party list @FOSSLight-dev (#421)
- Change self-check unclear obligation message @FOSSLight-dev (#420)
- Add division info to project/3rd Party mail @FOSSLight-dev (#419)

---

## v1.2.31 (25/02/2022)
## Changes
## 🚀 Features

- Add Division to Project, 3rd Party. @FOSSLight-dev (#417)

## 🐛 Hotfixes

- Fix the bug where the notification doesn't pop up. @FOSSLight-dev (#416)
- Fix the Unconfirmed Version OSS registration bug @FOSSLight-dev (#412)

## 🔧 Maintenance

- Check Oss Name > npm > registered OSS Name @FOSSLight-dev (#415)
- Add default comments to the Project mails @soimkim (#413)
