<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<jsp:include page="../common/gridCommonFn.jsp" flush="false" />
<div id="wrapIframe">
	<!---->
	<div>
		<fieldset class="listSearch">
            <form id="bomcompareSearch" >
                <dl class="basicSearch col2">
                    <dt>Basic Search Area</dt>
                    <dd>
                        <label>Before Project id</label>
                        <input id="beforePrjId" name="beforePrjId" type="text" onKeyup="this.value=this.value.replace(/[^0-9]/g,'')"/>
                    </dd>
                    <dd>
                        <label>After Project id</label>
                        <input id="afterPrjId" name="afterPrjId" type="text" onKeyup="this.value=this.value.replace(/[^0-9]/g,'')"/>
                    </dd>
                </dl>
                <input id="search" type="button" value="Search" class="btnColor search" />
            </form>
        </fieldset>
		<!---->
		<!---->
		<div class="btnLayout">
			<span class="right">
				<a href="#none" class="btnSet excel" onclick="fn.downloadExcel()"><span>Excel download</span></a>
			</span>
		</div>
		
		<!---->
		<div class="jqGridSet">
			<table id="list"><tr><td></td></tr></table>
			<div id="pager"></div>
		</div>
		<!---->
	</div>
	<!---->
</div>
