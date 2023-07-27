<%@ include file="/WEB-INF/constants.jsp"%>
<%-- Administrator screen template --%>
<!DOCTYPE html>
<html>
	<head>
		<tiles:insertAttribute name="meta" />
		<tiles:insertAttribute name="scripts" />
	</head>
	<body>
		<!-- skip-navigation -->
		<div class="skip-navigation">
			<p><a href="#container" tabindex="0">본문 바로가기</a></p>
		</div>
		<!-- //skip-navigation -->
		<!-- wrap -->
		<div id="wrap">
			<div id="wrapBack">
				<input type="button" value="<" class="headerHandle" />
				<tiles:insertAttribute name="header" />
				<hr/>
        		<div id="blind_wrap"></div>
				<!-- container -->
				<div id="section">
					<div class="sectionBack">
						<div id="nav-tabs" class="container">
							<!---->
							<div class="contentsFix">
								<div class="contentsFixBack">
									<div class="tabMenu">
										<ul class="nav-tab-menu">
											<li style="display:none;">
												<span></span>
											</li>
										</ul>
									</div>
								</div>
							</div>
							<!---->
							<div class="contents">
							</div>
							<!---->
						</div>
					</div>
				</div>
				<!-- //container -->
			</div>
		</div>
		<!-- //wrap -->
	</body>
</html>
