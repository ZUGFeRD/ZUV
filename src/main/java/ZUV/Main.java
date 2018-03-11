package ZUV;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;

import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;
import org.verapdf.core.VeraPDFException;
import org.verapdf.features.FeatureExtractorConfig;
import org.verapdf.features.FeatureFactory;
import org.verapdf.features.FeatureObjectType;
import org.verapdf.metadata.fixer.FixerFactory;
import org.verapdf.metadata.fixer.MetadataFixerConfig;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.validation.validators.ValidatorConfig;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.BatchProcessor;
import org.verapdf.processor.FormatOption;
import org.verapdf.processor.ProcessorConfig;
import org.verapdf.processor.ProcessorFactory;
import org.verapdf.processor.TaskType;
import org.verapdf.processor.plugins.Attribute;
import org.verapdf.processor.plugins.PluginConfig;
import org.verapdf.processor.plugins.PluginsCollectionConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceXSLT;
import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;

public class Main {

	static final ClassLoader cl = Main.class.getClassLoader();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getCanonicalName()); // log output is ignored for the time being

	public static void main(String[] args) {
	
		long startTime = Calendar.getInstance().getTimeInMillis();

		/***
		 * prerequisite is a mvn generate-resources
		 */

		CmdLineParser parser = new CmdLineParser();
		Option<String> filenameOption = parser.addStringOption('f', "filename");
		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			System.exit(-2);
		}

		String fileName = parser.getOptionValue(filenameOption);
		if (fileName == null) {
			System.err.println("usage: -f <ZUGFeRD PDF Filename.pdf>");

			System.exit(-1);
		}

		System.err.println("<validation><pdf>");
		// Validate PDF

		VeraGreenfieldFoundryProvider.initialise();
		// Default validator config
		ValidatorConfig validatorConfig = ValidatorFactory.defaultConfig();
		// Default features config
		FeatureExtractorConfig featureConfig = FeatureFactory.defaultConfig();
		// Default plugins config
		PluginsCollectionConfig pluginsConfig = PluginsCollectionConfig.defaultConfig();
		// Default fixer config
		MetadataFixerConfig fixerConfig = FixerFactory.defaultConfig();
		// Tasks configuring
		EnumSet tasks = EnumSet.noneOf(TaskType.class);
		tasks.add(TaskType.VALIDATE);
		// tasks.add(TaskType.EXTRACT_FEATURES);
		// tasks.add(TaskType.FIX_METADATA);
		// Creating processor config
		ProcessorConfig processorConfig = ProcessorFactory.fromValues(validatorConfig, featureConfig, pluginsConfig,
				fixerConfig, tasks);
		// Creating processor and output stream.
		ByteArrayOutputStream reportStream = new ByteArrayOutputStream();
		String pdfReport = "";
		try (BatchProcessor processor = ProcessorFactory.fileBatchProcessor(processorConfig)) {
			// Generating list of files for processing
			List<File> files = new ArrayList<>();
			files.add(new File(fileName));
			// starting the processor
			processor.process(files, ProcessorFactory.getHandler(FormatOption.MRR, true, reportStream, 100,
					processorConfig.getValidatorConfig().isRecordPasses()));

			pdfReport = reportStream.toString("utf-8").replaceAll("<\\?xml version=\"1\\.0\" encoding=\"utf-8\"\\?>",
					"");
		} catch (VeraPDFException e) {
			System.err.println("<exception message='" + e.getMessage() + "'>" + e.getStackTrace() + "</exception>");

			LOGGER.error(e.getMessage());
		} catch (IOException excep) {
			System.err.println(
					"<exception message='" + excep.getMessage() + "'>" + excep.getStackTrace() + "</exception>");
			LOGGER.error(excep.getMessage());
		}

		long startXMLTime = Calendar.getInstance().getTimeInMillis();
		LOGGER.info("Took " + (startXMLTime - startTime) + " ms.");
		System.err.println("<info><duration unit='ms'>" + (startXMLTime - startTime) + "</duration></info>");

		// Validate ZUGFeRD
		System.err.println(pdfReport + "</pdf><xml>");

		ZUGFeRDImporter zi = new ZUGFeRDImporter();
		zi.extract(fileName);
		if (zi.canParse()) {
			System.err.println(validateZUGFeRD(zi.getMeta()));
		}
		long endTime = Calendar.getInstance().getTimeInMillis();
		LOGGER.info("Took " + (endTime - startTime) + " ms.");
		System.err.println("<info><duration unit='ms'>" + (endTime - startXMLTime) + "</duration></info>");
		System.err.println("</xml>");
		System.err.println("<info><duration unit='ms'>" + (endTime - startTime) + "</duration></info>");

		System.err.println("</validation>");

	}

	public static String validateZUGFeRD(String xmlString) {

		ByteArrayInputStream xmlByteInputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));

		String schematronValidationString = "";

		// final ISchematronResource aResSCH =
		// SchematronResourceSCH.fromFile (new File("ZUGFeRD_1p0.scmt"));
		// ... DOES work but is highly deprecated (and rightly so) because
		// it takes 30-40min,

		try {

			/***
			 * private static final String VALID_SCHEMATRON = "test-sch/valid01.sch";
			 * private static final String VALID_XMLINSTANCE = "test-xml/valid01.xml";
			 * 
			 * @Test public void testWriteValid () throws Exception { final Document aDoc =
			 *       SchematronResourceSCH.fromClassPath (VALID_SCHEMATRON)
			 *       .applySchematronValidation (new ClassPathResource (VALID_XMLINSTANCE));
			 * 
			 */

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(xmlByteInputStream);

			Element root = doc.getDocumentElement();
			ISchematronResource aResSCH = null;

			if (root.getNodeName().equalsIgnoreCase("rsm:CrossIndustryInvoice")) {
				// ZUGFeRD 2.0
				aResSCH = SchematronResourceXSLT.fromClassPath("/xslt/cii16931schematron/EN16931-CII-validation2.xslt");
				// final ISchematronResource aResSCH = SchematronResourceXSLT.fromFile(new
				// File("/Users/jstaerk/workspace/ZUV/src/main/resources/ZUGFeRDSchematronStylesheet.xsl"));

				// takes around 10 Seconds.
				// http://www.bentoweb.org/refs/TCDL2.0/tsdtf_schematron.html
				// explains that
				// this xslt can be created using sth like
				// saxon java net.sf.saxon.Transform -o tcdl2.0.tsdtf.sch.tmp.xsl -s
				// tcdl2.0.tsdtf.sch iso_svrl.xsl

			} else {
				// ZUGFeRD 1.0
				aResSCH = SchematronResourceXSLT.fromClassPath("/xslt/ZUGFeRD_1p0.xslt");

			}
			if (!aResSCH.isValidSchematron()) {
				throw new IllegalArgumentException("Invalid Schematron!");
			}

			SchematronOutputType sout = aResSCH
					.applySchematronValidationToSVRL(new StreamSource(new StringReader(xmlString)));
			List<Object> failedAsserts = sout.getActivePatternAndFiredRuleAndFailedAssert();
			for (Object object : failedAsserts) {
				if (object instanceof FailedAssert) {

					FailedAssert failedAssert = (FailedAssert) object;

					schematronValidationString += "<error><criterion>" + failedAssert.getTest() + "</criterion><result>"
							+ failedAssert.getText() + "</result>\n";
				}

			}
			for (String currentString : sout.getText()) {

				schematronValidationString += "<output>" + currentString + "</output>";
			}

		} catch (

		Exception ex) {
			schematronValidationString += "<exception message='" + ex.getMessage() + "'>" + ex.getStackTrace()
					+ "</exception>";
			LOGGER.error(ex.getMessage());
		}
		return schematronValidationString;
	}

}
