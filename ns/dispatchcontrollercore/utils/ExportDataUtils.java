package com.straviso.ns.dispatchcontrollercore.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.straviso.ns.dispatchcontrollercore.constants.DispatchControllerConstants;
import com.straviso.ns.dispatchcontrollercore.constants.ServiceConstants;
import com.straviso.ns.dispatchcontrollercore.dto.response.FileWriterResponse;

import lombok.extern.log4j.Log4j2;
@Component
@Log4j2
public class ExportDataUtils {
	public String getDateTimeByTimeZone(String currenDateTime, String fromTimeZone,String toTimeZone, String dateTimeFormat,Long increaseBySec) {
		String formattedDate = null;
		ZoneId fromZone ;
		ZoneId toZone ;
		LocalDateTime ldt;	
		try {

			fromZone= StringUtils.isEmpty(fromTimeZone) ? ZoneId.systemDefault() : ZoneId.of(fromTimeZone) ;
			toZone = StringUtils.isEmpty(toTimeZone) ? ZoneId.systemDefault() : ZoneId.of(toTimeZone) ;
			if(StringUtils.isEmpty(currenDateTime)) {
				ldt = LocalDateTime.now();				
			}else {
				ldt = LocalDateTime.parse(currenDateTime);				
			}

			if(increaseBySec != null && increaseBySec != 0L) {
				ldt = ldt.plusSeconds(increaseBySec); 			 
			}


			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateTimeFormat);
			ZonedDateTime zdtUtc = ldt.atZone(fromZone).withZoneSameInstant(toZone);

			formattedDate =zdtUtc.format(dtf); 

			return formattedDate;

		}catch(Exception e) {
			log.info("Unable to getDateTimeByTimeZone due to : {}", e.getMessage());
			return currenDateTime;
		}
	}
	
	public String getDateTimeByTimeZoneForDisplay(String currenDateTime, String fromTimeZone,String toTimeZone, String dateTimeFormat,Long increaseBySec) {
		ZoneId fromZone ;
		ZoneId toZone ;
		LocalDateTime ldt;	
		try {
			if(StringUtils.isEmpty(dateTimeFormat)) {
				dateTimeFormat = DispatchControllerConstants.RESPONSE_DATETIME_FORMAT;
			}
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateTimeFormat);
			fromZone= StringUtils.isEmpty(fromTimeZone) ? ZoneId.systemDefault() : ZoneId.of(fromTimeZone) ;
			toZone = StringUtils.isEmpty(toTimeZone) ? ZoneId.systemDefault() : ZoneId.of(toTimeZone) ;
			if(StringUtils.isEmpty(currenDateTime)) {
				ldt = LocalDateTime.now();				
			}else {
				ldt = LocalDateTime.parse(currenDateTime,dtf);					
			}

			ZonedDateTime zdtUtc = ldt.atZone(fromZone).withZoneSameInstant(toZone);

			String  zoneTime = zdtUtc.format(dtf).toString();
			return zoneTime;

		}catch(Exception e) {
			log.info("Unable to getDateTimeByTimeZone due to : {}", e.getMessage());
			return currenDateTime;
		}
	}
	
	public String getDateTimeByTimeZoneForDisplay(String currenDateTime, String fromTimeZone,String toTimeZone, String fromDateTimeFormat,String toDateTimeFormat,Long increaseBySec) {
		ZoneId fromZone ;
		ZoneId toZone ;
		LocalDateTime ldt;	
		try {
			if(StringUtils.isEmpty(fromDateTimeFormat)) {
				fromDateTimeFormat = DispatchControllerConstants.PARSE_DATETIME_FORMAT;
			}
			if(StringUtils.isEmpty(toDateTimeFormat)) {
				toDateTimeFormat = DispatchControllerConstants.RESPONSE_DATETIME_FORMAT;
			}
			DateTimeFormatter fromDTF = DateTimeFormatter.ofPattern(fromDateTimeFormat);
			DateTimeFormatter toDTF = DateTimeFormatter.ofPattern(toDateTimeFormat);
			fromZone= StringUtils.isEmpty(fromTimeZone) ? ZoneId.systemDefault() : ZoneId.of(fromTimeZone) ;
			toZone = StringUtils.isEmpty(toTimeZone) ? ZoneId.systemDefault() : ZoneId.of(toTimeZone) ;
			if(StringUtils.isEmpty(currenDateTime)) {
				ldt = LocalDateTime.now();				
			}else {
				ldt = LocalDateTime.parse(currenDateTime,fromDTF);					
			}

			ZonedDateTime zdtUtc = ldt.atZone(fromZone).withZoneSameInstant(toZone);

			String  zoneTime = zdtUtc.format(toDTF).toString();
			return zoneTime;

		}catch(Exception e) {
			log.info("Unable to getDateTimeByTimeZone due to : {}", e.getMessage());
			return currenDateTime;
		}
	}

	public String convertDateTimeByFormat(String givenDateTime, String dateTimeFormat) {

		String formattedDate = null;
		try {
			if(StringUtils.isEmpty(dateTimeFormat)) {
				dateTimeFormat = DispatchControllerConstants.RESPONSE_DATETIME_FORMAT;
			}
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern(dateTimeFormat);

			if(StringUtils.isEmpty(givenDateTime)) {
				formattedDate = dtf.format(LocalDateTime.now());
			}else {
				formattedDate = dtf.format(LocalDateTime.parse(givenDateTime));
			}
			return formattedDate;
		}catch(Exception e) {
			log.info("Unable to getDateTimeByTimeZone due to : {}", e.getMessage());
			return givenDateTime;
		}
	}

	public String[] mergeStringArray(String[] array1, String[] array2)   
	{   
		Integer array1Len = array1.length;
		Integer array2Len = array2.length;
		String[] mergedArray = new String[array1Len+array2Len];

		System.arraycopy(array1, 0, mergedArray, 0, array1Len);
		System.arraycopy(array2, 0, mergedArray, array1Len,array2Len);
		return mergedArray;   
	}  

	public <T> Object[] mergeArray(T[] arr1, T[] arr2)   
	{   
		return Stream.of(arr1, arr2).flatMap(Stream::of).toArray();   
	}  

	public synchronized FileWriterResponse exportToCSV(List<Object[]> dataList,String[] columns,String reportName, String fileLocation,String fileName, String businessId, String userId,String logKey) {
		File csvFile = new File(fileLocation+businessId+"/"+reportName+"/"+userId+"/"+fileName+DispatchControllerConstants.FILE_EXTENSION_CSV);
		FileWriterResponse fileWriterResponse = new FileWriterResponse();
		FileWriter out;
		try {
			out = new FileWriter(csvFile,true);
		}catch(Exception e) {
			log.info("{} Unable to exportToCSV while opening file to write for business {} for userId {}, due to {} ",logKey,businessId,userId,e);
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
			fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			fileWriterResponse.setFilePath(null);
			fileWriterResponse.setFileSize(0L);
			return fileWriterResponse;
		}
		
		
		try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
				.withHeader(columns).withSkipHeaderRecord())) {
			dataList.stream().forEach(data -> {
				try {
					printer.printRecord(data);
				} catch (IOException e) {
					fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
					fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
					fileWriterResponse.setFilePath(null);
					fileWriterResponse.setFileSize(0L);
					log.info("{} Unable to exportToCSV while writing data to file for business {} for userId {}, due to {} ",logKey,businessId,userId,e);
				}
			});
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_OK_CODE);
			fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_OK);
			fileWriterResponse.setFilePath(csvFile.getPath());
			fileWriterResponse.setFileSize(csvFile.length());

		}catch(Exception e) {
			log.info("{} Unable to exportToCSV for business {} for userId {}, due to {} ",logKey,businessId,userId,e);
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
			fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			fileWriterResponse.setFilePath(null);
			fileWriterResponse.setFileSize(0L);
			return fileWriterResponse;
		}

		return fileWriterResponse;
	}

	public FileWriterResponse createCSVFileInVolume(String reportName,String[] columns, String fileLocation,String fileName,String businessId,String userId,String logKey) {
		FileWriterResponse fileWriterResponse = new FileWriterResponse();
		try {
			String businessIdPath = fileLocation.concat(businessId);
			File directoryBussinessId = new File(businessIdPath);
			if (!directoryBussinessId.exists()){
				directoryBussinessId.mkdir();
			}
			String directoryReportNameStr = businessIdPath.concat("/"+reportName);
			File directoryReportName = new File(directoryReportNameStr);
			if (!directoryReportName.exists()){
				directoryReportName.mkdir();
			}

			String directoryUserIdStr = directoryReportNameStr.concat("/"+userId);
			File directoryUserId = new File(directoryUserIdStr);
			if (!directoryUserId.exists()){
				directoryUserId.mkdir();
			}

			FileWriter out = new FileWriter(fileLocation+businessId+"/"+reportName+"/"+userId+"/"+fileName+DispatchControllerConstants.FILE_EXTENSION_CSV,true);
			try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
					.withHeader(columns))) {		

				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_OK_CODE);
				fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_OK);
			}catch(Exception e) {
				log.info("{} Unable to File Writer block of createCSVFileInVolumes for business {} for userId {} , due to {}",logKey,businessId,userId,e);
				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
				fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
				fileWriterResponse.setFilePath(null);
				fileWriterResponse.setFileSize(0L);
			}
		}catch(Exception e) {
			log.info("{} Unable to createCSVFileInVolume for business {} for userId {}, due to {} ",logKey,businessId,userId,e);
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
			fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			fileWriterResponse.setFilePath(null);
			fileWriterResponse.setFileSize(0L);
		}
		return fileWriterResponse;
	}

	public FileWriterResponse getFileDetails(String reportName, String fileLocation,String fileName, String businessId, String userId,String fileExtension,String logKey) {
		File csvFile = new File(fileLocation+businessId+"/"+reportName+"/"+userId+"/"+fileName+fileExtension);
		FileWriterResponse fileWriterResponse = new FileWriterResponse();
		try {
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_OK_CODE);
			fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_OK);
			fileWriterResponse.setFilePath(csvFile.getPath());
			fileWriterResponse.setFileSize(csvFile.length());

		}catch(Exception e) {
			log.info("{} Unable to getFileDetails for business {} for userId {}, due to {} ",logKey,businessId,userId,e);
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
			fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			fileWriterResponse.setFilePath(null);
			fileWriterResponse.setFileSize(0L);
			return fileWriterResponse;
		}

		return fileWriterResponse;
	}
	
	public FileWriterResponse deleteFile(String reportName, String fileLocation,String fileName, String businessId, String userId,String fileExtension,String logKey) {
		String filePath = fileLocation+businessId+"/"+reportName+"/"+userId+"/"+fileName+fileExtension;
		File csvFile = new File(filePath);
		FileWriterResponse fileWriterResponse = new FileWriterResponse();
		try {
			if(csvFile.exists()) {
				log.info("{} File {} exist, so deleting for business {} for userId {} ",logKey,filePath,businessId,userId);
				csvFile.delete();
				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_OK_CODE);
				fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_OK);
			}else {
				log.info("{} File {} doesn't exist, so can't delete for business {} for userId {} ",logKey,filePath,businessId,userId);
				fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_NO_DATA_FOUND_CODE);
				fileWriterResponse.setStatusMessage(DispatchControllerConstants.FILE_NOT_FOUND);
			}
			
			fileWriterResponse.setFilePath(null);
			fileWriterResponse.setFileSize(0L);

		}catch(Exception e) {
			log.info("{} Unable to deleteFile for business {} for userId {}, due to {}",logKey,businessId,userId,e);
			fileWriterResponse.setStatusCode(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR_CODE);
			fileWriterResponse.setStatusMessage(DispatchControllerConstants.RESPONSE_INTER_SERVER_ERROR);
			fileWriterResponse.setFilePath(null);
			fileWriterResponse.setFileSize(0L);
			return fileWriterResponse;
		}

		return fileWriterResponse;
	}
}
