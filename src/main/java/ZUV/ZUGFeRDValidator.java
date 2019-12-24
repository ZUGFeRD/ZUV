package ZUV;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.riversun.bigdoc.bin.BigFileSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//abstract class
public class ZUGFeRDValidator {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZUGFeRDValidator.class.getCanonicalName()); // log
																												// output
	protected ValidationContext context = new ValidationContext(LOGGER);
	private String sha1Checksum;
	private boolean pdfValidity;
	private boolean displayXMLValidationOutput;
	private long startTime;
	private boolean optionsRecognized;
	private String Signature;
	private boolean wasCompletelyValid = false;

	/***
	 * within the validation it turned out something in the options was wrong, e.g.
	 * the file did not exist. recommendation to show the help text again. Should be
	 * false if XML or PDF file was found
	 */
	public boolean hasOptionsError() {
		return !optionsRecognized;

	}

	/***
	 * in case the result was not valid the error code of the app will be set to -1
	 * 
	 * @return
	 */
	public boolean wasCompletelyValid() {
		return wasCompletelyValid;

	}

	/***
	 * performs a validation on the file filename
	 * 
	 * @param filename
	 * @return
	 */
	public String validate(String filename) {
		boolean xmlValidity;
		context.clear();
		StringBuffer finalStringResult = new StringBuffer();
		finalStringResult.append("<validation>");
		try {

			if (filename == null) {
				optionsRecognized = false;
				context.addResultItem(new ValidationResultItem(ESeverity.fatal, "Filename not specified").setSection(10)
						.setPart(EPart.pdf));
			}

			PDFValidator pdfv = new PDFValidator(context);
			File file = new File(filename);
			if (!file.exists()) {
				context.addResultItem(new ValidationResultItem(ESeverity.fatal, "File " + filename + " not found")
						.setSection(1).setPart(EPart.pdf));
			} else if (file.length() < 32) {
				// with less then 32 bytes it can not even be a proper XML file
				context.addResultItem(new ValidationResultItem(ESeverity.fatal, "File " + filename + " too small")
						.setSection(5).setPart(EPart.pdf));
			} else {
				BigFileSearcher searcher = new BigFileSearcher();
				XMLValidator xv = new XMLValidator(context);

				boolean isXML = searcher.indexOf(file, "<?xml".getBytes()) != -1;

				byte[] pdfSignature = { '%', 'P', 'D', 'F' };
				boolean isPDF = searcher.indexOf(file, pdfSignature) == 0;
				if (isPDF) {
					pdfv.setFilename(filename);

					optionsRecognized = true;
					try {
						if (!file.exists()) {
							context.addResultItem(
									new ValidationResultItem(ESeverity.exception, "File " + filename + " not found")
											.setSection(1));
						}
					} catch (IrrecoverableValidationError irx) {
						// @todo log
					}

					finalStringResult.append("<pdf>");
					optionsRecognized = true;
					try {
						pdfv.validate();

						sha1Checksum = calcSHA1(file);

						// Validate PDF

						finalStringResult.append(pdfv.getXMLResult());
						pdfValidity = context.isValid();
						Signature = context.getSignature();
						context.clear();
						if (pdfv.getRawXML() != null) {
							xv.setStringContent(pdfv.getRawXML());
							displayXMLValidationOutput = true;
						} else {
							context.addResultItem(
									new ValidationResultItem(ESeverity.exception, "XML could not be extracted")
											.setSection(17));
						}
					} catch (IrrecoverableValidationError irx) {
						// @todo log
					}

					finalStringResult.append("</pdf>\n");
				} else if (isXML) {
					pdfValidity = true;
					optionsRecognized = true;
					xv.setFilename(filename);
					if (file.exists()) {
						sha1Checksum = calcSHA1(file);
					}

					displayXMLValidationOutput = true;

				} else {
					optionsRecognized = false;
					context.addResultItem(new ValidationResultItem(ESeverity.exception,
							"File does not look like PDF nor XML (contains neither %PDF nor <?xml)").setSection(8));

				}
				if ((optionsRecognized) && (displayXMLValidationOutput)) {
					finalStringResult.append("<xml>");
					try {
						xv.validate();
					} catch (IrrecoverableValidationError irx) {
						// @todo log
					}
					finalStringResult.append(xv.getXMLResult());
					finalStringResult.append("</xml>");

				}

			}
		}

		catch (IrrecoverableValidationError irx) {
			// @todo log
		} finally {
			finalStringResult.append(context.getXMLResult());

		}
		finalStringResult.append("</validation>");
		xmlValidity = context.isValid();
		long duration = Calendar.getInstance().getTimeInMillis() - startTime;

		LOGGER.info("Parsed PDF:" + (pdfValidity ? "valid" : "invalid") + " XML:" + (xmlValidity ? "valid" : "invalid")
				+ " Signature:" + Signature + " Checksum:" + sha1Checksum + " Profile:" + context.getProfile()
				+ " Version:" + context.getVersion() + " Took:" + duration + "ms");
		wasCompletelyValid = ((pdfValidity) && (xmlValidity));
		return finalStringResult.toString();
	}

	/**
	 * Read the file and calculate the SHA-1 checksum
	 * 
	 * @param file the file to read
	 * @return the hex representation of the SHA-1 using uppercase chars
	 * @throws FileNotFoundException    if the file does not exist, is a directory
	 *                                  rather than a regular file, or for some
	 *                                  other reason cannot be opened for reading
	 * @throws IOException              if an I/O error occurs
	 * @throws NoSuchAlgorithmException should never happen
	 */
	private static String calcSHA1(File file) {
		MessageDigest sha1 = null;
		try {

			sha1 = MessageDigest.getInstance("SHA-1");
			InputStream input = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int len = input.read(buffer);

			while (len != -1) {
				sha1.update(buffer, 0, len);
				len = input.read(buffer);
			}
			input.close();
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error(e.getMessage(), e);
		}
		if (sha1 == null) {
			return "";
		} else {
			return new HexBinaryAdapter().marshal(sha1.digest());
		}
	}

}