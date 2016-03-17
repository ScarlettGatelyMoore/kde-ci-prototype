/**
 * 
 */
package helpers

import groovy.lang.Closure;

/**
 * @author Scarlett Clark
 * We can create many combinations here. Be sure to create a boolean to define where to use it.
 * The variations check will always need to be made once so add it to any new combinations.
 * Set each possibility as a variable add as resources become available.
 */
class CreatecombinationFilterHelper {
	static Closure createCombinationFilter(ArrayList variations, ArrayList varlin, ArrayList varwin, ArrayList varosx, String combinationset) {
		// Define the possible combinations as variables.		
		def lingcc = '(PLATFORM=="Linux" && compiler=="gcc")'
		def winvs2013 = '(PLATFORM=="Windows" && compiler=="vs2013")'
		def osxclang = '(PLATFORM=="OSX" && compiler=="clang")'
		def linvariationstring
		def winvariationstring
		def osxvariationstring
		// Check for variations and assign a variable then build a new string with the result.
		def variationstring
		if (variations != null) {
			linvariationstring = generatevariations("Linux", lingcc, variations, varlin)
			winvariationstring = generatevariations("Windows", winvs2013, variations, varwin)
			osxvariationstring = generatevariations("OSX", osxclang, variations, varosx)
		}
		
		if (combinationset == "default" && variations != null) {			
			def all = linvariationstring + ' || ' + winvariationstring + ' || ' + osxvariationstring
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}	 
		} else if (combinationset == "default" && variations == null) {
			def all = lingcc + ' || ' + winvs2013 + ' || ' + osxclang
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}		 
		} else if (combinationset == "Linux" && variations != null) {		
			def all = linvariationstring
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}						
		} else if (combinationset == "Linux" && variations == null) {
			def all = lingcc
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}	 					
		} else if (combinationset == "LinandOSX" && variations != null) {			
			def all = linvariationstring + ' || ' + osxvariationstring
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  					
			}						
		} else if (combinationset == "LinandOSX" && variations == null){			
			def all =  lingcc + ' || ' + osxclang
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}		 				
		} else if (combinationset == "LinandWin" &&  variations != null) {			
			def all = linvariationstring + ' || ' + winvariationstring
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}		 
		} else if (combinationset == "LinandWin" &&  variations == null) {
			def all =  lingcc + ' || ' + winvs2013
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}			
		} else if (combinationset == "Win" && variations != null) {
			def all = winvariationstring
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}		 			
		} else if (combinationset == "Win" && variations == null) {
			def all = winvs2013	
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}							
		} else if (combinationset == "OSX" && variations != null) {	
			def all = osxvariationstring
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}					
		} else if (combinationset == "OSX" && variations == null) {
		def all = osxclang
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<			 					
					combinationstring  
			}				
		}	
	}
	static String generatevariations(String platform, String compiler, ArrayList variations, ArrayList platformvariations) {
		// Final string variables
		def linvariation
		def winvariation
		def osxvariation
		// Check for platform level variation specifications.
		// These are defined in identifiers.json
		ArrayList definedvariation
		if (platformvariations != null) {
			definedvariation = platformvariations		
		} else {
			definedvariation = variations
		}
		def i
		//Run through each combination set (defined in idenifiers.json if not default (all)
		// generate the combinations and add them together in a string and return it.
		def variationstring = new StringBuilder()
		if (platform == "Linux") {
			for ( i=0; i< variations.size(); i++) {				
				linvariation = compiler + ' && ' + '(PLATFORM=="Linux" && Variation=="' + definedvariation[i] + '")'
				variationstring.append(linvariation + ' || ')
			}				
		} else if (platform == "Windows") {	
			for ( i=0; i< variations.size(); i++) {
				winvariation = compiler + ' && ' + '(PLATFORM=="Windows" && Variation=="' + definedvariation[i] + '")'
				variationstring.append(winvariation + ' || ')
			}					
		} else if (platform == "OSX") {	
			for ( i=0; i< variations.size(); i++) {
				osxvariation = compiler + ' && ' + '(PLATFORM=="OSX" && Variation=="' + definedvariation[i] + '")'
				variationstring.append(osxvariation + ' || ')	
			}				
		}	
		return variationstring.toString() - ~/[^)]*$/
	}

}

