
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
- Run `sh start.sh`


