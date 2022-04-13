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
    HashMap<String,ArrayList<String>> callableMethodRoles;
    HashMap<String,ArrayList<MethodParameterImpl>> parametersByMethod;
    ArrayList<String> publicMethodNames;
    ArrayList<String> methodsThatNeedCredentials;
    String authorizerMethodName;
    String className;
    String simpleClassName;
    
    public ClientControllerImpl(String name, String className, String simpleClassName) {
        super();
        this.className = className;
        this.simpleClassName = simpleClassName;
        this.doc=Util.getDocumentBuilder().newDocument();
        Element element=this.doc.createElement("controller");
        element.setAttribute("name",name);
        this.doc.appendChild(element);
        this.callableMethodNames = new ArrayList<>();
        this.callableMethodRoles = new HashMap<>();
        this.publicMethodNames = new ArrayList<>();
        this.parametersByMethod = new HashMap<>();
        this.methodsThatNeedCredentials = new ArrayList<String>();
    }
    
    public void addMethod(String name, boolean synchronous, boolean publicMethod, ArrayList<MethodParameterImpl> parameters, String returnType, boolean authorizer, boolean needsCredentials, String roles){
        Element methodElement=this.doc.createElement("method");
        methodElement.setAttribute("name", name);
        methodElement.setAttribute("returntype", returnType);
        this.callableMethodNames.add(name);
        if (roles!=null && roles.equals("")==false){
            String[] splitRoles =roles.split(",");
            ArrayList<String> roleList = new ArrayList<>();
            for (int i=0; i < splitRoles.length; i++){
                roleList.add(splitRoles[i]);
            }
            this.callableMethodRoles.put(name, roleList);
        }
        if (synchronous){
            methodElement.setAttribute("synchronous", "true");
        }else{
            methodElement.setAttribute("synchronous", "false");
        }
        
        if (authorizer){
            this.authorizerMethodName = name;
        }
        
        if (publicMethod){
            this.publicMethodNames.add(name);
        }
        
        if (needsCredentials){
            this.methodsThatNeedCredentials.add(name);
        }
        
        if (parameters.size()>0){
            for(MethodParameterImpl param : parameters){
                Element paramE = this.doc.createElement("parameter");
                paramE.setAttribute("name", param.getName());
                paramE.setAttribute("type", param.getType());
                methodElement.appendChild(paramE);
            }
        }
    
        if (needsCredentials){
            parameters.add(new MethodParameterImpl("accessToken","String"));
        }
        this.parametersByMethod.put(name, parameters);            
        
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
        log("getMethodReturnType:methodName="+methodName);
        String string = Util.documentToString(doc);
        log("getMethodReturnType:document="+string);
        Element e = Util.getElementByName(this.doc, Constant.METHOD, methodName);
        log("getMethodReturnType:element="+Util.getElementAsString(e));
        if (e==null){
            return null;
        }
        return e.getAttribute(Constant.RETURN_TYPE);
    }
    
    public ArrayList<MethodParameterImpl> getMethodParameters(String methodName){
        ArrayList<MethodParameterImpl> parameters = this.parametersByMethod.get(methodName);
        return this.parametersByMethod.get(methodName);
    }
    
    public boolean isPublicMethod(String methodName){
        if (this.publicMethodNames.contains(methodName)){
            return true;
        }
        else{
            return false;
        }
    }
    
    public boolean needsCredentials(String methodName){
        if (this.methodsThatNeedCredentials.contains(methodName)){
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

    @Override
    public String getClassName() {
        return this.className;
    }

    @Override
    public String getSimpleClassName() {
        return this.simpleClassName;
    }

    @Override
    public boolean canExecute(String methodName, String roleName) {
        ArrayList<String> roles = this.callableMethodRoles.get(methodName);
        if (roles.size()==1){
            String role = roles.get(0);
            if (role.equals("default")){
                return true;
            }
            if (role.equals(roleName)){
                return true;
            }else{
                return false;
            }
        }else{
            for(String role: roles){
                if (role.equals("roleName")){
                    return true;
                }
            }
        }
        return false;
    }
}
