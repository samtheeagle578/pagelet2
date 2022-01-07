package co.gerger.pagelet2;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONArrayObjectWrapper extends DefaultObjectWrapper {
    
    public JSONArrayObjectWrapper() {
        super();
    }
    
    @Override
    public TemplateModel handleUnknownType (Object obj) throws TemplateModelException {
        log("handleUnknownType:class="+obj.getClass().getName());
        if (obj instanceof JSONArray) {
            log("handleUnknownType:it's a JSONArray");
            return new JSONArraySequenceModel((JSONArray) obj);
        }
        log("handleUnknownType:returning");
        return super.handleUnknownType(obj);
    }
    
    private static void log(String message){
        System.out.println("ObjectWrapper:"+message);
    }
}
