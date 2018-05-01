package ZUV;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.verapdf.core.VeraPDFException;
import org.verapdf.features.FeatureExtractorConfig;
import org.verapdf.features.FeatureFactory;
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
import org.verapdf.processor.plugins.PluginsCollectionConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.svrl.SVRLMarshaller;
import com.helger.schematron.xslt.SchematronResourceXSLT;
import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;

public class Main {

	static final ClassLoader cl = Main.class.getClassLoader();

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getCanonicalName()); // log output is
																									// ignored for the
																									// time being

	protected static String ZUGFeRDVersion = null;

	protected static String ZUGFeRDProfile = null;

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

		System.out.println("<validation><pdf>");
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
			System.out.println("<exception message='" + e.getMessage() + "'>" + e.getStackTrace() + "</exception>");

			LOGGER.error(e.getMessage());
		} catch (IOException excep) {
			System.out.println(
					"<exception message='" + excep.getMessage() + "'>" + excep.getStackTrace() + "</exception>");
			LOGGER.error(excep.getMessage());
		}

		long startXMLTime = Calendar.getInstance().getTimeInMillis();
		LOGGER.info("Took " + (startXMLTime - startTime) + " ms.");
		System.out.println("<info><duration unit='ms'>" + (startXMLTime - startTime) + "</duration></info>");

		// Validate ZUGFeRD
		System.out.println(pdfReport + "</pdf><xml>");

		ZUGFeRDImporter zi = new ZUGFeRDImporter();
		zi.extract(fileName);
		System.out.println(validateZUGFeRD(zi.getMeta()));
		long endTime = Calendar.getInstance().getTimeInMillis();
		LOGGER.info("Took " + (endTime - startTime) + " ms.");
		System.out.println("<info><version>" + ((ZUGFeRDVersion != null) ? ZUGFeRDVersion : "invalid")
				+ "</version><profile>" + ((ZUGFeRDProfile != null) ? ZUGFeRDProfile : "invalid")
				+ "</profile><duration unit='ms'>" + (endTime - startXMLTime) + "</duration></info>");
		System.out.println("</xml>");
		System.out.println("<info><duration unit='ms'>" + (endTime - startTime) + "</duration></info>");

		System.out.println("</validation>");

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
			dbf.setNamespaceAware(true); // otherwise we can not act namespace independently, i.e. use
											// document.getElementsByTagNameNS("*",...

			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(xmlByteInputStream);

			Element root = doc.getDocumentElement();
			ISchematronResource aResSCH = null;

			NodeList ndList;

			// rootNode = document.getDocumentElement();
			// ApplicableSupplyChainTradeSettlement

			// Create XPathFactory object
			XPathFactory xpathFactory = XPathFactory.newInstance();

			// Create XPath object
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr = xpath.compile(
					"//*[local-name()=\"GuidelineSpecifiedDocumentContextParameter\"]/*[local-name()=\"ID\"]/text()");
			// evaluate expression result on XML document
			ndList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int bookingIndex = 0; bookingIndex < ndList.getLength(); bookingIndex++) {
				Node booking = ndList.item(bookingIndex);
				// if there is a attribute in the tag number:value
				// urn:ferd:CrossIndustryDocument:invoice:1p0:extended
				// setForeignReference(booking.getTextContent());

				ZUGFeRDProfile = booking.getNodeValue();
			}

			if (root.getNodeName().equalsIgnoreCase("rsm:CrossIndustryInvoice")) { // ZUGFeRD 2.0
				ZUGFeRDVersion = "2(public preview?)";
				aResSCH = SchematronResourceXSLT.fromClassPath("/xslt/cii16931schematron/EN16931-CII-validation2.xslt"); // final
				/*
				 * ISchematronResource aResSCH = SchematronResourceXSLT.fromFile(new File(
				 * "/Users/jstaerk/workspace/ZUV/src/main/resources/ZUGFeRDSchematronStylesheet.xsl"
				 * ));
				 */

				// takes around 10 Seconds. //
				// http://www.bentoweb.org/refs/TCDL2.0/tsdtf_schematron.html // explains that
				// this xslt can be created using sth like
				// saxon java net.sf.saxon.Transform -o tcdl2.0.tsdtf.sch.tmp.xsl -s
				// tcdl2.0.tsdtf.sch iso_svrl.xsl

			} else { // ZUGFeRD 1.0

				ZUGFeRDVersion = "1";
				aResSCH = SchematronResourceXSLT.fromClassPath("/xslt/ZUGFeRD_1p0.xslt");
			}
			if (!aResSCH.isValidSchematron()) {
				throw new IllegalArgumentException("Invalid Schematron!");
			}

			SchematronOutputType sout = aResSCH
					.applySchematronValidationToSVRL(new StreamSource(new StringReader(xmlString)));

			List<Object> failedAsserts = sout.getActivePatternAndFiredRuleAndFailedAssert();
			if (failedAsserts.size() > 0) {
				schematronValidationString += "<errors>";
				for (Object object : failedAsserts) {
					if (object instanceof FailedAssert) {

						FailedAssert failedAssert = (FailedAssert) object;

						schematronValidationString += "<error><criterion>" + failedAssert.getTest()
								+ "</criterion><result>" + failedAssert.getText() + "</result></error>\n";
					}

				}
				schematronValidationString += "</errors>";

			}
			for (String currentString : sout.getText()) {

				schematronValidationString += "<output>" + currentString + "</output>";
			}

			// schematronValidationString += new SVRLMarshaller ().getAsString (sout);
			// returns the complete SVRL

		} catch (

		Exception ex) {
			schematronValidationString += "<exception message='" + ex.getMessage() + "'>" + ex.getStackTrace()
					+ "</exception>";
			LOGGER.error(ex.getMessage());
		}
		return schematronValidationString;
	}

}
