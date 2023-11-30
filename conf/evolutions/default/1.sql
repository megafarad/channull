# --- !Ups
CREATE SCHEMA auth;

CREATE TABLE auth.user (
  id            UUID    NOT NULL PRIMARY KEY,
  handle        VARCHAR,
  first_name    VARCHAR,
  last_name     VARCHAR,
  full_name     VARCHAR,
  email         VARCHAR,
  activated     BOOLEAN NOT NULL,
  avatar_url    VARCHAR,
  signed_up_at  TIMESTAMPTZ DEFAULT NOW(),
  profile       TEXT,
  UNIQUE (handle),
  UNIQUE (email)
);

CREATE TABLE auth.login_info (
  id           BIGSERIAL NOT NULL PRIMARY KEY,
  provider_id  VARCHAR NOT NULL,
  provider_key VARCHAR NOT NULL
);

CREATE INDEX auth_login_info_key on auth.login_info(provider_id, provider_key);

CREATE TABLE auth.user_login_info (
  user_id       UUID   NOT NULL,
  login_info_id BIGINT NOT NULL,
  CONSTRAINT auth_user_login_info_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id),
  CONSTRAINT auth_user_login_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id),
  CONSTRAINT auth_user_login_info_pk PRIMARY KEY (user_id, login_info_id)
);

CREATE TABLE auth.google_totp_info (
  id            BIGSERIAL NOT NULL PRIMARY KEY,
  login_info_id BIGINT NOT NULL,
  shared_key    VARCHAR NOT NULL,
  CONSTRAINT auth_google_totp_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id)
);

CREATE TABLE auth.totp_scratch_code (
  id                    BIGSERIAL NOT NULL PRIMARY KEY,
  totp_google_info_id   BIGINT NOT NULL,
  hasher                VARCHAR NOT NULL,
  password              VARCHAR NOT NULL,
  salt                  VARCHAR,
  CONSTRAINT auth_totp_scratch_code_google_totp_info_id_fk FOREIGN KEY (totp_google_info_id) REFERENCES auth.google_totp_info (id)
);

CREATE TABLE auth.oauth2_info (
  id            BIGSERIAL NOT NULL PRIMARY KEY,
  access_token  VARCHAR   NOT NULL,
  token_type    VARCHAR,
  expires_in    INT,
  refresh_token VARCHAR,
  login_info_id BIGINT    NOT NULL,
  CONSTRAINT auth_oauth2_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id)
);

CREATE TABLE auth.password_info (
  id            BIGSERIAL NOT NULL PRIMARY KEY,
  hasher        VARCHAR NOT NULL,
  password      VARCHAR NOT NULL,
  salt          VARCHAR,
  login_info_id BIGINT  NOT NULL,
  CONSTRAINT auth_password_info_login_info_id_fk FOREIGN KEY (login_info_id) REFERENCES auth.login_info (id)
);

CREATE TABLE auth.token (
  id      UUID        NOT NULL PRIMARY KEY,
  user_id UUID        NOT NULL,
  expiry  TIMESTAMPTZ NOT NULL,
  CONSTRAINT auth_token_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id)
);

CREATE SCHEMA app;

CREATE TABLE app.channull (
  id            UUID        NOT NULL PRIMARY KEY,
  parent_id     UUID,
  name          VARCHAR     NOT NULL,
  description   TEXT,
  when_created  TIMESTAMPTZ NOT NULL,
  who_created   UUID        NOT NULL,
  access        VARCHAR     NOT NULL,
  UNIQUE (name),
  CONSTRAINT app_channull_parent_id_fk FOREIGN KEY (parent_id) REFERENCES app.channull (id),
  CONSTRAINT app_channull_who_created_fk FOREIGN KEY (who_created) REFERENCES auth.user (id)
);

CREATE TABLE app.channull_rule (
  id            UUID        NOT NULL PRIMARY KEY,
  channull_id   UUID        NOT NULL,
  number        SMALLINT    NOT NULL,
  rule          TEXT        NOT NULL,
  when_created  TIMESTAMPTZ NOT NULL,
  who_created   UUID        NOT NULL,
  CONSTRAINT app_channull_rule_channull_id_fk FOREIGN KEY (channull_id) REFERENCES app.channull (id),
  CONSTRAINT app_channull_rule_who_created_fk FOREIGN KEY (who_created) REFERENCES auth.user (id)
);

CREATE TABLE app.channull_permissions (
  id            UUID        NOT NULL PRIMARY KEY,
  channull_id   UUID        NOT NULL,
  role          VARCHAR     NOT NULL,
  can_post      BOOLEAN     NOT NULL,
  can_subpost   BOOLEAN     NOT NULL,
  can_ban       BOOLEAN     NOT NULL,
  UNIQUE (channull_id, role),
  CONSTRAINT app_channull_permissions_channull_id_fk FOREIGN KEY (channull_id) REFERENCES app.channull(id)
);

CREATE TABLE app.channull_ban (
  id                UUID        NOT NULL PRIMARY KEY,
  channull_id       UUID        NOT NULL,
  user_id           UUID        NOT NULL,
  banned_by         UUID        NOT NULL,
  reason            TEXT,
  when_created      TIMESTAMPTZ NOT NULL,
  expiry            TIMESTAMPTZ,
  CONSTRAINT app_channull_ban_channull_id_fk FOREIGN KEY (channull_id) REFERENCES app.channull (id),
  CONSTRAINT app_channull_ban_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id),
  CONSTRAINT app_channull_ban_banned_by_fk FOREIGN KEY (banned_by) REFERENCES auth.user (id)
);

CREATE TABLE app.channull_ban_violated_rule (
  id                UUID        NOT NULL PRIMARY KEY,
  ban_id            UUID        NOT NULL,
  violated_rule_id  UUID        NOT NULL,
  CONSTRAINT app_channull_ban_violated_rule_ban_id FOREIGN KEY (ban_id) REFERENCES app.channull_ban (id) ON DELETE CASCADE,
  CONSTRAINT app_channull_ban_violated_rule_rule_id FOREIGN KEY (violated_rule_id) REFERENCES app.channull_rule (id)
);

CREATE TABLE app.channull_post (
  id            UUID        NOT NULL PRIMARY KEY,
  parent_id     UUID,
  channull_id   UUID        NOT NULL,
  text          TEXT,
  when_created  TIMESTAMPTZ NOT NULL,
  who_created   UUID        NOT NULL,
  expiry        TIMESTAMPTZ,
  CONSTRAINT app_channull_post_channull_id_fk FOREIGN KEY (channull_id) REFERENCES app.channull (id),
  CONSTRAINT app_channull_post_who_created_fk FOREIGN KEY (who_created) REFERENCES auth.user (id),
  CONSTRAINT app_channull_post_parent_id_fk FOREIGN KEY (parent_id) REFERENCES app.channull_post (id) ON DELETE CASCADE
);

CREATE TABLE app.channull_post_media (
  id                UUID        NOT NULL PRIMARY KEY,
  post_id           UUID        NOT NULL,
  alt_text          TEXT,
  content_type      VARCHAR,
  content_url       TEXT,
  content_size      BIGINT,
  CONSTRAINT app_channull_post_media_post_id_fk FOREIGN KEY (post_id) REFERENCES app.channull_post (id) ON DELETE CASCADE
);

CREATE TABLE app.channull_post_reaction (
  id                UUID        NOT NULL PRIMARY KEY,
  post_id           UUID        NOT NULL,
  user_id           UUID        NOT NULL,
  reaction_type     VARCHAR     NOT NULL,
  timestamp         TIMESTAMPTZ NOT NULL,
  CONSTRAINT app_channull_post_reaction_post_id_fk FOREIGN KEY (post_id) REFERENCES app.channull_post (id) ON DELETE CASCADE,
  CONSTRAINT app_channull_post_reaction_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id)
);

CREATE TABLE app.channull_user (
  id                UUID        NOT NULL PRIMARY KEY,
  channull_id       UUID        NOT NULL,
  user_id           UUID        NOT NULL,
  role              VARCHAR     NOT NULL,
  CONSTRAINT app_channull_user_channull_id_fk FOREIGN KEY (channull_id) REFERENCES app.channull (id),
  CONSTRAINT app_channull_user_user_id_fk FOREIGN KEY (user_id) REFERENCES auth.user (id)
);

CREATE TABLE app.report (
  id                UUID        NOT NULL PRIMARY KEY,
  reporter          UUID        NOT NULL,
  post_id           UUID        NOT NULL,
  report            TEXT,
  timestamp         TIMESTAMPTZ NOT NULL,
  status            VARCHAR     NOT NULL,
  CONSTRAINT app_report_reporter_fk FOREIGN KEY (reporter) REFERENCES auth.user (id),
  CONSTRAINT app_report_post_id_fk FOREIGN KEY (post_id) REFERENCES app.channull_post (id)
);

CREATE TABLE app.report_violated_rule (
  id                UUID    NOT NULL PRIMARY KEY,
  report_id         UUID    NOT NULL,
  violated_rule_id  UUID    NOT NULL,
  CONSTRAINT app_report_violated_rule_report_id_fk FOREIGN KEY (report_id) REFERENCES app.report (id),
  CONSTRAINT app_report_violated_rule_violated_rule_id FOREIGN KEY (violated_rule_id) references app.channull_rule (id)
);

CREATE TABLE app.blocked_user (
  id                UUID            NOT NULL PRIMARY KEY,
  blocking_user_id  UUID            NOT NULL,
  blocked_user_id   UUID            NOT NULL,
  timestamp         TIMESTAMPTZ     NOT NULL,
  CONSTRAINT app_blocked_user_blocking_user_id_fk FOREIGN KEY (blocking_user_id) REFERENCES auth.user (id),
  CONSTRAINT app_blocked_user_blocked_user_id_fk FOREIGN KEY (blocked_user_id) REFERENCES auth.user (id)
);

# --- !Downs

DROP TABLE app.blocked_user;
DROP TABLE app.report_violated_rule;
DROP TABLE app.report;
DROP TABLE app.channull_user;
DROP TABLE app.channull_post_media;
DROP TABLE app.channull_post_reaction;
DROP TABLE app.channull_permissions;
DROP TABLE app.channull_ban_violated_rule;
DROP TABLE app.channull_ban;
DROP TABLE app.channull_post;
DROP TABLE app.channull_rule;
DROP TABLE app.channull;
DROP SCHEMA app;

DROP TABLE auth.token;
DROP TABLE auth.password_info;
DROP TABLE auth.oauth2_info;
DROP TABLE auth.totp_scratch_code;
DROP TABLE auth.google_totp_info;
DROP TABLE auth.user_login_info;
DROP TABLE auth.login_info;
DROP TABLE auth.user;
DROP SCHEMA auth;
