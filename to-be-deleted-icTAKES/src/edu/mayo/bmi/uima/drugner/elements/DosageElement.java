package edu.mayo.bmi.uima.drugner.elements;

public class DosageElement {
	
	private String doseage = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public DosageElement(){
    	
    
		
	}
    
	public DosageElement(String dose, int beginOffset, int endOffset){
		doseage = dose;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public String getDosage(){
		return doseage;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
