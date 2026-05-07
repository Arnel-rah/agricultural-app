# auto-commit-all-files.ps1
# Script pour commiter chaque fichier individuellement avec des messages conventionnels

param(
    [switch]$DryRun,
    [switch]$Push
)

# Fonction pour déterminer le type de commit basé sur le fichier
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

# Fonction pour déterminer le scope
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

# Fonction pour générer la description
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

# Fonction pour obtenir l'action du fichier
function Get-FileAction {
    param([string]$Status)

    if ($Status -match '^D') { return "DELETE" }
    if ($Status -match '^\?\?') { return "ADD" }
    return "MODIFY"
}

# Fonction pour créer un commit message conventionnel
function New-ConventionalCommitMessage {
    param(
        [string]$Type,
        [string]$Scope,
        [string]$Description
    )

    if ($Scope) {
        return "$Type($Scope): $Description"
    }
    else {
        return "$Type: $Description"
    }
}

# Collecter tous les fichiers modifiés
Write-Host "🔍 Scanning for changed files..." -ForegroundColor Cyan
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
    Write-Host "❌ No changes found to commit!" -ForegroundColor Red
    exit 1
}

Write-Host "📊 Found $($changedFiles.Count) changed files`n" -ForegroundColor Green

# Compteurs pour le résumé
$commitCount = 0
$failedCommits = @()

# Traiter chaque fichier individuellement
foreach ($file in $changedFiles) {
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
    Write-Host "📄 Processing: $($file.Path)" -ForegroundColor Yellow
    Write-Host "   Action: $($file.Action)" -ForegroundColor Cyan

    # Déterminer les paramètres du commit
    $commitType = Get-CommitType -FilePath $file.Path
    $scope = Get-Scope -FilePath $file.Path
    $description = Get-Description -FilePath $file.Path -Action $file.Action
    $commitMessage = New-ConventionalCommitMessage -Type $commitType -Scope $scope -Description $description

    Write-Host "   Message: $commitMessage" -ForegroundColor Green

    if ($DryRun) {
        Write-Host "   [DRY RUN] Would commit with: git commit -m `"$commitMessage`" -- $($file.Path)" -ForegroundColor Magenta
        $commitCount++
        continue
    }

    # Ajouter uniquement ce fichier
    git add $file.Path

    if ($LASTEXITCODE -ne 0) {
        Write-Host "   ❌ Failed to stage file!" -ForegroundColor Red
        $failedCommits += $file.Path
        continue
    }

    # Faire le commit
    $commitResult = git commit -m $commitMessage 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Host "   ✅ Committed successfully!" -ForegroundColor Green
        $commitCount++
    }
    else {
        Write-Host "   ❌ Commit failed: $commitResult" -ForegroundColor Red
        $failedCommits += $file.Path
    }
}

# Afficher le résumé
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "📊 COMMIT SUMMARY" -ForegroundColor Cyan
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
Write-Host "✅ Successful commits: $commitCount" -ForegroundColor Green

if ($failedCommits.Count -gt 0) {
    Write-Host "❌ Failed commits: $($failedCommits.Count)" -ForegroundColor Red
    Write-Host "   Failed files:" -ForegroundColor Red
    foreach ($failed in $failedCommits) {
        Write-Host "     - $failed" -ForegroundColor Red
    }
}

# Afficher l'historique des commits
Write-Host "`n📜 Recent commits:" -ForegroundColor Cyan
git log --oneline -$commitCount

# Push si demandé
if ($Push -and -not $DryRun -and $commitCount -gt 0) {
    Write-Host "`n🚀 Pushing to remote..." -ForegroundColor Yellow
    $currentBranch = git branch --show-current
    git push origin $currentBranch

    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Push successful!" -ForegroundColor Green
    }
    else {
        Write-Host "❌ Push failed!" -ForegroundColor Red
    }
}

Write-Host "`n✨ Done!" -ForegroundColor Green