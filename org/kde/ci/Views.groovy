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
	
	def genListViews(String view, jobsList) {
		return { node ->
			node / 'views' << 'listViews' {			
			name view
			filterExecutors false
			filterQueue false			
/*			jobNames {
				comparator(class:"hudson.util.CaseInsensitiveComparator") {
					jobsList.each {
						if (it != null) { string it }
					}
				}
			}			
			jobFilters {
				status {
					status(Status.ALL)
				}
			}
			columns {
				'hudson.views.StatusColumn'
				'hudson.views.WeatherColumn'
				'hudson.views.JobColumn'
				'hudson.views.LastSuccessColumn'
				'hudson.views.LastFailureColumn'
				'hudson.views.DurationColumn'
				'hudson.views.BuildButtonColumn'				
				'hudson.plugins.UpDownStreamViewColumn'
			}
		}*/
		}
	}
}
