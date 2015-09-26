package edu.wm.flat3.analysis.impact;

import java.io.File;

/**
 * A pair of files changed in a revision
 * @author mmwagner@email.wm.edu
 *
 */
public class ChangedFilePair {
	private File oldFile;
	private File newFile;
	public ChangedFilePair(File oldFile, File newFile) {
		super();
		this.oldFile = oldFile;
		this.newFile = newFile;
	}
	public File getOldFile() {
		return oldFile;
	}
	public File getNewFile() {
		return newFile;
	}
	
}
