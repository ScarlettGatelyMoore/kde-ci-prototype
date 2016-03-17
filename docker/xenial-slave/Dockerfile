#Please update the address to the private registry
FROM ubuntu:16.04
MAINTAINER Scarlett Clark <sgclark@kubuntu.org>

#Install Essentials up to qt5

RUN apt-get update && apt-get -y install software-properties-common python-software-properties
RUN apt-get -f installadd-apt-repository -y ppa:george-edison55/cmake-3.x && add-apt-repository -y ppa:ximion/packagekit && apt-add-repository -y ppa:mutlaqja/ppa
RUN apt-get update && apt-get -y install openssh-server sudo wget build-essential openjdk-8-jdk git python-lxml xvfb openbox dbus-x11 psmisc doxygen bzr cppcheck gperf bison ruby flex libpolkit-agent-1-dev libpolkit-backend-1-dev libpolkit-gobject-1-dev libxslt1-dev docbook-xml docbook-xsl libgcrypt-dev libboost1.55-dev gcovr libnm-glib-dev libnm-util-dev libopenal-dev  libsndfile1-dev libexiv2-dev libattr1-dev libxapian-dev xsltproc libgpgme11-dev libsasl2-dev libdmtx-dev libqrencode-dev libldap2-dev libfakekey-dev python3-dev libclang-3.5-dev libcdparanoia-dev clang libical-dev libxcb-composite0-dev libxcb-damage0-dev libxcb-dpms0-dev libraw-dev libxcb-image0-dev libxcb-xkb-dev libxkbfile-dev libboost-system1.55-dev libtag1-dev libsamplerate0-dev frei0r-plugins-dev libeigen3-dev libgettextpo-dev libaccounts-glib-dev xinput libboost1.55-all-dev libglew-dev libavutil-ffmpeg-dev libavcodec-ffmpeg-dev libavdevice-ffmpeg-dev libavformat-ffmpeg-dev libavfilter-ffmpeg-dev libpostproc-ffmpeg-dev libswscale-ffmpeg-dev libswresample-ffmpeg-dev libavresample-ffmpeg-dev libclucene-dev libsane-dev libhunspell-dev xserver-xorg-input-wacom libtelepathy-glib-dev libgstreamer1.0-dev libfarstream-0.2-2 libsvn-dev libpci-dev cmake libacl1-dev libgstreamer-plugins-base1.0-dev libtelepathy-farstream-dev libtelepathy-glib-dev intltool libeigen2-dev libasound2-dev  build-essential pkg-config  frei0r-plugins-dev libgtk2.0-dev libexif-dev  libmovit-dev libsdl1.2-dev libsox-dev libxml2-dev  ladspa-sdk libcairo2-dev virtuoso-opensource librdf-storage-virtuoso librdf0-dev libiodbc2-dev libxss-dev packagekit python-numpy libxcb-xtest0-dev libspeechd-dev libbz2-dev libcap-dev ruby-dev libnss3-dev xserver-xorg-input-wacom libepoxy-dev libarchive-dev libcfitsio3-dev libindi-dev wcslib-dev xplanet astrometry.net mercurial libwlocate-dev libshp-dev libgps-dev libprotobuf-dev protobuf-compiler libgif-dev libgtk-3-dev libclang-3.5-dev libgsl0-dev fontforge libblkid-dev libatasmart-dev libparted-dev sip-dev libtelepathy-logger-dev modemmanager-dev libpgf-dev libvcdinfo-dev libgstreamer0.10-dev libxerces-c-dev xsdcxx swig3.0 mono-gmcs libshp-dev libevent-pthreads-2.0-5 libtool gtk-doc-tools libgstreamer-plugins-base0.10-dev libtelepathy-glib-dev libclang-3.6-dev llvm libgraphviz-dev libmtp-dev libmusicbrainz3-dev libvncserver-dev libgpgme++2 subversion libotr5-dev python3-numpy xserver-xephyr xwayland weston openbox libinput-dev libxcb-cursor-dev libspnav-dev libxbase64-dev libxt-dev libgv-python libofx-dev liblmdb-dev sqlite3 cucumber && apt-get -y build-dep qtbase5-dev liblensfun-dev libjasper-dev mysql-server xinput texlive-latex-recommended libepub-dev

#SSH I have master and slave on the same host so I had to get creative with ssh ports. update as needed.
EXPOSE 222

RUN mkdir -p /var/run/sshd

RUN useradd -m -d /home/jenkins jenkins
RUN sed 's/#Port\ 22/Port\ 222/' -i /etc/ssh/sshd_config
RUN /usr/sbin/sshd
RUN echo 'jenkins:1234' | chpasswd # Please update this before build.
RUN su - jenkins -c "mkdir -p /home/jenkins/.ssh"

#This keys need to be in path (aka generated on your host and placed in build folder)
ADD jenkins_install /home/jenkins/.ssh/
ADD jenkins_install.pub /home/jenkins/.ssh/
RUN chown jenkins:jenkins -R /home/jenkins/.ssh
RUN chmod 0700 /home/jenkins/.ssh && chmod 0600 /home/jenkins/.ssh/*

#Our scripts install to here:
RUN mkdir /srv/jenkins && chown jenkins:jenkins /srv/jenkins 

#We want jenkins user to be able to install dependencies on the fly.
RUN echo "jenkins ALL=NOPASSWD: /usr/bin/apt-get install" >> /etc/sudoers
RUN echo "APT::Get::force-yes "true";" >> /etc/apt/apt.conf.d/90forceyes

#This entrypoint is how jenkins master will be able to rsync dependencies through the scripts.
ENTRYPOINT ["/usr/sbin/sshd"]
CMD ["-D"]
