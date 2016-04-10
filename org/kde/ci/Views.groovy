/**
 * 
 */
package org.kde.ci

/**
# <DSL Views Class for KDE Continous Integration System>
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
class Views {
	
	Views() {		
	}
	
	static Closure genListViews(String view, jobsList) {
		return { views ->
			views << listView(view) {	
				description 'All jobs for group: ' + "${view}"	
				filterExecutors false
				filterBuildQueue false
				jobs {	
					jobsList.each { job ->
						names(job)
					}		
				}
				jobFilters {			
				}
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
	}
	static Closure genBGRegexListViews() {
		def branchGroups = ['kf5-qt5', 'stable-kf5-qt5', 'kf5-minimum', 'kf5-qt5-patch', 'qt4-stable']
		branchGroups.each { bg ->
			return { views ->
				views << listView(bg) {
					description 'All jobs for branchGgroup: ' + "${bg}"
					filterExecutors false
					filterBuildQueue false
					jobs {
						jobsList.each { job ->
							regex(bg)
						}
					}
					jobFilters {
					}
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
		}
	}
}
	
