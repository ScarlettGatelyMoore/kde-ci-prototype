/**
 * 
 */
package helpers

import groovy.json.*

/**
 * @author scarlett clark
 * Classes to acquire all of the configuration data and return it to the project.
 */
class ProjectConfigHelper {
	static ArrayList getJobConfig(String json, String JENKINS_HOME) {
		List configs = new ArrayList()
		def JOBCONFIG = new java.io.FileReader("${JENKINS_HOME}/${json}")
		def result = new groovy.json.JsonSlurper().parse(JOBCONFIG)
		if (!result.equals(null)) {
			configs = (ArrayList) result;
		}
		return configs
	}
	static Map createKDEProjects(String workspace, String jobname) {
		def kdeprojects = new XmlParser().parse("${workspace}/projects.xml")
		def xmlmap = ['name' : jobname]				
		def newmap = kdeprojects.'**'.findAll{ it.@identifier == jobname }
		if(newmap.description != null) { 
			newmap.description.each { description ->
				xmlmap.put('description', description.text())
			}
		}
		if(newmap.path != null) {
			newmap.path.each { path ->
				xmlmap.put('path', path.text())
			}
		}
		if(newmap.web != null) {
			newmap.web.each { web ->				
				xmlmap.put('redmine', web.text())
			}
		}								
		if(newmap.repo != null) {										
			newmap.repo.each { repo ->	
				repo.each {	repoprotocolurl ->		
				def urls = repo.url.findAll { it.attributes() }
					def repourl = urls.find {	it.@protocol == 'git' || 'svn' || 'bzr' }								
					xmlmap.put('repourl', repourl)
				}		
			}
		}
		if(newmap.repo.active != null) {					
			newmap.repo.active.each { active ->
				xmlmap.put('active', active.text())
			}				
		}				
		return xmlmap	
	}	
}