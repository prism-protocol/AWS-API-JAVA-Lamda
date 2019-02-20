package com.serverless;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.serverless.data.Transaction;
import com.serverless.db.DynamoDBAdapter;

public class PostTransactionsHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse>{
	
	private static final Logger LOG = LogManager.getLogger(PostTransactionsHandler.class);
	
	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
	    LOG.info("received: " + input);
	    //lets get our path parameter for account_id
	    try{
	      ObjectMapper mapper = new ObjectMapper();
	      Map<String,String> pathParameters =  (Map<String,String>)input.get("pathParameters");
	      String accountId = pathParameters.get("account_id");
	      Transaction tx = new Transaction();
	      tx.setAccount_id(accountId);
	      JsonNode body = mapper.readTree((String) input.get("body"));
	      float amount = (float) body.get("amount").asDouble();
	      String txId = body.get("transaction_id").asText();
	      tx.setAmount(amount);
	      tx.setTransaction_date(new Date(System.currentTimeMillis()));
	      tx.setTransaction_id(txId);
	      DynamoDBAdapter.getInstance().putTransaction(tx);
	    } catch(Exception e){
	      LOG.error(e,e);
	      
	      Response responseBody = new Response("Failure putting transaction", input);
	      return ApiGatewayResponse.builder()
	      .setStatusCode(500)
	      .setObjectBody(responseBody)
	      .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
	      .build();
	    }
	    Map<String, Object> output = new HashMap<String, Object>();
	      output.put("response", 3);
	      output.put("from post", "Transaction stored success...");
	      
	    Response responseBody = new Response("Transaction added successfully!", output);
	   
	    return ApiGatewayResponse.builder()
	    .setStatusCode(200)
	    .setObjectBody(responseBody)
	    .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
	    .build();
	}
}
