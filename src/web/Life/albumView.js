$(function() {
    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");

    //ation='Records.html?data=Ya success!';
    //frames['test1'].document.getElementById("titleTarget").innerHTML='Ya';
    jQuery.ajax({
        url: "/rest/users/targetsWatch",
        type: "GET",
        beforeSend: function (xhr) {
            xhr.setRequestHeader ("Authorization", token);
        }
    }).done(function (data) {
        var targetList = data.content;
        for(var i = 0 ; i < targetList.length ; i++){
            var albumDiv = "<iframe MARGINWIDTH=0 MARGINHEIGHT=0 HSPACE=0 VSPACE=0 FRAMEBORDER=0 SCROLLING=no src=\"albumUnit.html?targetId="+targetList[i].id+"&targetName="+targetList[i].targetName+"\" height=450 width=\"100%\" name=\"test1\" id=\"test1\"></iframe>";
            $('#albumList').append(albumDiv);
        }
    })
})