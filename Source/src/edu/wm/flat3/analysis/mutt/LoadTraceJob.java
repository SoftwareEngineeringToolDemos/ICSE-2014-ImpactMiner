package edu.wm.flat3.analysis.mutt;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

public class LoadTraceJob extends UIJob {
	String query;
	public LoadTraceJob() {
		super("Loading MUTT trace");
		this.setPriority(Job.SHORT);
		this.setRule(new TraceRule());
	}

	public IStatus runInUIThread(IProgressMonitor monitor) {
   				MUTTTrace.loadTraceData();
	           return Status.OK_STATUS;
	        }
}