package at.ac.tuwien.infosys.governance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import at.ac.tuwien.infosys.governance.isolatedactuation.ProcessCapabilityInvocation;
import at.ac.tuwien.infosys.governance.isolatedactuation.ScopeCapabilityInvocation;
import at.ac.tuwien.infosys.model.DefaultMappingModel;
import at.ac.tuwien.infosys.proxy.ProcessContext;
import at.ac.tuwien.infosys.proxy.ProcessContext.Context;
import at.ac.tuwien.infosys.store.model.DeviceDTO;
import at.ac.tuwien.infosys.store.model.DevicesDTO;

import com.netflix.config.ConfigurationManager;

@RestController
@RequestMapping("/governanceScope")
public class GovernanceScopeManager {

	private static final Logger LOGGER = Logger
			.getLogger(GovernanceScopeManager.class);

	@Autowired
	private AsyncRestTemplate asyncRestTemplate;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ProcessContext processContext;
	volatile long endTS = 0;

	@RequestMapping(value = "/setProcessProps/{procId}", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<String> setInstanceProperties(@PathVariable String procId, @RequestBody String propertiesJson){
		
		LOGGER.info("Received properties for process "+ procId+"\n"+propertiesJson);
		this.processContext.registerIfAbsent(procId).setProperties(propertiesJson);
		this.processContext.registerIfAbsent(procId).refreshConfig();
		return new ResponseEntity<String>("Sucessfully updated configuration",HttpStatus.OK);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/globalScope", method = RequestMethod.GET)
	public ResponseEntity<DevicesDTO> getGlobalScope() {
		DevicesDTO devices = new DevicesDTO();
		ResponseEntity<List> balancerResponse = restTemplate.getForEntity(
				"http://localhost:8080/SDGBalancer/balancer/nodes", List.class);
		if (balancerResponse.getStatusCode() != HttpStatus.OK) {
			return new ResponseEntity<DevicesDTO>(
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		List<String> runningNodes = (List<String>) balancerResponse.getBody();
		for (String node : runningNodes) {
			LOGGER.info("Get devies from node " + node);
			ResponseEntity<DevicesDTO> managerResponse = restTemplate
					.getForEntity("http://" + node
							+ ":8080/SDGManager/device-manager/devices",
							DevicesDTO.class);

			if (managerResponse.getStatusCode() != HttpStatus.OK)
				return new ResponseEntity<DevicesDTO>(
						HttpStatus.INTERNAL_SERVER_ERROR);
			LOGGER.info("Node " + node + " has returned ["
					+ managerResponse.getBody().getDevices() + "]");
			devices.getDevices().addAll(managerResponse.getBody().getDevices());
		}

		return new ResponseEntity<DevicesDTO>(devices, HttpStatus.OK);
	}

	//TODO: Change this to post - scope=query - uncertainty info will be passed as Json in the body
	@RequestMapping(value = "/invokeScope/{procId}/{scope}/{capaId}/{method}", method = RequestMethod.GET)
	public ResponseEntity<String> invokeCapabilityOnScope(
			@PathVariable String procId,
			@PathVariable String scope, 
			@PathVariable String capaId,
			@PathVariable String method,
			@RequestParam(value = "args", required = false) String args) {

		long startTS = System.currentTimeMillis();

		LOGGER.info("Invoked Mapper on scope : " + scope);
		List<DeviceDTO> devices = getGlobalScope().getBody().getDevices();
		LOGGER.info("Found so many devices: " + devices.size());
		String[] scopeMeta = scope.split("=");
		LOGGER.info("Scope key=" + scopeMeta[0] + ", scope value="
				+ scopeMeta[1]);

		List<DeviceDTO> governanceScope = devices
				.stream()
				.filter(d -> d.getMeta().containsKey(scopeMeta[0])
						&& d.getMeta().get(scopeMeta[0]).equals(scopeMeta[1]))
				.collect(Collectors.toList());

		LOGGER.info("Governance scope includes " + governanceScope.size()
				+ "! " + governanceScope);

		String arguments = (args != null && !args.isEmpty()) ? "?args=" + args
				: "";
		// Async is executed in its own thread. How to return the result to
		// client?
		long beforeDeviceInvoceation = System.currentTimeMillis();
		String response = "Invoking " + governanceScope.size()
				+ " devices ...<br/><br/>";

		List<Future<ResponseEntity<String>>> invocationResults = new ArrayList<>();
		for (DeviceDTO deviceDTO : governanceScope) {

			String dynamicURL = new DefaultMappingModel(capaId,
					deviceDTO.getId(), method, args).getMapping();
			
			Context procC = this.processContext.registerIfAbsent(procId);

			Future<ResponseEntity<String>> r = new ProcessCapabilityInvocation(
					dynamicURL, procC).queue();
			
			invocationResults.add(r);

		LOGGER.info("Waiting for " + invocationResults.size() + " results!");
		for (Future<ResponseEntity<String>> result : invocationResults) {
			try {
				String resultBody = result.get().getBody();
				LOGGER.info("Got sync result " + resultBody);
				response += resultBody+"<br/><br/>";
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// One option is to collect all results and then return - logic the
		// client will do this anyway - problem is it cant process results one
		// by one
		// Second option is to return one by one
		// Last option is to return futures

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/checkDevices/{scopeSize}/{capaId}", method = RequestMethod.GET)
	public ResponseEntity<String> getCheckDevices(
			@PathVariable String scopeSize, @PathVariable String capaId) {

		final long startTime = System.currentTimeMillis();

		Integer size = Integer.valueOf(scopeSize);
		// List<DeviceDTO> devices = getAllDevices().getBody().getDevices();
		ResponseEntity<DevicesDTO> managerResponse = restTemplate.getForEntity(
				"http://" + "128.130.172.174"
						+ ":8080/SDGManager/device-manager/devices",
				DevicesDTO.class);

		List<DeviceDTO> governanceScope = new ArrayList<DeviceDTO>();

		for (int i = 0; i < size; i++) {

			governanceScope.add(managerResponse.getBody().getDevices().get(i));
		}

		for (DeviceDTO deviceDTO : governanceScope) {
			// TODO think how to reuse tomcat thread pool
			try {
				ListenableFuture<ResponseEntity<String>> result = asyncRestTemplate
						.getForEntity(
								"http://localhost:8080/APIManager/mapper/check/"
										+ capaId + "/" + deviceDTO.getId(),
								String.class);

				result.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
					@Override
					public void onSuccess(ResponseEntity<String> result) {
						LOGGER.info("Received result from node: "
								+ result.getBody());
						// Add to a queue
						endTS = System.currentTimeMillis();
						LOGGER.info("Invocation time was: " + startTime + ","
								+ endTS);
					}

					@Override
					public void onFailure(Throwable t) {
						LOGGER.info("Error contacting device: "
								+ t.getMessage());
					}
				});

				// ResponseEntity<String> res = invokeCapability(
				// deviceDTO.getId(), capaId, method, args);
				// response += res.getBody() + "/n </br> ";
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return new ResponseEntity<String>("Send checking invocations!",
				HttpStatus.OK);
	}

	@Configuration
	public static class AsyncRestTemplateFactory {

		@Bean
		public AsyncRestTemplate createAsyncRestTemplate() {
			AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();
			return asyncRestTemplate;
		}
	}

	@Configuration
	public static class RestTemplateFactory {

		@Bean
		public RestTemplate createRestTemplate() {
			RestTemplate restTemplate = new RestTemplate();
			return restTemplate;
		}
	}

}
