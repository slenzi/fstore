package org.lenzi.fstore.file2.web.rs;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNode;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
import org.lenzi.fstore.file2.service.FsJsonHelper;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.lenzi.fstore.web.rs.AbstractResource;
import org.lenzi.fstore.web.rs.exception.WebServiceException;
import org.lenzi.fstore.web.rs.exception.WebServiceException.WebExceptionType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Jax-rs resource for serving up directory data.
 * 
 * @author slenzi
 */
@Path( "/directory")
@Service("DirectoryResource")
public class DirectoryResource extends AbstractResource {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private FsQueuedResourceService fsQueuedResourceService;
    
    @Autowired
    private FsResourceService fsResourceService;    
    
    @Autowired
    private FsJsonHelper fsJsonHelper;
	
	public DirectoryResource() {

	}
	
	/**
	 * Fetch directory tree listing up to a maximum depth.
	 * 
	 * @param dirId - id of the directory
	 * @param maxDepth
	 * @return
	 * @throws WebServiceException
	 */
	@GET
	@Path("/{dirId}/depth/{maxDepth}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDirectory(@PathParam("dirId") Long dirId, @PathParam("maxDepth") Integer maxDepth) throws WebServiceException {
		
		logger.info("Fetching directory tree for dir id " + dirId + " up to max depth " + maxDepth);
		
		Tree<FsPathResource> tree = null;
		try {
			tree = fsResourceService.getPathResourceTree(dirId, maxDepth);
		} catch (ServiceException e) {
			handleError("Failed to fetch directory tree for dir id " + dirId + ", max depth " + maxDepth,
					WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		
		// error checking
		TreeNode<FsPathResource> rootNode = tree.getRootNode();
		FsPathResource rootResource = rootNode.getData();	
		if(!rootResource.getPathTypeValue().equals(FsPathType.DIRECTORY.getType())){
			handleError("Root resource of fetched tree is not a directory, dirId param = " + dirId + ", maxDepth = " + maxDepth,
					WebExceptionType.CODE_DATABSE_ERROR);
		}
		
		// sort first level child nodes. uses the default FsPathResource compareTo method
		rootNode.sortChildren(
				(node1, node2) -> {
					return node1.getData().compareTo(node2.getData());
				});
		
		// convert tree to JSON
		String jsonTree = null;
		try {
			jsonTree = fsJsonHelper.toJsonTree(rootNode);
		} catch (ServiceException e) {
			handleError(e.getMessage(),WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		
		logger.debug("JSON Tree = " + jsonTree);
		
		return Response.ok(jsonTree, MediaType.APPLICATION_JSON).build();
		
	}
	
	/**
	 * Fetch parent tree / breadcrumb for some child directory
	 * 
	 * @param dirId
	 * @return
	 * @throws WebServiceException
	 */
	@GET
	@Path("/breadcrumb/{dirId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDirectoryBreadcrumb(@PathParam("dirId") Long dirId) throws WebServiceException {
		
		logger.info("Fetching directory breadcrumb for dir id " + dirId);
		
		Tree<FsPathResource> tree = null;
		try {
			tree = fsResourceService.getParentPathResourceTree(dirId);
		} catch (ServiceException e) {
			handleError("Failed to fetch parent tree for dir id " + dirId,
					WebExceptionType.CODE_DATABSE_ERROR, e);
		}		
		
		// convert tree to JSON
		String jsonTree = null;
		try {
			jsonTree = fsJsonHelper.toJsonTree(tree.getRootNode());
		} catch (ServiceException e) {
			handleError(e.getMessage(),WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		
		logger.debug("JSON Tree = " + jsonTree);
		
		return Response.ok(jsonTree, MediaType.APPLICATION_JSON).build();
		
	}
	
	/**
	 * Delete a series of directories
	 * 
	 * @param dirIdList - list of file ids. All files will be deleted.
	 * @return
	 * @throws WebServiceException
	 */
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteDirectories(@QueryParam("dirId") List<Long> dirIdList) throws WebServiceException {
		
		logger.info(DirectoryResource.class.getName() + " jax-rs service called, delete directories");
		
		if(dirIdList == null || dirIdList.size() == 0){
			throw new WebServiceException(WebExceptionType.CODE_MISSING_REQUIRED_INPUT,
					"List of dir IDs for delete operation is null or empty. Need 1 or more 'dirId' query parameters in request.");
		}
		
		String delim = "";
		StringBuffer dirIdLog = new StringBuffer();
		for(Long dirId : dirIdList){
			dirIdLog.append(delim + dirId); delim = ",";
		}
		
		logger.info("Number of directories to delete: " + dirIdList.size() + ". [" + dirIdLog.toString() + "]");
		
		try {
			fsQueuedResourceService.removeDirectoryResourceList(dirIdList);
		} catch (ServiceException e) {
			handleError("Failed to delete directory data from server. Dir Id list = [" + dirIdLog.toString() + "]",
					WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		
		return Response.ok("{ \"message\": \"ok\" }", MediaType.APPLICATION_JSON).build();
		
	}
	
	/**
	 * Add new directory
	 * 
	 * @param dirId - id of parent directory
	 * @param dirName - name of new directory
	 * @return
	 * @throws WebServiceException
	 */
	@POST
	@Path("/add")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addDirectory(@QueryParam("dirId") Long dirId, @QueryParam("dirName") String dirName) throws WebServiceException {
		
		logger.info(DirectoryResource.class.getName() + " jax-rs service called, add directory");
	
		if(dirId == null || dirName == null || dirId == 0 || dirName.trim().equals("")){
			throw new WebServiceException(WebExceptionType.CODE_MISSING_REQUIRED_INPUT,
					"Missing required input in request. Need name of new directory and ID of parent directory.");
		}
		
		try {
			fsResourceService.addDirectoryResource(dirId, dirName);
		} catch (ServiceException e) {
			handleError("Failed to create new directory. [ parent dir id = " + dirId + ", new dir name = " + dirName + "]",
					WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		
		return Response.ok("{ \"message\": \"ok\" }", MediaType.APPLICATION_JSON).build();
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}	

}
