package ZUV;

import java.util.Vector;

import org.slf4j.Logger;

public class ValidationContext {
	protected Vector<ValidationResultItem> results;
	protected String customXML="";
	private String version=null;
	private String profile=null;
	private String signature=null;
	private boolean isValid=true;
	protected Logger logger;
	
	public ValidationContext(Logger log) {
		logger=log;
		results=new Vector<ValidationResultItem>();
	}
	public void addResultItem(ValidationResultItem vr) throws IrrecoverableValidationError {
		results.add(vr);

		if ((vr.getSeverity()==ESeverity.fatal)||(vr.getSeverity()==ESeverity.exception)) {
			logger.error("Fatal Error "+vr.getSection()+": "+vr.getMessage());
			isValid=false;
			throw new IrrecoverableValidationError(vr.getMessage());
		} else if ((vr.getSeverity()==ESeverity.error)) {
			logger.error("Error "+vr.getSection()+": "+vr.getMessage());
			isValid=false;
		} else if (vr.getSeverity()==ESeverity.warning) {
			logger.warn("Warning "+vr.getSection()+": "+vr.getMessage());
		}
		else if (vr.getSeverity()==ESeverity.notice) {
			logger.info("Notice "+vr.getSection()+": "+vr.getMessage());

		}
		
	}
		
	public void addCustomXML(String XML) {
		customXML+=XML;
	}
	public String getCustomXML() {
		return customXML;
	}
	public ValidationContext setVersion(String version) {
		this.version=version;
		return this;
	}
	public ValidationContext setProfile(String profile) {
		this.profile=profile;
		return this;
	}
	public ValidationContext setSignature(String signature) {
		this.signature=signature;
		return this;
	}
	public String getVersion() {
		return version;
	}
	public String getProfile() {
		return profile;
	}
	public String getSignature() {
		return signature;
	}
	
	public boolean isValid() {
		return isValid;
	}
	public void clear() {
		results.clear();
		isValid=true;
		customXML="";
		version=null;
		profile=null;
		signature=null;
		
	}

	public String getXMLResult() {
		String res=getCustomXML();
		if (results.size() > 0) {
			res+="<messages>";
		}

		for (ValidationResultItem validationResultItem : results) {
			res+=validationResultItem.getXML()+"\n";
		}
		if (results.size() > 0) {
			res+= "</messages>";
		}
		res+= "<summary status='"+(isValid?"valid":"invalid")+"'/>";
		return res;
	}
	public void setInvalid() {
		isValid=false;
	}
}
