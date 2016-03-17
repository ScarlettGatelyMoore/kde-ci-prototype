/**
 * 
 */
package helpers

import groovy.lang.Closure;

/**
 * @author Scarlett Clark
 * Classes to create the DSL for source control managers
 *
 */
class ProjectSCMHelpers {
	// SVN
	static Closure createSVNSCM(String repo) {
		return { project ->
        project / scm(class: 'hudson.scm.SubversionSCM') {
            locations {
                'hudson.scm.SubversionSCM_-ModuleLocation' {
                    remote repo
                    local '.'
                }
            }
            excludedRegions ''
            includedRegions ''
            excludedUsers ''
            excludedRevprop ''
            excludedCommitMessages ''
            workspaceUpdater(class: "hudson.scm.subversion.UpdateUpdater")
        }
    }	
	}
	// Create the Git DSL
	static Closure createGitSCM(String jobname, String giturl, String branch, String Redmine) {	
		if (Redmine != null ) {			
				return { project ->
					project.name = 'matrix-project'
					project / scm(class: 'hudson.plugins.git.GitSCM') {
						userRemoteConfigs {
							'hudson.plugins.git.UserRemoteConfig' {
								url "${giturl}"
							}
						}
						relativeTargetDir '${WORKSPACE}'						
						branches {
							'hudson.plugins.git.BranchSpec' {
								name "${branch}"
							}
						}
						browser(class: 'hudson.plugins.git.browser.GitWeb') {
							url "${Redmine}"
						}						
						extensions {							
							'hudson.plugins.git.extensions.impl.CloneOption' {
								shallow false
								timeout '20'
							}
						}
					}				
				}							
		} else {
			return { project ->
					project.name = 'matrix-project'
					project / scm(class: 'hudson.plugins.git.GitSCM') {
						userRemoteConfigs {
							 'hudson.plugins.git.UserRemoteConfig' {
								url "${giturl}"
							}
						}
						relativeTargetDir '${WORKSPACE}'
						branches {
							 'hudson.plugins.git.BranchSpec' {
								name "${branch}"
							}
						}						
						extensions {							
							'hudson.plugins.git.extensions.impl.CloneOption' {
								shallow false
								timeout '20'
							}
						}
					}
				}
		}		
	}
	static Closure createBzrSCM(String bzrurl) {	
		return { project ->
		project.name = 'matrix-project'
		project / scm(class: 'hudson.plugins.bazaar.BazaarSCM') {
			source "${bzrurl}"
			cleantree false
			checkout true
			}
		}
	}
	static Closure createMercurialSCM(String mercurl) {
		return { project ->
			project.name = 'matrix-project'
			project / scm(class: 'hudson.plugins.mercurial.MercurialSCM') {
				source "${mercurl}"
				modules ''
				revisionType BRANCH
				revision 'default'
				credentialsId ''
				clean true
                disableChangeLog false
			}
		}
	}
}
