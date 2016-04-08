/**
 * 
 */
package org.kde.ci

/**
# <Helper to determine SCM data to be used for KDE Continous Integration System>
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
class SCM {
	
	SCM() {		
	}
	
	static Closure generateSCM(scm, branch) {	
		
	def protocol = scm.get('protocol')
	def address = scm.get('address')
	boolean showbrowser = scm.get('browser')	
		switch(protocol) {
		case 'svn':
			return { project ->
				project / scm(class: 'hudson.scm.SubversionSCM') {
					locations {
						'hudson.scm.SubversionSCM_-ModuleLocation' {
							remote address
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
			break
		case 'git':
			return { project ->				
				project / scm(class: 'hudson.plugins.git.GitSCM') {
					userRemoteConfigs {
						'hudson.plugins.git.UserRemoteConfig' {
							url address
						}
					}
					relativeTargetDir '${WORKSPACE}'
					branches {
						'hudson.plugins.git.BranchSpec' {
							name branch
						}
					}
					if (showbrowser)	{
						browser(class: 'hudson.plugins.git.browser.GitWeb') {
							url 'https://quickgit.kde.org/?p=' + jobname + '.git'
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
			break
		case 'lp':
			return { project ->				
				project / scm(class: 'hudson.plugins.bazaar.BazaarSCM') {
					source address
					cleantree false
					checkout true
					}
				}
			break
		case 'hg':
			 return { project ->				
				project / scm(class: 'hudson.plugins.mercurial.MercurialSCM') {
					source address
					modules ''
					revisionType BRANCH
					revision 'default'
					credentialsId ''
					clean true
					disableChangeLog false
				}
			}
			break
		case 'tar':
			return { project ->				
				buildStep(class: 'hudson.tasks.' + "${this.shell}") {
					command "wget " + address + " \n" \
							+ "tar --strip-components=1 -xf PyQt-x11-gpl-4.11.3.tar.gz \n" \
							+ "rm PyQt-x11-gpl-4.11.3.tar.gz"
				}
			}
			break
		default:
			return { project ->				
				project / scm(class: 'hudson.plugins.git.GitSCM') {
					userRemoteConfigs {
						'hudson.plugins.git.UserRemoteConfig' {
							url address
						}
					}
					relativeTargetDir '${WORKSPACE}'
					branches {
						'hudson.plugins.git.BranchSpec' {
							name branch
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
			break
		}
	}
}
