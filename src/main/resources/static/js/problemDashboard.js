$(document).on("wb-ready.wb", function() {
	$(".resolveBtn").on("click", function(e) {
		var id = $(e.target).attr("id").replace("resolve", "");
		var items = $("#resolve" + id).parent().parent().parent().find("td");
		$("#notes").val($(items.get(7)).text());
		$('#submitWindow').find('input#submit').click(function(e) {
			e.preventDefault();
			e.stopImmediatePropagation();
			var obj = {
				"department" : $(items.get(0)).text(),
				"language" : $(items.get(1)).text(),
				"url" : $(items.get(2)).text(),
				"problem" : $(items.get(3)).text(),
				"problemDetails" : $(items.get(4)).text(),
				"problemDate" : $(items.get(5)).text(),
				"resolution" : $("#notes").val(),
				"id" : id
			};

			$.ajax({
				type : "post",
				data : obj,
				cache : false,
				url : "updateProblem",
				dataType : "text",
				error : function(xhr, status, error) {
					console.log(xhr.responseText);
				},
				success : function(date) {
					$(items.get(7)).text($("#notes").val());
					$("#notes").val("");
					$(items.get(8)).text(date);
					$(".popup-modal-dismiss").click();
				}
			});

		});
		wb.doc.trigger("open.wb-lbx", [ [ {
			src : "#submitWindow",
			type : "inline"
		} ] ]);

	});

});