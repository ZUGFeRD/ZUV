# ZUV
Proof of concept for a ZUgferd plugin (or policy) for Verapdf

Essentially this is supposed to be made available as a jar file which can be embedded via Maven and a standalone server version which starts a Jetty Webserver and allows file uploads. It is supposed check both PDF/A-3 compliance (based on VeraPDF) and ZUGFeRD XML correctness, e.g. with by validating against the official ZUGFeRD schematron file.


## Build
compile with `/opt/local/bin/mvn` clean package 


## Install

Download the VeraPDF installer from http://verapdf.org/software/ and install. 
Make sure you check the "veraPDF Sample Plugins" option in the installation process.

Copy the ZUV jar file into the plugins-subdirectory of the [VeraPDF installation directory].

 Add 

```xml
  <plugin enabled="true">
    <name>ZUGFeRD Validator</name>
    <version>0.3.0</version>
    <description>Validates ZUGFeRD PDFs against the official Schematron. Developed by Jochen Staerk.</description>
    <pluginJar>[VeraPDF installation directory]/plugins/ZUV-0.3.0.jar</pluginJar>
  </plugin>
```

to the config/plugin.xml file in the [VeraPDF installation directory] (and of course don't forget to replace
the placeholder [VeraPDF installation directory] with the complete path of your VeraPDF installation).


## Run

Run `verapdf-gui` . Check that embedded files is checked in Config|Features Config.
Select a ZUGFeRD file like [the mustangproject sample file](http://www.mustangproject.org/MustangGnuaccountingBeispielRE-20170509_505.pdf).
Select "Validation and Features" as report type and hit the execute button.
This will currently still take an awful lot of time (10-20 Minutes!).


## Output

Click the "View XML" button. We're working on publishing the schematron validation results in the node
Report/Jobs/job/featuresReport/embeddedFiles/customFeatures/pluginFeatures[name="ZUGFeRD Validator"].

## Embed

If you want to embed the functionality in your product please take note of 

http://docs.verapdf.org/develop/
and 
http://docs.verapdf.org/develop/processor/

From which it is relatively easy to derive 

```java
		PdfBoxFoundryProvider.initialise();
		// Default validator config
		ValidatorConfig validatorConfig = ValidatorFactory.defaultConfig();
		// Default features config
		FeatureExtractorConfig featureConfig = FeatureFactory.configFromValues(EnumSet.of(FeatureObjectType.EMBEDDED_FILE));
		// Default plugins config
		List<Attribute> a =new ArrayList<Attribute>();
		PluginConfig pc=PluginConfig.fromValues(true, "ZUV", "0.1", "ZUV description", Paths.get("/your/zuv/directory/andfilename.jar"), a);
		
		List<PluginConfig> pcl =new ArrayList<PluginConfig>();
		pcl.add(pc);
		PluginsCollectionConfig pluginsConfig = PluginsCollectionConfig.fromValues(pcl);
		// Default fixer config
		MetadataFixerConfig fixerConfig = FixerFactory.defaultConfig();
		// Tasks configuring
		EnumSet tasks = EnumSet.noneOf(TaskType.class);
		tasks.add(TaskType.VALIDATE);
		tasks.add(TaskType.EXTRACT_FEATURES);
		//tasks.add(TaskType.FIX_METADATA);
		// Creating processor config
		ProcessorConfig processorConfig = ProcessorFactory.fromValues(validatorConfig, featureConfig, pluginsConfig, fixerConfig, tasks);
		// Creating processor and output stream. In this example output stream is System.out
		try (BatchProcessor processor = ProcessorFactory.fileBatchProcessor(processorConfig);
			 OutputStream reportStream = System.out) {
			// Generating list of files for processing
			List<File> files = new ArrayList<>();
			files.add(new File("/directory/and/filenametovalidate.pdf"));
			// starting the processor
			processor.process(files, ProcessorFactory.getHandler(FormatOption.MRR, true, reportStream,
							100, processorConfig.getValidatorConfig().isRecordPasses()));
		} catch (VeraPDFException e) {
			System.err.println("Exception raised while processing batch");
			e.printStackTrace();
		} catch (IOException excep) {
			System.err.println("Exception raised closing MRR temp file.");
			excep.printStackTrace();
		}
```

## License

Permissive Open Source APL2, see LICENSE

## Todo

* Use https://github.com/veraPDF/veraPDF-rest as web interface
* Find out if anything from the konik.io validator can be used
* Find out which ZUGFeRD tools have been used if they can be identified
* output the results (not only in a xml file)

## Authors

Jochen Staerk
