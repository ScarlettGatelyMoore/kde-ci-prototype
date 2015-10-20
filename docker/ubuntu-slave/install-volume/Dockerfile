#DATA ONLY CONTAINER
# This container holds the install sync data in /srv/jenkins
# If loading a backup the files need to be in the build dir
FROM busybox
MAINTAINER Scarlett Clark <sgclark@kubuntu.org>

RUN adduser -g 1000 -u 1000 jenkins 
RUN chown -R jenkins.jenkins /srv/jenkins 

VOLUME /srv/jenkins

CMD ["/bin/sh"]
CMD ["true"]
