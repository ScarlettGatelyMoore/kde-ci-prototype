
/**
 * @author Scarlett Clark
 * Base application DSL Groovy code.
 * Insert each project name that you would like to create a job for. 
 * There must be a matching .json file in the config folder
 * */
 
import static helpers.MatrixProjectHelper.*
import static helpers.CreatecombinationFilterHelper.*
import static helpers.ProjectConfigHelper.*
import static helpers.ProjectSCMHelpers.*
import static helpers.ConditionalProjectHelper.*
import static helpers.ProjectMiscHelpers.*
import static helpers.PublishersProjectHelper.*
import static helpers.ProjectNotificationsHelper.*
import groovy.lang.Closure;
import javaposse.jobdsl.dsl.helpers.*
import java.lang.Object;
import java.util.ArrayList;

import hudson.model.*
import hudson.maven.*

/* Retrieve kde_projects.xml for later use*/
def xmlurl = 'http://projects.kde.org/kde_projects.xml'
//Read the file and save it locally
def projectsfile = new File("${WORKSPACE}/projects.xml").newOutputStream()
projectsfile << new URL(xmlurl).openStream()
projectsfile.close()

/* Bring in the identifiers.json data and if createjob is set true,
 *  continue with the DSL script. Otherwise print false so we can look into 
 *  whether or not we want to change that.
 */
ArrayList jobjsonfiles = (ArrayList) getJobConfig('identifiers.json', "${JENKINS_SLAVE_HOME}")
Iterator projects = jobjsonfiles.iterator();

//println verificationCheck(jobjsonfiles, "${WORKSPACE}")
/* Start the job variable configuration section for each each job*/	
while(projects.hasNext()) {
	/* This object contains the json config from
	 * identifiers.json for each project. Defaults 
	 * are set and if you need to set non default values
	 * they go in identifiers.json
	 */
	Object json = projects.next()
		
	def jobname = json.projectname

	Map projectxml = createKDEProjects("${WORKSPACE}", jobname)	
	
		
	// Assign Logrotator json variables
	def nondefaultlogrotator
	if (json.logrotator != null) {
		logrotator = json.logrotator.tokenize( ',' )
	}
	def (daysToKeep, numToKeep, artifactDaysToKeep, artifactNumToKeep) = nondefaultlogrotator ?: ["-1","10","-1","-1"]
	
	String jobdesc = projectxml.description ?: "KDE job"
	/* Matrix variables needed later, Default values. 
	 * PLATFORM DEFAULT
	 * If you wish to change these values
	 * update identifiers.json with desired values eg.
	 * "platforms":"Linux,Windows", */
	ArrayList platforms = json.platforms ?: ["Linux","Windows","OSX"]
	/* COMPILER DEFAULT
	* If you wish to change these values
	* update identifiers.json with desired values eg.
	* "compilers":"gcc,vs2013", */
	ArrayList compilers = json.compilers ?: ["gcc","vs2013","clang"]
	// Determine if a job has Variations and create a variations matrix if true.
	ArrayList Variation = json.variations ?: null
	// Now any os specific variation check
	ArrayList variationlin = json.variationlin ?: null
	ArrayList variationwin = json.variationwin ?: null
	ArrayList variationosx = json.variationosx ?: null
	/*END Matrix configuration variables */
	
	/* Email and IRC information needed for publishers
	 * These values can be added to identifiers.json
	 */
	String email = json.email ?: "sgclark@kde.org"
	String irc = json.irc ?: "#kde-builds"
	/* Repo specific variables */
	//Configure the repo from dependencies //TO-DO this better
	def repo
	def external
	if (json.svn != null) {
		repo = json.svn
		external = true
	} else if (json.bzr != null) {
		repo = json.bzr
		external = true
	} else if (json.git != null) {
		repo = json.git
		external = true
	} else if (json.hg != null) {
		repo = json.hg
		external = true
	} else if (json.tar != null) {
		external = true	
	} else {
		repo = projectxml.repourl.find { it.value }.toString()
		external = false
	}	
	

	//Determine path needed for branch configuration
	def path = projectxml.path
	/*These variables are used to define whether we create
	 * The respective DSL. If you need a value that differs from the default
	 * an entry must be made in the jobs definition in identifiers.json
	 */
	// This determines if publishers are generated: add "publishers":"false"
	// if you do not wish to have publishers: TO-DO could probably break out each 
	// publisher to its own variable.
	def flexpublishers = json.publishers ?: true
	// If you wish to disable html5 notifier set "html5":"true"
	def html5_notifier = json.html5 ?: false
	// If a project does not generate a cobertura report
	// this needs to be set "coberturareport":"false" or it
	// will fail the build. TO-DO: figure out why this refuses to work with flexible publisher.
	def coberturareport = json.coberturareport ?: true
	def cronschedule = json.cron ?: null
	
	// Grab the branches and branchgroups metadata from the logical-module-structure
	def JOBCONFIG = new java.io.FileReader("${JENKINS_SLAVE_HOME}/dependencies/logical-module-structure")
	def result = new groovy.json.JsonSlurper().parse(JOBCONFIG)
	Object metadata = result
	
	/*Downstream job set in identifiers.json
	 * We do not have enough slave support to handle more than one each.
	 * We need to find the downstream branch in the current branchgroup
	 * This is set up here due to need the need of the metadata  object.
	 */
	String setdownstream = json.downstream ?: null
	
	/* Change setdownstream to null by default (creates rebuild avalanches)
	* If you are bringing a new platform or what not and want an automated
	* build order comment out this line.
	*/
	setdownstream = null
	//Create a variable to hold the downstream branch information we need later.
	def downstreampath
	if (setdownstream != null) {
		Map downstreamxml = createKDEProjects("${WORKSPACE}", setdownstream)
		def dspath = downstreamxml.path
		downstreampath = findPath(dspath, metadata, setdownstream, external)
	}
	// Now with the metadata we need to search for the <path> and find data and or wildcards.	
	def grouppath = findPath(path, metadata, jobname, external)
	
	// Remove pesky null entries.
	if (external == true ) { 
	} else {
		grouppath.removeAll([null])		
	}
	// Put path result in a map
	def newmap
	def downstreammap
	if (repo =~ 'svn:/') {		
		println "Creating SVN job ${jobname}"
	} else if (external == true && json.hg != null) {
		println "Creating External Mercurial job ${jobname}"
	} else if (external == true && json.bzr != null) {
		println "Creating External Bazaar job ${jobname}"
	} else if (external == true && json.git != null) {
		println "Creating External job ${jobname}"
	} else if (external == true && json.tar != null) {
			println "Creating External tar job ${jobname}"
	} else if (grouppath == [] && external != true) {		
		println "Unable to create ${jobname} it is not defined in logical_module_structure"	
		continue;
	} else {
		newmap = [:] << grouppath
		
	}
	// And again for the downstream path
	if (downstreampath != null) {
		downstreampath.removeAll([null])
		downstreammap = [:] << downstreampath
	}
	// Retrieve the branchGroup/branch map from the path result.
	def branchlist	
	if (external == true) {
		def bl = json.getAt("branchgroups")
		newmap = [:] << bl
		def Map branchgroups = ['branchgroups':newmap]
		branchlist = branchgroups.values() as HashMap;
	} else {
		branchlist = newmap.values() as HashMap;
		def iterator = branchlist.entrySet().iterator()
		while (iterator.hasNext()) {
			if (!iterator.next().value) {
				iterator.remove()
			}
		}
	}
	//Collect the branchGroup/branch information for the downstream job.
	def downstreambranchlist
	if (downstreammap != null) {
		downstreambranchlist = downstreammap.values() as HashMap;
		def iterator = downstreambranchlist.entrySet().iterator()
		while (iterator.hasNext()) {
			if (!iterator.next().value) {
				iterator.remove()
			}
		}
	}

	// Gather the branchGroup keys and iterate branchGroup as a closure		
	def branchgroups = branchlist.keySet() as String[];

	
	// Redmine TODO This is quick dirty fix, we moved away from redmine.
	// Linking to anongit. Though I think this should be phabricator.
	def redmine = 'https://quickgit.kde.org/?p=' + jobname + '.git'
	// Now for each branchGroup determine branchGroup specific values.
	branchgroups.each { branchGroup ->
		
		// Now that we have a branchGroup value we need to grab the correct branch 
		// of the downstream project. And finally call the name builder and pass to the DSL.
		def bgdownstream
		if (downstreambranchlist != null) {
			String currbranch
			currbranch = downstreambranchlist.getAt(branchGroup).toString().trim()		
			bgdownstream = createDownstream(setdownstream, currbranch, branchGroup)
		}
		try{
		branch = branchlist.getAt(branchGroup).toString().trim()
		
		
				
		def tokenid = json.token ?: "PNcTKQORJW653QKVTwL0GV64OZA-${jobname}"
		
		// Determine which combination filter to use
		// Currently we support a filter for variations and without.
		/* Here we determine the combinations we want to build.
		 * It is *VERY* important that if platforms or compilers are
		 * changed in identifiers.json the appropriate combination
		 * must be selected and listed in identifiers.json eg.
		 * You want Linux and OSX would be:
		 * "combinations":"LinandOSX",
		 * Currently the compiler support is only
		 * linux-gcc, windows-msvc2013 osx-clang, but will likely change in the future.
		 * Current options are:
		 * 'Linux' -- only linux builds
		 * 'LinandOSX'
		 * 'LinandWin'
		 * 'OSX'
		 * 'Win'
		 * The variable combinations is also used to determine the build command in
		 * ConditionalProjectHelper.groovy.
		 * Please see CreatecombinationFilterHelper.groovy to make more combinations.
		 */
		// Default is 'Linux,Windows,OSX' 'gcc,vs2013,clang' respectively.
		//Check custom combination else use the default

/*		def	combinations = json.combinations ?: "LinandOSX"		
		if (branchGroup == "latest-qt4" || branchGroup ==  "stable-qt4") {
			combinations = "Linux"
		}*/
		// Temporary turn off osx till a resolution to macports and system libs is 
		// determined.
		combinations = "Linux"
		/* We need to determine the qt4 branchGroups and only create Linux builds
		 * Qt5 we use the json config values.
		 * We also need to set combination filters.
		 */
		jobplatforms = determinePlatforms(combinations, branchGroup, platforms)
		jobcompilers = determineCompilers(combinations, branchGroup, compilers)
		// Print each project being created and begin DSL.
		println "Creating -- ${jobname}: BranchGroup: ${branchGroup} Branch: ${branch}"  
		println "Downstream -- ${json.downstream}"		
		// change the patch jobname back to original so that python scripts can find the metadata
		// -patch is only needed to create a seperate job in the patch group that can be identified
		// easier.
		if (branchGroup == "patch") {
			jobname = jobname - '-patch'
		}
		/* BEGIN DSL CODE */
		
		matrixJob("${jobname} ${branch} ${branchGroup}".replaceAll('/','-')) {
		configure { project ->
			project / 'actions' {}	
			project << authToken( "${tokenid}")			
		}
		description "${jobdesc}\n ${branch} build for ${jobname}"
		
		// Disable kf5-minimum for now
		if ( jobname != "qt5" && branchGroup ==~ "kf5-minimum") {			 
			project << disabled(true)			 
		}
	
		// limit job cache (Set in json logrotator)
		logRotator(daysToKeep.toInteger(), numToKeep.toInteger(), artifactDaysToKeep.toInteger(), artifactNumToKeep.toInteger())
		configure { project ->
			project / 'properties' / 'org.jenkins.ci.plugins.html5__notifier.JobPropertyImpl' {
				skip html5_notifier.toBoolean()
			}
		}
		customWorkspace('${HOME}' + '/' + "${jobname}" + '/' + "${branchGroup}")
		childCustomWorkspace(".")
		if (jobname =~ "kolab")	{
			environmentVariables {
				env('GIT_SSL_NO_VERIFY', '1')
			}
		}
		
	// We want Qt4 buildss to use trusty containers, this is set here.
		
		if (branchGroup =~ "qt4") {
			configure { project ->
				project.name = 'matrix-project'
				project / 'properties' << 'jp.ikedam.jenkins.plugins.groovy_label_assignment.GroovyLabelAssignmentProperty' {
					groovyScript 'def labelMap = [ Linux: "QT4"]; return labelMap.get(binding.getVariables().get("PLATFORM"));'
				}
			}
		} /*else if (jobname == 'kdepim') {
			configure { project ->
				project.name = 'matrix-project'
				project / 'properties' << 'jp.ikedam.jenkins.plugins.groovy_label_assignment.GroovyLabelAssignmentProperty' {
					groovyScript 'def labelMap = [ Linux: "PIMBUILDER", Windows: "WINBUILDER", OSX: "OSXQTBUILDER"]; return labelMap.get(binding.getVariables().get("PLATFORM"));'
				}
			}
		} else {
			configure { project ->
				project.name = 'matrix-project'
				project / 'properties' << 'jp.ikedam.jenkins.plugins.groovy_label_assignment.GroovyLabelAssignmentProperty' {
					groovyScript 'def labelMap = [ Linux: "Linux", Windows: "WINBUILDER", OSX: "OSXBUILDER"]; return labelMap.get(binding.getVariables().get("PLATFORM"));'
				}
			}
		}*/
		// throttle jobs TO-DO: make this configurable
		throttleConcurrentBuilds {
			maxPerNode 1
			maxTotal 2 }
	
		// How often to automatically build job 
		triggers {
//			if (cronschedule != null) {
//			cron(cronschedule) 
//			}
			scm('')
			/*ArrayList upstream = json.upstream
			String upstreamall = ""
			String upstreamnew
			upstream.each { currupstream ->
				upstreamnew = currupstream + " " + branch + " " + branchGroup  + ", "
				upstreamall = upstreamall + upstreamnew
			}
			if(json.upstream) {						
				upstream(upstreamall, 'UNSTABLE')
			}*/
		}
		//Wrappers
		wrappers {
			timestamps()
//			preBuildCleanup()
			colorizeOutput()
			environmentVariables {
				env('JENKINS_SLAVE_HOME', '/home/jenkins/scripts')
				env('ASAN_OPTIONS', 'detect_leaks=0')
				env('XDG_CONFIG_DIRS', '/etc/xdg/xdg-plasma:/etc/xdg:/usr/share')
				env('XDG_DATA_DIRS', '/usr:/usr/share')
				env('XDG_DATA_HOME', '${JENKINS_SLAVE_HOME}/.local/share')			
			}  
			      
		}
		// Job SCM support (includes Git.bzr,svn and Redmine)
		// Match by repo address type.
		if (jobname == "mockcpp")	{
			configure createMercurialSCM(repo)
		} else
		if (repo =~ "git:/" || repo =~ "https://" || repo =~ "http://") {
			configure createGitSCM(jobname, "${repo}", "${branch}", redmine)
		} else if (repo =~ "svn:/")	{
			configure createSVNSCM(repo)
		}
		else if (repo =~ "lp:")	{
			configure createBzrSCM(repo)
		} 
		//Run the parent on a LINBUILDER
		configure miscParent()
		//Create matrix for each platform
		configure matrixPlatform(jobplatforms)
		//touchStoneFilter( 'PLATFORM=="Linux"' )
		configure createCombinationFilter(Variation, variationlin, variationwin, variationosx, combinations)	
		configure matrixCompiler(jobcompilers)
		// We only need to create this matrix if the job has variations
		configure matrixVariations(Variation)
		
		blockOnUpstreamProjects()
		
		// Create publishers based on if the file exists.
		publishers {
			if (coberturareport.toBoolean() != false) {
			cobertura('build/CoberturaLcovResults.xml') }
			analysisCollector()
			//set some downstreams to control some build order 
// 			if (bgdownstream != null) {
// 			downstream(bgdownstream.toString(), 'UNSTABLE')
// 			}
			configure createEmailNotifications(jobname, email)			
			configure createIRCNotifications(irc)
			//wsCleanup()
			if (json.downstream) {
				ArrayList downstream = json.downstream
				String downstreamall = ""
				String downstreamnew
				downstream.each { currdownstream ->
					downstreamnew = currdownstream + " " + branch + " " + branchGroup  + ", "
					downstreamall = downstreamall + downstreamnew
				}
				
				downstreamParameterized {
					trigger(downstreamall.toString() - ~/[^,]*$/) {
						condition('UNSTABLE_OR_BETTER')						
						parameters {
							currentBuild()
							triggerWithNoParameters()
						}
					}
				}
			}
		}
		/* We need to check for qt5 jobname and create a pre SCM to run --init-repository for
		 * submodules. I could not find an easy way to accomplish this with the git plugin.
		 */		
		configure createPreSCM(jobname, branchGroup)
		/* Create the actual build steps */
		configure conditionalbyCombinations(combinations, jobcompilers, jobname, branchGroup)
		configure createPublishers(flexpublishers.toBoolean(), jobname, jobcompilers)	
		
	}// branchGroup	
	}catch(Throwable e){}
}
}

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
