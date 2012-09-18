package edu.mayo.bmi.uima.ner.elements;

public class StrengthUnitElement {
	
	private String strengthUnit = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public StrengthUnitElement(){
    	
    
		
	}
    
	public StrengthUnitElement(String st, int beginOffset, int endOffset){
		strengthUnit = st;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public String getStrengthMention(){
		return strengthUnit;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
