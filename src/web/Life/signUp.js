$(function() {
    $("#messaging").hide();

    $("#signUp").click(function (e) {
        e.preventDefault();
        jQuery.ajax ({
            url:  "/rest/users",
            type: "POST",
            async: false,
            data: JSON.stringify({emailAddress:$("#inputEmail").val(), password: $("#inputPassword").val(), firstName: $("#inputFirstName").val()
                , lastName: $("#inputLastName").val(), nickName: $("#inputNickName").val(), phoneNumber: $("#inputPhoneNum").val()}),
            dataType: "json",
            contentType: "application/json; charset=utf-8"
        }).done(function(data){
            $("#messaging").text("Sign Up Successfully!");
            $("#messaging").show();
            $("#resourceTable").find(".cloned").remove();
            token = data.content.token;
            userId = data.content.userId;
            localStorage.setItem("token", token);
            localStorage.setItem("userId", userId);
            location.href = "AlbumManagement.html"
        }).fail(function(data){
            $("#messaging").text("Sign Up Fail ! Please check the information and try it again!");
            $("#messaging").show();
        })
    })

    $("#cancel").click(function (e) {
        e.preventDefault();
        location.href = "../"
    })

})