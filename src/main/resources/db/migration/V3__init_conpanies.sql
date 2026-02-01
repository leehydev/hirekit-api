CREATE TABLE companies (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  
  -- 기본 정보
  name VARCHAR(100) NOT NULL,
  industry VARCHAR(50),                -- 업종 (사용자 선택: IT, 금융, 제조 등)
  ceo_name VARCHAR(50),                -- 대표자명
  address TEXT,                        -- 기업기본주소
  founded_date DATE,                   -- 기업설립일자
  
  -- 공공API 연동용
  business_number VARCHAR(20) UNIQUE,  -- 사업자등록번호
  api_source VARCHAR(30),              -- 데이터 출처
  api_raw_data JSONB,                  -- 원본 데이터 보관
  
  -- 등록자
  registered_by UUID REFERENCES members(id),  -- null 허용
  
  -- 타임스탬프
  created_at TIMESTAMPTZ DEFAULT now(),
  updated_at TIMESTAMPTZ DEFAULT now()
);