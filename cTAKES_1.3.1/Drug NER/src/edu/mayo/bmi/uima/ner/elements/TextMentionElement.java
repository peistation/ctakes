package edu.mayo.bmi.uima.ner.elements;

public class TextMentionElement {
	
	private String drugName = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public TextMentionElement(){
    	
    
		
	}
    
	public TextMentionElement(String ner, int beginOffset, int endOffset){
		drugName = ner;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public String getTextMention(){
		return drugName;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
