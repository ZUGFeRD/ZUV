# ZUV
ZUV (ZUgferd+[VeraPDF](http://VeraPDF.org)) is a open-source e-invoice validator for the [ZUGFeRD](https://www.ferd-net.de/zugferd/specification/index.html)/[Factur-X](http://fnfe-mpe.org/factur-x/) standard.

It checks both PDF/A-3 compliance (based on VeraPDF) and ZUGFeRD version 1 respectively 2 (EN16931 profile only) XML for correctness.
The XML check is done by validating against the official ZUGFeRD schematron file for v1 and the EN16931 UN/CEFACT SCRDM v16B uncoupled schematron from the [CEN](https://github.com/CenPC434/validation).


## Build
compile with `/opt/local/bin/mvn clean package`

java -jar ./saxon9he.jar -o:minimum.xsl -s:ZUGFeRD20/Schema/BASIC\ und\ MINIMUM/zugferd2p0_basicwl_minimum.sch ./schematron_to_xslt/iso-schematron-xslt2/iso_svrl_for_xslt2.xsl 


      
## Install

Originally this was intended as VeraPDF plugin in which case you had to install a VeraPDF. Due to deployment issues we switched from ZUV being embedded in VeraPDF to VeraPDF being embedded into UV.

## Run

To check a PDF file for ZUGFEeRD conformance use

`java -jar ZUV-0.7.0.jar --action validate -z <filename of ZUGFeRD PDF.pdf>`

If you just want to check an XML file use

`java -jar ZUV-0.7.0.jar --action validate -x <filename of ZUGFeRD invoice.xml>`



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

Feel free to embed this into your java software, send me a PR to use it as a library, or exec it and parse it's output to put on the web.

For exec, you might try something like  
```
exec('java -Dfile.encoding=UTF-8 -jar /path/to/ZUV-0.7.0.jar --action validate -z '.escapeshellarg($uploadfile).' 2>/dev/null', $output);
```
* Redirecting stderr away (some logging messages might otherwise disturb XML well formedness)
* Escaping any file names in case you use original file names at all (apart from security concerns please take into account that they might contain spaces)
* Signal java to use UTF-8 even when otherwise it would not: You might run into trouble with XML files starting with a BOM otherwise and when you exec, keep in mind that you lose all env vars.  

## License

Permissive Open Source APL2, see LICENSE
## History

  * 0.7.0 (2019-05-31) ZUGFeRD 2 compatibility

  * 0.6.0 (2019-02-15) Factur-X compatibility

  * 0.5.0 (2018-09-10) Added license text, upgraded to mustangproject 1.5.3, logging to file, finding signatures, by default disable schematron check for non-matching ZF2 profiles

## Todo

* Use https://github.com/veraPDF/veraPDF-rest as web interface


## Authors

Jochen Staerk "Mustangproject Chief ZUGFeRD amatuer" <jochen@zugferd.org>
