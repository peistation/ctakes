package org.apache.ctakes.utils.wiki;

public class ApproximateMath {
	private static int numRoots = 10000;
	private static float[] roots = new float[numRoots];
	private static int cacheHit = 0;
	private static int cacheMiss = 0;
	
	private static int numLogs = 10000;
	private static float[] logs = new float[numLogs];
	static{
		for(int i = 0; i < numRoots; i++){
			roots[i] = (float) Math.sqrt(i);
		}
		
		for(int i = 0; i < numLogs; i++){
			logs[i] = (float) Math.log(i);
		}
	}

	public static final double asqrt(int i){
		if(i < numRoots){ cacheHit++;  return roots[i]; }
		else{ cacheMiss++; return Math.sqrt(i); }
	}
	
	public static final double asqrt(double d){
		if(d < numRoots) return roots[(int)d];
		else return Math.sqrt(d);
	}
	
	public static final double alog(double d){
		if(d < numLogs){ cacheHit++; return logs[(int)d];}
		else{ cacheMiss++; return Math.log(d);}
	}
	
	public static void dumpCache(){
		System.out.println(cacheHit + " cache hits");
		System.out.println(cacheMiss + " cache misses");
	}
}
