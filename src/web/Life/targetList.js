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
        data.content.forEach(function(item){
            //recordId = $('#'+item.id);
            var targetId = item.id;
            $( "#targetRow" ).clone().prop("id",item.id).appendTo( "#targetTable" );
            $("#"+item.id).find("#targetName").text(item.targetName);
            $("#"+item.id).find("#targetInfo").text(item.targetInfo);
            var recordList = null;
            $.ajax({
                url:  "/rest/targets/"+targetId+"/records",
                type: "GET",
                async: false,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
                }
            }).done(function(data){
                recordList = data.content;
            }).fail(function(data){
                recordList = null;
            })
            if(recordList == null || recordList.length == 0){
                $("#"+item.id).find("#condition").text("There is no record belonging to this target.");
            }
            else{
                $("#"+item.id).find("#condition").text("There are records belonging to this target. Clean the records before deleting it.");
            }

            var x = document.createElement("INPUT");
            x.setAttribute("type", "checkbox");
            x.setAttribute("name", "targetCheck");
            x.setAttribute("value", item.targetName);
            x.setAttribute("id", item.id);
            $("#"+item.id).find("#select").append("",x);

            $("#"+item.id).prop("class","cloned");
            $("#"+item.id).show();
        });
        // var dataList = data.content;
        //
        // for (var i = 0; i < dataList.length; i++) {
        //     var targetName = dataList[i].targetName;
        //     var targetInfo = dataList[i].targetInfo;
        //     var targetId = dataList[i].id;
        //     var albumList;
        //
        //
        // }

    })

    function getCheckedBoxes() {
        var checkboxes = document.getElementsByName(targetCheck);
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

    $('#addTarget').click(function () {
        location.href = "AddTarget.html"
    })

    $('#deleteTarget').click(function () {
        var checkboxesChecked = document.querySelectorAll('input[name=targetCheck]:checked');
        //var checkboxesChecked = getCheckedBoxes();
        localStorage.setItem("checkBoxNum",checkboxesChecked.length);
        for(var i = 0 ; i < checkboxesChecked.length ; i++) {
            jQuery.ajax({
                url: "/rest/targets/"+checkboxesChecked[i].id,
                type: "DELETE",
                beforeSend: function (xhr) {
                    xhr.setRequestHeader("Authorization", token);
                }
            }).done(function (data) {
                //alert("Target "+ checkboxesChecked[i].value +" has been deleted.");
            }).fail(function(data){
                //alert("Can not delete target "+ checkboxesChecked[i].value +".");
            })
        }
        location.href = "TargetsList.html"
    })
})