package oss.fosslight.controller;

import java.lang.reflect.Type;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import oss.fosslight.CoTopComponent;
import oss.fosslight.common.Url;
import oss.fosslight.domain.BinaryData;
import oss.fosslight.service.BinaryDataService;

@RequestMapping("/binary")
@Controller
@Slf4j
public class BinaryDataController extends CoTopComponent  {
	// Service
	@Autowired BinaryDataService binaryDataService;
	
	/**
	 * Bianry DB
	 */
	@GetMapping(value="", produces = "text/html; charset=utf-8")
	public String index(HttpServletRequest req, HttpServletResponse res, Model model){
		log.debug(" :: Start bat");
		model.addAttribute("searchBean", new BinaryData());

		return "binary/list :: content";
	}
	
	/**
	 * Select Binary DB list
	 */
	@GetMapping(value="/listAjax")
	public @ResponseBody ResponseEntity<Object> listAjax(
			@ModelAttribute BinaryData vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model){
		
		String page = req.getParameter("page");
		String rows = req.getParameter("rows");
		
		return makeJsonResponseHeader(binaryDataService.getBinaryList(page, rows , vo));
	}

	/**
	 * Change Binary DB data
	 * @param vo
	 * @param req
	 * @param res
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/modAjax")
	public @ResponseBody ResponseEntity<Object> binaryMod (
			@ModelAttribute BinaryData vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		boolean multiFlag = !isEmpty(vo.getParameter());
		
		if(multiFlag) {
			Type batType = new TypeToken<List<BinaryData>>(){}.getType();
			List<BinaryData> batList = (List<BinaryData>) fromJson(vo.getParameter(), batType);
			
			binaryDataService.setBinaryDataListModify(batList); // multi row 선택시 동작한 신규 service
		}else {
			binaryDataService.setBinaryDataModify(vo); // 기존 service
		}
		
		return makeJsonResponseHeader();
	}
	
	@GetMapping(value="/existBinaryName")
	public @ResponseBody ResponseEntity<Object> existBinaryName (
			@ModelAttribute BinaryData vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		return makeJsonResponseHeader(binaryDataService.getExistBinaryName(vo));
	}
	
	@GetMapping(value="/binarypopup")
	public String binarypopup (
			@ModelAttribute BinaryData vo
			, HttpServletRequest req
			, HttpServletResponse res
			, Model model) throws Exception{
		
		model.addAttribute("batInfo", vo);
		return Url.TILES_ROOT + "/binary/binarypopup";
	}	
}
