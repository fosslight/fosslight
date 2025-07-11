<!--
Copyright (c) 2022 LG Electronics
SPDX-License-Identifier: AGPL-3.0-only
 -->
<p align='right'>
  <a href="https://github.com/fosslight/fosslight_system/blob/main/RELEASE_NOTES.md">[Eng]</a>
</p>

## [2.3.0](https://github.com/fosslight/fosslight/releases/tag/v2.3.0) (2025-07-09)

### New
* 3rd Party
  - FOSSLight Report export시 3rd party information sheet가 추가
* Project
  - Project Information에 보안 담당자를 입력할 수 있는 필드가 새롭게 추가
    - Creator와 Editor와 함께 보안 담당자는 FOSSLight Hub에서 발송되는 vulnerability 관련 메일을 수신할 수 있음
  - OSORI DB 정보 추가
    - Pre-Review > Open Source, License 탭에 오소리(Osori)의 DB 정보를 참조할 수 있도록 추가
  - DEP 탭 Dependency Tree View 제공
    - FOSSLight Dependency Scanner를 통해 분석된 경우 각 의존성(dependency)의 관계를 트리 구조로 시각화하여 확인할 수 있음
* API
  - Security Responsible Person을 업데이트 할 수 있는 API 추가 (/api/v2/projects/{id}/security-person)
  - Security Mail을 disable/enable 할 수 있는 API 추가.(/api/v2/projects/{id}/security-mail)
* Common
  - Custom Column 확대 적용
    - Security 탭, Project/3rd Party Identification, Self-Check에 Custom Column 기능이 추가
  - 탭 새로고침
    - 상단에 열린 탭 클릭이 아닌 다른 방법으로 탭 진입시, refresh 팝업이 뜸

### Changed
* Project
  - Packaging
    - 기존에는 OSS Package 파일을 최대 4개까지 업로드 할 수 있었으나, 이번 업데이트를 통하여 업로드 가능한 파일의 개수가 5개로 확장
  - SPDX, CycloneDX
    - SPDX와 CycloneDX 문서가 생성될 때, DEP 탭의 Package URL 기준으로 출력이 되도록 변경
    - OSS Name과 OSS Version이 같더라도, Package URL이 다르면 각각 출력이 되어 모든 Relationship이 표시
  - CycloneDX 1.6 지원
* API
  - 사용자가 발급받은 Token 정보를 메일 뿐만 아니라, FOSSLight Hub 내 User Setting 메뉴에서도 확인
  - GEP /api/v2/projects API의 return 값에 security mail, security person, editors, publicYn 정보 추가
  - /api/v2/projects/{id}/{tab_name}/reports API에 bom탭을 save할 수 있는 "bomSave" parameter 추가
  - GET /api/v2/projects API에 modelName과 exact match하는 정보를 찾을 수 있는 "modelNameExactYn" parameter 추가 
  - /api/v2/projects/{id}/reset API에 모든 탭을 reset 할 수 있는 "all" 옵션 추가
* License, OSS
  - Share URL 버튼 추가
  - level에 따른 Restirciton 아이콘 색깔 변경
* Security
  - cpe 하위에 보이는 Running on/with는 OS 정보가 표시되도록 추가
* DataBase
  - Column 추가
  - PROJECT_MASTER: PACKAGE_FILE_ID5
  - Column 삭제
    - PROJECT_MASTER: PACKAGE_VUL_DOC_FILE_ID, VUL_DOC_SKIP_YN
  - Table 추가
    - NVD_DATA_RUNNING_ON_WITH_TEMP, NVD_DATA_RUNNING_ON_WITH

## [2.2.0](https://github.com/fosslight/fosslight/releases/tag/v2.2.0) (2025-02-19)

### New
* 3rd Party
  - 3rd Party Information 추가
  - 3rd Party Identification 추가 (3rd party / bom 탭 추가)
  - 3rd party BOM Compare 기능 추가 
* Project 
  - Pre-Review > codelinaro type 추가

### Changed
* License
  - License text에 null 값 저장 가능하도록 변경
  - list 초기 화면에서 modified date 내림차순으로 정렬되도록 변경
* Review Report
  - OSS Important Notes 정보도 출력하도록 변경
  - OSS, License 이름 클릭시 상세화면으로 이동하는 link 제공
* Project 
  - 여러개의 프로젝트를 “change status” 할 수 있도록 변경
  - BOM merge 조건 변경
     - OSS Name이 “-“인 경우, license, homepage, download location이 같으면 merge 하도록 변경
  - Loaded list가 최근에 추가된 순서대로 보이도록 변경
* API
  - /api/v1/vulnerability_data, /api/v2/vulnerabilities의 response에 description 제외하고 oss info 추가됨
* OSS
  - OSS name 변경은, 상세 화면에서 edit 버튼을 통해서만 변경 가능함
  - list 초기 화면에서 modified date 내림차순으로 정렬되도록 변경
* DB
  - Column 명 변경 
    - PROJECT_MASTER : DESTRIBUTION_STATUS > DISTRIBUTION_STATUS
  - Column 삭제
   - PRE_PROJECT_MASTER : OSS_TYPE, OS_TYPE_ETC, DISTRIBUTION_TYPE
* Mail
  - Code Management에서 division(Code No : 200)의 USE_YN을 N으로 변경시, 해당 division의 user에게 알려주는 기능 추가
* 버그 수정
  - Packaging에서 업로드한 package file이 여러개인 경우, 모든 파일의 banned list가 merge되어 보이도록 수정
  - OSS copy시 이름을 변경하여 저장하는 경우, 기존 데이터가 변경되는 이슈 수정(oss_common_id 초기화하는 로직 추가)
  - Statistics 메뉴에 정보가 제대로 보여지지 않는 이슈 수정
  - Vulnerability 메뉴가 조회되지 않는 이슈 수정
  - Grid table에 filter 검색 후에도 column width가 유지되도록 수정
  - OSS version 추가로 등록시 메일 발송되지 않는 이슈 수정

## [2.1.1](https://github.com/fosslight/fosslight/releases/tag/v2.1.1) (2024-12-13)

### New
* Open source
  - Important Notes 항목 추가
* Project 
  - Pre-review > cargo type 추가
  - Packaging - Notice에서 append 할 때 file 형태도 가능하도록 기능 추가
* UI
  - Restriction > level에 따른 icon 색상 추가
* API
  - Project reset API 추가
  - Project delete API 추가
  

### Changed
* Project 
  - Information에서 용어 변경: Watcher -> Editor
  - BOM Save시 불필요한 확인 팝업 제거
  - Identification > BIN탭에서 warning message 레벨 변경
    - OSS Name 다르고, License 같은 경우, Warning -> Info로 레벨 낮춤
  - Packaging > path 입력시 /가 입력될 수 없도록 변경
  - fosslight_binary.txt 영역 삭제. fosslight_binary.txt 대신 fosslight binary report에 tlsh, checksum값이 포함되도록 대체됨
  - 권한이 없는 프로젝트에 대해서는 BOM Compare 불가능하도록 수정
  - Request상태에서는 수정 불가능 하도록 권한 체크 로직 변경
  - Distribution시 따옴표, 큰따옴표 입력 불가하도록 처리
* Open Source
  - Sync기능에서 OSS_COMMON에 해당하는 항목 삭제
  - Sync에 restriction 추가
  - Sync동작 시 current version으로 comment 추가 되도록 변경
  - List 검색 기준에 cpe관련 항목 추가
* Mail
  - Opensource 공통정보에 대한 변경 사항도 표시
  - Opensource all version comment에 대한 포맷 변경 
  - Opensource purl 정보 추가
  - Vulnerability Discovered 메일에서 info 테이블을 가져오는 쿼리 수정. 
    Dependency탭에서도 OSS Name 사용 되도록 변경 되면서 쿼리 수정이 필요해짐
* Review Report
  - License review가 보여지는 조건 변경
* UI
  - License / Opensource 상세 화면에서 저장 버튼 클릭 시, input box에 입력된 값이 있는 경우 Add를 자동으로 해줌
* API
  - Watcher -> Editor용어 변경에 따라 API명 변경됨
  - API 호출 시, User permission check 관련 기능이 UI와 동일하도록 수정
  - Project search API
    - paging을 위한 parameter 추가
    - return 값에서 key 변경
  - Project create 개수 제한 해제
  - 토큰 생성시, 랜덤 토큰을 사용하도록 변경
* **버그 수정**
  - Project > Identification
    - Status bar 버그 수정
    - Pre-review 에러 수정
    - copyright 정보 업데이트 안되는 이슈 수정
  - Project > Packaging
    - Verify 과정 버그 수정
    - 여러 프로젝트에서 패키징 파일이 참조되는 경우, 한 프로젝트에서 삭제시, 물리적으로 패키징 파일 삭제되지 않도록 수정
    - 패키징 파일 로드 시 4번째 패키징 파일이 보이지 않거나, copy 안 되던 이슈 수정
  - Project > Security
    - Security 버튼에 상태 표기 이슈 수정
    - 버전없는 open source의 vulnerability 목록 보이지 않는 이슈 수정
  - 3rd Party
    - related document 삭제 안되는 버그 수정
    - 3rd party 생성화면 버그 수정
  - License
    - License에서 Comment만 남기는 경우, mail 발송 시 버그 수정
  - Open source    
    - 상세페이지에서 open source와 연결된 license의 restriction이 보이도록 수정
    - Purl 생성 관련 버그 수정
  - Vulnerability
    - recalculated 관련 로직 수정
  - DB
    - OSS COMMON ID가 중복으로 생기는 버그 수정
    - fosslight_create.sql에 누락되었던 table 추가
    - 누락된 코드 데이터 추가
      - Source code disclosure scope
      - Restriction
* 기타
  - Legacy code 삭제: jsp 및 사용하지 않는 라이브러리 파일 삭제
  - verify script path를 root.dir을 포함한 절대 경로로 변경


## [2.1.0](https://github.com/fosslight/fosslight/releases/tag/v2.1.0) (2024-11-05)

### New

* **Security 탭 기능 추가**
  - Need to Resolve / Full Discovered 로 내부 탭명 변경
    - 기존 Total / Fixed / Not Fixed 3개로 구분되는 방식에서 Need to Resolve / Full Discovered 2개의 탭으로 구분 되도록 변경
    - Need to Resolve: 기준 점수 이상의 CVE ID 표시. 
                       기준 점수는 Code management 메뉴에서 Security Vulnerability Standard Score로 설정
    - Full Discovered: 검출된 전체 CVE ID 표시
  - Column 추가 : Vulnerability Link, Security Comments 컬럼 추가
    - Vulnerability Link: 
    - Security Comments: Vulnerability Resolution 결과에 대한 Comments를 남길 수 있도록 Security Comments 컬럼을 추가함
  - 엑셀 업로드 기능 추가
* **Security Mail Enable/Disable 기능 추가**
  - 프로젝트의 Security Mail 수신여부를 설정할 수 있는 항목 추가
  - Project Information에서 설정 가능
  - Security Mail Disable 이유는 필수 입력
* **Packaging > Source 탭 Binary List 추가**
  - Packaging 과정에서 source code가 아닌 binary가 취합되는 것을 방지하기 위해 binary list 기능을 추가
* **Migration script 추가**
  - 20241025020001_update_v2.1.0.sql: v2.1.0 변경 사항 적용을 위한 migration script
  - 20241104111630_update_v2.1.0_update_license_data.sql: Opensource 에서 사용한 License 데이터 중 누락된 값을 추가를 위한 migration script

### Changed

* **fosslight_create.sql 데이터 추가**
  - fosslight_create.sql에 License 데이터 추가
  - License 데이터가 누락되어 Opensource List가 전체 데이터 중 일부만 보여지는 버그가 있었음
* **Packaging탭 업로드 파일 개수 추가**
  - 업로드 용량을 20GB까지 지원할 수 있도록, packaging 파일 업로드 개수 4개로 증가
* **버그 수정**
  - Packaging verify버그로 file count가 정상적으로 되지 않던 버그 수정
  - Statistics 엑셀 파일로 Export할 때 버그 수정
  - OSS Component ID 매칭 문제로 Project > Identification > Admin check되어있을 때 저장 안되는 버그 수정
  - License / Open Source List에서 보여지는 Default column 명 오류 수정 (Obligation -> Notice/Source)
  - Project > Identification 에서 3rd party data load 할 때 3rd party 명이 나오지 않는 오류 수정
  - Vulnerability matching 관련 버그 수정


## [2.0.2](https://github.com/fosslight/fosslight/releases/tag/v2.0.2) (2024-10-14)

### Changed
* **License / Open Source obligation 표기 방식 변경**
  - Obligation column을 분리: Obligation -> Notice / Source
  - Notice / Source 의 의무사항이 있는 경우 체크표시로 변경
* **Database minor 변경사항**
  - 누락되었던 Final Review Status를 T2_CODE_DTL 테이블에 추가
  - OSS_COMPONENTS_LICENSE table에서 foreign key 삭제됨
* **Save과정에서 발생하던 에러 수정**
  - grid data 저장 과정에서 license 취합 시 component id 를 setting 는 부분에서 신규 입력된 경우에 대한 id설정 에러 수정
  - identificatoin save시 foreign key로 인해 발생하던 이슈 수정
* **속도 개선** 
  - Project, 3rd party, self-check save 과정 속도 개선
* **Packaging 버그 및 기능 추가**
  - Project copy 이후 Packaging verify에서 발생하던 버그 수정
  - Packaging 과정에서 path에 공란 입력 불가능하도록 변경
* **Comment 버그 및 기능 추가**
  - confirm comment 메세지가 있으면 confirm 진행 안되던 버그 수정 
  - admin이 confirm / reject 할때 draft comment를 불러옵니다
* **Security탭 버그 및 기능 수정**
  - Security tab에서 검출된 vulnerability가 없는 경우 에러 수정
* **2.0.0 버전 release note 수정**
  - 2.0.0에 반영된 항목에 대해 구체적으로 내용 업데이트


## [2.0.1](https://github.com/fosslight/fosslight/releases/tag/v2.0.1) (2024-09-30)

### Changed
* **Jqgrid 로컬 리소스 사용으로 변경**
 - jqgrid의 CDN지원이 중단되어, 로컬 리소스를 사용하도록 변경되었습니다.


## [2.0.0](https://github.com/fosslight/fosslight/releases/tag/v2.0.0) (2024-09-27)

### NEW
* **오픈소스 데이터베이스 스키마 변경**
  - 효율적인 오픈소스 정보 관리를 위해 OSS_MASTER 테이블이가 'OSS_COMMON'과 'OSS_VERSION'으로 분리되었습니다.
    - **'OSS_COMMON' 테이블**: OSS의 공통 정보를 관리하기 위해 추가되었습니다.
    - **'OSS_VERSION' 테이블**: OSS의 버전 정보를 관리하기 위해 추가되었습니다.

* **라이선스 및 오픈소스 정보 강화**
  - OSORI 데이터를 기반으로 Restriction이 업데이트되었습니다.
  - 라이선스에 소스 코드 공개 범위 정보(Source Code Disclosure Scope)가 추가되었습니다.
  - 오픈소스에도 Restriction 정보가 포함됩니다.
  - 오픈소스의 전체 버전과 특정 버전에 대한 Comment를 구분하여 남길 수 있습니다.
  - 오픈소스의 Download location 정보가 공통 정보로 관리 됩니다.
  - Download location에 대해 PURL이 추가되었습니다.

* **오픈소스 보안 취약점 매칭 강화**
  - 보안취약점 매칭 강화를 위해 오픈소스에 'Include CPE', 'Exclude CPE', 'OSS Version Alias'가 추가되었습니다.

### Changed
* **Hub 버전 2.0을 위한 fosslight_create.sql 업데이트**
  - 2.0.0 버전에 맞게 fosslight_create.sql이 업데이트 되었습니다.

* **API V2 업데이트**
  - API v2 Swagger UI에서 인증 방법이 변경되었습니다.
  - 3rd party export API가 추가되었습니다.
  - BOM export/JSON API의 이름이 수정되었습니다.

* **버그 수정**
  - UI에서 발생한 문제들이 수정되었습니다.

* **속도 개선** 
  - Project 목록 화면 속도 개선을 위해 PROJECT_MASTER에 column이 추가되었습니다.

### Notes
기존 유저들을 위해 Hub 2.0.0 버전으로 업그레이드 하기 위한 Migration script를 제공합니다.  
(파일명: 20240725150921_update_v2.0.0.sql)

이번 Migration script는 여러 스키마가 변경되므로 전체적인 내용을 확인하시길 권장드립니다.  
또한 Migration script는 정상 케이스만 지원하고 있으니 사용에 참고하시기 바랍니다.

데이터 내용에 따라 Migration 과정에서 에러가 발생할 수 있어 **특히 주의 깊게 확인해야 할 사항**은 다음과 같습니다:

1. **OSS_LICENSE_DECLARED, OSS_LICENSE_DETECTED 테이블의 foreign key 변경**
  - OSS_ID에 대해 정상적이지 않은 데이터가 들어있는 경우, fk 변경에 에러가 발생할 수 있습니다.
  - 이로 인해 데이터 무결성을 확인하고, 필요시 정정 작업을 사전에 수행해야 합니다.

2. **T2_CODE, T2_CODE_DTL 데이터 삭제 및 변경**
  - 삭제 및 변경되는 항목: 913, 230
  - 기존에 해당 코드를 변경해서 사용하고 있었는지 확인하시기 바랍니다.
  - 이로 인해 관련된 비즈니스 로직이나 데이터 참조가 영향을 받을 수 있으므로, 사전 검토가 필요합니다.


이외의 주요 변경 사항들에 대해 정리하면 다음과 같습니다:

1. **Opensource 관련 데이터베이스 분리**
- 'OSS_COMMON'과 'OSS_VERSION'으로 나누어졌습니다.[RELEASE_NOTES.md](..%2FRELEASE_NOTES.md)

2. **OSS_DOWNLOADLOCATION_COMMON, OSS_NICKNAME_COMMON 테이블 추가**
  - 오픈소스 공통정보(OSS_COMMON_ID)와 Download location이 연동되도록 Schema가 변경되었습니다.
  - 오픈소스 공통정보(OSS_COMMON_ID)와 Nickname이 연동되도록 Schema가 변경되었습니다.

3. **OSS_INCLUDE_CPE, OSS_EXCLUDE_CPE, OSS_VERSION_ALIAS 테이블 추가**
  - 보안취약점 매칭 강화 데이터 관리를 위해 테이블이 추가되었습니다.

4. **LICENSE_NICKNAME 테이블의 Schema 변경**
  - LICENSE_ID를 사용하도록 Schema가 변경되었습니다.

5. **LICENSE_MASTER 테이블에 DISCLOSING_SRC column 추가**
  - DISCLOSING_SRC 정보를 위해 column이 추가되었습니다.

6. **PROJECT_MASTER 테이블에 column 추가**
  - Project 목록 화면 속도 개선을 위해 column이 추가되었습니다.


위의 내용을 참고하여 Migration script를 전체적으로 검토하시기 바랍니다.
Migration script를 실행하기 전에 반드시 데이터베이스를 백업하고, 
스크립트를 테스트 환경에서 먼저 실행하여 문제가 없는지 확인하시기 바랍니다.


## [2.0.1.pre-release](https://github.com/fosslight/fosslight/releases/tag/v2.0.1.pre-release) (2024-07-22)

### Changed
* fosslight_create.sql에서 잘못된 column 수정
* API V2의 버그 수정
  - 3rd party search API 반환 값 유형 변경
  - 소스 코드 분석 결과가 BIN 탭에 업로드되는 버그 수정
* 이메일 형식의 버그 수정
* 검토 보고서의 버그 수정
* 오픈소스 메뉴의 검색창 버그 수정
* SPDX 문서의 버그 수정


## [2.0.0.pre-release](https://github.com/fosslight/fosslight/releases/tag/v2.0.0.pre-release) (2024-07-02)

### New
* UI 2.0 릴리즈
  - UI 프레임워크를 Thymeleaf로 변경
  - 보다 직관적인 사용자 인터페이스로 개선
  - 업데이트된 UX 시나리오
  - UI 2.0에 대한 상세 정보는 [여기](https://fosslight.org/)에서 확인할 수 있습니다.

* Lite 릴리즈 (Thanks to @hjcdg1)
  - FOSSLight 시스템용 Lite 웹이 출시되었습니다.
  - 개인 사용자가 자가 점검을 위해 사용할 수 있도록 설계되었습니다.
  - 간단한 UI로 모바일 호환성을 지원합니다.

* **API v2 릴리즈** (Thanks to @cobaltblu27)
  - FOSSLight 시스템용 API v2가 출시되었습니다.
  - RESTful API 아키텍처로 전환되었습니다.
  - 응답 일관성이 개선되었습니다.


## [1.6.3](https://github.com/fosslight/fosslight/releases/tag/v1.6.3) (2024-05-21)
### New
* oss report API 들에 'sheetNames' parameter가 추가
  - /api/v1/oss_report_bin
  - /api/v1/oss_report_dep
  - /api/v1/oss_report_src
  - /api/v1/oss_report_selfcheck

### Changed
* 패키징 파일 업로드 가능 사이즈가 4GB에서 5GB로 변경
* DEP탭에서는 CheckOSSName 버튼을 사용할 수 없으므로, 버튼 UI disabled 처리
* Notice 파일의 Text Format에 open source homapage link 추가
* Distribution Type 이름 변경
* Mapper 값 변경:
  * Component 복사 시, DEPENDENCIES와 REF_OSS_NAME 추가
  * DEPENDENCIES Column의 data type을 text 로 변경

## [1.6.1](https://github.com/fosslight/fosslight/releases/tag/v1.6.1) (2023-11-23)

### New
* Project, 3rd party, Self-check에 watcher를 추가 할 수 있는 API Endpoint가 추가 (/api/v1/prj_watcher_add, /api/v1/partner_watcher_add, /api/v1/selfcheck_watcher_add)
* 3rd, src, bin 탭에 "N/A"로 체크할 수 있는 API Endpoint가 추가 (/api/v1/prj_not_applicable)
* Project, Self-check의 report 업로드 시 reset 여부를 선택할 수 있도록 parameter가 추가 (/api/v1/oss_report_src, /api/v1/oss_report_bin, /api/v1/oss_report_selfcheck)
* Yaml형식의 FOSSLight Report를 다운로드 받을 수 있는 버튼이 "Export"에 추가
* 버전 별로 다른 license를 가진 OSS가 시스템에 처음 저장이 될 때, 기존 저장된 detected license를 보여주는 팝업이 추가
* FOSSLight Dependency Scanner를 통해 분석한 결과를 작성할 수 있는 DEP 탭이 추가
  - "Dependencies" Column에 각 Package의 Relationship 정보가 추가
  - DEP 탭의 OSS Name에는 Rename 기능이 적용되지 않음
  - BOM 탭에서 Dependencies 아이콘을 클릭 시, Relationship 정보를 확인
  - SPDX 형식의 문서로 Export시, Relationship 정보가 포함
* CycloneDX 지원, Package 단계에서 notice 발급 양식으로 선택 가능, BOM 탭에서 CycloneDX 리포트를 발급 가능
* "admin check" 버튼 체크로, BOM 탭의 download location, homepage와 copyright 정보 수정 가능

### Changed
* Proejct, 3rd party에서 Export 시 모든 OSS가 고지 의무와 상관 없이 BOM에 출력
* OSS 보고서 양식이 업데이트
  - Dependency 분석 결과를 기록할 수 있는 "DEP" Sheet가 추가
  - Operating System 필드 및 Model Info sheet 내 카테고리 필드 자동 선택 양식 또한 최신 정보로 업데이트
* Notice를 login 전에 확인 가능
* "Not the same as property"의 워닝 메세지가 Copyright 열에 추가

## [1.6.0](https://github.com/fosslight/fosslight/releases/tag/v1.6.0) (2023-07-28)

### New
* 프로젝트에 Vulnerability가 검출된 OSS가 있는 경우, CVE ID별로 확인 및 조치 상태 관리할 수 있는 **[Security탭](https://fosslight.org/fosslight-guide/started/2_try/5_security.html)** 추가
* MyBatis Migrations를 이용하여 [DB 버전을 업그레이드](https://fosslight.org/fosslight-guide/features/3_maintenance.html#db-%EB%B2%84%EC%A0%84-%EC%97%85%EA%B7%B8%EB%A0%88%EC%9D%B4%EB%93%9C%ED%95%98%EA%B8%B0) 할 수 있습니다.
* 비밀번호 분실시, 비밀번호를 재설정 가능
* Self-Check 프로젝트를 Export 할 수 있는 API Endpoint([/api/v1/export_selfcheck](https://fosslight.org/fosslight-guide-en/features/2_rest_api.html#rest-api-list))가 추가
* License List에 **Bulk registration** 버튼 추가하여 여러 license를 spread sheet에 작성 후 업로드 하면 한번에 저장 가능
* 3rd party에 **OSS Bulk registration** 버튼 추가하여 여러 OSS를 spread sheet에 작성 후 업로드 하면 한번에 저장 가능
* Self-Check의 OSS Table에 Homepage 정보가 추가
* Identification tab의 3rd Party/Project Search에서 3rd Party/Project ID로 검색 할 수 있는 기능이 추가

### Changed
* FOSSLight report export시, BOM Tab에 취합되는 OSS 중 3rd Party Software에서 사용된 항목은 "From" Column에 "3rd-(3rd Party Name)" 양식으로 표기
* Project, 3rd Party, Self-Check에서 Exclude 체크된 OSS에 대해서 보안취약점이 표시되지 않고, List에서 Exclude OSS를 제외하고 Max Vulnerability Score가 표시

## [1.4.6](https://github.com/fosslight/fosslight/releases/tag/v1.4.6) (2022-07-22)
### New
* Project List에서 Change Division 버튼 추가
### Changed
* OSS Notice 발행시 기본적으로 License의 internal url 대신 Website를 출력

## [1.4.5](https://github.com/fosslight/fosslight/releases/tag/v1.4.5) (2022-07-15)
### Changed
* YAML 버튼 클릭시 출력되는 양식 최신화

## [1.4.3](https://github.com/fosslight/fosslight/releases/tag/v1.4.3) (2022-07-03)
### New
* Project의 Basic Information탭 > Model 정보 추가

## [1.4.2](https://github.com/fosslight/fosslight/releases/tag/v1.4.2) (2022-06-24)
### Changed
* API > bom compare 의 change data 포맷 변경

## [1.4.1](https://github.com/fosslight/fosslight/releases/tag/v1.4.1) (2022-06-17)
### Changed
* Spring boot version을 2.1.7에서 2.6.8로 변경
* OSS List에서 검색시 로딩 시간 단축

## [1.4.0](https://github.com/fosslight/fosslight/releases/tag/v1.4.0) (2022-06-03)
### Changed
* Java 버전을 8에서 11로 변경
### New
* Self-check > Check license 버튼 추가

## [1.3.9](https://github.com/fosslight/fosslight/releases/tag/v1.3.9) (2022-05-27)
### Fixed
* OSS bulk registration (안정화 버전)
    - OSS Name, nickname이 중복 저장되는 버그 수정

## [1.3.7](https://github.com/fosslight/fosslight/releases/tag/v1.3.7) (2022-05-13)
### Changed
* Check OSS name/License에서 DB의 OSS 정보를 찾을 때 Download location 매칭 방식 변경
    - Download location이 포함되었는 지 체크하는 방식 대신 완전 일치해야 매칭하도록 변경되었습니다.    
      ex. github.com/fosslight/fosslight 를 찾으면 github.com/fosslight/fosslight_util 와 매칭되는 버그가 수정되었습니다.

## [1.3.4](https://github.com/fosslight/fosslight/releases/tag/v1.3.4) (2022-04-22)
### New
* [OSS Details tab](https://fosslight.org/fosslight-guide-en/started/2_try/2_oss.html#oss-details-tab)에 **rename** 버튼 추가     
Rename 버튼은 OSS Name과 nickname을 모든 버전에 대해 일괄 변경합니다.

## [1.3.3](https://github.com/fosslight/fosslight/releases/tag/v1.3.3) (2022-04-15)
### New
* Menu에 통계 추가

## [1.3.0](https://github.com/fosslight/fosslight/releases/tag/v1.3.0) (2022-03-25)
### New
* OSS List에 **Bulk registration** 버튼 추가        
여러 OSS를 spread sheet에 작성 후 업로드하면 한번에 저장할 수 있습니다.  

### Fixed
* **Vulnerability List** > DB에 저장되지 않은 OSS에 대하여 CVE-ID로 검색 불가한 버그 수정

### Deprecated
* Identification 탭에서 OSS Bulk registration 버튼을 클릭시 unconfirmed 목록을 load 하는 방식 대신 spread sheet를 업로드하는 방식으로 교체 

## [1.2.34](https://github.com/fosslight/fosslight/releases/tag/v1.2.34) (2022-03-18)
### New
* Project를 복사할 때 복사할 status를 선택하는 기능 추가 (Status: Identification, Packaging)

## [1.2.33](https://github.com/fosslight/fosslight/releases/tag/v1.2.33) (2022-03-11)
### Changed
* Self-check > admin 기준 빨간색 warning message가 있다면 Obligation을 unclear 로 표시

## [1.2.32](https://github.com/fosslight/fosslight/releases/tag/v1.2.32) (2022-03-04)
### New
* 3rd Party List에 Vulnerability Score 추가

## [1.2.31](https://github.com/fosslight/fosslight/releases/tag/v1.2.31) (2022-02-25)
### Changed
* 3rd Party/Project의 division을 수정 가능하게 변경

## [1.2.29](https://github.com/fosslight/fosslight/releases/tag/v1.2.29) (2022-02-11)
### New
* 개인 설정 탭(메뉴에서 로그인 user 이름 클릭)에서 List별 검색 조건 설정 기능 추가

## [1.2.24](https://github.com/fosslight/fosslight/releases/tag/v1.2.24) (2021-12-31)
### New
* Self-check > URL 입력시 **[FOSSLight Scanner Service](https://github.com/fosslight/fosslight_scanner_service)** 연동
* 3rd Party > Check OSS Name/License 추가
* OSS Table 상단 **bulk edit** 버튼 추가
    - 여러 Row를 한번에 수정할 수 있습니다. 

## [1.2.22](https://github.com/fosslight/fosslight/releases/tag/v1.2.22) (2021-12-17)
### New
* Self-check > OSS Notice 발행 기능 추가
    - 등록되지 않은 OSS가 있더라도 Self-check에서 OSS Notice 발행 가능합니다. 단, OSS Notice 발행을 위하여 License는 모두 등록되어야 합니다. 
* Project의 Identification탭에 Check License 추가
### Changed
* Check OSS Name > 여러 OSS 이름 중 선택하는 기능 추가

## [1.2.21](https://github.com/fosslight/fosslight/releases/tag/v1.2.21) (2021-12-10)
### Fixed
* SPDX(json, yaml)가 OSS Notice 압축 파일에 포함되지 않는 버그 수정

## [1.2.19](https://github.com/fosslight/fosslight/releases/tag/v1.2.19) (2021-12-03)
### New
* "Create project for OSS Notice" 버튼 추가
     - 해당 3rd Party를 3rd Party 탭에 load한 Project를 생성하는 기능
* 사용자별 언어 세팅을 저장 기능 추가

## [1.2.17](https://github.com/fosslight/fosslight/releases/tag/v1.2.17) (2021-12-19)
### New
* Self-Check에 **Check License** 추가

## [1.2.15](https://github.com/fosslight/fosslight/releases/tag/v1.2.15) (2021-11-05)
### New
* License에 여러 webpage 입력 가능하도록 함
* OSS List에 OSS Type 검색 조건 추가

## [1.2.13](https://github.com/fosslight/fosslight/releases/tag/v1.2.13) (2021-10-27)
### New
* 3rd Party 업로드 포맷으로 SPDX Spread sheet 추가

## [1.2.12](https://github.com/fosslight/fosslight/releases/tag/v1.2.12) (2021-10-22)
### New
* Self-check/Project 업로드 포맷으로 SPDX Spread sheet 추가
* 3rd Party 업로드 포맷으로 csv 추가
* OSS Notice로 SPDX (json, yaml) 추가

## [1.2.11](https://github.com/fosslight/fosslight/releases/tag/v1.2.11) (2021-10-15)
### New
* 언어를 영어와 한국어 중 선택 가능하도록 함
* Self-check/Project 업로드 포맷으로 csv 추가

## [1.2.9](https://github.com/fosslight/fosslight/releases/tag/v1.2.9) (2021-10-01)
### Fixed
* 3rd Party/Project에서 "Show comments history"가 미동작하는 버그 수정

## [1.2.5](https://github.com/fosslight/fosslight/releases/tag/v1.2.5) (2021-09-10)
### New
* Check OSS Name > OSS 정보를 못 찾는 경우 Download location에 따라 추천 OSS Name을 표시

## [1.2.4](https://github.com/fosslight/fosslight/releases/tag/v1.2.4) (2021-09-03)
### New
* Docker 메일 서버를 docker-compose에 추가
* Restriction 아이콘 클릭시 license 정보 팝업을 띄움

## [1.2.3](https://github.com/fosslight/fosslight/releases/tag/v1.2.3) (2021-08-27)
### New
* OSS 상세 정보 탭 > synchronization 버튼 추가
    - synchronization는 OSS 정보를 버전별로 일괄 변경합니다. 

## [1.2.2](https://github.com/fosslight/fosslight/releases/tag/v1.2.2) (2021-08-20)
### New
* OSS 상세 정보 탭 > "deactivate" 추가
    - deactivate 체크한 OSS는 더 이상 사용하지 않음을 뜻합니다. 이에 OSS Table에 deactivate OSS를 작성하면 빨간색 warning message를 표시하고 Confirm이 불가합니다.  

## [1.2.0](https://github.com/fosslight/fosslight/releases/tag/v1.2.0) (2021-07-30)
### Changed
* 기존 OSS의 License를 **Declared license**로 변경하고 **Detected license**를 추가

## [1.0](https://github.com/fosslight/fosslight/releases/tag/v1.0) (2021-04-30)
> FOSSLight Hub v1.0 릴리즈
