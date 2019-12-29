package ZUV;

import java.io.File;

public class XMLValidatorTest extends ResourceCase {
	
	public void testZF2XMLValidation() {
		// ignored for the
		// time being

		ValidationContext ctx = new ValidationContext(null);
		XMLValidator xv = new XMLValidator(ctx);
		File tempFile = getResourceAsFile("invalidV2.xml");

		try {
			xv.setFilename(tempFile.getAbsolutePath());

			xv.validate();

			/*
			 * assertEquals(true, xv.getXMLResult().
			 * contains("<error location=\"/*[local-name()='CrossIndustryInvoice']/*[local-name()='SupplyChainTradeTransaction']/*[local-name()='ApplicableHeaderTradeSettlement']/*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']\" criterion=\"(ram:LineTotalAmount)\">\n"
			 * +
			 * "	Eine Rechnung (INVOICE) muss die Summe der Rechnungspositionen-Nettobeträge „Sum of Invoice line net amount“ (BT-106) enthalten.</error>\n"
			 * +
			 * "<error location=\"/*[local-name()='CrossIndustryInvoice']/*[local-name()='SupplyChainTradeTransaction']/*[local-name()='ApplicableHeaderTradeSettlement']/*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']\" criterion=\"(ram:TaxBasisTotalAmount = ram:LineTotalAmount - ram:AllowanceTotalAmount + ram:ChargeTotalAmount) or ((ram:TaxBasisTotalAmount = ram:LineTotalAmount - ram:AllowanceTotalAmount) and not (ram:ChargeTotalAmount)) or ((ram:TaxBasisTotalAmount = ram:LineTotalAmount + ram:ChargeTotalAmount) and not (ram:AllowanceTotalAmount)) or ((ram:TaxBasisTotalAmount = ram:LineTotalAmount) and not (ram:ChargeTotalAmount) and not (ram:AllowanceTotalAmount))\">\n"
			 * +
			 * "	Der Inhalt des Elementes „Invoice total amount without VAT“ (BT-109) entspricht der Summe aller Inhalte der Elemente „Invoice line net amount“ (BT-131) abzüglich der Summe aller in der Rechnung enthaltenen Nachlässe der Dokumentenebene „Sum of allowances on document level“ (BT-107) zuzüglich der Summe aller in der Rechnung enthaltenen Abgaben der Dokumentenebene „Sum of charges on document level“ (BT-108).</error>\n"
			 * +
			 * "<error location=\"/*[local-name()='CrossIndustryInvoice']/*[local-name()='SupplyChainTradeTransaction']/*[local-name()='ApplicableHeaderTradeSettlement']/*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']\" criterion=\"(ram:GrandTotalAmount = round(ram:TaxBasisTotalAmount*100 + ram:TaxTotalAmount[@currencyID=/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:InvoiceCurrencyCode]*100 +0) div 100) or ((ram:GrandTotalAmount = ram:TaxBasisTotalAmount) and not (ram:TaxTotalAmount[@currencyID=/rsm:CrossIndustryInvoice/rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:InvoiceCurrencyCode]))\">\n"
			 * +
			 * "	Der Inhalt des Elementes „Invoice total amount with VAT“ (BT-112) entspricht der Summe des Inhalts des Elementes „Invoice total amount without VAT“ (BT-109) und des Elementes „Invoice total VAT amount“ (BT-110).</error>\n"
			 * +
			 * "<error location=\"/*[local-name()='CrossIndustryInvoice']/*[local-name()='SupplyChainTradeTransaction']/*[local-name()='ApplicableHeaderTradeSettlement']/*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']\" criterion=\"ram:LineTotalAmount = (round(sum(../../ram:IncludedSupplyChainTradeLineItem/ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeSettlementLineMonetarySummation/ram:LineTotalAmount) * 10 * 10)div 100)\">\n"
			 * +
			 * "	Der Inhalt des Elementes „Sum of Invoice line net amount“ (BT-106) entspricht der Summe aller Inhalte der Elemente „Invoice line net amount“ (BT-131).</error>\n"
			 * +
			 * "<error location=\"/*[local-name()='CrossIndustryInvoice']\" criterion=\"(rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty/ram:Name!='')\">\n"
			 * +
			 * "	Eine Rechnung (INVOICE) muss den Erwerbernamen „Buyer name“ (BT-44) enthalten.</error>\n"
			 * +
			 * "<error location=\"/*[local-name()='CrossIndustryInvoice']/*[local-name()='SupplyChainTradeTransaction']/*[local-name()='ApplicableHeaderTradeAgreement']/*[local-name()='BuyerTradeParty']\" criterion=\"count(ram:Name)=1\">\n"
			 * + "	Das Element 'ram:Name' muss genau 1 mal auftreten.</error>\n" +
			 * "<error location=\"/*[local-name()='CrossIndustryInvoice']/*[local-name()='SupplyChainTradeTransaction']/*[local-name()='ApplicableHeaderTradeSettlement']/*[local-name()='SpecifiedTradeSettlementHeaderMonetarySummation']\" criterion=\"count(ram:LineTotalAmount)=1\">\n"
			 * + "	Das Element 'ram:LineTotalAmount' muss genau 1 mal auftreten.</error>\n"
			 * +
			 * "<error location=\"/*[local-name()='CrossIndustryInvoice']/*[local-name()='SupplyChainTradeTransaction']/*[local-name()='IncludedSupplyChainTradeLineItem'][2]/*[local-name()='SpecifiedLineTradeDelivery']/*[local-name()='BilledQuantity']\" criterion=\"document('zugferd2p0_extended_codedb.xml')//cl[@id=7]/enumeration[@value=$codeValue7]\">\n"
			 * + "	Wert von '@unitCode' ist unzulässig.</error>\n" +
			 * "</messages><summary status='invalid'/>"));
			 *
			 */

			tempFile = getResourceAsFile("invalidV2Profile.xml");

			xv.setFilename(tempFile.getAbsolutePath());

			xv.validate();
		} catch (IrrecoverableValidationError e) {
			// ignore, will be in XML output anyway
		}
		assertTrue(xv.getXMLResult().contains("<error type=\"25\""));

		ctx.clear();
		tempFile = getResourceAsFile("validV2Basic.xml");
		try {

			xv.setFilename(tempFile.getAbsolutePath());
			xv.validate();
			assertEquals(true, xv.getXMLResult().contains("valid") && !xv.getXMLResult().contains("invalid"));

			ctx.clear();
			tempFile = getResourceAsFile("ZUGFeRD-invoice_rabatte_3_abschlag_duepayableamount.xml");
			xv.setFilename(tempFile.getAbsolutePath());
			xv.validate();
			assertEquals(true, xv.getXMLResult().contains("valid") && !xv.getXMLResult().contains("invalid"));

		/* this test failure might have to be upstreamed
		 	ctx.clear();
			tempFile = getResourceAsFile("ZUGFeRD-invoice_rabatte_4_abschlag_taxbasistotalamount.xml");
			xv.setFilename(tempFile.getAbsolutePath());
			xv.validate();
			assertEquals(true, xv.getXMLResult().contains("valid") && !xv.getXMLResult().contains("invalid"));
		*/	
			ctx.clear();
			tempFile = getResourceAsFile("attributeBasedXMP_zugferd_2p0_EN16931_Einfach_corrected.xml");
			xv.setFilename(tempFile.getAbsolutePath());
			xv.validate();
			assertEquals(true, xv.getXMLResult().contains("valid") && !xv.getXMLResult().contains("invalid"));
			
		} catch (IrrecoverableValidationError e) {
			// ignore, will be in XML output anyway
		}

	}

	public void testZF1XMLValidation() {
		ValidationContext ctx = new ValidationContext(null);
		XMLValidator xv = new XMLValidator(ctx);
		File tempFile = getResourceAsFile("validV1.xml");
		try {
			xv.setFilename(tempFile.getAbsolutePath());
			xv.validate();
			assertEquals(true, xv.getXMLResult().contains("valid") && !xv.getXMLResult().contains("invalid"));

			tempFile = getResourceAsFile("invalidV1ExtraTags.xml");
			xv.setFilename(tempFile.getAbsolutePath());
			xv.validate();
			assertEquals(true, xv.getXMLResult().contains("invalid"));

			tempFile = getResourceAsFile("invalidV1TooMinimal.xml");
			xv.setFilename(tempFile.getAbsolutePath());
			xv.validate();
			assertEquals(true, xv.getXMLResult().contains("<error type=\"26\""));
		} catch (IrrecoverableValidationError e) {
			// ignore, will be in XML output anyway
		}

	}

}
