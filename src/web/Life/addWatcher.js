$(function() {
    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");
    var userSelect = document.getElementById('userIdSelect');
    var targetSelect = document.getElementById('targetIdSelect');

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
            url:  "/rest/watchers/",
            type: "POST",
            async: false,
            data: JSON.stringify({userId:userSelect.options[userSelect.selectedIndex].value,targetId:targetSelect.options[targetSelect.selectedIndex].value}),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
            }
        }).done(function(data){
            $("#greeting").text("Add a new watcher " + userSelect.options[userSelect.selectedIndex].innerHTML + " successful!");
            $("#greeting").show();
            location.href = "WatchersList.html"
        }).fail(function(data){
            $("#greeting").text("Fail ! Please check the data!");
            $("#greeting").show();
        })
    })
    $('#cancelButton').click(function () {
        location.href = "WatchersList.html"
    })
})

function targetChange() {

    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");
    var userSelect = document.getElementById('userIdSelect');
    var targetSelect = document.getElementById('targetIdSelect');
    for(var i = userSelect.options.length - 1 ; i >= 0 ; i--)
    {
        userSelect.remove(i);
    }
    jQuery.ajax({
        url: "/rest/users/",
        type: "GET",
        // beforeSend: function (xhr) {
        //     xhr.setRequestHeader ("Authorization", token);
        // }
    }).done(function (data) {
        var dataList = data.content;
        var watcherList;
        $.ajax({
            url:  "/rest/targets/"+targetSelect.options[targetSelect.selectedIndex].value+"/watchers",
            type: "GET",
            async: false,
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
            }
        }).done(function(data){
            watcherList = data.content;
        }).fail(function(data){
            watcherList = null;
        })

        for (var i = 0; i < dataList.length; i++) {
            var name = dataList[i].firstName + " " + dataList[i].lastName;
            var watcherId = dataList[i].id;
            var found = false;
            if(watcherId == "59fc1ddfa69f5d401c607623"){continue;}
            else {
                for (var j = 0; j < watcherList.length; j++) {
                    if (watcherId == watcherList[j].id) {
                        found = true;
                    }
                }
            }
            if(found == false) {
                var opt = document.createElement('option');
                opt.value = watcherId;
                opt.innerHTML = name;
                userSelect.appendChild(opt);
            }
        }
    })
}