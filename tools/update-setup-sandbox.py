#!/usr/bin/python
import sys
import os
import subprocess
import shutil
import socket
from os.path import expanduser
import shlex
from subprocess import STDOUT

# Settings

home = expanduser("~")
hostname = socket.gethostname()
repo_name=""
if sys.platform == "win32":
	scriptsLocation="D:/kderoot-live/scripts/"
	EMERGE_ETC="D:/kderoot-live/etc/"
	EMERGE_BASE="D:/kderoot-live"
else:
	scriptsLocation=home + "/scripts/"
	
JENKINS_BRANCH="ci-test-merge"
JENKINS_DEPENDENCY_BRANCH="master"


def getRepository(repo_name, repoUrl, repoBranch="master"):		
		if repo_name == "scripts":
			repoPath=scriptsLocation
		else:
			repoPath=scriptsLocation + repo_name
	
		if not os.path.exists(repoPath):
			os.mkdir(repoPath)			
			
		originalDir = os.getcwd()
		os.chdir(repoPath)
		if not os.path.exists(os.path.join(repoPath, '.git')):
			try:
				command = "git clone %s %s" % (repoUrl, repoPath)
				print repoPath + "/.git does not exist " + str(command)
				process = subprocess.Popen( command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True )	
				output = process.communicate()[0]
				print output
				command = "git checkout " + repoBranch
				print "Checkout " + str(command)
				process = subprocess.Popen( command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True )	
				output = process.communicate()[0]
				print output					
			except subprocess.CalledProcessError,e: print "subproccess CalledProcessError.output = " + str(e)			
			
		try:
			command = "git checkout " + repoBranch
			print "Checkout " + str(command)
			process = subprocess.Popen( command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True )	
			output = process.communicate()[0]
			print output
			command = "git pull origin " + repoBranch
			print repoPath + "/.git exists " + str(command)
			process = subprocess.Popen( command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True )	
			output = process.communicate()[0]
			print output											
		except subprocess.CalledProcessError,e: print "subproccess CalledProcessError.output = " + str(e)
			
		os.chdir(originalDir)			
	

getRepository("scripts", "git://anongit.kde.org/sysadmin/ci-tools-experimental", JENKINS_BRANCH)
getRepository("dependencies", "git://anongit.kde.org/kde-build-metadata", JENKINS_DEPENDENCY_BRANCH)
getRepository("poppler-test-data", "git://git.freedesktop.org/git/poppler/test", JENKINS_DEPENDENCY_BRANCH)
getRepository("kapidox", "git://anongit.kde.org/kapidox", JENKINS_DEPENDENCY_BRANCH)
getRepository("config", "git://anongit.kde.org/sysadmin/ci-builder-tools", 'nellie-migration')

if sys.platform == "win32":
	settingsfile = JENKINS_SLAVE_HOME + "etc/kdesettings.ini"
	dstroot = EMERGE_ETC
	print "Copying " + settingsfile + " to " + dstroot
	try:
	  shutil.copy(settingsfile, dstroot)
	except:
	  print "Oh No! Something went wrong"
	else: 
	  print "Copy was successful"
	envfile = JENKINS_SLAVE_HOME + "emerge/kdeenv.bat"
	dstroot = EMERGE_BASE
	print "Copying " + envfile + " to " + dstroot 
	try:
	  shutil.copy(envfile, dstroot)
	except:
	  print "Oh No! Something went wrong"
	else: 
	  print "Copy was successful"
