package edu.wm.flat3.analysis.impact.actions;

import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.analysis.impact.MSR;
import edu.wm.flat3.analysis.impact.MSRDialog;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class StartMSRAction extends Action {

	public StartMSRAction() {
		super();
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/msroff.png"));
		setText("Start Mining");
		setToolTipText("Start Mining");
	}
	
	@Override
	public void run(){
		PreferenceStore prefs = FLATTT.getIAPreferenceStore();
		prefs.setValue("seedAlreadySelected", false);
		try {
			prefs.save();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MSRDialog d = new MSRDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		d.open();
		if (d.getReturnCode()==Window.OK){
			MSR.doMSR();
			FLATTT.tableView.updateToolbarButtons();
			TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
			contentP.refreshTable();
		}
	}
	
}
