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
	def genBuildTrack(options, track) {
		def tracks = options.find { key, value -> key == 'tracks' }
			 if( tracks.getValue().toString().contains(track) ) {  return true }
			 else { return false }
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
	static Closure Compilers(compilers) {
		return { project ->			
			project / axes / 'hudson.matrix.TextAxis' {
				name 'compiler'
				values {
					 compilers.each {
						 if (it != null) { string it }
					}
				}
			}
		}
	}
	static Closure OptionalPlatformsMatrix(platforms) {
		return { project ->			
			project / axes / 'hudson.matrix.TextAxis' {
				name 'PLATFORM'
				values {
					platforms.each {
						if (it != null) { string it }
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
	static Closure genBuildStep(jobType, jobname, platform, custom_command=null, lin_custom_command=null, win_custom_command=null, osx_custom_command=null, android_job_command=null, snappy_job_command=null) {		
		def shell
		if (platform == "Windows") {
			shell = 'BatchFile'
		} else {
			shell = 'Shell'
		}
		
		def job_command = commandBuilder(jobname, platform, custom_command=null, lin_custom_command=null, win_custom_command=null, osx_custom_command=null, android_job_command=null, snappy_job_command=null)
		if (jobType == 'matrixJob' ) {
			return { project ->
				project / builders <<
				'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
					condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
						arg1 '${ENV,var="PLATFORM"}'
						arg2 "${platform}"
						ignoreCase false
					}
					runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
					buildStep(class: "hudson.tasks.${shell}") {
						command "${job_command}"
					}	
				}
			} 
		} else {
			return { project ->
				project / builders <<
				buildStep(class: "hudson.tasks.${shell}") {
					command "${job_command}"
				}
			}
		}	
	
	}
		
	static String commandBuilder(jobname, platform, custom_command=null, lin_custom_command=null, win_custom_command=null, osx_custom_command=null, android_job_command=null, snappy_job_command=null) {
		def jobcommand = new StringBuilder()
		if (custom_command) {
			jobcommand.append(custom_command + '\n')
		}
		switch(platform) {
			case 'Linux':
				if (lin_custom_command) {
					jobcommand.append(lin_custom_command)
				} else {
				    jobcommand.append('git clone git://anongit.kde.org/sysadmin/ci-tools-experimental.git ' + '${JENKINS_SLAVE_HOME}/scripts\n')
					jobcommand.append('python3 ' + '${JENKINS_SLAVE_HOME}/scripts/tools/update-setup-sandbox.py\n')
					jobcommand.append('python3 ' + '${JENKINS_SLAVE_HOME}/scripts/tools/perform-build.py')
				}
					return jobcommand
				break
		    case 'Windows':
				if (win_custom_command) {
					jobcommand.append(win_custom_command)
				} else {
					jobcommand.append('python3 ' + '${JENKINS_SLAVE_HOME}/scripts/tools/update-setup-sandbox.py\n')
					jobcommand.append('emerge --install-deps' + jobname)
					jobcommand.append('emerge ' + jobname)
				}
					return jobcommand
				break
			case 'OSX':
				if (osx_custom_command) {
					jobcommand.append(osx_custom_command)
				} else {
					jobcommand.append('python3.5 -u ${JENKINS_SLAVE_HOME}/tools/perform-build.py')
				}
				 	return jobcommand
				break
			case 'android':
				if (android_job_command) {
					jobcommand.append(android_job_command)
				} else {
				    jobcommand.append('git clone git://anongit.kde.org/sysadmin/ci-tools-experimental.git ' + '${JENKINS_SLAVE_HOME}/scripts\n')
					jobcommand.append('python3 ' + '${JENKINS_SLAVE_HOME}/scripts/tools/update-setup-sandbox.py\n')
					jobcommand.append('python3 ' + '${JENKINS_SLAVE_HOME}/scripts/tools/perform-build.py')
				}
					 return jobcommand
				break
			case 'snappy':
				if(snappy_job_command) {
					jobcommand.append(snappy_job_command)
				} else {
				    jobcommand.append('git clone git://anongit.kde.org/sysadmin/ci-tools-experimental.git ' + '${JENKINS_SLAVE_HOME}/scripts\n')
					jobcommand.append('snapcraft ' + jobname)
				}
					return jobcommand
				break
			default:
			    jobcommand.append('git clone git://anongit.kde.org/sysadmin/ci-tools-experimental.git ' + '${JENKINS_SLAVE_HOME}/scripts\n')
			 	jobcommand.append('python3 ' + '${JENKINS_SLAVE_HOME}/scripts/tools/update-setup-sandbox.py\n')
				jobcommand.append('python3 ' + '${JENKINS_SLAVE_HOME}/scripts/tools/perform-build.py')			
					return jobcommand
			 	break
		}		
	}

}
