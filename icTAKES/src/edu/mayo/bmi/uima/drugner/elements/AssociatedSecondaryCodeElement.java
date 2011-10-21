package edu.mayo.bmi.uima.drugner.elements;

public class AssociatedSecondaryCodeElement {
	
	private String cuiCode = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public AssociatedSecondaryCodeElement(){
    	
    
		
	}
    
	public AssociatedSecondaryCodeElement(String cui, int beginOffset, int endOffset){
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
