# Changelog

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