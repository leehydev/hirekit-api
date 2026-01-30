-- 소셜 계정 연동 해제 관련 컬럼 추가

-- 연동 활성화 여부 (기본값: true)
ALTER TABLE social_accounts 
ADD COLUMN is_active BOOLEAN DEFAULT true NOT NULL;

-- 연동 해제 일시
ALTER TABLE social_accounts 
ADD COLUMN unlinked_at TIMESTAMP;

-- updated_at 컬럼 추가 (BaseEntity와 맞추기 위해)
ALTER TABLE social_accounts 
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;