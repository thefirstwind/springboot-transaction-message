package com.tdw.transaction.component;

import java.util.Date;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(
	    properties = { "presend.back.threshods=1200,600,60,30,10,8","result.back.threshods=30" }
	)
public class ThreshodsTimeManageTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ThreshodsTimeManageTest.class);
	

    @Value("${presend.back.threshods}")
    String preSendBackThreshodstr;
    
    @Autowired
    ThreshodsTimeManage threshodsTimeManage;
    
    /**
     * 检测配置是否生效；
     */
	@Test
	public void assetValue() {
		logger.info("===============================  {} =================================" , preSendBackThreshodstr);
		Assertions.assertThat(preSendBackThreshodstr).isEqualTo("1200,600,60,30,10,8");
	}
	

	/**
	 * 检测创建预发送时间，是否符合期望
	 */
	@Test
	public void createPreSendBackTime() {
		Date pdt = new Date();
		Date dt = threshodsTimeManage.createPreSendBackTime(10);
		//logger.info("===============1========= {} =======  {} =================================" , pdt.toGMTString(), dt.toGMTString());
		Assertions.assertThat((dt.getTime() - pdt.getTime()) / 1000).isGreaterThan(6);

		dt = threshodsTimeManage.createPreSendBackTime(3);
		//logger.info("===============2========= {} =======  {} =================================" , pdt.toGMTString(), dt.toGMTString());
		Assertions.assertThat((dt.getTime() - pdt.getTime()) / 1000).isGreaterThan(58);
	}
	


	/**
	 * 检测创建结果回调时间，是否符合期望
	 */
	@Test
	public void createResultBackTime() {
		Date pdt = new Date();
		Date dt = threshodsTimeManage.createResultBackTime();
		//logger.info("===============1========= {} =======  {} =================================" , pdt.toGMTString(), dt.toGMTString());
		Assertions.assertThat((dt.getTime() - pdt.getTime()) / 1000).isGreaterThan(28);

	}
}
