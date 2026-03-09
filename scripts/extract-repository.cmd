@echo off
setlocal
powershell.exe -ExecutionPolicy Bypass -File "%~dp0extract-repository.ps1" %*



