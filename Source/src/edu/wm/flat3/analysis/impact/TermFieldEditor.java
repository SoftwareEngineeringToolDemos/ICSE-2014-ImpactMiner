package edu.wm.flat3.analysis.impact;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class TermFieldEditor extends StringFieldEditor{
	public TermFieldEditor(String string, String string2,
			Composite fieldEditorParent) {
		super(string,string2,fieldEditorParent);
		super.setValidateStrategy(VALIDATE_ON_KEY_STROKE);
	}
	@Override
	public boolean checkState(){
		return (this.getStringValue()!=null && this.getStringValue().length() > 0);
	}
}

