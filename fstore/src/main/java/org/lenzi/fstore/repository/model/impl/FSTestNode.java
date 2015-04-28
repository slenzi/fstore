/**
 * 
 */
package org.lenzi.fstore.repository.model.impl;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Node for testing the closure repository code. Also serves as an example custom
 * node which extends FSNode. Other nodes can extends FSNode to link any type of
 * data to a node in a tree.
 * 
 * @author slenzi
 */
@Entity
@DiscriminatorValue("TestNode")
@Table(name="FS_TEST_NODE")
public class FSTestNode extends FSNode {

	/**
	 * 
	 */
	@Transient
	private static final long serialVersionUID = -7740617004986387192L;
	
	@Column(name = "TEST_VALUE", nullable = true)
	private String testValue;

	/**
	 * 
	 */
	public FSTestNode() {
		
	}

	/**
	 * @param testValue
	 */
	public FSTestNode(String name, String testValue) {
		this.setName(name);
		this.testValue = testValue;
	}

	/**
	 * @return the testValue
	 */
	public String getTestValue() {
		return testValue;
	}

	/**
	 * @param testValue the testValue to set
	 */
	public void setTestValue(String testValue) {
		this.testValue = testValue;
	}
	
}
