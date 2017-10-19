$(function() {
    var offset = 0;
    var count = 15;
    var total = -1;
    var recordId = '59e861e0553b2c522820805b';
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
                //recordId = data.content.firstChild.recordId;
                data.content.forEach(function(item){
                    //recordId = $('#'+item.id);
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
        loadPics();
    });

    $("#next").click(function(e){
        e.preventDefault();
        if (offset+count < total) {
            offset = offset+count;
            loadPics();
        }
    })

    $("#previous").click(function(e){
        e.preventDefault();
        console.log("Cliked")
        if (offset-count >= 0) {
            offset = offset-count;
            loadPics();

        }
    })

    function loadPics(){
        jQuery.ajax ({
            url:  "/rest/records/" + recordId + "/pictures?sort=url&offset=" + offset + "&count="  + count,
            type: "GET",
            /*beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            }*/
        })
            .done(function(data){
                total = data.metadata.total;
                $("#page").text("Page " + Math.floor(offset/count+1) + " of " + (Math.ceil(total/count)));
                $("#picTable").find(".cloned").remove();
                data.content.forEach(function(item){
                    $( "#picRow" ).clone().prop("id",item.id).appendTo( "#picTable" );
                    $("#"+item.id).find("#url").text(item.url);
                    $("#"+item.id).find("#recordId").text(item.recordId);
                    $("#"+item.id).prop("class","cloned");
                    $("#"+item.id).show();
                });
            })
            .fail(function(data){
                $("#carlist").text("Sorry no record");
            })
    }
})


