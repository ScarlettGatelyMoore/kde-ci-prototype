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

def basePath = System.getProperty('user.home') + '/scripts/metadata/'
def GroupFile = []
def configFiles = new File(basePath).eachFileMatch(FileType.FILES, ~/.*.yml/) {	GroupFile << it.name }


println(GroupFile.toString())
assert GroupFile == ['qt.yml', 'frameworks.yml']


GroupFile.each { group ->	
	println(group)
	// Get the Yaml data into a usable object
	def yamldata = new ImportConfig().getConfig(basePath, group)	
	
	// Now for each project data Map we feed that in a current Project Object Class	
	yamldata.each { jobkey, curr_project ->
		def jobname = jobkey				
		Project job = Project.newInstance(curr_project)
		//debug only
		println("Processing " + jobname + " Value Dump: " + curr_project.toString() + "\n")	
		
		// Lets start with.. Are we active?
		if(job.getActive()) {	
			// Bring in development tracks to determine branches.
			Map tracks = job.getBranch()		
			// We have branchGroups that split into sections for releases/development 
			// We need to process a new jobset for each of these groups.
			if (jobname != 'project') {
				Map bg = job.getBranchGrouptracks()
				//Now we determine which track this branchGroup wishes to use. Which will determine the branch.
				bg.each { branchGroup , track  -> 
					def branch = tracks.get(track)
					println "Processing Project " + jobname + " " + branchGroup + " Track " + track + " Branch " + branch
 			//Bring in our DSL Closure generation classes	
			DSLMisc misc = new DSLMisc()
			SCM scm = new SCM()			
			/* BEGIN DSL CODE */
		
			matrixJob(job.SetProjectFullName(jobname, branchGroup, track, branch)) {
			configure { project ->
				project / 'actions' {}				
			}	
			// token for api		
			configure misc.SetToken(jobname)
			// Job description
			description job.DefineDescription()
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
			childCustomWorkspace(".")
			// Make sure qt4 builds are using trusty containers
			if (branchGroup =~ "qt4") {
				configure { project ->
					project.name = 'matrix-project'
					project / 'properties' << 'jp.ikedam.jenkins.plugins.groovy_label_assignment.GroovyLabelAssignmentProperty' {
					groovyScript 'def labelMap = [ Linux: "QT4"]; return labelMap.get(binding.getVariables().get("PLATFORM"));'
					}
				}
			}
			}
			}
		}			
	}
}	
}
