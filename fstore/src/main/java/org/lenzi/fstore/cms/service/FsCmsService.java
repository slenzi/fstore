/**
 * 
 */
package org.lenzi.fstore.cms.service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import org.lenzi.fstore.cms.repository.FsCmsSiteAdder;
import org.lenzi.fstore.cms.repository.FsCmsSiteRepository;
import org.lenzi.fstore.cms.repository.model.impl.FsCmsSite;
import org.lenzi.fstore.core.repository.exception.DatabaseException;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.main.properties.ManagedProperties;
import org.lenzi.fstore.web.util.AppServices;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for CMS related actions
 * 
 * @author sal
 */
@Service
@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Throwable.class)
public class FsCmsService {

	@InjectLogger
	private Logger logger;
	
	@Autowired
	private AppServices appServices;
	
    @Autowired
    private ManagedProperties appProps; 	
	
	//@Autowired
	//ServletContext servletContext = null;
	
	//
	// cms site operators
	//
	@Autowired
	private FsCmsSiteAdder fsCmsSiteAdder;
	@Autowired
	private FsCmsSiteRepository fsCmsSiteRepository;
	
	/**
	 * 
	 */
	public FsCmsService() {

	}
	
	/**
	 * Create new CMS site entry
	 * 
	 * @param siteName - name of site, keep short. will show in URL. a new resource store will be created at [fstore]/WEB-INF/jsp/[siteName]
	 * @param description - site description
	 * @param clearIfExists - clear existing resource store path if it already exists
	 * @return
	 * @throws ServiceException
	 */
	public FsCmsSite createSite(String siteName, String description, boolean clearIfExists) throws ServiceException {
	
		String appPath = appServices.getRuntimePath();
		String sitesRoot = appProps.getProperty("cms.sites.root");
		Path sitePath = Paths.get(appPath + sitesRoot + File.separator + siteName);
		
		siteName += "CMS: ";
		
		FsCmsSite site = null;
		try {
			site = fsCmsSiteAdder.createCmsSite(sitePath, siteName, description, clearIfExists);
		} catch (DatabaseException e) {
			throw new ServiceException("Error creating cms site => [ path =" + sitePath + ", name = " + 
					siteName + ", clear if exists = " + clearIfExists + "]. " + e.getMessage(), e);
		}
		
		return site;
		
	}

}
