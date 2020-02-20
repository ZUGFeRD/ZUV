package ZUV;

import java.io.File;

public class ZUGFeRDValidatorTest extends ResourceCase {

	public void testPDFValidation() {
		File tempFile = getResourceAsFile("invalidPDF.pdf");
		ZUGFeRDValidator zfv = new ZUGFeRDValidator();

		String report = zfv.validate(tempFile.getAbsolutePath());
		assertEquals(true, report.contains("status='invalid'/></pdf>"));
		assertEquals(true, report.contains("status='invalid'/></validation>"));
		
		 tempFile = getResourceAsFile("validAvoir_FR_type380_BASICWL.pdf");
		 zfv = new ZUGFeRDValidator();

		 report = zfv.validate(tempFile.getAbsolutePath());
		assertEquals(true, report.contains("status='valid'"));
		assertEquals(false, report.contains("status='invalid'"));

		
	}
}
