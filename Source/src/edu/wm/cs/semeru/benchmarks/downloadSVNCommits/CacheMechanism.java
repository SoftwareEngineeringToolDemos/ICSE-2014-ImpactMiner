package edu.wm.cs.semeru.benchmarks.downloadSVNCommits;

import java.util.ArrayList;

public class CacheMechanism {
	
	private long[] cacheInterval;
	
	public CacheMechanism(String[] cachefile)
	{
		if(cachefile==null)
		{
			cacheInterval = null;
		}
		else
		{
			cacheInterval = new long[cachefile.length];
			for(int i=0;i<cachefile.length;++i){
				cacheInterval[i]=Long.valueOf(cachefile[i]);
			}
		}
	}
	
	public long[] getCacheInterval()
	{
		return this.cacheInterval;
	}
	
	public long[] downloadInterval(long start,long end)
	{
		long[] downloadinterval;
		ArrayList<Long> temp=new ArrayList<Long>();
		int i;
		// initialize lowerbound and upperbound to 0 can return correct value 
		//even if start or/and end is lesser than the smallest element in array
		if(this.cacheInterval==null)
		{
			downloadinterval = new long[2];
			downloadinterval[0] = start;
			downloadinterval[1] = end;
			return downloadinterval;
		}
		int lowerbound=0;
		int upperbound=0;
		for(i=0; i< cacheInterval.length-1; i++){
			if(cacheInterval[i]<=start&&cacheInterval[i+1]>=start){
				lowerbound = i+1;
			}
			if(cacheInterval[i]<=end&&cacheInterval[i+1]>=end){
				upperbound = i+1;
			}
		}
		// if start or/and end is larger than the greatest value in array 
		if(cacheInterval[i]< start){
			lowerbound = i+1;
		}
		if(cacheInterval[i]< end){
			upperbound = i+1;
		}
		if(lowerbound == upperbound && lowerbound%2==0){ // [start, end] is an interval that has no overlap with existing intervals
			temp.add(start);
			temp.add(end);
		}
		else if(lowerbound != upperbound){
			if(lowerbound%2==0){	// lowerbound is not within existing intervals
				 temp.add(start);
				 temp.add(cacheInterval[lowerbound]);
				 int j = lowerbound+1;
				 for(; j<upperbound-1; j+=2){
					 temp.add(cacheInterval[j]);
					 temp.add(cacheInterval[j+1]);
				 }
				 if(upperbound%2==0){
					 temp.add(cacheInterval[j]);
					 temp.add(end);
				 }
			}
			else{
				int j=lowerbound;
				for(; j<upperbound-1; j+=2){
					 temp.add(cacheInterval[j]);
					 temp.add(cacheInterval[j+1]);
				 }
				 if(upperbound%2==0){
					 temp.add(cacheInterval[j]);
					 temp.add(end);
				 }
			}
		}
		if(temp.size()%2!=0)
			System.err.println("The download interval should be even!");
		downloadinterval = new long[temp.size()];
		for(int k=0; k< temp.size(); k++){
			downloadinterval[k]=temp.get(k);
		}
		return downloadinterval;
	}
	
	public void ChangeCacheInterval(long[] download)
	{
		int cL = 0;
		if(this.cacheInterval!=null)
			cL = this.cacheInterval.length;
		int dL = download.length;
		ArrayList<Long> temp = new ArrayList();
		ArrayList<Long> temp2 = new ArrayList();
		int i=0,j=0,k=0;
		while(i<cL && j<dL)
		{
			if(this.cacheInterval[i]<=download[j])
				temp.add(this.cacheInterval[i++]);
			else
				temp.add(download[j++]);
		}
		while (i<cL)
			temp.add(this.cacheInterval[i++]);
		while(j<dL)
			temp.add(download[j++]);
		
		for(k=0;k<temp.size();++k)
		{
			if(k+1<temp.size())
			{
				if(temp.get(k).longValue()!=temp.get(k+1).longValue())
					temp2.add(temp.get(k));
				else
				{
					if(k%2==0)
						temp2.add(temp.get(k));
					else
						k++;
				}
			}
			else
				temp2.add(temp.get(k));
		}
		if(temp2.size()%2!=0)
			System.err.println("The cache interval should be even!");
		this.cacheInterval = new long[temp2.size()];
		for(k=0;k<temp2.size();++k)
			this.cacheInterval[k] = temp2.get(k);
	}
	
}
