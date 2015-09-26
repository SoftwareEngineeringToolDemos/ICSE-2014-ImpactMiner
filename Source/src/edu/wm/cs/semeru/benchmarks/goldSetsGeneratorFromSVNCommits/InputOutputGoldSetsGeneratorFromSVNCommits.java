package edu.wm.cs.semeru.benchmarks.goldSetsGeneratorFromSVNCommits;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class InputOutputGoldSetsGeneratorFromSVNCommits
{
	public static final String LINE_ENDING="\r\n";

	public static final String FOLDER_NAME_GOLD_SETS_FROM_SVN_COMMITS="GoldSetsFromSVNCommits/"; 
	public static final String FOLDER_NAME_GOLD_SETS_FROM_SVN_COMMITS_DEBUG="GoldSetsFromSVNCommitsDebug/"; 
	public static final String EXTENSION_GOLD_SET_FROM_SVN_COMMITS=".goldSetSVNCommit"; 
	public static final String EXTENSION_GOLD_SET_FROM_SVN_COMMITS_DEBUG=".goldSetSVNCommitDebug"; 
	public static final String CACHE_FILE_GOLD_SETS="cacheOfGoldSets.txt";
	
	private String fileNameListOfSVNCommits;
	private String folderNameListOfFiles;
	private String folderNameListOfFilesSideBySide;
	private String outputFolder;
	private ArrayList<String> listOfSVNCommits;
	
	private BufferedWriter outputFileGoldSetFromSVNCommit;
	private BufferedWriter outputFileGoldSetFromSVNCommitDebug;
	
	public InputOutputGoldSetsGeneratorFromSVNCommits(String fileNameListOfSVNCommits,String folderNameListOfFiles,String folderNameListOfFilesSideBySide,String outputFolder) throws Exception
	{
		this.fileNameListOfSVNCommits=fileNameListOfSVNCommits;
		this.folderNameListOfFiles=folderNameListOfFiles;
		this.folderNameListOfFilesSideBySide=folderNameListOfFilesSideBySide;
		this.outputFolder=outputFolder;
		this.listOfSVNCommits=loadListOfSVNCommits();
	}
	
	public String getFileNameofCache()
	{
		return outputFolder+CACHE_FILE_GOLD_SETS;
	}

	private ArrayList<String> loadListOfSVNCommits() throws Exception
	{
		BufferedReader br=new BufferedReader(new FileReader(fileNameListOfSVNCommits));

		ArrayList<String> listOfSVNCommits=new ArrayList<String>();
		String buf;
		while ((buf=br.readLine())!=null)
		{
			listOfSVNCommits.add(buf);
		}
		br.close();
		return listOfSVNCommits;
	}

	public ArrayList<String> getListOfSVNCommits()
	{
		return listOfSVNCommits;
	}
	
	public ArrayList<String> getListOfFiles(String svnCommit) throws Exception
	{
		ArrayList<String> listOfFiles=new ArrayList<String>();

		BufferedReader br=new BufferedReader(new FileReader(folderNameListOfFiles+svnCommit+".SVNListOfFiles"));
		
		String fileName;
		while ((fileName=br.readLine())!=null)
		{
			listOfFiles.add(fileName);
		}
		br.close();
		return listOfFiles;
	}
	
	public String getCurrentVersionForFileNameForCommit(String svnCommit,String fileName)
	{
		return folderNameListOfFilesSideBySide+svnCommit+"/"+fileName+".v"+svnCommit;
	}
	
	public String getPreviousVersionFileNameForCommit(String svnCommit,String fileName)
	{
		return folderNameListOfFilesSideBySide+svnCommit+"/"+fileName+".vPrevious";
	}
	
	private String getFileNameGoldSetSVNCommit(String svnCommit)
	{
		return outputFolder+FOLDER_NAME_GOLD_SETS_FROM_SVN_COMMITS+svnCommit+EXTENSION_GOLD_SET_FROM_SVN_COMMITS;
	}

	private String getFileNameGoldSetSVNCommitDebug(String svnCommit)
	{
		return outputFolder+FOLDER_NAME_GOLD_SETS_FROM_SVN_COMMITS_DEBUG+svnCommit+EXTENSION_GOLD_SET_FROM_SVN_COMMITS_DEBUG;
	}
	
	public void initializeFolderStructure() throws Exception
	{
		createFolder(outputFolder);
		createFolder(outputFolder+FOLDER_NAME_GOLD_SETS_FROM_SVN_COMMITS);
		createFolder(outputFolder+FOLDER_NAME_GOLD_SETS_FROM_SVN_COMMITS_DEBUG);
	}
	
	private void createFolder(String folderName) throws Exception
	{
		File folder=new File(folderName);
		if (folder.exists())
			return;
		
		if (folder.mkdir()==false)
			throw new Exception();
	}
	
	public void initializeGoldSetFilesStream(String svnCommit) throws Exception
	{
		outputFileGoldSetFromSVNCommit=new BufferedWriter(new FileWriter(getFileNameGoldSetSVNCommit(svnCommit)));
		outputFileGoldSetFromSVNCommitDebug=new BufferedWriter(new FileWriter(getFileNameGoldSetSVNCommitDebug(svnCommit)));
	}

	public void closeGoldSetFilesStream() throws Exception
	{
		outputFileGoldSetFromSVNCommit.close();
		outputFileGoldSetFromSVNCommitDebug.close();
	}
	
	public static void appendToFile(BufferedWriter outputFile,String buf) throws Exception
	{
		outputFile.write(buf+LINE_ENDING);
	}
	
	public void appendToGoldSetFile(String goldSetMethod) throws Exception
	{
		appendToFile(outputFileGoldSetFromSVNCommit,goldSetMethod);
	}

	public void appendToGoldSetFileDebug(String debugInformation) throws Exception
	{
		appendToFile(outputFileGoldSetFromSVNCommitDebug,debugInformation);
	}
	
	public static String readFile(String fileName)
	{
		BufferedReader br;
		try
		{
			br=new BufferedReader(new FileReader(fileName));
			StringBuilder fileContent=new StringBuilder();
			String buf;
			while ((buf=br.readLine())!=null)
			{
				fileContent.append(buf+LINE_ENDING);
			}
			br.close();
		
			return fileContent.toString();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String[] getCacheFile() throws Exception
	{
		String filename = getFileNameofCache();
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
		File file = new File(getFileNameofCache());
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
