package com.tistory.devyongsik.analyzer.util;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.junit.Assert;

import com.google.common.collect.Lists;


public class AnalyzerTestUtil {
	protected TestToken getToken(String term, int start, int end) {
		TestToken t = new TestToken();
		t.setTerm(term);
		t.setStartOffset(start);
		t.setEndOffset(end);
		
		return t;
	}
	
	protected void verify(List<TestToken> expectedTokens, List<TestToken> extractedTokens) {
		assertContainsTokens(expectedTokens, extractedTokens);
	}

	protected void assertContainsTokens(List<TestToken> expectedTokens, List<TestToken> extractedTokens) {
		for(TestToken testToken : expectedTokens) {
			Assert.assertTrue("Missing expected token " + formatToken(testToken) + "\nexpected: "
					+ formatTokens(expectedTokens) + "\nactual: " + formatTokens(extractedTokens),
					extractedTokens.contains(testToken));
		}
	}

	protected void assertTokensExactly(List<TestToken> expectedTokens, List<TestToken> extractedTokens) {
		Assert.assertEquals("Token list mismatch.\nexpected: " + formatTokens(expectedTokens)
				+ "\nactual: " + formatTokens(extractedTokens), expectedTokens, extractedTokens);
	}

	protected void assertTokensIgnoringOrder(List<TestToken> expectedTokens, List<TestToken> extractedTokens) {
		Map<TestToken, Integer> expectedCounts = countTokens(expectedTokens);
		Map<TestToken, Integer> actualCounts = countTokens(extractedTokens);

		Assert.assertEquals("Token multiset mismatch.\nexpected: " + formatTokenCounts(expectedCounts)
				+ "\nactual: " + formatTokenCounts(actualCounts), expectedCounts, actualCounts);
	}

	protected List<TestToken> collectExtractedNouns(TokenStream stream) throws IOException {
		CharTermAttribute charTermAtt = stream.addAttribute(CharTermAttribute.class);
		OffsetAttribute offSetAtt = stream.addAttribute(OffsetAttribute.class);

		List<TestToken> extractedTokens = Lists.newArrayList();

		while(stream.incrementToken()) {
			TestToken t = getToken(charTermAtt.toString(), offSetAtt.startOffset(), offSetAtt.endOffset());

			extractedTokens.add(t);
		}

		return extractedTokens;
	}

	private Map<TestToken, Integer> countTokens(List<TestToken> tokens) {
		Map<TestToken, Integer> counts = new LinkedHashMap<TestToken, Integer>();

		for(TestToken token : tokens) {
			Integer count = counts.get(token);
			counts.put(token, count == null ? 1 : count + 1);
		}

		return counts;
	}

	private String formatTokens(List<TestToken> tokens) {
		List<String> formattedTokens = Lists.newArrayList();

		for(TestToken token : tokens) {
			formattedTokens.add(formatToken(token));
		}

		return formattedTokens.toString();
	}

	private String formatTokenCounts(Map<TestToken, Integer> tokenCounts) {
		List<String> formattedTokens = Lists.newArrayList();

		for(Map.Entry<TestToken, Integer> entry : tokenCounts.entrySet()) {
			formattedTokens.add(formatToken(entry.getKey()) + " x " + entry.getValue());
		}

		return formattedTokens.toString();
	}

	private String formatToken(TestToken token) {
		return token.getTerm() + "(" + token.getStartOffset() + "," + token.getEndOffset() + ")";
	}
}
