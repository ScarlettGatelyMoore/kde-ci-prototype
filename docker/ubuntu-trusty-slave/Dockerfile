FROM ubuntu:trusty
MAINTAINER Scarlett Clark <sgclark@kde.org>

#Strange changes and deb-src is no longer in base..
RUN echo "deb-src http://us.archive.ubuntu.com/ubuntu/ trusty main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb-src http://ca.archive.ubuntu.com/ubuntu/ trusty main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb http://ca.archive.ubuntu.com/ubuntu/ trusty-updates main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb-src http://ca.archive.ubuntu.com/ubuntu/ trusty-updates main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb http://us.archive.ubuntu.com/ubuntu/ trusty-backports main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb-src http://us.archive.ubuntu.com/ubuntu/ trusty-backports main restricted universe multiverse" >> /etc/apt/sources.list

#Adding any need external PPAs here. We do not want any unecessary installs from these ppa's.
#We want appstream for the test. We want ffmpeg for *a project*, trusty at the time of release uses a forked version that
#is not compatible.. # Want libindi only from this PPA
# Yes we are doing this again. We want libexiv2 for libkgeomap, they want a version that will never make into trusty.
# We also do not want anything else from mutlaqja ppa (# currently disabled, breaks image)
RUN apt-get -y install software-properties-common \
	&& add-apt-repository -y ppa:ximion/packagekit \	 
	&& add-apt-repository -y ppa:anton+/photo-video-apps \	
	&& add-apt-repository -y ppa:mc3man/trusty-media \
	&& add-apt-repository -y ppa:mutlaqja/ppa \	
	&& apt-get update 
	&& DEBIAN_FRONTEND=noninteractive apt-get -y install appstream libexiv2-dev libindi-dev ffmpeg
	
# Needed for building qt
RUN apt-get update && apt-get upgrade -y && apt-get -y build-dep libqt4-dev
# Dependencies for ALL KDE applications.
RUN DEBIAN_FRONTEND=noninteractive apt-get -y install openssh-server \
    ssh \ 
    wget \	
    openjdk-7-jdk \
    git \
    language-pack-en \
    xvfb \
    openbox \
    dbus-x11 \
    gnupg2 \
    gnupg-agent \
    psmisc \
    doxygen \
    bzr \
    cppcheck \
    gperf \
    bison \
    ruby \    
    libpolkit-agent-1-dev \
    libpolkit-backend-1-dev \
    libpolkit-gobject-1-dev \   
    docbook-xml \
    docbook-xsl \  
    gcovr \
    libnm-glib-dev \
    libnm-util-dev \
    libopenal-dev  \
    libsndfile1-dev \
    libattr1-dev \
    libxapian-dev \
    xsltproc \
    libgpgme11-dev \
    libsasl2-dev \
    libdmtx-dev \
    libqrencode-dev \
    libldap2-dev \
    libfakekey-dev \
    python3-dev \
    libclang-3.5-dev \
    libcdparanoia-dev \
    clang \
    libical-dev \
    libxcb-composite0-dev \
    libxcb-damage0-dev \
    libxcb-dpms0-dev \
    libraw-dev \
    libxcb-image0-dev \
    libxcb-xkb-dev \
    libxkbfile-dev \
    libxcb-keysyms1-dev \
    libtag1-dev \
    libsamplerate0-dev \
    frei0r-plugins-dev \
    libeigen3-dev \
    libgettextpo-dev \
    libaccounts-glib-dev \    
    libboost1.55-all-dev \
    libglew-dev \
    libclucene-dev \
    libsane-dev \
    libhunspell-dev \
    xserver-xorg-input-wacom \
    texlive-latex-recommended \
    libtelepathy-glib-dev \
    libgstreamer1.0-dev \   
    libsvn-dev \
    libpci-dev \
    cmake \
    libacl1-dev \
    libgstreamer-plugins-base1.0-dev \
    libtelepathy-farstream-dev \
    libtelepathy-glib-dev \
    intltool \
    libeigen2-dev \  
    frei0r-plugins-dev \  
    libexif-dev \    
    libsdl1.2-dev \
    libsox-dev \
    ladspa-sdk \   
    virtuoso-opensource \
    libxss-dev \
    packagekit \
    python-numpy \
    libxcb-xtest0-dev \
    libspeechd-dev \
    libbz2-dev \
    libcap-dev \
    ruby-dev \
    libnss3-dev \
    xserver-xorg-input-wacom \
    libepoxy-dev \
    libarchive-dev \
    libcfitsio3-dev \    
    wcslib-dev \
    xplanet \
    astrometry.net \
    mercurial \
    libwlocate-dev \
    libshp-dev \
    libgps-dev \
    libprotobuf-dev \
    protobuf-compiler \
    libgif-dev \
    libgtk-3-dev \
    libclang-3.5-dev \
    libgsl0-dev \
    fontforge \
    libblkid-dev \
    libatasmart-dev \
    libparted-dev \
    sip-dev \
    libtelepathy-logger-dev \
    modemmanager-dev \
    libpgf-dev \
    libvcdinfo-dev \    
    libxerces-c-dev \
    xsdcxx swig3.0 \
    mono-gmcs \
    libshp-dev \
    libevent-pthreads-2.0-5 \
    libtool \
    gtk-doc-tools \   
    libtelepathy-glib-dev \
    libclang-3.6-dev \
    llvm \
    libgraphviz-dev \
    libmtp-dev \
    libmusicbrainz3-dev \
    libneon27-dev \
    libkrb5-dev \
    libcurl4-gnutls-dev \
    librdf-storage-virtuoso \
    librdf0-dev \  
    librasqal3-dev \
    libraptor2-dev \
    libvncserver-dev \
    libgpgme++2 \
    subversion \
    libotr5-dev \
    python3-numpy \    
    libinput-pad-dev \
    libxcb-cursor-dev \
    libspnav-dev \
    libxbase64-dev \   
    libgv-python \
    libofx-dev \
    liblmdb-dev \
    sqlite3 \
    liblensfun-dev \
    libjasper-dev \
    mysql-server \
    xinput \
    texlive-latex-recommended \
    libepub-dev \
    libxcb-util0-dev \    
    python-lxml \     
    libfam-dev \
    media-player-info \
    swig \
    liblmdb-dev \
    libsearchclient-dev \
    libgraphicsmagick++1-dev \
    libmagick++-dev \
    libgphoto2-dev \
    libavcodec-dev \
    libavformat-dev \
    libavdevice-dev \
    libavutil-dev \
    libswscale-dev \
    libpostproc-dev \   
    libaspell-dev \
    libenchant-dev \
    hspell \
    libhupnp-dev \
    libgit2-dev \
    libsqlcipher-dev \
    python-sip \
    gcovr \
    libssh-dev

RUN gem install cucumber 
 
RUN mkdir -p /var/run/sshd
RUN mkdir -p /tmp/1000-runtime-dir
RUN groupadd -g 1000 jenkins
RUN useradd -m -u 1000 -g 1000 -d /home/jenkins jenkins
RUN echo 'jenkins:1234' | chpasswd #changeme
RUN chown jenkins.jenkins /tmp/1000-runtime-dir
RUN sed s,exit\ 101,exit\ 0, /usr/sbin/policy-rc.d -i
RUN echo 'export LANG="en_US.UTF-8"' >> /etc/profile
RUN dpkg-reconfigure openssh-server

EXPOSE 22

CMD ["/usr/sbin/sshd", "-D"]
