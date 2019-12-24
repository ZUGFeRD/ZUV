package ZUV;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MiscValidatorTest extends ResourceCase  {


	public void testInvalidFileValidation() {

		ZUGFeRDValidator zfv=new ZUGFeRDValidator();

		String res=zfv.validate("/dhfkbv/sfjkh");
		assertEquals("<validation><messages><error type=\"1\">File /dhfkbv/sfjkh not found</error>\n" + 
				"</messages><summary status='invalid'/></validation>", res);

		File tempFile=null;
		try {
			tempFile = File.createTempFile("hello", ".tmp");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		res=zfv.validate(tempFile.getAbsolutePath());
		assertEquals("<validation><messages>" + 
				"<error type=\"5\">File "+tempFile.getAbsolutePath()+" too small</error>\n" + 
				"</messages><summary status='invalid'/></validation>", res);
		
		
		String fileContent = "ladhvkdbfk  wkhfbkhdhkb svbkfsvbksfbvk sdvsdvbksjdvbkfdsv sdvbskdvbsjhkvbfskh dvbskfvbkfsbvke"
				+ "ladhvkdbfk  wkhfbkhdhkb svbkfsvbksfbvk sdvsdvbksjdvbkfdsv sdvbskdvbsjhkvbfskh dvbskfvbkfsbvke";
	     
	    BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(tempFile));
		    writer.write(fileContent);
		    writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 

		res=zfv.validate(tempFile.getAbsolutePath());
		assertEquals("<validation><messages><exception type=\"8\">File does not look like PDF nor XML (contains neither %PDF nor <?xml)</exception>\n" + 
				"</messages><summary status='invalid'/></validation>", res);

		
		// clean up
		tempFile.delete();
		
	}
	
}
