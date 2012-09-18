package edu.mayo.bmi.uima.ner.elements;

public class FrequencyElement {
	
	private String frequency = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public FrequencyElement(){
    	
    
		
	}
    
	public FrequencyElement(String freq, int beginOffset, int endOffset){
		frequency = freq;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public String getFrequencyMention(){
		return frequency;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
