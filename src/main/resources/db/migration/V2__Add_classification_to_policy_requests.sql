ALTER TABLE policy_requests
ADD COLUMN classification VARCHAR(50) NULL;

ALTER TABLE policy_requests
ADD CONSTRAINT valid_classification
CHECK (classification IN ('REGULAR', 'HIGH_RISK', 'PREFERENTIAL', 'NO_INFORMATION') OR classification IS NULL);