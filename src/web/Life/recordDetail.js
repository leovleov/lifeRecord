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
    // var dislikeNum = ["dislikeNum1","dislikeNum2","dislikeNum3","dislikeNum4"];
    // var dislikeBtn = ["dislikeBtn1","dislikeBtn2","dislikeBtn3","dislikeBtn4"];
    var likeBtn = ["likeBtn1","likeBtn2","likeBtn3","likeBtn4"];
    var like = ["like1","like2","like3","like4"];
    var view = ["view1","view2","view3","view4"];
    var recordName = ["recordName1","recordName2","recordName3","recordName4"];
    var recordInfo = ["recordInfo1","recordInfo2","recordInfo3","recordInfo4"];
    var editBtn = ["editBtn1","editBtn2", "editBtn3", "editBtn4"];
    var deleteBtn = ["deleteBtn1", "deleteBtn2", "deleteBtn3", "deleteBtn4"];
    var recordGroup = ["recordGroup1", "recordGroup2", "recordGroup3", "recordGroup4"];
    var recordIds = [];
    var picIds = [];
    var picUrls = [];

    // var albumId = "5a126ede35abf62df0428e98";
    // var albumName = "Study";
    // var targetId = "59fcfcfce5526519d43e0940";
    var albumId = getUrlParameter('albumId');
    var albumName = getUrlParameter('albumName');
    var targetId = getUrlParameter('targetId');
    document.getElementById('titleRecord').innerHTML = "Album: "+albumName;
    var albumSelect = document.getElementById('albumIdSelect');
    var curRecordCount;
    var recordList;
    var likeList;
    loadRecords();


    function loadRecords(){
        recordIds = [];
        picUrls = [];
        picIds = [];
        likeList = [];
        jQuery.ajax({
            //offset?
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
            recordList = data.content;
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
                        picUrls.push(picture.url);
                        picIds.push(picture.id);
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
                        document.getElementById(likeNum[i]).innerHTML = data2.content[1];
                        if(data2.content[0] == 0){
                            likeList.push(0);
                        }
                        else{
                            likeList.push(1);
                            $("#"+likeBtn[i]).removeClass("btn-dark").addClass("btn-danger");
                        }

                    })
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
        if (offset-count >= 0) {
            offset = offset-count;
            loadRecords();

        }
    })


    $("#likeBtn1").click(function(e){
        e.preventDefault();
        likeMove(0);
    })
    $("#likeBtn2").click(function(e){
        e.preventDefault();
        likeMove(1);
    })
    $("#likeBtn3").click(function(e){
        e.preventDefault();
        likeMove(2);
    })
    $("#likeBtn4").click(function(e){
        e.preventDefault();
        likeMove(3);
    })
    function likeMove(num) {
        var likeBtnCur = likeBtn[num];
        if(likeList[num] == 0) {
            jQuery.ajax({
                url: "/rest/records/" + recordIds[num] + "/likes",
                type: "POST",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Authorization", token);
                }
            }).done(function (data) {
                $.ajax({
                    url: "/rest/records/" + recordIds[num] + "/likeNumber",
                    type: "GET",
                    async: false,
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader("Authorization", localStorage.getItem("token"));
                    }
                }).done(function (data2) {
                    document.getElementById(likeNum[num]).innerHTML = data2.content;
                    $("#"+likeBtnCur).removeClass("btn-dark").addClass("btn-danger");
                    likeList[num] = 1;
                })
            })
        }
        else{
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
                    $("#"+likeBtnCur).removeClass("btn-danger").addClass("btn-dark");
                    likeList[num] = 0;
                })
            })
        }
    }


    // $("#dislikeBtn1").click(function(e){
    //     e.preventDefault();
    //     var num = 0;
    //     var likeBtn = "likeBtn1";
    //     var dislikeBtn = "dislikeBtn1";
    //     jQuery.ajax({
    //         url:  "/rest/records/" + recordIds[num] + "/likes",
    //         type: "DELETE",
    //         beforeSend: function (xhr) {
    //             xhr.setRequestHeader ("Authorization", token);
    //         }
    //     }).done(function (data) {
    //         $.ajax({
    //             url:  "/rest/records/"+recordIds[num]+"/likeNumber",
    //             type: "GET",
    //             async: false,
    //             beforeSend: function (xhr) {
    //                 xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
    //             }
    //         }).done(function(data2){
    //             document.getElementById(likeNum[num]).innerHTML = data2.content;
    //             //document.getElementById(dislikeNum[num]).innerHTML = data2.content;
    //             document.getElementById(likeBtn).style.visibility = 'visible';
    //             document.getElementById(dislikeBtn).style.visibility = 'hidden';
    //         })
    //     })
    // })
    // $("#likeBtn2").click(function(e){
    //     e.preventDefault();
    //     var num = 1;
    //     var likeBtn = "likeBtn2";
    //     var dislikeBtn = "dislikeBtn2";
    //     jQuery.ajax({
    //         url:  "/rest/records/" + recordIds[num] + "/likes",
    //         type: "POST",
    //         beforeSend: function (xhr) {
    //             xhr.setRequestHeader ("Authorization", token);
    //         }
    //     }).done(function (data) {
    //         $.ajax({
    //             url:  "/rest/records/"+recordIds[num]+"/likeNumber",
    //             type: "GET",
    //             async: false,
    //             beforeSend: function (xhr) {
    //                 xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
    //             }
    //         }).done(function(data2){
    //             //document.getElementById(likeNum[num]).innerHTML = data2.content;
    //             document.getElementById(dislikeNum[num]).innerHTML = data2.content;
    //             document.getElementById(likeBtn).style.visibility = 'hidden';
    //             document.getElementById(dislikeBtn).style.visibility = 'visible';
    //         })
    //     })
    // })
    //
    // $("#dislikeBtn2").click(function(e){
    //     e.preventDefault();
    //     var num = 1;
    //     var likeBtn = "likeBtn2";
    //     var dislikeBtn = "dislikeBtn2";
    //     jQuery.ajax({
    //         url:  "/rest/records/" + recordIds[num] + "/likes",
    //         type: "DELETE",
    //         beforeSend: function (xhr) {
    //             xhr.setRequestHeader ("Authorization", token);
    //         }
    //     }).done(function (data) {
    //         $.ajax({
    //             url:  "/rest/records/"+recordIds[num]+"/likeNumber",
    //             type: "GET",
    //             async: false,
    //             beforeSend: function (xhr) {
    //                 xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
    //             }
    //         }).done(function(data2){
    //             document.getElementById(likeNum[num]).innerHTML = data2.content;
    //             //document.getElementById(dislikeNum[num]).innerHTML = data2.content;
    //             document.getElementById(likeBtn).style.visibility = 'visible';
    //             document.getElementById(dislikeBtn).style.visibility = 'hidden';
    //         })
    //     })
    // })
    // $("#likeBtn3").click(function(e){
    //     e.preventDefault();
    //     var num = 2;
    //     var likeBtn = "likeBtn3";
    //     var dislikeBtn = "dislikeBtn3";
    //     jQuery.ajax({
    //         url:  "/rest/records/" + recordIds[num] + "/likes",
    //         type: "POST",
    //         beforeSend: function (xhr) {
    //             xhr.setRequestHeader ("Authorization", token);
    //         }
    //     }).done(function (data) {
    //         $.ajax({
    //             url:  "/rest/records/"+recordIds[num]+"/likeNumber",
    //             type: "GET",
    //             async: false,
    //             beforeSend: function (xhr) {
    //                 xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
    //             }
    //         }).done(function(data2){
    //             //document.getElementById(likeNum[num]).innerHTML = data2.content;
    //             document.getElementById(dislikeNum[num]).innerHTML = data2.content;
    //             document.getElementById(likeBtn).style.visibility = 'hidden';
    //             document.getElementById(dislikeBtn).style.visibility = 'visible';
    //         })
    //     })
    // })
    //
    // $("#dislikeBtn3").click(function(e){
    //     e.preventDefault();
    //     var num = 2;
    //     var likeBtn = "likeBtn3";
    //     var dislikeBtn = "dislikeBtn3";
    //     jQuery.ajax({
    //         url:  "/rest/records/" + recordIds[num] + "/likes",
    //         type: "DELETE",
    //         beforeSend: function (xhr) {
    //             xhr.setRequestHeader ("Authorization", token);
    //         }
    //     }).done(function (data) {
    //         $.ajax({
    //             url:  "/rest/records/"+recordIds[num]+"/likeNumber",
    //             type: "GET",
    //             async: false,
    //             beforeSend: function (xhr) {
    //                 xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
    //             }
    //         }).done(function(data2){
    //             document.getElementById(likeNum[num]).innerHTML = data2.content;
    //             //document.getElementById(dislikeNum[num]).innerHTML = data2.content;
    //             document.getElementById(likeBtn).style.visibility = 'visible';
    //             document.getElementById(dislikeBtn).style.visibility = 'hidden';
    //         })
    //     })
    // })
    // $("#likeBtn4").click(function(e){
    //     e.preventDefault();
    //     var num = 3;
    //     var likeBtn = "likeBtn4";
    //     var dislikeBtn = "dislikeBtn4";
    //     jQuery.ajax({
    //         url:  "/rest/records/" + recordIds[num] + "/likes",
    //         type: "POST",
    //         beforeSend: function (xhr) {
    //             xhr.setRequestHeader ("Authorization", token);
    //         }
    //     }).done(function (data) {
    //         $.ajax({
    //             url:  "/rest/records/"+recordIds[num]+"/likeNumber",
    //             type: "GET",
    //             async: false,
    //             beforeSend: function (xhr) {
    //                 xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
    //             }
    //         }).done(function(data2){
    //             //document.getElementById(likeNum[num]).innerHTML = data2.content;
    //             document.getElementById(dislikeNum[num]).innerHTML = data2.content;
    //             document.getElementById(likeBtn).style.visibility = 'hidden';
    //             document.getElementById(dislikeBtn).style.visibility = 'visible';
    //         })
    //     })
    // })
    //
    // $("#dislikeBtn4").click(function(e){
    //     e.preventDefault();
    //     var num = 3;
    //     var likeBtn = "likeBtn4";
    //     var dislikeBtn = "dislikeBtn4";
    //     jQuery.ajax({
    //         url:  "/rest/records/" + recordIds[num] + "/likes",
    //         type: "DELETE",
    //         beforeSend: function (xhr) {
    //             xhr.setRequestHeader ("Authorization", token);
    //         }
    //     }).done(function (data) {
    //         $.ajax({
    //             url:  "/rest/records/"+recordIds[num]+"/likeNumber",
    //             type: "GET",
    //             async: false,
    //             beforeSend: function (xhr) {
    //                 xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
    //             }
    //         }).done(function(data2){
    //             document.getElementById(likeNum[num]).innerHTML = data2.content;
    //             //document.getElementById(dislikeNum[num]).innerHTML = data2.content;
    //             document.getElementById(likeBtn).style.visibility = 'visible';
    //             document.getElementById(dislikeBtn).style.visibility = 'hidden';
    //         })
    //     })
    // })
    $("#view1").click(function(e){
        e.preventDefault();
        curRecordCount = 0;
        fetchComments(0);
    })
    $("#view2").click(function(e){
        e.preventDefault();
        curRecordCount = 1;
        fetchComments(1);
    })
    $("#view3").click(function(e){
        e.preventDefault();
        curRecordCount = 2;
        fetchComments(2);
    })
    $("#view4").click(function(e){
        e.preventDefault();
        curRecordCount = 3;
        fetchComments(3);
    })
    $("#commentSubmitBtn").click(function(e){
        e.preventDefault();
        jQuery.ajax({
            url:  "/rest/records/" + recordIds[curRecordCount] + "/messages",
            type: "POST",
            async: false,
            data: JSON.stringify({messageInfo:$("#myComment").val()}),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            fetchComments(curRecordCount);
        }).fail(function(data){
            alert("The comment can not be added. Please try again later.");
        })
    })
    function fetchComments(btnNum){
        $('#comments').empty();
        document.getElementById('myComment').value = "";
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
    $("#deleteBtn1").click(function(e){
        e.preventDefault();
        deleteRecord(0);
    })
    $("#deleteBtn2").click(function(e){
        e.preventDefault();
        deleteRecord(1);
    })
    $("#deleteBtn3").click(function(e){
        e.preventDefault();
        deleteRecord(2);
    })
    $("#deleteBtn4").click(function(e){
        e.preventDefault();
        deleteRecord(3);
    })
    function deleteRecord(btnNum){
        var r = confirm("Are you sure you want to delete this record?\nThis will delete all related data (likes, comments, pictures) and it's irreversible.");
        if(r == true) {
            jQuery.ajax({
                url:  "/rest/records/" + recordIds[btnNum],
                type: "DELETE",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", token);
                }
            }).done(function (data) {
                location.href = "Records.html?albumId="+albumId+"&albumName="+albumName+"&targetId="+targetId;
            }).fail(function(data){
                alert("Delete Record fail!");
            })
        } else {
            alert("Cancel delete!");
        }
    }
    $("#editBtn1").click(function(e){
        e.preventDefault();
        editRecord(0);
    })
    $("#editBtn2").click(function(e){
        e.preventDefault();
        editRecord(1);
    })
    $("#editBtn3").click(function(e){
        e.preventDefault();
        editRecord(2);
    })
    $("#editBtn4").click(function(e){
        e.preventDefault();
        editRecord(3);
    })
    function editRecord(btnNum){
        curRecordCount = btnNum;
        for(var i = albumSelect.options.length - 1 ; i >= 0 ; i--)
        {
            albumSelect.remove(i);
        }
        jQuery.ajax({
            url: "http://localhost:8080/rest/targets/"+targetId+"/albums?count=99",
            type: "GET",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }
        }).done(function (data) {
            var dataList = data.content;
            var curSelect;
            for (var i = 0; i < dataList.length; i++) {
                var name = dataList[i].albumName;
                var curAlbumId = dataList[i].id;

                var opt = document.createElement('option');
                opt.value = curAlbumId;
                opt.innerHTML = name;
                albumSelect.appendChild(opt);

                if(albumId == curAlbumId)
                    curSelect = i;
            }
            albumSelect.selectedIndex  = curSelect;
        })
        document.getElementById("recordName").value = recordList[btnNum].recordName;
        document.getElementById("recordInfo").value = recordList[btnNum].recordInfo;
        document.getElementById("recordURL").value = picUrls[btnNum];
    }
    $('#editSubmitBtn').click(function () {
        jQuery.ajax ({
            url:  "http://localhost:8080/rest/records/" + recordIds[curRecordCount],
            type: "PATCH",
            async: false,
            data: JSON.stringify({albumId:albumSelect.options[albumSelect.selectedIndex].value,recordName:$("#recordName").val(),recordInfo:$("#recordInfo").val()}),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
            }
        }).done(function(data){
            if(picUrls[curRecordCount]!=document.getElementById("recordURL").value){
                $.ajax({
                    url:  "http://localhost:8080/rest/pictures/" + picIds[curRecordCount],
                    type: "PATCH",
                    async: false,
                    data: JSON.stringify({url:document.getElementById("recordURL").value}),
                    dataType: "json",
                    contentType: "application/json; charset=utf-8",
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                    }
                }).done(function(data){
                }).fail(function(data){
                    alert("Fail to update this picture! Please check the data and try again later!");
                })
            }
            location.href = "Records.html?albumId="+albumId+"&albumName="+albumName+"&targetId="+targetId;
        }).fail(function(data){
            alert("Fail to update this record! Please check the data and try again later!");
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