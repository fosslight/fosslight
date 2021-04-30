<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div>
		<!---->
		<div class="tbws1 w1025">
				<table class="dCase">
					<colgroup>
						<col width="188" />
						<col />
					</colgroup>
					<tbody>
						<tr>
							<th class="dCase txStr">License Name</th>
							<td class="dCase">
								<div class="required">
									<div id="licenseName" class="viewLicenseTd" >${licenseInfo.licenseName}</div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase txStr">License Type</th>
							<td class="dCase">
								<div class="required">
									<div id="licenseType" class="viewLicenseTd" >${ct:getCodeString(ct:getConstDef('CD_LICENSE_TYPE') ,licenseInfo.licenseType)}</div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase">Restriction</th>
							<td class="dCase">
								<div id="restriction" class="viewLicenseTd" ></div>
							</td>
						</tr>
						<tr>
							<th class="dCase">Obligation</th>
							<td class="dCase">
								<div id="obligation" class="viewLicenseTd" ></div>
							</td>
						</tr>
						<tr>
							<th class="dCase">SPDX Short Identifier</th>
							<td class="dCase">
								<div id="shortIdentifier" class="viewLicenseTd" >${licenseInfo.shortIdentifier}</div>
							</td>
						</tr>
						<tr>
							<th class="dCase">Nick Name</th>
							<td class="dCase">
								<div class="multiTxtSet">	
									<div class="required">						
										<div id="nickNames" class="viewLicenseTd" ></div>	
									</div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase">Web site for the license</th>
							<td class="dCase">
								<div id="webpage" class="viewLicenseTd" ><a href="" class="urlLink" target="_blank"></a></div>
							</td>
						</tr>
						<tr>
							<th class="dCase">User Guide</th>
							<td class="dCase"><div id="description" class="viewLicenseTd viewLicenseTextArea">${licenseInfo.description}</div></td>
						</tr>
						<tr>
							<th class="dCase txStr">License Text</th>
							<td class="dCase">
								<div class="required">
									<div id="licenseText" class="viewLicenseTd viewLicenseTextArea"></div>
								</div>
							</td>
						</tr>
						<tr>
							<th class="dCase">Attribution</th>
							<td class="dCase"><div id="attribution" class="viewLicenseTd viewLicenseTextArea"></div></td>
						</tr>
					</tbody>
				</table>
		</div>

	</div>
	<!---->
</div>
<!-- //wrap --> 