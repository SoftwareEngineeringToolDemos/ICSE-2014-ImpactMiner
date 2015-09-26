package edu.wm.flat3.analysis.impact.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class ShowTraceAction extends Action {

	public ShowTraceAction() {
		super();
		this.setEnabled(FLATTT.traceRun);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/traceoff.png"));
		setText("Show Trace Results");
		setToolTipText("Show Trace Results");
	}
	
	public void run(){
		if (FLATTT.traceRun)
			if (FLATTT.traceShown){
				FLATTT.traceShown = false;
				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						FLATTT.ID_PLUGIN, "icons/traceoff.png"));
			}
			else{
				FLATTT.traceShown = true;
				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						FLATTT.ID_PLUGIN, "icons/traceon.png"));
			}
		else
			FLATTT.debugOutput("Big, bad problem.  This button should not be operable unless trace has been run");
		CombineResultsAction.combine();
	}

}
