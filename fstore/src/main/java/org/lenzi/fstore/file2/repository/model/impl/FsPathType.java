/**
 * 
 */
package org.lenzi.fstore.file2.repository.model.impl;

/**
 * @author sal
 *
 */
public enum FsPathType {

	// path denotes a file
	FILE("FILE"),		
	
	// path denotes a directory
	DIRECTORY("DIRECTORY");
	
	private String type;
	
	FsPathType(String type){
		this.type = type;
	}

	/**
	 * Get string value for type
	 * 
	 * @return
	 */
	public String getType(){
		return this.type;
	}
	
	/**
	 * Get PathType enum from string value
	 * 
	 * @param type
	 * @return
	 */
	public static FsPathType get(String type){
		if (type != null) {
			for (FsPathType pathType : FsPathType.values()) {
				if (type.equalsIgnoreCase(pathType.type)) {
					return pathType;
				}
			}
		}
		return null;
	}
	
}
