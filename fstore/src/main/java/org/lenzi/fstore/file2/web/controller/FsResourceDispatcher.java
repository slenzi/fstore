/**
 * 
 */
package org.lenzi.fstore.file2.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.example.service.TestTreeService;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    
	/**
	 * 
	 */
	public FsResourceDispatcher() {
		
	}
	
	//@RequestMapping("/**")
	
	@RequestMapping("/{storeId}/{fileId}")
	public String dispatchResource(
			@PathVariable("storeId") Long storeId, @PathVariable("fileId") Long fileId,
			HttpServletRequest request, HttpServletResponse response, ModelMap model){
	
		logger.info("Dispatching request for URL => " + request.getRequestURL());
		
		// TODO - if file data is on file system, then use it, otherwise go to database.
		
		FsFileMetaResource fileResource = null;
		
		try {
			fileResource = fsResourceService.getFileResource(fileId, FsFileResourceFetch.FILE_META);
		} catch (ServiceException e) {
			handleError(logger,"Error fetching file, id = " + fileId, model, e);
		}
		
		// TODO - function for getting store from file ID...
		
		byte[] fileData = fileResource.getFileResource().getFileData();
		
		return "/test/filetest/test.jsp";
		
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
