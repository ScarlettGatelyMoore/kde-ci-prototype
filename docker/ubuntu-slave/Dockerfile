FROM ubuntu:wily
MAINTAINER Scarlett Clark <sgclark@kde.org>

#Install Essentials up to qt5
RUN apt-get update && apt-get upgrade -y
RUN apt-get -y build-dep qtbase5-dev
RUN apt-get -y install ssh \
	sudo \
	wget \
	build-essential \
	default-jre-headless \
	git \	
	xvfb \
	openbox \
	dbus-x11 \
	gnupg2 \
	gnupg-agent 

RUN mkdir -p /var/run/sshd
RUN /usr/sbin/sshd
RUN useradd -m -d /home/jenkins jenkins
RUN echo 'jenkins:1234' | chpasswd #changeme

#Our scripts install to here:
RUN mkdir /srv/jenkins && chown jenkins:jenkins /srv/jenkins

EXPOSE 22

VOLUME /home/jenkins:/home/jenkins HOME
VOLUME /srv/jenkins:/srv/jenkins INSTALL