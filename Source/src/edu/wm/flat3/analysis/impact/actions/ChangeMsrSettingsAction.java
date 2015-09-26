package edu.wm.flat3.analysis.impact.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.analysis.impact.MiningPreferencesDialog;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class ChangeMsrSettingsAction extends Action{
	public ChangeMsrSettingsAction(){
		setText("Change MSR Settings");
		setToolTipText("Change MSR Settings");
	}
	public void run(){
		MiningPreferencesDialog d = new MiningPreferencesDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		d.open();
	}
}
