#!/usr/bin/python3
import sys
import os
import subprocess
import shutil
import socket
import os.path
import shlex
import configparser

# Settings
home = os.path.expanduser("~")
repo_name=""
EMERGE_BASE=""
EMERGE_ETC=""
scriptsLocation=home + "/scripts/"
def generate_server_config():
  config = configparser.ConfigParser()
  #Read in the defaults.
  config.read( scriptsLocation + 'tools/' + 'server.cfg' )
  #Get hostname and add .cfg to get file name.  
  hostConf=socket.gethostname() + '.cfg'
  #If the host has config overrides read it in.
  if os.path.isfile(scriptsLocation + 'tools/' + hostConf ):
    print(hostConf + " exists, using overrides")
    config.read( scriptsLocation + 'tools/' + hostConf )
  return config

#Set the variables
config=generate_server_config()
JENKINS_MASTER_REPO = config.get( 'Repo', 'jenkinsMasterRepo' )
JENKINS_CONFIG_REPO = config.get( 'Repo', 'jenkinsConfigRepo' )
JENKINS_METADATA_REPO = config.get( 'Repo', 'jenkinsMetadataRepo' )
JENKINS_BRANCH = config.get( 'Repo', 'jenkinsBranch' )
JENKINS_DEPENDENCY_BRANCH = config.get( 'Repo', 'jenkinsDepBranch' )
JENKINS_CONFIG_BRANCH = config.get( 'Repo', 'jenkinsConfigBranch' )

def getRepository(repo_name, repoUrl, repoBranch="master"):
  #Retrieve config settings 
  originalDir = os.getcwd()
  
  if repo_name == "scripts":
    repoPath=scriptsLocation
  else:
    repoPath=scriptsLocation + repo_name
    
  while not os.path.exists(os.path.join(repoPath, '.git')):  
    try:
      print("No valid repo exists in " + repoPath + " Cloning as requested.")
      command = "git clone %s %s" % (repoUrl, repoPath)
      process = subprocess.run( command )
      output = process.communicate()[0]
      print( output )
      command = "git checkout " + repoBranch
      print( "Checkout " + str(command) )
      process = subprocess.run( command )
      output = process.communicate()[0]
      print( output )
    except subprocess.CalledProcessError: 
      print( "subproccess CalledProcessError.output = " + str(sys.exc_info()[0]))
  while os.path.exists(os.path.join(repoPath, '.git')):  
    try:
      print("There appears to be a repo already in: " + repoPath + " Pulling instead")  
      os.chdir(repoPath)	
      command = "git checkout " + repoBranch
      print( "Checkout " + str(command) )
      process = subprocess.run( command )
      output = process.communicate()[0]
      print( output )
      command = "git pull origin " + repoBranch
      print( repoPath + "/.git exists " + str(command))
      process = subprocess.run( command )
      output = process.communicate()[0]
      print( output )
      os.chdir(originalDir)
    except subprocess.CalledProcessError: 
      print( "subproccess CalledProcessError.output = " + str(sys.exc_info()[0]))
			
  os.chdir(originalDir)			
	
getRepository("scripts", JENKINS_MASTER_REPO, JENKINS_BRANCH)
getRepository("dependencies", JENKINS_METADATA_REPO, JENKINS_DEPENDENCY_BRANCH)
getRepository("poppler-test-data", "git://git.freedesktop.org/git/poppler/test", JENKINS_DEPENDENCY_BRANCH)
getRepository("kapidox", "git://anongit.kde.org/kapidox", JENKINS_DEPENDENCY_BRANCH)
getRepository("config", JENKINS_CONFIG_REPO, JENKINS_CONFIG_BRANCH)

if sys.platform == "win32":
  settingsfile = scriptsLocation + "etc/kdesettings.ini"
  dstroot = EMERGE_ETC
  print( "Copying " + settingsfile + " to " + dstroot )
try:
  shutil.copy(settingsfile, dstroot)
except:
  print( "Oh No! Something went wrong" )
else: 
  print( "Copy was successful" )
  envfile = scriptsLocation + "emerge/kdeenv.bat"
  dstroot = EMERGE_BASE
  print( "Copying " + envfile + " to " + dstroot )
try:
  shutil.copy(envfile, dstroot)
except:
  print( "Oh No! Something went wrong" )
else: 
  print( "Copy was successful" )
