package com.tistory.devyongsik.analyzer;



import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.tistory.devyongsik.analyzer.util.AnalyzerTestUtil;
import com.tistory.devyongsik.analyzer.util.TestToken;

/**
 *
 * @author 장용석, 2011.07.16 need4spd@naver.com
 */

public class KoreanCharacterTokenizerTest extends AnalyzerTestUtil {

	private List<TestToken> tokenizedToken = Lists.newArrayList();
	private StringReader content = new StringReader("삼성전자absc1234엠피3mp3버전1.2  띄어쓰기");
	private KoreanCharacterTokenizer tokenizer = new KoreanCharacterTokenizer();

	@Before
	public void setUp() throws IOException {
		tokenizedToken.add(getToken("삼성전자", 0, 4));
		tokenizedToken.add(getToken("absc", 4, 8));
		tokenizedToken.add(getToken("1234", 8, 12));
		tokenizedToken.add(getToken("엠피", 12, 14));
		tokenizedToken.add(getToken("3", 14, 15));
		tokenizedToken.add(getToken("mp", 15, 17));
		tokenizedToken.add(getToken("3", 17, 18));
		tokenizedToken.add(getToken("버전", 18, 20));
		tokenizedToken.add(getToken("1", 20, 21));
		tokenizedToken.add(getToken("2", 22, 23));
		tokenizedToken.add(getToken("띄어쓰기", 25, 29));

		tokenizer.setReader(content);
		tokenizer.reset();
	}

	@Test
	public void testIncrementToken() throws IOException {
		List<TestToken> extractedTokens = collectExtractedNouns(tokenizer);

		assertTokensExactly(tokenizedToken, extractedTokens);
	}
}
