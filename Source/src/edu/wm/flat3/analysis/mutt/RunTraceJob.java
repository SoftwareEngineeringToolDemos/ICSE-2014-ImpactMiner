package edu.wm.flat3.analysis.mutt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import edu.wm.flat3.CodeModelRule;
import edu.wm.flat3.FLATTT;
import edu.wm.flat3.ui.InfoBox;

public class RunTraceJob extends Job {
		
		private String className;
		private String[] arguments;
		private String directory;
		
		public RunTraceJob(String directory, String className, String[] arguments) {
			// TODO, tracing: handle a job cancel properly
			// TODO, tracing: autostop tracing if eclipse is closed??
			super("Tracing "+className+" with MUTT");
			this.directory = directory;
			this.className = className;
			this.arguments = arguments;
			
			this.setPriority(Job.SHORT);
			this.setRule(new TraceAndCodeModelRule());
		}

		public IStatus run(IProgressMonitor monitor) {
			// no better way to do this? gotta get the path to the bin directory or to our jar
			// so we can launch our MUTT tracer
			// TODO: double check this when we package plugin for distribution and/or in a jar
			String bundlePaths = "";
			try {
				bundlePaths = FLATTT.singleton().getInstallLocation();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		
			// Classpath delimiter varies by platform, along with other things
			String classpathDelimiter = "";
			String fileDelim = "";
			String extraEscapeCharacter = "";
			String pathQuote = "";
			if (System.getProperty("os.name").startsWith("Windows")) {
				classpathDelimiter = ";";
				
				//bundlePaths = bundlePaths.replaceFirst("^reference:file:","");
				bundlePaths = bundlePaths.replaceFirst("^/","");
				bundlePaths = bundlePaths.replace("/", "\\");
				bundlePaths = bundlePaths.replaceFirst("\\\\$","");
				fileDelim = "\\";
				
				pathQuote = "\""; // windows also requires paths to be quoted in case they have spaces in them. linux, on the other hand, tries to put those quotes in the path
				extraEscapeCharacter = "\\"; // windows needs an extra \ to keep the last \ at the end of the path from escaping the " after it into nothingness
			}
			 else{ // Unix-like
				 //TODO, test: do paths with spaces work on linux/etc?
				classpathDelimiter = ":";
				fileDelim = "/";
			 }
			 // other possibilities?
			
			// toolsAutomatic seems to be the canonical way to locate tools.jar
			String toolsAutomatic = new Path(System.getProperty("java.home")).append("lib").append("tools.jar").toOSString();
			String toolsPlugin = FLATTT.singleton().getStateLocation().append("tools.jar").toOSString();
			
			// so, check possible locations for tools, including somewhere special for the plugin
			if (!(new File(toolsAutomatic).exists() || new File(toolsPlugin).exists())) {
				// if none found, display dialog: issue. fixes: install jdk, switch to the jdk, or copy tools.jar to one of these locations
				InfoBox box = new InfoBox("Could not locate tools.jar, which is needed to trace programs and which comes with the JDK (not the JRE). Locations tried:\n"+toolsAutomatic+"\n"+toolsPlugin+"\n\nPlease switch to a JDK runtime or move a copy of your tools.jar to "+toolsPlugin);
				box.schedule();
				return Status.CANCEL_STATUS;
			}
			
			// Class path for tools.jar and for the MUTT classes
			// All directory paths are quoted in case thre's a space in them. Also, note, a spacebefore the last quote - so the \ doesn't escape it away
			String muttClassDirectory = toolsAutomatic+classpathDelimiter+toolsPlugin+classpathDelimiter+bundlePaths+fileDelim+"bin"+classpathDelimiter+bundlePaths; 
			String traceArgs[] = {"java",
					"-classpath", pathQuote + muttClassDirectory + pathQuote, // quotes in case there are spaces in class path
					// MUTT's class
					"com.sun.tools.example.trace.Trace",
					// dot directory
					"-dotdir",pathQuote+FLATTT.singleton().getTraceLocation().addTrailingSeparator().toOSString()+extraEscapeCharacter+pathQuote,
					// flag for the output file. we don't need this, we only use .dot files.
					//"-output", FLATTT.singleton().getTraceLocation().append("output").toOSString(),
					
					className};
			
			// Append arguments
			String args[] = new String[traceArgs.length+arguments.length];
			for (int i = 0; i < traceArgs.length; i++) args[i] = traceArgs[i];
			for (int i = traceArgs.length; i < (traceArgs.length+arguments.length); i++) args[i] = arguments[i-traceArgs.length];

			// Make sure we have a directory to store the traces in
			if (!FLATTT.singleton().getTraceLocation().toFile().exists())
				FLATTT.singleton().getTraceLocation().toFile().mkdirs();
			
			// Delete all .dot files
			for ( File dot : FLATTT.singleton().getTraceLocation().toFile().listFiles())
				dot.delete();
			
			// Print out the command we'll execute for debugging
			String command = "";
			for (int i = 0; i < args.length; i++)
				command += " " +args[i];
			System.out.println(command);
			//TODO:Critical section here.  Run the code on a command line program.
			//Modify to produce the command trace, not just the unique methods called
			//become familiar with command trace.
			// Call MUTT
			try {
				// Start MUTT
				FLATTT.muttProcess = Runtime.getRuntime().exec(args, null, new File(directory));
				FLATTT.concernView.startTraceAction.updateIcon();	//changes the icon to a stop sign
				
				// Handle the process's output; otherwise it'll hang!
				new streamHandler(FLATTT.muttProcess.getInputStream()).start();
				new streamHandler(FLATTT.muttProcess.getErrorStream()).start();
				
				// Wait for MUTT to finish
				FLATTT.muttProcess.waitFor();
				FLATTT.muttProcess = null;
				FLATTT.concernView.startTraceAction.updateIcon();	//changes icon back to normal
				return Status.OK_STATUS;
				 
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
           return Status.OK_STATUS;
        }	//end run() function
		
		
		class streamHandler extends Thread {
			private InputStream stream;
			
			public streamHandler(InputStream inputStream) {
				stream = inputStream;
			}

			public void run () {
				BufferedReader streamReader  = new BufferedReader(new InputStreamReader(stream));  
				  
				try {
					for(String line;(line=streamReader.readLine())!=null;)  
					{  
						// Just ignore it unless we're debugging MUTT
					     //System.out.println(line+"\n");  
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		}
	}
	