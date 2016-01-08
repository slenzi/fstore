/**
 * 
 */
package org.lenzi.fstore.cms.web.rs;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lenzi.fstore.cms.constants.CmsConstants;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.core.util.StringUtil;
import org.lenzi.fstore.web.rs.AbstractResource;
import org.lenzi.fstore.web.rs.exception.WebServiceException;
import org.lenzi.fstore.web.rs.exception.WebServiceException.WebExceptionType;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;


/**
 * Controls http session attributes related to CMS section.
 * 
 * @author sal
 */
@Path( "/httpsession")
@Service("CmsHttpSessionResource")
public class CmsHttpSessionResource extends AbstractResource {

    @InjectLogger
    Logger logger;
    
    @Context
    private HttpServletRequest servletRequest;
	
	public CmsHttpSessionResource(){
		
	}
	
	/**
	 * Fetch current CMS view mode, either OFFLINE or ONLINE.
	 * 
	 * @return
	 * @throws WebServiceException
	 */
	@GET
	@Path("/viewmode")
	@Produces(MediaType.APPLICATION_JSON)
	public Response fetchViewMode() throws WebServiceException{
	
		logger.info(CmsHttpSessionResource.class.getName() + " fetchViewMode()");
	
		String viewMode = StringUtil.changeNull((String)servletRequest.getSession()
				.getAttribute(CmsConstants.SESSION_CMS_VIEW_MODE)).trim().toUpperCase();
		
		// assume online if currently not set
		if(viewMode.equals("")){
			viewMode = "ONLINE";
		}
		
		return Response.ok("{ \"" + CmsConstants.SESSION_CMS_VIEW_MODE + "\": \"" + viewMode + "\" }", MediaType.APPLICATION_JSON).build();
		
	}	
	
	/**
	 * Toggle CMS OFFLINE/ONLINE mode.
	 * 
	 * @throws WebServiceException 
	 */
	@POST
	@Path("/viewmode/{mode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response toggleCmsViewMode(@PathParam("mode") String mode) throws WebServiceException{
	
		logger.info(CmsHttpSessionResource.class.getName() + " toggleCmsViewMode(...) mode = " + mode);
		
		String viewMode = StringUtil.changeNull(mode).toUpperCase();
		
		if(!viewMode.equals("OFFLINE") && !viewMode.equals("ONLINE")){
			handleError("Missing CMS view mode in path. Acceptable options are 'OFFLINE' or 'ONLINE'", WebExceptionType.CODE_INVALID_INPUT);
		}else if(servletRequest == null){
			handleError("HttpServletRequest is null, check injection.", WebExceptionType.CODE_NOT_FOUND);
		}
		
		servletRequest.getSession().setAttribute(CmsConstants.SESSION_CMS_VIEW_MODE, viewMode);
		
		return Response.ok("{ \"message\": \"ok\" }", MediaType.APPLICATION_JSON).build();
		
	}	
	
	@Override
	public Logger getLogger() {
		return logger;
	}
	
	


}
