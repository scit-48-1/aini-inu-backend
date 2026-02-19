# ERD DDL Sync Checklist

## Entity-Level
- 엔티티 존재 여부
- PK/FK 존재 여부
- 관계 방향/카디널리티

## Column-Level
- 타입 일치
- nullable 일치
- default 일치
- 길이/정밀도 일치

## Constraint-Level
- unique/key
- check constraint
- soft delete 정책

## Index-Level
- 필수 검색 인덱스
- 복합 인덱스 컬럼 순서

## Change Classification
- Spec Missing
- Schema Missing
- Type Mismatch
- Constraint Mismatch

