// $('#demo').likeDislike({
//   // update like / dislike counters
//   click: function (btnType, likes, dislikes, event) {
//       var likesElem = $(this).find('.likes');
//       var dislikedsElem = $(this).find('.dislikes');
//       likesElem.text(parseInt(likesElem.text()) + likes);
//       dislikedsElem.text(parseInt(dislikedsElem.text()) + dislikes);
//   }
// });
// $('#demo').likeDislike()
$(function() {
    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");
    var offset = 0;
    var count = 4;
    var total = -1;
    var isEditor = false;
    var img = ["img1","img2","img3","img4"];
    //var likeNum = ["likeNum1","likeNum2","likeNum3","likeNum4"];
    //var like = ["like1","like2","like3","like4"];
    var view = ["view1","view2","view3","view4"];
    var albumName = ["albumName1","albumName2","albumName3","albumName4"];
    var editAlbum = ["editAlbum1","editAlbum2", "editAlbum3", "editAlbum4"];
    var deleteAlbum = ["deleteAlbum1", "deleteAlbum2", "deleteAlbum3", "deleteAlbum4"];
    var albumIds = [];

    var albumId = "5a126ede35abf62df0428e98";
    var albumName = "Study";
    // var albumId = getUrlParameter('albumId');
    // var albumName = getUrlParameter('albumName');
    document.getElementById('titleRecord').innerHTML = "Record: "+albumName;
    loadRecords();


    function loadRecords(){
        albumIds.clear();
        jQuery.ajax({
            url:  "/rest/targets/" + targetId + "/albums?offset=" + offset + "&count="  + count,
            type: "GET",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/targets/"+targetId+"/editors",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                var editorList = data2.content;
                for(var j = 0 ; j < editorList.length ; j++){
                    if(userId == editorList[j].id)
                        isEditor = true;
                }
            }).fail(function(data2){
                editorList = null;
            })


            total = data.metadata.total;
            $("#page").text("Page " + Math.floor(offset/count+1) + " of " + (Math.ceil(total/count)));
            var targetList = data.content;
            for(var i = 0 ; i < img.length ; i++){
                document.getElementById(editAlbum[i]).style.visibility = 'visible';
                document.getElementById(deleteAlbum[i]).style.visibility = 'visible';
                document.getElementById(img[i]).style.visibility = 'visible';
                //document.getElementById(like[i]).style.visibility = 'visible';
                document.getElementById(albumName[i]).style.visibility = 'visible';
                document.getElementById(view[i]).style.visibility = 'visible';
                if(i < targetList.length) {
                    albumIds.add(targetList[i].id);
                    document.getElementById(albumName[i]).innerHTML = targetList[i].albumName;
                    if (isEditor == false) {
                        document.getElementById(editAlbum[i]).style.visibility = 'hidden';
                        document.getElementById(deleteAlbum[i]).style.visibility = 'hidden';
                    }
                }
                else{
                    document.getElementById(editAlbum[i]).style.visibility = 'hidden';
                    document.getElementById(deleteAlbum[i]).style.visibility = 'hidden';
                    document.getElementById(img[i]).style.visibility = 'hidden';
                    //document.getElementById(like[i]).style.visibility = 'hidden';
                    document.getElementById(albumName[i]).style.visibility = 'hidden';
                    document.getElementById(view[i]).style.visibility = 'hidden';
                }
            }

        })
    }

    $("#next").click(function(e){
        e.preventDefault();
        if (offset+count < total) {
            offset = offset+count;
            loadRecords();
        }
    })

    $("#previous").click(function(e){
        e.preventDefault();
        // console.log("Cliked")
        if (offset-count >= 0) {
            offset = offset-count;
            loadRecords();

        }
    })
    $("#view1").click(function(e){
        e.preventDefault();
        location.href = "RecordView.html?albumId="+albumIds[0];
    })
})

var getUrlParameter = function getUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
};