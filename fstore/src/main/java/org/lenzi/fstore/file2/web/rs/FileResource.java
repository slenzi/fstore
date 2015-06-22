package org.lenzi.fstore.file2.web.rs;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.web.rs.exception.WebServiceException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Path( "/file")
@Service("FileResource")
public class FileResource {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private FsQueuedResourceService fsResourceService;
	
	public FileResource() {
		
	}
	
	@GET
	@Path("/{fileId}")
	@Produces("application/octet-stream")
	public Response getFile(@PathParam("fileId") Long fileId, @Context HttpServletResponse response) throws WebServiceException {
		
		logger.info(FileResource.class.getName() + " jax-rs service called, fileId = " + fileId);
		
		return Response.ok().build();
		
	}

}
