package edu.mayo.bmi.fsm.ner.output.util;

import edu.mayo.bmi.fsm.ner.output.elements.BaseTokenImpl;


/**
 *
 * @author DUFFP
 */
public class RangeToken extends BaseTokenImpl
{
	public RangeToken(int startOffset, int endOffset)
	{
		super(startOffset, endOffset);
	}

}
