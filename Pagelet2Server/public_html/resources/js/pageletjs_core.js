function log(message){
    console.log(message);
}

function logDOM(document,message){
    var s = new XMLSerializer();
    var str = s.serializeToString(document);
    console.log(message+' = '+str);
}

function fixPageletName(inPageletName){
    var pageletName = null;
    if (inPageletName.endsWith(".js") || inPageletName.endsWith(".html")){
            
    }
    else{
        pageletName = inPageletName + ".html"
    }
    return pageletName;
}

function getIncludeTags(pageletDoc){
    var nodeList = pageletDoc.getElementsByTagName("p:include");
    var length = nodeList.length;
    var includes = [];
    for(var i = 0; i < length; i++){
        if (nodeList[i].getAttribute("pagelet")!=""){
            includes[includes.length] = nodeList[i];
        }
    }
    return includes;
}

function getDocument(name,data){
    var domParser = new DOMParser();
    var doc=domParser.parseFromString(data,'text/html');
    var elementsWithIDs = doc.querySelectorAll('[id]');
    var length = elementsWithIDs.length;
    var e = null;
    for (var i = 1; i < length; i++){
        e = elementsWithIDs[i];
        if (e.getAttribute("id")==null || e.getAttribute("id")==""){
            
        }
        else{
            /*var cleanName = null;
            if (name.indexOf(".")>-1){
                cleanName = name.substring(0,name.indexOf("."));
            }
            else{
                cleanName = name;
            }*/
            e.setAttribute("id",name+"-"+e.getAttribute("id"));
        }
    }
    return doc.body.firstChild;
}

function getDocumentAsString(document){
    var s = new XMLSerializer();
    //document.body.firstChild.removeAttribute("xmlns");
    var str = s.serializeToString(document);
    //str=str.replace('xmlns="http://www.w3.org/1999/xhtml"','');
    //log("DOCUMENT="+str);
    return str;
}

function Pagelet(app,nameWithDot,data){
    this.name = nameWithDot.substring(0,nameWithDot.indexOf("."));
    //log("PAGELET NAME="+this.name+" nameWithDot="+nameWithDot);
    this.suffix = nameWithDot.substring(nameWithDot.indexOf("."));
    data = "<div style='width:100%; height:100%;' id='"+this.name+"'>"+data+"</div>";
    this.doc = getDocument(this.name,data);
    this.data = getDocumentAsString(this.doc);
    //if (doc!=null && doc.documentElement!=null){
        //doc.documentElement.setAttribute("id",name);    
    //}
    this.app = app;
    this.documentRootNode = null;
    this.replaceIncludeTag = function(includeTag){
        var parentNode = includeTag.parentNode;
        if (this.documentRootNode==null){
            var contentDoc=this.doc;
            this.documentRootNode=document.importNode(contentDoc, true);
        }
        parentNode.replaceChild(this.documentRootNode,includeTag);
    }
    this.showAsPage = function(){
        $('#defaultdialog').modal('hide');
        var root = app.getRootNode();
        root.innerHTML="";
        if (this.documentRootNode==null){
            this.documentRootNode = document.getElementById(this.name);
            if (this.documentRootNode==null){
                root.innerHTML=this.data;
                this.documentRootNode = document.getElementById(this.name);    
            }
            else{
                root.appendChild(this.documentRootNode);
            }
        }
        else{
            root.appendChild(this.documentRootNode);
        }
        log("pagelet.showAsPage:end");
    }
    this.showAsDialog = function(modal){
        log("dialog name="+this.name);
        var contentRoot = document.getElementById("defaultdialogbody");
        contentRoot.innerHTML="";
        if (this.documentRootNode==null){
            this.documentRootNode = document.getElementById(this.name);
            if (this.documentRootNode==null){
                contentRoot.innerHTML=this.data;
                this.documentRootNode = document.getElementById(this.name);    
            }
            else{
                contentRoot.appendChild(this.documentRootNode);
            }
        }
        else{
            contentRoot.appendChild(this.documentRootNode);
        }
        var dialogLabel = document.getElementById("defaultdialoglabel");
        dialogLabel.textContent = "";
        var textNode = document.createTextNode(this.documentRootNode.firstChild.getAttribute("title"));
        dialogLabel.appendChild(textNode);
        var closeButton = document.getElementById("defaultdialogclosebutton");
        if (modal){
            //log("dialog is modal");
            var $modal = $('#defaultdialog');
            //log(typeof $modal.data('bs.modal'));
            var keyboard = false; // Prevent to close by ESC
            var backdrop = 'static'; // Prevent to close on click outside the modal
    
            if(typeof $modal.data('bs.modal') === 'undefined') { // Modal did not open yet
                //log("dialog not open");
                $modal.modal({
                    keyboard: keyboard,
                    backdrop: backdrop
                });
            } else { // Modal has already been opened
                $modal.data('bs.modal')._config.keyboard = keyboard;
                $modal.data('bs.modal')._config.backdrop = backdrop;
                if(keyboard === false) { 
                    $modal.off('keydown.dismiss.bs.modal'); // Disable ESC
                } else { // 
                    $modal.data('bs.modal').escape(); // Resets ESC
                }
            }
            $('#defaultdialog').modal('show');   
            //log("dialog done");
            closeButton.style.display='none';
        }
        else{
            //log("dialog is not modal");
            $("#defaultdialog").modal({
                //backdrop: backdrop,
                keyboard: true
            });        
            //$('#defaultdialog').off('hide.bs.modal');
            closeButton.removeAttribute("style");
        }              
    }
}

function PageletJSApp(rootName){
    this.rootName = rootName;
    this.getRootNode = function(){
        return document.getElementById(this.rootName);
    }
    //var domParser = new DOMParser();
    this.name = "PageletJSApp";
    var that = this;
    this.pagelets = new Map();
    this.replaceAllIncludeTags = function(){
        var includeTags = document.getElementsByTagName("p:include");
        var includeTag = null;
        if (includeTags!=null && includeTags.length>0){
            log("p:include array length:"+includeTags.length);
            var arrayLength = includeTags.length;
            var includeTagsArray = Array.from(includeTags);
            for (var j = 0; j < arrayLength; j++){
                log("looping trough p:include tags on the document.i="+j);
                includeTag = includeTagsArray[j];
                //var parentNode = includeTag.parentNode;
                var includedPageletName = includeTag.getAttribute("pagelet");
                var pageletNameWithDot = fixPageletName(includedPageletName);
                this.replaceIncludeTag(includeTag,pageletNameWithDot);
                log("end of loop i="+j);
            }
        }
    }    
    this.loadPageletFromServer = function(pageletNameWithDot){
                                    $.ajax({
                                        url : pageletNameWithDot,
                                        type : 'POST',
                                        dataType:'text',
                                        async: false,
                                        success : function(data){
                                                    that.doLoadPageletFromServerResponse(pageletNameWithDot,data);
                                                  },
                                        error : function(request,error){
                                                    alert(JSON.stringify(error));
                                                    alert("Error: "+JSON.stringify(request));
                                                }
                                    });            
    }
    this.loadPagelet = function(pageletNameWithDot){
        var pagelet = this.pagelets.get(pageletNameWithDot);
        if (pagelet==null){
            this.loadPageletFromServer(pageletNameWithDot);
        }
    }
    this.doLoadPageletFromServerResponse = function(pageletNameWithDot,data){
        var pagelet = new Pagelet(this,pageletNameWithDot,data);
        log("Adding pagelet="+pageletNameWithDot+" to pagelets");
        this.pagelets.set(pageletNameWithDot,pagelet);
        var includeTags = getIncludeTags(pagelet.doc);
        var includeTag = null;
        for (var i = 0; i < includeTags.length; i++){
            log("Detected includeTag.");
            includeTag = includeTags[i];
            var pageletNameWithDot = fixPageletName(includeTag.getAttribute("pagelet"));
            this.loadPagelet(pageletNameWithDot);
        }
    }
    this.replaceIncludeTag = function(includeTag,pageletNameWithDot){
        log("replaceIncludeTag for pagelet="+pageletNameWithDot);
        var pagelet = this.pagelets.get(pageletNameWithDot);
        pagelet.replaceIncludeTag(includeTag);
    }
    this.wasDialogOpenBeforeAlert = false;
    this.isDialogOpen = function(){
        log("is dialog open");
        if ($("#defaultdialog").hasClass("show")){
            log("dialog is open");
            this.wasDialogOpenBeforeAlert = true;
        }
    }
    this.hideAlert1 = function(){
        if (this.wasDialogOpenBeforeAlert){
            this.wasDialogOpenBeforeAlert = false;
            $("#alert1").modal('hide');
            $('#defaultdialog').modal('show');
        }
        else{
            $("#alert1").modal('hide');
        }
    }
    this.showAlert1 = function(message){
        $("#alert1content").empty();
        if (message!=null && message.length>400){
            message = message.substring(0,400);
        }
        $("#alert1content").append(message);
        if (this.isDialogOpen()){
            this.wasDialogOpenBeforeAlert = true;
            $('#defaultdialog').modal('hide');
        }
        $("#alert1").modal({
            show:true,
            backdrop:'static'
        });
    }
    this.showAlert2 = function(message,button1,action1,button2,action2){
        $("#alert2content").empty();
        $("#alert2content").append(message);
        if (this.isDialogOpen()){
            this.wasDialogOpenBeforeAlert = true;
            $('#defaultdialog').modal('hide');
        }
        log("action1 class="+action1.constructor.name);
        $("#alert2button1").on("click",action1);
        $("#alert2button1").text(button1);
        $("#alert2button2").on("click",action2);
        $("#alert2button2").text(button2);
        $("#alert2").modal({
            show:true,
            backdrop:'static'
        });
    }    
    this.showPage = function(inPageName){
        this.hideDialog();
        var pageletNameWithDot = fixPageletName(inPageName);
        //var root = document.getElementById(that.rootName);
        //root.innerHTML="";
        //root.innerHTML=data;
        this.loadPagelet(pageletNameWithDot);
        log("Reading pagelet="+pageletNameWithDot+" from pagelets");
        var pagelet = this.pagelets.get(pageletNameWithDot);
        var jsTreeContextMenus = document.getElementsByClassName("jstree-contextmenu");
        log("context menu count="+jsTreeContextMenus.length);
        var array = Array.from(jsTreeContextMenus);
        log("context menu array count="+array.length);
        if (array){
            for (var i=0; i < array.lengh; i++){
                log("removing array item="+i);
                array[i].remove();
            }
        }
        
        pagelet.showAsPage();
        this.replaceAllIncludeTags();
    }
    this.showDialog = function (inPageName,modal){
        this.hideDialog();
        var pageletNameWithDot = fixPageletName(inPageName);
        this.loadPagelet(pageletNameWithDot);
        var pagelet = this.pagelets.get(pageletNameWithDot);
        pagelet.showAsDialog(modal);
        this.replaceAllIncludeTags();
    }
    this.hideDialog = function(){
        $('#defaultdialog').modal('hide');
        this.wasDialogOpenBeforeAlert = false;
    }
}

//var currentPageletServer = null;

function PageletServer(servletName, appName){
    this.servletName = servletName;
    this.appName = appName;
    //this.authenticationKey = null;
    //this.setAuthenticationKey = function(key){
        //this.authenticationKey = key;
    //}
    var that = this;
    //currentPageletServer = this;
    this.createServerMethods = function(data){
        log("server methods="+data);
        var responseDoc = null;
        if (window.DOMParser){
            parser = new DOMParser();
            responseDoc = parser.parseFromString(data, "text/xml");
        }
        else{ // Internet Explorer
            responseDoc = new ActiveXObject("Microsoft.XMLDOM");
            responseDoc.async = false;
            responseDoc.loadXML(response);
        }
        doControllers(responseDoc,this.servletName,this.appName);
    }
    this.loadServerMethods = function(){
        $.ajax({
            url : this.servletName,
            type : 'POST',
            dataType:'text',
            data : {
                'application' : this.appName,
                'action' : 'listservermethods'
            },                
            async: false,
            success : function(data){
                        that.createServerMethods(data);
                    },
            error : function(request,error){
                        alert(JSON.stringify(error));
                        alert("Error: "+JSON.stringify(request));
                    }
        });                        
    }
    this.loadServerMethods();
}


function doControllers(responseDoc,servletName,appName){
    var controllerElements=responseDoc.getElementsByTagName("controller");
    var evalText="";
    for (var i = 0; i < controllerElements.length; i++) {
        var controllerElement=controllerElements[i];
        
        var controllerName=controllerElement.getAttribute("name");
        //evalText="if (!isController('"+controllerName+"')){ ";
        evalText=evalText+controllerName+" = {};";
        //evalText=evalText+"allControllers.set('"+controllerName+"',"+controllerName+");"
        //evalText=evalText+"}";
        var methodElements=controllerElement.getElementsByTagName("method");
        
        for (var ii = 0; ii < methodElements.length; ii++) {
            var methodElement=methodElements[ii];
            var methodName=methodElement.getAttribute('name');
            var paramElements=methodElement.getElementsByTagName("parameter");
            var synchronous = methodElement.getAttribute('synchronous');
            var paramElement = null;
            if (synchronous=="true"){
                if (paramElements.length==0){
                    evalText = evalText+" "+controllerName+"['"+methodName+"']=function(){ var inputs='';";
                }
                else{
                    evalText = evalText+" "+controllerName+"['"+methodName+"']=function(";
                    for (var i4 = 0; i4 < paramElements.length; i4++) {
                        paramElement = paramElements[i4];
                        if (i4==0){
                            evalText = evalText + paramElement.getAttribute("name");        
                        }
                        else{
                            evalText = evalText + "," + paramElement.getAttribute("name");
                        }
                        
                    }
                    evalText = evalText + "){ var inputs = ''+";
                    for (var i6 = 0; i6 < paramElements.length; i6++) {
                        paramElement = paramElements[i6];
                        if (i6==0){
                            evalText = evalText + "(" + paramElement.getAttribute("name")+"||'{NULL_VALUE}')";;
                        }
                        else{
                            evalText = evalText + "+'~~~'+(" + paramElement.getAttribute("name")+"||'{NULL_VALUE}')";;
                        }
                    }
                    evalText = evalText + ";";                    
                }
                
                evalText = evalText+" return callServerMethod('"+servletName+"','"+appName+"','"+methodName+"',false,inputs,'"+controllerName+"')};";    
            }
            else{
                if (paramElements.length==0){
                    evalText = evalText+" "+controllerName+"['"+methodName+"']=function(){ var inputs='';";
                }
                else{
                    evalText = evalText+" "+controllerName+"['"+methodName+"']=function(";
                    for (var i4 = 0; i4 < paramElements.length; i4++) {
                        paramElement = paramElements[i4];
                        if (i4==0){
                            evalText = evalText + paramElement.getAttribute("name");        
                        }
                        else{
                            evalText = evalText + "," + paramElement.getAttribute("name");
                        }
                        
                    }
                    evalText = evalText + "){ var inputs = ''+";
                    for (var i6 = 0; i6 < paramElements.length; i6++) {
                        paramElement = paramElements[i6];
                        if (i6==0){
                            evalText = evalText + "(" + paramElement.getAttribute("name")+"||'{NULL_VALUE}')";;
                        }
                        else{
                            evalText = evalText + "+'~~~'+(" + paramElement.getAttribute("name")+"||'{NULL_VALUE}')";;
                        }
                    }
                    evalText = evalText + ";";                    
                }            
                evalText = evalText+" return callServerMethod('"+servletName+"','"+appName+"','"+methodName+"',true,inputs,'"+controllerName+"')};";
            }
            
        }
        
        //evalText = evalText+" "+controllerName+"['pageletServer']=currentPageletServer;";
        //currentPageletServer = null;
        log('evalText = '+evalText);
        eval(evalText); 
    }
}

function callServerMethod(servletName,appName,methodName,async,inputs,controllerName){
    var result = "";
    //var key = eval("("+controllerName+".pageletServer.authenticationKey)");
    //log("callServerMethod:authentication key="+key);
    $.ajax({
            url : servletName,
            type : 'POST',
            dataType:'text',
            data : {
                'application' : appName,
                'action' : 'execute',
                'method' : methodName,
                'controller' : controllerName,
                'inputs' : inputs
            },                
            async: async,
            success : function(data){
                        log("direct response data="+data);
                        result = data; 
                    },
            error : function(request,error){
                        alert(JSON.stringify(error));
                        alert("Error: "+JSON.stringify(request));
                    }
        });
    if (result.startsWith("ServerException:")){
        throw new Error(result.substr(16));
    }
    log();
    if (result.startsWith("[") || result.startsWith("{")){
        try{
            result = JSON.parse(result);    
        }
        catch(err){
            
        }
    }
    else if (result.startsWith("RELOAD")){
        window.location.reload(true); 
    }
    return result;        
}