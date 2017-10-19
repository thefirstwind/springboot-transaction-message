package com.tdw.transaction.controller;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;



@RestController
@RequestMapping(value = "/api")
public class ApiController {

	private static final Logger logger = LoggerFactory.getLogger(ApiController.class);


    @ApiOperation(value = "測試", notes = "hello world")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "名字", paramType = "query", dataType = "string")
    })
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public HashMap<String, Object> get(@RequestParam String name) {
		
    	logger.info("get: {}",name);
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("title", "hello world");
		map.put("name", name);
		
		return map;
	}
	
		
}