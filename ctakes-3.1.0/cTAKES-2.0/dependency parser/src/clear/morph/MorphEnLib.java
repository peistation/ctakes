package clear.morph;

public class MorphEnLib
{
	static public boolean isBe(String form)
	{
		form = form.toLowerCase();
		return form.equals("be") || form.equals("am") || form.equals("are") || form.equals("is") || form.equals("was") || form.equals("were");
	}

	static public boolean isHave(String form)
	{
		form = form.toLowerCase();
		return form.equals("have") || form.equals("has") || form.equals("had");
	}
}
