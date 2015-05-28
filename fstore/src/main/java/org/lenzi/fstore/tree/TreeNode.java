/**
 * 
 */
package org.lenzi.fstore.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sal
 *
 */
public class TreeNode<T> {
	
	private T data = null;
	private TreeNode<T> parent = null;
	private List<TreeNode<T>> childList = null;
	
	public TreeNode() {
		super();
	}
	
	public TreeNode(T data) {
		super();
		this.data = data;
	}

	public TreeNode(T data, TreeNode<T> parent) {
		super();
		this.data = data;
		this.parent = parent;
	}

	public void setData(T data) {
		this.data = data;
	}

	public T getData() {
		return data;
	}

	public void setParent(TreeNode<T> parent) {
		this.parent = parent;
	}

	public TreeNode<T> getParent() {
		return parent;
	}

	public void addChildNode(TreeNode<T> child) {
		if(child == null){
			return;
		}
		if(childList == null){
			childList = new ArrayList<TreeNode<T>>();
		}
		child.setParent(this);
		childList.add(child);
	}

	public void addChildren(List<TreeNode<T>> children) {
		if(children == null || children.size() == 0){
			return;
		}
		if(childList == null){
			childList = new ArrayList<TreeNode<T>>();
		}
		for(TreeNode<T> n : children){
			n.setParent(this);
		}
		childList.addAll(children);
	}

	public void setChildren(List<TreeNode<T>> children) {
		childList = children;
		if(childList != null){
			for(TreeNode<T> n : childList){
				n.setParent(this);
			}			
		}
	}

	public List<TreeNode<T>> getChildren() {
		return childList;
	}

	public boolean hasChildren() {
		return ((childList != null && childList.size() > 0) ? true : false);
	}

	public int getChildCount() {
		return ((childList != null) ? childList.size() : 0);
	}
	
	// print tree with provided line separator (e.d. <br> tag)
	public String printTree(String lineSepartor){
		StringBuffer buf = new StringBuffer();
        print(buf, "", true, lineSepartor);
        return buf.toString();		
	}
	
	// print tree, default like separator is, System.getProperty("line.separator")
	public String printTree() {
		return printTree(System.getProperty("line.separator"));
    }
	
	public String printHtmlTree() {
		StringBuffer buf = new StringBuffer();
        printHtml(buf, "", true, "<br>");
        return buf.toString();	
    }	

    private void print(StringBuffer buf, String prefix, boolean isTail, String lineSepartor) {
    	buf.append(prefix + (isTail ? "|__" : "|__") + data.toString() + lineSepartor);
    	if(childList != null){
	        for (int i = 0; i < childList.size() - 1; i++) {
	        	childList.get(i).print(buf, prefix + (isTail ? "   " : "|  "), false, lineSepartor);
	        }
	        if (childList.size() >= 1) {
	        	childList.get(childList.size() - 1).print(buf, prefix + (isTail ? "   " : "|  "), true, lineSepartor);
	        }
    	}
    }
    
    private void printHtml(StringBuffer buf, String prefix, boolean isTail, String lineSepartor) {
    	buf.append(prefix + (isTail ? "|__" : "|__") + data.toString() + lineSepartor);
    	if(childList != null){
	        for (int i = 0; i < childList.size() - 1; i++) {
	        	childList.get(i).printHtml(buf, prefix + (isTail ? "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" : "|&nbsp;&nbsp;&nbsp;&nbsp;"), false, lineSepartor);
	        }
	        if (childList.size() >= 1) {
	        	childList.get(childList.size() - 1).printHtml(buf, prefix + (isTail ? "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" : "|&nbsp;&nbsp;&nbsp;&nbsp;"), true, lineSepartor);
	        }
    	}
    }	    

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeNode other = (TreeNode) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}
}