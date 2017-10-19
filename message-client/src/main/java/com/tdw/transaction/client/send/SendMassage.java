package com.tdw.transaction.client.send;

import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.tdw.transaction.client.producer.LocalTransactionExecuter;
import com.tdw.transaction.client.producer.LocalTransactionState;
import com.tdw.transaction.model.request.MessageIdCreator;

public class SendMassage {

	private static final Logger logger = LoggerFactory.getLogger(SendMassage.class);

	private static HttpHeaders header = new HttpHeaders();

	//Timeout waiting for connection from pool
	private static HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create()
            .setMaxConnTotal(200)
            .setMaxConnPerRoute(100)
            .build());

	static {
		header.setAccept(ImmutableList.of(MediaType.APPLICATION_JSON_UTF8));
		header.setContentType(MediaType.APPLICATION_JSON_UTF8);

		//此处超时时长设置相对要长一点，防止并发压力导致延时
		httpRequestFactory.setConnectTimeout(5000);
		// 数据读取超时时间，即SocketTimeout
		httpRequestFactory.setReadTimeout(5000);
		// 连接不够用的等待时间，不宜过长，必须设置，比如连接不够用时，时间过长将是灾难性的
		httpRequestFactory.setConnectionRequestTimeout(5000);
        // 缓冲请求数据，默认值是true。通过POST或者PUT大量发送数据时，建议将此属性更改为false，以免耗尽内存。
        // httpRequestFactory.setBufferRequestBody(false);

	}

	private String _massageManageUrl;

	public SendMassage(String massageManageUrl) {
		_massageManageUrl = massageManageUrl;
	}

	public LocalTransactionState preSendMassage(MessageIdCreator messageIdCreator,
			LocalTransactionExecuter localTransactionExecuter, final Object arg) {

		RestTemplate thRestTemplate = new RestTemplate(httpRequestFactory);

		final ResponseEntity<String> response = thRestTemplate.exchange(_massageManageUrl + "/message/creator",
				HttpMethod.POST, new HttpEntity<MessageIdCreator>(messageIdCreator, header), String.class);

		if (response.getStatusCode().equals(HttpStatus.OK)) {
			logger.debug("response.getBody: {}", response.getBody());
			JSONObject jo = JSONObject.parseObject(response.getBody());
			String msgId = jo.getString("data");
			// 业务处理
			LocalTransactionState localTransactionState = localTransactionExecuter.executeLocalTransactionBranch(msgId,
					arg);

			if (LocalTransactionState.COMMIT_MESSAGE.equals(localTransactionState)) {
				// 提交事务消息
				final ResponseEntity<String> resp = thRestTemplate.exchange(
						_massageManageUrl + "/message/send/" + msgId + "/true", HttpMethod.POST,
						new HttpEntity<String>("", header), String.class);
				logger.info("提交事务消息: {}", resp.getBody());

			} else if (LocalTransactionState.ROLLBACK_MESSAGE.equals(localTransactionState)) {
				// 取消事务消息
				final ResponseEntity<String> resp = thRestTemplate.exchange(
						_massageManageUrl + "/message/send/" + msgId + "/false", HttpMethod.POST,
						new HttpEntity<String>("", header), String.class);
				logger.info("取消事务消息: {}", resp.getBody());
			}

			return localTransactionState;
		}

		return LocalTransactionState.UNKNOW;
	}

}
