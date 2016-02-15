FROM java:7
# Cleaning
RUN apt-get clean

RUN mkdir -p /assetsSource
WORKDIR /assetsSource

COPY . /apkSource
WORKDIR /apkSource

RUN git clone -b fcm https://flqgithubdeploy:flgithub4npm@github.com/FoodLogiQ/flq-android-assets.git /tmp/flqassets

RUN chmod u+rw /tmp/flqassets/
