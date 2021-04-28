# Auditor Node

## Development setup
In order to run the application locally for development purposes, GraalVM needs to be installed. The version of GraalVM
should match the version of `org.graalvm.sdk:graal-sdk` dependency, which is set to `21.0.0`. Java 8 GraalVM should be
used. To install GraalVM, follow the instructions found [here](https://www.graalvm.org/docs/getting-started/).
Alternatively, you can try installing GraalVM via Homebrew, as described
[here](https://github.com/graalvm/homebrew-tap).

When starting the application via IntelliJ, make sure that JRE is set to GraalVM. If you get `IllegalStateException`
with message "No language and polyglot implementation was found on the classpath. Make sure the truffle-api.jar is on
the classpath." when starting up the application, you are probably running it on non-GraalVM JRE.  

TODO: some other stuff that may be necessary for local development, still WIP:
`gu install espresso`  
`gu install native-image`  
`gu rebuild-images polyglot`  

Kotlin/JavaScript interoperability via GraalVM: https://www.graalvm.org/reference-manual/js/JavaInteroperability/  
Polyglot programming: https://www.graalvm.org/reference-manual/polyglot-programming/  
JS compatibility: https://www.graalvm.org/reference-manual/js/JavaScriptCompatibility/  

### IPFS server
You can either download [IPFS](https://ipfs.io/) desktop application and start it manually or run IPFS via Docker
Compose. To start IPFS via Docker Compose, position yourself into the `docker` directory and run `docker-compose up -d`.
To upload a file to your local IPFS, first place it in the `ipfs-staging` directory which was created when you started
Docker Compose. After that, you can add the file to IPFS like this:
`docker exec ipfs-node ipfs add /export/<your-file-name>`.  

#### Testing
You can upload the test JavaScript file located in `src/integTest/resources/` to IPFS to try out the application. To do
so, execute the following commands from the project root:  
`cp src/integTest/resources/test-script.js docker/ipfs-staging/`  
`docker exec ipfs-node ipfs add /export/test-script.js`  

The second command will print the IPFS file hash you can use to fetch the file. This hash must be provided as a program
argument. Current version of the script file should produce the hash value of
`QmcUq5zoErvE63anX4NpS6NYTeHX6Gp1RNs1U2Jb43gpYq`.  

While the Docker IPFS container is running, you can access its web-ui via `http://localhost:5001/webui`.  

If you are running the desktop IPFS application, you can upload the file through its interface. The hash of the file
will then be displayed there.
