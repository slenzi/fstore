package org.lenzi.fstore.file2.web.messaging.model;

public enum UploadMessageType {

	UPLOAD_RECEIVED		("UPLOAD_RECEIVED",		"Upload has been received, and is queued for processing."),
	UPLOAD_PROCESSED	("UPLOAD_PROCESSED",	"Upload has been processed. ");
	
	private final String value;
	private final String message;

    UploadMessageType(String value, String message) {
        this.value = value;
        this.message = message;
    }

    public String getValue() {
        return value;
    }
    
    public String getMessage(){
    	return message;
    }
	
}
