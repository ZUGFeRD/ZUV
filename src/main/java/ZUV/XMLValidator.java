package ZUV;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceXSLT;

public class XMLValidator extends Validator {
	public XMLValidator(ValidationContext ctx) {
		super(ctx);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLValidator.class.getCanonicalName()); // log output
																											// is
	// ignored for the
	// time being

	protected String zfXML = "";
	protected String filename = "";

	public void setFilename(String name) { // from XML Filename
		filename = name;
		File file = new File(filename);
		if (!file.exists()) {
			context.addResultItem(
					new ValidationResultItem(ESeverity.error, "File not found").setSection(1).setPart(EPart.xml));
			LOGGER.error("Error 1: XML file " + filename + " not found");
			return;
		}

		try {
			zfXML = removeBOMFromString(Files.readAllBytes(Paths.get(name)));
		} catch (IOException e) {

			ValidationResultItem vri = new ValidationResultItem(ESeverity.exception, e.getMessage()).setSection(9)
					.setPart(EPart.xml);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			vri.setStacktrace(sw.toString());
			context.addResultItem(vri);
			LOGGER.error(e.getMessage(), e);
		}
	}

	public void setStringContent(String xml) {
		zfXML = xml;
	}

	protected String removeBOMFromString(byte[] rawXML) {
		byte[] bomlessData;

		if ((rawXML != null) && (rawXML.length > 3) && (rawXML[0] == (byte) 0xEF) && (rawXML[1] == (byte) 0xBB)
				&& (rawXML[2] == (byte) 0xBF)) {
			// I don't like BOMs, lets remove it
			bomlessData = new byte[rawXML.length - 3];
			System.arraycopy(rawXML, 3, bomlessData, 0, rawXML.length - 3);
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
	@Override
	public void validate() {
		long startXMLTime = Calendar.getInstance().getTimeInMillis();

		ByteArrayInputStream xmlByteInputStream = new ByteArrayInputStream(zfXML.getBytes(StandardCharsets.UTF_8));

		if (zfXML.isEmpty()) {
			ValidationResultItem res = new ValidationResultItem(ESeverity.exception,
					"XML data not found in " + filename
							+ ": did you specify a pdf or xml file and does the xml file contain an embedded XML file?")
									.setSection(3);
			context.addResultItem(res);
			LOGGER.error("No XML data found");

		} else {

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

					context.setProfile(booking.getNodeValue());
				}
				boolean isMiniumum = false;
				boolean isEN16931 = false;
				boolean isExtended = false;
				// urn:ferd:CrossIndustryDocument:invoice:1p0:extended,
				// urn:ferd:CrossIndustryDocument:invoice:1p0:comfort,
				// urn:ferd:CrossIndustryDocument:invoice:1p0:basic,

				// urn:cen.eu:en16931:2017
				// urn:cen.eu:en16931:2017:compliant:factur-x.eu:1p0:basic
				if (root.getNodeName().equalsIgnoreCase("rsm:CrossIndustryInvoice")) { // ZUGFeRD 2.0 or Factur-X
					context.setVersion("2");

					isMiniumum = context.getProfile().contains("minimum") || context.getProfile().contains("basic");
					isEN16931 = context.getProfile().equals("urn:cen.eu:en16931:2017:compliant:factur-x.eu:1p0:en16931")
							|| context.getProfile().equals("urn:cen.eu:en16931:2017");

					isExtended = context.getProfile().contains("extended");
					if (isMiniumum) {
						aResSCH = SchematronResourceXSLT.fromClassPath("/xslt/zugferd2p0_basicwl_minimum.xslt");
					} else if (isEN16931) {
						aResSCH = SchematronResourceXSLT.fromClassPath("/xslt/zugferd2p0_en16931.xslt");
					} else if (isExtended) {
						aResSCH = SchematronResourceXSLT.fromClassPath("/xslt/zugferd2p0_extended.xslt");
					} /*
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
					context.setVersion("1");
					//
					if ((!context.getProfile().equals("urn:ferd:CrossIndustryDocument:invoice:1p0:basic"))
							&& (!context.getProfile().equals("urn:ferd:CrossIndustryDocument:invoice:1p0:comfort"))
							&& (!context.getProfile().equals("urn:ferd:CrossIndustryDocument:invoice:1p0:extended"))) {
						context.addResultItem(new ValidationResultItem(ESeverity.error, "Unsupported profile type")
								.setSection(25).setPart(EPart.xml));
					}
					aResSCH = SchematronResourceXSLT.fromClassPath("/xslt/ZUGFeRD_1p0.xslt");
				}

				if (context.getVersion().equals("2") && (!isEN16931) && (!isMiniumum) && (!isExtended)) {
					context.addResultItem(new ValidationResultItem(ESeverity.error,
							"Unsupported profile type").setSection(25).setPart(EPart.xml));

				}
				if (context.getVersion().equals("2")) {
					if ((!context.getProfile().equals("urn:factur-x.eu:1p0:minimum"))
							&& (!context.getProfile().equals("urn:zugferd.de:2p0:minimum"))
							&& (!context.getProfile().equals("urn:factur-x.eu:1p0:basicwl"))
							&& (!context.getProfile().equals("urn:zugferd.de:2p0:basicwl"))
							&& (!context.getProfile()
									.equals("urn:cen.eu:en16931:2017#compliant#urn:factur-x.eu:1p0:basic"))
							&& (!context.getProfile()
									.equals("urn:cen.eu:en16931:2017#compliant#urn:zugferd.de:2p0:basic"))
							&& (!context.getProfile().equals("urn:cen.eu:en16931:2017"))
							&& (!context.getProfile()
									.equals("urn:cen.eu:en16931:2017#conformant#urn:factur-x.eu:1p0:extended"))
							&& (!context.getProfile()
									.equals("urn:cen.eu:en16931:2017#conformant#urn:zugferd.de:2p0:extended")))

					{
						context.addResultItem(
								new ValidationResultItem(ESeverity.error, "Unsupported profile type")
										.setSection(25).setPart(EPart.xml));

					}
				} else /** v1 */
				{//urn:ferd:invoice:rc:comfort
					if ((!context.getProfile().equals("urn:ferd:CrossIndustryDocument:invoice:1p0:basic"))
							&& (!context.getProfile().equals("urn:ferd:CrossIndustryDocument:invoice:1p0:comfort"))
							&& (!context.getProfile().equals("urn:ferd:CrossIndustryDocument:invoice:1p0:extended"))) {
						context.addResultItem(new ValidationResultItem(ESeverity.error, "Unsupported profile type")
								.setSection(25).setPart(EPart.xml));

					}
				}

				if (aResSCH != null) {
					if (!aResSCH.isValidSchematron()) {
						throw new IllegalArgumentException("Invalid Schematron!");
					}

					SchematronOutputType sout = aResSCH
							.applySchematronValidationToSVRL(new StreamSource(new StringReader(zfXML)));

					List<Object> failedAsserts = sout.getActivePatternAndFiredRuleAndFailedAssert();
					if (failedAsserts.size() > 0) {
						for (Object object : failedAsserts) {
							if (object instanceof FailedAssert) {

								FailedAssert failedAssert = (FailedAssert) object;

								context.addResultItem(new ValidationResultItem(ESeverity.error, failedAssert.getText())
										.setLocation(failedAssert.getLocation()).setCriterion(failedAssert.getTest())
										.setPart(EPart.xml));
							}

						}

					}
					for (String currentString : sout.getText()) {

						// schematronValidationString += "<output>" + currentString + "</output>";
					}

					// schematronValidationString += new SVRLMarshaller ().getAsString (sout);
					// returns the complete SVRL

				}

			} catch (Exception e) {
				ValidationResultItem vri = new ValidationResultItem(ESeverity.exception, e.getMessage()).setSection(22)
						.setPart(EPart.xml);
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				vri.setStacktrace(sw.toString());
				context.addResultItem(vri);
				LOGGER.error(e.getMessage(), e);
			}

		}
		long endTime = Calendar.getInstance().getTimeInMillis();

		context.addCustomXML("<info><version>" + ((context.getVersion() != null) ? context.getVersion() : "invalid")
				+ "</version><profile>" + ((context.getProfile() != null) ? context.getProfile() : "invalid")
				+ "</profile>" + "<duration unit='ms'>" + (endTime - startXMLTime) + "</duration></info>");

	}

}
