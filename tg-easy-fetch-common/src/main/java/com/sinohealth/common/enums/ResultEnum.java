/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sinohealth.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * process define release state
 */
public enum ResultEnum {

    /**
     * 0 offline
     * 1 on line
     */
/*
    OFFLINE(0, "offline"),
    ONLINE(1, "online");
*/


	//resultEnum中的参数
	ERROR_MOBILE(1001002,"请填写正确的手机号码"),

	MESSAGE_TIME_ERROR(1001003,"60秒内只能发送一次短信,请稍后再试"),

	MESSAGE_SEND_ERROR(1001004,"发送短信出错,请稍后再试"),

	MOBILE_NOT_SEND_CODE(1001005,"该手机号未获取验证码,请先获取验证码"),

	MOBILE_CAN_NOT_CHANGE(1001006,"此手机号与获取验证码的手机号不一致"),

	MESSAGE_CODE_INVALID(1001007,"此验证码失效,请重新获取验证码"),

	MESSAGE_CODE_ERROR(1001008,"验证码错误");




    ResultEnum(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    @EnumValue
    private final int code;
    private final String msg;

    public static ResultEnum getEnum(int value){
        for (ResultEnum e: ResultEnum.values()) {
            if(e.ordinal() == value) {
                return e;
            }
        }
        //For values out of enum scope
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
