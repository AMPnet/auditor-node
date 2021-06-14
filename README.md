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
`docker exec ipfs-node ipfs add /export/<your-file-name>`. If you need to upload a directory instead, you can do so by
adding the `-r` flag to the `ipfs add` command: `docker exec ipfs-node ipfs add -r /export/<your-directory-name>`.

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

### Tests

The build process specifies multiple test sets:

- `test` for unit tests
- `integTest` for integration tests
- `apiTest` for API endpoint tests

Each of the listed test sets can be executed by using Gradle. Additionally, `apiTest` set can be run against the
compiled native image: `./gradlew apiTestNativeImage`.

### Testing manually

#### Full flow

You can upload the test directory located in `examples/example-auditing-script` to IPFS to try out the application. To
do so, execute the following commands from the project root:  
`cp -r examples/example-auditing-script docker/ipfs-staging/`  
`docker exec ipfs-node ipfs add -r /export/example-auditing-script`

The second command will print the IPFS file hashes as well as the directory hash. Current version of the script
directory should produce the hash value of `QmXXFKbcs8n2brZrs9rnbt8FUeow5NKJbHfqbzEWm28vnW`.

While the Docker IPFS container is running, you can access its web-ui via `http://localhost:5001/webui`.

If you are running the desktop IPFS application, you can upload the directory through its interface. The hash of the
directory will then be displayed there.

When the application is started, it will listen to HTTP requests on port `8080`. Connecting to `/audit` via web socket
will start the test audit procedure which will first try to fetch asset info IPFS hash stored in the Ethereum contract
with address `0xcaA9f2F9d9137E2fB806ecDf731CdD927aA9d97F` on the Ropsten testnet. After that, asset category ID and will
be fetched from the same contract address. Using the retrieved asset category ID, auditing procedure directory IPFS hash
is retrieved from registry contract with address `0x9C1d4593148c26249624d334AA8316A3446a0cD2`. This directory hash is
then used to fetch the `audit.js` script file which the IPFS directory contains. The script is then executed
interactively via web socket. After the execution finishes, the result of the audit is returned via web socket - this
will also include an unsigned transaction which can be used to write audit result to blockchain. The transaction will
only be generated if the auditing process was not aborted, i.e. only if the auditing result finished with success or
failure. The contract for which the transaction will be generated has address
`0xE239E7a361e0C82A1CF9E8C8B53353186B616EB7` on Ropsten testnet.

#### Scripts

You can test auditing scripts directly by using `POST /script/execute` endpoint. Simply send the script as `text/plain`
content, and the auditor will execute it. To run scripts interactively, web socket api can be used. See web socket API
documentation located in `doc/websocket-api.md` for more info. There is also an example HTML page for running scripts
interactively located in `examples/interactive-script.html`.

## Web UI

Auditor node offers basic web UI for purposes of running auditing scripts and script development. The UI is available
at the `/webui` endpoint when the application is running.

## Program arguments

| Argument | Description | Default value |
|:--------:|:-----------:|:-------------:|
| `-rpc.url=<url>` | Ethereum RPC API URL. | `https://ropsten.infura.io/v3/08664baf7af14eda956db2b71a79f12f` |
| `-ipfs.gateway-url=<url>` | IPFS gateway URL, not used when `--local-ipfs` is specified; must contain  `{ipfsHash}` placeholder. | `https://ipfs.io/ipfs/{ipfsHash}` |
| `-ipfs.local-client-port=<port>` | Port of local IPFS client, used when `--local-ipfs` is specified. | `5001` |
| `-auditor.asset-contract-address=<address>` | Ethereum address of the asset contract. | `0xcaA9f2F9d9137E2fB806ecDf731CdD927aA9d97F` |
| `-auditor.registry-contract-address=<address>` | Ethereum address of the registry contract. | `0x9C1d4593148c26249624d334AA8316A3446a0cD2` |
| `-auditor.audit-registry-contract-address=<address>` | Ethereum address of the audit registry contract. | `0xE239E7a361e0C82A1CF9E8C8B53353186B616EB7` |
| `--local-ipfs` | Use local IPFS client to fetch files. | Disabled by default. |
| `-script.properties.<propertyName>=<propertyValue>` | Sets specified `<propertyName>` and `<propertyValue>` which is then visible inside auditing scripts via `Properties` object. All property names are converted into `kebab-case` and property values are always strings. See auditor script API specification for more info. | No properties are set by default. |

## Useful documentation

[Micronaut Documentation](https://docs.micronaut.io/latest/guide/)  
[Micronaut Gradle plugin](https://github.com/micronaut-projects/micronaut-gradle-plugin)  
[Λrrow Core Documentation](https://arrow-kt.io/docs/core/)  
[Reflection in Graal Native Image](https://www.graalvm.org/reference-manual/native-image/Reflection/)  
[Kotlin/JavaScript interoperability via GraalVM](https://www.graalvm.org/reference-manual/js/JavaInteroperability/)  
[Polyglot programming](https://www.graalvm.org/reference-manual/polyglot-programming/)  
[JS compatibility](https://www.graalvm.org/reference-manual/js/JavaScriptCompatibility/)
