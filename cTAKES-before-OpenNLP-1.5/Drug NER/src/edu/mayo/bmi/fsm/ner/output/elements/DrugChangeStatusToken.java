package edu.mayo.bmi.fsm.ner.output.elements;



/**
 *
 * @author Mayo Clinic
 */
public class DrugChangeStatusToken extends BaseTokenImpl
{
   
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String INCREASEFROM = "increasefrom";
    public static final String DECREASEFROM = "decreasefrom";
    public static final String INCREASE = "increase";
    public static final String DECREASE = "decrease";
    public static final String NOCHANGE = "noChange";
    public static final String SUM = "add";
    public static final String MAX = "maximum";
    public static final String OTHER = "change";
    
    private String status = NOCHANGE;
    
    public DrugChangeStatusToken(int startOffset, int endOffset, String statusChange)
	{
		super(startOffset, endOffset);
		status = statusChange;
	}
    
    public String getDrugChangeStatus(){
    	return status;
    }
    
    public void setDrugChangeStatus(String localStatus){
    	status = localStatus;
    }
}


