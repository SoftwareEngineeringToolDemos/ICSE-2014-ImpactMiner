package edu.wm.flat3.analysis.lucene;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import edu.wm.flat3.CodeModelRule;
import edu.wm.flat3.FLATTT;
import edu.wm.flat3.repository.Component;
import edu.wm.flat3.repository.ConcernRepository;
//import org.severe.jripples.eig.JRipplesEIG;
//import org.severe.jripples.eig.JRipplesEIGNode;
//import org.severe.jripples.logging.JRipplesLog;


import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
/**
 * This class provides a fairly convenient interface to the specific lucene features used by this plugin.
 * The method search(String) is the main one that should be used to simply run a search.  It returns
 * a hashmap of source elements and relevance scores. 
 * @author trevor savage (presumably)
 *
 */
public class LuceneIndexer {

	/**
	 * This seems to be the variable in which the the results of a search are stored.
	 */
	private static HashMap <IMember, String> pathMatches = new HashMap<IMember, String>();
	/**
	 * This seems to be the variable in which represents the indexing of the project.
	 */
	private static HashMap <String, IMember> nodesMap = new HashMap<String, IMember>();
	private static File indexDir;

	private static String[] getStopWords() {
		String[] JAVA_STOP_WORDS = { "public", "private", "protected",
				"interface", "abstract", "implements", "extends", "null",
				"new", "switch", "case", "default", "synchronized", "do", "if",
				"else", "break", "continue", "this", "assert", "for",
				"instanceof", "transient", "final", "static", "void", "catch",
				"try", "throws", "throw", "class", "finally", "return",
				"const", "native", "super", "while", "import", "package",
				"true", "false" };

		HashSet<String> st = new HashSet<String>(Arrays.asList(StopAnalyzer.ENGLISH_STOP_WORDS));
		st.addAll(Arrays.asList(JAVA_STOP_WORDS));

		return st.toArray(new String[st.size()]);
	}

	/**
	 * This function indexes the workspace.
	 * @param concernRep This seems to be a data structure in which all project components are stored
	 */ 
	/* 
	 * Find out where this method is called from and where the parameter concernRep is defined.
	 * 
	 * This is called from FLATTT.index(), a static function.  concernRep is defined by means of a call to
	 * a static function of the ConcernRepository class.
	 */
	public static void index(final ConcernRepository concernRep) {
		try {
			//System.err.println("Lucene: Creating a new Job");
			Job job = new Job("FLAT3 Lucene Indexing") { // this block of code defines what happens when the job is run
		     protected IStatus run(IProgressMonitor monitor) {
		    		nodesMap.clear();  //clear this, which is presumably a representation of source code elements

					try {
						IndexWriter writer = new IndexWriter(indexDir,new SnowballAnalyzer("English", getStopWords()),true);

						// TODO, vital: Modify this to work with the FLATTT database instead (did we do this yet?)
						List<Component> nodes = concernRep.getAllComponents();
						monitor.beginTask("Building index", nodes.size());//this sets the name of the task as displayed in the pop-up window.
						/*
						 * We just got a large list of components into a list called nodes
						 * for each component:
						 *   We are going to first see if the user hit 'cancel' on the progress window that pops up
						 * 	 Next if the component is either a method or a field:
						 *     call indexNode, passing in the IndexWriter object and the component, cast as an IMember
						 *     TODO: figure out what indexNode does
						 *     puts the component into nodesMap
						 *     lets the monitor know that 1 element of work has been done.
						 */
						for (Component comp : nodes) { //(int i = 0; i < nodes.length; i++) {
						    if (monitor.isCanceled()) return Status.CANCEL_STATUS;
							if (( comp.getJavaElement() instanceof IField) ||
							    ( comp.getJavaElement() instanceof IMethod)) {
								indexNode(writer, (IMember) comp.getJavaElement());
								nodesMap.put(comp.getJavaElement().getHandleIdentifier(), (IMember) comp.getJavaElement());
								monitor.worked(1);
							}
						}
						
						monitor.done();
						writer.close();
						
					} catch (Exception e) {
						//JRipplesLog.logError(e); // TODO: Log via some other method?
					}
		        //   if (monitor.isCanceled()) return Status.CANCEL_STATUS;
		           
		           return Status.OK_STATUS;
		        }
		     }; //end of 'new Job' statement.  This preceding code defines what happens when the job is run
		  job.setPriority(Job.LONG);
		  job.setRule(new CodeModelRule());
		  job.setUser(true);
		  job.schedule();
		
//		ProgressMonitorDialog progress = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
//		try {
//			progress.run(false, false, new IRunnableWithProgress() {
//				public void run(IProgressMonitor monitor)
//				throws InvocationTargetException, InterruptedException {
//
//					nodesMap.clear();
//
//					try {
//						IndexWriter writer = new IndexWriter(indexDir,new SnowballAnalyzer("English", getStopWords()),true);
//
//						// TODO, vital: Modify this to work with the FLATTT database instead (did we do this yet?)
//						List<Component> nodes = concernRep.getAllComponents();
//						monitor.beginTask("Building index", nodes.size());
//						
//						for (Component comp : nodes) { //(int i = 0; i < nodes.length; i++) {
//							if (( comp.getJavaElement() instanceof IField) ||
//							    ( comp.getJavaElement() instanceof IMethod)) {
//								indexNode(writer, (IMember) comp.getJavaElement());
//								nodesMap.put(comp.getJavaElement().getHandleIdentifier(), (IMember) comp.getJavaElement());
//								monitor.worked(1);
//							}
//						}
//						
//						monitor.done();
//						writer.close();
//						
//					} catch (Exception e) {
//						//JRipplesLog.logError(e); // TODO: Log via some other method?
//					}
//				}
//			}
//			);
		} catch (Exception e) {
		}
		;
	}

	/**
	 * Creates a <code>Document</code> representing <code>node</code> and adds it the <code>writer</code>.
	 * @param writer
	 * @param node
	 */
	private static void indexNode(IndexWriter writer,IMember node) {
		Document doc = new Document();
		String source;
		try {
			node.getOpenable().open(new NullProgressMonitor());	
			source=node.getSource();
			if (source!=null) {
				doc.add(new Field("contents",source,Field.Store.NO, Field.Index.TOKENIZED));
				doc.add(new Field("nodeHandlerID", node.getHandleIdentifier(), Field.Store.YES, Field.Index.UN_TOKENIZED));
				writer.addDocument(doc);}
		} catch (Exception e) {
			//JRipplesLog.logError(e);
		}
	}


	public static void setIndexDir(File indexDir) {
		if (LuceneIndexer.indexDir!=null) 
			if (LuceneIndexer.indexDir.compareTo(indexDir)!=0)
				if (nodesMap!=null)
					nodesMap.clear();
		LuceneIndexer.indexDir = indexDir;
	}

	/**
	 * This function indexes the workspace if it has not been indexed.
	 * If it HAS been indexed, it rebuilds the <code>nodesMap</code> by gathering the active
	 * <code>ConcernRepository</code> and then calling the <code>rebuildNodesMap()</code> function
	 * of this class. 
	 * @throws Exception
	 * @see rebuildNodesMap
	 */
	public static void checkIfIndexed()  throws Exception {
		if ((nodesMap==null) || (nodesMap.size()==0)) {
			if (indexDir.exists()) FLATTT.rebuildNodesMap();  // TODO: why does it know it doesn't need to index if the dir exists??
			else FLATTT.index();	//This function basically gathers some contextual information (folder names, etc) and
									//calls the void index() function of this class. 
		}
		
		if (FLATTT.nextSearch != null) {// now that indexing's started, do any pending search
			FLATTT.nextSearch.schedule();
			FLATTT.nextSearch = null;
		}
	}

	/**
	 * This is the function that actually performs the search.
	 * @param queryString
	 * @return <code>pathMatches</code>: the private member variable in which the results of the search are stored.
	 * @throws Exception
	 */
	public static HashMap<IMember, String> search(String queryString) throws Exception {
//		FLATTT.debugOutpout("Starting a search for " + queryString, System.err);
		pathMatches.clear();			// clear the results variable
		checkIfIndexed();				// the search requires that the project be indexed.

		Directory fsDir = FSDirectory.getDirectory(indexDir, false);		//does this get the directory where the raw data is stored?

		if (fsDir.list()==null) FLATTT.index();
		if (fsDir.list().length==0) FLATTT.index();
		//Im not sure why these two preceding lines are needed.  Doesn't the call to checkIfIndexed() ensure that
		//the project has been initialized once we get this far into the function?

		IndexSearcher indexSearcher=null;
		try {
			//IndexSearcher, QueryParser, Query and Hits are all components defined by the Lucene library
			indexSearcher = new IndexSearcher(fsDir);			

			QueryParser parser = new QueryParser("contents",	
					new SnowballAnalyzer("English", getStopWords()));

			Query query = parser.parse(queryString);
//			FLATTT.debugOutput("The parsed query string is: " + query,System.err);
			Hits hits = indexSearcher.search(query);

			//for loop iterates over each hit, puts them into pathMatches along with their relevance score.
			for (int i = 0; i < hits.length(); i++) {
				Document doc = hits.doc(i);
				pathMatches.put(nodesMap.get(doc.get("nodeHandlerID")), Float.valueOf(hits.score(i)).toString());
			}
		} finally {
			if (indexSearcher != null) {
				indexSearcher.close();
			}
		}

		return pathMatches;
	}

	public static void rebuildNodesMap(final ConcernRepository concernRep) throws IOException {
		if (nodesMap==null) nodesMap = new HashMap<String, IMember>();

		ProgressMonitorDialog progress = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		try {
			progress.run(false, false, new IRunnableWithProgress (){

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					
					List<Component> nodes = concernRep.getAllComponents();
					HashMap<String, IMember> nodesHandelMap=new HashMap<String, IMember>();

					IndexReader reader;
					try {
						monitor.beginTask("Rebuilding node index map", 10);
						IProgressMonitor subMonitor=new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );
						subMonitor.beginTask("Building EIG nodes map", nodes.size());
						
						for (Component comp : nodes) { //(int i = 0; i < nodes.length; i++) {
							if ( comp.getJavaElement() instanceof IMember) { // == IMember.TYPE ) {
								nodesHandelMap.put(comp.getJavaElement().getHandleIdentifier(), (IMember) comp.getJavaElement());
								subMonitor.worked(1);
							}
						}
						
						subMonitor.done();
						subMonitor=new SubProgressMonitor(monitor, 9, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK );

						reader = IndexReader.open(indexDir);
						subMonitor.beginTask("Building EIG nodes map", reader.numDocs());

						for (int i=0;i<reader.numDocs();i++) {
							String handler=reader.document(i).get("nodeHandlerID");
							if (handler==null) continue;
							if (nodesHandelMap.containsKey(handler))
								nodesMap.put(handler, nodesHandelMap.get(handler));
							subMonitor.worked(1);
						}
						
						subMonitor.done();
						monitor.done();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			} 


			);	
		}
		catch (Exception e) {

		};

	}
	public static HashMap<String, IMember> getNodesMap(){
		return nodesMap;
	}

}
