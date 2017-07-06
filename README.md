# ZUV
Proof of concept for a ZUgferd plugin (or policy) for Verapdf

Essentially this is supposed to be made available as a jar file which can be embedded via Maven and a standalone server version which starts a Jetty Webserver and allows file uploads. It is supposed check both PDF/A-3 compliance (based on VeraPDF) and ZUGFeRD XML correctness, e.g. with by validating against the official ZUGFeRD schematron file.


## Build
compile with /opt/local/bin/mvn clean package 


## Install

Download the VeraPDF installer from http://verapdf.org/software/ and install .

Copy the ZUV jar file into the plugins-subdirectory of the [VeraPDF installation directory].

 Add 

```xml
  <plugin enabled="true">
    <name>ZUGFeRD Validator</name>
    <version>0.2.0</version>
    <description>Validates ZUGFeRD PDFs against the official Schematron. Developed by Jochen Staerk.</description>
    <pluginJar>[VeraPDF installation directory]/plugins/ZUV-0.2.0-SNAPSHOT.jar</pluginJar>
  </plugin>
```

to the config/plugin.xml file in the [VeraPDF installation directory] (and of course don't forget to replace
the placeholder [VeraPDF installation directory] with the complete path of your VeraPDF installation).


## Run

Run verapdf-gui . Check that embedded files is checked in Config|Features Config.
Select a ZUGFeRD file like [the mustangproject sample file](http://www.mustangproject.org/MustangGnuaccountingBeispielRE-20170509_505.pdf).
Select "Validation and Features" as report type and hit the execute button.


## Output

Click the "View XML" button. We're working on publishing the schematron validation results in the node
Report/Jobs/job/featuresReport/embeddedFiles/customFeatures/pluginFeatures[name="ZUGFeRD Validator"].

## License

Permissive Open Source APL2, see LICENSE

## Authors

Jochen Staerk
