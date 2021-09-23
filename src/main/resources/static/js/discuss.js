function like(btn,type,id) {
    $.post(
        CONTEXT_PATH+"like",
        {"entityType":type,"entityId":id},
        function (data) {
            data = $.parseJSON(data);
            if(data.code == 0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus==1?"已赞":"赞");
            }else{
                alert(data.msg);
            }
        }
    )
}