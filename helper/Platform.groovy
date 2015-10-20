/**
 * 
 */
package helper

/**
 * @author Scarlett Clark
 *
 */
import static groovy.json.JsonOutput.*
import groovy.lang.Closure;
import java.util.ArrayList;
import org.jenkinsci.*

class Platform {
	static List platformCompilers = []
	static List platformsToBuild = []
	String platform	
	boolean build
	String shell
	Closure build_step
	String compilers
	String platform_email
	String platform_irc
	ArrayList tracks
	Map combinations
	ArrayList Variation
	ArrayList VariationLinux
	ArrayList VariationWindows
	ArrayList VariationOSX
	ArrayList VariationAndroid
	ArrayList VariationUbuntuP
		
	Platform() {		
	}	
	Platform(String platform, boolean build, String compilers, String platform_email, String platform_irc, \
		ArrayList tracks, ArrayList VariationLinux, ArrayList VariationWindows, ArrayList VariationOSX, \
		ArrayList VariationAndroid, ArrayList VariationUbuntuP) {
		this.platform = platform
		this.build = build
		this.compilers = compilers
		this.platform_email	= platform_email
		this.platform_irc = platform_irc
		this.tracks = tracks
		this.Variation = Variation
		this.VariationLinux = VariationLinux
		this.VariationWindows = VariationWindows
		this.VariationOSX = VariationOSX
		this.VariationAndroid = VariationAndroid
		this.VariationUbuntuP = VariationUbuntuP
	}
	def addPlatform(String key, String compiler, String track) {			
		if(this.build != false && this.tracks.contains(track)) {			
			this.platformsToBuild << key
			this.platformCompilers << compiler
		}			
	}
	def newTrack()	{		
		platformCompilers = []		
		platformsToBuild = []	
	} 
	def newCombinations(String platform, String compiler) {
		this.combinations = [
			    { 'PLATFORM:' platform, 'compiler:' compiler }				
		]
	}
	def initialPlatformBlock(String home) {
		return { project ->
			project.name = 'matrix-project'
			this.platformsToBuild.each { platform ->				
				if (platform == "windows") {
					this.shell = 'BatchFile'
				} else {
					this.shell = 'Shell'
				}
				project / builders <<
				'org.jenkinsci.plugins.conditionalbuildstep.singlestep.SingleConditionalBuilder' {
					condition(class: 'org.jenkins_ci.plugins.run_condition.core.StringsMatchCondition') {
						arg1 '${ENV,var="PLATFORM"}'
						arg2 platform
						ignoreCase false
					}
					runner(class: "org.jenkins_ci.plugins.run_condition.BuildStepRunner\$Fail")
					buildStep(class: 'hudson.tasks.' + "${this.shell}") {
						command "python ${home}/scripts/tools/update-setup.py \n" + \
								"python ${home}/scripts/tools/prepare-environment.py"
					}
				}
			}
		}
		
	}
}
