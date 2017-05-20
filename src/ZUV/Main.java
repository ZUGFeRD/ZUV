package ZUV;

import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.PDFAValidator;



public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 VeraGreenfieldFoundryProvider.initialise();
		 String filename="veraPDF test suite 6-8-t02-pass-g.pdf";
		 System.out.println("Testing validity of "+filename);
		 try (PDFAParser parser = Foundries.defaultInstance().createParser(new FileInputStream(filename))) {
		      PDFAValidator validator = Foundries.defaultInstance().createValidator(parser.getFlavour(), false);
		      ValidationResult result = validator.validate(parser);
		      if (result.isCompliant()) {
		        // File is a valid PDF/A 1b
		    		 System.out.println("Complies");
		      } else {
		        // it isn't

		    		 System.out.println("Fails "+result.toString());
		      }
		  } catch (ModelParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EncryptedPdfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
