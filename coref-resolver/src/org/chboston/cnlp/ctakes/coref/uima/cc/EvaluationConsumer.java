package org.chboston.cnlp.ctakes.coref.uima.cc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;
import org.chboston.cnlp.ctakes.coref.eval.BlancScore;
import org.chboston.cnlp.ctakes.coref.eval.MatScore;
import org.chboston.cnlp.ctakes.coref.eval.helpers.B3Type;
import org.chboston.cnlp.ctakes.coref.eval.helpers.CEAFType;
import org.chboston.cnlp.ctakes.coref.eval.helpers.GraphViz;
import org.chboston.cnlp.ctakes.coref.eval.helpers.MatType;
import org.chboston.cnlp.ctakes.coref.eval.helpers.Span;
import org.chboston.cnlp.ctakes.coref.eval.helpers.SpanAlignment;
import org.chboston.cnlp.ctakes.coref.eval.helpers.SpanOffsetComparator;

import Jama.Matrix;

import edu.mayo.bmi.coref.util.ParentPtrTree;
import edu.mayo.bmi.uima.core.type.relation.CoreferenceRelation;
import edu.mayo.bmi.uima.core.type.syntax.BaseToken;
import edu.mayo.bmi.uima.core.util.DocumentIDAnnotationUtil;
import edu.mayo.bmi.uima.coref.type.Markable;
/* @author Jiaping Zheng
 * @author Tim Miller
 */

public class EvaluationConsumer extends CasConsumer_ImplBase {

	private int maxSpanID = 0;
	private Vector<int[]> collectionPairs_gold = new Vector<int[]>();
	private Vector<int[]> collectionPairs_sys = new Vector<int[]>();

	private String gold_pair_path;
	private String debug_out_path;
	private String i2b2Path;
	private int debug_context;

	private boolean o_muc = true;
	private boolean o_eb3 = true;
	private boolean o_cb3 = true;
	private boolean o_mc  = true;
	private boolean o_ec  = true;
	private boolean o_ka  = true;
	private boolean o_bl  = true;
	private boolean i2b2 = false;
	
	private BlancScore blanc = null;

	@Override
	public void initialize() throws ResourceInitializationException {
		gold_pair_path = (String) getConfigParameterValue("GoldPairDir");
		gold_pair_path = (new File(gold_pair_path)).getPath();
		debug_out_path = (String) getConfigParameterValue("DebugOutputDir");
		if (debug_out_path!=null)
			debug_out_path = (new File(debug_out_path)).getPath();
		debug_context = (Integer) getConfigParameterValue("DebugContext");
//		i2b2Path = (String) getConfigParameterValue("i2b2OutputDir");
//		if(i2b2Path != null){
//			i2b2 = true;
//			File gChainPath = new File(i2b2Path + "/goldChain/");
//			if(!gChainPath.exists()) gChainPath.mkdir();
//			File gMarkPath = new File(i2b2Path + "/goldMark/");
//			if(!gMarkPath.exists()) gMarkPath.mkdir();
//			File sChainPath = new File(i2b2Path + "/sysChain/");
//			if(!sChainPath.exists()) sChainPath.mkdir();
//			File sMarkPath = new File(i2b2Path + "/sysMark/");
//			if(!sMarkPath.exists()) sMarkPath.mkdir();
//		}
		i2b2 = false;
		if (debug_context<0) debug_context = 0;
		o_muc = (Boolean) getConfigParameterValue("MUC");
		o_eb3 = (Boolean) getConfigParameterValue("entity_B3");
		o_cb3 = (Boolean) getConfigParameterValue("class_B3");
		o_mc  = (Boolean) getConfigParameterValue("mention_CEAF");
		o_ec  = (Boolean) getConfigParameterValue("entity_CEAF");
		o_ka  = (Boolean) getConfigParameterValue("KAlpha");
		o_bl  = (Boolean) getConfigParameterValue("BLANC");
		if(o_bl) blanc = new BlancScore();
	}

	@Override
	public void processCas(CAS cas) throws ResourceProcessException {
		try {
			JCas jc = cas.getJCas();
			String docTxt = jc.getDocumentText();

			// load gold standard pairs
			String docName = DocumentIDAnnotationUtil.getDocumentID(jc);
			Vector<Span> markables_gold = new Vector<Span>();
			Hashtable<Span, Integer> m2id_gold = new Hashtable<Span, Integer>();
			Vector<Span[]> pairs_gold = new Vector<Span[]>();
			if (docName!=null) buildGoldPairs(docName, markables_gold, m2id_gold, pairs_gold);

			// get system generated pairs
			Vector<Span> markables_sys = new Vector<Span>();
			Hashtable<Span, Integer> m2id_sys = new Hashtable<Span, Integer>();
			Vector<Span[]> pairs_sys = new Vector<Span[]>();
			buildSystemPairs(jc, markables_sys, m2id_sys, pairs_sys);

			// align the spans
			SpanAlignment sa = new SpanAlignment(markables_gold.toArray(new Span[markables_gold.size()]),
					markables_sys.toArray(new Span[markables_sys.size()]));
			Hashtable<Span, Integer> m2aid_gold = new Hashtable<Span, Integer>();
			Hashtable<Span, Integer> m2aid_sys = new Hashtable<Span, Integer>();

			int[] id = sa.get1();
			for (int i = 0; i < id.length; i++)
				m2aid_gold.put(markables_gold.get(i), id[i]+maxSpanID);
			id = sa.get2();
			for (int i = 0; i < id.length; i++)
				m2aid_sys.put(markables_sys.get(i), id[i]+maxSpanID);

			int[][] p1 = new int[pairs_gold.size()][2];
			for (int i = 0; i < p1.length; i++) {
				Span[] s = pairs_gold.get(i);
				p1[i][0] = m2aid_gold.get(s[0]);
				p1[i][1] = m2aid_gold.get(s[1]);
			}
			ParentPtrTree ppt_gold = new ParentPtrTree(p1);
			int[] eqv_gold = new int[ppt_gold.getSize()];
			int[][] p2 = new int[pairs_sys.size()][2];
			ppt_gold.equivCls(eqv_gold);
			for (int i = 0; i < p2.length; i++) {
				Span[] s = pairs_sys.get(i);
				p2[i][0] = m2aid_sys.get(s[0]);
				p2[i][1] = m2aid_sys.get(s[1]);
			}
			ParentPtrTree ppt_sys = new ParentPtrTree(p2);
			int[] eqv_sys = new int[ppt_sys.getSize()];
			ppt_sys.equivCls(eqv_sys);

			if(o_bl) blanc.addDocumentScore(p1, p2);
			// write debug graphs
			Vector<Span> m = new Vector<Span>();
			m.addAll(markables_gold);
			m.addAll(markables_sys);
			writeDot(m,
					m2aid_gold, m2aid_sys,
					pairs_gold, pairs_sys,
					eqv_gold, eqv_sys,
					docTxt, docName);

			for (Span[] p : pairs_gold)
				collectionPairs_gold.add(new int[]{m2aid_gold.get(p[0]), m2aid_gold.get(p[1])});
			for (Span[] p : pairs_sys)
				collectionPairs_sys.add(new int[]{m2aid_sys.get(p[0]), m2aid_sys.get(p[1])});
			maxSpanID += sa.getMaxID();
			
//			if(i2b2){
//				// write system chain file:
//				try {
//					PrintWriter writer = new PrintWriter(i2b2Path + "/sysChain/" + docName + ".chain");
//					HashMap<Integer,Integer> tok2ind = mapTokensToIndices(jc);
//					FSIterator iter = jc.getJFSIndexRepository().getAllIndexedFS(CoreferenceChain.type);
//					while(iter.hasNext()){
//						CoreferenceChain chain = (CoreferenceChain) iter.next();
//						FSList members = chain.getMembers();
//						while(members instanceof NonEmptyFSList){
//							NonEmptyFSList node = (NonEmptyFSList) members;
//							Markable mark = (Markable) node.getHead();
//							writer.print("c=\"");
//							writer.print(mark.getCoveredText());
//							writer.print("\" ");
//							writer.print(getSpanString(mark, tok2ind));
//							writer.print("||");
//							members = node.getTail();
//						}
//						// write the type information
//						writer.println("t=\"coref problem\"");
//					}
//					writer.close();
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}

		} catch (CASException e) { e.printStackTrace(); }
	}

	private String getSpanString(Markable mark,
			HashMap<Integer, Integer> tok2ind) {
		Integer tok1 = tok2ind.get(mark.getBegin());
		Integer tok2 = tok2ind.get(mark.getEnd());
		return "1:" + tok1 + " 1:" + tok2;
	}

	private HashMap<Integer, Integer> mapTokensToIndices(JCas jc) {
		HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
		
		int i = 1;
		FSIterator iter = jc.getAnnotationIndex(BaseToken.type).iterator();
		while(iter.hasNext()){
			BaseToken tok = (BaseToken) iter.next();
			map.put(tok.getBegin(), i);
			map.put(tok.getEnd(), i);
			i++;
		}
		return map;
	}

	@Override
	public void collectionProcessComplete (ProcessTrace arg0) throws ResourceProcessException {
		try {
			super.collectionProcessComplete(arg0);

			int[][] a = collectionPairs_gold.toArray(new int[collectionPairs_gold.size()][2]);
			int[][] b = collectionPairs_sys.toArray(new int[collectionPairs_sys.size()][2]);
			ParentPtrTree p1 = new ParentPtrTree(a);
			ParentPtrTree p2 = new ParentPtrTree(b);

			Matrix m = MatScore.partition(p1, p2);
			if (o_muc){ System.out.println("MUC                = " + MatScore.MUC(m));
						System.out.println("     recall:       = \t" + MatScore.MUC_recall(m));
						System.out.println("     precision     = \t" + MatScore.MUC_recall(m.transpose()));
			}
			
			if (o_cb3){
				System.out.println("class-B3           = " + MatScore.B3(m,B3Type.CLASS));
				System.out.println("   recall          = \t" + MatScore.B3_recall(m,B3Type.CLASS));
				System.out.println("   precision       = \t" + MatScore.B3_recall(m.transpose(),B3Type.CLASS));				
			}
			if (o_eb3){
				System.out.println("entity-B3          = " + MatScore.B3(m,B3Type.ENTITY));
				System.out.println("      recall       = \t" + MatScore.B3_recall(m, B3Type.ENTITY));
				System.out.println("      precision    = \t" + MatScore.B3_recall(m.transpose(), B3Type.ENTITY));
			}
			if (o_mc) System.out.println("mention-based CEAF = " + MatScore.CEAF(m,CEAFType.MENTION));
			try{
				if (o_ec) System.out.println("entity-based CEAF  = " + MatScore.CEAF(MatScore.calcPhi(p1,p2,MatType.matchingType(CEAFType.ENTITY)),CEAFType.ENTITY));
			}catch(Exception e){
				System.err.println("Exception caught with e-b CEAF");
			}
			try{
				if (o_bl) System.out.println("BLANC              = " + blanc.getScore());
			}catch(Exception e){
				System.err.println("Exception caught with blanc score.");
			}
			try{
				if (o_ka) System.out.println("KAlpha             = " + MatScore.KAlpha(p1,p2));
			}catch(Exception e){
				System.err.println("Exception caught with kalpha.");
			}

		} catch (IOException e) { 
			e.printStackTrace(); 
		}
	}

	private void buildGoldPairs(String docName,
			Vector<Span> spans,
			Hashtable<Span, Integer> span2id,
			Vector<Span[]> pairs) {
		File f = new File(gold_pair_path + File.separator + docName);
		int id = 0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String l;
			while ((l = br.readLine())!=null) {
				String[] p = l.split("\\t");
				Span s1 = new Span(p[0]);
				Span s2 = new Span(p[1]);
				if (!span2id.containsKey(s1)) {
					span2id.put(s1, ++id);
					spans.add(s1);
				}
				if (!span2id.containsKey(s2)) {
					span2id.put(s2, ++id);
					spans.add(s2);
				}
				pairs.add(new Span[]{s1, s2});
			}
			java.util.Collections.sort(spans, new SpanOffsetComparator());
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void buildSystemPairs (JCas jc,
			Vector<Span> spans,
			Hashtable<Span, Integer> span2id,
			Vector<Span[]> pairs) {
		FSIterator iter = jc.getJFSIndexRepository().getAllIndexedFS(CoreferenceRelation.type);
		while (iter.hasNext()) {
			CoreferenceRelation cr = (CoreferenceRelation) iter.next();
			if (cr.getConfidence()<0.5) continue;
			Markable m1 = (Markable) cr.getArg1().getArgument();
			Markable m2 = (Markable) cr.getArg2().getArgument();
			Span s1 = new Span(m1.getBegin()+"-"+m1.getEnd());
			Span s2 = new Span(m2.getBegin()+"-"+m2.getEnd());
			int id1 = m1.getId();
			if (!span2id.containsKey(s1)) {
				span2id.put(s1, id1);
				spans.add(s1);
			}
			int id2 = m2.getId();
			if (!span2id.containsKey(s2)) {
				span2id.put(s2, id2);
				spans.add(s2);
			}
			pairs.add(new Span[]{s1, s2});
		}
		java.util.Collections.sort(spans, new SpanOffsetComparator());
	}

	private void writeDot(
			Vector<Span> markables,
			Hashtable<Span, Integer> m2aid_gold,
			Hashtable<Span, Integer> m2aid_sys,
			Vector<Span[]> pairs_gold,
			Vector<Span[]> pairs_sys,
			int[] eqv_gold,
			int[] eqv_sys,
			String docTxt,
			String toFile) {

		Hashtable<Integer, Span> aid2m_gold = new Hashtable<Integer, Span>();
		Hashtable<Integer, Span> aid2m_sys = new Hashtable<Integer, Span>();
		for (Span s : m2aid_gold.keySet())
			aid2m_gold.put(m2aid_gold.get(s), s);
		for (Span s : m2aid_sys.keySet())
			aid2m_sys.put(m2aid_sys.get(s), s);

		GraphViz gv = new GraphViz();
		gv.addln(gv.start_graph());

		// create a unique Vector of markables (both gold and sys)
		java.util.Collections.sort(markables, new SpanOffsetComparator());
		Vector<Integer> dup = new Vector<Integer>();
		for (int i = 1; i < markables.size(); i++)
			if (markables.get(i).toString().equals(markables.get(i-1).toString()))
				dup.add(i);
		for (int i = dup.size()-1; i >= 0; i--)
			markables.remove(dup.get(i).intValue());


		if (markables.size()>1) {
			// print invisible edges so that the markables are ordered correctly
			Vector<Integer> mseq = mid_seq(markables, m2aid_gold, aid2m_gold, m2aid_sys, aid2m_sys);

			gv.addln("{");
			gv.add("gm"+mseq.get(0));
			for (int i = 1; i < mseq.size(); i++)
				gv.addln(" -> gm"+mseq.get(i));
			gv.addln("[style=\"invis\"];");
			gv.addln("}");

			gv.addln("{");
			gv.addln("node [shape=\"box\"];");
			gv.add("sm"+mseq.get(0));
			for (int i = 1; i < mseq.size(); i++)
				gv.addln(" -> sm"+mseq.get(i));
			gv.addln("[style=\"invis\"];");
			gv.addln("}");


			// force aligned markables to be on the same row
			Vector<Integer> aligned = new Vector<Integer>();
			aligned.addAll(m2aid_gold.values());
			aligned.retainAll(m2aid_sys.values());
			for (int id : aligned) {
				gv.addln("{ rank=same;");
				gv.add("gm"+id+"; ");
				gv.addln("sm"+id+"; }");
			}

			// print markable offsets and text
			for (int i : mseq) {
				if (aid2m_gold.containsKey(i)) {
					Span span = aid2m_gold.get(i);
					gv.addln("gm"+i+"[label=\""+span.toString()+"\\n"+
							coveredText(docTxt, span)+"\",style=\"filled\","+
							"color=\"/set312/"+(eqv_gold[i-1]%12==0?12:eqv_gold[i-1]%12)+"\","+
							"tooltip=\""+surroundingText(docTxt, span, debug_context, debug_context)+"\"];");
				} else {
					gv.addln("gm"+i+"[style=\"invis\"];");
				}
				if (aid2m_sys.containsKey(i)) {
					Span span = aid2m_sys.get(i);
					gv.addln("sm"+i+"[label=\""+span.toString()+"\\n"+
							coveredText(docTxt, span)+"\",style=\"filled\","+
							"color=\"/set312/"+(eqv_sys[i-1]%12==0?12:eqv_sys[i-1]%12)+"\","+
							"tooltip=\""+surroundingText(docTxt, span, debug_context, debug_context)+"\"];");
				} else {
					gv.addln("sm"+i+"[style=\"invis\"];");
				}
			}


			// print edges between antecedent and anaphor
			for (Span[] p : pairs_gold)
				gv.addln("gm"+m2aid_gold.get(p[1])+" -> gm"+m2aid_gold.get(p[0])+"[constraint=false];");
			for (Span[] p : pairs_sys)
				gv.addln("sm"+m2aid_sys.get(p[1])+" -> sm"+m2aid_sys.get(p[0])+"[constraint=false];");
		}

		gv.addln(gv.end_graph());
		String ftype = "svg";
		File ofile = new File (debug_out_path+File.separator+toFile+"."+ftype);
		gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), ftype), ofile);
		if(debug_out_path != null){
			try {
				java.io.BufferedWriter o = new java.io.BufferedWriter(new java.io.FileWriter(debug_out_path+File.separator+toFile+".dot"));
				o.write(gv.getDotSource());
				o.close();
			} catch (IOException e) { e.printStackTrace(); }
		}

	}

	private Vector<Integer> mid_seq (
			Vector<Span> markables,
			Hashtable<Span, Integer> m2aid_mine,
			Hashtable<Integer, Span> aid2m_mine,
			Hashtable<Span, Integer> m2aid_other,
			Hashtable<Integer, Span> aid2m_other) {
		Vector<Integer> ret = new Vector<Integer>();

		int i = 0;
		int _id = dotMarkableID(markables.get(i++), m2aid_mine, aid2m_mine, m2aid_other, aid2m_other);
		while (_id < 0)
			_id = dotMarkableID(markables.get(i++), m2aid_mine, aid2m_mine, m2aid_other, aid2m_other);
		ret.add(_id);
		while (i < markables.size()) {
			_id = dotMarkableID(markables.get(i++), m2aid_mine, aid2m_mine, m2aid_other, aid2m_other);
			if (_id > 0) ret.add(_id);
		}
		return ret;
	}

	private int dotMarkableID (
			Span markable,
			Hashtable<Span, Integer> m2aid_mine,
			Hashtable<Integer, Span> aid2m_mine,
			Hashtable<Span, Integer> m2aid_other,
			Hashtable<Integer, Span> aid2m_other) {
		int _id = -1;
		SpanOffsetComparator soc = new SpanOffsetComparator();
		//		int aligned = 0;  //  0 : not aligned,
		// <0 : markable is aligned to another preceding markable
		// >0 : markable is aligned to another following markable

		if (m2aid_mine.containsKey(markable)) {
			int aid_mine = m2aid_mine.get(markable);
			if (!aid2m_other.containsKey(aid_mine)) 
				_id = aid_mine;
			else if (soc.compare(aid2m_other.get(aid_mine), markable)<=0)
				_id = aid_mine;
		} else {
			int aid_other = m2aid_other.get(markable);
			if (!aid2m_mine.containsKey(aid_other))
				_id = aid_other;
			else if (soc.compare(aid2m_mine.get(aid_other), markable)<=0)
				_id = aid_other;
		}
		return _id;
	}

	private String coveredText (String doc, Span s) {
		String ret = "";
		if (s.size()>1) {
			int[] subspan = s.get(0);
			ret = doc.substring(subspan[0], subspan[1]);
			for (int i = 1; i < s.size(); i++) {
				subspan = s.get(i);
				ret += "..."+doc.substring(subspan[0], subspan[1]);
			}
		} else {
			ret = doc.substring(s.get(0)[0], s.get(0)[1]);
		}
		return ret.replaceAll("\\\n", "\\\\n").replaceAll("\"", "\\\\\"");
	}

	private String surroundingText (String doc, Span s, int left, int right) {
		int start = s.get(0)[0] - left;
		int end = s.get(s.size()-1)[1] + right;
		if (start<0) start = 0;
		if (end>doc.length()) end = doc.length();
		return doc.substring(start, end).replaceAll("\\\n", " ").//replaceAll("[\"\\[\\]]", "");
		replaceAll("\"", "\\\\\"");//.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
	}
}
