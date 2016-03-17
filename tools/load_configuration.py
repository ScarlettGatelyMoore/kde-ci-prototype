#!/usr/bin/python3
import sys
import os
import subprocess
import configparser
import socket
from kdecilib import *

# Loads a configuration for a given project
def load_project_configuration( project, branchGroup, platform, compiler, variation = None ):
	# Create a configuration parser
	config = ConfigParser.ConfigParser()
	# List of prospective files to parse
	configFiles =  ['global.cfg', '{compiler}.cfg', '{platform}.cfg', '{branchGroup}.cfg', '{host}.cfg']
	configFiles += ['{branchGroup}-{platform}.cfg']
	configFiles += ['{project}/project.cfg', '{project}/{platform}.cfg', '{project}/{variation}.cfg', '{project}/{branchGroup}.cfg']
	configFiles += ['{project}/{branchGroup}-{platform}.cfg', '{project}/{branchGroup}-{variation}.cfg']
	configFiles += ['server.cfg', '{server-host}.cfg']
	# Go over the list and load in what we can
	for confFile in configFiles:
		confFile = confFile.format( host=socket.gethostname(), branchGroup=branchGroup, compiler=compiler, platform=platform, project=project, variation=variation )
		config.read( 'config/build/' + confFile )		
	# All done, return the configuration		
	return config