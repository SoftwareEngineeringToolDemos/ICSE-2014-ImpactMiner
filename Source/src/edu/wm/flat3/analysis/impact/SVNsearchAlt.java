package edu.wm.flat3.analysis.impact;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.PreferenceStore;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import edu.wm.cs.semeru.benchmarks.downloadSVNCommits.MainDownloadSVNCommits;
import edu.wm.cs.semeru.benchmarks.goldSetsGeneratorFromSVNCommits.MainGoldSetsGeneratorFromSVNCommits;
import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;


/**
 * @author mmwagner@email.wm.edu
 *
 */
public class SVNsearchAlt {
	protected static String pathDel = null;
	protected static IPath location = FLATTT.singleton().getStateLocation().append("MSR");
	/**
	 * This is the complete list of <code>AssociationRules</code>s that were
	 * mined from the repository.
	 */
	public static ArrayList<AssociationRule> ruleList;
	/**
	 * This contains the list of all methods which appeared as a premise in any
	 * of the mined rules.  It is a complete list of all methods in the project
	 * from which inferences can be made based on the repository data.
	 */
	public static ArrayList<FLATTTMember> elementList;
	public static File datFile = null;
	private static String name;
	private static String password;
	private static String repoShortName;

	
	/**
	 * Queries the svn repository and obtains all of the association rules
	 * derived from analysis of the repository commits during the specified
	 * period of time.   
	 * @param svnurl The address of the svn repository to be queried
	 * @param start "YYYY-MM-DD" - start of period to examine 
	 * @param end "YYYY-MM-DD" - end of period to examine
	 * @throws IOException If files downloaded from repository cannot be written to disk.
	 */
	@SuppressWarnings("rawtypes")
	public static void search(IProgressMonitor monitor) throws IOException, SVNAuthenticationException{
		//FLATTT.debugOutput("Entering function", System.out);
		if (pathDel == null) assignPathDel();
		File topDir = location.toFile();
		//File diffDir = location.append("diffs").toFile();
		if (!topDir.exists()) topDir.mkdirs();
		//if (!diffDir.exists()) diffDir.mkdir();
		//FLATTT.debugOutput("finished making directories", System.out);
		DAVRepositoryFactory.setup( );/////////////////////////////////////////////////
		//if (name == null) name = "guest";
		//if (password == null) password = "";
		long startRevision = 0;
		long endRevision = -1;	// last revision, head
		PreferenceStore inputPrefs = FLATTT.getIAPreferenceStore();/////////////////////////////////////
		try {
			inputPrefs.load();
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("Did not find previous IA options, starting with blank");
		}

		
		//FLATTT.debugOutput("about to call suffixRepUrl", System.out);
		String svnurl =inputPrefs.getString("addy");
		repoShortName = suffixRepUrl(svnurl);
		//System.err.println("the short name of the repo is: " + repoShortName);
		SVNAuthenticationException ex = null;//////////////////////////////////////////////////////
		try {
			SVNURL url = SVNURL.parseURIEncoded(svnurl);
			SVNRepository repo = SVNRepositoryFactory.create(url);
			/*
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( name, password );
			repo.setAuthenticationManager(authManager);
			Collection logEntries = null;
			*/
			if (inputPrefs.getBoolean("useRev")){
				startRevision = inputPrefs.getInt("startRev");
				endRevision = inputPrefs.getInt("endRev");
			}
			else{
				String start = inputPrefs.getString("start");
				String end = inputPrefs.getString("end");
				Calendar c = Calendar.getInstance();
				String[] startVals = start.split("-");
				c.set(Integer.valueOf(startVals[0]), Integer.valueOf(startVals[1])-1, Integer.valueOf(startVals[2]));
				Date startDate = c.getTime();

				String[] endVals = end.split("-");
				c.set(Integer.valueOf(endVals[0]), Integer.valueOf(endVals[1])-1, Integer.valueOf(endVals[2]));
				Date endDate = c.getTime();
				startRevision = repo.getDatedRevision(startDate);
				//if (startRevision < 2) startRevision = 2;
				endRevision = repo.getDatedRevision(endDate);
			}
			
			ruleList =  new ArrayList<AssociationRule>();
			elementList = new ArrayList<FLATTTMember>();
			monitor.beginTask("Querying SVN Repository", (int) (endRevision - startRevision));

			//IPath location = FLATTT.singleton().getStateLocation().append("history").append("MSRprefs");
			PreferenceStore miningPrefs = FLATTT.getIAPreferenceStore();
			try {
				miningPrefs.load();
			} catch (IOException e) {
				System.out.println("could not read IA settings from disk, using defaults");
			}
			/*
			if (miningPrefs.getBoolean("seedAlreadySelected")){
				datFile = location.append(repoShortName + "_" + "rev(" + startRevision 
						+ "-" + endRevision + ")-c" + miningPrefs.getString("minConf") 
						+ "-s" + miningPrefs.getString("minSup") 
						+ miningPrefs.getString("seedMethod") +  ".dat").toFile();
			}else{
				datFile = location.append(repoShortName + "_" + "rev(" + startRevision 
						+ "-" + endRevision + ")-c" + miningPrefs.getString("minConf") 
						+ "-s" + miningPrefs.getString("minSup") +  ".dat").toFile();
			}*/
			String filename = startRevision+"-"+endRevision+".dat";
			datFile = location.append(repoShortName).append("Rules").append(filename).toFile();
			if (datFile.exists()){
				System.out.println("Reading rules from file:"+datFile.toString());
				ruleList = RuleMiner.mineRules(datFile);
				elementList = RuleMiner.elementList;
				FLATTT.msrRulesFile = datFile;
				System.out.println("Get "+ruleList.size()+" rules from the file");
				monitor.done();
				return;
			}
			//*****************added by shasha*******************//
			try{
	    		String[] args;
	    		args=new String[5];
	    		args[0]=svnurl;
	    		args[1]=String.valueOf(startRevision);
	    		args[2]=String.valueOf(endRevision);
	    		args[3]=location.append(repoShortName).toString()+"/";
	    		try{
	    			monitor.setTaskName("Downloading the commits");
	    			MainDownloadSVNCommits.Download(args,monitor);
	    			if(monitor.isCanceled())
	    				return;
	    		}catch(Exception e){
	    			System.err.println("Error in downloading");
	    		}
	    		try{
	    			monitor.setTaskName("Generating the gold sets");
	    			MainGoldSetsGeneratorFromSVNCommits.GoldSetsGenerator(args[3],startRevision,endRevision,monitor);
	    			if(monitor.isCanceled())
	    				return;
	    		}catch(Exception e){
	    			System.err.println("Error in generating the gold sets");
	    		}
			}catch (Exception e)
			{
				System.err.println("Error in accessing the repositories");
			}
			//****************************************************//
			
			////////////////////////////////////////////////////////////////
			try{
				monitor.setTaskName("Building rules");
				ruleList = RuleMiner.mineRules(repoShortName, startRevision,endRevision);
				elementList = RuleMiner.elementList;
			}catch(Exception e){
				System.err.println("Errors occur when building the rules");
			}
//			FLATTT.debugOutput("About to print " + ruleList.size() + " processed rules:");
//			for (AssociationRule rule : ruleList)
//				System.out.println(rule);
		} catch (SVNAuthenticationException e){
			ex = e;
		} catch (SVNException e) {
				e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		if (ex != null)
			throw ex;
		//FLATTT.debugOutput("Exiting the Function");
	} //end method search
	
	/**
	 * Determines if the given file should be downloaded.   It should be
	 * downloaded if it is of a supported source type. 
	 * @param filename
	 * @return <code>true</code> if the filename is of a supported source type
	 * and should be downloaded, <code>false</code> otherwise.
	 */
	protected static boolean suffixTest(String filename) {
		String[] goodSuffixes={".java", ".c", ".h", ".cpp", ".cc", ".hpp", ".py"};
		for (String suffix : goodSuffixes) {
			if (filename.endsWith(suffix))
				return true;
		}
		return false;
	}


	private static String suffixRepUrl(final String svnurl) {
		String tmpSvnUrl= String.copyValueOf(svnurl.toCharArray());
		String suffix = tmpSvnUrl.substring(tmpSvnUrl.lastIndexOf('/')+1, tmpSvnUrl.length()).trim();
		while (suffix.equalsIgnoreCase("trunk")){
			System.err.println(suffix);
			tmpSvnUrl = tmpSvnUrl.substring(0, tmpSvnUrl.lastIndexOf('/')); 
			suffix = tmpSvnUrl.substring(tmpSvnUrl.lastIndexOf('/')+1, tmpSvnUrl.length()).trim();
		}
		return suffix;
	}

	/**
	 * Determines whether the system path deliminator is / or \.
	 */
	static void assignPathDel() {
		if (System.getProperty("os.name").startsWith("Windows")) 
			pathDel = "\\";
		else
			pathDel = "/";
	}
	
	/**
	 * Sets the username that will be used to log into the repository.
	 * @param name
	 */
	public static void setName(String name) {
		SVNsearchAlt.name = name;
	}

	/**
	 * sets the password that will be used to log into the repository.
	 * Not guaranteed secure.
	 * @param password
	 */
	public static void setPassword(String password) {
		SVNsearchAlt.password = password;
	}
	
	
	
}
