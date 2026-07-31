CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id     VARCHAR(255) NOT NULL,
    user_name       VARCHAR(255) NULL,
    user_email      VARCHAR(255) NULL,
    message         TEXT         NOT NULL,
    category        VARCHAR(64)  NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    created_date    DATETIME(6)  NULL,
    created_at      DATETIME(6)  NULL,
    updated_at      DATETIME(6)  NULL,
    PRIMARY KEY (notification_id),
    KEY idx_notif_employee (employee_id),
    KEY idx_notif_email    (user_email),
    KEY idx_notif_category (category)
) ENGINE=InnoDB;
