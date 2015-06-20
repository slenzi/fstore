/**
 * 
 */
package org.lenzi.fstore.file2.web.controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Handles requests and serves up resources from fstore.
 * 
 * @author slenzi
 */
@Controller
@RequestMapping("/file2/res")
public class FsResourceDispatcher extends AbstractSpringController {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private TestTreeService treeService;
	
    @Autowired
    private FsQueuedResourceService fsResourceService;
    
    @Autowired
    ServletContext context;
    
	/**
	 * 
	 */
	public FsResourceDispatcher() {
		
	}
	
	//@RequestMapping("/**")
	
	@RequestMapping(
			value = "/{storeId}/{fileId}", 
			method = RequestMethod.GET
			)
	public HttpEntity<byte[]> dispatchResource(
			@PathVariable("storeId") Long storeId, @PathVariable("fileId") Long fileId){
	
		//logger.info("Dispatching request for URL => " + request.getRequestURL());
		
		// TODO - if file data is on file system, then use it, otherwise go to database.
		
		// TODO - function for getting store from file ID...
		
		FsFileMetaResource fileResource = null;
		
		try {
			fileResource = fsResourceService.getFileResource(fileId, FsFileResourceFetch.FILE_META_WITH_DATA);
		} catch (ServiceException e) {
			logger.error("Failed to fetch file data from database, " + e.getMessage(), e);
			//handleError(logger,"Error fetching file, id = " + fileId, model, e);
		}
		
		String mimeType = fileResource.getMimeType();
		
		byte[] fileData = fileResource.getFileResource().getFileData();
		
		logger.info("Sending file, name => " + fileResource.getName() + ", fs meta mime => " + fileResource.getMimeType() +
				", byte size => " + fileData.length);
	
		final HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.parseMediaType(mimeType));
	    headers.setContentLength(fileData.length);
	    headers.setContentDispositionFormData(fileResource.getName(), fileResource.getName());
	    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
	    
	    logger.info("Spring media type => " + MediaType.valueOf(mimeType));
	    
	    //return new ResponseEntity<byte[]>(fileData, headers, HttpStatus.OK);
	    return new HttpEntity<byte[]>(fileData, headers);
		
	}
	
	/**
	 * Extract path from a controller mapping. /controllerUrl/** => return matched **
	 * 
	 * @param request incoming request.
	 * @return extracted path
	 */
	public static String extractPathFromPattern(final HttpServletRequest request){

	    String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
	    String bestMatchPattern = (String ) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
	    
	    AntPathMatcher apm = new AntPathMatcher();
	    String finalPath = apm.extractPathWithinPattern(bestMatchPattern, path);

	    return finalPath;

	}

}
