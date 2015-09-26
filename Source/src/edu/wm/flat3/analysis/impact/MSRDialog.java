package edu.wm.flat3.analysis.impact;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.FLATTT;
import edu.wm.cs.semeru.benchmarks.downloadSVNCommits.*;
import edu.wm.cs.semeru.benchmarks.goldSetsGeneratorFromSVNCommits.*;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class MSRDialog extends Dialog {
	private static final String mboxLabel = "Mining Software Repository";
	private String repoAddy;
	private String startDate;
	private String endDate;
	private FieldEditorPreferencePage page;
	private PreferenceStore inputPrefs;
	private SVNAddressFieldEditor addySelector;
	private DateFieldEditor startSelector;
	private DateFieldEditor endSelector;
	private IntegerFieldEditor startRevSelector;
	private IntegerFieldEditor endRevSelector;
	private BooleanFieldEditor useRevInsteadOfDateSelector;
	//private StringFieldEditor nameSelector;
	
	public MSRDialog(Shell parentShell) {
		super(parentShell);
		inputPrefs = FLATTT.getIAPreferenceStore();
		try {
			inputPrefs.load();
		} catch (IOException e) {
			System.out.println("Did not find previous IA options, starting with blank");
		}
	}
	
	@Override
    protected void configureShell(Shell newShell) { 
        super.configureShell(newShell); 
        newShell.setText(mboxLabel);
        newShell.setMinimumSize(500, 100);//set minimum width, height will be greater than 100
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
            	addySelector = new SVNAddressFieldEditor("addy", "Repository Address:", getFieldEditorParent());
            	addySelector.setPage(this);
            	addySelector.setPropertyChangeListener(page);
            	addField(addySelector);
            	startSelector = new DateFieldEditor("start", "Start Date YYYY-MM-DD", getFieldEditorParent());
            	startSelector.setPage(this);
            	startSelector.setPropertyChangeListener(page);
            	addField(startSelector);
            	endSelector = new DateFieldEditor("end", "End Date YYYY-MM-DD", getFieldEditorParent());
            	endSelector.setPage(this);
            	endSelector.setPropertyChangeListener(page);
            	addField(endSelector);
            	///
            	useRevInsteadOfDateSelector = new BooleanFieldEditor("useRev", 
            							"Sepcify Revision Numbers instead of Dates?",
            							getFieldEditorParent());
            	useRevInsteadOfDateSelector.setPage(this);
            	useRevInsteadOfDateSelector.setPropertyChangeListener(page);
            	addField(useRevInsteadOfDateSelector);
            	startRevSelector = new IntegerFieldEditor("startRev", "Start Revision", getFieldEditorParent());
            	startRevSelector.setPage(this);
            	startRevSelector.setPropertyChangeListener(page);
            	addField(startRevSelector);
            	endRevSelector = new IntegerFieldEditor("endRev", "End Revision", getFieldEditorParent());
            	endRevSelector.setPage(this);
            	endRevSelector.setPropertyChangeListener(page);
            	addField(endRevSelector);
            	/*nameSelector = new StringFieldEditor("name", "Project Name", getFieldEditorParent());
            	nameSelector.setPage(this);
            	nameSelector.setPropertyChangeListener(page);
            	addField(nameSelector);*/
            } 
            @Override
            protected void updateApplyButton() {
                updateButtons(isValid());
                super.updateApplyButton(); 
             } 

            @Override
			public boolean isValid(){
            	boolean toReturn = addySelector.isValid();
            	if (useRevInsteadOfDateSelector.getBooleanValue())
        			toReturn = toReturn & startRevSelector.isValid() & endRevSelector.isValid();
        		else
        			toReturn = toReturn & startSelector.isValid() & endSelector.isValid();
            	return (toReturn);
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
		
		Button options = new Button(parent, SWT.LEFT);
		options.setText("More Mining Options");
		options.addListener(SWT.MouseDown, new Listener(){

			@Override
			public void handleEvent(Event event) {
				MiningPreferencesDialog d = new MiningPreferencesDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
				d.open();
			}
			
		});
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
	    	repoAddy = addySelector.getStringValue();
	    	startDate = startSelector.getStringValue();
	    	endDate = endSelector.getStringValue();
	    	page.performOk(); // calls store() method of each FieldEditor
	    	try {
	    		inputPrefs.save();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	if (buttonId == IDialogConstants.CANCEL_ID){
    		page.isValid();
    	}
    	super.buttonPressed(buttonId);
    }
 
    public String getRepoAddy() {return repoAddy;}
	public String getStartDate() {return startDate;}
	public String getEndDate() {return endDate;}
}
