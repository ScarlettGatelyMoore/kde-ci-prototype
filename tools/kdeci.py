#
# <one line to give the library's name and an idea of what it does.>
# Copyright 2015  <copyright holder> <email>
# 
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License as
# published by the Free Software Foundation; either version 2 of
# the License or (at your option) version 3 or any later version
# accepted by the membership of KDE e.V. (or its successor approved
# by the membership of KDE e.V.), which shall act as a proxy
# defined in Section 14 of version 3 of the license.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
#
# Python library to help manage and run the KDE Continuous Integration system
import os
import sys
import time
import copy
import json
import shlex
import shutil
import argparse
import subprocess
from distutils import dir_util
from collections import defaultdict
from os.path import expanduser
import urllib2
from pprint import pprint
import socket

# Settings
hostname = socket.gethostname()

class LoadData(object):
    def load_project_configuration( project, branchGroup, platform, compiler, variation = None ):
        # Create a configuration parser
        config = ConfigParser.SafeConfigParser()
        # List of prospective files to parse
        configFiles =  ['global.cfg', '{compiler}.cfg', '{platform}.cfg', '{branchGroup}.cfg', '{host}.cfg']
        configFiles += ['{branchGroup}-{platform}.cfg']
        configFiles += ['{project}/project.cfg', '{project}/{platform}.cfg', '{project}/{variation}.cfg', '{project}/{branchGroup}.cfg']
        configFiles += ['{project}/{branchGroup}-{platform}.cfg', '{project}/{branchGroup}-{variation}.cfg']
        # Go over the list and load in what we can
        for confFile in configFiles:
            confFile = confFile.format( host=socket.gethostname(), branchGroup=branchGroup, compiler=compiler, platform=platform, project=project, variation=variation )
            config.read( 'config/build/' + confFile )        
            # All done, return the configuration        
            return config
        
    def load_all_projects( projectFile, configDirectory):
        data_file = json.loads(open(projectFile).read()) 
    # Now load the list of projects into the project manager    
        try:
            ProjectManager.load_projects( data_file )
        except:          
            return False

#     # Load the branch group data now
#     with open(moduleStructure, 'r') as fileHandle:
#         ProjectManager.setup_branch_groups( json.load(fileHandle) )

        # Finally, load special projects
        for dirname, dirnames, filenames in os.walk( configDirectory ):
            for filename in filenames:
                filePath = os.path.join( dirname, filename )
                ProjectManager.load_extra_project( filePath )

                # We are successful
                return True

    def check_jenkins_environment():
        # Prepare
        arguments = argparse.Namespace()

        # Do we have a job name?
        if 'JOB_NAME' in os.environ:
            # Split it out
            jobMatch = re.match("(?P<project>[^_]+)_?(?P<branch>[^_]+)?_?(?P<branchGroup>[^_/]+)?", os.environ['JOB_NAME'])
            # Now transfer in any non-None attributes
            # If we have the project name, transfer it
            if jobMatch.group('project') is not None:
                arguments.project = jobMatch.group('project')
                # Determine our branch group, based on the given branch/base combo
                arguments.branchGroup = jobMatch.group('branchGroup')             

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

class ProjectManager(object):
    @staticmethod
    def load_projects(data_file):
        # Get a list of all repositories, then create projects for them
        
        for x in data_file:
            repoData = (x['repositories'])
            for repos in repoData:
                pprint( repos )
                projectData = repos.getParent()
                pprint( projectData )