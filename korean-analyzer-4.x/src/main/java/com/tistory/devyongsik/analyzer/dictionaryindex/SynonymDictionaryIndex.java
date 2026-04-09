package com.tistory.devyongsik.analyzer.dictionaryindex;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynonymDictionaryIndex {

	private Directory ramDirectory = new ByteBuffersDirectory();
	private SearcherManager searcherManager = null;
	private Logger logger = LoggerFactory.getLogger(SynonymDictionaryIndex.class);

	private static SynonymDictionaryIndex indexingModule = new SynonymDictionaryIndex();

	private IndexWriter indexWriter = null;

	private SynonymDictionaryIndex() {
		try {
			Analyzer analyzer = new SimpleAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);

			indexWriter = new IndexWriter(ramDirectory, iwc);
			searcherManager = new SearcherManager(indexWriter, new SearcherFactory());

		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException();
		}
	}

	public static SynonymDictionaryIndex getIndexingModule() {
		return indexingModule;
	}

	public SearcherManager getSearcherManager() {
		return searcherManager;
	}

	public synchronized void indexingDictionary(List<String> synonyms) {

		try {
			indexWriter.deleteAll();
			indexWriter.commit();

			int recordCnt = 0;
			// 동의어들을 ,로 잘라내어 색인합니다.
			// 하나의 document에 syn이라는 이름의 필드를 여러개 추가합니다.
			// 나중에 syn=노트북 으로 검색한다면 그때 나온 결과 Document로부터
			// 모든 동의어 리스트를 얻을 수 있습니다.

			FieldType fieldType = new FieldType();
			fieldType.setStored(true);
			fieldType.setIndexOptions(IndexOptions.DOCS);
			fieldType.setTokenized(false);
			fieldType.freeze();

			for (String syn : synonyms) {
				String[] synonymWords = syn.split(",");
				Document doc = new Document();
				for (int i = 0, size = synonymWords.length; i < size; i++) {
					String fieldValue = synonymWords[i];
					Field field = new Field("syn", fieldValue, fieldType);
					doc.add(field);
					recordCnt++;
				}
				indexWriter.addDocument(doc);
			}

			indexWriter.commit();

			logger.info("동의어 색인 단어 갯수 : {}", recordCnt);

		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}
}
