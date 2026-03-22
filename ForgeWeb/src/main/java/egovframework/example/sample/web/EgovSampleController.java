package egovframework.example.sample.web;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleDefaultVO;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class EgovSampleController {

	@Autowired
	private EgovSampleService sampleService;

	@RequestMapping(value = "/egovSampleList.do")
	public String selectSampleList(SampleDefaultVO searchVO, ModelMap model) throws Exception {
		// 🔥 기존 SAMPLE 테이블 조회 로직은 주석 처리 (에러 방지)
		// model.addAttribute("resultList", sampleService.selectSampleList(searchVO));

		// 🔥 우리 오프힙 엔진 데이터만 쏙 담기
		model.addAttribute("forgeList", sampleService.selectForgeList());

		return "sample/egovSampleList";
	}
}