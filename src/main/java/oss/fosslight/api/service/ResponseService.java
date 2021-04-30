/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only 
 */

package oss.fosslight.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import oss.fosslight.api.entity.CommonResult;
import oss.fosslight.api.entity.ListResult;
import oss.fosslight.api.entity.SingleResult;

@Service
public class ResponseService {
	
    // enum으로 api 요청 결과에 대한 code, message를 정의합니다.
    public enum CommonResponse {
        
        /** The success. */
        SUCCESS("100", "");

        /** The code. */
        String code;
        
        /** The msg. */
        String msg;
        
        CommonResponse(String code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMsg() {
            return msg;
        }
    }
    
    // 단일건 결과를 처리하는 메소드
    public <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }
    
    // 다중건 결과를 처리하는 메소드
    public <T> ListResult<T> getListResult(List<T> list) {
        ListResult<T> result = new ListResult<>();
        result.setList(list);
        setSuccessResult(result);
        return result;
    }
    
    // 성공 결과만 처리하는 메소드
    public CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return result;
    }
    
    // 실패 결과만 처리하는 메소드
    public CommonResult getFailResult(String code, String msg) {
        CommonResult result = new CommonResult();
        result.setSuccess(false);
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
    
    // 결과 모델에 api 요청 성공 데이터를 세팅해주는 메소드
    private void setSuccessResult(CommonResult result) {
        result.setSuccess(true);
        result.setCode(CommonResponse.SUCCESS.getCode());
        result.setMsg(CommonResponse.SUCCESS.getMsg());
    }

}
