<!DOCTYPE html>
<html lang="ko">
	<head>
		<meta charset="utf-8" />
		<meta http-equiv="X-UA-Compatible" content="IE=edge" />
		<title>FOSSLight Hub</title>
		<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
		<link rel="stylesheet" type="text/css" href="http://www.ok-soft-gmbh.com/jqGrid/jquery.jqGrid-4.4.1/css/ui.jqgrid.css" />
		<link rel="stylesheet" type="text/css" href="/css/common.css" />
		<script type="text/javascript" src="//code.jquery.com/jquery-1.11.3.min.js"></script>
		<script type="text/javascript" src="//code.jquery.com/jquery-migrate-1.2.1.min.js"></script>
		<script type="text/javascript" src="//code.jquery.com/ui/1.11.3/jquery-ui.min.js"></script>
		<script type="text/javascript" src="http://www.ok-soft-gmbh.com/jqGrid/jquery.jqGrid-4.4.1/js/i18n/grid.locale-en.js"></script>
		<script type="text/javascript">
			$.jgrid.no_legacy_api = true;
			$.jgrid.useJSON = true;
		</script>
		<script type="text/javascript" src="http://www.ok-soft-gmbh.com/jqGrid/jquery.jqGrid-4.4.1/js/jquery.jqGrid.src.js"></script>
		<script type="text/javascript" src="/js/basic.js"></script>
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
				<hr/>
				<!-- container -->
				<div id="section">
					<div class="sectionBack">
						<div id="nav-tabs" class="container">
							<!---->
							<div class="contentsFix">
								<div class="contentsFixBack">
									<div class="tabMenu">
										<ul class="nav-tab-menu">
											<li>
												<span><a href="#tabs-1">License List</a></span>
												<input type='button' value='x' class='ui-icon ui-icon-close' />
											</li>
										</ul>
									</div>
								</div>
							</div>
							<!---->
							<div class="contents">
								<div id="tabs-1" class="contentsBack">
									<!----->
									<fieldset class="listSearch">
										<form>
											<dl class="basicSearch col2">
												<dt>Basic Search Area</dt>
												<dd>
													<label>License Name</label>
													<input type="text" class="autoCom" placeholder="autocomplete" />
												</dd>
												<dd class="textArea">
													<label>License Text</label>
													<textarea></textarea>
												</dd>
												<dd>
													<label>License Type</label>
													<span class="selectSet">
														<strong title="License type selected value">select item</strong>
														<select>
															<option>select item 1</option>
															<option>select item 2</option>
														</select>
													</span>
												</dd>
											</dl>
											<dl class="adminSearch">
												<dt><strong>Search Area for Admin</strong><span class="checkSet"><input type="checkbox" id="c1" /><label for="c1">Expand</label></span></dt>
												<dd>
													<label>Creator</label>
													<input type="text" />
												</dd>
												<dd>
													<label>Created date</label>
													<input type="text" class="cal" title="Search Start Date" /> ~ 
													<input type="text" class="cal" title="Search End Date" /> 
												</dd>
												<dd>
													<label>Modifier</label>
													<input type="text" />
												</dd>
												<dd>
													<label>Modified date</label>
													<input type="text" class="cal" title="Search Start Date" /> ~ 
													<input type="text" class="cal" title="Search End Date" /> 
												</dd>
											</dl>
											<input type="submit" value="Search" class="btnColor search" />
										</form>
									</fieldset>
									<!----->
									<div class="btnLayout">
										<span class="right">
											<a href="#" class="btnSet excel" onclick="setModifyRowData()"><span>Excel download</span></a>
											<a href="" class="btnColor">Add</a>
										</span>
									</div>
									<!----->
									<div class="jqGridSet">
										<table id="list"><tr><td></td></tr></table>
										<div id="pager"></div>
									</div>
									<!----->
									<div class="btnLayout">
										<span class="right">
											<a href="#" class="btnSet excel" onclick="setModifyRowData()"><span>Excel download</span></a>
											<a href="#" class="btnColor" onclick="addRow()">Add</a>
										</span>
									</div>
									<!----->
								</div>
							</div>
							<!---->
						</div>
					</div>
				</div>
				<!-- //container -->
			</div>
		</div>
		<!-- //wrap -->
		<style>
			.jqGridSet .ui-state-default, 
			.jqGridSet .ui-widget-content .ui-state-default, 
			.jqGridSet .ui-widget-header .ui-state-default {
			    border: 1px solid #cbc7bd;
			    background: #f5f4f3;
			    font-weight: normal;
			    color: #555555;
			}
			.jqGridSet .ui-jqgrid {overflow:hidden;}
			.jqGridSet .ui-jqgrid .ui-jqgrid-view {border-top-left-radius:4px;border-top-right-radius:4px;overflow:hidden;}
			.ui-jqgrid .ui-jqgrid-htable th div {padding:2px 0 0 0;font-size:11px;font-weight:bold;color:#454545;}
			.ui-icon-triangle-1-s {margin-left:-1px;}
			.ui-jqgrid .ui-pg-input {height:17px;}
		</style>
	</body>
</html>