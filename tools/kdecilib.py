# Python library to help manage and run the KDE Continuous Integration system
import re
import os
import sys
import time
import copy
import json
import shlex
import urllib.request
import shutil
import socket
import fnmatch
import argparse
import subprocess
import configparser
import multiprocessing
import distutils
from distutils import dir_util
from lxml import etree
from collections import defaultdict
from os.path import expanduser
from subprocess import call

# Settings
hostname = socket.gethostname()

def localAwareJoin(local, *p):
	if local:
		return os.path.join(*p)
	else:
		return "/".join(p)

def makeRelativeLocation(path):
	if sys.platform == "win32":
		return path[3:]
	else:
		return path[1:]

class ProjectManager(object):
	# Projects which we know, keyed by their identifier
	_projects = {}
	# Regex for the dependency rules
	_dependencyRuleRe = re.compile(r"""
		(?P<project>[^\[]+)
		\s*
		(?:
			\[
				(?P<project_branch>[^ ]+)
			\]
		)?
		\s*
		:
		\s*
		(?P<ignore_dependency>-)?
		(?P<dependency>[^\[]+)
		\s*
		(:?
			\[
				(?P<dependency_branch>[^ ]+)
			\]
		)?
		""",re.X)

	# Sets up a project from a configuration file
	@staticmethod
	def load_extra_project( projectFilename ):
		# Read the project configuration
		projectData = configparser.ConfigParser()
		projectData.read( projectFilename )

		# Determine if we already know a project by this name (ie. override rather than create)
		identifier = projectData.get('Project', 'identifier')
		project = ProjectManager.lookup( identifier )
		if project is None:
			project = Project()
			project.identifier = identifier
			ProjectManager._projects[ project.identifier ] = project

		# Are we overriding the path?
		if projectData.has_option('Project', 'path'):
			project.path = projectData.get('Project', 'path')

		# Are we overriding the url?
		if projectData.has_option('Project', 'url'):
			project.url = projectData.get('Project', 'url')

		# Are we changing the general dependency state?
		if projectData.has_option('Project', 'sharedDependency'):
			project.sharedDependency = projectData.getboolean('Project', 'sharedDependency')

		# Are we registering any branch group associations?
		if projectData.has_section('BranchGroups'):
			for symName in projectData.options('BranchGroups'):
				project.branchGroups[ symName ] = projectData.get('BranchGroups', symName)

	# Load the kde_projects.xml data in
	@staticmethod
	def load_projects( xmlData ):
		# Get a list of all repositories, then create projects for them
		for repoData in xmlData.iterfind('.//repo'):
			# Grab the actual project xml item
			projectData = repoData.getparent()

			# Create the new project and set the bare essentials
			project = Project()
			project.identifier = projectData.get('identifier')
			project.path = projectData.find('path').text
			project.url = repoData.find('url[@protocol="git"]').text

			# What branches has this project got?
			for branchItem in repoData.iterfind('branch'):
				# Maybe this branch is invalid?
				if branchItem.text is None or branchItem.text == 'none':
					continue

				# Must be a normal branch then
				project.branches.append( branchItem.text )

			# Register this project now - all setup is completed
			ProjectManager._projects[ project.identifier ] = project

	# Load in information concerning project branch groups
	@staticmethod
	def setup_branch_groups( moduleStructure ):
		# Let's go over the given groups and categorise them appropriately
		for entry, groups in moduleStructure['groups'].items():
			# If it is dynamic, then store it away appropriately
			if entry[-1] == '*':
				Project.wildcardBranchGroups[entry] = groups
				continue

			# If it isn't dynamic, then find out the project it belongs to
			project = ProjectManager.lookup( entry )
			if project != None:
				project.branchGroups = groups

	# Setup ignored project metadata
	@staticmethod
	def setup_ignored( ignoreData ):
		# First, remove any empty lines as well as comments
		ignoreList = [ project.strip() for project in ignoreData if project.find("#") == -1 and project.strip() ]
		# Now mark any listed project as ignored
		for entry in ignoreList:
			project = ProjectManager.lookup( entry )
			if entry:
				project.ignore = True

	# Setup the dependencies from kde-build-metadata.git
	@staticmethod
	def setup_dependencies( depData ):
		for depEntry in depData:
			# Cleanup the dependency entry and remove any comments
			commentPos = depEntry.find("#")
			if commentPos >= 0:
				depEntry = depEntry[0:commentPos]

			# Prepare to extract the data and skip if the extraction fails
			match = ProjectManager._dependencyRuleRe.search( depEntry.strip() )
			if not match:
				continue

			# Determine which project is being assigned the dependency
			projectName = match.group('project').lower()
			project = ProjectManager.lookup( projectName )
			# Validate it (if the project lookup failed and it is not dynamic, then it is a virtual dependency)
			if project == None and projectName[-1] != '*':
				# Create the virtual dependency
				project = Project()
				project.path = projectName
				project.virtualDependency = True
				# Generate an identifier for it
				splitted = projectName.split('/')
				project.identifier = splitted[-1]
				# Now register it - we can continue normally after this
				ProjectManager._projects[ project.identifier ] = project

			# Ensure we know the dependency - if it is marked as "ignore" then we skip this
			dependencyName = match.group('dependency').lower()
			dependency = ProjectManager.lookup( dependencyName )
			if dependency == None or dependency.ignore:
				continue

			# Are any branches specified for the project or dependency?
			projectBranch = dependencyBranch = '*'
			if match.group('project_branch'):
				projectBranch = match.group('project_branch')
			if match.group('dependency_branch'):
				dependencyBranch = match.group('dependency_branch')

			# Is this a dynamic project?
			if projectName[-1] == '*':
				dependencyEntry = ( projectName, projectBranch, dependency, dependencyBranch )
				# Is it negated or not?
				if match.group('ignore_dependency'):
					Project.dynamicNegatedDeps.append( dependencyEntry )
				else:
					Project.dynamicDependencies.append( dependencyEntry )
			# Otherwise it must be a project specific rule
			else:
				dependencyEntry = ( dependency, dependencyBranch )
				# Is it negated or not?
				if match.group('ignore_dependency'):
					project.negatedDeps[ projectBranch ].append( dependencyEntry )
				else:
					project.dependencies[ projectBranch ].append( dependencyEntry )

	# Lookup the given project name
	@staticmethod
	def lookup( projectName ):
		# We may have been passed a path, reduce it down to a identifier
		splitted = projectName.split('/')
		identifier = splitted[-1]
		# Now we try to return the desired project
		try:
			return ProjectManager._projects[identifier]
		except Exception:
			return

class Project(object):
	# Lists of known branch groups, and dynamically determined branch group mappings
	wildcardBranchGroups = {}
	# Lists of "dynamic dependencies" and "dynamic negated dependencies" which apply to multiple projects
	dynamicDependencies = []
	dynamicNegatedDeps = []

	# Setup our defaults
	def __init__(self):
		# The identifier, path and repository url are simply not set
		self.identifier = self.path = self.url = None
		# We are not an ignored project by default
		self.ignore = False
		# We are not a shared dependency by default
		self.sharedDependency = False
		# We are not a virtual dependency by default either
		self.virtualDependency = False
		# We have no branches or symbolic branches
		self.branches = []
		self.branchGroups = {}
		self.scriptsLocation= ''
		# We have no dependencies or negated dependencies
		self.dependencies = defaultdict(list)
		self.negatedDeps = defaultdict(list)

	# Give ourselves a pretty name
	def __repr__(self):
		return "<Project instance with identifier %s>" % self.identifier

	# Determine the actual name of the desired branch
	def resolve_branch(self, branchGroup):
		# Do we know this branch group internally?
		if branchGroup in self.branchGroups:
			return self.branchGroups[ branchGroup ]

		# Go over the dynamic group names
		for wildcardName, groups in Project.wildcardBranchGroups.items():
			# Does the dynamic name match the path of the project?
			if not fnmatch.fnmatch( self.path, wildcardName ):
				continue

			# Cache the whole wildcard to save future runs
			self.branchGroups = groups
			if branchGroup in self.branchGroups:
				return self.branchGroups[ branchGroup ]

		# It seems everything above failed, so assume they mean 'master'
		return 'master'

	# Return a list of dependencies of ourselves
	def determine_dependencies(self, branchGroup, includeSubDeps = True, checkDynamicDeps = True, currentlyKnown = []):
		# Determine what the real name is for dependency information lookups
		ourBranch = self.resolve_branch(branchGroup)
		ourDeps = finalDynamic = []

		if checkDynamicDeps:
			# Prepare: Get the list of dynamic dependencies and negations which apply to us
			dynamicDeps = self._resolve_dynamic_dependencies( ourBranch, Project.dynamicDependencies )
			negatedDynamic = self._resolve_dynamic_dependencies( ourBranch, Project.dynamicNegatedDeps )

			# Start our list of dependencies
			# Run the list of dynamic dependencies against the dynamic negations to do so
			ourDeps = finalDynamic = self._negate_dependencies( branchGroup, dynamicDeps, negatedDynamic )

		# Add the project level dependencies to the list of our dependencies
		# Then run the list of our dependencies against the project negations
		ourDeps = self._negate_dependencies( branchGroup, ourDeps + self.dependencies['*'], self.negatedDeps['*'] )

		# Add the branch level dependencies to the list of our dependencies
		# Then run the list of our dependencies against the branch negations
		ourDeps = self._negate_dependencies( branchGroup, ourDeps + self.dependencies[ ourBranch ], self.negatedDeps[ ourBranch ] )

		# Ensure the current project is not listed (due to a dynamic dependency for instance)
		ourDeps = [(project, branch) for project, branch in ourDeps if project != self]

		# Add the dependencies of our dependencies if requested
		# Dynamic dependencies are excluded otherwise it will be infinitely recursive
		if includeSubDeps:
			toLookup = set(ourDeps) - set(finalDynamic) - set(currentlyKnown)
			for dependency, dependencyBranch in toLookup:
				ourDeps = ourDeps + dependency.determine_dependencies(branchGroup, includeSubDeps = True, currentlyKnown = ourDeps)

			dynamicLookup = set(ourDeps) & set(finalDynamic)
			for dependency, dependencyBranch in dynamicLookup:
				ourDeps = ourDeps + dependency.determine_dependencies(branchGroup, includeSubDeps = True, checkDynamicDeps = False, currentlyKnown = ourDeps)

		# Re-ensure the current project is not listed and that they are not virtual
		# Dynamic dependency resolution of sub-dependencies may have re-added it
		ourDeps = [(project, branch) for project, branch in ourDeps if project != self and not project.virtualDependency]

		# Ensure we don't have any duplicates
		return list(set(ourDeps))

	def _resolve_dynamic_dependencies(self, desiredBranch, dynamicDeps):
		# Go over the dynamic dependencies list we have and see if we match
		projectDeps = []
		for dynamicName, dynamicBranch, dependency, dependencyBranch in dynamicDeps:
			# First we need to see if the dynamic name matches against our path
			if not fnmatch.fnmatch( self.path, dynamicName ):
				continue

			# Next - make sure the dynamicBranch is compatible with our desired branch name
			if dynamicBranch != desiredBranch and dynamicBranch != '*':
				continue

			# We match this - add it
			dependencyEntry = ( dependency, dependencyBranch )
			projectDeps.append( dependencyEntry )

		return projectDeps

	def _negate_dependencies(self, branchGroup, dependentProjects, negatedProjects):
		# First we go over both lists and resolve the branch name
		resolvedDependencies = [(project, project.resolve_branch(branchGroup)) for project, branch in dependentProjects]
		resolvedNegations =    [(project, project.resolve_branch(branchGroup)) for project, branch in negatedProjects]

		# Remove any dependencies which have been negated
		return list( set(resolvedDependencies) - set(resolvedNegations) )
	
	def determine_scriptshome(self):
		home = expanduser("~") + '/scripts'
		return home

class BuildManager(object):
	# Make sure we have our configuration and the project we are building available
	def __init__(self, project, branchGroup, projectSources, configuration, platform):
		# Save them for later use
		self.project = project
		self.branchGroup = branchGroup
		if sys.platform == "win32":
		  self.projectSources = os.path.normpath(projectSources)
		else:
		  self.projectSources = projectSources
		self.config = configuration
		self.platform = platform
		self.scriptsLocation = project.determine_scriptshome()
		# Resolve the branch
		self.projectBranch = project.resolve_branch( self.branchGroup )
		# Get the list of dependencies to ensure we only build it once
		self.dependencies = project.determine_dependencies( self.branchGroup )
		# We set the installPrefix now for convenience access elsewhere
		self.installPrefix = self.project_prefix( self.project )
		# Determine our Python version (used later for environment variable setup)
		self.pythonVersion = sys.version[:3].split(' ')[0]
		# Determine the various paths Perl looks in by default
		# Need so that we can set it to look at these in our prefixes		
		self.perlSuffixes = self.find_perl_suffixes()
		# Determine the name of the variable used to influence the include path
		if sys.platform == 'darwin':
			self.libraryPathVariable = 'IGNORE_THIS_VARIABLE'
		elif sys.platform == 'win32':
			self.libraryPathVariable = 'PATH'
		else:
			self.libraryPathVariable = 'LD_LIBRARY_PATH'
		# Determine the name of the variable used to influence the library loader search path
		if sys.platform == 'darwin':
			self.includePathVariable = 'HEADER_SEARCH_PATHS'
		elif sys.platform == 'win32':
			self.includePathVariable = 'PATH'
		else:
			self.includePathVariable = 'CPLUS_INCLUDE_PATH'

	def find_perl_suffixes(self):
		suffixes = []
		# Prepare a Regexp to clean off the prefix and lib* component of the path we have been given
		regexp = '.*/lib[0-9]*/(.*)[\n]'
		compiledRegexp = re.compile(regexp)
		# Get the list of system wide paths Perl searches for modules
		command = self.config.get('General', 'listPerlIncludeDirsCommand')
		process = subprocess.Popen( shlex.split(command), stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
		process.wait()
		# Parse each path in turn (one per line)
		for variable in process.stdout:
			# Determine the prefix/lib less part of the path - and ensure it isn't a duplicate
			result = compiledRegexp.match(variable)
			if result is not None and result.group(1) not in suffixes:
				# just keep path endings (i.e. path remaining after '/lib' pattern)
				suffixes.append(result.group(1))

		return suffixes

	# Determine the proper prefix (either local or remote) where a project is installed
	def project_prefix(self, project, local = True, includeHost = True, section = 'General'):
		# Determine the appropriate prefix
		prefix = self.config.get( section, 'installPrefix' )
		if not local and includeHost:
			prefix = self.config.get( section, 'remoteHostPrefix' )
		elif not local:
			prefix = self.config.get( section, 'remotePrefix' )

		# Do we have a proper Project instance which is not a shared dependency?
		if isinstance(project, Project) and not project.sharedDependency:
			return localAwareJoin( local, prefix, self.branchGroup, project.path, 'inst' )
		# Maybe we have a proper Project instance which is a shared dependency?
		# Currently not supported by DSL
		elif isinstance(project, Project) and project.sharedDependency:
			return localAwareJoin( local, prefix, 'shared', project.path )
		# We want branchGroup patch to use kf5-qt5 dependencies.
		elif self.branchGroup == 'patch':
			return localAwareJoin( local, prefix, 'kf5-qt5', project.path, 'inst' )
		# Final last-ditch fallback (should not happen)
		else:
			return os.path.join( prefix, self.branchGroup, project )

	def build_directory(self):
		# Maybe we have a project which prefers a in-source build?
		if self.config.getboolean('Build', 'inSourceBuild'):
			return self.projectSources

		# Assume an out-of-source build if it does not want an in-source build
		return os.path.join( self.projectSources, 'build' )

	def perform_rsync(self, source, destination):
		# Get the rsync command
		#A windows check to get a path that will make rsync happy...
		if source[1] == ':':
			try:
				converted = subprocess.check_output(['cygpath', '-u', source])
				source = converted.strip('.').strip()
			except subprocess.CalledProcessError:
				return False
		if destination[1] == ':':
			try:
				converted = subprocess.check_output(['cygpath', '-u', destination])
				destination = converted.strip('.').strip()
			except subprocess.CalledProcessError:
				return False

		rsyncCommand = self.config.get('General', 'rsyncCommand')
		rsyncCommand= rsyncCommand.format(home=expanduser("~" ))
		rsyncCommand = shlex.split( rsyncCommand)

		# If we have a Linux destination, but a Windows source we have to specify the permissions to be used
		if source[1:9] == 'cygdrive' and not destination[1:9] == 'cygdrive':
			rsyncCommand.append("--chmod=Du=rwx,Dgo=rx,Fu=rw,Fgo=r")

		# Add the source and destination to our arguments
		rsyncCommand.append( source + '/' )
		rsyncCommand.append( destination )
		# Execute rsync and wait for it to finish	
		process = subprocess.Popen( rsyncCommand, stdout=sys.stdout, stderr=sys.stderr )
		process.wait()
		# Indicate our success
		return process.returncode == 0

	def run_build_commands(self, buildCommands):
		# Prepare, and load parameters we will need later
		cpuCount = multiprocessing.cpu_count()
		buildDirectory = self.build_directory()
		installPath = os.path.join( self.projectSources, 'local-inst' )

		# Prepare the environment
		# We need to ensure that 'make install' will deploy to the appropriate directory
		buildEnv = self.generate_environment()
		buildEnv['DESTDIR'] = buildEnv['INSTALL_ROOT'] = installPath

		# Actually invoke the commands
		for command in buildCommands:
			# Put the appropriate tokens in place
			# {instPrefix} = Directory where the project should be installed
			# {sources} = Base directory where the project sources are located
			# {loadLevel} = The desired maximum load level during the build
			# {jobCount} = The appropriate number of jobs which should be started during a build
			command = command.format( scriptsLocation=self.scriptsLocation, instPrefix=self.installPrefix.replace("\\", "/"), sources=self.projectSources.replace("\\", "/"), loadLevel=cpuCount, jobCount=cpuCount + 1 )
			
			command = shlex.split( command )

			# Execute the command which is part of the build execution process
			try:
                #Windows ignores PATH set in env with shell=False(default). This is a quick dirty fix to move forward. Needs to be improved.
				if sys.platform == "win32":
					process = subprocess.check_call( command, stdout=sys.stdout, stderr=sys.stderr, shell=True, cwd=buildDirectory, env=buildEnv )
				else:
					process = subprocess.check_call( command, stdout=sys.stdout, stderr=sys.stderr, cwd=buildDirectory, env=buildEnv )
			except subprocess.CalledProcessError:
				# Abort if it fails to complete
				return False

		# We are successful
		return True

	# Sync all of our dependencies from the master server
	def sync_dependencies(self):
		# Sync the project and shared system base dependencies
		for candidate, candidateBranch in self.dependencies:
			HOME=expanduser("~")
			# Determine the host (source) and local (destination) directories we are syncing
			hostPath = self.project_prefix( candidate, local=False )
			localPath = self.project_prefix( candidate )
			# Make sure the local path exists (otherwise rsync will fail)
			if not os.path.exists( localPath ):
				os.makedirs( localPath )
			# Execute the command
			if not self.perform_rsync( source=hostPath, destination=localPath ):
				return False
# 			if sys.platform == "OSX":
# 				if os.path.isdir(localPath + "/Library/Application Support") == True:
# 					datadir = localPath + "/Library/Application Support"
# 			     	dstroot = HOME +  "/Library/Application Support"			      
# 			    	try:
# 			      		distutils.dir_util.copy_tree(datadir, dstroot)
# 			      	except distutils.dir_util.log.DEBUG as e:
# 			      		print( "Oh No! Something went wrong. Error: %s" % e)
# 			      		return False
# 			      	
# 			      	if os.path.isdir(localPath + "/Applications/KF5") == True:
# 			    		bundledir = localPath + "/Applications/KF5"
# 			      		dstroot = HOME + "/Applications/KF5"			     
# 			      		try:
# 			      			distutils.dir_util.copy_tree(bundledir, dstroot)
# 			      		except distutils.dir_util.log.DEBUG as e:
# 			      			print( "Oh No! Something went wrong. Error: %s" % e)
# 			      			return False
# 			     
# 			      	if os.path.isdir(localPath + "/Library/Preferences") == True:
# 			      		configdir = localPath + "/Library/Preferences"
# 			      		dstroot = HOME + "/Library/Preferences"			     
# 			      		try:
# 							distutils.dir_util.copy_tree(configdir, dstroot)
# 			      		except distutils.dir_util.log.DEBUG as e:
# 							print( "Oh No! Something went wrong. Error: %s" % e)
# 							return False
			    
		return True

	# Generate environment variables for configure / building / testing
	def generate_environment(self, runtime = False):
		# Build the list of projects we need to include
		requirements = self.dependencies
		# For runtime (ie. running tests) we need to include ourselves too and kde-runtime as well
		if runtime:
			# First we try to find kde-runtime - use the same branch as the kdelibs dependency
			kdeRuntime = ProjectManager.lookup('kde-runtime')
			kdelibsDep = [(project, branch) for project, branch in self.dependencies if project.identifier == 'kdelibs']
			if kdelibsDep and kdeRuntime:
				libsProject, libsBranch = kdelibsDep[0]
				requirements.append( (kdeRuntime, libsBranch) )

		# Turn the list of requirements into a list of prefixes
		reqPrefixes = [self.project_prefix( requirement ) for requirement, requirementBranch in requirements]

		# Determine what character joins together environment variables
		if sys.platform == "win32":
			splitChar = ';'
		else:
			splitChar = ':'

		# We need to include any prefixes specified in our configuration
		# These may contain extra items not supplied by the base system and installed separately (MySQL outside OSS operating systems for instance)
		if self.config.has_option('General', 'extraPrefix'):
			extraPrefixes = self.config.get('General', 'extraPrefix')
			for extraPrefix in extraPrefixes.split(splitChar):
				if os.path.exists(extraPrefix):
					reqPrefixes.append(extraPrefix)

		# For runtime, we need to add ourselves as well
		# We add the local install/ root so this will work properly even if it has not been deployed
		if runtime:
			localInstall = os.path.join( self.projectSources, 'local-inst', makeRelativeLocation(self.installPrefix) )
			reqPrefixes.append( localInstall )

		# Generate the environment
		envChanges = defaultdict(list)
		for reqPrefix in reqPrefixes:
			# Make sure the prefix exists
			if not os.path.exists(reqPrefix):
				continue

			# Setup CMAKE_PREFIX_PATH
			extraLocation = os.path.join( reqPrefix, 'usr' )
			if os.path.exists( extraLocation ):
				envChanges['CMAKE_PREFIX_PATH'].append( extraLocation )
			# Setup KDEDIRS
			extraLocation = os.path.join( reqPrefix, 'usr' )
			if os.path.exists( extraLocation ):
				envChanges['KDEDIRS'].append( extraLocation )

			# Setup PATH
			extraLocation = os.path.join( reqPrefix, 'usr/bin' )
			if os.path.exists( extraLocation ):
				envChanges['PATH'].append(extraLocation)

			# Handle those paths which involve $prefix/lib*
			for libraryDirName in ['usr/lib', 'usr/lib32', 'usr/lib64', 'usr/lib/x86_64-linux-gnu']:
				# Do LD_LIBRARY_PATH
				extraLocation = os.path.join( reqPrefix, libraryDirName )
				if os.path.exists( extraLocation ):
					envChanges[self.libraryPathVariable].append(extraLocation)

				# Now do PKG_CONFIG_PATH
				extraLocation = os.path.join( reqPrefix, libraryDirName, 'pkgconfig' )
				if os.path.exists( extraLocation ):
					envChanges['PKG_CONFIG_PATH'].append(extraLocation)
				
				# Set up include path CPLUS_INCLUDE_PATH
				extraLocation = os.path.join( reqPrefix, 'usr/include' )
				if os.path.exists( extraLocation ):
					envChanges[self.includePathVariable].append(extraLocation)

				# Now we check PYTHONPATH
				extraLocation = os.path.join( reqPrefix, libraryDirName, 'python' + self.pythonVersion + '/site-packages' )
				if os.path.exists( extraLocation ):
					envChanges['PYTHONPATH'].append(extraLocation)
				
				# Now we check PYTHON3PATH
				extraLocation = os.path.join( reqPrefix, libraryDirName, 'python3' + self.pythonVersion + '/site-packages' )
				if os.path.exists( extraLocation ):
					envChanges['PYTHON3PATH'].append(extraLocation)

				# Next is PERL5LIB
				for perlSuffix in self.perlSuffixes:
					extraLocation = os.path.join( reqPrefix, libraryDirName, perlSuffix )
					if os.path.exists( extraLocation ):
						envChanges['PERL5LIB'].append(extraLocation)

				# Next up is QT_PLUGIN_PATH
				for pluginDirName in ['qt4/plugins', 'kde4/plugins', 'plugins', 'qca']:
					extraLocation = os.path.join( reqPrefix, libraryDirName, pluginDirName )
					if os.path.exists( extraLocation ):
						envChanges['QT_PLUGIN_PATH'].append(extraLocation)

				# Finally we do QML_IMPORT_PATH
				for pluginDirName in ['qt4/imports', 'kde4/imports', 'imports']:
					extraLocation = os.path.join( reqPrefix, libraryDirName, pluginDirName )
					if os.path.exists( extraLocation ):
						envChanges['QML_IMPORT_PATH'].append(extraLocation)

				# And to finish, QML2_IMPORT_PATH
				for pluginDirName in ['qml']:
					extraLocation = os.path.join( reqPrefix, libraryDirName, pluginDirName )
					if os.path.exists( extraLocation ):
						envChanges['QML2_IMPORT_PATH'].append(extraLocation)

			# Setup PKG_CONFIG_PATH
			if sys.platform == "darwin":
				extraLocation = os.path.join( reqPrefix, 'Library/Application Support/pkgconfig' )
				if os.path.exists( extraLocation ):
					envChanges['PKG_CONFIG_PATH'].append(extraLocation)
			else:
				extraLocation = os.path.join( reqPrefix, 'usr/share/pkgconfig' )
				if os.path.exists( extraLocation ):
					envChanges['PKG_CONFIG_PATH'].append(extraLocation)

			# Setup XDG_DATA_DIRS Linux / DATA Directories OSX
			if sys.platform == "darwin":
				extraLocation = os.path.join( reqPrefix, 'usr/local/share' )
				if os.path.exists( extraLocation ):
					envChanges['SHARE_INSTALL_PREFIX'].append(extraLocation)
				extraLocation = os.path.join( reqPrefix, 'Library/Application Support' )
				if os.path.exists( extraLocation ):
					envChanges['DATA_INSTALL_DIR'].append(extraLocation)
				extraLocation = os.path.join( reqPrefix, 'Applications/KF5' )
				if os.path.exists( extraLocation ):
					envChanges['BUNDLE_INSTALL_DIR'].append(extraLocation)					
			else:
				extraLocation = os.path.join( reqPrefix, 'usr/share' )
				if os.path.exists( extraLocation ):
					envChanges['XDG_DATA_DIRS'].append(extraLocation)
			
			# Setup KDE_INSTALL_SYSCONFDIR
				extraLocation = os.path.join( reqPrefix, 'etc/xdg' )
				if os.path.exists( extraLocation ):
					envChanges['KDE_INSTALL_SYSCONFDIR'].append(extraLocation)
					
			# Setup osx
			if sys.platform == "darwin":
				extraLocation = os.path.join( reqPrefix, 'Library/Preferences' )
				if os.path.exists( extraLocation ):
					envChanges['CONFIG_INSTALL_DIR'].append(extraLocation)
			else:
				extraLocation = os.path.join( reqPrefix, 'etc/xdg' )
				if os.path.exists( extraLocation ):
					envChanges['XDG_CONFIG_DIRS'].append(extraLocation)
			
			# Setup PYTHONPATH
			extraLocation = os.path.join( reqPrefix, 'usr/share/sip' )
			if os.path.exists( extraLocation ):
				envChanges['PYTHONPATH'].append( extraLocation )

			# Setup QMAKEFEATURES
			extraLocation = os.path.join( reqPrefix, 'usr/mkspecs/features' )
			if os.path.exists( extraLocation ):
				envChanges['QMAKEFEATURES'].append( extraLocation )

		# Finally, we can merge this into the real environment
		clonedEnv = copy.deepcopy(os.environ.__dict__['data'])
		for variableName, variableEntries in envChanges.iteritems():
			# Join them
			newEntry = splitChar.join( variableEntries )
			# If the variable already exists in the system environment, we prefix ourselves on
			if variableName in clonedEnv:
				newEntry = '%s%s%s' % (newEntry, splitChar, clonedEnv[variableName])
			# Set the variable into our cloned environment
			clonedEnv[variableName] = newEntry

		# Mark ourselves as a KDE session, for things like the platform plugin, etc.
		clonedEnv['XDG_CURRENT_DESKTOP'] = 'KDE'

		# Return the dict of the cloned environment, suitable for use with subprocess.Popen
		return clonedEnv

	def checkout_sources(self, doCheckout = False):
		# We cannot handle projects we don't have Git repository urls for
		if not self.project.url:
			return True

		# Does the git repository exist?
		gitDirectory = os.path.join( self.projectSources, '.git' )
		if not os.path.exists(gitDirectory):
			# Clone the repository
			command = self.config.get('Source', 'gitCloneCommand')
			command = command.format( url=self.project.url )
			try:
				subprocess.check_call( shlex.split(command), cwd=self.projectSources )
			except subprocess.CalledProcessError:
				return False

		# Update the git repository
		command = self.config.get('Source', 'gitFetchCommand')
		try:
			subprocess.check_call( shlex.split(command), cwd=self.projectSources )
		except subprocess.CalledProcessError:
			return False

		# Ensure our desired branch is in place
		# Disable this for kf5-minimum, we only have a tag available.
		if self.branchGroup == "kf5-minimum":
		  	command = self.config.get('Source', 'gitSetBranchCommand')
		  	command = command.format( targetBranch=self.projectBranch )
		  	try:
			  	subprocess.check_call( shlex.split(command), cwd=self.projectSources )
		  	except subprocess.CalledProcessError:
		  		return False

		elif self.config.getboolean('Source', 'alwaysCheckoutSources') or doCheckout:
		# Check the sources out
		  	command = self.config.get('Source', 'gitCheckoutCommand')
		  	command = command.format( branch=self.projectBranch )
		  	try:
		  		subprocess.check_call( shlex.split(command), cwd=self.projectSources )
		  	except subprocess.CalledProcessError:
		  		return False

		# Do we need to run a post-checkout command?
		if self.config.getboolean('Source', 'runPostCheckoutCommand'):
			# Get the command in question and run it
			command = self.config.get('Source', 'postCheckoutCommand')
			if sys.platform == "win32":
			  	command = command.format( sources=self.projectSources.replace("\\","/"))
			else:
			  	command = command.format( sources=self.projectSources )
			try:
				subprocess.check_call( shlex.split(command), cwd=self.projectSources )
			except subprocess.CalledProcessError:
				return False

		# All successful
		return True

	def cleanup_sources(self):
		# Prepare Git/Subversion paths
		gitDirectory = os.path.join( self.projectSources, '.git' )
		svnDirectory = os.path.join( self.projectSources, '.svn' )
		bzrDirectory = os.path.join( self.projectSources, '.bzr' )

		# Which directories are we cleaning?
		pathsToClean = [self.projectSources]

		# Maybe it is a Git repository?
		if os.path.exists( gitDirectory ):
			command = self.config.get('Source', 'gitCleanCommand')
			# Because Git is silly, we have to build it a special list of paths to clean
			pathsToClean = []
			for root, dirs, files in os.walk( self.projectSources, topdown=False ):
				if '.git' in dirs:
					pathsToClean.append( root )
		# Maybe it is a SVN checkout?
		elif os.path.exists( svnDirectory ):
			command = self.config.get('Source', 'svnRevertCommand')
		# Maybe it is a BZR checkout?
		elif os.path.exists( bzrDirectory ):
			command = self.config.get('Source', 'bzrCleanCommand')
		# Nothing for us to do
		else:
			return

		for path in pathsToClean:
			process = subprocess.Popen(command, shell=True, cwd=path)
			process.wait()

		return

	def apply_patches(self):
		# Do we have anything to apply?	
		scriptsLocation=  expanduser("~") + '/scripts'
		if os.path.isdir(os.path.join(scriptsLocation, 'patches', self.project.identifier, self.branchGroup, self.platform )) == True:
		  	patchesDir = os.path.join( scriptsLocation, 'patches', self.project.identifier, self.branchGroup, self.platform )
		else: 
		  	patchesDir = os.path.join( scriptsLocation, 'patches', self.project.identifier, self.branchGroup )
		if not os.path.exists(patchesDir):
			print( "=== No patches to apply")
			return True

		# Iterate over the patches and apply them
		command = shlex.split( self.config.get('Source', 'patchCommand') )
		for dirname, dirnames, filenames in os.walk(patchesDir):
			for filename in filenames:
				# Get the full path to the patch
				patchPath = os.path.join( dirname, filename )	
				# Apply the patch
				try:
					print( "=== Applying: %s\n" % patchPath)
					process = subprocess.check_call( command + [patchPath], stdout=sys.stdout, stderr=sys.stderr, cwd=self.projectSources )
				except subprocess.CalledProcessError:
					# Make sure the patch applied successfully - if it failed, then we should halt here
					return False

		return True

	def configure_build(self):
		# Determine the directory we will perform the build in and make sure it exists
		buildDirectory = self.build_directory()
		if not os.path.exists( buildDirectory ):
			os.makedirs( buildDirectory )

		# Get the initial configuration command
		command = self.config.get('Build', 'configureCommand')
		buildCommands = [ command ]

		# Next comes the post configure command
		if self.config.has_option('Build', 'postConfigureCommand'):
			command = self.config.get('Build', 'postConfigureCommand')
			buildCommands.append( command )

		# Do the configure		
		return self.run_build_commands( buildCommands )

	def compile_build(self):
		# Load the command and run it
		command = self.config.get('Build', 'makeCommand')
		return self.run_build_commands( [command] )

	def install_build(self):
		# Perform the installation
		command = self.config.get('Build', 'makeInstallCommand')
		buildCommands = [ command ]

		# Final command which is needed to finish up the installation
		if self.config.has_option('Build', 'postInstallationCommand'):
			command = self.config.get('Build', 'postInstallationCommand')
			buildCommands.append( command )

		# Do the installation
		if not self.run_build_commands( buildCommands ):
			return False

		# Do we need to run update-mime-database?
		buildEnv = self.generate_environment()
		installRoot = os.path.join( self.projectSources, 'local-inst', makeRelativeLocation(self.installPrefix) )
		mimeDirectory = os.path.join( installRoot, 'usr', 'share', 'mime' )
		if os.path.exists( mimeDirectory ):
			# Invoke update-mime-database
			command = self.config.get('Build', 'updateMimeDatabaseCommand')
			subprocess.call( shlex.split(command), stdout=sys.stdout, stderr=sys.stderr, cwd=installRoot, env=buildEnv )

		#OSX will not play nice with our env paths, so we must copy the files into 
		# a destination that compile time qt will find them.
# 		if sys.platform == "darwin":
# 			  HOME=expanduser("~")
# 			  if os.path.isdir(installRoot + "/Library/Application Support") == True:
# 			      datadir = installRoot + "/Library/Application Support"
# 			      dstroot = HOME + "/Library/Application Support"			     
# 			      try:
# 				distutils.dir_util.copy_tree(datadir, dstroot)
# 			      except distutils.dir_util.log.DEBUG as e:
# 				print( "Oh No! Something went wrong. Error: %s" % e)
# 				return False
# 			     
# 			  if os.path.isdir(installRoot + "/Applications/KF5") == True:
# 			      bundledir = installRoot + "/Applications/KF5"
# 			      dstroot = HOME + "/Applications/KF5"			      
# 			      try:
# 				distutils.dir_util.copy_tree(bundledir, dstroot)
# 			      except distutils.dir_util.log.DEBUG as e:
# 				print( "Oh No! Something went wrong. Error: %s" % e)
# 				return False
# 			     
# 			  if os.path.isdir(installRoot + "/Library/Preferences") == True:
# 			      configdir = installRoot + "/Library/Preferences"
# 			      dstroot = HOME + "/Library/Preferences"			     
# 			      try:
# 				distutils.dir_util.copy_tree(configdir, dstroot)
# 			      except distutils.dir_util.log.DEBUG as e:
# 				print( "Oh No! Something went wrong. Error: %s" % e)
# 				return False
			      		
		# None of the commands failed, so assume we succeeded
		return True
		
		

	def deploy_installation(self):
		# Is deployment disabled?
		if not self.config.getboolean('Build', 'deployInstallation'):
			return True

		# Make sure the local destination exists
		if not os.path.exists( self.installPrefix ):
			os.makedirs( self.installPrefix )

		# First we have to transfer the install from the "install root" to the actual install location
		sourcePath = os.path.join( self.projectSources, 'local-inst', makeRelativeLocation(self.installPrefix) )
		if not self.perform_rsync( source=sourcePath, destination=self.installPrefix ):
			return False

		# Next we ensure the remote directory exists
		serverPath = self.project_prefix( self.project, local=False, includeHost=False )
		command = self.config.get('General', 'createRemotePathCommand').format( remotePath=serverPath , home=expanduser("~"))
		process = subprocess.Popen( shlex.split(command), stdout=sys.stdout, stderr=sys.stderr)
		process.wait()

		# Now we sync the actual install up to the master server so it can be used by other build slaves
		serverPath = self.project_prefix( self.project, local=False )
		return self.perform_rsync( source=self.installPrefix, destination=serverPath )

	def execute_tests(self):
		# Prepare
		buildDirectory = self.build_directory()
		runtimeEnv = self.generate_environment(True)
		junitFilename = os.path.join( buildDirectory, 'JUnitTestResults.xml' )

		# Determine if we have tests to run....
		command = self.config.get('Test', 'ctestCountTestsCommand')
		#Windows ignore PATH set in env with shell=False(default). This is a quick dirty fix to move forward. Needs to be improved.
		if sys.platform == "win32":
			process = subprocess.Popen( shlex.split(command), stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True, env=runtimeEnv, cwd=buildDirectory)
		else:
			process = subprocess.Popen( shlex.split(command), stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=runtimeEnv, cwd=buildDirectory)
		stdout, stderr = process.communicate()
		# Is it necessary to run tests? (tests must be enabled per configuration and CTest must report more than 1 test)
		if not self.config.getboolean('Test', 'testsEnabled') or re.search('Total Tests: 0', stdout, re.MULTILINE):
			# Copy in the skeleton file to keep Jenkins happy
			unitTestSkeleton = os.path.join( self.scriptsLocation, 'templates', 'JUnitTestResults-Success.xml' )
			shutil.copyfile( unitTestSkeleton, junitFilename )
			# All done
			return

		# Spawn a base user interface if needed
		if self.use_xorg_environment():
			# Setup Xvfb
			runtimeEnv['DISPLAY'] = self.config.get('Test', 'xvfbDisplayName')
			command = self.config.get('Test', 'xvfbCommand')
			xvfbProcess = subprocess.Popen( shlex.split(command), stdout=open(os.devnull, 'w'), stderr=subprocess.STDOUT, env=runtimeEnv )
			
			# Startup a Window Manager
			command = self.config.get('Test', 'wmCommand')
			wmProcess = subprocess.Popen( shlex.split(command), stdout=open(os.devnull, 'w'), stderr=subprocess.STDOUT, env=runtimeEnv )
			
		# Spawn D-Bus if we need to...
		if self.platform_uses_dbus():
		# Startup D-Bus and ensure the environment is adjusted
		#OSX requires two commands to accomplish this task
			if sys.platform == "darwin":
				command = self.config.get('Test', 'dbusloadCommand')
				process = subprocess.Popen( shlex.split(command), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, env=runtimeEnv )
				command = self.config.get('Test', 'dbusLaunchCommand')
				process = subprocess.Popen( shlex.split(command), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, env=runtimeEnv )
				for variable in process.stdout:					
					runtimeEnv['DBUS_LAUNCHD_SESSION_BUS_SOCKET'] = variable
			else:
				command = self.config.get('Test', 'dbusLaunchCommand')
				process = subprocess.Popen( shlex.split(command), stdout=subprocess.PIPE, stderr=subprocess.STDOUT, env=runtimeEnv )
				process.wait()
				for variable in process.stdout:
					splitVars = variable.split('=', 1)
					runtimeEnv[ splitVars[0] ] = splitVars[1].strip()			

		# Rebuild the Sycoca
		command = self.config.get('Test', 'kbuildsycocaCommand')
		try:
			process = subprocess.Popen( shlex.split(command), stdout=open(os.devnull, 'w'), stderr=subprocess.STDOUT, env=runtimeEnv )
			process.wait()
		except OSError:
			pass

		# Fire-up kdeinit and nepomuk
		kdeinitCommand = self.config.get('Test', 'kdeinitCommand')
		nepomukCommand = self.config.get('Test', 'nepomukCommand')
		try:
			subprocess.Popen( shlex.split(kdeinitCommand), stdout=open(os.devnull, 'w'), stderr=subprocess.STDOUT, env=runtimeEnv )
			subprocess.Popen( shlex.split(nepomukCommand), stdout=open(os.devnull, 'w'), stderr=subprocess.STDOUT, env=runtimeEnv )
		except OSError:
			pass

		# Sleep for a little while, to let kdeinit / nepomuk complete their startup processes
		time.sleep( self.config.getint('Test', 'kdeStartupWait') )

		# Execute CTest
		command = self.config.get('Test', 'ctestRunCommand')
                #Windows ignore PATH set in env with shell=False(default). This is a quick dirty fix to move forward. Needs to be improved.
		if sys.platform == "win32":
			ctestProcess = subprocess.Popen( shlex.split(command), stdout=sys.stdout, stderr=sys.stderr, shell=True, cwd=buildDirectory, env=runtimeEnv )
		else:
			ctestProcess = subprocess.Popen( shlex.split(command), stdout=sys.stdout, stderr=sys.stderr, cwd=buildDirectory, env=runtimeEnv )

		# Determine the maximum amount of time we will permit CTest to run for
		# To accomodate for possible inconsistencies, we allow an extra 2 lots of the permissible time per test
		testsFound = re.search('Total Tests: ([0-9]+)', stdout, re.MULTILINE).group(1)
		permittedTime = ( int(testsFound) + 2 ) * self.config.getint('Test', 'testTimePermitted')
		# Start timing it
		timeRunning = 0
		while timeRunning < permittedTime and ctestProcess.poll() is None:
			time.sleep(1)
			timeRunning += 1

		# Is it still running? 
		if ctestProcess.returncode is None:			
			# Kill it
			ctestProcess.kill()
			# Copy in the failure skeleton file to keep Jenkins happy
			unitTestSkeleton = os.path.join( self.scriptsLocation, 'templates', 'JUnitTestResults-Failure.xml' )
			shutil.copyfile( unitTestSkeleton, junitFilename )
			# Report our failure - overruns are not permitted
			return

		# Transform the CTest output into JUnit output
		junitOutput = self.convert_ctest_to_junit( buildDirectory )
		with open(junitFilename, 'w') as junitFile:
			junitFile.write( str(junitOutput) )

		# Tidy up all the other processes if we can
		if self.platform_uses_dbus():
			command = self.config.get('Test', 'terminateTestEnvCommand')
			subprocess.Popen( shlex.split(command) )

		# Shutdown the X processes too
		if self.use_xorg_environment():
			wmProcess.terminate()
			xvfbProcess.terminate()

	def convert_ctest_to_junit(self, buildDirectory):
		# Where is the base prefix for all test data for this project located?
		testDataDirectory = os.path.join( buildDirectory, 'Testing' )

		# Determine where we will find the test run data for the latest run
		filename = os.path.join( testDataDirectory, 'TAG' )
		with open(filename, 'r') as tagFile:
			testDirectoryName = tagFile.readline().strip()

		# Open the test result XML and load it
		filename = os.path.join( testDataDirectory, testDirectoryName, 'Test.xml' )
		with open(filename , 'r') as xmlFile:
			xmlDocument = etree.parse( xmlFile )

		# Load the XSLT file		
		filename = os.path.join(self.scriptsLocation, 'templates', 'ctesttojunit.xsl' )
		with open(filename, 'r') as xslFile:
			xslContent = xslFile.read()
			xsltRoot = etree.XML(xslContent)

		# Transform the CTest XML into JUnit XML
		transform = etree.XSLT(xsltRoot)
		return transform(xmlDocument)

	def execute_cppcheck(self):
		# Prepare to do the cppcheck run
		cpuCount = multiprocessing.cpu_count()
		buildDirectory = self.build_directory()
		runtimeEnv = self.generate_environment(True)
		cppcheckFilename = os.path.join( self.build_directory(), 'cppcheck.xml' )

		# Are we able to run cppcheck?
		if not self.config.getboolean('QualityCheck', 'runCppcheck') or self.projectSources == buildDirectory:		
			# Add a empty template
			cppcheckSkeleton = os.path.join( self.scriptsLocation, 'templates', 'cppcheck-empty.xml' )
			shutil.copyfile( cppcheckSkeleton, cppcheckFilename )
			return

		# Prepare the command
		command = self.config.get('QualityCheck', 'cppcheckCommand')		
		command = command.format( cpuCount=cpuCount, sources=self.projectSources, buildDirectory=buildDirectory )
		command = shlex.split(command)

		# Run cppcheck and wait for it to finish
		with open(cppcheckFilename, 'w') as cppcheckXml:
        #Windows ignore PATH set in env with shell=False(default). This is a quick dirty fix to move forward. Needs to be improved.
			if sys.platform == "win32":
				process = subprocess.check_call( command, stdout=sys.stdout, stderr=sys.stderr, shell=True, cwd=buildDirectory, env=buildEnv )
			else:
				process = subprocess.Popen( command, stdout=sys.stdout, stderr=cppcheckXml, cwd=self.projectSources, env=runtimeEnv )
				process.wait()

	def generate_lcov_data_in_cobertura_format(self):
		# Prepare to execute gcovr
		coberturaFile = os.path.join( self.build_directory(), 'CoberturaLcovResults.xml' )
		command = self.config.get('QualityCheck', 'gcovrCommand')		
		command = command.format( scriptsLocation=self.scriptsLocation, sources=self.projectSources )
		command = shlex.split(command)

		# Run gcovr to gather up the lcov data and present it in Cobertura format
		with open(coberturaFile, 'w') as coberturaXml:
			process = subprocess.Popen( command, stdout=coberturaXml, stderr=sys.stderr, cwd=self.projectSources )
			process.wait()

	def extract_dependency_information(self):
		# Prepare our execution parameters
		runtimeEnv = self.generate_environment(runtime=True)
		outputDirectory = os.path.join( self.projectSources, 'dotdata' )

		# Prepare to execute depdiagram-prepare
		command = self.config.get('DependencyInformation', 'extractionCommand')
		command = command.format( sources=self.projectSources.replace("\\", "/"), outputDir=outputDirectory.replace("\\", "/"), scriptsLocation=self.scriptsLocation )
		command = shlex.split(command)

		# Run depdiagram-prepare to extract the dependency information in *.dot format
		process = subprocess.Popen( command, stdout=sys.stdout, stderr=sys.stderr, env=runtimeEnv )
		process.wait()

		# Next we ensure the remote directory exists
		serverPath = self.project_prefix( self.project, local=False, includeHost=False, section='DependencyInformation' )
		command = self.config.get('General', 'createRemotePathCommand').format( remotePath=serverPath, home=expanduser("~") )
		process = subprocess.Popen( shlex.split(command), stdout=sys.stdout, stderr=sys.stderr)
		process.wait()

		# Now we need to transfer the data to it's final home, so it can be picked up by the API generation runs
		serverPath = self.project_prefix( self.project, local=False, section='DependencyInformation' )
		return self.perform_rsync( source=outputDirectory, destination=serverPath )

	def extract_cmake_dependency_metadata(self):
		# Prepare to start work...
		runtimeEnv = self.generate_environment(runtime=True)
		buildDirectory = self.build_directory()
		serverPath = self.config.get('CMakeDependencyMetadata', 'remoteHostPrefix')
		metadataPath = self.config.get('CMakeDependencyMetadata', 'localPrefix')

		# Update the dependency metadata first
		if not self.perform_rsync( source=serverPath, destination=metadataPath ):
			return False

		# Get the file we will be storing the resulting JSON in
		metaFilename = self.config.get('CMakeDependencyMetadata', 'metadataFilename')
		metaFilename = metaFilename.format( project=self.project.identifier, branchGroup=self.branchGroup )
		metaFilename = os.path.join( metadataPath, metaFilename )

		# Build up our command
		command = self.config.get('CMakeDependencyMetadata', 'extractionCommand')
		command = command.format( scriptsLocation=self.scriptsLocation )
		command = shlex.split(command)

		# Run depdiagram-prepare to extract the dependency information in *.dot format
		with open(metaFilename, 'w') as metadataOutput:
			process = subprocess.Popen( command, stdout=metadataOutput, stderr=metadataOutput, env=runtimeEnv, cwd=buildDirectory )
			process.wait()

		# Now we need to transfer the data to it's final home, so it can be picked up by the API generation runs
		return self.perform_rsync( source=metadataPath, destination=serverPath )

		
	# Check if the current platform we are running on needs an X environment
	def use_xorg_environment(self):
		# Linux systems use Xorg
		if sys.platform == 'linux2':
			return True

		# Assume everything else does not (Mac and Windows for instance)
		return False

	# Check if the current platform we are running on should be using D-Bus
	def platform_uses_dbus(self):
		# Windows 
		if sys.platform == 'win32':
			return False

		# Assume everything else does
		return True

# Loads a configuration for a given project
# def load_project_configuration( project, branchGroup, platform, compiler, variation = None ):
# 	# Create a configuration parser
# 	config = ConfigParser.SafeConfigParser()
# 	# List of prospective files to parse
# 	configFiles =  ['global.cfg', '{compiler}.cfg', '{platform}.cfg', '{branchGroup}.cfg', '{host}.cfg']
# 	configFiles += ['{branchGroup}-{platform}.cfg']
# 	configFiles += ['{project}/project.cfg', '{project}/{platform}.cfg', '{project}/{variation}.cfg', '{project}/{branchGroup}.cfg']
# 	configFiles += ['{project}/{branchGroup}-{platform}.cfg', '{project}/{branchGroup}-{variation}.cfg']
# 	# Go over the list and load in what we can
# 	for confFile in configFiles:
# 		confFile = confFile.format( host=socket.gethostname(), branchGroup=branchGroup, compiler=compiler, platform=platform, project=project, variation=variation )
# 		config.read( 'config/build/' + confFile )		
# 	# All done, return the configuration		
# 	return config

# # Loads the projects
# def load_projects( projectFile, projectFileUrl, configDirectory, moduleStructure ):
# 	# Download the list of projects if necessary
# 	if not os.path.exists(projectFile) or time.time() > os.path.getmtime(projectFile) + 60*60:
# 		urllib.request.urlretrieve(projectFileUrl, projectFile)
# 
# 	# Now load the list of projects into the project manager
# 	with open(projectFile, 'r') as fileHandle:
# 		print( fileHandle )
# 		try:
# 			ProjectManager.load_projects( etree.parse(fileHandle) )
# 		except:
# 			os.remove(projectFile)
# 			return False
# 
# 	# Load the branch group data now
# 	with open(moduleStructure, 'r') as fileHandle:
# 		ProjectManager.setup_branch_groups( json.load(fileHandle) )
# 
# 	# Finally, load special projects
# 	for dirname, dirnames, filenames in os.walk( configDirectory ):
# 		for filename in filenames:
# 			filePath = os.path.join( dirname, filename )
# 			ProjectManager.load_extra_project( filePath )
# 
# 	# We are successful
# 	return True

# Load dependencies


def load_project_dependencies( baseDepDirectory, baseName, platform, globalDepDirectory ):
	# Load base specific dependencies
	with open( baseDepDirectory + baseName, 'r' ) as fileHandle:
		ProjectManager.setup_dependencies( fileHandle )

	# Load the local list of ignored projects
	with open( baseDepDirectory + 'ignore', 'r' ) as fileHandle:
		print(fileHandle)
		ProjectManager.setup_ignored( fileHandle )

	# Load the global list of ignored projects
	with open( globalDepDirectory + 'build-script-ignore', 'r' ) as fileHandle:
		ProjectManager.setup_ignored( fileHandle )

	# Load the dependencies - common ones first
	with open( globalDepDirectory + 'dependency-data-common', 'r' ) as fileHandle:
		ProjectManager.setup_dependencies( fileHandle )

	# Load the dependencies - now branch group specific ones
	with open( globalDepDirectory + 'dependency-data-' + baseName, 'r' ) as fileHandle:
		ProjectManager.setup_dependencies( fileHandle )
	# Load the dependencies - now platform specific ones if it exists
	if os.path.isfile( baseDepDirectory + baseName + '-' + platform ) == True:
	    with open( baseDepDirectory + baseName  + '-' + platform, 'r' ) as fileHandle:
	    	ProjectManager.setup_dependencies( fileHandle )
	if os.path.isfile(  globalDepDirectory + 'dependency-data-' + baseName  + '-' + platform ) == True:
	    with open(  globalDepDirectory + 'dependency-data-' + baseName  + '-' + platform, 'r' ) as fileHandle:
	    	ProjectManager.setup_dependencies( fileHandle )

# Checks for a Jenkins environment, and sets up a argparse.Namespace appropriately if found
# def check_jenkins_environment():
# 	# Prepare
# 	arguments = argparse.Namespace()
# 
# 	# Do we have a job name?
# 	if 'JOB_NAME' in os.environ:
# 		# Split it out
# 		jobMatch = re.match("(?P<project>[^_]+)_?(?P<branch>[^_]+)?_?(?P<base>[^_/]+)?", os.environ['JOB_NAME'])
# 		# Now transfer in any non-None attributes
# 		# If we have the project name, transfer it
# 		if jobMatch.group('project') is not None:
# 			arguments.project = jobMatch.group('project')
# 		# Determine our branch group, based on the given branch/base combo
# 		if jobMatch.group('base') == 'qt5':
# 			if jobMatch.group('branch') == 'stable':
# 				arguments.branchGroup = 'stable-kf5-qt5'
# 			else:
# 				arguments.branchGroup = 'kf5-qt5'
# 		elif jobMatch.group('branch') == 'oldstable':
# 			arguments.branchGroup = 'oldstable-qt4'
# 		elif jobMatch.group('branch') == 'stable':
# 			arguments.branchGroup = 'stable-qt4'
# 		elif jobMatch.group('branch') == 'master':
# 			arguments.branchGroup = 'latest-qt4'
# 
# 	# Do we have a workspace?
# 	if 'WORKSPACE' in os.environ:
# 		# Transfer it
# 		arguments.sources = os.environ['WORKSPACE']
# 
# 	# Do we have a build variation?
# 	if 'Variation' in os.environ:
# 		# We need this to determine our specific build variation
# 		arguments.variation = os.environ['Variation']
# 
# 	# Do we need to change into the proper working directory?
# 	if 'JENKINS_SLAVE_HOME' in os.environ:
# 		# Change working directory
# 		os.chdir( os.environ['JENKINS_SLAVE_HOME'] )
# 
# 	return arguments
