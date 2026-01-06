-- 用户表
CREATE TABLE IF NOT EXISTS "user"
(
    id
                  BIGSERIAL
        PRIMARY
            KEY,                                        -- 1. 自增主键
    user_account
                  VARCHAR(256) NOT NULL,                -- 2. 列名小写+下划线
    user_password VARCHAR(512) NOT NULL,
    user_name     VARCHAR(256),
    user_avatar   VARCHAR(1024),
    user_profile  VARCHAR(512),
    user_role     VARCHAR(256) NOT NULL DEFAULT 'user', -- 3. 默认值用单引号
    edit_time     TIMESTAMPTZ  NOT NULL DEFAULT NOW
                                                (
                                                ),      -- 4. 时间类型
    create_time   TIMESTAMPTZ  NOT NULL DEFAULT NOW
                                                (
                                                ),
    update_time   TIMESTAMPTZ  NOT NULL DEFAULT NOW
                                                (
                                                ),
    is_delete     SMALLINT     NOT NULL DEFAULT 0,      -- 5. 布尔/数值
    CONSTRAINT uk_user_account UNIQUE
        (
         user_account
            )                                           -- 6. 唯一约束
);

-- 7. 普通索引
CREATE INDEX IF NOT EXISTS idx_user_name ON "user" (user_name);

-- 8. 表注释
COMMENT
    ON TABLE "user" IS '用户';
COMMENT
    ON COLUMN "user".id IS '主键';
COMMENT
    ON COLUMN "user".user_account IS '账号';
COMMENT
    ON COLUMN "user".user_password IS '密码';
COMMENT
    ON COLUMN "user".user_name IS '用户昵称';
COMMENT
    ON COLUMN "user".user_avatar IS '用户头像';
COMMENT
    ON COLUMN "user".user_profile IS '用户简介';
COMMENT
    ON COLUMN "user".user_role IS '用户角色：user/admin';
COMMENT
    ON COLUMN "user".edit_time IS '编辑时间';
COMMENT
    ON COLUMN "user".create_time IS '创建时间';
COMMENT
    ON COLUMN "user".update_time IS '更新时间';
COMMENT
    ON COLUMN "user".is_delete IS '是否删除';

-- 图片表
create table if not exists picture
(
    id
                 bigserial
        primary
            key,
    url
                 varchar(512) not null,
    name         varchar(128) not null,
    introduction varchar(512),
    category     varchar(64),
    tags         varchar(512),
    picSize      bigint,
    picWidth     int,
    picHeight    int,
    picScale     double precision,
    picFormat    varchar(32),
    userId       bigint       not null,
    edit_time    TIMESTAMPTZ  NOT NULL DEFAULT NOW
                                               (
                                               ), -- 4. 时间类型
    create_time  TIMESTAMPTZ  NOT NULL DEFAULT NOW
                                               (
                                               ),
    update_time  TIMESTAMPTZ  NOT NULL DEFAULT NOW
                                               (
                                               ),
    is_delete    SMALLINT     NOT NULL DEFAULT 0  -- 5. 布尔/数值
);
-- 添加索引
create index idx_name on picture (name);
create index idx_introduction on picture (introduction);
create index id_category on picture (category);
create index idx_tags on picture (tags);
create index ids_userId on picture (userId);

-- 添加注释
comment
    on table picture is '图片';
comment
    on column picture.id is 'id';
comment
    on column picture.name is '图片名';
comment
    on column picture.url is 'url地址';
comment
    on column picture.introduction is '简介';
comment
    on column picture.category is '类别';
comment
    on column picture.tags is '标签';
comment
    on column picture.picSize is '图片大小';
comment
    on column picture.picWidth is '图片宽度';
comment
    on column picture.picHeight is '图片高度';
comment
    on column picture.picScale is '图片比例';
comment
    on column picture.picFormat is '图片类型';
comment
    on column picture.userId is '创建者id';
COMMENT
    ON COLUMN picture.edit_time IS '编辑时间';
COMMENT
    ON COLUMN picture.create_time IS '创建时间';
COMMENT
    ON COLUMN picture.update_time IS '更新时间';
COMMENT
    ON COLUMN picture.is_delete IS '是否删除';

alter table picture
    add column review_status  smallint default 0 not null,
    add column review_message varchar(512)       null,
    add column review_time    timestamp          null,
    add column review_id      bigint             null;
comment on column picture.review_status is '审核状态';
comment on column picture.review_message is '审核信息';
comment on column picture.review_time is '审核时间';
comment on column picture.review_id is '审核人id';


-- 空间表的创建
create table if not exists space
(
    id          bigserial primary key,
    space_name  varchar(128),
    space_level int                default 0,
    max_size    bigint             default 0,
    max_count   bigint             default 0,
    total_size  bigint             default 0,
    total_count bigint             default 0,
    user_id     bigint    not null,
    create_time timestamp not null default now(),
    edit_time   timestamp not null default now(),
    update_time timestamp not null default now(),
    is_delete   smallint           default 0
);
comment on table space is '空间';
comment on column space.id is 'id';
comment on column space.space_name is '空间名称';
comment on column space.space_level is '空间级别：0-普通版 1-专业版 2-旗舰版';
comment on column space.max_size is '空间图片的最大总大小';
comment on column space.max_count is '空间图片的最大数量';
comment on column space.total_size is '当前空间下的图片总大小';
comment on column space.total_count is '当前空间下的图片数量';
comment on column space.user_id is '创建用户 id';
COMMENT ON COLUMN space.edit_time IS '编辑时间';
COMMENT
    ON COLUMN space.create_time IS '创建时间';
COMMENT
    ON COLUMN space.update_time IS '更新时间';
COMMENT
    ON COLUMN space.is_delete IS '是否删除';

create index idx_user_id on space (user_id);
create index idx_space_name on space (space_name);
create index idx_space_level on space (space_level);


-- 为图片库新增一个空间id字段
alter table picture
    add column space_id bigint null;
comment on column picture.space_id is '空间id(空表示公共空间)';
create index idx_space_id on picture (space_id);

-- 图片主色调
alter table picture
    add column pic_color varchar(16) null;
comment on column picture.pic_color is '图片主色调';