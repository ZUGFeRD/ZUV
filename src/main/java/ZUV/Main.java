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
import java.util.Vector;

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

public class Main  {

	static final ClassLoader cl = Main.class.getClassLoader();
	private Vector<ValidationResultItem> results;
	protected ValidationContext context=new ValidationContext();
	private String customXML="";
	private long startXMLTime;

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getCanonicalName()); // log output is
																									// ignored for the
																									// time being

	public void run(String[] args){

		long startTime = Calendar.getInstance().getTimeInMillis();
		results=new Vector<ValidationResultItem>();
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
		if (!logdir.exists() || !logdir.isDirectory() || !logdir.canWrite()) {
			System.err.println("Need writable subdirectory 'log' for log files.");
		}
		String sha1Checksum = "";

		Vector<ValidationResultItem> results = new Vector<ValidationResultItem>();
		if ((action != null) && (action.equals("validate"))) {

			System.out.println("<validation>");

			if (pdfFileName != null) {
				PDFValidator pdfv = new PDFValidator(context);
				pdfv.setFilename(pdfFileName);
				pdfv.validate();

				optionsRecognized = true;
				File file = new File(pdfFileName);
				if (!file.exists()) {
					results.add(new ValidationResultItem(ESeverity.exception, "File not found").setSection(1));
					LOGGER.error("Error 1: PDF file " + pdfFileName + " not found");

				}

				sha1Checksum = calcSHA1(file);

				System.out.println("<pdf>");
				// Validate PDF

				System.out.println(pdfv.getXMLResult());

				System.out.println("</pdf>");

			} else {

				System.out.println("<xml>");

				XMLValidator xv = new XMLValidator(context);

				if (xmlFileName != null) {
					optionsRecognized = true;
					xv.setFilename(xmlFileName);
					File file = new File(xmlFileName);

					if (!file.exists()) {
						results.add(new ValidationResultItem(ESeverity.exception, "XML file "+xmlFileName+" not found").setSection(1));
					
						LOGGER.error("XML file " + xmlFileName + " not found");

						
					}

					sha1Checksum = calcSHA1(file);
				}
				startXMLTime = Calendar.getInstance().getTimeInMillis();
				System.out.println("<info><duration unit='ms'>" + (startXMLTime - startTime) + "</duration></info>");

				xv.setOverrideProfileCheck(overrideRequested != null && overrideRequested.booleanValue());

				xv.validate();
				
			}

			System.out.println("</xml>");

			String res="";
			for (ValidationResultItem validationResultItem : results) {
				res+=validationResultItem.getXML();
			}
			if (res.length()>0) {
				System.out.println("<messages>"+res+"</messages>");
			}

			System.out.println("</validation>");

		}

		if ((!optionsRecognized) || (helpRequested != null && helpRequested.booleanValue())) {
			System.out.println(
					"usage: --action validate -z <ZUGFeRD PDF Filename.pdf>|-x <ZUGFeRD XML Filename.xml> [-l (shows license)] [-o overrideprofilecheck: check all ZF2 against EN16931]");
			System.exit(-1);
		}

	}
	
	public static void main(String[] args) {
		new Main().run(args);
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


}
