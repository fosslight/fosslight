<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- 관리자 화면 템플릿 --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript">
		var ossNames = [];
		var licenseNames = [];
		
		$(document).ready(function() {
			data.init();

			$("#ossName").autocomplete({
				source: ossNames
				, minLength: 3 // IE 스크립트 성능이슈로 0->3 으로 변경 yuns
				, open: function() { $(this).attr('state', 'open');}
				, close: function () { $(this).attr('state', 'closed');}
			}).focus(function() {
				if ($(this).attr('state') != 'open') {
					$(this).autocomplete("search");
				}
			}).on('autocompletechange', function() {
				var value = $("#ossName").val();
				if(value != ""){
					fn.getOssVersions($("#ossVersion"), value);
				}
			});
			
			$("#licenseNameSelect").autocomplete({
				source: licenseNames
				, minLength: 0
				, open: function() { $(this).attr('state', 'open'); }
				, close: function () { $(this).attr('state', 'closed'); }
			}).focus(function(){
				if ($(this).attr('state') != 'open') {
					$(this).autocomplete("search");
				}
			});

			$("#licenseNameSelect").on( "autocompletechange", function() {
				var val = $("#licenseNameSelect").val();
				var licenseName = $("#licenseName").val();
				var mult = null;

				for (var i in licenseNames){
					if("" != val && val == licenseNames[i].value){
						mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'>" + licenseNames[i].value + "<button onclick='fn.bulkEditDeleteLicense(this)'>x</button></span>";
						if(licenseName != ""){
							licenseName += "," + licenseNames[i].value;
							$("#licenseName").val(licenseName);
						}else{
							$("#licenseName").val(licenseNames[i].value);
						}
						break;
					}
				}

				if(mult == null){
					mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'>" + val + "<button onclick='fn.bulkEditDeleteLicense(this)'>x</button></span>";
					if(licenseName != ""){
						licenseName += "," + val;
						$("#licenseName").val(licenseName);
					}else{
						$("#licenseName").val(val);
					}
				}
				
				$('#licenseNameBtn').append(mult);
				$('#licenseNameSelect').val("");
				
			}).on("keypress", function(evt){
				if(evt.keyCode == 13){
					var val = $("#licenseNameSelect").val();
					var licenseName = $("#licenseName").val();
					var mult = null;

					for (var i in licenseNames){
						if("" != val && val == licenseNames[i].value){
							mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'>" + licenseNames[i].value + "<button onclick='fn.bulkEditDeleteLicense(this)'>x</button></span>";
							if(licenseName != ""){
								licenseName += "," + licenseNames[i].value;
								$("#licenseName").val(licenseName);
							}else{
								$("#licenseName").val(licenseNames[i].value);
							}
							break;
						}
					}

					if(mult == null){
						mult = "<span class=\"btnMulti\" style='margin-bottom:2px;'>" + val + "<button onclick='fn.bulkEditDeleteLicense(this)'>x</button></span>";
						if(licenseName != ""){
							licenseName += "," + licenseNames[i].value;
							$("#licenseName").val(licenseName);
						}else{
							$("#licenseName").val(licenseNames[i].value);
						}
					}

					$('#licenseNameBtn').append(mult);
					$('#licenseNameSelect').val("");
				}
			});
		});

		var data = {
			init : function(){
				// ossNames auto complete
				fn.getOssNames().success(function(data, status, headers, config){
					if(data != null && ossNames == ""){
						data.forEach(function(obj){
							ossNames.push(obj.ossName);
						})
					}
				});

				// licenseNames auto complete
				commonAjax.getLicenseTags().success(function(data, status, headers, config){
					if(data != null && licenseNames == ""){
						var tag = "";
						data.forEach(function(obj){
							if(obj!=null) {
								tag ={
									value : obj.shortIdentifier.length > 0 ? obj.shortIdentifier : obj.licenseName,
									label : obj.licenseName + (obj.shortIdentifier.length > 0 ? (" (" + obj.shortIdentifier + ")") : ""),
									type : obj.licenseType,
									obligation : obj.obligation,
									obligationChecks : obj.obligationChecks
								}
								
								licenseNames.push(tag);
							}
						});
					}
				});
			}
		}

		var fn = {
			getOssNames : function(data){
				return $.ajax({
					type: 'GET',
					url: "<c:url value='/project/getOssNames'/>",
					data: data,
					headers: {
						'Content-Type': 'application/json'
					},
				});
			},
			getOssVersions : function(e, ossName){
				var ossVersions = [];
				if(ossName=="") return false;

				return $.ajax({
					type: 'GET',
					url: '<c:url value="/project/getOssVersions"/>',
					async: false,
					data: {ossName : ossName },
					headers: {
						'Content-Type': 'application/json'
					},
					success : 
						function(data, status, headers, config){
			 				if(data != null){
		 						data.forEach(function(obj){
		 							ossVersions.push(obj.ossVersion);
		 						});
								$(e).autocomplete({
									source: ossVersions
									, minLength: 0
									, open: function() { $(this).attr('state', 'open');}
									, close: function () { $(this).attr('state', 'closed');}
								}).focus(function() {
									if ($(this).attr('state') != 'open') {
										$(this).autocomplete("search");
									}
								});
			 					ossVersions = [];
			 					$(e).focus();
							}
						}
				});
			},
			bulkEditDeleteLicense : function(target){
				$(target).text("|");
				var delLicenseName = $(target).parent().text();
				delLicenseName = delLicenseName.replace("|", "");
				
				var licenseName = $("#licenseName").val();
				
				if(licenseName.indexOf(",") > -1){
					var reMarkLicenseName = licenseName.split(",");
					for(var i=0; i<reMarkLicenseName.length; i++){
						if(reMarkLicenseName[i] == delLicenseName){
							reMarkLicenseName.splice(i, 1);
							break;
						}
					}

					$("#licenseName").val(reMarkLicenseName);
				}else{
					$("#licenseName").val("");
				}

				$(target).parent().remove();
			},
			bulkEditChange : function(){
				var notCheckedArr = [];
				var checkedArr = [];

				$(".bulkEditCheck:not(:checked)").each(function(){
					var val = $(this).val().replace("Check", "");
					notCheckedArr.push(val);
				});
				
				$(".bulkEditCheck:checked").each(function(){
					var val = $(this).val().replace("Check", "");
					checkedArr.push(val);
				});
				
				if(checkedArr.length > 0){
					var obj = $("#bulkEditForm").serializeObject();

					delete obj["licenseNameSelect"];

					if(notCheckedArr.length > 0){
						for(var i=0; i<notCheckedArr.length; i++){
							delete obj[notCheckedArr[i]];
						}
					}
					
					if(obj["target"] == ("selfCheck")){
						obj["target"] = "srcList";
					}
					
					alertify.confirm('<spring:message code="msg.oss.change.bulkedit"/>', function (e) {
						if (e){
							$(".loading").show();

							setTimeout(function(){
								opener.com_fn.bulkEditOssInfo(obj);
								$(".loading").hide();
								self.close();
							}, 300);
						}else{
							return false;
						}
					});
				}else{
					alertify.alert('<spring:message code="msg.oss.select.attribute.bulkedit"/>', function(){});
				}
			},
			bulkEditDelete : function(){
				var target = $("#target").val();
				if(target == "selfCheck"){
					target = "srcList";
				}
				var rowId = $("#rowId").val();

				alertify.confirm('<spring:message code="msg.oss.delete.bulkedit"/>', function (e) {
					if (e){
						$(".loading").show();

						setTimeout(function(){
							opener.com_fn.bulkEditDelRow(target, rowId, 'main');
							$(".loading").hide();
							self.close();
						}, 300);
					}else{
						return false;
					}
				});
			},
			bulkEditClose : function(){
				self.close();
			}
		}
		
		</script>
	</head>
	<body>
		<div id="loading_wrap_popup" class="loading" style="display:none;">
			<div class="loadingBlind"></div>
			<img src="<c:url value="/images/loading.gif"/>" alt="loading" />
		</div>
		<div id="wrap" style="padding-top: 10px;">
			<span style="text-align:center;"><h1 style="font-size:16px;">Bulk Edit OSS Attributes</h1></span>
			<div align="center" style="padding-top:10px;">
				<div class="tbws1" style="width:90%;">
					<form name="bulkEditForm" id="bulkEditForm">
					<table class="dCase">
						<input type="hidden" id="rowId" name="rowId" value="${rowId}"/>
						<input type="hidden" id="target" name="target" value="${target}"/>
						<colgroup>
							<col width="27%"/>
							<col/>
						</colgroup>
						<tbody>
							<tr>
								<th class="dCase">Attributes</th>
								<th class="dCase" style="text-align:center;">Contents</th>
							</tr>
							<c:if test="${(target eq 'srcList') or (target eq 'selfCheck') or (target eq 'list')}">
								<tr>
								<c:if test="${target eq 'srcList'}">
									<th class="dCase">Source Name or Path</th>
								</c:if>
								<c:if test="${(target eq 'selfCheck') or (target eq 'list')}">
									<th class="dCase">Binary Name or Source Path</th>
								</c:if>
									<td class="dCase"><input type="text" style="width:60%;" id="filePath" name="filePath"/> <input type="checkbox" class="bulkEditCheck" value="filePathCheck"/></td>
								</tr>
							</c:if>
							<c:if test="${(target eq 'binList') }">
								<tr>
									<c:if test="${target eq 'binList'}">
										<th class="dCase">Binary Name</th>
									</c:if>
									<td class="dCase"><input type="text" style="width:60%;" id="binaryName" name="binaryName"/> <input type="checkbox" class="bulkEditCheck" value="binaryNameCheck"/></td>
								</tr>
							</c:if>
							<c:if test="${target eq 'binAndroidList'}">
								<tr>
									<th class="dCase">Binary Name</th>
									<td class="dCase"><input type="text" style="width:60%;" id="binaryName" name="binaryName"/> <input type="checkbox" class="bulkEditCheck" value="binaryNameCheck"/></td>
								</tr>
								<tr>
									<th class="dCase">Source Path</th>
									<td class="dCase"><input type="text" style="width:60%;" id="filePath" name="filePath"/> <input type="checkbox" class="bulkEditCheck" value="filePathCheck"/></td>
								</tr>
							</c:if>
							<tr>
								<th class="dCase">OSS Name</th>
								<td class="dCase"><input type="text" style="width:60%;" id="ossName" name="ossName" /> <input type="checkbox" class="bulkEditCheck" value="ossNameCheck"/></td>
							</tr>
							<tr>
								<th class="dCase">OSS Version</th>
								<td class="dCase"><input type="text" style="width:60%;" id="ossVersion" name="ossVersion" /> <input type="checkbox" class="bulkEditCheck" value="ossVersionCheck"/></td>
							</tr>
							<tr>
								<th class="dCase">License</th>
								<td class="dCase">
									<div style="width:100%; display:table; table-layout:fixed;">
										<div style="width:50%; display:table-cell; vertical-align:middle;">
											<input type="text" id="licenseNameSelect" name="licenseNameSelect" style="width:90%;"/> <input type="checkbox" class="bulkEditCheck" value="licenseNameCheck"/>
											<input type="hidden" id="licenseName" name="licenseName"/>
										</div>
										<div id="licenseNameBtn" style="display:table-cell; vertical-align:middle;"></div>
									</div>
								</td>
							</tr>
							<tr>
								<th class="dCase">Download Location</th>
								<td class="dCase"><input type="text" style="width:60%;" id="downloadLocation" name="downloadLocation"/> <input type="checkbox" class="bulkEditCheck" value="downloadLocationCheck"/></td>
							</tr>
							<c:if test="${target ne 'selfCheck'}">
							<tr>
								<th class="dCase">Home Page</th>
								<td class="dCase"><input type="text" style="width:60%;" id="homepage" name="homepage"/> <input type="checkbox" class="bulkEditCheck" value="homepageCheck"/></td>
							</tr>
							</c:if>
							<tr>
								<th class="dCase">Copyright Text</th>
								<td class="dCase"><input type="text" style="width:60%;" id="copyrightText" name="copyrightText"/> <input type="checkbox" class="bulkEditCheck" value="copyrightTextCheck"/></td>
							</tr>
							<c:if test="${(target ne 'list') and (target ne 'selfCheck')}">
							<tr>
								<th class="dCase">Comment</th>
								<td class="dCase"><input type="text" style="width:60%;" id="comments" name="comments"/> <input type="checkbox" class="bulkEditCheck" value="commentsCheck"/></td>
							</tr>
							</c:if>
						</tbody>
					</table>
					</form>
				</div>
			</div>
			<div class="btnLayout">
				<span class="btnLayoutSyncPopupBtn">
	            	<input type="button" value="Cancel" class="btnCancel btnColor" onclick="fn.bulkEditClose();"/>
	            	<input type="button" value="Delete" class="btnColor red" onclick="fn.bulkEditDelete();"/>
	            	<input type="button" value="Change" class="btnColor red" onclick="fn.bulkEditChange();"/>
	            </span>
	        </div>
		</div>
	</body>
</html>