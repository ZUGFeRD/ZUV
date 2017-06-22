package ZUV;

import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.annotation.WebInitParam;
import javax.xml.transform.stream.StreamSource;

import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.results.ValidationResult;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.xslt.SchematronResourceSCH;
import com.helger.schematron.xslt.SchematronResourceXSLT;

import org.verapdf.pdfa.PDFAValidator;



public class Main {

	public static void main(String[] args) {

		 System.out.println("Starting validation");
		  // Instantiate a Date object
	      Date date = new Date();

	      // display time and date using toString()
	      System.out.println(date.toString());
		try {
			//final ISchematronResource aResSCH = SchematronResourceSCH.fromFile (new File("ZUGFeRD_1p0.scmt"));
			//  ... DOES work but is highly deprecated (and rightly so) because it takes 30-40min,
			final ISchematronResource aResSCH = SchematronResourceXSLT.fromFile (new File("utils/ZUGFeRDSchematronStylesheet.xsl"));
			// takes around 10 Seconds. http://www.bentoweb.org/refs/TCDL2.0/tsdtf_schematron.html explains that
			// this xslt can be created using sth like 
			// saxon java net.sf.saxon.Transform -o tcdl2.0.tsdtf.sch.tmp.xsl -s tcdl2.0.tsdtf.sch iso_svrl.xsl
		    if (!aResSCH.isValidSchematron ())
	      throw new IllegalArgumentException ("Invalid Schematron!");
	    SchematronOutputType sout= aResSCH.applySchematronValidationToSVRL (new StreamSource (new File("ZUGFeRD-invoice.xml")));
		
	    for (String currentString : sout.getText()) {
			System.out.println(currentString);
		}
	    
	    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	      date = new Date();

	      // display time and date using toString()
	      System.out.println(date.toString());
		 System.out.println("Starting HTTP Server on :8080");
		 WebInterfaceThread wit=new WebInterfaceThread();
		 wit.start();

		 System.out.println("Press any key to exit");
		 try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 System.out.println("Shutting down");
		 wit.interrupt();
		 System.exit(0);

	}

}
