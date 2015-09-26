package edu.wm.flat3.analysis;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
//import org.severe.jripples.eig.JRipplesEIGNode;


public class Sorters {
	private ViewerSorter nameComparator; 
	private ViewerSorter markComparator;
	private ViewerSorter probabilityComparator;
	private ViewerSorter fullyQualifiedNameComparator;
	private ViewerSorter memberComparator;
	private ViewerSorter rankComparator;
	
	public Sorters() {
		initCopmarators();
	}

	
	public ViewerSorter getNameComparator() { 
		return nameComparator;
	}
	
	public ViewerSorter getMarkComparator() {
		return markComparator;
	}
	
	public ViewerSorter getProbabilityComparator() {
		return probabilityComparator;
	}
	public ViewerSorter getFullyQualifiedNameComparator() {
		return fullyQualifiedNameComparator;
	}
	public ViewerSorter getMemberComparator() {
		return memberComparator;
	}
	public ViewerSorter getRankComparator(){
		return rankComparator;
	}
	private void initCopmarators() {
		nameComparator = new ViewerSorter() {
			public int compare(Viewer viewer,Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				if ((((FLATTTMember) o1).getShortName()==null) || (((FLATTTMember) o2).getShortName() ==null)) return 0;
				return ((FLATTTMember) o1).getShortName().compareTo(

						((FLATTTMember) o2).getShortName());
			}

		};

		markComparator = new ViewerSorter() {
			public int compare(Viewer viewer,Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				if ((((FLATTTMember) o1).getMark()==null) || (((FLATTTMember) o2).getMark() ==null)) return 0;
				
				return ((FLATTTMember) o1).getMark().compareTo(
						((FLATTTMember) o2).getMark());
			}

		};

		
		
		probabilityComparator = new ViewerSorter() {
			public int compare(Viewer viewer,Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				
				if ((((FLATTTMember) o1).getProbability()==null) && (((FLATTTMember) o2).getProbability()==null)) return 0;
				if ((((FLATTTMember) o1).getProbability()==null) && (((FLATTTMember) o2).getProbability()!=null)) return -1;
				if ((((FLATTTMember) o1).getProbability()!=null) && (((FLATTTMember) o2).getProbability()==null)) return 1;
				if (((((FLATTTMember) o1).getProbability()+"").compareTo("")==0) && ((((FLATTTMember) o2).getProbability()+"").compareTo("")==0)) return 0;
				if (((((FLATTTMember) o1).getProbability()+"").compareTo("")==0) && ((((FLATTTMember) o2).getProbability()+"").compareTo("")!=0)) return -1;
				if (((((FLATTTMember) o1).getProbability()+"").compareTo("")!=0) && ((((FLATTTMember) o2).getProbability()+"").compareTo("")==0)) return 1;
				
				return ((Float.valueOf(((FLATTTMember) o1)
						.getProbability()+"")).compareTo((Float
						.valueOf(((FLATTTMember) o2).getProbability()+""))));
			}
		};

		fullyQualifiedNameComparator = new ViewerSorter() {
			public int compare(Viewer viewer,Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				if ((((FLATTTMember) o1).getFullName()==null) || (((FLATTTMember) o2).getFullName() ==null)) return 0;
				return ((FLATTTMember) o1).getFullName().compareTo(
						((FLATTTMember) o2).getFullName());
			}

		};
		
		memberComparator = new ViewerSorter() {
			public int compare(Viewer viewer, Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				if (((FLATTTMember) o1).getNodeIMember().getElementType()!=((FLATTTMember) o2).getNodeIMember().getElementType()) return Integer.valueOf(((FLATTTMember) o1).getNodeIMember().getElementType()).compareTo(Integer.valueOf(((FLATTTMember) o2).getNodeIMember().getElementType()));
					else return ((FLATTTMember) o1).getNodeIMember().getElementName().compareTo(((FLATTTMember) o2).getNodeIMember().getElementName());
				
			}

		};
		
		rankComparator = new ViewerSorter(){
			public int compare(Viewer viewer, Object o1, Object o2) {
				if ((o1==null) || (o2==null)) return 0;
				if ( ((FLATTTMember) o1).getRank() < ((FLATTTMember) o2).getRank()) return -1;
				else if ( ((FLATTTMember) o1).getRank() == ((FLATTTMember) o2).getRank()) return 0;
				else return 1;
			}
		};
	}
}
