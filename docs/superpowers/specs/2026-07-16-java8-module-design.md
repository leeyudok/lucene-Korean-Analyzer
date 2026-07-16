# korean-analyzer-8.x 모듈 설계 (Java 8 / Lucene 8.11.4)

- 날짜: 2026-07-16
- 이슈: leeyudok/lucene-Korean-Analyzer#1
- 배경: 은행 운영 환경의 최대 Java 버전이 1.8. Lucene 9.x는 Java 11 이상을 요구하므로
  현재 `korean-analyzer-9.x`(Java 11 + Lucene 9.10.0)를 그대로 사용할 수 없다.

## 결정 사항

- **별도 모듈 방식**: `korean-analyzer-9.x`를 복사해 `korean-analyzer-8.x` 모듈 신설.
  업스트림(need4spd)이 원래 Lucene 메이저 버전별 모듈(`3.x`, `4.x`, ...)을 나란히 두던
  관례를 따른다. 9.x 모듈은 그대로 유지한다.
- **Lucene 8.11.4**: Java 8을 지원하는 마지막 Lucene 라인. 은행 측에 기존 Lucene
  제약이 없으므로 8.x 최신 패치를 사용한다.

## 구조

- `settings.gradle`에 `korean-analyzer-8.x` 추가.
- 루트 `build.gradle`의 Java 11 고정(sourceCompatibility/targetCompatibility)을
  모듈별로 이관: 9.x는 Java 11 유지, 8.x는 `options.release = 8`.
- 사전 리소스(`src/main/resources`)는 그대로 복사.

## 의존성 (8.x 모듈)

| 라이브러리 | 9.x 모듈 | 8.x 모듈 | 비고 |
|---|---|---|---|
| lucene-core 외 | 9.10.0 | 8.11.4 | `lucene-analysis-common` → `lucene-analyzers-common` (8.x 아티팩트명) |
| slf4j-api | 2.0.13 | 1.7.36 | 보수적 선택 (은행 환경 호환성) |
| logback | 1.5.6 | 1.2.13 | 1.3+는 Java 11 필요 |
| guava | 33.2.0-jre | 33.2.0-jre | Java 8 지원, 유지 |
| junit | 4.13.2 | 4.13.2 | 유지 |

## 코드 변환 포인트

- Java 9+ 문법 제거: `StringUtil.java` 1건 확인됨. 컴파일(`--release 8`)로 잔여 건 검출.
- Lucene 9 → 8 API 차이 해소: `Analyzer#createComponents`, `ByteBuffersDirectory`,
  `IndexWriterConfig`, `IndexOptions` 등 — 컴파일 에러 기준으로 하나씩 대응.
- artifactId: `korean-analyzer-8.x`, group/version은 9.x와 동일 유지.

## 검증

- `./gradlew :korean-analyzer-8.x:test` 전체 통과 (JDK 17로 구동, `--release 8` 컴파일)
- 클래스파일 major version 52(=Java 8) 확인 (`javap -verbose`)
- 9.x 모듈 빌드/테스트가 깨지지 않는지 확인
