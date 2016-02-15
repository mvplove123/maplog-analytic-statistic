$(document).ready(function() {

});

function init() {

	// 获取表单参数
	var limitNums = $('#limitNum').val();

	var url = "/hotelAnalyse/createHotelAnalyseIndex.do?limit=" + limitNums;

	$.ajax({
		url : url,
		type : 'post',
		cache : false,
		dataType : 'json',
		success : function(data) {
			$("#injectResult").html("injecting "+limitNums+ "   hotelanalyse done!");
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
	
			alert(errorThrown);

		}
	});
	
}

function search() {
	

	$('#myTb').empty();
	// 获取表单参数
	var cityCode = $('#cityCode').val();
	var query = $('#query').val();

	var url = "/hotelAnalyse/findByCityCodeAndQuery.do?cityCode=" + cityCode
			+ '&query=' + query;

	$.ajax({
		url : url,
		type : 'post',
		cache : false,
		dataType : 'json',
		success : function(data) {
			result = data.data;

			var tbBody = "<tr><th>ID</th><th>cityCode</th><th>query</th><th>searchCnt</th></tr>"
			$.each(result, function(i, a) {
				

				tbBody += "<tr><td>" + a.id
						+ "</td>" + "<td>" + a.cityCode + "</td>" + "<td>"
						+ a.query  + "</td>" +"<td>"+ a.searchCnt + "</td></tr>";
			});
			$('#myTb').append(tbBody);

			//alert(tbBody);
		},
		error : function(XMLHttpRequest, textStatus, errorThrown) {
			alert(XMLHttpRequest.status);
			alert(XMLHttpRequest.readyState);
			alert(textStatus);

		}
	});
}
