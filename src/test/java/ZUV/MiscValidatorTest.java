package ZUV;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MiscValidatorTest extends ResourceCase  {


	public void testInvalidFileValidation() {

		ZUGFeRDValidator zfv=new ZUGFeRDValidator();

		String res=zfv.validate(null);
		assertTrue(res.matches("<validation filename='' datetime='.*?'><messages><error type=\"10\">Filename not specified</error>\n" + 
				"</messages><summary status='invalid'/></validation>"));

		res=zfv.validate("/dhfkbv/sfjkh");
		assertTrue(res.matches("<validation filename='sfjkh' datetime='.*?'><messages><error type=\"1\">File not found</error>\n" + 
								"</messages><summary status='invalid'/></validation>"));

		File tempFile=null;
		try {
			tempFile = File.createTempFile("hello", ".tmp");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		res=zfv.validate(tempFile.getAbsolutePath());
		assertTrue(res.matches("<validation.*?><messages>" + 
				"<error type=\"5\">File too small</error>\n" + 
				"</messages><summary status='invalid'/></validation>"));
		
		
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
		assertTrue(res.matches("<validation filename='"+tempFile.getName()+"' .*?><messages><exception type=\"8\">File does not look like PDF nor XML .*</exception>\n" + 
				"</messages><summary status='invalid'/></validation>"));

		
		// clean up
		tempFile.delete();
		
	}
	
}
