package edu.mayo.bmi.uima.drugner.elements;

public class AssociatedCodeElement {
	
	private String cuiCode = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public AssociatedCodeElement(){
    	
    
		
	}
    
	public AssociatedCodeElement(String cui, int beginOffset, int endOffset){
		cuiCode = cui;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public String getCuiCode(){
		return cuiCode;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
