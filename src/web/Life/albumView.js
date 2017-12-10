$(function() {
    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");

    //ation='Records.html?data=Ya success!';
    //frames['test1'].document.getElementById("titleTarget").innerHTML='Ya';
    jQuery.ajax({
        url: "/rest/users/targetsWatch",
        type: "GET",
        beforeSend: function (xhr) {
            xhr.setRequestHeader ("Authorization", token);
        }
    }).done(function (data) {
        var targetList = data.content;
        for(var i = 0 ; i < targetList.length ; i++){
            var albumDiv = "<iframe MARGINWIDTH=0 MARGINHEIGHT=0 HSPACE=0 VSPACE=0 FRAMEBORDER=0 SCROLLING=no src=\"albumUnit.html?targetId="+targetList[i].id+"&targetName="+targetList[i].targetName+"\" height=700 width=\"100%\" name=\"iFrame1\" id=\"iFrame1\"></iframe>";
            $('#albumList').append(albumDiv);
        }
        // $(window).load(resizeIFrameToFitContent(iFrame1));
    })
})

// $('iFrame1').load(function() {
//     this.style.height =
//         this.contentWindow.document.body.offsetHeight + 'px';
// });
//
// function resizeIFrameToFitContent( iFrame ) {
//
//     // iFrame.width  = iFrame.contentWindow.document.body.scrollWidth;
//     iFrame.height = iFrame.contentWindow.document.body.scrollHeight;
// }

// window.addEventListener('DOMContentLoaded', function(e) {
//
//     // var iFrame = document.getElementById( 'iFrame1' );
//     // resizeIFrameToFitContent( iFrame );
//
//     // or, to resize all iframes:
//     var iframes = document.querySelectorAll("iframe");
//     for( var i = 0; i < iframes.length; i++) {
//         resizeIFrameToFitContent( iframes[i] );
//     }
// } );