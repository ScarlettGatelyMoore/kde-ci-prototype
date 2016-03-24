/**
 * 
 */
package org.kde.ci

/**
# <DSL Project Class for KDE Continous Integration System>
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

import static java.lang.String.format;

public final class Project {
	// Brought in via yaml config
	String project_name
	String group_name
	Map branchGrouptracks
	Map branch
	Map platforms
	List products_needed
	Map repositories
	List dependencies
	List excluded_repositories
	String view
	String description
	String email
	String irc
	List upstream
	List downstream
	String logrotator
	Integer priority
	Boolean active
	Boolean webview
	
	// Internal
	List viewjobslist
	String full_jobname
		
	public void setProjectName(String project_name){
		this.project_name = project_name
	}
	public void setGroupName(String group_name){
		this.group_name = group_name
	}
	public void setBranchGroupTracks(Map branchGrouptracks){
		this.branchGrouptracks = branchGrouptracks
	}
	public void setBranch(Map branch){
		this.branch = branch
	}
	public void setPlatforms(Map platforms){
		this.platforms = platforms
	}
	public void setProductsNeeded(List products_needed){
		this.products_needed = products_needed
	}
	public void setRepositories(Map repositories){
		this.repositories = repositories
	}
	public void setDependencies(List dependencies){
		this.dependencies = dependencies
	}
	public void setExcludedRepositories(List excluded_repositories){
		this.excluded_repositories = excluded_repositories
	}
	public void setView(String view){
		this.view = view
	}
	public void setDescription(String description){
		this.description = description
	}
	public void setEmail(String email){
		this.email = email
	}
	public void setIRC(String irc){
		this.irc = irc
	}
	public void setLogrotator(String logrotator){
		this.logrotator = logrotator
	}
	public void setUpstream(List upstream){
		this.upstream = upstream
	}
	public void setDownstream(List downstream){
		this.downstream = downstream
	}
	public void setPriority(Integer priority){
		this.priority = priority
	}
	public void setActive(Boolean active){
		this.active = active
	}
	public void setBrowser(Boolean webview){
		this.browser = webview
	}	
	
	Project() {		
	}
	
	def Boolean IsActive() {
		if (this.active) { return true }
	}
	
	def AssignPriority() {
		//#STUB
	}
	
	def List NotificationData() {
		List data = []
		data.push(this.email)
		data.push(this.irc)
		return data		
	}
	
	def String DefineDescription() {				
		return this.full_jobname + "\n\n" + this.description		
	}	
	
	def Map getBranchGroupTracks() {
		return this.branchGrouptracks
	}
	def String getLogrotator() {
		return this.logrotator.tokenize()
	}
	
	def Map SetRepoMap() {
		def repo =  [:]
		this.repositories.each { key, value ->
			repo.put(key, value)
		}
		this.branch.each { key, value ->
			repo.put(key, value)
		}
		this.branchGrouptracks.each { key, value ->
			repo.put(key, value)
		}
		repo.put('excluded', this.excluded_repositories)
		repo.put('browser', this.webview)
		return repo
	}
	
	def Map SetPlatformMap() {
		return [:] << this.platforms
	}
	
	def String SetProjectFullName(jobname, branchGroup, track, branch) {
		this.full_jobname = (jobname + " " + branchGroup + " " + track + " " + branch).replaceAll('/','-') 		
		return this.full_jobname
	}
	
	def List CreateViewJobList(full_jobname) {			
		this.viewjobslist.push(this.full_jobname)
		return this.viewjobslist		
	}
}
