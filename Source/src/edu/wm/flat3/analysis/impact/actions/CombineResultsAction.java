package edu.wm.flat3.analysis.impact.actions;

import java.util.ArrayList;

import edu.wm.flat3.FLATTT;
import edu.wm.flat3.analysis.FLATTTMember;
import edu.wm.flat3.analysis.TableViewContentProvider;

/**
 * @author mmwagner@email.wm.edu
 *
 */
public class CombineResultsAction {
	private static int max = 500;
	
	public static void combine(){
		ArrayList<FLATTTMember> toShow = null;
		FLATTTMember toAdd;
		int r;
		
		if (FLATTT.luceneRun && FLATTT.luceneShown){
//			toShow = FLATTT.luceneResults;
			//FLATTT.sortFLATTTMemberList(FLATTT.luceneResults);
			toShow = new ArrayList<FLATTTMember>();
			r = 1;
			for (FLATTTMember flatttMember : FLATTT.luceneResults) {
				toAdd = flatttMember.copy();
				toAdd.setRank(r++);
				toShow.add(toAdd);
				if (r >= max) break;
			}
			
			if (FLATTT.traceRun && FLATTT.traceShown){
				toShow = new ArrayList<FLATTTMember>();
				r = 1;
				for (FLATTTMember flatttMember : FLATTT.luceneResults) {
					if (FLATTT.traceResults.contains(flatttMember)){
						toAdd = flatttMember.copy();
						toAdd.setRank(r++);
						toShow.add(toAdd);
					}
				}
			}
			
			if (FLATTT.MSRRun && FLATTT.MSRShown){
				ArrayList<FLATTTMember> tmp = new ArrayList<FLATTTMember>();
				FLATTT.sortFLATTTMemberList(FLATTT.msrResults);
				//FLATTT.sortFLATTTMemberList(toShow);
				int lCursor = 0;
				int hCursor = 0;
				//int numAdded = 0;
				r = 1;
				while (lCursor < toShow.size() || hCursor < FLATTT.msrResults.size()){
					while (lCursor < toShow.size()){
						if (!tmp.contains(toShow.get(lCursor))){
							toAdd = toShow.get(lCursor++).copy();
							toAdd.setRank(r++);
							tmp.add(toAdd);
							//numAdded++;
							break;
						}
						lCursor++;
					}
					while (hCursor < FLATTT.msrResults.size()){
						if (!tmp.contains(FLATTT.msrResults.get(hCursor))){
							toAdd = FLATTT.msrResults.get(hCursor++).copy();
							toAdd.setRank(r++);
							tmp.add(toAdd);
							//numAdded++;
							break;
						}
						hCursor++;
					}
				}
				toShow = tmp;
			}
		}else{ // Lucene was not run/not shown
			if (FLATTT.MSRRun && FLATTT.MSRShown){
				//toShow = FLATTT.msrResults;
				toShow = new ArrayList<FLATTTMember>();
				r = 1;
				FLATTT.sortFLATTTMemberList(FLATTT.msrResults);
				if (FLATTT.traceRun && FLATTT.traceShown){
					for (FLATTTMember flatttMember : FLATTT.msrResults) {
						if (FLATTT.traceResults.contains(flatttMember)){
							toAdd = flatttMember.copy();
							toAdd.setRank(r++);
							toShow.add(toAdd);
						}
					}
				}
				else{
					for (FLATTTMember flatttMember : FLATTT.msrResults) {
						toAdd = flatttMember.copy();
						toAdd.setRank(r++);
						toShow.add(toAdd);
					}
				}
			}else{ // lucene was not run/not shown, msr was not run/not shown
				if (FLATTT.traceRun && FLATTT.traceShown)
					toShow = FLATTT.traceResults;
				//else toShow remains null.
			}
			
			
		} // end else
		FLATTT.searchResults = toShow;
		FLATTT.searchResultsAreCombinational = false;
		TableViewContentProvider contentP =  (TableViewContentProvider) FLATTT.tableView.getViewer().getContentProvider();
		contentP.refreshTable();
	} // end function
	

} //end class
