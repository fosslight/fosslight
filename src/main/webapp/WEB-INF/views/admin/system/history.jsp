<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
		<!-- wrap -->
		<div id="wrapIframe">
			<!---->
			<div>
				<!---->
				<fieldset class="listSearch">
					<form id="historySearch" name="historySearch">
						<dl class="basicSearch col2">
							<dt>Basic Search Area</dt>
							<dd>
								<label>Modify Type</label>
								<span class="selectSet overHidden">
									<strong title="Modify Type selected value"></strong>
									<select name="hType">
										<option value=""></option>
										<option value="300">License</option>
										<option value="301">Open Source</option>
										<option value="303">Project</option>
									</select>
								</span>
							</dd>
							<dd>
								<label>Name</label>
								<input type="text" name="hTitle" class="autoCom" maxlength="150" />
							</dd>
							<dd>
								<label>Modifier</label>
								<input type="text" name="modifier" class="autoCom" maxlength="100" />
							</dd>
							<dd>
								<label>Action</label>
								<span class="selectSet overHidden">
									<strong title="Action Type selected value"></strong>
									<select name="hAction">
										<option value=""></option>
										<option value="INSERT">INSERT</option>
										<option value="UPDATE">UPDATE</option>
										<option value="DELETE">DELETE</option>
									</select>
								</span>
							</dd>
							<dd>
								<label>Modified Date</label>
								<input type="text" name="startDate"  class="cal" title="Search Start Date" maxlength="8"/> ~ 
								<input type="text" name="endDate" class="cal" title="Search End Date" maxlength="8"/> 
							</dd>                                              
						</dl>
						<input type="submit" id="search" value="Search" class="btnColor search" />
					</form>
				</fieldset>
				<!---->
				<div class="jqGridSet mt20">
					<table id="list"><tr><td></td></tr></table>
					<div id="pager"></div>
				</div>
				<!---->
			</div>
			<!---->
		</div>
		<!-- //wrap -->