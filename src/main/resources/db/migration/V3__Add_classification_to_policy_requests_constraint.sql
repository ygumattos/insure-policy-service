ALTER TABLE policy_requests
DROP CONSTRAINT IF EXISTS valid_classification;

ALTER TABLE policy_requests
ADD CONSTRAINT valid_classification
CHECK (
    classification IS NULL
    OR classification IN ('REGULAR', 'HIGH_RISK', 'PREFERENTIAL', 'NO_INFORMATION')
);
