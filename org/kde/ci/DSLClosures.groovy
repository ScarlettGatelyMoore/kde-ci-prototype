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
	static Closure genBuildStep(platform, custom_command) {		
		def shell
		if (platform == "Windows") {
			shell = 'BatchFile'
		} else {
			shell = 'Shell'
		}
		
		def job_command = commandBuilder(platform, custom_command)		
		return { project ->
			project / builders <<
			'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
			   condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
				   arg1 '${ENV,var="PLATFORM"}'
				   arg2 "${platform}"
				   ignoreCase false
			   }
			   runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
			   buildStep(class: 'hudson.tasks.Shell') {
				   command "${job_command}"
		   }
		}
		}
	
	}
	static Closure genWarningsPublisher(platform, compiler) {
	List parselist = genParsers(platform, compiler)
	parselist.each { parser ->
	return	{ project ->
			project / publishers << 'org.jenkins__ci.plugins.flexible__publish.FlexiblePublisher' {
				publishers {
					'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
					condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
						arg1 '${ENV,var="compiler"}'
						arg2 "${compiler}"
						ignoreCase false
					}
					publisherList {
						'hudson.plugins.warnings.WarningsPublisher' {
							canRunOnFailed false
							usePreviousBuildAsReference false
							useStableBuildAsReference false
							useDeltaValues false
							shouldDetectModules false
							dontComputeNew true
							doNotResolveRelativePaths true
							parserConfigurations {}
							consoleParsers {
								'hudson.plugins.warnings.ConsoleParser' {
									parserName {
										parselist.each {
											if (it) { string it }
										}
									}
								}
							}
					}
					analysisCollector {
						warnings()
						computeNew()
						useStableBuildAsReference()
					}						
					runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")			
				
			}
		}
	}
	
	}
	
	static List genParsers(platform, compiler) {
		List parserList = []
		
		parserList.push('Missing Dependencies')						
		if (platform == 'Linux') {
			parserList.push('Appstreamercli')			
		}
		if (compiler == 'gcc') {
			parserList.push('GNU C Compiler 4 (gcc)')
		}
		return parserList
	}				
	static String commandBuilder(platform, custom_command=null, lin_custom_command=null, win_custom_command=null, osx_custom_command=null) {
		def jobcommand = new StringBuilder()
		def home = System.getProperty('user.home')
		if (custom_command) {
			jobcommand.append(custom_command)
		}
		if (platform == 'Linux') {
			jobcommand.append('python3 ' + "${home}" + '/scripts/tools/update-setup-sandbox-local.py\n')
			jobcommand.append('python '+ "${home}" + '/scripts/tools/prepare-environment.py\n')
			jobcommand.append('python '+ "${home}" + '/scripts/tools/perform-build.py')
		}
		
		//TODO other platforms.
		return jobcommand
	}

}
