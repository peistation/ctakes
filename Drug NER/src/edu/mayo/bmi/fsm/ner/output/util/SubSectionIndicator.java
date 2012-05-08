/*
 * Created on Aug 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.mayo.bmi.fsm.ner.output.util;

import edu.mayo.bmi.fsm.output.BaseTokenImpl;

/**
 * @author Mayo Clinic
 *
 */
public class SubSectionIndicator extends BaseTokenImpl
{
	public static final int CONFIRMED_STATUS = 0;
    public static final int HISTORY_STATUS = 1;
    public static final int FAMILY_HISTORY_STATUS = 2;
    public static final int PROBABLE_STATUS = 3;
    private int iv_status;
    
    public SubSectionIndicator(int start, int end, int status)
    {
    	super(start, end);
        iv_status = status;
    }

    public int getStatus()
    {
        return iv_status;
    }
}
