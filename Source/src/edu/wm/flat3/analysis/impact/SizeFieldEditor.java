package edu.wm.flat3.analysis.impact;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class SizeFieldEditor extends IntegerFieldEditor{
	public SizeFieldEditor(String string, String string2,
			Composite fieldEditorParent) {
		super(string,string2,fieldEditorParent);
	}

	@Override
	public boolean checkState(){
		try {
			int i = Integer.parseInt(this.getStringValue());
			if (i < 1) return false;
			return super.checkState();
		} catch (Exception e) {
			return false;
		}			
	}
}