package com.tdw.transaction.component;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.tdw.transaction.domain.TransactionMessage;

@Component
public class TransactionMessageMongodbTemplate {

	@Autowired
	private MongoTemplate mongoTemplate;

	public void insert(TransactionMessage transactionMessage) {
		mongoTemplate.insert(transactionMessage);
	}

	public void save(TransactionMessage transactionMessage) {
		mongoTemplate.save(transactionMessage);
	}

	public void deleteById(String id) {
		Criteria criteria = Criteria.where("_id").is(id);
		Query query = new Query(criteria);
		mongoTemplate.remove(query, TransactionMessage.class);
	}

	public void updateMultiStateById(TransactionMessage transactionMessage) {
		Criteria criteria = Criteria.where("_id").is(transactionMessage.getId());
		Query query = new Query(criteria);
		Update update = new Update();
		update.set("msgs", transactionMessage.getMessageState());
		mongoTemplate.updateMulti(query, update, TransactionMessage.class);
	}

	public TransactionMessage selectById(String id) {
		Criteria criteria = Criteria.where("_id").is(id);
		Query query = new Query(criteria);
		return mongoTemplate.findOne(query, TransactionMessage.class);
	}

	public List<TransactionMessage> findForPresendBack(Date dt, int messageState) {
		Criteria criteria = Criteria.where("msgs").is(messageState).and("pbnst").lt(dt);
		Query query = new Query(criteria);
		return mongoTemplate.find(query.limit(1000), TransactionMessage.class);
	}
	

	public List<TransactionMessage> findForSendMQ(Date dt, int messageState) {
		Criteria criteria = Criteria.where("msgs").is(messageState).and("mnst").lt(dt);
		Query query = new Query(criteria);
		return mongoTemplate.find(query.limit(1000), TransactionMessage.class);
	}
	

	public List<TransactionMessage> findForDoneBack(Date dt, int messageState) {
		Criteria criteria = Criteria.where("msgs").is(messageState).and("rbnst").lt(dt).and("rbt").gt(0);
		Query query = new Query(criteria);
		return mongoTemplate.find(query.limit(1000), TransactionMessage.class);
	}
	
}
