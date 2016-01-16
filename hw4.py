#with assistance from: http://docs.python.org/2/library/

import math
import struct

# PROBLEM 1

# parse the file named fname into a dictionary of the form 
# {'width': int, 'height' : int, 'max' : int, pixels : (int * int * int) list}
def parsePPM(fname):
	f=open(fname, 'rb')
	f.readline()	#skip P6
	width=''
	height=''
	max=''	

	b=True
	while b:
		c=f.read(1)
		if c==' ':
			b=False
		else:
			width+=c
	width=int(width)
	
	b=True
	while b:
		c=f.read(1)
		if c=='\n':
			b=False
		else:
			height+=c
	height=int(height)

	b=True
	while b:
		c=f.read(1)
		if c=='\n':
			b=False
		else:
			max+=c
	max=int(max)
	
	pixels=[]
	b=True
	while b:
		c=f.read(3)
		if c=='':
			b=False
		else:
			pixels+=(struct.unpack('BBB', c),)
	f.close()
	return {'width':width, 'height':height, 'max':max, 'pixels':pixels}


# write the given ppm dictionary as a PPM image file named fname
# the function should not return anything
def unparsePPM(ppm, fname):
	f=open(fname, 'wb')
	f.write('P6\n')
	f.write(str(ppm['width'])+' '+str(ppm['height'])+'\n')
	f.write(str(ppm['max'])+'\n')
	for t in ppm['pixels']:
		for c in t:
			f.write(struct.pack('B', c))
	f.close()
	


# PROBLEM 2
def negate(ppm):
	max=ppm['max']
	width=ppm['width']
	height=ppm['height']
	pixels=[(max-r, max-g, max-b) for (r,g,b) in ppm['pixels']]
	return {'width':width, 'height':height, 'max':max, 'pixels':pixels}



# PROBLEM 3
def mirrorImage(ppm):
	max=ppm['max']
	width=ppm['width']
	height=ppm['height']
	pixels=[ppm['pixels'][n:n+width] for n in range(0, len(ppm['pixels']), width)]
	for l in pixels:
		l.reverse()
	pixels=[t for l in pixels for t in l]
	return {'width':width, 'height':height, 'max':max, 'pixels':pixels}


# PROBLEM 4

# produce a greyscale version of the given ppm dictionary.
# the resulting dictionary should have the same format, 
# except it will only have a single value for each pixel, 
# rather than an RGB triple.
def greyscale(ppm):
	max=ppm['max']
	width=ppm['width']
	height=ppm['height']
	pixels=[int(round((.299 * r) + (.587 * g) + (.114 * b)))
		for (r,g,b) in ppm['pixels']]
	return {'width':width, 'height':height, 'max':max, 'pixels':pixels}


# take a dictionary produced by the greyscale function and write it as a PGM image file named fname
# the function should not return anything
def unparsePGM(pgm, fname):
	f=open(fname, 'wb')
	f.write('P5\n')
	f.write(str(pgm['width'])+' '+str(pgm['height'])+'\n')
	f.write(str(pgm['max'])+'\n')
	for c in pgm['pixels']:
		f.write(struct.pack('B', c))
	f.close()



# PROBLEM 5

# gaussian blur code adapted from:
# http://stackoverflow.com/questions/8204645/implementing-gaussian-blur-how-to-calculate-convolution-matrix-kernel
def gaussian(x, mu, sigma):
  return math.exp( -(((x-mu)/(sigma))**2)/2.0 )

def gaussianFilter(radius, sigma):
    # compute the actual kernel elements
    hkernel = [gaussian(x, radius, sigma) for x in range(2*radius+1)]
    vkernel = [x for x in hkernel]
    kernel2d = [[xh*xv for xh in hkernel] for xv in vkernel]

    # normalize the kernel elements
    kernelsum = sum([sum(row) for row in kernel2d])
    kernel2d = [[x/kernelsum for x in row] for row in kernel2d]
    return kernel2d

# blur a given ppm dictionary, returning a new dictionary  
# the blurring uses a gaussian filter produced by the above function
def gaussianBlur(ppm, radius, sigma):
    # obtain the filter
	gfilter = gaussianFilter(radius, sigma)
	max=ppm['max']
	width=ppm['width']
	height=ppm['height']

	reds=[r for (r,g,b) in ppm['pixels']]
	reds=[reds[n:n+width] for n in range(0, len(reds), width)]
	blurR=[l for l in reds]

	greens=[g for (r,g,b) in ppm['pixels']]
	greens=[greens[n:n+width] for n in range(0, len(greens), width)]
	blurG=[l for l in greens]

	blues=[b for (r,g,b) in ppm['pixels']]
	blues=[blues[n:n+width] for n in range(0, len(blues), width)]
	blurB=[l for l in blues]

	for row in range(1,height-1):
		for col in range(1,width-1):
			blurR[row][col]=round(gfilter[0][0]*reds[row-1][col-1]+
					gfilter[0][0]*reds[row-1][col]+
					gfilter[0][0]*reds[row-1][col+1]+
					gfilter[0][0]*reds[row][col-1]+
					gfilter[0][0]*reds[row][col]+
					gfilter[0][0]*reds[row][col+1]+
					gfilter[0][0]*reds[row+1][col-1]+
					gfilter[0][0]*reds[row+1][col]+
					gfilter[0][0]*reds[row+1][col+1])
			blurG[row][col]=round(gfilter[0][0]*greens[row-1][col-1]+
					gfilter[0][0]*greens[row-1][col]+
					gfilter[0][0]*greens[row-1][col+1]+
					gfilter[0][0]*greens[row][col-1]+
					gfilter[0][0]*greens[row][col]+
					gfilter[0][0]*greens[row][col+1]+
					gfilter[0][0]*greens[row+1][col-1]+
					gfilter[0][0]*greens[row+1][col]+
					gfilter[0][0]*greens[row+1][col+1])
			blurB[row][col]=round(gfilter[0][0]*blues[row-1][col-1]+
					gfilter[0][0]*blues[row-1][col]+
					gfilter[0][0]*blues[row-1][col+1]+
					gfilter[0][0]*blues[row][col-1]+
					gfilter[0][0]*blues[row][col]+
					gfilter[0][0]*blues[row][col+1]+
					gfilter[0][0]*blues[row+1][col-1]+
					gfilter[0][0]*blues[row+1][col]+
					gfilter[0][0]*blues[row+1][col+1])
	blurR=[t for l in blurR for t in l]
	blurG=[t for l in blurG for t in l]
	blurB=[t for l in blurB for t in l]
	pixels=zip(blurR,blurG,blurB)
	return {'width':width, 'height':height, 'max':max, 'pixels':pixels}



