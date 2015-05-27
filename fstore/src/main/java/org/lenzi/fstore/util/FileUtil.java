package org.lenzi.fstore.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileUtil {

	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	public static void copyFile(Path source, Path target) throws IOException, SecurityException {
		
		try {
			Files.copy(source, target);
		} catch (IOException e) {
			throw new IOException("Error copying " + source.toString() + " to " + target.toString() + ". " + e.getMessage(), e);
		} catch (SecurityException e) {
			throw new SecurityException("Error copying " + source.toString() + " to " + target.toString() + ". " + e.getMessage(), e);
		}
		
	}
	
	/**
	 * Check if a directory is empty.
	 * 
	 * @param directory
	 * @return
	 * @throws IOException
	 */
	public static boolean isDirectoryEmpty(final Path directory) throws IOException {
	    try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
	        return !dirStream.iterator().hasNext();
	    }
	}
	
	/**
	 * Create a directory
	 * 
	 * @param directory - path of directory to create
	 * @param clearIfNotEmpty - pass true to delete any files in the directory if it already exists and contains files.
	 * @throws IOException - if there is any error creating the directory, and if directory already
	 * 	exists, and it contains files, and 'clearIfExists' is false.
	 */
	public static void createDirectory(final Path directory, boolean clearIfNotEmpty) throws IOException {
		
		boolean dirExists = Files.exists(directory);
		
		if(dirExists){
			
			logger.info(directory.toString() + " already exists.");
			
			boolean isEmpty = false;
			try {
				isEmpty = FileUtil.isDirectoryEmpty(directory);
			} catch (IOException e) {
				throw new IOException("Error checking if " + directory.toString() + " exists and contains files.", e);
			}
			
			logger.info(directory.toString() + " is not empty.");
			
			if(!isEmpty && !clearIfNotEmpty){
				throw new IOException("Cannot create new file store. Path " + directory.toString() + " already exists and contains files");
			}
			if(!isEmpty && clearIfNotEmpty){
				logger.info("Deleting " + directory.toString());
				try {
					deleteDirectory(directory);
					//Files.delete(directory);
				} catch (IOException e) {
					throw new IOException("Error removing existing directory " + directory.toString());
				}
				try {
					logger.info("Re-creating " + directory.toString());
					Files.createDirectories(directory);
				} catch (IOException e) {
					throw new IOException("Failed to re-create directory " + directory.toString(), e);
				}
			}
		}else{
			
			logger.info(directory.toString() + " does not exists.");
			logger.info("Creating " + directory.toString());
			
			try {
				Files.createDirectories(directory);
			} catch (IOException e) {
				throw new IOException("Failed to create directory " + directory.toString(), e);
			}	
		}
		
		// verify it exists
		if(!Files.exists(directory)){
			throw new IOException("Failed to created directory on file system for unknown reason. No exception was thrown during create operation, but directory does not exist.");
		}
		
	}
	
	/**
	 * Delete a directory, and everything in it if the directories contains files and sub directories.
	 * 
	 * @param directory
	 * @throws IOException
	 */
	public static void deleteDirectory(final Path directory) throws IOException {
		
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
			
			   @Override
			   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				   Files.delete(file);
				   return FileVisitResult.CONTINUE;
			   }

			   @Override
			   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				   Files.delete(dir);
				   return FileVisitResult.CONTINUE;
			   }

		   });		
		
	}

}
