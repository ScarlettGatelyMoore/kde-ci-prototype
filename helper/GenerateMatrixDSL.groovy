/**
 * 
 */
package helper

/**
 * @author scarlett
 *
 */
class GenerateMatrixDSL {
	String combo_platform
	String combo_compiler
	String combo_variation
	GenerateMatrixDSL() {
		
	}
	
	def generateDSLPlatformMatrix(jobPlatforms) {
		if (jobPlatforms) {
			return { project ->
				project.name = 'matrix-project'
				project / axes / 'hudson.matrix.LabelAxis' {
					name 'PLATFORM'
					values {
						jobPlatforms.each {
							if (it) { string it }
						}
					}
				}
			}
		}
	}
	
	def generateDSLCompilerMatrix(compilers) {
		if (compilers) {
			return { project ->
				project.name = 'matrix-project'
				project / axes / 'hudson.matrix.TextAxis' {
					name 'compiler'
					values {
						compilers.each { 					
							if (it) { string it }
						}
					}
				}
			}
		}
	}
	
	def generateDSLVariationsMatrix(Variation) {
		if (Variation) {
		return { project ->
			project.name = 'matrix-project'
			project / axes << 'hudson.matrix.TextAxis' {
				name 'Variation'
				values {
					Variation.each {
						if (it) { string it }
					}
				}
			}
		}
		} else { return {} }
	}
	// TO-DO DEUGLYFY make this more dynamic with json variables. Put into class.
	def createCombinationFilter(ArrayList variations, ArrayList varlin, ArrayList varwin, ArrayList varosx, ArrayList varand, ArrayList varubu) {
		// Define the possible combinations as variables.
		def lingcc = '(PLATFORM=="Linux" && compiler=="gcc")'
		def winvs2013 = '(PLATFORM=="Windows" && compiler=="vs2013")'
		def osxclang = '(PLATFORM=="OSX" && compiler=="clang")'
		def android = '(PLATFORM=="Android" && compiler=="android_sdk")'
		def ubuntup = '(PLATFORM=="UbuntuP" && compiler=="ubuntu_sdk")'
		def linvariationstring
		def winvariationstring
		def osxvariationstring
		def andvariationstring
		def ubuvariationstring
		// Check for variations and assign a variable then build a new string with the result.
		def variationstring
		if (variations) {
			linvariationstring = generatevariations("Linux", lingcc, variations, varlin)
			winvariationstring = generatevariations("Windows", winvs2013, variations, varwin)
			osxvariationstring = generatevariations("OSX", osxclang, variations, varosx)
			andvariationstring = generatevariations("Android", android, variations, varand)
			ubuvariationstring = generatevariations("UbuntuP", ubuntup, variations, varubu)
		}
		
		if (variations) {
			def all = linvariationstring + ' || ' + winvariationstring + ' || ' + osxvariationstring + ' || ' + andvariationstring \
			+ ' || ' + ubuvariationstring
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<
					combinationstring
			}
		} else if (!variations) {
			def all = lingcc + ' || ' + winvs2013 + ' || ' + osxclang + ' || ' + android + ' || ' + ubuntup
			def combinationstring = all.toString().replaceAll (/'/, '')
			return { project ->
				project.name = 'matrix-project'
				project / combinationFilter <<
					combinationstring
			}
		} /*else if (combinationset == "Linux" && variations != null) {
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
		}*/
	}
	static String generatevariations(String platform, String compiler, ArrayList variations, ArrayList platformvariations) {
		// Final string variables
		def linvariation
		def winvariation
		def osxvariation
		def andvariation
		def ubuvariation
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
		} else if (platform == "Android") {
			for ( i=0; i< variations.size(); i++) {
				andvariation = compiler + ' && ' + '(PLATFORM=="Android" && Variation=="' + definedvariation[i] + '")'
				variationstring.append(andvariation + ' || ')
			}
		} else if (platform == "UbuntuP") {
			for ( i=0; i< variations.size(); i++) {
				ubuvariation = compiler + ' && ' + '(PLATFORM=="UbuntuP" && Variation=="' + definedvariation[i] + '")'
				variationstring.append(ubuvariation + ' || ')
			}
		}
		return variationstring.toString() - ~/[^)]*$/
	}

}
