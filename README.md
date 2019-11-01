# coins1920-group5-code
A ticket board fetcher for Condor.

## How to build it
Install a [Java SE Development Kit](https://www.oracle.com/technetwork/java/javase/downloads/index.html) as well
as [Apache Maven](https://maven.apache.org/). Clone or download this repository, and build by running:
```bash
$ mvn install
```

## How to run it
With your API key and OAuth token string at hand run:
```bash
$ java -jar target/condor-ticketboard-fetcher.jar trello myApiKey myOauthToken
```
