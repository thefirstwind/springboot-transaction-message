package com.tdw.transaction.component;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TransactionMessageMongodbRepositoryTest {

	@Autowired
	private TransactionMessageMongodbRepository transactionMessageMongodbRepository;

	/**
	 * 
	 */
	@Test
	public void findByPresendBack() {
		
//		Pageable pageable = new PageRequest(0, 2, new Sort(new Order(Direction.DESC, "createTime")));
//		Calendar calendar = Calendar.getInstance();
//		SimpleDateFormat sdff =   new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.fffZ");  
//	   
//	    
//		Page<TransactionMessage> ptm = transactionMessageMongodbRepository.findByPresendBack(10, sdff.format(calendar.getTime()), pageable);
//		
//		Assertions.assertThat(ptm.getContent().size()).isGreaterThan(1);
		
	}
}
