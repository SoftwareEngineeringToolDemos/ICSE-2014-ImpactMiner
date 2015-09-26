package edu.wm.flat3;
//comment added by Bogdan
//another comment added by Weilin
// this is a test Shasha
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import edu.wm.flat3.analysis.mutt.TraceAndCodeModelRule;

public class CodeModelRule implements ISchedulingRule  {

	@Override
	public boolean contains(ISchedulingRule rule) {
		// TODO Auto-generated method stub
		return rule instanceof CodeModelRule;
	}

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return rule instanceof CodeModelRule || rule instanceof TraceAndCodeModelRule;
	}

}
