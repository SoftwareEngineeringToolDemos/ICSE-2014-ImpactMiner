package edu.wm.flat3.actions;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.impact.MSR;
import edu.wm.flat3.repository.Concern;

public class SetSeedMethodAction extends Action implements IActionDelegate, IEditorActionDelegate {
	private MultiElementAction sdf;

	public SetSeedMethodAction() {
		super();
		setText("Use as Seed for MSR");
		setToolTipText("Run MSR using this method as the seed");
		setEnabled(true);
		
		
		sdf=new UnlinkElementsAction();
		
	}

	private FLATTTMember selectedItem = null;
	protected IEditorPart aJavaEditor;
	
	public void run(){
		//FLATTT.debugOutput("selection="+FLATTT.tableView.getSelectedItems());
		FLATTT.debugOutput("Trying to do MSR by setting the seed within the search results view\n");
		List<FLATTTMember> selectedItems = FLATTT.tableView.getSelectedItems();
		if (selectedItems.size() != 1){
			FLATTT.debugOutput("Either too few or too many selected Elements\n");
			return;
		}
		FLATTTMember sel = selectedItems.get(0);
		if (!(sel.getNodeIMember() instanceof IMethod)){
			FLATTT.debugOutput("Selected item not a method");
			return;
		}
		selectedItem = sel;
		if (selectedItem == null){
			FLATTT.debugOutput("selectedItem is null, cannot do MSR\n", System.err);
			return;
		}
		PreferenceStore prefs = FLATTT.getIAPreferenceStore();
		String key = selectedItem.getClassName() + "."
				+ selectedItem.getShortName();
		prefs.setValue("seedMethod", key);
		prefs.setValue("seedAlreadySelected",true);
		FLATTT.debugOutput(key, System.err);
		try{
			prefs.save();
			MSR.doMSR();
		}catch(Exception n){
			FLATTT.debugOutput("Caught an exception, could not perform MSR\n",System.err);
			n.printStackTrace();
			return;
		}

	}


	@Override
	public void run(IAction action) {
		this.run();
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		FLATTT.debugOutput("In selectionChanged");
		if (selection instanceof FLATTTMember){
			FLATTTMember fm = (FLATTTMember) selection;
			IMember im = fm.getNodeIMember();
			if (im instanceof IMethod){
				selectedItem = fm;
				FLATTT.debugOutput(selectedItem.toString());
			}
		}
		else return;
	}


	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		aJavaEditor = targetEditor;
	}

}
