#!/usr/bin/python
import sys
import argparse
from kdecilib import *

# Load our command line arguments
parser = argparse.ArgumentParser(description='Utility to control building and execution of tests in an automated manner.')
parser.add_argument('--project', type=str, required=True)
parser.add_argument('--branchGroup', type=str, required=True)
parser.add_argument('--sources', type=str, required=True)
parser.add_argument('--variation', type=str)
parser.add_argument('--platform', type=str, choices=['linux64-g++', 'darwin-mavericks', 'windows-mingw-w64'], default='linux64-g++')
parser.add_argument('--compiler', type=str, choices=['gcc', 'clang', 'mingw', 'vs2013'], default='gcc')
arguments = parser.parse_args()

# Load our configuration, projects and dependencies
config = load_project_configuration( arguments.project, arguments.branchGroup, arguments.platform, arguments.compiler, arguments.variation )
if not load_projects( 'kde_projects.xml', 'http://projects.kde.org/kde_projects.xml', 'config/projects', 'dependencies/logical-module-structure' ):
	sys.exit("Failure to load projects - unable to continue")
load_project_dependencies( 'config/base/', arguments.branchGroup, arguments.platform, 'dependencies/' )

# Load the requested project
project = ProjectManager.lookup( arguments.project )
if project is None:
	sys.exit("Requested project %s was not found." % arguments.project)

# Prepare the build manager
manager = BuildManager(project, arguments.branchGroup, arguments.sources, config, arguments.platform)
