/**
 * 
 */
package org.lenzi.fstore.core.persistence;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @author sal
 * 
 * Convert boolean values in jpa entities to "Y" or "N" string values for database, and vice versa.
 * 
 * Y = true
 * N = false
 * 
 */
@Converter
public class BooleanToStringConverter implements AttributeConverter<Boolean, String> {

	/* (non-Javadoc)
	 * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
	 */
	@Override
	public String convertToDatabaseColumn(Boolean value) {
		return (value != null && value) ? "Y" : "N";
	}

	/* (non-Javadoc)
	 * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
	 */
	@Override
	public Boolean convertToEntityAttribute(String value) {
		return "Y".equals(value);
	}

}
