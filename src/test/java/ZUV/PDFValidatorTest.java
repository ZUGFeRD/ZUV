package ZUV;

import java.io.File;
import java.util.Vector;

public class PDFValidatorTest extends ResourceCase  {

	protected Vector<ValidationResultItem> results;

	public void testPDFValidation() {

		results = new Vector<ValidationResultItem>();
		PDFValidator pv = new PDFValidator(new ValidationContext());
		File tempFile = getResourceAsFile("MustangGnuaccountingBeispielRE-20171118_506.pdf");

		pv.setFilename(tempFile.getAbsolutePath());
		pv.validate();
		String actual=pv.getXMLResult();
		assertEquals(true, actual.contains("validationReport profileName=\"PDF/A-3"));
		assertEquals(true, actual.contains("batchSummary totalJobs=\"1\" failedToParse=\"0\" encrypted=\"0\""));
		assertEquals(true, actual.contains("validationReports compliant=\"1\" nonCompliant=\"0\" failedJobs=\"0\">"));

		// test some xml
		assertEquals(true, actual.contains("<error  location='/*:CrossIndustryInvoice[namespace-uri()='urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100'][1]/*:SupplyChainTradeTransaction[namespace-uri()='urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100'][1]/*:ApplicableHeaderTradeSettlement[namespace-uri()='urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100'][1]/*:SpecifiedTradeSettlementHeaderMonetarySummation[namespace-uri()='urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100'][1]/*:DuePayableAmount[namespace-uri()='urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100'][1]' criterion='not(@currencyID)'>[CII-DT-031] - currencyID should not be present</error>"));
		// test some binary signature recognition
		assertEquals(true, actual.contains("<version>2</version>"));
	}

}
