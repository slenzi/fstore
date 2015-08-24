package org.lenzi.fstore.cms.service;

import java.util.ArrayList;
import java.util.List;

import org.lenzi.fstore.cms.repository.model.impl.FsCmsSite;
import org.lenzi.fstore.cms.web.rs.model.JsCmsSite;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.DateUtil;
import org.lenzi.fstore.file2.service.FsResourceJsonHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Helper class for converting objects to JSON.
 * 
 * @author sal
 */
@Service
public class FsCmsJsonHelper {

	@InjectLogger
	private Logger logger;	
	
	@Autowired
	private FsResourceJsonHelper fsResourceJsonHelper;
	
	public FsCmsJsonHelper() {
		
	}
	
	/**
	 *  Convert database layer cms site object to service layer object
	 * 
	 * @param sites
	 * @return
	 */
	public List<JsCmsSite> convertSites(List<FsCmsSite> sites){
		
		if(sites == null){
			return null;
		}
		List<JsCmsSite> jsSites = new ArrayList<JsCmsSite>();
		for(FsCmsSite site : sites){
			jsSites.add(convertSite(site));
		}
		return jsSites;
		
	}	
	
	/**
	 * Convert database layer cms site object to service layer object
	 * 
	 * @param site
	 * @return
	 */
	public JsCmsSite convertSite(FsCmsSite site){
		
		JsCmsSite js = new JsCmsSite();
		
		js.setId(String.valueOf(site.getSiteId()));
		js.setName(site.getName());
		js.setDescription(site.getDescription());		
		js.setDateCreated(DateUtil.defaultFormat(site.getDateCreated()));
		js.setDateUpdated(DateUtil.defaultFormat(site.getDateUpdated()));
		
		if(site.getOfflineResourceStore() != null){
			js.setOfflineStore(fsResourceJsonHelper.convertStore(site.getOfflineResourceStore()));
		}
		
		if(site.getOnlineResourceStore() != null){
			js.setOnlineStore(fsResourceJsonHelper.convertStore(site.getOnlineResourceStore()));
		}		
		
		return js;
		
	}

}
