package org.chboston.cnlp.ctakes.coref.eval;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.chboston.cnlp.ctakes.coref.eval.helpers.B3Type;
import org.chboston.cnlp.ctakes.coref.eval.helpers.CEAFType;
import org.junit.Test;

import Jama.Matrix;

import java.util.Vector;

import edu.mayo.bmi.coref.util.ParentPtrTree;

public class TestCorefScore {

	static ParentPtrTree ppt_gold;

	{
		ppt_gold = new ParentPtrTree(70);
		Vector<int[]> pairs_gold = new Vector<int[]>();
		pairs_gold.add(new int[]{62, 63, 64, 65});
		pairs_gold.add(new int[]{66,67,68});
		pairs_gold.add(new int[]{69,70});
		addPairs(ppt_gold, pairs_gold);
	}

	public void addPairs (ParentPtrTree ppt, Vector<int[]> pairs) {
		for (int[] p : pairs)
			for (int i = 1; i < p.length; i++)
				ppt.union(p[i-1]-1, p[i]-1);
	}

	private static void assertEqualsRounded (String message, double expected, double actual) {
		double diff = expected - actual;
		assertTrue(message, diff >= -0.05 && diff <= 0.04);
	}

	@Test
	public void testSysA () {
		Vector<int[]> sys = new Vector<int[]>();
		sys.add(new int[]{1, 2});
		sys.add(new int[]{62, 63, 64, 65});
		sys.add(new int[]{66, 67, 68});
		sys.add(new int[]{69, 70});
		ParentPtrTree ppt_sys = new ParentPtrTree(70);
		addPairs(ppt_sys, sys);

		Matrix m = MatScore.partition(ppt_gold, ppt_sys);
		assertEqualsRounded("System A MUC", 0.9231, MatScore.MUC(m));
		assertEqualsRounded("System A B3", 0.9928, MatScore.B3(m, B3Type.ENTITY));
		assertEqualsRounded("System A CEAF", 0.9857, MatScore.CEAF(m, CEAFType.MENTION));
//		assertEqualsRounded("System A BLANC", 0.9761, BlancScore.score(ppt_gold, ppt_sys));
	}

	@Test
	public void testSysB () {
		Vector<int[]> sys = new Vector<int[]>();
		sys.add(new int[]{1, 62, 63, 64, 65});
		sys.add(new int[]{66, 67, 68});
		sys.add(new int[]{69, 70});
		ParentPtrTree ppt_sys = new ParentPtrTree(70);
		addPairs(ppt_sys, sys);

		Matrix m = MatScore.partition(ppt_gold, ppt_sys);
		assertEqualsRounded("System B MUC", 0.9231, MatScore.MUC(m));
		assertEqualsRounded("System B B3", 0.9884, MatScore.B3(m, B3Type.ENTITY));
		assertEqualsRounded("System B CEAF", 0.9857, MatScore.CEAF(m, CEAFType.MENTION));
//		assertEqualsRounded("System B BLANC", 0.9163, BlancScore.score(ppt_gold, ppt_sys));
	}

	@Test
	public void testSysC () {
		Vector<int[]> sys = new Vector<int[]>();
		sys.add(new int[]{62, 63, 64, 65});
		sys.add(new int[]{69, 70});
		ParentPtrTree ppt_sys = new ParentPtrTree(70);
		addPairs(ppt_sys, sys);

		Matrix m = MatScore.partition(ppt_gold, ppt_sys);
		assertEqualsRounded("System C MUC", 0.8000, MatScore.MUC(m));
		assertEqualsRounded("System C B3", 0.9855, MatScore.B3(m, B3Type.ENTITY));
		assertEqualsRounded("System C CEAF", 0.9714, MatScore.CEAF(m, CEAFType.MENTION));
//		assertEqualsRounded("System C BLANC", 0.9115, BlancScore.score(ppt_gold, ppt_sys));
	}

	@Test
	public void testSysD () {
		Vector<int[]> sys = new Vector<int[]>();
		sys.add(new int[]{62, 63, 64, 65, 66, 67, 68});
		sys.add(new int[]{69, 70});
		ParentPtrTree ppt_sys = new ParentPtrTree(70);
		addPairs(ppt_sys, sys);

		Matrix m = MatScore.partition(ppt_gold, ppt_sys);
		assertEqualsRounded("System D MUC", 0.9231, MatScore.MUC(m));
		assertEqualsRounded("System D B3", 0.9749, MatScore.B3(m, B3Type.ENTITY));
		assertEqualsRounded("System D CEAF", 0.9571, MatScore.CEAF(m, CEAFType.MENTION));
//		assertEqualsRounded("System D BLANC", 0.8112, BlancScore.score(ppt_gold, ppt_sys));
	}

	@Test
	public void testSysE () {
		Vector<int[]> sys = new Vector<int[]>();
		sys.add(new int[]{1, 62, 63});
		sys.add(new int[]{28, 64, 65});
		sys.add(new int[]{66, 67, 68});
		sys.add(new int[]{69, 70});
		ParentPtrTree ppt_sys = new ParentPtrTree(70);
		addPairs(ppt_sys, sys);

		Matrix m = MatScore.partition(ppt_gold, ppt_sys);
		assertEqualsRounded("System E MUC", 0.7692, MatScore.MUC(m));
		assertEqualsRounded("System E B3", 0.9666, MatScore.B3(m, B3Type.ENTITY));
		assertEqualsRounded("System E CEAF", 0.9571, MatScore.CEAF(m, CEAFType.MENTION));
//		assertEqualsRounded("System E BLANC", 0.7992, BlancScore.score(ppt_gold, ppt_sys));
	}

	@Test
	public void testSysF () {
		Vector<int[]> sys = new Vector<int[]>();
		sys.add(new int[]{1, 62});
		sys.add(new int[]{4, 63});
		sys.add(new int[]{28, 64});
		sys.add(new int[]{57, 65});
		sys.add(new int[]{66, 67, 68});
		sys.add(new int[]{69, 70});
		ParentPtrTree ppt_sys = new ParentPtrTree(70);
		addPairs(ppt_sys, sys);

		Matrix m = MatScore.partition(ppt_gold, ppt_sys);
		assertEqualsRounded("System F MUC", 0.4615, MatScore.MUC(m));
		assertEqualsRounded("System F B3", 0.9499, MatScore.B3(m, B3Type.ENTITY));
		assertEqualsRounded("System F CEAF", 0.9429, MatScore.CEAF(m, CEAFType.MENTION));
//		assertEqualsRounded("System F BLANC", 0.7212, BlancScore.score(ppt_gold, ppt_sys));
	}

	@Test
	public void testSysG () {
		Vector<int[]> sys = new Vector<int[]>();
		ParentPtrTree ppt_sys = new ParentPtrTree(70);
		addPairs(ppt_sys, sys);

		Matrix m = MatScore.partition(ppt_gold, ppt_sys);
		assertEquals("System G MUC", Double.NaN, MatScore.MUC(m), 0);
		assertEqualsRounded("System G B3", 0.9552, MatScore.B3(m, B3Type.ENTITY));
		assertEqualsRounded("System G CEAF", 0.9143, MatScore.CEAF(m, CEAFType.MENTION));
//		assertEqualsRounded("System G BLANC", 0.4990, BlancScore.score(ppt_gold, ppt_sys));
	}

	@Test
	public void testSysH () {
		Vector<int[]> sys = new Vector<int[]>();
		int[] h = new int[70];
		for (int i = 1; i <=70; i++) h[i-1] = i;
		sys.add(h);
		ParentPtrTree ppt_sys = new ParentPtrTree(70);
		addPairs(ppt_sys, sys);

		Matrix m = MatScore.partition(ppt_gold, ppt_sys);
		assertEqualsRounded("System H MUC", 0.1600, MatScore.MUC(m));
		assertEqualsRounded("System H B3", 0.0361, MatScore.B3(m, B3Type.ENTITY));
		assertEqualsRounded("System H CEAF", 0.0571, MatScore.CEAF(m, CEAFType.MENTION));
//		assertEqualsRounded("System H BLANC", 0.0041, BlancScore.score(ppt_gold, ppt_sys));
	}

}
