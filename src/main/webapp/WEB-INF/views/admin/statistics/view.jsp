<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<c:set var="now" value="<%=new java.util.Date()%>" />
<fmt:formatDate value="${now}" var="startDate" pattern="yyyy0101" />
<fmt:formatDate value="${now}" var="endDate" pattern="yyyy1231" /> 
<div id="wrapIframe">
	<!---->
	<div class="statisticsLayout">
		<input type="hidden" id="startDate" value="${startDate}">
		<input type="hidden" id="endDate" value="${endDate}">
		<form id="chartForm">
	    	<!---->
			<div class="kindArea">
				<div class="topBtn">
					<fieldset class="listSearch">
						<dl class="basicSearch col3">
							<dt>Basic Search Area</dt>
							<dd>
								<label>search Date</label>
								<input name="schStartDate" id="schStartDate" type="text" class="cal" title="Search Start Date" value="${startDate}" maxlength="8"/> ~ 
								<input name="schEndDate" id="schEndDate" type="text" class="cal" title="Search End Date" value="${endDate}" maxlength="8"/>
							</dd>
						</dl>
						<input id="schStatistics" type="submit" value="Search" class="btnColor search" />
					</fieldset>
					<a href="#none" class="btnSet excel" onclick="chart_fn.downloadExcel()"><span>현재 화면 Excel download</span></a>
				</div>
				<c:if test="${projectFlag}">
					<h2>Project related</h2>
					<div class="selectTerm">
						<h3>Divisional Project</h3>
						<span class="right">
							<span class="selectSet">
								<strong for="divisionalProjectChartSelect" title="selected value">Status</strong>
								<select id="divisionalProjectChartSelect" name="divisionalProjectChartSelect">
									<option value="STT" selected>Status</option>
									<option value="REV">Reviewer</option>
									<option value="DST">Distribution Type</option>
								</select>
							</span>
						</span>
					</div>
					<div class="graphArea ga01">
						<div id="divisionalProjectChart"></div>
					</div>
				</c:if>
			</div>
		    <!---->
			<div class="kindArea">
				<h2>OSS/License related</h2>
				<div class="grdSet first">
					<div class="grdItem">
						<!---->
						<div class="selectTerm">
							<h3>Most Used OSS</h3>
								<span class="right">
									<span class="selectSet w170">
										<strong for="mostUsedOssChartDivision" title="selected value">전체</strong>
										<select id="mostUsedOssChartDivision" name="mostUsedOssChartDivision">
											<option value="">전체</option>
											${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
										</select>
									</span>
									<span class="selectSet w50">
										<strong for="mostUsedOssChartPieSize" title="selected value">10</strong>
										<select id="mostUsedOssChartPieSize" name="mostUsedOssChartPieSize">
											<option value="10">10</option>
											<option value="30">30</option>
											<option value="50">50</option>
										</select>
									</span>
								</span>
						</div>
						<div class="graphArea ga02">
							<div id="mostUsedOssChart"></div>
						</div>
						<!--//-->
					</div>
					<div class="grdItem">
						<!---->
						<div class="selectTerm">
							<h3>Most Used License</h3>
							<span class="right">
								<span class="selectSet w170">
									<strong for="mostUsedLicenseChartDivision" title="selected value">전체</strong>
									<select id="mostUsedLicenseChartDivision" name="mostUsedLicenseChartDivision">
										<option value="">전체</option>
										${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
									</select>
								</span>
								<span class="selectSet w50">
									<strong for="mostUsedLicenseChartPieSize" title="selected value">10</strong>
									<select id="mostUsedLicenseChartPieSize" name="mostUsedLicenseChartPieSize">
										<option value="10">10</option>
										<option value="30">30</option>
										<option value="50">50</option>
									</select>
								</span>
							</span>
						</div>
						<div class="graphArea ga03">
							<div id="mostUsedLicenseChart"></div>
						</div>
						<!--//-->
					</div>
				</div>
			</div>
			<!---->
			<div class="kindArea">
				<div class="selectTerm">
					<h3>Updated OSS</h3>
					<span class="right"></span>
				</div>
				<div class="graphArea ga04">
					<div id="updatedOssChart"></div>
				</div>
			</div>
		    <!---->
			<div class="kindArea">
				<div class="selectTerm">
					<h3>Updated License</h3>
					<span class="right"></span>
				</div>
				<div class="graphArea ga05">
					<div id="updatedLicenseChart"></div>
				</div>
			</div>
		    <!---->
		    <c:if test="${partnerFlag}">
				<div class="kindArea">
					<h2>3rd Party related</h2>
					<div class="selectTerm">
						<h3>기간별 생성된 3rd party 수</h3>
						<span class="right">
							<span class="selectSet">
								<strong for="trdPartyRelatedChartSelect" title="selected value">Status</strong>
								<select id="trdPartyRelatedChartSelect" name="trdPartyRelatedChartSelect">
									<option value="STT" selected>Status</option>
									<option value="REV">Reviewer</option>
								</select>
							</span>
						</span>
					</div>
					<div class="graphArea ga06">
						<div id="trdPartyRelatedChart"></div>
					</div>
				</div>
			</c:if>
		    <!---->
			<div class="kindArea">
				<h2>User related</h2>
				<div class="selectTerm">
					<h3>조직 별 user 수</h3>
					<span class="right"></span>
				</div>
				<div class="graphArea ga07">
					<div id="userRelatedChart"></div>
				</div>
			</div>
		    <!--//-->
		</form>
	</div>
	<!---->
</div>
<!-- //wrap -->