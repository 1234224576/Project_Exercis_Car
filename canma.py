#!/usr/bin/python
#coding: UTF-8

dataFile = open('sample.txt','r')
dataFile2 = open('sample2.txt','w')
lines = dataFile.readlines()

x = []
for line in lines:
	line.replace("\n","")
	x.append(line +",")

for s in x:
	dataFile2.write(s)
dataFile2.close()
