package ZUV;


//abstract class
public abstract class Validator {
	
	protected ValidationContext context;
	
	public Validator(ValidationContext ctx){
		this.context=ctx;
	}
	
	//abstract method
	
	public abstract void setFilename(String filename);
	public abstract void validate();

	public String getXMLResult() {
		return context.getXMLResult();
	}


}