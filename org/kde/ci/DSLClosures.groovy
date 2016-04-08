/**
 * 
 */
package org.kde.ci

import groovy.lang.Closure;

/**
# <Collection of DSL Closures to be used for KDE Continous Integration System>
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


class DSLClosures {	
		
	DSLClosures() {		
	}
	
	static Closure SetToken(jobname) {
		def tokenid = "PNcTKQORJW653QKVTwL0GV64OZA-${jobname}"
		return { project ->
			project << authToken( "${tokenid}")
		}
	}
	
	static Closure Variations(Variations) {
		return { project ->			
			project / axes << 'hudson.matrix.TextAxis' {
				name 'Variation'
				values {
					Variations.each {
						if (it) { string it }
					}
				}
			}
		}
	}
	static Closure DownstreamTriggers(downstream, branchGroup, track, branch, platform, compiler) {
		String downstreamall = ""
		String downstreamnew
		downstream.each { currdownstream ->
			downstreamnew = currdownstream + " " + branchGroup + " " + track  + " " + branch + " " + platform + " " + compiler
			downstreamall = downstreamall + downstreamnew
		}
		
		downstreamParameterized {
			trigger(downstreamall.toString() - ~/[^,]*$/) {
				condition('UNSTABLE_OR_BETTER')
				parameters {
					currentBuild()					
					onlyIfSCMChanges()
				}
			}
		}
				
	}
	def genBuildStep(platform) {		
		def shell
		if (platform == "Windows") {
			shell = 'BatchFile'
		} else {
			shell = 'Shell'
		}
		return { project ->
				project / builders <<
				'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
					condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
						arg1 '${ENV,var="PLATFORM"}'
						arg2 "${platform}"
						ignoreCase false
					}
					runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
					buildStep(class: 'hudson.tasks.' + "${shell}") {
						command commandBuilder()
					}
				}			
			}
	}
	def commandBuilder() {
		def home = System.getProperty('user.home')
		return 'python3 '+ "${home}" + '/scripts/tools/update-setup-sandbox-local.py' + "\n" + \
			   'python '+ "${home}" + '/scripts/tools/prepare-environment.py' + "\n" + \
			   'python '+ "${home}" + '/scripts/tools/perform-build.py'
	}

}
