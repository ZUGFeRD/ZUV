package ZUV;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
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
import com.helger.schematron.xslt.SchematronResourceXSLT;
import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;
import org.riversun.bigdoc.bin.BigFileSearcher;

public class Main {

	static final ClassLoader cl = Main.class.getClassLoader();

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getCanonicalName()); // log output is
																									// ignored for the
																									// time being

	protected static String ZUGFeRDVersion = null;

	protected static String ZUGFeRDProfile = null;

	/***
	 * The signature is the library or SDK used to add ZUGFeRD information, if that
	 * can be determined
	 */
	protected static String Signature = null;

	public static void main(String[] args) {

		long startTime = Calendar.getInstance().getTimeInMillis();

		/***
		 * prerequisite is a mvn generate-resources
		 */

		CmdLineParser parser = new CmdLineParser();
		Option<String> actionOption = parser.addStringOption('a', "action");
		Option<String> pdfFilenameOption = parser.addStringOption('z', "ZUGFeRDfilename");
		Option<String> xmlFilenameOption = parser.addStringOption('x', "XMLfilename");
		Option<Boolean> overrideOption = parser.addBooleanOption('o', "overrideprofilecheck");

		Option<Boolean> licenseOption = parser.addBooleanOption('l', "license");
		Option<Boolean> helpOption = parser.addBooleanOption('h', "help");

		boolean optionsRecognized = false;

		try {
			parser.parse(args);
		} catch (CmdLineParser.OptionException e) {
			System.err.println(e.getMessage());
			System.exit(-2);
		}

		Boolean helpRequested = parser.getOptionValue(helpOption);
		Boolean overrideRequested = parser.getOptionValue(overrideOption);

		if (parser.getOptionValue(licenseOption) != null) {
			optionsRecognized = true;

			System.out.println("Copyright 2018 Jochen Stärk\n" + "\n"
					+ "Licensed under the Apache License, Version 2.0 (the \"License\");\n"
					+ "you may not use this file except in compliance with the License.\n"
					+ "You may obtain a copy of the License at\n" + "\n"
					+ "    http://www.apache.org/licenses/LICENSE-2.0\n" + "\n"
					+ "Unless required by applicable law or agreed to in writing, software\n"
					+ "distributed under the License is distributed on an \"AS IS\" BASIS,\n"
					+ "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
					+ "See the License for the specific language governing permissions and\n"
					+ "limitations under the License.\n\n\n\n\n"
					+ "This software is embedding the PDF/A validator VeraPDF, "
					+ "http://verapdf.org/, which is available under GPL and MPL licenses.");

			System.exit(0);
		}
		String pdfFileName = parser.getOptionValue(pdfFilenameOption);
		String xmlFileName = parser.getOptionValue(xmlFilenameOption);
		String action = parser.getOptionValue(actionOption);
		File logdir = new File("log");
		String zfXML = "";
		if (!logdir.exists() || !logdir.isDirectory() || !logdir.canWrite()) {
			System.err.println("Need writable subdirectory 'log' for log files.");
		}
		String sha1Checksum="";

		if ((action!=null)&&(action.equals("validate"))) {

			System.out.println("<validation>");
			if (pdfFileName != null) {
				optionsRecognized = true;
				File file = new File(pdfFileName);
				if (!file.exists()) {
					System.out.println(
							"<xml><info><errors><error code='1'>File not found</error></errors></info></xml></validation>");
					LOGGER.error("Error 1: PDF file "+pdfFileName+" not found");

					System.exit(-1);

				}

				sha1Checksum = calcSHA1(file);

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
				ProcessorConfig processorConfig = ProcessorFactory.fromValues(validatorConfig, featureConfig,
						pluginsConfig, fixerConfig, tasks);
				// Creating processor and output stream.
				ByteArrayOutputStream reportStream = new ByteArrayOutputStream();
				String pdfReport = "";
				try (BatchProcessor processor = ProcessorFactory.fileBatchProcessor(processorConfig)) {
					// Generating list of files for processing
					List<File> files = new ArrayList<>();
					files.add(new File(pdfFileName));
					// starting the processor
					processor.process(files, ProcessorFactory.getHandler(FormatOption.MRR, true, reportStream, 100,
							processorConfig.getValidatorConfig().isRecordPasses()));

					pdfReport = reportStream.toString("utf-8")
							.replaceAll("<\\?xml version=\"1\\.0\" encoding=\"utf-8\"\\?>", "");
				} catch (VeraPDFException e) {
					System.out.println(
							"<error code='6'>" + e.getMessage() + ":" + e.getStackTrace() + "</error>");

					LOGGER.error(e.getMessage(), e);
				} catch (IOException excep) {
					System.out.println("<error code='7'>" + excep.getMessage() + ":" + excep.getStackTrace()
							+ "</error>");
					LOGGER.error(excep.getMessage(), excep);
				}

				
				// step 2 find signature
				try {
					byte[] mustangSignature = "via mustangproject".getBytes("UTF-8");
					byte[] facturxpythonSignature = "by Alexis de Lattre".getBytes("UTF-8");
					byte[] intarsysSignature = "intarsys ".getBytes("UTF-8");
					byte[] konikSignature = "Konik".getBytes("UTF-8");
					BigFileSearcher searcher = new BigFileSearcher();

					if (searcher.indexOf(file, mustangSignature) != -1) {
						Signature = "Mustang";
					} else if (searcher.indexOf(file, facturxpythonSignature) != -1) {
						Signature = "Factur/X Python";
					} else if (searcher.indexOf(file, intarsysSignature) != -1) {
						Signature = "Intarsys";
					} else if (searcher.indexOf(file, konikSignature) != -1) {
						Signature = "Konik";
					}
				} catch (UnsupportedEncodingException e) {
					LOGGER.error(e.getMessage(), e);
				}

				System.out.println(pdfReport + "</pdf>");

				ZUGFeRDImporter zi = new ZUGFeRDImporter();
				zi.extract(pdfFileName);
				zfXML = zi.getUTF8();
			}
			// step 3 Validate ZUGFeRD
			
			System.out.println("<xml>");

			if (xmlFileName != null) {
				optionsRecognized = true;
				File file = new File(xmlFileName);
				
				if (!file.exists()) {
					System.out.println(
							"<xml><info><errors><error code='2'>File not found</error></errors></info></xml></validation>");
					LOGGER.error("XML file "+xmlFileName+" not found");

					System.exit(-1);

				}
				
				sha1Checksum = calcSHA1(file);

				try {
					zfXML = removeBOMFromString(Files.readAllBytes(Paths.get(xmlFileName)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (zfXML.isEmpty()) {
				System.out.println(
						"<xml><info><errors><error code='3'>XML data not found: did you specify a pdf or xml file and does the xml file contain an embedded XML file?</error></errors></info></xml></validation>");
				LOGGER.error("No XML data found ");

				System.exit(-1);
			}
			long startXMLTime = Calendar.getInstance().getTimeInMillis();
			System.out.println("<info><duration unit='ms'>" + (startXMLTime - startTime) + "</duration></info>");

			System.out.println(validateZUGFeRD(zfXML, overrideRequested != null && overrideRequested.booleanValue()));
			long endTime = Calendar.getInstance().getTimeInMillis();
			System.out.println("<info><version>" + ((ZUGFeRDVersion != null) ? ZUGFeRDVersion : "invalid")
					+ "</version><profile>" + ((ZUGFeRDProfile != null) ? ZUGFeRDProfile : "invalid")
					+ "</profile><signature>" + ((Signature != null) ? Signature : "unknown")
					+ "</signature><duration unit='ms'>" + (endTime - startXMLTime) + "</duration></info>");
			System.out.println("</xml>");

			System.out.println("<info><duration unit='ms'>" + (endTime - startTime)
					+ "</duration><checksum type='sha1'>" + sha1Checksum + "</checksum></info>");
			LOGGER.info("Validationresult: SHA1: {}, Version: "
					+ ((ZUGFeRDVersion != null) ? ZUGFeRDVersion : "invalid") + ", Profile: "
					+ ((ZUGFeRDProfile != null) ? ZUGFeRDProfile : "invalid") + ", Signature: "
					+ ((Signature != null) ? Signature : "unknown") + ", Duration: " + (endTime - startTime) + " ms.",
					sha1Checksum);

			System.out.println("</validation>");

		}

		if ((!optionsRecognized) || (helpRequested != null && helpRequested.booleanValue())) {
			System.out.println("usage: --action validate -z <ZUGFeRD PDF Filename.pdf>|-x <ZUGFeRD XML Filename.xml> [-l (shows license)] [-o overrideprofilecheck: check all ZF2 against EN16931]");
			System.exit(-1);
		}

	}

	/**
	 * Read the file and calculate the SHA-1 checksum
	 * 
	 * @param file
	 *            the file to read
	 * @return the hex representation of the SHA-1 using uppercase chars
	 * @throws FileNotFoundException
	 *             if the file does not exist, is a directory rather than a regular
	 *             file, or for some other reason cannot be opened for reading
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws NoSuchAlgorithmException
	 *             should never happen
	 */
	private static String calcSHA1(File file) {
		MessageDigest sha1 = null;
		try {

			sha1 = MessageDigest.getInstance("SHA-1");
			InputStream input = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int len = input.read(buffer);

			while (len != -1) {
				sha1.update(buffer, 0, len);
				len = input.read(buffer);
			}

		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(e.getMessage(), e);
		}
		if (sha1 == null) {
			return "";
		} else {
			return new HexBinaryAdapter().marshal(sha1.digest());
		}
	}
	
	protected static String removeBOMFromString(byte[] rawXML) {
		byte[] bomlessData;
		
	    if ((rawXML!=null)&&(rawXML.length>3)&&(rawXML[0] == (byte) 0xEF)
	                    && (rawXML[1] == (byte) 0xBB)
	                    && (rawXML[2] == (byte) 0xBF)) {
	            // I don't like BOMs, lets remove it
	            bomlessData = new byte[rawXML.length - 3];
	            System.arraycopy(rawXML, 3, bomlessData, 0,
	            		rawXML.length - 3);
	    } else {
	    		bomlessData = rawXML;
	    }
		
		return new String(bomlessData);
	
	}

	
	/***
	 * 
	 * @param xmlString
	 * @param overrideProfileCheck
	 *            if set to true, all ZF2 files will be checked against EN16931
	 *            schematron, since no other schematron is available
	 * @return
	 */
	public static String validateZUGFeRD(String xmlString, boolean overrideProfileCheck) {
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
			// urn:ferd:CrossIndustryDocument:invoice:1p0:extended,
			// urn:ferd:CrossIndustryDocument:invoice:1p0:comfort,
			// urn:ferd:CrossIndustryDocument:invoice:1p0:basic,
			// urn:cen.eu:en16931:2017
			// urn:cen.eu:en16931:2017:compliant:factur-x.eu:1p0:basic
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

			schematronValidationString += "<messages>";
			if (ZUGFeRDVersion.equals("1") || (ZUGFeRDVersion.equals("2(public preview?)")
					&& ((ZUGFeRDProfile.startsWith("urn:cen.eu:en16931:2017")) || (overrideProfileCheck)))) {

				SchematronOutputType sout = aResSCH
						.applySchematronValidationToSVRL(new StreamSource(new StringReader(xmlString)));

				List<Object> failedAsserts = sout.getActivePatternAndFiredRuleAndFailedAssert();
				if (failedAsserts.size() > 0) {
					schematronValidationString += "<errors>";
					for (Object object : failedAsserts) {
						if (object instanceof FailedAssert) {

							FailedAssert failedAssert = (FailedAssert) object;

							schematronValidationString += "<error code='4'><criterion>" + failedAssert.getTest()
									+ "</criterion><result>" + failedAssert.getText() + "</result><location>"+failedAssert.getLocation()+"</location></error>\n";
						}

					}
					schematronValidationString += "</errors>";

				}
				for (String currentString : sout.getText()) {

					schematronValidationString += "<output>" + currentString + "</output>";
				}

				// schematronValidationString += new SVRLMarshaller ().getAsString (sout);
				// returns the complete SVRL

			} else {
				schematronValidationString += "<notices><notice code='5'>XML validation not yet implemented for profile type '"
						+ ZUGFeRDProfile
						+ "', you might try the override option -o to check nevertheless</notice></notices>";
			}
			schematronValidationString += "</messages>";

		} catch (

		Exception ex) {
			schematronValidationString += "<exception message='" + ex.getMessage() + "'>" + ex.getStackTrace()
					+ "</exception>";
			LOGGER.error(ex.getMessage(), ex);
		}

		return schematronValidationString;
	}

}
