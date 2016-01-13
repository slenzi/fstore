/**
 * 
 */
package org.lenzi.fstore.cms.constants;

/**
 * @author sal
 *
 */
public final class CmsConstants {

	public static final String DISPATCHER_MAPPING = "/spring/cms";
	
	
	/**
	 * Session Variables
	 */
	
	// Currently two possibilities, ONLINE or OFFLINE. Defaults to ONLINE. 
	public static String SESSION_CMS_VIEW_MODE = "SESSION_CMS_VIEW_MODE";
	
	// when we use the dispatcher to forward to JSPS, we store the CmsService object
	// in this session attribute. CmsService lets the developer get access to various
	// CMS helper functions & tools.
	public static String SESSION_ATT_CMS_SERVICE = "SESSION_ATT_CMS_SERVICE";	

	/**
	 * Request attributes
	 */
	
}