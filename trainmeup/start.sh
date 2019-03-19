#!/bin/bash
print 'Start database...'
nohup mongod --dbpath /home/user/db-data &
print 'Start service...'
nohup java -jar trainmeup-service-0.1.0.jar &
print 'Restart web tier...'
systemctl restart nginx
