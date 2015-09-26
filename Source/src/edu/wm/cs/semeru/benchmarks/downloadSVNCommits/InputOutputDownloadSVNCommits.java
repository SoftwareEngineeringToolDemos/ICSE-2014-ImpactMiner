package edu.wm.cs.semeru.benchmarks.downloadSVNCommits;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;

public class InputOutputDownloadSVNCommits
{
	public static final String LINE_ENDING="\r\n";
	public static String[] listOfValidExtensions=new String[]{".java",".txt",".xml",".html",".htm"};
	
	public static final String FOLDER_NAME_SVN_FILES_SIDE_BY_SIDE="SVNFilesSideBySide/"; 
	public static final String FOLDER_NAME_SVN_COMMENTS="SVNComments/"; 
	public static final String FOLDER_NAME_SVN_LIST_OF_FILES="SVNListOfFiles/"; 
	public static final String FOLDER_NAME_SVN_DEBUG="SVNDebug/";
	public static final String FILE_NAME_LIST_OF_SVN_COMMITS="listOfSVNCommits.txt";
	public static final String FILE_NAME_LIST_OF_SVN_COMMITS_DEBUG=FOLDER_NAME_SVN_DEBUG+"listOfSVNCommitsDebug.txt";
	public static final String FILE_NAME_CACHE_OF_SVN_COMMITS="cacheOfSVNCommits.txt";
	
	private String outputFolder;

	public InputOutputDownloadSVNCommits(String outputFolder)
	{
		this.outputFolder=outputFolder;
	}

	public String getFolderNameSVNFilesSideBySide()
	{
		return outputFolder+FOLDER_NAME_SVN_FILES_SIDE_BY_SIDE;
	}

	public String getFolderNameSVNComments()
	{
		return outputFolder+FOLDER_NAME_SVN_COMMENTS;
	}

	public String getFolderNameSVNListOfFiles()
	{
		return outputFolder+FOLDER_NAME_SVN_LIST_OF_FILES;
	}

	public String getFolderNameSVNDebug()
	{
		return outputFolder+FOLDER_NAME_SVN_DEBUG;
	}
	
	public String getFileNameListOfSVNCommits()
	{
		return outputFolder+FILE_NAME_LIST_OF_SVN_COMMITS;
	}

	public String getFileNameListOfSVNCommitsDebug()
	{
		return outputFolder+FILE_NAME_LIST_OF_SVN_COMMITS_DEBUG;
	}
	
	public String getFileNameCacheOfSVNCommits()
	{
		return outputFolder+FILE_NAME_CACHE_OF_SVN_COMMITS;
	}

	public void initializeFolderStructure() throws Exception
	{
		createFolder(outputFolder);
		createFolder(getFolderNameSVNFilesSideBySide());
		createFolder(getFolderNameSVNComments());
		createFolder(getFolderNameSVNListOfFiles());
		createFolder(getFolderNameSVNDebug());
	}
	
	private void createFolder(String folderName) throws Exception
	{
		File folder=new File(folderName);
		if (folder.exists())
			return;
		
		if (folder.mkdir()==false)
			throw new Exception();
	}
	
	public void clearListOfSVNCommits() throws Exception
	{
		BufferedWriter outputFile=new BufferedWriter(new FileWriter(getFileNameListOfSVNCommits()));
		outputFile.write("");
		outputFile.close();
		
		outputFile=new BufferedWriter(new FileWriter(getFileNameListOfSVNCommitsDebug()));
		outputFile.write("");
		outputFile.close();
	}
	
	void saveFile(String outputFileName,String content) throws Exception
	{
		BufferedWriter outputFile=new BufferedWriter(new FileWriter(outputFileName));
		outputFile.write(content+LINE_ENDING);
		outputFile.close();
	}
	
	void saveFileWithoutLineEnding(String outputFileName,String content) throws Exception
	{
		BufferedWriter outputFile=new BufferedWriter(new FileWriter(outputFileName));
		outputFile.write(content);
		outputFile.close();
	}
	
	public void appendToFile(String outputFileName,String content) throws Exception
	{
		BufferedWriter outputFile=new BufferedWriter(new FileWriter(outputFileName,true));
		outputFile.write(content+LINE_ENDING);
		outputFile.close();
	}
	
	public void saveSVNComments(SVNLogEntry svnLogEntry) throws Exception
	{
		String filename = getFolderNameSVNComments()+svnLogEntry.getRevision()+".SVNComment";
		File file = new File(filename);
		if(file.exists())
			return;
		saveFile(filename,svnLogEntry.getMessage());
	}
	
	public void saveListOfFiles(SVNLogEntry svnLogEntry,String listOfFiles) throws Exception
	{
		String filename = getFolderNameSVNListOfFiles()+svnLogEntry.getRevision()+".SVNListOfFiles";
		File file = new File(filename);
		if(file.exists())
			return;
		saveFileWithoutLineEnding(filename,listOfFiles);
	}
	
	public void saveSVNDebugInformation(SVNLogEntry svnLogEntry,String debugInformation) throws Exception
	{
		String filename = getFolderNameSVNDebug()+svnLogEntry.getRevision()+".SVNDebug";
		File file = new File(filename);
		if(file.exists())
			return;
		saveFile(filename,debugInformation);
	}
	
	public void createRevisionFolderInFolderSideBySideFiles(SVNLogEntry svnLogEntry) throws Exception
	{
		createFolder(getFolderNameSVNFilesSideBySide()+svnLogEntry.getRevision()+"/");
	}
	
	public String SVNLogEntryPathToString(SVNLogEntryPath svnLogEntryPath)
	{
		return svnLogEntryPath.getType()+"\t"+getFileNameOnDisk(svnLogEntryPath.getPath());
	}

	public String SVNLogEntryToStringDebug(SVNLogEntry svnLogEntry)
	{
		StringBuilder buf=new StringBuilder();
		buf.append("Revision: "+svnLogEntry.getRevision()+LINE_ENDING);
		buf.append("Author: "+svnLogEntry.getAuthor()+LINE_ENDING);
		buf.append("Date: "+svnLogEntry.getDate()+LINE_ENDING);
		buf.append("Log:"+LINE_ENDING);
		buf.append(svnLogEntry.getMessage().replace("\n"," ").replace("\r"," ")+LINE_ENDING+LINE_ENDING);

		buf.append("List of files:"+LINE_ENDING);
		
		return buf.toString();
	}
	
	public String SVNLogEntryPathToStringDebug(SVNLogEntryPath svnLogEntryPath)
	{
		StringBuilder buf=new StringBuilder();
		buf.append(" "+svnLogEntryPath.getType()+" "+svnLogEntryPath.getPath());
		if (svnLogEntryPath.getCopyPath()!=null)
			buf.append(" (from "+svnLogEntryPath.getCopyPath()+":"+svnLogEntryPath.getCopyRevision()+")");
		
		buf.append(" - ("+svnLogEntryPath.getKind()+")");
		return buf.toString();
	}

	String getFileNameOnDisk(String fileNameOnRepository)
	{
		return fileNameOnRepository.substring(1).replace("/","_");
	}
	
	public boolean hasValidFileExtension(String fileNameOnRepository)
	{
		for (String extension : listOfValidExtensions)
		{
			if (fileNameOnRepository.endsWith(extension))
				return true;
		}
		return false;
	}
	
	public String getFileNameCurrentVersion(String fileNameOnRepository,long revision)
	{
		return getFolderNameSVNFilesSideBySide()+revision+"/"+getFileNameOnDisk(fileNameOnRepository)+".v"+revision;
		
	}

	public String getFileNamePreviousVersion(String fileNameOnRepository,long revision)
	{
		return getFolderNameSVNFilesSideBySide()+revision+"/"+getFileNameOnDisk(fileNameOnRepository)+".vPrevious";
	}
	
	public void appendToListOfSVNCommits(SVNLogEntry svnLogEntry) throws Exception
	{
		appendToFile(getFileNameListOfSVNCommits(),svnLogEntry.getRevision()+"");
	}
	
	public void appendToListOfSVNCommitsDebug(SVNLogEntry svnLogEntry) throws Exception
	{
		appendToFile(getFileNameListOfSVNCommitsDebug(),svnLogEntry.getRevision()+"");
	}
	public String[] getCacheFile() throws Exception
	{
		String filename = getFileNameCacheOfSVNCommits();
		File file = new File(filename);
		String line = new String();
		String[] temp = new String[200];
		if(file.exists())
		{
			Scanner scanner = new Scanner(file);
			if(scanner.hasNext()){
				line=scanner.nextLine();
				temp = line.split(" ");				
			}
			else{
				scanner.close();
				return null;
			}
			scanner.close();
		}
		else
		{
			file.createNewFile();
			return null;
		}
		return temp;
	}
	
	public void saveCacheFile(long[] interval) throws Exception
	{
		File file = new File(getFileNameCacheOfSVNCommits());
		//FileOutputStream fout = new FileOutputStream(file);
		FileWriter fwrite = new FileWriter(file,false);
		int i =0;
		for(;i<interval.length-1;++i)
		{
			fwrite.write(String.valueOf(interval[i])+" ");
		}
		fwrite.write(String.valueOf(interval[i]));
		fwrite.close();
	}
}
