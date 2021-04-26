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
