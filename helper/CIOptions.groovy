/**
 * 
 */
package helper

import java.util.Map;
import static groovy.json.JsonOutput.*


/**
 * @author Scarlett Clark
 *
 */
//Class to hold the configuration options.
class CIOptions {	
	String combinations 
	String downstream 
	String logrotator 
	String projectname
	String description
	String email 
	String irc 
	String html5
	String path
	String view	
	Map repository
	Map repo_data
	Map branches
	Map protocol
	int priority 
	String cron
	boolean publishers
		
	CIOptions() {		
	}
	CIOptions(projectname, description, combinations, downstream, logrotator, \
		priority, email, irc, html5, cron, publishers, view){
		this.projectname = projectname	
		this.description = description	
		this.combinations = combinations ?: "Linux"
		this.downstream = downstream ?: "qt5"
		this.logrotator = logrotator ?: "-1,50,-1,-1"
		this.priority = priority ?: 100
		this.email = email ?: "sgclark@kde.org"
		this.irc = irc ?: "#kde-builds"
		this.html5 = html5 ?: false	
		this.cron = cron ?: null
		this.publishers = publishers ?: true
		this.view = view ?: "default"		
	}		
	def getRepoInfo() {	
		this.repository.each { path -> 
		this.path = path.key
		this.repo_data = [:] << path.value
		this.branches = [:] << this.repo_data.getAt("branch")
		this.protocol = [:] << this.repo_data.getAt("protocol")
		}		
	}

}
