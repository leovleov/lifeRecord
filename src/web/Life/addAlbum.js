$(function() {
    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");
    var select = document.getElementById('targetIdSelect');

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
            select.appendChild(opt);
        }
    })

    $('#addButton').click(function () {
        jQuery.ajax ({
            url:  "/rest/targets/"+select.options[select.selectedIndex].value+"/albums",
            type: "POST",
            async: false,
            data: JSON.stringify({albumName:$("#albumName").val()}),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
            }
        }).done(function(data){
            $("#greeting").text("Add a new album " + $("#albumName").val() + " successful!");
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