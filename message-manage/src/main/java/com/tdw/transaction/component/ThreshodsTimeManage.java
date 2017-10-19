package com.tdw.transaction.component;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ThreshodsTimeManage {

	@Value("${presend.back.threshods}")
	String preSendBackThreshodstr;

	@Value("${result.back.threshods}")
	String resultBackThreshodstr;

	@Value("${send.threshods}")
	String sendThreshodstr;

	/**
	 * 获取预发送回调接口，调用时间算法； 按照配置位，相应延迟多少秒
	 * 
	 * @param currentThreshods
	 * @return
	 */
	public Date createPreSendBackTime(int currentThreshods) {
		if (currentThreshods < 1) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		String[] preSendBackThreshods = preSendBackThreshodstr.split(",");
		if (currentThreshods > preSendBackThreshods.length) {
			calendar.add(Calendar.SECOND, Integer.valueOf(preSendBackThreshods[preSendBackThreshods.length - 1]));
			return calendar.getTime();
		} else {
			calendar.add(Calendar.SECOND, Integer.valueOf(preSendBackThreshods[currentThreshods - 1]));
			return calendar.getTime();
		}
	}

	/**
	 * 获取结果回调接口，调用时间算法； 按照配置位，相应延迟多少秒
	 * 
	 * @param currentThreshods
	 * @return
	 */
	public Date createResultBackTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, Integer.valueOf(resultBackThreshodstr));
		return calendar.getTime();
	}
	

	/**
	 * 获取发送MQ，调用时间算法； 按照配置位，相应延迟多少秒
	 * 
	 * @param currentThreshods
	 * @return
	 */
	public Date createSendNextTime(int currentThreshods) {
		if (currentThreshods < 1) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		String[] threshods = sendThreshodstr.split(",");
		if (currentThreshods > threshods.length) {
			calendar.add(Calendar.SECOND, Integer.valueOf(threshods[threshods.length - 1]));
			return calendar.getTime();
		} else {
			calendar.add(Calendar.SECOND, Integer.valueOf(threshods[currentThreshods - 1]));
			return calendar.getTime();
		}
	}

}
