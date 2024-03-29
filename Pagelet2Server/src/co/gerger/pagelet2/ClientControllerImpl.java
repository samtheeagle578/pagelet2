package co.gerger.pagelet2;

import java.util.ArrayList;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class ClientControllerImpl implements ClientController {
    Document doc;
    ArrayList<String> callableMethodNames;
    HashMap<String,ArrayList<String>> parameterNamesByMethod;
    ArrayList<String> publicMethodNames;
    String authorizerMethodName;
    public ClientControllerImpl(String name) {
        super();
        this.doc=Util.getDocumentBuilder().newDocument();
        Element element=this.doc.createElement("controller");
        element.setAttribute("name",name);
        this.doc.appendChild(element);
        this.callableMethodNames = new ArrayList<>();
        this.publicMethodNames = new ArrayList<>();
        this.parameterNamesByMethod = new HashMap<>();
    }
    
    public void addMethod(String name, boolean synchronous, boolean publicMethod, ArrayList<String> parameterNames, String returnType, boolean authorizer){
        Element methodElement=this.doc.createElement("method");
        methodElement.setAttribute("name", name);
        methodElement.setAttribute("returntype", returnType);
        this.callableMethodNames.add(name);
        this.parameterNamesByMethod.put(name, parameterNames);
        if (synchronous){
            methodElement.setAttribute("synchronous", "true");
        }
        
        if (authorizer){
            this.authorizerMethodName = name;
        }
        
        if (publicMethod){
            this.publicMethodNames.add(name);
        }
        if (parameterNames.size()>0){
            for(String param : parameterNames){
                Element paramE = this.doc.createElement("parameter");
                paramE.setAttribute("name", param);
                methodElement.appendChild(paramE);
            }
        }
        this.doc.getDocumentElement().appendChild((Node)methodElement);
    }
    
    public String getText(){
        Document document = this.doc;
        DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        serializer.getDomConfig().setParameter("xml-declaration", false);
        String str = serializer.writeToString(document);
        log("getText="+str);
        return str;   
    }
    
    public String getMethodReturnType(String methodName){
        Element e = Util.getElementByName(this.doc, Constant.METHOD, methodName);
        if (e==null){
            return null;
        }
        return e.getAttribute(Constant.RETURN_TYPE);
    }
    
    public boolean isPublicMethod(String methodName){
        if (this.publicMethodNames.contains(methodName)){
            return true;
        }
        else{
            return false;
        }
    }
    
    private void log(String message){
        System.out.println("ClientControllerImpl: "+message);    
    }

    @Override
    public boolean isAuthorizer(String methodName) {
        if (methodName!=null && methodName.equals(this.authorizerMethodName)){
            return true;
        }
        else{
            return false;    
        }
    }
}
