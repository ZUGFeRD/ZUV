# ZUV
Proof of concept for a ZUgferd plugin (or policy) for Verapdf

Essentially this is supposed to be made available as a jar file which can be embedded via Maven and a standalone server version which starts a Jetty Webserver and allows file uploads. It is supposed check both PDF/A-3 compliance (based on VeraPDF) and ZUGFeRD XML correctness, e.g. with by validating against the official ZUGFeRD schematron file.
