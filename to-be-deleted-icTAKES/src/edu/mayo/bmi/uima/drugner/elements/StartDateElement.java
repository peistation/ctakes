package edu.mayo.bmi.uima.drugner.elements;

public class StartDateElement {
	
	private String vdate = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public StartDateElement(){
    	
    
		
	}
    
	public StartDateElement(String date, int beginOffset, int endOffset){
		vdate = date;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public String getDate(){
		return vdate;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
