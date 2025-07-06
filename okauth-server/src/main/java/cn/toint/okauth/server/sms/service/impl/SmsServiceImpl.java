//package cn.toint.okauth.server.sms.service.impl;
//
//import cn.toint.okauth.server.openclient.model.OpenClientDo;
//import cn.toint.okauth.server.openclient.service.OkAuthOpenClientService;
//import cn.toint.okauth.server.user.model.UserLoginSendSmsRequest;
//import cn.toint.okauth.server.sms.model.SmsCodeResponse;
//import cn.toint.okauth.server.sms.service.SmsService;
//import cn.toint.oksms.aliyun.AliyunSmsClient;
//import cn.toint.oksms.aliyun.model.AliyunRegionEnum;
//import cn.toint.oksms.aliyun.model.AliyunSmsClientConfig;
//import cn.toint.oksms.aliyun.model.AliyunSmsSendRequest;
//import cn.toint.oksms.util.SmsUtil;
//import cn.toint.oktool.util.Assert;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.dromara.hutool.core.lang.Validator;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Slf4j
//@Service
//public class SmsServiceImpl implements SmsService {
//    @Resource
//    private OkAuthOpenClientService okAuthOpenClientService;
//
//    @Override
//    public SmsCodeResponse code(UserLoginSendSmsRequest request) {
//        Assert.notNull(request, "发送验证码请求参数不能为空");
//        Assert.validate(request, "发送验证码请求参数校验失败: {}");
//
//        String phone = request.getPhone();
//        Assert.isTrue(Validator.isMobile(phone), "请输入正确的手机号码");
//
//        // 获取开放应用
//        String clientId = request.getClientId();
//        OpenClientDo openClientDo = okAuthOpenClientService.getById(Long.valueOf(clientId));
//        Assert.notNull(openClientDo, "当前开放应用[{}]不存在", clientId);
//
//        // 短信配置
//        String smsKey = openClientDo.getSmsKey();
//        Assert.notBlank(smsKey, "开放应用未开通短信验证码");
//        String smsSecret = openClientDo.getSmsSecret();
//        Assert.notBlank(smsSecret, "开放应用未开通短信验证码");
//        String smsSignName = openClientDo.getSmsSignName();
//        Assert.notBlank(smsSignName, "开放应用未开通短信验证码");
//        String smsTemplateCode = openClientDo.getSmsTemplateCode();
//        Assert.notBlank(smsTemplateCode, "开放应用未开通短信验证码");
//
//        AliyunSmsClientConfig aliyunSmsClientConfig = new AliyunSmsClientConfig();
//        aliyunSmsClientConfig.setAccessKeyId(smsKey);
//        aliyunSmsClientConfig.setAccessKeySecret(smsSecret);
//        aliyunSmsClientConfig.regionId(AliyunRegionEnum.CN_HANGZHOU);
//        AliyunSmsClient aliyunSmsClient = SmsUtil.aliyunSms(aliyunSmsClientConfig);
//
//        AliyunSmsSendRequest aliyunSmsSendRequest = new AliyunSmsSendRequest();
//        aliyunSmsSendRequest.setPhoneNumbers(List.of(phone));
//        aliyunSmsSendRequest.setSignName(smsSignName);
//        aliyunSmsSendRequest.setTemplateCode(smsTemplateCode);
//        aliyunSmsSendRequest.setTemplateParam(Map.of);
//
//        aliyunSmsClient.send(aliyunSmsSendRequest);
//
//        return null;
//    }
//}
