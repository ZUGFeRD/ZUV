package ZUV;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractEmbeddedFileFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;


import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;
import com.helger.schematron.xslt.SchematronResourceXSLT;


import javax.xml.transform.stream.StreamSource;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
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
		//	Handler fh = new FileHandler("/tmp/wombat.log");
		//	LOGGER.addHandler(fh);

			// final ISchematronResource aResSCH =
			// SchematronResourceSCH.fromFile (new File("ZUGFeRD_1p0.scmt"));
			// ... DOES work but is highly deprecated (and rightly so) because
			// it takes 30-40min,
	//		final ISchematronResource aResSCH = SchematronResourceXSLT.fromClassPath("/ZUGFeRDSchematronStylesheet.xsl");
			final SchematronResourceXSLT aResSCH = SchematronResourceXSLT.fromFile(new File("/Users/jstaerk/workspace/ZUV/src/main/resources/ZUGFeRDSchematronStylesheet.xsl"));
					// takes around 10 Seconds.
			// http://www.bentoweb.org/refs/TCDL2.0/tsdtf_schematron.html
			// explains that
			// this xslt can be created using sth like
			// saxon java net.sf.saxon.Transform -o tcdl2.0.tsdtf.sch.tmp.xsl -s
			// tcdl2.0.tsdtf.sch iso_svrl.xsl

			if (aResSCH.getXSLTProvider()==null) {
				throw new IllegalArgumentException("Invalid provider!!");
			}

			if (!aResSCH.isValidSchematron()) {
				throw new IllegalArgumentException("Invalid Schematron!!");
			}
			SchematronOutputType sout = aResSCH.applySchematronValidationToSVRL(new StreamSource(
					new FileInputStream(new File("/Users/jstaerk/workspace/ZUV/ZUGFeRD-invoice.xml"))));

//			SchematronOutputType sout = aResSCH
//					.applySchematronValidationToSVRL(new StreamSource(embeddedFileFeaturesData.getStream()));

			List<Object> failedAsserts = sout.getActivePatternAndFiredRuleAndFailedAssert();
			for (Object object : failedAsserts) {
				if (object instanceof FailedAssert) {
					FailedAssert failedAssert = (FailedAssert) object;
					schematronValidationString+=failedAssert.getText();
					schematronValidationString+=failedAssert.getTest();
				}
			}


			addObjectNode("ValidationResult", schematronValidationString, res);
			
			addObjectNode("Validation", "test12", res);
			
		} catch (Exception e) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			
			LOGGER.log(Level.WARNING, e.getMessage()+" @ "+sw.toString(), e);
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
