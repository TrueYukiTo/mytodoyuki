@echo off
chcp 65001 >nul
title YukiWorld 7.2 - download mods
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0download-mods.ps1"
pause
