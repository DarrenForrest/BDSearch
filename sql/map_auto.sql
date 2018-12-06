---------------------------------------------------------
-- Export file for user MAP_AUTO                       --
-- Created by Administrator on 2018\12\6 星期四, 10:55:06 --
---------------------------------------------------------

set define off
spool map_auto.log

prompt
prompt Creating table MAP_RESOURCE
prompt ===========================
prompt
create table MAP_AUTO.MAP_RESOURCE
(
  first_type  VARCHAR2(20),
  second_type VARCHAR2(20),
  remark      VARCHAR2(100)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 8K
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table MAP_STATION_INFO
prompt ===============================
prompt
create table MAP_AUTO.MAP_STATION_INFO
(
  celllac    VARCHAR2(50),
  cellci     VARCHAR2(50),
  laclong    BINARY_DOUBLE,
  laclat     BINARY_DOUBLE,
  laccode    VARCHAR2(200),
  lacname    VARCHAR2(200),
  title      VARCHAR2(200),
  address    VARCHAR2(500),
  lng        BINARY_DOUBLE,
  lat        BINARY_DOUBLE,
  citycode   VARCHAR2(50),
  firsttype  VARCHAR2(200),
  secondtype VARCHAR2(200),
  createtime VARCHAR2(30),
  range      VARCHAR2(4000),
  net_type   VARCHAR2(20)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_AMS_ACTION_LOG
prompt ================================
prompt
create table MAP_AUTO.TB_AMS_ACTION_LOG
(
  id             NUMBER(10) not null,
  ams_staff_id   NUMBER(10),
  ams_staff_name VARCHAR2(100),
  work_date      VARCHAR2(10),
  action         VARCHAR2(100),
  plan_time      DATE,
  exe_time       DATE,
  exe_code       VARCHAR2(100),
  exe_msg        VARCHAR2(4000),
  exe_err_msg    VARCHAR2(4000),
  create_date    DATE
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_AMS_ACTION_LOG
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_AMS_STAFF
prompt ===========================
prompt
create table MAP_AUTO.TB_AMS_STAFF
(
  id                 NUMBER(10) not null,
  login_name         VARCHAR2(100) not null,
  password           VARCHAR2(100),
  staff_name         VARCHAR2(100),
  remark             VARCHAR2(500),
  state              NUMBER(3),
  admin_name         VARCHAR2(100),
  email              VARCHAR2(100),
  lock_state         VARCHAR2(10),
  auto_login_flag    VARCHAR2(10),
  auto_loginout_flag VARCHAR2(10),
  email_notice_flag  VARCHAR2(10),
  create_date        DATE,
  update_date        DATE
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_AMS_STAFF
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_CATALOG_MANAGE
prompt ================================
prompt
create table MAP_AUTO.TB_CATALOG_MANAGE
(
  id          NUMBER not null,
  name        VARCHAR2(100),
  path        VARCHAR2(100),
  create_date DATE,
  ip          VARCHAR2(50)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
comment on column MAP_AUTO.TB_CATALOG_MANAGE.name
  is '目录名称';
comment on column MAP_AUTO.TB_CATALOG_MANAGE.path
  is '路径';
alter table MAP_AUTO.TB_CATALOG_MANAGE
  add constraint ID_CATALOG_MANAGE primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_ENUM_CATALOG
prompt ==============================
prompt
create table MAP_AUTO.TB_ENUM_CATALOG
(
  id           NUMBER(6) not null,
  catalog_code VARCHAR2(100) not null,
  catalog_name VARCHAR2(100) not null,
  state        NUMBER(2) not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_ENUM_CATALOG
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_ENUM_CATALOG
  add unique (CATALOG_CODE)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_ENUM_CFG
prompt ==========================
prompt
create table MAP_AUTO.TB_ENUM_CFG
(
  id          NUMBER(6) not null,
  enum_key    VARCHAR2(300) not null,
  enum_value  VARCHAR2(100) not null,
  catalog_id  NUMBER(3),
  order_num   NUMBER(2),
  pid         VARCHAR2(300),
  update_date DATE not null,
  state       NUMBER(2) not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_ENUM_CFG
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_ENUM_CFG
  add unique (CATALOG_ID, ENUM_KEY)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_FILE
prompt ======================
prompt
create table MAP_AUTO.TB_FILE
(
  id           NUMBER(15) not null,
  file_name    VARCHAR2(256) not null,
  file_path    VARCHAR2(256),
  file_type_id NUMBER(6) not null,
  file_header  VARCHAR2(1000),
  remark       VARCHAR2(200),
  staff_id     NUMBER(12) not null,
  state        NUMBER(2) not null,
  state_date   DATE not null,
  create_date  DATE not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_FILE
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_HOLIDAY
prompt =========================
prompt
create table MAP_AUTO.TB_HOLIDAY
(
  id           NUMBER(5) not null,
  holiday_type NUMBER(1) default 0,
  holiday      DATE not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_HOST_INFO
prompt ===========================
prompt
create table MAP_AUTO.TB_HOST_INFO
(
  id          NUMBER not null,
  ip          VARCHAR2(100),
  projectname VARCHAR2(20),
  address     VARCHAR2(100),
  file_name   VARCHAR2(100),
  info        CLOB,
  create_time VARCHAR2(50),
  time        VARCHAR2(50),
  file_type   VARCHAR2(20)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
comment on column MAP_AUTO.TB_HOST_INFO.projectname
  is '??';
comment on column MAP_AUTO.TB_HOST_INFO.address
  is '??';
comment on column MAP_AUTO.TB_HOST_INFO.file_name
  is '???';
comment on column MAP_AUTO.TB_HOST_INFO.info
  is '??';
comment on column MAP_AUTO.TB_HOST_INFO.time
  is '????????';
comment on column MAP_AUTO.TB_HOST_INFO.file_type
  is '????????';
alter table MAP_AUTO.TB_HOST_INFO
  add constraint ID_HOST_INFO primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_HOST_MANAGE
prompt =============================
prompt
create table MAP_AUTO.TB_HOST_MANAGE
(
  id          NUMBER not null,
  ip          VARCHAR2(100),
  user_name   VARCHAR2(100),
  pass_word   VARCHAR2(100),
  name        VARCHAR2(100),
  backup_host CHAR(1),
  create_date DATE
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
comment on column MAP_AUTO.TB_HOST_MANAGE.backup_host
  is '是否备份主机，“0”否，“1”是';
alter table MAP_AUTO.TB_HOST_MANAGE
  add constraint ID_HOST_MANAGE primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_INTERFACE_LOG
prompt ===============================
prompt
create table MAP_AUTO.TB_INTERFACE_LOG
(
  id               NUMBER(15) not null,
  url              VARCHAR2(1024),
  parameter        VARCHAR2(1024),
  request_content  CLOB,
  response_content VARCHAR2(1024),
  request_date     DATE,
  response_date    DATE,
  create_date      DATE not null,
  type             VARCHAR2(50),
  local_addr       VARCHAR2(50),
  local_port       VARCHAR2(50),
  remote_addr      VARCHAR2(50),
  remote_port      VARCHAR2(50)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_INTERFACE_LOG
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_JOB_DEFINE
prompt ============================
prompt
create table MAP_AUTO.TB_JOB_DEFINE
(
  id               NUMBER(10) not null,
  job_name         VARCHAR2(128) not null,
  job_class        VARCHAR2(128) not null,
  stored_procedure VARCHAR2(128),
  cron_expression  VARCHAR2(64) not null,
  job_desc         VARCHAR2(1024) not null,
  status           NUMBER(1) not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_JOB_LOG
prompt =========================
prompt
create table MAP_AUTO.TB_JOB_LOG
(
  exe_flow_id  NUMBER(10) not null,
  job_id       NUMBER(10) not null,
  job_name     VARCHAR2(500) not null,
  start_date   DATE not null,
  end_date     DATE,
  state        NUMBER(1) not null,
  ret_msg      VARCHAR2(4000),
  trigger_type NUMBER(1) not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_MAIL_LIST
prompt ===========================
prompt
create table MAP_AUTO.TB_MAIL_LIST
(
  id           NUMBER(15) not null,
  mail_to      VARCHAR2(2000) not null,
  mail_cc      VARCHAR2(2000),
  mail_subject VARCHAR2(2000),
  mail_txt     CLOB,
  state        NUMBER(2) not null,
  tag          VARCHAR2(2000),
  send_cnt     NUMBER(5),
  create_date  DATE not null,
  update_date  DATE not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_MESSAGE_TEMPLATE
prompt ==================================
prompt
create table MAP_AUTO.TB_MESSAGE_TEMPLATE
(
  id          NUMBER(6) not null,
  description VARCHAR2(1000),
  content     VARCHAR2(1000) not null,
  sql         VARCHAR2(1000),
  state_date  DATE not null,
  create_date DATE not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_MESSAGE_TEMPLATE
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_MESSAGE_HINT
prompt ==============================
prompt
create table MAP_AUTO.TB_MESSAGE_HINT
(
  id          NUMBER(6) not null,
  description VARCHAR2(1000),
  template_id NUMBER(6),
  express     VARCHAR2(1000) not null,
  show_type   NUMBER(1) not null,
  url         VARCHAR2(200),
  state       NUMBER(1) not null,
  state_date  DATE not null,
  create_date DATE not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_MESSAGE_HINT
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_MESSAGE_HINT
  add constraint FK_MESSAGE_HINT_TPL foreign key (TEMPLATE_ID)
  references MAP_AUTO.TB_MESSAGE_TEMPLATE (ID);

prompt
prompt Creating table TB_NOTICE
prompt ========================
prompt
create table MAP_AUTO.TB_NOTICE
(
  id          NUMBER(10) not null,
  notice_type NUMBER(10) not null,
  title       VARCHAR2(300),
  content     CLOB,
  top_flag    NUMBER(2),
  hot_flag    NUMBER(2),
  create_date DATE not null,
  update_date DATE,
  staff_id    NUMBER(12),
  state       NUMBER(2)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_ORG
prompt =====================
prompt
create table MAP_AUTO.TB_ORG
(
  org_id             NUMBER(12) not null,
  org_name           VARCHAR2(160) not null,
  level_id           NUMBER(2),
  state              NUMBER(1) not null,
  state_date         DATE not null,
  create_date        DATE not null,
  parent_id          NUMBER(12) not null,
  ext_parent_id      NUMBER(12),
  ext_type_id        NUMBER(2),
  channel_segment_id NUMBER(9)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_ORG
  add primary key (ORG_ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_ORG_EXTEND
prompt ============================
prompt
create table MAP_AUTO.TB_ORG_EXTEND
(
  org_id           NUMBER(12) not null,
  org_mark         VARCHAR2(40),
  cash_save_rule   VARCHAR2(50),
  cash_zero_date   DATE,
  cheque_save_rule VARCHAR2(50),
  cheque_zero_date DATE,
  arrived_speed    NUMBER(3) default 1,
  no_alarm         NUMBER(1)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_ORG_EXTEND
  add constraint UK_ORG_EXTEND unique (ORG_ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_ORG_EXTEND
  add constraint FK_ORG_EXTEND foreign key (ORG_ID)
  references MAP_AUTO.TB_ORG (ORG_ID)
  disable;

prompt
prompt Creating table TB_PROJECT_MANAGE
prompt ================================
prompt
create table MAP_AUTO.TB_PROJECT_MANAGE
(
  id          NUMBER not null,
  name        VARCHAR2(100),
  code        VARCHAR2(100),
  create_date DATE
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
comment on column MAP_AUTO.TB_PROJECT_MANAGE.name
  is '中文名称';
comment on column MAP_AUTO.TB_PROJECT_MANAGE.code
  is '英文编码';
alter table MAP_AUTO.TB_PROJECT_MANAGE
  add constraint ID_PROJECT_MANAGE primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_ROLE
prompt ======================
prompt
create table MAP_AUTO.TB_ROLE
(
  role_id     NUMBER(6) not null,
  role_name   VARCHAR2(60) not null,
  state       NUMBER(1) not null,
  state_date  DATE not null,
  create_date DATE not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_ROLE
  add primary key (ROLE_ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_ROLE_RULE
prompt ===========================
prompt
create table MAP_AUTO.TB_ROLE_RULE
(
  id      NUMBER(6) not null,
  role_id NUMBER(6) not null,
  rule_id NUMBER(6) not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_ROLE_RULE
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_RULE
prompt ======================
prompt
create table MAP_AUTO.TB_RULE
(
  rule_id     NUMBER(6) not null,
  rule_name   VARCHAR2(60) not null,
  url         VARCHAR2(128),
  icon        VARCHAR2(128),
  order_num   NUMBER(2) not null,
  visible     NUMBER(1) not null,
  node_type   NUMBER(1) not null,
  node_code   VARCHAR2(40) not null,
  parent_id   NUMBER(6),
  state       NUMBER(1) not null,
  state_date  DATE not null,
  create_date DATE not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_RULE
  add primary key (RULE_ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_RULE_WHITE
prompt ============================
prompt
create table MAP_AUTO.TB_RULE_WHITE
(
  url       VARCHAR2(128) not null,
  need_auth NUMBER(1) not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_RULE_WHITE
  add primary key (URL)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_STAFF
prompt =======================
prompt
create table MAP_AUTO.TB_STAFF
(
  staff_id          NUMBER(12) not null,
  staff_name        VARCHAR2(120) not null,
  login_name        VARCHAR2(20) not null,
  password          VARCHAR2(32) not null,
  org_id            NUMBER(12) not null,
  station_id        NUMBER(12),
  state             NUMBER(1) not null,
  state_date        DATE not null,
  password_exp_date DATE,
  create_date       DATE not null
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_STAFF
  add primary key (STAFF_ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_STAFF_EXTEND
prompt ==============================
prompt
create table MAP_AUTO.TB_STAFF_EXTEND
(
  staff_id        NUMBER(15) not null,
  mobile          VARCHAR2(20),
  update_date     DATE,
  create_date     DATE,
  email           VARCHAR2(100),
  remark          VARCHAR2(1000),
  update_staff_id NUMBER(15)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_STAFF_ROLE
prompt ============================
prompt
create table MAP_AUTO.TB_STAFF_ROLE
(
  id         NUMBER(6) not null,
  staff_id   NUMBER(12) not null,
  role_id    NUMBER(6) not null,
  org_id     NUMBER(12),
  range_type NUMBER(1)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table MAP_AUTO.TB_STAFF_ROLE
  add primary key (ID)
  using index 
  tablespace TENANT
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating table TB_THIRD_ORDER
prompt =============================
prompt
create table MAP_AUTO.TB_THIRD_ORDER
(
  id               NUMBER(15) not null,
  third_party_id   NUMBER(3) not null,
  third_party_flow VARCHAR2(100) not null,
  acct_id          NUMBER(15) not null,
  trans_type_id    NUMBER(6) not null,
  amount           NUMBER(15) not null,
  create_date      DATE not null,
  return_date      DATE,
  return_log_id    NUMBER(15),
  return_state     NUMBER(1) not null,
  state            NUMBER(1) not null,
  send_msg         CLOB,
  recv_msg         CLOB,
  remark           VARCHAR2(256),
  bank_id          VARCHAR2(100),
  bank_flow        VARCHAR2(100),
  fee              NUMBER(15),
  third_fee        NUMBER(15),
  project_id       NUMBER(15)
)
tablespace TENANT
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

prompt
prompt Creating sequence SEQ_AMS_ACTION_LOG
prompt ====================================
prompt
create sequence MAP_AUTO.SEQ_AMS_ACTION_LOG
minvalue 1
maxvalue 9999999999999999999999999999
start with 15461
increment by 1
cache 20;

prompt
prompt Creating sequence SEQ_AMS_STAFF
prompt ===============================
prompt
create sequence MAP_AUTO.SEQ_AMS_STAFF
minvalue 1
maxvalue 9999999999999999999999999999
start with 181
increment by 1
cache 20;

prompt
prompt Creating sequence SEQ_HOST_INFO
prompt ===============================
prompt
create sequence MAP_AUTO.SEQ_HOST_INFO
minvalue 1
maxvalue 9999999999999999999999999999
start with 361
increment by 1
cache 20;

prompt
prompt Creating sequence SEQ_INTERFACE_LOG
prompt ===================================
prompt
create sequence MAP_AUTO.SEQ_INTERFACE_LOG
minvalue 1
maxvalue 9999999999999999999999999999
start with 370
increment by 1
cache 20;

prompt
prompt Creating sequence SEQ_JOB_LOG
prompt =============================
prompt
create sequence MAP_AUTO.SEQ_JOB_LOG
minvalue 1
maxvalue 9999999999999999999999999999
start with 33721
increment by 1
cache 20;

prompt
prompt Creating sequence SEQ_MAIL_LIST
prompt ===============================
prompt
create sequence MAP_AUTO.SEQ_MAIL_LIST
minvalue 1
maxvalue 9999999999999999999999999999
start with 281
increment by 1
cache 20;

prompt
prompt Creating sequence SEQ_NOTICE
prompt ============================
prompt
create sequence MAP_AUTO.SEQ_NOTICE
minvalue 1
maxvalue 9999999999999999999999999999
start with 1
increment by 1
cache 20;

prompt
prompt Creating view VIEW_ENUM_CFG
prompt ===========================
prompt
create or replace force view map_auto.view_enum_cfg as
select a."ID",a."ENUM_KEY",a."ENUM_VALUE",a."CATALOG_ID",a."ORDER_NUM",a."PID",a."UPDATE_DATE",a."STATE", b.catalog_code, b.catalog_name
  from tb_enum_cfg a, tb_enum_catalog b
   where a.catalog_id = b.id
     and b.state = 1;

prompt
prompt Creating view V_GENARATE_MAPPER
prompt ===============================
prompt
create or replace force view map_auto.v_genarate_mapper as
select column_id,
       table_name,
       column_name,
       ('private ' || java_type || ' ' || java_property || ';') as entity_bean,
       ('<result property="' || java_property || '" column="' || column_name || '" jdbcType="' || jdbc_type || '"/>') as mapper_entity,
       (column_name || decode(column_id, count(*)over(partition by table_name), '', ',')) as mapper_column,
       ('#{' || java_property || ', jdbcType=' || decode(jdbc_type, 'DATE', 'TIMESTAMP', jdbc_type) || '}' || decode(column_id, count(*)over(partition by table_name), '', ',')) as mapper_variable,
       count(*) over() as cnt
  from (
    select table_name,
           column_name,
           column_id,
           data_type,
           lower(substr(replace(initcap(column_name), '_'), 1, 1)) || substr(replace(initcap(column_name), '_'), 2, length(column_name) - 1) as java_property,
           decode(data_type, 'NUMBER', 'Long', 'VARCHAR2', 'String', 'DATE', 'Date', 'BLOB', 'Object', 'CLOB', 'Object', 'String') as java_type,
           decode(data_type, 'NUMBER', 'INTEGER', 'VARCHAR2', 'VARCHAR', data_type) as jdbc_type
      from user_tab_columns a
     --where Table_Name='TB_XXX'
     --order by table_name, column_id
  ) v_col_type
  order by table_name, column_id;

prompt
prompt Creating function F_GET_ENUM_NAME
prompt =================================
prompt
create or replace function map_auto.f_get_enum_name(catalogCode in varchar2, key in varchar2) return varchar2 is
  ret varchar2(100);
begin
  select enum_value into ret
    from view_enum_cfg
   where catalog_code = catalogCode
     and enum_key =key;

  return(ret);
exception
 when others then
  ret := ' ';
  return ret;
end ;
/

prompt
prompt Creating function F_GET_SEARCHNO
prompt ================================
prompt
create or replace function map_auto.F_GET_SEARCHNO(searchak in varchar2) return number is
  Result number;
begin
  select nvl(searchno,0) into Result
    from map_poisearch_aknum
   where ak = searchak
     and createdate =to_char(sysdate,'yyyy-MM-dd');
  return(Result);
  exception
 when others then
  Result := 0;
  return Result;
end F_GET_SEARCHNO;
/

prompt
prompt Creating procedure DEALPOISEARCHAKNUM
prompt =====================================
prompt
create or replace procedure map_auto.dealpoisearchaknum(searchak in varchar2) as
    icount number; 
begin
  select f_get_searchno(searchak) into icount from dual;
  if icount=0 then  
     insert into map_poisearch_aknum(ak,searchno,createdate) values (searchak,1,to_char(sysdate,'yyyy-MM-dd')); 
  else  
     update map_poisearch_aknum set searchno =icount+1 where ak = searchak and createdate = to_char(sysdate,'yyyy-MM-dd'); 
  end if; 
  exception  
  when too_many_rows then  
   DBMS_OUTPUT.PUT_LINE('返回值多于1行');  
  when others then  
   DBMS_OUTPUT.PUT_LINE('在dealpoisearchaknum过程中出错！');   
end dealpoisearchaknum;
/


spool off
