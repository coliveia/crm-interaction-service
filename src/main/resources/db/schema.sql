-- Interactions Table
CREATE TABLE interactions (
    id VARCHAR2(36) PRIMARY KEY,
    case_id VARCHAR2(36) NOT NULL,
    customer_id VARCHAR2(36) NOT NULL,
    agent_id VARCHAR2(36),
    type VARCHAR2(50) NOT NULL,
    channel VARCHAR2(50) NOT NULL,
    content CLOB,
    transcription CLOB,
    summary CLOB,
    sentiment VARCHAR2(20),
    sentiment_score NUMBER(3,2),
    duration_seconds NUMBER(10),
    audio_url VARCHAR2(500),
    attachment_count NUMBER(5) DEFAULT 0,
    metadata JSON,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Attachments Table
CREATE TABLE attachments (
    id VARCHAR2(36) PRIMARY KEY,
    interaction_id VARCHAR2(36) NOT NULL,
    file_name VARCHAR2(255) NOT NULL,
    file_type VARCHAR2(100),
    file_size NUMBER(15),
    file_url VARCHAR2(500) NOT NULL,
    uploaded_by VARCHAR2(36),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_attachment_interaction FOREIGN KEY (interaction_id) 
        REFERENCES interactions(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_interactions_case ON interactions(case_id);
CREATE INDEX idx_interactions_customer ON interactions(customer_id);
CREATE INDEX idx_interactions_agent ON interactions(agent_id);
CREATE INDEX idx_interactions_created ON interactions(created_at);
CREATE INDEX idx_interactions_sentiment ON interactions(sentiment);
CREATE INDEX idx_attachments_interaction ON attachments(interaction_id);

-- Duality View for Interactions (JSON API)
CREATE OR REPLACE JSON RELATIONAL DUALITY VIEW interaction_dv AS
SELECT JSON {
    '_id': i.id,
    'caseId': i.case_id,
    'customerId': i.customer_id,
    'agentId': i.agent_id,
    'type': i.type,
    'channel': i.channel,
    'content': i.content,
    'transcription': i.transcription,
    'summary': i.summary,
    'sentiment': i.sentiment,
    'sentimentScore': i.sentiment_score,
    'durationSeconds': i.duration_seconds,
    'audioUrl': i.audio_url,
    'attachmentCount': i.attachment_count,
    'metadata': i.metadata,
    'createdAt': i.created_at,
    'updatedAt': i.updated_at,
    'attachments': [
        SELECT JSON {
            '_id': a.id,
            'fileName': a.file_name,
            'fileType': a.file_type,
            'fileSize': a.file_size,
            'fileUrl': a.file_url,
            'uploadedBy': a.uploaded_by,
            'createdAt': a.created_at
        }
        FROM attachments a
        WHERE a.interaction_id = i.id
    ]
}
FROM interactions i;

-- Duality View for Customer Timeline (Aggregated)
CREATE OR REPLACE JSON RELATIONAL DUALITY VIEW customer_timeline_dv AS
SELECT JSON {
    '_id': i.customer_id,
    'customerId': i.customer_id,
    'interactions': [
        SELECT JSON {
            '_id': i2.id,
            'caseId': i2.case_id,
            'type': i2.type,
            'channel': i2.channel,
            'summary': i2.summary,
            'sentiment': i2.sentiment,
            'createdAt': i2.created_at
        }
        FROM interactions i2
        WHERE i2.customer_id = i.customer_id
        ORDER BY i2.created_at DESC
    ]
}
FROM interactions i
GROUP BY i.customer_id;

-- Sample Data (Optional)
-- INSERT INTO interactions (id, case_id, customer_id, agent_id, type, channel, content, created_at, updated_at)
-- VALUES (
--     SYS_GUID(),
--     'CASE-001',
--     'CUST-12345',
--     'AGENT-001',
--     'CALL',
--     'PHONE',
--     'Customer called about billing issue',
--     SYSTIMESTAMP,
--     SYSTIMESTAMP
-- );
