package egovframework.example.sample.web;

import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.SampleDefaultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ResponseBody;

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

	// 🔥 Phase 50: (수정) 뷰 리졸버 무시하고 JSON 텍스트 강제 출력
	@RequestMapping(value = "/api/forge/liveData.do", produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String getForgeLiveData() throws Exception {
		// 1. DB에서 데이터 가져오기
		List<?> list = sampleService.selectForgeList();

		// 2. Jackson ObjectMapper로 직접 JSON 문자열로 변환
		com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		String jsonResult = mapper.writeValueAsString(list);

		// 3. 뷰 리졸버 거치지 않고 순수 문자열 반환
		return jsonResult;
	}
}