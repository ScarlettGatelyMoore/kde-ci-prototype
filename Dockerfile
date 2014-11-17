FROM ubuntu:14.10
#Original MAINTAINER Michael Neale <mneale@cloudbees.com>
MAINTAINER Scarlett Clark <sglcark@kubuntu.org>
# Grab jenkins apt sources
RUN apt-get update
RUN echo deb http://pkg.jenkins-ci.org/debian binary/ >> /etc/apt/sources.list
RUN apt-get install -y wget
RUN wget -q -O - http://pkg.jenkins-ci.org/debian/jenkins-ci.org.key | apt-key add -

#Install all base system requirements for python, cmake, Qt, tests, and random stuff scripts need.
RUN apt-get update && apt-get -y upgrade && apt-get -y install jenkins python docker python-lxml openssh-server build-essential git mercurial xvfb python-xvfbwrapper openbox dbus-x11 bzr && apt-get -y build-dep qtbase5-dev libqt4-dev

# Make the jenkins env
ENV JENKINS_HOME /var/lib/jenkins
RUN mkdir /opt/jenkins && chown jenkins.jenkins /opt/jenkins

#Attempt at getting ssh to work - currently broken.
RUN mkdir -p /var/run/sshd
CMD ["/usr/sbin/sshd", "-D"]
EXPOSE 22

#grab jenkins war and kde jenkins-cli
ADD http://mirrors.jenkins-ci.org/war-stable/latest/jenkins.war /opt/jenkins/jenkins.war
RUN chmod 644 /opt/jenkins/jenkins.war
ADD http://build.kde.org/jnlpJars/jenkins-cli.jar /opt/jenkins/jenkins-cli.jar
RUN chmod 644 /opt/jenkins/jenkins-cli.jar

# Following commands need to be run under the jenkins user
USER jenkins

COPY jenkins.sh /opt/jenkins/jenkins.sh
