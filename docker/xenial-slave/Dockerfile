#Please update the address to the private registry
FROM ubuntu:16.04
MAINTAINER Scarlett Clark <sgclark@kde.org>

#Strange changes and deb-src is no longer in base..
RUN echo "deb http://ca.archive.ubuntu.com/ubuntu/ wily main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb-src http://ca.archive.ubuntu.com/ubuntu/ wily main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb http://ca.archive.ubuntu.com/ubuntu/ wily-updates main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb-src http://ca.archive.ubuntu.com/ubuntu/ wily-updates main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb http://ca.archive.ubuntu.com/ubuntu/ wily-backports main restricted universe multiverse" >> /etc/apt/sources.list
RUN echo "deb-src http://ca.archive.ubuntu.com/ubuntu/ wily-backports main restricted universe multiverse" >> /etc/apt/sources.list
# Needed for add-apt-repository.
RUN apt-get update && apt-get -y install software-properties-common \
	&& add-apt-repository -y ppa:ximion/packagekit \
	&& add-apt-repository -y ppa:mutlaqja/ppa
# Needed for building qt5
RUN apt-get update && apt-get upgrade -y && apt-get -y build-dep qtbase5-dev
# Dependencies for ALL KDE applications.
RUN DEBIAN_FRONTEND=noninteractive apt-get -y --force-yes install \
    openssh-server \
    ssh \    
    wget \	
    openjdk-8-jdk \
    git \	
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
    flex \
    libpolkit-agent-1-dev \
    libpolkit-backend-1-dev \
    libpolkit-gobject-1-dev \
    libxslt1-dev \
    docbook-xml \
    docbook-xsl \
    libgcrypt-dev \    
    gcovr \       
    libopenal-dev  \
    libsndfile1-dev \
    libexiv2-dev \
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
    libxkbfile-dev \    
    libtag1-dev \
    libsamplerate0-dev \
    frei0r-plugins-dev \
    libeigen3-dev \
    libgettextpo-dev \
    libaccounts-glib-dev \    
    libboost1.58-all-dev \
    libglew-dev \
    libavutil-ffmpeg-dev \
    libavcodec-ffmpeg-dev \
    libavdevice-ffmpeg-dev \
    libavformat-ffmpeg-dev \
    libavfilter-ffmpeg-dev \
    libpostproc-ffmpeg-dev \
    libswscale-ffmpeg-dev \
    libswresample-ffmpeg-dev \
    libavresample-ffmpeg-dev \
    libclucene-dev \
    libsane-dev \
    libhunspell-dev \
    xserver-xorg-input-wacom \
    libtelepathy-glib-dev \
    libgstreamer1.0-dev \
    libfarstream-0.2-5 \
    libsvn-dev \
    libpci-dev \
    cmake \
    libacl1-dev \
    libgstreamer-plugins-base1.0-dev \
    libtelepathy-farstream-dev \
    libtelepathy-glib-dev \
    intltool \
    libeigen2-dev \
    libasound2-dev \
    build-essential \    
    frei0r-plugins-dev \
    libexif-dev \
    libmovit-dev \
    libsdl1.2-dev \
    libsox-dev \
    libxml2-dev \
    ladspa-sdk \   
    virtuoso-opensource \
    librdf-storage-virtuoso \
    librdf0-dev \
    libiodbc2-dev \
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
    libindi-dev \
    wcslib-dev \
    xplanet \
    astrometry.net \
    mercurial \
    libwlocate-dev \
    libshp-dev \
    libgps-dev \   
    protobuf-compiler \
    libgif-dev \
    libgtk-3-dev \
    libclang-3.5-dev \
    libgsl-dev \
    fontforge \
    libblkid-dev \
    libatasmart-dev \
    libparted-dev \
    sip-dev \
    libtelepathy-logger-dev \
    modemmanager-dev \
    libpgf-dev \
    libvcdinfo-dev \
    libgstreamer0.10-dev \
    libxerces-c-dev \
    xsdcxx swig3.0 \
    mono-mcs \
    libshp-dev \
    libevent-pthreads-2.0-5 \
    libtool \
    gtk-doc-tools \
    libgstreamer-plugins-base0.10-dev \
    libtelepathy-glib-dev \
    libclang-3.6-dev \
    llvm \
    libgraphviz-dev \
    libmtp-dev \
    libmusicbrainz3-dev \
    libvncserver-dev \
    libgpgme++2v5 \
    subversion \
    libotr5-dev \
    python3-numpy \
    xserver-xephyr \
    xwayland \
    weston \    
    libxcb-cursor-dev \
    libspnav-dev \
    libxbase64-dev \
    libxt-dev \
    libgv-python \
    libofx-dev \
    liblmdb-dev \
    sqlite3 \
    cucumber \
    liblensfun-dev \
    libjasper-dev \
    mysql-server \
    xinput \
    texlive-latex-recommended \
    libepub-dev \
    libxcb-util0-dev \
    libpam0g-dev \
    appstream \
    python-lxml \    
    liblzma-dev \
    libfam-dev \
    media-player-info \
    swig \
    liblmdb-dev \    
    mercurial \
    libgit2-dev \
    bzr \
    samba-common \
    libssh2-1-dev \
    libhttp-parser-dev \
    postgresql \
    libqalculate-dev \
    libsystemd-dev \
    libopenexr-dev \
    libslp-dev \
    libtidy-dev \
    libncurses5-dev \    
    libmagick++-dev \
    libnm-dev \
    libnm-glib-dev \
    libnm-gtk-dev \
    libnm-glib-vpn-dev \
    libnm-util-dev \
    network-manager-dev \
    libpocketsphinx-dev \
    libopencv-core-dev \
    libgraphicsmagick++1-dev \
    libmagick++-dev \
    mobile-broadband-provider-info \
    libapt-pkg-dev \
    libgphoto2-dev \
    unzip \
    ragel \
    dmz-cursor-theme \
    python3.5-dev \
    libapt-pkg-dev \
    libisocodes-dev \
    libsqlcipher-dev \
    sqlcipher \
    sphinxsearch \
    libgraphviz-dev \
    libyaml-dev \
    xmlto \
    gobject-introspection \
    mesa-utils \
    libgirepository1.0-dev \
    libprotobuf-dev \
    protobuf-compiler \
    libelektra-dev \
    libxmu-dev \
    dmz-cursor-theme \
    libsmbclient-dev \
    p7zip
    
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

