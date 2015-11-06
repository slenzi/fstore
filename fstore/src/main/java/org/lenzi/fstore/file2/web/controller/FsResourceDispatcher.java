/**
 * 
 */
package org.lenzi.fstore.file2.web.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceHelper;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.lenzi.fstore.web.rs.exception.WebServiceException.WebExceptionType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
    private Logger logger;
	
    @Autowired
    private ManagedProperties appProps;
    
    @Autowired
    private FsQueuedResourceService fsQueuedResourceService;
    
    @Autowired
    private FsResourceService fsResourceService;
    
    @Autowired
    private FsResourceHelper fsResourceHelper;
    
    @Autowired
    private ServletContext context;
    
	public FsResourceDispatcher() {
		
	}
	
	//@RequestMapping("/**")
	
	/**
	 * Download file resource. User will be prompted with a save/save-as dialog to download/save the file.
	 * 
	 * @param fileId - id of file resource to download
	 * @return
	 */
	@RequestMapping(
			value = "/download/id/{fileId}", 
			method = RequestMethod.GET
			)
	public HttpEntity<byte[]> downloadFileResourceById(@PathVariable("fileId") Long fileId){
		
		// TODO - if file data is on file system, then use it, otherwise go to database.

		FsFileMetaResource fileResource = getFileById(fileId);
		String mimeType = fileResource.getMimeType();
		byte[] fileData = null;
		
		boolean isFileDataInDatabase = fileResource.isFileDataInDatabase();
		if(isFileDataInDatabase){
			fileData = fileResource.getFileResource().getFileData();
		}else{
			// file probably too big to store in database, get file data from file system
			FsResourceStore store = null;
			try {
				store = fsResourceService.getStoreByPathResourceId(fileId);
			} catch (ServiceException e) {
				logger.error("Failed to fetch resource store for file resource with id => " + fileId, e);
				throw new RuntimeException(e);
			}
			java.nio.file.Path filePath = fsResourceHelper.getAbsoluteFilePath(store, fileResource);
			try {
				fileData = Files.readAllBytes(filePath);
			} catch (IOException e) {
				logger.error("Failed to read file " + filePath, e);
				throw new RuntimeException(e);
			}
		}
		
		//logger.info("Download file, name => " + fileResource.getName() + ", fs meta mime => " + fileResource.getMimeType() +
		//		", byte size => " + fileData.length);
	
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
	 * Load file resource. File should be loaded directly in the clients browser for mime types that the browser knows
	 * how to load. For other types the user should get a save/save-as dialog
	 * 
	 * @param fileId - id of file resource to load
	 * @return
	 */
	@RequestMapping(
			value = "/load/id/{fileId}", 
			method = RequestMethod.GET
			)
	public ResponseEntity<InputStreamResource> loadFileResourceById(@PathVariable("fileId") Long fileId){
		
		// TODO - if file data is on file system, then use it, otherwise go to database.

		FsFileMetaResource fileResource = getFileById(fileId);
		String mimeType = fileResource.getMimeType();
		byte[] fileData = fileResource.getFileResource().getFileData();
		
		//logger.info("Load file, name => " + fileResource.getName() + ", fs meta mime => " + fileResource.getMimeType() +
		//		", byte size => " + fileData.length);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
		
		return ResponseEntity.ok()
	            .contentLength(fileData.length)
	            .contentType(MediaType.parseMediaType(mimeType))
	            .body(new InputStreamResource(bis));
		
	}
	
	/**
	 * Load file resource. File should be loaded directly in the clients browser for mime types that the browser knows
	 * how to load. For other types the user should get a save/save-as dialog
	 * 
	 * @param path - resource store root dir name + file relative path
	 * @return
	 */
	@RequestMapping(
			value = "/load/path/**", 
			method = RequestMethod.GET
			)
	public ResponseEntity<InputStreamResource> loadFileResourceByPath(HttpServletRequest request, HttpServletResponse response){
		
		// TODO - if file data is on file system, then use it, otherwise go to database.

		// extract the remainder of the URL (the ** part)
		String filePath = extractPathFromPattern(request);
		
		logger.info("loading file path before => " + filePath);
		//filePath = filePath.replace("/", File.separator); // convert web path separator to file path separator
		logger.info("loading file path after => " + filePath);
		
		FsFileMetaResource fileResource = this.getFileByPath(filePath);
		String mimeType = fileResource.getMimeType();
		byte[] fileData = fileResource.getFileResource().getFileData();
		
		//logger.info("Load file, name => " + fileResource.getName() + ", fs meta mime => " + fileResource.getMimeType() +
		//		", byte size => " + fileData.length);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
		
		//request.getRequestDispatcher("/file.jsp").forward(request, response);
		
		//request.getRequestDispatcher(arg0)
		
		return ResponseEntity.ok()
	            .contentLength(fileData.length)
	            .contentType(MediaType.parseMediaType(mimeType))
	            .body(new InputStreamResource(bis));
		
	}
	
	@RequestMapping(
			value = "/dispatch/**", 
			method = RequestMethod.GET
			)
	public void dispatchResource(HttpServletRequest request, HttpServletResponse response){
		
		String resourcePath = "/" + extractPathFromPattern(request);
		//String springDispatcher = "/spring";
		//String appContext = appProps.getProperty("application.context");
		//String dispatchLoadPath = "/file2/res/load/path";
		
		String jspHome = "/WEB-INF/jsp";
		String forwardPath = jspHome + resourcePath;
		
		//String fullResourcePath = appContext + springDispatcher + dispatchLoadPath + resourcePath;
		
		logger.info("Dispatch forward => " + forwardPath);
		
		RequestDispatcher requestDispatcher = request.getRequestDispatcher(forwardPath);
		
		try {
			requestDispatcher.forward(request, response);
		} catch (ServletException e) {
			logger.error("Servlet expception thrown while attempting to use RequestDispatcher to inlcude resource, " + forwardPath);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IO expception thrown while attempting to use RequestDispatcher to inlcude resource, " + forwardPath);
			e.printStackTrace();
		}
		
	}
	
	/*
	public void dispatchResource(HttpServletRequest request, HttpServletResponse response){
		
		String resourcePath = "/" + extractPathFromPattern(request);
		String springDispatcher = "/spring";
		String appContext = appProps.getProperty("application.context");
		String dispatchLoadPath = "/file2/res/load/path";
		
		String fullResourcePath = appContext + springDispatcher + dispatchLoadPath + resourcePath;
		
		logger.info("Dispatching path => " + fullResourcePath);
		
		RequestDispatcher requestDispatcher = request.getRequestDispatcher(fullResourcePath);
		
		try {
			requestDispatcher.include(request, response);
		} catch (ServletException e) {
			logger.error("Servlet expception thrown while attempting to use RequestDispatcher to inlcude resource, " + fullResourcePath);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error("IO expception thrown while attempting to use RequestDispatcher to inlcude resource, " + fullResourcePath);
			e.printStackTrace();
		}
		
	}
	 */
	
	/*
	public ResponseEntity<InputStreamResource> loadFileResourceByPath(HttpServletRequest request, HttpServletResponse response){
		
		// TODO - if file data is on file system, then use it, otherwise go to database.

		// extract the remainder of the URL (the ** part)
		String filePath = extractPathFromPattern(request);
		
		logger.info("loading file path before => " + filePath);
		//filePath = filePath.replace("/", File.separator); // convert web path separator to file path separator
		logger.info("loading file path after => " + filePath);
		
		FsFileMetaResource fileResource = this.getFileByPath(filePath);
		String mimeType = fileResource.getMimeType();
		byte[] fileData = fileResource.getFileResource().getFileData();
		
		//logger.info("Load file, name => " + fileResource.getName() + ", fs meta mime => " + fileResource.getMimeType() +
		//		", byte size => " + fileData.length);
		
		ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
		
		//request.getRequestDispatcher("/file.jsp").forward(request, response);
		
		return ResponseEntity.ok()
	            .contentLength(fileData.length)
	            .contentType(MediaType.parseMediaType(mimeType))
	            .body(new InputStreamResource(bis));
		
	}
	 */
	
	/**
	 * Fetch file data from database, including byte data
	 * 
	 * @param fileId - id of file resource
	 * @return
	 * @throws ServiceException
	 */
	private FsFileMetaResource getFileById(Long fileId) {
		
		FsFileMetaResource fileResource = null;
		try {
			fileResource = fsQueuedResourceService.getFileResourceById(fileId, FsFileResourceFetch.FILE_META_WITH_DATA);
		} catch (ServiceException e) {
			logger.error("Failed to fetch file data from database, " + e.getMessage(), e);
		}
		return fileResource;
		
	}
	
	/**
	 * Fetch file data from database, including byte data
	 * 
	 * @param path - resource store root dir name + file relative path
	 * @return
	 * @throws ServiceException
	 */
	private FsFileMetaResource getFileByPath(String path) {
		
		FsFileMetaResource fileResource = null;
		try {
			fileResource = fsQueuedResourceService.getFileResourceByPath(path, FsFileResourceFetch.FILE_META_WITH_DATA);
		} catch (ServiceException e) {
			logger.error("Failed to fetch file data from database, " + e.getMessage(), e);
		}
		return fileResource;
		
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
