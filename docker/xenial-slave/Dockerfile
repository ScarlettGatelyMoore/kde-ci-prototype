#Please update the address to the private registry
FROM ubuntu:16.04
MAINTAINER Scarlett Clark <sgclark@kde.org>

#Strange changes and deb-src is no longer in base..
RUN echo "deb http://ca.archive.ubuntu.com/ubuntu/ xenial main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb-src http://ca.archive.ubuntu.com/ubuntu/ xenial main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb http://ca.archive.ubuntu.com/ubuntu/ xenial-updates main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb-src http://ca.archive.ubuntu.com/ubuntu/ xenial-updates main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb http://ca.archive.ubuntu.com/ubuntu/ xenial-backports main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb-src http://ca.archive.ubuntu.com/ubuntu/ xenial-backports main restricted universe multiverse" >> /etc/apt/sources.list
# Needed for add-apt-repository.
RUN apt-get update && apt-get -y install software-properties-common \
	&& add-apt-repository -y ppa:ximion/packagekit \
	&& add-apt-repository -y ppa:mutlaqja/ppa
# Needed for building qt5
RUN apt-get update && apt-get upgrade -y && apt-get -y build-dep qtbase5-dev
# Dependencies for ALL KDE applications.
RUN DEBIAN_FRONTEND=noninteractive apt-get -y --force-yes install \
    appstream \
    astrometry.net \
    bison \
    build-essential \    
    bzr \
    bzr \
    clang \
    cmake \
    cppcheck \
    cucumber \
    dbus-x11 \
    dmz-cursor-theme \
    dmz-cursor-theme \
    docbook-xml \
    docbook-xsl \
    doxygen \
    flex \
    fontforge \
    frei0r-plugins-dev \
    frei0r-plugins-dev \
    gcovr \       
    git \	
    gnupg-agent \
    gnupg2 \
    gobject-introspection \
    gperf \
    gtk-doc-tools \
    intltool \
    ladspa-sdk \   
    libaccounts-glib-dev \    
    libacl1-dev \
    libapt-pkg-dev \
    libapt-pkg-dev \
    libarchive-dev \
    libasound2-dev \
    libatasmart-dev \
    libattr1-dev \
    libavcodec-dev \
    libavdevice-dev \
    libavfilter-dev \
    libavformat-dev \
    libavutil-dev \
    libblkid-dev \
    libboost1.58-all-dev \
    libbz2-dev \
    libcap-dev \
    libcdparanoia-dev \
    libcfitsio3-dev \
    libclang-3.5-dev \
    libclang-3.5-dev \
    libclang-3.6-dev \
    libclucene-dev \
    libdmtx-dev \
    libeigen2-dev \
    libeigen3-dev \
    libelektra-dev \
    libepoxy-dev \
    libepub-dev \
    libevent-pthreads-2.0-5 \
    libexif-dev \
    libexiv2-dev \
    libfakekey-dev \
    libfam-dev \
    libfarstream-0.2-5 \
    libgcrypt-dev \    
    libgettextpo-dev \
    libgif-dev \
    libgirepository1.0-dev \
    libgit2-dev \
    libglew-dev \
    libgpgme++2v5 \
    libgpgme11-dev \
    libgphoto2-dev \
    libgps-dev \   
    libgraphicsmagick++1-dev \
    libgraphviz-dev \
    libgraphviz-dev \
    libgsl-dev \
    libgstreamer-plugins-base0.10-dev \
    libgstreamer-plugins-base1.0-dev \
    libgstreamer0.10-dev \
    libgstreamer1.0-dev \
    libgtk-3-dev \
    libgv-python \
    libhttp-parser-dev \
    libhunspell-dev \
    libical-dev \
    libindi-dev \
    libiodbc2-dev \
    libisocodes-dev \
    libjasper-dev \
    libldap2-dev \
    liblensfun-dev \
    liblmdb-dev \
    liblmdb-dev \    
    liblzma-dev \
    libmagick++-dev \
    libmagick++-dev \
    libmovit-dev \
    libmtp-dev \
    libmusicbrainz3-dev \
    libncurses5-dev \    
    libnm-dev \
    libnm-glib-dev \
    libnm-glib-vpn-dev \
    libnm-gtk-dev \
    libnm-util-dev \
    libnss3-dev \
    libofx-dev \
    libopenal-dev  \
    libopencv-core-dev \
    libopenexr-dev \
    libotr5-dev \
    libpam0g-dev \
    libparted-dev \
    libpci-dev \
    libpgf-dev \
    libpocketsphinx-dev \
    libpolkit-agent-1-dev \
    libpolkit-backend-1-dev \
    libpolkit-gobject-1-dev \
    libpostproc-dev \
    libprotobuf-dev \
    libqalculate-dev \
    libqrencode-dev \
    libraw-dev \      
    librdf-storage-virtuoso \
    librdf0-dev \
    libsamplerate0-dev \
    libsane-dev \
    libsasl2-dev \
    libsdl1.2-dev \
    libshp-dev \
    libshp-dev \
    libslp-dev \
    libsmbclient-dev \
    libsndfile1-dev \
    libsox-dev \
    libspeechd-dev \
    libspnav-dev \
    libsqlcipher-dev \
    libssh2-1-dev \
    libsvn-dev \
    libswresample-dev \
    libswscale-dev \
    libsystemd-dev \
    libtag1-dev \
    libtelepathy-farstream-dev \
    libtelepathy-glib-dev \
    libtelepathy-glib-dev \
    libtelepathy-glib-dev \
    libtelepathy-logger-dev \
    libtidy-dev \
    libtool \
    libvcdinfo-dev \
    libvncserver-dev \
    libwlocate-dev \
    libxapian-dev \
    libxbase64-dev \
    libxcb-composite0-dev \
    libxcb-cursor-dev \
    libxcb-damage0-dev \
    libxcb-dpms0-dev \
    libxcb-util0-dev \
    libxcb-xtest0-dev \
    libxerces-c-dev \
    libxinerama-dev \
    libxkbfile-dev \    
    libxml2-dev \
    libxmu-dev \
    libxslt1-dev \
    libxss-dev \
    libxt-dev \
    libyaml-dev \
    llvm \
    media-player-info \
    mercurial \
    mercurial \
    mesa-utils \
    mobile-broadband-provider-info \
    modemmanager-dev \
    mono-mcs \
    mysql-server \
    network-manager-dev \
    openbox \
    openjdk-8-jdk \
    openssh-server \
    p7zip
    packagekit \
    postgresql \
    protobuf-compiler \
    protobuf-compiler \
    psmisc \
    python-lxml \    
    python-numpy \
    python2.7 \
    python3-dev \
    python3-lxml \
    python3-numpy \
    python3.5-dev \
    ragel \
    ruby \
    ruby-dev \
    samba-common \
    sip-dev \
    sphinxsearch \
    sqlcipher \
    sqlite3 \
    ssh \    
    subversion \
    swig \
    texlive-latex-recommended \
    unzip \
    virtuoso-opensource \
    wcslib-dev \
    weston \    
    wget \	
    xinput \
    xmlto \
    xplanet \
    xsdcxx swig3.0 \
    xserver-xephyr \
    xserver-xorg-input-wacom \
    xserver-xorg-input-wacom \
    xsltproc \
    xwayland \
    
RUN mkdir -p /var/run/sshd
RUN groupadd -g 1000 jenkins
RUN useradd -m -u 1000 -g 1000 -d /home/jenkins jenkins
RUN echo 'jenkins:1234' | chpasswd #changeme

USER jenkins
RUN echo "eval \`dbus-launch --auto-syntax 2>&1\`" > ~/.bashrc
RUN mkdir /tmp/xdg-runtime-dir
RUN chown jenkins.jenkins /tmp/xdg-runtime-dir

USER root
RUN sed s,exit\ 101,exit\ 0, /usr/sbin/policy-rc.d -i
RUN dpkg-reconfigure openssh-server
EXPOSE 22

CMD ["/usr/sbin/sshd", "-D"]

