#!/usr/bin/python3
import sys
import os
import subprocess
import configparser
import socket
from kdecilib import *

def check_jenkins_environment():
	# Prepare
	arguments = argparse.Namespace()

	# Do we have a job name?
	if 'JOB_NAME' in os.environ:
		# Split it out
		jobMatch = re.match("(?P<project>[^ ]+) ?(?P<branchGroup>[^ ]+)? ?(?P<track>[^ ]+)? ?(?P<branch>[^ ]+)? ?(?P<platform>[^ ]+)? ?(?P<compiler>[^ ]+)?", os.environ['JOB_NAME'])
		# Now transfer in any non-None attributes
		# If we have the project name, transfer it
		if jobMatch.group('project') is not None:
			arguments.project = jobMatch.group('project')
		# Determine our branch group, based on the given branch/base combo
		if jobMatch.group('branchGroup') is not None:
			arguments.branchGroup = jobMatch.group('branchGroup')
		if jobMatch.group('track') is not None:
			arguments.track = jobMatch.group('track')
		if jobMatch.group('branch') is not None:
			arguments.branch = jobMatch.group('branch')
		if jobMatch.group('platform') is not None:
			arguments.platform = jobMatch.group('platform')
		if jobMatch.group('compiler') is not None:
			arguments.compiler = jobMatch.group('compiler')
	# Do we have a workspace?
	if 'WORKSPACE' in os.environ:
		# Transfer it
		arguments.sources = os.environ['WORKSPACE']

	# Do we have a build variation?
	if 'Variation' in os.environ:
		# We need this to determine our specific build variation
		arguments.variation = os.environ['Variation']

	# Do we need to change into the proper working directory?
	if 'JENKINS_SLAVE_HOME' in os.environ:
		# Change working directory
		os.chdir( os.environ['JENKINS_SLAVE_HOME'] )

	return arguments

# Loads a configuration for a given project
def load_project_configuration( project, branchGroup, platform, compiler, variation = None ):
	# Create a configuration parser
	config = configparser.ConfigParser()
	# List of prospective files to parse
	configFiles =  ['global.cfg', '{compiler}.cfg', '{platform}.cfg', '{branchGroup}.cfg', '{host}.cfg']
	configFiles += ['{branchGroup}-{platform}.cfg']
	configFiles += ['{project}/project.cfg', '{project}/{platform}.cfg', '{project}/{variation}.cfg', '{project}/{branchGroup}.cfg']
	configFiles += ['{project}/{branchGroup}-{platform}.cfg', '{project}/{branchGroup}-{variation}.cfg']
	configFiles += ['server.cfg']
	# Go over the list and load in what we can
	for confFile in configFiles:
		confFile = confFile.format( host=socket.gethostname(), branchGroup=branchGroup, compiler=compiler, platform=platform, project=project, variation=variation )
		config.read( 'scripts/config/build/' + confFile )		
	# All done, return the configuration		
	return config

# Loads the projects
def load_projects( projectFile, projectFileUrl, configDirectory, moduleStructure ):
	# Download the list of projects if necessary
	if not os.path.exists(projectFile) or time.time() > os.path.getmtime(projectFile) + 60*60:
		urllib.request.urlretrieve(projectFileUrl, projectFile)

	# Now load the list of projects into the project manager
	with open(projectFile, 'r') as fileHandle:
		print( fileHandle )
		try:
			ProjectManager.load_projects( etree.parse(fileHandle) )
		except:
			os.remove(projectFile)
			return False

	# Load the branch group data now
	with open(moduleStructure, 'r') as fileHandle:
		ProjectManager.setup_branch_groups( json.load(fileHandle) )

	# Finally, load special projects
	for dirname, dirnames, filenames in os.walk( configDirectory ):
		for filename in filenames:
			filePath = os.path.join( dirname, filename )
			ProjectManager.load_extra_project( filePath )

	# We are successful
	return True