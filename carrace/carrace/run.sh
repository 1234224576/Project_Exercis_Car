#!/bin/sh
if [ "$1" -eq 2 ]; then
	rm simplerace/KeyboardControllerOne.class
	rm simplerace/MyController.class
	rm simplerace/MyController2.class
	javac simplerace/KeyboardControllerOne.java
	javac simplerace/MyController.java
	javac simplerace/MyController2.java
	java simplerace.Play simplerace.MyController simplerace.MyController2
else
	rm simplerace/KeyboardControllerOne.class
	rm simplerace/MyController.class
	javac simplerace/KeyboardControllerOne.java
	javac simplerace/MyController.java
	java simplerace.Play simplerace.MyController 
fi
