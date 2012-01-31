package org.chboston.cnlp.ctakes.relationextractor.knowtator;

/**
 * Represents span of a named entity
 * 
 * @author dmitriy dligach
 *
 */
public class Span {
	// represents a span of an entity
	
	public Span(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	public int start;
	public int end;
}