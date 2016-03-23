package helpers

import java.lang.Integer
import groovy.lang.Closure;
import javaposse.jobdsl.dsl.helpers.*
import hudson.plugins.cobertura.*
import java.lang.Object;

/**
 * @author Scarlett Clark
 * A class to create the DSL for code related publishers.
 */
class PublishersProjectHelper {
	static Closure createPublishers(boolean create, String jobname, ArrayList compilers) {
		if ( create != false ) {
			if (compilers.contains("gcc") && compilers.contains("clang") && compilers.contains("vs2013")) {
				return { project ->							
				// Linux compiler warnings (gcc)					
				project / publishers << 'org.jenkins__ci.plugins.flexible__publish.FlexiblePublisher' {	
					publishers {				
						'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
						condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
							arg1 '${ENV,var="compiler"}'
							arg2 'gcc'
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
									'hudson.plugins.warnings.ConsoleParser' {
										parserName 'Appstreamercli'
									}
									'hudson.plugins.warnings.ConsoleParser' {
										parserName 'GNU C Compiler 4 (gcc)'
										}
									}
								analysisCollector {
									warnings()
									computeNew()
									useStableBuildAsReference()
								}
								}							
							}
						runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
						}																				
													
						// Mac OS X compiler warnings (clang)				
						'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
							condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
								arg1 '${ENV,var="compiler"}'
								arg2 'clang'
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
										'hudson.plugins.warnings.ConsoleParser' {
											parserName 'Appstreamercli'
										}
										'hudson.plugins.warnings.ConsoleParser' {
											parserName 'Clang (LLVM based)'
										}
									}
									analysisCollector {
										warnings()
										computeNew()
										useStableBuildAsReference()
									}
								}							
							}
							runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
						}
						// Windows compiler warnings (vs2013)
						 'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
							 condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
								 arg1 '${ENV,var="compiler"}'
								 arg2 'vs2013'
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
										 'hudson.plugins.warnings.ConsoleParser' {
											 parserName 'Appstreamercli'
										 }
										 'hudson.plugins.warnings.ConsoleParser' {
											 parserName 'MSBuild'
											 }
									 }
									 analysisCollector {
										 warnings()
										 computeNew()
										 useStableBuildAsReference()
									 }
								 }
							 }
							 runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
						 }	
						// JUnit test results
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
					// CppCheck results
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
						}													
						runner(class: 'org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Run')
						executionStrategy(class: "org.jenkins_ci.plugins.flexible_publish.strategy.FailAtEndExecutionStrategy")
					}// end cppcheck
				} // end publishers
			}
			} // end flexible 							
			} else if (compilers.contains("gcc") && compilers.contains("clang") && compilers.contains("vs2013") == false) {
					return { project ->
					// Linux compiler warnings (gcc)
					project / publishers << 'org.jenkins__ci.plugins.flexible__publish.FlexiblePublisher' {
						publishers {
							'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
							condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
								arg1 '${ENV,var="compiler"}'
								arg2 'gcc'
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
										'hudson.plugins.warnings.ConsoleParser' {
											parserName 'Appstreamercli'
										}
										'hudson.plugins.warnings.ConsoleParser' {
											parserName 'GNU C Compiler 4 (gcc)'
											}
										}
									analysisCollector {
										warnings()
										computeNew()
										useStableBuildAsReference()
									}
									}
								}
							runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
							}
														
							// Mac OS X compiler warnings (clang)
							'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
								condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
									arg1 '${ENV,var="compiler"}'
									arg2 'clang'
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
											'hudson.plugins.warnings.ConsoleParser' {
												parserName 'Appstreamercli'
											}
											'hudson.plugins.warnings.ConsoleParser' {
												parserName 'Clang (LLVM based)'
											}
										}
										analysisCollector {
											warnings()
											computeNew()
											useStableBuildAsReference()
										}
									}
								}
								runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
							}						
							// JUnit test results
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
					// CppCheck results
					'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
						condition(class: 'org.jenkins_ci.plugins.run_condition.core.FileExistsCondition') {
							file 'build/cppcheck.xml'
							baseDir(class: 'org.jenkins_ci.plugins.run_condition.common.BaseDirectory\$Workspace')
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
						}													
						runner(class: 'org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Run')
						executionStrategy(class: "org.jenkins_ci.plugins.flexible_publish.strategy.FailAtEndExecutionStrategy")
					}// end cppcheck
				} // end publishers
				} // end flexible
			} // end return
			} else if (compilers.contains("gcc") && compilers.contains("clang") == false) {
			return { project ->
				// Linux compiler warnings (gcc)
				project / publishers << 'org.jenkins__ci.plugins.flexible__publish.FlexiblePublisher' {
					publishers {
						'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
							condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
								arg1 '${ENV,var="compiler"}'
								arg2 'gcc'
								ignoreCase false
							}
							publisherList {
								'hudson.plugins.warnings.WarningsPublisher' {
									canRunOnFailed false
									usePreviousBuildAsReference false
									useStableBuildAsReference true
									useDeltaValues false
									shouldDetectModules false
									dontComputeNew true
									doNotResolveRelativePaths true
									aggregationCondition true
									parserConfigurations {}
									consoleParsers {
										'hudson.plugins.warnings.ConsoleParser' {
											parserName 'Missing Dependencies'
										}
										'hudson.plugins.warnings.ConsoleParser' {
											parserName 'Appstreamercli'
										}
										'hudson.plugins.warnings.ConsoleParser' {
											parserName 'GNU C Compiler 4 (gcc)'
										}
									}
									analysisCollector {
										warnings()
										computeNew()
										useStableBuildAsReference()
									}
								}
							}
							
							runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
						}					
				// JUnit test results
				'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
					condition(class: 'org.jenkins_ci.plugins.run_condition.core.FileExistsCondition') {
						file 'build/JUnitTestResults.xml'
						baseDir(class: 'org.jenkins_ci.plugins.run_condition.common.BaseDirectory\$Workspace')
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
					// CppCheck results
					'org.jenkins__ci.plugins.flexible__publish.ConditionalPublisher' {
						condition(class: 'org.jenkins_ci.plugins.run_condition.core.FileExistsCondition') {
							file 'build/cppcheck.xml'
							baseDir(class: 'org.jenkins_ci.plugins.run_condition.common.BaseDirectory\$Workspace')
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
						}													
						runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Run")
						executionStrategy(class: "org.jenkins_ci.plugins.flexible_publish.strategy.FailAtEndExecutionStrategy")
					}// end cppcheck
				}
			}			
		} 
		} else return { println "No publishers configured" }
	}
	}
}

