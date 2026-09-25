CREATE TABLE department (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    department_id BIGINT REFERENCES department(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    department_id BIGINT NOT NULL REFERENCES department(id),
    created_by_id BIGINT NOT NULL REFERENCES employee(id),
    assigned_to_id BIGINT REFERENCES employee(id),
    ai_summary TEXT,
    ai_suggested_priority VARCHAR(30),
    ai_analyzed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ticket_comment (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES ticket(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES employee(id),
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_priority ON ticket(priority);
CREATE INDEX idx_ticket_department ON ticket(department_id);
CREATE INDEX idx_comment_ticket ON ticket_comment(ticket_id);
