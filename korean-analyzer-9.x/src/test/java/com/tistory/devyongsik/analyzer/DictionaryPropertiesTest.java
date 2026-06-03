package com.tistory.devyongsik.analyzer;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.lucene.analysis.kr.morph.MorphException;
import org.apache.lucene.analysis.kr.utils.FileUtil;
import org.apache.lucene.analysis.kr.utils.KoreanEnv;
import org.junit.Test;

public class DictionaryPropertiesTest {

	@Test
	public void propertiesLoad() {
		DictionaryProperties dp = DictionaryProperties.getInstance();
		Assert.assertNotNull(dp);
	}

	@Test
	public void customDictionaryResourcesLoadFromClasspath() throws Exception {
		ClassLoader loader = DictionaryProperties.class.getClassLoader();
		Properties properties = loadProperties(loader, "com/tistory/devyongsik/analyzer/dictionary.properties");

		assertResourceExists(loader, properties.getProperty("compounds.txt"));
		assertResourceExists(loader, properties.getProperty("custom.txt"));
		assertResourceExists(loader, properties.getProperty("stop.txt"));
		assertResourceExists(loader, properties.getProperty("synonym.txt"));
	}

	@Test
	public void morphDictionaryResourcesLoadFromClasspath() throws Exception {
		ClassLoader loader = DictionaryProperties.class.getClassLoader();
		Properties properties = loadProperties(loader, KoreanEnv.FILE_KOREAN_PROPERTY);

		assertResourceExists(loader, properties.getProperty("dictionary.dic"));
		assertResourceExists(loader, properties.getProperty("extension.dic"));
		assertResourceExists(loader, properties.getProperty("compounds.dic"));
		assertResourceExists(loader, properties.getProperty("tagger.dic"));
		assertResourceExists(loader, "org/apache/lucene/analysis/kr/dic/mapHanja.dic");

		String dictionaryPath = KoreanEnv.getInstance().getValue(KoreanEnv.FILE_DICTIONARY);
		List<?> dictionaryLines = FileUtil.readLines(dictionaryPath, "UTF-8");

		Assert.assertEquals("org/apache/lucene/analysis/kr/dic/total.dic", dictionaryPath);
		Assert.assertTrue(dictionaryLines.size() > 0);
	}

	@Test
	public void missingClasspathResourceReportsResourceName() throws Exception {
		try {
			FileUtil.readLines("missing-resource.dic", "UTF-8");
			Assert.fail("Expected MorphException");
		} catch (MorphException e) {
			Assert.assertTrue(e.getMessage().contains("Unable to find classpath resource missing-resource.dic"));
		}
	}

	private Properties loadProperties(ClassLoader loader, String resource) throws Exception {
		InputStream inputStream = loader.getResourceAsStream(resource);
		Assert.assertNotNull(resource + " should exist on the classpath", inputStream);

		try {
			Properties properties = new Properties();
			properties.load(inputStream);
			return properties;
		} finally {
			inputStream.close();
		}
	}

	private void assertResourceExists(ClassLoader loader, String resource) throws Exception {
		InputStream inputStream = loader.getResourceAsStream(resource.trim());

		try {
			Assert.assertNotNull(resource + " should exist on the classpath", inputStream);
		} finally {
			if(inputStream != null) {
				inputStream.close();
			}
		}
	}
}
