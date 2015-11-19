package org.lenzi.fstore.core.util;

public abstract class NumUtil {

	/**
	 * If l is null, return new Long set to 0, otherwise return l.
	 * 
	 * @param l
	 * @return
	 */
	public static Long changeNull(Long l){
		if(l == null){
			return new Long(0);
		}
		return l;
	}

}
