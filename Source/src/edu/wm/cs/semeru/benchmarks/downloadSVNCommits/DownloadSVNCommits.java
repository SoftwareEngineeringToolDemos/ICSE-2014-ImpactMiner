package edu.wm.cs.semeru.benchmarks.downloadSVNCommits;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class DownloadSVNCommits
{
	private String url;
	private long startRevision;
	private long endRevision;
	private String username;
	private String password;
	private SVNRepository repository;
	private InputOutputDownloadSVNCommits inputOutput;
	private CacheMechanism cache;
	public IProgressMonitor monitor;
			
	DownloadSVNCommits(String url,String startRevision,String endRevision,String outputFolder,String username,String password,IProgressMonitor mon)
	{
		this.url=url;
		this.startRevision=Long.parseLong(startRevision);
		this.endRevision=Long.parseLong(endRevision);
		this.inputOutput=new InputOutputDownloadSVNCommits(outputFolder);
		this.username=username;
		this.password=password;
		this.monitor=mon;
		this.repository=null;
	}

	void initializeRepository() throws Exception
	{
		DAVRepositoryFactory.setup();

		SVNURL svnURL=SVNURL.parseURIDecoded(url);
		repository=SVNRepositoryFactory.create(svnURL);
		
		if (username!=null)
		{
			ISVNAuthenticationManager authenticationManager=SVNWCUtil.createDefaultAuthenticationManager(username,password);
			repository.setAuthenticationManager(authenticationManager);
		}
	}

	void downloadSVNCommits() throws Exception
	{
		inputOutput.initializeFolderStructure();
		inputOutput.clearListOfSVNCommits();
		
		System.out.println("Loading cache of downloading commits");
		cache = new CacheMechanism(inputOutput.getCacheFile());
		long[] downloadRevision = cache.downloadInterval(startRevision, endRevision);
		long current = startRevision;
		
		for(int i=0;i+1<downloadRevision.length;i+=2)
		{
			Collection<SVNLogEntry> logEntries=null;
			
			monitor.worked((int) (downloadRevision[i]-current));

			logEntries=repository.log(new String[]{""},null,downloadRevision[i],downloadRevision[i+1],true,true);
			
			for (Iterator<SVNLogEntry> logEntriesIterator=logEntries.iterator();logEntriesIterator.hasNext();)
			{
				if(monitor.isCanceled())
					return;
				SVNLogEntry svnLogEntry=logEntriesIterator.next();
				try{
					parseSVNLogEntry(svnLogEntry);
					Thread.sleep(1000);
				}catch(Exception e){
					System.err.println("Error ocurred when downloading commit "+svnLogEntry.getRevision());
				}
				monitor.worked(1);
			}
			//monitor.worked((int) (downloadRevision[i+1]-downloadRevision[i]));
			current = downloadRevision[i+1];
		}
		monitor.worked((int) (endRevision-current));
		
		cache.ChangeCacheInterval(downloadRevision);
		inputOutput.saveCacheFile(cache.getCacheInterval());
		System.out.println("The cache of downloading commits has been modified");
	}

	private void parseSVNLogEntry(SVNLogEntry svnLogEntry) throws Exception
	{
		String debugInformation=inputOutput.SVNLogEntryToStringDebug(svnLogEntry);
		System.out.println(debugInformation);
		
		inputOutput.saveSVNComments(svnLogEntry);
		
		String listOfFiles="";
		String svnLogEntryPathDebug="";

//		inputOutput.clearSVNListOfFiles(svnLogEntry);
		inputOutput.createRevisionFolderInFolderSideBySideFiles(svnLogEntry);
		
		Map<String,SVNLogEntryPath> changedPaths=svnLogEntry.getChangedPaths();
		ArrayList<SVNLogEntryPath> listSVNLogEntryPaths=new ArrayList<SVNLogEntryPath>(changedPaths.values());

		boolean hasValidFiles=false;
		System.out.println("Changed paths:");
		for (SVNLogEntryPath svnLogEntryPath : listSVNLogEntryPaths)
		{
			svnLogEntryPathDebug=inputOutput.SVNLogEntryPathToStringDebug(svnLogEntryPath);
			debugInformation+=svnLogEntryPathDebug+inputOutput.LINE_ENDING;
			System.out.println(svnLogEntryPathDebug);
			
//			//had to remove this because for ArgoUML the kind is unknown
//			if (svnLogEntryPath.getKind()!=SVNNodeKind.FILE)
//			{
//				System.err.println("Is not a file...");
//				continue;
//			}
			
			char fileType=svnLogEntryPath.getType();
			String fileNameOnRepository=svnLogEntryPath.getPath();
			long revision=svnLogEntry.getRevision();

			if (inputOutput.hasValidFileExtension(fileNameOnRepository)==false)
			{
				System.err.println("Invalid extension...");
				continue;
			}
			if (fileType==SVNLogEntryPath.TYPE_ADDED)
			{
				listOfFiles+=inputOutput.SVNLogEntryPathToString(svnLogEntryPath)+inputOutput.LINE_ENDING;
				hasValidFiles=true;

				//in revision 38
				//   A /trunk/FileBRenamed.java (from /branches/FileB.java:36)
				//the following files will be saved to disk
				// - trunk_FileBRenamed.java.r38 //current file
				// - trunk_FileBRenamed.java.rPrevious //previous file (if exists) which is actually the file /branches/FileB.java from revision36
				
				System.out.println("File save to currentVersion "+inputOutput.getFileNameCurrentVersion(fileNameOnRepository,revision));
				saveFileFromRepository(fileNameOnRepository,revision,inputOutput.getFileNameCurrentVersion(fileNameOnRepository,revision));
				
				String fileNameOnRepositoryFrom=svnLogEntryPath.getCopyPath();
				long revisionFrom=svnLogEntryPath.getCopyRevision();
				if (fileNameOnRepositoryFrom!=null)
				{
					System.out.println("File save to previousVersion "+inputOutput.getFileNamePreviousVersion(fileNameOnRepository,revision));
					//save file "from" under new name
					saveFileFromRepository(fileNameOnRepositoryFrom,revisionFrom,inputOutput.getFileNamePreviousVersion(fileNameOnRepository,revision));
				}
				
				continue;
			}

			if (fileType==SVNLogEntryPath.TYPE_MODIFIED)
			{
				listOfFiles+=inputOutput.SVNLogEntryPathToString(svnLogEntryPath)+inputOutput.LINE_ENDING;
				hasValidFiles=true;

				System.out.println("File save to currentVersion "+inputOutput.getFileNameCurrentVersion(fileNameOnRepository,revision));
				System.out.println("File save to previousVersion "+inputOutput.getFileNamePreviousVersion(fileNameOnRepository,revision));
				saveFileFromRepository(fileNameOnRepository,revision,inputOutput.getFileNameCurrentVersion(fileNameOnRepository,revision));
				saveFileFromRepository(fileNameOnRepository,revision-1,inputOutput.getFileNamePreviousVersion(fileNameOnRepository,revision));
				
				continue;
			}
			
			if (fileType==SVNLogEntryPath.TYPE_REPLACED)
			{
				listOfFiles+=inputOutput.SVNLogEntryPathToString(svnLogEntryPath)+inputOutput.LINE_ENDING;
				hasValidFiles=true;

//				if there is a text changed between previous version "from" and current version, then we should download the previous one as well. Otherwise, we don't need to
				
				System.err.println("FILE REPLACED");
				System.out.println("File save to currentVersion "+inputOutput.getFileNameCurrentVersion(fileNameOnRepository,revision));
				saveFileFromRepository(fileNameOnRepository,revision,inputOutput.getFileNameCurrentVersion(fileNameOnRepository,revision));
				
				String fileNameOnRepositoryFrom=svnLogEntryPath.getCopyPath();
				long revisionFrom=svnLogEntryPath.getCopyRevision();
				if (fileNameOnRepositoryFrom!=null)
				{
					System.out.println("File save to previousVersion "+inputOutput.getFileNamePreviousVersion(fileNameOnRepository,revision));
					//save file "from" under new name
					saveFileFromRepository(fileNameOnRepositoryFrom,revisionFrom,inputOutput.getFileNamePreviousVersion(fileNameOnRepository,revision));
				}
				continue;
			}
			
			if (fileType==SVNLogEntryPath.TYPE_DELETED)
			{
				System.err.println("FILE DELETED");
				continue;
			}

			//this should never be thrown 
			throw new Exception();
		}
		
		inputOutput.saveListOfFiles(svnLogEntry,listOfFiles);
		inputOutput.saveSVNDebugInformation(svnLogEntry,debugInformation);

		inputOutput.appendToListOfSVNCommitsDebug(svnLogEntry);
		if (hasValidFiles)
		{
			inputOutput.appendToListOfSVNCommits(svnLogEntry);
		}
	}

	private void saveFileFromRepository(String fileNameOnRepository,long revision,String fileNameOnDisk) throws Exception
	{
		SVNProperties fileProperties=new SVNProperties();
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		repository.getFile(fileNameOnRepository,revision,fileProperties,baos);

		FileOutputStream fos=new FileOutputStream(fileNameOnDisk);

		baos.writeTo(fos);
	}
}
