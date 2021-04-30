<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<script type="text/javascript">

$(document).ready(function(){
	
	var linkV = ${ct:getAllValuesJson(ct:getConstDef("CD_EXTERNAL_LINK_DETAIL"))};
	var tbody = $('.linkCase').find('tbody');
	
	$.each(linkV, function(index, value) {
		var strA = value.cdDtlExp.split("|"); 
		var topPosition = 60 + (100  * index);
		var linkSeq = (index+1);
		
		var tr  = "<tr>";
				tr += "<th class='dCase'>";
			  		tr += '<a class="right" id="helpLink_'+linkSeq+'" style="position:absolute; cursor: pointer; top:'+topPosition+'px; left:1060px; display:none;"><img alt="" src="/images/user-guide.png" /></a>';
			  		tr += value.cdDtlNm;
			    tr += "</th>";
				tr += "<td class='dCase tleft'>"+strA[0]+"<br>"+strA[1]+"</td> ";
			    tr += "<td class='dCase'>";
			    	tr += "<a href='"+strA[1]+"' class='btnCLight darkgray' ";
				if(strA[1] != null && strA[1] != "") {
					tr += "target='_blank'";
				}
					tr += ">GO ▶</a>";
				tr += "</td>";
			tr += "</tr>";
		
		$(tbody).append(tr);

		showHelpLink("External_Link_"+linkSeq, "helpLink_"+linkSeq);
	});
});

</script>