package edu.mayo.bmi.uima.ner.elements;

public class ConfidenceScoreElement {
	
	private double confidence = 0;
	
	private int begOff = 0;
	
	private int endOff = 0;
    
    public ConfidenceScoreElement(){
    	
    
		
	}
    
	public ConfidenceScoreElement(double score, int beginOffset, int endOffset){
		confidence = score;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public double getConfidenceScoreElement(){
		return confidence;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
