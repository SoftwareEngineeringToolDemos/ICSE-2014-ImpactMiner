package edu.wm.flat3.analysis.impact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MethodRule {
	
	private String LMethod;
	private int times;
	private HashMap<String,Integer> RMethods;
	
	public MethodRule(String method)
	{
		this.LMethod = method;
		this.RMethods = new HashMap<String,Integer>();
		times = 1;
	}
	
	public void addTimes()
	{
		times++;
	}
	
	public void addRMethod(String rmethod)
	{
		this.RMethods.put(rmethod, 1);
	}
	
	public boolean findRMethod(String rmethod)
	{
		if(RMethods.containsKey(rmethod))
		{
			RMethods.put(rmethod, RMethods.get(rmethod).intValue()+1);
			return true;
		}
		return false;
	}
	
	public int getTimes()
	{
		return times;
	}
	
	public HashMap<String,Integer> getRMethods()
	{
		return RMethods;
	}
	
	public String[] getRMethodsTostring()
	{
		String[] rmethods = new String[this.RMethods.size()*2];
		int i = 0;
		for(Map.Entry<String, Integer> entry:RMethods.entrySet())
		{
			rmethods[i++] = entry.getKey();
			rmethods[i++] = entry.getValue().toString();
		}
		return rmethods;
	}
	
	public String getLMethod()
	{
		return this.LMethod;
	}
}
