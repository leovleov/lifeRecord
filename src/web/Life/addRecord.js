$(function() {
    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");
    var userSelect = document.getElementById('userIdSelect');
    var targetSelect = document.getElementById('targetIdSelect_Record');
    var albumSelect = document.getElementById('albumIdSelect');

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
            url:  "http://localhost:8080/rest/targets/" + targetSelect.options[targetSelect.selectedIndex].value + "/records",
            type: "POST",
            async: false,
            data: JSON.stringify({albumId:albumSelect.options[albumSelect.selectedIndex].value,recordName:$("#recordName").val(),recordInfo:$("#recordInfo").val(),picture:$("#recordURL").val()}),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
            }
        }).done(function(data){
            $("#greeting").text("Add a new record successfully!");
            $("#greeting").show();
           location.href = "AlbumView.html"
        }).fail(function(data){
            $("#greeting").text("Fail ! Please check the data!");
            $("#greeting").show();
        })
    })
    $('#cancelButton').click(function () {
        location.href = "AlbumView.html"
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
        url: "http://localhost:8080/rest/targets/"+targetSelect.options[targetSelect.selectedIndex].value+"/albums?count=99",
        // url: "/rest/users/",
        type: "GET",
        beforeSend: function (xhr) {
            xhr.setRequestHeader ("Authorization", token);
        }
    }).done(function (data) {
        var dataList = data.content;

        for (var i = 0; i < dataList.length; i++) {
            var name = dataList[i].albumName;
            var albumId = dataList[i].id;

            var opt = document.createElement('option');
                opt.value = albumId;
                opt.innerHTML = name;
                albumSelect.appendChild(opt);
        }
    })
}