// load feed api
google.load("feeds", "1");

/**
 * 
 */
function initFeed(elementid) {
  var feed,maxItems,feedURL,entry,container,itemElement,itemContentString,trimContent,formattedDate,authormatch,author = "";

  feedURL = "https://groups.google.com/group/openolat/feed/rss_v2_0_msgs.xml";
  // load max Items
  maxItems = 7;
  
  // contentString gets substringed...
  trimContent=200;
  
  // the feed object
  feed = new google.feeds.Feed(feedURL);
  feed.setNumEntries(maxItems);
  //load the feed, callback on load
  feed.load(function(result) {
    if (!result.error) {
      container = $('#'+elementid);
      container.html("");
      //loop over rss-feed entries
      for ( var i = 0; i < result.feed.entries.length; i++) {
        entry = result.feed.entries[i];
        itemContentString = (entry.content.lenght <= trimContent)?entry.content:entry.content.substring(0,trimContent)+"...";
        itemContentString = itemContentString.replace(/<br>/g,"");
        formattedDate = entry.publishedDate.substring(0,entry.publishedDate.length-6);
        formattedDate = new Date(Date.parse(formattedDate));
        formattedDate = formattedDate.toGMTString();
        authormatch = entry.author.match(/\(.*\)/);
        author = entry.author;
        if( authormatch != null ){
        	if(authormatch.length > 0){
        		author = authormatch[0].replace(/<br>/g,"").replace(/\(/,"").replace(/\)/,"")
        	}
        }
        	
        
        itemElement = $('<div class="fxml-entry"><span class="fxml-title"><a href="'+entry.link+'" target="_blank">'+entry.title+'</a></span>\
            <span class="fxml-content">'+itemContentString+'</span>\
            <span class="fxml-footer">'+author+'\
            ·    <span class="fxml-date">'+formattedDate+'</span></div>');
        container.append(itemElement);
      }
    }
  });
}

$(document).ready(function() {
  initFeed("fxmailinglist-inner");
});