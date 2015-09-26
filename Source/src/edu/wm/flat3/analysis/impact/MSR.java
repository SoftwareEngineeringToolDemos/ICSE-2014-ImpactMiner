package edu.wm.flat3.analysis.impact;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.tmatesoft.svn.core.SVNAuthenticationException;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.analysis.impact.actions.CombineResultsAction;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class MSR {
	private static ArrayList<FLATTTMember> histResults = new ArrayList<FLATTTMember>();
//	static String repoName;
//	static String startDate;
//	static String endDate;
	static ArrayList<FLATTTMember> histElements;
	static ArrayList<AssociationRule> rules;
	static boolean passed;
	static boolean cancelled;
	public static void doMSR(){
		histResults = new ArrayList<FLATTTMember>();
//		repoName = name;
//		startDate = start;
//		endDate = end;
		
		Job job = new RunMSRJob();
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();
		//new RunMSRJob().runInUIThread(new IProgressMonitor() );
	}
	static void queryRepo(IProgressMonitor monitor){
		// Using a shiny new java implementation
		
		
		passed = false;
		cancelled = false;
		while (!passed && !cancelled){
			try {
				SVNsearchAlt.search(monitor);
				passed = true;
			} catch (IOException e) {
				System.err.println("Could not write to file");
				e.printStackTrace(System.err);
			}catch (SVNAuthenticationException e){
				Display.getDefault().syncExec(new Runnable(){

					@Override
					public void run() {
						AuthInputDialog creds = new MSR().new AuthInputDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() );
						creds.open();
						if (creds.getReturnCode() != Window.OK){
							passed = true;
							cancelled = true;
							return;
						}
					}
					
				});
			}
		}
		if (cancelled) return;
		if(monitor.isCanceled())
			return;
		monitor.done();
		// Start Allowing the user to select a VSE
		// all of the unique elements found via the search
		histElements = SVNsearchAlt.elementList;
		rules = SVNsearchAlt.ruleList;

		String[] elementNames = new String[histElements.size()];
		for (int i=0; i < histElements.size(); i++){
			elementNames[i] = histElements.get(i).getFullName();
		}

		//TODO: if seedAlreadySelected, just use it;
		PreferenceStore prefs = FLATTT.getIAPreferenceStore();
		if (prefs.getBoolean("seedAlreadySelected")){
			FLATTTMember vse = new FLATTTMember((IMember) FLATTT.repository.getComponentWithName(prefs.getString("seedMethod")).getJavaElement());
			FLATTTMember rhs;
			String vseName = vse.getFullName();
			for (AssociationRule rule : rules){
				rhs = rule.getRhs();
				if (vseName.equalsIgnoreCase(rule.getLhs().getFullName())
						&& !histResults.contains(rhs)){
					rhs.setProbability(String.valueOf(rule.getConfidenceValue()));
					histResults.add(rhs);
				}
			}
			FLATTT.msrRulesFile = SVNsearchAlt.datFile;

			FLATTT.msrResults = histResults;
			FLATTT.MSRRun = true;
			FLATTT.MSRShown = true;
			Display.getDefault().syncExec(new Runnable(){

				@Override
				public void run() {
					CombineResultsAction.combine();
					FLATTT.tableView.updateToolbarButtons();
					TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
					contentP.refreshTable();
				}
				
			});
			
		}else{
			Display.getDefault().syncExec(new Runnable(){

				@Override
				public void run() {
					FLATTTMember vse;
					FLATTTMember rhs;
					VSEChooser chooseVSE = new VSEChooser(PlatformUI
							.getWorkbench().getActiveWorkbenchWindow()
							.getShell(),
							histElements.toArray());
					chooseVSE.open();
					if (chooseVSE.getReturnCode()==Window.OK) {
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
						FLATTT.msrRulesFile = SVNsearchAlt.datFile;

						FLATTT.msrResults = histResults;
						FLATTT.MSRRun = true;
						FLATTT.MSRShown = true;
						CombineResultsAction.combine();
						FLATTT.tableView.updateToolbarButtons();
						TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
						contentP.refreshTable();
					}

				}

			});
		}
		
	}


	
	private class AuthInputDialog extends Dialog{
		private static final int RESET_ID = IDialogConstants.NO_TO_ALL_ID + 1;
		private Text uName;
		private Text pwd;
		protected AuthInputDialog(Shell parentShell) {
			super(parentShell);
		}
		protected AuthInputDialog(IShellProvider p){
			super(p);
		}
		protected Control createDialogArea(Composite parent) {
		    Composite comp = (Composite) super.createDialogArea(parent);
		    GridLayout layout = (GridLayout) comp.getLayout();
//		    layout.numColumns = 1;
		    GridData span = new GridData();
		    span.horizontalSpan = 2;
		    Label reason = new Label(comp, SWT.CENTER);
		    reason.setText("The Repository you specified requires Authentication.");
		    reason.setLayoutData(span);
//		    Label dummy = new Label(comp, SWT.LEFT);
//		    dummy.setText("");
		    layout.numColumns = 2;
		    Label usernameLabel = new Label(comp, SWT.RIGHT);
		    usernameLabel.setText("Username: ");
		    uName = new Text(comp, SWT.SINGLE);
		    GridData data = new GridData(GridData.FILL_HORIZONTAL);
		    uName.setLayoutData(data);
		    Label passwordLabel = new Label(comp, SWT.RIGHT);
		    passwordLabel.setText("Password: ");
		    pwd = new Text(comp, SWT.SINGLE | SWT.PASSWORD);
		    data = new GridData(GridData.FILL_HORIZONTAL);
		    pwd.setLayoutData(data);
		    return comp;
		}
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			createButton(parent, RESET_ID, "Reset All", false);
		}

		protected void buttonPressed(int buttonId) {
			if (buttonId == RESET_ID) {
				uName.setText("");
				pwd.setText("");
			} else if (buttonId == IDialogConstants.OK_ID) {
				SVNsearchAlt.setName(uName.getText());
				SVNsearchAlt.setPassword(pwd.getText());
				super.buttonPressed(buttonId);
			}else {
				super.buttonPressed(buttonId);
			}
		}
	}	

}

