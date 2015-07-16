/**
 * 
 */
package org.lenzi.fstore.file2.web.controller;

import java.nio.file.Paths;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsUploadPipeline;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Process HTTP multi-part uploads.
 * 
 * @author sal
 */
@Controller
@RequestMapping("/file2/upload")
public class FsUploadController extends AbstractSpringController {

    @InjectLogger
    private Logger logger;
    
    //@Autowired
    //private ManagedProperties appProps;    
    
    //@Autowired
    //private FsQueuedResourceService fsResourceService;
    
    @Autowired
    private FsUploadPipeline uploadPipeline;

	public FsUploadController() {
		
	}
	
	/**
	 * Process incoming data
	 * 
	 * @param request
	 * @param resp
	 * @param model
	 * @return
	 */
	@RequestMapping(
			method = {
					RequestMethod.POST
			})
	@Transactional	
	private String processUpload(MultipartHttpServletRequest request, HttpServletResponse resp, ModelMap model){
		
		logger.info("Processing incoming HTTP upload");
		
		Map<String, MultipartFile> fileMap = request.getFileMap();
		if(fileMap == null){
			handleError(logger, "No multipart file data found in request.", model);
		}
		
		logger.info("File map contains " + fileMap.values().size() + " entries.");
		
		fileMap.values().stream().forEach(
			(filePart) -> {
				logger.info("Received file: " + filePart.getOriginalFilename() + ", " + filePart.getSize() + " bytes.");
			});
		
		// save all files to holding store
		try {
			uploadPipeline.processToHolding(fileMap);
		} catch (ServiceException e) {
			handleError(logger, "Failed to process upload to holding store", model);
			return "error, Failed to process upload to holding store";
		}
		
		logger.info("Upload processing complete");
		
		return "ok";
		
	}

}
