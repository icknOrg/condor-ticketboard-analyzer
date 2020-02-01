# condor-ticketboard-analyzer
A ticket board fetcher for Condor.

## How to build it
Install a [Java SE Development Kit](https://www.oracle.com/technetwork/java/javase/downloads/index.html) as well
as [Apache Maven](https://maven.apache.org/). Clone or download this repository, and build by running:
```bash
$ mvn install
```

## How to run it
### Trello
Export your API key (e.g. "y1o2u3r4e5k6e7y8") and OAuth token string (e.g. "o0a9u8t7h6") as environment variables
and execute the *.jar file you've just compiled:
```bash
$ export TRELLO_API_KEY=y1o2u3r4e5k6e7y8
$ export TRELLO_OAUTH_KEY=o0a9u8t7h6
$ java -jar target/condor-ticketboard-fetcher.jar trello myboardid /my/output/folder/
```

### GitHub
Export your OAuth token and execute the *.jar file:
```bash
$ export GITHUB_OAUTH_KEY=y1o2u3r4e5k6e7y8
$ java -jar target/condor-ticketboard-fetcher.jar github linuxmint cinnamon-spices-extensions /my/output/folder/
```
