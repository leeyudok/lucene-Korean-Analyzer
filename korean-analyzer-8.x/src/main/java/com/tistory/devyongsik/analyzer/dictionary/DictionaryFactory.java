package com.tistory.devyongsik.analyzer.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tistory.devyongsik.analyzer.DictionaryProperties;
import com.tistory.devyongsik.analyzer.dictionaryindex.SynonymDictionaryIndex;

public class DictionaryFactory {
	private Logger logger = LoggerFactory.getLogger(DictionaryFactory.class);

	private static DictionaryFactory factory = new DictionaryFactory();

	private Map<String, List<String>> compoundDictionaryMap = new HashMap<String, List<String>>();
	private Map<String, String> customNounDictionaryMap = new HashMap<String, String>();
	private Map<String, String> stopWordDictionaryMap = new HashMap<String, String>();
	private List<String> synonymList = new ArrayList<String>();

	private Map<DictionaryType, List<String>> dictionaryMap = new HashMap<DictionaryType, List<String>>();

	public static DictionaryFactory getFactory() {
		return factory;
	}

	private DictionaryFactory() {
		initDictionary();
	}

	private void initDictionary() {
		DictionaryLoader dictionaryLoader = new DictionaryLoader();
		dictionaryLoader.loadDictionaries();
	}

	public List<String> get(DictionaryType dictionaryType) {
		return dictionaryMap.get(dictionaryType);
	}

	public List<String> getSynonymList() {
		return synonymList;
	}

	public void setSynonymList(List<String> synonymList) {
		this.synonymList = synonymList;
	}

	public Map<String, List<String>> getCompoundDictionaryMap() {
		return compoundDictionaryMap;
	}

	public void setCompoundDictionaryMap(
			Map<String, List<String>> compoundDictionaryMap) {
		this.compoundDictionaryMap = compoundDictionaryMap;
	}

	public Map<String, String> getCustomNounDictionaryMap() {
		return customNounDictionaryMap;
	}

	public void setCustomNounDictionaryMap(
			Map<String, String> customNounDictionaryMap) {
		this.customNounDictionaryMap = customNounDictionaryMap;
	}

	public Map<String, String> getStopWordDictionaryMap() {
		return stopWordDictionaryMap;
	}

	public void setStopWordDictionaryMap(Map<String, String> stopWordDictionaryMap) {
		this.stopWordDictionaryMap = stopWordDictionaryMap;
	}

	class DictionaryLoader {

		public void loadDictionaries() {
			DictionaryType[] dictionaryTypes = DictionaryType.values();

			for(DictionaryType dictionaryType : dictionaryTypes) {
				if(logger.isDebugEnabled()) {
					logger.debug("[{}] create wordset from file", dictionaryType.getDescription());
				}

				List<String> dictionary = loadDictionary(dictionaryType);
				dictionaryMap.put(dictionaryType, dictionary);
			}

			List<String> dictionaryData = dictionaryMap.get(DictionaryType.COMPOUND);
			String[] extractKey = null;
			String key = null;
			String[] nouns = null;

			for(String data : dictionaryData) {
				extractKey = data.split(":");
				key = extractKey[0];
				nouns = extractKey[1].split(",");

				compoundDictionaryMap.put(key, Arrays.asList(nouns));
			}

			List<String> customNouns = dictionaryMap.get(DictionaryType.CUSTOM);
			for(String noun : customNouns) {
				customNounDictionaryMap.put(noun, null);
			}

			synonymList = dictionaryMap.get(DictionaryType.SYNONYM);

			List<String> stopWords = dictionaryMap.get(DictionaryType.STOP);
			for(String stopWord : stopWords) {
				stopWordDictionaryMap.put(stopWord, null);
			}
		}

		private List<String> loadDictionary(DictionaryType name) {

			String dictionaryFile = DictionaryProperties.getInstance().getProperty(name.getPropertiesKey());
			List<String> words = new ArrayList<String>();

			try (
					InputStream inputStream = openResource(dictionaryFile);
					BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
			) {
				String readWord = "";

				while( (readWord = in.readLine()) != null ) {
					words.add(readWord.trim());
				}

				if(logger.isDebugEnabled()) {
					logger.debug("{} : {}", name.getDescription(), words.size());
				}

				if(logger.isDebugEnabled()) {
					logger.debug("create wordset from file complete");
				}

			}catch(IOException e){
				throw new IllegalStateException("Failed to load dictionary resource: " + dictionaryFile, e);
			}

			return words;
		}

		private InputStream openResource(String dictionaryFile) {
			InputStream inputStream = DictionaryFactory.class.getClassLoader().getResourceAsStream(dictionaryFile);

			if(inputStream == null) {
				throw new IllegalStateException("Dictionary resource was not found: " + dictionaryFile);
			}

			return inputStream;
		}
	}

	public void rebuildDictionary(DictionaryType dictionaryType) {

		if(DictionaryType.CUSTOM == dictionaryType) {
			List<String> customNouns = dictionaryMap.get(DictionaryType.CUSTOM);
			customNounDictionaryMap.clear();
			for(String noun : customNouns) {
				customNounDictionaryMap.put(noun, null);
			}

			return;
		}

		if(DictionaryType.COMPOUND == dictionaryType) {
			List<String> dictionaryData = dictionaryMap.get(DictionaryType.COMPOUND);
			compoundDictionaryMap.clear();
			for(String data : dictionaryData) {
				String[] extractKey = data.split(":");
				String[] nouns = extractKey[1].split(",");
				compoundDictionaryMap.put(extractKey[0], Arrays.asList(nouns));
			}
		}

		if(DictionaryType.STOP == dictionaryType) {
			List<String> stopWords = dictionaryMap.get(DictionaryType.STOP);
			stopWordDictionaryMap.clear();
			for(String stopWord : stopWords) {
				stopWordDictionaryMap.put(stopWord, null);
			}
		}

		if(DictionaryType.SYNONYM == dictionaryType) {
			List<String> synonymWords = dictionaryMap.get(DictionaryType.SYNONYM);
			SynonymDictionaryIndex indexModule = SynonymDictionaryIndex.getIndexingModule();
			indexModule.indexingDictionary(synonymWords);
		}
	}
}
