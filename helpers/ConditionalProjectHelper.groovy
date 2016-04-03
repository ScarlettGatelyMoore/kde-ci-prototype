
package helpers

import javaposse.jobdsl.dsl.helpers.*
import groovy.lang.Closure;

/**
 * @author Scarlett Clark
 * This file is a helper to determine variables needed during the creation
 * of conditional builders.
 */
class ConditionalProjectHelper {
	static Closure conditionalbyCombinations(String combination, ArrayList compilers, String jobname, String branchGroup) {
	try{	
		if (combination == "default") {
		return { project ->
			project / builders <<
			'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
			   condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
				   arg1 '${ENV,var="PLATFORM"}'
				   arg2 'Linux'
				   ignoreCase false
			   }
			   runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
			   buildStep(class: 'hudson.tasks.Shell') {
				   command commandBuilder(jobname, 'Linux', compilers, jobname, branchGroup)
		   }
		}
			project / builders <<
			'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
				condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
					arg1 '${ENV,var="PLATFORM"}'
					arg2 'Windows'
					ignoreCase false
				}
				runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Unstable")
				buildStep(class: 'hudson.tasks.BatchFile') {
					command commandBuilder(jobname, 'Windows', compilers, jobname, branchGroup)
			}
		 }
			project / builders <<
			'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
				condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
					arg1 '${ENV,var="PLATFORM"}'
					arg2 'OSX'
					ignoreCase false
				}
				runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Unstable")
				buildStep(class: 'hudson.tasks.Shell') {
					command commandBuilder(jobname, 'OSX', compilers, jobname, branchGroup)
				}
			}
		}
		} else if (combination == "LinandOSX") {
		return { project ->
			project / builders <<
			'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
			   condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
				   arg1 '${ENV,var="PLATFORM"}'
				   arg2 'Linux'
				   ignoreCase false
			   }
			   runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
			   buildStep(class: 'hudson.tasks.Shell') {
				   command commandBuilder(jobname, 'Linux', compilers, jobname, branchGroup)
		   }
		}
			project / builders <<
			'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
				condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
					arg1 '${ENV,var="PLATFORM"}'
					arg2 'OSX'
					ignoreCase false
				}
				runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Unstable")
				buildStep(class: 'hudson.tasks.Shell') {
					command commandBuilder(jobname, 'OSX', compilers, jobname, branchGroup)
				}
			}
		}
		} else if (combination == "LinandWin") {
			return { project ->
					project / builders <<
					'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
						condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
							arg1 '${ENV,var="PLATFORM"}'
							arg2 'Linux'
							ignoreCase false
						}
						runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
						buildStep(class: 'hudson.tasks.Shell') {
							command commandBuilder(jobname, 'Linux', compilers, jobname, branchGroup)
						}
					}
					project / builders <<
					'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
						condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
							arg1 '${ENV,var="PLATFORM"}'
							arg2 'Windows'
							ignoreCase false
						}
						runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Unstable")
						buildStep(class: 'hudson.tasks.BatchFile') {
							command commandBuilder(jobname, 'Windows', compilers, jobname, branchGroup)
						}
					}
				}
		} else if (combination == "Linux") {
			return { project ->
				project / builders <<
				'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
				   condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
					   arg1 '${ENV,var="PLATFORM"}'
					   arg2 'Linux'
					   ignoreCase false
				   }
				   runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
				   buildStep(class: 'hudson.tasks.Shell') {
					   command commandBuilder(jobname, 'Linux', compilers, jobname, branchGroup)
				   }
				}
			}
		} else if (combination == "Win") {
		return { project ->
			project / builders <<
					'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
						condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
							arg1 '${ENV,var="PLATFORM"}'
							arg2 'Windows'
							ignoreCase false
						}
						runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Unstable")
						buildStep(class: 'hudson.tasks.BatchFile') {
							command commandBuilder(jobname, 'Windows', compilers, jobname, branchGroup)
						}
					}
			}
		} else if (combination == "OSX") {
			return { project ->
				project / builders <<
				'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
					condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
						arg1 '${ENV,var="PLATFORM"}'
						arg2 'OSX'
						ignoreCase false
					}
					runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Unstable")
					buildStep(class: 'hudson.tasks.Shell') {
						command commandBuilder(jobname, 'OSX', compilers, jobname, branchGroup)
					}
				}
			}
		} else return {}
		}catch(Throwable e){}
	}
	static Closure createPreSCM(String jobname, String branchGroup) {
		try{ 		
		// We need to put those job here so our prepare-environment scripts can handle them until the bugs are fixed.
		if (jobname == 'pyqt4') {
				return { project ->
					project / buildWrappers << 'org.jenkinsci.plugins.preSCMbuildstep.PreSCMBuildStepsWrapper' {
							buildSteps {
							'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
								condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
									arg1 '${ENV,var="PLATFORM"}'
									arg2 'Linux'
									ignoreCase false
							}
								runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
								buildStep(class: 'hudson.tasks.Shell') {
									command 'wget http://iweb.dl.sourceforge.net/project/pyqt/PyQt4/PyQt-4.11.3/PyQt-x11-gpl-4.11.3.tar.gz \n' +
									'tar --strip-components=1 -xf PyQt-x11-gpl-4.11.3.tar.gz \n' +
									'rm PyQt-x11-gpl-4.11.3.tar.gz'
								}
							}
						}
					}
				} 		
				} else if (jobname == 'opencv') {				
					return { project ->
						project / buildWrappers << 'org.jenkinsci.plugins.preSCMbuildstep.PreSCMBuildStepsWrapper' {
								buildSteps {
								'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
									condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
										arg1 '${ENV,var="PLATFORM"}'
										arg2 'Linux'
										ignoreCase false
								}
									runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
									buildStep(class: 'hudson.tasks.Shell') {
										command 'git clone https://github.com/Itseez/opencv_contrib.git ${WORKSPACE}/modules'
									}
								}
							}	
						}
					} 
				} else if (jobname == 'qt4') {
					return { project ->
						project / buildWrappers << 'org.jenkinsci.plugins.preSCMbuildstep.PreSCMBuildStepsWrapper' {
								buildSteps {
								'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
									condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
										arg1 '${ENV,var="PLATFORM"}'
										arg2 'Linux'
										ignoreCase false
								}
									runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
									buildStep(class: 'hudson.tasks.Shell') {
										command 'python ${JENKINS_SLAVE_HOME}/tools/prepare-environment.py --project ' + "${jobname}" + ' --branchGroup ' + "${branchGroup}" + ' --compiler ' + 'gcc' + ' --platform ' + 'linux64-g++' + ' --sources ' + '${WORKSPACE}'
									}
								}
							}
						}
					}
					} else {	return {}
			}}catch(Throwable e){}
		}
	static String commandBuilder(String jobname, String platform, ArrayList compiler, String projectname, String branchGroup) {
		/* This section defines the command for each platform/compiler. NOTE: Possibly better in a config script */
		def buildcommand
		def jobcustomcommand = ""
		/*
		 *  Default base commands 
		 *  Linux + gcc
		 *  Windows + vs2013
		 *  OSX + clang	
		 *  jobcustomcommand is to be used for job specific platform additions eg. platform needed export
		 */
		def windefaultbasebuildcommand = 'c:/python27/python.exe %JENKINS_SLAVE_HOME%/tools/perform-build.py ' + '--project ' + "${projectname}" + ' --branchGroup ' + "${branchGroup}" + ' --compiler ' + 'vs2013' + ' --platform ' + 'windows64-vs2013' + ' --sources ' + '%WORKSPACE%'
		def lindefaultbasebuildcommand = 'python ${JENKINS_SLAVE_HOME}/tools/update-setup.py \n' \
						 + 'python -u ${JENKINS_SLAVE_HOME}/tools/perform-build.py ' + '--project ' + "${projectname}" + ' --branchGroup ' + "${branchGroup}"	+ ' --compiler ' + 'gcc' + ' --platform ' + 'linux64-g++' + ' --sources ' + '${WORKSPACE}'									
		def osxdefaultbasebuildcommand = 'launchctl unload -w /Library/LaunchAgents/org.freedesktop.dbus-session.plist \n' \
		                                 + 'launchctl load -w /Library/LaunchAgents/org.freedesktop.dbus-session.plist \n' \
                                         + 'launchctl getenv DBUS_LAUNCHD_SESSION_BUS_SOCKET \n' \
                                         + 'python2.7 -u ${JENKINS_SLAVE_HOME}/tools/perform-build.py ' + '--project ' + "${projectname}" + ' --branchGroup ' + "${branchGroup}" + ' --compiler ' + 'clang' + ' --platform ' + 'darwin-mavericks' + ' --sources ' + '${WORKSPACE}'	
		// Build the buildcommand				
		if (platform == "Windows" && compiler.contains('vs2013')){			
			buildcommand = jobcustomcommand + windefaultbasebuildcommand
		} else if (platform == "Linux" && compiler.contains('gcc')){
			if (jobname == "libaccounts-glib") {
				jobcustomcommand = 'export HAVE_GCOV_FALSE=\'#\' \n'
			} else if (jobname == 'qt5') {
				jobcustomcommand = 'perl init-repository -f -q \n'
			} /*else if (jobname == 'libhybris') {
				jobcustomcommand = '${WORKSPACE}/hybris/utils/extract-headers.sh /usr/include/android . \n'
			}else if (jobname == 'qoauth') {
				jobcustomcommand = 'qmake \n' \
				+ 'make \n' \
				+ 'export instPrefix="/srv/jenkins/install/ubuntu/x86_64/g++/kf5-qt5/general/qoauth/inst" \n' \
				+ 'make INSTALL_ROOT=${instPrefix} install'
			} */		
			buildcommand = jobcustomcommand + lindefaultbasebuildcommand
		} else if (platform == "OSX" && compiler.contains('clang')){
			if (jobname == "qt5") {
				jobcustomcommand = 'perl init-repository -f -q \n' + 'export PATH=/opt/local/lib/mysql55/bin:$PATH' + '\n' \
				+ 'export CFLAGS="-isystem/opt/local/include" \n' + 'export CXXFLAGS="-isystem/opt/local/include" \n'
			/*} else if (jobname == "libaccounts-glib") {
				jobcustomcommand = 'export HAVE_GCOV_FALSE=\'#\' \n'*/
			} else if (jobname == 'qwt') {
                                jobcustomcommand = 'python -u ${JENKINS_SLAVE_HOME}/tools/perform-build.py ' + '--project ' + "${projectname}" + ' --branchGroup ' + "${branchGroup}"   + ' --compiler ' + 'gcc' + ' --platform ' + 'linux64-g++' + ' --sources ' + '${WORKSPACE}/qwt'
                                buildcommand = jobcustomcommand
                        }
			buildcommand = jobcustomcommand + osxdefaultbasebuildcommand
		}
		return buildcommand
	}
}
	
class ConditionInvalidException extends Exception {
		public ConditionInvalidException () {
		
			}
		
			public ConditionInvalidException (String message) {
				super (message);
			}
		
			public ConditionInvalidException (Throwable cause) {
				super (cause);
			}
	
		public ConditionInvalidException (String message, Throwable cause) {
			super (message, cause);
		}
		
}

//No mingw right now, here for later.	
/*else if (platform == "Windows" && compiler.contains('mingw')) {
 buildcommand = 'rename c:\\Git\\bin\\sh.exe sh1 \n' + 'rename c:\\cygwin64\\bin\\sh.exe sh1 \n' +
	'python %JENKINS_SLAVE_HOME%/tools/perform-build.py ' + '--project ' + "${projectname}" + ' --branchGroup ' + "${branchGroup}" + ' --compiler ' + 'mingw' + ' --platform ' + "windows64" + ' --sources ' + '${WORKSPACE}'
} */
