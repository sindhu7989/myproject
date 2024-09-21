package com.straviso.ns.dispatchcontrollercore.serviceImpl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessContext;
import com.straviso.ns.dispatchcontrollercore.multitenancy.BusinessTokenContext;
import com.straviso.ns.dispatchcontrollercore.repository.SystemConfigDCRepository;

import lombok.extern.log4j.Log4j2;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.dto.CsvData;
import com.straviso.ns.dispatchcontrollercore.dto.request.FetchTokenAPIRequest;
import com.straviso.ns.dispatchcontrollercore.dto.response.FetchTokenAPIResponse;
import com.straviso.ns.dispatchcontrollercore.entity.SystemConfigDC;
import com.opencsv.CSVReader;
import java.io.FileReader;

@Log4j2
@Service
public class SFTPFileService {
	
	@Autowired
	private SystemConfigDCRepository systemConfigDCRepository;
	
	@Value("${service.constants.dcs.ig.workday_url}")
    private String workdayUrl;
	

	public List<Map<String, String>> downloadAndConvertCSV(String csvFileNames) {
		List<Map<String, String>> jsonData = new ArrayList<>();
		Session session = null;
		ChannelSftp channelSftp = null;
		
		List<SystemConfigDC> systemConfigDCList = new ArrayList<>();
		Map<String,SystemConfigDC> systemConfigDCMap = new HashMap<>();
		try {
			systemConfigDCList = systemConfigDCRepository.findByConfigRoleAndIsActive(DispatchControllerConstants.ROLE_DC_CORE ,DispatchControllerConstants.FLAG_Y);
			systemConfigDCList.forEach(config->{
				systemConfigDCMap.put(config.getConfigProperty(), config);
			});
		}catch(Exception e) {
			log.info("Unable to fetch System DC Config List due to {}",e.getMessage());
		}
		
		try {
		
			String sftpUsername=DispatchControllerConstants.SFTP_USERNAME;
			String sftpHostName=DispatchControllerConstants.SFTP_HOST;
			String sftpPassWord=DispatchControllerConstants.SFTP_PASSWORD;
			String sftpPath=DispatchControllerConstants.SFTP_REMOTE_DIRECTORY;
			
			try {
				if(!CollectionUtils.isEmpty(systemConfigDCMap)) {

					if(systemConfigDCMap.containsKey(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_USERNAME)) {
						sftpUsername =  systemConfigDCMap.get(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_USERNAME).getConfigValue();
					}
					if(systemConfigDCMap.containsKey(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_HOST)) {
						sftpHostName =  systemConfigDCMap.get(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_HOST).getConfigValue();
					}
					if(systemConfigDCMap.containsKey(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_PASSWORD)) {
						sftpPassWord =  systemConfigDCMap.get(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_PASSWORD).getConfigValue();
					}
					if(systemConfigDCMap.containsKey(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_REMOTE_DIRECTORY)) {
						sftpPath =  systemConfigDCMap.get(DispatchControllerConstants.SYS_CONFIG_DEFAULT_SFTP_REMOTE_DIRECTORY).getConfigValue();
						log.info("Connecting to db systemConfig to fetch path : {} : "+ sftpPath);
					}
				}else {
					sftpUsername=DispatchControllerConstants.SFTP_USERNAME;
					sftpHostName=DispatchControllerConstants.SFTP_HOST;
					sftpPassWord=DispatchControllerConstants.SFTP_PASSWORD;
					sftpPath=DispatchControllerConstants.SFTP_REMOTE_DIRECTORY;
					log.info("fetch from constant DEFAULT_SFTP_USERNAME : "+ sftpUsername);
				}
			}catch(Exception e) {
				log.info("Unable to get Default System Config Id due to {}",e.getMessage());

				sftpUsername=DispatchControllerConstants.SFTP_USERNAME;
				sftpHostName=DispatchControllerConstants.SFTP_HOST;
				sftpPassWord=DispatchControllerConstants.SFTP_PASSWORD;
				sftpPath=DispatchControllerConstants.SFTP_REMOTE_DIRECTORY;
				log.info("exception from fetching db , fetch from constant DEFAULT_SFTP_USERNAME : "+ sftpUsername);
			}
			log.info("Connecting to the sftpHostName : {}  for : {} at path :{}",sftpHostName, csvFileNames,sftpPath); 
			
			JSch jsch = new JSch();
			session = jsch.getSession(sftpUsername,sftpHostName, 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(sftpPassWord);
			session.connect();

			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();

			channelSftp.cd(sftpPath);
			Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls("*.csv");
			
			log.info("List of csv File recevied at sftp  : " + fileList);

			for (ChannelSftp.LsEntry entry : fileList) {
				if (entry.getAttrs().isReg()) {
					String fileName = entry.getFilename();
					if (fileName.equals(csvFileNames)) {
						channelSftp.get(fileName, fileName);
						List<Map<String, String>> csvData = convertCSVToJSON(fileName);
						jsonData.addAll(csvData);
					}
				}
			}

		
		}catch (JSchException | SftpException e) {
			e.printStackTrace();
		} finally {
			if (channelSftp != null) {
				channelSftp.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}

		return jsonData;
	}

	private List<Map<String, String>> convertCSVToJSON(String csvFileName) {
		List<Map<String, String>> jsonData = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(csvFileName))) {
			String line;
			String[] headers = null;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (headers == null) {
					headers = values;

				} else if (values.length == headers.length) {
																
					Map<String, String> data = new LinkedHashMap<>();
					for (int i = 0; i < headers.length; i++) {
						data.put(headers[i], values[i]);

					}
					jsonData.add(data);

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return jsonData;
	}

	public List<Map<String, String>> getEmployeeQualityAndServiceData( String fileName) {

	    List<Map<String, String>> jsonData = downloadAndConvertCSV(fileName);
//	    System.out.println("jsonData 11: " + jsonData);
	    List<Map<String, String>> resultset = new ArrayList<>();

	    for (Map<String, String> data : jsonData) {
	        Map<String, String> result = new HashMap<>(); 

//	        if (data.containsKey("employee_id")) {
	            result.put("TechnicianId", data.get("employee_id"));
	            result.put("data", data.toString());

	            resultset.add(result);
//	        } else if (data.containsKey("ID")) {
//	            result.put("TechnicianId", data.get("ID"));
//	            result.put("data", data.toString());
//
//	            resultset.add(result);
//	        }
	    }
	    return resultset;
	    
	}

	public List<Map<String, String>> getServiceData() throws JsonMappingException, JsonProcessingException {
		
		List<SystemConfigDC> systemConfigDCList = new ArrayList<>();
		Map<String,SystemConfigDC> systemConfigDCMap = new HashMap<>();
		try {
			systemConfigDCList = systemConfigDCRepository.findByConfigRoleAndIsActive(DispatchControllerConstants.ROLE_DC_CORE ,DispatchControllerConstants.FLAG_Y);
			systemConfigDCList.forEach(config->{
				systemConfigDCMap.put(config.getConfigProperty(), config);
			});
		}catch(Exception e) {
			log.info("Unable to fetch System DC Config List due to {}",e.getMessage());
		}
		
		String apiUrl = workdayUrl;
		
		try {
			if(!CollectionUtils.isEmpty(systemConfigDCMap)) {

				if(systemConfigDCMap.containsKey(DispatchControllerConstants.SYS_CONFIG_DEFAULT_WORKDAY_URL)) {
					apiUrl =  systemConfigDCMap.get(DispatchControllerConstants.SYS_CONFIG_DEFAULT_WORKDAY_URL).getConfigValue();
					log.info("Connecting to db systemConfig to fetch WORKDAY_URL : {} : "+ apiUrl);
				}
			}else {
				apiUrl=workdayUrl;
				log.info("fetch from constant WORKDAY_URL : "+ apiUrl);
				
			}
		}catch(Exception e) {
			log.info("Unable to get Default System Config Id due to {}",e.getMessage());

			apiUrl=workdayUrl;
			log.info("exception from fetching db , fetch from constant WORKDAY_URL : "+ apiUrl);
		}
		log.info("Connecting to the Workday URL : {} ",apiUrl); 
		
		String JWTToken = BusinessTokenContext.getBusinessToken();
		
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + JWTToken);

		HttpEntity<String> requestEntity = new HttpEntity<>(headers);
		try {
			ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.GET, requestEntity,
					String.class);
			log.info("Response from workday url :  {}",responseEntity.getStatusCode());
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				String jsonResponse = responseEntity.getBody();
				if (StringUtils.isNotBlank(jsonResponse)) {
					ObjectMapper objectMapper = new ObjectMapper();

					List<Map<String, String>> employeeDataList = new ArrayList<>();

					JsonNode responseData = objectMapper.readTree(jsonResponse).get("responseData");
	                
	                if (responseData != null) {
	                    JsonNode reportEntry = responseData.get("Report_Entry");
	                    if (reportEntry != null && reportEntry.isArray()) {
	                        for (JsonNode entry : reportEntry) {
	                            JsonNode idNode = entry.get("ID");
	                            JsonNode continuousServiceDateNode = entry.get("Continuous_Service_Date");
	                            JsonNode mostRecentHireDateNode = entry.get("Most_Recet_Hire_Date");

	                            if (idNode != null && continuousServiceDateNode != null && mostRecentHireDateNode != null) {
	                                String ID = idNode.asText();
	                                String continuousServiceDate = continuousServiceDateNode.asText();
	                                String mostRecentHireDate = mostRecentHireDateNode.asText();
						Map<String, String> employeeData = new HashMap<>();
						employeeData.put("technicianId", ID);
						employeeData.put("continuousServiceDate", continuousServiceDate);
						employeeData.put("mostRecentHireDate", mostRecentHireDate);
						employeeDataList.add(employeeData);
					}
	                        }
	                        }
					return employeeDataList;
				} else {
					return new ArrayList<>();
				}
	                    }else {
					return new ArrayList<>();
				}
	                
			} else {
				return new ArrayList<>();
			}
				
		} catch (Exception e) {
			log.info("Buisness Id :{} : Exception will calling IG API of workday connector : {} ",
					BusinessContext.getTenantId(), e.getMessage());
			return new ArrayList<>();
		}
	}

}