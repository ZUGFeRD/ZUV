package main.java.ZUV;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractEmbeddedFileFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;


/* JS@2017-07-09 comment 2/3: (1/3 is in the pom.xml) 
 * 
 * I want to remove this comment, the one below, and the one in the pom.xml. In order to perform
 * schematron validation on the submitted file.
 
This one goes....	

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;
import com.helger.schematron.xslt.SchematronResourceXSLT;


import javax.xml.transform.stream.StreamSource;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
... up to here

*/

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jochen Stärk
 */
public class ZUGFeRDExtractor extends AbstractEmbeddedFileFeaturesExtractor {

	private static final Logger LOGGER = Logger.getLogger(ZUGFeRDExtractor.class.getCanonicalName());

	@Override
	public List<FeatureTreeNode> getEmbeddedFileFeatures(EmbeddedFileFeaturesData embeddedFileFeaturesData) {
		List<FeatureTreeNode> res = new ArrayList<>();
		String schematronValidationString = "";

		try {
			// final ISchematronResource aResSCH =
			// SchematronResourceSCH.fromFile (new File("ZUGFeRD_1p0.scmt"));
			// ... DOES work but is highly deprecated (and rightly so) because
			// it takes 30-40min,
			ClassLoader classLoader = ZUGFeRDExtractor.class.getClassLoader();
			
			InputStream xsltInputStream =   classLoader.getResourceAsStream("ZUGFeRDSchematronStylesheet.xsl");
			File xsltFile=File.createTempFile("verapdf-zugferd", "xsl");
			if (xsltFile.exists()) {
				xsltFile.delete();
			}
			Files.copy(xsltInputStream, xsltFile.toPath());
/* JS@2017-07-09  comment 3/3: I want to remove this comment, which goes....	
			final ISchematronResource aResSCH = SchematronResourceXSLT.fromFile(xsltFile);
			// takes around 10 Seconds.
			// http://www.bentoweb.org/refs/TCDL2.0/tsdtf_schematron.html
			// explains that
			// this xslt can be created using sth like
			// saxon java net.sf.saxon.Transform -o tcdl2.0.tsdtf.sch.tmp.xsl -s
			// tcdl2.0.tsdtf.sch iso_svrl.xsl
			if (!aResSCH.isValidSchematron()) {
				throw new IllegalArgumentException("Invalid Schematron!");
			}
			SchematronOutputType sout = aResSCH
					.applySchematronValidationToSVRL(new StreamSource(embeddedFileFeaturesData.getStream()));

			for (String currentString : sout.getText()) {
				schematronValidationString += currentString;
			}

			addObjectNode("Validation", schematronValidationString, res);
... up to here
 */
			addObjectNode("Validation", "test10", res);
			xsltFile.delete();
			
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "IO/Exception when adding information", e);
		}
		
		return res;
	}

	private static void addObjectNode(String nodeName, Object toAdd, List<FeatureTreeNode> list)
			throws FeatureParsingException {
		if (toAdd != null) {
			FeatureTreeNode node = FeatureTreeNode.createRootNode(nodeName);
			node.setValue(toAdd.toString());
			list.add(node);
		}
	}

	private static String formatXMLDate(Calendar calendar) throws DatatypeConfigurationException {
		if (calendar == null) {
			return null;
		}
		GregorianCalendar greg = new GregorianCalendar(Locale.US);
		greg.setTime(calendar.getTime());
		greg.setTimeZone(calendar.getTimeZone());
		XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(greg);
		return xmlCalendar.toXMLFormat();

	}

	private static byte[] inputStreamToByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[1024];
		int length;
		while ((length = is.read(bytes)) != -1) {
			baos.write(bytes, 0, length);
		}
		return baos.toByteArray();
	}
}
