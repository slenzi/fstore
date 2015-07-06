package org.lenzi.fstore.file2.web.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNode;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
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
    private FsQueuedResourceService fsResourceService;
	
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
		if(!rootResource.getPathType().equals(FsPathType.DIRECTORY.getType())){
			handleError("Root resource of fetched tree is not a directory, dirId param = " + dirId + ", maxDepth = " + maxDepth,
					WebExceptionType.CODE_DATABSE_ERROR);
		}
		
		// convert tree to JSON
		String jsonTree = toJsonTree(rootNode);
		
		logger.debug("JSON Tree = " + jsonTree);
		
		return Response.ok(jsonTree, MediaType.APPLICATION_JSON).build();
		
	}
	
	/**
	 * Convert tree to JSON
	 * 
	 * @param node
	 * @return
	 */
	private String toJsonTree(TreeNode<FsPathResource> node){
		return toJsonTree(node, new StringBuffer(), "");
	}
	
	/**
	 * Convert tree to JSON
	 * 
	 * @param node
	 * @param jsonData
	 * @param delim
	 * @return
	 */
	private String toJsonTree(TreeNode<FsPathResource> node, StringBuffer jsonData, String delim){
	
		jsonData.append(delim + "{");
		
		FsPathResource resource = node.getData();
		
		// convert file
		if(resource.getPathType().getType().equals(FsPathType.FILE.getType())){
			
			jsonData.append(" \"name\" : \"" + resource.getName() + "\" ");
			jsonData.append(", \"type\" : \"" + resource.getPathType().getType() + "\" ");
			jsonData.append(", \"dateCreated\" : \"" + DateUtil.defaultFormat(resource.getDateCreated()) + "\" ");
			jsonData.append(", \"dateUpdated\" : \"" + DateUtil.defaultFormat(resource.getDateUpdated()) + "\" ");
			jsonData.append(", \"size\" : \"" + ((FsFileMetaResource)resource).getFileSize() + "\" ");
			jsonData.append(", \"mimeType\" : \"" + ((FsFileMetaResource)resource).getMimeType() + "\" ");			
		
		// convert directory, and recursively all children
		}else if(resource.getPathType().getType().equals(FsPathType.DIRECTORY.getType())){
			
			jsonData.append(" \"name\" : \"" + resource.getName() + "\" ");
			jsonData.append(", \"type\" : \"" + resource.getPathType().getType() + "\" ");
			jsonData.append(", \"dateCreated\" : \"" + DateUtil.defaultFormat(resource.getDateCreated()) + "\" ");
			jsonData.append(", \"dateUpdated\" : \"" + DateUtil.defaultFormat(resource.getDateUpdated()) + "\" ");
			
			if(node.hasChildren()){
				
				String arraydelim = "";
				jsonData.append(", \"children\" : [");
				for(TreeNode<FsPathResource> childResource : node.getChildren()){
					jsonData.append( toJsonTree(childResource, new StringBuffer(), arraydelim) );
					arraydelim = ",";
				}
				jsonData.append("]");
				
			}else{
				
				jsonData.append(", \"children\" : [] ");
				
			}
			
		}
		
		jsonData.append("}");
		
		return jsonData.toString();
	}	
	
	@Override
	public Logger getLogger() {
		return logger;
	}	

}
