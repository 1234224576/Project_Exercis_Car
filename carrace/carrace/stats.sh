#!/bin/sh
if [ "$1" -eq 2 ]; then
	javac simplerace/MyController.java
	javac simplerace/MyController2.java
	java simplerace.Stats simplerace.MyController simplerace.MyController2
else
	javac simplerace/MyController.java
	java simplerace.Stats simplerace.MyController 
fi
