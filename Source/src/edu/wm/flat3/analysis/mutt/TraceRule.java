package edu.wm.flat3.analysis.mutt;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class TraceRule implements ISchedulingRule  {

	@Override
	public boolean contains(ISchedulingRule rule) {
		// TODO Auto-generated method stub
		return rule instanceof TraceRule;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return rule instanceof TraceRule || rule instanceof TraceAndCodeModelRule;
	}

}
