# Changelog

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

---

## v1.2.0 (30/07/2021)
## Changes
## 🚀 Features

- Add Declared/Detected License to OSS @soimkim (#14)

## 🐛 Hotfixes

- Fix a bug - Vulnerability score of Self-Check List @soimkim (#14)

## 🔧 Maintenance

- Change 3rd Party List - Searching UI @soimkim (#14)
- Add a spring boot badge to README @soimkim (#12)
- Add Description of ID and Password for Demo site @fu7mu4 (#10)

---

## v1.1.0 (22/07/2021)
## Changes
## 🚀 Features

- Improve project-related functions @FOSSLight-dev (#6)
- Created Dockerfile and docker-compose.yml @sameer1046 (#3)

## 🐛 Hotfixes

- Improve project-related functions @FOSSLight-dev (#6)
- Move DB related files to db directory @soimkim (#5)

## 🔧 Maintenance

- Add action for PR @soimkim (#9)
- Move DB related files to db directory @soimkim (#5)
- Update docker files @soimkim (#4)
- Remove unnecessary files @soimkim (#1)

---

## v1.0.1 (27/05/2021)
## Changes
* Set up automated deployment.

---

## v1.0 (30/04/2021)
Release FOSSLight v1.0 🎉