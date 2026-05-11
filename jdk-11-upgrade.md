# Java 11 / Lucene 9 마이그레이션 기록

이 문서는 Java 11과 Lucene 9.x 기준으로 정리된 현재 상태를 기록합니다.

## 현재 상태

- Gradle wrapper: 8.7
- Java source/target compatibility: 11
- 활성 모듈: `korean-analyzer-9.x`
- Lucene: 9.10.0
- SLF4J: 2.0.13
- Logback: 1.5.6
- Guava: 33.2.0-jre
- JUnit: 4.13.2

## 완료된 정리

- 구 Lucene 3.x 모듈 제거
- 기존 Lucene 9 호환 모듈을 `korean-analyzer-9.x`로 rename
- Gradle `maven-publish` 기반 배포 설정 사용
- Lucene 9의 `Analyzer#createComponents(String)` 시그니처 사용
- Lucene 9의 `ByteBuffersDirectory`, `IndexWriterConfig`, `IndexOptions` API 사용
- `logback.xml` 기반 로깅 설정 사용
- 사전 리소스 누락 시 명확한 예외 처리 추가
- 복합명사 offset 계산 보정

## 검증 명령

```bash
./gradlew clean test
```

## 참고

Lucene 9.x는 Java 11 이상을 요구합니다. 따라서 이 저장소는 Java 11을 기준으로 유지하는 것이 자연스럽습니다.
