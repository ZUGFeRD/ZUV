package ZUV;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.verapdf.core.EncryptedPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.results.ValidationResult;

public class UploadServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

       
    public void doPost(HttpServletRequest request, 
            HttpServletResponse response)
           throws ServletException, java.io.IOException {
   // Check that we have a file upload request
   boolean isMultipart = ServletFileUpload.isMultipartContent(request);
   response.setContentType("text/html");
   java.io.PrintWriter out = response.getWriter( );
   if( !isMultipart ){
      out.println("<!DOCTYPE html>\n" + 
      		"<html>\n" + 
      		"<meta charset=\"UTF-8\">\n" + 
      		"<body>\n" + 
      		"<html>");
      out.println("<p>No file uploaded</p>"); 
      out.println("</body>");
      out.println("</html>");
      return;
   }
   DiskFileItemFactory factory = new DiskFileItemFactory();
   // maximum size that will be stored in memory
   factory.setSizeThreshold(1024 * 1024 * 40 );// 40MB
   // Location to save data that is larger than maxMemSize.
   factory.setRepository(new File("/tmp/"));

   // Create a new file upload handler
   ServletFileUpload upload = new ServletFileUpload(factory);
   // maximum file size to be uploaded.
   upload.setSizeMax( 1024 * 1024 * 40  );// 40MB

   try{ 
   // Parse the request to get file items.
   List fileItems = upload.parseRequest(request);
	
   // Process the uploaded file items
   Iterator i = fileItems.iterator();

   out.println("\n" + 
   		"   <!DOCTYPE html>\n" + 
   		"   <html>\n" + 
   		"   <meta charset=\"UTF-8\">\n" + 
   		"   <body>\n" + 
   		"");
   while ( i.hasNext () ) 
   {
      FileItem fi = (FileItem)i.next();
      if ( !fi.isFormField () )	
      {
         // Get the uploaded file parameters
         String fieldName = fi.getFieldName();
         String fileName = fi.getName();
         String contentType = fi.getContentType();
         boolean isInMemory = fi.isInMemory();
         long sizeInBytes = fi.getSize();
         // Write the file
        /* File file;
         if( fileName.lastIndexOf("\\") >= 0 ){
            file = new File(  
            fileName.substring( fileName.lastIndexOf("\\"))) ;
         }else{
            file = new File(  
            fileName.substring(fileName.lastIndexOf("\\")+1)) ;
         }
         fi.write( file ) ;
         */
		 VeraGreenfieldFoundryProvider.initialise();

         out.println("Uploaded Filename: " + fileName + "<br>");
		 try (PDFAParser parser = Foundries.defaultInstance().createParser(fi.getInputStream())) {
		      PDFAValidator validator = Foundries.defaultInstance().createValidator(parser.getFlavour(), false);
		      ValidationResult result = validator.validate(parser);
		      if (result.isCompliant()) {
		        // File is a valid PDF/A 1b
		    		 out.println("Complies");
		      } else {
		        // it isn't

		    		 out.println("Fails "+result.toString());
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
   out.println("</body>");
   out.println("</html>");
}catch(Exception ex) {
    System.out.println(ex);
}
}
}
