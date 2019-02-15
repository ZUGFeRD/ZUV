# ZUV
ZUV (ZUgferd+[VeraPDF](http://VeraPDF.org)) is a open-source e-invoice validator for the [ZUGFeRD](https://www.ferd-net.de/zugferd/specification/index.html)/[Factur-X](http://fnfe-mpe.org/factur-x/) standard.

It checks both PDF/A-3 compliance (based on VeraPDF) and ZUGFeRD version 1 respectively 2 (EN16931 profile only) XML for correctness.
The XML check is done by validating against the official ZUGFeRD schematron file for v1 and the EN16931 UN/CEFACT SCRDM v16B uncoupled schematron from the [CEN](https://github.com/CenPC434/validation).


## Build
compile with `/opt/local/bin/mvn clean package`


## Install

Originally this was intended as VeraPDF plugin in which case you had to install a VeraPDF. Due to deployment issues we switched from ZUV being embedded in VeraPDF to VeraPDF being embedded into UV.

## Run

`java -jar target/ZUV-0.4.2-SNAPSHOT.jar -f <filename of ZUGFeRD PDF.pdf>`


## Output
```
<validation><pdf>
<info><duration unit='ms'>2065</duration></info>

<report>
  <buildInformation>
    <releaseDetails id="core" version="1.10.2" buildDate="2017-11-30T12:47:00+01:00"></releaseDetails>
    <releaseDetails id="validation-model" version="1.10.5" buildDate="2017-12-28T11:50:00+01:00"></releaseDetails>
  </buildInformation>
  <jobs>
    <job>
      <item size="163545">
        <name>/Users/jstaerk/workspace/ZUV/fail3.pdf</name>
      </item>
      <validationReport profileName="PDF/A-3U validation profile" statement="PDF file is compliant with Validation Profile requirements." isCompliant="true">
        <details passedRules="125" failedRules="0" passedChecks="11200" failedChecks="0"></details>
      </validationReport>
      <duration start="1520759189894" finish="1520759191502">00:00:01.608</duration>
    </job>
  </jobs>
  <batchSummary totalJobs="1" failedToParse="0" encrypted="0">
    <validationReports compliant="1" nonCompliant="0" failedJobs="0">1</validationReports>
    <featureReports failedJobs="0">0</featureReports>
    <repairReports failedJobs="0">0</repairReports>
    <duration start="1520759189547" finish="1520759191533">00:00:01.986</duration>
  </batchSummary>
</report>
</pdf><xml><errors>
<error><criterion>@format</criterion><result>
	Attribute '@format' is required in this context.</result>
</error></errors>
<info><duration unit='ms'>12612</duration></info>
</xml>
<info><duration unit='ms'>14677</duration></info>
</validation>
```

## Embed

Go wild.
XML output is currently still on STDERR, this might change to STDOUT. 
Logging is disabled, this might be changed in the future, i.e. logging to a file

## License

Permissive Open Source APL2, see LICENSE
## History

0.5.0 Added license text, upgraded to mustangproject 1.5.3, logging to file, finding signatures, by default disable schematron check for non-matching ZF2 profiles

## Todo

* Use https://github.com/veraPDF/veraPDF-rest as web interface


## Authors

Jochen Staerk "Mustangproject Chief ZUGFeRD amatuer" <jochen@zugferd.org>
