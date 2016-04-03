FROM debian:stable
MAINTAINER Scarlett Clark <sgclark@kde.org>

RUN echo "deb-src http://httpredir.debian.org/debian jessie main" >> /etc/apt/sources.list
#RUN echo "deb-src http://security.debian.org/debian updates/jessie main" >> /etc/apt/sources.list
	
# Needed for building qt
RUN apt-get update && apt-get upgrade -y && apt-get -y build-dep qtbase5-dev
# Dependencies for KF5 only, This is for kf5 minimum.
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install openssh-server \
    ssh \ 
    wget \	
    openjdk-7-jdk \
    git     
 
RUN mkdir -p /var/run/sshd
RUN mkdir -p /tmp
RUN mkdir -p /srv/jenkins
RUN mkdir -p /home/jenkins/.ssh
RUN mkdir -p /home/jenkins/scripts

RUN groupadd -g 1000 jenkins
RUN useradd -m -u 1000 -g 1000 -d /home/jenkins jenkins
RUN echo 'jenkins:1234' | chpasswd #changeme
RUN chown jenkins.jenkins /tmp
RUN chown jenkins.jenkins /srv/jenkins
RUN chown jenkins.jenkins /home/jenkins/.ssh
RUN chown jenkins.jenkins /home/jenkins/scripts
RUN sed s,exit\ 101,exit\ 0, /usr/sbin/policy-rc.d -i
RUN dpkg-reconfigure openssh-server

EXPOSE 22

CMD ["/usr/sbin/sshd", "-D"]
