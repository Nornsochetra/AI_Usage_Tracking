# Role Permission Matrix

This document defines the Role-Based Access Control (RBAC) matrix and permission hierarchy for the **AI Token & Daily Budget Tracking System** in the backend.

---

## 📊 Role Permission Matrix Summary

| Module / System Action | REST API Endpoint | `MEMBER` (Developer) | `ADMIN` (Team Lead) | `OWNER` (Organization Manager) | Description |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **AI Telemetry Ingestion** | | | | | |
| Send Local AI Telemetry Logs | `POST /api/v1/telemetry/ai/log` | ✅ | ✅ | ✅ | Stream prompt/completion token usage from tools like Claude Code, Cursor, or Gemini CLI. |
| Check Budget Status Before Request | `GET /api/v1/telemetry/ai/check-budget` | ✅ | ✅ | ✅ | Pre-flight API check used by local agents or proxies to see if the user is currently throttled. |
| **Personal Analytics** | | | | | |
| View Personal Token History | `GET /api/v1/usage/my-history` | ✅ | ✅ | ✅ | View individual daily spend ($), prompt tokens, and completion tokens. |
| View Personal Budget Limit | `GET /api/v1/usage/my-budget` | ✅ | ✅ | ✅ | Check current daily limit allocation (e.g., $10/day) and remaining balance. |
| **Team Management & Budgets** | | | | | |
| View Full Team Usage Dashboard | `GET /api/v1/teams/{teamId}/analytics` | ❌ | ✅ | ✅ | View aggregated team token consumption and per-member breakdown reports. |
| Adjust Member Daily Budget Cap | `PUT /api/v1/teams/{teamId}/members/{userId}/budget` | ❌ | ✅ | ✅ | Override or increase specific user daily spend limits in Redis & PostgreSQL. |
| Invite / Remove Team Members | `POST /api/v1/teams/{teamId}/members` | ❌ | ✅ | ✅ | Add new developers to the team so their requests are properly attributed. |
| View Budget Limit Violations | `GET /api/v1/teams/{teamId}/alerts` | ❌ | ✅ | ✅ | Get a list of team members who exceeded their daily token budget today. |
| **Billing & Administration** | | | | | |
| Manage Overall Team API Keys | `POST /api/v1/teams/{teamId}/keys` | ❌ | ❌ | ✅ | Configure centralized provider keys (for AI Gateway Proxy mode). |
| Modify Team Subscription Plan | `POST /api/v1/teams/{teamId}/billing` | ❌ | ❌ | ✅ | Manage plan upgrades, credit cards, or total monthly spending caps. |
| Delete Team Account | `DELETE /api/v1/teams/{teamId}` | ❌ | ❌ | ✅ | Wipe team data and drop all associated users and usage records. |

---

## 🔐 Permission Level Definitions

### 1. `MEMBER` (Developer / End-User)
* **Goal:** Focuses on standard development work using local AI tools.
* **Scope:** Read-only access to personal token metrics and telemetry streaming. Cannot alter budget constraints or inspect other team members' token consumption.

### 2. `ADMIN` (Tech Lead / Project Manager)
* **Goal:** Manages team operational efficiency and controls daily AI expenditure.
* **Scope:** Full visibility into team consumption metrics, ability to adjust individual daily budget caps, manage member roster, and review spend limit alerts.

### 3. `OWNER` (Organization Manager / Account Owner)
* **Goal:** Manages organization billing, corporate provider keys, and system governance.
* **Scope:** Complete administrative control over team lifecycle, financial billing settings, global API proxy configuration keys, and account deletion.

---

## 💻 Spring Security Controller Annotations Example

Below is an example of how these permissions are mapped to REST Controller endpoints in Spring Boot using `@PreAuthorize`:

```java
package com.admin.backend.usage.controller;

import com.admin.backend.usage.payload.BudgetUpdateRequest;
import com.admin.backend.usage.service.AIUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamManagementController {

    private final AIUsageService aiUsageService;

    // Accessible by MEMBER, ADMIN, and OWNER
    @GetMapping("/my-history")
    @PreAuthorize("hasAnyRole('MEMBER', 'ADMIN', 'OWNER')")
    public ResponseEntity<?> getPersonalHistory() {
        return ResponseEntity.ok().build();
    }

    // Accessible only by ADMIN and OWNER
    @PutMapping("/{teamId}/members/{userId}/budget")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Void> updateMemberBudget(
            @PathVariable String teamId,
            @PathVariable String userId,
            @RequestBody BudgetUpdateRequest request) {
        
        aiUsageService.updateDailyBudgetLimit(userId, request.newLimit());
        return ResponseEntity.ok().build();
    }

    // Accessible ONLY by OWNER
    @DeleteMapping("/{teamId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteTeam(@PathVariable String teamId) {
        aiUsageService.deleteTeamAccount(teamId);
        return ResponseEntity.noContent().build();
    }
}