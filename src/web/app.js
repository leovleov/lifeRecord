$(function() {

    $("#recordRow").hide();


    $("#getrecords").click(function (e) {
        e.preventDefault();
        jQuery.ajax ({
            url:  "/rest/records/",
            type: "GET",
            /*beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }*/
        })
            .done(function(data){
                data.content.forEach(function(item){
                    $( "#recordRow" ).clone().prop("id",item.id).appendTo( "#recordTable" );
                    $("#"+item.id).find("#recordName").text(item.recordName);
                    $("#"+item.id).find("#recordInfo").text(item.recordInfo);
                    $("#"+item.id).find("#albumId").text(item.albumId);
                    $("#"+item.id).find("#targetId").text(item.targetId);
                    $("#"+item.id).find("#viewId").text(item.viewId);
                    $("#"+item.id).find("#likeId").text(item.likeId);
                    $("#"+item.id).find("#editorId").text(item.editorId);
                    $("#"+item.id).prop("class","cloned");
                    $("#"+item.id).show();
                });
            })
            .fail(function(data){
                $("#carlist").text("Sorry no record");
            })
    });
})


