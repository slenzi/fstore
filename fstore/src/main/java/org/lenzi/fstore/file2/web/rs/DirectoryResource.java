package org.lenzi.fstore.file2.web.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.Tree;
import org.lenzi.fstore.core.tree.TreeNodeVisitException;
import org.lenzi.fstore.core.tree.Trees;
import org.lenzi.fstore.core.tree.Trees.WalkOption;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file.repository.model.impl.FsDirectory;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsDirectoryResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsFileResource;
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
		
		// convert tree to JSON
		StringBuffer jsonTree = new StringBuffer();
		jsonTree.append("{");
		try {
			
			Trees.walkTree(tree,
				(treeNode) -> {
				
					logger.info("Convert tree node to JSON => " + treeNode.getData().getPathType().getType());
					
					FsPathResource pathResource = treeNode.getData();
					
					jsonTree.append(" \"name\" : \"" + pathResource.getName() + "\" ");
					jsonTree.append(" \"type\" : \"" + pathResource.getPathType().getType() + "\" ");
					jsonTree.append(" \"dateCreated\" : \"" + DateUtil.defaultFormat(pathResource.getDateCreated()) + "\" ");
					jsonTree.append(" \"dateUpdated\" : \"" + DateUtil.defaultFormat(pathResource.getDateUpdated()) + "\" ");
					
					if(treeNode.getData().getPathType().getType().equals(FsPathType.FILE.getType())){
						
						FsFileMetaResource fileResource = (FsFileMetaResource)treeNode.getData();
						
						jsonTree.append(" \"size\" : \"" + fileResource.getFileSize() + "\" ");
						jsonTree.append(" \"mimeType\" : \"" + fileResource.getMimeType() + "\" ");
						
					}else if(treeNode.getData().getPathType().getType().equals(FsPathType.DIRECTORY.getType())){
						
						FsDirectoryResource directoryResource = (FsDirectoryResource)treeNode.getData();
						
						jsonTree.append(" \"children\" : \"\" ");
						
					}else{
						
						throw new TreeNodeVisitException("Don't know how to convert tree node type " + 
								treeNode.getData().getPathType().getType() +  " to JSON");
						
					}
				
			}, WalkOption.PRE_ORDER_TRAVERSAL);
			
		} catch (TreeNodeVisitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jsonTree.append("}");
		
		return Response.ok("{ \"error\" : \"feature not implemented yet\"}", MediaType.APPLICATION_JSON).build();
		
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}	

}
