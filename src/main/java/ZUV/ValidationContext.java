package ZUV;

import java.util.Vector;

public class ValidationContext {
	protected Vector<ValidationResultItem> results;
	protected String customXML="";
	private String version=null;
	private String profile=null;
	private String signature=null;
	
	public ValidationContext() {
		results=new Vector<ValidationResultItem>();
	}
	
	public Vector<ValidationResultItem> getResultItems() {
		return results;
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
	
}
