create table if not exists dict
(
    id          bigint                              not null
    primary key,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    type        varchar(200)                        not null,
    `key`       varchar(200)                        not null,
    value       text                                not null,
    remark      text                                null,
    constraint uid_type_key
    unique (type, `key`)
    );

create table if not exists permission
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
    sort           int                                 not null,
    code           varchar(255)                        null,
    constraint uid_code
    unique (code),
    constraint uid_parentid_id
    unique (parent_id, id)
    );

create index id_parent_id
    on permission (parent_id);

create table if not exists role
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
    on role (name);

create table if not exists role_mtm_permission
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
    on role_mtm_permission (permission_id);

create index id_role_id
    on role_mtm_permission (role_id);

create table if not exists user_mtm_dept
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
    on user_mtm_dept (dept_id);

create index id_user_id
    on user_mtm_dept (user_id);

create table if not exists user_mtm_role
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
    on user_mtm_role (role_id);

create index id_user_id
    on user_mtm_role (user_id);

