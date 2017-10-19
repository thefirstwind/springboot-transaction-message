package com.tdw.transaction.exception;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tdw.transaction.util.Result;


@ControllerAdvice
public class RunTimeExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(RunTimeExceptionHandler.class);

    @ExceptionHandler(value = ServiceException.class)
    @ResponseBody
    public ResponseEntity<Result<String>> ServiceExceptionHandler(HttpServletRequest req, ServiceException e) throws Exception {
        logger.error("ServiceException：{}",e);
        return new ResponseEntity<Result<String>>(new Result<String>(e.getCode(),e.getMessage(),null), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(value = NullPointerException.class)
    @ResponseBody
    public ResponseEntity<Result<String>> NullPointerExceptionHandler(HttpServletRequest req, NullPointerException e) throws Exception {
        logger.error("NullPointerException：{}",e);
        return new ResponseEntity<Result<String>>(new Result<String>(HttpStatus.NOT_FOUND.value(),e.getMessage(),null), HttpStatus.NOT_FOUND);
    }
    
}