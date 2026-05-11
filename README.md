lucene-Korean-Analyzer
======================

Lucene Analyzer For Korean

이수명님의 Analyzer(http://cafe.naver.com/korlucene)를 형태소 분석 Filter 형태로 변형하고, 동의어 Filter와 복합명사 Filter 등을 추가한 Lucene용 한글 분석기입니다.

현재 프로젝트는 Java 11과 Lucene 9.10.0 기준의 단일 모듈로 관리합니다.

## Module

```text
korean-analyzer-9.x
```

## Build

```bash
./gradlew clean test
```

## Resources

Analyzer dictionaries and properties live under `korean-analyzer-9.x/src/main/resources` and are included by Gradle's standard resource processing.

## Analyzer Pipeline

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
