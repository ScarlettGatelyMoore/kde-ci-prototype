import subprocess
from kdecilib import *

def run_appstreamcli_test(manager):
	    # Prepare to start work...
	    runtimeEnv = manager.generate_environment(runtime=True)
	    buildDirectory = manager.build_directory()
	    enableTest = manager.config.get('AppstreamcliTest', 'appstreamcliEnabled')
	    installRoot = os.path.join( manager.projectSources, 'local-inst', makeRelativeLocation(manager.installPrefix) )

	    # Build up our command
	    command = manager.config.get('AppstreamcliTest', 'appstreamcliCommand')
	    command = command.format( scriptsLocation=manager.scriptsLocation, localInstall=installRoot )
	    command = shlex.split(command)
	    print( command )
	    if enableTest:
	    	if sys.platform == "linux2":
	    		process = subprocess.Popen( command, stdout=sys.stdout, stderr=sys.stderr, env=runtimeEnv, cwd=installRoot )
	    	else:
	    		return
		 
