package edu.wm.flat3.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.progress.UIJob;

import edu.wm.flat3.FLATTT;

public class InfoBox extends UIJob {
	
	private String message;

	 public InfoBox(String message) {
		 super("Displaying FLAT3 Info Box");
		 this.message = message;
	 }
	 
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {
		MessageBox box = new MessageBox(FLATTT.singleton().getWorkbench().getDisplay().getActiveShell(),
				SWT.ICON_INFORMATION);	
		box.setMessage(message);
		box.open();
		   return Status.OK_STATUS;
	}
	
	public static void display(String message) {
		InfoBox box = new InfoBox(message);
		box.schedule();
	}
}