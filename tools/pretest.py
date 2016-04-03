import subprocess
from kdecilib import *

def run_pre_test(manager):
	    # Prepare to start work...
	    runtimeEnv = manager.generate_environment(runtime=True)
	    buildDirectory = manager.build_directory()
	    command = manager.config.get('Test', 'kbuildsycocaCommand')	 
	    installRoot = os.path.join( manager.projectSources, 'local-inst', makeRelativeLocation(manager.installPrefix) ) 

	    command = shlex.split(command)
	    print command	   
	    if sys.platform == "linux2":
	    	if os.path.exists(command):
	    		process = subprocess.Popen( command, stdout=sys.stdout, stderr=sys.stderr, env=runtimeEnv, cwd=installRoot )
	    else:
			return