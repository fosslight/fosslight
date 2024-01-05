<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<%-- 관리자 화면 템플릿 --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
		<script type="text/javascript">
		var fn = {
			bulkEditChange : function(){
				alertify.confirm('<spring:message code="msg.oss.change.bulkedit"/>', function (e) {
					if (e){
						$(".loading").show();
						var obj = $("#secBulkEditForm").serializeObject();
						
						setTimeout(function(){
							opener.sec_com_fn.bulkEditDataInfo(obj, "mod");
							$(".loading").hide();
							self.close();
						}, 300);
					} else {
						return false;
					}
				});
			},
			bulkEditReset : function(){
				alertify.confirm('<spring:message code="msg.project.security.check.reset"/>', function (e) {
					if (e){
						$(".loading").show();
						
						$("select[name=vulnerabilityResolution]").val("Unresolved").prop("selected", true);
						var obj = $("#secBulkEditForm").serializeObject();
						
						setTimeout(function(){
							opener.sec_com_fn.bulkEditDataInfo(obj, "reset");
							$(".loading").hide();
							self.close();
						}, 300);
					} else {
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
			<span style="text-align:center;"><h1 style="font-size:16px;">Bulk Edit Security Attributes</h1></span>
			<div align="center" style="padding-top:10px;">
				<div class="tbws1" style="width:90%;">
					<form name="secBulkEditForm" id="secBulkEditForm">
					<table class="dCase">
						<input type="hidden" id="rowId" name="rowId" value="${rowId}"/>
						<input type="hidden" id="gridId" name="gridId" value="${gridId}"/>
						<input type="hidden" id="target" name="target" value="${target}"/>
						<colgroup>
							<col width="35%"/>
							<col/>
						</colgroup>
						<tbody>
							<tr>
								<th class="dCase" style="text-align:center;">Attributes</th>
								<th class="dCase" style="text-align:center;">Contents</th>
							</tr>
							<tr>
								<th class="dCase">Vulnerability Resolution</th>
								<td class="dCase">
									<select name="vulnerabilityResolution" style="width:60%;">
										<option value="Unresolved">Unresolved</option>
										<option value="Fixed">Fixed</option>
									</select>
								</td>
							</tr>
						</tbody>
					</table>
					</form>
				</div>
			</div>
			<div class="btnLayout">
				<span class="btnLayoutSyncPopupBtn">
	            	<input type="button" value="Cancel" class="btnCancel btnColor" onclick="fn.bulkEditClose();"/>
	            	<input type="button" value="Reset" class="btnColor red" onclick="fn.bulkEditReset();"/>
	            	<input type="button" value="Change" class="btnColor red" onclick="fn.bulkEditChange();"/>
	            </span>
	        </div>
		</div>
	</body>
</html>