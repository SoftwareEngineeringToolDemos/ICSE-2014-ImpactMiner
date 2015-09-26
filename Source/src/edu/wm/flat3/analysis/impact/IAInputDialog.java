package edu.wm.flat3.analysis.impact;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
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
/**
 * @author mmwagner@email.wm.edu
 *
 */
public class IAInputDialog extends Dialog{
	private static final String mboxLabel = "ImpactMiner - Impact Analysis";
	private int inputSize;
	private String searchTerm;
	private Boolean runTrace;
	private Boolean runHist;
	private String repoAddy;
	private String startDate;
	private String endDate;
	private FieldEditorPreferencePage page;
	private PreferenceStore inputPrefs;
	private SizeFieldEditor sizeSelector;
	private TermFieldEditor termSelector;
	private BooleanFieldEditor traceSelector;
	private BooleanFieldEditor histSelector;
	private SVNAddressFieldEditor addySelector;
	private DateFieldEditor startSelector;
	private DateFieldEditor endSelector;
	private IntegerFieldEditor startRevSelector;
	private IntegerFieldEditor endRevSelector;
	private BooleanFieldEditor useRevInsteadOfDateSelector;

	public IAInputDialog(Shell parentShell) {
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
            	sizeSelector = new SizeFieldEditor("size", "Select the desired Impact Set size:", getFieldEditorParent());
            	sizeSelector.setValidRange(1, 1000);
            	sizeSelector.setPage(this);
            	sizeSelector.setPropertyChangeListener(page);
            	addField(sizeSelector);
            	termSelector = new TermFieldEditor("term", "Search Term:", getFieldEditorParent());
            	termSelector.setEmptyStringAllowed(false);
            	termSelector.setPage(this);
            	termSelector.setPropertyChangeListener(page);
            	addField(termSelector);
            	traceSelector = new BooleanFieldEditor("trace", "Run a Trace of the slected file?", getFieldEditorParent());
            	traceSelector.setPage(this);
            	traceSelector.setPropertyChangeListener(page);
            	addField(traceSelector);
            	histSelector = new BooleanFieldEditor("hist", "Use Repository Mining?", getFieldEditorParent());
            	histSelector.setPage(this);
            	histSelector.setPropertyChangeListener(page);
            	addField(histSelector);
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
            } 
            @Override
            protected void updateApplyButton() {
                updateButtons(isValid());
                super.updateApplyButton(); 
             } 

            @Override
			public boolean isValid(){
            	boolean toReturn = sizeSelector.isValid() & termSelector.isValid() & traceSelector.isValid() & histSelector.isValid();
            	if (histSelector.getBooleanValue()){
            		toReturn = toReturn & addySelector.isValid();
            		if (useRevInsteadOfDateSelector.getBooleanValue())
            			toReturn = toReturn & startRevSelector.isValid() & endRevSelector.isValid() ;
            		else
            			toReturn = toReturn & startSelector.isValid() & endSelector.isValid();
            		
            	}
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
		
		Button options = new Button(parent, SWT.CENTER);
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
	    	inputSize = sizeSelector.getIntValue();
	    	searchTerm = termSelector.getStringValue();
	    	runTrace = traceSelector.getBooleanValue();
	    	runHist = histSelector.getBooleanValue();
	    	repoAddy = addySelector.getStringValue();
	    	startDate = startSelector.getStringValue();
	    	endDate = endSelector.getStringValue();
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
	public int getInputSize() {return inputSize;}
	public String getSearchTerm() {return searchTerm;}
	public Boolean getRunTrace() {return runTrace;}
	public Boolean getRunHist() {return runHist;}
	public String getRepoAddy() {return repoAddy;}
	public String getStartDate() {return startDate;}
	public String getEndDate() {return endDate;}
	public FieldEditorPreferencePage getPage() {return page;}
}
