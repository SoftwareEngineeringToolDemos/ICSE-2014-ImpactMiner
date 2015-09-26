package edu.wm.cs.semeru.benchmarks.goldSetsGeneratorFromSVNCommits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.tmatesoft.svn.core.SVNLogEntry;

import edu.wm.cs.semeru.benchmarks.downloadSVNCommits.CacheMechanism;

public class GoldSetGeneratorFromSVNCommits
{
	private InputOutputGoldSetsGeneratorFromSVNCommits inputOutput;
	private CacheMechanism cache;
	public static final String DUMMY_JAVA_FILE_FOR_COMPARISON_WITH_ADDED_CLASSES="DummyJavaFileForComparisonForAddedClasses.java";

	GoldSetGeneratorFromSVNCommits(String fileNameListOfSVNCommits,String folderNameListOfFiles,String folderNameListOfFilesSideBySide,String outputFolder) throws Exception
	{
		this.inputOutput=new InputOutputGoldSetsGeneratorFromSVNCommits(fileNameListOfSVNCommits,folderNameListOfFiles,folderNameListOfFilesSideBySide,outputFolder);
	}
	
	void parseAndSaveMultipleSVNCommits(long startRevision, long endRevision, IProgressMonitor monitor) throws Exception
	{
		System.out.println("Loading cache of Gold sets");
		cache = new CacheMechanism(inputOutput.getCacheFile());
		long[] downloadRevision = cache.downloadInterval(startRevision, endRevision);
		
		for(int i=0;i+1<downloadRevision.length;i+=2)
		{
			for(long j=downloadRevision[i];j<=downloadRevision[i+1];++j)
			{
				if(monitor.isCanceled())
					return;
				String svnCommit = String.valueOf(j);
				inputOutput.initializeFolderStructure();
				inputOutput.initializeGoldSetFilesStream(svnCommit);
				try{
					parseAndSaveSVNCommit(svnCommit,inputOutput);
				}catch(Exception e){
					System.err.println("Error ocurred when generating golden set for commit "+svnCommit);
				}
				inputOutput.closeGoldSetFilesStream();
			}
		}
		
		cache.ChangeCacheInterval(downloadRevision);
		inputOutput.saveCacheFile(cache.getCacheInterval());
		System.out.println("The cache of Gold sets has been modified");
	}

	private void parseAndSaveSVNCommit(String svnCommit,InputOutputGoldSetsGeneratorFromSVNCommits inputOutput) throws Exception
	{
		inputOutput.appendToGoldSetFileDebug("SVN Commit: "+svnCommit);
		
		ArrayList<String> listOfFiles=inputOutput.getListOfFiles(svnCommit);
		
		for (String fileNameAndType:listOfFiles)
		{
			inputOutput.appendToGoldSetFileDebug("File Name and Type: "+fileNameAndType);
			String[] fileNameAndTypeSplitted=fileNameAndType.split("\t");
			if (fileNameAndTypeSplitted[0].length()!=1)
				throw new Exception();
			
			char fileType=fileNameAndTypeSplitted[0].charAt(0);
			String fileName=fileNameAndTypeSplitted[1];
			
			//ignore non-java files
			if (fileName.endsWith(".java")==false)
				continue;
			
			String filePathCurrentVersion=inputOutput.getCurrentVersionForFileNameForCommit(svnCommit,fileName);
			String filePathPreviousVersion=inputOutput.getPreviousVersionFileNameForCommit(svnCommit,fileName);

			switch (fileType)
			{
				case 'M':
					genenerateGoldSet(filePathPreviousVersion,filePathCurrentVersion);
					break;
				case 'A':
					genenerateGoldSet(DUMMY_JAVA_FILE_FOR_COMPARISON_WITH_ADDED_CLASSES,filePathCurrentVersion);
					break;
				case 'R':
					genenerateGoldSet(filePathPreviousVersion,filePathCurrentVersion);
					break;
				default:
					throw new Exception("");
			}
		}
	}
	
	private void genenerateGoldSet(String fileNamePreviousVersion,String fileNameCurrentVersion) throws Exception
	{
		ArrayList<CorpusMethod> listOfCorpusMethodsPreviousVersion=getMethodsFromFile(fileNamePreviousVersion);
		ArrayList<CorpusMethod> listOfCorpusMethodsCurrentVersion=getMethodsFromFile(fileNameCurrentVersion);

		inputOutput.appendToGoldSetFileDebug("ListOfCorpusMethodsPreviousVersion size: "+listOfCorpusMethodsPreviousVersion.size());
		inputOutput.appendToGoldSetFileDebug("ListOfCorpusMethodsCurrentVersion size: "+listOfCorpusMethodsCurrentVersion.size());
		inputOutput.appendToGoldSetFileDebug("Gold set methods: ");
		
		for (CorpusMethod currentCorpusMethod:listOfCorpusMethodsCurrentVersion)
		{
			CorpusMethod correspondingMethodInPreviousVersion=findInList(listOfCorpusMethodsPreviousVersion,currentCorpusMethod);
			if (correspondingMethodInPreviousVersion!=null)
			{
				if (correspondingMethodInPreviousVersion.methodContent.equals(currentCorpusMethod.methodContent)==false)
				{
					inputOutput.appendToGoldSetFile(currentCorpusMethod.methodID);
					inputOutput.appendToGoldSetFileDebug(currentCorpusMethod.methodID);
				}
			}
			else
			{
				inputOutput.appendToGoldSetFile(currentCorpusMethod.methodID);
				inputOutput.appendToGoldSetFileDebug(currentCorpusMethod.methodID);
			}
		}
		
		inputOutput.appendToGoldSetFileDebug("\n##########################################################\n");
	}
	
	private ArrayList<CorpusMethod> getMethodsFromFile(String fileName) throws Exception
	{
		inputOutput.appendToGoldSetFileDebug("Reading file: "+fileName);
		if (fileName.equals(DUMMY_JAVA_FILE_FOR_COMPARISON_WITH_ADDED_CLASSES))
			return new ArrayList<CorpusMethod>();
		
		String fileContent=InputOutputGoldSetsGeneratorFromSVNCommits.readFile(fileName);

		ParserGoldSets parser=new ParserGoldSets(inputOutput,fileContent);
		CompilationUnit compilationUnitSourceCode=parser.parseSourceCode();
		ArrayList<CorpusMethod> listOfCorpusMethods=parser.exploreSourceCodeAndIgnoreComments(compilationUnitSourceCode);

		inputOutput.appendToGoldSetFileDebug("List of methods:");
		for (CorpusMethod corpusMethod : listOfCorpusMethods)
		{
			inputOutput.appendToGoldSetFileDebug(corpusMethod.toString());
		}

		inputOutput.appendToGoldSetFileDebug("-------------------------------------");
		return listOfCorpusMethods;
	}

	private CorpusMethod findInList(ArrayList<CorpusMethod> listOfCorpusMethods,CorpusMethod currentCorpusMethod)
	{
		for (CorpusMethod corpusMethod:listOfCorpusMethods)
		{
			if (corpusMethod.equals(currentCorpusMethod))
				return corpusMethod;
		}
		return null;
	}
}
