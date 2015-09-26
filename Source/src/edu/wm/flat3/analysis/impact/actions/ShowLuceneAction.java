/**
 * 
 */
package edu.wm.flat3.analysis.impact.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;


/**
 * @author mmwagner@email.wm.edu
 *
 */
public class ShowLuceneAction extends Action {
	public ShowLuceneAction() {
		super();
		this.setEnabled(FLATTT.luceneRun);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/luceneoff.png"));
		setText("Show Lucene Results");
		setToolTipText("Show Lucene Results");
	}
	
	public void run(){
		if (FLATTT.luceneRun)
			if (FLATTT.luceneShown){
				FLATTT.luceneShown = false;
				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						FLATTT.ID_PLUGIN, "icons/luceneoff.png"));
			}
			else{
				FLATTT.luceneShown = true;
				setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
						FLATTT.ID_PLUGIN, "icons/luceneon.png"));
			}
		else
			FLATTT.debugOutput("Big, bad problem.  This button should not be operable unless Lucene has been run");
		CombineResultsAction.combine();
	}
}
