
##Deploy locally

- Start database
`./mongod --dbpath /path/to/db/data`

- Restore a fresh or dev database (see db/README.md)

- Start backend tier
`mvn spring-boot:run`

- Start web tier
`ng serve --open`

##Deploy prod first time

These steps are intended to deploy TrainMeUp app in one single server for the first time.

###Requirements on server

- MongoDB installed
- Nginx installed
- Java 8 or above installed

###Steps

- Go to trainmeup-web
- Run `ng build --prod`
- Copy the folder within "dist" to the server
- Go to trainmeup-service
- Run `mvn clean package`
- Copy the jar within target to the server.
- Go to db folder and copy the folder fresh to the server.
- Restore a fresh database (see db/README.md) in server.
- Copy trainmeup-nginx.conf file to /etc/nginx/sites-available/ folder in server.
- Go to /etc/nginx/sites-enabled/ and remove default symlink:
`unlink default`
- Create a new symlink
`ln -s ../sites-available/trainmeup-nginx.conf trainmeup`
- In server edit /etc/mongod.conf file and change the dbPath to the one you defined, e.g
```
storage:
	dbPath: /home/user/db-data
```
- Change permissions on that directory so mongod service is allowed to read the folder:
`sudo chown -R mongod:mongod /home/user/db-data`
- Copy trainmeup.service to /lib/systemd/system folder in server
- Start services:
`sudo systemctl start mongod`
`sudo systemctl enable mongod`

`sudo systemctl start trainmeup`
`sudo systemctl enable trainmeup`

- Reboot server and check services are up and running
`sudo systemctl status mongod`
`sudo systemctl status trainmeup`

- Try reaching the application through http://<IP>:4200/ and get the category tree to test backend communication.


