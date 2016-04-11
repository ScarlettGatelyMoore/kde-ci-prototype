/**
# <DSL for KDE Continuous Integration System>
# Copyright 2016  Scarlett Clark <sgclark@kde.org>
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License as
# published by the Free Software Foundation; either version 2 of
# the License or (at your option) version 3 or any later version
# accepted by the membership of KDE e.V. (or its successor approved
# by the membership of KDE e.V.), which shall act as a proxy
# defined in Section 14 of version 3 of the license.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
#
*/

import org.kde.ci.*

import groovy.io.FileType

import org.yaml.snakeyaml.Yaml



// Initialize config class
def configs = new ImportConfig()
// Setup repo-metadata (https://anongit.kde.org/sysadmin/repo-metadata) Repo is updated via update-setup.py
Map repoConfig
def repobasePath = System.getProperty('user.home') + '/scripts/repometadata/projects/'
def repoDataFile = configs.genListOfFilesinDir(repobasePath)
repoDataFile.each { file ->	
	def project
	def projectpart = file.toString().minus(repobasePath).minus('/metadata.yaml').replaceAll('/', ' ')
	
	def findlast = (projectpart =~ /\s\w$/)
	project = findlast.getAt(0)
	def projrepoyaml = configs.getConfig(file)
	RepoMetaValues rd = RepoMetaValues.newInstance(projrepoyaml)
	println project
	println rd.projectpath
}

println repoConfig

// Begin with the base defaults yaml that get retrieved via update-setup
def basePath = System.getProperty('user.home') + '/scripts/metadata/'
def GroupFile = []
def allJobsList = []
def CurrentView
def CurrentViewJobs = []

def fileList = configs.genListOfFilesinDir(basePath)
//def configFiles = new File(basePath).eachFileMatch(FileType.FILES, ~/.*.yml/) {	GroupFile << it.name }
fileList.each { file ->
	if(file =~ '.yml'){
		metadataConfig = file
		defaultyamldata = new ImportConfig().getConfig(metadataConfig)
	}
}
// Now lets get the repo-metadata and bring in any overrides
//def rout = new StringBuilder(), rerr = new StringBuilder()
//def getFile = 'git archive --remote=git://anongit.kde.org/sysadmin/repo-metadata.git HEAD:path/to/directory filename | tar -x'
// Get repo-metadata


path = repometa.projectpath


println(GroupFile.toString())

assert GroupFile == ['qt5.yml', 'frameworks.yml', 'kdesupport.yml']


GroupFile.each { group ->	
	println(group)
	CurrentViewJobs = []
	// Get the Yaml data into a usable object
	def defaultyamldata = new ImportConfig().getConfig(basePath, group)	
	groupName = group - '.yml'	
	// Now for each project data Map we feed that in a current Project Object Class	
	defaultyamldata.each { jobkey, curr_project ->		
		def jobname = jobkey
		allJobsList << jobname				
		Project job = Project.newInstance(curr_project)
		//debug only
		assert job.group_name == groupName
		println "Processing group: " + groupName
		def path = groupName + '/'	+ jobname
		// get repo-metadata for all except the default project		
		if (jobname != 'project') {		
		// Lets start with.. Are we active?
		if(job.getActive()) {	
			assert job.getActive() == true
			// Bring in development tracks to determine branches.
			Map tracks = job.SetRepoMap()
			println job.repositories
			// We have branchGroups that split into sections for releases/development 
			// We need to process a new jobset for each of these groups.
			Map bg = job.getBranchGrouptracks()
			//Now we determine which track this branchGroup wishes to use. Which will determine the branch.
			bg.each { branchGroup , track  -> 
				Map branches = tracks.getAt(path)
				def branch = branches.get(track)				
				// Process each platform
				Map pf = job.SetPlatformMap()	
				pf.each { PLATFORM , options ->																	
						Platform platform = new Platform()
						def compiler = platform.genCompilers(options)
						//Bring in our DSL Closure generation classes
						DSLClosures misc = new DSLClosures()
						Publishers pub = new Publishers()
						//Publishers can be disabled in the yaml config files
						def gen_publishers = job.gen_publishers ?: true		
						
						def variations = platform.PlatformVariations(options)
						def variationClosure
						def compilerClosure
						if (variations) {
							variationClosure = misc.Variations(variations)
						}
						if (compiler.getClass() == List) {
							compilerClosure = misc.Compilers(compiler)
						}
						def job_command = job.custom_build_command
						// We only want matrix jobs for variations, multiple compilers, requested. They are annoying with reports.
						def jobType = platform.determineJobType(variations, compiler)
						boolean currtrack = misc.genBuildTrack(options, track)
						def fullname = job.SetProjectFullName(jobname, branchGroup, track, branch, PLATFORM, compiler)
						CurrentView = job.view
						CurrentViewJobs << fullname
						// If the current track is enabled for this platform generate job						
						if (currtrack) {
							println "Processing Project " + jobname + " " + branchGroup + " Track " + track + " Branch " + branch + " platform " + PLATFORM \
							+ " compiler " + compiler
							
							if ( repometa.hasrepo && repometa.repoactive ) {
								SCM scm = new SCM()									
								scmClosure = scm.generateSCM(jobname, job.SetRepoMap(), branch)
									
								/* BEGIN DSL CODE */
								"${jobType}"(fullname) {
									configure { project ->
										project / 'actions' {}				
									}	
									// token for api		
									configure misc.SetToken(jobname)
									// Job description
									description job.DefineDescription(repometa.name, repometa.description)
									// Set the log history
									logRotator(job.getLogrotator())
									// Setting this to false, I have never seen it set to true in the last year. Not even sure why we have it...
									configure { project ->
										project / 'properties' / 'org.jenkins.ci.plugins.html5__notifier.JobPropertyImpl' {
											skip false
										}
									}
									// Jenkins likes to get creative with workspaces, especially with matrix jobs. Putting in sane place.
									customWorkspace(System.getProperty('user.home') + '/sources/' + "${branchGroup}" + '/' + "${jobname}")
									
									// Make sure qt4 builds are using trusty containers
									if (branchGroup =~ "qt4") {
										configure { project ->									
											project / 'properties' << 'jp.ikedam.jenkins.plugins.groovy_label_assignment.GroovyLabelAssignmentProperty' {
												groovyScript 'def labelMap = [ Linux: "QT4"]; return labelMap.get(binding.getVariables().get("PLATFORM"));'
											}
										}
									} else if (branchGroup =~ "kf5-minimum") {
										configure { project ->								
											project / 'properties' << 'jp.ikedam.jenkins.plugins.groovy_label_assignment.GroovyLabelAssignmentProperty' {
												groovyScript 'def labelMap = [ Linux: "MINIMUM"]; return labelMap.get(binding.getVariables().get("PLATFORM"));'
										}
									}
									} else {
										configure { project ->				
											project / 'properties' << 'jp.ikedam.jenkins.plugins.groovy_label_assignment.GroovyLabelAssignmentProperty' {
												groovyScript 'def labelMap = [ Linux: "Linux", Windows: "WINBUILDER", OSX: "OSXBUILDER"]; return labelMap.get(binding.getVariables().get("PLATFORM"));'
										}	
									}
									}
									wrappers {
										timestamps()
										colorizeOutput()
										environmentVariables {
											env('JENKINS_SLAVE_HOME', '/home/jenkins/scripts')
											env('JENKINS_TEST_HOME', '/home/jenkins')
											env('ASAN_OPTIONS', 'detect_leaks=0')
											env('XDG_CONFIG_DIRS', '/etc/xdg/xdg-plasma:/etc/xdg:/usr/share/:${JENKINS_TEST_HOME}/.qttest/config')
											env('XDG_DATA_DIRS', '/usr:/usr/share:${JENKINS_TEST_HOME}/.local/share')
											env('XDG_DATA_HOME', '$XDG_DATA_HOME:${JENKINS_TEST_HOME}/.qttest/share:${JENKINS_TEST_HOME}/.local/share')
											env('XDG_RUNTIME_DIR', '/tmp/xdg-runtime-dir')
											env('XDG_CACHE_HOME', '${JENKINS_TEST_HOME}/.qttest/cache')
										}
											  
									}
									blockOnUpstreamProjects()
									configure scmClosure
									configure misc.genBuildStep(PLATFORM, job_command)	
									if(gen_publishers != false) {
										configure pub.genWarningsPublisher(PLATFORM, compiler)	
										configure pub.genCppCheckPublisher()
										configure pub.genCoberturaPublisher()
										configure pub.genJunitPublisher()
									}
										
									if (jobType == 'matrixJob' ) {
										childCustomWorkspace(".")
										configure variationClosure
										configure compilerClosure
										label('master')
									}
									
								}// END DSL
							} else { "Repo status: " + repometa.repoactive + "Has Repo? " + repometa.hasrepo }
							// End repo / Failed repo check
							} else { // end current job track	
								println "${jobname} does not have track: ${track} configured for ${PLATFORM}"
								return	
							} 	
					} // End current platform 
				} // End branchGroup 
		} else {
			println "No job creation due to ${jobname} Active status: " + job.getActive()
			return // end job
		}	// End Active
	} } // End current project	

	listView(CurrentView) {	
		description 'All jobs for group: ' + "${CurrentView}"	
		filterExecutors false
		filterBuildQueue false
		jobs {	
			CurrentViewJobs.each { job ->
				names(job)
			}		
		}
		jobFilters {			
		}
		//statusFilter(StatusFilter.ENABLED)
		columns {
			status()
			weather()
			name()
			lastSuccess()
			lastFailure()
			lastDuration()
			buildButton()
			//'hudson.plugins.UpDownStreamViewColumn'
		}
	}
	def branchGroups = ['kf5-qt5', 'stable-kf5-qt5', 'kf5-minimum', 'kf5-qt5-patch', 'qt4-stable']
	branchGroups.each { bg ->
		listView(bg) {
			description 'All jobs for branchGroup: ' + bg
			filterExecutors false
			filterBuildQueue false
			jobs {
				regex(".* " + bg.toString() + ".+")				
			}
			jobFilters {}
			statusFilter(StatusFilter.ENABLED)
			columns {
				status()
				weather()
				name()
				lastSuccess()
				lastFailure()
				lastDuration()
				buildButton()
			'hudson.plugins.UpDownStreamViewColumn'
			}
		}
	}
	//Views view = new Views()
	
	/*listView(CurrentView) {
		configure view.genListViews(CurrentView, CurrentViewJobs)
	}*/
	//view.genBGRegexListViews()
} // End group	

userContent('kde.css', streamFileFromWorkspace('css/kde.css'))




