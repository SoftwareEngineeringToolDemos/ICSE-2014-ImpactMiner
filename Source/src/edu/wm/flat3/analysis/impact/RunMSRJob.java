package edu.wm.flat3.analysis.impact;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

import edu.wm.flat3.analysis.mutt.TraceRule;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class RunMSRJob extends Job {

	public RunMSRJob(){
		super("Querying SVN Repository");
		this.setPriority(Job.LONG);
		this.setRule(new TraceRule());
	}
	@Override
	public IStatus run(IProgressMonitor monitor) {
		//monitor.beginTask(getName(), 100);
		MSR.queryRepo(monitor);
		monitor.done();
		return Status.OK_STATUS;
	}

}
