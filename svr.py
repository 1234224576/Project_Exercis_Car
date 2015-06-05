# !/usr/bin/python
# coding: UTF-8
import scipy as sp
import matplotlib.pyplot as plt
from sklearn.svm import SVR
import numpy as np

def error(f,x,y):
	return sp.sum((f(x)-y)**2)

dataFile = open('sample2.txt','r')
lines = dataFile.readlines()
x_params = []
y_params = []
for line in lines:
	x,y = line.split(" ")
	x_params.append(float(x))
	y_params.append(float(y))



fp1 = sp.polyfit(y_params,x_params,10)
f1 = sp.poly1d(fp1)

print fp1
#Plot graph


plt.plot(y_params,x_params,"o")
fx = sp.linspace(200,500)
plt.plot(fx,f1(fx),linewidth=4)
plt.autoscale(tight=True)

plt.grid()
plt.show()
