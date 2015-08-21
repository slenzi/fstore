/**
 * 
 */
package org.lenzi.fstore.cms.api;

import org.lenzi.fstore.cms.constants.CmsConstants;

/**
 * Create links on CMS pages. Will create path to CMS dispatcher.
 * 
 * @author sal
 */
public class CmsLink {

	private static CmsLink link = null;
	
	private CmsLink() {

	}
	
	public static CmsLink getInstance(){
		if(link == null){
			link = new CmsLink();
		}
		return link;
	}
	
	public String createLink(String sitePath){
		
		if(sitePath == null){
			sitePath = "/";
		}
		if(!sitePath.startsWith("/")){
			sitePath = "/" + sitePath;
		}
		
		return CmsConstants.DISPATCHER_MAPPING + sitePath;
		
	}

}
