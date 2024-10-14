<!--
Copyright (c) 2022 LG Electronics
SPDX-License-Identifier: AGPL-3.0-only
 -->
<p align='right'>
  <a href="https://github.com/fosslight/fosslight_system/blob/main/RELEASE_NOTES.md">[Eng]</a>
</p>

## [2.0.2](https://github.com/fosslight/fosslight/releases/tag/v2.0.2) (2024-10-14)

### Changed
* **Save과정에서 발생하던 에러 수정 **
 - grid data 저장 과정에서 license 취합 시 component id 를 setting 는 부분에서 신규 입력된 경우에 대한 id설정 에러 수정
 - identificatoin save시 foreign key로 인해 발생하던 이슈 수정 - table에서 foreign key 삭제됨
* **속도 개선** *
- Project, 3rd party, self-check save 과정 속도 개선
* **Packaging 버그 및 기능 추가**
 - Project copy 이후 Packaging verify에서 발생하던 버그 수정
 - Packaging 과정에서 path에 공란 입력 불가능하도록 변경
* **Comment 버그 및 기능 추가**
 - confirm comment 메세지가 있으면 confirm 진행 안되던 버그 수정 
 - admin이 confirm / reject 할때 draft comment를 불러옵니다
* **Security탭 버그 및 기능 수정**
 - Security tab에서 검출된 vulnerability가 없는 경우 에러 수정
* 2.0.0 버전 release note 수정
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
