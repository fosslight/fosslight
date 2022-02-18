# Changelog

## v1.2.30 (18/02/2022)
## Changes
## 🐛 Hotfixes

- Fix a bug that is not searched by restriction in the License List @FOSSLight-dev (#409)

## 🔧 Maintenance

- Fix the bug where the License List is not filtered by restriction @FOSSLight-dev (#410)
- Separate the handling of npm's @group name from Check OSS Name @FOSSLight-dev (#408)
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

---

## v1.2.19 (03/12/2021)
## Changes
## 🚀 Features

- Change github token in code management @riyenas0925 (#308)
- Update Open API and deleting files function @FOSSLight-dev (#305)
- Add to show Check License conversion evidences @riyenas0925 (#300)
- Add a function to copy 3rd party to Project @FOSSLight-dev (#303)
- Save language settings per user @riyenas0925 (#285)
- SPDX Spreadsheet License List parsing @kimtaehyun98 (#255)

## 🔧 Maintenance

- Change the license url in OSS Notice (simple version) @FOSSLight-dev (#311)
- Fix a cross-site scripting issue @yugeeklab (#259)
- Upgrade ckeditor4 @yugeeklab (#307)
- Fix a vulnerability for resource leak @yugeeklab (#278)
- Fix bugs related to partner registration @FOSSLight-dev (#309)
- Eliminate Null Point Exception @yugeeklab (#290)
- Fix a vulnerability for public methods that return private array @yugeeklab (#291)
- Fix the vulnerability that public array is assigned to private variable @yugeeklab (#302)

---

## v1.2.18 (26/11/2021)
## Changes
- Modify the length of the license name and revert it @FOSSLight-dev (#281)

## 🚀 Features

- Add file path and copyright to Self-check @FOSSLight-dev (#292)
- Add license inquiry function using external API (Github, Clearly Defined) @riyenas0925 (#269)
- Save configuration when users search @astrod (#276)
- Update OSS Table Function @FOSSLight-dev (#284)
- Update OSS Table > view comment info of oss popup @FOSSLight-dev (#282)

## 🐛 Hotfixes

- Fix check oss name save error @riyenas0925 (#277)

## 🔧 Maintenance

- Fix heap memory issue when registering CPE Match Feed @FOSSLight-dev (#289)
- Update the project's report template @soimkim (#286)

---

## v1.2.17 (19/11/2021)
## Changes
## 🚀 Features

- Create integration test codes @astrod @mingukang-kr  @sw-develop @Lee-JaeHyuk (#254)
- Add Check License in Self-Check @riyenas0925 (#265)
- Check OSS License in existing db @riyenas0925 (#258)

## 🔧 Maintenance

- Shorten the time required to save during OSS sync @FOSSLight-dev (#273)
- Fix the vulnerability that public array is assigned to private variable @yugeeklab (#267)
- Fix the vulnerability for public method that return private type @yugeeklab (#266)
- Fix Null pointer exception  @yugeeklab (#252)
- Fix the vulnerability for inapproate error message @yugeeklab (#268)
- Fix the vulnerability for inapproate error message @yugeeklab (#263)
- Refactor Check License, Check OSS Name @riyenas0925 (#262)

---

## v1.2.16 (12/11/2021)
## Changes
## 🐛 Hotfixes

- Update Check OSS Name Function @FOSSLight-dev (#257)
- Fix SPDX convert error (when invalid download location) @riyenas0925 (#251)

## 🔧 Maintenance

- Update spdx/tools-java version @riyenas0925 (#260)
- Fix the bug that occurs when loading the BOM @FOSSLight-dev (#253)

---

## v1.2.15 (05/11/2021)
## Changes
## 🚀 Features

- Support multiple webpages of License @FOSSLight-dev (#247)
- Add OSS ​​Type to search condition in OSS List @yugeeklab (#230)
- Change CVE-ID to a link (#212) @acafela (#237)

## 🐛 Hotfixes

- Fix NPE in common/CoCodeManager @yugeeklab (#241)
- Changed - Recover Missing Messages @riyenas0925 (#248)
- Fix language select box unselected error @riyenas0925 (#240)
- Fix bugs related to BOM loading and comment history @FOSSLight-dev(#243)

## 🔧 Maintenance

- Retry when NVD download fails and update email information. @FOSSLight-dev (#238)
- Delete the github action for merging into main @soimkim (#239)

---

## v1.2.14 (29/10/2021)
## Changes
## 🐛 Hotfixes

- Revert - Column mapping for import SPDX Spreadsheet @FOSSLight-dev  (#229)

## 🔧 Maintenance

- Update send email function (oss type image) @FOSSLight-dev (#235)
- Update OSS Table legacy function, OSS Info mail format @FOSSLight-dev (#231)

---

## v1.2.13 (27/10/2021)
## Changes
## 🚀 Features

- Load and save all sheets with sheet names starting with Self-check, SRC, BIN @namkyu1999 (#223)
- Support Import SPDX Spreadsheet in 3rd Party @kimtaehyun98 (#220)

## 🐛 Hotfixes

- Fix the missing item that print the "Declared :" message @FOSSLight-dev (#228)
- Fix the bug where the link tag is displayed in the mail subject. @FOSSLight-dev (#225)


---

## v1.2.12 (22/10/2021)
## Changes
## 🚀 Features
- Supports 3rd Party List OSS report as csv attachment @riyenas0925 (#203)
- Support Import SPDX Spreadsheet in Self-Check List @kimtaehyun98 (#201)
- Support Import SPDX Spreadsheet in BIN tab @kimtaehyun98 (#200)
- Update statistics mostused & oss name merge function @FOSSLight-dev (#216)
- Add SPDX json and yaml formats for modified notice packaging @hyewoncc (#193)
- Support Import SPDX Spreadsheet in SRC tab @kimtaehyun98 (#184)

## 🐛 Hotfixes

- Update statistics mostused & oss name merge function @FOSSLight-dev (#216)
- Add missing STATISTICS_MOSTUSED table @riyenas0925 (#214)
- Modify detected licenses to be separated by , in mail sent @riyenas0925 (#205)
- Bug Fix when Import SPDX Spreadsheet in SRC tab @kimtaehyun98 (#199)

## 🔧 Maintenance

- Moving hardcoded messages to US and KR properties @epicarts (#210)
- Change PR merged action to only work for main @soimkim (#208)
- Add contributors to README @soimkim (#202)

---

## v1.2.11 (15/10/2021)
## Changes
## 🚀 Features

- Changed - Recover Missing Messages @FOSSLight-dev (#198)
- Supports Self-Check List OSS report as csv attachment @riyenas0925 (#190)
- Supports BIN tab OSS report as csv attachment @riyenas0925 (#181)
- Add send to everyone when sending comments @riyenas0925 (#176)
- Add SPDX json and yaml types for pakaging notice download @hyewoncc (#177)
- Supports SRC tab OSS report as csv attachment @riyenas0925 (#162)
- Add a language change function using the dropdown for task 4 @suhwan-cheon (#151)

## 🐛 Hotfixes

- Changed - Recover Missing Messages @FOSSLight-dev (#198)
- Change link format of vulnerability discovered mail (#168) @acafela (#175)
- Restore message.properties @epicarts (#185)

## 🔧 Maintenance

- Translated from English to Korean in ko properties file @epicarts (#183)
- Change Self-Check in Obligation Warning message @Lee-JaeHyuk (#192)
- Fix error where download location is not output when ossname is "-" @riyenas0925 (#186)
- Change the SPDX Spreadsheet output method for OSS Name is - @riyenas0925 (#179)
- Change the status column to icon in 3rd party list @epicarts (#154)

---

## v1.2.10 (08/10/2021)
## Changes
## 🚀 Features

- Updated bom compare & oss name merge @FOSSLight-dev (#178)
- Delete spdx-tools jar file and add spdx tools java dependency @riyenas0925 (#148)

## 🐛 Hotfixes

- bug fix - Handling exceptions when creating a new project @FOSSLight-dev (#180)
- Fix no items are printed in the "Per File Info" sheet @riyenas0925 (#173)
- Updated bom compare & oss name merge @FOSSLight-dev (#178)
- Update SPDX tool and fix typo @soimkim (#174)

## 🔧 Maintenance

- Update SPDX tool and fix typo @soimkim (#174)
- Enable cursor pointer on the vulnerbility icon @epicarts (#157)
- Fix the nickname input width size to double @epicarts (#155)
- Change from RestTemplate to WebClient @riyenas0925 (#152)
- Fix nickname typo in mail template @epicarts (#153)
- Add template to docker environment @soimkim (#150)

---

## v1.2.9 (01/10/2021)
## Changes
## 🚀 Features

- Update Statistics NONE value except @FOSSLight-dev (#149)
- Add External Service settings to the configuration page @riyenas0925 (#145)
- RestTemplate config for task 1 @riyenas0925 (#141)

## 🐛 Hotfixes

- Fix the comments history shows properly in identification(project), third-party, project info tabs @su-ram (#134)

## 🔧 Maintenance

- Check whether comments are entered when the drop button is clicked @namkyu1999 (#146)

---

## v1.2.8 (24/09/2021)
## Changes
## 🔧 Maintenance

- Exclude test on release @soimkim (#144)

---

## v1.2.7 (24/09/2021)
## Changes
## 🚀 Features

- Update warning message condition @FOSSLight-dev (#143)
- Change load all sheets starting with SRC in oss_report_src (REST_API) @riyenas0925 (#131)
- Add deactivate flag on getOssInfo @doggai10 (#129)
- Update warning message condition @FOSSLight-dev (#132)

## 🐛 Hotfixes

- Update warning message condition @FOSSLight-dev (#143)

## 🔧 Maintenance

- unquoted port mapping may be interpreted as a base-60 value @riyenas0925 (#139)
- Initialize test code setting @astrod (#135)

---

## v1.2.6 (17/09/2021)
## Changes
## 🚀 Features

- Update warning message condition @FOSSLight-dev (#132)
- Add function to save NVD Feed Data from 2002 @FOSSLight-dev (#118)
- Search website regardless of http://, https://, www. @doggai10 (#114)

## 🔧 Maintenance
- Add alert when the number of OSS versions is 1 @riyenas0925 (#123)
- Add line separator between notice intro and OSS list @wkdalsgh192 (#121)
- Add user guide and restriction to license popup @riyenas0925 (#125)

## 🐛 Hotfixes

- Update Oss Sync Function @FOSSLight-dev (#117)

---

## v1.2.5 (09/09/2021)
## Changes
## 🚀 Features

- Recommend the OSS Name according to the OSS Naming Rule @namkyu1999 (#82)

## 🐛 Hotfixes

- Update OSS Sync Function @FOSSLight-dev (#117)
- Exclude from 'Check OSS Name' unless it is an 'Unconfirmed open source' @soimkim (#116)

## 🔧 Maintenance

- Delete "Need check" in the Obligation Type from the search box in the License List @Lee-JaeHyuk (#101)
- Translate some korean comments to english @wkdalsgh192 (#60)

---

## v1.2.4 (03/09/2021)
## Changes
## 🚀 Features

- 3rd party / project status check func & oss sync func @FOSSLight-dev (#113)
- Add Docker mailserver in docker-compose @epicarts (#112)
- Add license information display when clicking the Restriction icon @riyenas0925 (#71)
- Expose a save button for a creator, watcher, and admin @astrod (#88)

## 🐛 Hotfixes

- 3rd party/project status check function @FOSSLight-dev (#115)
- 3rd party / project status check func & oss sync func @FOSSLight-dev (#113)

## 🔧 Maintenance

- Fix hide 'Check OSS Name' button from unrelated users @hyewoncc (#110)
- Modify pop-up phrases that occur when you press the reopen button @suhwan-cheon (#97)
- Rename 'Excel Download' Button to 'Export' @sw-develop (#104)
- Drop button requires comment @soimkim (#105)

---

## v1.2.3 (27/08/2021)
## Changes
## 🚀 Features

- Add a new function to synchronize from OSS information @FOSSLight-dev (#93)

## 🐛 Hotfixes

- Add a new function to synchronize from OSS information @FOSSLight-dev (#93)

## 🔧 Maintenance

- Modify 'Request Review' button(Delete icon and rename to 'Request') @ubermen5che (#91)
- Fix header column names of Vulnerability Log table @kimtaehyun98 (#87)
- Fix typo 'SourceCode' in Self-Check @hyewoncc (#83)
- Add label:improvement to also appear as child of Feature in release draft @riyenas0925 (#81)
- Modify github action to deploy 30 seconds after release @soimkim (#76)

---

## v1.2.2 (20/08/2021)
## Changes
## 🚀 Features

- Add deactivate function to OSS @FOSSLight-dev (#72)
- Add a function to check details detected license. @ubermen5che (#69)
- Add CVE link to vulnerability discovered mail body (#64) @acafela (#66)
- Add License Type option for OSS List search @hyewoncc (#62)

## 🐛 Hotfixes

- Add deactivate function to OSS @FOSSLight-dev (#72)
- Fix a bug for type check error. @ubermen5che (#61)

## 🔧 Maintenance

- Fix toggle button color to match others @hyewoncc (#73)
- Change Admin can Restart/Complete with no comment @kimtaehyun98 (#63)
- Update message.properties @k2heart (#65)
- Deploy the latest release to demo.fosslight.org @soimkim (#68)
- Add auto-updating CHANGELOG.md on develop branch @riyenas0925 (#58)

---

## v1.2.1 (13/08/2021)
## Changes
- Add to show docker-compose log in github-actions @riyenas0925 (#50)

## 🚀 Features

- Bug fix - validation check @FOSSLight-dev (#27)

## 🐛 Hotfixes

- Update the function related to detected license and changing status @soimkim (#56)
- Add DROP status to project search @riyenas0925 (#45)
- Bug fix - validation check @FOSSLight-dev (#27)
- Remove warning message @riyenas0925 (#25)
- Fix a bug related to load properties @soimkim (#22)
- Fix a bug to create directories @soimkim (#17)

## 🔧 Maintenance

- Update the function related to detected license and changing status @soimkim (#56)
- Move the Drop button to the right of the Delete button in "Project > Basic Information" @suhwan-cheon (#55)
- FIx typo 'website' in UI @epicarts (#48)
- Fix Timezone to Asia/Seoul @epicarts (#46)
- Typo in Class @kimtaehyun98 (#41)
- Fix gradew file permission @epicarts (#38)
- Fix typo in CoTopComponent.java in fosslight @ubermen5che (#34)
- Fix Typo @greeenly (#32)
- Update .gitignore to ignore personal data folders @namkyu1999 (#31)
- Change db settings for running with Docker @soimkim (#24)
- Run PR action for all branches @soimkim (#23)
