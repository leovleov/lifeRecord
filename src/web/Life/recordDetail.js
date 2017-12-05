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
    var dislikeNum = ["dislikeNum1","dislikeNum2","dislikeNum3","dislikeNum4"];
    var dislikeBtn = ["dislikeBtn1","dislikeBtn2","dislikeBtn3","dislikeBtn4"];
    var likeBtn = ["likeBtn1","likeBtn2","likeBtn3","likeBtn4"];
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
                    $.ajax({
                        url:  "/rest/records/"+recordIds[i]+"/likeStatus",
                        type: "GET",
                        async: false,
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                        }
                    }).done(function(data2){
                        if(data2.content[0] == 0){
                            document.getElementById(dislikeBtn[i]).style.visibility = 'hidden';
                            document.getElementById(likeNum[i]).innerHTML = data2.content[1];
                            document.getElementById(likeBtn[i]).style.visibility = 'visible';
                        }
                        else{
                            document.getElementById(likeBtn[i]).style.visibility = 'hidden';
                            document.getElementById(dislikeNum[i]).innerHTML = data2.content[1];
                            document.getElementById(dislikeBtn[i]).style.visibility = 'visible';
                        }

                    })
                }
                else{

                    document.getElementById(recordGroup[i]).style.visibility = 'hidden';
                    document.getElementById(likeBtn[i]).style.visibility = 'hidden';
                    document.getElementById(dislikeBtn[i]).style.visibility = 'hidden';
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
        if (offset-count >= 0) {
            offset = offset-count;
            loadRecords();

        }
    })

    $("#likeBtn1").click(function(e){
        e.preventDefault();
        var num = 0;
        var likeBtn = "likeBtn1";
        var dislikeBtn = "dislikeBtn1";
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[num] + "/likes",
            type: "POST",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/records/"+recordIds[num]+"/likeNumber",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                //document.getElementById(likeNum[num]).innerHTML = data2.content;
                document.getElementById(dislikeNum[num]).innerHTML = data2.content;
                document.getElementById(likeBtn).style.visibility = 'hidden';
                document.getElementById(dislikeBtn).style.visibility = 'visible';
            })
        })
    })

    $("#dislikeBtn1").click(function(e){
        e.preventDefault();
        var num = 0;
        var likeBtn = "likeBtn1";
        var dislikeBtn = "dislikeBtn1";
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[num] + "/likes",
            type: "DELETE",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/records/"+recordIds[num]+"/likeNumber",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                document.getElementById(likeNum[num]).innerHTML = data2.content;
                //document.getElementById(dislikeNum[num]).innerHTML = data2.content;
                document.getElementById(likeBtn).style.visibility = 'visible';
                document.getElementById(dislikeBtn).style.visibility = 'hidden';
            })
        })
    })
    $("#likeBtn2").click(function(e){
        e.preventDefault();
        var num = 1;
        var likeBtn = "likeBtn2";
        var dislikeBtn = "dislikeBtn2";
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[num] + "/likes",
            type: "POST",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/records/"+recordIds[num]+"/likeNumber",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                //document.getElementById(likeNum[num]).innerHTML = data2.content;
                document.getElementById(dislikeNum[num]).innerHTML = data2.content;
                document.getElementById(likeBtn).style.visibility = 'hidden';
                document.getElementById(dislikeBtn).style.visibility = 'visible';
            })
        })
    })

    $("#dislikeBtn2").click(function(e){
        e.preventDefault();
        var num = 1;
        var likeBtn = "likeBtn2";
        var dislikeBtn = "dislikeBtn2";
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[num] + "/likes",
            type: "DELETE",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/records/"+recordIds[num]+"/likeNumber",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                document.getElementById(likeNum[num]).innerHTML = data2.content;
                //document.getElementById(dislikeNum[num]).innerHTML = data2.content;
                document.getElementById(likeBtn).style.visibility = 'visible';
                document.getElementById(dislikeBtn).style.visibility = 'hidden';
            })
        })
    })
    $("#likeBtn3").click(function(e){
        e.preventDefault();
        var num = 2;
        var likeBtn = "likeBtn3";
        var dislikeBtn = "dislikeBtn3";
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[num] + "/likes",
            type: "POST",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/records/"+recordIds[num]+"/likeNumber",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                //document.getElementById(likeNum[num]).innerHTML = data2.content;
                document.getElementById(dislikeNum[num]).innerHTML = data2.content;
                document.getElementById(likeBtn).style.visibility = 'hidden';
                document.getElementById(dislikeBtn).style.visibility = 'visible';
            })
        })
    })

    $("#dislikeBtn3").click(function(e){
        e.preventDefault();
        var num = 2;
        var likeBtn = "likeBtn3";
        var dislikeBtn = "dislikeBtn3";
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[num] + "/likes",
            type: "DELETE",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/records/"+recordIds[num]+"/likeNumber",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                document.getElementById(likeNum[num]).innerHTML = data2.content;
                //document.getElementById(dislikeNum[num]).innerHTML = data2.content;
                document.getElementById(likeBtn).style.visibility = 'visible';
                document.getElementById(dislikeBtn).style.visibility = 'hidden';
            })
        })
    })
    $("#likeBtn4").click(function(e){
        e.preventDefault();
        var num = 3;
        var likeBtn = "likeBtn4";
        var dislikeBtn = "dislikeBtn4";
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[num] + "/likes",
            type: "POST",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/records/"+recordIds[num]+"/likeNumber",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                //document.getElementById(likeNum[num]).innerHTML = data2.content;
                document.getElementById(dislikeNum[num]).innerHTML = data2.content;
                document.getElementById(likeBtn).style.visibility = 'hidden';
                document.getElementById(dislikeBtn).style.visibility = 'visible';
            })
        })
    })

    $("#dislikeBtn4").click(function(e){
        e.preventDefault();
        var num = 3;
        var likeBtn = "likeBtn4";
        var dislikeBtn = "dislikeBtn4";
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[num] + "/likes",
            type: "DELETE",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/records/"+recordIds[num]+"/likeNumber",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                document.getElementById(likeNum[num]).innerHTML = data2.content;
                //document.getElementById(dislikeNum[num]).innerHTML = data2.content;
                document.getElementById(likeBtn).style.visibility = 'visible';
                document.getElementById(dislikeBtn).style.visibility = 'hidden';
            })
        })
    })
    $("#view1").click(function(e){
        e.preventDefault();
        // document.getElementById("comments").innerHTML = '';
        fetchComments(0);
    })
    $("#view2").click(function(e){
        e.preventDefault();
        // document.getElementById("comments").innerHTML = '';
        fetchComments(1);
    })
    $("#view3").click(function(e){
        e.preventDefault();
        // document.getElementById("comments").innerHTML = '';
        fetchComments(2);
    })
    $("#view4").click(function(e){
        e.preventDefault();
        // document.getElementById("comments").innerHTML = '';
        fetchComments(3);
    })
    function fetchComments(btnNum){
        $('#comments').empty();
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[btnNum] + "/messages",
            type: "GET",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            var count = data.metadata.count;
            var comments = '';
            for(var i = 0 ; i < count ; i++){
                var message = data.content[i];
                comments = comments +
                    '<li>\n' +
                    '                                      <div class="commenterImage">\n' +
                    '                                          <p class="">'+message.userName+':</p>\n' +
                    '                                      </div>\n' +
                    '                                      <div class="commentText">\n' +
                    '                                          <p class="">'+message.messageInfo+'</p> <span class="date sub-text">on '+message.createDate+'</span>\n' +
                    '                                      </div>\n' +
                    '                                  </li>';
            }
            document.getElementById("comments").innerHTML = comments;
        })
    }
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