package org.chboston.cnlp.ctakes.relationextractor.knowtator;

import com.google.common.base.Objects;

/**
 * Represents span of a named entity
 * 
 * @author dmitriy dligach
 *
 */
public class Span {
	
	public int start;
	public int end;
	
	public Span(int start, int end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	public boolean equals(Object object) {
		
		boolean isEqual = false;
		
		if(object instanceof Span) {
			Span span = (Span) object;
			isEqual = ((this.start == span.start) && (this.end == span.end));
		}
		
		return isEqual;
	}
	
	@Override
  public int hashCode()
  {
  	return Objects.hashCode(start, end);
  }
	
	public String toString() {
		return String.format("%d -- %d", start, end);
	}
}