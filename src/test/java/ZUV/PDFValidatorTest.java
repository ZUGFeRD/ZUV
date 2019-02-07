package ZUV;

import java.io.File;

public class PDFValidatorTest extends ResourceCase  {


	public void testPDFValidation() {
		ValidationContext vc=new ValidationContext();
		PDFValidator pv = new PDFValidator(vc);
		
		
		File tempFile = getResourceAsFile("XMLinvalidV2PDF.pdf");

		pv.setFilename(tempFile.getAbsolutePath());
		pv.validate();
	
		XMLValidator xv = new XMLValidator(vc);
		xv.setOverrideProfileCheck(true);		
		xv.setStringContent(pv.getRawXML());
		xv.validate();
		String actual=vc.getXMLResult();

		assertEquals(true, actual.contains("validationReport profileName=\"PDF/A-3"));
		assertEquals(true, actual.contains("batchSummary totalJobs=\"1\" failedToParse=\"0\" encrypted=\"0\""));
		assertEquals(true, actual.contains("validationReports compliant=\"1\" nonCompliant=\"0\" failedJobs=\"0\">"));
	// test some xml
		assertEquals(true, actual.contains("<error location='/*:CrossIndustryInvoice[namespace-uri()='urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100'][1]/*:SupplyChainTradeTransaction[namespace-uri()='urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100'][1]/*:ApplicableHeaderTradeSettlement[namespace-uri()='urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100'][1]/*:SpecifiedTradeSettlementHeaderMonetarySummation[namespace-uri()='urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100'][1]/*:DuePayableAmount[namespace-uri()='urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100'][1]' criterion='not(@currencyID)'>[CII-DT-031] - currencyID should not be present</error>"));
		// test some binary signature recognition
		assertEquals(true, actual.contains("<version>2</version>"));

		// valid one
		tempFile = getResourceAsFile("validV2PDF.pdf");

		pv.setFilename(tempFile.getAbsolutePath());
		vc.clear();
		pv.validate();
		actual=pv.getXMLResult();
		assertEquals(true, actual.contains("validationReport profileName=\"PDF/A-3"));
		assertEquals(true, actual.contains("batchSummary totalJobs=\"1\" failedToParse=\"0\" encrypted=\"0\""));
		assertEquals(true, actual.contains("validationReports compliant=\"1\" nonCompliant=\"0\" failedJobs=\"0\">"));

		assertEquals(false, actual.contains("<error"));
		
		
		
	 tempFile = getResourceAsFile("invalidXMP.pdf");

		pv.setFilename(tempFile.getAbsolutePath());
		vc.clear();
		pv.validate();
		actual=pv.getXMLResult();

		assertEquals(true,actual.contains("<error type='12'>XMP Metadata: ConformanceLevel contains invalid value</error>"));

	}

}
