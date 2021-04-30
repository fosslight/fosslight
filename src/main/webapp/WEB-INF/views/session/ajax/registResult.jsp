<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
{
	"success":"${success}"
	,"userIdErr":<spring:bind path="user.userId">"${status.errorMessage}"</spring:bind>
	,"emailErr":<spring:bind path="user.email">"${status.errorMessage}"</spring:bind>
	,"userPwdErr":<spring:bind path="user.userPwd">"${status.errorMessage}"</spring:bind>
	,"userNmErr":<spring:bind path="user.userNm">"${status.errorMessage}"</spring:bind>
	,"cellPhoneErr":<spring:bind path="user.cellPhone">"${status.errorMessage}"</spring:bind>
	,"condiAgreelErr":<spring:bind path="user.condiAgree">"${status.errorMessage}"</spring:bind>
}
