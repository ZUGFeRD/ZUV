package ZUV;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;

import org.mustangproject.ZUGFeRD.ZUGFeRDImporter;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceXSLT;
import com.sanityinc.jargs.CmdLineParser;
import com.sanityinc.jargs.CmdLineParser.Option;

public class Main {

	static final ClassLoader cl = Main.class.getClassLoader();
	private static final Logger LOGGER = Logger.getLogger(Main.class.getCanonicalName());

	public static void main(String[] args) {

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

		System.out.println("Validating...");
		ZUGFeRDImporter zi = new ZUGFeRDImporter();
		zi.extract(fileName);
		System.out.println("Reading ZUGFeRD");
		if (zi.canParse()) {
			System.out.println(validateZUGFeRD(zi.getMeta()));
		}

	}

	public static String validateZUGFeRD(String xmlString) {
		Calendar rightNow = Calendar.getInstance();
		long startTime = rightNow.getTimeInMillis();
		{
			ByteArrayInputStream xmlByteInputStream = new ByteArrayInputStream(
					xmlString.getBytes(StandardCharsets.UTF_8));

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
					aResSCH = SchematronResourceXSLT
							.fromClassPath("/xslt/cii16931schematron/EN16931-CII-validation2.xslt");
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
						schematronValidationString += failedAssert.getText() + "\n";
						schematronValidationString += failedAssert.getTest() + "\n";
					}
				}
				for (String currentString : sout.getText()) {

					schematronValidationString += currentString;
				}

			} catch (

			Exception ex) {
				ex.printStackTrace();
			}
			rightNow = Calendar.getInstance();
			long endTime = rightNow.getTimeInMillis();
			LOGGER.info("Took " + (endTime - startTime) + " ms.");
			return schematronValidationString;

		}
	}
}
