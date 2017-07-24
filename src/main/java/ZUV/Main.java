package ZUV;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceXSLT;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;

public class Main {
	

	static final ClassLoader cl = Main.class.getClassLoader();
	private static final TransformerFactory factory = getTransformerFactory();
	private static final String xslExt = ".xsl"; //$NON-NLS-1$
	private static final String resourcePath = ""; //$NON-NLS-1$
	private static final String isoDsdlXsl = resourcePath + "iso_dsdl_include" + xslExt; //$NON-NLS-1$
	private static final String isoExpXsl = resourcePath + "iso_abstract_expand" + xslExt; //$NON-NLS-1$
	private static final String isoSvrlXsl = resourcePath + "iso_svrl_for_xslt1" + xslExt; //$NON-NLS-1$
	private static final Templates cachedIsoDsdXsl = createCachedTransform(isoDsdlXsl);
	private static final Templates cachedExpXsl = createCachedTransform(isoExpXsl);
	private static final Templates cachedIsoSvrlXsl = createCachedTransform(isoSvrlXsl);

	private static TransformerFactory getTransformerFactory() {
		TransformerFactory fact = TransformerFactory.newInstance();
		fact.setURIResolver(new ClasspathResourceURIResolver());
		return fact;
	}

	static Templates createCachedTransform(final String transName) {
		try {
			return factory.newTemplates(new StreamSource(cl.getResourceAsStream(transName)));
		} catch (TransformerConfigurationException excep) {
			throw new IllegalStateException("Policy Schematron transformer XSL " + transName + " not found.", excep); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	public static void processSchematron(InputStream schematronSource, OutputStream xslDest)
			throws TransformerException, IOException {
		File isoDsdResult = createTempFileResult(cachedIsoDsdXsl.newTransformer(), new StreamSource(schematronSource),
				"IsoDsd"); //$NON-NLS-1$
		File isoExpResult = createTempFileResult(cachedExpXsl.newTransformer(), new StreamSource(isoDsdResult),
				"ExpXsl"); //$NON-NLS-1$
		cachedIsoSvrlXsl.newTransformer().transform(new StreamSource(isoExpResult), new StreamResult(xslDest));
		isoDsdResult.delete();
		isoExpResult.delete();
	}

	private static File createTempFileResult(final Transformer transformer, final StreamSource toTransform,
			final String suffix) throws TransformerException, IOException {
		File result = File.createTempFile("ZUV_", suffix); //$NON-NLS-1$
		result.deleteOnExit();

		try (FileOutputStream fos = new FileOutputStream(result)) {
			transformer.transform(toTransform, new StreamResult(fos));
		}
		return result;
	}
	
	private static class ClasspathResourceURIResolver implements URIResolver {
		ClasspathResourceURIResolver() {
			// Do nothing, just prevents synthetic access warning.
		}

		@Override
		public Source resolve(String href, String base) throws TransformerException {
			return new StreamSource(cl.getResourceAsStream(resourcePath + href));
		}
	}

	public static void main(String[] args) {
		

		System.out.println("Creating xsl files...");

		try {
			Main.processSchematron(new FileInputStream(new File("/Users/jstaerk/workspace/ZUV/src/main/resources/ZUGFeRD_1p0.scmt")), new FileOutputStream("fos.xsl") );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Validating...");

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

			//final ISchematronResource aResSCH = SchematronResourceXSLT.fromClassPath("/ZUGFeRDSchematronStylesheet.xsl");
			final ISchematronResource aResSCH = SchematronResourceXSLT.fromFile(new File("/Users/jstaerk/workspace/ZUV/src/main/resources/ZUGFeRDSchematronStylesheet.xsl"));

			// takes around 10 Seconds.
			// http://www.bentoweb.org/refs/TCDL2.0/tsdtf_schematron.html
			// explains that
			// this xslt can be created using sth like
			// saxon java net.sf.saxon.Transform -o tcdl2.0.tsdtf.sch.tmp.xsl -s
			// tcdl2.0.tsdtf.sch iso_svrl.xsl
			if (!aResSCH.isValidSchematron()) {
				throw new IllegalArgumentException("Invalid Schematron!");
			}
			SchematronOutputType sout = aResSCH.applySchematronValidationToSVRL(new StreamSource(
					new FileInputStream(new File("/Users/jstaerk/workspace/ZUV/ZUGFeRD-invoice.xml"))));
			List<Object> failedAsserts = sout.getActivePatternAndFiredRuleAndFailedAssert();
			for (Object object : failedAsserts) {
				if (object instanceof FailedAssert) {
					FailedAssert failedAssert = (FailedAssert) object;
					System.out.println(failedAssert.getText());
					System.out.println(failedAssert.getTest());
				}
			}
			for (String currentString : sout.getText()) {
				System.out.print(".");
				schematronValidationString += currentString;
			}

		} catch (

		Exception ex) {
			ex.printStackTrace();
		}
		System.out.println(schematronValidationString);

	}
}
