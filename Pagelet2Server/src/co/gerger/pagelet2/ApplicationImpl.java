package co.gerger.pagelet2;

import bsh.EvalError;

import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import bsh.Interpreter;

import java.lang.reflect.Parameter;

import java.util.ArrayList;

import java.util.Collection;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import org.reflections.Reflections;

public class ApplicationImpl {
    private static ConcurrentHashMap<String,ClientController> controllers = new ConcurrentHashMap<>();
    private static String authenticatorController;
    private static String authenticatorMethod;   
    
    static{
        
    }
    
    public ApplicationImpl(String packageName) {
        super();
        //this.generateXMLForServerMethods(packageName);
    }
    
    public static String getServerMethods(){
        String xml="<response>";
        Collection<ClientController> ccs= controllers.values();
        for (ClientController c:ccs){
            log("looping client controllers");
            xml=xml+System.lineSeparator()+c.getText();    
        }
        xml = xml +System.lineSeparator()+"</response>";
        return xml;        
    }
    
    private void generateXMLForServerMethods(String packageName){
        if (ApplicationImpl.controllers.size()>0){
            log("Controllers already initialized");
        }
        else{
            log("Initializing controllers");
            Reflections reflections = new Reflections(packageName);
            Set<Class<?>> controllers = 
                reflections.getTypesAnnotatedWith(Controller.class);
            log("SIZE ="+controllers.size());
            for (Class<?> clazz : controllers) {
                try {
                    Controller controller=clazz.getAnnotation(Controller.class); 
                    log("CLAZZ NAME ="+clazz.getSimpleName());
                    String controllerName="";
                    if (controller.name().equals("default")){
                        controllerName=clazz.getSimpleName().toLowerCase();    
                    }
                    else{
                        controllerName=controller.name();
                    }

                    ClientController cc=new ClientControllerImpl(controllerName,clazz.getName());
                    
                    Method[] methods=clazz.getDeclaredMethods();
                    int length=methods.length;
                    for (int i = 0; i < length; i++){
                        Method method=methods[i]; 
                        log("Method name = "+method.getName());
                        
                        boolean synchronous = false;
                        boolean publicMethod = false;
                        boolean authorizer = false;
                        boolean needsCredentials = false;
                        if (method.isAnnotationPresent(Callable.class)){
                            //if (method.isAnnotationPresent(Synchronous.class)){
                            synchronous = true;
                            //}
                            
                            if (method.isAnnotationPresent(PublicMethod.class)){
                                publicMethod = true;
                            }
                            
                            if (method.isAnnotationPresent(NeedsCredentials.class)){
                                needsCredentials = true;
                            }
                            
                            if (method.isAnnotationPresent(Authorizer.class)){
                                authorizer = true;
                            }
                            
                            Parameter[] parameters = method.getParameters();
                            Class<?> returnType = method.getReturnType();
                            //log("return type for method "+method.getName()+" is type="+returnType.getName());
                            ArrayList<String> parameterNames = new ArrayList<>();
                            String parameterName = null;
                            for (Parameter parameter : parameters) {
                                if(!parameter.isNamePresent()) {
                                    parameterName = "defaultParamName";
                                }

                                parameterName = parameter.getName();
                                parameterNames.add(parameterName);
                            }
                            
                            cc.addMethod(method.getName(),synchronous, publicMethod, parameterNames,returnType.getName(),authorizer, needsCredentials);
                            
                        }
                        ApplicationImpl.controllers.put(controllerName, cc);
                        log("ADDED CONTROLLER="+controllerName);
                        if (method.isAnnotationPresent(Authenticator.class)){
                            authenticatorController = controllerName;
                            authenticatorMethod = method.getName();
                        }
                        
                    }    
                } catch (Exception e) {
                    //log("Error with bean initialization");
                    e.printStackTrace();
                    }


            }            
        }
    }

    private static Interpreter getInterpreter(){
        Interpreter interpreter =  new Interpreter();
        try {
            interpreter.eval("import org.json.JSONArray; JSONArray testArray=new JSONArray();");
            Collection<ClientController> clientControllers = ApplicationImpl.controllers.values();
            for(ClientController cc: clientControllers){
                //interpreter.eval("import "+clazz.getName()+";"+" "+clazz.getSimpleName()+" "+controllerName+"=new "+clazz.getSimpleName()+"();");    
                interpreter.eval("import "+cc.getClassName()+"; int xx=1;");    
            }
            
        } catch (EvalError e) {
            e.printStackTrace();
        }
        return interpreter;
    }

    /*public String getName() {
        return name;
    }*/
    
    private static void log(String message){
        System.out.println("ApplicationImpl: "+message);    
    }

    private static void setParam(int nthParam, String value) throws PageletServerException {
        try {
            log("Setting param"+nthParam+" to "+value);
            ApplicationImpl.getInterpreter().set("param"+nthParam, value);
        } catch (EvalError e) {
            log("ScriptStacktrace="+e.getScriptStackTrace());
            //e.printStackTrace();
            throw new PageletServerException(e.getMessage());
        }
    }
    
    private static String addParams2(String textToRun,String inputs) throws PageletServerException {
        if (inputs!=null && inputs.equals("")==false){
            String[] params = inputs.split("~~~");
            if (params!=null && params.length>0){
                for (int i=0; i<params.length; i++){
                    //params[i] = params[i].replace(String.valueOf((char)10), "\\n");
                    //params[i] = params[i].replace("\"", "\\\"");
                    if (i==0){
                        if (params[i].equals(Constant.NULL_VALUE)){
                            setParam(i,"");
                            textToRun = textToRun + "param"+i;
                        }
                        else{
                            setParam(i,params[i]);
                            textToRun = textToRun + "param"+i;    
                        }
                        
                    }
                    else{
                        if (params[i].equals(Constant.NULL_VALUE)){
                            setParam(i,"");
                            textToRun = textToRun + ",param"+i;        
                        }
                        else{
                            setParam(i,params[i]);
                            textToRun = textToRun + ",param"+i;        
                        }
                        
                    }
                }
            }
        }
        textToRun = textToRun + ");";
        return textToRun;
    }
    //fix code injection problems later
    //right before attempting surgery
    private String addParams(String textToRun, String inputs){
        if (inputs!=null && inputs.equals("")==false){
            String[] params = inputs.split("~~~");
            if (params!=null && params.length>0){
                for (int i=0; i<params.length; i++){
                    params[i] = params[i].replace(String.valueOf((char)10), "\\n");
                    params[i] = params[i].replace("\"", "\\\"");
                    if (i==0){
                        if (params[i].equals(Constant.NULL_VALUE)){
                            textToRun = textToRun + "\""+"\"";    
                        }
                        else{
                            textToRun = textToRun + "\""+params[i]+"\"";    
                        }
                        
                    }
                    else{
                        if (params[i].equals(Constant.NULL_VALUE)){
                            textToRun = textToRun + ",\""+"\"";        
                        }
                        else{
                            textToRun = textToRun + ",\""+params[i]+"\"";        
                        }
                        
                    }
                }
            }    
        }
        textToRun = textToRun + ");";
        return textToRun;
    }
    
    private static String addExceptionHandling(String textToRun){
        return textToRun+" } catch (Exception e){e.printStackTrace(); if (e.getMessage()!=null && e.getMessage().equals(\"\")==false){ error = e.getMessage();} else{ error = \"An error without a message has occured.\";}}";
    }


    private static String getBeginning(){
        return " resultString = \"\"; error=\"NO_ERROR\"; try { ";
    }
    
    private static String getJSONBeginning(){
        return " JSONArray result = null; error=\"NO_ERROR\"; try { ";
    }
    
    private static String callInterpreter(String controllerName, String methodName, String inputs) throws PageletServerException {
        Interpreter in = ApplicationImpl.getInterpreter();
        ClientController controller = ApplicationImpl.controllers.get(controllerName);
        String returnType = controller.getMethodReturnType(methodName);
        String output = null;
        log("callInterpreter:returnType="+returnType);
        if (Constant.VOID.equals(returnType)){
            String textToRun=getBeginning()+controllerName+"."+methodName+"(";
            log("callInterpreter:1:inputs="+inputs);
            textToRun = addParams2(textToRun,inputs);
            textToRun = addExceptionHandling(textToRun);
            output = "void";
            try {
                log("textToRun="+textToRun);
                in.eval(textToRun);
                String error = in.get("error").toString();
                if (error.equals("NO_ERROR")==false){
                    throw new PageletServerException(error);    
                }               
            } catch (EvalError f) {
                //log("EvalError:textToRun="+textToRun);
                log("ErrorText="+f.getMessage());
                f.printStackTrace();
            }
        }
        else if (Constant.JSONArray.equals(returnType)){
            String textToRun=getJSONBeginning()+"result = "+controllerName+"."+methodName+"(";
            textToRun = addParams2(textToRun,inputs);
            textToRun = addExceptionHandling(textToRun);
            JSONArray jsonArray = null;
            try {
                log("Running:json code="+textToRun);
                in.eval(textToRun);
                Object resultObject = in.get("result");
                if (resultObject!=null){
                    jsonArray = (JSONArray)resultObject;
                    output = jsonArray.toString();    
                }
                
                //log("callInterpreter:RECEIVED result="+in.get("result").toString());
                String error = in.get("error").toString();
                if (error.equals("NO_ERROR")==false){
                    throw new PageletServerException(error);    
                }
                
            } catch (EvalError f) {
                //log("EvalError:textToRun="+textToRun);
                f.printStackTrace();             
            }            
        }
        else{
            log("callInterpreter:2:inputs="+inputs);
            String textToRun=getBeginning()+"resultString = "+controllerName+"."+methodName+"(";
            textToRun = addParams2(textToRun,inputs);
            textToRun = addExceptionHandling(textToRun);
            try {
                log("Running:code="+textToRun);
                in.eval(textToRun);
                Object resultObject = in.get("resultString");
                if (resultObject!=null){
                    output = resultObject.toString();    
                }
                
                //log("callInterpreter:RECEIVED result="+in.get("result").toString());
                String error = in.get("error").toString();
                if (error.equals("NO_ERROR")==false){
                    throw new PageletServerException(error);    
                }
                
            } catch (EvalError f) {
                //log("EvalError:textToRun="+textToRun);
                f.printStackTrace();             
            }
        }
        return output;
    }   

    private static void authenticate(String accessToken) throws PageletServerException {
        if (ApplicationImpl.authenticatorMethod!=null && ApplicationImpl.authenticatorMethod.equals("")==false){
            try {
                if (accessToken!=null && "".equals(accessToken)==false){
                    String output = ApplicationImpl.callInterpreter(ApplicationImpl.authenticatorController, ApplicationImpl.authenticatorMethod, accessToken);    
                }
                else{
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new PageletServerException("This session is not authorized to execute this function");
            }    
        }        
    }

    
    public static String execute(String appName, String controllerName,String methodName, String inputs, String accessToken, HttpServletResponse response) throws PageletServerException {
        ClientController controller = controllers.get(controllerName);
        if (controller==null){
            throw new PageletServerException("Cannot recognize "+controllerName+"."+methodName);
        }
        if (controller.isPublicMethod(methodName)==false){
            authenticate(accessToken);    
        }
        log("execute:controllerName="+controllerName+", methodName="+methodName+", inputs="+inputs);
    
        if (controller.needsCredentials(methodName)){
            if (inputs!=null && inputs.equals("")==false){
                inputs = inputs + "~~~" + accessToken;    
            }
            else{
                inputs = accessToken;
            }
        }
    
        String output = ApplicationImpl.callInterpreter(controllerName, methodName, inputs);
        log("execute:output="+output);
        if (controller.isAuthorizer(methodName)){
            Cookie accessTokenCookie = new Cookie("pagelet2accesstoken",output);
            accessTokenCookie.setMaxAge(3600*24*3650);
            response.addCookie(accessTokenCookie);
            return "";
        }
        return output;
        
        
    }
    
}
