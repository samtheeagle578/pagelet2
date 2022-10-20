package co.gerger.pagelet2;

import java.io.File;

import java.io.IOException;
import java.io.StringWriter;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class Util {
    
    public static final String CHARSET = "utf-8";
    
    public Util() {
        super();
    }
    
    public static DocumentBuilder getDocumentBuilder(){
        final DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null; 
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            //e.printStackTrace();
        }
        return builder;
    }
    
    public static Element getElementByName(Document doc, String tagName, String name){
        log("getElementByName:=tagName="+tagName+",name="+name);
        NodeList elements = doc.getElementsByTagName(tagName);
        int length = elements.getLength();
        Element element = null;
        for (int i=0; i < length; i++){
            element = (Element)elements.item(i);
            log("getElementByName:elementinprocess="+Util.getElementAsString(element));
            log("getElementByName:=name="+name+",elementname="+element.getAttribute(Constant.NAME));
            if (name.equals(element.getAttribute(Constant.NAME))){
                return element;
            }
        }
        return null;
    }
    
    public static JSONArray getAsJSON(ArrayList<File> files){
        JSONArray array = new JSONArray();
        JSONObject object = null;
        for(File file:files){
            object = new JSONObject();
            object.put("name", file.getName());
            array.put(object);
        }
        return array;
    }
    
    public static Element getElementByAttributeValue(String attributeName,String value, NodeList nodes, boolean deep){        
        if (deep==false){
            int length=nodes.getLength();
            Element element=null;
            for (int i = 0; i < length; i++){
                Node node = nodes.item(i);
                if (node.getNodeType()==Node.ELEMENT_NODE){
                    Element el=(Element)node;
                    
                    if (el.getAttribute(attributeName).equals(value)){
                        return el;
                    }                
                }
            }    
        }
        else{
            int length=nodes.getLength();
            Element element=null;
            for (int i = 0; i < length; i++){
                Node node = nodes.item(i);
                if (node.getNodeType()==Node.ELEMENT_NODE){
                    Element el=(Element)node;
                    
                    if (el.getAttribute(attributeName).equals(value)){
                        return el;
                    }                
                }
            }
            
            for (int i = 0; i < length; i++){
                NodeList children = nodes.item(i).getChildNodes();
                Element el = getElementByAttributeValue(attributeName,value,children,deep);
                if (el==null){
                    
                }
                else{
                    if (el.getAttribute(attributeName).equals(value)){
                        return el;
                    }    
                }
                
            }
        }
        
        return null; 
    }
    
    public static String documentToString(Document doc) {
        DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
        LSSerializer lsSerializer = domImplementation.createLSSerializer();
        lsSerializer.getDomConfig().setParameter("xml-declaration", false);
        return lsSerializer.writeToString(doc);  
    }
    
    public static String prettyPrintDocument(Document doc) throws PageletServerException {
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            //initialize StreamResult with File object to save to file
            StreamResult result = new StreamResult(new StringWriter());
            /*if (doc==null){
                log("DOC IS NULL");
            }
            else{
                log("DOC="+documentToString(doc));
            }*/
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();
            return xmlString;
        } catch (TransformerConfigurationException e) {
            throw new PageletServerException(e.getMessage());
        } catch (TransformerException e) {
            //e.printStackTrace();
            throw new PageletServerException(e.getMessage());
        }

    }
    
    public static ArrayList<String> getJSONPropertyAsArrayList(JSONObject jsonObject, String propertyName){
        ArrayList<String> arrayList = new ArrayList<>();
        try{
            String updateColumns = jsonObject.getString(propertyName);    
            if (updateColumns!=null && updateColumns.equals("")==false){
                String[] updateColumnsArray = updateColumns.split(",");
                for (int i=0; i< updateColumnsArray.length; i++){
                    arrayList.add(updateColumnsArray[i]);
                }
            }
        }
        catch(JSONException e){
            
        }
        return arrayList;
    }
    
    public static String getFileContent(String path) throws IOException {
        String content = null;
        byte[] encoded = null;
        encoded = Files.readAllBytes(Paths.get(path));
        content = new String(encoded, CHARSET);        
        return content;
    }
    
    public static JSONArray getSortedList(JSONArray array) throws JSONException {
                List<JSONObject> list = new ArrayList<JSONObject>();
                for (int i = 0; i < array.length(); i++) {
                        list.add(array.getJSONObject(i));
                }
                Collections.sort(list, new Comparator<JSONObject>()
                                        {
                                            public int compare(JSONObject column1, JSONObject column2){
                                                return column1.getString("name").compareTo(column2.getString("name"));
                                            }        
                                        }
                                 );

                JSONArray resultArray = new JSONArray(list);

                return resultArray;
    }
    
    private static void log(String message){
        //System.out.println("Util: "+message);     
    }
    
    public static Element getChildElementByAttributeValue(String name, String value, Element parentElement){        
        NodeList nodeList = parentElement.getChildNodes();
        int length=nodeList.getLength();
        Element element=null;
        for (int i = 0; i < length; i++){
            Node node = nodeList.item(i);
            if (node.getNodeType()==Node.ELEMENT_NODE){
                Element el=(Element)nodeList.item(i);
                //log("TAG="+el.getTagName()+",name="+el.getAttribute("name"));
                if (el.getAttribute(name).equals(value)){
                    element=el;
                }                
            }
        }
        
        return element;          
    }
    
    public static String getElementAsString(Element e){
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer=null;
        try {
            transformer = transFactory.newTransformer();
        } catch (TransformerConfigurationException f) {
            //f.printStackTrace();
        }
        StringWriter buffer = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        try {
            transformer.transform(new DOMSource(e), new StreamResult(buffer));
        } catch (TransformerException f) {
            //f.printStackTrace();
        }
        String str = buffer.toString();
        return str;
        //System.out.println(text+str);    
    }
    
    
}