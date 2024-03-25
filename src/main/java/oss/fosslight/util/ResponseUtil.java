package oss.fosslight.util;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import oss.fosslight.common.response.CommonResult;
import oss.fosslight.common.response.ListResult;
import oss.fosslight.common.response.SingleResult;

public class ResponseUtil {

	public static void DefaultAlertAndGo(HttpServletResponse response, String msg, String url) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf("<script>alert('%s');window.parent.location.href='%s';</script>", msg, url);
	}

	public static void DefaultalertAndBack(HttpServletResponse response, String msg) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf("<script>alert('%s');history.go(-1);</script>", msg);
	}

	public static void DefaultbasicAlert(HttpServletResponse response, String msg) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf("<script>alert('%s');</script>", msg);
	}

	public static void alertAndGo(HttpServletResponse response, String msg, String url) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf("<script>util.alert('%s', function(){location.href='%s';});</script>", msg, url);
	}
	
	public static void alertAndReload(HttpServletResponse response, String msg) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf("<script>util.alert('%s', function(){location.reload();});</script>", msg);
	}

	public static void alert(HttpServletResponse response, String msg) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf("<script>util.alert('%s');</script>", msg);
	}

	public static void toast(HttpServletResponse response, String msg, boolean isSuc) throws IOException {
			response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf("<script>util.toast('%s', %b);</script>", msg, isSuc);
	}
	
	public static void toastAndGo(HttpServletResponse response, String msg, boolean isSuc, String url) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf("<script>util.toast('%s', %b, function(){location.href='%s';});</script>", msg, isSuc, url);
	}

	public static void redirect(HttpServletResponse response, String url) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().printf("<script>window.parent.location.href='%s';</script>", url);
	}

	public static void write(HttpServletResponse response, String contents) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.getWriter().print(contents);
	}

	public static void writeJson(HttpServletResponse response, String contents) throws IOException {
		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().print(contents);
	}

	public static void wrapHtml(HttpServletResponse response, String contents) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		StringBuffer sb = new StringBuffer();
		sb.append("<html>");
		sb.append("<body>");
		sb.append("<head>");
		sb.append("<meta charset=\"utf-8\">");
		sb.append("<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\" />");
		sb.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\" />");
		sb.append("<link href=\"/hpms/fonts/nanumbarungothic.css\" rel=\"stylesheet\" type=\"text/css\" />");
		sb.append(
				"<style>*{ font-family: 'Nanum Barun Gothic','돋움', '굴림', Dotum, Gulim, sans-serif;  font-size:13px; }</style>");
		sb.append("<script src=\"/hpms/js/jquery-2.0.2.min.js\"></script>");
		sb.append("<script>$(window).load(function(){parent.resizeContents();});</script>");
		sb.append("</head>");
		sb.append(contents);
		sb.append("</body>");
		sb.append("</html>");
		response.getWriter().print(sb.toString());
	}

	// enum으로 api 요청 결과에 대한 code, message를 정의합니다.
	public static enum CommonResponse {

		/** The success. */
		SUCCESS("100", "");

		/** The code. */
		String code;

		/** The msg. */
		String msg;

		CommonResponse(String code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public String getCode() {
			return code;
		}

		public String getMsg() {
			return msg;
		}
	}

	// 단일건 결과를 처리하는 메소드
	public static <T> SingleResult<T> getSingleResult(T data) {
		SingleResult<T> result = new SingleResult<>();
		result.setData(data);
		setSuccessResult(result);
		return result;
	}

	// 다중건 결과를 처리하는 메소드
	public static <T> ListResult<T> getListResult(List<T> list) {
		ListResult<T> result = new ListResult<>();
		result.setList(list);
		setSuccessResult(result);
		return result;
	}

	// 성공 결과만 처리하는 메소드
	public static CommonResult getSuccessResult() {
		CommonResult result = new CommonResult();
		setSuccessResult(result);
		return result;
	}

	// 실패 결과만 처리하는 메소드
	public static CommonResult getFailResult(String code, String msg) {
		CommonResult result = new CommonResult();
		result.setSuccess(false);
		result.setCode(code);
		result.setMsg(msg);
		return result;
	}

	// 결과 모델에 api 요청 성공 데이터를 세팅해주는 메소드
	private static void setSuccessResult(CommonResult result) {
		result.setSuccess(true);
		result.setCode(CommonResponse.SUCCESS.getCode());
		result.setMsg(CommonResponse.SUCCESS.getMsg());
	}
}
