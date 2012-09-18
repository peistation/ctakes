package edu.mayo.bmi.uima.drugner.elements;

public class DrugChangeStatusElement {
	
	private String drugChange = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
	public static final String STOP_STATUS = "stop";
	public static final String START_STATUS = "start";
	public static final String INCREASE_STATUS = "increase";
	public static final String DECREASE_STATUS = "decrease";
	public static final String NOCHANGE_STATUS = "noChange";
	public static final String SUMMATION_STATUS = "add";
	public static final String MAXIMUM_STATUS = "maximum";
	
    public DrugChangeStatusElement(){
    	
    
		
	}
    
	public DrugChangeStatusElement(String status, int beginOffset, int endOffset){
		drugChange = status;
		begOff = beginOffset;
		endOff = endOffset;
	}
	
	public String getDrugChangeStatus(){
		return drugChange;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}

}
