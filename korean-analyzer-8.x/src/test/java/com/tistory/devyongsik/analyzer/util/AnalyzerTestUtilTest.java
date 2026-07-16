package com.tistory.devyongsik.analyzer.util;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class AnalyzerTestUtilTest extends AnalyzerTestUtil {

	@Test
	public void assertContainsTokensAllowsAdditionalActualTokens() {
		List<TestToken> expectedTokens = Arrays.asList(getToken("검색", 0, 2));
		List<TestToken> actualTokens = Arrays.asList(getToken("검색", 0, 2), getToken("엔진", 2, 4));

		assertContainsTokens(expectedTokens, actualTokens);
	}

	@Test
	public void assertContainsTokensReportsMissingToken() {
		AssertionError error = Assert.assertThrows(AssertionError.class, () -> assertContainsTokens(
				Arrays.asList(getToken("검색", 0, 2)), Arrays.asList(getToken("엔진", 2, 4))));

		Assert.assertTrue(error.getMessage().contains("Missing expected token 검색(0,2)"));
		Assert.assertTrue(error.getMessage().contains("actual: [엔진(2,4)]"));
	}

	@Test
	public void assertTokensExactlyRequiresSameOrderAndSize() {
		List<TestToken> expectedTokens = Arrays.asList(getToken("검색", 0, 2), getToken("엔진", 2, 4));
		List<TestToken> actualTokens = Arrays.asList(getToken("검색", 0, 2), getToken("엔진", 2, 4));

		assertTokensExactly(expectedTokens, actualTokens);
	}

	@Test
	public void assertTokensExactlyReportsOrderMismatch() {
		AssertionError error = Assert.assertThrows(AssertionError.class, () -> assertTokensExactly(
				Arrays.asList(getToken("검색", 0, 2), getToken("엔진", 2, 4)),
				Arrays.asList(getToken("엔진", 2, 4), getToken("검색", 0, 2))));

		Assert.assertTrue(error.getMessage().contains("Token list mismatch."));
	}

	@Test
	public void assertTokensIgnoringOrderAllowsDifferentOrder() {
		List<TestToken> expectedTokens = Arrays.asList(getToken("검색", 0, 2), getToken("엔진", 2, 4));
		List<TestToken> actualTokens = Arrays.asList(getToken("엔진", 2, 4), getToken("검색", 0, 2));

		assertTokensIgnoringOrder(expectedTokens, actualTokens);
	}

	@Test
	public void assertTokensIgnoringOrderReportsDuplicateMismatch() {
		AssertionError error = Assert.assertThrows(AssertionError.class, () -> assertTokensIgnoringOrder(
				Arrays.asList(getToken("검색", 0, 2), getToken("검색", 0, 2)),
				Arrays.asList(getToken("검색", 0, 2))));

		Assert.assertTrue(error.getMessage().contains("Token multiset mismatch."));
	}
}
