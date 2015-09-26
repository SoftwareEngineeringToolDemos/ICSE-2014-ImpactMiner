package edu.wm.flat3.analysis.impact;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class SVNAddressFieldEditor extends StringFieldEditor{
	public SVNAddressFieldEditor(String string, String string2,
			Composite fieldEditorParent) {
		super(string,string2,fieldEditorParent);
		super.setValidateStrategy(VALIDATE_ON_KEY_STROKE);
	}

	@Override
	public boolean checkState(){
		try {
			SVNURL.parseURIEncoded(this.getStringValue());
			return true;
		} catch (SVNException e) {
			return false;
		}			
	}
}