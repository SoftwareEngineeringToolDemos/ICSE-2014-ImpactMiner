package edu.wm.flat3.analysis.impact;

import java.io.IOException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.wm.flat3.FLATTT;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class MiningPreferencesDialog extends Dialog {
	private PreferenceStore inputPrefs;
	private static final String mboxLabel = "ImpactMiner - Association Rule Settings";
	private FieldEditorPreferencePage page;
	private FloatFieldEditor minSupSelector;
	private FloatFieldEditor minConfSelector;
	private SizeFieldEditor maxSetSizeSelector;
	private FloatFieldEditor maxSupSelector;
	private String minSup;
	private String minConf;
	
	
	public MiningPreferencesDialog(Shell parentShell) {
		super(parentShell);
		inputPrefs = FLATTT.getIAPreferenceStore();
		try {
			inputPrefs.load();
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Did not find previous IA options, starting with blank");
		}
	}
	
	@Override
    protected void configureShell(Shell newShell) { 
        super.configureShell(newShell); 
        newShell.setText(mboxLabel);
        newShell.setMinimumSize(100, 100);//set minimum width, height will be greater than 100
    } 
	
	@Override
	protected Control createDialogArea(Composite parent) { 
		Composite composite = (Composite) super.createDialogArea(parent); 
        page = new FieldEditorPreferencePage(FieldEditorPreferencePage.GRID) { 
        	@Override
			public void createControl(Composite parentComposite) {
        		noDefaultAndApplyButton();
        		setPreferenceStore(inputPrefs);
				super.createControl(parentComposite);
			}

			@Override
			protected void createFieldEditors() {
				minConfSelector = new FloatFieldEditor("minConf", "Minimum Confidence Value", getFieldEditorParent());
				minConfSelector.setValidRange(0.0f,  1.0f);
				minConfSelector.setPage(this);
				minConfSelector.setPropertyChangeListener(page);
				addField(minConfSelector);
				minSupSelector = new FloatFieldEditor("minSup","Minimum Support Value", getFieldEditorParent());
				minSupSelector.setValidRange(0.0f, 1.0f);
				minSupSelector.setPage(this);
				minSupSelector.setPropertyChangeListener(page);
				addField(minSupSelector);
				maxSupSelector = new FloatFieldEditor("maxSup","Maximum Support Value", getFieldEditorParent());
				maxSupSelector.setValidRange(0.0f, 1.0f);
				maxSupSelector.setPage(this);
				maxSupSelector.setPropertyChangeListener(page);
				addField(maxSupSelector);
				maxSetSizeSelector = new SizeFieldEditor("maxSetSize", "Maximum Methods per Commit", getFieldEditorParent());
				maxSetSizeSelector.setPage(this);
				maxSetSizeSelector.setPropertyChangeListener(page);
				addField(maxSetSizeSelector);
			}
            @Override
            protected void updateApplyButton() {
                updateButtons(isValid());
                super.updateApplyButton(); 
             }
        	@Override
        	public boolean isValid(){
        		return (minSupSelector.isValid() && minConfSelector.isValid());
        	}
            @Override
            public void propertyChange(PropertyChangeEvent event){
            	updateButtons(isValid());
            	super.propertyChange(event);
            } 
        };
        page.createControl(composite); 
        Control pageControl = page.getControl(); 
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        return pageControl;
	}
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		updateButtons(page.isValid());
	}
	private void updateButtons(boolean isValid) {
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(isValid);
		}
	}
    @Override
    protected void buttonPressed(int buttonId) {
    	if (buttonId == IDialogConstants.OK_ID) {
	    	minSupSelector.setStringValue("" + Float.parseFloat(minSupSelector.getStringValue()));
	    	minSup = minSupSelector.getStringValue();
	    	minConfSelector.setStringValue("" + Float.parseFloat(minConfSelector.getStringValue()));
	    	minConf = minConfSelector.getStringValue();
	    	page.performOk(); // calls store() method of each FieldEditor
	    	try {
				inputPrefs.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	if (buttonId == IDialogConstants.CANCEL_ID){
    		page.isValid();
    	}
    	super.buttonPressed(buttonId);
    }
    public String getMinSupport(){return minSup;}
    public String getMinConfidence(){return minConf;}
    public FieldEditorPreferencePage getPage() {return page;}
}
