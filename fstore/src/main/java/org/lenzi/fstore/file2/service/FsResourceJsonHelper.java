package org.lenzi.fstore.file2.service;

import java.util.ArrayList;
import java.util.List;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.tree.TreeNode;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathResource;
import org.lenzi.fstore.file2.repository.model.impl.FsPathType;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.web.rs.model.JsResourceStore;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Helper class for converting objects to JSON.
 * 
 * @author sal
 */
@Service
public class FsResourceJsonHelper {

	@InjectLogger
	private Logger logger;	
	
	public FsResourceJsonHelper() {
		
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
	
	/**
	 * Convert database layer FsResourceStore to web service layer JsResourceStore
	 * 
	 * @param stores
	 * @return
	 */
	public List<JsResourceStore> convertStore(List<FsResourceStore> stores){
		
		if(stores == null){
			return null;
		}
		List<JsResourceStore> jsStores = new ArrayList<JsResourceStore>();
		for(FsResourceStore store : stores){
			jsStores.add(convertStore(store));
		}
		return jsStores;
		
	}
	
	/**
	 * Convert database layer FsResourceStore to web service layer JsResourceStore
	 * 
	 * @param store
	 * @return
	 */
	public JsResourceStore convertStore(FsResourceStore store){
		
		JsResourceStore js = new JsResourceStore();
		
		js.setId(String.valueOf(store.getStoreId()));
		js.setName(store.getName());
		js.setDescription(store.getDescription());
		js.setStorePath(store.getStorePath());
		js.setDateCreated(DateUtil.defaultFormat(store.getDateCreated()));
		js.setDateUpdated(DateUtil.defaultFormat(store.getDateUpdated()));
		js.setRootDirectoryId(String.valueOf(store.getNodeId()));
		
		return js;
		
	}	

}
