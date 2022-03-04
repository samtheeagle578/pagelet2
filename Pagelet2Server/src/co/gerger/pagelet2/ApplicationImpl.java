package co.gerger.pagelet2;

import bsh.EvalError;

import java.lang.reflect.Method;

import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import bsh.Interpreter;

import freemarker.core.ParseException;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Parameter;

import java.util.ArrayList;

import java.util.Collection;

import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import org.reflections.Reflections;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

import java.util.logging.Level;
import java.util.logging.Logger;


public class ApplicationImpl {
    private static ConcurrentHashMap<String,ClientController> controllers = new ConcurrentHashMap<>();
    private static boolean clientControllersProcessed = false;
    private static String authenticatorController;
    private static String authenticatorMethod;
    private static String valueListProviderController;
    private static String valueListProviderMethod;
    private static Configuration cfg;
    
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
    
    //This doesn't belong here but for now it will do.
    public static void initializeFreeMarker(String path){
        if (ApplicationImpl.clientControllersProcessed==false){
            cfg = new Configuration(Configuration.VERSION_2_3_31);
            try {
                cfg.setDirectoryForTemplateLoading(new File(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
            cfg.setLogTemplateExceptions(false);
            cfg.setWrapUncheckedExceptions(true);
            cfg.setFallbackOnNullLoopVariable(false);
            cfg.setObjectWrapper(new JSONArrayObjectWrapper());
        }
    }
    
    public static void processServerMethods(String packageName) throws PageletServerException {
        if (ApplicationImpl.clientControllersProcessed){
            log("Controllers already initialized");
        }
        else{
            ApplicationImpl.clientControllersProcessed = true;
            log("Initializing controllers");
            //try{
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

                    ClientController cc=new ClientControllerImpl(controllerName,clazz.getName(), clazz.getSimpleName());
                    
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
                            
                            if (method.isAnnotationPresent(Async.class)){
                                synchronous = false;
                            }else{
                                synchronous = true;
                            }
                                
                            //}
                            
                            
                            
                            if (method.isAnnotationPresent(PublicMethod.class)){
                                publicMethod = true;
                            }
                            
                            if (method.isAnnotationPresent(Credentials.class)){
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
                            for (int ii=0; ii < parameters.length; ii++){
                                Parameter parameter = parameters[ii];
                                if (ii==parameters.length-1 && needsCredentials){
                                    
                                }else{
                                    if(!parameter.isNamePresent()) {
                                        parameterName = "defaultParamName"+ii;
                                    }
                                    else{
                                        parameterName = parameter.getName();    
                                    }
                                    
                                    parameterNames.add(parameterName);
                                }
                            }
                            
                            /*
                            for (Parameter parameter : parameters) {
                                if(!parameter.isNamePresent()) {
                                    parameterName = "defaultParamName";
                                }
                                parameterName = parameter.getName();
                                parameterNames.add(parameterName);
                            }*/
                            
                            cc.addMethod(method.getName(),synchronous, publicMethod, parameterNames,returnType.getName(),authorizer, needsCredentials);
                            
                        }
                        ApplicationImpl.controllers.putIfAbsent(controllerName, cc);
                        log("ADDED CONTROLLER="+controllerName);
                        if (method.isAnnotationPresent(Authenticator.class)){
                            authenticatorController = controllerName;
                            authenticatorMethod = method.getName();
                        }
                        
                        if (method.isAnnotationPresent(ValueListProvider.class)){
                            log("ADDING VALUE LIST PROVIDER="+controllerName+"."+method.getName());
                            valueListProviderController = controllerName;
                            valueListProviderMethod = method.getName();
                        }else{
                            log("NOT A VALUE LIST PROVIDER="+controllerName+"."+method.getName());
                        }
                        
                    }    
                } catch (Exception e) {
                    //log("Error with bean initialization");
                    e.printStackTrace();
                    }


            }
        //}catch(Exception e){
            //log("processServerMethods:ERROR:"+e.getMessage());
            //e.printStackTrace();
            //throw new PageletServerException(e);
        //}     
        }
    }

    private static Interpreter getInterpreter(){
        log("NEW INTERPRETER METHOD");
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
        //System.out.println("ApplicationImpl: "+message);
        Logger.getLogger("ApplicationImpl").log(Level.WARNING, message);  
    }

    private static void setParam(int nthParam, String value, Interpreter interpreter) throws PageletServerException {
        try {
            log("Setting param"+nthParam+" to "+value);
            interpreter.set("param"+nthParam, value);
        } catch (EvalError e) {
            log("ScriptStacktrace="+e.getScriptStackTrace());
            //e.printStackTrace();
            throw new PageletServerException(e.getMessage());
        }
    }
    
    private static String addParams2(String textToRun,String inputs, Interpreter interpreter) throws PageletServerException {
        if (inputs!=null && inputs.equals("")==false){
            String[] params = inputs.split("~~~");
            if (params!=null && params.length>0){
                for (int i=0; i<params.length; i++){
                    //params[i] = params[i].replace(String.valueOf((char)10), "\\n");
                    //params[i] = params[i].replace("\"", "\\\"");
                    if (i==0){
                        if (params[i].equals(Constant.NULL_VALUE)){
                            setParam(i,"",interpreter);
                            textToRun = textToRun + "param"+i;
                        }
                        else{
                            setParam(i,params[i],interpreter);
                            textToRun = textToRun + "param"+i;    
                        }
                        
                    }
                    else{
                        if (params[i].equals(Constant.NULL_VALUE)){
                            setParam(i,"",interpreter);
                            textToRun = textToRun + ",param"+i;        
                        }
                        else{
                            setParam(i,params[i],interpreter);
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
        log("callInterpreter:methodName="+methodName);
        log("callInterpreter:returnType="+returnType);
        log("callInterpreter:simpleClassName="+controller.getSimpleClassName());
        log("callInterpreter:1:inputs="+inputs);
        if (Constant.VOID.equals(returnType)){
            String textToRun=getBeginning()+controller.getSimpleClassName()+"."+methodName+"(";
            textToRun = addParams2(textToRun,inputs,in);
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
            String textToRun=getJSONBeginning()+"result = "+controller.getSimpleClassName()+"."+methodName+"(";
            textToRun = addParams2(textToRun,inputs,in);
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
            String textToRun=getBeginning()+"resultString = "+controller.getSimpleClassName()+"."+methodName+"(";
            textToRun = addParams2(textToRun,inputs,in);
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

    
    public static String execute(String controllerName,String methodName, String inputs, String accessToken, 
                                 HttpServletResponse response, HttpServletRequest request) throws PageletServerException {
        ClientController controller = controllers.get(controllerName);
        if (controller==null){
            throw new PageletServerException("Cannot recognize "+controllerName+"."+methodName);
        }
        if (controller.isPublicMethod(methodName)==false){
            log("execute:methodName="+methodName+",accessToken="+accessToken);
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
            log("EXECUTE:DOMAIN="+request.getRequestURL().toString().replace(request.getRequestURI(),""));
            //String domain = request.getRequestURL().toString().replace(request.getRequestURI(),"");
            //int colon = domain.indexOf(":",7);
            //log("EXECUTE:COLON POSITION="+colon);
            //if (colon>0){
                //domain = domain.substring(0, colon);    
            //}
            //log("EXECUTE:DOMAIN2="+domain);
            //accessTokenCookie.setDomain(domain);
            //log("EXECUTE:COOKIE INFO="+accessTokenCookie.getDomain()+","+ accessTokenCookie.getPath());
            accessTokenCookie.setMaxAge(3600*24*3650);
            response.addCookie(accessTokenCookie);
            return "";
        }
        return output;
        
        
    }
    
    public static String getValueList(String valueListName,String accessToken) throws PageletServerException {
        log("GET VALUE LIST:valueListName="+valueListName);
        String inputs = null;
        if (ApplicationImpl.valueListProviderController==null){
            return "{}";
        }
        ClientController controller = controllers.get(ApplicationImpl.valueListProviderController);
        if (controller==null){
            throw new PageletServerException("Cannot find a Value List Provider Controller ");
        }
        if (controller.isPublicMethod(ApplicationImpl.valueListProviderMethod)==false){
            authenticate(accessToken);
        }
    
        if (controller.needsCredentials(ApplicationImpl.valueListProviderMethod)){
            if (valueListName!=null && valueListName.equals("")==false){
                inputs = valueListName + "~~~" + accessToken;    
            }
            else{
                inputs = Constant.NULL_VALUE + "~~~" + accessToken;    
            }
        }
        log("GET VALUE LIST:inputs="+inputs);
        String output = ApplicationImpl.callInterpreter(ApplicationImpl.valueListProviderController, ApplicationImpl.valueListProviderMethod, inputs);
        log("execute:output="+output);
        return output;
    }
    
    public static Template getTemplate(String name) throws TemplateNotFoundException, MalformedTemplateNameException,
                                                           ParseException, IOException {
        return ApplicationImpl.cfg.getTemplate(name);
    }
}
