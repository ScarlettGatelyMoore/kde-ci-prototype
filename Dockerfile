FROM ubuntu:14.10
#Original MAINTAINER Michael Neale <mneale@cloudbees.com>
MAINTAINER Scarlett Clark <sglcark@kubuntu.org>
# Grab jenkins apt sources
RUN apt-get update
RUN echo deb http://pkg.jenkins-ci.org/debian binary/ >> /etc/apt/sources.list
RUN apt-get install -y wget
RUN wget -q -O - http://pkg.jenkins-ci.org/debian/jenkins-ci.org.key | apt-key add -

#Install all base system requirements for python, cmake, Qt, tests, and random stuff scripts need.
RUN apt-get update && apt-get -y upgrade && apt-get -y install jenkins python docker python-lxml openssh-server build-essential git mercurial xvfb python-xvfbwrapper openbox dbus-x11 && apt-get -y build-dep qtbase5-dev libqt4-dev

# Make the jenkins user
RUN usermod -m -d /home/jenkins jenkins
RUN sed s,JENKINS_HOME=/var/lib/jenkins,JENKINS_HOME=/home/jenkins, /etc/default/jenkins -i
RUN mkdir /srv/jenkins && chown jenkins.jenkins /srv/jenkins
ENV JENKINS_HOME /home/jenkins

#Attempt at getting ssh to work - currently broken.
RUN mkdir -p /var/run/sshd
EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]

#temp disabled - kde jar is not working
#ADD http://build.kde.org/jnlpJars/jenkins-cli.jar /home/jenkins/
#RUN chmod 644 /home/jenkins/jenkins-cli.jar

#grab jenkins war instead
ADD http://mirrors.jenkins-ci.org/war-stable/latest/jenkins.war /home/jenkins/jenkins.war
RUN chmod 644 /home/jenkins/jenkins.war

# Following commands need to be run under the jenkins user
USER jenkins

#grab the kde build/config scripts
RUN cd $HOME && git clone git://anongit.kde.org/websites/build-kde-org
RUN cd $HOME && cd build-kde-org && git checkout production && /bin/bash update-setup.sh

# key needed for build scripts
RUN ssh-keygen -t rsa -N "" -f $HOME/jenkins-private.key


## passwordless login for jenkins more attempt for ssh - not working
RUN mkdir -p /home/jenkins/.ssh
RUN ssh-keygen -N '' -f /home/jenkins/.ssh/id_dsa
RUN cat /home/jenkins/.ssh/id_dsa.pub >> /home/jenkins/.ssh/authorized_keys
RUN echo -n "localhost " > /home/jenkins/.ssh/known_hosts
RUN cat /etc/ssh/ssh_host_rsa_key.pub >> /home/jenkins/.ssh/known_hosts

# Allows access to localhost:8080 on the host
ENTRYPOINT java -jar /home/jenkins/jenkins.war
