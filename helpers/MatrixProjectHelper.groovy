package helpers;
import hudson.matrix.*

/**
 * @author Scarlett Clark
 * Helper class to build KDE Matrix
 */
class MatrixProjectHelper {
	static Closure matrixPlatform(Iterable<String> labels) {
		if (labels != null) {
		return { project ->			
			project.name = 'matrix-project'
			project / axes / 'hudson.matrix.LabelAxis' {
				name 'PLATFORM'
				values {					
					labels.each { 
						if (it != null) { string it }
					}
					}
				}
			}
		}
	}
	static Closure matrixCompiler(Iterable<String> labels) {		
		return { project ->
			project.name = 'matrix-project'
			project / axes / 'hudson.matrix.TextAxis' {
				name 'compiler'
				values {					
					 labels.each { 
						 if (it != null) { string it }					 
					}				
				}				
			}				
		}
	}
	static Closure matrixVariations(Iterable<String> labels) {
		if ( labels != null ) {
		return { project ->
			project.name = 'matrix-project'
			project / axes << 'hudson.matrix.TextAxis' {
				name 'Variation'
				values {
					labels.each { string it }
				}
			}
		}
		} else { return {} }
	}
	static ArrayList determinePlatforms(String combinations, String branchGroup, ArrayList platforms) {
		if (combinations == "Linux") {			
			return platforms = ["Linux",null,null]
		} else if (combinations == "LinandOSX") {
			return platforms = ["Linux","OSX",null]
		} else if (combinations == "LinandWin") {
			return platforms = ["Linux","Windows",null]
		} else if (branchGroup == "stable-qt4") {
			return platforms = ["Linux",null,null]			
		} else if (branchGroup == "latest-qt4")	{
			return platforms = ["Linux",null,null]
		} else if (branchGroup == "kf5-qt5") {
			//Disabling Win temporarily
			//return platforms
			return platforms = ["Linux","OSX",null]	
		} else if (branchGroup == "kf5-minimum") {
				//Disabling Win temporarily
				//return platforms
			return platforms = ["Linux","OSX",null]
		} else if (branchGroup == "stable-kf5-qt5") {
			//Disabling Win temporarily
			//return platforms
			return platforms = ["Linux","OSX",null]
		}
	}	
	static ArrayList determineCompilers(String combinations, String branchGroup, ArrayList compilers) {
		 /* And the same for compilers */
		if (combinations == "Linux") {
			return compilers = ["gcc",null,null]
		} else if (combinations == "LinandOSX") {
			return compilers = ["gcc","clang",null]	
		} else if (combinations == "LinandWin") {
			return compilers = ["gcc","vs2013",null]
		} else if (branchGroup == "stable-qt4") {
			return compilers = ["gcc",null,null]
		} else if (branchGroup == "latest-qt4") {
			return compilers = ["gcc",null,null]
		} else if (branchGroup == "kf5-qt5") {
			// Disabling Win temporarily
			//return compilers
			return compilers = ["gcc","clang",null]	
		} else if (branchGroup == "kf5-minimum") {
			// Disabling Win temporarily
			//return compilers
			return compilers = ["gcc","clang",null]
		} else if (branchGroup == "stable-kf5-qt5") {
			// Disabling Win temporarily
			//return compilers
			return compilers = ["gcc","clang",null]
		}
	}
	
}