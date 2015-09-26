package edu.wm.flat3.analysis.mutt;

import org.eclipse.core.runtime.jobs.ISchedulingRule;

import edu.wm.flat3.CodeModelRule;

public class TraceAndCodeModelRule implements ISchedulingRule  {

	@Override
	public boolean contains(ISchedulingRule rule) {
		// TODO Auto-generated method stub
		return rule instanceof TraceAndCodeModelRule;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return rule instanceof TraceAndCodeModelRule || rule instanceof TraceRule || rule instanceof CodeModelRule;
	}

}
