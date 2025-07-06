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

package cn.toint.okauth.server.user.event;

import cn.toint.okauth.server.user.model.UserLoginResponse;
import org.springframework.context.ApplicationEvent;

/**
 * 用户登录事件
 *
 * @author Toint
 * @date 2025/7/2
 */
public class UserLoginEvent extends ApplicationEvent {

    public UserLoginEvent(UserLoginResponse source) {
        super(source);
    }

    @Override
    public UserLoginResponse getSource() {
        return (UserLoginResponse) super.getSource();
    }
}
