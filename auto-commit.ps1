# auto-commit-all-files.ps1
# Script to commit each file individually with conventional commit messages

param(
    [switch]$DryRun,
    [switch]$Push
)

# Function to determine commit type based on file
function Get-CommitType {
    param([string]$FilePath)

    $file = $FilePath.ToLower()

    if ($file -match "\.java$") {
        if ($file -match "controller") { return "feat" }
        if ($file -match "service") { return "feat" }
        if ($file -match "repository|dao") { return "feat" }
        if ($file -match "model|entity") { return "feat" }
        if ($file -match "dto") { return "feat" }
        if ($file -match "config") { return "chore" }
        if ($file -match "exception") { return "fix" }
        if ($file -match "validator") { return "feat" }
        return "feat"
    }

    if ($file -match "\.sql$|schema") { return "db" }
    if ($file -match "\.properties$|\.yml$|\.yaml$") { return "chore" }
    if ($file -match "pom\.xml|build\.gradle|settings\.gradle") { return "build" }
    if ($file -match "\.md$|docs\.yaml") { return "docs" }
    if ($file -match "\.json$") { return "chore" }
    if ($file -match "gradlew|mvnw") { return "build" }
    if ($file -match "\.gitignore|\.gitattributes") { return "chore" }
    if ($file -match "\.env") { return "chore" }

    return "chore"
}

# Function to determine scope
function Get-Scope {
    param([string]$FilePath)

    $file = $FilePath.ToLower()

    if ($file -match "collectivity") { return "collectivity" }
    if ($file -match "member") { return "member" }
    if ($file -match "payment|fee") { return "payment" }
    if ($file -match "activity|attendance") { return "activity" }
    if ($file -match "statistics") { return "stats" }
    if ($file -match "account|financial") { return "finance" }
    if ($file -match "database|schema|migration") { return "database" }
    if ($file -match "config") { return "config" }
    if ($file -match "exception") { return "error-handling" }
    if ($file -match "validator") { return "validation" }
    if ($file -match "test") { return "test" }
    if ($file -match "controller") { return "api" }
    if ($file -match "service") { return "business" }
    if ($file -match "repository") { return "persistence" }

    return "core"
}

# Function to generate description
function Get-Description {
    param([string]$FilePath, [string]$Action)

    $fileName = Split-Path $FilePath -Leaf
    $cleanName = $fileName -replace '\.(java|sql|properties|yml|yaml|json|xml|md)$', ''

    switch ($Action) {
        "ADD" { return "add $cleanName" }
        "DELETE" { return "remove $cleanName" }
        "MODIFY" { return "update $cleanName" }
        default { return "add $cleanName" }
    }
}

# Function to get file action
function Get-FileAction {
    param([string]$Status)

    if ($Status -match '^D') { return "DELETE" }
    if ($Status -match '^\?\?') { return "ADD" }
    return "MODIFY"
}

# Function to create conventional commit message
function New-ConventionalCommitMessage {
    param(
        [string]$Type,
        [string]$Scope,
        [string]$Description
    )

    if ($Scope) {
        return "${Type}(${Scope}): ${Description}"
    }
    else {
        return "${Type}: ${Description}"
    }
}

# Collect all changed files
Write-Host "[SCAN] Scanning for changed files..." -ForegroundColor Cyan
$changedFiles = git status --porcelain | ForEach-Object {
    $status = $_.Substring(0, 2).Trim()
    $file = $_.Substring(3)
    [PSCustomObject]@{
        Status = $status
        Path = $file
        Action = Get-FileAction -Status $status
    }
}

if ($changedFiles.Count -eq 0) {
    Write-Host "[ERROR] No changes found to commit!" -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Found $($changedFiles.Count) changed files" -ForegroundColor Green
Write-Host ""

# Counters for summary
$commitCount = 0
$failedCommits = @()

# Process each file individually
foreach ($file in $changedFiles) {
    Write-Host "--------------------------------------------------" -ForegroundColor Gray
    Write-Host "[PROCESS] File: $($file.Path)" -ForegroundColor Yellow
    Write-Host "[ACTION] $($file.Action)" -ForegroundColor Cyan

    # Determine commit parameters
    $commitType = Get-CommitType -FilePath $file.Path
    $scope = Get-Scope -FilePath $file.Path
    $description = Get-Description -FilePath $file.Path -Action $file.Action
    $commitMessage = New-ConventionalCommitMessage -Type $commitType -Scope $scope -Description $description

    Write-Host "[MESSAGE] $commitMessage" -ForegroundColor Green

    if ($DryRun) {
        Write-Host "[DRY RUN] Would commit: git commit -m `"$commitMessage`" -- $($file.Path)" -ForegroundColor Magenta
        $commitCount++
        continue
    }

    # Add only this file
    git add $file.Path

    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Failed to stage file!" -ForegroundColor Red
        $failedCommits += $file.Path
        continue
    }

    # Make the commit
    $commitResult = git commit -m $commitMessage 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[SUCCESS] Committed successfully!" -ForegroundColor Green
        $commitCount++
    }
    else {
        Write-Host "[ERROR] Commit failed: $commitResult" -ForegroundColor Red
        $failedCommits += $file.Path
    }
}

# Display summary
Write-Host ""
Write-Host "==================================================" -ForegroundColor Gray
Write-Host "[SUMMARY] COMMIT SUMMARY" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Gray
Write-Host "[SUCCESS] Successful commits: $commitCount" -ForegroundColor Green

if ($failedCommits.Count -gt 0) {
    Write-Host "[FAILED] Failed commits: $($failedCommits.Count)" -ForegroundColor Red
    Write-Host "Failed files:" -ForegroundColor Red
    foreach ($failed in $failedCommits) {
        Write-Host "  - $failed" -ForegroundColor Red
    }
}

# Show recent commits
Write-Host ""
Write-Host "[HISTORY] Recent commits:" -ForegroundColor Cyan
git log --oneline -$commitCount

# Push if requested
if ($Push -and -not $DryRun -and $commitCount -gt 0) {
    Write-Host ""
    Write-Host "[PUSH] Pushing to remote..." -ForegroundColor Yellow
    $currentBranch = git branch --show-current
    git push origin $currentBranch

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[SUCCESS] Push successful!" -ForegroundColor Green
    }
    else {
        Write-Host "[ERROR] Push failed!" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "[DONE] Script completed!" -ForegroundColor Green