/**
 * 
 */
package helpers

import groovy.lang.Closure;

/**
 * @author Scarlett Clark
 * Misc DSL generation classes.
 */
class ProjectMiscHelpers {
	// Used for Frameworks downstream setup.
	static String createDownstream(String downstream, String branch, String branchGroup) {		
		def String downstreamnew		
		downstreamnew = downstream + ' ' + branch  + ' ' + branchGroup 	
		return downstreamnew
	}
	static Closure miscParent() {
	// We always want the "Parent" to build on master	
		return {
			it / assignedNode <<  'master'
			it / canRoam <<	 false						
		}		
	}
	static Object findPath(String path, Object metadata, String jobname, boolean external ) {
		try { 
		// For reasons unknown the path in kde_projects.xml says qt5/qt5
		// which does not match logical_module_structure...
		if (jobname == 'qt5' ) {
			def newpath = "qt5"	
			def qt5path = metadata.groups.find {key, value -> key == newpath}
			if (qt5path != null) {
				def newmap = qt5path.collect()
				return newmap
			}
		}
		//SVN jobs do not have branches nor are they in the xml. 
		// We need to continus to skip this check
		if ( external == true ) {
			return 
			}
		
		//We need to find the path in logical-module-structure, 
		//and use parent if there is not a specific application setting.
		def basepath = ( ~/${path}\// )
		def wholepath = metadata.groups.find {key, value -> key == "${path}"}
		def wildcardpath = basepath.toString().minus(jobname + '/') + '*'
		def subpath = metadata.groups.find {key, value -> key == "${wildcardpath}"}	
		// An ugly fix for ktp that captures the ktp* wildcard. Needs to be replaced
		// with a fancy regex that captures any job that uses this approach. 
		def ktpappgroupwildcard = basepath.toString().minus(jobname + '/') + 'ktp*'
		def ktpsubpath = metadata.groups.find {key, value -> key == "${ktpappgroupwildcard}"}
		if (wholepath != null) {
			def newmap = wholepath.collect()
			return newmap
		} else if (ktpsubpath != null) {
			def newmap = ktpsubpath.collect()
			return newmap
		} else if (subpath != null ){
			def newmap = subpath.collect()
			return newmap		
			} else { 
				def catchallkde
				if (basepath =~ "kde/[A-Za-z]+") {
				catchallkde = 'kde/*'
			}
			def wcwholepath = metadata.groups.find {key, value -> key == "${catchallkde}"}
			def newmap = wcwholepath.collect()
			return newmap			
		}
		} catch(Exception e) { throw new JobNotFoundException("Unable to create job Not defined in logical_module_structure", e); }			
	}		
} // end

class JobNotFoundException extends Exception {
	public JobNotFoundException () {
	
		}
	
		public JobNotFoundException (String message) {
			super (message);
		}
	
		public JobNotFoundException (Throwable cause) {
			super (cause);
		}

	public JobNotFoundException (String message, Throwable cause) {
		super (message, cause);
	}
	
}

