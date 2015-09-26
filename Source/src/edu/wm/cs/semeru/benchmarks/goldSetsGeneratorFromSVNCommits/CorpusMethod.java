package edu.wm.cs.semeru.benchmarks.goldSetsGeneratorFromSVNCommits;

public class CorpusMethod
{
	String methodID;
	String methodContent;

	public CorpusMethod(String methodID,String methodContent)
	{
		this.methodID=methodID;
		this.methodContent=methodContent;
	}

	public boolean equals(Object corpusMethod)
	{
		CorpusMethod otherCorpusMethod=(CorpusMethod)corpusMethod;
		if (this.methodID.equals(otherCorpusMethod.methodID))
			return true;
		return false;
	}

	public String toString()
	{
		return methodID+"\r\n"+methodContent+"\r\n";
	}
}