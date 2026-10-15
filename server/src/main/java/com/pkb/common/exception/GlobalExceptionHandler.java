package com.pkb.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import com.pkb.common.result.Result;
import com.pkb.common.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：任何异常都收敛成统一返回体 {@code {code, message, data}}。
 *
 * <p>约定：HTTP 状态码一律 200，真实语义由 {@code code} 承载（前端只判断 code）。
 * <p>兜底分支只回通用文案，堆栈只进日志，避免把内部细节暴露到响应里。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：按异常自带的 code 返回 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** @RequestBody 上的 @Valid 校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return Result.fail(ResultCode.PARAM_ERROR, fieldMessage(e.getBindingResult().getFieldError()));
    }

    /** 表单/查询参数绑定校验失败 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        return Result.fail(ResultCode.PARAM_ERROR, fieldMessage(e.getBindingResult().getFieldError()));
    }

    /** 方法参数上的约束校验失败（@Validated + @RequestParam 等） */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(ResultCode.PARAM_ERROR.getMessage());
        log.warn("参数校验失败：{}", message);
        return Result.fail(ResultCode.PARAM_ERROR, message);
    }

    /** 缺少必填参数 / 参数类型不匹配 / 请求体解析失败 */
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public Result<Void> handleBadRequest(Exception e) {
        log.warn("请求参数有误：{}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR);
    }

    /**
     * 上传时缺少文件部件（如「未选文件直接提交」）。
     *
     * <p>不单独处理的话会落到兜底分支变成 50000「系统繁忙」，用户不知道自己少选了个文件。
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Result<Void> handleMissingPart(MissingServletRequestPartException e) {
        log.warn("缺少上传部件：{}", e.getRequestPartName());
        return Result.fail(ResultCode.PARAM_ERROR, "请选择要上传的文件");
    }

    /** 请求方式不被支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方式不支持：{}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR, "请求方式不被支持：" + e.getMethod());
    }

    /**
     * 上传文件超过 spring.servlet.multipart 限制。
     *
     * <p>不拦的话会冒成 500 系统异常，用户看到的是"系统繁忙"，无法判断是自己文件太大。
     * 这里只说通用上限：图片另有 2MB 的业务校验（文案更精确），能走到这里说明已经超过全局闸门。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("上传文件超限：{}", e.getMessage());
        return Result.fail(ResultCode.PARAM_ERROR, "上传的文件过大，请压缩或拆分后重试");
    }

    /** 访问了不存在的接口/资源 */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound(NoResourceFoundException e) {
        log.warn("请求的资源不存在：{}", e.getResourcePath());
        return Result.fail(ResultCode.NOT_FOUND);
    }

    /** Sa-Token：未登录 */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录访问：{}", e.getMessage());
        return Result.fail(ResultCode.UNAUTHORIZED);
    }

    /**
     * ⚠️ 与 OSC 的差异：**没有「无权限 / 角色不足」的 handler**。
     *
     * <p>PKB 是单账号系统，没有权限矩阵（PRD §2.2），
     * 故 {@code ResultCode} 里不再有 {@code FORBIDDEN}，也不再引用
     * {@code NotPermissionException} / {@code NotRoleException}。
     */

    /** 兜底：未知异常 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.SYSTEM_ERROR);
    }

    private String fieldMessage(FieldError fieldError) {
        if (fieldError == null) {
            return ResultCode.PARAM_ERROR.getMessage();
        }
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }
}
