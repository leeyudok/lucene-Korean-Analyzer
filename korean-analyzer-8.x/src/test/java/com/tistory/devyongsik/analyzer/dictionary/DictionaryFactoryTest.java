package com.tistory.devyongsik.analyzer.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

public class DictionaryFactoryTest {

	@Test
	public void loadDictionary() {
		DictionaryFactory factory = DictionaryFactory.getFactory();
		List<String> readWords = factory.getSynonymList();

		Assert.assertTrue(readWords.size() > 0);
	}

	@Test
	public void rebuildCompoundDictionaryRefreshesCompoundMap() {
		DictionaryFactory factory = DictionaryFactory.getFactory();
		List<String> compoundDictionary = factory.get(DictionaryType.COMPOUND);
		Map<String, List<String>> originalCompoundMap = new HashMap<String, List<String>>(factory.getCompoundDictionaryMap());
		String compoundEntry = "커버리지복합명사:커버리지,복합명사";

		try {
			compoundDictionary.add(compoundEntry);
			factory.getCompoundDictionaryMap().remove("커버리지복합명사");

			factory.rebuildDictionary(DictionaryType.COMPOUND);

			Assert.assertEquals(2, factory.getCompoundDictionaryMap().get("커버리지복합명사").size());
			Assert.assertTrue(factory.getCompoundDictionaryMap().get("커버리지복합명사").contains("커버리지"));
			Assert.assertTrue(factory.getCompoundDictionaryMap().get("커버리지복합명사").contains("복합명사"));
		} finally {
			compoundDictionary.remove(compoundEntry);
			factory.setCompoundDictionaryMap(originalCompoundMap);
		}
	}

	@Test
	public void rebuildCustomDictionaryRefreshesCustomMap() {
		DictionaryFactory factory = DictionaryFactory.getFactory();
		List<String> customDictionary = factory.get(DictionaryType.CUSTOM);
		Map<String, String> originalCustomMap = new HashMap<String, String>(factory.getCustomNounDictionaryMap());
		String customEntry = "커버리지사용자명사";

		try {
			customDictionary.add(customEntry);
			factory.getCustomNounDictionaryMap().remove(customEntry);

			factory.rebuildDictionary(DictionaryType.CUSTOM);

			Assert.assertTrue(factory.getCustomNounDictionaryMap().containsKey(customEntry));
		} finally {
			customDictionary.remove(customEntry);
			factory.setCustomNounDictionaryMap(originalCustomMap);
		}
	}

	@Test
	public void rebuildStopDictionaryRefreshesStopMap() {
		DictionaryFactory factory = DictionaryFactory.getFactory();
		List<String> stopDictionary = factory.get(DictionaryType.STOP);
		Map<String, String> originalStopMap = new HashMap<String, String>(factory.getStopWordDictionaryMap());
		String stopEntry = "커버리지불용어";

		try {
			stopDictionary.add(stopEntry);
			factory.getStopWordDictionaryMap().remove(stopEntry);

			factory.rebuildDictionary(DictionaryType.STOP);

			Assert.assertTrue(factory.getStopWordDictionaryMap().containsKey(stopEntry));
		} finally {
			stopDictionary.remove(stopEntry);
			factory.setStopWordDictionaryMap(originalStopMap);
		}
	}

	@Test
	public void settersReplaceDictionaryViews() {
		DictionaryFactory factory = DictionaryFactory.getFactory();
		Map<String, List<String>> originalCompoundMap = factory.getCompoundDictionaryMap();
		Map<String, String> originalCustomMap = factory.getCustomNounDictionaryMap();
		Map<String, String> originalStopMap = factory.getStopWordDictionaryMap();
		List<String> originalSynonymList = factory.getSynonymList();

		Map<String, List<String>> compoundMap = new HashMap<String, List<String>>();
		List<String> compoundParts = new ArrayList<String>();
		compoundParts.add("부분");
		compoundMap.put("전체", compoundParts);
		Map<String, String> customMap = new HashMap<String, String>();
		customMap.put("사용자", null);
		Map<String, String> stopMap = new HashMap<String, String>();
		stopMap.put("불용어", null);
		List<String> synonymList = new ArrayList<String>();
		synonymList.add("가,나");

		try {
			factory.setCompoundDictionaryMap(compoundMap);
			factory.setCustomNounDictionaryMap(customMap);
			factory.setStopWordDictionaryMap(stopMap);
			factory.setSynonymList(synonymList);

			Assert.assertSame(compoundMap, factory.getCompoundDictionaryMap());
			Assert.assertSame(customMap, factory.getCustomNounDictionaryMap());
			Assert.assertSame(stopMap, factory.getStopWordDictionaryMap());
			Assert.assertSame(synonymList, factory.getSynonymList());
		} finally {
			factory.setCompoundDictionaryMap(originalCompoundMap);
			factory.setCustomNounDictionaryMap(originalCustomMap);
			factory.setStopWordDictionaryMap(originalStopMap);
			factory.setSynonymList(originalSynonymList);
		}
	}
}
