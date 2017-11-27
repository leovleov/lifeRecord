$(function() {
    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");

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
            var watcherList;
            $.ajax({
                url:  "/rest/targets/"+targetId+"/watchers",
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

            var watcherDiv = '<div class="row col-md-8 col-md-offset-2">\n' +
                '        <form class="form-signin" >\n' +
                '            <h2 class="form-signin-heading">Target: '+targetName+'</h2>\n';

            for(var j = 0 ; j < watcherList.length ; j++){
                watcherDiv = watcherDiv +
                    '<div class="form-check">\n' +
                    '  <label class="form-check-label">\n' +
                    '    <input class="form-check-input" type="checkbox" id = "'+watcherList[j].id+'_'+i+'" name="watcherCheck" value="'+targetId+'">\n' +
                    watcherList[j].firstName + " " + watcherList[j].lastName +
                    '  </label>\n' +
                    '</div>';

            }

            watcherDiv = watcherDiv +    '        </form>' +
                '</div>'
            $('#watcherList').append(watcherDiv);


        }

    })



    $('#addWatcher').click(function () {
        location.href = "AddWatcher.html"
    })

    $('#deleteWatcher').click(function () {
        var checkboxesChecked = document.querySelectorAll('input[name=watcherCheck]:checked');
        //var checkboxesChecked = getCheckedBoxes();
        localStorage.setItem("checkBoxNum",checkboxesChecked.length);
        for(var i = 0 ; i < checkboxesChecked.length ; i++) {
            jQuery.ajax({
                url: "/rest/watchers/",
                type: "DELETE",
                async: false,
                data: JSON.stringify({userId:checkboxesChecked[i].id.split("_")[0],targetId:checkboxesChecked[i].value}),
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Authorization", token);
                }
            }).done(function (data) {

            })
        }
        location.href = "WatchersList.html"
    })
})