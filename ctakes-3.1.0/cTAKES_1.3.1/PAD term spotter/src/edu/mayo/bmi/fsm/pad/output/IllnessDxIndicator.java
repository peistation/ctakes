package edu.mayo.bmi.fsm.pad.output;

import edu.mayo.bmi.fsm.output.BaseTokenImpl;



/**
 *
 * @author Mayo Clinic
 */

public class IllnessDxIndicator extends BaseTokenImpl
{
	   public static final int BREAST_STATUS = 4;
	   public static final int BRAIN_STATUS = 5;
	   public static final int COLON_STATUS = 6;
	   public static final int PAD_STATUS = 7;
	   
	   private int iv_status;
	    
	public IllnessDxIndicator(int startOffset, int endOffset, int status)	
	{
		super(startOffset, endOffset);
        iv_status = status;
    }

    public int getStatus()
    {
        return iv_status;
    }
}
