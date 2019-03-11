
##Deployment steps (locally)

- Start database
`./mongod --dbpath /path/to/db/data`

- Restore a fresh or dev database (see db/README.md)

- Start backend tier
`mvn spring-boot:run`

- Start web tier
`ng serve --open`