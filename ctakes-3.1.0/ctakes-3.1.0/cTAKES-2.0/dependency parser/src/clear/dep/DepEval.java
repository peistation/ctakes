package clear.dep;

public class DepEval
{
	private int n_las;
	private int n_uas;
	private int n_ls;
	private int n_total;
	
	public DepEval()
	{
		n_las   = 0;
		n_uas   = 0;
		n_ls    = 0;
		n_total = 0;
	}

	public void evaluate(DepTree gold, DepTree sys)
	{
		for (int i=1; i<gold.size(); i++)
		{
			DepNode gNode = gold.get(i);
			DepNode sNode = sys .get(i);
			
			if (gNode.isDeprel(sNode.deprel))
			{
				n_ls++;
				if (gNode.headId == sNode.headId)
					n_las++;
			}
			
			if (gNode.headId == sNode.headId)
				n_uas++;
			
			n_total++;
		}
	}
	
	public double getLas()
	{
		return (double)n_las / n_total;
	}
	
	public double getUas()
	{
		return (double)n_uas / n_total;
	}
	
	public double getLs()
	{
		return (double)n_ls / n_total;
	}
	
	public void print()
	{
		System.out.printf("LAS: %4.2f%", getLas()*100);
		System.out.printf("UAS: %4.2f%", getUas()*100);
		System.out.printf("LS : %4.2f%", getLs() *100);
	}
}
