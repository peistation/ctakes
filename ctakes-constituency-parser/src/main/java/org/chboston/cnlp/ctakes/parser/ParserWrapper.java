package org.chboston.cnlp.ctakes.parser;

import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;

public interface ParserWrapper {
	public String getParseString(FSIterator tokens);
	public void createAnnotations(JCas jcas);
}
