package edu.wm.flat3.analysis.impact;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class ChangeSet extends ArrayList<String> implements Serializable{

	private long revision;
	public long getRevision() {
		return revision;
	}
	public void setRevision(long revision) {
		this.revision = revision;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 2110058238374800361L;

	@Override
	public boolean add(String s){
		if (this.contains(s)) return false;
		else return super.add(s);
	}
}
