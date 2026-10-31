# ReqForge

ReqForge is a Java console application for tracking software requirements. It saves data locally in `requirements.db` in the current folder.

## Run

Requires JDK 17 or newer. Open PowerShell in this folder and run:

```powershell
javac ReqForge.java
java ReqForge
```

## Features

- Add requirements with a title, description, and priority
- List and search requirements
- Update requirement details, status, and priority
- Delete requirements

Statuses: `open`, `in progress`, `done`  
Priorities: `low`, `medium`, `high`
