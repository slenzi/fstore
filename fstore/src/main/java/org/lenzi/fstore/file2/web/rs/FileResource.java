package org.lenzi.fstore.file2.web.rs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.cxf.attachment.ContentDisposition;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.web.rs.exception.WebServiceException;
import org.lenzi.fstore.web.rs.exception.WebServiceException.WebExceptionType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Jax-rs resource for serving up files.
 * 
 * @author slenzi
 */
@Path( "/file")
@Service("FileResource")
public class FileResource extends AbstractResource {

    @InjectLogger
    Logger logger;
    
    @Autowired
    private FsQueuedResourceService fsResourceService;
	
	public FileResource() {
		
	}
	
	/**
	 * Download file resource
	 * 
	 * @param fileId - id of file resource to download
	 * @return
	 * @throws WebServiceException
	 */
	@GET
	@Path("/download/id/{fileId}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getFile(@PathParam("fileId") Long fileId) throws WebServiceException {
		
		logger.info(FileResource.class.getName() + " jax-rs service called, fileId = " + fileId);
		
		// TODO - stream file from database rather than loading entire file into memory / byte[]
		
		//
		// pull file from database
		//
		FsFileMetaResource fileResource = null;
		try {
			fileResource = fsResourceService.getFileResourceById(fileId, FsFileResourceFetch.FILE_META_WITH_DATA);
		} catch (ServiceException e) {
			handleError("Failed to fetch file data from database", WebExceptionType.CODE_DATABSE_ERROR, e);
			
		}
		String mimeType = fileResource.getMimeType();
		byte[] fileData = fileResource.getFileResource().getFileData();
		
		
		//
		// Write data to output/response
		//
		ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
		//ContentDisposition contentDisposition = ContentDisposition.type("attachment")
		//	    .fileName("filename.csv").creationDate(new Date()).build();
		ContentDisposition contentDisposition = new ContentDisposition("attachment; filename=image.jpg");
		
		return Response.ok(
			new StreamingOutput() {
				@Override
				public void write(OutputStream out) throws IOException, WebApplicationException {
					byte[] buffer = new byte[4 * 1024];
					int bytesRead;
					while ((bytesRead = bis.read(buffer)) != -1) {
						out.write(buffer, 0, bytesRead);
					}
					out.flush();
					out.close();
					bis.close();
				}
			}
		).header("Content-Disposition", "attachment; filename=" + fileResource.getName()).build();
		
	}

	@Override
	public Logger getLogger() {
		return logger;
	}	
	
	/*
	public Response getFile(@PathParam("fileId") Long fileId, @Context HttpServletResponse response) throws WebServiceException {
		
		logger.info(FileResource.class.getName() + " jax-rs service called, fileId = " + fileId);
		
		//
		// pull file from database
		//
		FsFileMetaResource fileResource = null;
		try {
			fileResource = fsResourceService.getFileResource(fileId, FsFileResourceFetch.FILE_META_WITH_DATA);
		} catch (ServiceException e) {
			e.printStackTrace();
			logger.error("Failed to fetch file data from database, " + e.getMessage(), e);
			throw new WebServiceException(WebServiceException.CODE_DATABSE_ERROR,
					"Failed to fetch file data from database, " + e.getMessage());
		}
		String mimeType = fileResource.getMimeType();
		byte[] fileData = fileResource.getFileResource().getFileData();
		
		
		//
		// Write data to response
		//
		ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
		//InputStream is = new FileInputStream(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        byte[] buffer = new byte[4096];
        while ((len = bis.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, len);
        }
        
        logger.info("Server size: " + baos.size());
        
        // TODO - consider using StreamingOutput class to stream binary data back to client. Would make more
        // sense if you could also stream the file data from the database..
        
        return Response.ok(baos)
        		.header("Content-Disposition", "attachment; filename=" + fileResource.getName()).build();
		
	}
	 */

}
