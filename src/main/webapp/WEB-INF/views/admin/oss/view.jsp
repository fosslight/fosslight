<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!-- wrap -->
<div id="wrapIframe">
	<div>
		<div class="tbws1 w1025">
			<!-- Main Table [S] -->
			<table class="dCase">
				<colgroup>
					<col width="188"/>
					<col/>
				</colgroup>
				<tbody>
					<tr>
						<th class="dCase txStr">OSS Name</th>
						<td class="dCase">
							<div class="required">
								<div id="ossName" class="viewOssTd"></div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase">Nick Name</th>
						<td class="dCase">
							<div class="multiTxtSet">
								<div class="required">
									<div id="nickNames" class="viewOssTd" ></div>
								</div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase">OSS Version</th>
						<td class="dCase">
							<div class="required">
								<div id="ossVersion" class="viewOssTd" ></div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase txStr">Declared License<br><input type="button" id="btnShowLicenseText" value="Show license text" class="btnCLight gray"></th>
						<td class="dCase">
							<div class="required">
								<div class="licenseMulti">
									<div class="mark"></div>
									<div class="mt5"><table id="_licenseChoice"><tr><td></td></tr></table></div>
								</div>
							</div>
							<input type="hidden" id="licenseName" />
							<div id="disp_licenseText" style="display: none;"></div>
						</td>
					</tr>
					<tr>
						<th class="dCase">Detected License</th>
						<td class="dCase">
							<div class="multiItemSet multiDetectedLicenseSet">
								<div class="required">
									<div name="detectedLicenses" class="viewOssTd" ></div>
								</div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase">Copyright</th>
						<td class="dCase">
							<div id="Copyright" class="viewOssTd viewOssTextArea">${copyright}</div>
						</td>
					</tr>
					<tr id="lt">
						<th class="dCase">License Type</th>
						<td class="dCase"><div id="licenseType" class="viewOssTd" ></div></td>
					</tr>
					<tr id="ob">
						<th class="dCase">Obligation</th>
						<td class="dCase"><div id="obligation" class="viewOssTd" ></div></td>
					</tr>
					<tr>
						<th class="dCase">Download Location</th>
						<td class="dCase">
							<div class="multiItemSet multiDownloadLocationSet">
								<div class="required">
									<div name="downloadLocations" class="viewOssTd" ><a href="" class="urlLink" target="_blank"></a></div>
								</div>
							</div>
						</td>
					</tr>
					<tr>
						<th class="dCase">Home Page</th>
						<td class="dCase">
							<div id="homepage" class="viewOssTd"><a href="" class="urlLink" target="_blank"></a></div>
						</td>
					</tr>
					<tr>
						<th class="dCase">Summary Description</th>
						<td class="dCase"><div id="summaryDescription" class="viewOssTd viewOssTextArea" ></div></td>
					</tr>
					<tr>
						<th class="dCase">Attribution</th>
						<td class="dCase"><div id="attribution" class="viewOssTd viewOssTextArea" ></div></td>
					</tr>
					<c:if test="${!empty ossId}">
					<tr>
						<th class="dCase">Project<br/><input id="listMore" type="button" value="List more" class="btnCLight gray" /></th>
						<td class="dCase">
							<table id="_projectList"><tr><td></td></tr></table>
						</td>
					</tr>
						<c:if test="${not empty vulnInfoList}">
							<tr>
								<th class="dCase">Vulnerability</th>
								<td class="dCase">
									<table id="_vulnInfoList"><tr><td></td></tr></table>
								</td>
							</tr>
						</c:if>
					</c:if>
				</tbody>
			</table>
			<!-- Main Table [E] -->
		</div>
	</div>
	<!---->
</div>
<!-- //wrap -->