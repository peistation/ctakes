package edu.mayo.bmi.uima.drugner.elements;

public class FrequencyUnitElement {
	
	private String frequencyUnit = null;
	
	private int begOff = 0;
	
	private int endOff = 0;
	public static final String DAILY = "day";
	public static final String WEEKLY = "week";
	public static final String MONTHLY = "month";
	public static final String HOURLY = "hour";
	public static final String EVERYOTHERDAY = "every-other-day";
	public static final String ASNEEDED = "as-needed";
	public static final String OTHER = "other";
	

    public FrequencyUnitElement(){
    	
    
		
	}
    
	public FrequencyUnitElement(String freq, int beginOffset, int endOffset){
		frequencyUnit = freq;
		begOff = beginOffset;
		endOff = endOffset;

	}
	
	public String getFrequencyUnitMention(){
		return frequencyUnit;
	}
	
	public int getBeginOffset(){
		return begOff;
	}
	
	public int getEndOffset(){
		return endOff;
	}
	

}
