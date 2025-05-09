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

package org.apache.seatunnel.app.utils;

import org.apache.seatunnel.app.common.Result;
import org.apache.seatunnel.common.access.AccessDeniedException;
import org.apache.seatunnel.datasource.plugin.api.DataSourcePluginException;
import org.apache.seatunnel.server.common.ParamValidationException;
import org.apache.seatunnel.server.common.SeatunnelErrorEnum;
import org.apache.seatunnel.server.common.SeatunnelException;

import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = SeatunnelException.class)
    private Result<String> portalExceptionHandler(SeatunnelException e) {
        logDebug(e);
        return Result.failure(e);
    }

    @ExceptionHandler(value = DataSourcePluginException.class)
    private Result<String> dsHandler(DataSourcePluginException e) {
        logError(e);
        return Result.failure(
                SeatunnelErrorEnum.INVALID_DATASOURCE.getCode(),
                SeatunnelErrorEnum.INVALID_DATASOURCE.getMsg() + ". " + e.getMessage());
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    private Result<String> missParam(MissingServletRequestParameterException e) {
        logDebug(e);
        return Result.failure(SeatunnelErrorEnum.MISSING_PARAM, e.getParameterName());
    }

    @ExceptionHandler(value = IllegalStateException.class)
    private Result<String> illegalStateExceptionHandler(IllegalStateException e) {
        logError(e);
        return Result.failure(SeatunnelErrorEnum.ILLEGAL_STATE, e.getMessage());
    }

    @ExceptionHandler(value = ExpiredJwtException.class)
    private Result<String> expiredJwtException(ExpiredJwtException e) {
        logDebug(e);
        return Result.failure(SeatunnelErrorEnum.TOKEN_ILLEGAL, e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    private Result<String> exceptionHandler(Exception e) {
        logError(e);
        return Result.failure(
                SeatunnelErrorEnum.UNKNOWN.getCode(),
                SeatunnelErrorEnum.UNKNOWN.getMsg() + ". " + e.getMessage());
    }

    private void logError(Throwable throwable) {
        log.error(throwable.getMessage(), throwable);
    }

    private void logDebug(Throwable throwable) {
        if (log.isDebugEnabled()) {
            log.debug(throwable.getMessage(), throwable);
        }
    }

    @ExceptionHandler(value = ParamValidationException.class)
    private Result<String> paramValidationHandler(SeatunnelException e) {
        return Result.failure(e);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    private Result<String> paramValidationHandler(AccessDeniedException e) {
        return Result.failure(SeatunnelErrorEnum.ACCESS_DENIED, e.getMessage());
    }
}
