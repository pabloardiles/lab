
## Deploy locally

### Requirements

- MongoDB installed
- Nginx installed
- Java 8 or above installed

### Steps

- Start database
`./mongod --dbpath /path/to/db/data`

- Restore a fresh or dev database (see db/README.md)

- Start backend tier
`mvn spring-boot:run`

- Start web tier
`ng serve --open`

## Deploy prod first time

These steps are intended to deploy TrainMeUp app in one single server for the first time.

### Requirements on server

- MongoDB installed
- Nginx installed
- Java 8 or above installed

### Build for prod

- Go to trainmeup-web
- Run `ng build --prod`
- Go to trainmeup-service
- Run `mvn clean package`
- Wrap everything in a tarball file (e.g. trainmeup.tar): 
-- trainmeup-web dist
-- trainmeup service jar
-- Fresh database
-- trainmeup-nginx.conf file
-- trainmeup.service file

### Steps

Note: Use the following command to copy files into the server `scp -i <certificate> /path/to/file ec2-user@<instance_ip>:/home/ec2-user/`

- Copy the tarball to server
- Run `tar -xvf trainmeup.tar`
- Copy trainmeup-nginx.conf file to /etc/nginx/sites-available/ folder in server (or /etc/nginx/conf.d in CentOS or similar).
- Go to /etc/nginx/sites-enabled/ and remove default symlink (does not applies on CentOS or similar):
`unlink default`
- Create a new symlink (does not applies on CentOS or similar)
`ln -s ../sites-available/trainmeup-nginx.conf trainmeup`
- Only CentOS: comment the server block in /etc/nginx/nginx.conf file.
- Copy trainmeup-web files to /var/www/html directory. In CentOS you must copy those files to /usr/share/nginx/html directory and modify the root path in /etc/nginx/conf.d/trainmeup-nginx.conf accordingly.
- Create a directory to hold database files (mount an additional volume for this purpose and edit /etc/fstab; see resources below)
*Important:* in Amazon Linux 2 instance mount this volume in root folder (/), for some reason mongod service is not able to read or write data in another location.
- Change permissions on that directory so mongod service is allowed to read the folder:
`sudo chown -R mongod:mongod /db-data`
- In server edit /etc/mongod.conf file and change the dbPath to the one you defined, e.g
```
storage:
	dbPath: /db-data
```
- Start DB service:
`sudo systemctl start mongod`
`sudo systemctl enable mongod`
- Restore a fresh database (see db/README.md) in server.
- Copy trainmeup.service to /lib/systemd/system folder in server
- Modify trainmeup.service accordingly to setup user and paths.
- Start trainmeup service:
`sudo systemctl start trainmeup`
`sudo systemctl enable trainmeup`
- Reboot server and check services are up and running
`sudo systemctl status mongod`
`sudo systemctl status trainmeup`
- Try reaching the application through http://<IP>:4200/ and click on Test button to test backend communication.

## Resources

- Install Nginx: https://docs.nginx.com/nginx/admin-guide/installing-nginx/installing-nginx-open-source/#prebuilt
- Install MongoDB on Amazon Linux: https://docs.mongodb.com/manual/tutorial/install-mongodb-on-amazon/
- Install JDK 8: 
`sudo yum install java-1.8.0-openjdk.x86_64`
- Mount additional volume on Amazon Linux: https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-using-volumes.html
