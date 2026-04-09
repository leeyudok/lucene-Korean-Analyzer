package com.tistory.devyongsik.analyzer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 입력되는 문장을 읽어 Token으로 만들어 return
 * split 기준은 not char, not digit, 그리고 한글/영문/숫자 경계
 *
 * Lucene 9.x 호환으로 재작성 (Reader 파라미터 제거, CharacterUtils 제거)
 *
 * @author 장용석, need4spd@naver.com
 */
public class KoreanCharacterTokenizer extends Tokenizer {

	private Logger logger = LoggerFactory.getLogger(KoreanCharacterTokenizer.class);

	private int offset = 0, bufferIndex = 0, dataLen = 0, finalOffset = 0;
	private static final int MAX_WORD_LEN = 255;
	private static final int IO_BUFFER_SIZE = 4096;

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
	private final PositionIncrementAttribute positionAtt = addAttribute(PositionIncrementAttribute.class);

	private final char[] ioBuffer = new char[IO_BUFFER_SIZE];

	private int preChar = 0;
	private int preCharType = 99;
	private int nowCharType = 99;

	private final int DIGIT = 0;
	private final int KOREAN = 1;
	private final int ALPHA = 2;

	protected boolean isTokenChar(int c) {
		return Character.isLetterOrDigit(c);
	}

	protected int normalize(int c) {
		return Character.toLowerCase(c);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		clearAttributes();

		logger.debug("incrementToken");

		int length = 0;
		int start = -1;
		int end = -1;
		char[] buffer = termAtt.buffer();

		while (true) {
			if (bufferIndex >= dataLen) {
				offset += dataLen;
				dataLen = input.read(ioBuffer, 0, IO_BUFFER_SIZE);

				if (dataLen <= 0) {
					dataLen = 0;
					if (length > 0) {
						break;
					} else {
						finalOffset = correctOffset(offset);
						return false;
					}
				}
				bufferIndex = 0;
			}

			final int c = ioBuffer[bufferIndex];
			bufferIndex++;

			if (isTokenChar(c)) {
				// 이전 문자와 현재 문자의 종류가 다르면 토큰을 분리
				if (length > 0) {
					if (Character.isDigit(preChar)) preCharType = this.DIGIT;
					else if (preChar < 127) preCharType = this.ALPHA;
					else preCharType = this.KOREAN;

					if (Character.isDigit(c)) nowCharType = this.DIGIT;
					else if (c < 127) nowCharType = this.ALPHA;
					else nowCharType = this.KOREAN;

					if (preCharType != nowCharType) {
						bufferIndex--;  // 현재 문자를 다음 토큰으로 돌려놓음
						termAtt.setLength(length);
						offsetAtt.setOffset(correctOffset(start), correctOffset(start + length));
						typeAtt.setType("word");
						positionAtt.setPositionIncrement(1);
						return true;
					}
				}

				preChar = c;

				if (length == 0) {
					assert start == -1;
					start = offset + bufferIndex - 1;
					end = start;
				} else if (length >= buffer.length - 1) {
					buffer = termAtt.resizeBuffer(2 + length);
				}

				end++;
				length += Character.toChars(normalize(c), buffer, length);

				if (length >= MAX_WORD_LEN) break;

			} else if (length > 0) {
				break;
			}
		}

		termAtt.setLength(length);
		assert start != -1;
		offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
		typeAtt.setType("word");
		positionAtt.setPositionIncrement(1);

		return true;
	}

	@Override
	public final void end() throws IOException {
		super.end();
		offsetAtt.setOffset(finalOffset, finalOffset);
	}

	@Override
	public void reset() throws IOException {
		super.reset();
		bufferIndex = 0;
		offset = 0;
		dataLen = 0;
		finalOffset = 0;
		preChar = 0;
		preCharType = 99;
		nowCharType = 99;
	}
}
