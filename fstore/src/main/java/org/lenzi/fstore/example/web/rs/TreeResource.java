package org.lenzi.fstore.example.web.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.example.repository.model.impl.FSTestNode;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.web.rs.exception.WebServiceException;
import org.lenzi.fstore.web.rs.exception.WebServiceException.WebExceptionType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Path( "/tree")
@Service("TreeResource")
public class TreeResource {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private TestTreeService testTreeService;
    
	/**
	 * Get service name
	 * 
	 * @return JSON object
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getServiceName(){
		logger.info(TreeResource.class.getName() + ".getServiceName() called");
		return TreeResource.class.getName();
	}    
	
    /**
     * Fetch tree by id
     * 
     * @param treeid
     * @return
     * @throws WebServiceException
     */
	@GET
	@Path("{treeid}")
	@Produces(MediaType.APPLICATION_JSON)
    public Tree<FSTestNode> getTreeById(@PathParam("treeid") String treeid) throws WebServiceException {
    	
		logger.info("Fetch tree by id = " + treeid);	
		
		Long longTreeId = null;
		try {
			longTreeId = Long.parseLong(treeid);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			logger.error("Error parsing tree ID parameter: " + e.getMessage());
			throw new WebServiceException(WebExceptionType.CODE_INVALID_INPUT,
					"Error parsing tree ID parameter: " + e.getMessage());
		}    	
    	

		Tree<FSTestNode> tree = null;
		try {
			tree = testTreeService.buildTree(new FSTestNode(longTreeId));
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error("Error building tree for tree id " + longTreeId + ". " + e.getMessage());
			throw new WebServiceException(WebExceptionType.CODE_INVALID_INPUT,
					"Error building tree for tree id " + longTreeId + ". " + e.getMessage());
		}
		
		return tree;
    	
    }
    
}
