# language-detection
* * * 

Given a string of text, identify what language the text is written in.
(origionally for android but I seen no reason for it. It is java so.....)

This project is a fork of an excellent Java language detection library 
([language-detection](http://code.google.com/p/language-detection/)) written by Nakatani Shuyo. 
The original git version control history and commit messages are retained in this project.



## Changes (by IvoNet)

* I'm starting to make some extensive changes to the code because it is not very clean.
  I love the algorithm and stuff but the code is not very readable
* I cloned this repository in the hope of using it in my [epub-processor](https://github.com/IvoNet/epub-processor)
  and I found out that it didn't work quite right when working with multiple threads (it died)
  So I started refactoring a bit.
* As far as I know I have not changed anything in the algorithm except maybe for some code cleanup (yet).


## Build

    mvn -N clean install -f superpom/pom.xml
    mvn clean install

## Sample usage

See [the original project on Google Code](http://code.google.com/p/language-detection/).

Set up the language profile list in `DetectorFactory.java`.

OR

    Detector detector = DetectorFactory.create();
    String language = detector.detect("Hello World I am an English text");

## Training: Generating language profiles

To generate a language profile, [download](http://dumps.wikimedia.org/backup-index.html) a 
Wikipedia abstract file to use as a training data set.

For example, click `anwiki` and download `anwiki-20121227-abstract.xml` to 
`language-detection/abstracts/` and do:

    cd language-detection
    mkdir abstracts/profiles
    java -jar lib/langdetect.jar --genprofile -d language-detection/abstracts an
    python scripts/genprofile.py -i abstracts/profiles/an > AN.java

## Maven

Maven repository:

    <repository>
        <id>nitin.public.maven.repository.release</id>
        <name>Nitin's Public Release Repository</name>
        <url>https://raw.github.com/nitinverma/public.maven.repository/master/releases/</url>
        <snapshots>
            <enabled>false</enabled>
        </snapshots>
    </repository>

Maven dependency:

        <dependency>
            <groupId>com.cybozu.labs</groupId>
            <artifactId>langdetect</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>


## License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)
