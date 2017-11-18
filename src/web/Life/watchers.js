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
            var watchersList;
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
                //localStorage.setItem("isAdmin",false);
            })

            var watcherDiv = '<div class="row col-md-8 col-md-offset-2">\n' +
                '        <form class="form-signin">\n' +
                '            <h2 class="form-signin-heading">Target: '+targetName+'</h2>\n';

            for(var j = 0 ; j < watcherList.length ; j++){
                watcherDiv = watcherDiv +
                    '<div class="form-check">\n' +
                    '  <label class="form-check-label">\n' +
                    '    <input class="form-check-input" type="checkbox" name="watcherCheck" id="watcherCheck" value="'+watcherList[j].id+'">\n' +
                    watcherList[j].userId +
                    '  </label>\n' +
                    '</div>';

                // '<div class="checkbox">\n' +
                // '                <label>\n' +
                // '                    <input type="checkbox" value="'+albumList[j].albumName+'"> '+albumList[j].albumName+'\n' +
                // '                </label>\n' +
                // '            </div>';
            }

            watcherDiv = watcherDiv +    '        </form>' +
                '</div>'
            $('#watcherList').append(watcherDiv);

        }

    })

    function getCheckedBoxes() {
        var checkboxes = document.getElementsByName(watcherCheck);
        var checkboxesChecked = [];
        // loop over them all
        for (var i=0; i<checkboxes.length; i++) {
            // And stick the checked ones onto an array...
            if (checkboxes[i].checked) {
                checkboxesChecked.push(checkboxes[i]);
            }
        }
        // Return the array if it is non-empty, or null
        return checkboxesChecked.length > 0 ? checkboxesChecked : null;
    }

    $('#addWatcher').click(function () {
        location.href = "AddWatcher.html"
    })

    $('#deleteWatcher').click(function () {
        var checkboxesChecked = document.querySelectorAll('input[name=WatcherCheck]:checked');
        //var checkboxesChecked = getCheckedBoxes();
        localStorage.setItem("checkBoxNum",checkboxesChecked.length);
        for(var i = 0 ; i < checkboxesChecked.length ; i++) {
            jQuery.ajax({
                url: "/rest/Watchers/"+checkboxesChecked[i].value,
                type: "DELETE",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Authorization", token);
                }
            }).done(function (data) {

            })
        }
        location.href = "WatchersList.html"
    })
})