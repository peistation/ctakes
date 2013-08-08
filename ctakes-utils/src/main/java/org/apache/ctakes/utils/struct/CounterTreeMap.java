package org.apache.ctakes.utils.struct;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeMap;

// This class is a simplifying class which makes it easy to build hashes to keep track of counts
// and write less boilerplate code.  If you just call it with an object, it will increment the
// object's count by 1, initializing it to zero first if necessary.
public class CounterTreeMap<K> {

//	IntValueComparator<K> comp = new IntValueComparator<K>();
	TreeMap<K,Integer> map = null;
	
	public CounterTreeMap(){
		map = new TreeMap<K,Integer>();
//		super(this);
	}
	
	public Integer get(Object key) {
		if(map.containsKey(key))	return map.get(key);
		return 0;
	}
	
	public void add(K key){
		add(key, 1);
	}
	
	public void add(K key, Integer i){
		if(!map.containsKey(key)){
			map.put(key,0); 
		}
		map.put(key, map.get(key)+i);
	}

	public int size(){
		return map.size();
	}
	
//	@Override
//	public int compare(K o1, K o2) {
//		return o1 - o2;
//	}

	public Set<K> keySet() {
		return map.keySet();
	}
	
}
