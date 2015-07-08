package org.lenzi.fstore.file2.service;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.TreeNode;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Helper class for converting objects to JSON.
 * 
 * @author sal
 */
@Service
public class FsJsonHelper {

	@InjectLogger
	private Logger logger;	
	
	public FsJsonHelper() {
		
	}
	
	/**
	 * Convert tree to JSON
	 * 
	 * @param node
	 * @return
	 */
	public String toJsonTree(TreeNode<FsPathResource> node) throws ServiceException {
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
	private String toJsonTree(TreeNode<FsPathResource> node, StringBuffer jsonData, String delim) throws ServiceException {
	
		FsPathResource resource = node.getData();
		
		jsonData.append(delim + "{");
		
		// convert file
		if(resource.getPathTypeValue().equals(FsPathType.FILE.getType())){
			
			// file id == node id
			jsonData.append(" \"fileId\" : \"" + resource.getNodeId() + "\" ");
			jsonData.append(", \"name\" : \"" + resource.getName() + "\" ");
			jsonData.append(", \"type\" : \"" + resource.getPathType().getType() + "\" ");
			jsonData.append(", \"dateCreated\" : \"" + DateUtil.defaultFormat(resource.getDateCreated()) + "\" ");
			jsonData.append(", \"dateUpdated\" : \"" + DateUtil.defaultFormat(resource.getDateUpdated()) + "\" ");
			jsonData.append(", \"size\" : \"" + ((FsFileMetaResource)resource).getFileSize() + "\" ");
			jsonData.append(", \"mimeType\" : \"" + ((FsFileMetaResource)resource).getMimeType() + "\" ");			
		
		// convert directory
		}else if(resource.getPathTypeValue().equals(FsPathType.DIRECTORY.getType())){
			
			// dir id == node id
			jsonData.append(" \"dirId\" : \"" + resource.getNodeId() + "\" ");
			jsonData.append(", \"name\" : \"" + resource.getName() + "\" ");
			jsonData.append(", \"type\" : \"" + resource.getPathType().getType() + "\" ");
			jsonData.append(", \"dateCreated\" : \"" + DateUtil.defaultFormat(resource.getDateCreated()) + "\" ");
			jsonData.append(", \"dateUpdated\" : \"" + DateUtil.defaultFormat(resource.getDateUpdated()) + "\" ");
			
			// recursively convert all sub directories and files
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
			
		}else{
			
			throw new ServiceException("Unknown resource path type '" + resource.getPathTypeValue() + "' for node id " + resource.getNodeId() + 
					", node name " + resource.getName() + ". Don't know how to convert this node type to JSON.");	
			
		}
		
		jsonData.append("}");
		
		return jsonData.toString();
	}	

}
