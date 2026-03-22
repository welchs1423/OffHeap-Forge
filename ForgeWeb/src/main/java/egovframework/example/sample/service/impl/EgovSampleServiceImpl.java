package egovframework.example.sample.service.impl;

import java.util.List;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import egovframework.example.sample.service.EgovSampleService;
import egovframework.example.sample.service.ForgeVO;
import egovframework.example.sample.service.SampleVO;
import egovframework.example.sample.service.SampleDefaultVO;
import org.springframework.beans.factory.annotation.Autowired;

@Service("sampleService")
public class EgovSampleServiceImpl extends EgovAbstractServiceImpl implements EgovSampleService {

	@Autowired
	private SampleMapper sampleDAO;

	@Override
	public List<?> selectForgeList() throws Exception {
		return sampleDAO.selectForgeList();
	}

	@Override
	public String insertSample(SampleVO vo) throws Exception { return null; }

	@Override
	public void updateSample(SampleVO vo) throws Exception { }

	@Override
	public void deleteSample(SampleVO vo) throws Exception { }

	@Override
	public SampleVO selectSample(SampleVO vo) throws Exception { return null; }

	@Override
	public List<?> selectSampleList(SampleDefaultVO searchVO) throws Exception { return null; }

	@Override
	public int selectSampleListTotCnt(SampleDefaultVO searchVO) {
		return 0;
	}
}