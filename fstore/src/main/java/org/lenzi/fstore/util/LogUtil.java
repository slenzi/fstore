package org.lenzi.fstore.util;

import java.util.List;

import org.lenzi.fstore.repository.model.Closure;
import org.lenzi.fstore.repository.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LogUtil {

	private static Logger logger = LoggerFactory.getLogger(LogUtil.class.getName());
	
	public static void logClosure(List<Closure> closureList){
		if(closureList == null){
			return;
		}
		logger.info("Closure list size => " + closureList.size());
		Node parent = null, child = null;
		Integer depth = 0;
		for(Closure c : closureList){
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
	public static String getNodeString(Node n){
		if(n == null){
			return "null";
		}
		return "{id = " + n.getNodeId() + ", name = " + n.getName() + ", paren Id = " + n.getParentNodeId() + 
				", dt created = " + n.getDateCreated() + ", dt updated = " + n.getDateUpdated() + "}";
	}
	private static String padLeft(String s, int n){
		return String.format("%1$" + n + "s", s); 
	}

}
