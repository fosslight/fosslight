<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ include file="/WEB-INF/constants.jsp"%>
<!-- wrap -->
<div id="wrapIframe">
	<!---->
	<div>
        <fieldset class="listSearch">
            <form id="userSearch" >
                <dl class="basicSearch col2">
                    <dt>Basic Search Area</dt>
                    <dd>
                        <label>User Name</label>
                        <input id="userName" name="userName" type="text"/>
                    </dd>
                    <dd>
                        <label>User Id</label>
                        <input id="userId" name="userId" type="text"/>
                    </dd>
                    <dd>
                        <label>Division</label>
	                    <select id="division" name="division">
                            <option value=""></option>
                            ${ct:genOption(ct:getConstDef("CD_USER_DIVISION"))}
                        </select>
                    </dd>
                </dl>
                <input id="search" type="button" value="Search" class="btnColor search" />
            </form>
        </fieldset>
		<!---->
		<div class="btnLayout">
			<span class="right">
				<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Excel download</span></a>
				<a onclick="fn.saveUser()" class="btnColor red btnUser">Save</a>
			</span>
		</div>
		<!---->
		<div class="jqGridSet">
			<table id="list"><tr><td></td></tr></table>
			<div id="pager"></div>
		</div>
	</div>
	<!---->

</div>
<!-- //wrap -->