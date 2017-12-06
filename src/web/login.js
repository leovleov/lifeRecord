$(function() {
    var isAdmin = null;
    var token = null;
    var userId = null;
    var userName = null;
    var offset = 0;
    var count = 20;
    var total = -1;

    $("#greeting").hide();
    // $("#resourceRow").hide();

    $("#signin").click(function (e) {
        e.preventDefault();
        jQuery.ajax ({
            url:  "/rest/sessions",
            type: "POST",
            async: false,
            data: JSON.stringify({emailAddress:$("#inputEmail").val(), password: $("#inputPassword").val()}),
            dataType: "json",
            contentType: "application/json; charset=utf-8"
        }).done(function(data){
            $("#greeting").text("Hello " + data.content.firstName);
            $("#greeting").show();
            $("#resourceTable").find(".cloned").remove();
            token = data.content.token;
            userId = data.content.userId;
            userName = data.content.firstName + " " + data.content.lastName;
            localStorage.setItem("token", token);
            localStorage.setItem("userId", userId);
            localStorage.setItem("userName", userName);
            location.href = "Life/AlbumView.html"
        }).fail(function(data){
            $("#greeting").text("Authentication Fail ! You might want to try it again!");
            $("#greeting").show();
        })
    })

    $("#signup").click(function (e) {
        e.preventDefault();
        location.href = "Life/SignUp.html"
    })
//
//     $("#loadcontent").click(function (e) {
//         e.preventDefault();
//         loadReview();
//     });
//
//     $("#next").click(function(e){
//         e.preventDefault();
//         if (offset+count < total) {
//             offset = offset+count;
//             loadReview();
//         }
//     })
//
//     $("#previous").click(function(e){
//         e.preventDefault();
//         console.log("Cliked")
//         if (offset-count >= 0) {
//             offset = offset-count;
//             loadReview();
//
//         }
//     })

    // function loadReview() {
    //     jQuery.ajax ({
    //         url:  "/api/users/" + userId + "/reviews?sort=rate&offset=" + offset + "&count="  + count,
    //         type: "GET",
    //         beforeSend: function (xhr) {
    //             xhr.setRequestHeader ("Authorization", token);
    //         }
    //     })
    //         .done(function(data){
    //             total = data.metadata.total;
    //             $("#page").text("Page " + Math.floor(offset/count+1) + " of " + (Math.ceil(total/count)));
    //             $("#resourceTable").find(".cloned").remove();
    //             data.content.forEach(function(item){
    //                 $( "#resourceRow" ).clone().prop("id",item.reviewId).appendTo( "#resourceTable" );
    //                 $("#"+item.reviewId).find("#showId").text(item.showId);
    //                 $("#"+item.reviewId).find("#episodeId").text(item.episodeId);
    //                 $("#"+item.reviewId).find("#rate").text(item.rate);
    //                 $("#"+item.reviewId).find("#topic").text(item.reviewTopic);
    //                 $("#"+item.reviewId).find("#content").text(item.reviewContent);
    //                 $("#"+item.reviewId).find("#likes").text(item.likes);
    //                 $("#"+item.reviewId).prop("class","cloned");
    //                 $("#"+item.reviewId).show();
    //             });
    //         })
    //         .fail(function(data){
    //             $("#reviewlist").text("Sorry no reviewss");
    //         })
    //
    // }

})


