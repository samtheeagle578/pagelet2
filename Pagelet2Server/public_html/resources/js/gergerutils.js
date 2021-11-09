function Utility(){
    this.setSelectOptions = function(selectID,optionsArray,startHTML,valueAttribute,displayAttribute){
        $('#'+selectID).empty();
        //var optionsArray = JSON.parse(optionsJSON);
        //log("setSelectedOptions:1");
        $.each( optionsArray, function( key, val ) {
                    //log("setSelectedOptions:key="+key+",val="+val);
                    startHTML = startHTML + "<option value='" + val[valueAttribute] + "'>" + val[displayAttribute] + "</option>";
                });
        //log("setSelectedOptions:startHTML="+startHTML);
        $(startHTML).appendTo( "#"+selectID);
    }
    this.setSelectOptions2 = function(elementId, optionValues){
        $("#"+elementId+" option").remove();
        if (optionValues!=null && optionValues!==""){
            var options = optionValues.split(',');
            for (const option of options){
                $("#"+elementId).append($("<option></option>").attr("value", option).text(option)); 
            }
            
        }
        
    }
    this.getOptionValuesAsText = function(options){
        var values = $.map(options ,function(option) {
                                        return option.value;
                                    }
                           );
        var text = "";
        if (values){
            for (var i=0; i<values.length; i++){
                if (text===""){
                    text = values[i];
                }
                else{
                    text = text + "," + values[i];
                }
            }
        }
        return text;
    }
    this.getOptionsArray = function(selectId){
        var options = $("#"+selectId+" option");
        var values = $.map(options ,function(option) {
                                        return option.value;
                                    }
                           );
        return values;
    }
    this.getOptionsJSONArray = function(selectId){
        var options = $("#"+selectId+" option");
        var values = $.map(options ,function(option) {
                                        return {"column":option.value};
                                    }
                           );
        return values;
    }
    this.getArray = function(commaDelimetedList){
        if (commaDelimetedList!=null && commaDelimetedList!==""){
            return commaDelimetedList.split(',');
        }
        var dummy = [];
        return dummy;
    }
}

var util = new Utility();