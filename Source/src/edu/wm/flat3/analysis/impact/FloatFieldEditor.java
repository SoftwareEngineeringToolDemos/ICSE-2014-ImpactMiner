package edu.wm.flat3.analysis.impact;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class FloatFieldEditor extends StringFieldEditor {
	float min = 0.0f;
	float max = 1.0f;
	public FloatFieldEditor(String string1, String string2,
			Composite fieldEditorParent) {
		super(string1, string2, fieldEditorParent);
		super.setValidateStrategy(VALIDATE_ON_KEY_STROKE);
		min = 0.0f;
		max = 1.0f;
	}
	
	@Override
	public boolean checkState(){
		float f;
		try{
			f = Float.parseFloat(this.getStringValue());
			if (f >= min && f <= max)
				return true;
			else
				return false;
		}catch (NumberFormatException e){
			return false;
		}
		
	}
	
	public void setValidRange(float min, float max){
		this.min = min;
		this.max = max;
	}
	
}
