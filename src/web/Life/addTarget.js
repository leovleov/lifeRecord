$(function() {
    var token = localStorage.getItem("token");
    var userId = localStorage.getItem("userId");
    var targetNameText = document.getElementById('targetNameText');
    var targetInfoText = document.getElementById('targetInfoText');

    $("#greeting").hide();


    $('#addButton').click(function () {
        jQuery.ajax ({
            url:  "/rest/targets/",
            type: "POST",
            async: false,
            data: JSON.stringify({targetName:targetNameText.value,targetInfo:targetInfoText.value}),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", localStorage.getItem("token"));
            }
        }).done(function(data){
            $("#greeting").text("Add a new Target " + $("#targetNameText").val() + " successful!");
            $("#greeting").show();
            location.href = "TargetsList.html"
        }).fail(function(data){
            $("#greeting").text("Fail ! Please check the data!");
            $("#greeting").show();
        })
    })
    $('#cancelButton').click(function () {
        location.href = "TargetsList.html"
    })
})