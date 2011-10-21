package edu.mayo.bmi.uima.drugner.elements;

public class DurationElement {
	
	private String duration = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public DurationElement(){
    	
    
		
	}
    
	public DurationElement(String da, int beginOffset, int endOffset){
		duration = da;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public String getDuration(){
		return duration;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
