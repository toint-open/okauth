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

create table if not exists okauth_open_client
(
    id                  bigint                              not null
        primary key,
    create_time         timestamp default CURRENT_TIMESTAMP not null,
    update_time         timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    name                varchar(50)                         not null,
    subject_id          varchar(50)                         not null,
    secret              varchar(50)                         not null,
    allow_redirect_urls text                                null,
    status              int                                 not null
);

create index id_create_time
    on okauth_open_client (create_time);

create index id_name
    on okauth_open_client (name);

create index id_status
    on okauth_open_client (status);

create index id_subject_id
    on okauth_open_client (subject_id);

create index id_update_time
    on okauth_open_client (update_time);

create table if not exists okauth_user
(
    id          bigint                              not null
        primary key,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    name        varchar(20)                         not null,
    phone       varchar(20)                         null,
    constraint uid_phone
        unique (phone)
);