//package cn.toint.okauth.server.sms.controller;
//
//import cn.toint.okauth.server.model.Response;
//import cn.toint.okauth.server.sms.service.SmsService;
//import cn.toint.okauth.server.user.model.UserLoginSendSmsRequest;
//import cn.toint.okauth.server.sms.model.SmsCodeResponse;
//import jakarta.annotation.Resource;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * 短信
// */
//@RestController
//public class SmsController {
//
//    @Resource
//    private SmsService smsService;
//
//    /**
//     * 发送验证码
//     */
//    @PostMapping("/sms/code")
//    public Response<SmsCodeResponse> code(@RequestBody UserLoginSendSmsRequest request) {
//        SmsCodeResponse smsCodeResponse = smsService.code(request);
//        return Response.success(smsCodeResponse);
//    }
//}
