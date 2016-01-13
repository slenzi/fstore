package org.lenzi.fstore.cms.api;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.lenzi.fstore.cms.service.FsCmsService;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * Exposes various CMS functionality to CMS developers. For example, for use in their JSPs.
 * 
 * Use proxy mode so we can wire this session bean into our (singleton) controllers.
 * 
 * @author sal
 */
@Service
@Scope(value = "session", proxyMode= ScopedProxyMode.TARGET_CLASS)
public class CmsService implements Serializable {

	private static final long serialVersionUID = 1712930986532324253L;

	@InjectLogger
	private Logger logger;
	
    @Autowired
    private ManagedProperties appProps;  
    
    @Autowired
    private CmsPath cmsPath;
	
	public CmsService() {
		
	}
	
	/**
	 * Get new CmsLink object for creating CMS ahref links.
	 * 
	 * @return
	 */
	public CmsLink getCmsLink(){
		return CmsLink.getInstance(getCurrentHttpServletRequest());
	}
	
	/**
	 * Get CmsPath object for creating local CMS file system paths.
	 * 
	 * @return
	 */
	public CmsPath getCmsPath(){
		return cmsPath;
	}
	
	/**
	 * Get the root directory for all CMS sites
	 * 
	 * @return
	 */
	public String getCmsSiteRoot(){
		
		return appProps.getProperty("cms.sites.root");
		
	}
	
	/**
	 * Get current HttpServletRequest
	 * 
	 * @return
	 */
	public HttpServletRequest getCurrentHttpServletRequest(){
		return ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	}
	
	/**
	 * Get HttpSession object
	 *  
	 * @return
	 */
	public HttpSession getHttpSession(){
		return getCurrentHttpServletRequest().getSession();
	}
	
	/**
	 * Check if we have the http session object
	 * 
	 * @return
	 */
	public boolean haveHttpSession(){
		
		return getHttpSession() != null ? true : false;
		
	}

}
