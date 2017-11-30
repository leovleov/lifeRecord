$(function() {
    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");
    var userSelect = document.getElementById('userIdSelect');
    var targetSelect = document.getElementById('targetIdSelect_Record');

    $("#greeting").hide();

    jQuery.ajax({
        url: "/rest/users/"+userId+"/targets",
        type: "GET",
        beforeSend: function (xhr) {
            xhr.setRequestHeader ("Authorization", token);
        }
    }).done(function (data) {
        var dataList = data.content;

        for (var i = 0; i < dataList.length; i++) {
            var targetName = dataList[i].targetName;
            var targetInfo = dataList[i].targetInfo;
            var targetId = dataList[i].id;
            var opt = document.createElement('option');
            opt.value = targetId;
            opt.innerHTML = targetName;
            targetSelect.appendChild(opt);
        }
    })

    $('#addButton').click(function () {
        jQuery.ajax ({
            url:  "/rest/albums/",
            type: "POST",
            async: false,
            data: JSON.stringify({userId:userSelect.options[userSelect.selectedIndex].value,targetId:targetSelect.options[targetSelect.selectedIndex].value}),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
            }
        }).done(function(data){
            $("#greeting").text("Add a new record successfully!");
            $("#greeting").show();
            location.href = "AlbumManagement.html"
        }).fail(function(data){
            $("#greeting").text("Fail ! Please check the data!");
            $("#greeting").show();
        })
    })
    $('#cancelButton').click(function () {
        location.href = "AlbumManagement.html"
    })
})

function targetChange() {

    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");
    var albumSelect = document.getElementById('albumIdSelect');
    var targetSelect = document.getElementById('targetIdSelect_Record');
    for(var i = albumSelect.options.length - 1 ; i >= 0 ; i--)
    {
        albumSelect.remove(i);
    }
    jQuery.ajax({
        url: "/rest/users/",
        type: "GET",
        // beforeSend: function (xhr) {
        //     xhr.setRequestHeader ("Authorization", token);
        // }
    }).done(function (data) {
        var dataList = data.content;
        var albumList;
        $.ajax({
            url:  "/rest/targets/"+targetSelect.options[targetSelect.selectedIndex].value+"/albums",
            type: "GET",
            async: false,
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
            }
        }).done(function(data){
            albumList = data.content;
        }).fail(function(data){
            albumList = null;
        })

        for (var i = 0; i < dataList.length; i++) {
            var name = dataList[i].albumName;
            var albumId = dataList[i].id;
            var found = false;
            if(albumId == "5a02b5f339b860dea0b27a04") continue;
            else {
                for (var j = 0; j < albumList.length; j++) {
                    if (albumId == albumList[j].id) {
                        found = true;
                    }
                }
            }
            if(found == false) {
                var opt = document.createElement('option');
                opt.value = albumId;
                opt.innerHTML = name;
                albumSelect.appendChild(opt);
            }
        }
    })
}