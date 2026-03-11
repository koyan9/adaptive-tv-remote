@echo off
setlocal
set SPRING_PROFILES_ACTIVE=real
call "%~dp0mvnw.cmd" -q spring-boot:run
