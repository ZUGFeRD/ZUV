package ZUV;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;

import org.riversun.bigdoc.bin.BigFileSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.verapdf.core.VeraPDFException;
import org.verapdf.features.FeatureExtractorConfig;
import org.verapdf.features.FeatureFactory;
import org.verapdf.metadata.fixer.FixerFactory;
import org.verapdf.metadata.fixer.MetadataFixerConfig;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.validation.validators.ValidatorConfig;
import org.verapdf.pdfa.validation.validators.ValidatorFactory;
import org.verapdf.processor.BatchProcessor;
import org.verapdf.processor.FormatOption;
import org.verapdf.processor.ProcessorConfig;
import org.verapdf.processor.ProcessorFactory;
import org.verapdf.processor.TaskType;
import org.verapdf.processor.plugins.PluginsCollectionConfig;

public class PDFValidator extends Validator {
	
	public PDFValidator(ValidationContext ctx) {
		super(ctx);
		// TODO Auto-generated constructor stub
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getCanonicalName()); // log output is
	

	private String pdfFilename;

	private String pdfReport;


	private String Signature;
	
	public void validate() {
		File file = new File(pdfFilename);
		if (!file.exists()) {
			context.getResultItems().add(new ValidationResultItem(ESeverity.error,"File not found").setSection(1).setPart(EPart.pdf));
			LOGGER.error("Error 1: PDF file "+pdfFilename+" not found");
			return;
		}
		long startPDFTime = Calendar.getInstance().getTimeInMillis();
		
		// Step 1 Validate PDF
	
		VeraGreenfieldFoundryProvider.initialise();
		// Default validator config
		ValidatorConfig validatorConfig = ValidatorFactory.defaultConfig();
		// Default features config
		FeatureExtractorConfig featureConfig = FeatureFactory.defaultConfig();
		// Default plugins config
		PluginsCollectionConfig pluginsConfig = PluginsCollectionConfig.defaultConfig();
		// Default fixer config
		MetadataFixerConfig fixerConfig = FixerFactory.defaultConfig();
		// Tasks configuring
		EnumSet tasks = EnumSet.noneOf(TaskType.class);
		tasks.add(TaskType.VALIDATE);
		// tasks.add(TaskType.EXTRACT_FEATURES);
		// tasks.add(TaskType.FIX_METADATA);
		// Creating processor config
		ProcessorConfig processorConfig = ProcessorFactory.fromValues(validatorConfig, featureConfig,
				pluginsConfig, fixerConfig, tasks);
		// Creating processor and output stream.
		ByteArrayOutputStream reportStream = new ByteArrayOutputStream();
		try (BatchProcessor processor = ProcessorFactory.fileBatchProcessor(processorConfig)) {
			// Generating list of files for processing
			List<File> files = new ArrayList<>();
			files.add(new File(pdfFilename));
			// starting the processor
			processor.process(files, ProcessorFactory.getHandler(FormatOption.MRR, true, reportStream, 100,
					processorConfig.getValidatorConfig().isRecordPasses()));
	
			pdfReport = reportStream.toString("utf-8")
					.replaceAll("<\\?xml version=\"1\\.0\" encoding=\"utf-8\"\\?>", "");
		} catch (VeraPDFException e) {
			context.getResultItems().add(new ValidationResultItem(ESeverity.exception,  e.getMessage() ).setSection(6).setStacktrace(e.getStackTrace().toString()).setPart(EPart.pdf));
			LOGGER.error(e.getMessage(), e);
		} catch (IOException excep) {
			context.getResultItems().add(new ValidationResultItem(ESeverity.exception,  excep.getMessage() ).setSection(7).setPart(EPart.pdf).setStacktrace(excep.getStackTrace().toString()));
			LOGGER.error(excep.getMessage(), excep);
		}
				

		// step 2 find signature
		try {
			byte[] mustangSignature = "via mustangproject".getBytes("UTF-8");
			byte[] facturxpythonSignature = "by Alexis de Lattre".getBytes("UTF-8");
			byte[] intarsysSignature = "intarsys ".getBytes("UTF-8");
			byte[] konikSignature = "Konik".getBytes("UTF-8");
			BigFileSearcher searcher = new BigFileSearcher();

			if (searcher.indexOf(file, mustangSignature) != -1) {
				Signature = "Mustang";
			} else if (searcher.indexOf(file, facturxpythonSignature) != -1) {
				Signature = "Factur/X Python";
			} else if (searcher.indexOf(file, intarsysSignature) != -1) {
				Signature = "Intarsys";
			} else if (searcher.indexOf(file, konikSignature) != -1) {
				Signature = "Konik";
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage(), e);
		}
		context.getResultItems().add(new ValidationResultItem(ESeverity.info, "Signature: "+Signature).setSection(8).setPart(EPart.pdf));
	
		long endTime = Calendar.getInstance().getTimeInMillis();
		
		context.addCustomXML(pdfReport+"<info><signature>" + ((context.getSignature() != null) ? context.getSignature() : "unknown")
				+ "</signature><duration unit='ms'>" + (endTime - startPDFTime) + "</duration></info>");

		// step 3 validate xml
		XMLValidator xv=new XMLValidator(context);
		xv.setPDF(pdfFilename);
		xv.validate();
	
	}

	@Override
	public void setFilename(String filename) {
		this.pdfFilename=filename;
		
	}

	public String getSignature() {
		return Signature;
	}


}
