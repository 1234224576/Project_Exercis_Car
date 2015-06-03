# !/usr/bin/python
# coding: UTF-8
import scipy as sp
import matplotlib.pyplot as plt
from sklearn.svm import SVR
import numpy as np

def error(f,x,y):
	return sp.sum((f(x)-y)**2)

dataFile = open('sample.txt','r')
lines = dataFile.readlines()
x_params = []
y_params = []
for line in lines:
	x,y = line.split("\t")
	x_params.append(float(x))
	y_params.append(float(y))



fp1 = sp.polyfit(y_params,x_params,5)
f1 = sp.poly1d(fp1)

print fp1
#Plot graph


plt.plot(y_params,x_params,"o")
plt.autoscale(tight=True)
fx = sp.linspace(0,0.4)
plt.plot(fx,f1(fx),linewidth=4)
plt.grid()
plt.show()
