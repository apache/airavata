::---code borrowed from http://stackoverflow.com/a/524491 ---
@echo off
setLocal EnableDelayedExpansion
set CLASSPATH=
for %%jar in (lib\*.jar) do (
	set CLASSPATH=!CLASSPATH!%%jar;
)
::-----------------------------------------------------------
::java -classpath !CLASSPATH! cct.JMolEditor
