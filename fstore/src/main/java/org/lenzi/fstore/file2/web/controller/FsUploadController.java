/**
 * 
 */
package org.lenzi.fstore.file2.web.controller;

import java.nio.file.Path;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.lenzi.fstore.core.security.FsSecureUser;
import org.lenzi.fstore.core.security.service.FsSecurityService;

//used before upgrading to hibernate 5. now we use org.springframework.transaction.annotation.Transactional
//import javax.transaction.Transactional;








import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.StringUtil;
import org.lenzi.fstore.file2.service.FsUploadPipeline;
import org.lenzi.fstore.file2.web.messaging.UploadMessageService;
import org.lenzi.fstore.web.constants.WebConstants;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.lenzi.fstore.web.exception.WebAppException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

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
    
	//
	// ACLs security
	//
	@Autowired
	private FsSecurityService fsSecurityService;     

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
	// @Transactional
	@RequestMapping(
			method = {
					RequestMethod.POST
			})
	@ResponseStatus(value = HttpStatus.OK)	
	private void processUpload(MultipartHttpServletRequest request, HttpServletResponse resp, ModelMap model) throws WebAppException {
		
		logger.info("Processing incoming HTTP upload");
		
		// see who is performing an upload
		FsSecureUser fsUser = fsSecurityService.getLoggedInUser();
		if(fsUser == null){
			throw new WebAppException("FsSecureUser is null. Cannot determine who is submitting an HTTP upload. Check security setup.");
		}
		logger.info("User [id='" + fsUser.getFsUser().getUserId() + "', name='" + fsUser.getUsername() + "'] is submitting an HTTP upload");	
		
		// get id of destination directory
		String dirId = StringUtil.changeNull(request.getParameter("dirId")).trim();
		if(dirId.equals("")){
			throw new WebAppException("Failed to process upload. Parent directory ID is missing in request.");
		}
		Long parentDirId = 0L;
		try {
			parentDirId = Long.valueOf(dirId);
		} catch (NumberFormatException e) {
			throw new WebAppException("Failed to parse parent dir ID to long. " + e.getMessage(), e);
		}
		
		// make sure we actually have some uploaded data
		Map<String, MultipartFile> fileMap = request.getFileMap();
		if(fileMap == null){
			throw new WebAppException("No multipart file data found in request.");
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
			throw new WebAppException("Failed to process uploaded files to temporary directory. " + e.getMessage(), e);
		}
		
		// submit files to fstore database
		try {
			uploadPipeline.processToDirectory(tempDir, parentDirId, true);
		} catch (ServiceException e) {
			throw new WebAppException("Failed to process uploaded files to directory " + parentDirId + ". " + e.getMessage(), e);
		}
		
		logger.info("Upload controller complete");
		
	}
	
	/**
	 * Handle web app exceptions thrown by the controller
	 * 
	 * @param e
	 * @return
	 */
	@ExceptionHandler(WebAppException.class)
	public ModelAndView handleCustomException(WebAppException e) {

		logger.error(e.getMessage(), e);
		
		ModelAndView model = new ModelAndView("error/error_page");
		
		model.addObject(WebConstants.APP_REQUEST_ERROR_MESSAGE, e.getMessage());
		model.addObject("errorMessage", e.getMessage());
		
		model.addObject("stackTrace", e.getStackTrace());

		return model;

	}	

}
