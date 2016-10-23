var ssReader = Class({
  $singleton: true,

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

  setFont: function(fontName){
    $("#ss-wrapper").removeClass().addClass("ss-wrapper, ss-wrapper-"+fontName);
  },

  setComment: function(comment, inputId){
    $("#"+inputId).html(this.base64decode(comment));
  },

  base64encode: function(str){
    return btoa(str);
  },

  base64decode: function(str){
    return atob(str);
  }
});

$(function(){
  $("code").each(function(i){
    $(this).append($("<div class='textarea'/>").attr("id", "input-"+i).click(function(){
      SSBridge.saveComments(
        ssReader.base64encode($(this).html()),
      $(this).attr("id"));
    }));
  });
});
