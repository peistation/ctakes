package org.chboston.cnlp.ctakes.coref.eval;

import org.chboston.cnlp.ctakes.coref.eval.helpers.*;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Hashtable;
import java.util.Vector;

/* @author Jiaping Zheng
 * 
 */
public class KwtXMLAlign {

	Vector<String> pairs1, pairs2;
	Hashtable<String,Integer> markables1, markables2;
	Vector<Span> span1, span2;
	public KwtXMLAlign () {
		pairs1 = new Vector<String>(); // antecedent_internal_ID!anaphor_internal_ID, ordered as listed in input file
		pairs2 = new Vector<String>();
		markables1 = new Hashtable<String,Integer>(); // internal_ID -> consolidated ID
		markables2 = new Hashtable<String,Integer>();
		span1 = new Vector<Span>(); // spans, ordered as listed in input file (from beginning to end of the report)
		span2 = new Vector<Span>();
	}

	public static void main(String[] args) {
		KwtXMLAlign k = new KwtXMLAlign();
		try {
			Vector<String> v1 = new Vector<String>(); // span1[i]'s internal ID
			Vector<String> v2 = new Vector<String>();
			Vector<String> m1c = new Vector<String>(); // comments after markables lines
			Vector<String> m2c = new Vector<String>();
			Vector<String> p1c = new Vector<String>(); // comments after pairs lines 
			Vector<String> p2c = new Vector<String>();
			k.readFile(new BufferedReader(new FileReader(args[0])), k.pairs1, k.span1, v1, m1c, p1c);
			k.readFile(new BufferedReader(new FileReader(args[1])), k.pairs2, k.span2, v2, m2c, p2c);

			SpanAlignment sa = new SpanAlignment(k.span1.toArray(new Span[k.span1.size()]), k.span2.toArray(new Span[k.span2.size()]));
			int[] id = sa.get1();
			for (int i = 0; i < id.length; i++)
				k.markables1.put(v1.get(i), id[i]);
			id = sa.get2();
			for (int i = 0; i < id.length; i++)
				k.markables2.put(v2.get(i), id[i]);

			for (int i = 0; i < v1.size(); i++)
				System.err.println("1." + (k.markables1.get(v1.get(i))+"\t->\t"+k.span1.get(i)+"\t"+m1c.get(i)).trim());
			System.err.println("------------");
			for (int i = 0; i < v2.size(); i++)
				System.err.println("2." + (k.markables2.get(v2.get(i))+"\t->\t"+k.span2.get(i)+"\t"+m2c.get(i)).trim());

			for (int i = 0; i < k.pairs1.size(); i++) {
				String[] ss = k.pairs1.get(i).split("!");
				System.out.println((k.markables1.get(ss[0]) + "\t" + k.markables1.get(ss[1]) + "\t" + p1c.get(i)).trim());
			}
			System.out.println("------------");
			for (int i = 0; i < k.pairs2.size(); i++) {
				String[] ss = k.pairs2.get(i).split("!");
				System.out.println((k.markables2.get(ss[0]) + "\t" + k.markables2.get(ss[1]) + "\t" + p2c.get(i)).trim());
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	private void readFile (BufferedReader br, Vector<String> pairs, Vector<Span> span, Vector<String> v, Vector<String> mc, Vector<String> pc) {
		try {
			String l;
			while ((l = br.readLine()) != null) {
				int p;
				String c = "";
				if ((p=l.indexOf("#")) != -1) {
					c = l.substring(p);
					l = l.substring(0, p).trim();
				}
				String[] s = l.split("\t");
				if (l.indexOf('!') != -1) {
					pairs.add(s[1]);
					pc.add(c);
				}
				else {
					int[] a = new int[s.length-1];
					for (int i = 1; i < s.length; i++)
						a[i-1] = Integer.parseInt(s[i]);
					span.add(new Span(a));
					v.add(s[0]);
					mc.add(c);
				}
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
}
