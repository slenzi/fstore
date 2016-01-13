package org.lenzi.fstore.cms.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.lenzi.fstore.cms.exception.CmsException;
import org.lenzi.fstore.cms.service.FsCmsService;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.StringUtil;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Helper for creating local file system paths to cms resources.
 * 
 * For example, use with the jsp:include tag (specify jsp path)
 * 
 * @author sal
 */
@Service
public class CmsPath {

	@InjectLogger
	private Logger logger;
	
    @Autowired
    private ManagedProperties appProps;
    
    @Autowired
    private FsCmsService fsCmsService;      
	
	public CmsPath() {
		
	}
	
	private HttpServletRequest getCurrentHttpServletRequest(){
		return ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	}
	
	public HttpSession getHttpSession(){
		return getCurrentHttpServletRequest().getSession();
	}	
	
	/**
	 * e.g. createPath("/test/includes/test.jsp") will create the following:
	 * 
	 * [site_offline_root]/test/includes/test.jsp
	 * 	or,
	 * [site_online_root]/test/includes/test.jsp
	 * 
	 * depending on if the user is in 'online' or 'offline' mode
	 * 
	 * i.e.
	 * 
	 * /WEB-INF/jsp/cms/sites/offline/test/includes/test.jsp, or,
	 * /WEB-INF/jsp/cms/sites/online/test/includes/test.jsp
	 * 
	 * @param siteResourcePath
	 * @return
	 */
	public String createPath(String siteResourcePath) throws CmsException {
		
		if(StringUtil.changeNull(siteResourcePath).equals("")){
			throw new CmsException(CmsPath.class.getName() + ".createPath(...) cannot create cms path, require param is null or empty.");
		}
		siteResourcePath = siteResourcePath.replace("\\", "/");
		if(!siteResourcePath.startsWith("/")){
			siteResourcePath = "/" + siteResourcePath;
		}
		if(fsCmsService.isOfflineMode()){
			
			// create path to offline resource
			String sitesOffline = appProps.getProperty("cms.sites.offline");
			return sitesOffline + siteResourcePath;
			
		}else{
			
			// create path to online resource
			String sitesOnline = appProps.getProperty("cms.sites.online");
			return sitesOnline + siteResourcePath;
		}
		
	}

}
