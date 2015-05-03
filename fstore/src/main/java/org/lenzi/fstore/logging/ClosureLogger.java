package org.lenzi.fstore.logging;

import java.util.List;

import org.lenzi.fstore.repository.model.DBClosure;
import org.lenzi.fstore.repository.model.impl.FSNode;
import org.lenzi.fstore.stereotype.InjectLogger;
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
		logger.info("Closure list size => " + closureList.size());
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
				logger.info("link id: " + c.getLinkId() + ", parent: " + getNodeString(parent) + ", child: " + getNodeString(child));
			}else if(parent != null){
				logger.info("link id: " + c.getLinkId() + ", parent: " + getNodeString(parent));
			}else if(child != null){
				logger.info("link id: " + c.getLinkId() + ", child: " + getNodeString(child));
			}
		}
	}
	public String getNodeString(N n){
		if(n == null){
			return "null";
		}
		return "{id = " + n.getNodeId() + ", name = " + n.getName() + ", paren Id = " + n.getParentNodeId() + 
				", dt created = " + n.getDateCreated() + ", dt updated = " + n.getDateUpdated() + "}";
	}
	private String padLeft(String s, int n){
		return String.format("%1$" + n + "s", s); 
	}

}
