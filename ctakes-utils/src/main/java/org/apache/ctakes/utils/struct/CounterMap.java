package org.apache.ctakes.utils.struct;

import java.util.HashMap;

// This class is a simplifying class which makes it easy to build hashes to keep track of counts
// and write less boilerplate code.  If you just call it with an object, it will increment the
// object's count by 1, initializing it to zero first if necessary.
public class CounterMap<K> extends HashMap<K, java.lang.Integer> {

	@Override
	public Integer get(Object key) {
		if(super.containsKey(key))	return super.get(key);
		else{
			return 0;
		}
	}
	
	public void add(K key){
		add(key, 1);
	}
	
	public void add(K key, Integer i){
		if(!super.containsKey(key)){
			super.put(key,0); 
		}
		super.put(key, super.get(key)+i);
	}
}
