package ZUV;

public class SchemaPipeline {
	import javax.xml.XMLConstants;
	import javax.xml.transform.Source;
	import javax.xml.transform.stream.StreamSource;
	import javax.xml.validation.*;
	import java.net.URL;
	import org.xml.sax.SAXException;
	//import java.io.File; // if you use File
	import java.io.IOException;
	...
	URL schemaFile = new URL("http://host:port/filename.xsd");
	// webapp example xsd: 
	// URL schemaFile = new URL("http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd");
	// local file example:
	// File schemaFile = new File("/location/to/localfile.xsd"); // etc.
	Source xmlFile = new StreamSource(new File("web.xml"));
	SchemaFactory schemaFactory = SchemaFactory
	    .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
	try {
	  Schema schema = schemaFactory.newSchema(schemaFile);
	  Validator validator = schema.newValidator();
	  validator.validate(xmlFile);
	  System.out.println(xmlFile.getSystemId() + " is valid");
	} catch (SAXException e) {
	  System.out.println(xmlFile.getSystemId() + " is NOT valid reason:" + e);
	} catch (IOException e) {}
}
