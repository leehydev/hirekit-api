-- 질문 테이블
CREATE TABLE questions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID NOT NULL REFERENCES companies(id) ON DELETE RESTRICT,
    job             VARCHAR(30) NOT NULL,
    content         TEXT NOT NULL,
    author_id       UUID NOT NULL REFERENCES members(id) ON DELETE RESTRICT,
    visibility      VARCHAR(20) NOT NULL,
    author_hidden   BOOLEAN NOT NULL DEFAULT FALSE,
    forced_private  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT now(),
    updated_at      TIMESTAMPTZ DEFAULT now()
);

-- 답변 테이블
CREATE TABLE answers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id     UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    content         TEXT NOT NULL,
    tip             TEXT,
    pass_status     VARCHAR(20),
    interview_date  DATE,
    author_id       UUID NOT NULL REFERENCES members(id) ON DELETE RESTRICT,
    visibility      VARCHAR(20) NOT NULL,
    author_hidden   BOOLEAN NOT NULL DEFAULT FALSE,
    forced_private  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ DEFAULT now(),
    updated_at      TIMESTAMPTZ DEFAULT now()
);

-- 답변 좋아요 테이블 (한 회원이 한 답변에 한 번만 좋아요 가능)
CREATE TABLE answer_likes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    answer_id       UUID NOT NULL REFERENCES answers(id) ON DELETE CASCADE,
    member_id       UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ DEFAULT now(),
    updated_at      TIMESTAMPTZ DEFAULT now(),
    UNIQUE (answer_id, member_id)
);

-- 인덱스: 질문 목록 조회 (회사·직무·공개 여부 등)
CREATE INDEX idx_questions_company_id ON questions(company_id);
CREATE INDEX idx_questions_job ON questions(job);
CREATE INDEX idx_questions_author_id ON questions(author_id);
CREATE INDEX idx_questions_visibility ON questions(visibility);
CREATE INDEX idx_questions_created_at ON questions(created_at DESC);

-- 인덱스: 답변 목록 조회 (질문별, 작성자별)
CREATE INDEX idx_answers_question_id ON answers(question_id);
CREATE INDEX idx_answers_author_id ON answers(author_id);
CREATE INDEX idx_answers_visibility ON answers(visibility);
CREATE INDEX idx_answers_created_at ON answers(created_at DESC);

-- 인덱스: 좋아요 조회 (답변별 좋아요 수, 회원별 좋아요 여부)
CREATE INDEX idx_answer_likes_answer_id ON answer_likes(answer_id);
CREATE INDEX idx_answer_likes_member_id ON answer_likes(member_id);
