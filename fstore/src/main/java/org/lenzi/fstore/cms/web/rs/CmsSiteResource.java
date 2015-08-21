/**
 * 
 */
package org.lenzi.fstore.cms.web.rs;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.lenzi.fstore.cms.repository.model.impl.FsCmsSite;
import org.lenzi.fstore.cms.service.FsCmsService;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.web.rs.AbstractResource;
import org.lenzi.fstore.web.rs.exception.WebServiceException;
import org.lenzi.fstore.web.rs.exception.WebServiceException.WebExceptionType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sal
 *
 */
@Path( "/site")
@Service("CmsSiteResource")
public class CmsSiteResource extends AbstractResource {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private FsCmsService cmsService;
	
	/**
	 * 
	 */
	public CmsSiteResource() {
		
	}
	
	/**
	 * Add new CMS site
	 * 
	 * @return
	 * @throws WebServiceException
	 */
	@GET
	@POST
	@Path("/add")
	@Produces(MediaType.APPLICATION_JSON)
	public Response addSite() throws WebServiceException {
		
		logger.info("Adding CMS site...");
		
		FsCmsSite site = null;
		try {
			site = cmsService.createSite("testing", "this is a test cms site", true);
		} catch (ServiceException e) {
			handleError("Failed to create new cms site.",
					WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		
		return Response.ok("{ \"message\": \"ok\" }", MediaType.APPLICATION_JSON).build();
		
	}

	/* (non-Javadoc)
	 * @see org.lenzi.fstore.web.rs.AbstractResource#getLogger()
	 */
	@Override
	public Logger getLogger() {
		return logger;
	}

}
