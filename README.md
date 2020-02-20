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

## Dealing with rate limits
The condor-ticketboard-fetcher has a built-in mechanism to deal with rate limits that occur during fetching. When
fetching a repository, you might run into the following message:
```bash
"Couldn't fetch everything, the partial result is: xyz"
```

In this case, a rate limit occurred and the fetcher created a _partial_ result file at the location shown. When you
run the fetcher again, pointing it to the very (same) directory where this result file lies, it will not start fetching
from scratch but rather continue, where it failed before.

This can be repeated until everything is fetched. The fetcher will create ordinary CSV files, once no more rate
limits did occur.

> Note: This has _only_ been implemented for GitHub's API, not for the Trello variant!
