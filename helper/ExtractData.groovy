/**
 * @author Scarlett Clark
 * Classes to acquire all of the configuration data and return it to the project.
 */
package helper

import groovy.json.*
import java.util.Map;
import static groovy.json.JsonOutput.*

//Class to do the data extraction from the json file.
class ExtractData {
	static Map parse_kde_projects_json(home) {			
		Object all_json_data_parsed = new JsonSlurper().parseText( new File("${home}" + "/scripts/metadata/kde_projects.json").getText())			
		Map all_json_data_map = [:] << all_json_data_parsed
		assert all_json_data_map instanceof Map
		return all_json_data_map
	}
	static Map extract_data(Map all_json_data, String item) {		
		def Map file_item_ci = [:] << all_json_data.getAt(item)
		assert file_item_ci instanceof Map
		return file_item_ci
	}
}