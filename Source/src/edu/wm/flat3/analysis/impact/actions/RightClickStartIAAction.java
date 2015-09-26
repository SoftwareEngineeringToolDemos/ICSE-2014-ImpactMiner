package edu.wm.flat3.analysis.impact.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import edu.wm.flat3.actions.OpenSearchViewAction;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.analysis.impact.AssociationRule;
import edu.wm.flat3.analysis.impact.IAInputDialog;
import edu.wm.flat3.analysis.impact.MSR;
import edu.wm.flat3.analysis.impact.SVNsearchAlt;
import edu.wm.flat3.analysis.lucene.FLATTTLuceneAnalysis;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.mutt.MUTTTrace;
/**
 * <code>RightClickStartIAAction</code> is the action that gets executed when the user
 * right clicks on a source code file and selects the option to do impact analysis.
 * @author mmwagner@email.wm.edu
 *
 */
@SuppressWarnings({ "restriction", "unused" })
public class RightClickStartIAAction extends Action implements
		IObjectActionDelegate {
	static Object selection;
	public static boolean inUse = false;
	private static boolean usingDyn = false;
	private static boolean usingHist = false;
	public static boolean muttDone = false;
	
	public static ArrayList<FLATTTMember> impactSet;		// the ultimate results of out efforts
	private static ArrayList<FLATTTMember> luceneResults;
	private static ArrayList<FLATTTMember> traceResults;
	private static ArrayList<FLATTTMember> histResults;
	private static ArrayList<FLATTTMember> tmp;				// used for moving stuff around
	
	private static int n;									// the desired size of the impact set
	
	//variables for repository mining;
	private static String repoName = "";
	private static String startDate;
	private static String endDate;
	private static final boolean fine = true;
	private static CompilationUnit unit;
	private static IPath directory;

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(IAction action) {
		if (selection instanceof CompilationUnit) {
			inUse = true;
			unit = (CompilationUnit) selection;
			
			directory = unit.getJavaProject().getResource().getLocation();
			if (directory.append("bin").toFile().exists()) directory = directory.append("bin");
			
			String className = "";
			char[][] packageName = unit.getPackageName();
			for (char[] pck : packageName) {
					className += String.valueOf(pck)+".";
			}
			className += unit.getElementName().substring(0, unit.getElementName().indexOf(".java"));		
			
			traceResults = new ArrayList<FLATTTMember>();
			histResults = new ArrayList<FLATTTMember>();
			impactSet = new ArrayList<FLATTTMember>();
			tmp = new ArrayList<FLATTTMember> (); 
			n = 0; 
		//Get input			
			IAInputDialog settings = new IAInputDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
			settings.open();
			if (settings.getReturnCode() == Window.OK){
				n = settings.getInputSize();
				FLATTTLuceneAnalysis.setSearchString(settings.getSearchTerm());
				//FLATTTLuceneAnalysis.luceneDoSearch();
				
				//TODO: try to make sure that the results are written to the correct variable when this is run.
				//luceneResults = FLATTT.searchResults;
				FLATTT.sortFLATTTMemberList(FLATTT.luceneResults);
				usingDyn = settings.getRunTrace();
				usingHist = settings.getRunHist();
				repoName = settings.getRepoAddy();
				startDate = settings.getStartDate();
				endDate = settings.getEndDate();
				PreferenceStore prefs = FLATTT.getIAPreferenceStore();
				prefs.setValue("seedAlreadySelected", false);
				try {
					prefs.save();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			else{
				inUse = false;
				return;
			}
			
			
			
		//Done getting input
			//FLATTT.luceneRun = true;  //Done with call to luceneDoSearch()
			getData1();
		}

	}

	public static void getData1(){
		if (usingDyn){
			FLATTT.traceRun = true;
			String className = "";
			char[][] packageName = unit.getPackageName();
			for (char[] pck : packageName) {
					className += String.valueOf(pck)+".";
			}
			className += unit.getElementName().substring(0, unit.getElementName().indexOf(".java"));
			String args[] = {};
			MUTTTrace.trace(directory.toOSString(), className, args, unit.getJavaProject());
		}
		else {
			getData2();
		}
	}
	public static void getData2(){
		if (usingHist){
			MSR.doMSR();			
		}
		processData();
	}
	
	public static void processData(){
		FLATTT.luceneShown = true;
		
		// Update the table view with the results of our search.
		CombineResultsAction.combine();
		FLATTT.tableView.updateToolbarButtons();
		TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
		contentP.refreshTable();
		inUse = false;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection)
			RightClickStartIAAction.selection = ((StructuredSelection)selection).getFirstElement();

	}


}
