Changelog:
v0.2.4
	- Changed the trace button to rerun the last trace run this session, if any has been run yet.
	- Better location of the plugin bundle file path, so that we can sucessfully call MUTT.
	- Automatically generate code model before running a trace. (otherwise no results will be seen)
	
v0.2.3
	- Better support for spaces in pathnames when tracing, fixed finding the bin directory of the project to trace.
	- Clear combinational search button was greyed out when it shouldn't have been.
	- Fixed some exceptions that could occur when tracing programs.
	- Probabilities are now displayed properly for combinational searches.
	- We now do tracing in a thread so Eclipse doesn't hang while MUTT runs.
	- Fixed a case where the traced program would hang in Windows and possibly other systems.
	- Added a stop trace button to the Eclipse UI (in the concerns view).
	
v0.2.2
    - Fixed tracing on Windows.
    - Warns user if tools.jar can't be found, gives some ideas for fixing the issue.
    - Code model is now automatically generated before indexing with Lucene; before had be done manually.
    - Search results table is no longer virtual; may be a bit slower, but this was causing major problems with removing old results.
    - Fixed draging from search results to features; would add the wrong element.
    - Minor UI tweaks and fixes (one new icon, probability is truncated to 3 decimals, better table column sizes, etc).

v0.2.1
	- Some bugfixes, particularly with regard to faulty behavior when using the plugin for the first time.



Features:
	- Drag search/trace items into features.
	- Right click on a java file to run a trace on it.
	- Save/load traces with toolbar buttons.
	- Visualize traces/search results with toolbar button.
	- Right click on a feature to visualize it's components' locations in source.
	- Double click on search results to open their source file.
	
Attributions:
	- Search uses the Lucene library, as modeled after JRipples' implementation.
	- Concern/Features feature is from the ConcernTagger tool.
	- Tracing is done using MUTT, and traces are read using code from the ET tool.

	- New icons from the silk icon package: http://www.famfamfam.com/lab/icons/silk/
	
	
	
Developer notes:
Things to fix for next release:

   
Known issues:
   - When reindexing lucene, code model is always regenerated. may not be neccessary.
   - If program being traced requires arguments, extra libraries, etc, currently may not be able to set those things up in the GUI
   
   - There may be some situations in which the index/code model database gets corrupt, won't reindex - have to manually delete and/or restart eclipse.
   - Can't add java elements without source to features?
   
   - (In OSX, must be run with 64 bit cocoa version of eclipse since FLAT3 requries java 1.6
  
  
  
Wishlist/potential issues: (some may already be fixed)
   - maybe periodically show trace results as trace is running?
   - maybe highlight the search term in source code when doing searches?

   - Hmm, table view doesn't show all the info in the traces (methods from superclasses, call relationships)
   - probability for traces (n method appearances / N)? not in dot files though
   
   - is it possible for code modeling, indexing, searching, etc to happen in the wrong order?
   - did we need the speed of virtual tables?
   - need to do async refreshes anywhere?
   - change droping to like right clicking- add to conern, then generate code model
   - can't dragadd - remove - draggadd same item, have to do another one in between?
   
   - allow cutoffs for search results? by prob/count?
   - can't search for classes? is that a problem?
   - project selection when searching instead of searching whole workspace?
   
   - are the source ranges in visualization strictly accurate for things like subclasses? probably
   - better visualization; click to go to source item, hover info, etc