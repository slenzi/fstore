/**
 * 
 */
package org.lenzi.fstore.filemanager2.repository.model.impl;

/**
 * @author sal
 *
 */
public enum PathType {

	// path denotes a file
	FILE("FILE"),		
	
	// path denotes a directory
	DIRECTORY("DIRECTORY");
	
	private String type;
	
	PathType(String type){
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
	public static PathType get(String type){
		if (type != null) {
			for (PathType pathType : PathType.values()) {
				if (type.equalsIgnoreCase(pathType.type)) {
					return pathType;
				}
			}
		}
		return null;
	}
	
}
