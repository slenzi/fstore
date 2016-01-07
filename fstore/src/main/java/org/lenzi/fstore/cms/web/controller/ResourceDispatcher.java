package org.lenzi.fstore.cms.web.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.lenzi.fstore.cms.constants.CmsConstants;
import org.lenzi.fstore.core.repository.security.model.impl.FsUser;
import org.lenzi.fstore.core.security.FsSecureUser;
import org.lenzi.fstore.core.security.service.FsSecurityService;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.StringUtil;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.lenzi.fstore.web.controller.AbstractSpringController;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Dispatches requests to CMS resources under /WEB-INF/jsp
 * 
 * @author sal
 */
@Controller
@RequestMapping("/cms/**")
public class ResourceDispatcher extends AbstractSpringController {

	@InjectLogger
	private Logger logger;	
	
    @Autowired
    private ManagedProperties appProps; 	
    
    @Autowired
    private FsSecurityService fsSecurityService;
    
	
	public ResourceDispatcher() {
		
	}
	
	/**
	 * Forward the request to the resource that's mapped in the request URL (the part after /cms)
	 * 
	 * If the user is logged into fstore, check if they are in 'online' or 'offline' mode. If offline mode then
	 * forward the request to the offline version of the resource, otherwise forward to the online version. When in
	 * doubt assume the online version. (feature coming shortly.) 
	 * 
	 * @param request
	 * @param response
	 * @param model
	 */
	@RequestMapping(method = RequestMethod.GET)
	public void dispatchResource(HttpServletRequest request, HttpServletResponse response, ModelMap model){
		
		logDebugUserDetails();
		
		String resourcePath = "/" + extractPathFromPattern(request);
		String sitesRoot = appProps.getProperty("cms.sites.root");
		String sitesOnline = appProps.getProperty("cms.sites.online");
		String sitesOffline = appProps.getProperty("cms.sites.offline");
		
		// assume online, and check for offline
		String sitePath = sitesOnline;
		boolean isOffline = isOfflineMode(request);
		if(isOffline){
			sitePath = sitesOffline;
			logger.info("CMS offline mode is on.");
		}
		
		String forwardPath = sitePath + resourcePath;
		
		logger.debug("Dispatch forward => " + forwardPath);
		
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
	
	/**
	 * Check if the users is currently viewing in CMS OFFLINE mode. Default mode is ONLINE.
	 * 
	 * @param request
	 * @return
	 */
	private boolean isOfflineMode(final HttpServletRequest request){
		
		HttpSession session = request.getSession();
		String cmsViewMode = StringUtil.changeNull((String)session.getAttribute(CmsConstants.SESSION_CMS_VIEW_MODE)).trim();
		
		// assume online mode, and check for offline mode
		
		if(cmsViewMode.equals("OFFLINE")){
			return true;
		}
		
		return false;
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
	
	/**
	 * Logs all details of current logged in user (debug level.)
	 */
	private void logDebugUserDetails(){
		
		FsSecureUser principalUser = fsSecurityService.getLoggedInUser();

		boolean havePricipalUser = (principalUser != null) ? true : false;
		boolean haveFsUser = (havePricipalUser && principalUser.getFsUser() != null) ? true : false;
		
		String username = fsSecurityService.getUsername();
		List<String> authorityNames = fsSecurityService.getAuthorities();
	
		logger.debug("Logged in user details: ");
		logger.debug("Have principal user (spring security) => " + havePricipalUser);
		logger.debug("Have FsUser => " + haveFsUser);
		
		if(authorityNames != null){
			authorityNames.forEach((role) -> {
				logger.debug("Granted Authority => " + role);
			});
		}		
		
		if(haveFsUser){
			FsUser fsUser = principalUser.getFsUser();
			logger.debug("User ID => " + fsUser.getUserId());
			logger.debug("First name => " + fsUser.getFirstName());
			logger.debug("Last name => " + fsUser.getLastName());
			logger.debug("Primary Email => " + fsUser.getPrimaryEmail());
			logger.debug("Role count => " + fsUser.roleCount());
			logger.debug("Group count => " + fsUser.groupCount());
		}
		
		logger.debug("Username => " + username);		
		
	}

}
