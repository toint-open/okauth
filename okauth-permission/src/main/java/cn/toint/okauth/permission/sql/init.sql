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

create table if not exists okauth_dept
(
    id          bigint                              not null
    primary key,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    parent_id   bigint                              not null,
    name        varchar(100)                        not null,
    remark      text                                null,
    constraint uid_parentid_id
    unique (parent_id, id)
    );

create index id_name
    on okauth_dept (name);

create table if not exists okauth_dept_mtm_permission
(
    id            bigint                              not null
    primary key,
    create_time   timestamp default CURRENT_TIMESTAMP not null,
    update_time   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    dept_id       bigint                              not null,
    permission_id bigint                              not null,
    constraint uid_deptid_permissionid
    unique (dept_id, permission_id)
    );

create index id_dept_id
    on okauth_dept_mtm_permission (dept_id);

create index id_permission_id
    on okauth_dept_mtm_permission (permission_id);

create table if not exists okauth_permission
(
    id             bigint                              not null
    primary key,
    create_time    timestamp default CURRENT_TIMESTAMP not null,
    update_time    timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    parent_id      bigint                              not null,
    type           int                                 not null,
    name           varchar(50)                         not null,
    path           varchar(255)                        null,
    component      varchar(255)                        null,
    component_name varchar(255)                        null,
    icon           varchar(255)                        null,
    `order`        int                                 not null,
    code           varchar(255)                        not null,
    constraint uid_code
    unique (code),
    constraint uid_parentid_id
    unique (parent_id, id)
    );

create index id_parent_id
    on okauth_permission (parent_id);

create table if not exists okauth_role
(
    id          bigint                              not null
    primary key,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    code        varchar(100)                        not null,
    name        varchar(100)                        not null,
    remark      text                                null,
    constraint uid_code
    unique (code)
    );

create index id_name
    on okauth_role (name);

create table if not exists okauth_role_mtm_permission
(
    id            bigint                              not null
    primary key,
    create_time   timestamp default CURRENT_TIMESTAMP not null,
    update_time   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    role_id       bigint                              not null,
    permission_id bigint                              not null,
    constraint uid_roleid_permissionid
    unique (role_id, permission_id)
    );

create index id_permission_id
    on okauth_role_mtm_permission (permission_id);

create index id_role_id
    on okauth_role_mtm_permission (role_id);

create table if not exists okauth_user_mtm_dept
(
    id          bigint                              not null
    primary key,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    user_id     bigint                              not null,
    dept_id     bigint                              not null,
    constraint uid_userid_deptid
    unique (user_id, dept_id)
    );

create index id_dept_id
    on okauth_user_mtm_dept (dept_id);

create index id_user_id
    on okauth_user_mtm_dept (user_id);

create table if not exists okauth_user_mtm_permission
(
    id            bigint                              not null
    primary key,
    create_time   timestamp default CURRENT_TIMESTAMP not null,
    update_time   timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    user_id       bigint                              not null,
    permission_id bigint                              not null,
    constraint uid_userid_permissionid
    unique (user_id, permission_id)
    );

create index id_permission_id
    on okauth_user_mtm_permission (permission_id);

create index id_user_id
    on okauth_user_mtm_permission (user_id);

create table if not exists okauth_user_mtm_role
(
    id          bigint                              not null
    primary key,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    user_id     bigint                              not null,
    role_id     bigint                              not null,
    constraint uid_userid_roleid
    unique (user_id, role_id)
    );

create index id_role_id
    on okauth_user_mtm_role (role_id);

create index id_user_id
    on okauth_user_mtm_role (user_id);

