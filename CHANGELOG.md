# Changelog

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

---

## v1.2.30 (18/02/2022)
## Changes
## 🐛 Hotfixes

- Fix a bug that is not searched by restriction in the License List @FOSSLight-dev (#409)

## 🔧 Maintenance

- Fix the bug where the License List is not filtered by restriction @FOSSLight-dev (#410)
- Separate the handling of npm's group name from Check OSS Name @FOSSLight-dev (#408)
- Modify the written offer in notice template. @dd-jy (#407)

---

## v1.2.29 (11/02/2022)
## 🚀 Features

- Add personal list search condition to setting @FOSSLight-dev (#405)

## 🐛 Hotfixes

- Fix the bug where the license was not displayed on the OSS details page @FOSSLight-dev (#398)
- Fix bug where OSS Rename popup appears for new OSS @FOSSLight-dev (#397)

## 🔧 Maintenance

- Make the Favicon background transparent @soimkim (#406)
- Match OSS not deactivated in Check License @soimkim (#402)
- Change the format of OSS mail @soimkim (#401)
- Add a message stating that a Notice file is required @FOSSLight-dev (#400)
- Update FOSSLight icon @soimkim (#399)

---

## v1.2.28 (28/01/2022)
## 🐛 Hotfixes

- Fix the bug where OSS Type is not Dual and not version diff is displayed incorrectly @FOSSLight-dev (#393)

## 🔧 Maintenance

- Add the function to change the OSS Name of OSS with different versions @FOSSLight-dev (#395)
- Reorder user comments before email default content @soimkim (#394)

---

## v1.2.27 (21/01/2022)
## Changes
## 🐛 Hotfixes

- Fix bugs related to Auto ID and BOM Obligation. @FOSSLight-dev (#391)
- Fix bugs in BOM Compare and Auto ID @FOSSLight-dev (#389)
- Delete the arrow from the left menu @FOSSLight-dev (#388)

## 🔧 Maintenance

- Add a default value for server domain @FOSSLight-dev (#392)
- Add a shortcut link when sending a license email @soimkim (#390)
- Delete the arrow from the left menu @FOSSLight-dev (#388)

---

## v1.2.26 (14/01/2022)
## Changes
## 🐛 Hotfixes

- Fix bugs related to Auto ID, mailing, and check license. @FOSSLight-dev  (#386)
    - API > Fix the bug that the admin account does not have permission to the project.
    - Auto ID > Fix the bug where the license is not automatically selected according to the license type priority for OSS with OR.
    - Mailing (License) > Fix the bug where the email arrives as if it was fixed even if the license website was not modified.
- Fix project version not displayed bug @FOSSLight-dev (#384)

## 🔧 Maintenance

- Fix the bug that the written License is not included in the Check License related to the Proprietary License @FOSSLight-dev (#385)
    - License modification email > If there are multiple websites, separate them with line breaks.
    - Check License > If the written license is a Proprietary License, even though there is a warning message of Declared, it is not included in the Check License.
    - When distribution, add a notice when the release date is not set.
    - Fix the bug that when registering or deleting a license, there are multiple websites, but only one is displayed in the email.
- Update version to 1.2.26 @soimkim (#387)
---

## v1.2.25 (07/01/2022)
## Changes
## 🔧 Maintenance

- Remove license duplication in a cell @FOSSLight-dev (#381)
- Change message from System to Hub @FOSSLight-dev (#378)
- Support variable context path.  @FOSSLight-dev  (#382)
- Show progress bar when deleting OSS.  @FOSSLight-dev  (#382)
- Deduplicate Licenses in one cell.  @FOSSLight-dev  (#382)
---

## v1.2.24 (31/12/2021)
## 🚀 Features

- Added function to generate OSS List through github link in Self-Check @namkyu1999 (#353)
- Add Check OSS Name and Check License function in 3rd party @riyenas0925 (#364)
- Add the Bulk Edit funtion @FOSSLight-dev (#361)

## 🐛 Hotfixes

- Update redistribution and vulnerability discovered mailing sql @FOSSLight-dev (#369)
- Fix the bug that occurs when checking hide version in OSS notice @FOSSLight-dev (#368)
- Fix Vulnerability, Sent mail list not searchable bug @FOSSLight-dev (#365)
- Fix infinite loading bug when clicking Check License @riyenas0925 (#362)
- Fix xss filter for License List with &amp; @yugeeklab (#356)

## 🔧 Maintenance

- Add display in comments only when License is OR, AND @riyenas0925 (#376)
- Add comment output when loading SPDX @riyenas0925 (#373)
- Delete License output when loading SPDX @riyenas0925 (#372)
- Fix License and Comment output when loading SPDX @riyenas0925 (#371)
- Add Check License function in Self-Check page @riyenas0925 (#367)
- License and Comment output when loading SPDX @riyenas0925 (#358)
- Hide the Check License, Check OSS Name buttons with the same conditions as the Save button. @Zeusjonass (#354)

---

## v1.2.23 (24/12/2021)
## 🚀 Features

- Show FOSSLight version in sidebar @riyenas0925 (#352)

## 🐛 Hotfixes

- Fix mismatching error in Check License @riyenas0925 (#349)

## 🔧 Maintenance

- Delete ${sidx}, ${sord}, ${schKeywordSql} and ${filterCondition} for defending SQL Injection  @yugeeklab (#346)
- Update distribution mailing condition. @FOSSLight-dev  (#359)
- Check OSS Name > Change download location to git:// from https:// @FOSSLight-dev (#357)
- Send comments to Creator, Watcher, and Reviewer by mail. @riyenas0925 (#355)
- Add parameter for merging option to BOM export. @FOSSLight-dev (#350)

---

## v1.2.22 (17/12/2021)
## Changes

- Add multilingual support about delivery form @yugeeklab (#335)

## 🚀 Features

- Add OSS Notice issuance function to Self-Check @namkyu1999 (#288)
- In Check OSS Name, make it possible to select from multiple OSS Names. @FOSSLight-dev  (#342)
- Add External Service health check @riyenas0925 (#310)

## 🐛 Hotfixes

- Eliminate null pointer exception in OSS Controller @yugeeklab (#316)
- Change the ckeditor config value related to file upload. @FOSSLight-dev (#343)
- Eliminate null pointer exception in CoMailManager @yugeeklab (#317)
- Fix heap memory issue when registering CPE Data Feed (GC Limit) @FOSSLight-dev (#332)
- Apply autogrow to ckeditor @yugeeklab (#328)

## 🔧 Maintenance

- Support Check OSS Name, Check License multilingual @riyenas0925 (#344)
- Do not print () if the OSS version is null when leaving a comment in the Check License. @riyenas0925 (#339)
- Add a function to change the OSS table in bulk (incomplete) @FOSSLight-dev (#338)
- Change max height of editor @soimkim (#336)
- Add Check License to Project tab (bin, android) @riyenas0925 (#324)

---

## v1.2.21 (10/12/2021)
## 🚀 Features

- Fix OSS Notices to include SPDX (json) and SPDX (yaml) @riyenas0925 (#320)

## 🐛 Hotfixes

- Fix the bug where the notice file format is unchecked whenever the status is changed @FOSSLight-dev (#329)
- Fix the license duplication bug in Check License @riyenas0925 (#318)

## 🔧 Maintenance

- Add test to github action for PR @yugeeklab (#301)
- Remove the label setting for PR from the guide @soimkim (#327)
- Update 3rd party list UI @riyenas0925 (#315)
- Change SPDX dependecy version @riyenas0925 (#323)
- Exception handling when accessing a deleted project @FOSSLight-dev (#322)
- Change token to invisible @riyenas0925 (#312)

---

## v1.2.20 (03/12/2021)
## Changes
## 🚀 Features

- Change the creator and modifier from ID to name in OSS list, License … @acafela (#295)

## 🐛 Hotfixes

- Fix the bug where data is not output when sending mail @yugeeklab (#313)
