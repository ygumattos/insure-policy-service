CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE policy_requests (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    category VARCHAR(50) NOT NULL,
    sales_channel VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    total_monthly_premium_amount DECIMAL(15,2) NOT NULL,
    insured_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP,

    CONSTRAINT valid_status CHECK (status IN (
        'RECEIVED', 'VALIDATED', 'PENDING', 'REJECTED', 'APPROVED', 'CANCELLED'
    ))
);

CREATE TABLE policy_coverages (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_request_id VARCHAR(36) NOT NULL,
    coverage_name VARCHAR(100) NOT NULL,
    coverage_value DECIMAL(15,2) NOT NULL,

    FOREIGN KEY (policy_request_id) REFERENCES policy_requests(id) ON DELETE CASCADE
);

CREATE TABLE policy_assistances (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_request_id VARCHAR(36) NOT NULL,
    assistance_name VARCHAR(100) NOT NULL,

    FOREIGN KEY (policy_request_id) REFERENCES policy_requests(id) ON DELETE CASCADE
);

CREATE TABLE policy_status_history (
    id VARCHAR(36) PRIMARY KEY DEFAULT gen_random_uuid(),
    policy_request_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    changed_at TIMESTAMP NOT NULL,

    FOREIGN KEY (policy_request_id) REFERENCES policy_requests(id) ON DELETE CASCADE,

    CONSTRAINT valid_history_status CHECK (status IN (
        'RECEIVED', 'VALIDATED', 'PENDING', 'REJECTED', 'APPROVED', 'CANCELLED'
    ))
);

CREATE INDEX idx_policy_requests_customer_id ON policy_requests(customer_id);
CREATE INDEX idx_policy_requests_status ON policy_requests(status);
CREATE INDEX idx_policy_requests_created_at ON policy_requests(created_at);