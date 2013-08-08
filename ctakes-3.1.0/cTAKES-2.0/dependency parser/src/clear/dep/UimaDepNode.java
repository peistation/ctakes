package clear.dep;

import clear.ftr.FtrLib;

public class UimaDepNode extends DepNode {

	public int begin;
	public int end;
	
	/** Initializes the node as a null node. */
	public UimaDepNode()
	{
		init(DepLib.NULL_ID, FtrLib.TAG_NULL, FtrLib.TAG_NULL, FtrLib.TAG_NULL, DepLib.NULL_HEAD_ID, FtrLib.TAG_NULL);
	}
	
	/**
	 * If (<code>isRoot</code> is true ), initializes the node as the root.
	 * If (<code>isRoot</code> is false), initializes the node as a null node.
	 */
	public UimaDepNode(boolean isRoot)
	{
		if (isRoot)	init(DepLib.ROOT_ID, DepLib.ROOT_TAG, DepLib.ROOT_TAG, DepLib.ROOT_TAG, DepLib.NULL_HEAD_ID, DepLib.ROOT_TAG);
		else		init(DepLib.NULL_ID, FtrLib.TAG_NULL, FtrLib.TAG_NULL, FtrLib.TAG_NULL, DepLib.NULL_HEAD_ID, FtrLib.TAG_NULL);
	}

	@Override
	public UimaDepNode clone()
	{
		UimaDepNode node = new UimaDepNode();
		node.copy(this);
		
		return node;
	}
//	
//	public int getBegin() {
//		return begin;
//	}
//	
//	public int getEnd() {
//		return end;
//	}
//
//	public int setBegin() {
//		return begin;
//	}
//	
//	public int setEnd() {
//		return end;
//	}

}
