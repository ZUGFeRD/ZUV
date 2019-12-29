package ZUV;

import java.io.File;

public class ZUGFeRDValidatorTest extends ResourceCase {

	public void testPDFValidation() {
		File tempFile = getResourceAsFile("invalidPDF.pdf");
		ZUGFeRDValidator zfv = new ZUGFeRDValidator();

		String report = zfv.validate(tempFile.getAbsolutePath());
		assertEquals(true, report.contains("status='invalid'/></pdf>"));
		assertEquals(true, report.contains("status='invalid'/></validation>"));

	}
}
