FROM ubuntu:latest

ARG DEBIAN_FRONTEND=noninteractive
ARG CANTALOUPE_VERSION=5.0.5
ARG CANTALOUPE_ZIP=cantaloupe-${CANTALOUPE_VERSION}.zip

# Install various dependencies

RUN apt-get update && apt-get install -y --no-install-recommends \
        openjdk-11-jdk-headless \
		ffmpeg \
		maven \
		wget \
                rpl \
                unzip \
  		libopenjp2-tools \
                liblcms2-dev \
                libpng-dev \
                libzstd-dev \
                libtiff-dev \
                libjpeg-dev \
                zlib1g-dev \
                libwebp-dev \
                libimage-exiftool-perl \
    && apt-get clean

# Install Cantaloupe into /cantaloupe

RUN wget https://github.com/cantaloupe-project/cantaloupe/releases/download/v${CANTALOUPE_VERSION}/${CANTALOUPE_ZIP} && unzip ${CANTALOUPE_ZIP} && mv cantaloupe-${CANTALOUPE_VERSION} /cantaloupe && rm ${CANTALOUPE_ZIP}

COPY cantaloupe.properties /cantaloupe

# Make libs available to Java
RUN cp /cantaloupe/deps/Linux-x86-64/lib/* /usr/lib/

WORKDIR /cantaloupe
CMD java -Dcantaloupe.config=./cantaloupe.properties -Xmx2g -jar cantaloupe*.jar
