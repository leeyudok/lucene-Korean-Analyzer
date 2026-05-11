# lucene-Korean-Analyzer 프로젝트 분석

## 개요

이 저장소는 Apache Lucene용 한국어 Analyzer 라이브러리입니다. 이수명님의 한국어 Analyzer를 형태소 분석 Filter 형태로 변형하고, 동의어 Filter와 복합명사 Filter 등을 추가한 프로젝트입니다.

현재 구조는 Java 11과 Lucene 9.10.0 기준의 단일 모듈입니다.

```text
lucene-Korean-Analyzer/
├── build.gradle
├── settings.gradle
└── korean-analyzer-9.x/
```

## 기술 스택

- Java 11
- Gradle 8.7 wrapper
- Lucene 9.10.0
- SLF4J 2.0.13
- Logback 1.5.6
- Guava 33.2.0-jre
- JUnit 4.13.2

## 모듈 구성

`settings.gradle`은 `korean-analyzer-9.x` 하나만 포함합니다. 이전 Lucene 3.x 모듈은 제거되었고, 기존 `korean-analyzer-4.x` 모듈은 현재 Lucene 9.x 기준에 맞춰 `korean-analyzer-9.x`로 이름이 변경되었습니다.

`korean-analyzer-9.x/build.gradle`은 다음을 명시합니다.

- group: `com.tistory.devyongsik`
- version: `0.9-SNAPSHOT`
- artifactId: `korean-analyzer-9.x`
- archivesName: `korean-analyzer-9.x`

## 핵심 아키텍처

분석 파이프라인은 다음과 같습니다.

```text
입력 텍스트
→ KoreanCharacterTokenizer
→ KoreanNounFilter
   → KoreanCompoundNounEngine
   → KoreanBaseNounEngine
   → KoreanLongestNounEngine
   → KoreanSynonymEngine
   → KoreanMorphEngine
→ KoreanStopFilter
→ Lucene TokenStream
```

### `KoreanAnalyzer`

`com.tistory.devyongsik.analyzer.KoreanAnalyzer`가 전체 파이프라인을 구성합니다.

- 기본 생성자는 색인 모드입니다.
- `new KoreanAnalyzer(true)`는 색인 모드입니다.
- `new KoreanAnalyzer(false)`는 검색 모드입니다.

색인 모드에서는 다음 엔진을 모두 사용합니다.

- `KoreanCompoundNounEngine`
- `KoreanBaseNounEngine`
- `KoreanLongestNounEngine`
- `KoreanSynonymEngine`
- `KoreanMorphEngine`

검색 모드에서는 `KoreanBaseNounEngine`을 제외합니다. 색인 시에는 더 넓게 토큰을 생성하고, 검색 시에는 과도한 기본 명사 분해를 줄이려는 의도로 보입니다.

### `KoreanCharacterTokenizer`

Lucene 9의 `Tokenizer` API에 맞춰 Reader 생성자 없이 동작합니다. 입력 문장을 읽어 다음 기준으로 토큰을 생성합니다.

- 문자/숫자가 아닌 문자는 구분자 처리
- 한글, 영문, 숫자 타입이 바뀌면 토큰 분리
- 영문은 소문자로 정규화
- 각 토큰의 type은 `"word"`로 설정

### `KoreanNounFilter`

원본 토큰을 통과시키면서 여러 `Engine` 구현체가 추가로 추출한 토큰 상태를 보관했다가 순차적으로 반환합니다.

1. 이전 토큰에서 추출된 추가 토큰이 있으면 먼저 반환합니다.
2. 없으면 upstream token stream에서 새 원본 토큰을 읽습니다.
3. 각 엔진에 원본 토큰의 attribute clone을 넘겨 추가 토큰을 수집합니다.
4. 수집된 추가 토큰은 start offset 기준으로 정렬됩니다.
5. 원본 토큰을 반환한 뒤, 다음 `incrementToken()` 호출에서 추가 토큰을 반환합니다.

중복 방지는 `term_startOffset_endOffset` 형태의 키를 `returnedTokens` 맵에 넣어 처리합니다.

## 명사 추출 엔진

### `KoreanCompoundNounEngine`

복합명사 사전에 등록된 전체 단어를 찾고, 해당 단어에 매핑된 구성 명사들을 추가 토큰으로 생성합니다.

이번 정리에서 문장 중간에 등장하는 복합명사의 offset이 원본 토큰의 시작 offset을 반영하도록 보정했습니다.

### `KoreanBaseNounEngine`

사용자 정의 명사 사전을 기반으로 입력 토큰 내부에서 매칭되는 모든 명사 후보를 추출합니다. 색인 모드에서만 사용됩니다.

### `KoreanLongestNounEngine`

사용자 정의 명사 사전을 기반으로 입력 토큰 내부에서 가능한 가장 긴 명사 후보를 우선 추출합니다.

### `KoreanSynonymEngine`

동의어 사전을 Lucene 인메모리 인덱스로 색인한 뒤, 현재 토큰과 같은 동의어 그룹에 포함된 단어들을 position increment 0의 동의어 토큰으로 생성합니다.

동의어 인덱스는 `SynonymDictionaryIndex`에서 Lucene 9의 `ByteBuffersDirectory` 기반으로 관리합니다.

### `KoreanMorphEngine`

`org.apache.lucene.analysis.kr.morph.MorphAnalyzer`를 사용해 형태소 분석을 수행하고, 명사 분석 결과와 복합명사 후보를 추가 토큰으로 생성합니다.

## 사전 시스템

사전은 `DictionaryFactory` 싱글턴이 초기화 시점에 로드합니다.

주요 사전 파일은 다음과 같습니다.

- `compounds.txt`: 복합명사 사전
- `custom.txt`: 사용자 정의 명사 사전
- `stop.txt`: 불용어 사전
- `synonym.txt`: 동의어 사전
- `*.dic`: 형태소 분석용 사전

사전 경로는 `dictionary.properties`에서 읽습니다. 기본 설정 파일은 다음 경로에 있습니다.

```text
com/tistory/devyongsik/analyzer/dictionary.properties
```

현재 사전 파일들은 `src/main/java` 아래에 같이 들어 있으며, Gradle `copyDictionary` 태스크가 빌드 출력 디렉터리로 복사합니다.

이번 정리에서 사전 리소스가 누락된 경우 NPE 대신 명확한 `IllegalStateException`을 던지도록 보강했습니다.

## 빌드와 테스트

기본 검증 명령은 다음과 같습니다.

```bash
./gradlew clean test
```

이전에는 전체 테스트가 제거 예정인 3.x 모듈 때문에 실패했지만, 현재는 Lucene 9 모듈만 포함하도록 정리되었습니다.

## 주요 변경 사항

- `korean-analyzer-3.x` 모듈 제거
- `korean-analyzer-4.x`를 `korean-analyzer-9.x`로 rename
- Gradle include, artifactId, archivesName을 Lucene 9 naming에 맞게 정리
- `deploy_to_local_repo.sh`를 `maven-publish` 기반 명령으로 변경
- 복합명사 offset 계산 보정
- 사전 리소스 누락 예외 처리 보강
- `printStackTrace()` 제거 및 logger 기반 예외 전달
- README와 분석 문서를 Lucene 9 단일 모듈 기준으로 갱신

## 남은 개선 후보

- 테스트 유틸의 token 검증을 포함 여부 비교에서 전체 token list 비교로 강화
- 운영 코드의 과도한 info 로그 조정
- 사전 파일을 `src/main/resources`로 옮겨 `copyDictionary` 태스크 의존성을 줄이는 구조 검토
