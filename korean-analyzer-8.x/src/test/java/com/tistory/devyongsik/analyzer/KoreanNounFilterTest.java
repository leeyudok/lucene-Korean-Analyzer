package com.tistory.devyongsik.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.junit.Test;

public class KoreanNounFilterTest {

	@Test(expected = IllegalStateException.class)
	public void incrementTokenRejectsNullEngines() throws Exception {
		KoreanCharacterTokenizer tokenizer = new KoreanCharacterTokenizer();
		tokenizer.setReader(new StringReader("테스트"));
		TokenStream stream = new KoreanNounFilter(tokenizer, null);
		stream.reset();

		stream.incrementToken();
	}

	@Test
	public void incrementTokenReturnsOriginalTokenWhenEngineFails() throws Exception {
		KoreanCharacterTokenizer tokenizer = new KoreanCharacterTokenizer();
		tokenizer.setReader(new StringReader("테스트"));

		List<Engine> engines = new ArrayList<Engine>();
		engines.add(new Engine() {
			@Override
			public void collectNounState(AttributeSource attributeSource, List<ComparableState> comparableStateList,
					Map<String, String> returnedTokens) throws Exception {
				throw new Exception("boom");
			}
		});

		TokenStream stream = new KoreanNounFilter(tokenizer, engines);
		CharTermAttribute termAttr = stream.addAttribute(CharTermAttribute.class);
		stream.reset();

		assertTrue(stream.incrementToken());
		assertEquals("테스트", termAttr.toString());

		stream.close();
	}

	@Test
	public void incrementTokenReturnsQueuedEngineStateBeforeNextInputToken() throws Exception {
		KoreanCharacterTokenizer tokenizer = new KoreanCharacterTokenizer();
		tokenizer.setReader(new StringReader("원본 다음"));

		List<Engine> engines = new ArrayList<Engine>();
		engines.add(new Engine() {
			@Override
			public void collectNounState(AttributeSource attributeSource, List<ComparableState> comparableStateList,
					Map<String, String> returnedTokens) throws Exception {
				CharTermAttribute termAttr = attributeSource.getAttribute(CharTermAttribute.class);
				OffsetAttribute offsetAttr = attributeSource.getAttribute(OffsetAttribute.class);
				PositionIncrementAttribute positionAttr = attributeSource.getAttribute(PositionIncrementAttribute.class);
				TypeAttribute typeAttr = attributeSource.getAttribute(TypeAttribute.class);

				termAttr.setEmpty();
				termAttr.append("추가");
				offsetAttr.setOffset(0, 2);
				positionAttr.setPositionIncrement(0);
				typeAttr.setType("test_noun");

				ComparableState comparableState = new ComparableState();
				comparableState.setState(attributeSource.captureState());
				comparableState.setStartOffset(offsetAttr.startOffset());
				comparableStateList.add(comparableState);
			}
		});

		TokenStream stream = new KoreanNounFilter(tokenizer, engines);
		CharTermAttribute termAttr = stream.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAttr = stream.addAttribute(TypeAttribute.class);
		stream.reset();

		assertTrue(stream.incrementToken());
		assertEquals("원본", termAttr.toString());
		assertEquals("word", typeAttr.type());

		assertTrue(stream.incrementToken());
		assertEquals("추가", termAttr.toString());
		assertEquals("test_noun", typeAttr.type());

		assertTrue(stream.incrementToken());
		assertEquals("다음", termAttr.toString());

		stream.close();
	}
}
