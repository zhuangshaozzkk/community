$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	// 获取用户标题、内容
	let title = $("#recipient-name").val();
	let content = $("#message-text").val();
	// 发送ajax请求
	$.post(
		CONTEXT_PATH+"/discussPost/add",
		{"title":title, "content":content},
		function (data) {
			data=$.parseJSON(data);
			// 在提示框显示返回的信息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 2s后提示框消失
			setTimeout(function(){
				$("#hintModal").modal("hide");
				if(data.code == 0){
					// 刷新页面
					window.location.reload();
				}
			}, 2000);
		}
	)


}