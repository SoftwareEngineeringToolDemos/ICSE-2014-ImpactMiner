package edu.wm.flat3.analysis.impact.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;


import edu.wm.flat3.FLATTT;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class ExportMiningAction extends Action {
	IViewPart view;
	
	public ExportMiningAction(IViewPart view){
		this.view = view;
		this.setEnabled(FLATTT.MSRRun);
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				FLATTT.ID_PLUGIN, "icons/msrExport.png"));
		setText("Export Mining Results");
		setToolTipText("Export Mining Results");
	}
	
	public void run(){
		if (FLATTT.MSRRun){
			FileDialog fileDialog = new FileDialog(view.getViewSite().getShell(), SWT.SAVE);
			fileDialog.setFilterNames(new String[] {"Zip Files (*.zip)"});
			fileDialog.setFilterExtensions(new String[] {"*.zip"});
			fileDialog.setText("Save MSR data...");
			
			String file = fileDialog.open();
			File dest = new File(file);
			
			//FLATTT.msrRules.renameTo(new File(file));
			try{
			InputStream inStream = new FileInputStream(FLATTT.msrRulesFile);
			OutputStream outStream = new FileOutputStream(dest);
			
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inStream.read(buffer)) > 0){
				outStream.write(buffer, 0, length);
			}
			inStream.close();
    	    outStream.close();
			}catch(IOException e){
				System.err.println("Error: can't export Mining results.");
			}
		}
	}
}
