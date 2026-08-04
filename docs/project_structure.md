# Project Directory Structure

```
src/
├── app/
│   ├── (app)/
│   ├── (auth)/
│   ├── portal/
│   ├── globals.css
│   ├── layout.tsx
│   ├── not-found.tsx
│   └── page.tsx
├── components/
├── config/
│   └── navigation.ts
├── data/
│   └── employees.ts
├── hooks/
│   ├── useAuth.ts
│   ├── useDiscardGuard.tsx
│   ├── useInfiniteScroll.ts
│   ├── useNotifications.ts
│   ├── useRoleGuard.ts
│   └── useToast.ts
├── lib/
├── providers/
└── types/
```

## Mermaid Diagram

```mermaid
graph TD
    src[src/]
    
    %% App Section
    src --> app[app/]
    app --> app_group[(app/)]
    app --> auth_group[(auth/)]
    app --> portal[portal/]
    app --> globals[globals.css]
    app --> layout[layout.tsx]
    app --> not_found[not-found.tsx]
    app --> page[page.tsx]
    
    %% Components Section
    src --> components[components/]
    
    %% Config Section
    src --> config[config/]
    config --> nav[navigation.ts]
    
    %% Data Section
    src --> data[data/]
    data --> emp[employees.ts]
    
    %% Hooks Section
    src --> hooks[hooks/]
    hooks --> useAuth[useAuth.ts]
    hooks --> useDiscardGuard[useDiscardGuard.tsx]
    hooks --> useInfiniteScroll[useInfiniteScroll.ts]
    hooks --> useNotifications[useNotifications.ts]
    hooks --> useRoleGuard[useRoleGuard.ts]
    hooks --> useToast[useToast.ts]
    
    %% Other Directories
    src --> lib[lib/]
    src --> providers[providers/]
    src --> types[types/]
```
