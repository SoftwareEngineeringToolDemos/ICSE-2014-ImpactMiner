package edu.wm.flat3.analysis.impact.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class ShowMSRAction extends Action {
	public ShowMSRAction() {
		super();
		this.setEnabled(FLATTT.MSRRun);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/msroff.png"));
		setText("Show Mining Results");
		setToolTipText("Show Mining Results");
	}
	
	public void run(){
		if (FLATTT.MSRRun)
			if (FLATTT.MSRShown){
				FLATTT.MSRShown = false;
				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						FLATTT.ID_PLUGIN, "icons/msroff.png"));
			}
			else{
				FLATTT.MSRShown = true;
				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						FLATTT.ID_PLUGIN, "icons/msron.png"));
			}
		else
			FLATTT.debugOutput("Big, bad problem.  This button should not be operable unless MSR has been run");
		CombineResultsAction.combine();
	}
}
