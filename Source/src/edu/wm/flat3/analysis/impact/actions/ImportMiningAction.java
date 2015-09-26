package edu.wm.flat3.analysis.impact.actions;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.analysis.impact.AssociationRule;
import edu.wm.flat3.analysis.impact.RuleMiner;
import edu.wm.flat3.analysis.impact.SVNsearchAlt;
import edu.wm.flat3.analysis.impact.VSEChooser;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class ImportMiningAction extends Action {
	IViewPart view;

	public ImportMiningAction(IViewPart view) {
		super();
		this.view = view;
		this.setEnabled(true);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/msrImport.png"));
		setText("Import Mining Results");
		setToolTipText("Import Mining Results");
	}
	
	public void run(){
		FileDialog fileDialog = new FileDialog(view.getViewSite().getShell(), SWT.SAVE);
		fileDialog.setFilterNames(new String[] {"Zip Files (*.zip)"});
		fileDialog.setFilterExtensions(new String[] {"*.zip"});
		fileDialog.setText("Load MSR data...");
		String file = fileDialog.open();
		if(file==null) return;
		File datFile = new File(file);
		if (!datFile.exists())return;
		
		////
		FLATTTMember vse;
		FLATTTMember rhs;
		////
		ArrayList<AssociationRule> rules = RuleMiner.mineRules(datFile);
		//if (rules == null) return;
		ArrayList<FLATTTMember> histElements = SVNsearchAlt.elementList;
		ArrayList<FLATTTMember> histResults = new ArrayList<FLATTTMember>();

		String[] elementNames = new String[histElements.size()];
		for (int i=0; i < histElements.size(); i++){
			elementNames[i] = histElements.get(i).getFullName();
		}

		VSEChooser chooseVSE = new VSEChooser(PlatformUI
				.getWorkbench().getActiveWorkbenchWindow()
				.getShell(),
				histElements.toArray());
		chooseVSE.open();
		if (chooseVSE.getReturnCode()==Window.OK) {
			histResults = new ArrayList<FLATTTMember>();
			Object[] array = chooseVSE.getResult();
			for (Object obj : array){
				vse = (FLATTTMember) obj;
				
	
				for (AssociationRule rule : rules) {
					String vseName = vse.getFullName();
					rhs = rule.getRhs();
					if (vseName.equalsIgnoreCase(rule.getLhs().getFullName())
							&& !histResults.contains(rhs)){
						rhs.setProbability(String.valueOf(rule.getConfidenceValue()));
						histResults.add(rhs);
					}
				}
			}
			FLATTT.msrRulesFile = datFile;

			FLATTT.msrResults = histResults;
			FLATTT.MSRRun = true;
			FLATTT.MSRShown = true;
			CombineResultsAction.combine();
			FLATTT.tableView.updateToolbarButtons();
			TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
			contentP.refreshTable();
		}
	}
}
