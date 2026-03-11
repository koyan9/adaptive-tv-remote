@echo off
setlocal
if "%SPRING_PROFILES_ACTIVE%"=="" set SPRING_PROFILES_ACTIVE=mock
call "%~dp0mvnw.cmd" -q spring-boot:run


