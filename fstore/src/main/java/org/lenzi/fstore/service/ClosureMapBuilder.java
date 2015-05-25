/**
 * 
 */
package org.lenzi.fstore.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.util.CollectionUtil;
import org.springframework.stereotype.Service;

/**
 * @author slenzi
 *
 */
@Service
public class ClosureMapBuilder<N extends FSNode<N>> {

	/**
	 * 
	 */
	public ClosureMapBuilder() {
		
	}
	
	/**
	 * Builds a map where the keys are node IDs, and the values are Lists of DBNode objects.
	 * 
	 * @param closureList
	 * @return
	 */
	public HashMap<Long,List<N>> buildMapFromClosure(List<DBClosure<N>> closureList) {
		
		if(CollectionUtil.isEmpty(closureList)){
			return null;
		}
		
		DBClosure<N> closure = null;
		HashMap<Long,List<N>> map = new HashMap<Long,List<N>>();
		
		for(int closureIndex=0; closureIndex<closureList.size(); closureIndex++){
			closure = closureList.get(closureIndex);
			if(closure.hasParent() && closure.hasChild()){
				if(map.containsKey(closure.getParentNode().getNodeId())){
					map.get(closure.getParentNode().getNodeId()).add(closure.getChildNode());
				}else{
					List<N> childList = new ArrayList<N>();
					childList.add(closure.getChildNode());
					map.put(closure.getParentNode().getNodeId(), childList);
				}
			}
		}
		
		return map;
	}
	
	/**
	 * Builds a map where the keys are node IDs, and the values are Lists of DBNode objects.
	 * 
	 * @param closureSet
	 * @return
	 */
	public HashMap<Long,List<N>> buildMapFromClosure(Set<DBClosure<N>> closureSet) {
		
		List<DBClosure<N>> list = new ArrayList<DBClosure<N>>();
		list.addAll(closureSet);
		
		return buildMapFromClosure(list);
		
	}

}
