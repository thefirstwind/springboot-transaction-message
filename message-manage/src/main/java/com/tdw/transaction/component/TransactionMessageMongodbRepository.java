package com.tdw.transaction.component;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.tdw.transaction.domain.TransactionMessage;


public interface TransactionMessageMongodbRepository extends MongoRepository<TransactionMessage, Integer>{

	TransactionMessage findById(String id);
	
	TransactionMessage findByMessageType(int messageType);

	Page<TransactionMessage> findByMessageTopic(String messageTopic, Pageable pageable);

	Page<TransactionMessage> findByMessageState(int messageState, Pageable pageable);
		
	Page<TransactionMessage> findByMessageStateAndPresendBackSendTimesLessThan(int messageState,Date dt, Pageable pageable);
	
	
}
