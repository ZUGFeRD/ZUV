package ZUV;

import java.util.Vector;

public class ValidationContext {
	protected Vector<ValidationResultItem> results;
	protected String customXML="";
	private String version=null;
	private String profile=null;
	private String signature=null;
	private boolean isValid=true;
	
	public ValidationContext() {
		results=new Vector<ValidationResultItem>();
	}
	public void addResultItem(ValidationResultItem vr) {
		results.add(vr);
		if ((vr.getSeverity()==ESeverity.error)||(vr.getSeverity()==ESeverity.exception)) {
			isValid=false;
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
