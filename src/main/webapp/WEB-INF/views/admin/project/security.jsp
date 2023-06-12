<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<jsp:include page="../common/gridCommonFn.jsp" flush="false" />
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div class="projdecTop">
		<div class="projectInfo">
			<h2>Project Information</h2>
			<ul>
				<li class="first"><span>Project</span><strong><label id="prjName"></label>
						<span onclick="sec_com_fn.editTab()" class="btnIcon basic" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Basic Info</span>
						<c:if test="${not empty project.prjId}">
						<span onclick="sec_com_fn.identificationTab()" class="btnIcon identi" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Identification</span>
						</c:if>
						<c:if test="${project.verificationStatus ne 'NA' and (not empty project.verificationStatus or project.identificationStatus eq 'CONF')}">
						<span onclick="sec_com_fn.packagingTab()" class="btnIcon packag" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Packaging</span>
						</c:if>
						<c:if test="${distributionFlag and project.destributionStatus ne 'NA' and (not empty project.destributionStatus or project.verificationStatus eq 'CONF')}">
						<span onclick="sec_com_fn.distributionTab()" class="btnIcon distr" style="display:inline-block;width:16px;padding:0;margin-left:3px;">Distribution</span>
						</c:if>
					</strong>
				</li>
				<li><span>Creator</span><strong><label id="creator"></label></strong></li>
			</ul>
			<a class="right" id="helpLink" style="position:relative; cursor: pointer; top:-37px; right:-75px; display: none;"><img alt="" src="<c:url value="/images/user-guide.png"/>" /></a>
		</div>
		<!---->
		<div class="projdecTab">
			<div class="subTab">
			<div class="tabMenu">
				<a rel="Total">Total</a>
				<a rel="Fixed">Fixed</a>
				<a rel="NotFixed">Not Fixed</a>
			</div>
			</div>
			<input type="button" value="Comment Edit" class="btnColor commentBtn" />
			<span style="right: 125px; position: absolute; bottom: -23px; font-size: 11px;" >
				<input type="button" value="Show Comment History" class="btnColor purple btnCommentHistory" style="width: 160px; height: 18px;"/>
			</span>
		</div>
	</div>
	<div class="commentEditor" style="display:none;">
		<div class="cBtn">
		<input type="button" value="Save & Send comment" class="btnCLight saveEditor" onclick="sec_com_fn.sendEditor('WR');"/>
		<input type="button" value="Save draft" class="btnCLight" onclick="sec_com_fn.saveEditor();"/>
		</div>
		<div class="grid-container">
			<div class="grid-width-100">
				<div id="editor">${project.userComment}</div>
			</div>
		</div>
	</div>
	<div>
		<input type="hidden" name="prjId" value="${project.prjId}"/>
		<div id="Total" class="tabContent">
			<div class="projectContents">
				<div class="btnLayout">
					<span class="right">
						<input type="button" value="Export" class="btnColor red" onclick="total.downloadExcel()" />
						<input type="button" value="Save" class="btnColor red" onclick="total.save()" />
					</span>
				</div>
				<div class="jqGridSet">
					<table id="totalList"><tr><td></td></tr></table>
					<div id="totalListPager"></div>
				</div>
				
			</div>
			<!---->
		</div>
		<div id="Fixed" class="tabContent">
			<div class="projectContents">
				<div class="btnLayout">
					<span class="right">
						<input type="button" value="Export" class="btnColor red" onclick="fixed.downloadExcel()" />
						<input type="button" value="Save" class="btnColor red" onclick="fixed.save()" />
					</span>
				</div>
				<div class="jqGridSet">
					<table id="fixedList"><tr><td></td></tr></table>
					<div id="fixedListPager"></div>
				</div>
			</div>
			<!---->
		</div>
		<div id="NotFixed" class="tabContent">
			<div class="projectContents">
				<div class="btnLayout">
					<span class="right">
						<input type="button" value="Export" class="btnColor red" onclick="notFixed.downloadExcel()" />
						<input type="button" value="Save" class="btnColor red" onclick="notFixed.save()" />
					</span>
				</div>
				<div class="jqGridSet">
					<table id="notFixedList"><tr><td></td></tr></table>
					<div id="notFixedListPager"></div>
				</div>
			</div>
			<!---->
		</div>
	</div>
</div>
<!-- //wrap -->
<div id="blind_wrap"></div>

<c:if test="${not empty userGuideLicenseList}">
<div class="pop warningPop">
	<div class="popdata">
		<p><b><spring:message code="msg.project.packaging.verify.userguide" /></b></p><br/>
		<c:forEach items="${userGuideLicenseList}" var="userGuide">
			<p style="font-style: italic;"><b>${userGuide.licenseName}</b></p>
			<p style="margin-left: 30px;">${userGuide.descriptionHtml}</p><br/>
		</c:forEach>
	</div>
	<div class="pbtn">
		<input type="button" value="OK" class="btnColor red OKcolse" />
	</div>
</div>
</c:if>
