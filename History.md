0.8.0
======
2019-07-08

* updated Mustang to mitigate #20 zf1 validation does not always return a <xml> section
* validate not only against *schematron* but also against *schema* files

0.7.0
======
2019-05-31

* ZUGFeRD 2 final compatible
* now displaying number of applied and failed rules
* now failing if no rule could be applied at all
* profileoverride option no longer needed
* version dependent checks for profiles (i.e. comfort is not a valid ZF2 profile!)
* allow new ZF2 filename (zugferd-invoice.xml instead ZUGFeRD-invoice.xml)
* more JUnit tests
* include own version number in XML report


0.6.0
=====
2019-02-15
Factur-X compatible
Mention xpath location where the errors occurred
JUnit tests
new way to collect&handle errors
XMP/PDF-A-Schema extension validation


0.5.0
=====
2018-09-10
Non-16931 profiles are no longer automatically (but still can be manually) checked,
now writing a log file, attempt to find out which toolkit created that particular file (issue #2),
fixing that XML could not be extracted from certain valid PDF/A-3 files (issue #9),
added --action parameter (has to be validate), -f option changes to "-o -z", added APL license, 
added possibility to check XML files only, fixing issues with files that contained a 
UTF8 BOM in the XML content ("Content not allowed in prolog"), 
Upgraded VeraPDF-validation from 1.10.5 to 1.12.1

0.4.3
=====
2018-03-19

Java 8 capability (fixes issue #8)

0.4.2
=====
2018-03-11
POJO embedding VeraPDF: unable to solve some severe deployment issues


0.4.1
=====
2018-02-12
ZUGFeRD 2 public preview EN16931 validation using the schematron files the CEN had
published under APL


0.3.0
=====
2017-07-24
First release as VeraPDF plugin