<!--
Copyright (c) 2022 LG Electronics
SPDX-License-Identifier: AGPL-3.0-only
 -->
<p align='right'>
  <a href="https://github.com/fosslight/fosslight_system/blob/main/RELEASE_NOTES.md">[Eng]</a>
</p>

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