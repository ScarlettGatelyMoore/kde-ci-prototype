/**
 * 
 */
package kde_dsl_code

/**
 * @author scarlett
 *
 */
class create_views {
	def generate_Views(jobname, currbranch) {
	listView('Frameworks') {
		description('All unstable jobs for project A')
		filterBuildQueue()
		filterExecutors()
		jobs {
			names(frameworks_list)			
		}
		jobFilters {
			status {
				status(Status.ALL)
			}
		}
		columns {
			status()
			weather()
			name()
			lastSuccess()
			lastFailure()
			lastDuration()
			buildButton()
		}
	}
	}
}
