package org.lenzi.fstore.file2.web.rs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.cxf.attachment.ContentDisposition;
import org.lenzi.fstore.core.security.FsSecureUser;
import org.lenzi.fstore.core.security.auth.service.FsAuthenticationService;
import org.lenzi.fstore.core.security.service.FsSecurityService;
import org.lenzi.fstore.core.service.exception.ServiceException;
import org.lenzi.fstore.core.stereotype.InjectLogger;
import org.lenzi.fstore.file2.FsFile;
import org.lenzi.fstore.file2.concurrent.service.FsQueuedResourceService;
import org.lenzi.fstore.file2.repository.FsFileResourceRepository.FsFileResourceFetch;
import org.lenzi.fstore.file2.repository.model.impl.FsFileMetaResource;
import org.lenzi.fstore.file2.repository.model.impl.FsResourceStore;
import org.lenzi.fstore.file2.service.FsResourceHelper;
import org.lenzi.fstore.file2.service.FsResourceService;
import org.lenzi.fstore.file2.service.FsUploadService;
import org.lenzi.fstore.web.rs.AbstractResource;
import org.lenzi.fstore.web.rs.exception.WebServiceException;
import org.lenzi.fstore.web.rs.exception.WebServiceException.WebExceptionType;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private Logger logger;
    
    @Autowired
    private FsQueuedResourceService fsQueuedResourceService;
    
    @Autowired
    private FsResourceService fsResourceService;
    
    @Autowired
    private FsResourceHelper fsResourceHelper;
    
    @Autowired
    private FsUploadService fsUploadService;
    
	//
	// ACLs security
	//
	//@Autowired
	//private FsSecurityService fsSecurityService;
    @Autowired
    private FsAuthenticationService fsAuthService;     
	
	public FileResource() {
		
	}
	
	/**
	 * Delete single file resource
	 * 
	 * @param fileId - id of file to delete
	 * @return
	 * @throws WebServiceException
	 */
	@DELETE
	@Path("{fileId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteFile(@PathParam("fileId") Long fileId) throws WebServiceException {
		
		logger.info(FileResource.class.getName() + " jax-rs service called, delete file, fileId = " + fileId);
		
		try {
			fsQueuedResourceService.removeFileResource(fileId);
		} catch (ServiceException e) {
			handleError("Failed to delete file data from server", WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		
		return Response.ok("{ \"message\": \"ok\" }", MediaType.APPLICATION_JSON).build();
		
	}
	
	/**
	 * Delete a list of files
	 * 
	 * @param fileIdList - list of file ids. All files will be deleted.
	 * @return
	 * @throws WebServiceException
	 */
	@POST
	@Path("/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteFiles(@QueryParam("fileId") List<Long> fileIdList) throws WebServiceException {
		
		logger.info(FileResource.class.getName() + " jax-rs service called, delete files");
		
		if(fileIdList == null || fileIdList.size() == 0){
			throw new WebServiceException(WebExceptionType.CODE_MISSING_REQUIRED_INPUT,
					"List of file IDs for delete operation is null or empty. Need 1 or more 'fileId' query parameters in request.");
		}
		
		String delim = "";
		StringBuffer fileIdLog = new StringBuffer();
		for(Long fileId : fileIdList){
			fileIdLog.append(delim + fileId); delim = ",";
		}
		
		logger.info("Number of files to delete: " + fileIdList.size() + ". [" + fileIdLog.toString() + "]");
		
		try {
			fsQueuedResourceService.removeFileResourceList(fileIdList);
		} catch (ServiceException e) {
			handleError("Failed to delete file data from server. File Id list = [" + fileIdLog.toString() + "]",
					WebExceptionType.CODE_DATABSE_ERROR, e);
		}
		
		return Response.ok("{ \"message\": \"ok\" }", MediaType.APPLICATION_JSON).build();
		
	}
	

	/**
	 * Copy a list of files
	 * 
	 * @param fileIdList - list of file ids. These are the files being copied
	 * @param dirId - destination directory id. where the files will be copied to.
	 * @param replaceExisting - clear/overwrite file in destination directory if same file already exists.)
	 * @return
	 * @throws WebServiceException
	 */
	@POST
	@Path("/copy")
	@Produces(MediaType.APPLICATION_JSON)
	public Response copyFiles(@QueryParam("fileId") List<Long> fileIdList, @QueryParam("dirId") Long dirId,
			@QueryParam("replaceExisting") Boolean replaceExisting) throws WebServiceException {
		
		if(fileIdList == null || fileIdList.size() == 0){
			throw new WebServiceException(WebExceptionType.CODE_MISSING_REQUIRED_INPUT,"Missing 'fileId' parameter. Need at least one file ID for copy.");
		}
		if(dirId == null || dirId <=0){
			throw new WebServiceException(WebExceptionType.CODE_MISSING_REQUIRED_INPUT,"Missing 'dirId' parameter. Need ID of destination directory.");
		}
		if(replaceExisting == null){
			throw new WebServiceException(WebExceptionType.CODE_MISSING_REQUIRED_INPUT,"Missing 'replaceExisting' parameter. Need parameter to determine if user wants to overwrite existing files in destinatioin directory.");
		}
		
		Long nextFileId = 0L;
		for(Long fileId : fileIdList){
			nextFileId = fileId;
			try {
				fsQueuedResourceService.copyFileResource(nextFileId, dirId, replaceExisting);
			} catch (ServiceException e) {
				handleError("Failed to copy file. [fileId=" + nextFileId + ", dirId=" + dirId + ", replaceExisting=" + replaceExisting + "]",
						WebExceptionType.CODE_DATABSE_ERROR, e);
			}
		}
		
		return Response.ok("{ \"message\": \"ok\" }", MediaType.APPLICATION_JSON).build();
		
	}
	
	/**
	 * Move a list of files
	 * 
	 * @param fileIdList - list of file ids. These are the files being moved
	 * @param dirId - destination directory id. where the files will be moved to.
	 * @param replaceExisting - clear/overwrite file in destination directory if same file already exists.)
	 * @return
	 * @throws WebServiceException
	 */
	@POST
	@Path("/move")
	@Produces(MediaType.APPLICATION_JSON)
	public Response moveFiles(@QueryParam("fileId") List<Long> fileIdList, @QueryParam("dirId") Long dirId,
			@QueryParam("replaceExisting") Boolean replaceExisting) throws WebServiceException {
		
		if(fileIdList == null || fileIdList.size() == 0){
			throw new WebServiceException(WebExceptionType.CODE_MISSING_REQUIRED_INPUT,"Missing 'fileId' parameter. Need at least one file ID for move.");
		}
		if(dirId == null || dirId <=0){
			throw new WebServiceException(WebExceptionType.CODE_MISSING_REQUIRED_INPUT,"Missing 'dirId' parameter. Need ID of destination directory.");
		}
		if(replaceExisting == null){
			throw new WebServiceException(WebExceptionType.CODE_MISSING_REQUIRED_INPUT,"Missing 'replaceExisting' parameter. Need parameter to determine if user wants to overwrite existing files in destinatioin directory.");
		}
		
		Long nextFileId = 0L;
		for(Long fileId : fileIdList){
			nextFileId = fileId;
			try {
				fsQueuedResourceService.moveFileResource(nextFileId, dirId, replaceExisting);
			} catch (ServiceException e) {
				handleError("Failed to move file. [fileId=" + nextFileId + ", dirId=" + dirId + ", replaceExisting=" + replaceExisting + "]",
						WebExceptionType.CODE_DATABSE_ERROR, e);
			}
		}
		
		return Response.ok("{ \"message\": \"ok\" }", MediaType.APPLICATION_JSON).build();
		
	}	
	
	/**
	 * Download file resource.
	 * 
	 * Secured with @PreAuthorize annotation which is method level spring security.
	 * 
	 * @param fileId - id of file resource to download
	 * @return
	 * @throws WebServiceException
	 */
	@GET
	@Path("/download/id/{fileId}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	//@PreAuthorize("isAuthenticated() and hasPermission(#fileId, 'path.to.file', 'read')")
	public Response getFile(@PathParam("fileId") Long fileId) throws WebServiceException {
		
		logger.info(FileResource.class.getName() + " jax-rs service called, download file, fileId = " + fileId);
		
		// see who is asking for the file...see if they have permission
		//FsSecureUser fsUser = fsSecurityService.getLoggedInUser();
		FsSecureUser fsUser = fsAuthService.getLoggedInUser();
		if(fsUser != null){
			logger.info("User '" + fsUser.getUsername() + "' is requesting file, file id =>  " + fileId);
		}else{
			logger.error("FsSecureUser is null.. cannot dermin who is request file, file id => " + fileId);
		}		
		
		// TODO - stream file from database rather than loading entire file into memory / byte[]
		
		FsFile fsFile = null;
		try {
			fsFile = fsResourceService.getFsFileById(fileId);
		} catch (ServiceException e) {
			handleError("Failed to retrieve file data for file path resource with id => " + fileId, WebExceptionType.CODE_IO_ERROR, e);
		}
		
		//
		// Write data to output/response
		//
		ByteArrayInputStream bis = new ByteArrayInputStream(fsFile.getBytes());
		
		//ContentDisposition contentDisposition = ContentDisposition.type("attachment")
		//	    .fileName("filename.csv").creationDate(new Date()).build();
		//ContentDisposition contentDisposition = new ContentDisposition("attachment; filename=image.jpg");
		
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
		).header("Content-Disposition", "attachment; filename=" + fsFile.getFileName()).build();
		
	}
	
	/**
	 * Return file as plain text. Obviously only for text files... 
	 * 
	 * @param fileId - id of the file path resource which is of text/plain type.
	 * @return
	 * @throws WebServiceException
	 */
	@GET
	@Path("/text/id/{fileId}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getTextFile(@PathParam("fileId") Long fileId) throws WebServiceException {
		
		logger.info(FileResource.class.getName() + " jax-rs service called, get text file, fileId = " + fileId);
		
		// TODO - stream file from database rather than loading entire file into memory / byte[]
		
		FsFile fsFile = null;
		try {
			fsFile = fsResourceService.getFsFileById(fileId);
		} catch (ServiceException e) {
			handleError("Failed to retrieve file data for file path resource with id => " + fileId, WebExceptionType.CODE_IO_ERROR, e);
		}
		
		ByteArrayInputStream bis = new ByteArrayInputStream(fsFile.getBytes());
		
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
		).build();	
		
	}
	
	/**
	 * Save text file.
	 * 
	 * @return
	 * @throws WebServiceException
	 */
	@POST
	@Path("/text")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveTextFile(@FormParam("fileId") Long fileId, @FormParam("text") String text) throws WebServiceException {
		
		logger.info(FileResource.class.getName() + " jax-rs service called, save text file");
		logger.info("fileId = " + fileId);
		logger.info("text = " + text);
		
		
		try {
			fsUploadService.rewriteTextFile(fileId, text);
		} catch (ServiceException e) {
			handleError("Failed to update text file with new data, fileId => " + fileId, WebExceptionType.CODE_IO_ERROR, e);
		}
		
		return Response.ok("{ \"message\": \"ok\" }", MediaType.APPLICATION_JSON).build();
		
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
