# ZUV
ZUV (ZUgferd+[VeraPDF](http://VeraPDF.org)) is an open-source e-invoice validator for the [ZUGFeRD](https://www.ferd-net.de/zugferd/specification/index.html)/[Factur-X](http://fnfe-mpe.org/factur-x/) standard.

It checks both PDF/A-3 compliance (based on VeraPDF) and ZUGFeRD version 1 respectively 2 (EN16931 profile only) XMLs for correctness.
The XML check is done by validating against the official ZUGFeRD schematron file for v1 and the EN16931 UN/CEFACT SCRDM v16B uncoupled schematron from the [CEN](https://github.com/CenPC434/validation).


## Build
In the pom.xml directory compile the jar with `/opt/local/bin/mvn clean package`

To prepare the schematron files they have to be converted to XSLT files, which is done with a XSLT transformation
itself.
Get [Saxon](http://saxon.sourceforge.net/#F9.9HE) and the [XSLT](https://github.com/Schematron/stf/tree/master/iso-schematron-xslt2) to convert schematron to XSLT 
and run
`java -jar ./saxon9he.jar -o:minimum.xsl -s:zugferd2p0_basicwl_minimum.sch ./schematron_to_xslt/iso-schematron-xslt2/iso_svrl_for_xslt2.xsl`
to do so.



      
## Install

Originally this was intended as VeraPDF plugin in which case you had to install a VeraPDF. Due to deployment issues we switched from ZUV being embedded in VeraPDF to VeraPDF being embedded into ZUV.

## Run

To check a file for ZUGFeRD conformance use

`java -jar ZUV-0.8.3.jar --action validate -f <filename of ZUGFeRD PDF.pdf>`

You can provide either the complete PDF which will be checked for XML and PDF correctness, or just a XML file, which of course
will only be checked for XML correctness.


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
exec('java -Dfile.encoding=UTF-8 -jar /path/to/ZUV-0.8.3.jar --action validate -f '.escapeshellarg($uploadfile).' 2>/dev/null', $output);
```
* Redirecting stderr away (some logging messages might otherwise disturb XML well formedness)
* Escaping any file names in case you use original file names at all (apart from security concerns please take into account that they might contain spaces)
* Signal java to use UTF-8 even when otherwise it would not: You might run into trouble with XML files starting with a BOM otherwise and when you exec, keep in mind that you lose all env vars.  

## License

Permissive Open Source APL2, see LICENSE

## Codes

| section  | meaning  |
|---|---|
| 1  | file not found  |
| 2  | additional data schema validation fails  |
| 3  | xml data not found  |
| 4  | schematron rule failed  |
| 5  | file too small  |
| 6  | VeraPDFException |
| 7  | IOException PDF  |
| 8  | File does not look like PDF nor XML (contains neither %PDF nor <?xml)  |
| 9  | IOException XML  |
| 11  | XMP Metadata: ConformanceLevel not found  |
| 12  | XMP Metadata: ConformanceLevel contains invalid value  |
| 13  | XMP Metadata: DocumentType not found  |
| 14  | XMP Metadata: DocumentType invalid  |
| 15  | XMP Metadata: Version not found  |
| 16  | XMP Metadata: Version contains invalid value  |
| 18  | schema validation failed  |
| 19  | XMP Metadata: DocumentFileName contains invalid value  |
| 20  | not a pdf  |
| 21  | XMP Metadata: DocumentFileName not found")  |
| 22  | generic XML validation exception  |
| 23  | Not a PDF/A-3  |
| 25  | Unsupported profile type  |
| 26  | No rules matched, XML to minimal?  |

## History

see the [history file](History.md)

## Todo

* Use https://github.com/veraPDF/veraPDF-rest as web interface


## Authors

Jochen Staerk "Mustangproject Chief ZUGFeRD amatuer" <jochen@zugferd.org>

https://blog.eight02.com/2011/05/validating-xml-with-iso-schematron-on.html

Saxon 9he from https://sourceforge.net/projects/saxon/
https://github.com/Schematron/stf
/Users/jstaerk/Downloads/stf-master/iso-schematron-xslt2/iso_svrl_for_xslt2.xsl
java -jar /Users/jstaerk/Downloads/SaxonHE9-9-1-6J\ \(1\)/saxon9he.jar -o:minimum.xsl -s:FACTUR-X_MINIMUM.scmt /Users/jstaerk/Downloads/stf-master/iso-schematron-xslt2/iso_svrl_for_xslt2.xsl

					// http://www.bentoweb.org/refs/TCDL2.0/tsdtf_schematron.html // explains that
					// this xslt can be created using sth like
					// saxon java net.sf.saxon.Transform -o tcdl2.0.tsdtf.sch.tmp.xsl -s
					// tcdl2.0.tsdtf.sch iso_svrl.xsl

Syntax error near {...radeAddress[ram:CountryID=7...} at char 158 in expression in xsl:when/@test on line 10762 column 187 of en16931.xsl:
  XPST0003: expected ")", found "<eof>"

  java -jar /Users/jstaerk/Downloads/SaxonHE9-9-1-6J\ \(1\)/saxon9he.jar -o:zugferd21_basic.xsl -s:/Users/jstaerk/workspace/zugferd/release/BASIC/FACTUR-X_BASIC.scmt /Users/jstaerk/Downloads/stf-master/iso-schematron-xslt1/iso_svrl_for_xslt1.xsl  