import java.io.*;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.*; 

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class XMLUtil 
{	
	public static void writeDocumentToFile(Document doc, String filename) throws Exception
	{		
		// Prepare the DOM document for writing 
		Source source = new DOMSource(doc); 
		// Prepare the output file 
		File file = new File(filename); 
		Result result = new StreamResult(file); 
		// Write the DOM document to the file 
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		xformer.setOutputProperty(OutputKeys.METHOD, "xml");
		xformer.setOutputProperty(OutputKeys.INDENT, "yes");
		xformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
		xformer.transform(source, result);
	}
	public static String writeDocumentToString(Node node) throws Exception
	{
		DOMSource domSource = new DOMSource(node);
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.transform(domSource, result);
		return writer.toString();
	}

	public static String getXMLDocumentAsString(Node node) {
		try {
			java.io.StringWriter sw = new java.io.StringWriter();
			outputXMLToWriter(node, sw);
			return sw.toString();
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	private static void outputXMLToWriter(Node node, Writer writer) throws Exception 
	{
		DOMSource domSource = new DOMSource(node);
		outputXMLToWriter(domSource, writer);
	}

	private static void outputXMLToWriter(DOMSource domSource, Writer writer) throws Exception 
	{
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StreamResult sr = new StreamResult(writer);
		transformer.transform(domSource, sr);
	}
	
	public static Document createDocument(String rootElementName)
	{
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        DOMImplementation di = db.getDOMImplementation();
	        Document doc = di.createDocument(null, rootElementName, null);
	        return doc;
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static Document getDocumentFromFile(String filename) 
	{
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        
	        return docBuilder.parse(new File(filename));
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static Document getDocumentFromURL(String urlString) 
	{
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        URL url = new URL(urlString);
			InputStream stream = url.openStream();
	        return docBuilder.parse(stream);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	// assumes UTF-8 or UTF-16 as encoding,
	public static String makeXMLSafe(String content)
	{
	    StringBuffer buffer = new StringBuffer();
	    for(int i = 0;i < content.length();i++)
	    {
	       char c = content.charAt(i);
	       if(c == '<')
	          buffer.append("&lt;");
	       else if(c == '>')
	          buffer.append("&gt;");
	       else if(c == '&')
	          buffer.append("&amp;");
	       else if(c == '"')
	          buffer.append("&quot;");
	       else if(c == '\'')
	          buffer.append("&apos;");
	       else
	          buffer.append(c);
	    }
	    return buffer.toString();
	}
	
	public static HashMap<String, String> getHashMapOfAttributes(Element element)
	{
		HashMap<String, String> map = new HashMap<String, String>();
		
		NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++)
		{
			Attr attribute = (Attr)attributes.item(i);
			map.put(attribute.getNodeName(), attribute.getNodeValue());
		}
		
		return map;
	}
	public static Document getDocumentFromString(String xml) throws Exception
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		
		return document;
	}
}

class NoRecordSeparatorReader extends FilterReader 
{
    // (For testing)
    public static void main(String[] args) throws IOException 
    {
          String s = "Rec1\u001eRec2\u001eRec3";
          NoRecordSeparatorReader in = new NoRecordSeparatorReader(new StringReader(s));
          int c = -1;
          while ((c = in.read()) > -1) 
          {
                System.out.print((char)c);
          }
          in.close();
    }

    public NoRecordSeparatorReader(Reader in) 
    {
          super(in);
    }

    public int read() throws IOException 
    {
          int c = in.read();
          switch (c) 
          {
	          case 0x1E:
	                return read();
	          default:
	                return c;
          }
    }

    public int read(char cbuf[], int off, int len) throws IOException 
    {
          int charsRead = 0;
          int c = -1;
          while ((c = read()) > -1 && charsRead < len) 
          {
                cbuf[charsRead++] = (char)c;
          }
          return c > -1 ? charsRead : -1;
    }
}
