/**
 * 
 */
package org.lenzi.fstore.cms.api;

import javax.servlet.http.HttpServletRequest;

import org.lenzi.fstore.cms.constants.CmsConstants;

/**
 * Create links on CMS pages. Will create path to CMS dispatcher.
 * 
 * @author sal
 */
public class CmsLink {
	
	private HttpServletRequest request = null;
	
	private CmsLink(HttpServletRequest request) {
		this.request = request;
	}
	
	public static CmsLink getInstance(HttpServletRequest request){
		
		return new CmsLink(request);
		
	}
	
	public String createLink(String sitePath){
		
		if(sitePath == null){
			sitePath = "/";
		}
		if(!sitePath.startsWith("/")){
			sitePath = "/" + sitePath;
		}
		
		return request.getContextPath() + CmsConstants.DISPATCHER_MAPPING + sitePath;
		
	}

}
