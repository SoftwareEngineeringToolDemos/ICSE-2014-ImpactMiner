package edu.wm.flat3.analysis.mutt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.actions.OpenSearchViewAction;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.FLATTTTableView;
import edu.wm.flat3.analysis.TableViewContentProvider;
import edu.wm.flat3.analysis.impact.actions.CombineResultsAction;
import edu.wm.flat3.analysis.impact.actions.RightClickStartIAAction;
import edu.wm.flat3.repository.Component;

public class MUTTTrace {

	public static void trace(String directory, String className, String arguments[], IJavaProject proj) {
		if (FLATTT.muttProcess == null) {
			// First, check if code model is loaded and if not load it!
			if (FLATTT.importCodeModel(proj)) { // It's imported
				// Run the trace, then load the results
				new RunTraceJob(directory, className, arguments).schedule();
				new LoadTraceJob().schedule();
			} else {
				// User chose not to import code model; trace won't work properly
			}
		} else {
			// Trace already in progress, tell the user
			MessageBox dialog = new MessageBox(Display.getCurrent().getActiveShell());
			dialog.setMessage("Another trace is already running. Please complete or cancel this trace before begining another.");
			dialog.open();
		}
	}
	
	static void loadTraceData() {
		// Parse all .dot files, right?
		File dir = FLATTT.singleton().getTraceLocation().toFile();//new File(directory);
		ArrayList<FLATTTMember> nodes = new ArrayList<FLATTTMember>();
		
		for (String file : dir.list()) {
			if (file.endsWith(".dot") && file.startsWith("dcg.")) {
		
				TraceGraph g = new TraceGraph();
				if (!g.parseDOT(dir + "/" + file)) continue; // if file's invalid, skip it
		
				Hashtable<String,CallNode> results = g.callNodes;
				 
				Iterator itr = g.callNodes.keySet().iterator();
				while(itr.hasNext()) {
					String key = (String) itr.next();
				
					String cname = key.split("\\.",2)[0];
					String name = key.split("\\.",2)[1];
					//System.out.println(cname+ " : " + name);
		
					IMember imember = null;
		
					Component comp =  FLATTT.repository.getComponentWithName(key);
					//System.err.println("The key is: " + key);
					if (comp != null) imember = (IMember) comp.getJavaElement();
					
					if (imember != null) {
						FLATTTMember n = new FLATTTMember(imember);
						nodes.add(n);
					}
				}
			}
		}
		
//		System.err.println("In MUTTTTtrace.loadTraceData()...\nprinting list of methods");
//		for (FLATTTMember member : nodes) {
//			System.err.println(member);
//		}
	
		// Put nodes somewhere ContentProvider can get to it
		FLATTT.searchResults = nodes;
		FLATTT.originalSearchResults = nodes;
		//FLATTT.searchResultsAreTrace = true;
		FLATTT.traceResults = nodes;
		FLATTT.traceRun = true;
		FLATTT.traceShown = true;
		CombineResultsAction.combine();
		FLATTT.tableView.updateToolbarButtons();

		try{
			if (RightClickStartIAAction.inUse)
				RightClickStartIAAction.getData2();
			else throw new NullPointerException();
		}catch (NullPointerException e){
			System.out.println("doing stand-alone trace.");
		}

	
		OpenSearchViewAction a = new OpenSearchViewAction();
		a.run();
			
		TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
		contentP.refreshTable();
		
		//System.err.println("Done loading TraceData");
	}
	
	/**
	 * Saves the last trace done to a zip file for later reloading.
	 * @param filename
	 * @throws IOException
	 */
	public static void exportToFile(String filename) throws IOException {
		FileOutputStream zipFile = new FileOutputStream(filename);
		ZipOutputStream zip = new ZipOutputStream(zipFile);

		File dir = FLATTT.singleton().getTraceLocation().toFile();
		
		for (String file : dir.list()) {
			ZipEntry entry = new ZipEntry(file);
			FileInputStream fileStream = new FileInputStream(dir +"/" + file);
			
			zip.putNextEntry(entry);
	        for (int c = fileStream.read(); c != -1; c = fileStream.read()) {
	        	zip.write(c);
	        }
			fileStream.close();
		}
		
		
		zip.close();
		zipFile.close();
	}
	
	/**
	 * Loads a trace from a zip file into our data directory and loads it into the view
	 * @param filename
	 * @throws IOException
	 */
	public static void importFromFile(String filename) throws IOException {
		// overwrite existing map? GUI should warn about that "this will overwrite your current map for project x, are you sure?"
		
		// extract files to current map directory, load them up.
		ZipFile zip = new ZipFile(filename);
		 for (Enumeration e = zip.entries(); e.hasMoreElements();)
         {
			ZipEntry entry = (ZipEntry) e.nextElement();
			InputStream entryIn = zip.getInputStream(entry);
		
			FileOutputStream entryOut = new FileOutputStream(FLATTT.singleton().getTraceLocation().toOSString() + "/" + entry.getName());		
			for (int c = entryIn.read(); c != -1; c = entryIn.read()) {
			       entryOut.write(c);
			}
		}
		
		loadTraceData();
	}	
	
}
	
