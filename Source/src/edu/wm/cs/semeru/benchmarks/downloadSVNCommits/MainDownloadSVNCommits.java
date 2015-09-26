package edu.wm.cs.semeru.benchmarks.downloadSVNCommits;

import org.eclipse.core.runtime.IProgressMonitor;

public class MainDownloadSVNCommits
{
	
	public static void Download(String[] args,IProgressMonitor monitor) throws Exception
	{
		
		args[4]="guest";
		
		if (args.length<4 || args.length>6)
		{
			System.err.println("Extract SVN data from the SVN repositories");
			System.err.println("Usage:");
			System.err.println("  java -jar DownloadSVNCommits.jar URL startRevision endRevision outputFolder [username] [password]");
			System.err.println();
			System.err.println("Where:");
			System.err.println("  URL");
			System.err.println("    is the url of the SVN repository");
			System.err.println("  startRevision");
			System.err.println("    is the start revision number (e.g., 7)");
			System.err.println("  endRevision");
			System.err.println("    is the end revision number (e.g., 123)");
			System.err.println("  outputFolder");
			System.err.println("    is the folder name where the SVN data will be saved");
			System.err.println("  [username]");
			System.err.println("    (optional) username for the SVN repository");
			System.err.println("  [password]");
			System.err.println("    (optional) password for the SVN repository");
			System.err.println();
			System.err.println("The tool will produce the following folders and files in the outputFolder folder:");
			System.err.println("  "+InputOutputDownloadSVNCommits.FOLDER_NAME_SVN_FILES_SIDE_BY_SIDE);
			System.err.println("    contains a folder for each SVN revision (e.g., N). Each of those folders contains the previous version of the file (if it exists) and the version of the file at revision N");
			System.err.println("  "+InputOutputDownloadSVNCommits.FOLDER_NAME_SVN_COMMENTS);
			System.err.println("    contains a file for each SVN revision (e.g., N.SVNComment, where N is the revision number)");
			System.err.println("  "+InputOutputDownloadSVNCommits.FOLDER_NAME_SVN_LIST_OF_FILES);
			System.err.println("    contains a file for each SVN revision (e.g., N.SVNListOfFiles, where N is the revision number). Each line of that file contains the file type (e.g., A, R, M) and the file name");
			System.err.println("  "+InputOutputDownloadSVNCommits.FOLDER_NAME_SVN_DEBUG);
			System.err.println("    contains a file for each SVN revision (e.g., N.SVNDebug, where N is the revision number). This file contains data (e.g., revision number, author, date, list of files, message, etc.) extracted from the repository");
			System.err.println("  "+InputOutputDownloadSVNCommits.FILE_NAME_LIST_OF_SVN_COMMITS);
			System.err.println("    is a file that contains the revision numbers (one per line) that have files with valid extension (e.g., *.java) that were added or modified in the commit");
			System.err.println();
			System.err.println("Example:");
			System.err.println("  java -jar DownloadSVNCommits.jar http://argouml.tigris.org/svn/argouml/trunk 15245 15248 TestCases/Output/ArgoUML/ guest");
			System.err.println("  java -jar DownloadSVNCommits.jar https://jedit.svn.sourceforge.net/svnroot/jedit 12212 12213 TestCases/Output/jEdit/");
			System.exit(1);
		}
		
		String username=null;
		String password=null;
		
		if (args.length>=5)
		{
			username=args[4];
			password="";
		}

		if (args.length==6)
		{
			password=args[5];
		}
		
		
		DownloadSVNCommits downloadSVNCommits=new DownloadSVNCommits(
				args[0],
				args[1],
				args[2],
				args[3],
				username,
				password,
				monitor);
		
		downloadSVNCommits.initializeRepository();
		downloadSVNCommits.downloadSVNCommits();
		if(monitor.isCanceled())
			return;
		
		System.out.println("The downloaded commits has been saved in the folder: "+args[3]);
	}
}
