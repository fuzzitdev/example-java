fuzzit.dev was [acquired](https://about.gitlab.com/press/releases/2020-06-11-gitlab-acquires-peach-tech-and-fuzzit-to-expand-devsecops-offering.html) by GitLab and the new home for this repo is [here](https://gitlab.com/gitlab-org/security-products/demos/go-fuzzing-example)

# Continuous Fuzzing for Java Example

This is an example of how to integrate your [JQF](https://github.com/rohanpadhye/jqf) targets with the
[Fuzzit](https://fuzzit.dev) Continuous Fuzzing Platform (Java support is currently in Beta).

This example will show the following steps:
* [Building and running a simple JQF target locally](#building-jqf-target)
* [Integrate the JQF target with Fuzzit via Travis-CI](#integrating-with-fuzzit-from-ci)

Result:
* Fuzzit will run the fuzz targets continuously on a daily basis with the latest release.
* Fuzzit will run regression tests on every pull-request with the generated corpus and crashes to catch bugs early on.

Coverage Guided Structure Aware Fuzzing for Java can help find both complex bugs, as well as correctness bugs.
Java is a safe language so memory corruption bugs are very unlikely to happen, but some bugs can still have security
implications.

This tutorial focuses less on how to build JQF targets and more on how to integrate the targets with Fuzzit. A lot of 
great information is available at the [JQF Wiki](https://github.com/rohanpadhye/jqf/wiki/Fuzzing-with-Zest).

## Building JQF Target

The targets that are currently supported on Fuzzit are targets that utilize the
[JQF+Zest](https://github.com/rohanpadhye/jqf/wiki/Fuzzing-with-Zest) engine.

### Understanding the bug

The bug is located at `ParseComplex.Java` in the following code

```java
package dev.fuzzit.examplejava;

public class ParseComplex {
    public static boolean parse(String data) {
        if (data.length() > 4) {
            return false;
        }
        return data.charAt(0) == 'F' &&
                data.charAt(1) == 'U' &&
                data.charAt(2) == 'Z' &&
                data.charAt(3) == 'Z';
    }
}
```

This is a VERY simple case for the sake of the example the author made a mistake.
and Instead of `data.length() > 4 ` the correct code should be `data.length() < 4`.

### Understanding the fuzzer

the fuzzer is located at `ParseComplexFuzz.Java` in the following code:

```java
import org.junit.runner.RunWith;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;

@RunWith(JQF.class)
public class ParseComplexFuzz {

    @Fuzz
    public void fuzz(String data) {
        ParseComplex.parse(data);
    }
}
```

This is pretty straight forward the fuzzer will generate the psudo random string via data according to
the coverage feedback

### Building & Running the fuzzer

```bash
git clone https://github.com/fuzzitdev/example-java
cd example-java
docker run -v `pwd`:/app -it maven:3.6.1-jdk-12 /bin/bash
cd /app
# Change to maven repo once 1.3 is out
curl -o zest-cli.jar https://storage.googleapis.com/public-fuzzit/jqf-fuzz-1.3-SNAPSHOT-zest-cli.jar
mvn package
java -jar zest-cli.jar  -e ./target/example-java-1.0-SNAPSHOT-fat-tests.jar dev.fuzzit.examplejava.ParseComplexTest fuzz
```

Will print the following output and stacktrace:

```text
Semantic Fuzzing with Zest
--------------------------

Test name:            dev.fuzzit.examplejava.ParseComplexTest#fuzz
Results directory:    /app/target/fuzz-results/dev.fuzzit.examplejava.ParseComplexTest/fuzz
Elapsed time:         2s (no time limit)
Number of executions: 582
Valid inputs:         562 (96.56%)
Cycles completed:     0
Unique failures:      1
Queue size:           2 (0 favored last cycle)
Current parent input: 0 (favored) {581/800 mutations}
Execution speed:      263/sec now | 221/sec overall
Total coverage:       5 branches (0.01% of map)
```

You can see 1 crash is instantly found. Results are saved by default to target/fuzz-results


```text
.id_000000: FAILURE (java.lang.StringIndexOutOfBoundsException)
E
Time: 0.079
There was 1 failure:
1) fuzz(dev.fuzzit.examplejava.ParseComplexTest)
java.lang.StringIndexOutOfBoundsException: String index out of range: 0
        at java.base/java.lang.StringLatin1.charAt(StringLatin1.java:47)
        at java.base/java.lang.String.charAt(String.java:702)
        at dev.fuzzit.examplejava.ParseComplex.parse(ParseComplex.java)
        at dev.fuzzit.examplejava.ParseComplexTest.fuzz(ParseComplexFuzz.java)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:567)
        at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
        at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
        at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
        at edu.berkeley.cs.jqf.fuzz.junit.TrialRunner$1.evaluate(TrialRunner.java:59)
        at edu.berkeley.cs.jqf.fuzz.junit.TrialRunner.run(TrialRunner.java:65)
        at edu.berkeley.cs.jqf.fuzz.junit.quickcheck.FuzzStatement.evaluate(FuzzStatement.java:165)
        at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
        at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
        at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
        at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
        at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
        at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
        at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
        at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
        at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
        at edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing.run(GuidedFuzzing.java:184)
        at edu.berkeley.cs.jqf.fuzz.junit.GuidedFuzzing.run(GuidedFuzzing.java:126)
        at edu.berkeley.cs.jqf.plugin.ReproGoal.execute(ReproGoal.java:184)
        at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo(DefaultBuildPluginManager.java:137)
        at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:210)
        at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:156)
        at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:148)
        at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject(LifecycleModuleBuilder.java:117)
        at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject(LifecycleModuleBuilder.java:81)
        at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build(SingleThreadedBuilder.java:56)
        at org.apache.maven.lifecycle.internal.LifecycleStarter.execute(LifecycleStarter.java:128)
        at org.apache.maven.DefaultMaven.doExecute(DefaultMaven.java:305)
        at org.apache.maven.DefaultMaven.doExecute(DefaultMaven.java:192)
        at org.apache.maven.DefaultMaven.execute(DefaultMaven.java:105)
        at org.apache.maven.cli.MavenCli.execute(MavenCli.java:956)
        at org.apache.maven.cli.MavenCli.doMain(MavenCli.java:288)
        at org.apache.maven.cli.MavenCli.main(MavenCli.java:192)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.base/java.lang.reflect.Method.invoke(Method.java:567)
        at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced(Launcher.java:282)
        at org.codehaus.plexus.classworlds.launcher.Launcher.launch(Launcher.java:225)
        at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode(Launcher.java:406)
        at org.codehaus.plexus.classworlds.launcher.Launcher.main(Launcher.java:347)

FAILURES!!!
Tests run: 1,  Failures: 1

```

For the possible command-lines for ZestCLI you can run `java -jar zest-cli.jar --help`

## Integrating with Fuzzit from CI

The best way to integrate with Fuzzit is by adding a two stages in your Continuous Build system
(like Travis, CircleCI, Github Actions or any other CI).

Fuzzing stage:

* Build a package containing the fuzz targets together with all dependencies. This can be done using the assembly plugin like in this repository
* Download `fuzzit` cli
* Authenticate via passing `FUZZIT_API_KEY` environment variable
* Create a fuzzing job by uploading the fuzzing target

Regression stage (This stage in Java is currently in Alpha and will be rolled out to Public beta in the upcoming week)
* Build a fuzzing target
* Download `fuzzit` cli
* Authenticate via passing `FUZZIT_API_KEY` environment variable OR defining the corpus as public. This way
No authentication would be require and regression can be used for [forked PRs](https://docs.travis-ci.com/user/pull-requests#pull-requests-and-security-restrictions) as well
* Create a local regression fuzzing job - This will pull all the generated corpuses and run them through
the fuzzing binary. If new bugs are introduced this will fail the CI and alert

Here is the relevant snippet from the [fuzzit.sh](https://github.com/fuzzitdev/example-java/blob/master/fuzzit.sh)
which is being run by [.travis.yml](https://github.com/fuzzitdev/example-java/blob/master/.travis.yml)

```bash
wget -q -O fuzzit https://github.com/fuzzitdev/fuzzit/releases/download/v2.4.35/fuzzit_Linux_x86_64
chmod a+x fuzzit

## upload fuzz target for long fuzz testing on fuzzit.dev server or run locally for regression
./fuzzit create job --engine jqf --type ${1} --args "dev.fuzzit.examplejava.ParseComplexTest fuzz" fuzzitdev/parse-complex ./target/example-java-1.0-SNAPSHOT-fat-tests.jar
``` 

In production it is advised to download a pinned version of the [CLI](https://github.com/fuzzitdev/fuzzit)
like in the example. In development you can use the latest version:
https://github.com/fuzzitdev/fuzzit/releases/latest/download/fuzzit_${OS}_${ARCH}.
Valid values for `${OS}` are: `Linux`, `Darwin`, `Windows`.
Valid values for `${ARCH}` are: `x86_64` and `i386`.


