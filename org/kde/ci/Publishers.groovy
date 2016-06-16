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

public final class Publishers {
	Publishers() {				
	}

	def genWarningsPublisher(platform, compiler) {
		return { node ->
			  node / 'publishers' / 'org.jenkins__ci.plugins.flexible__publish.FlexiblePublisher' / 'publishers' <<
			  	'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {				  
					  condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
						arg1 '${compiler}'
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
									parserName 'Missing Dependencies'
								}
								if(platform == 'Linux'){
								'hudson.plugins.warnings.ConsoleParser' {
									parserName 'Appstreamercli'
									}
								}
								if (compiler == 'gcc') {
								'hudson.plugins.warnings.ConsoleParser' {
									parserName  'GNU C Compiler 4 (gcc)'
									}
								}
								if (compiler == 'clang') {
								'hudson.plugins.warnings.ConsoleParser' {
									parserName 'Clang (LLVM based)'
									}
								}
								if (compiler == 'vs2015') {
								'hudson.plugins.warnings.ConsoleParser' {
									parserName 'MSBuild'
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
	
	def genCppCheckPublisher() {
		return { node ->
			  node / 'publishers' / 'org.jenkins__ci.plugins.flexible__publish.FlexiblePublisher' / 'publishers' <<
			  	'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {			
				condition(class: 'org.jenkins_ci.plugins.run_condition.core.FileExistsCondition') {
					file 'build/cppcheck.xml'
					baseDir(class: 'org.jenkins_ci.plugins.run_condition.common.BaseDirectory$Workspace')
				}
				publisherList {
					'org.jenkinsci.plugins.cppcheck.CppcheckPublisher' {
						cppcheckConfig {
							pattern 'build/cppcheck.xml'
							ignoreBlankFiles true
							allowNoReport true
							useWorkspaceAsRootPath true
							configSeverityEvaluation {
								severityError true
								severityWarning true
								severityStyle true
								severityPerformance true
								severityInformation true
								severityNoCategory true
								severityPortability true
							}
						}
						configGraph {
							xSize '500'
							ySize '200'
							numBuildsInGraph '1'
							displayAllErrors true
							displayErrorSeverity true
							displayWarningSeverity true
							displayStyleSeverity true
							displayPerformanceSeverity true
							displayInformationSeverity true
							displayNoCategorySeverity true
							displayPortabilitySeverity true
						}							
					}
				}					
				runner(class: 'org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Run')
				executionStrategy(class: "org.jenkins_ci.plugins.flexible_publish.strategy.FailAtEndExecutionStrategy")
			}
		}
				
	}// end cppcheck			
	
	static Closure genCoberturaPublisher() {
		return { node ->
			  node / 'publishers' / 'org.jenkins__ci.plugins.flexible__publish.FlexiblePublisher' / 'publishers' <<
			  	'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {			
					condition(class: 'org.jenkins_ci.plugins.run_condition.core.FileExistsCondition') {
						file 'build/CoberturaLcovResults.xml'
						baseDir(class: 'org.jenkins_ci.plugins.run_condition.common.BaseDirectory\$Workspace')
					}
					publisherList {
						'hudson.plugins.cobertura.CoberturaPublisher' {
							coberturaReportFile 'build/CoberturaLcovResults.xml'
							onlyStable false
							failUnhealthy false
							failUnstable false
							autoUpdateHealth false
							autoUpdateStability false
							zoomCoverageChart false
							maxNumberOfBuilds 10
							failNoReports false
							sourceEncoding 'UTF_8'
							healthyTarget {
								targets('class:"enum-map"' + ' enum-type="hudson.plugins.cobertura.targets.CoverageMetric"') {
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'METHOD'
										'int' 8000000
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'LINE'
										'int' 8000000
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'CONDITIONAL'
										'int' 8000000
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'PACKAGES'
										'int' 7000000
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'FILES'
										'int' 7000000
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'CLASSES'
										'int' 7000000
									}
								}							
							}
							unhealthyTarget {
								targets('class:"enum-map"' + ' enum-type="hudson.plugins.cobertura.targets.CoverageMetric"') {
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'METHOD'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'LINE'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'CONDITIONAL'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'PACKAGES'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'FILES'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'CLASSES'
										'int' 0
									}
								}
							}							
							failingTarget {
								targets('class:"enum-map"' + ' enum-type="hudson.plugins.cobertura.targets.CoverageMetric"') {
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'METHOD'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'LINE'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'CONDITIONAL'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'PACKAGES'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'FILES'
										'int' 0
									}
									entry {
										'hudson.plugins.cobertura.targets.CoverageMetric' 'CLASSES'
										'int' 0
									}
								}
							}							
						}
					}										
					runner(class: 'org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Run')
					executionStrategy(class: "org.jenkins_ci.plugins.flexible_publish.strategy.FailAtEndExecutionStrategy")
				}
			}		
	}// end cobertura
	static Closure genJunitPublisher() {
		return { node ->
			  node / 'publishers' / 'org.jenkins__ci.plugins.flexible__publish.FlexiblePublisher' / 'publishers' <<
			  	'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {			
					condition(class: 'org.jenkins_ci.plugins.run_condition.core.FileExistsCondition') {
						file 'build/JUnitTestResults.xml'
						baseDir(class: 'org.jenkins_ci.plugins.run_condition.common.BaseDirectory$Workspace')
					}
					publisherList {
						'hudson.tasks.junit.JUnitResultArchiver' {
							testResults 'build/JUnitTestResults.xml'
							keepLongStdio true
							healthScaleFactor '1.0'
						}
					}
					runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
				}
			}
	}
	
}
