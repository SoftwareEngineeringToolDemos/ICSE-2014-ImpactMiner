/* ConcernMapper - A concern modeling plug-in for Eclipse
 * Copyright (C) 2006  McGill University (http://www.cs.mcgill.ca/~martin/cm)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * $Revision: 1.11 $
 */

package edu.wm.flat3;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.FLATTTTableView;
import edu.wm.flat3.analysis.lucene.FLATTTLuceneAnalysis;
import edu.wm.flat3.analysis.lucene.LuceneIndexer;
import edu.wm.flat3.analysis.visualization.VisualizationView;
import edu.wm.flat3.model.ConcernModelFactory;
import edu.wm.flat3.repository.CodeModelImporter;
import edu.wm.flat3.repository.Concern;
import edu.wm.flat3.repository.ConcernRepository;
import edu.wm.flat3.ui.ConcernView;
import edu.wm.flat3.ui.concerntree.ConcernTreeViewer;
import edu.wm.flat3.util.ProblemManager;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author vibhav.garg
 */
public class FLATTT extends AbstractUIPlugin
{
	// The following ids and class names must match the ids and
	// names defined in the plugin.xml file *exactly*.  Don't
	// forget to update them when plugin.xml is modified.
	
	/** Must match the Plugin ID shown in Overview - General Information */ 
	public static final String ID_PLUGIN = 
		"edu.wm.ImpactMiner";

	public static final String ID_DECORATOR = 
		"edu_wm_flat3_Decorator";

	public static final String ID_SCATTERING_METRICS_VIEW = 
		"edu_wm_flat3_ScatteringMetricsView";
	
	private static final String NAME_RESOURCE_CLASS = 
		"edu.wm.flat3.FLATTTResources";

	public static final String ID_SEARCH_VIEW = "edu_wm_flat3_TableView";
	

	public static final String ID_VISUALIZATION_VIEW = "edu_wm_flat3_VisualizationView";
	
	// The shared instance.
	private static FLATTT singleton;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	// Reference to HSQLDB object
	public static ConcernRepository repository;

	// Results for FLATTTTableView
	// This is the variable that the table view pulls from in order to display
	// I should keep each combinational result saved elsewhere, and point this
	// at the one I want to display.
	public static ArrayList<FLATTTMember> searchResults;

	// If a combinational search is done, this will contain the original results\
	public static ArrayList<FLATTTMember> originalSearchResults = null;
	
	public static ArrayList<FLATTTMember> luceneResults;
	public static ArrayList<FLATTTMember> traceResults;
	public static ArrayList<FLATTTMember> msrResults;
	
	//public static boolean searchResultsAreTrace;
	//This represents whether or not the displayed results are the result of a refining search.
	public static boolean searchResultsAreCombinational;
	
	/* 
	 * List of boolean variables for each module run
	 */
	public static boolean traceRun 	= false;
	public static boolean luceneRun = false;
	public static boolean MSRRun 	= false;
	
	/*
	 * List of boolean variables representing which modules are currently shown
	 */
	public static boolean traceShown 	= false;
	public static boolean luceneShown 	= false;
	public static boolean MSRShown 		= false;
	
	public static File msrRulesFile = null;
	
	public static FLATTTTableView tableView;
	
	public static VisualizationView visualizationView;
	
	public static ConcernTreeViewer treeView;
	public static ConcernView concernView;

	public static UIJob nextSearch;

	public static Process muttProcess = null;
	public static Process svnProcess = null;

	// Fields to allow quick re-running of a trace
	public static String lastTraceDir = null;
	public static String lastTraceClassName;
	public static String[] lastTraceArgs;
	public static IJavaProject lastTraceProject;
	
	
	private static PreferenceStore inputPrefs;

	
	/**
	 * The constructor. Loads the resource bundle.
	 */
	public FLATTT()
	{
		assert singleton == null;
		
		singleton = this;

		try
		{
			resourceBundle = ResourceBundle.getBundle(NAME_RESOURCE_CLASS);
		}
		catch (MissingResourceException e)
		{
			ProblemManager.reportException(new Exception(
				"Missing Resource file: " + NAME_RESOURCE_CLASS));
		}

		// JavaCore.addElementChangedListener(new Checker());
	}

	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		// Create the default concern model
		ConcernModelFactory.singleton().getConcernModel(getRepository(), null);
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
		repository.shutdown();
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance.
	 * 
	 * @return The shared instance.
	 */
	public static FLATTT singleton()
	{
		return singleton;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 * 
	 * @param pKey
	 *            The key to use for the property lookup
	 * @return A string representing the resource.
	 */
	public static String getResourceString(String pKey)
	{
		final ResourceBundle lBundle = FLATTT.singleton()
				.getResourceBundle();
		try
		{
			return (lBundle != null) ? lBundle.getString(pKey) : pKey;
		}
		catch (MissingResourceException lException)
		{
			return pKey;
		}
	}
	
	public static boolean importCodeModel(IJavaProject proj) {
		return Concern.verifyCodeModelExists(getRepository(),proj);
	}
	
	public static void index() {
		// TODO: how do we determine when to recode model? when to reindex lucene?
		// TODO: Ask user which project(s) they want imported??
		// for now, just get all projects:
		ArrayList<IJavaProject> projects = new ArrayList<IJavaProject>();
		
		IWorkspace root = ResourcesPlugin.getWorkspace();
	    IProject[] allProjects = root.getRoot().getProjects();
	    
	    for( int i =0; i < allProjects.length; i++)  {  
	    	try {
				IJavaProject javaProj = (IJavaProject)allProjects[i].getNature(JavaCore.NATURE_ID);
		    	if (javaProj != null)
		    		projects.add(javaProj);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			}
	 	}
		
		// if the code model isn't generated, do it first automatically!
		for (IJavaProject proj : projects) {
			CodeModelImporter importer = new CodeModelImporter(getRepository(), proj);
			importer.run();
		}
		
		// then do the lucene indexing
		LuceneIndexer.setIndexDir(FLATTT.singleton().getStateLocation().append("luceneindex").toFile()); //TODO: should this be the projectname? or do we want one index for the whole workspace?
		LuceneIndexer.index(getRepository());
		
		// reset last search, so it'll run again if user runs same query
		FLATTTLuceneAnalysis.searchString = null;
	}
	
	public static void rebuildNodesMap() throws IOException {
		LuceneIndexer.rebuildNodesMap(getRepository());
	}
	
	/**
	 * @return The plugin's resource bundle
	 */
	public ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}

	/**
	 * Get the reference to the database
	 * 
	 * @return
	 */
	private static ConcernRepository getRepository()
	{
		if (repository == null)
		{
			// True means create the database if it doesn't exist
			repository = ConcernRepository.openDatabase();
		}

		return repository;
	}

	public IPath getTraceLocation() {
		//if (!this.getStateLocation().append("trace").toFile().exists()) this.getStateLocation().append("trace").toFile().mkdir();
		// TODO: empty out the directory?
		return this.getStateLocation().append("trace");
	}
	
	public void openView(String ID) {
		try
		{	IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		
//				IViewReference domainSpecificView =
//					site.getPage().findViewReference(ID);
				
//				if (domainSpecificView != null)
//				{
					// Activate it
					page.showView(ID);
					//site.getPage().activate(site.getPage().showView(ID));
//				} else
//					page.showView(ID); // Open it if it's not open yet
		}
		catch (PartInitException e)
		{
			ProblemManager.reportException(e);
		}
	}
	
	public String getInstallLocation() throws Exception {
		Bundle bundle = getBundle();
		URL locationUrl =
		FileLocator.find(bundle,new Path("/"), null);
		URL fileUrl = FileLocator.toFileURL(locationUrl);
		return fileUrl.getFile();
	}
	
	/**
	 * Prints msg to the specified PrintStream, followed by \t\t package.classname.functionname line #
	 * @param msg The message to be written
	 * @param outs the PrintStream to write on 
	 */
	public static void debugOutput(String msg, PrintStream outs)
	{
		if (msg.length() > 0) outs.print(msg + "\t\t");
		Throwable t = new Throwable();
		int i = 1;
		if (t.getStackTrace()[1].getFileName().equalsIgnoreCase("flattt.java")) i++;
		outs.println(t.getStackTrace()[i].getClassName() +
				":" + t.getStackTrace()[i].getMethodName() +
				" line " + t.getStackTrace()[i].getLineNumber());
	}
	/**
	 * Prints msg to the System.out, followed by \t\t package.classname.functionname line #
	 * @param msg The message to be written
	 */
	public static void debugOutput(String msg){
		debugOutput(msg,System.out);
	}
	
	public static void sortFLATTTMemberList(ArrayList<FLATTTMember> list) {
		Collections.sort(list, new Comparator<FLATTTMember>(){
			@Override
			public int compare(FLATTTMember arg0, FLATTTMember arg1) {
				float a0 = Float.valueOf(arg0.getProbability());
				float a1 = Float.valueOf(arg1.getProbability());
				if (a1 == a0) return 0;
				else if (a1<a0) return -1;
				else return 1;
			}
			
		});
	}
	
	public static PreferenceStore getIAPreferenceStore(){
		inputPrefs = new PreferenceStore(singleton().getStateLocation().append("IAprefs.cfg").toOSString());
		inputPrefs.setDefault("size", 100);
		inputPrefs.setDefault("term", "");
		inputPrefs.setDefault("trace", true);
		inputPrefs.setDefault("hist", true);
		inputPrefs.setDefault("addy", "");
		inputPrefs.setDefault("start", "");
		inputPrefs.setDefault("end", "");
		inputPrefs.setDefault("startRev", 0);
		inputPrefs.setDefault("endRev", 0);
		inputPrefs.setDefault("useRev",	true);
		inputPrefs.setDefault("minConf", 0.1);
		inputPrefs.setDefault("minSup", 0.05);
		inputPrefs.setDefault("maxSetSize", 100);
		inputPrefs.setDefault("maxSup", 1.0);
		inputPrefs.setDefault("seedAlreadySelected", false);
		
		try {
			inputPrefs.load();
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Did not find previous IA options, starting with blank");
		}
		return inputPrefs;
		
	}
}
