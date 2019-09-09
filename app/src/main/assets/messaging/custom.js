    var elements = document.getElementsByClassName('infobox');
     for (var i=0; i<elements.length; i++){
         elements[i].style.backgroundColor = '#FFCD18';
    }

browser.runtime.sendNativeMessage("browser", "Color set");