/**
* Copyright (c) 2009, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package clear.util;

import gnu.trove.TIntArrayList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * Data structure utilities.
 * @author Jinho D. Choi
 * <b>Last update:</b> 6/30/2010
 */
public class DSUtil
{
        /** @return List of integers converted from <code>strArr</code> */
        static public TIntArrayList toTIntArrayList(String[] strArr)
        {
                TIntArrayList list = new TIntArrayList(strArr.length);
                
                for (String str : strArr)
                        list.add(Integer.parseInt(str));
                
                return list;
        }
               
        /** @return HashSet contains strings from <code>strArr</code> */
        static public HashSet<String> toHashSet(String[] strArr)
        {
                HashSet<String> set = new HashSet<String>(strArr.length);
                
                for (String str : strArr)
                        set.add(str);
                
                return set;
        }
        
        /** @return HashMap whose keys are strings from <code>list</code> and values are sequential integers starting at <code>beginId</code> */
        static public ObjectIntOpenHashMap<String> toHashMap(ArrayList<String> list, int beginId)
        {
                ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<String>(list.size());
                
                for (int i=0; i<list.size(); i++)
                        map.put(list.get(i), i+beginId);
                
                return map;
        }
        
        
        static public int max(int[] arr)
        {
                int max = Integer.MIN_VALUE;
                for (int i : arr)       max = Math.max(max, i);
                
                return max;
        }
        
        static public int max(ArrayList<Integer> arrlist)
        {
                int max = Integer.MIN_VALUE;
                for (int i : arrlist)   max = Math.max(max, i);
                
                return max;
        }
        
        static public double max(double[] arr)
        {
                double max = Double.MIN_VALUE;
                for (double x : arr)    max = Math.max(max, x);
                
                return max;
        }
        
        static public double min(double[] arr)
        {
                double max = Double.MAX_VALUE;
                for (double x : arr)    max = Math.max(max, x);
                
                return max;
        }
        
        static public int[] toIntArray(StringTokenizer tok)
        {
                int[] arr = new int[tok.countTokens()];
                
                for (int i=0; i<arr.length; i++)
                        arr[i] = Integer.parseInt(tok.nextToken());
                
                return arr;
        }
        
        static public double[] toDoubleArray(StringTokenizer tok)
        {
                double[] arr = new double[tok.countTokens()];
                
                for (int i=0; i<arr.length; i++)
                        arr[i] = Double.parseDouble(tok.nextToken());
                
                return arr;
        }
        
        static public String[] toStringArray(StringTokenizer tok)
        {
                String[] arr = new String[tok.countTokens()];
                
                for (int i=0; i<arr.length; i++)
                        arr[i] = tok.nextToken();
                
                return arr;
        }
        
        static public ArrayList<Integer> toArrayList(StringTokenizer tok)
        {
                ArrayList<Integer> arrlist = new ArrayList<Integer>(tok.countTokens());
                
                while (tok.hasMoreTokens())
                        arrlist.add(Integer.parseInt(tok.nextToken()));
                
                return arrlist;
        }
        
        static public ArrayList<Integer> toArrayList(int[] arr)
        {
                ArrayList<Integer> arrlist = new ArrayList<Integer>(arr.length);
                
                for (int value : arr)   arrlist.add(value);
                return arrlist;
        }
        
        static public String toString(ArrayList<Integer> arr, String delim)
        {
                String str = "";
                for (int i : arr)       str += i + delim;
                
                return str.trim();
        }
        
        static public String toString(int[] arr, String delim)
        {
                String str = "";
                for (int i : arr)       str += i + delim;
                
                return str.trim();
        }
        
        // copy d1 = d2 
        static public void copy(double[] d1, double[] d2)
        {
                for (int i=0; i < d1.length && i < d2.length; i++)
                        d1[i] = d2[i];
        }
}
