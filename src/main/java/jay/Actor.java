package jay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gbdata.common.json.JSONObject;
import com.gbdata.common.json.JSONValue;
import com.gbdata.common.mq.IMQWorker;
import com.gbdata.common.mq.RabbitMQConsumer;
import com.gbdata.common.mq.RabbitMQProducer;
import com.gbdata.common.util.Logger;
import com.gbdata.common.util.Object2Json;
import com.gbdata.common.util.RandomUtil;

public class Actor implements IMQWorker{

	public static String[] ActorIds = {"a","b","c","d","e"};
	
	private static int quorumSize = 3;
	
	Map<String, RabbitMQProducer> producers = new HashMap<String, RabbitMQProducer>();
	
	// actor number
	private String actorId;
	private int proposalNumBase = 0;
	private int sid = 0;
	
	//proposer data
	private Map<String, ProposerState> proposerStates = new HashMap<String, ProposerState>(); //instance id ---> proposer state
	
	//acceptor data
	private Map<String, AccepterState> accepterStates = new HashMap<String, AccepterState>(); //instance id ---> accepter state

	
	public static Set<String> getRandonQuorum(int size)
	{
		Set<String> set = new HashSet<String>();
		return getRandonQuorum(size, set);
	}
	
	public static Set<String> getRandonQuorum(int size, Set<String> removeSet)
	{
		Set<String> set = new HashSet<String>();
		
		List<String> remaining = new ArrayList<String>();
		
		for(String accepter : ActorIds)
		{
			if(!removeSet.contains(accepter))
				remaining.add(accepter);
		}
		
		Collections.shuffle(remaining);
	
		int len = Math.min(size, remaining.size());
		
		for(int i = 0 ; i < len; ++ i)
			set.add(remaining.get(i));
		
		return set;
	}
	
	public static String getQueue(String actorId)
	{
		return "Msg_Pipe_" + actorId;
	}
	
	public Actor(String id)
	{
		this.actorId = id;
		for(int i = 0 ; i < ActorIds.length; ++ i)
			producers.put(ActorIds[i], new RabbitMQProducer(getQueue(ActorIds[i])));
	}
	
	private Set<String> broadcast(Message msg)
	{
		Set<String> set = getRandonQuorum(quorumSize);

		broadcast(msg, set);
		
		return set;
	}
	
	
	private void broadcast(Message msg, Set<String> accepters)
	{
		msg.sender = actorId;
		
		if(msg.sid == null)
			msg.sid = actorId + "_" + sid ++;
		
		for(String accepter : accepters)
		{
			RabbitMQProducer producer = producers.get(accepter);
			
			int rand = RandomUtil.getRandomInt(0, 10);
			try {
				Thread.sleep(rand * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			producer.send(Object2Json.toJsonString(msg));
		}
	}
	
	
	private void send(String target, Message msg)
	{
		int rand = RandomUtil.getRandomInt(0, 10);
		try {
			Thread.sleep(rand * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RabbitMQProducer producer = producers.get(target);
		
		msg.sender = actorId;
		if(msg.sid == null)
			msg.sid = actorId + "_" + sid ++;
		
		if(producer != null)
			producer.send(Object2Json.toJsonString(msg));
		
	}
	
	public void onTimeout(String instance)
	{
		
	}
	
	
	//proposer logic
	private void process(ClientReq req)
	{
		startPrepare(req.instance, req.value);
	}
	
	
	private void startPrepare(String instance, String value)
	{
		String proposalNum = String.format("%02d", proposalNumBase ++ ) + "_" + actorId;
		
		Proposal proposal = new Proposal();
		
		proposal.value = value;
		proposal.number = proposalNum;
		
		PrepareReq preq = new PrepareReq();
		
		preq.proposal = proposal;
		preq.instance = instance;
		
		Logger.logger.info("Start Parepare phrase: " + preq);
		
		Set<String> accepters = broadcast(preq);
		
		ProposerState state = new ProposerState();
		
		state.currentProposal = proposal;
		state.instance = instance;
		state.status = "prepare";
		
		for(String accepter: accepters)
			state.prepareResponses.put(accepter, null);
	
		proposerStates.put(instance, state);
		
	}
	
	private void process(PrepareRes res)
	{
		String instance = res.instance;
		
		ProposerState state = proposerStates.get(instance);
		
		if("fail".equals(state.status))
		{
			Logger.logger.warn("instance already failed: " + state);
			return;
		}
		
		if("promise".equals(res.response))
		{
			Map<String, PrepareRes> map = state.prepareResponses;
			
			map.put(res.sender, res);
			
			int numOK = 0;
			
			String maxPropNum = null;
			
			String maxPropNumValue = null;
			
			Set<String> quorumSet = new HashSet<String>();
			
			for(PrepareRes pres : map.values())
			{
				if(pres != null && pres.response.equals("promise"))
				{
					quorumSet.add(pres.sender);
					
					numOK++;
					Proposal prop = pres.recentAcceptProposal;
					String v = prop.number;
					if(Proposal.isGreaterThan(v, maxPropNum))
					{
						maxPropNum = v;
						maxPropNumValue = prop.value;
					}
				}
			}
			
			if(numOK >= quorumSize )
			{
				AcceptReq areq = new AcceptReq();
				
				areq.instance = instance;
				
				areq.proposal.number = state.currentProposal.number;
				
				if(maxPropNumValue != null)
					state.currentProposal.value = maxPropNumValue;
				
				areq.proposal.value = state.currentProposal.value;
				
				state.status = "accept";
				
				Logger.logger.info("Start Accept Phrase: " + areq);
				
				broadcast(areq, quorumSet);
			}
		}
		else if("reject".equals(res.response))
		{
			state.status = "fail";
		}
	}
	
	private void process(AcceptRes res)
	{
		String instance = res.instance;
		
		ProposerState state = this.proposerStates.get(instance);
		
		if(state == null)
		{
			System.err.println("fatal error!");
			System.exit(1);
		}
		
		if("fail".equals(state.status))
		{
			Logger.logger.warn("instance already failed: " + state);
			return;
		}
		
		if("accept".equals(res.response))
		{
			Map<String, AcceptRes> map = state.acceptResponses;
			
			map.put(res.sender, res);
			
			int numOK = 0;
			
			for(AcceptRes pres : map.values())
			{
				if(pres != null && pres.response.equals("accept"))
					numOK++;
			}
			if(numOK >= quorumSize )
			{
				state.status = "success";
			}
		}
		else if("reject".equals(res.response))
		{
			state.status = "fail";
		}
	}
	
	
	//accepter logic
	private void process(PrepareReq req)
	{
		Proposal proposal = req.proposal;
		
		AccepterState state = accepterStates.get(req.instance);
		
		PrepareRes res = new PrepareRes();
		
		res.sid = req.sid;
		
		res.instance = req.instance;
		
		//first time to receive prepare request
		if(state == null)
		{
			res.response = "promise";
			
			state = new AccepterState();
			
			state.instance = req.instance;
			state.maxNumber = proposal.number;
			state.recentAcceptProposal = proposal;
			
			accepterStates.put(state.instance, state);
		}
		else 
		{
			if(Proposal.isGreaterThan(proposal.number, state.maxNumber))
			{
				res.response = "promise";
				
				if(state.recentAcceptProposal != null)
					res.recentAcceptProposal = state.recentAcceptProposal;
				
				state.maxNumber = proposal.number;
				
//				// send "nack" back to proposer before accept phrase happens
//				for(Map<String, PrepareRes> map : promisesReceived.values())
//				{
//					for(PrepareRes pres : map.values())
//					{
//						AcceptRes ares = new AcceptRes();
//						
//						ares.proposal = new Proposal();
//						
//						ares.proposal.number = pres.proposalNum;
//						
//						ares.response = "nack";
//						
//						send(pres.sender, ares);
//					}
//				}
			}
			else 
			{
				res.response = "reject";
			}
		}
		
		send(req.sender, res);
	}
	
	private void process(AcceptReq req)
	{
		Proposal proposal = req.proposal;
		
		AccepterState state = accepterStates.get(req.instance);
		
		if(state == null)
		{
			System.err.println("fatal error!");
			System.exit(1);
		}
		
		AcceptRes res = new AcceptRes();
		
		res.sid = req.sid;
		
		res.instance = req.instance;
		
		if(Proposal.isGreaterThan(proposal.number, state.maxNumber))
		{
			res.response = "accept";
			res.value = proposal.value;
		}
		else 
			res.response = "reject";
		
		send(req.sender, res);
		

	}
	
	
	@Override
	public boolean execTask(String message) throws Exception 
	{
		//simulate the deplay of message passing
		int rand = RandomUtil.getRandomInt(0, 10);
		Thread.sleep(rand * 100);
		
		
		JSONObject obj = JSONValue.parseJSONObject(message);
		
		String type = obj.getString("type");
		
		String sender = obj.getString("sender");
		
		String instance = obj.getString("instance");
		
		if(type.equals("ClientReq"))
		{
			ClientReq req = new ClientReq();
			
			req.sender = sender;
			req.instance = instance;
			req.value = obj.getString("value");
			
			process(req);
		}
		else if(type.equals("PrepareReq"))
		{
			PrepareReq req = new PrepareReq();
			
			req.sender = sender;
			req.instance = instance;
			
			JSONObject pobj = obj.getJSONObject("proposal");
			
			req.proposal.fromJson(pobj);
			 
			process(req);
		}
		else if(type.equals("PrepareRes"))
		{
			PrepareRes res = new PrepareRes();
			
			res.sender = sender;
			
			res.instance = instance;
			
			res.response = obj.getString("response");
			
			JSONObject prop = obj.getJSONObject("recentAcceptProposal");
			
			res.recentAcceptProposal = new Proposal();
			
			res.recentAcceptProposal.fromJson(prop);
			
			process(res);
		}
		else if(type.equals("AcceptReq"))
		{
			AcceptReq req = new AcceptReq();
			
			req.sender = sender;
			req.instance = instance;
			
			JSONObject prop = obj.getJSONObject("proposal");
			
			req.proposal.fromJson(prop);
			
			process(req);
		}
		else if(type.equals("AcceptRes"))
		{
			AcceptRes res = new AcceptRes();
			
			res.sender = sender;
			res.instance = instance;
			
			res.value = obj.getString("value");
			
			res.response = obj.getString("response");
			
			process(res);
		}
//		else if(type.equals("DecisionNotice"))
//		{
//			DecisionNotice notice = new DecisionNotice();
//			
//			JSONObject prop = obj.getJSONObject("proposal");
//			
//			Proposal proposal = new Proposal();
//			
//			proposal.number = prop.getString("number");
//			
//			proposal.value = prop.getString("value");
//		
//			notice.proposal = proposal;
//			
//			process(notice);
//		}
		
		displayState();
		
		return true;
	}
	
	public void displayState()
	{
		
		{
			String json = Object2Json.toJsonString(proposerStates);
			
			JSONObject jobj = JSONValue.parseJSONObject(json);
			
			json = JSONValue.format(jobj);
			
			System.out.println(json);
		}

		{
			String json = Object2Json.toJsonString(accepterStates);
			
			JSONObject jobj = JSONValue.parseJSONObject(json);
			
			json = JSONValue.format(jobj);
			
			System.out.println(json);
		}
	}
	
	public static void main(String[] args) {
		
		String actorId = args[0];
		
		Actor actor = new Actor(actorId);
		
		RabbitMQConsumer con = new RabbitMQConsumer(getQueue(actorId));
//		con.cleanQueue();
		con.pollBlock(actor);
		
	}

}
