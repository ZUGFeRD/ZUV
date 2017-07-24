package ZUV;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.verapdf.core.FeatureParsingException;
import org.verapdf.features.AbstractEmbeddedFileFeaturesExtractor;
import org.verapdf.features.EmbeddedFileFeaturesData;
import org.verapdf.features.tools.FeatureTreeNode;

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
	
		    Date startDate = new Date();
			   
			ByteArrayOutputStream schematronValidationReport=new ByteArrayOutputStream();
			SchematronPipeline.applySchematronXsl(embeddedFileFeaturesData.getStream(), schematronValidationReport);

			schematronValidationString=schematronValidationReport.toString("UTF-8");
			
			String filenameCheck="fail";
			if (embeddedFileFeaturesData.getName().equals("ZUGFeRD-invoice.xml")) {
				filenameCheck="pass";
			}
			addObjectNode("Validation", "Check for filename:"+filenameCheck, res);
			addObjectNode("FullReport", schematronValidationString, res);
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    //get current date time with Date()
			 Date endDate = new Date();
			   

			addObjectNode("ValidationStart", dateFormat.format(startDate), res);
			addObjectNode("ValidationEnd", dateFormat.format(endDate), res);
			
			
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
