#!/usr/bin/python3
import sys
import argparse
from load_configuration import *
from kdecilib import *
from appstreamtest import *

# Load our command line arguments
parser = argparse.ArgumentParser(description='Utility to control building and execution of tests in an automated manner.')
parser.add_argument('--project', type=str)
parser.add_argument('--branchGroup', type=str, default='kf5-qt5')
parser.add_argument('--sources', type=str)
parser.add_argument('--variation', type=str)
parser.add_argument('--platform', type=str, choices=['Linux', 'OSX', 'Windows', 'android', 'snappy', 'flatpak'], default='Linux')
parser.add_argument('--compiler', type=str, choices=['gcc', 'clang', 'mingw', 'vs2013', 'arm-android-gcc'], default='gcc')

# Parse the arguments
environmentArgs = check_jenkins_environment()
arguments = parser.parse_args( namespace=environmentArgs )

# Load our configuration, projects and dependencies
print( "Loading config with the following parameters: " + arguments.project + " " + arguments.branchGroup \
	+ " " + arguments.platform + " " + arguments.compiler + " " + arguments.variation )
config = load_project_configuration( arguments.project, arguments.branchGroup, arguments.platform, arguments.compiler, arguments.variation )
try:
	load_projects( 'kde_projects.xml', 'http://projects.kde.org/kde_projects.xml', 'config/projects', 'dependencies/logical-module-structure' ):
except OSError
load_project_dependencies( 'config/base/', arguments.branchGroup, arguments.platform, 'dependencies/' )

# Load the requested project
project = ProjectManager.lookup( arguments.project )
if project is None:
	sys.exit("Requested project %s was not found." % arguments.project)

# Prepare the build manager
manager = BuildManager(project, arguments.branchGroup, arguments.sources, config, arguments.platform)

# Give out some information on what we are going to do...
print("\nKDE Continuous Integration Build")
print("== Building Project: %s - Branch %s" % (project.identifier, manager.projectBranch))
print( "== Build Dependencies:")
for dependency, dependencyBranch in manager.dependencies:
	print( "==== %s - Branch %s" %(dependency.identifier, dependencyBranch))

# Apply any necessary patches if we have them
print( "\n== Applying Patches")
if not manager.apply_patches():
	sys.exit("Applying patches to project %s failed." % project.identifier)

# Sync all the dependencies
print( "\n== Syncing Dependencies from Master Server\n")
if not manager.sync_dependencies():
	sys.exit("Syncing dependencies from master server for project %s failed." % project.identifier)
	
print( "\n== Preparing the Environment\n")
environment = manager.generate_environment(True)

# We care about these environment variables
neededVariables = [
	'CMAKE_PREFIX_PATH', 'XDG_CONFIG_DIRS', 'XDG_DATA_DIRS', 'KDEDIRS', 'PATH', 'LD_LIBRARY_PATH', 'PKG_CONFIG_PATH', 'PYTHONPATH',
	'PERL5LIB', 'QT_PLUGIN_PATH', 'QML_IMPORT_PATH', 'QML2_IMPORT_PATH', 'QMAKEFEATURES', 'XDG_CURRENT_DESKTOP', 'PYTHON3PATH', 'CPLUS_INCLUDE_PATH'
]

osxneededVariables = [
	'CMAKE_PREFIX_PATH', 'KDEDIRS', 'PATH', 'DYLD_LIBRARY_PATH', 'PKG_CONFIG_PATH', 'PYTHONPATH',
	'PERL5LIB', 'QT_PLUGIN_PATH', 'QML_IMPORT_PATH', 'QML2_IMPORT_PATH', 'QMAKEFEATURES',
	'DATA_INSTALL_DIR', 'CONFIG_INSTALL_DIR', 'PYTHON3PATH', 'HEADER_SEARCH_PATHS',
	'SHARE_INSTALL_PREFIX', 'BUNDLE_INSTALL_DIR'
]
# Generate the shell format environment file, suitable for sourcing
if sys.platform == 'OSX':
	for variable in osxneededVariables:
		if variable in environment:
			print( 'export %s="%s"' % (variable, environment[variable]))
else:
	for variable in neededVariables:
		if variable in environment:			
			print( 'export %s="%s"' % (variable, environment[variable]))

# Configure the build
print( "\n== Configuring Build\n")
if not manager.configure_build():
	sys.exit("Configure step exited with non-zero code, assuming failure to configure for project %s." % project.identifier)

# Build the project
print( "\n== Commencing the Build\n")
if not manager.compile_build():
	sys.exit("Compilation step exited with non-zero code, assuming failure to build from source for project %s." % project.identifier)

# Install the project
print( "\n== Installing the Build\n")
if not manager.install_build():
	sys.exit("Installation step exited with non-zero code, assuming failure to install from source for project %s." % project.identifier)

# Deploy the newly completed build to the local tree as well as the master server
print( "\n== Deploying Installation\n")
if not manager.deploy_installation():
	sys.exit("Deployment of completed installation failed for project %s." % project.identifier)

# Execute the tests
print("\n== Executing Tests\n")
manager.execute_tests()

# Run cppcheck
print( "\n== Executing cppcheck\n")
manager.execute_cppcheck()

# Perform a lcov processing run
print( "\n== Performing lcov processing\n")
manager.generate_lcov_data_in_cobertura_format()

# Extract dependency data from CMake
print( "\n== Extracting dependency information from CMake\n")
manager.extract_dependency_information()
manager.extract_cmake_dependency_metadata()

# Run appstreamcli tests
print( "\n== Running Appstreamcli Test\n")
enableTest = manager.config.getboolean('AppstreamcliTest', 'appstreamcliEnabled')

if not enableTest:
   print( "\n Appstream test is disabled")  
else:
   run_appstreamcli_test(manager)

print( "\n== Run Completed Successfully\n")
