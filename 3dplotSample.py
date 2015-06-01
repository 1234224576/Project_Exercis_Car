#!/usr/bin/python
# coding: UTF-8
# from mpl_toolkits.mplot3d import Axes3D
# import matplotlib.pyplot as plt
# import numpy as np

# dataFile = open('sample.txt','r')
# lines = dataFile.readlines()

# x_params = []
# y_params = []
# z_params = []

# model_x1 = []
# model_x2 = []
# model_x3 = []

# for line in lines:
# 	x,y,z = np.array(line.split("\t"))
# 	x_params.append(int(x))
# 	y_params.append(int(y))
# 	z_params.append(float(z))

# print

# for x in range(0,1000,10):
# 	for y in range(0,5):
# 		model_x1.append(x)
# 		model_x2.append(y)
# 		model_x3.append((0.1492 * x) + (18.0 * y) + 5.90)

# figure = plt.figure()
# ax = Axes3D(figure)
# ax.scatter3D(x_params,y_params,z_params)
# ax.plot3D(model_x1,model_x2,model_x3)
# plt.show()

import scipy as sp
import matplotlib.pyplot as plt

def error(f,x,y):
	return sp.sum((f(x)-y)**2)

dataFile = open('sample.txt','r')
lines = dataFile.readlines()
count = 0
x_params = []
y_params = []
for line in lines:
	x,y = line.split("\t")
	x_params.append(float(x))
	a = (float(y) - 200.0)
	b = pow(3200.0,0.5)
	c = a/b
	print a
	y_params.append(c)

	count += 1

# y_params = range(0,count)



fp1 = sp.polyfit(x_params,y_params,100)
f1 = sp.poly1d(fp1)

print error(f1,x_params,y_params)



#Plot graph
# fx = sp.linspace(0,5,100)
# plt.plot(fx,f1(fx),linewidth=4)
plt.plot(y_params,x_params,linewidth=2)
plt.autoscale(tight=True)
plt.grid()
plt.show()



# 参考http://d.hatena.ne.jp/white_wheels/20100327/p3