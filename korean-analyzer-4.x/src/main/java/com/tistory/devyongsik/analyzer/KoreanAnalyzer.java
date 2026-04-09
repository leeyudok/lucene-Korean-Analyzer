package com.tistory.devyongsik.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

/**
 * @author need4spd, need4spd@cplanet.co.kr, 2011. 8. 31.
 *
 */
public class KoreanAnalyzer extends Analyzer {

	private boolean isIndexingMode = false;
	
	public KoreanAnalyzer() {
		isIndexingMode = true;
	}
	
	public KoreanAnalyzer(boolean isIndexingMode) {
		this.isIndexingMode = isIndexingMode;
	}
	
	@Override
	protected TokenStreamComponents createComponents(final String fieldName) {

		if(isIndexingMode) {
			List<Engine> nounExtractEngines = new ArrayList<Engine>();
			nounExtractEngines.add(new KoreanCompoundNounEngine());
			nounExtractEngines.add(new KoreanBaseNounEngine());
			nounExtractEngines.add(new KoreanLongestNounEngine());
			nounExtractEngines.add(new KoreanSynonymEngine());
			nounExtractEngines.add(new KoreanMorphEngine());

			Tokenizer tokenizer = new KoreanCharacterTokenizer();
			TokenStream tok = new KoreanNounFilter(tokenizer, nounExtractEngines);
			tok = new KoreanStopFilter(tok);

			return new TokenStreamComponents(tokenizer, tok);
		} else {
			List<Engine> nounExtractEngines = new ArrayList<Engine>();
			nounExtractEngines.add(new KoreanCompoundNounEngine());
			nounExtractEngines.add(new KoreanLongestNounEngine());
			nounExtractEngines.add(new KoreanSynonymEngine());
			nounExtractEngines.add(new KoreanMorphEngine());

			Tokenizer tokenizer = new KoreanCharacterTokenizer();
			TokenStream tok = new KoreanNounFilter(tokenizer, nounExtractEngines);
			tok = new KoreanStopFilter(tok);

			return new TokenStreamComponents(tokenizer, tok);
		}
	}

}