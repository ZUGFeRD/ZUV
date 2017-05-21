package ZUV;

import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebInitParam;

import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.PDFAValidator;



public class Main {

	public static void main(String[] args) {


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
