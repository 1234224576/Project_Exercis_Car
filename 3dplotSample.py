#!/usr/bin/python
# coding: UTF-8
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
import numpy as np

dataFile = open('sample.txt','r')
lines = dataFile.readlines()

x_params = []
y_params = []
z_params = []

model_x1 = []
model_x2 = []
model_x3 = []

for line in lines:
	x,y,z = np.array(line.split("\t"))
	x_params.append(int(x))
	y_params.append(int(y))
	z_params.append(float(z))

print

for x in range(0,1000,10):
	for y in range(0,5):
		model_x1.append(x)
		model_x2.append(y)
		model_x3.append((0.1492 * x) + (18.0 * y) + 5.90)

figure = plt.figure()
ax = Axes3D(figure)
ax.scatter3D(x_params,y_params,z_params)
ax.plot3D(model_x1,model_x2,model_x3)
plt.show()

# 参考http://d.hatena.ne.jp/white_wheels/20100327/p3