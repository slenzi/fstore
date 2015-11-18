package org.lenzi.fstore.core.logging;

import java.util.List;
import java.util.Set;

import org.lenzi.fstore.core.repository.tree.model.DBClosure;
import org.lenzi.fstore.core.repository.tree.model.impl.FSNode;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class ClosureLogger<N extends FSNode<N>> {

	@InjectLogger
	private Logger logger;

	public ClosureLogger(){}
	
	public void logClosure(List<DBClosure<N>> closureList){
		if(closureList == null){
			return;
		}
		logger.debug("Closure list size => " + closureList.size());
		N parent = null, child = null;
		Integer depth = 0;
		for(DBClosure<N> c : closureList){
			parent = c.getParentNode();
			child = c.getChildNode();
			depth = c.getDepth();
			if(parent == null && child == null){
				logger.warn("link id: " + c.getLinkId() + ", parent: null, child: null");
			}
			if(parent != null && child != null){
				logger.info("link id: " + c.getLinkId() + ", depth: " + depth + ", parent: " + getNodeString(parent) + ", child: " + getNodeString(child));
			}else if(parent != null){
				logger.info("link id: " + c.getLinkId() + ", depth: " + depth + ", parent: " + getNodeString(parent));
			}else if(child != null){
				logger.info("link id: " + c.getLinkId() + ", depth: " + depth + ", child: " + getNodeString(child));
			}
		}
	}
	
	public void logClosure(Set<DBClosure<N>> closureList){
		if(closureList == null){
			return;
		}
		logger.debug("Closure list size => " + closureList.size());
		N parent = null, child = null;
		Integer depth = 0;
		for(DBClosure<N> c : closureList){
			parent = c.getParentNode();
			child = c.getChildNode();
			depth = c.getDepth();
			if(parent == null && child == null){
				logger.warn("link id: " + c.getLinkId() + ", parent: null, child: null");
			}
			if(parent != null && child != null){
				logger.info("link id: " + c.getLinkId() + ", depth: " + depth + ", parent: " + getNodeString(parent) + ", child: " + getNodeString(child));
			}else if(parent != null){
				logger.info("link id: " + c.getLinkId() + ", depth: " + depth + ", parent: " + getNodeString(parent));
			}else if(child != null){
				logger.info("link id: " + c.getLinkId() + ", depth: " + depth + ", child: " + getNodeString(child));
			}
		}
	}	
	
	public String getNodeString(N n){
		if(n == null){
			return "null";
		}
		return n.toString();
	}
	private String padLeft(String s, int n){
		return String.format("%1$" + n + "s", s); 
	}

}
