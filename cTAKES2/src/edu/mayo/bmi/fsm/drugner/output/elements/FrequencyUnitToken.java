package edu.mayo.bmi.fsm.drugner.output.elements;



/**
 *
 * @author Mayo Clinic
 */
public class FrequencyUnitToken extends BaseTokenImpl
{
    public static final double QUANTITY_PRN = 0;
    public static final double QUANTITY_ONE = 1;
    public static final double QUANTITY_TWO = 2;
    public static final double QUANTITY_THREE = 3;
    public static final double QUANTITY_FOUR = 4;
    public static final double QUANTITY_FIVE = 5;
    public static final double QUANTITY_SIX = 6;
    public static final double QUANTITY_SEVEN = 7;
    public static final double QUANTITY_EIGHT = 8;
    public static final double QUANTITY_NINE = 9;
    public static final double QUANTITY_TEN = 10;
    public static final double QUANTITY_ELEVEN = 11;
    public static final double QUANTITY_24 = 24;
    public static final double QUANTITY_WEEKLY = 0.14;
    public static final double QUANTITY_BIWEEKLY = 0.07;
    public static final double QUANTITY_MONTHLY = 0.03;
    public static final double QUANTITY_EVERY_OTHER_DAY = 0.5;
    public static final double QUANTITY_YEARLY = 0.003;
        
    
    private double iv_quantity;

	public FrequencyUnitToken(int startOffset, int endOffset, double quantity)
	{
		super(startOffset, endOffset);
	    iv_quantity = quantity;	
	}

    public double getFrequencyUnitQuantity()
    {
        return iv_quantity;
    }

}
