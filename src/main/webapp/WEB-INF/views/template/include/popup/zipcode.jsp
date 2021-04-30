<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<!-- 우편번호 팝업 -->
<div class="pop zip-pop" style="display:none;">
	<h1><spring:message code="comm.label.address-search" text="주소 검색" /></h1>
	<img src="<c:url value="/images/btn_pclose.png"/>" alt="Close" class="pclose" onclick="ZipAPI.close();"/>
	<div class="popdata">
		<!-- 우편번호 검색 -->
		<div class="code_area">
			<fieldset>
				<legend><spring:message code="comm.label.address-name-search" text="주소명 검색" /></legend>
				<input type="text" id="src_adr" name="q" placeholder="<spring:message code="comm.label.address-name" text="주소명" />"  value="" onkeypress="if(event.keyCode === 13) document.querySelectorAll('.srh')[0].click()"/>
				<input type="hidden" id="src_adr_pagenum" name="p"/>
				<input type="hidden" id="src_adr_totalPageNum" />
				<a onclick="ZipAPI.search()" class="srh" style="cursor:pointer;"><spring:message code="comm.label.search" text="검색" /></a>
			</fieldset>
			<strong class="mt10">* <spring:message code="comm.msg.address-info-message1" text="도로명, 건물명, 지번, 사서함에 대해 통합검색이 가능합니다." /></strong>
			<div class="databox-line mt10">
				<div class="box">
					<table class="tbvs1" style="border:0;border-spacing:0;border-collapse:collapse;">
						<colgroup>
							<col />
							<col />
						</colgroup>
						<tbody>
							<tr>
								<td><spring:message code="comm.label.road-name" text="도로명" /> + <spring:message code="comm.label.build-number" text="건물번호" /> <span><spring:message code="comm.msg.address-example-message1" text="예) 종로 6" /></span></td>
								<td><spring:message code="comm.msg.address-info-message3" text="읍/면/동/리 + 지번" /> <span><spring:message code="comm.msg.address-example-message2" text="예) 서린동 154-1" /></span></td>
							</tr>
							<tr>
								<td><spring:message code="comm.label.build-name" text="건물명" /> <span><spring:message code="comm.msg.address-example-message3" text="예) 나주시 OO아파트" /></span></td>
								<td><spring:message code="comm.label.mailbox-and-mailbox-number" text="사서함 + 사서함번호" /> <span><spring:message code="comm.msg.address-example-message4" text="예) 광화문우체국사서함 45" /></span></td>
							</tr>
						</tbody>
					</table>
				</div>
			</div>
			<strong class="mt10">* <spring:message code="comm.msg.address-info-message2" text="우편번호 상세주소가 검색되지 않는 경우 범위 주소로 검색됩니다." /></strong>
		</div>
		<!-- //우편번호 검색 -->
		<!-- 우편번호 리스트 -->
		<div class="result databox-line mt20" style="display:none;">
			<div class="box" id="searchResultBox">
				<p style="display:none;"><spring:message code="comm.msg.domestic-address-only-possible-input" text="국내 주소만 입력이 가능합니다." /></p>
				<table class="tbls1" style="border:0;border-spacing:0;border-collapse:collapse;">
					<colgroup>
						<col width="70" />
						<col />
					</colgroup>
					<thead>
						<tr>
							<th><spring:message code="comm.label.post-number" text="우편번호" /></th>
							<th><spring:message code="comm.label.address2" text="주소"/></th>
						</tr>
					</thead>
					<tbody id="searchResultArea">
						<tr id="msg-loadMore"><td colspan="2"><spring:message code="comm.label.address-searching" text="주소 검색중" />...</td></tr> 
						<!-- Loop -->
					</tbody>
				</table>
			</div>
		</div>
		<!-- //우편번호 리스트 -->
	</div>
	<!-- // tab -->
	<div class="pbtn">
		<span class="btnset cancel"><a><spring:message code="comm.label.close" text="닫기" /></a></span>
	</div>
</div>
<script type="text/javascript">
var ZipAPI = {
		runningMore: false,
		search: function(){
			var $srcAdrPagenum = $('#src_adr_pagenum');
			
			$('#searchResultArea').find('.resultItem').remove();
			$('#searchResultArea').find('#msg-loadMore').hide();
			
			$srcAdrPagenum.val(0);
			ZipAPI.loadData();
		},
		loadData: function(){
			if (ZipAPI.runningMore) {
				 return;
			}
			
			var $srcAdrPagenum = $('#src_adr_pagenum');
			var searchKey = $('#src_adr').val();
			
			if (!$srcAdrPagenum.val()){
				$srcAdrPagenum.val(1);
			} else {
				$srcAdrPagenum.val(parseInt($srcAdrPagenum.val())+1);
			}
			
			if($.trim(searchKey).length == 0) {
				ZipAPI.printMsg('<spring:message code="comm.msg.enter-the-search-value" text="검색어를 입력해 주십시오."/>'); return;
			}
			
			$('#searchResultArea').find('#msg-loadMore').show();
			var param = {q : searchKey, p : $srcAdrPagenum.val()};
			var URL = '<c:url value="/v1/post/search"/>';
			
			ZipAPI.runningMore = true;
			ZipAPI.unbindMoreEvent();
			
			$.ajax({
				url : URL
				, data : param
				, dataType : 'json'
				, success : function(data, textStatus, jqXHR){
					ZipAPI.runningMore = false;
					
					$('#searchResultArea').find('#msg-loadMore').hide();
					
					if(data.statusCode == 1){
						var result = data.data.NewAddressListResponse.newAddressListAreaCdSearchAll;
						
						if(!!result && result.length != 0){
							// 전체 페이지 수
							$('#src_adr_totalPageNum').val(data.data.NewAddressListResponse.cmmMsgHeader.totalPage);
							
							ZipAPI.printAddr(result);
							
							if(data.data.NewAddressListResponse.cmmMsgHeader.totalPage == 1
								|| data.data.NewAddressListResponse.cmmMsgHeader.totalPage == data.data.NewAddressListResponse.cmmMsgHeader.currentPage){
							} else {
								ZipAPI.bindMoreEvent();
							}
						} else {
							ZipAPI.printMsg('<spring:message code="comm.msg.empty-result-value" text="검색결과가 없습니다."/>');
						}
					} else {
						ZipAPI.printMsg('<spring:message code="comm.msg.enter-the-search-value" text="검색어를 입력해 주십시오."/>');
					}
				}
				, error : function(){
					ZipAPI.runningMore = false;
					
					$('#searchResultArea').find('#msg-loadMore').hide();
					
					ZipAPI.printMsg('<spring:message code="comm.msg.request-processing-error" text="요청 처리 중 오류가 발생하였습니다."/>');
				}
			});
		},
		moreFn: function(){
			var elem = $('#searchResultBox');
			
			if ( elem[0].scrollHeight - elem.scrollTop() < 700){
				ZipAPI.loadData();
			}
		},
		unbindMoreEvent: function(){
			$('#searchResultBox').off('scroll', ZipAPI.moreFn);
		},
		bindMoreEvent: function(){
			ZipAPI.unbindMoreEvent();
			
			$('#searchResultBox').on('scroll', ZipAPI.moreFn);
		},
		printMsg: function(msg){
			$('#searchResultArea').append($('<tr/>').addClass('resultItem').append($('<td/>').attr('colspan','2').text(msg)));
		},
		printAddr: function(addr){
			var appendHtml = '';
			
			if(typeof addr['push'] === 'function'){
				appendHtml = [];
				
				for (var idx in addr) {
					appendHtml.push($('<tr/>').addClass('resultItem')
						.append($('<td/>').text(addr[idx].zipNo))
						.append($('<td/>').attr('class','subject').attr('style','cursor:pointer;')
								.append($('<div/>').attr('class','address1').text(addr[idx].lnmAdres))
								.append($('<div/>').attr('class','blue address2').text(addr[idx].rnAdres))));
				}
			} else {
				appendHtml = $('<tr/>').addClass('resultItem')
					.append($('<td/>').text(addr.zipNo))
					.append($('<td/>').attr('class','subject').attr('style','cursor:pointer;')
							.append($('<div/>').attr('class','address1').text(addr.lnmAdres))
							.append($('<div/>').attr('class','blue address2').text(addr.rnAdres)));
			}
			
			$('#msg-loadMore').before(appendHtml);
			
			// 클릭 펑션
			$('td.subject').click(function(){
				var zipCode = $(this).prev().text();
				var address1 = $(this).find('div.address1').text();
				$('#Addr1').val(address1);
				$('#ZipCode').val(zipCode);
				ZipAPI.close();
			});
		},
		close : function(){
			$('div.result').hide();
			$('#searchResultArea').find('.resultItem').remove();
			$('#src_adr').val('');
		}
};
</script>
<!-- //우편번호 팝업 -->