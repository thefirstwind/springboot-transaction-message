package com.tdw.transaction.service.util;

import java.util.Date;

import com.tdw.transaction.domain.OptLog;

public class OptLogsService {

	
	public static OptLog createOptLogs(int bState,int eState,String logstr)
	{
		OptLog log = new OptLog();

		log.setBeginState(bState);
		log.setEndState(eState);
		log.setOptionLog(logstr);
		log.setOptionTime(new Date());
		
		return log;
	}
	
}
