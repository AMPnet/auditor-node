# Auditor Node

## Development setup
In order to run the application locally for development purposes, GraalVM needs to be installed. The version of GraalVM
should match the version of `org.graalvm.sdk:graal-sdk` dependency, which is set to `21.0.0`. Java 8 GraalVM should be
used. To install GraalVM, follow the instructions found [here](https://www.graalvm.org/docs/getting-started/).
Alternatively, you can try installing GraalVM via Homebrew, as described
[here](https://github.com/graalvm/homebrew-tap). When installing via Homebrew, be mindful of GraalVM version. After
installing GraalVM, run the following commands to enable building of native image:  
`gu install native-image`  
`gu rebuild-images polyglot`  

When starting the application via IntelliJ, make sure that JRE is set to GraalVM. If you get `IllegalStateException`
with message "No language and polyglot implementation was found on the classpath. Make sure the truffle-api.jar is on
the classpath." when starting up the application, you are probably running it on non-GraalVM JRE.

### IntelliJ setup for Micronaut
To successfully build the application using IntelliJ, enable annotation processing by navigating to
`Preferences -> Build, Execution, Deployment -> Compiler -> Annotation Processors` and check the
`Enable annotation processing` option.

### IPFS & Geth nodes
You can either download [IPFS](https://ipfs.io/) desktop application and [Geth](https://geth.ethereum.org/) and start
them manually or run them via Docker Compose.

To start IPFS and Geth via Docker Compose, position yourself into the `docker` directory and run `docker-compose up -d`.
To upload a file to your local IPFS, first place it in the `ipfs-staging` directory which was created when you started
Docker Compose. After that, you can add the file to IPFS like this:
`docker exec ipfs-node ipfs add /export/<your-file-name>`.  

**IMPORTANT NOTE: when you start local Geth node, it may take up to 30 minutes to get it synced with Ropsten testnet.
During this time it is possible that reading IPFS hash from the contract will fail because your local Geth node is not
yet fully synchronized.**

### Build
The application can be compiled into a native image by executing: `./gralew clean build nativeImage`. This process can
take some tame and will use up a lot of memory (up to 10 GB). After the build is complete, you can find the compiled
native image in `build/native-image` folder. **NOTE: always specify Gradle `clean` task before compiling into native
image, otherwise any code changes may not be propagated into the new native image.**  

To build a Docker image for the application running on GraalVM, execute: `./gradlew dockerBuild`.  

To build a Docker image for the application compiled as a native image, execute: `./gradlew dockerBuildNative`. When
building the native Docker image, Docker will need up to 10 GB of memory in order to successfully build the Graal native
image. It is therefore not recommended running this task locally; this task is primarily used to build a lightweight
Docker image of the application for distribution.  

The two of the Docker images can be pushed as such:  
- `./gradlew dockerPush` will push GraalVM-based Docker image with the name of `ampnet/auditor-node-graal`  
- `./gradlew dockerPushNative` will push native Docker image with the name of `ampnet/auditor-node`

### Testing
You can upload the test JavaScript file located in `src/integTest/resources/` to IPFS to try out the application. To do
so, execute the following commands from the project root:  
`cp src/integTest/resources/test-script.js docker/ipfs-staging/`  
`docker exec ipfs-node ipfs add /export/test-script.js`  

The second command will print the IPFS file hash you can use to fetch the file. This hash must be provided as a program
argument. Current version of the script file should produce the hash value of
`QmSuwCUCZXzPunnrCWL7CnSLixboTa7HftVBjcVgi3TMaK`.  

While the Docker IPFS container is running, you can access its web-ui via `http://localhost:5001/webui`.  

If you are running the desktop IPFS application, you can upload the file through its interface. The hash of the file
will then be displayed there.  

When the application is started, it will listen to HTTP requests on port `8080`. Making a GET request to `/audit` will
start the test audit procedure which will first try to fetch IPFS file hash stored in the Ethereum contract with address
`0x992E8FeA2D91807797717178Aa6abEc7F20c31a8` on the Ropsten testnet. After that, the file will be fetched via public
IPFS gateway (`https://ipfs.io/ipfs/`). The stored hash value will be fetched from contract via the Infura node on
Ropsten network (`https://ropsten.infura.io/v3/08664baf7af14eda956db2b71a79f12f`). If you want to use local IPFS and
Geth nodes instead, you can specify `--local-ipfs` and `-rpc.url=http://localhost:8545` as program arguments.

## Program arguments

| Argument | Description | Default value |
|:--------:|:-----------:|:-------------:|
| `-rpc.url=<url>` | Ethereum RPC API URL | `http://localhost:8545` |
| `-ipfs.gateway-url=<url>` | IPFS gateway URL, not used when  `--local-ipfs`  is specified; must contain  `{ipfsHash}` placeholder | `https://ipfs.io/ipfs/{ipfsHash}` |
| `-ipfs.local-client-port=<port>` | Port of local IPFS client, used when  `--local-ipfs` is specified | `5001` |
| `-auditor.contract-address=<address>` | Ethereum address of auditor contract | `0x992E8FeA2D91807797717178Aa6abEc7F20c31a8` |
| `--local-ipfs` | Use local IPFS client to fetch files | Disabled by default |
| `-script.properties.<propertyName>=<propertyValue>` | Sets specified `<propertyName>` and `<propertyValue>` which is then visible inside auditing scripts via `Properties` object. All property names are converted into `kebab-case` and property values are always strings. See auditor script API specification for more info. | No properties are set by default |

## Useful documentation

[Micronaut Documentation](https://docs.micronaut.io/latest/guide/)  
[Micronaut Gradle plugin](https://github.com/micronaut-projects/micronaut-gradle-plugin)  
[Λrrow Core Documentation](https://arrow-kt.io/docs/core/)  
[Reflection in Graal Native Image](https://www.graalvm.org/reference-manual/native-image/Reflection/)  
[Kotlin/JavaScript interoperability via GraalVM](https://www.graalvm.org/reference-manual/js/JavaInteroperability/)  
[Polyglot programming](https://www.graalvm.org/reference-manual/polyglot-programming/)  
[JS compatibility](https://www.graalvm.org/reference-manual/js/JavaScriptCompatibility/)
