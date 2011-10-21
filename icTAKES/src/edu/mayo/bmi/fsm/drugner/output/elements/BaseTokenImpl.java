package edu.mayo.bmi.fsm.drugner.output.elements;

import edu.mayo.bmi.fsm.token.BaseToken;

public class BaseTokenImpl implements BaseToken
{
	private int iv_startOffset;
	private int iv_endOffset;

	public BaseTokenImpl(int startOffset, int endOffset)
	{
		iv_startOffset = startOffset;
		iv_endOffset = endOffset;
	}

	public int getStartOffset()
	{
		return iv_startOffset;
	}

	public int getEndOffset()
	{
		return iv_endOffset;
	}

}
