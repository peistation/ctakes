package edu.mayo.bmi.uima.ner.elements;

public class StrengthElement {
	
	private String strength = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public StrengthElement(){
    	
    
		
	}
    
	public StrengthElement(String st, int beginOffset, int endOffset){
		strength = st;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public String getStrengthMention(){
		return strength;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
