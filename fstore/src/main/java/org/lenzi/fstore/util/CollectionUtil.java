package org.lenzi.fstore.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class CollectionUtil {

	/**
	 * Check if collection is empty.
	 * 
	 * @param c
	 * @return
	 */
	public static <T> boolean isEmpty(Collection<T> c){
		return ((c == null || c.size() == 0) ? true : false);
	}
	
	/**
	 * If iterable is null return empty list, otherwise return iterable.
	 * 
	 * e.g.
	 * for (Object object : emptyIfNull(someList)) {
	 * 	 // do something
	 * }
	 * 
	 * @param iterable
	 * @return
	 */
	public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
	    return iterable == null ? Collections.<T>emptyList() : iterable;
	}
	
	/**
	 * If c is null return empty list, otherwise return list.
	 * 
	 * @param c
	 * @return
	 */
	public static <T> List<T> emptyListIfNull(List<T> c) {
		
		return c == null ? Collections.<T>emptyList() : c;
		
	}

}
