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
    var likeNum = ["likeNum1","likeNum2","likeNum3","likeNum4"];
    var like = ["like1","like2","like3","like4"];
    var view = ["view1","view2","view3","view4"];
    var recordName = ["recordName1","recordName2","recordName3","recordName4"];
    var recordInfo = ["recordInfo1","recordInfo2","recordInfo3","recordInfo4"];
    var editBtn = ["editBtn1","editBtn2", "editBtn3", "editBtn4"];
    var deleteBtn = ["deleteBtn1", "deleteBtn2", "deleteBtn3", "deleteBtn4"];
    var recordGroup = ["recordGroup1", "recordGroup2", "recordGroup3", "recordGroup4"];
    var recordIds = [];

    // var albumId = "5a126ede35abf62df0428e98";
    // var albumName = "Study";
    // var targetId = "59fcfcfce5526519d43e0940";
    var albumId = getUrlParameter('albumId');
    var albumName = getUrlParameter('albumName');
    var targetId = getUrlParameter('targetId');
    document.getElementById('titleRecord').innerHTML = "Album: "+albumName;
    loadRecords();


    function loadRecords(){
        recordIds = [];
        jQuery.ajax({
            url:  "/rest/albums/" + albumId + "/records?offset=" + offset + "&count="  + count,
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
                var editorList = null;
            })


            total = data.metadata.total;
            $("#page").text("Page " + Math.floor(offset/count+1) + " of " + (Math.ceil(total/count)));
            var recordList = data.content;
            for(var i = 0 ; i < img.length ; i++){

                document.getElementById(recordGroup[i]).style.visibility = 'visible';
                if(i < recordList.length) {
                    recordIds.push(recordList[i].id);
                    document.getElementById(recordName[i]).innerHTML = recordList[i].recordName;
                    document.getElementById(recordInfo[i]).innerHTML = recordList[i].recordInfo;
                    $.ajax({
                        url:  "/rest/records/"+recordIds[i]+"/pictures",
                        type: "GET",
                        async: false,
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                        }
                    }).done(function(data2){
                        var picture = data2.content[0];
                        if(picture != null && picture.url !="")
                            document.getElementById(img[i]).src=picture.url;
                        else
                            document.getElementById(img[i]).src="http://localhost:8080/Life/pic/nosignal.jpg";
                    })
                    if (isEditor == false) {
                        document.getElementById(editBtn[i]).style.visibility = 'hidden';
                        document.getElementById(deleteBtn[i]).style.visibility = 'hidden';
                    }
                }
                else{

                    document.getElementById(recordGroup[i]).style.visibility = 'hidden';
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
    // $("#view1").click(function(e){
    //     e.preventDefault();
    //     location.href = "RecordView.html?albumId="+albumIds[0];
    // })
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