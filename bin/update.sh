#!/usr/bin/env bash
rsync -avzP --delete -e "ssh -p 2395" /mnt/Data/Projects/redis-protocol/redis-server/build/libs/redis-server-1.0.jar  chiennd@10.3.14.156:/home/chiennd/23.101/atp-service
