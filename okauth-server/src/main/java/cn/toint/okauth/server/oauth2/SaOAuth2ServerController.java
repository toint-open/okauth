/*
 * Copyright 2025 Toint (599818663@qq.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.toint.okauth.server.oauth2;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.oauth2.SaOAuth2Manager;
import cn.dev33.satoken.oauth2.config.SaOAuth2ServerConfig;
import cn.dev33.satoken.oauth2.consts.GrantType;
import cn.dev33.satoken.oauth2.consts.SaOAuth2Consts;
import cn.dev33.satoken.oauth2.data.generate.SaOAuth2DataGenerate;
import cn.dev33.satoken.oauth2.data.model.AccessTokenModel;
import cn.dev33.satoken.oauth2.data.model.CodeModel;
import cn.dev33.satoken.oauth2.data.model.loader.SaClientModel;
import cn.dev33.satoken.oauth2.data.model.request.RequestAuthModel;
import cn.dev33.satoken.oauth2.error.SaOAuth2ErrorCode;
import cn.dev33.satoken.oauth2.exception.SaOAuth2Exception;
import cn.dev33.satoken.oauth2.processor.SaOAuth2ServerProcessor;
import cn.dev33.satoken.oauth2.strategy.SaOAuth2Strategy;
import cn.dev33.satoken.oauth2.template.SaOAuth2Template;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaFoxUtil;
import cn.dev33.satoken.util.SaResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Sa-Token OAuth2 Server端 控制器
 */
@RestController
public class SaOAuth2ServerController {

    @Resource
    private SaOAuth2ServerConfig oauth2Server;


    // OAuth2-Server 端：处理所有 OAuth2 相关请求
    @GetMapping("/oauth2/userinfo")
    public Object userinfo() {
       return Map.of("userid", "1");
    }

    // OAuth2-Server 端：处理所有 OAuth2 相关请求
    @RequestMapping("/oauth2/*")
    public Object request() {
        System.out.println("------- 进入请求: " + SaHolder.getRequest().getUrl());
        return SaOAuth2ServerProcessor.instance.dister();
    }

    // Sa-Token OAuth2 定制化配置
    @PostConstruct
    public void configOAuth2Server() {

        this.oauth2Server.isNewRefresh = false;

        // 添加 client 信息
        oauth2Server.addClient(
                new SaClientModel()
                        .setClientId("1001")    // client id
                        .setClientSecret("aaaa-bbbb-cccc-dddd-eeee")    // client 秘钥
                        .addAllowRedirectUris("*")    // 所有允许授权的 url
                        .addContractScopes("unionid", "openid", "userid", "userinfo")    // 所有签约的权限
                        .addAllowGrantTypes(     // 所有允许的授权模式
                                GrantType.authorization_code, // 授权码式
                                GrantType.refresh_token  // 刷新令牌
                        )
        );

        // 可以添加更多 client 信息，只要保持 clientId 唯一就行了
        // oauth2Server.addClient(...)

        // 配置：未登录时返回的View
        SaOAuth2Strategy.instance.notLoginView = () -> {
            // 简化模拟表单
            String doLoginCode =
                    "fetch(`/oauth2/doLogin?name=${document.querySelector('#name').value}&pwd=${document.querySelector('#pwd').value}`) " +
                            " .then(res => res.json()) " +
                            " .then(res => { if(res.code === 200) { location.reload() } else { alert(res.msg) } } )";
            String res =
                    "<h2>当前客户端在 OAuth-Server 认证中心尚未登录，请先登录</h2>" +
                            "用户：<input id='name' /> <br> " +
                            "密码：<input id='pwd' /> <br>" +
                            "<button onclick=\"" + doLoginCode + "\">登录</button>";
            return res;
        };

        // 配置：登录处理函数
        SaOAuth2Strategy.instance.doLoginHandle = (name, pwd) -> {
            if ("sa".equals(name) && "123456".equals(pwd)) {
                StpUtil.login(10001);
                return SaResult.ok();
            }
            return SaResult.error("账号名或密码错误");
        };

        // 配置：确认授权时返回的 view
        SaOAuth2Strategy.instance.confirmView = (clientId, scopes) -> {
            String scopeStr = SaFoxUtil.convertListToString(scopes);
            String yesCode =
                    "fetch('/oauth2/doConfirm?client_id=" + clientId + "&scope=" + scopeStr + "', {method: 'POST'})" +
                            ".then(res => res.json())" +
                            ".then(res => location.reload())";
            String res = "<p>应用 " + clientId + " 请求授权：" + scopeStr + "，是否同意？</p>"
                    + "<p>" +
                    "        <button onclick=\"" + yesCode + "\">同意</button>" +
                    "        <button onclick='history.back()'>拒绝</button>" +
                    "</p>";
            return res;
        };
    }


    /**
     * 获取最终授权重定向地址，形如：http://xxx.com/xxx?code=xxxxx
     *
     * <p> 情况1：客户端未登录，返回 code=401，提示用户登录 <p/>
     * <p> 情况2：请求的 scope 需要客户端手动确认授权，返回 code=411，提示用户手动确认 <p/>
     * <p> 情况3：已登录且请求的 scope 已确认授权，返回 code=200，redirect_uri=最终重定向 url 地址(携带code码参数) <p/>
     *
     * @return /
     */
    @PostMapping("/oauth2/getRedirectUri")
    public Object getRedirectUri() {

        // 获取变量
        SaRequest req = SaHolder.getRequest();
        SaOAuth2ServerConfig cfg = SaOAuth2Manager.getServerConfig();
        SaOAuth2DataGenerate dataGenerate = SaOAuth2Manager.getDataGenerate();
        SaOAuth2Template oauth2Template = SaOAuth2Manager.getTemplate();
        String responseType = req.getParamNotNull(SaOAuth2Consts.Param.response_type);

        // 1、先判断是否开启了指定的授权模式
        SaOAuth2ServerProcessor.instance.checkAuthorizeResponseType(responseType, req, cfg);

        // 2、如果尚未登录, 则先去登录
        long loginId = SaOAuth2Manager.getStpLogic().getLoginId(0L);
        if(loginId == 0L) {
            return SaResult.get(401, "need login", null);
        }

        // 3、构建请求 Model
        RequestAuthModel ra = SaOAuth2Manager.getDataResolver().readRequestAuthModel(req, loginId);

        // 4、开发者自定义的授权前置检查
        SaOAuth2Strategy.instance.userAuthorizeClientCheck.run(ra.loginId, ra.clientId);

        // 5、校验：重定向域名是否合法
        oauth2Template.checkRedirectUri(ra.clientId, ra.redirectUri);

        // 6、校验：此次申请的Scope，该Client是否已经签约
        oauth2Template.checkContractScope(ra.clientId, ra.scopes);

        // 7、判断：如果此次申请的Scope，该用户尚未授权，则转到授权页面
        boolean isNeedCarefulConfirm = oauth2Template.isNeedCarefulConfirm(ra.loginId, ra.clientId, ra.scopes);
        if(isNeedCarefulConfirm) {
            SaClientModel cm = oauth2Template.checkClientModel(ra.clientId);
            if( ! cm.getIsAutoConfirm()) {
                // code=411，需要用户手动确认授权
                return SaResult.get(411, "need confirm", null);
            }
        }

        // 8、判断授权类型，重定向到不同地址
        //         如果是 授权码式，则：开始重定向授权，下放code
        if(SaOAuth2Consts.ResponseType.code.equals(ra.responseType)) {
            CodeModel codeModel = dataGenerate.generateCode(ra);
            String redirectUri = dataGenerate.buildRedirectUri(ra.redirectUri, codeModel.code, ra.state);
            return SaResult.ok().set("redirect_uri", redirectUri);
        }

        //         如果是 隐藏式，则：开始重定向授权，下放 token
        if(SaOAuth2Consts.ResponseType.token.equals(ra.responseType)) {
            AccessTokenModel at = dataGenerate.generateAccessToken(ra, false, null);
            String redirectUri = dataGenerate.buildImplicitRedirectUri(ra.redirectUri, at.accessToken, ra.state);
            return SaResult.ok().set("redirect_uri", redirectUri);
        }

        // 默认返回
        throw new SaOAuth2Exception("无效 response_type: " + ra.responseType).setCode(SaOAuth2ErrorCode.CODE_30125);
    }


}
