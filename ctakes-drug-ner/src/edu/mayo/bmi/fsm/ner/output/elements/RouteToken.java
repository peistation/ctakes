package edu.mayo.bmi.fsm.ner.output.elements;



/**
 *
 * @author Mayo Clinic
 */
public class RouteToken extends BaseTokenImpl {
	
    public static final int TOPICAL = 0;
    public static final int ORAL = 1;
    public static final int GASTRIC = 2;
    public static final int RECTAL = 3;
    public static final int INTRAVENOUS = 4;
    public static final int INTRAARTERIAL = 5;
    public static final int INTRAMUSCULAR = 6;
    public static final int INTRACARDIAC = 7;
    public static final int SUBCUTANEOUS = 8;
    public static final int INTRATHECAL = 9;
    public static final int INTRAPERITONEAL = 10;
    public static final int TRANSDERMAL = 11;
    public static final int TRANSMUCOSAL = 12;
    
    private int current = 0;
    
	public RouteToken(int startOffset, int endOffset) {
		super(startOffset, endOffset);
	}
	
	public int getFormMethod(){
		return current;
	}
	
	public void setFormMethod(int localCurrent){
		current = localCurrent;
	}
}