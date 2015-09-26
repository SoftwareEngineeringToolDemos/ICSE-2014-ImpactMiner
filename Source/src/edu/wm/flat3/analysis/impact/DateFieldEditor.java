package edu.wm.flat3.analysis.impact;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class DateFieldEditor extends StringFieldEditor{
	public DateFieldEditor(String string, String string2,
			Composite fieldEditorParent) {
		super(string, string2, fieldEditorParent);
		super.setValidateStrategy(VALIDATE_ON_KEY_STROKE);
	}

	@Override
	public boolean checkState(){
		try{
			if (this.getStringValue().length() != 10) 	throw new Exception();
			Integer.parseInt(this.getStringValue().substring(0, 4));
			int month = Integer.parseInt(this.getStringValue().substring(5, 7));
			if (month < 1 || month > 12)	throw new Exception();
			int date = Integer.parseInt(this.getStringValue().substring(8, 10));
			if (date < 1 || date > 31)		throw new Exception();
			if (this.getStringValue().charAt(4) != '-' || this.getStringValue().charAt(7) != '-') throw new Exception();
			return true;
		}catch (Exception e){
			return false;
		}
	}
}