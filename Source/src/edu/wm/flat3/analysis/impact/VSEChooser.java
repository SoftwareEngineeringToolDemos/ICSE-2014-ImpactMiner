package edu.wm.flat3.analysis.impact;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import edu.wm.flat3.analysis.FLATTTMember;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class VSEChooser extends ElementListSelectionDialog {
	public VSEChooser(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
	}
	public VSEChooser(Shell parent, Object[] elements){
		super(parent, new ILabelProvider(){

			@Override
			public Image getImage(Object element) {
				return null;
			}

			@Override
			public String getText(Object element) {
				FLATTTMember member = (FLATTTMember) element;
				return member.getClassName()+"."+member.getShortName();
			}

			@Override
			public void addListener(ILabelProviderListener listener) {

			}

			@Override
			public void dispose() {

			}

			@Override
			public boolean isLabelProperty(Object element,
					String property) {
				return false;
			}

			@Override
			public void removeListener(
					ILabelProviderListener listener) {

			}

		});
		setElements(elements);
		setTitle("Select a starting Element");
		setMessage("Select the starting element from which to infer suggested elements\n" +
				"? = wildcard character\n" +
				"* = wildcard string");
		setMultipleSelection(true);
	}


}
