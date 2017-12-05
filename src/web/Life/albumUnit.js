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
    var albumGroup = ["albumGroup1", "albumGroup2", "albumGroup3", "albumGroup4"];
    var albumIds = [];
    var albumNames = [];
    var changeId = "";

    // var targetId = "59fcfcfce5526519d43e0940";
    // var targetName = "Leo Tseng";
    var targetId = getUrlParameter('targetId');
    var targetName = getUrlParameter('targetName');
    document.getElementById('titleTarget').innerHTML = "Target: "+targetName;
    loadAlbums();


    function loadAlbums(){
        albumIds = [];
        albumNames = [];
        jQuery.ajax({
            url:  "/rest/targets/" + targetId + "/albums?offset=" + offset + "&count="  + count,
            type: "GET",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            $.ajax({
                url:  "/rest/targets/"+targetId+"/isEditor",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data2){
                // var editorList = data2.content;
                // for(var j = 0 ; j < editorList.length ; j++){
                //     if(userId == editorList[j].id)
                //         isEditor = true;
                // }
                isEditor = data2.content;
            })


            total = data.metadata.total;
            $("#page").text("Page " + Math.floor(offset/count+1) + " of " + (Math.ceil(total/count)));
            var targetList = data.content;
            for(var i = 0 ; i < img.length ; i++){
                document.getElementById(albumGroup[i]).style.visibility = 'visible';
                if(i < targetList.length) {
                    albumIds.push(targetList[i].id);
                    albumNames.push(targetList[i].albumName);
                    document.getElementById(albumName[i]).innerHTML = targetList[i].albumName;
                    $.ajax({
                        url:  "/rest/albums/"+albumIds[i]+"/topicPicture",
                        type: "GET",
                        async: false,
                        beforeSend: function (xhr) {
                            xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                        }
                    }).done(function(data2){
                        var picture = data2.content;
                        if(picture != null && picture.url !="")
                            document.getElementById(img[i]).src=picture.url;
                        else
                            document.getElementById(img[i]).src="http://localhost:8080/Life/pic/nosignal.jpg";
                    })
                    if (isEditor == false) {
                        document.getElementById(editAlbum[i]).style.visibility = 'hidden';
                        document.getElementById(deleteAlbum[i]).style.visibility = 'hidden';
                    }
                }
                else{
                    document.getElementById(albumGroup[i]).style.visibility = 'hidden';
                }
            }

        })
    }

    $("#next").click(function(e){
        e.preventDefault();
        if (offset+count < total) {
            offset = offset+count;
            loadAlbums();
        }
    })

    $("#previous").click(function(e){
        e.preventDefault();
        // console.log("Cliked")
        if (offset-count >= 0) {
            offset = offset-count;
            loadAlbums();

        }
    })
    $("#view1").click(function(e){
        e.preventDefault();
        window.top.location.href = "Records.html?albumId="+albumIds[0]+"&albumName="+albumNames[0]+"&targetId="+targetId;
    })
    $("#view2").click(function(e){
        e.preventDefault();
        window.top.location.href = "Records.html?albumId="+albumIds[1]+"&albumName="+albumNames[1]+"&targetId="+targetId;
    })
    $("#view3").click(function(e){
        e.preventDefault();
        window.top.location.href = "Records.html?albumId="+albumIds[2]+"&albumName="+albumNames[2]+"&targetId="+targetId;
    })
    $("#view4").click(function(e){
        e.preventDefault();
        window.top.location.href = "Records.html?albumId="+albumIds[3]+"&albumName="+albumNames[3]+"&targetId="+targetId;
    })
    $("#deleteAlbum1").click(function(e){
        e.preventDefault();
        var r=confirm("Are you sure you want to delete this album");
        if(r == true) {
        jQuery.ajax({
            url:  "/rest/albums/" + albumIds[0],
            type: "DELETE",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            location.href = "albumUnit.html?targetId="+targetId+"&targetName="+targetName;
        }).fail(function(data){
            alert("Delete album fail!");
        })
        } else {
            alert("Cancel delete!");
        }
    })
    $("#deleteAlbum2").click(function(e){
        e.preventDefault();
        var r=confirm("Are you sure you want to delete this album");
        if(r == true) {
        jQuery.ajax({
            url:  "/rest/albums/" + albumIds[1],
            type: "DELETE",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            location.href = "albumUnit.html?targetId="+targetId+"&targetName="+targetName;
        }).fail(function(data){
            alert("Delete album fail!");
        })
        } else {
            alert("Cancel delete!");
        }
    })
    $("#deleteAlbum3").click(function(e){
        e.preventDefault();
        var r=confirm("Are you sure you want to delete this album");
        if(r == true) {
        jQuery.ajax({
            url:  "/rest/albums/" + albumIds[2],
            type: "DELETE",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            location.href = "albumUnit.html?targetId="+targetId+"&targetName="+targetName;
        }).fail(function(data){
            alert("Cancel delete!");
        })
    } else {
        alert("Delete album fail!");
    }
    })
    $("#deleteAlbum4").click(function(e){
        e.preventDefault();
        var r=confirm("Are you sure you want to delete this album");
        if(r == true) {
        jQuery.ajax({
            url:  "/rest/albums/" + albumIds[3],
            type: "DELETE",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            location.href = "albumUnit.html?targetId="+targetId+"&targetName="+targetName;
        }).fail(function(data){
            alert("Delete album fail!");
        })
        } else {
            alert("Cancel delete!");
        }
    })



    $("#editAlbum1").click(function(e){
        e.preventDefault();
        changeId = albumIds[0];
    })
    $("#editAlbum2").click(function(e){
        e.preventDefault();
        changeId = albumIds[1];
    })
    $("#editAlbum3").click(function(e){
        e.preventDefault();
        changeId = albumIds[2];
    })
    $("#editAlbum4").click(function(e){
        e.preventDefault();
        changeId = albumIds[3];
    })
    $("#btnChangeName").click(function(e){
        e.preventDefault();
        var changeName =
        jQuery.ajax({
            url:  "/rest/albums/" + changeId,
            type: "PATCH",
            async: false,
            data: JSON.stringify({albumName:$("#inputChange").val()}),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            location.href = "albumUnit.html?targetId="+targetId+"&targetName="+targetName;
        }).fail(function(data){
            alert("Edit album fail!");
        })
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


// Confirmation before delete
function confirmDelete() {
    var r=confirm("Are you sure you want to delete this album");

    // var albumIds = [];
    // for(var i = 0 ; i < img.length ; i++){
    //     document.getElementById(albumGroup[i]).style.visibility = 'visible';
    //     if(i < targetList.length) {
    //         albumIds.push(targetList[i].id);
    //     }

    if (r==true)
    {
        e.preventDefault();
        jQuery.ajax({
            url:  "/rest/albums/" + albumIds[1],
            type: "DELETE",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            location.href = "albumUnit.html?targetId="+targetId+"&targetName="+targetName;
        })

    }
    else
    {
        alert("Delete album fail!");
    }
}


