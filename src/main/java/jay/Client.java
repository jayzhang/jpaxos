package jay;

import com.gbdata.common.mq.RabbitMQProducer;
import com.gbdata.common.util.Object2Json;

public class Client {

	public static void main(String[] args) {
	
//		String value = args[0];
		
//		for(String target : Actor.ActorIds)
		
		for(String accepter : Actor.ActorIds)
		{
			RabbitMQProducer prod = new RabbitMQProducer(Actor.getQueue(accepter));
			prod.clear();
			prod.close();
		}
		
		{
			ClientReq req = new ClientReq();
			
			req.instance = "demo";
			
			req.value = "pa";
			
			req.sender = "C1";
			
			RabbitMQProducer prod = new RabbitMQProducer(Actor.getQueue("a"));
			
			prod.send(Object2Json.toJsonString(req));
			
			prod.close();
		}
		
		
		{
			ClientReq req = new ClientReq();
			
			req.instance = "demo";
			
			req.value = "pb";
			
			req.sender = "C1";
			
			RabbitMQProducer prod = new RabbitMQProducer(Actor.getQueue("b"));

			prod.send(Object2Json.toJsonString(req));
			
			prod.close();
		}
		
		{
			ClientReq req = new ClientReq();
			
			req.instance = "demo";
			
			req.value = "pc";
			
			req.sender = "C1";
			
			RabbitMQProducer prod = new RabbitMQProducer(Actor.getQueue("c"));

			prod.send(Object2Json.toJsonString(req));
			
			prod.close();
		}
		
		{
			ClientReq req = new ClientReq();
			
			req.instance = "demo";
			
			req.value = "pd";
			
			req.sender = "C1";
			
			RabbitMQProducer prod = new RabbitMQProducer(Actor.getQueue("d"));

			prod.send(Object2Json.toJsonString(req));
			
			prod.close();
		}
		
		{
			ClientReq req = new ClientReq();
			
			req.instance = "demo";
			
			req.value = "pe";
			
			req.sender = "C1";
			
			RabbitMQProducer prod = new RabbitMQProducer(Actor.getQueue("e"));

			prod.send(Object2Json.toJsonString(req));
			
			prod.close();
		}
	}
}
