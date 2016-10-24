

$(function(){
    window.ssReader = Class({
      $singleton: true,

      init: function() {
        rangy.init();

        this.highlighter = rangy.createHighlighter();

        this.highlighter.addClassApplier(rangy.createClassApplier("highlight_orange", {
          ignoreWhiteSpace: true,
          tagNames: ["span", "a"]
        }));

        this.highlighter.addClassApplier(rangy.createClassApplier("highlight_yellow", {
          ignoreWhiteSpace: true,
          tagNames: ["span", "a"]
        }));

        this.highlighter.addClassApplier(rangy.createClassApplier("highlight_green", {
          ignoreWhiteSpace: true,
          tagNames: ["span", "a"]
        }));

        this.highlighter.addClassApplier(rangy.createClassApplier("highlight_blue", {
          ignoreWhiteSpace: true,
          tagNames: ["span", "a"]
        }));
      },

      setFontAndada: function(){
        this.setFont("andada");
      },

      setFontLato: function(){
        this.setFont("lato");
      },

      setFontPtSerif: function(){
        this.setFont("pt-serif");
      },

      setFontPtSans: function(){
        this.setFont("pt-sans");
      },

      base64encode: function(str){
        return btoa(str);
      },

      base64decode: function(str){
        return atob(str);
      },

      clearSelection: function(){
        if (window.getSelection) {
          if (window.getSelection().empty) {  // Chrome
            window.getSelection().empty();
          } else if (window.getSelection().removeAllRanges) {  // Firefox
            window.getSelection().removeAllRanges();
          }
        } else if (document.selection) {  // IE?
          document.selection.empty();
        }
      },

      // Public methods

      setFont: function(fontName){
        $("#ss-wrapper-font").removeClass().addClass("ss-wrapper-"+fontName);
      },

      setSize: function(size){
        $("#ss-wrapper-size").removeClass().addClass("ss-wrapper-"+size);
      },

      setTheme: function(theme){
        $("#ss-wrapper-theme").removeClass().addClass("ss-wrapper-"+theme);
      },

      setComment: function(comment, inputId){
        $("#"+inputId).html(ssReader.base64decode(comment));
      },

      highlightSelection: function(color){
        try {
          this.highlighter.highlightSelection("highlight_" + color);
          this.clearSelection();
          SSBridge.onReceiveHighlights(this.getHighlights());
        } catch(err){}
      },

      unHighlightSelection: function(color){
        try {
          this.highlighter.unhighlightSelection();
          SSBridge.onReceiveHighlights(this.getHighlights());
        } catch(err){}
      },

      getHighlights: function(){
        try {
          return this.highlighter.serialize();
        } catch(err){}
      },

      setHighlights: function(serializedHighlight){
        try {
          this.highlighter.deserialize(serializedHighlight);
        } catch(err){}
      },

      copy: function(){
        SSBridge.onCopy(window.getSelection().toString());
        this.clearSelection();
      },

      share: function(){
        SSBridge.onShare(window.getSelection().toString());
        this.clearSelection();
      },

      search: function(){
        SSBridge.onSearch(window.getSelection().toString());
        this.clearSelection();
      }
    });


  if(ssReader){ssReader.init();}
  $("code").each(function(i){
    $(this).append($("<div class='textarea'/>").attr("id", "input-"+i).click(function(){
      if (ssReader){
          SSBridge.onCommentsClick(
            ssReader.base64encode($(this).html()),
            $(this).attr("id")
          );
      }
    }));
  });
});
