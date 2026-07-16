package com.tistory.devyongsik.analyzer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tistory.devyongsik.analyzer.dictionary.DictionaryFactory;
import com.tistory.devyongsik.analyzer.util.AnalyzerTestUtil;
import com.tistory.devyongsik.analyzer.util.TestToken;

public class KoreanCompoundNounEngineTest extends AnalyzerTestUtil {
	private List<TestToken> compondNouns = Lists.newArrayList();
	private StringReader reader = new StringReader("월드컵조직위원회분과위");
	private KoreanCharacterTokenizer tokenizer = new KoreanCharacterTokenizer();
	private List<Engine> engines = new ArrayList<Engine>();
	private DictionaryFactory dictionaryFactory;

	@Before
	public void initDictionary() {
		compondNouns.add(getToken("월드컵조직위원회분과위", 0, 11));
		compondNouns.add(getToken("월드컵", 0, 3));
		compondNouns.add(getToken("조직", 3, 5));
		compondNouns.add(getToken("위원회", 5, 8));
		compondNouns.add(getToken("분과위", 8, 11));

		dictionaryFactory = DictionaryFactory.getFactory();
	}

	@Test
	public void testCompoundNounExtract() throws Exception {
		Map<String, List<String>> compoundNounDictionaryMap = Maps.newHashMap();
		List<String> compoundList = Lists.newArrayList();
		compoundList.add("분과위");
		compoundList.add("위원회");
		compoundList.add("조직");
		compoundList.add("월드컵");

		compoundNounDictionaryMap.put("월드컵조직위원회분과위", compoundList);

		dictionaryFactory.setCompoundDictionaryMap(compoundNounDictionaryMap);

		createEngines();

		tokenizer.setReader(reader);
		TokenStream stream = new KoreanNounFilter(tokenizer, engines);

		stream.reset();

		List<TestToken> extractedTokens = collectExtractedNouns(stream);

		stream.close();

		assertTokensExactly(compondNouns, extractedTokens);
	}

	@Test
	public void testCompoundNounExtractWithTokenOffset() throws Exception {
		Map<String, List<String>> compoundNounDictionaryMap = Maps.newHashMap();
		List<String> compoundList = Lists.newArrayList();
		compoundList.add("분과위");
		compoundList.add("위원회");
		compoundList.add("조직");
		compoundList.add("월드컵");
		compoundNounDictionaryMap.put("월드컵조직위원회분과위", compoundList);

		dictionaryFactory.setCompoundDictionaryMap(compoundNounDictionaryMap);

		List<Engine> localEngines = new ArrayList<Engine>();
		localEngines.add(new KoreanCompoundNounEngine());

		KoreanCharacterTokenizer localTokenizer = new KoreanCharacterTokenizer();
		localTokenizer.setReader(new StringReader("오늘 월드컵조직위원회분과위"));
		TokenStream stream = new KoreanNounFilter(localTokenizer, localEngines);
		stream.reset();

		List<TestToken> expectedTokens = Lists.newArrayList();
		expectedTokens.add(getToken("오늘", 0, 2));
		expectedTokens.add(getToken("월드컵조직위원회분과위", 3, 14));
		expectedTokens.add(getToken("월드컵", 3, 6));
		expectedTokens.add(getToken("조직", 6, 8));
		expectedTokens.add(getToken("위원회", 8, 11));
		expectedTokens.add(getToken("분과위", 11, 14));

		List<TestToken> extractedTokens = collectExtractedNouns(stream);

		stream.close();

		assertTokensExactly(expectedTokens, extractedTokens);
	}

	@Test
	public void testCompoundNounSkipsDictionaryPartsThatAreNotInToken() throws Exception {
		Map<String, List<String>> compoundNounDictionaryMap = Maps.newHashMap();
		List<String> compoundList = Lists.newArrayList();
		compoundList.add("월드컵");
		compoundList.add("없는명사");
		compoundNounDictionaryMap.put("월드컵조직위원회분과위", compoundList);

		dictionaryFactory.setCompoundDictionaryMap(compoundNounDictionaryMap);

		List<Engine> localEngines = new ArrayList<Engine>();
		localEngines.add(new KoreanCompoundNounEngine());

		KoreanCharacterTokenizer localTokenizer = new KoreanCharacterTokenizer();
		localTokenizer.setReader(new StringReader("오늘 월드컵조직위원회분과위"));
		TokenStream stream = new KoreanNounFilter(localTokenizer, localEngines);
		stream.reset();

		List<TestToken> extractedTokens = collectExtractedNouns(stream);

		stream.close();

		List<TestToken> expectedTokens = Lists.newArrayList();
		expectedTokens.add(getToken("오늘", 0, 2));
		expectedTokens.add(getToken("월드컵조직위원회분과위", 3, 14));
		expectedTokens.add(getToken("월드컵", 3, 6));

		assertTokensExactly(expectedTokens, extractedTokens);
	}

	private void createEngines() {
		engines.add(new KoreanCompoundNounEngine());
	}

}
