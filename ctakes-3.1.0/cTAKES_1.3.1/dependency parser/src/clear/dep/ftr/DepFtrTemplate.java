package clear.dep.ftr;

public class DepFtrTemplate
{
	public String        field;
	public DepFtrToken[] tokens;
	
	public DepFtrTemplate(String field, DepFtrToken[] tokens)
	{
		set(field, tokens);
	}
	
	public void set(String field, DepFtrToken[] tokens)
	{
		this.field  = field;
		this.tokens = tokens;
	}
}
