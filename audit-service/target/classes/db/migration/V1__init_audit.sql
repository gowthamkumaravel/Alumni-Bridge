CREATE TABLE IF NOT EXISTS audit_logs (
    audit_id   BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id VARCHAR(255) NULL,
    user_name  VARCHAR(255) NULL,
    action     VARCHAR(255) NOT NULL,
    module     VARCHAR(255) NOT NULL,
    timestamp  DATETIME(6)  NOT NULL,
    PRIMARY KEY (audit_id),
    KEY idx_audit_user   (employee_id),
    KEY idx_audit_module (module)
) ENGINE=InnoDB;
