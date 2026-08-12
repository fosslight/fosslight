<!--
Copyright (c) 2021 LG Electronics
SPDX-License-Identifier: AGPL-3.0-only
 -->

# PURL 작성 규칙

- PURL SPEC: https://github.com/package-url/purl-spec/blob/master/PURL-TYPES.rst
- purl-spec v1.0.1 기준으로 작성

## 공통 사항

- protocol을 제거한다 (`git+`, `sum:`, `com:`, `ssh:` 등) 또는 `://` 이전 문자열 모두 제거. leading `www.`도 제거한다.
  - 예) `https://www.github.com/org/repo` → `github.com/org/repo`
- Port number를 제거한다.
  - 예) `github.com:443/{org}/{repo}` → `github.com/{org}/{repo}`
- 제거된 URL의 호스트 타입이 Package Type 별 표준화 방식에 정의된 기본 repository url에 해당하는 지 확인한다
  - 해당하면 PURL 규칙에 맞게 PURL 생성
  - 해당하지 않으면 generic 타입으로 PURL 생성
- 마지막 `/`는 제거
- OSS name 및 download location에 separator character (purl > specification > Core specification) `:/@?=&#` 및 공백 포함된 경우, 퍼센트 인코딩되어야 하며 타입별로 대소문자 구분 여부 다름
  - 단, purl에서 qualifier (`?` 뒤에 `key=value`) 다음 규칙을 따름
    - **key**: 인코딩하지 않음, `a-z`, `0-9`, `.`, `-`, `_` 만 허용
    - **value**
      - 인코딩하지 않음: `.-_~`, `:`, `A-Z a-z 0-9`
      - 인코딩함:

        | 문자  | 인코딩  |
        | ----- | ------- |
        | `/` | `%2F` |
        | `@` | `%40` |
        | `?` | `%3F` |
        | `=` | `%3D` |
        | `&` | `%26` |
        | `#` | `%23` |
        | 공백  | `%20` |
      - 예)

        - `download_url=https:%2F%2Fopenssl.org%2Fsource%2Fopenssl-1.1.0g.tar.gz`
        - `repository_url=https:%2F%2Fmaven.google.com`
        - `vcs_url=git%2Bhttps:%2F%2Fgit.fsfe.org%2Fdxtr%2Fbitwarderl%40cc55108da32`
  - 그외
    - `:` → `%3A`
    - `/` → `%2F`
    - `@` → `%40`
    - `?` → `%3F`
    - `=` → `%3D`
    - `&` → `%26`
    - `#` → `%23`
    - 공백 → `%20`
- 대소문자 구별해야 함 → URL로부터 추출한 namespace 또는 name의 대소문자 유지

## Package Type 별 표준화 방식

### Github

- 기본 repository url
  - `https://github.com/{organization}/{repository}`
- syntax: `pkg:github/<namespace>/<name>`
- namespace
  - required
  - organization 정보
- name
  - required
  - repository 이름
- Purl 예)
  - `pkg:github/package-url/purl-spec`

### Npm

- 기본 repository url
  - `https://registry.npmjs.org/{package_name}`
  - `https://www.npmjs.com/package/{package_name}`
  - `https://www.npmjs.com/package/{@organization}/{package_name}`
- syntax: `pkg:npm/<namespace>/<name>`
- namespace
  - optional, 대소문자 구별해야 함
  - organization (항상 `@` prefix로 시작함, `@`는 `%40`으로 인코딩되어야 함)
- name
  - required, 대소문자 구별해야 함
  - package name
- Purl 예)
  - `pkg:npm/foobar`
  - `pkg:npm/%40angular/animation`

### Pypi

- 기본 repository url
  - `https://pypi.python.org/project/{package_name}`
  - `https://pypi.org/project/{package_name}`
  - `https://pypi.python.org/pypi/{package_name}`
  - `https://files.pythonhosted.org/packages/source/*/{package_name}/{package_name}-{version}.tar.gz`
- syntax: `pkg:pypi/<name>@<version>`
- name
  - required
  - python package 이름
  - Pypi에서는 `-`(dash)와 `_`(underscore)를 같은 것으로 판단하기 때문에 underscore는 dash로 치환
  - 소문자로 변경
- Purl 예)
  - `pkg:pypi/django`
  - `pkg:pypi/django-allauth`
  - `https://files.pythonhosted.org/packages/source/m/{package_name}/mcp-1.20.0.tar.gz`
    - `pkg:pypi/mcp`

### Maven

- 기본 repository url
  - `https://repo.maven.apache.org/maven2/{group id}/{artifact id}`
  - `https://mvnrepository.com/artifact/{group id}/{artifact id}`
  - `https://repo.maven.apache.org/`
  - `https://repo1.maven.org/maven2/`
  - `https://maven.google.com/web/index.html#{group id}/{artifact id}`에 대한 purl
    - `https://maven.google.com/web/index.html#androidx.test.ext:junit` → `pkg:maven/androidx.test.ext/junit?repository_url=https:%2F%2Fmaven.google.com`
  - `https://repo.spring.io/milestone/org/springframework/data/spring-data-r2dbc/1.5.0-M1/spring-data-r2dbc-1.5.0-M1-sources.jar` → `pkg:maven/org.springframework.data/spring-data-r2dbc?repository_url=https:%2F%2Frepo.spring.io`
  - `https://dl.google.com/android/maven2/androidx/test/ext/junit/1.2.0/junit-1.2.0.pom` → `pkg:maven/androidx.test.ext/junit?repository_url=https:%2F%2Fmaven.google.com`
  - Maven은 매우 다양한 레포지토리 주소를 가진다. 각각의 케이스에 대해서는 리뷰 요청 들어온 경우 추가하기로 결정.
    - `https://packages.atlassian.com/content/repositories/atlassian-public`
    - `https://repo.hortonworks.com/repository/releases`
    - `https://repo.spring.io/artifactory/libs-release`
    - `https://repository.jboss.org/nexus/content/repositories/releases`
    - `https://jitpack.io`
- syntax: `pkg:maven/<namespace>/<name>`
- namespace
  - required, 대소문자 구별해야 함
  - group id 정보
- name
  - required, 대소문자 구별해야 함
  - artifact id 정보
- Purl 예)
  - `pkg:maven/org.apache.xmlgraphics/batik-anim`
  - `pkg:maven/net.sf.jacob-projec/jacob`

### Cocoapods

- 기본 repository url
  - `https://cocoapods.org/pods/{pod name}`
  - `https://cdn.cocoapods.org/` → 형태 확인이 안됨. 일단 규칙에서 제외 필요.
- syntax: `pkg:cocoapods/<name>`
- name
  - required, 대소문자 구별해야 함
  - pod 이름
- Purl 예)
  - `pkg:cocoapods/Alamofire`
  - `pkg:cocoapods/MapsIndoors`
- 일반적으로 기본 레포지토리 형태로 구성되지 않고 `github.com/{org}/{name}` 형태로 구성된다. Github Package로 구성될 가능성이 높다.
  - 예) `https://cocoapods.org/pods/Alamofire#installation`

### Gem

- 기본 repository url
  - `https://rubygems.org/gems/{package name}`
- syntax: `pkg:gem/<name>`
- name
  - required
  - package name
- Purl 예)
  - `pkg:gem/ruby-advisory-db-check`
  - `pkg:gem/jruby-launcher`
  - `https://rubygems.org/gems/ropencv` → `pkg:gem/ropencv`

### Golang

- 기본 repository url
  - `https://pkg.go.dev/{namespace&name}`
  - pkg.go.dev url의 경우, `pkg.go.dev` 이후 url에 대해 namespace 및 name 포함된 값으로 처리
- syntax: `pkg:golang/<namespace>/<name>@<version>?<qualifiers>#<subpath>`
- namespace
  - required, 소문자로만 구성
  - ex) `google.golang.org`
- name
  - required, 소문자로만 구성
- subpath: 패키지 안에 특정 구성 요소를 지정할 때 사용
- Purl 예)
  - `pkg:golang/github.com/gorilla/context` : namespace, name 구성
  - `pkg:golang/google.golang.org/genproto#googleapis/api/annotations` : namespace, name, subpath 구성의 경우
  - `pkg:golang/google.golang.org/genproto`
    - namespace: `google.golang.org`, name: `genproto`, subpath: `googleapis/api/annotations`
- 일반적으로 `github.com` 등의 주소를 바로 사용하기 때문에 github package url로 구분될 가능성이 높다.

### Android

- purl-spec v1.0.1으로 정의되지 않음 → git url로 처리
- 기본 repository url
  - `https://android.googlesource.com/platform/{하위 URL 구성}`
  - `https://android.googlesource.com/platform/bionic/+/refs/tags/aml_tz3_312511020`와 같이 `+/` 제거
- syntax: `pkg:git/<namespace>/<name>@<version>?<qualifiers>#<subpath>`
- namespace, name
  - required, 대소문자 구별해야 함
  - git url에 대해 namespace & name으로 처리
- Purl 예)
  - `pkg:git/android.googlesource.com/platform/external/alsa-lib`
  - `pkg:git/android.googlesource.com/platform/bionic`

### Cargo

- 기본 repository url
  - `https://crates.io/crates/{package name}`
- syntax: `pkg:cargo/<name>`
- name
  - required, 대소문자 구별해야 함
  - package name
- Purl 예)
  - `pkg:cargo/rand`
  - `pkg:cargo/clap`

### Nuget

- 기본 repository url
  - `https://nuget.org/packages/{package name}`
  - `https://www.nuget.org/packages/{package name}`
- syntax: `pkg:nuget/<name>`
- name
  - required, 대소문자 구별해야 함
  - package name
- Purl 예)
  - `pkg:nuget/SocketIO.Serializer.Core`
  - `https://www.nuget.org/packages/Honoo.Net.UPnP` → `pkg:nuget/Honoo.Net.UPnP`

### Bitbucket

- 기본 repository url
  - `https://bitbucket.org/{user or organization}/{repository name}`
- syntax: `pkg:bitbucket/<namespace>/<name>`
- namespace
  - required, 소문자여야 함
  - the user or organization
- name
  - required, 소문자여야 함
  - repository name
- Purl 예)
  - `pkg:bitbucket/birkenfeld/pygments-main`
  - `https://bitbucket.org/blackteckel/job-board-tails-njs/get/v0.1.5.zip` → `pkg:bitbucket/blackteckel/job-board-tails-njs`

### Composer

- 기본 repository url
  - `https://packagist.org/packages/{vendor}/{name}`
- syntax: `pkg:composer/<namespace>/<name>`
- namespace
  - required, 소문자여야 함
  - vendor
- name
  - required, 소문자여야 함 (private 또는 로컬 패키지인 경우, name 존재하지 않으므로 purl 생성할 수 없음)
  - name
- Purl 예)
  - `pkg:composer/laravel/laravel`
  - `https://packagist.org/packages/geerlingguy/ping` → `pkg:composer/geerlingguy/ping`

### CPAN

- 기본 repository url
  - `https://www.cpan.org/`
  - `https://metacpan.org/pod/{name}`
- syntax: `pkg:cpan/<namespace>/<name>`
- namespace
  - Optional, 무조건 대문자여야 함
- name
  - required, 대소문자 구별해야 함
  - distribution name (CPAN 자체의 표준 명명 규칙에 따라 모듈의 구분자 `::`는 배포판 이름으로 변환될 때 항상 `-`로 치환)
- Purl 예)
  - `https://www.cpan.org/authors/id/O/OA/OALDERS/libwww-perl-6.18.tar.gz` → `pkg:cpan/libwww-perl`
  - `https://www.cpan.org/authors/id/I/IN/INA/Jacode4e/RoundTrip/Jacode4e-RoundTrip-2.13.81.6.tar.gz` → `pkg:cpan/Jacode4e-RoundTrip`

### CRAN

- 기본 repository url
  - `https://cran.r-project.org`
- syntax: `pkg:cran/<name>`
- name
  - required, 대소문자 구별해야 함
  - package name
- Purl 예)
  - `pkg:cran/rJava`
  - `https://cran.r-project.org/web/packages/ECharts2Shiny/index.html` → `pkg:cran/ECharts2Shiny`

### Docker

- 기본 repository url
  - `https://hub.docker.com`
- syntax: `pkg:docker/<namespace>/<name>`
- namespace
  - optional
  - registry/user/organization 존재하는 경우에만
- name
  - required
  - package name
- Purl 예)
  - `https://hub.docker.com/_/cassandra` → `pkg:docker/cassandra`
  - `https://hub.docker.com/r/bitnami/mariadb-galera` → `pkg:docker/bitnami/mariadb-galera`
  - `https://hub.docker.com/hardened-images/catalog/dhi/python` → `pkg:docker/python?repository_url=dhi.io`
    - `"^https?://hub\\.docker\\.com/hardened-images/catalog/dhi/([^/?#]+)/?$"` 인 경우, `repository_url=dhi.io`를 붙임

### Hackage

- 기본 repository url
  - `https://hackage.haskell.org/package/{package name}`
- syntax: `pkg:hackage/<name>`
- name
  - required, 대소문자 구별해야 함 (kebab-case normalization)
  - package name
- Purl 예)
  - `pkg:hackage/AC-HalfInteger`
  - `https://hackage.haskell.org/package/csg` → `pkg:hackage/csg`

### Huggingface

- 기본 repository url
  - `https://huggingface.co/{organization}/{repository name}`
- syntax: `pkg:huggingface/<namespace>/<name>`
- namespace
  - required, 대소문자 구별해야 함
  - model repository username or organization 존재하는 경우에만 (대소문자 구별해야 함)
- name
  - required, 대소문자 구별해야 함
  - model repository name
- Purl 예)
  - `https://huggingface.co/openai/whisper-small` → `pkg:huggingface/openai/whisper-small`
  - `https://huggingface.co/datasets/stanfordnlp/snli` → `pkg:huggingface/datasets/stanfordnlp/snli` (dataset에 대한 정확한 spec은 없음, spec은 모델에 대해서만 표준화되어 있음, 이에 datasets 추가하여 표시하도록 함)

### Yocto

- namespace: 생략 (optional). `layer.conf`의 `BBFILE_COLLECTIONS`를 읽지 않음. 경로의 `meta-*` 폴더명으로 추정하면 잘못된 값이 될 수 있음
- name: BPN (https://docs.yoctoproject.org/ref-manual/variables.html#term-BPN) in a yocto recipe (대소문자 구별해야 함)
- Purl 예) `pkg:yocto/<name>`
- ex. `https://git.openembedded.org/openembedded-core/tree/meta/recipes-core/gettext/gettext_1.0.bb`
  - `pkg:yocto/gettext?repository_url=https:%2F%2Fgit.openembedded.org%2Fopenembedded-core`
- URL에서 추출 방법
  1. 아래 중 하나이고 맨 끝이 `.bb`인 경우
     - url path에 `/meta*/`가 존재
     - 또는 OE-Classic repo: `https://github.com/openembedded/openembedded/`
       - ex. `https://github.com/openembedded/openembedded/blob/master/recipes/ncurses/ncurses_5.9.bb`
         → `pkg:yocto/ncurses?repository_url=https:%2F%2Fgithub.com%2Fopenembedded%2Fopenembedded`
  2. URL에서 파일명만 추출. ex. `gettext_1.0.bb`
  3. `_` 앞을 PN으로 추출 (`_` 없을 수도 있음)
  4. PN에서 접미/접두 제거 → BPN으로 변경 (대소문자 구별해야 함)
     - suffix 제거:
       - `-native`
       - `-cross`
       - `-initial`
       - `-intermediate`
       - `-crosssdk`
       - `-cross-canadian`
     - prefix 제거 (고정 목록 없음. class가 runtime에 설정)
       - `lib32-`
       - `lib64-`
       - `libx32-`
       - `nativesdk-`
     - ex. `binutils-cross` → `binutils`
  5. `repo_url`을 정제 (ex. `tree`, `blob` 이후 제거)하여 layer의 git repository url로 추출 (인코딩 필요)
  6. `pkg:yocto/{BPN}`으로 하고 `repository_url={repo_url}`을 붙임.

### Git

- 링크가 git 저장소인 경우
- ex. `.git`으로 끝나거나 `git://` 그 외는 `git ls-remote <url>`를 이용하여 git인지 체크
- namespace: The url path to the git host 대소문자 구별해야 함
- name: repository name with owner 대소문자 구별해야 함
- ex.
  - `pkg:git/codeberg.org/forgejo/forgejo`
  - `pkg:git/cygwin.com/cgit/newlib-cygwin`
  - `pkg:git/projects.blender.org/blender/blender`
  - `pkg:git/gitlab.gnome.org/GNOME/adwaita-fonts`
  - `https://git.codelinaro.org/clo/la/platform/external/volley` → `pkg:git/git.codelinaro.org/clo/la/platform/external/volley`
  - `https://source.codeaurora.org/external/imx/weston-imx/` → `pkg:git/source.codeaurora.org/external/imx/weston-imx`
- 특이 사항. repo 끝 git을 허용하고 있음. (우리는 끝 `.git` 제거해야 함. 그래야 purl 일치 체크)

### Generic

- 위 사항이 아닌 경우 모두 generic type으로 PURL 생성
- syntax: `pkg:generic/<namespace>/<name>@<version>?<qualifiers>#<subpath>`
- namespace: optional이므로 사용하지 않음
- name: OSS name
- qualifiers: `download_url` 선택 후, Download location 정보
- Purl 예)
  - `pkg:generic/<OSS name>?download_url=<Download location>`
  - `pkg:generic/openssl?download_url=https:%2F%2Fopenssl.org%2Fsource%2Fopenssl-1.1.0g.tar.gz`
