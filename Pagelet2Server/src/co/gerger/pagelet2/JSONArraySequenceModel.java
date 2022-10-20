package co.gerger.pagelet2;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateSequenceModel;

import freemarker.template.WrappingTemplateModel;

import org.json.JSONArray;
import org.json.JSONException;

public class JSONArraySequenceModel extends WrappingTemplateModel implements TemplateSequenceModel {
    
    private JSONArray jsonArray;

    public JSONArraySequenceModel(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    @Override
    public TemplateModel get(int index) throws TemplateModelException {
        TemplateModel model = null;
        log("get:index="+index);
        try {
            model = wrap(jsonArray.get(index));
        } catch (JSONException e) {
            log("get:error");
            //e.printStackTrace();
        }
        return model;
    }
    
    private static void log(String message){
        //System.out.println("SequenceModel:"+message);
    }

    @Override
    public int size() throws TemplateModelException {
        return jsonArray.length();
    }
}
