package com.tdw.transaction.component;

import java.util.Calendar;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tdw.transaction.domain.TransactionMessage;
import com.tdw.transaction.util.IdGenerator;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionMongodbComponentTest {

	@Autowired
	private TransactionMessageMongodbTemplate transactionMessageMongodbTemplate;

	@Before
	public void before()
	{
		//to do
	}

	@Test
	public void insert() {
		TransactionMessage transactionMessage = new TransactionMessage();
		transactionMessage.setMessageTopic("test.topic");
		Object jo = JSONObject.stringToValue("{'name':'lily'}");
		String _id = IdGenerator.uuid36();
		transactionMessage.setId(_id);
		transactionMessage.setMessage(jo);
		transactionMessage.setExpectResult("a");
		transactionMessage.setMessageType(0);
		transactionMessage.setMessageState(10);
		
		transactionMessageMongodbTemplate.deleteById(_id);
		transactionMessageMongodbTemplate.insert(transactionMessage);
		Assertions.assertThat(transactionMessageMongodbTemplate.selectById(_id)).isNotNull();
		transactionMessageMongodbTemplate.deleteById(_id);
	}
	

	@Test
	public void findForPresendBack() {
		
		TransactionMessage transactionMessage = new TransactionMessage();
		transactionMessage.setMessageTopic("test.topic");
		Object jo = JSONObject.stringToValue("{'name':'lily'}");
		String _id = IdGenerator.uuid36();
		transactionMessage.setId(_id);
		transactionMessage.setMessage(jo);
		transactionMessage.setExpectResult("a");
		transactionMessage.setMessageType(0);
		transactionMessage.setMessageState(10);
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, -30);
		transactionMessage.setPresendBackNextSendTime(calendar.getTime());

		transactionMessageMongodbTemplate.deleteById(_id);
		transactionMessageMongodbTemplate.insert(transactionMessage);

		calendar.add(Calendar.SECOND, 60);
		
		List<TransactionMessage>  transactionMessageList = transactionMessageMongodbTemplate.findForPresendBack(calendar.getTime(), 10);
		Assertions.assertThat(transactionMessageList.size()).isGreaterThanOrEqualTo(1);
		transactionMessageMongodbTemplate.deleteById(_id);
	}
	
	
}
