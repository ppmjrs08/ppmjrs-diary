param([switch]$Test,[switch]$Build,[ValidateSet('demo','mysql')][string]$Profile='demo')
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try {
  $m2Maven=(Get-Command mvn -ErrorAction SilentlyContinue).Source
  $m2LocalTools=Join-Path (Split-Path $PSScriptRoot -Parent) '10_本学期课程_2026-2027-1\CSIT883_系统分析与项目管理\SmartCampus_Workspace\tools'
  if(!$m2Maven -and $env:M2_MAVEN_HOME){$m2Maven=Join-Path $env:M2_MAVEN_HOME 'bin\mvn.cmd'}
  if(!$m2Maven){$m2Maven=Join-Path $m2LocalTools 'maven\apache-maven-3.9.16\bin\mvn.cmd'}
  $m2Args=@('-B')
  if($env:M2_MAVEN_REPO){$m2Args+="-Dmaven.repo.local=$env:M2_MAVEN_REPO"}
  elseif(Test-Path -LiteralPath (Join-Path $m2LocalTools 'maven-repository')){$m2Args+="-Dmaven.repo.local=$(Join-Path $m2LocalTools 'maven-repository')"}
  $m2Jar='target\smartcampus-m2-1.0.0.jar'
  if($Test -or $Build -or !(Test-Path -LiteralPath $m2Jar)){
    if(!(Test-Path -LiteralPath $m2Maven)){throw 'Install Maven 3.6.3+ or set M2_MAVEN_HOME, then retry.'}
    $m2Goal=if($Test){'test'}else{'package'}
    & $m2Maven @m2Args $m2Goal
    if($LASTEXITCODE -ne 0){throw 'Java build/tests failed. Stop the running M2 server before rebuilding its JAR.'}
  }
  if($Test){
    $m2Node=(Get-Command node -ErrorAction SilentlyContinue).Source
    if(!$m2Node){throw 'Install Node.js 18+ to run frontend race tests.'}
    & $m2Node --test frontend-test.mjs
    if($LASTEXITCODE -ne 0){throw 'Frontend tests failed.'}
    return
  }
  if($Build){return}
  Write-Host "Open http://127.0.0.1:8082 ($Profile profile). Press Ctrl+C to stop."
  & java -jar $m2Jar "--spring.profiles.active=$Profile"
  if($LASTEXITCODE -ne 0){throw 'M2 failed to start. Review configuration or the previous error.'}
} finally {Pop-Location}
