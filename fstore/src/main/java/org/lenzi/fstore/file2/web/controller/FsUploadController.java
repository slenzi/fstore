/**
 * 
 */
package org.lenzi.fstore.file2.web.controller;

import java.nio.file.Path;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

//used before upgrading to hibernate 5. now we use org.springframework.transaction.annotation.Transactional
//import javax.transaction.Transactional;


import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.StringUtil;
import org.lenzi.fstore.file2.service.FsUploadPipeline;
import org.lenzi.fstore.file2.web.messaging.UploadMessageService;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
    
    @Autowired
    private FsUploadPipeline uploadPipeline;
    
    @Autowired
    private UploadMessageService uploadMessageService;

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
		
		// get id of destination directory
		String dirId = StringUtil.changeNull(request.getParameter("dirId")).trim();
		if(dirId.equals("")){
			handleError(logger, "Failed to process upload. Parent directory ID is missing in request.", model);
			return "Failed to process upload. Parent directory ID is missing in request.";			
		}
		Long parentDirId = 0L;
		try {
			parentDirId = Long.valueOf(dirId);
		} catch (NumberFormatException e) {
			handleError(logger, "Failed to parse parent ID to long. " + e.getMessage(), model);
			return "Failed to parse parent ID to long. " + e.getMessage();
		}
		
		// make sure we actually have some uploaded data
		Map<String, MultipartFile> fileMap = request.getFileMap();
		if(fileMap == null){
			handleError(logger, "No multipart file data found in request.", model);
		}
		
		logger.info("File map contains " + fileMap.values().size() + " entries.");
		
		// send upload received message to client/s
		fileMap.values().stream().forEach(
			(filePart) -> {
				logger.info("Received file: " + filePart.getOriginalFilename() + ", " + filePart.getSize() + " bytes.");
				uploadMessageService.sendUploadReceivedMessage(filePart.getOriginalFilename());
			});
		
		// save files to temporary directory
		Path tempDir = null;
		try {
			tempDir = uploadPipeline.processToTemp(fileMap);
		} catch (ServiceException e) {
			handleError(logger, "Failed to process uploaded files to temporary directory. " + e.getMessage(), model);
			return "Failed to process uploaded files to temporary directory. " + e.getMessage();
		}
		
		// submit files to fstore database
		try {
			uploadPipeline.processToDirectory(tempDir, parentDirId, true);
		} catch (ServiceException e) {
			handleError(logger, "Failed to process uploaded files to directory " + parentDirId + ". " + e.getMessage(), model);
			return "Failed to process uploaded files to directory " + parentDirId + ". " + e.getMessage();
		}
		
		logger.info("Upload controller complete");
		
		return "ok";
		
	}

}
