Ext.namespace('Ext.fxMenuTree');
Ext.fxMenuTree.DDProxy = function(id, sGroup, dropUrl, overUrl, config) {
	this.endUrl = dropUrl;
	this.overUrl = overUrl;
	Ext.fxMenuTree.DDProxy.superclass.constructor.call(this, id, sGroup, config);
};
  
Ext.extend(Ext.fxMenuTree.DDProxy, Ext.dd.DDProxy, {
    startDrag: function(x, y) {
        var dragEl = Ext.get(this.getDragEl());
        var el = Ext.get(this.getEl());
 
        dragEl.applyStyles({border:'','z-index':2000});
        dragEl.update(el.dom.innerHTML);
        dragEl.addClass(el.dom.className + ' b_dd_proxy');
    },
    
	onDragOver: function(e, targetId) {
		if(targetId && (targetId.indexOf('dd') == 0 || targetId.indexOf('ds') == 0)) {
    		var target = Ext.get(targetId);
    		this.lastTarget = target;
    		if(this.overUrl && this.overUrl.length > 0) {
    			var url = this.overUrl + "/";
    			var dropId = this.id.substring(2,this.id.length);
    			var targetId = this.lastTarget.id.substring(2,this.lastTarget.id.length);
    			var sibling = (this.lastTarget.id.indexOf('ds') == 0);
    			var stat = new Ajax.Request(url, { 
    				method: 'get',
    				asynchronous : false,
            		parameters : { nidle:dropId, tnidle:targetId, sne:sibling },
            		onSuccess: function(transport) {
            			target.addClass('b_dd_over');
					}
          		});
    		} else {
    			target.addClass('b_dd_over');
    		}
    	}
	},
		
	onDragOut: function(e, targetId) {
    	if(targetId && (targetId.indexOf('dd') == 0 || targetId.indexOf('ds') == 0)) {
    		var target = Ext.get(targetId);
    		this.lastTarget = null;
    		target.removeClass('b_dd_over');
    	}
	},
		
	endDrag: function() {
    	var dragEl = Ext.get(this.getDragEl());
    	var el = Ext.get(this.getEl());
    	if(this.lastTarget) {
    		Ext.get(this.lastTarget).appendChild(el);
    		el.applyStyles({position:'', width:''});
    		var url =  this.endUrl;
    		if(url.lastIndexOf('/') == (url.length - 1)) {
    			url = url.substring(0,url.length-1);
    		}
    		var targetId = this.lastTarget.id.substring(2,url.length);
    		url += '%3Atnidle%3A' + targetId;
    		if(this.lastTarget.id.indexOf('ds') == 0) {
    			url += '%3Asne%3Ayes';
    		}
    		frames['oaa0'].location.href = url + '/';
    	}
	}
});