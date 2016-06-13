/**
 * 
 */
package org.kde.ci

import java.util.List;
import java.util.Map;

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


class RepoMetaValues {
	def name
	def description
	def hasrepo
	def projectpath
	def repoactive
	def repopath
	def type
	def icon
	def members
		
	public void setPrettyName(String name){
		this.name = name
	}
	public void setDescription(String description){
		this.description = description
	}
	public void setHasRepo(boolean hasrepo){
		this.hasrepo = hasrepo
	}
	public void setPath(String projectpath){
		this.projectpath = projectpath
	}
	public void setrepoactive(boolean repoactive){
		this.repoactive = repoactive
	}
	public void setRepoPath(String repopath){
		this.repopath = repopath
	}
	public void setType(String type){
		this.type = type
	}
	public void setIcon(String icon){
		this.icon = icon ?: "none"
	}
	public void setMembers(List members){
		this.members = members
	}
	RepoMetaValues() {		
	}
	
	def setDescOverride() {
		if (this.description) {
			return this.description
		}
	}
	
	static Closure Variations(Variations) {
		return { project ->
			project.name = 'matrix-project'
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

}
