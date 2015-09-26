package edu.wm.flat3.analysis.impact;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.preference.PreferenceStore;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.repository.Component;
import weka.associations.AssociationRules;
import weka.associations.Item;
import weka.associations.FPGrowth;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

//import weka.associations.*;
//import weka.core.*;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class RuleMiner {
	private static final String e_r_seperator = "===";
	/**
	 * This contains the list of all methods which appeared as a premise in any
	 * of the mined rules.  It is a complete list of all methods in the project
	 * from which inferences can be made based on the repository data.
	 * It is the responsibility of the calling code to ensure 
	 * that mineRules() has been called with a valid argument before attempting
	 * to read this Field.
	 */
	protected static ArrayList<FLATTTMember> elementList;
	
	//this is the part of the code that will need to be modified
	public static ArrayList<AssociationRule> mineRules(File ruleListFile){
		ArrayList<AssociationRule> toReturn = new ArrayList<AssociationRule>();
		elementList = new ArrayList<FLATTTMember>();
		try {
			if (ruleListFile.exists()){
				//open the file and read each association rule
				BufferedReader in = new BufferedReader(new FileReader(ruleListFile));
				String line;
				String[] linelist;
				line = in.readLine();
				line = line.trim();
				while (!line.equalsIgnoreCase(e_r_seperator)){
					//System.err.println(line);
					elementList.add(new FLATTTMember((IMember) FLATTT.repository.getComponentWithName(line).getJavaElement() ));
					line = in.readLine();
					line = line.trim();
				}
				line = in.readLine();
				
				//get seed name first
				PreferenceStore miningPrefs = FLATTT.getIAPreferenceStore();
				try {
					miningPrefs.load();
				} catch (IOException e) {
					System.out.println("could not read IA settings from disk, using defaults");
				}
				String seedMethod = miningPrefs.getString("seedMethod");
				
				
				//Add condition, so only related rules are read
				if (miningPrefs.getBoolean("seedAlreadySelected")){
					//boolean flag = false; //true when reach the seedMethod
					while (line != null){
						linelist = line.split(" ");	
				
						if(!linelist[0].equals(seedMethod)){
							line = in.readLine();
							continue;
						}
						else{
							if(!linelist[0].equals(seedMethod))
								break;
							
							toReturn.add(new AssociationRule(
									new FLATTTMember((IMember) FLATTT.repository.getComponentWithName(linelist[0]).getJavaElement()),
									new FLATTTMember((IMember) FLATTT.repository.getComponentWithName(linelist[1]).getJavaElement()),
									Float.parseFloat(linelist[2]), Integer.parseInt(linelist[3])));
							line = in.readLine();	
						}
				    }		
				}
				else{
				
					while (line != null){
						linelist = line.split(" ");	
				
						toReturn.add(new AssociationRule(
							new FLATTTMember((IMember) FLATTT.repository.getComponentWithName(linelist[0]).getJavaElement()),
							new FLATTTMember((IMember) FLATTT.repository.getComponentWithName(linelist[1]).getJavaElement()),
							Float.parseFloat(linelist[2]), Integer.parseInt(linelist[3])));
						line = in.readLine();				
				    }
				}
				SVNsearchAlt.elementList = elementList;
				in.close();
				return toReturn;
			}
		}
		catch (FileNotFoundException e){
			//do nothing, file was not found so continue as if it were not there.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Mines a list of <code>AssociationRule</code>s from the given 
	 * <code>ChangeSet</code>s.
	 * @param changeSets
	 * @return the list of <code>AssociationRule</code>s mined from <code>
	 * changeSets</code>
	 */
	public static ArrayList<AssociationRule> mineRules(ArrayList<ChangeSet> changeSets, String name, long start, long end){
//		FLATTT.debugOutput("Just entered the function");

		PreferenceStore miningPrefs = FLATTT.getIAPreferenceStore();
		try {
			miningPrefs.load();
		} catch (IOException e) {
			System.out.println("could not read IA settings from disk, using defaults");
		}
		String seedName;
		boolean seedSelected = miningPrefs.getBoolean("seedAlreadySelected");
//		try{
//		Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
//        String originalString=(String)clipboard.getData(DataFlavor.stringFlavor);
//        seedName=originalString;
//		}catch(Exception e){
//			seedName = "SettingsDialog.SettingsDialog";
//		}
//		miningPrefs.setValue("seedMethod", seedName);
//		try {
//			miningPrefs.save();
//		} catch (IOException e1) {
//		}
		seedName = miningPrefs.getString("seedMethod");
		
		String dbFileName = name + "_" + "rev(" + start + "-" + end + ")";
		if (seedSelected) dbFileName = dbFileName + seedName + ".arff";
		else dbFileName = dbFileName + ".arff";
		File dbFile = SVNsearchAlt.location.append(dbFileName).toFile();
		
		String rListFileName;
		if (seedSelected){
			rListFileName = name + "_" + "rev(" + start + "-" + end 
				+ ")-c" + miningPrefs.getString("minConf") 
				+ "-s" + miningPrefs.getString("minSup") 
				+ miningPrefs.getString("seedMethod") +".dat";
		}else{
			rListFileName = name + "_" + "rev(" + start + "-" + end 
					+ ")-c" + miningPrefs.getString("minConf") 
					+ "-s" + miningPrefs.getString("minSup") +".dat";
		}
		File ruleListFile = SVNsearchAlt.location.append(rListFileName).toFile();
		
		ArrayList<AssociationRule> toReturn = new ArrayList<AssociationRule>();
		elementList = new ArrayList<FLATTTMember>();
		ArrayList<String> check = new ArrayList<String>();
		
		int maxNumChanges = miningPrefs.getInt("maxSetSize");
		
		// Check to see if we have already done mining for this range of revisions
		// if we have, read from the file we left
		try {
			if (ruleListFile.exists()){
				//open the file and read each association rule
				BufferedReader in = new BufferedReader(new FileReader(ruleListFile));
				String line;
				String[] linelist;
				line = in.readLine();
				line = line.trim();
				while (!line.equalsIgnoreCase(e_r_seperator)){
					//System.err.println(line);
					elementList.add(new FLATTTMember((IMember) FLATTT.repository.getComponentWithName(line).getJavaElement() ));
					line = in.readLine();
					line = line.trim();
				}
				line = in.readLine();
				while (line != null){
					linelist = line.split(" ");
					toReturn.add(new AssociationRule(
							new FLATTTMember((IMember) FLATTT.repository.getComponentWithName(linelist[0]).getJavaElement()),
							new FLATTTMember((IMember) FLATTT.repository.getComponentWithName(linelist[1]).getJavaElement()),
							Float.parseFloat(linelist[2]), Integer.parseInt(linelist[3])));
					line = in.readLine();
				}
				in.close();
				return toReturn;
			}
		}
		catch (FileNotFoundException e){
			//do nothing, file was not found so continue as if it were not there.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//get a list of all methods added or changed over the whole period.
		ArrayList<String> allFoundMethods = new ArrayList<String>();
		for (ChangeSet changeSet : changeSets) {
			if (!seedSelected || changeSet.contains(seedName)){
				for (String string : changeSet) {
					if (!allFoundMethods.contains(string)) allFoundMethods.add(string);
				}
			}
		}
		Collections.sort(allFoundMethods);
		//map each method name to a sequential column number
		//use indexOf instead?
		HashMap<String, Integer> colNumbers = new HashMap<String, Integer>();
		int i = 0;
		for (String string : allFoundMethods) {
			colNumbers.put(string, i++);
		}
		
		
		
		
//		FLATTT.debugOutput("About to create arff file");
		try {
			if (!dbFile.exists()){
				dbFile.createNewFile();
				//location.deleteOnExit();
				PrintStream arff = new PrintStream(dbFile);
				arff.println("@RELATION changeSets");
				arff.println();
				for (String string : allFoundMethods) {
					arff.println("@ATTRIBUTE " + string + " {false, true}");
				}
				arff.println();
				arff.println("@DATA");
				arff.println();
				for (ChangeSet changeSet : changeSets) {
					if (changeSet.size() == 0) continue;
					
					if (changeSet.size() > maxNumChanges) continue;
					if (!seedSelected || changeSet.contains(seedName)){
						Collections.sort(changeSet);
						String line = "{";
						for (String string : changeSet) {
							line = line + allFoundMethods.indexOf(string) + " true, ";
						}
						line = line.substring(0, line.lastIndexOf(','));
						line = line + "}";
						arff.println(line);				
					}
				}
				arff.close();
			}
		// done creating ARFF file
			
		// Now try to read ARFF file.
			DataSource source = new DataSource(SVNsearchAlt.location.append(dbFileName).toOSString());
			Instances data = source.getDataSet();
			
			// setting class attribute if the data format does not provide this information
			// For example, the XRFF format saves the class attribute information as well
//			if (data.classIndex() == -1) data.setClassIndex(data.numAttributes() - 1);
			
//			if (seedSelected){
//				Attribute a = data.attribute(seedName);
//				for (int k = data.numInstances()-1; k >= 0; k--){
//					if (data.instance(k).stringValue(a).equalsIgnoreCase("false"))
//						data.remove(k);
//				}
//
//				ArffSaver saver = new ArffSaver();
//				saver.setInstances(data);
//				saver.setFile(new File(SVNsearchAlt.location.append(name + "_" + "rev(" + start + "-" + end + ")"+seedName).toOSString()+".arff"));
//				saver.writeBatch();
//				if (data.numInstances() == 0) //if no instances, no rules.
//					return new ArrayList<AssociationRule>();
//			}
			FPGrowth fpgrowth = new FPGrowth();
			// -P 2 -I -1 -N 100 -T 0 -C 0.1 -D 0.05 -U 1.0 -M 0.05
			String[] options = { "-N", "100","-C", miningPrefs.getString("minConf"), 
					"-U", miningPrefs.getString("maxSup"), "-M", miningPrefs.getString("minSup")};
//			for (String opt : fpgrowth.getOptions())
//				System.err.println(opt);
			fpgrowth.setOptions(options);
			if (!fpgrowth.canProduceRules()) throw new Exception("Cannot build rules!");
			
			

			fpgrowth.buildAssociations(data);
			AssociationRules rawRules = fpgrowth.getAssociationRules();

			for (weka.associations.AssociationRule rawRule : rawRules.getRules()) {
				float confidence = (float) rawRule.getMetricValuesForRule()[0];
				int support = rawRule.getTotalSupport();
				for(Item premise : rawRule.getPremise()){
//					FLATTT.debugOutput("current premise: " + premise.toString());
					String leftKey = premise.toString().substring(0, premise.toString().indexOf('='));
					Component leftComp = null;
					leftComp = FLATTT.repository.getComponentWithName(leftKey);//--------------------------------------
//					FLATTT.debugOutput("current premise key: " + leftKey);
					if (leftComp == null) continue;
					IMember leftMember = null;
					leftMember = (IMember) leftComp.getJavaElement();
					if (leftMember == null) continue;
					FLATTTMember lhs = new FLATTTMember(leftMember);
					for (Item consequence : rawRule.getConsequence()){
//						FLATTT.debugOutput("current consequence: " + consequence);
						String rightKey = consequence.toString().substring(0, consequence.toString().indexOf('='));
						Component rightComp = null;
						rightComp = FLATTT.repository.getComponentWithName(rightKey);
//						FLATTT.debugOutput("current consequence key: " + rightKey);
						if (rightComp == null) continue;
						IMember rightMember = null;
						rightMember = (IMember) rightComp.getJavaElement();
						if (rightMember == null) continue;
						FLATTTMember rhs = new FLATTTMember (rightMember);
						AssociationRule processedRule = new AssociationRule(lhs, rhs, confidence, support);
						toReturn.add(processedRule);
						if (!check.contains(lhs.getFullName())){
							elementList.add(lhs);
							check.add(lhs.getFullName());
						}
//						if (!check.contains(rhs.getFullName())){
//							elementList.add(rhs);
//							check.add(rhs.getFullName());
//						}
					} // end for each consequence in the rule
				} // end for each premise in the rule
			} //end for each rule
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		FLATTT.debugOutput("End of function, returning normally", System.err);
		
		//write the data we have to file so that we can access it right away next time
		try {
			ruleListFile.createNewFile();
			PrintStream ruleListWriter = new PrintStream(ruleListFile);
			for (FLATTTMember element : elementList) {
				ruleListWriter.println(element.getClassName() + "." + element.getShortName());
			}
			ruleListWriter.println(e_r_seperator);
			for (AssociationRule rule : toReturn) {
				ruleListWriter.println(rule.getLhs().getClassName() + "." + rule.getLhs().getShortName() + " " 
						+ rule.getRhs().getClassName() + "." + rule.getRhs().getShortName() + " " 
						+ rule.getConfidenceValue() + " " 
						+ rule.getSupportValue());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return toReturn;
	}

	public static ArrayList<AssociationRule> mineRules(String name, long start, long end)
	{
		String file = start+"-"+end+".dat";
		File ruleFile = SVNsearchAlt.location.append(name).append("Rules").append(file).toFile();
		ArrayList<AssociationRule> rulesReturn = new ArrayList<AssociationRule>();
		elementList = new ArrayList<FLATTTMember>();
		ArrayList<String> methods = new ArrayList<String>();
		ArrayList<MethodRule> rules = new ArrayList<MethodRule>();
		long i = start;
		for(;i<=end;++i)
		{
			String goldMethodFile = name+"/GoldSetsFromSVNCommits/"+i+".goldSetSVNCommit";
			File goldCommitFile = SVNsearchAlt.location.append(goldMethodFile).toFile();
			try{
				if(goldCommitFile.exists())
				{
					methods.clear();
					String line = new String();
					Scanner scanner = new Scanner(goldCommitFile);
					while(scanner.hasNext()){
						line=scanner.nextLine();
						line=methodNameFilter(line);
						methods.add(line);
					}
					scanner.close();
					int j = 0;
					if(methods.size()>0)
					{
						for(;j<methods.size();++j)
						{
							int k = 0;
							for(;k<rules.size();++k)
							{
								if(rules.get(k).getLMethod().equals(methods.get(j)))
								{
									rules.get(k).addTimes();
									for(int s = 0;s<methods.size();++s)
									{
										if(s!=j)
										{
											if(rules.get(k).findRMethod(methods.get(s)))
												continue;
											else
												rules.get(k).addRMethod(methods.get(s));
										}
									}
									break;
								}
							}
							if(k>=rules.size())
							{
								MethodRule mr = new MethodRule(methods.get(j));
								for(int s = 0;s<methods.size();++s)
								{
									if(s!=j){
										mr.addRMethod(methods.get(s));
									}
								}
								rules.add(mr);
							}
						}
					}
				}
			}
			catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try{
			for(int k =0;k<rules.size();++k)
			{
				//String leftKey = ;
				MethodRule temp = rules.get(k);
				Component leftComp = null;
				leftComp = FLATTT.repository.getComponentWithName(temp.getLMethod());//--------------------------------------
				if (leftComp == null) continue;
				IMember leftMember = null;
				leftMember = (IMember) leftComp.getJavaElement();
				if (leftMember == null) continue;
				FLATTTMember lhs = new FLATTTMember(leftMember);
				boolean flag = false;
				for (Map.Entry<String, Integer> entry : temp.getRMethods().entrySet()){
					String rightKey = entry.getKey();
					Component rightComp = null;
					rightComp = FLATTT.repository.getComponentWithName(rightKey);
					if (rightComp == null) continue;
					IMember rightMember = null;
					rightMember = (IMember) rightComp.getJavaElement();
					if (rightMember == null) continue;
					flag = true;
					FLATTTMember rhs = new FLATTTMember (rightMember);
					float confidence = (float)entry.getValue()/(float)temp.getTimes();
					AssociationRule processedRule = new AssociationRule(lhs, rhs, confidence, entry.getValue());
					rulesReturn.add(processedRule);
				} // end for each consequence in the rule
				if(flag)
				{
					elementList.add(lhs);
					sortRules(rulesReturn);
				}
			}
		}catch(Exception e){
			System.err.println("Rule traslation error");
		}
		
		try{
			String temp = name+"/Rules";
			File ruleFolder = SVNsearchAlt.location.append(temp).toFile();
			if(!ruleFolder.exists())
				ruleFolder.mkdir();
			ruleFile.createNewFile();
			PrintStream ruleListWriter = new PrintStream(ruleFile);
			for (FLATTTMember element : elementList) {
				ruleListWriter.println(element.getClassName() + "." + element.getShortName());
			}
			ruleListWriter.println(e_r_seperator);
			for (AssociationRule rule : rulesReturn) {
				ruleListWriter.println(rule.getLhs().getClassName() + "." + rule.getLhs().getShortName() + " " 
						+ rule.getRhs().getClassName() + "." + rule.getRhs().getShortName() + " " 
						+ rule.getConfidenceValue() + " " 
						+ rule.getSupportValue());
			}
			ruleListWriter.close();
		}catch(Exception e){
			System.err.println("Can not save the rules!");
		}
		return rulesReturn;
	}
	
	public static String methodNameFilter(String fullname)
	{
		String toReturn = new String();
		if(fullname.contains("("))
			fullname = fullname.substring(0, fullname.indexOf('('));
		String[] temp = fullname.split("\\.");
		int i = temp.length;
		toReturn = temp[i-2]+"."+temp[i-1];
		return toReturn;
	}
	
 	public static void sortRules(ArrayList<AssociationRule> list)
 	{
 		Collections.sort(list, new Comparator<AssociationRule>(){
			@Override
			public int compare(AssociationRule arg0, AssociationRule arg1) {
				float con0 = arg0.getConfidenceValue();
				float con1 = arg1.getConfidenceValue();
				int supp0 = arg0.getSupportValue();
				int supp1 = arg1.getSupportValue();
				if (con1 == con0) 
				{
					if(supp0 == supp1) return 0;
					else if(supp1<supp0) return -1;
					else return 1;
				}
				else if (con1<con0) return -1;
				else return 1;
			}
			
		});
 	}
}
