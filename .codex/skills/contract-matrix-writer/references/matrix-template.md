# Frontend API Contract Matrix Template

| Domain | Screen/Route | User Action | Current Frontend Call | Spec Endpoint | Request Mapping | Response Mapping | Gap Type | Decision Status | Owner | Notes |
|---|---|---|---|---|---|---|---|---|---|---|
| Thread | /threads | 참여 신청 | POST /threads/{id}/join | POST /threads/{id}/apply | ... | ... | Path mismatch | Decision Needed | BE/FE | Q-TH-001 |

## Gap Type Values
- Path mismatch
- Method mismatch
- Field mismatch
- Missing endpoint
- Missing UX action

## Decision Status Values
- Aligned
- Frontend Patch
- Spec Update
- Decision Needed

