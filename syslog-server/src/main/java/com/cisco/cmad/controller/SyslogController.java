package com.cisco.cmad.controller;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

//import javax.persistence.Temporal;
//import javax.persistence.TemporalType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cisco.cmad.dao.SyslogRepository;
import com.cisco.cmad.dto.SeverityStatistics;
import com.cisco.cmad.model.Syslog;

@RestController
@CrossOrigin
public class SyslogController {

	@Autowired
	private SyslogRepository repo;

	//create a single log
	@RequestMapping(path = "/log", method = RequestMethod.POST)
	public ResponseEntity<Syslog> add(@RequestBody Syslog log) {
		repo.save(log);
		return new ResponseEntity<Syslog>(log, HttpStatus.CREATED);
	}
	
	//find all logs
	@RequestMapping(path = "/logs", method = RequestMethod.GET)
	public ResponseEntity<List<Syslog>> findAllLogs() {
		List<Syslog> logs = repo.findAll();
		return new ResponseEntity<List<Syslog>>(logs, HttpStatus.OK);
	}
	
	//get logs with start and end time
	@RequestMapping(path = "/log", method = RequestMethod.GET)
	public ResponseEntity<List<Syslog>> findByTimePeriod(@RequestParam(name = "startTime") Timestamp startTime, @RequestParam(name = "endTime") Timestamp endTime) {
		List<Syslog> logs = repo.findByTimestampBetween(startTime,endTime);
		return new ResponseEntity<List<Syslog>>(logs, HttpStatus.OK);
	}
	
	//Returns an array of arrays with severity and corresponding count
	@RequestMapping(path = "/log/severity/count", method = RequestMethod.GET)
	public ResponseEntity<List<SeverityStatistics>> getStats(@RequestParam(name = "startTime") Timestamp startTime, @RequestParam(name = "endTime") Timestamp endTime) {
//		List<SeverityStatistics> count = repo.syslogCountBySeverityInTimePeriod(startTime, endTime);
//		return new ResponseEntity<List<SeverityStatistics>>(count, HttpStatus.OK);
//		Query query = new Query();
		
		MatchOperation filterLogs = Aggregation.match(new Criteria("timestamp").gt(startTime).lt(endTime));
		GroupOperation groupBySeverityAndSumDocuments = Aggregation.group("severity").count().as("count");
		Aggregation aggregation = Aggregation.newAggregation(filterLogs,groupBySeverityAndSumDocuments);
		AggregationResults<SeverityStatistics> result = mongoTemplate.aggregate(aggregation);
	}	
	
}
