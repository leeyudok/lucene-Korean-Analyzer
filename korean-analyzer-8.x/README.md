korean-analyzer-8.x
===================

Lucene Korean analyzer for Lucene 8.x — **Java 8 제약 환경용** (예: 은행 등 JDK 1.8이 최대인 운영 환경)

`korean-analyzer-9.x`(Java 11 / Lucene 9.10.0)와 동일한 소스를 Lucene 8.11.4 + Java 8 바이트코드(`--release 8`, class major version 52)로 빌드한 모듈입니다. Lucene 8.11.4는 Java 8을 지원하는 마지막 Lucene 라인입니다.

## Build

```bash
./gradlew :korean-analyzer-8.x:jar
```

산출물: `korean-analyzer-8.x/target/libs/korean-analyzer-8.x-0.9-SNAPSHOT.jar`

테스트:

```bash
./gradlew :korean-analyzer-8.x:test
```

## Deployment (runtime dependencies)

이 jar에는 **이 프로젝트의 코드와 사전 리소스만** 들어 있습니다. 의존 라이브러리는 포함되지 않으므로, 사용하는 애플리케이션의 classpath에 아래 jar들이 함께 있어야 합니다.

필수:

| jar | 버전 | 용도 |
|---|---|---|
| `lucene-core` | 8.11.4 | Lucene 코어 |
| `lucene-analyzers-common` | 8.11.4 | Lucene 분석기 공통 모듈 |
| `slf4j-api` | 1.7.36 | 로깅 인터페이스 |

사용 기능에 따라 추가:

| jar | 버전 | 용도 |
|---|---|---|
| `lucene-queries` / `lucene-queryparser` | 8.11.4 | 쿼리 관련 기능 사용 시 |
| `guava` | 33.2.0-jre | 동의어 사전 인덱스 등 내부 사용 |
| `logback-core` / `logback-classic` | 1.2.13 | SLF4J 구현체가 따로 없을 때 (앱에 이미 log4j 등 다른 SLF4J 바인딩이 있으면 생략) |

주의사항:

- **Maven/Gradle 프로젝트에 넣는 경우**: 위 표를 신경 쓸 필요 없이 의존성 선언만 하면 transitive로 해결됩니다.
- **jar 직접 복사 배포**(WAS `lib/` 폴더 등)인 경우: 위 jar들을 함께 복사해야 합니다. 전부 Maven Central에서 받을 수 있습니다.
- **버전 충돌**: 대상 애플리케이션에 이미 다른 버전의 Lucene이 classpath에 있으면 충돌합니다. 반드시 8.11.4로 통일하거나 격리(별도 classloader)해야 합니다.
- SLF4J는 1.7 라인을 사용합니다 (2.x는 바인딩 방식이 달라 1.7 기반 앱과 섞이면 로깅이 동작하지 않을 수 있음).

## 참고

- 상위 프로젝트 README: [`../README.md`](../README.md)
- 설계 문서: [`../docs/superpowers/specs/2026-07-16-java8-module-design.md`](../docs/superpowers/specs/2026-07-16-java8-module-design.md)
- 원본 프로젝트: https://github.com/need4spd/lucene-Korean-Analyzer
