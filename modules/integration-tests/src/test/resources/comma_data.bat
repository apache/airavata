@ECHO off & setlocal enabledelayedexpansion
SET first=1
FOR %%A IN (%*) DO (
	IF !first! EQU 1 (
		ECHO %%A
		SET first=0
	) ELSE (
		ECHO ,%%A
	)
)